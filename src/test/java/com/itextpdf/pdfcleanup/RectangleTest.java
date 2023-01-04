/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
    Authors: iText Software.

    This program is offered under a commercial and under the AGPL license.
    For commercial licensing, contact us at https://itextpdf.com/sales.  For AGPL licensing, see below.

    AGPL licensing:
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.itextpdf.pdfcleanup;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class RectangleTest extends ExtendedITextTest {

    private static final String SOURCE_FOLDER = "./src/test/resources/com/itextpdf/pdfcleanup/RectangleTest/";
    private static final String DESTINATION_FOLDER = "./target/test/com/itextpdf/pdfcleanup/RectangleTest/";

    @BeforeClass
    public static void beforeClass()
    {
        createDestinationFolder(DESTINATION_FOLDER);
    }

    @Test
    // TODO DEVSIX-7136 Rectangles drawn with zero-width line disappear on sweeping
    public void zeroWidthLineTest() throws IOException, InterruptedException {
        String outPdf = DESTINATION_FOLDER + "zeroWidthLine.pdf";
        String cmpPdf = SOURCE_FOLDER + "cmp_zeroWidthLine.pdf";

        ByteArrayOutputStream outDocBaos = new ByteArrayOutputStream();
        try (PdfDocument pdfDocument = new PdfDocument(new PdfWriter(outDocBaos))) {
            PdfPage page = pdfDocument.addNewPage();
            PdfCanvas canvas = new PdfCanvas(page);

            canvas.setStrokeColor(DeviceRgb.BLUE)
                    .setLineWidth(0)
                    .rectangle(new Rectangle(350, 400, 100, 100))
                    .stroke();
            canvas.setStrokeColor(DeviceRgb.RED)
                    .setLineWidth(0)
                    .moveTo(100, 100)
                    .lineTo(100, 200)
                    .lineTo(200, 200)
                    .lineTo(200, 100)
                    .closePath()
                    .stroke();
        }

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(new ByteArrayInputStream(outDocBaos.toByteArray())),
                new PdfWriter(outPdf));
        PdfCleanUpTool workingTool = new PdfCleanUpTool(pdfDocument);
        Rectangle area = new Rectangle(0, 50, 150, 150);
        workingTool.addCleanupLocation(new PdfCleanUpLocation(1, area, ColorConstants.RED));
        workingTool.cleanUp();
        pdfDocument.close();

        Assert.assertNull(new CompareTool().compareByContent(outPdf, cmpPdf, DESTINATION_FOLDER, "diff_"));
    }
}
