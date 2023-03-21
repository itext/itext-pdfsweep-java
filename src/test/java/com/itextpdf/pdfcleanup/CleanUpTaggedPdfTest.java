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
package com.itextpdf.pdfcleanup;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;
import java.io.IOException;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class CleanUpTaggedPdfTest extends ExtendedITextTest {
    private static final String inputPath = "./src/test/resources/com/itextpdf/pdfcleanup/CleanUpTaggedPdfTest/";
    private static final String outputPath = "./target/test/com/itextpdf/pdfcleanup/CleanUpTaggedPdfTest/";
    @BeforeClass
    public static void before() {
        createOrClearDestinationFolder(outputPath);
    }

    @Test
    public void cleanTextFull() throws IOException, InterruptedException {
        String input = inputPath + "cleanText_full.pdf";
        String output = outputPath + "cleanText_full.pdf";
        String cmp = inputPath + "cmp_cleanText_full.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_text_full");
    }

    @Test
    public void cleanTextPartial() throws IOException, InterruptedException {
        String input = inputPath + "cleanText_partial.pdf";
        String output = outputPath + "cleanText_partial.pdf";
        String cmp = inputPath + "cmp_cleanText_partial.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_text_partial");
    }

    @Test
    public void cleanImageFull() throws IOException, InterruptedException {
        String input = inputPath + "cleanImage_full.pdf";
        String output = outputPath + "cleanImage_full.pdf";
        String cmp = inputPath + "cmp_cleanImage_full.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_image_full");
    }

    @Test
    public void cleanImagePartial() throws IOException, InterruptedException {
        String input = inputPath + "cleanImage_partial.pdf";
        String output = outputPath + "cleanImage_partial.pdf";
        String cmp = inputPath + "cmp_cleanImage_partial.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_image_partial");
    }

    @Test
    public void cleanPathFull() throws IOException, InterruptedException {
        String input = inputPath + "cleanPath_full.pdf";
        String output = outputPath + "cleanPath_full.pdf";
        String cmp = inputPath + "cmp_cleanPath_full.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_path_full");
    }

    @Test
    public void cleanPathPartial() throws IOException, InterruptedException {
        String input = inputPath + "cleanPath_partial.pdf";
        String output = outputPath + "cleanPath_partial.pdf";
        String cmp = inputPath + "cmp_cleanPath_partial.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_path_partial");
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
