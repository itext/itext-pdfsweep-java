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

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.CompressionConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfcleanup.autosweep.CompositeCleanupStrategy;
import com.itextpdf.pdfcleanup.autosweep.PdfAutoSweepTools;
import com.itextpdf.pdfcleanup.autosweep.RegexBasedCleanupStrategy;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.ByteArrayOutputStream;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;

@Category(IntegrationTest.class)
public class PdfAutoSweepToolsTest extends ExtendedITextTest {

    private static final String INPUT_PATH = "./src/test/resources/com/itextpdf/pdfcleanup/PdfAutoSweepTest/";
    private static final String OUTPUT_PATH = "./target/test/com/itextpdf/pdfcleanup/PdfAutoSweepTest/";

    @BeforeClass
    public static void before() {
        createOrClearDestinationFolder(OUTPUT_PATH);
    }

    @Test
    public void tentativeCleanUpTest() throws IOException, InterruptedException {
        String input = INPUT_PATH + "Lipsum.pdf";
        String output = OUTPUT_PATH + "tentativeCleanUp.pdf";
        String cmp = INPUT_PATH + "cmp_tentativeCleanUp.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("(D|d)olor").setRedactionColor(ColorConstants.GREEN));

        PdfDocument pdf = new PdfDocument(new PdfReader(input), new PdfWriter(output).setCompressionLevel(0));

        // sweep
        PdfAutoSweepTools autoSweep = new PdfAutoSweepTools(strategy);
        autoSweep.tentativeCleanUp(pdf);

        pdf.close();

        // compare
        compareByContent(cmp, output, OUTPUT_PATH, "diff_tentativeCleanUp_");
    }

    @Test
    public void highlightTest() throws IOException, InterruptedException {
        String input = INPUT_PATH + "Lipsum.pdf";
        String output = OUTPUT_PATH + "highlightTest.pdf";
        String cmp = INPUT_PATH + "cmp_highlightTest.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("(D|d)olor").setRedactionColor(ColorConstants.GREEN));

        PdfDocument pdf = new PdfDocument(new PdfReader(input), new PdfWriter(output)
                .setCompressionLevel(CompressionConstants.NO_COMPRESSION));

        // sweep
        PdfAutoSweepTools autoSweep = new PdfAutoSweepTools(strategy);
        autoSweep.highlight(pdf);

        pdf.close();

        // compare
        compareByContent(cmp, output, OUTPUT_PATH, "diff_highlightTest_");
    }

    @Test
    public void getPdfCleanUpLocationsTest() throws IOException {
        String input = INPUT_PATH + "Lipsum.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("(D|d)olor"));

        PdfDocument pdf = new PdfDocument(new PdfReader(input), new PdfWriter(new ByteArrayOutputStream()));

        // sweep
        List cleanUpLocations = (List) new PdfAutoSweepTools(strategy).getPdfCleanUpLocations(pdf.getPage(1));

        pdf.close();

        // compare
        Assert.assertEquals(2, cleanUpLocations.size());
    }

    private void compareByContent(String cmp, String output, String targetDir, String diffPrefix) throws IOException, InterruptedException {
        CompareTool cmpTool = new CompareTool();
        String errorMessage = cmpTool.compareByContent(output, cmp, targetDir, diffPrefix + "_");

        if (errorMessage != null) {
            Assert.fail(errorMessage);
        }
    }
}
