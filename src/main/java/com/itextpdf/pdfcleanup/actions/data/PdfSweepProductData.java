/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
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
package com.itextpdf.pdfcleanup.actions.data;

import com.itextpdf.commons.actions.data.ProductData;

/**
 * Stores an instance of {@link ProductData} related to iText pdfSweep module.
 */
public class PdfSweepProductData {
    public static final String PDF_SWEEP_PRODUCT_NAME = "pdfSweep";
    public static final String PDF_SWEEP_PUBLIC_PRODUCT_NAME = PDF_SWEEP_PRODUCT_NAME;

    private static final String PDF_SWEEP_VERSION = "4.0.1";
    private static final int PDF_SWEEP_COPYRIGHT_SINCE = 2000;
    private static final int PDF_SWEEP_COPYRIGHT_TO = 2023;

    private static final ProductData PDF_SWEEP_PRODUCT_DATA = new ProductData(PDF_SWEEP_PUBLIC_PRODUCT_NAME,
            PDF_SWEEP_PRODUCT_NAME, PDF_SWEEP_VERSION, PDF_SWEEP_COPYRIGHT_SINCE, PDF_SWEEP_COPYRIGHT_TO);

    private PdfSweepProductData() {}

    /**
     * Getter for an instance of {@link ProductData} related to iText pdfSweep module.
     *
     * @return iText pdfSweep product description
     */
    public static ProductData getInstance() {
        return PDF_SWEEP_PRODUCT_DATA;
    }
}
