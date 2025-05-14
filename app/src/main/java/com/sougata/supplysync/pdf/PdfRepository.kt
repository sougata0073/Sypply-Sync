package com.sougata.supplysync.pdf

import com.sougata.supplysync.models.CustomerPayment
import com.sougata.supplysync.models.Order
import com.sougata.supplysync.models.OrderedItem
import com.sougata.supplysync.models.SupplierPayment
import com.sougata.supplysync.pdf.util.PdfGeneratorHelper
import com.sougata.supplysync.util.Converters
import com.sougata.supplysync.util.DateTime
import com.sougata.supplysync.util.Status

class PdfRepository {

    private val pdfGeneratorHelper = PdfGeneratorHelper()

    suspend fun generateSupplierPaymentsPdf(
        supplierPaymentsList: List<SupplierPayment>,
        onComplete: (Status, ByteArray?, String) -> Unit
    ) {

        val headerNames = listOf(
            "Supplier Name", "Data & Time of Payment", "Payment Amount"
        )
        val converter: (SupplierPayment) -> List<String> = { sp ->
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
        this.pdfGeneratorHelper.generateAnyPdfWithTable(
            dataList = supplierPaymentsList,
            headerNames = headerNames,
            fontSize = 15f,
            converter = converter,
            onComplete = onComplete
        )
    }

    suspend fun generatePurchasePdf(
        orderedItemsList: List<OrderedItem>,
        onComplete: (Status, ByteArray?, String) -> Unit
    ) {

        val headerNames = listOf(
            "Item Name",
            "Supplier Name",
            "Data & Time of Order",
            "Order Details"
        )
        val converter: (OrderedItem) -> List<String> = { oi ->
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
        this.pdfGeneratorHelper.generateAnyPdfWithTable(
            dataList = orderedItemsList,
            headerNames = headerNames,
            fontSize = 15f,
            converter = converter,
            onComplete = onComplete
        )
    }

    suspend fun generateSalesPdf(
        ordersList: List<Order>,
        onComplete: (Status, ByteArray?, String) -> Unit
    ) {

        val headerNames = listOf(
            "Item Name",
            "Customer Name",
            "Data & Time Sold",
            "Selling Info"
        )
        val converter: (Order) -> List<String> = { ord ->
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

        this.pdfGeneratorHelper.generateAnyPdfWithTable(
            dataList = ordersList,
            headerNames = headerNames,
            fontSize = 15f,
            converter = converter,
            onComplete = onComplete
        )
    }

    suspend fun generatePaymentsReceivedPdf(
        customerPaymentsList: List<CustomerPayment>,
        onComplete: (Status, ByteArray?, String) -> Unit
    ) {
        val headerNames = listOf(
            "Customer Name", "Data & Time of Payment", "Payment Amount"
        )
        val converter: (CustomerPayment) -> List<String> = { cp ->
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

        this.pdfGeneratorHelper.generateAnyPdfWithTable(
            dataList = customerPaymentsList,
            headerNames = headerNames,
            fontSize = 15f,
            converter = converter,
            onComplete = onComplete
        )
    }

}
