package com.itextpdf.pdfcleanup;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfBoolean;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNull;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.kernel.pdf.xobject.PdfXObject;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.UnitTest;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import static org.junit.Assert.*;

@Category(UnitTest.class)
public class PdfCleanUpProcessorUnitTest extends ExtendedITextTest {

    @Test
    public void areColorSpacesDifferentForJavaNullValuesTest() {
        Assert.assertFalse(createAndCompareImages(null, null));
    }

    @Test
    public void areColorSpacesDifferentForPdfNullAndJavaNullValuesTest() {
        Assert.assertTrue(createAndCompareImages(new PdfNull(), null));
    }

    @Test
    public void areColorSpacesDifferentForPdfNullValuesTest() {
        Assert.assertFalse(createAndCompareImages(new PdfNull(), new PdfNull()));
    }

    @Test
    public void areColorSpacesDifferentForNameAndJavaNullValuesTest() {
        Assert.assertTrue(createAndCompareImages(PdfName.DeviceRGB, null));
    }

    @Test
    public void areColorSpacesDifferentForNameAndPdfNullValuesTest() {
        Assert.assertTrue(createAndCompareImages(PdfName.DeviceRGB, new PdfNull()));
    }

    @Test
    public void areColorSpacesDifferentForArrayAndJavaNullValuesTest() {
        PdfArray pdfArray = createPdfArray(PdfName.Separation, new PdfNumber(1), new PdfStream());
        Assert.assertTrue(createAndCompareImages(pdfArray, null));
    }

    @Test
    public void areColorSpacesDifferentForArrayAndPdfNullValuesTest() {
        PdfArray pdfArray = createPdfArray(PdfName.Separation, new PdfNumber(1), new PdfStream());
        Assert.assertTrue(createAndCompareImages(pdfArray, new PdfNull()));
    }

    @Test
    public void areColorSpacesDifferentForEqualPdfNameValuesTest() {
        Assert.assertFalse(createAndCompareImages(new PdfName("DeviceGray"), PdfName.DeviceGray));
    }

    @Test
    public void areColorSpacesDifferentForDifferentPdfNameValuesTest() {
        Assert.assertTrue(createAndCompareImages(new PdfName("DeviceGray"), new PdfName("DeviceRGB")));
    }

    @Test
    public void areColorSpacesDifferentForTheSamePdfArraysValuesTest() {
        PdfArray pdfFirstArray = createPdfArray(PdfName.Separation, new PdfNumber(1), new PdfStream());
        Assert.assertFalse(createAndCompareImages(pdfFirstArray, pdfFirstArray));
    }

    @Test
    public void areColorSpacesDifferentForPdfArraysWithStreamValuesTest() {
        PdfArray pdfFirstArray = createPdfArray(PdfName.Separation, new PdfNumber(1), new PdfStream());
        PdfArray pdfSecondArray = createPdfArray(PdfName.Separation, new PdfNumber(1), new PdfStream());
        Assert.assertTrue(createAndCompareImages(pdfFirstArray, pdfSecondArray));
    }

    @Test
    public void areColorSpacesDifferentForEqualPdfArraysValuesTest() {
        PdfArray pdfFirstArray = createPdfArray(PdfName.Separation, new PdfNumber(1), new PdfBoolean(true));
        PdfArray pdfSecondArray = createPdfArray(PdfName.Separation, new PdfNumber(1), new PdfBoolean(true));
        Assert.assertFalse(createAndCompareImages(pdfFirstArray, pdfSecondArray));
    }

    @Test
    public void areColorSpacesDifferentForEqualPdfArraysWithNullsValuesTest() {
        PdfArray pdfFirstArray = createPdfArray(PdfName.Separation, new PdfNull());
        PdfArray pdfSecondArray = createPdfArray(PdfName.Separation, new PdfNull());
        Assert.assertFalse(createAndCompareImages(pdfFirstArray, pdfSecondArray));
    }

    @Test
    public void areColorSpacesDifferentForPdfArraysWithDifferentSizeValuesTest() {
        PdfArray pdfFirstArray = createPdfArray(PdfName.Separation, new PdfNumber(1), new PdfBoolean(true));
        PdfArray pdfSecondArray = createPdfArray(PdfName.Separation, new PdfNumber(1));
        Assert.assertTrue(createAndCompareImages(pdfFirstArray, pdfSecondArray));
    }

    @Test
    public void areColorSpacesDifferentForPdfNameAndPdfArrayValuesTest() {
        PdfArray pdfArray = createPdfArray(PdfName.Separation);
        Assert.assertTrue(createAndCompareImages(PdfName.Separation, pdfArray));
    }

    @Test
    public void areColorSpacesDifferentForPdfNullAndPdfArrayValuesTest() {
        PdfArray pdfArray = createPdfArray(PdfName.Separation);
        Assert.assertTrue(createAndCompareImages(new PdfNull(), pdfArray));
    }

    @Test
    public void areColorSpacesDifferentForJavaNullAndPdfArrayValuesTest() {
        PdfArray pdfArray = createPdfArray(PdfName.Separation);
        Assert.assertTrue(createAndCompareImages(null, pdfArray));
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
