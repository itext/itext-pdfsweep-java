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

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.pdfcleanup.util.CleanUpImageUtil;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

@Category(IntegrationTest.class)
public class CleanUpImageUtilTest extends ExtendedITextTest {
    private static final String SOURCE_PATH = "./src/test/resources/com/itextpdf/pdfcleanup/CleanupImageHandlingUtilTest/";

    @Rule
    public ExpectedException junitExpectedException = ExpectedException.none();

    @Test
    public void cleanUpImageNullImageBytesTest() {
        List<Rectangle> areasToBeCleaned = new ArrayList<>();
        areasToBeCleaned.add(new Rectangle(100, 100));
        junitExpectedException.expect(RuntimeException.class);

        CleanUpImageUtil.cleanUpImage(null, areasToBeCleaned);
    }

    @Test
    public void cleanUpImageEmptyAreasToCleanTest() throws Exception {
        ImageData data = ImageDataFactory.create(SOURCE_PATH + "cleanUpImageEmptyAreasToClean.png");
        PdfImageXObject imageXObject = new PdfImageXObject(data);
        byte[] sourceImageBytes = imageXObject.getImageBytes();

        byte[] resultImageBytes = CleanUpImageUtil.cleanUpImage(new PdfImageXObject(data).getImageBytes(),
                new ArrayList<Rectangle>());

        Assert.assertArrayEquals(sourceImageBytes, resultImageBytes);
    }
}
