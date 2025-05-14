package com.sougata.supplysync.pdf

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.sougata.supplysync.models.CustomerPayment
import com.sougata.supplysync.models.Order
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.pdf.util.Helper
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class PdfRepository {

    private val helper = Helper()

    suspend fun generateSupplierPaymentsPdf(
        supplierPaymentsList: List<SupplierPayment>,
        onComplete: (Status, ByteArray?, String) -> Unit
    ) {

        val outputStream = ByteArrayOutputStream()

        val writer = PdfWriter(outputStream)
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)

        try {
            val result = this.helper.getCurrentUserDetailsParagraph()

            val status = result.first
            val paragraph = result.second
            val message = result.third

            if (status == Status.SUCCESS) {
                val headerNames = listOf(
                    "Supplier Name", "Data & Time of Payment", "Payment Amount"
                )

                val table = this.helper.tableMaker(
                    headerNames,
                    supplierPaymentsList,
                    15f
                ) { sp ->
                    val dateString =
                        DateTime.getDateStringFromTimestamp(sp.paymentTimestamp)
                    val timeString =
                        DateTime.getTimeStringFromTimestamp(sp.paymentTimestamp)

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

    suspend fun generatePurchasePdf(
        orderedItemsList: List<OrderedItem>,
        onComplete: (Status, ByteArray?, String) -> Unit
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
                    15f
                ) { oi ->
                    val dateString =
                        DateTime.getDateStringFromTimestamp(oi.orderTimestamp)
                    val timeString =
                        DateTime.getTimeStringFromTimestamp(oi.orderTimestamp)

                    val dateTimeString = "At: $dateString\nOn: $timeString"
                    val orderDetails =
                        """
                        Quantity: ${oi.quantity}
                        Amount: ${Converters.numberToMoneyString(oi.amount)}
                        Received: ${if (oi.received) "Yes" else "No"}
                        """.trimIndent()

                    listOf(
                        oi.supplierItemName,
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

    suspend fun generateSalesPdf(
        ordersList: List<Order>,
        onComplete: (Status, ByteArray?, String) -> Unit
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
                    "Customer Name",
                    "Data & Time Sold",
                    "Selling Info"
                )
                val table = helper.tableMaker(
                    headerNames,
                    ordersList,
                    15f
                ) { ord ->
                    val dateString =
                        DateTime.getDateStringFromTimestamp(ord.deliveryTimestamp)
                    val timeString =
                        DateTime.getTimeStringFromTimestamp(ord.deliveryTimestamp)

                    val dateTimeString = "At: $dateString\nOn: $timeString"
                    val orderDetails =
                        """
                        Quantity: ${ord.quantity}
                        Amount: ${Converters.numberToMoneyString(ord.amount)}
                        """.trimIndent()

                    listOf(
                        ord.userItemName,
                        ord.customerName,
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

    suspend fun generatePaymentsReceivedPdf(
        customerPaymentsList: List<CustomerPayment>,
        onComplete: (Status, ByteArray?, String) -> Unit
    ) {
        val outputStream = ByteArrayOutputStream()

        val writer = PdfWriter(outputStream)
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)

        try {
            val result = this.helper.getCurrentUserDetailsParagraph()

            val status = result.first
            val paragraph = result.second
            val message = result.third

            if (status == Status.SUCCESS) {
                val headerNames = listOf(
                    "Customer Name", "Data & Time of Payment", "Payment Amount"
                )

                val table = this.helper.tableMaker(
                    headerNames,
                    customerPaymentsList,
                    15f
                ) { cp ->
                    val dateString =
                        DateTime.getDateStringFromTimestamp(cp.paymentTimestamp)
                    val timeString =
                        DateTime.getTimeStringFromTimestamp(cp.paymentTimestamp)

                    val dateTimeString = "At: $dateString\nOn: $timeString"

                    listOf(
                        cp.customerName,
                        dateTimeString,
                        Converters.numberToMoneyString(cp.amount)
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
