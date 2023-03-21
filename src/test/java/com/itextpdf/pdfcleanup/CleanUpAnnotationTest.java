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
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfcleanup.logs.CleanUpLogMessageConstant;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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

        PdfCleanUpLocation linkLoc = new PdfCleanUpLocation(1, new Rectangle(235, 740, 30, 16), ColorConstants.BLUE);
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

        PdfCleanUpLocation textLoc = new PdfCleanUpLocation(1, new Rectangle(150, 650, 0, 0), ColorConstants.RED);
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

        PdfCleanUpLocation lineLoc = new PdfCleanUpLocation(1, new Rectangle(20, 20, 555, 0), ColorConstants.GREEN);
        cleanUpLocations.add(lineLoc);

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_Annotation_line01");

    }

    @Test
    public void cleanLineAnnotation02() throws IOException, InterruptedException {
        String input = inputPath + "lineAnnotationLeaders.pdf";
        String output = outputPath + "cleanLineAnnotation02.pdf";
        String cmp = inputPath + "cmp_cleanLineAnnotation02.pdf";

        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();

        PdfCleanUpLocation lineLoc = new PdfCleanUpLocation(1, new Rectangle(100, 560, 200, 30), ColorConstants.GREEN);
        cleanUpLocations.add(lineLoc);

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath);
    }

    @Test
    public void cleanHighlightAnnotation01() throws IOException, InterruptedException {
        String input = inputPath + "cleanAnnotation.pdf";
        String output = outputPath + "cleanAnnotation_highlight01.pdf";
        String cmp = inputPath + "cmp_cleanAnnotation_highlight01.pdf";
        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();

        PdfCleanUpLocation highLightLoc = new PdfCleanUpLocation(1, new Rectangle(105, 500, 70, 10), ColorConstants.BLACK);
        cleanUpLocations.add(highLightLoc);

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_text_highlight01");
    }

    @Test
    public void cleanStrikeOutAnnotation01() throws IOException, InterruptedException {
        String input = inputPath + "strikeOutAnnotQuadOutsideRect.pdf";
        String output = outputPath + "cleanStrikeOutAnnotation01.pdf";
        String cmp = inputPath + "cmp_cleanStrikeOutAnnotation01.pdf";
        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();

        cleanUpLocations.add(new PdfCleanUpLocation(1, new Rectangle(10, 490, 10, 30), ColorConstants.BLACK));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath);
    }

    @Test
    public void cleanStrikeOutAnnotation02() throws IOException, InterruptedException {
        String input = inputPath + "strikeOutAnnotQuadOutsideRect.pdf";
        String output = outputPath + "cleanStrikeOutAnnotation02.pdf";
        String cmp = inputPath + "cmp_cleanStrikeOutAnnotation02.pdf";
        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();

        cleanUpLocations.add(new PdfCleanUpLocation(1, new Rectangle(0, 0, 200, 200), ColorConstants.BLACK));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath);
    }

    @Test
    public void cleanFreeTextAnnotation01() throws IOException, InterruptedException {
        String input = inputPath + "freeTextAnnotation.pdf";
        String output = outputPath + "cleanFreeTextAnnotation01.pdf";
        String cmp = inputPath + "cmp_cleanFreeTextAnnotation01.pdf";
        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();

        cleanUpLocations.add(new PdfCleanUpLocation(1, new Rectangle(100, 560, 200, 30), ColorConstants.BLACK));

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath);
    }

    @Test
    public void cleanFormAnnotations01() throws IOException, InterruptedException {
        String input = inputPath + "formAnnotation.pdf";
        String output = outputPath + "formAnnotation01.pdf";
        String cmp = inputPath + "cmp_formAnnotation01.pdf";
        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();

        PdfCleanUpLocation highLightLoc = new PdfCleanUpLocation(1, new Rectangle(20, 600, 500, 170), ColorConstants.YELLOW);
        cleanUpLocations.add(highLightLoc);

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_form01");
    }

    @Test
    public void cleanFormAnnotations02() throws IOException, InterruptedException {
        String input = inputPath + "formAnnotation.pdf";
        String output = outputPath + "formAnnotation02.pdf";
        String cmp = inputPath + "cmp_formAnnotation02.pdf";
        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();

        PdfCleanUpLocation highLightLoc = new PdfCleanUpLocation(1, new Rectangle(20, 600, 300, 100), ColorConstants.YELLOW);
        cleanUpLocations.add(highLightLoc);

        cleanUp(input, output, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff_form02");
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = CleanUpLogMessageConstant.REDACTION_OF_ANNOTATION_TYPE_WATERMARK_IS_NOT_SUPPORTED))
    // TODO: update cmp file after DEVSIX-2471 fix
    public void cleanWatermarkAnnotation() throws IOException, InterruptedException {
        String input = inputPath + "watermarkAnnotation.pdf";
        String output = outputPath + "watermarkAnnotation.pdf";
        String cmp = inputPath + "cmp_watermarkAnnotation.pdf";

        cleanUp(input, output, Collections.singletonList(new PdfCleanUpLocation(1,
                new Rectangle(410, 410, 50, 50), ColorConstants.YELLOW)));
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
        compareByContent(cmp, output, targetDir, null);
    }

    private void compareByContent(String cmp, String output, String targetDir, String diffPrefix) throws IOException, InterruptedException {
        CompareTool cmpTool = new CompareTool();
        String errorMessage = cmpTool.compareByContent(output, cmp, targetDir, diffPrefix + "_");

        if (errorMessage != null) {
            Assert.fail(errorMessage);
        }
    }
}
