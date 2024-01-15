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

import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.pdfcleanup.logs.CleanUpLogMessageConstant;
import com.itextpdf.pdfcleanup.util.CleanUpCsCompareUtil;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class CleanUpCsCompareUtilTest extends ExtendedITextTest {
    @Test
    public void differentImageBitsPerPixelTest() {
        PdfImageXObject image1 = createMockedPdfImageXObject(PdfName.DeviceRGB, 8);
        PdfImageXObject image2 = createMockedPdfImageXObject(PdfName.DeviceRGB, 16);

        Assert.assertFalse(CleanUpCsCompareUtil.isOriginalCsCompatible(image1, image2));
    }

    @Test
    public void differentImageColorTypeTest() {
        PdfImageXObject image1 = createMockedPdfImageXObject(PdfName.DeviceRGB, 8);
        PdfImageXObject image2 = createMockedPdfImageXObject(PdfName.DeviceGray, 8);

        Assert.assertFalse(CleanUpCsCompareUtil.isOriginalCsCompatible(image1, image2));
    }

    @Test
    public void imagesCsApplicableTest() {
        PdfImageXObject image1 = createMockedPdfImageXObject(PdfName.DeviceGray, 8);
        PdfImageXObject image2 = createMockedPdfImageXObject(PdfName.DeviceGray, 8);

        Assert.assertTrue(CleanUpCsCompareUtil.isOriginalCsCompatible(image1, image2));
    }

    @Test
    public void imagesNotCsApplicableTest() {
        PdfImageXObject image1 = createMockedPdfImageXObject(PdfName.DeviceGray, 8);
        PdfImageXObject image2 = createMockedPdfImageXObject(PdfName.DeviceGray, 16);

        Assert.assertFalse(CleanUpCsCompareUtil.isOriginalCsCompatible(image1, image2));
    }

    @Test
    @LogMessages(messages = @LogMessage(messageTemplate = CleanUpLogMessageConstant.CANNOT_OBTAIN_IMAGE_INFO_AFTER_FILTERING))
    public void imageReadExceptionTest() {
        PdfStream stream1 = new PdfStream();
        stream1.put(PdfName.BitsPerComponent, new PdfNumber(8));
        stream1.put(PdfName.ColorSpace, PdfName.DeviceCMYK);
        PdfImageXObject image1 = new PdfImageXObject(stream1);

        stream1 = new PdfStream();
        stream1.put(PdfName.BitsPerComponent, new PdfNumber(8));
        stream1.put(PdfName.ColorSpace, PdfName.DeviceCMYK);
        PdfImageXObject image2 = new PdfImageXObject(stream1);

        Assert.assertFalse(CleanUpCsCompareUtil.isOriginalCsCompatible(image1, image2));
    }

    private PdfImageXObject createMockedPdfImageXObject(PdfName colorSpace, int bitsPerComponent) {
        PdfStream stream1 = new PdfStream();
        stream1.put(PdfName.BitsPerComponent, new PdfNumber(bitsPerComponent));
        stream1.put(PdfName.ColorSpace, colorSpace);
        stream1.put(PdfName.Width, new PdfNumber(1));
        stream1.put(PdfName.Height, new PdfNumber(1));
        return new PdfImageXObject(stream1);
    }
}
