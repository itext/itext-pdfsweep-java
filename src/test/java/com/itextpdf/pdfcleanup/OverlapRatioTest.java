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

import com.itextpdf.io.util.DecimalFormatUtil;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.ExtendedITextTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Tag("IntegrationTest")
public class OverlapRatioTest extends ExtendedITextTest {

    private static final String inputPath = "./src/test/resources/com/itextpdf/pdfcleanup/OverlapRatioTest/";
    private static final String outputPath = "./target/test/com/itextpdf/pdfcleanup/OverlapRatioTest/";

    @BeforeAll
    public static void before() {
        createOrClearDestinationFolder(outputPath);
    }

    private static final double[][] coordinates = {
            //Areas with small line spacing
            {1, 149, 700, 63.75, 10.75},
            {1, 149, 640, 63.75, 10.75},
            {1, 149, 520, 163.75, 50.75},
            //Areas with big line spacing
            {1, 149, 374, 63.75, 10.75},
            {1, 149, 310, 63.75, 10.75},
            {1, 149, 120, 163.75, 50.75}
    };

    @Test
    public void extractionWithoutSettingOverlapRatio() throws IOException, InterruptedException {
        String inputFile = inputPath + "redact_aspect_ratio_simple.pdf";
        String targetFile = outputPath + "wo_redact_aspect_ratio_simple_redact.pdf";
        String cmpFile = inputPath + "cmp_wo_redact_aspect_ratio_simple.pdf";

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(inputFile), new PdfWriter(targetFile));
        CleanUpProperties properties = new CleanUpProperties();
        PdfCleaner.cleanUp(pdfDoc, convertCleanupLocations(), properties);

        pdfDoc.close();

        CompareTool cmpTool = new CompareTool();
        String errorMessage = cmpTool.compareByContent(targetFile, cmpFile, outputPath, "diff_");
        Assertions.assertNull(errorMessage);
    }

    @Test
    public void extractionWithSettingOverlapRatio() throws IOException, InterruptedException {
        String inputFile = inputPath + "redact_aspect_ratio_simple.pdf";
        String targetFile = outputPath + "redact_aspect_ratio_simple_redact.pdf";
        String cmpFile = inputPath + "cmp_redact_aspect_ratio_simple.pdf";

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(inputFile), new PdfWriter(targetFile));


        CleanUpProperties properties = new CleanUpProperties();
        properties.setOverlapRatio(0.35);

        PdfCleaner.cleanUp(pdfDoc, convertCleanupLocations(), properties);
        pdfDoc.close();

        CompareTool cmpTool = new CompareTool();
        String errorMessage = cmpTool.compareByContent(targetFile, cmpFile, outputPath, "diff_");
        Assertions.assertNull(errorMessage);
    }

    @Test
    public void extractionWithSettingOverlapRatioCloseTo0() throws IOException, InterruptedException {
        //In this test we expect it to behave as normal that everything that gets touched by the redaction \
        //area should be redacted.
        String inputFile = inputPath + "redact_aspect_ratio_simple.pdf";
        String targetFile = outputPath + "redact_aspect_ratio_0_simple_redact.pdf";
        String cmpFile = inputPath + "cmp_redact_aspect_ratio_0_simple.pdf";

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(inputFile), new PdfWriter(targetFile));

        CleanUpProperties properties = new CleanUpProperties();
        properties.setOverlapRatio(0.0001);

        PdfCleaner.cleanUp(pdfDoc, convertCleanupLocations(), properties);
        pdfDoc.close();

        CompareTool cmpTool = new CompareTool();
        String errorMessage = cmpTool.compareByContent(targetFile, cmpFile, outputPath, "diff_");
        Assertions.assertNull(errorMessage);
    }

    @Test
    public void extractionWithSettingOverlapRatio1() throws IOException, InterruptedException {
        //In this sample we expect nothing to be redacted because of none of the items actually overlaps all of it.
        String inputFile = inputPath + "redact_aspect_ratio_simple.pdf";
        String targetFile = outputPath + "redact_aspect_ratio_1_simple_redact.pdf";
        String cmpFile = inputPath + "cmp_redact_aspect_ratio_1_simple.pdf";

        PdfDocument pdfDoc = new PdfDocument(new PdfReader(inputFile), new PdfWriter(targetFile));

        CleanUpProperties properties = new CleanUpProperties();
        properties.setOverlapRatio(1d);

        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<>();
        cleanUpLocations.add(new PdfCleanUpLocation(1, new Rectangle(20, 690, 263.75f, 40), ColorConstants.YELLOW));
        PdfCleaner.cleanUp(pdfDoc, cleanUpLocations, properties);
        pdfDoc.close();

        CompareTool cmpTool = new CompareTool();
        String errorMessage = cmpTool.compareByContent(targetFile, cmpFile, outputPath, "diff_");
        Assertions.assertNull(errorMessage);
    }

    @Test
    public void differentTextRenderInfo() throws IOException, InterruptedException {
        final String inputFile = inputPath + "differentTextRenderInfo.pdf";
        final Double[] ratioArray = new Double[] {0d, 0.001, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1d};
        final Rectangle r = new Rectangle(110, 700, 400, 130);

        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
        PdfCleanUpLocation location = new PdfCleanUpLocation(1, r);
        cleanUpLocations.add(location);
        CleanUpProperties cleanUpProperties = new CleanUpProperties();

        for (Double ratio : ratioArray) {
            final String targetFile = outputPath + "differentTextRenderInfo_" +
                    DecimalFormatUtil.formatNumber(ratio.doubleValue(), "#.000#") + "_redact.pdf";
            final String cmpFile = inputPath + "cmp_differentTextRenderInfo_" +
                    DecimalFormatUtil.formatNumber(ratio.doubleValue(), "#.000#") + "_redact.pdf";

            try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(inputFile), new PdfWriter(targetFile))) {
                if (ratio == 0d) {
                    cleanUpProperties.setOverlapRatio(null);
                } else {
                    cleanUpProperties.setOverlapRatio(ratio);
                }
                PdfCleaner.cleanUp(pdfDoc, cleanUpLocations, cleanUpProperties);

                // Draw a rectangle to visualize the cleanup
                PdfCanvas pdfCanvas = new PdfCanvas(pdfDoc.getPage(1));
                pdfCanvas.setStrokeColor(ColorConstants.RED).rectangle(r).stroke();
            }

            Assertions.assertNull(new CompareTool().compareByContent(targetFile, cmpFile, outputPath, "diff_"));
        }
    }

    private static List<PdfCleanUpLocation> convertCleanupLocations() {
        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<>();
        for (double[] coord : coordinates) {
            int pageNumber = (int) coord[0];
            double x = coord[1];
            double y = coord[2];
            double width = coord[3];
            double height = coord[4];
            PdfCleanUpLocation location = new PdfCleanUpLocation(pageNumber, new Rectangle((float) x, (float) y, (float) width, (float) height), ColorConstants.BLACK);
            cleanUpLocations.add(location);
        }
        return cleanUpLocations;
    }
}
