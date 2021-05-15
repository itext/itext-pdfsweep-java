/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
    Authors: iText Software.

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

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.UnitTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class TextPositioningTest extends ExtendedITextTest {

    public static final float EPS = 0.0001F;

    @Test
    public void checkNoNpeThrowsInWritePositioningOperatorTest() throws IOException {
        PdfCanvas canvasForTest = createTestCanvas(1.0F);

        TextPositioning textPositioning = new TextPositioning();
        try {
            textPositioning.appendPositioningOperator("T*", new ArrayList<>());
            textPositioning
                    .writePositionedText("T*", new ArrayList<>(), new PdfArray(), canvasForTest);
        } catch (NullPointerException nullPointerException) {
            Assert.fail("We don't expect, that NPE will be thrown in this test!");
        }
        Assert.assertEquals(0.0, canvasForTest.getGraphicsState().getLeading(), EPS);
    }

    @Test
    public void checkNoNpeThrowsInWritePositioningTextTest() throws IOException {
        PdfCanvas canvasForTest = createTestCanvas(2.0F);

        TextPositioning textPositioning = new TextPositioning();
        try {
            textPositioning.appendPositioningOperator("'", new ArrayList<>());
            textPositioning.writePositionedText("'", new ArrayList<>(), new PdfArray(), canvasForTest);
        } catch (NullPointerException nullPointerException) {
            Assert.fail("We don't expect, that NPE will be thrown in this test!");
        }
        Assert.assertEquals(0.0, canvasForTest.getGraphicsState().getLeading(), EPS);
    }

    private static PdfCanvas createTestCanvas(float canvasLeading) throws IOException {
        PdfDocument document = new PdfDocument(new PdfWriter(new ByteArrayOutputStream()));
        PdfPage documentPage = document.addNewPage();
        PdfCanvas canvas = new PdfCanvas(documentPage);

        canvas.setLeading(canvasLeading);
        canvas.setFontAndSize(PdfFontFactory.createFont(StandardFonts.COURIER), 14);
        return canvas;
    }
}