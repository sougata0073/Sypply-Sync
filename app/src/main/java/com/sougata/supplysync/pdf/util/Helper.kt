package com.sougata.supplysync.pdf.util

import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.sougata.supplysync.firestore.SupplierRepository
import com.sougata.supplysync.models.Model
import com.sougata.supplysync.util.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Helper {

    private val firestoreRepository = SupplierRepository()

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


    suspend fun getCurrentUserDetailsParagraph(): Triple<Int, Paragraph?, String> =
        withContext(Dispatchers.Default) {
            val result = firestoreRepository.getCurrentUserDetails()

            val status = result.first
            val user = result.second
            val message = result.third

            if (status == Status.SUCCESS) {
                user!!

                val normalFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)
                val boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)

                val paragraph =
                    Paragraph().add(
                        Text("Name: ${user.name}\n")
                            .setFont(boldFont).setFontSize(25f)
                    )
                        .add(
                            Text("Email: ${user.email}\n")
                                .setFont(normalFont).setFontSize(18f)
                        )
                        .add(
                            Text("Phone number: ${user.phone}")
                                .setFont(normalFont).setFontSize(18f)
                        )

                return@withContext Triple(Status.SUCCESS, paragraph, message)
            }
            return@withContext Triple(Status.FAILED, null, message)
        }
}