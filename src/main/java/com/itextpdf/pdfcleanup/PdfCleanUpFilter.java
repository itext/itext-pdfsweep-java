/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2018 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.pdfcleanup;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.AffineTransform;
import com.itextpdf.kernel.geom.BezierCurve;
import com.itextpdf.kernel.geom.Line;
import com.itextpdf.kernel.geom.LineSegment;
import com.itextpdf.kernel.geom.Matrix;
import com.itextpdf.kernel.geom.NoninvertibleTransformException;
import com.itextpdf.kernel.geom.Point;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.geom.Subpath;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfTextArray;
import com.itextpdf.kernel.pdf.canvas.PdfCanvasConstants;
import com.itextpdf.kernel.pdf.canvas.parser.clipper.ClipperBridge;
import com.itextpdf.kernel.pdf.canvas.parser.clipper.ClipperOffset;
import com.itextpdf.kernel.pdf.canvas.parser.clipper.DefaultClipper;
import com.itextpdf.kernel.pdf.canvas.parser.clipper.IClipper;
import com.itextpdf.kernel.pdf.canvas.parser.clipper.IClipper.ClipType;
import com.itextpdf.kernel.pdf.canvas.parser.clipper.IClipper.EndType;
import com.itextpdf.kernel.pdf.canvas.parser.clipper.IClipper.JoinType;
import com.itextpdf.kernel.pdf.canvas.parser.clipper.IClipper.PolyFillType;
import com.itextpdf.kernel.pdf.canvas.parser.clipper.IClipper.PolyType;
import com.itextpdf.kernel.pdf.canvas.parser.clipper.Paths;
import com.itextpdf.kernel.pdf.canvas.parser.clipper.PolyTree;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.PathRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.ImagingConstants;
import org.apache.commons.imaging.formats.tiff.constants.TiffConstants;

public class PdfCleanUpFilter {

    private static final Color CLEANED_AREA_FILL_COLOR = Color.WHITE;

    /* There is no exact representation of the circle using Bezier curves.
     * But, for a Bezier curve with n segments the optimal distance to the control points,
     * in the sense that the middle of the curve lies on the circle itself, is (4/3) * tan(pi / (2*n))
     * So for 4 points it is (4/3) * tan(pi/8) = 4 * (sqrt(2)-1)/3 = 0.5522847498
     * In this approximation, the Bézier curve always falls outside the circle,
     * except momentarily when it dips in to touch the circle at the midpoint and endpoints.
     * However, a better approximation is possible using 0.55191502449
     */
    private static final double CIRCLE_APPROXIMATION_CONST = 0.55191502449;

    private List<Rectangle> regions;

    public PdfCleanUpFilter(List<Rectangle> regions) {
        this.regions = regions;
    }

    /**
     * Filter a TextRenderInfo object
     *
     * @param text the TextRenderInfo to be filtered
     */
    FilterResult<PdfArray> filterText(TextRenderInfo text) {
        PdfTextArray textArray = new PdfTextArray();

        if (isTextNotToBeCleaned(text)) {
            return new FilterResult<>(false, new PdfArray(text.getPdfString()));
        }

        for (TextRenderInfo ri : text.getCharacterRenderInfos()) {
            if (isTextNotToBeCleaned(ri)) {
                textArray.add(ri.getPdfString());
            } else {
                textArray.add(new PdfNumber(
                        -ri.getUnscaledWidth() * 1000f / (text.getFontSize() * text.getHorizontalScaling() / 100)
                ));
            }
        }

        return new FilterResult<PdfArray>(true, textArray);
    }

    FilteredImagesCache.FilteredImageKey createFilteredImageKey(ImageRenderInfo image, PdfDocument document) {
        return FilteredImagesCache.createFilteredImageKey(image, getImageAreasToBeCleaned(image), document);
    }

    /**
     * Filter an ImageRenderInfo object
     *
     * @param image the ImageRenderInfo object to be filtered
     */
    FilterResult<ImageData> filterImage(ImageRenderInfo image) {
        return filterImage(image, getImageAreasToBeCleaned(image));
    }

    FilterResult<ImageData> filterImage(FilteredImagesCache.FilteredImageKey imageKey) {
        return filterImage(imageKey.getImageRenderInfo(), imageKey.getCleanedAreas());
    }

    private FilterResult<ImageData> filterImage(ImageRenderInfo image, List<Rectangle> imageAreasToBeCleaned) {
        if (imageAreasToBeCleaned == null) {
            return new FilterResult<>(true, null);
        } else if (imageAreasToBeCleaned.isEmpty()) {
            return new FilterResult<>(false, null);
        }

        byte[] filteredImageBytes;
        try {
            byte[] originalImageBytes = image.getImage().getImageBytes();
            filteredImageBytes = processImage(originalImageBytes, imageAreasToBeCleaned);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new FilterResult<>(true, ImageDataFactory.create(filteredImageBytes));
    }

    /**
     * Filter a PathRenderInfo object
     *
     * @param path the PathRenderInfo object to be filtered
     */
    com.itextpdf.kernel.geom.Path filterStrokePath(PathRenderInfo path) {
        PdfArray dashPattern = path.getLineDashPattern();
        LineDashPattern lineDashPattern = new LineDashPattern(dashPattern.getAsArray(0), dashPattern.getAsNumber(1).floatValue());

        return filterStrokePath(path.getPath(), path.getCtm(), path.getLineWidth(), path.getLineCapStyle(),
                path.getLineJoinStyle(), path.getMiterLimit(), lineDashPattern);
    }

    /**
     * Filter a PathRenderInfo object
     *
     * @param path the PathRenderInfo object to be filtered
     */
    com.itextpdf.kernel.geom.Path filterFillPath(PathRenderInfo path, int fillingRule) {
        return filterFillPath(path.getPath(), path.getCtm(), fillingRule);
    }

    /**
     * Returns whether the given TextRenderInfo object needs to be cleaned up
     *
     * @param renderInfo the input TextRenderInfo object
     */
    private boolean isTextNotToBeCleaned(TextRenderInfo renderInfo) {
        Point[] textRect = getTextRectangle(renderInfo);

        for (Rectangle region : regions) {
            Point[] redactRect = getRectangleVertices(region);

            if (checkIfRectanglesIntersect(textRect, redactRect)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Calculates intersection of the image and the render filter region in the coordinate system relative to the image.
     *
     * @return {@code null} if the image is fully covered and therefore is completely cleaned, {@link java.util.List} of
     * {@link Rectangle} objects otherwise.
     */
    private List<Rectangle> getImageAreasToBeCleaned(ImageRenderInfo image) {
        Rectangle imageRect = calcImageRect(image);
        if (imageRect == null) {
            return null;
        }

        List<Rectangle> areasToBeCleaned = new ArrayList<>();

        for (Rectangle region : regions) {
            Rectangle intersectionRect = getRectanglesIntersection(imageRect, region);

            if (intersectionRect != null) {
                if (imageRect.equalsWithEpsilon(intersectionRect)) { // true if the image is completely covered
                    return null;
                }

                areasToBeCleaned.add(transformRectIntoImageCoordinates(intersectionRect, image.getImageCtm()));
            }
        }

        return areasToBeCleaned;
    }

    private com.itextpdf.kernel.geom.Path filterStrokePath(com.itextpdf.kernel.geom.Path sourcePath, Matrix ctm,
                                                           float lineWidth, int lineCapStyle, int lineJoinStyle,
                                                           float miterLimit, LineDashPattern lineDashPattern) {
        com.itextpdf.kernel.geom.Path path = sourcePath;
        JoinType joinType = ClipperBridge.getJoinType(lineJoinStyle);
        EndType endType = ClipperBridge.getEndType(lineCapStyle);

        if (lineDashPattern != null) {
            if (!lineDashPattern.isSolid()) {
                path = LineDashPattern.applyDashPattern(path, lineDashPattern);
            }
        }

        ClipperOffset offset = new ClipperOffset(miterLimit, PdfCleanUpTool.arcTolerance * PdfCleanUpTool.floatMultiplier);
        List<Subpath> degenerateSubpaths = ClipperBridge.addPath(offset, path, joinType, endType);

        PolyTree resultTree = new PolyTree();
        offset.execute(resultTree, lineWidth * PdfCleanUpTool.floatMultiplier / 2);
        com.itextpdf.kernel.geom.Path offsetedPath = ClipperBridge.convertToPath(resultTree);

        if (degenerateSubpaths.size() > 0) {
            if (endType == EndType.OPEN_ROUND) {
                List<Subpath> circles = convertToCircles(degenerateSubpaths, lineWidth / 2);
                offsetedPath.addSubpaths(circles);
            } else if (endType == EndType.OPEN_SQUARE && lineDashPattern != null) {
                List<Subpath> squares = convertToSquares(degenerateSubpaths, lineWidth, sourcePath);
                offsetedPath.addSubpaths(squares);
            }
        }

        return filterFillPath(offsetedPath, ctm, PdfCanvasConstants.FillingRule.NONZERO_WINDING);
    }

    /**
     * Note: this method will close all unclosed subpaths of the passed path.
     *
     * @param fillingRule If the subpath is contour, pass any value.
     */
    private com.itextpdf.kernel.geom.Path filterFillPath(com.itextpdf.kernel.geom.Path path, Matrix ctm, int fillingRule) {
        path.closeAllSubpaths();

        IClipper clipper = new DefaultClipper();
        ClipperBridge.addPath(clipper, path, PolyType.SUBJECT);

        for (Rectangle rectangle : regions) {
            Point[] transfRectVertices = transformPoints(ctm, true, getRectangleVertices(rectangle));
            ClipperBridge.addRectToClipper(clipper, transfRectVertices, PolyType.CLIP);
        }

        PolyFillType fillType = PolyFillType.NON_ZERO;

        if (fillingRule == PdfCanvasConstants.FillingRule.EVEN_ODD) {
            fillType = PolyFillType.EVEN_ODD;
        }

        PolyTree resultTree = new PolyTree();
        clipper.execute(ClipType.DIFFERENCE, resultTree, fillType, PolyFillType.NON_ZERO);

        return ClipperBridge.convertToPath(resultTree);
    }

    /**
     * Return true if two given rectangles (specified by an array of points) intersect.
     *
     * @param rect1 the first rectangle, considered as a subject of intersection. Even if it's width is zero,
     *              it still can be intersected by second rectangle.
     * @param rect2 the second rectangle, considered as intersecting rectangle. If it has zero width rectangles
     *              are never considered as intersecting.
     * @return true if the rectangles intersect, false otherwise
     */
    static boolean checkIfRectanglesIntersect(Point[] rect1, Point[] rect2) {
        IClipper clipper = new DefaultClipper();
        ClipperBridge.addPolygonToClipper(clipper, rect2, PolyType.CLIP);
        // According to clipper documentation:
        // The function will return false if the path is invalid for clipping. A path is invalid for clipping when:
        // - it has less than 2 vertices;
        // - it has 2 vertices but is not an open path;
        // - the vertices are all co-linear and it is not an open path.
        // Reference: http://www.angusj.com/delphi/clipper/documentation/Docs/Units/ClipperLib/Classes/ClipperBase/Methods/AddPath.htm
        // If addition returns false, this means that there are less than 3 distinct points, because of rectangle zero width.
        // Let's in this case specify the path as polyline, because we still want to know if redaction area
        // intersects even with zero-width rectangles.
        boolean intersectionSubjectAdded = ClipperBridge.addPolygonToClipper(clipper, rect1, PolyType.SUBJECT);
        if (intersectionSubjectAdded) {
            // working with paths is considered to be a bit faster in terms of performance.
            Paths paths = new Paths();
            clipper.execute(ClipType.INTERSECTION, paths, PolyFillType.NON_ZERO, PolyFillType.NON_ZERO);
            return !paths.isEmpty();
        } else {
            int rect1Size = rect1.length;
            intersectionSubjectAdded = ClipperBridge.addPolylineSubjectToClipper(clipper, rect1);
            if (!intersectionSubjectAdded) {
                // According to the comment above,
                // this could have happened only if all four passed points are actually the same point.
                // Adding here a point really close to the original point, to make sure it's not covered by the
                // intersecting rectangle.
                double smallDiff = 0.01;
                List<Point> rect1List = new ArrayList<Point>(Arrays.asList(rect1));
                rect1List.add(new Point(rect1[0].getX() + smallDiff, rect1[0].getY()));
                rect1 = rect1List.toArray(new Point[rect1Size]);
                intersectionSubjectAdded = ClipperBridge.addPolylineSubjectToClipper(clipper, rect1);
                assert intersectionSubjectAdded;
            }
            PolyTree polyTree = new PolyTree();
            clipper.execute(ClipType.INTERSECTION, polyTree, PolyFillType.NON_ZERO, PolyFillType.NON_ZERO);
            return !Paths.makePolyTreeToPaths(polyTree).isEmpty();
        }
    }

    /**
     * @return Image boundary rectangle in device space.
     */
    private static Rectangle calcImageRect(ImageRenderInfo renderInfo) {
        Matrix ctm = renderInfo.getImageCtm();

        if (ctm == null) {
            return null;
        }

        Point[] points = transformPoints(ctm, false,
                new Point(0, 0), new Point(0, 1),
                new Point(1, 0), new Point(1, 1));

        return getAsRectangle(points[0], points[1], points[2], points[3]);
    }

    /**
     * Transforms the given Rectangle into the image coordinate system which is [0,1]x[0,1] by default
     */
    private static Rectangle transformRectIntoImageCoordinates(Rectangle rect, Matrix imageCtm) {
        Point[] points = transformPoints(imageCtm, true, new Point(rect.getLeft(), rect.getBottom()),
                new Point(rect.getLeft(), rect.getTop()),
                new Point(rect.getRight(), rect.getBottom()),
                new Point(rect.getRight(), rect.getTop()));
        return getAsRectangle(points[0], points[1], points[2], points[3]);
    }

    /**
     * Clean up an image using a List of Rectangles that need to be redacted
     *
     * @param imageBytes       the image to be cleaned up
     * @param areasToBeCleaned the List of Rectangles that need to be redacted out of the image
     */
    private static byte[] processImage(byte[] imageBytes, List<Rectangle> areasToBeCleaned) {
        if (areasToBeCleaned.isEmpty()) {
            return imageBytes;
        }

        try {
            BufferedImage image = getBuffer(imageBytes);
            ImageInfo imageInfo = Imaging.getImageInfo(imageBytes);
            cleanImage(image, areasToBeCleaned);

            // Apache can only read JPEG, so we should use awt for writing in this format
            if (imageInfo.getFormat() == ImageFormats.JPEG) {
                return getJPGBytes(image);
            } else {
                Map<String, Object> params = new HashMap<>();

                if (imageInfo.getFormat() == ImageFormats.TIFF) {
                    params.put(ImagingConstants.PARAM_KEY_COMPRESSION, TiffConstants.TIFF_COMPRESSION_LZW);
                }

                return Imaging.writeImageToBytes(image, imageInfo.getFormat(), params);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads the image bytes into a {@link BufferedImage}.
     * Isolates and catches known Apache Commons Imaging bug for JPEG:
     * https://issues.apache.org/jira/browse/IMAGING-97
     *
     * @param imageBytes the image to be read, as a byte array
     * @return a BufferedImage, independent of the reading strategy
     */
    private static BufferedImage getBuffer(byte[] imageBytes) throws IOException {
        try {
            return Imaging.getBufferedImage(imageBytes);
        } catch (ImageReadException ire) {
            return ImageIO.read(new ByteArrayInputStream(imageBytes));
        }
    }

    /**
     * Clean up a BufferedImage using a List of Rectangles that need to be redacted
     *
     * @param image            the image to be cleaned up
     * @param areasToBeCleaned the List of Rectangles that need to be redacted out of the image
     */
    private static void cleanImage(BufferedImage image, List<Rectangle> areasToBeCleaned) {
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(CLEANED_AREA_FILL_COLOR);

        // A rectangle in the areasToBeCleaned list is treated to be in standard [0,1]x[0,1] image space
        // (y varies from bottom to top and x from left to right), so we should scale the rectangle and also
        // invert and shear the y axe.
        for (Rectangle rect : areasToBeCleaned) {
            int scaledBottomY = (int) Math.ceil(rect.getBottom() * image.getHeight());
            int scaledTopY = (int) Math.floor(rect.getTop() * image.getHeight());

            int x = (int) Math.ceil(rect.getLeft() * image.getWidth());
            int y = scaledTopY * -1 + image.getHeight();
            int width = (int) Math.floor(rect.getRight() * image.getWidth()) - x;
            int height = scaledTopY - scaledBottomY;

            graphics.fillRect(x, y, width, height);
        }

        graphics.dispose();
    }

    /**
     * Get the bytes of the BufferedImage (in JPG format)
     *
     * @param image input image
     */
    private static byte[] getJPGBytes(BufferedImage image) {
        ByteArrayOutputStream outputStream = null;

        try {
            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(1.0f);

            outputStream = new ByteArrayOutputStream();
            jpgWriter.setOutput(new MemoryCacheImageOutputStream((outputStream)));
            IIOImage outputImage = new IIOImage(image, null, null);

            jpgWriter.write(null, outputImage, jpgWriteParam);
            jpgWriter.dispose();
            outputStream.flush();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeOutputStream(outputStream);
        }
    }

    /**
     * Converts specified degenerate subpaths to circles.
     * Note: actually the resultant subpaths are not real circles but approximated.
     *
     * @param radius Radius of each constructed circle.
     * @return {@link java.util.List} consisting of circles constructed on given degenerated subpaths.
     */
    private static List<Subpath> convertToCircles(List<Subpath> degenerateSubpaths, double radius) {
        List<Subpath> circles = new ArrayList<>(degenerateSubpaths.size());

        for (Subpath subpath : degenerateSubpaths) {
            BezierCurve[] circleSectors = approximateCircle(subpath.getStartPoint(), radius);

            Subpath circle = new Subpath();
            circle.addSegment(circleSectors[0]);
            circle.addSegment(circleSectors[1]);
            circle.addSegment(circleSectors[2]);
            circle.addSegment(circleSectors[3]);

            circles.add(circle);
        }

        return circles;
    }

    /**
     * Converts specified degenerate subpaths to squares.
     * Note: the list of degenerate subpaths should contain at least 2 elements. Otherwise
     * we can't determine the direction which the rotation of each square depends on.
     *
     * @param squareWidth Width of each constructed square.
     * @param sourcePath  The path which dash pattern applied to. Needed to calc rotation angle of each square.
     * @return {@link java.util.List} consisting of squares constructed on given degenerated subpaths.
     */
    private static List<Subpath> convertToSquares(List<Subpath> degenerateSubpaths, double squareWidth, com.itextpdf.kernel.geom.Path sourcePath) {
        List<Point> pathApprox = getPathApproximation(sourcePath);

        if (pathApprox.size() < 2) {
            return Collections.emptyList();
        }

        Iterator<Point> approxIter = pathApprox.iterator();
        Point approxPt1 = approxIter.next();
        Point approxPt2 = approxIter.next();
        StandardLine line = new StandardLine(approxPt1, approxPt2);

        List<Subpath> squares = new ArrayList<>(degenerateSubpaths.size());
        float widthHalf = (float) squareWidth / 2;

        for (Subpath subpath : degenerateSubpaths) {
            Point point = subpath.getStartPoint();

            while (!line.contains(point)) {
                approxPt1 = approxPt2;
                approxPt2 = approxIter.next();
                line = new StandardLine(approxPt1, approxPt2);
            }

            double slope = line.getSlope();
            double angle;

            if (slope != Float.POSITIVE_INFINITY) {
                angle = Math.atan(slope);
            } else {
                angle = Math.PI / 2;
            }

            squares.add(constructSquare(point, widthHalf, angle));
        }

        return squares;
    }

    /**
     * Approximates a given Path with a List of Point objects
     *
     * @param path input path
     */
    private static List<Point> getPathApproximation(com.itextpdf.kernel.geom.Path path) {
        List<Point> approx = new ArrayList<Point>() {
            @Override
            public boolean addAll(Collection<? extends Point> c) {
                Point prevPoint = (size() - 1 < 0 ? null : get(size() - 1));
                boolean ret = false;

                for (Point pt : c) {
                    if (!pt.equals(prevPoint)) {
                        add(pt);
                        prevPoint = pt;
                        ret = true;
                    }
                }

                return true;
            }
        };

        for (Subpath subpath : path.getSubpaths()) {
            approx.addAll(subpath.getPiecewiseLinearApproximation());
        }

        return approx;
    }

    private static Subpath constructSquare(Point squareCenter, double widthHalf, double rotationAngle) {
        // Orthogonal square is the square with sides parallel to one of the axes.
        Point[] ortogonalSquareVertices = {
                new Point(-widthHalf, -widthHalf),
                new Point(-widthHalf, widthHalf),
                new Point(widthHalf, widthHalf),
                new Point(widthHalf, -widthHalf)
        };

        Point[] rotatedSquareVertices = getRotatedSquareVertices(ortogonalSquareVertices, rotationAngle, squareCenter);

        Subpath square = new Subpath();
        square.addSegment(new Line(rotatedSquareVertices[0], rotatedSquareVertices[1]));
        square.addSegment(new Line(rotatedSquareVertices[1], rotatedSquareVertices[2]));
        square.addSegment(new Line(rotatedSquareVertices[2], rotatedSquareVertices[3]));
        square.addSegment(new Line(rotatedSquareVertices[3], rotatedSquareVertices[0]));

        return square;
    }

    private static Point[] getRotatedSquareVertices(Point[] orthogonalSquareVertices, double angle, Point squareCenter) {
        Point[] rotatedSquareVertices = new Point[orthogonalSquareVertices.length];

        AffineTransform.getRotateInstance((float) angle).
                transform(orthogonalSquareVertices, 0, rotatedSquareVertices, 0, rotatedSquareVertices.length);
        AffineTransform.getTranslateInstance((float) squareCenter.getX(), (float) squareCenter.getY()).
                transform(rotatedSquareVertices, 0, rotatedSquareVertices, 0, orthogonalSquareVertices.length);

        return rotatedSquareVertices;
    }

    /**
     * Approximate a circle with 4 Bezier curves (one for each 90 degrees sector)
     *
     * @param center center of the circle
     * @param radius radius of the circle
     */
    private static BezierCurve[] approximateCircle(Point center, double radius) {
        // The circle is split into 4 sectors. Arc of each sector
        // is approximated  with bezier curve separately.
        BezierCurve[] approximation = new BezierCurve[4];
        double x = center.getX();
        double y = center.getY();

        approximation[0] = new BezierCurve(Arrays.asList(
                new Point(x, y + radius),
                new Point(x + radius * CIRCLE_APPROXIMATION_CONST, y + radius),
                new Point(x + radius, y + radius * CIRCLE_APPROXIMATION_CONST),
                new Point(x + radius, y)));

        approximation[1] = new BezierCurve(Arrays.asList(
                new Point(x + radius, y),
                new Point(x + radius, y - radius * CIRCLE_APPROXIMATION_CONST),
                new Point(x + radius * CIRCLE_APPROXIMATION_CONST, y - radius),
                new Point(x, y - radius)));

        approximation[2] = new BezierCurve(Arrays.asList(
                new Point(x, y - radius),
                new Point(x - radius * CIRCLE_APPROXIMATION_CONST, y - radius),
                new Point(x - radius, y - radius * CIRCLE_APPROXIMATION_CONST),
                new Point(x - radius, y)));

        approximation[3] = new BezierCurve(Arrays.asList(
                new Point(x - radius, y),
                new Point(x - radius, y + radius * CIRCLE_APPROXIMATION_CONST),
                new Point(x - radius * CIRCLE_APPROXIMATION_CONST, y + radius),
                new Point(x, y + radius)));

        return approximation;
    }

    private static Point[] transformPoints(Matrix transformationMatrix, boolean inverse, Point... points) {
        AffineTransform t = new AffineTransform(transformationMatrix.get(Matrix.I11), transformationMatrix.get(Matrix.I12),
                transformationMatrix.get(Matrix.I21), transformationMatrix.get(Matrix.I22),
                transformationMatrix.get(Matrix.I31), transformationMatrix.get(Matrix.I32));
        Point[] transformed = new Point[points.length];

        if (inverse) {
            try {
                t = t.createInverse();
            } catch (NoninvertibleTransformException e) {
                throw new RuntimeException(e);
            }
        }

        t.transform(points, 0, transformed, 0, points.length);

        return transformed;
    }

    /**
     * Get the bounding box of a TextRenderInfo object
     *
     * @param renderInfo input TextRenderInfo object
     */
    private static Point[] getTextRectangle(TextRenderInfo renderInfo) {
        LineSegment ascent = renderInfo.getAscentLine();
        LineSegment descent = renderInfo.getDescentLine();

        return new Point[]{
                new Point(ascent.getStartPoint().get(0), ascent.getStartPoint().get(1)),
                new Point(ascent.getEndPoint().get(0), ascent.getEndPoint().get(1)),
                new Point(descent.getEndPoint().get(0), descent.getEndPoint().get(1)),
                new Point(descent.getStartPoint().get(0), descent.getStartPoint().get(1)),
        };
    }

    /**
     * Convert a Rectangle object into 4 Points
     *
     * @param rect input Rectangle
     */
    private static Point[] getRectangleVertices(Rectangle rect) {
        Point[] points = {
                new Point(rect.getLeft(), rect.getBottom()),
                new Point(rect.getRight(), rect.getBottom()),
                new Point(rect.getRight(), rect.getTop()),
                new Point(rect.getLeft(), rect.getTop())
        };

        return points;
    }

    /**
     * Convert 4 Point objects into a Rectangle
     *
     * @param p1 first Point
     * @param p2 second Point
     * @param p3 third Point
     * @param p4 fourth Point
     */
    private static Rectangle getAsRectangle(Point p1, Point p2, Point p3, Point p4) {
        List<Double> xs = Arrays.asList(p1.getX(), p2.getX(), p3.getX(), p4.getX());
        List<Double> ys = Arrays.asList(p1.getY(), p2.getY(), p3.getY(), p4.getY());

        double left = Collections.min(xs);
        double bottom = Collections.min(ys);
        double right = Collections.max(xs);
        double top = Collections.max(ys);

        return new Rectangle((float) left, (float) bottom, (float) (right - left), (float) (top - bottom));
    }

    /**
     * Calculate the intersection of 2 Rectangles
     *
     * @param rect1 first Rectangle
     * @param rect2 second Rectangle
     */
    private static Rectangle getRectanglesIntersection(Rectangle rect1, Rectangle rect2) {
        float x1 = Math.max(rect1.getLeft(), rect2.getLeft());
        float y1 = Math.max(rect1.getBottom(), rect2.getBottom());
        float x2 = Math.min(rect1.getRight(), rect2.getRight());
        float y2 = Math.min(rect1.getTop(), rect2.getTop());
        return (x2 - x1 > 0 && y2 - y1 > 0)
                ? new Rectangle(x1, y1, x2 - x1, y2 - y1)
                : null;
    }

    private static void closeOutputStream(OutputStream os) {
        if (os != null) {
            try {
                os.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Generic class representing the result of filtering an object of type T
     */
    static class FilterResult<T> {
        private boolean isModified;
        private T filterResult;

        FilterResult(boolean isModified, T filterResult) {
            this.isModified = isModified;
            this.filterResult = filterResult;
        }

        /**
         * Get whether the object was modified or not
         *
         * @return true if the object was modified, false otherwise
         */
        boolean isModified() {
            return isModified;
        }

        /**
         * Get the result after filtering
         */
        T getFilterResult() {
            return filterResult;
        }
    }

    // Constants from the standard line representation: Ax+By+C
    private static class StandardLine {

        float A;
        float B;
        float C;

        StandardLine(Point p1, Point p2) {
            A = (float) (p2.getY() - p1.getY());
            B = (float) (p1.getX() - p2.getX());
            C = (float) (p1.getY() * (-B) - p1.getX() * A);
        }

        float getSlope() {
            if (B == 0) {
                return Float.POSITIVE_INFINITY;
            }

            return -A / B;
        }

        boolean contains(Point point) {
            return Float.compare(Math.abs(A * (float) point.getX() + B * (float) point.getY() + C), 0.1f) < 0;
        }
    }
}
