/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
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

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.CharacterRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IPdfTextLocation;
import com.itextpdf.kernel.pdf.canvas.parser.listener.RegexBasedLocationExtractionStrategy;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfcleanup.autosweep.CompositeCleanupStrategy;
import com.itextpdf.pdfcleanup.autosweep.ICleanupStrategy;
import com.itextpdf.pdfcleanup.autosweep.RegexBasedCleanupStrategy;
import com.itextpdf.pdfcleanup.util.CleanUpImagesCompareTool;
import com.itextpdf.test.ExtendedITextTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag("IntegrationTest")
public class BigDocumentAutoCleanUpTest extends ExtendedITextTest {

    private static final String inputPath = "./src/test/resources/com/itextpdf/pdfcleanup/BigDocumentAutoCleanUpTest/";
    private static final String outputPath = "./target/test/com/itextpdf/pdfcleanup/BigDocumentAutoCleanUpTest/";

    @BeforeAll
    public static void before() {
        createOrClearDestinationFolder(outputPath);
    }

    @Test
    public void redactTonySoprano() throws IOException, InterruptedException {
        String input = inputPath + "TheSopranos.pdf";
        String output = outputPath + "redactTonySoprano.pdf";
        String cmp = inputPath + "cmp_redactTonySoprano.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("Tony( |_)Soprano"));
        strategy.add(new RegexBasedCleanupStrategy("Soprano"));
        strategy.add(new RegexBasedCleanupStrategy("Sopranos"));

        PdfWriter writer = new PdfWriter(output);
        writer.setCompressionLevel(0);
        PdfDocument pdf = new PdfDocument(new PdfReader(input), writer);

        // sweep
        PdfCleaner.autoSweepCleanUp(pdf, strategy);

        pdf.close();

        // compare
        compareResults(cmp, output, outputPath, "4");
    }

    @Test
    public void cleanUpAreaCalculationPrecisionTest() throws IOException, InterruptedException {
        String input = inputPath + "cleanUpAreaCalculationPrecision.pdf";
        String output = outputPath + "cleanUpAreaCalculationPrecision.pdf";
        String cmp = inputPath + "cmp_cleanUpAreaCalculationPrecision.pdf";
        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new CustomLocationExtractionStrategy("(iphone)|(iPhone)"));
        PdfDocument pdf = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        // sweep
        PdfCleaner.autoSweepCleanUp(pdf, strategy);

        pdf.close();

        // compare
        CompareTool compareTool = new CompareTool();
        String errorMessage = compareTool.compareByContent(output, cmp, outputPath);
        Assertions.assertNull(errorMessage);
    }

    @Test
    public void redactIPhoneUserManualMatchColor() throws IOException, InterruptedException {
        String input = inputPath + "iphone_user_guide_untagged.pdf";
        String output = outputPath + "redactIPhoneUserManualMatchColor.pdf";
        String cmp = inputPath + "cmp_redactIPhoneUserManualMatchColor.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new CustomLocationExtractionStrategy("(iphone)|(iPhone)"));

        PdfDocument pdf = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        // sweep
        PdfCleaner.autoSweepCleanUp(pdf, strategy);

        pdf.close();

        // compare
        CleanUpImagesCompareTool cmpTool = new CleanUpImagesCompareTool();
        String errorMessage = cmpTool.extractAndCompareImages(output,
                cmp, outputPath, "4");

        // TODO DEVSIX-4047 Switch to compareByContent() when the ticket will be resolved
        String compareByContentResult = cmpTool.compareVisually(output, cmp, outputPath,
                "diff_redactIPhoneUserManualMatchColor_", cmpTool.getIgnoredImagesAreas());
        if (compareByContentResult != null) {
            errorMessage += compareByContentResult;
        }

        if (!errorMessage.equals("")) {
            Assertions.fail(errorMessage);
        }
    }

    @Test
    public void redactIPhoneUserManual() throws IOException, InterruptedException {
        String input = inputPath + "iphone_user_guide_untagged.pdf";
        String output = outputPath + "redactIPhoneUserManual.pdf";
        String cmp = inputPath + "cmp_redactIPhoneUserManual.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("(iphone)|(iPhone)"));

        PdfDocument pdf = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        // sweep
        PdfCleaner.autoSweepCleanUp(pdf, strategy);

        pdf.close();

        // compare
        CleanUpImagesCompareTool cmpTool = new CleanUpImagesCompareTool();
        String errorMessage = cmpTool.extractAndCompareImages(output,
                cmp, outputPath, "4");

        // TODO DEVSIX-4047 Switch to compareByContent() when the ticket will be resolved
        String compareByContentResult = cmpTool.compareVisually(output, cmp, outputPath,
                "diff_redactIPhoneUserManual_", cmpTool.getIgnoredImagesAreas());
        if (compareByContentResult != null) {
            errorMessage += compareByContentResult;
        }

        if (!errorMessage.equals("")) {
            Assertions.fail(errorMessage);
        }
    }

    @Test
    public void redactIPhoneUserManualColored() throws IOException, InterruptedException {
        String input = inputPath + "iphone_user_guide_untagged_small.pdf";
        String output = outputPath + "redactIPhoneUserManualColored.pdf";
        String cmp = inputPath + "cmp_redactIPhoneUserManualColored.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("(iphone)|(iPhone)").setRedactionColor(ColorConstants.GREEN));

        PdfDocument pdf = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        // sweep
        PdfCleaner.autoSweepCleanUp(pdf, strategy);

        pdf.close();

        compareResults(cmp, output, outputPath, "4");
    }

    private void compareResults(String cmp, String output, String targetDir, String fuzzValue)
            throws IOException, InterruptedException {
        CleanUpImagesCompareTool cmpTool = new CleanUpImagesCompareTool();
        String errorMessage = cmpTool.extractAndCompareImages(output,
                cmp, targetDir, fuzzValue);
        String compareByContentResult = cmpTool.compareByContent(output, cmp, targetDir);
        if (compareByContentResult != null) {
            errorMessage += compareByContentResult;
        }

        if (!errorMessage.equals("")) {
            Assertions.fail(errorMessage);
        }
    }
}

/*
 * color matching text redaction
 */

class CCharacterRenderInfo extends CharacterRenderInfo {

    private Color strokeColor;
    private Color fillColor;

    public CCharacterRenderInfo(TextRenderInfo tri) {
        super(tri);
        this.strokeColor = tri.getStrokeColor();
        this.fillColor = tri.getFillColor();
    }

    public Color getStrokeColor() {
        return strokeColor;
    }

    public Color getFillColor() {
        return fillColor;
    }
}

class CustomLocationExtractionStrategy extends RegexBasedLocationExtractionStrategy implements ICleanupStrategy {

    private String regex;
    private Map<Rectangle, Color> colorByRectangle = new HashMap<>();

    public CustomLocationExtractionStrategy(String regex) {
        super(regex);
        this.regex = regex;
    }

    @Override
    public List<CharacterRenderInfo> toCRI(TextRenderInfo tri) {
        List<CharacterRenderInfo> cris = new ArrayList<>();
        for (TextRenderInfo subTri : tri.getCharacterRenderInfos()) {
            cris.add(new CCharacterRenderInfo(subTri));
        }
        return cris;
    }

    @Override
    public List<Rectangle> toRectangles(List<CharacterRenderInfo> cris) {
        Color col = ((CCharacterRenderInfo) cris.get(0)).getFillColor();
        List<Rectangle> rects = new ArrayList<>(super.toRectangles(cris));
        for (Rectangle rect : rects) {
            colorByRectangle.put(rect, col);
        }
        return rects;
    }

    @Override
    public Color getRedactionColor(IPdfTextLocation rect) {
        return colorByRectangle.containsKey(rect.getRectangle()) ? colorByRectangle.get(rect.getRectangle()) : ColorConstants.BLACK;
    }

    @Override
    public ICleanupStrategy reset() {
        return new CustomLocationExtractionStrategy(regex);
    }
}
