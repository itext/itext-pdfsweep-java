/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
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
