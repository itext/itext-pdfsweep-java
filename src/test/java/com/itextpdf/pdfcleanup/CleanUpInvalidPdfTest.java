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

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.kernel.exceptions.KernelExceptionMessageConstant;
import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.test.ExtendedITextTest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("IntegrationTest")
public class CleanUpInvalidPdfTest extends ExtendedITextTest {
    private static final String inputPath = "./src/test/resources/com/itextpdf/pdfcleanup/CleanUpInvalidPdfTest/";
    private static final String outputPath = "./target/test/com/itextpdf/pdfcleanup/CleanUpInvalidPdfTest/";


    @BeforeAll
    public static void before() {
        createOrClearDestinationFolder(outputPath);
    }

    @Test
    public void cleanCircularReferencesInResourcesTest() throws IOException {
        String input = inputPath + "circularReferencesInResources.pdf";

        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(input), new PdfWriter(new ByteArrayOutputStream()))) {
            List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
            cleanUpLocations.add(new PdfCleanUpLocation(1, pdfDoc.getPage(1).getPageSize(), null));

            Exception exception = Assertions.assertThrows(PdfException.class,
                    () -> PdfCleaner.cleanUp(pdfDoc, cleanUpLocations));
            Assertions.assertEquals(MessageFormatUtil.format(
                            KernelExceptionMessageConstant.FORM_XOBJECT_HAS_CIRCULAR_REFERENCES, 12, 0),
                    exception.getMessage());
        }
    }
}
