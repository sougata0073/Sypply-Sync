package com.sougata.supplysync.pdf.util

import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Link
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.sougata.supplysync.firestore.util.HelperRepository
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.util.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class PdfGeneratorHelper {

    private val helperRepo = HelperRepository()

    suspend fun <T : Model> tableMaker(
        headerNames: List<String>,
        dataList: List<T>,
        fontSize: Float,
        converter: (T) -> List<String>
    ): Table = withContext(Dispatchers.Default) {
        val table = Table(headerNames.size).apply {
            setHorizontalAlignment(HorizontalAlignment.CENTER)
            setWidth(UnitValue.createPercentValue(100f))
            setTextAlignment(TextAlignment.LEFT)
            setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
            setFontSize(fontSize)
        }

        val normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)
        val boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)

        for (headerName in headerNames) {
            val headerCell = Cell().add(Paragraph(headerName)).setPadding(10f).setFont(boldFont)
                .setTextAlignment(TextAlignment.CENTER)

            table.addHeaderCell(headerCell)
        }

        for (data in dataList) {
            val cells = converter(data)
            for (data in cells) {
                val cell = Cell().add(Paragraph(data)).setPadding(5f).setFont(normalFont)

                table.addCell(cell)
            }
        }

        return@withContext table
    }


    suspend fun getCurrentUserDetailsParagraph(): Triple<Status, Paragraph?, String> =
        withContext(Dispatchers.Default) {
            val result = helperRepo.getCurrentUserDetails()

            val status = result.first
            val user = result.second
            val message = result.third

            if (status == Status.SUCCESS) {
                user!!

                val normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)
                val boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)

                val paragraph =
                    Paragraph().add(
                        Text("Name: ${user.name.uppercase()}\n")
                            .setFont(boldFont).setFontSize(25f)
                    )
                        .add(
                            Text("Email: ")
                                .setFont(normalFont).setFontSize(18f)
                        )
                        .add(
                            Link(
                                "${user.email}\n", PdfAction.createURI("mailto:${user.email}")
                            )
                                .setFontColor(ColorConstants.BLUE).setFont(normalFont)
                                .setFontSize(18f)
                        )
                        .add(
                            Text("Phone: ").setFont(normalFont).setFontSize(18f)
                        )
                        .add(
                            Link(
                                user.phone, PdfAction.createURI("tel:${user.phone}")
                            )
                                .setFontColor(ColorConstants.BLUE).setFont(normalFont)
                                .setFontSize(18f)
                        )


                return@withContext Triple(Status.SUCCESS, paragraph, message)
            }
            return@withContext Triple(Status.FAILED, null, message)
        }

    suspend fun <T : Model> generateAnyPdfWithTable(
        dataList: List<T>, headerNames: List<String>, fontSize: Float,
        converter: (T) -> List<String>, onComplete: (Status, ByteArray?, String) -> Unit
    ) {
        val outputStream = ByteArrayOutputStream()

        val writer = PdfWriter(outputStream)
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)

        try {
            val result = this.getCurrentUserDetailsParagraph()

            val status = result.first
            val paragraph = result.second
            val message = result.third

            if (status == Status.SUCCESS) {

                val table = this.tableMaker(headerNames, dataList, fontSize, converter)

                withContext(Dispatchers.Default) {
                    paragraph?.setMarginBottom(30f)

                    document.add(paragraph)
                    document.add(table)
                    document.close()
                }

                onComplete(
                    Status.SUCCESS,
                    outputStream.toByteArray(),
                    message
                )

            } else if (status == Status.FAILED) {
                onComplete(Status.FAILED, null, message)
            }

        } catch (e: Exception) {
            onComplete(Status.FAILED, null, e.message.toString())
        }
    }
}