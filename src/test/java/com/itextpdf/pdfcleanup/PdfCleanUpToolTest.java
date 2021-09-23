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

import com.itextpdf.io.logs.IoLogMessageConstant;
import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;
import com.itextpdf.kernel.pdf.annot.PdfRedactAnnotation;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfcleanup.autosweep.CompositeCleanupStrategy;
import com.itextpdf.pdfcleanup.autosweep.RegexBasedCleanupStrategy;
import com.itextpdf.pdfcleanup.exceptions.CleanupExceptionMessageConstant;
import com.itextpdf.pdfcleanup.logs.CleanUpLogMessageConstant;
import com.itextpdf.pdfcleanup.util.CleanUpImagesCompareTool;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PdfCleanUpToolTest extends ExtendedITextTest {

    private static final String INPUT_PATH = "./src/test/resources/com/itextpdf/pdfcleanup/PdfCleanUpToolTest/";
    private static final String OUTPUT_PATH = "./target/test/com/itextpdf/pdfcleanup/PdfCleanUpToolTest/";

    @BeforeClass
    public static void before() {
        createOrClearDestinationFolder(OUTPUT_PATH);
    }

    @Test
    public void cleanUpTest01() throws IOException, InterruptedException {
        String input = INPUT_PATH + "page229.pdf";
        String output = OUTPUT_PATH + "page229_01.pdf";
        String cmp = INPUT_PATH + "cmp_page229_01.pdf";
        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(240.0f, 602.3f, 275.7f - 240.0f, 614.8f - 602.3f), ColorConstants.GRAY),
                new PdfCleanUpLocation(1, new Rectangle(171.3f, 550.3f, 208.4f - 171.3f, 562.8f - 550.3f), ColorConstants.GRAY),
                new PdfCleanUpLocation(1, new Rectangle(270.7f, 459.2f, 313.1f - 270.7f, 471.7f - 459.2f), ColorConstants.GRAY),
                new PdfCleanUpLocation(1, new Rectangle(249.9f, 329.3f, 279.6f - 249.9f, 341.8f - 329.3f), ColorConstants.GRAY),
                new PdfCleanUpLocation(1, new Rectangle(216.2f, 303.3f, 273.0f - 216.2f, 315.8f - 303.3f), ColorConstants.GRAY));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_01");
    }

    @Test
    public void cleanUpTest02() throws IOException, InterruptedException {
        String input = INPUT_PATH + "page229-modified-Tc-Tw.pdf";
        String output = OUTPUT_PATH + "page229-modified-Tc-Tw.pdf";
        String cmp = INPUT_PATH + "cmp_page229-modified-Tc-Tw.pdf";
        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(240.0f, 602.3f, 275.7f - 240.0f, 614.8f - 602.3f), ColorConstants.GRAY),
                new PdfCleanUpLocation(1, new Rectangle(171.3f, 550.3f, 208.4f - 171.3f, 562.8f - 550.3f), ColorConstants.GRAY),
                new PdfCleanUpLocation(1, new Rectangle(270.7f, 459.2f, 313.1f - 270.7f, 471.7f - 459.2f), ColorConstants.GRAY),
                new PdfCleanUpLocation(1, new Rectangle(249.9f, 329.3f, 279.6f - 249.9f, 341.8f - 329.3f), ColorConstants.GRAY),
                new PdfCleanUpLocation(1, new Rectangle(216.2f, 303.3f, 273.0f - 216.2f, 315.8f - 303.3f), ColorConstants.GRAY));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_02");
    }

    @Test
    public void cleanUpTest03() throws IOException, InterruptedException {
        String input = INPUT_PATH + "page166_03.pdf";
        String output = OUTPUT_PATH + "page166_03.pdf";
        String cmp = INPUT_PATH + "cmp_page166_03.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_03");
    }

    @Test
    // TODO: update cmp file after DEVSIX-3185 fixed
    public void cleanUpTestSvg() throws IOException, InterruptedException {
        String input = INPUT_PATH + "line_chart.pdf";
        String output = OUTPUT_PATH + "line_chart.pdf";
        String cmp = INPUT_PATH + "cmp_line_chart.pdf";

        cleanUp(input, output, Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(60f, 780f, 60f, 45f), ColorConstants.GRAY)));
        compareByContent(cmp, output, OUTPUT_PATH, "diff_Svg");
    }

    @Test
    public void cleanUpTest04() throws IOException, InterruptedException {
        String input = INPUT_PATH + "hello_05.pdf";
        String output = OUTPUT_PATH + "hello_05.pdf";
        String cmp = INPUT_PATH + "cmp_hello_05.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_04");
    }

    @Test
    public void cleanUpTest05() throws IOException, InterruptedException {
        String input = INPUT_PATH + "BigImage-jpg.pdf";
        String output = OUTPUT_PATH + "BigImage-jpg.pdf";
        String cmp = INPUT_PATH + "cmp_BigImage-jpg.pdf";

        cleanUp(input, output, null);
        CleanUpImagesCompareTool cmpTool = new CleanUpImagesCompareTool();
        String errorMessage = cmpTool.extractAndCompareImages(output, cmp, OUTPUT_PATH, "1");
        String compareByContentResult = cmpTool.compareByContent(output, cmp, OUTPUT_PATH);
        if (compareByContentResult != null) {
            errorMessage += compareByContentResult;
        }

        if (!errorMessage.equals("")) {
            Assert.fail(errorMessage);
        }
    }

    @Test
    public void cleanUpTest06() throws IOException, InterruptedException {
        String input = INPUT_PATH + "BigImage-png.pdf";
        String output = OUTPUT_PATH + "BigImage-png.pdf";
        String cmp = INPUT_PATH + "cmp_BigImage-png.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_06");
    }

    @Test
    public void cleanUpTest07() throws IOException, InterruptedException {
        String input = INPUT_PATH + "BigImage-tif.pdf";
        String output = OUTPUT_PATH + "BigImage-tif.pdf";
        String cmp = INPUT_PATH + "cmp_BigImage-tif.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_07");
    }

    @Test
    public void cleanUpTest08() throws IOException, InterruptedException {
        String input = INPUT_PATH + "BigImage-tif-lzw.pdf";
        String output = OUTPUT_PATH + "BigImage-tif-lzw.pdf";
        String cmp = INPUT_PATH + "cmp_BigImage-tif-lzw.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_08");
    }

    @Test
    public void cleanUpTest09() throws IOException, InterruptedException {
        String input = INPUT_PATH + "simpleImmediate.pdf";
        String output = OUTPUT_PATH + "simpleImmediate.pdf";
        String cmp = INPUT_PATH + "cmp_simpleImmediate.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f - 97f, 445f - 405f), ColorConstants.GRAY));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_09");
    }

    @Test
    public void cleanUpTest10() throws IOException, InterruptedException {
        String input = INPUT_PATH + "simpleImmediate-tm.pdf";
        String output = OUTPUT_PATH + "simpleImmediate-tm.pdf";
        String cmp = INPUT_PATH + "cmp_simpleImmediate-tm.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f - 97f, 445f - 405f), ColorConstants.GRAY));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_10");
    }

    @Test
    public void cleanUpTest11() throws IOException, InterruptedException {
        String input = INPUT_PATH + "multiUseIndirect.pdf";
        String output = OUTPUT_PATH + "multiUseIndirect.pdf";
        String cmp = INPUT_PATH + "cmp_multiUseIndirect.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(97f, 605f, 480f - 97f, 645f - 605f), ColorConstants.GRAY));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_11");
    }

    @Test
    public void cleanUpTest12() throws IOException, InterruptedException {
        String input = INPUT_PATH + "multiUseImage.pdf";
        String output = OUTPUT_PATH + "multiUseImage.pdf";
        String cmp = INPUT_PATH + "cmp_multiUseImage.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f - 97f, 445f - 405f), ColorConstants.GRAY));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_12");
    }

    @Test
    public void cleanUpTest13() throws IOException, InterruptedException {
        String input = INPUT_PATH + "maskImage.pdf";
        String output = OUTPUT_PATH + "maskImage.pdf";
        String cmp = INPUT_PATH + "cmp_maskImage.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f - 97f, 445f - 405f), ColorConstants.GRAY));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_13");
    }

    @Test
    public void cleanUpTest14() throws IOException, InterruptedException {
        String input = INPUT_PATH + "rotatedImg.pdf";
        String output = OUTPUT_PATH + "rotatedImg.pdf";
        String cmp = INPUT_PATH + "cmp_rotatedImg.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f - 97f, 445f - 405f), ColorConstants.GRAY));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_14");
    }

    @Test
    public void cleanUpTest15() throws IOException, InterruptedException {
        String input = INPUT_PATH + "lineArtsCompletely.pdf";
        String output = OUTPUT_PATH + "lineArtsCompletely.pdf";
        String cmp = INPUT_PATH + "cmp_lineArtsCompletely.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_15");
    }

    @Test
    public void cleanUpTest16() throws IOException, InterruptedException {
        String input = INPUT_PATH + "lineArtsPartially.pdf";
        String output = OUTPUT_PATH + "lineArtsPartially.pdf";
        String cmp = INPUT_PATH + "cmp_lineArtsPartially.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_16");
    }

    @Test
    public void cleanUpTest17() throws IOException, InterruptedException {
        String input = INPUT_PATH + "dashedStyledClosedBezier.pdf";
        String output = OUTPUT_PATH + "dashedStyledClosedBezier.pdf";
        String cmp = INPUT_PATH + "cmp_dashedStyledClosedBezier.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_17");
    }

    @Test
    public void cleanUpTest18() throws IOException, InterruptedException {
        String input = INPUT_PATH + "styledLineArts.pdf";
        String output = OUTPUT_PATH + "styledLineArts.pdf";
        String cmp = INPUT_PATH + "cmp_styledLineArts.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_18");
    }

    @Test
    public void cleanUpTest19() throws IOException, InterruptedException {
        String input = INPUT_PATH + "dashedBezier.pdf";
        String output = OUTPUT_PATH + "dashedBezier.pdf";
        String cmp = INPUT_PATH + "cmp_dashedBezier.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_19");
    }

    @Test
    public void cleanUpTest20() throws IOException, InterruptedException {
        String input = INPUT_PATH + "closedBezier.pdf";
        String output = OUTPUT_PATH + "closedBezier.pdf";
        String cmp = INPUT_PATH + "cmp_closedBezier.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_20");
    }

    @Test
    public void cleanUpTest21() throws IOException, InterruptedException {
        String input = INPUT_PATH + "clippingNWRule.pdf";
        String output = OUTPUT_PATH + "clippingNWRule.pdf";
        String cmp = INPUT_PATH + "cmp_clippingNWRule.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_21");
    }

    @Test
    public void cleanUpTest22() throws IOException, InterruptedException {
        String input = INPUT_PATH + "dashedClosedRotatedTriangles.pdf";
        String output = OUTPUT_PATH + "dashedClosedRotatedTriangles.pdf";
        String cmp = INPUT_PATH + "cmp_dashedClosedRotatedTriangles.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_22");
    }

    @Test
    public void cleanUpTest23() throws IOException, InterruptedException {
        String input = INPUT_PATH + "miterTest.pdf";
        String output = OUTPUT_PATH + "miterTest.pdf";
        String cmp = INPUT_PATH + "cmp_miterTest.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_23");
    }

    @Test
    public void cleanUpTest24() throws IOException, InterruptedException {
        String input = INPUT_PATH + "degenerateCases.pdf";
        String output = OUTPUT_PATH + "degenerateCases.pdf";
        String cmp = INPUT_PATH + "cmp_degenerateCases.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_24");
    }

    @Test
    public void cleanUpTest25() throws IOException, InterruptedException {
        String input = INPUT_PATH + "absentICentry.pdf";
        String output = OUTPUT_PATH + "absentICentry.pdf";
        String cmp = INPUT_PATH + "cmp_absentICentry.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_25");
    }

    @Test
    public void cleanUpTest26() throws IOException, InterruptedException {
        String input = INPUT_PATH + "lotOfDashes.pdf";
        String output = OUTPUT_PATH + "lotOfDashes.pdf";
        String cmp = INPUT_PATH + "cmp_lotOfDashes.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_26");
    }

    @Test
    public void cleanUpTest27() throws IOException, InterruptedException {
        String input = INPUT_PATH + "clipPathReduction.pdf";
        String output = OUTPUT_PATH + "clipPathReduction.pdf";
        String cmp = INPUT_PATH + "cmp_clipPathReduction.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(212, 394, 186, 170), null));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_27");
    }

    @Test
    public void cleanUpTest30() throws IOException, InterruptedException {
        String input = INPUT_PATH + "inlineImages.pdf";
        String output = OUTPUT_PATH + "inlineImages_full.pdf";
        String cmp = INPUT_PATH + "cmp_inlineImages_full.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(10, 100, 400, 600), null));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_30");
    }

    @Test
    public void cleanUpTest32() throws IOException, InterruptedException {
        String input = INPUT_PATH + "page229.pdf";
        String output = OUTPUT_PATH + "wholePageCleanUp.pdf";
        String cmp = INPUT_PATH + "cmp_wholePageCleanUp.pdf";

        cleanUp(input, output, Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(1, 1, PageSize.A4.getWidth() - 1, PageSize.A4.getHeight() - 1))));
        compareByContent(cmp, output, OUTPUT_PATH, "diff_32");
    }

    @Test
    public void cleanUpTest33() throws IOException, InterruptedException {
        String input = INPUT_PATH + "viewer_prefs_dict_table.pdf";
        String output = OUTPUT_PATH + "complexTextPositioning.pdf";
        String cmp = INPUT_PATH + "cmp_complexTextPositioning.pdf";

        cleanUp(input, output, Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(300f, 370f, 215f, 270f))));
        compareByContent(cmp, output, OUTPUT_PATH, "diff_33");
    }

    @Test
    public void cleanUpTest34() throws IOException, InterruptedException {
        String input = INPUT_PATH + "new_york_times.pdf";
        String output = OUTPUT_PATH + "textAndImages.pdf";
        String cmp = INPUT_PATH + "cmp_textAndImages.pdf";

        cleanUp(input, output, Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(150f, 235f, 230f, 445f))));
        CleanUpImagesCompareTool cmpTool = new CleanUpImagesCompareTool();
        String errorMessage = cmpTool.extractAndCompareImages(output, cmp, OUTPUT_PATH, "1.2");
        String compareByContentResult = cmpTool.compareByContent(output, cmp, OUTPUT_PATH);
        if (compareByContentResult != null) {
            errorMessage += compareByContentResult;
        }

        if (!errorMessage.equals("")) {
            Assert.fail(errorMessage);
        }
    }

    @Test
    public void cleanUpTest35() throws IOException, InterruptedException {
        String input = INPUT_PATH + "lineArtsSimple.pdf";
        String output = OUTPUT_PATH + "lineArtsSimple.pdf";
        String cmp = INPUT_PATH + "cmp_lineArtsSimple.pdf";

        cleanUp(input, output, Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(60f, 80f, 460f, 65f), ColorConstants.GRAY)));
        compareByContent(cmp, output, OUTPUT_PATH, "diff_35");
    }

    /**
     * In this test, glyph "1" got removed by the clean up area that on first sight is not covering the glyph.
     * However, we can't get the particular glyphs height and instead we have the same height for all glyphs.
     * Because of this, in case of the big font sizes such situations might occur, that even though visually glyph is
     * rather away from the cleanup location we still get it removed because it's bbox intersects with cleanup area rectangle.
     *
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void cleanUpTest36() throws IOException, InterruptedException {
        String input = INPUT_PATH + "bigOne.pdf";
        String output = OUTPUT_PATH + "bigOne.pdf";
        String cmp = INPUT_PATH + "cmp_bigOne.pdf";

        cleanUp(input, output, Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(300f, 370f, 215f, 270f), ColorConstants.GRAY)));
        compareByContent(cmp, output, OUTPUT_PATH, "diff_36");
    }

    /**
     * In this test we check that line style operators (such as 'w') are processed correctly
     */
    @Test
    public void cleanUpTest37() throws IOException, InterruptedException {
        String input = INPUT_PATH + "helloHelvetica.pdf";
        String output = OUTPUT_PATH + "helloHelvetica.pdf";
        String cmp = INPUT_PATH + "cmp_helloHelvetica.pdf";
        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(0f, 0f, 595f, 680f), ColorConstants.GRAY));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_37");
    }

    @Test
    public void cleanUpTest38() throws IOException, InterruptedException {
        String input = INPUT_PATH + "helloHelvetica02.pdf";
        String output = OUTPUT_PATH + "helloHelvetica02.pdf";
        String cmp = INPUT_PATH + "cmp_helloHelvetica02.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(0f, 0f, 0f, 0f), ColorConstants.GRAY));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_38");
    }

    @Test
    public void cleanUpTest39() throws IOException, InterruptedException {
        String input = INPUT_PATH + "corruptJpeg.pdf";
        String output = OUTPUT_PATH + "corruptJpeg.pdf";
        String cmp = INPUT_PATH + "cmp_corruptJpeg.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(100, 350, 100, 200), ColorConstants.ORANGE));

        cleanUp(input, output, cleanUpLocations);
        CleanUpImagesCompareTool cmpTool = new CleanUpImagesCompareTool();
        String errorMessage = cmpTool.extractAndCompareImages(output, cmp, OUTPUT_PATH, "1.2");
        String compareByContentResult = cmpTool.compareByContent(output, cmp, OUTPUT_PATH);
        if (compareByContentResult != null) {
            errorMessage += compareByContentResult;
        }

        if (!errorMessage.equals("")) {
            Assert.fail(errorMessage);
        }
    }

    @Test
    public void cleanUpTest40() throws IOException, InterruptedException {
        String input = INPUT_PATH + "emptyTj01.pdf";
        String output = OUTPUT_PATH + "emptyTj01.pdf";
        String cmp = INPUT_PATH + "cmp_emptyTj01.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(70f, 555f, 200f, 5f), ColorConstants.ORANGE));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_40");
    }

    @Test
    public void cleanUpTest41() throws IOException, InterruptedException {
        String input = INPUT_PATH + "newLines01.pdf";
        String output = OUTPUT_PATH + "newLines01.pdf";
        String cmp = INPUT_PATH + "cmp_newLines01.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(70f, 555f, 200f, 10f), ColorConstants.ORANGE));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_41");
    }

    @Test
    public void cleanUpTest42() throws IOException, InterruptedException {
        String input = INPUT_PATH + "newLines02.pdf";
        String output = OUTPUT_PATH + "newLines02.pdf";
        String cmp = INPUT_PATH + "cmp_newLines02.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(36f, 733f, 270f, 5f), ColorConstants.ORANGE));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_42");
    }

    @Test
    public void cleanUpTest43() throws IOException, InterruptedException {
        String input = INPUT_PATH + "newLines03.pdf";
        String output = OUTPUT_PATH + "newLines03.pdf";
        String cmp = INPUT_PATH + "cmp_newLines03.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(36f, 733f, 230f, 5f), ColorConstants.ORANGE));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_43");
    }

    @Test
    public void cleanUpTest44() throws IOException, InterruptedException {
        String input = INPUT_PATH + "emptyTj02.pdf";
        String output = OUTPUT_PATH + "emptyTj02.pdf";
        String cmp = INPUT_PATH + "cmp_emptyTj02.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(70f, 565f, 200f, 5f), ColorConstants.ORANGE));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_44");
    }

    @Test
    public void cleanUpTest45() throws IOException, InterruptedException {
        String input = INPUT_PATH + "emptyPdf.pdf";
        String output = OUTPUT_PATH + "emptyPdf.pdf";
        String cmp = INPUT_PATH + "cmp_emptyPdf.pdf";

        PdfAnnotation redactAnnotation = new PdfRedactAnnotation(new Rectangle(97, 405, 383, 40))
                .setOverlayText(new PdfString("OverlayTest"))
                .setDefaultAppearance(new PdfString("/Helv 0 Tf 0 g"));

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        pdfDocument.getFirstPage().addAnnotation(redactAnnotation);

        PdfCleaner.cleanUpRedactAnnotations(pdfDocument);

        pdfDocument.close();
        compareByContent(cmp, output, OUTPUT_PATH, "diff_45");
    }

    @Test
    public void cleanUpTest46() throws IOException {
        String input = INPUT_PATH + "emptyPdf.pdf";
        String output = OUTPUT_PATH + "emptyPdf.pdf";

        PdfAnnotation redactAnnotation = new PdfRedactAnnotation(new Rectangle(97, 405, 383, 40))
                .setOverlayText(new PdfString("OverlayTest"));

        try (PdfReader reader = new PdfReader(input);
                PdfWriter writer = new PdfWriter(output);
                PdfDocument pdfDocument = new PdfDocument(reader, writer)) {
            pdfDocument.getFirstPage().addAnnotation(redactAnnotation);

            Exception e = Assert.assertThrows(PdfException.class, () -> PdfCleaner.cleanUpRedactAnnotations(pdfDocument));
            Assert.assertEquals(CleanupExceptionMessageConstant.DEFAULT_APPEARANCE_NOT_FOUND, e.getMessage());
        }
    }

    @Test
    public void autoCleanWithLocationAndStreamParamsTest() throws Exception {
        String input = INPUT_PATH + "fontCleanup.pdf";
        String output = OUTPUT_PATH + "autoCleanWithLocationAndStreamParamsTest.pdf";
        String cmp = INPUT_PATH + "cmp_autoCleanWithLocationAndStreamParamsTest.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("leonard"));
        List<PdfCleanUpLocation> additionalLocation = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(0, 0, 200, 100)));

        PdfCleaner
                .autoSweepCleanUp(new FileInputStream(input), new FileOutputStream(output), strategy, additionalLocation);

        compareByContent(cmp, output, OUTPUT_PATH, "autoCleanWithLocationAndStreamParamsTest");
    }

    @Test
    public void autoCleanPageTest() throws Exception {
        String input = INPUT_PATH + "fontCleanup.pdf";
        String output = OUTPUT_PATH + "autoCleanPageTest.pdf";
        String cmp = INPUT_PATH + "cmp_autoCleanPageTest.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("leonard"));

        try (
                PdfReader reader = new PdfReader(input);
                PdfWriter writer = new PdfWriter(output);
                PdfDocument document = new PdfDocument(reader, writer)
        ) {
            PdfCleaner.autoSweepCleanUp(document.getPage(1), strategy);
        }

        compareByContent(cmp, output, OUTPUT_PATH, "autoCleanPageTest");
    }

    @Test
    public void autoCleanPageWithAdditionalLocationTest() throws Exception {
        String input = INPUT_PATH + "fontCleanup.pdf";
        String output = OUTPUT_PATH + "autoCleanPageWithAdditionalLocationTest.pdf";
        String cmp = INPUT_PATH + "cmp_autoCleanPageWithAdditionalLocationTest.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("leonard"));
        List<PdfCleanUpLocation> additionalLocation = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(0, 0, 200, 100), ColorConstants.RED)
        );

        try (
                PdfReader reader = new PdfReader(input);
                PdfWriter writer = new PdfWriter(output);
                PdfDocument document = new PdfDocument(reader, writer)
        ) {
            PdfCleaner.autoSweepCleanUp(document.getPage(1), strategy, additionalLocation);
        }

        compareByContent(cmp, output, OUTPUT_PATH, "autoCleanPageWithAdditionalLocationTest");
    }

    @Test
    public void autoCleanPageWithAdditionalLocationAndPropertyTest() throws Exception {
        String input = INPUT_PATH + "fontCleanup.pdf";
        String output = OUTPUT_PATH + "autoCleanPageWithAdditionalLocationAndPropertyTest.pdf";
        String cmp = INPUT_PATH + "cmp_autoCleanPageWithAdditionalLocationAndPropertyTest.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("leonard"));
        List<PdfCleanUpLocation> additionalLocation = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(0, 0, 200, 100), ColorConstants.RED)
        );

        try (
                PdfReader reader = new PdfReader(input);
                PdfWriter writer = new PdfWriter(output);
                PdfDocument document = new PdfDocument(reader, writer)
        ) {
            PdfCleaner.autoSweepCleanUp(document.getPage(1), strategy, additionalLocation, new CleanUpProperties());
        }

        compareByContent(cmp, output, OUTPUT_PATH, "autoCleanPageWithAdditionalLocationAndPropertyTest");
    }

    @Test
    public void autoCleanWithCleaUpPropertiesTest() throws Exception {
        String input = INPUT_PATH + "absentICentry.pdf";
        String output = OUTPUT_PATH + "autoCleanWithCleaUpPropertiesTest.pdf";
        String cmp = INPUT_PATH + "cmp_autoCleanWithCleaUpPropertiesTest.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("leonard"));
        List<PdfCleanUpLocation> additionalLocation = new ArrayList<>();
        additionalLocation.add(new PdfCleanUpLocation(1, new Rectangle(100, 100, 500, 500)));

        PdfCleaner.autoSweepCleanUp(new FileInputStream(input), new FileOutputStream(output), strategy,
                additionalLocation, new CleanUpProperties());

        compareByContent(cmp, output, OUTPUT_PATH, "autoCleanWithCleaUpPropertiesTest");
    }

    @Test
    public void autoCleanWithFalseProcessAnnotationTest() throws Exception {
        String input = INPUT_PATH + "absentICentry.pdf";
        String output = OUTPUT_PATH + "autoCleanWithFalseProcessAnnotationTest.pdf";
        String cmp = INPUT_PATH + "cmp_autoCleanWithFalseProcessAnnotationTest.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("leonard"));
        List<PdfCleanUpLocation> additionalLocation = new ArrayList<>();
        additionalLocation.add(new PdfCleanUpLocation(1, new Rectangle(100, 560, 200, 30)));

        CleanUpProperties properties = new CleanUpProperties();
        properties.setProcessAnnotations(false);
        PdfCleaner
                .autoSweepCleanUp(new FileInputStream(input), new FileOutputStream(output), strategy, additionalLocation, properties);

        compareByContent(cmp, output, OUTPUT_PATH, "autoCleanWithFalseProcessAnnotationTest");
    }

    @Test
    public void documentInNonStampingModeTest() throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(INPUT_PATH + "fontCleanup.pdf"));

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(0, 0, 500, 500)));

        Exception e = Assert.assertThrows(PdfException.class, () -> PdfCleaner.cleanUp(pdfDocument, cleanUpLocations));
        Assert.assertEquals(CleanupExceptionMessageConstant.PDF_DOCUMENT_MUST_BE_OPENED_IN_STAMPING_MODE, e.getMessage());
    }

    @Test
    public void documentWithoutReaderTest() {
        PdfDocument pdfDocument = new PdfDocument (new PdfWriter(new ByteArrayOutputStream()));

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(0, 0, 500, 500)));

        Exception e = Assert.assertThrows(PdfException.class, () -> PdfCleaner.cleanUp(pdfDocument, cleanUpLocations));
        Assert.assertEquals(CleanupExceptionMessageConstant.PDF_DOCUMENT_MUST_BE_OPENED_IN_STAMPING_MODE, e.getMessage());
    }

    @Test
    public void cleanUpTestFontColor() throws IOException, InterruptedException {
        String filename = "fontCleanup.pdf";
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(INPUT_PATH + filename), new PdfWriter(OUTPUT_PATH + filename));
        PdfCleaner.cleanUpRedactAnnotations(pdfDoc);
        pdfDoc.close();
        Assert.assertNull(new CompareTool().compareVisually(OUTPUT_PATH + filename, INPUT_PATH + "cmp_" + filename, OUTPUT_PATH, "diff_"));
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = IoLogMessageConstant.PDF_REFERS_TO_NOT_EXISTING_PROPERTY_DICTIONARY))
    public void noPropertiesInResourcesTest() throws IOException, InterruptedException {
        String fileName = "noPropertiesInResourcesTest";
        String input = INPUT_PATH + fileName + ".pdf";
        String output = OUTPUT_PATH + fileName + ".pdf";
        String cmp = INPUT_PATH + "cmp_" + fileName + ".pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(0, 0, 595, 842), ColorConstants.RED));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_" + fileName);
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = IoLogMessageConstant.PDF_REFERS_TO_NOT_EXISTING_PROPERTY_DICTIONARY))
    public void incorrectBDCToBMCTest() throws IOException, InterruptedException {
        String fileName = "incorrectBDCToBMCTest";
        String input = INPUT_PATH + fileName + ".pdf";
        String output = OUTPUT_PATH + fileName + ".pdf";
        String cmp = INPUT_PATH + "cmp_" + fileName + ".pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(0, 0, 10, 10), ColorConstants.RED));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_" + fileName);
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = CleanUpLogMessageConstant.FAILED_TO_PROCESS_A_TRANSFORMATION_MATRIX))
    public void noninvertibleMatrixRemoveAllTest() throws IOException, InterruptedException {
        String fileName = "noninvertibleMatrixRemoveAllTest";
        String input = INPUT_PATH + "noninvertibleMatrix.pdf";
        String output = OUTPUT_PATH + fileName + ".pdf";
        String cmp = INPUT_PATH + "cmp_" + fileName + ".pdf";

        PdfCleanUpLocation wholePageLocation = new PdfCleanUpLocation(1, new Rectangle(0, 0, 595, 842), null);

        cleanUp(input, output, Arrays.asList(wholePageLocation));
        compareByContent(cmp, output, OUTPUT_PATH, "diff_noninvertibleMatrixRemoveAllTest");
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = CleanUpLogMessageConstant.FAILED_TO_PROCESS_A_TRANSFORMATION_MATRIX))
    public void noninvertibleMatrixRemoveAllTest02() throws IOException, InterruptedException {
        String fileName = "noninvertibleMatrixRemoveAllTest02";
        String input = INPUT_PATH + "noninvertibleMatrix.pdf";
        String output = OUTPUT_PATH + fileName + ".pdf";
        String cmp = INPUT_PATH + "cmp_" + fileName + ".pdf";

        PdfCleanUpLocation wholePageLocation = new PdfCleanUpLocation(1, new Rectangle(-1000, -1000, 2000, 2000), null);

        cleanUp(input, output, Arrays.asList(wholePageLocation));
        compareByContent(cmp, output, OUTPUT_PATH, "diff_noninvertibleMatrixRemoveAllTest");
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = CleanUpLogMessageConstant.FAILED_TO_PROCESS_A_TRANSFORMATION_MATRIX))
    public void noninvertibleMatrixRemoveNothingTest() throws IOException, InterruptedException {
        String fileName = "noninvertibleMatrixRemoveNothingTest";
        String input = INPUT_PATH + "noninvertibleMatrix.pdf";
        String output = OUTPUT_PATH + fileName + ".pdf";
        String cmp = INPUT_PATH + "cmp_" + fileName + ".pdf";

        PdfCleanUpLocation dummyLocation = new PdfCleanUpLocation(1, new Rectangle(0, 0, 0, 0), null);

        cleanUp(input, output, Arrays.asList(dummyLocation));
        compareByContent(cmp, output, OUTPUT_PATH, "diff_noninvertibleMatrixRemoveNothingTest");
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = CleanUpLogMessageConstant.FAILED_TO_PROCESS_A_TRANSFORMATION_MATRIX, count = 7))
    public void pathAndIncorrectCMTest() throws IOException, InterruptedException {
        String fileName = "pathAndIncorrectCM";
        String input = INPUT_PATH + "pathAndIncorrectCM.pdf";
        String output = OUTPUT_PATH + fileName + ".pdf";
        String cmp = INPUT_PATH + "cmp_" + fileName + ".pdf";

        List<PdfCleanUpLocation> dummyLocationsList = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            dummyLocationsList.add(new PdfCleanUpLocation(i + 1, new Rectangle(0, 0, 0, 0), null));
        }

        cleanUp(input, output, dummyLocationsList);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_pathAndIncorrectCMTest");
    }

    @Test
    public void cleanUpStreamParamsTest() throws Exception {
        String in = INPUT_PATH + "page229.pdf";
        String out = OUTPUT_PATH + "cleanUpStreamParamsTest.pdf";
        String cmp = INPUT_PATH + "cmp_cleanUpStreamParamsTest.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<>();
        cleanUpLocations.add(new PdfCleanUpLocation(1, new Rectangle(100, 560, 200, 30)));

        InputStream file = new FileInputStream(in);
        OutputStream output = new FileOutputStream(out);
        PdfCleaner.cleanUp(file, output, cleanUpLocations);

        compareByContent(cmp, out, OUTPUT_PATH, "diff_cleanUpStreamParamsTest");
    }

    @Test
    public void autoSweepCleanUpWithAdditionalLocationTest() throws Exception {
        String in = INPUT_PATH + "page229.pdf";
        String out = OUTPUT_PATH + "autoSweepCleanUpWithAdditionalLocationTest.pdf";
        String cmp = INPUT_PATH + "cmp_autoSweepCleanUpWithAdditionalLocationTest.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<>();
        cleanUpLocations.add(new PdfCleanUpLocation(1, new Rectangle(100, 560, 200, 30)));

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy(" (T|t)o ").setRedactionColor(ColorConstants.GREEN));

        try (
                PdfReader reader = new PdfReader(in);
                PdfWriter writer = new PdfWriter(out);
                PdfDocument document = new PdfDocument(reader, writer)
        ) {
            PdfCleaner.autoSweepCleanUp(document, strategy, cleanUpLocations);

        }

        CompareTool cmpTool = new CompareTool();
        String errorMessage = cmpTool.compareVisually(out, cmp, OUTPUT_PATH, "diff_autoSweepCleanUpWithAdditionalLocationTest_");
        if (errorMessage != null) {
            Assert.fail(errorMessage);
        }
    }

    @Test
    public void simpleCleanUpOnRotatedPages() throws IOException, InterruptedException {
        String fileName = "simpleCleanUpOnRotatedPages";
        String input = INPUT_PATH + "documentWithRotatedPages.pdf";
        String output = OUTPUT_PATH + fileName + ".pdf";
        String cmp = INPUT_PATH + "cmp_" + fileName + ".pdf";

        List<PdfCleanUpLocation> locationsList = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            locationsList.add(new PdfCleanUpLocation(i + 1, new Rectangle(100, 100, 200, 100), ColorConstants.GREEN));
        }

        cleanUp(input, output, locationsList);
        compareByContent(cmp, output, OUTPUT_PATH, "diff_pathAndIncorrectCMTest");
    }

    @Test
    public void simpleCleanUpOnRotatedPagesIgnoreRotation() throws IOException, InterruptedException {
        String fileName = "simpleCleanUpOnRotatedPagesIgnoreRotation";
        String input = INPUT_PATH + "documentWithRotatedPages.pdf";
        String output = OUTPUT_PATH + fileName + ".pdf";
        String cmp = INPUT_PATH + "cmp_" + fileName + ".pdf";

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        List<PdfCleanUpLocation> locationsList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            locationsList.add(new PdfCleanUpLocation(i + 1, Rectangle.getRectangleOnRotatedPage(new Rectangle(100, 100, 200, 100), pdfDocument.getPage(i+1)), ColorConstants.GREEN));
        }

        PdfCleaner.cleanUp(pdfDocument, locationsList);

        pdfDocument.close();
        compareByContent(cmp, output, OUTPUT_PATH, "diff_pathAndIncorrectCMTest");
    }

    @Test
    public void cleanUpDocWithoutReaderTest() {
        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(new ByteArrayOutputStream()));

        Exception e = Assert.assertThrows(PdfException.class, () -> PdfCleaner.cleanUpRedactAnnotations(pdfDoc));
        Assert.assertEquals(CleanupExceptionMessageConstant.PDF_DOCUMENT_MUST_BE_OPENED_IN_STAMPING_MODE, e.getMessage());
    }

    @Test
    public void cleanUpDocWithoutWriterTest() throws IOException {
        PdfDocument pdfDoc = new PdfDocument(new PdfReader(INPUT_PATH + "emptyPdf.pdf"));

        Exception e = Assert.assertThrows(PdfException.class, () -> PdfCleaner.cleanUpRedactAnnotations(pdfDoc));
        Assert.assertEquals(CleanupExceptionMessageConstant.PDF_DOCUMENT_MUST_BE_OPENED_IN_STAMPING_MODE, e.getMessage());
    }

    @Test
    public void redactLipsum() throws IOException, InterruptedException {
        String input = INPUT_PATH + "Lipsum.pdf";
        String output = OUTPUT_PATH + "cleanUpDocument.pdf";
        String cmp = INPUT_PATH + "cmp_cleanUpDocument.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("(D|d)olor").setRedactionColor(ColorConstants.GREEN));

        PdfWriter writer = new PdfWriter(output);
        writer.setCompressionLevel(0);
        PdfDocument pdf = new PdfDocument(new PdfReader(input), writer);

        // sweep
        PdfCleaner.autoSweepCleanUp(pdf, strategy);

        pdf.close();

        // compare
        compareByContent(cmp, output, OUTPUT_PATH, "diff_cleanUpDocument_");
    }

    @Test
    public void cleanUpPageTest() throws IOException, InterruptedException {
        String input = INPUT_PATH + "Lipsum.pdf";
        String output = OUTPUT_PATH + "cleanUpPage.pdf";
        String cmp = INPUT_PATH + "cmp_cleanUpPage.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("(D|d)olor").setRedactionColor(ColorConstants.GREEN));

        PdfWriter writer = new PdfWriter(output);
        writer.setCompressionLevel(0);
        PdfDocument pdf = new PdfDocument(new PdfReader(input), writer);

        // sweep
        PdfCleaner.autoSweepCleanUp(pdf.getPage(1), strategy);

        pdf.close();

        // compare
        compareByContent(cmp, output, OUTPUT_PATH, "diff_cleanUpPage_");
    }

    @Test
    public void redactLipsumPatternStartsWithWhiteSpace() throws IOException, InterruptedException {
        String input = INPUT_PATH + "Lipsum.pdf";
        String output = OUTPUT_PATH + "redactLipsumPatternStartsWithWhitespace.pdf";
        String cmp = INPUT_PATH + "cmp_redactLipsumPatternStartsWithWhitespace.pdf";
        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("\\s(D|d)olor").setRedactionColor(ColorConstants.GREEN));
        PdfWriter writer = new PdfWriter(output);
        writer.setCompressionLevel(0);
        PdfDocument pdf = new PdfDocument(new PdfReader(input), writer);

        // sweep
        PdfCleaner.autoSweepCleanUp(pdf, strategy);

        pdf.close();
        // compare
        compareByContent(cmp, output, OUTPUT_PATH, "diff_redactLipsumPatternStartsWithWhitespace_");
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = CleanUpLogMessageConstant.FAILED_TO_PROCESS_A_TRANSFORMATION_MATRIX, count = 2))
    public void redactPdfWithNoninvertibleMatrix() throws IOException, InterruptedException {
        String input = INPUT_PATH + "noninvertibleMatrix.pdf";
        String output = OUTPUT_PATH + "redactPdfWithNoninvertibleMatrix.pdf";
        String cmp = INPUT_PATH + "cmp_redactPdfWithNoninvertibleMatrix.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("Hello World!").setRedactionColor(ColorConstants.GREEN));

        PdfDocument pdf = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        // sweep
        PdfCleaner.autoSweepCleanUp(pdf, strategy);

        pdf.close();

        // compare
        compareByContent(cmp, output, OUTPUT_PATH, "diff_redactPdfWithNoninvertibleMatrix_");
    }

    @Test
    @Ignore("DEVSIX-4047")
    public void lineArtsDrawingOnCanvasTest() throws IOException, InterruptedException {
        String input = INPUT_PATH + "lineArtsDrawingOnCanvas.pdf";
        String output = OUTPUT_PATH + "lineArtsDrawingOnCanvas.pdf";
        String cmp = INPUT_PATH + "cmp_lineArtsDrawingOnCanvas.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("(iphone)|(iPhone)"));

        PdfDocument pdf = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        PdfCleaner.autoSweepCleanUp(pdf, strategy);

        pdf.close();

        compareByContent(cmp, output, OUTPUT_PATH, "diff_lineArtsDrawingOnCanvasTest_");
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
