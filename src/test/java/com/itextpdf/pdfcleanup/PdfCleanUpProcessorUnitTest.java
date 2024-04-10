/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
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

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfBoolean;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNull;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.canvas.CanvasTag;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.UnitTest;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class PdfCleanUpProcessorUnitTest extends ExtendedITextTest {

    @Test
    public void areColorSpacesDifferentForJavaNullValuesTest() {
        Assertions.assertFalse(createAndCompareImages(null, null));
    }

    @Test
    public void areColorSpacesDifferentForPdfNullAndJavaNullValuesTest() {
        Assertions.assertTrue(createAndCompareImages(new PdfNull(), null));
    }

    @Test
    public void areColorSpacesDifferentForPdfNullValuesTest() {
        Assertions.assertFalse(createAndCompareImages(new PdfNull(), new PdfNull()));
    }

    @Test
    public void areColorSpacesDifferentForNameAndJavaNullValuesTest() {
        Assertions.assertTrue(createAndCompareImages(PdfName.DeviceRGB, null));
    }

    @Test
    public void areColorSpacesDifferentForNameAndPdfNullValuesTest() {
        Assertions.assertTrue(createAndCompareImages(PdfName.DeviceRGB, new PdfNull()));
    }

    @Test
    public void areColorSpacesDifferentForArrayAndJavaNullValuesTest() {
        PdfArray pdfArray = createPdfArray(PdfName.Separation, new PdfNumber(1), new PdfStream());
        Assertions.assertTrue(createAndCompareImages(pdfArray, null));
    }

    @Test
    public void areColorSpacesDifferentForArrayAndPdfNullValuesTest() {
        PdfArray pdfArray = createPdfArray(PdfName.Separation, new PdfNumber(1), new PdfStream());
        Assertions.assertTrue(createAndCompareImages(pdfArray, new PdfNull()));
    }

    @Test
    public void areColorSpacesDifferentForEqualPdfNameValuesTest() {
        Assertions.assertFalse(createAndCompareImages(new PdfName("DeviceGray"), PdfName.DeviceGray));
    }

    @Test
    public void areColorSpacesDifferentForDifferentPdfNameValuesTest() {
        Assertions.assertTrue(createAndCompareImages(new PdfName("DeviceGray"), new PdfName("DeviceRGB")));
    }

    @Test
    public void areColorSpacesDifferentForTheSamePdfArraysValuesTest() {
        PdfArray pdfFirstArray = createPdfArray(PdfName.Separation, new PdfNumber(1), new PdfStream());
        Assertions.assertFalse(createAndCompareImages(pdfFirstArray, pdfFirstArray));
    }

    @Test
    public void areColorSpacesDifferentForPdfArraysWithStreamValuesTest() {
        PdfArray pdfFirstArray = createPdfArray(PdfName.Separation, new PdfNumber(1), new PdfStream());
        PdfArray pdfSecondArray = createPdfArray(PdfName.Separation, new PdfNumber(1), new PdfStream());
        Assertions.assertTrue(createAndCompareImages(pdfFirstArray, pdfSecondArray));
    }

    @Test
    public void areColorSpacesDifferentForEqualPdfArraysValuesTest() {
        PdfArray pdfFirstArray = createPdfArray(PdfName.Separation, new PdfNumber(1), new PdfBoolean(true));
        PdfArray pdfSecondArray = createPdfArray(PdfName.Separation, new PdfNumber(1), new PdfBoolean(true));
        Assertions.assertFalse(createAndCompareImages(pdfFirstArray, pdfSecondArray));
    }

    @Test
    public void areColorSpacesDifferentForEqualPdfArraysWithNullsValuesTest() {
        PdfArray pdfFirstArray = createPdfArray(PdfName.Separation, new PdfNull());
        PdfArray pdfSecondArray = createPdfArray(PdfName.Separation, new PdfNull());
        Assertions.assertFalse(createAndCompareImages(pdfFirstArray, pdfSecondArray));
    }

    @Test
    public void areColorSpacesDifferentForPdfArraysWithDifferentSizeValuesTest() {
        PdfArray pdfFirstArray = createPdfArray(PdfName.Separation, new PdfNumber(1), new PdfBoolean(true));
        PdfArray pdfSecondArray = createPdfArray(PdfName.Separation, new PdfNumber(1));
        Assertions.assertTrue(createAndCompareImages(pdfFirstArray, pdfSecondArray));
    }

    @Test
    public void areColorSpacesDifferentForPdfNameAndPdfArrayValuesTest() {
        PdfArray pdfArray = createPdfArray(PdfName.Separation);
        Assertions.assertTrue(createAndCompareImages(PdfName.Separation, pdfArray));
    }

    @Test
    public void areColorSpacesDifferentForPdfNullAndPdfArrayValuesTest() {
        PdfArray pdfArray = createPdfArray(PdfName.Separation);
        Assertions.assertTrue(createAndCompareImages(new PdfNull(), pdfArray));
    }

    @Test
    public void areColorSpacesDifferentForJavaNullAndPdfArrayValuesTest() {
        PdfArray pdfArray = createPdfArray(PdfName.Separation);
        Assertions.assertTrue(createAndCompareImages(null, pdfArray));
    }

    @Test
    public void openNotWrittenTagsUsualTest() {
        final Deque<CanvasTag> tags = new ArrayDeque<>(Arrays.asList(new CanvasTag(new PdfName("tag name1")),
                new CanvasTag(new PdfName("tag name2")), new CanvasTag(new PdfName("tag name3"))));
        testOpenNotWrittenTags(tags);
    }

    @Test
    public void openNotWrittenTagsEmptyTest() {
        testOpenNotWrittenTags(new ArrayDeque<CanvasTag>());
    }

    private void testOpenNotWrittenTags(final Deque<CanvasTag> tags) {
        PdfCleanUpProcessor processor = new PdfCleanUpProcessor(null, null) {
            @Override
            PdfCanvas getCanvas() {
                return new PdfCanvas(new PdfStream(), null, null) {
                    final Deque<CanvasTag> tagsToCompare = tags;

                    @Override
                    public PdfCanvas openTag(CanvasTag tag) {
                        Assertions.assertEquals(tagsToCompare.pop(), tag);
                        return null;
                    }
                };
            }
        };
        for (CanvasTag tag : tags) {
            processor.addNotWrittenTag(tag);
        }
        processor.openNotWrittenTags();
    }

    private static PdfArray createPdfArray(PdfObject... objects) {
        return new PdfArray(Arrays.asList(objects));
    }

    private static boolean createAndCompareImages(PdfObject firstCs, PdfObject secondCs) {
        PdfImageXObject firstImage = createImageWithCs(firstCs);
        PdfImageXObject secondImage = createImageWithCs(secondCs);
        boolean compareFirstToSecondResult = PdfCleanUpProcessor.areColorSpacesDifferent(firstImage, secondImage);
        boolean compareSecondToFirstResult = PdfCleanUpProcessor.areColorSpacesDifferent(secondImage, firstImage);
        if (compareFirstToSecondResult != compareSecondToFirstResult) {
            throw new IllegalStateException("The comparing of CS shall be a commutative operation.");
        }
        return compareFirstToSecondResult;
    }

    private static PdfImageXObject createImageWithCs(PdfObject cs) {
        PdfStream stream = new PdfStream();
        stream.put(PdfName.Type, PdfName.XObject);
        stream.put(PdfName.Subtype, PdfName.Image);
        if (cs != null) {
            stream.put(PdfName.ColorSpace, cs);
        }
        return new PdfImageXObject(stream);
    }
}
