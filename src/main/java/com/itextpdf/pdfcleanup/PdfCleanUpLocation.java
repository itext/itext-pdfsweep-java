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

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.geom.Rectangle;

/**
 * Defines the region to be erased in a PDF document.
 *
 */
public class PdfCleanUpLocation {
    private int page;
    private Rectangle region;
    private Color cleanUpColor;

    /**
     * Constructs a {@link PdfCleanUpLocation} object.
     *
     * @param page   specifies the number of the page which the region belongs to.
     * @param region represents the boundaries of the area to be erased.
     */
    public PdfCleanUpLocation(int page, Rectangle region) {
        this.page = page;
        this.region = region;
    }

    /**
     * Constructs a {@link PdfCleanUpLocation} object.
     *
     * @param page         specifies the number of the page which the region belongs to.
     * @param region       represents the boundaries of the area to be erased.
     * @param cleanUpColor a color used to fill the area after erasing it. If {@code null}
     *                     the erased area left uncolored.
     */
    public PdfCleanUpLocation(int page, Rectangle region, Color cleanUpColor) {
        this(page, region);
        this.cleanUpColor = cleanUpColor;
    }

    /**
     * @return the number of the page which the region belongs to.
     */
    public int getPage() {
        return page;
    }

    /**
     * @return A {@link Rectangle} representing the boundaries of the area to be erased.
     */
    public Rectangle getRegion() {
        return region;
    }

    /**
     * Returns a color used to fill the area after erasing it. If {@code null} the erased area left uncolored.
     *
     * @return a color used to fill the area after erasing it.
     */
    public Color getCleanUpColor() {
        return cleanUpColor;
    }
}
