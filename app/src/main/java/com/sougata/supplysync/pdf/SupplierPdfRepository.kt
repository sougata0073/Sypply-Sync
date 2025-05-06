package com.sougata.supplysync.pdf

import android.util.Log
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.pdf.util.Helper
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.KeysAndMessages
import com.sougata.supplysync.util.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class SupplierPdfRepository {

    private val helper = Helper()

    suspend fun generateSupplierPaymentsPdf(
        supplierPaymentsList: List<SupplierPayment>,
        onComplete: (Int, ByteArray?, String) -> Unit
    ) {

        val outputStream = ByteArrayOutputStream()

        val writer = PdfWriter(outputStream)
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)

        try {
            val result = helper.getCurrentUserDetailsParagraph()

            val status = result.first
            val paragraph = result.second
            val message = result.third

            if (status == Status.SUCCESS) {
                val headerNames = listOf(
                    "Supplier Name", "Data & Time of Payment", "Payment Amount"
                )

                val table = helper.tableMaker(
                    headerNames,
                    supplierPaymentsList,
                    15f
                ) { sp ->
                    val dateString =
                        Converters.getDateStringFromTimestamp(sp.paymentTimestamp)
                    val timeString =
                        Converters.getTimeStringFromTimestamp(sp.paymentTimestamp)

                    val dateTimeString = "At: $dateString\nOn: $timeString"

                    listOf(
                        sp.supplierName,
                        dateTimeString,
                        Converters.numberToMoneyString(sp.amount)
                    )
                }

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

    suspend fun generateOrderedItemsPdf(
        orderedItemsList: List<OrderedItem>,
        onComplete: (Int, ByteArray?, String) -> Unit
    ) {

        val outputStream = ByteArrayOutputStream()

        val writer = PdfWriter(outputStream)
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)

        try {
            val result = helper.getCurrentUserDetailsParagraph()

            val status = result.first
            val paragraph = result.second
            val message = result.third

            if (status == Status.SUCCESS) {

                val headerNames = listOf(
                    "Item Name",
                    "Supplier Name",
                    "Data & Time of Order",
                    "Order Details"
                )

                val table = helper.tableMaker(
                    headerNames,
                    orderedItemsList,
                    12f
                ) { oi ->
                    val dateString =
                        Converters.getDateStringFromTimestamp(oi.orderTimestamp)
                    val timeString =
                        Converters.getTimeStringFromTimestamp(oi.orderTimestamp)

                    val dateTimeString = "At: $dateString\nOn: $timeString"
                    val orderDetails =
                        """
                        Quantity: ${oi.quantity}
                        Amount: ${Converters.numberToMoneyString(oi.amount)}
                        Received: ${if (oi.isReceived) "Yes" else "No"}
                        """.trimIndent()

                    listOf(
                        oi.itemName,
                        oi.supplierName,
                        dateTimeString,
                        orderDetails
                    )
                }

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
