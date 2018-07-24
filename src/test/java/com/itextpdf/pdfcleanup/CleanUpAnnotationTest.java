/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2018 iText Group NV
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
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import com.itextpdf.pdfcleanup.PdfCleanupProductInfo;
import com.itextpdf.kernel.Version;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Category(IntegrationTest.class)
public class CleanUpAnnotationTest extends ExtendedITextTest {
    private static final String inputPath = "./src/test/resources/com/itextpdf/pdfcleanup/CleanUpAnnotationTest/";
    private static final String outputPath = "./target/test/com/itextpdf/pdfcleanup/CleanUpAnnotationTest/";

    @BeforeClass
    public static void before() {
        createOrClearDestinationFolder(outputPath);
    }

    @Test
    public void cleanFull01() throws IOException, InterruptedException {
        String input = inputPath + "cleanAnnotation.pdf";
        String output = outputPath + "cleanAnnotation_full01.pdf";
        String cmp = inputPath + "cmp_cleanAnnotation_full01.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
        cleanUpLocations.add(new PdfCleanUpLocation(1, PageSize.A4, ColorConstants.WHITE));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_Annotation_full");
    }

    @Test
    public void cleanLinkAnnotation01() throws IOException, InterruptedException {
        String input = inputPath + "cleanAnnotation.pdf";
        String output = outputPath + "cleanAnnotation_Link01.pdf";
        String cmp = inputPath + "cmp_cleanAnnotation_Link01.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();

        PdfCleanUpLocation linkLoc = new PdfCleanUpLocation(1,new Rectangle(235,740,30,16), ColorConstants.BLUE);
        cleanUpLocations.add(linkLoc);

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_Annotation_link01");

    }

    @Test
    public void cleanTextAnnotation01() throws IOException, InterruptedException {
        String input = inputPath + "cleanAnnotation.pdf";
        String output = outputPath + "cleanAnnotation_Text01.pdf";
        String cmp = inputPath + "cmp_cleanAnnotation_Text01.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();

        PdfCleanUpLocation textLoc = new PdfCleanUpLocation(1,new Rectangle(150,650,0,0), ColorConstants.RED);
        cleanUpLocations.add(textLoc);

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_Annotation_text01");
    }

    @Test
    public void cleanLineAnnotation01() throws IOException, InterruptedException {
        String input = inputPath + "cleanAnnotation.pdf";
        String output = outputPath + "cleanAnnotation_Line01.pdf";
        String cmp = inputPath + "cmp_cleanAnnotation_Line01.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();

        PdfCleanUpLocation lineLoc = new PdfCleanUpLocation(1,new Rectangle(20,20,555,0), ColorConstants.GREEN);
        cleanUpLocations.add(lineLoc);

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_Annotation_line01");

    }

    @Test
    public void cleanHighlightAnnotation01() throws IOException, InterruptedException {
        String input = inputPath + "cleanAnnotation.pdf";
        String output = outputPath + "cleanAnnotation_highlight01.pdf";
        String cmp = inputPath + "cmp_cleanAnnotation_highlight01.pdf";
        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();

        PdfCleanUpLocation highLightLoc = new PdfCleanUpLocation(1,new Rectangle(105,500,70,10), ColorConstants.BLACK);
        cleanUpLocations.add(highLightLoc);

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_text_highlight01");
    }

    @Test
    public void cleanFormAnnotations01() throws IOException,InterruptedException{
        String input = inputPath + "formAnnotation.pdf";
        String output = outputPath + "formAnnotation01.pdf";
        String cmp = inputPath + "cmp_formAnnotation01.pdf";
        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();

        PdfCleanUpLocation highLightLoc = new PdfCleanUpLocation(1,new Rectangle(20,600,500,170), ColorConstants.YELLOW);
        cleanUpLocations.add(highLightLoc);

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_form01");
    }
    @Test
    public void cleanFormAnnotations02() throws IOException,InterruptedException{
        String input = inputPath + "formAnnotation.pdf";
        String output = outputPath + "formAnnotation02.pdf";
        String cmp = inputPath + "cmp_formAnnotation02.pdf";
        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();

        PdfCleanUpLocation highLightLoc = new PdfCleanUpLocation(1,new Rectangle(20,600,300,100), ColorConstants.YELLOW);
        cleanUpLocations.add(highLightLoc);

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_form01");
    }

    private void cleanUp(String input, String output, List<PdfCleanUpLocation> cleanUpLocations) throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        PdfCleanUpTool cleaner = (cleanUpLocations == null)
                ? new PdfCleanUpTool(pdfDocument, true)
                : new PdfCleanUpTool(pdfDocument, cleanUpLocations);
        cleaner.cleanUp();

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
