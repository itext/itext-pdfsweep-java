package com.itextpdf.pdfcleanup;

import com.itextpdf.kernel.geom.Point;
import com.itextpdf.test.annotations.type.UnitTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(UnitTest.class)
public class PdfCleanUpFilterUnitTest {

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
        Assert.assertTrue(PdfCleanUpFilter.checkIfRectanglesIntersect(intersectSubject, intersecting));
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
        Assert.assertTrue(PdfCleanUpFilter.checkIfRectanglesIntersect(intersectSubject, intersecting));
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
        Assert.assertTrue(PdfCleanUpFilter.checkIfRectanglesIntersect(intersectSubject, intersecting));
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
        Assert.assertTrue(PdfCleanUpFilter.checkIfRectanglesIntersect(intersectSubject, intersecting));
    }
}
