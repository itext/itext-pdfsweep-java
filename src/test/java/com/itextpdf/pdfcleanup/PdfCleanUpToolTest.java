/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2017 iText Group NV
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


import com.itextpdf.kernel.PdfException;
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
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Category(IntegrationTest.class)
public class PdfCleanUpToolTest extends ExtendedITextTest {

    private static final String inputPath = "./src/test/resources/com/itextpdf/pdfcleanup/PdfCleanUpToolTest/";
    private static final String outputPath = "./target/test/com/itextpdf/pdfcleanup/PdfCleanUpToolTest/";

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    @BeforeClass
    public static void before() {
        createOrClearDestinationFolder(outputPath);
    }

    @Test
    public void cleanUpTest01() throws IOException, InterruptedException {
        String input = inputPath + "page229.pdf";
        String output = outputPath + "page229_01.pdf";
        String cmp = inputPath + "cmp_page229_01.pdf";
        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(240.0f, 602.3f, 275.7f - 240.0f, 614.8f - 602.3f), ColorConstants.GRAY),
                new PdfCleanUpLocation(1, new Rectangle(171.3f, 550.3f, 208.4f - 171.3f, 562.8f - 550.3f), ColorConstants.GRAY),
                new PdfCleanUpLocation(1, new Rectangle(270.7f, 459.2f, 313.1f - 270.7f, 471.7f - 459.2f), ColorConstants.GRAY),
                new PdfCleanUpLocation(1, new Rectangle(249.9f, 329.3f, 279.6f - 249.9f, 341.8f - 329.3f), ColorConstants.GRAY),
                new PdfCleanUpLocation(1, new Rectangle(216.2f, 303.3f, 273.0f - 216.2f, 315.8f - 303.3f), ColorConstants.GRAY));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_01");
    }

    @Test
    public void cleanUpTest02() throws IOException, InterruptedException {
        String input = inputPath + "page229-modified-Tc-Tw.pdf";
        String output = outputPath + "page229-modified-Tc-Tw.pdf";
        String cmp = inputPath + "cmp_page229-modified-Tc-Tw.pdf";
        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(240.0f, 602.3f, 275.7f - 240.0f, 614.8f - 602.3f), ColorConstants.GRAY),
                new PdfCleanUpLocation(1, new Rectangle(171.3f, 550.3f, 208.4f - 171.3f, 562.8f - 550.3f), ColorConstants.GRAY),
                new PdfCleanUpLocation(1, new Rectangle(270.7f, 459.2f, 313.1f - 270.7f, 471.7f - 459.2f), ColorConstants.GRAY),
                new PdfCleanUpLocation(1, new Rectangle(249.9f, 329.3f, 279.6f - 249.9f, 341.8f - 329.3f), ColorConstants.GRAY),
                new PdfCleanUpLocation(1, new Rectangle(216.2f, 303.3f, 273.0f - 216.2f, 315.8f - 303.3f), ColorConstants.GRAY));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_02");
    }

    @Test
    public void cleanUpTest03() throws IOException, InterruptedException {
        String input = inputPath + "page166_03.pdf";
        String output = outputPath + "page166_03.pdf";
        String cmp = inputPath + "cmp_page166_03.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_03");
    }

    @Test
    public void cleanUpTest04() throws IOException, InterruptedException {
        String input = inputPath + "hello_05.pdf";
        String output = outputPath + "hello_05.pdf";
        String cmp = inputPath + "cmp_hello_05.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_04");
    }

    @Test
    public void cleanUpTest05() throws IOException, InterruptedException {
        String input = inputPath + "BigImage-jpg.pdf";
        String output = outputPath + "BigImage-jpg.pdf";
        String cmp = inputPath + "cmp_BigImage-jpg.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_05");
    }

    @Test
    public void cleanUpTest06() throws IOException, InterruptedException {
        String input = inputPath + "BigImage-png.pdf";
        String output = outputPath + "BigImage-png.pdf";
        String cmp = inputPath + "cmp_BigImage-png.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_06");
    }

    @Test
    public void cleanUpTest07() throws IOException, InterruptedException {
        String input = inputPath + "BigImage-tif.pdf";
        String output = outputPath + "BigImage-tif.pdf";
        String cmp = inputPath + "cmp_BigImage-tif.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_07");
    }

    @Test
    public void cleanUpTest08() throws IOException, InterruptedException {
        String input = inputPath + "BigImage-tif-lzw.pdf";
        String output = outputPath + "BigImage-tif-lzw.pdf";
        String cmp = inputPath + "cmp_BigImage-tif-lzw.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_08");
    }

    @Test
    public void cleanUpTest09() throws IOException, InterruptedException {
        String input = inputPath + "simpleImmediate.pdf";
        String output = outputPath + "simpleImmediate.pdf";
        String cmp = inputPath + "cmp_simpleImmediate.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f - 97f, 445f - 405f), ColorConstants.GRAY));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_09");
    }

    @Test
    public void cleanUpTest10() throws IOException, InterruptedException {
        String input = inputPath + "simpleImmediate-tm.pdf";
        String output = outputPath + "simpleImmediate-tm.pdf";
        String cmp = inputPath + "cmp_simpleImmediate-tm.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f - 97f, 445f - 405f), ColorConstants.GRAY));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_10");
    }

    @Test
    public void cleanUpTest11() throws IOException, InterruptedException {
        String input = inputPath + "multiUseIndirect.pdf";
        String output = outputPath + "multiUseIndirect.pdf";
        String cmp = inputPath + "cmp_multiUseIndirect.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(97f, 605f, 480f - 97f, 645f - 605f), ColorConstants.GRAY));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_11");
    }

    @Test
    public void cleanUpTest12() throws IOException, InterruptedException {
        String input = inputPath + "multiUseImage.pdf";
        String output = outputPath + "multiUseImage.pdf";
        String cmp = inputPath + "cmp_multiUseImage.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f - 97f, 445f - 405f), ColorConstants.GRAY));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_12");
    }

    @Test
    public void cleanUpTest13() throws IOException, InterruptedException {
        String input = inputPath + "smaskImage.pdf";
        String output = outputPath + "smaskImage.pdf";
        String cmp = inputPath + "cmp_smaskImage.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f - 97f, 445f - 405f), ColorConstants.GRAY));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_13");
    }

    @Test
    public void cleanUpTest14() throws IOException, InterruptedException {
        String input = inputPath + "rotatedImg.pdf";
        String output = outputPath + "rotatedImg.pdf";
        String cmp = inputPath + "cmp_rotatedImg.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(97f, 405f, 480f - 97f, 445f - 405f), ColorConstants.GRAY));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_14");
    }

    @Test
    public void cleanUpTest15() throws IOException, InterruptedException {
        String input = inputPath + "lineArtsCompletely.pdf";
        String output = outputPath + "lineArtsCompletely.pdf";
        String cmp = inputPath + "cmp_lineArtsCompletely.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_15");
    }

    @Test
    public void cleanUpTest16() throws IOException, InterruptedException {
        String input = inputPath + "lineArtsPartially.pdf";
        String output = outputPath + "lineArtsPartially.pdf";
        String cmp = inputPath + "cmp_lineArtsPartially.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_16");
    }

    @Test
    public void cleanUpTest17() throws IOException, InterruptedException {
        String input = inputPath + "dashedStyledClosedBezier.pdf";
        String output = outputPath + "dashedStyledClosedBezier.pdf";
        String cmp = inputPath + "cmp_dashedStyledClosedBezier.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_17");
    }

    @Test
    public void cleanUpTest18() throws IOException, InterruptedException {
        String input = inputPath + "styledLineArts.pdf";
        String output = outputPath + "styledLineArts.pdf";
        String cmp = inputPath + "cmp_styledLineArts.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_18");
    }

    @Test
    public void cleanUpTest19() throws IOException, InterruptedException {
        String input = inputPath + "dashedBezier.pdf";
        String output = outputPath + "dashedBezier.pdf";
        String cmp = inputPath + "cmp_dashedBezier.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_19");
    }

    @Test
    public void cleanUpTest20() throws IOException, InterruptedException {
        String input = inputPath + "closedBezier.pdf";
        String output = outputPath + "closedBezier.pdf";
        String cmp = inputPath + "cmp_closedBezier.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_20");
    }

    @Test
    public void cleanUpTest21() throws IOException, InterruptedException {
        String input = inputPath + "clippingNWRule.pdf";
        String output = outputPath + "clippingNWRule.pdf";
        String cmp = inputPath + "cmp_clippingNWRule.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_21");
    }

    @Test
    public void cleanUpTest22() throws IOException, InterruptedException {
        String input = inputPath + "dashedClosedRotatedTriangles.pdf";
        String output = outputPath + "dashedClosedRotatedTriangles.pdf";
        String cmp = inputPath + "cmp_dashedClosedRotatedTriangles.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_22");
    }

    @Test
    public void cleanUpTest23() throws IOException, InterruptedException {
        String input = inputPath + "miterTest.pdf";
        String output = outputPath + "miterTest.pdf";
        String cmp = inputPath + "cmp_miterTest.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_23");
    }

    @Test
    public void cleanUpTest24() throws IOException, InterruptedException {
        String input = inputPath + "degenerateCases.pdf";
        String output = outputPath + "degenerateCases.pdf";
        String cmp = inputPath + "cmp_degenerateCases.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_24");
    }

    @Test
    public void cleanUpTest25() throws IOException, InterruptedException {
        String input = inputPath + "absentICentry.pdf";
        String output = outputPath + "absentICentry.pdf";
        String cmp = inputPath + "cmp_absentICentry.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_25");
    }

    @Test
    public void cleanUpTest26() throws IOException, InterruptedException {
        String input = inputPath + "lotOfDashes.pdf";
        String output = outputPath + "lotOfDashes.pdf";
        String cmp = inputPath + "cmp_lotOfDashes.pdf";

        cleanUp(input, output, null);
        compareByContent(cmp, output, outputPath, "diff_26");
    }

    @Test
    public void cleanUpTest27() throws IOException, InterruptedException {
        String input = inputPath + "clipPathReduction.pdf";
        String output = outputPath + "clipPathReduction.pdf";
        String cmp = inputPath + "cmp_clipPathReduction.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(212, 394, 186, 170), null));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_27");
    }

    @Test
    public void cleanUpTest30() throws IOException, InterruptedException {
        String input = inputPath + "inlineImages.pdf";
        String output = outputPath + "inlineImages_full.pdf";
        String cmp = inputPath + "cmp_inlineImages_full.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(10, 100, 400, 600), null));
        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_30");
    }

    @Test
    public void cleanUpTest32() throws IOException, InterruptedException {
        String input = inputPath + "page229.pdf";
        String output = outputPath + "wholePageCleanUp.pdf";
        String cmp = inputPath + "cmp_wholePageCleanUp.pdf";

        cleanUp(input, output, Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(1, 1, PageSize.A4.getWidth() - 1, PageSize.A4.getHeight() - 1))));
        compareByContent(cmp, output, outputPath, "diff_32");
    }

    @Test
    public void cleanUpTest33() throws IOException, InterruptedException {
        String input = inputPath + "viewer_prefs_dict_table.pdf";
        String output = outputPath + "complexTextPositioning.pdf";
        String cmp = inputPath + "cmp_complexTextPositioning.pdf";

        cleanUp(input, output, Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(300f, 370f, 215f, 270f))));
        compareByContent(cmp, output, outputPath, "diff_33");
    }

    @Test
    public void cleanUpTest34() throws IOException, InterruptedException {
        String input = inputPath + "new_york_times.pdf";
        String output = outputPath + "textAndImages.pdf";
        String cmp = inputPath + "cmp_textAndImages.pdf";

        cleanUp(input, output, Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(150f, 235f, 230f, 445f))));
        compareByContent(cmp, output, outputPath, "diff_34");
    }

    @Test
    public void cleanUpTest35() throws IOException, InterruptedException {
        String input = inputPath + "lineArtsSimple.pdf";
        String output = outputPath + "lineArtsSimple.pdf";
        String cmp = inputPath + "cmp_lineArtsSimple.pdf";

        cleanUp(input, output, Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(60f, 80f, 460f, 65f), ColorConstants.GRAY)));
        compareByContent(cmp, output, outputPath, "diff_35");
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
        String input = inputPath + "bigOne.pdf";
        String output = outputPath + "bigOne.pdf";
        String cmp = inputPath + "cmp_bigOne.pdf";

        cleanUp(input, output, Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(300f, 370f, 215f, 270f), ColorConstants.GRAY)));
        compareByContent(cmp, output, outputPath, "diff_36");
    }

    /**
     * In this test we check that line style operators (such as 'w') are processed correctly
     */
    @Test
    public void cleanUpTest37() throws IOException, InterruptedException {
        String input = inputPath + "helloHelvetica.pdf";
        String output = outputPath + "helloHelvetica.pdf";
        String cmp = inputPath + "cmp_helloHelvetica.pdf";
        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(0f, 0f, 595f, 680f), ColorConstants.GRAY));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_37");
    }

    @Test
    public void cleanUpTest38() throws IOException, InterruptedException {
        String input = inputPath + "helloHelvetica02.pdf";
        String output = outputPath + "helloHelvetica02.pdf";
        String cmp = inputPath + "cmp_helloHelvetica02.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(0f, 0f, 0f, 0f), ColorConstants.GRAY));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_38");
    }

    @Test
    public void cleanUpTest39() throws IOException, InterruptedException {
        String input = inputPath + "corruptJpeg.pdf";
        String output = outputPath + "corruptJpeg.pdf";
        String cmp = inputPath + "cmp_corruptJpeg.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(100, 350, 100, 200), ColorConstants.ORANGE));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_39");
    }

    @Test
    public void cleanUpTest40() throws IOException, InterruptedException {
        String input = inputPath + "emptyTj01.pdf";
        String output = outputPath + "emptyTj01.pdf";
        String cmp = inputPath + "cmp_emptyTj01.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(70f, 555f, 200f, 5f), ColorConstants.ORANGE));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_40");
    }

    @Test
    public void cleanUpTest41() throws IOException, InterruptedException {
        String input = inputPath + "newLines01.pdf";
        String output = outputPath + "newLines01.pdf";
        String cmp = inputPath + "cmp_newLines01.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(70f, 555f, 200f, 10f), ColorConstants.ORANGE));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_41");
    }

    @Test
    public void cleanUpTest42() throws IOException, InterruptedException {
        String input = inputPath + "newLines02.pdf";
        String output = outputPath + "newLines02.pdf";
        String cmp = inputPath + "cmp_newLines02.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(36f, 733f, 270f, 5f), ColorConstants.ORANGE));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_42");
    }

    @Test
    public void cleanUpTest43() throws IOException, InterruptedException {
        String input = inputPath + "newLines03.pdf";
        String output = outputPath + "newLines03.pdf";
        String cmp = inputPath + "cmp_newLines03.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(36f, 733f, 230f, 5f), ColorConstants.ORANGE));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_43");
    }

    @Test
    public void cleanUpTest44() throws IOException, InterruptedException {
        String input = inputPath + "emptyTj02.pdf";
        String output = outputPath + "emptyTj02.pdf";
        String cmp = inputPath + "cmp_emptyTj02.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = Arrays.asList(
                new PdfCleanUpLocation(1, new Rectangle(70f, 565f, 200f, 5f), ColorConstants.ORANGE));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_44");
    }

    @Test
    public void cleanUpTest45() throws IOException, InterruptedException {
        String input = inputPath + "emptyPdf.pdf";
        String output = outputPath + "emptyPdf.pdf";
        String cmp = inputPath + "cmp_emptyPdf.pdf";

        PdfAnnotation redactAnnotation = new PdfRedactAnnotation(new Rectangle(97, 405, 383, 40))
                .setOverlayText(new PdfString("OverlayTest"))
                .setDefaultAppearance(new PdfString("/Helv 0 Tf 0 g"));

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        pdfDocument.getFirstPage().addAnnotation(redactAnnotation);

        new PdfCleanUpTool(pdfDocument, true).cleanUp();

        pdfDocument.close();
        compareByContent(cmp, output, outputPath, "diff_45");
    }

    @Test
    public void cleanUpTest46() throws IOException {
        junitExpectedException.expect(PdfException.class);
        junitExpectedException.expectMessage(PdfException.DefaultAppearanceNotFound);
        
        String input = inputPath + "emptyPdf.pdf";
        String output = outputPath + "emptyPdf.pdf";

        PdfAnnotation redactAnnotation = new PdfRedactAnnotation(new Rectangle(97, 405, 383, 40))
                .setOverlayText(new PdfString("OverlayTest"));

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        pdfDocument.getFirstPage().addAnnotation(redactAnnotation);

        new PdfCleanUpTool(pdfDocument, true).cleanUp();

        pdfDocument.close();
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
