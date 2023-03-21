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
package com.itextpdf.pdfcleanup.util;

import com.itextpdf.kernel.geom.Rectangle;

/**
 * Utility class providing clean up helping methods.
 */
public final class CleanUpHelperUtil {
    private static final float EPS = 1e-4F;

    private CleanUpHelperUtil() {
    }

    /**
     * Calculates the coordinates of the image rectangle to clean by the passed {@link Rectangle},
     * specifying the area to clean.
     *
     * @param rect      the {@link Rectangle} specifying the area to clean.
     * @param imgWidth  width of the image to clean
     * @param imgHeight height of the image to clean
     * @return an array of the resultant rectangle coordinates
     */
    public static int[] getImageRectToClean(Rectangle rect, int imgWidth, int imgHeight) {
        double bottom = (double) rect.getBottom() * imgHeight;
        int scaledBottomY = (int) Math.ceil(bottom - EPS);
        double top = (double) rect.getTop() * imgHeight;
        int scaledTopY = (int) Math.floor(top + EPS);

        double left = (double) rect.getLeft() * imgWidth;
        int x = (int) Math.ceil(left - EPS);
        int y = imgHeight - scaledTopY;
        double right = (double) rect.getRight() * imgWidth;
        int w = (int) Math.floor(right + EPS) - x;
        int h = scaledTopY - scaledBottomY;
        return new int[]{x, y, w, h};
    }
}
