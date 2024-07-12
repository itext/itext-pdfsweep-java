/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
    Authors: Apryse Software.

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
package com.itextpdf.pdfcleanup.images;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.pdfcleanup.PdfCleaner;
import com.itextpdf.pdfcleanup.util.CleanUpImagesCompareTool;
import com.itextpdf.test.ExtendedITextTest;

import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class CleanUpTransformedImageTest extends ExtendedITextTest {

    private static final String inputPath = "./src/test/resources/com/itextpdf/pdfcleanup/images/CleanUpTransformedImageTest/";
    private static final String outputPath = "./target/test/com/itextpdf/pdfcleanup/images/CleanUpTransformedImageTest/";


    @BeforeAll
    public static void before() {
        createOrClearDestinationFolder(outputPath);
    }

    @Test
    public void skewedGrayscaleImageBBoxCleanUpTest() throws Exception {
        // TODO DEVSIX-5089 skewed images cleanup is not supported
        String input = inputPath + "skewedGrayImage.pdf";
        String output = outputPath + "skewedGrayImage.pdf";
        String cmp = inputPath + "cmp_skewedGrayImage.pdf";

        Rectangle cleanupRegion = new Rectangle(150, 250, 100, 100);

        Assertions.assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            cleanFirstPageAndDrawCleanupRegion(cleanupRegion, input, output);
            Assertions.assertNull(findDifferencesBetweenOutputAndCmp(output, cmp));
        });
    }

    @Test
    public void skewedRgbImageBBoxCleanUpTest() throws Exception {
        // TODO DEVSIX-5089 skewed images cleanup is not supported
        String input = inputPath + "skewedRgbImage.pdf";
        String output = outputPath + "skewedRgbImage.pdf";
        String cmp = inputPath + "cmp_skewedRgbImage.pdf";

        Rectangle cleanupRegion = new Rectangle(150, 250, 100, 100);

        cleanFirstPageAndDrawCleanupRegion(cleanupRegion, input, output);
        Assertions.assertNull(findDifferencesBetweenOutputAndCmp(output, cmp));
    }

    private static void cleanFirstPageAndDrawCleanupRegion(Rectangle cleanupRegion, String input, String output) throws IOException {
        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output))) {
            PdfCleaner.cleanUp(pdfDocument, Collections.singletonList(new PdfCleanUpLocation(1, cleanupRegion)));

            drawCleanupRegionOnPage(pdfDocument, cleanupRegion);
        }
    }

    private static void drawCleanupRegionOnPage(PdfDocument pdfDocument, Rectangle cleanupRegion) {
        new PdfCanvas(pdfDocument.getFirstPage())
                .setLineDash(3, 3).setStrokeColor(ColorConstants.CYAN)
                .rectangle(cleanupRegion).stroke();
    }

    private static String findDifferencesBetweenOutputAndCmp(String output, String cmp) throws IOException, InterruptedException {
        CleanUpImagesCompareTool compareTool = new CleanUpImagesCompareTool();
        String imgCompare = compareTool.extractAndCompareImages(output, cmp, outputPath);
        String contentCompare = compareTool.compareByContent(output, cmp, outputPath);
        return imgCompare.isEmpty() ? contentCompare : String.join("\n", imgCompare, contentCompare);
    }
}
