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

import java.util.ArrayList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("UnitTest")
public class PdfCleanUpFilterUnitTest extends ExtendedITextTest {

    @Test
    public void pointIntersectLineCaseTest1() {
        Point[] intersectSubject = new Point[] {
                new Point(50, 60),
                new Point(70, 60),
                new Point(50, 60)
        };
        Point[] intersecting = new Point[] {
                new Point(50, 50),
                new Point(50, 70),
                new Point(50, 50)
        };
        PdfCleanUpFilter filter = new PdfCleanUpFilter(new ArrayList<>(), new CleanUpProperties());
        Assertions.assertTrue(filter.checkIfRectanglesIntersect(intersectSubject, intersecting));
    }

    @Test
    public void pointIntersectLineCaseTest2() {
        Point[] intersectSubject = new Point[] {
                new Point(50, 60),
                new Point(70, 60),
                new Point(50, 60)
        };
        Point[] intersecting = new Point[] {
                new Point(50, 50),
                new Point(50, 30),
                new Point(50, 50)
        };
        PdfCleanUpFilter filter = new PdfCleanUpFilter(new ArrayList<>(), new CleanUpProperties());
        Assertions.assertFalse(filter.checkIfRectanglesIntersect(intersectSubject, intersecting));
    }

    @Test
    public void pointIntersectLineCaseTest3() {
        Point[] intersectSubject = new Point[] {
                new Point(50, 65),
                new Point(70, 65),
                new Point(50, 65)
        };
        Point[] intersecting = new Point[] {
                new Point(40, 50),
                new Point(60, 70),
                new Point(40, 50)
        };
        PdfCleanUpFilter filter = new PdfCleanUpFilter(new ArrayList<>(), new CleanUpProperties());
        Assertions.assertFalse(filter.checkIfRectanglesIntersect(intersectSubject, intersecting));
    }

    @Test
    public void pointIntersectLineCaseTest4() {
        Point[] intersectSubject = new Point[] {
                new Point(50, 60),
                new Point(70, 60),
                new Point(50, 60)
        };
        Point[] intersecting = new Point[] {
                new Point(30, 50),
                new Point(70, 70),
                new Point(30, 50)
        };
        PdfCleanUpFilter filter = new PdfCleanUpFilter(new ArrayList<>(), new CleanUpProperties());
        Assertions.assertTrue(filter.checkIfRectanglesIntersect(intersectSubject, intersecting));
    }

    @Test
    public void pointIntersectLineCaseTest5() {
        Point[] intersectSubject = new Point[] {
                new Point(50, 60),
                new Point(70, 60),
                new Point(50, 60)
        };
        Point[] intersecting = new Point[] {
                new Point(70, 50),
                new Point(30, 70),
                new Point(70, 50)
        };
        PdfCleanUpFilter filter = new PdfCleanUpFilter(new ArrayList<>(), new CleanUpProperties());
        Assertions.assertTrue(filter.checkIfRectanglesIntersect(intersectSubject, intersecting));
    }

    @Test
    public void pointIntersectLineCaseTest6() {
        Point[] intersectSubject = new Point[] {
                new Point(50, 80),
                new Point(70, 80),
                new Point(50, 80)
        };
        Point[] intersecting = new Point[] {
                new Point(50, 50),
                new Point(50, 70),
                new Point(50, 50)
        };
        PdfCleanUpFilter filter = new PdfCleanUpFilter(new ArrayList<>(), new CleanUpProperties());
        Assertions.assertTrue(filter.checkIfRectanglesIntersect(intersectSubject, intersecting));
    }

    @Test
    public void pointIntersectLineCaseTest7() {
        Point[] intersectSubject = new Point[] {
                new Point(50, 40),
                new Point(70, 40),
                new Point(50, 40)
        };
        Point[] intersecting = new Point[] {
                new Point(50, 50),
                new Point(50, 70),
                new Point(50, 50)
        };
        PdfCleanUpFilter filter = new PdfCleanUpFilter(new ArrayList<>(), new CleanUpProperties());
        Assertions.assertTrue(filter.checkIfRectanglesIntersect(intersectSubject, intersecting));
    }

    @Test
    public void pointIntersectLineCaseTest8() {
        Point[] intersectSubject = new Point[] {
                new Point(50, 20),
                new Point(70, 20),
                new Point(50, 20)
        };
        Point[] intersecting = new Point[] {
                new Point(50, 50),
                new Point(50, 30),
                new Point(50, 50)
        };
        PdfCleanUpFilter filter = new PdfCleanUpFilter(new ArrayList<>(), new CleanUpProperties());
        Assertions.assertFalse(filter.checkIfRectanglesIntersect(intersectSubject, intersecting));
    }

    @Test
    public void pointIntersectLineCaseTest9() {
        Point[] intersectSubject = new Point[] {
                new Point(50, 40),
                new Point(70, 40),
                new Point(50, 40)
        };
        Point[] intersecting = new Point[] {
                new Point(50, 50),
                new Point(50, 30),
                new Point(50, 50)
        };
        PdfCleanUpFilter filter = new PdfCleanUpFilter(new ArrayList<>(), new CleanUpProperties());
        Assertions.assertFalse(filter.checkIfRectanglesIntersect(intersectSubject, intersecting));
    }

    @Test
    public void pointIntersectLineCaseTest10() {
        Point[] intersectSubject = new Point[] {
                new Point(30, 80),
                new Point(90, 80),
                new Point(30, 80)
        };
        Point[] intersecting = new Point[] {
                new Point(60, 50),
                new Point(40, 70),
                new Point(60, 50)
        };
        PdfCleanUpFilter filter = new PdfCleanUpFilter(new ArrayList<>(), new CleanUpProperties());
        Assertions.assertTrue(filter.checkIfRectanglesIntersect(intersectSubject, intersecting));
    }

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
        PdfCleanUpFilter filter = new PdfCleanUpFilter(new ArrayList<>(), new CleanUpProperties());
        Assertions.assertTrue(filter.checkIfRectanglesIntersect(intersectSubject, intersecting));
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
        Assertions.assertTrue(new PdfCleanUpFilter(new ArrayList<>(), new CleanUpProperties()).checkIfRectanglesIntersect(intersectSubject, intersecting));
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
        Assertions.assertTrue(new PdfCleanUpFilter(new ArrayList<>() , new CleanUpProperties()).checkIfRectanglesIntersect(intersectSubject, intersecting));
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
        Assertions.assertTrue(new PdfCleanUpFilter(new ArrayList<>(), new CleanUpProperties()).checkIfRectanglesIntersect(intersectSubject, intersecting));
    }
}
