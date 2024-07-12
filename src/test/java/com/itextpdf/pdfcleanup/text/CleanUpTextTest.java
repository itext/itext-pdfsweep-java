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
package com.itextpdf.pdfcleanup.text;

import com.itextpdf.io.logs.IoLogMessageConstant;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.pdfcleanup.PdfCleaner;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class CleanUpTextTest extends ExtendedITextTest{
    private static final String inputPath = "./src/test/resources/com/itextpdf/pdfcleanup/text/CleanUpTextTest/";
    private static final String outputPath = "./target/test/com/itextpdf/pdfcleanup/text/CleanUpTextTest/";

    @BeforeAll
    public static void before() {
        createOrClearDestinationFolder(outputPath);
    }

    @Test
    @LogMessages(messages = {
            @LogMessage(messageTemplate = IoLogMessageConstant.FONT_DICTIONARY_WITH_NO_FONT_DESCRIPTOR),
            @LogMessage(messageTemplate = IoLogMessageConstant.FONT_DICTIONARY_WITH_NO_WIDTHS)})
    public void cleanZeroWidthTextInvalidFont() throws IOException, InterruptedException {
        String input = inputPath + "cleanZeroWidthTextInvalidFont.pdf";
        String output = outputPath + "cleanZeroWidthTextInvalidFont.pdf";
        String cmp = inputPath + "cmp_cleanZeroWidthTextInvalidFont.pdf";

        cleanUp(input, output, Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(50, 50, 500, 500))));
        compareByContent(cmp, output, outputPath);
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

    private void compareByContent(String cmp, String output, String targetDir) throws IOException, InterruptedException {
        CompareTool cmpTool = new CompareTool();
        String errorMessage = cmpTool.compareByContent(output, cmp, targetDir);

        if (errorMessage != null) {
            Assertions.fail(errorMessage);
        }
    }
}
