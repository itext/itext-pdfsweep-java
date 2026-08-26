/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2026 Apryse Group NV
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

import com.itextpdf.commons.utils.FileUtil;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.ExtendedITextTest;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class TaggedDocumentTest extends ExtendedITextTest {
    private static final String SOURCE_FOLDER = "./src/test/resources/com/itextpdf/pdfcleanup/TaggedDocumentTest/";
    private static final String DESTINATION_FOLDER = "./target/test/com/itextpdf/pdfcleanup/TaggedDocumentTest/";

    @BeforeAll
    public static void beforeClass()
    {
        createOrClearDestinationFolder(DESTINATION_FOLDER);
    }

    @Test
    public void canvasSimpleMcTreeRedactChildTest() throws IOException, InterruptedException {
        String inPdf = SOURCE_FOLDER + "canvasSimpleMcTree.pdf";
        String outPdf = DESTINATION_FOLDER + "canvasSimpleMcTreeRedactChild.pdf";
        String cmpPdf = SOURCE_FOLDER + "cmp_canvasSimpleMcTreeRedactChild.pdf";
        cleanup(inPdf, outPdf, cmpPdf, new Rectangle(300, 300, 100, 300));
    }

    @Test
    public void canvasSimpleMcTreeRedactParentTest() throws IOException, InterruptedException {
        String inPdf = SOURCE_FOLDER + "canvasSimpleMcTree.pdf";
        String outPdf = DESTINATION_FOLDER + "canvasSimpleMcTreeRedactParent.pdf";
        String cmpPdf = SOURCE_FOLDER + "cmp_canvasSimpleMcTreeRedactParent.pdf";

        cleanup(inPdf, outPdf, cmpPdf, new Rectangle(100, 300, 100, 300));
    }

    @Test
    public void canvasImagesMcTreeRedactImageTest() throws IOException, InterruptedException {
        String inPdf = SOURCE_FOLDER + "canvasImagesMcTree.pdf";
        String outPdf = DESTINATION_FOLDER + "canvasImagesMcTreeRedactImage.pdf";
        String cmpPdf = SOURCE_FOLDER + "cmp_canvasImagesMcTreeRedactImage.pdf";

        cleanup(inPdf, outPdf, cmpPdf, new Rectangle(40, 435, 5, 5));
    }

    @Test
    public void canvasImagesMcTreeRedactFullImageTest() throws IOException, InterruptedException {
        String inPdf = SOURCE_FOLDER + "canvasImagesMcTree.pdf";
        String outPdf = DESTINATION_FOLDER + "canvasImagesMcTreeRedactFullImage.pdf";
        String cmpPdf = SOURCE_FOLDER + "cmp_canvasImagesMcTreeRedactFullImage.pdf";

        cleanup(inPdf, outPdf, cmpPdf, new Rectangle(32, 425, 50, 50));
    }

    @Test
    public void canvasImagesMcTreeRedactInlineImageTest() throws IOException, InterruptedException {
        String inPdf = SOURCE_FOLDER + "canvasImagesMcTree.pdf";
        String outPdf = DESTINATION_FOLDER + "canvasImagesMcTreeRedactInlineImage.pdf";
        String cmpPdf = SOURCE_FOLDER + "cmp_canvasImagesMcTreeRedactInlineImage.pdf";

        cleanup(inPdf, outPdf, cmpPdf, new Rectangle(100, 435, 5, 5));
    }

    @Test
    public void formXobjectImagesMcTreeTest() throws IOException, InterruptedException {
        String inPdf = SOURCE_FOLDER + "formXobjectImagesMcTree.pdf";
        String outPdf = DESTINATION_FOLDER + "formXobjectImagesMcTree.pdf";
        String cmpPdf = SOURCE_FOLDER + "cmp_formXobjectImagesMcTree.pdf";

        cleanup(inPdf, outPdf, cmpPdf, new Rectangle(40, 435, 5, 5));
    }

    @Test
    public void canvasTagFormXobjectImagesMcTreeTest() throws IOException, InterruptedException {
        String inPdf = SOURCE_FOLDER + "canvasTagFormXobjectImagesMcTree.pdf";
        String outPdf = DESTINATION_FOLDER + "canvasTagFormXobjectImagesMcTree.pdf";
        String cmpPdf = SOURCE_FOLDER + "cmp_canvasTagFormXobjectImagesMcTree.pdf";

        cleanup(inPdf, outPdf, cmpPdf, new Rectangle(100, 435, 5, 5));
    }

    @Test
    public void canvasMcTreeRedactFillPathTest() throws IOException, InterruptedException {
        String inPdf = SOURCE_FOLDER + "canvasPathsMcTree.pdf";
        String outPdf = DESTINATION_FOLDER + "canvasMcTreeRedactFillPath.pdf";
        String cmpPdf = SOURCE_FOLDER + "cmp_canvasMcTreeRedactFillPath.pdf";

        cleanup(inPdf, outPdf, cmpPdf, new Rectangle(160, 435, 5, 5));
    }

    @Test
    public void canvasMcTreeRedactStrokePathTest() throws IOException, InterruptedException {
        String inPdf = SOURCE_FOLDER + "canvasPathsMcTree.pdf";
        String outPdf = DESTINATION_FOLDER + "canvasMcTreeRedactStrokePath.pdf";
        String cmpPdf = SOURCE_FOLDER + "cmp_canvasMcTreeRedactStrokePath.pdf";

        cleanup(inPdf, outPdf, cmpPdf, new Rectangle(220, 435, 5, 40));
    }

    @Test
    public void canvasMcTreeRedactClipPathTest() throws IOException, InterruptedException {
        String inPdf = SOURCE_FOLDER + "canvasPathsMcTree.pdf";
        String outPdf = DESTINATION_FOLDER + "canvasMcTreeRedactClipPath.pdf";
        String cmpPdf = SOURCE_FOLDER + "cmp_canvasMcTreeRedactClipPath.pdf";

        cleanup(inPdf, outPdf, cmpPdf, new Rectangle(284, 430, 2, 2));
    }

    @Test
    public void canvasMcTreeRedactClipPath2Test() throws IOException, InterruptedException {
        String inPdf = SOURCE_FOLDER + "canvasPathsMcTree.pdf";
        String outPdf = DESTINATION_FOLDER + "canvasMcTreeRedactClipPath2.pdf";
        String cmpPdf = SOURCE_FOLDER + "cmp_canvasMcTreeRedactClipPath2.pdf";

        cleanup(inPdf, outPdf, cmpPdf, new Rectangle(380, 515, 5, 5));
    }

    @Test
    public void structTreeRootTest() throws IOException, InterruptedException {
        String inPdf = SOURCE_FOLDER + "structTreeRoot.pdf";
        String outPdf = DESTINATION_FOLDER + "structTreeRoot.pdf";
        String cmpPdf = SOURCE_FOLDER + "cmp_structTreeRoot.pdf";

        cleanup(inPdf, outPdf, cmpPdf, new Rectangle(36, 735, 180, 40));
    }

    private static void cleanup(String input, String output, String cmp, Rectangle cleanupArea)
            throws IOException, InterruptedException {
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(input), new PdfWriter(output))) {
            PdfCleanUpTool workingTool = new PdfCleanUpTool(pdfDoc);
            workingTool.addCleanupLocation(new PdfCleanUpLocation(1, cleanupArea, ColorConstants.BLACK));
            workingTool.cleanUp();
        }

        String diff = new CompareTool().compareByContent(output, cmp, DESTINATION_FOLDER);
        if (diff != null) {
            String cmp2;
            int lastDot = cmp.lastIndexOf('.');
            if (lastDot <= 0) {
                cmp2 = cmp + "_2";
            } else {
                String base = cmp.substring(0, lastDot);
                String ext = cmp.substring(lastDot);
                cmp2 = base + "_2" + ext;
            }
            if (FileUtil.fileExists(cmp2)) {
                // Second cmp is required on .NET because cleanup if inline images differs per system
                diff = new CompareTool().compareByContent(output, cmp2, DESTINATION_FOLDER);
            }
        }

        Assertions.assertNull(diff);
    }
}
