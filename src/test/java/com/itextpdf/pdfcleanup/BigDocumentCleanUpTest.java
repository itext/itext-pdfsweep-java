/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
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
import com.itextpdf.pdfcleanup.util.CleanUpImagesCompareTool;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Tag("IntegrationTest")
public class BigDocumentCleanUpTest extends ExtendedITextTest {

    private static final String inputPath = "./src/test/resources/com/itextpdf/pdfcleanup/BigDocumentCleanUpTest/";
    private static final String outputPath = "./target/test/com/itextpdf/pdfcleanup/BigDocumentCleanUpTest/";

    @BeforeAll
    public static void before() {
        createOrClearDestinationFolder(outputPath);
    }

    @Test
    public void bigUntaggedDocument() throws IOException, InterruptedException {
        String input = inputPath + "iphone_user_guide_untagged.pdf";
        String output = outputPath + "bigUntaggedDocument.pdf";
        String cmp = inputPath + "cmp_bigUntaggedDocument.pdf";

        List<Rectangle> rects = Arrays.asList(new Rectangle(60f, 80f, 460f, 65f), new Rectangle(300f, 370f, 215f, 260f));
        cleanUp(input, output, initLocations(rects, 130));
        compareByContent(cmp, output, outputPath, "4");
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = IoLogMessageConstant.CREATED_ROOT_TAG_HAS_MAPPING))
    public void bigTaggedDocument() throws IOException, InterruptedException {
        String input = inputPath + "chapter8_Interactive_features.pdf";
        String output = outputPath + "bigTaggedDocument.pdf";
        String cmp = inputPath + "cmp_bigTaggedDocument.pdf";

        List<Rectangle> rects = Arrays.asList(new Rectangle(60f, 80f, 460f, 65f), new Rectangle(300f, 370f, 215f, 270f));
        cleanUp(input, output, initLocations(rects, 131));
        compareByContent(cmp, output, outputPath, "4");
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = IoLogMessageConstant.CREATED_ROOT_TAG_HAS_MAPPING))
    public void bigTaggedDocumentDynamicOffsetMultiplier() throws IOException, InterruptedException {
        String input = inputPath + "chapter8_Interactive_features.pdf";
        String output = outputPath + "bigTaggedDocumentDynamicOffsetMultiplier.pdf";
        String cmp = inputPath + "cmp_bigTaggedDocument.pdf";

        List<Rectangle> rects = Arrays.asList(new Rectangle(60f, 80f, 460f, 65f), new Rectangle(300f, 370f, 215f, 270f));
        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output))) {
            PdfCleaner.cleanUp(pdfDocument, initLocations(rects, 131), new CleanUpProperties().setOffsetProperties(
                    new PathOffsetApproximationProperties().calculateOffsetMultiplierDynamically(true)
            ));
        }
        compareByContent(cmp, output, outputPath, "4");
    }

    @Test
    public void textPositioning() throws IOException, InterruptedException {
        String input = inputPath + "textPositioning.pdf";
        String output = outputPath + "textPositioning.pdf";
        String cmp = inputPath + "cmp_textPositioning.pdf";

        List<Rectangle> rects = Arrays.asList(new Rectangle(0f, 0f, 1f, 1f)); // just to enable cleanup processing of the pages
        cleanUp(input, output, initLocations(rects, 163));
        compareByContent(cmp, output, outputPath, "4");
    }



    private void cleanUp(String input, String output, List<PdfCleanUpLocation> cleanUpLocations) throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        PdfCleaner.cleanUp(pdfDocument, cleanUpLocations);

        pdfDocument.close();
    }

    private List<PdfCleanUpLocation> initLocations(List<Rectangle> rects, int pagesNum) {
        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<>();
        for (int i = 0; i < pagesNum; ++i) {
            for (int j = 0; j < rects.size(); ++j) {
                cleanUpLocations.add(new PdfCleanUpLocation(i + 1, rects.get(j)));
            }
        }

        return cleanUpLocations;
    }

    private void compareByContent(String cmp, String output, String targetDir, String fuzzValue)
            throws IOException, InterruptedException {
        CleanUpImagesCompareTool cmpTool = new CleanUpImagesCompareTool();
        String errorMessage = cmpTool.extractAndCompareImages(output, cmp, targetDir, fuzzValue);
        String compareByContentResult = cmpTool.compareByContent(output, cmp, targetDir);
        if (compareByContentResult != null) {
            errorMessage += compareByContentResult;
        }

        if (!errorMessage.equals("")) {
            Assertions.fail(errorMessage);
        }
    }
}
