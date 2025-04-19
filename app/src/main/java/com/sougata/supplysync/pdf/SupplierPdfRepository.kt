package com.sougata.supplysync.pdf

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.FileProvider
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.sougata.supplysync.cloud.SupplierFirestoreRepository
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

class SupplierPdfRepository {

    private val firestoreRepository = SupplierFirestoreRepository()

    suspend fun generateSupplierPaymentsPdf(
        supplierPaymentsList: List<Model>,
        file: File,
        fileName: String,
        context: Context,
        onComplete: (Int, String) -> Unit
    ) {

        try {
            withContext(Dispatchers.IO) {
                val pdfFile = File(file, fileName)

                val writer = PdfWriter(pdfFile)
                val pdfDocument = PdfDocument(writer)
                val document = Document(pdfDocument)

                getCurrentUserParagraph { status, paragraph, message ->

                    if (status == Status.SUCCESS) {

                        // name, date time, amount paid
                        val table = Table(3).apply {
                            setHorizontalAlignment(HorizontalAlignment.CENTER)
                            setWidth(UnitValue.createPercentValue(100f))
                            setTextAlignment(TextAlignment.LEFT)
                            setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                            setFontSize(15f)

                            addHeaderCell("Supplier Name")
                            addHeaderCell("Data & Time of Payment")
                            addHeaderCell("Payment Amount")
                        }

                        for (sp in supplierPaymentsList) {
                            sp as SupplierPayment

                            var year = 0
                            var month = 0
                            var date = 0

                            Converters.getDateFromTimestamp(sp.paymentTimestamp).apply {
                                year = first
                                month = second
                                date = third
                            }

                            var hour = 0
                            var minute = 0

                            Converters.getTimeFromTimestamp(sp.paymentTimestamp).apply {
                                hour = first
                                minute = second
                            }

                            val dateTimeString = String.format(
                                Locale.getDefault(), "At: %02d-%02d-%04d\nOn: %02d:%02d",
                                date, month, year, hour, minute
                            )

                            table.addCell(sp.supplierName)
                            table.addCell(dateTimeString)
                            table.addCell(Converters.numberToMoneyString(sp.amount))
                        }

                        paragraph?.setMarginBottom(30f)

                        document.add(paragraph)
                        document.add(table)
                        document.close()

                        sharePdf(context, pdfFile)
                        onComplete(Status.SUCCESS, KeysAndMessages.TASK_COMPLETED_SUCCESSFULLY)

                    } else if (status == Status.FAILED) {
                        onComplete(Status.FAILED, KeysAndMessages.SOMETHING_WENT_WRONG)
                    }
                }
            }
        } catch (_: Exception) {
            onComplete(Status.FAILED, KeysAndMessages.SOMETHING_WENT_WRONG)
        }

    }

    private fun getCurrentUserParagraph(onComplete: (Int, Paragraph?, String) -> Unit) {
        this.firestoreRepository.getCurrentUserDetails { status, user, message ->
            if (status == Status.SUCCESS) {

                user!!

                val normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)
                val boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)

                val paragraph = Paragraph()
                    .add(Text("Name: ${user.name}\n").setFont(boldFont).setFontSize(20f))
                    .add(Text("Email: ${user.email}\n").setFont(normalFont).setFontSize(18f))
                    .add(Text("Phone number: ${user.phone}").setFont(normalFont).setFontSize(18f))

                onComplete(Status.SUCCESS, paragraph, message)

            } else if (status == Status.FAILED) {
                onComplete(Status.FAILED, null, message)
            }
        }
    }

    private fun sharePdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            pdfFile
        )

        val intent = Intent().apply {

            action = Intent.ACTION_SEND
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
//            setPackage("com.google.android.gm") // For gmail only
//            setPackage("com.whatsapp") // For whatsapp only
            // No set package means send to any app that can handle this intent
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.d("err", e.message.toString())
        }
    }

}
