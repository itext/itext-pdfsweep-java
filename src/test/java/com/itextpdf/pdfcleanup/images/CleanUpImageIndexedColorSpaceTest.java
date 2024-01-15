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
package com.itextpdf.pdfcleanup.images;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.pdfcleanup.PdfCleaner;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.util.Arrays;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class CleanUpImageIndexedColorSpaceTest extends ExtendedITextTest {

    private static final String inputPath = "./src/test/resources/com/itextpdf/pdfcleanup/images/CleanUpImageIndexedColorSpaceTest/";
    private static final String outputPath = "./target/test/com/itextpdf/pdfcleanup/images/CleanUpImageIndexedColorSpaceTest/";

    @BeforeClass
    public static void before() {
        createOrClearDestinationFolder(outputPath);
    }

    @Test
    public void noWhiteColorTest() throws Exception {
        String input = inputPath + "indexedImageNoWhite.pdf";
        String output = outputPath + "indexedImageNoWhite.pdf";
        String cmp = inputPath + "cmp_indexedImageNoWhite.pdf";

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output))) {
            PdfCleaner.cleanUp(pdfDocument,
                    Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(150, 250, 100, 100)))
            );
        }

        /*
          Result in Java and .NET is different.

          Java is able to process images with indexed colorspace same as others and
          doesn't preserve indexed colorspace. .NET requires special processing for
          indexed colorspace images, but preserves indexed colorspace.

          In .NET color of cleaned area is the first color of indexed color palette.
          In Java color of cleaned area is white.
         */
        Assert.assertNull(new CompareTool().compareByContent(output, cmp, outputPath));
    }
}
