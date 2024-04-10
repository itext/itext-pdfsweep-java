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

import com.itextpdf.kernel.geom.Point;
import com.itextpdf.test.ExtendedITextTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class PdfCleanUpFilterUnitTest extends ExtendedITextTest {

    @Test
    public void checkIfRectanglesIntersect_completelyCoveredBasic() {
        Point[] intersectSubject = new Point[] {
                new Point(70, 70),
                new Point(80, 70),
                new Point(80, 80),
                new Point(70, 80)
        };
        Point[] intersecting = new Point[] {
                new Point(50, 50),
                new Point(100, 50),
                new Point(100, 100),
                new Point(50, 100)
        };
        Assertions.assertTrue(PdfCleanUpFilter.checkIfRectanglesIntersect(intersectSubject, intersecting));
    }

    @Test
    public void checkIfRectanglesIntersect_completelyCoveredDegenerateWidth() {
        Point[] intersectSubject = new Point[] {
                new Point(70, 70),
                new Point(70, 70),
                new Point(70, 80),
                new Point(70, 80)
        };
        Point[] intersecting = new Point[] {
                new Point(50, 50),
                new Point(100, 50),
                new Point(100, 100),
                new Point(50, 100)
        };
        Assertions.assertTrue(PdfCleanUpFilter.checkIfRectanglesIntersect(intersectSubject, intersecting));
    }

    @Test
    public void checkIfRectanglesIntersect_completelyCoveredDegenerateHeight() {
        Point[] intersectSubject = new Point[] {
                new Point(70, 70),
                new Point(80, 70),
                new Point(80, 70),
                new Point(70, 70)
        };
        Point[] intersecting = new Point[] {
                new Point(50, 50),
                new Point(100, 50),
                new Point(100, 100),
                new Point(50, 100)
        };
        Assertions.assertTrue(PdfCleanUpFilter.checkIfRectanglesIntersect(intersectSubject, intersecting));
    }

    @Test
    public void checkIfRectanglesIntersect_completelyCoveredDegeneratePoint() {
        Point[] intersectSubject = new Point[] {
                new Point(70, 70),
                new Point(70, 70),
                new Point(70, 70),
                new Point(70, 70)
        };
        Point[] intersecting = new Point[] {
                new Point(50, 50),
                new Point(100, 50),
                new Point(100, 100),
                new Point(50, 100)
        };
        Assertions.assertTrue(PdfCleanUpFilter.checkIfRectanglesIntersect(intersectSubject, intersecting));
    }
}
