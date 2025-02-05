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
package com.itextpdf.pdfcleanup.autosweep;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ILocationExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IPdfTextLocation;
import com.itextpdf.pdfcleanup.PdfCleaner;

/**
 * This class represents a generic cleanup strategy to be used with {@link PdfCleaner} or {@link PdfAutoSweepTools}
 * ICleanupStrategy must implement Cloneable to ensure a strategy can be reset after having handled a page.
 */
public interface ICleanupStrategy extends ILocationExtractionStrategy {
    /**
     * Get the color in which redaction is to take place
     *
     * @param location where to get the redaction color from
     *
     * @return a {@link Color}
     */
    Color getRedactionColor(IPdfTextLocation location);

    /**
     * ICleanupStrategy objects have to be reset at times
     * {@code PdfAutoSweep} will use the same strategy for all pages,
     * and expects to receive only the rectangles from the last page as output.
     * Hence the reset method.
     *
     * @return a clone of this Object
     */
    ICleanupStrategy reset();

}
