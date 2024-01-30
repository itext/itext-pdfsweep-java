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
package com.itextpdf.pdfcleanup.transparency;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfcleanup.PdfCleaner;
import com.itextpdf.pdfcleanup.util.CleanUpImagesCompareTool;
import com.itextpdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class MaskedImagesTest extends ExtendedITextTest {

    private static final String inputPath = "./src/test/resources/com/itextpdf/pdfcleanup/transparency/MaskedImagesTest/";
    private static final String outputPath = "./target/test/com/itextpdf/pdfcleanup/transparency/MaskedImagesTest/";

    @BeforeClass
    public static void before() {
        createOrClearDestinationFolder(outputPath);
    }

    @Test
    public void imageTransparencyImageMask() throws IOException, InterruptedException {
        runTest("imageIsMask", "0");
    }

    @Test
    public void imageTransparencyMask() throws IOException, InterruptedException {
        runTest("imageMask", "1");
    }

    @Test
    public void imageTransparencySMask() throws IOException, InterruptedException {
        runTest("imageSMask", "1");
    }

    @Test
    public void imageTransparencySMaskAIS() throws IOException, InterruptedException {
        runTest("imageSMaskAIS", "1");
    }

    @Test
    public void imageTransparencyColorKeyMaskArray() throws IOException, InterruptedException {
        runTest("imageColorKeyMaskArray", "1");
    }

    @Test
    public void imageTransparencyTextOnTransparentField() throws IOException, InterruptedException {
        String fileName = "textOnTransparentField";
        String input = inputPath + fileName + ".pdf";
        String output = outputPath + fileName + "_cleaned.pdf";
        String cmp = inputPath + "cmp_" + fileName + ".pdf";
        List<PdfCleanUpLocation> cleanUpLocations = Collections.singletonList(
                new PdfCleanUpLocation(1, new Rectangle(280, 360, 200, 75))
        );

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        PdfCleaner.cleanUp(pdfDocument, cleanUpLocations);

        new PdfCanvas(pdfDocument.getFirstPage().newContentStreamBefore(), pdfDocument.getFirstPage().getResources(), pdfDocument)
                .setColor(ColorConstants.LIGHT_GRAY, true)
                .rectangle(0, 0, 1000, 1000)
                .fill()
                .setColor(ColorConstants.BLACK, true);

        pdfDocument.close();

        Assert.assertNull(new CompareTool().compareByContent(output, cmp, outputPath));
    }

    private static void runTest(String fileName, String fuzzValue) throws IOException, InterruptedException {
        String input = inputPath + fileName + ".pdf";
        String output = outputPath + fileName + "_cleaned.pdf";
        String cmp = inputPath + "cmp_" + fileName + ".pdf";
        List<PdfCleanUpLocation> cleanUpLocations = Collections.singletonList(
                new PdfCleanUpLocation(1, new Rectangle(308, 520, 200, 75))
        );

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        PdfCleaner.cleanUp(pdfDocument, cleanUpLocations);

        pdfDocument.close();
        CleanUpImagesCompareTool cmpTool = new CleanUpImagesCompareTool();
        String errorMessage = cmpTool.extractAndCompareImages(output,
                cmp, outputPath, fuzzValue);
        String compareByContentResult = cmpTool.compareByContent(output, cmp, outputPath);
        if (compareByContentResult != null) {
            errorMessage += compareByContentResult;
        }

        if (!errorMessage.equals("")) {
            Assert.fail(errorMessage);
        }
    }
}
