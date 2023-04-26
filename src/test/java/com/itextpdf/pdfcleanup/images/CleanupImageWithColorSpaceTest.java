/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
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
import com.itextpdf.pdfcleanup.PdfCleaner;
import com.itextpdf.pdfcleanup.util.CleanUpImagesCompareTool;
import com.itextpdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

@Category(IntegrationTest.class)
public class CleanupImageWithColorSpaceTest extends ExtendedITextTest {

    private static final String inputPath = "./src/test/resources/com/itextpdf/pdfcleanup/images/CleanupImageWithColorSpaceTest/";
    private static final String outputPath = "./target/test/com/itextpdf/pdfcleanup/images/CleanupImageWithColorSpaceTest/";

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    @BeforeClass
    public static void before() {
        createOrClearDestinationFolder(outputPath);
    }

    @Test
    public void cleanUpTestColorSpace() throws Exception {
        String input = inputPath + "imgSeparationCs.pdf";
        String output = outputPath + "imgSeparationCs.pdf";
        String cmp = inputPath + "cmp_imgSeparationCs.pdf";

        cleanUp(input, output,
                Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(60f, 780f, 60f, 45f), ColorConstants.GREEN)));
        compareByContent(cmp, output, outputPath, "9");
    }

    @Test
    public void cleanUpTestColorSpaceJpegBaselineEncoded() throws Exception {
        // cleanup jpeg image with baseline encoded data
        String input = inputPath + "imgSeparationCsJpegBaselineEncoded.pdf";
        String output = outputPath + "imgSeparationCsJpegBaselineEncoded.pdf";
        String cmp = inputPath + "cmp_imgSeparationCsJpegBaselineEncoded.pdf";

        cleanUp(input, output,
                Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(60f, 600f, 100f, 50f), ColorConstants.GREEN)));
        compareByContent(cmp, output, outputPath, "11");
    }

    @Test
    public void cleanUpTestColorSpaceJpegBaselineEncodedWithApp14Segment() throws Exception {
        // cleanup jpeg image with baseline encoded data and app14 segment with unknown color type
        // Adobe Photoshop will always add an APP14 segment into the resulting jpeg file.
        // To make Unknown color type we have set the quality of an image to maximum during the "save as" operation
        String input = inputPath + "imgSeparationCsJpegBaselineEncodedWithApp14Segment.pdf";
        String output = outputPath + "imgSeparationCsJpegBaselineEncodedWithApp14Segment.pdf";
        String cmp = inputPath + "cmp_imgSeparationCsJpegBaselineEncodedWithApp14Segment.pdf";

        cleanUp(input, output,
                Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(60f, 600f, 100f, 50f), ColorConstants.GREEN)));
        compareByContent(cmp, output, outputPath, "10");
    }

    private void cleanUp(String input, String output, List<PdfCleanUpLocation> cleanUpLocations) throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        if (cleanUpLocations == null) {
            PdfCleaner.cleanUpRedactAnnotations(pdfDocument);
        } else {
            PdfCleaner.cleanUp(pdfDocument, cleanUpLocations);
        }

        pdfDocument.close();
    }

    private void compareByContent(String cmp, String output, String targetDir, String fuzzValue)
            throws IOException, InterruptedException {
        CleanUpImagesCompareTool cmpTool = new CleanUpImagesCompareTool();
        cmpTool.useGsImageExtracting(true);
        String errorMessage = cmpTool.extractAndCompareImages(output, cmp, targetDir, fuzzValue);
        String compareByContentResult = cmpTool.compareByContent(output, cmp, targetDir);
        if (compareByContentResult != null) {
            errorMessage += compareByContentResult;
        }

        if (!errorMessage.equals("")) {
            Assert.fail(errorMessage);
        }
    }
}
