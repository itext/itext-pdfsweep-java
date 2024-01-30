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
package com.itextpdf.pdfcleanup;


import com.itextpdf.io.logs.IoLogMessageConstant;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfcleanup.util.CleanUpImagesCompareTool;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Category(IntegrationTest.class)
public class PdfCleanUpToolWithInlineImagesTest extends ExtendedITextTest {

    private static final String inputPath = "./src/test/resources/com/itextpdf/pdfcleanup/PdfCleanUpToolWithInlineImagesTest/";
    private static final String outputPath = "./target/test/com/itextpdf/pdfcleanup/PdfCleanUpToolWithInlineImagesTest/";

    @BeforeClass
    public static void before() {
        createOrClearDestinationFolder(outputPath);
    }

    @Test
    @LogMessages(messages = {
            @LogMessage(messageTemplate = IoLogMessageConstant.IMAGE_SIZE_CANNOT_BE_MORE_4KB)
    })
    public void cleanUpTest28() throws IOException, InterruptedException {
        String input = inputPath + "inlineImages.pdf";
        String output = outputPath + "inlineImages_partial.pdf";
        String cmp = inputPath + "cmp_inlineImages_partial.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(62, 100, 20, 800), null));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_28");
    }

    @Test
    public void cleanUpTest29() throws IOException, InterruptedException {
        String input = inputPath + "inlineImages.pdf";
        String output = outputPath + "inlineImages_partial2.pdf";
        String cmp = inputPath + "cmp_inlineImages_partial2.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(10, 100, 70, 599), null));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_29");
    }

    @Test
    @LogMessages(messages = {
            @LogMessage(messageTemplate = IoLogMessageConstant.IMAGE_SIZE_CANNOT_BE_MORE_4KB)
    })
    public void cleanUpTest31() throws IOException, InterruptedException {
        String input = inputPath + "inlineImageCleanup.pdf";
        String output = outputPath + "inlineImageCleanup.pdf";
        String cmp = inputPath + "cmp_inlineImageCleanup.pdf";

        cleanUp(input, output, null);
        CleanUpImagesCompareTool cmpTool = new CleanUpImagesCompareTool();
        String errorMessage = cmpTool.extractAndCompareImages(output, cmp, outputPath, "1");
        String compareByContentResult = cmpTool.compareByContent(output, cmp, outputPath);
        if (compareByContentResult != null) {
            errorMessage += compareByContentResult;
        }

        if (!errorMessage.equals("")) {
            Assert.fail(errorMessage);
        }
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

    private void compareByContent(String cmp, String output, String targetDir, String diffPrefix) throws IOException, InterruptedException {
        CompareTool cmpTool = new CompareTool();
        String errorMessage = cmpTool.compareByContent(output, cmp, targetDir, diffPrefix + "_");

        if (errorMessage != null) {
            Assert.fail(errorMessage);
        }
    }
}
