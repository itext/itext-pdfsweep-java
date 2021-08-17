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

import com.itextpdf.io.LogMessageConstant;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Category(IntegrationTest.class)
public class BigDocumentCleanUpTest extends ExtendedITextTest {

    private static final String inputPath = "./src/test/resources/com/itextpdf/pdfcleanup/BigDocumentCleanUpTest/";
    private static final String outputPath = "./target/test/com/itextpdf/pdfcleanup/BigDocumentCleanUpTest/";

    @BeforeClass
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
    @LogMessages(messages = @LogMessage(messageTemplate = LogMessageConstant.CREATED_ROOT_TAG_HAS_MAPPING))
    public void bigTaggedDocument() throws IOException, InterruptedException {
        String input = inputPath + "chapter8_Interactive_features.pdf";
        String output = outputPath + "bigTaggedDocument.pdf";
        String cmp = inputPath + "cmp_bigTaggedDocument.pdf";

        List<Rectangle> rects = Arrays.asList(new Rectangle(60f, 80f, 460f, 65f), new Rectangle(300f, 370f, 215f, 270f));
        cleanUp(input, output, initLocations(rects, 131));
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
            Assert.fail(errorMessage);
        }
    }
}
