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
