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

import com.itextpdf.commons.utils.MessageFormatUtil;
import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.geom.AffineTransform;
import com.itextpdf.kernel.geom.BezierCurve;
import com.itextpdf.kernel.geom.Line;
import com.itextpdf.kernel.geom.LineSegment;
import com.itextpdf.kernel.geom.Matrix;
import com.itextpdf.kernel.geom.NoninvertibleTransformException;
import com.itextpdf.kernel.geom.Path;
import com.itextpdf.kernel.geom.Point;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.geom.Subpath;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfStream;
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
import com.itextpdf.kernel.pdf.canvas.parser.clipper.LongRect;
import com.itextpdf.kernel.pdf.canvas.parser.clipper.Paths;
import com.itextpdf.kernel.pdf.canvas.parser.clipper.PolyTree;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.PathRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.pdfcleanup.exceptions.CleanupExceptionMessageConstant;
import com.itextpdf.pdfcleanup.logs.CleanUpLogMessageConstant;
import com.itextpdf.pdfcleanup.util.CleanUpHelperUtil;
import com.itextpdf.pdfcleanup.util.CleanUpImageUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PdfCleanUpFilter {
    private static final Logger logger = LoggerFactory.getLogger(PdfCleanUpFilter.class);

    /* There is no exact representation of the circle using Bézier curves.
     * But, for a Bézier curve with n segments the optimal distance to the control points,
     * in the sense that the middle of the curve lies on the circle itself, is (4/3) * tan(pi / (2*n))
     * So for 4 points it is (4/3) * tan(pi/8) = 4 * (sqrt(2)-1)/3 = 0.5522847498
     * In this approximation, the Bézier curve always falls outside the circle,
     * except momentarily when it dips in to touch the circle at the midpoint and endpoints.
     * However, a better approximation is possible using 0.55191502449
     */
    private static final double CIRCLE_APPROXIMATION_CONST = 0.55191502449;

    private static final float EPS = 1e-4f;

    private static final Set<PdfName> NOT_SUPPORTED_FILTERS_FOR_DIRECT_CLEANUP = Collections.unmodifiableSet(
            new LinkedHashSet<>(Arrays.asList(PdfName.JBIG2Decode, PdfName.DCTDecode, PdfName.JPXDecode)));

    private final List<Rectangle> regions;

    private final CleanUpProperties properties;

    public PdfCleanUpFilter(List<Rectangle> regions, CleanUpProperties properties) {
        this.regions = regions;
        this.properties = properties;
    }

    static boolean imageSupportsDirectCleanup(PdfImageXObject image) {
        PdfObject filter = image.getPdfObject().get(PdfName.Filter);
        boolean supportedFilterForDirectCleanup = isSupportedFilterForDirectImageCleanup(filter);
        boolean deviceGrayOrNoCS = PdfName.DeviceGray.equals(image.getPdfObject().getAsName(PdfName.ColorSpace))
                || !image.getPdfObject().containsKey(PdfName.ColorSpace);
        return deviceGrayOrNoCS && supportedFilterForDirectCleanup;
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
    boolean checkIfRectanglesIntersect(Point[] rect1, Point[] rect2) {
        IClipper clipper = new DefaultClipper();
        ClipperBridge clipperBridge = properties.getOffsetProperties().calculateOffsetMultiplierDynamically() ?
                new ClipperBridge(rect1, rect2) : new ClipperBridge();
        // If the redaction area is degenerate, the result will be false
        if (!clipperBridge.addPolygonToClipper(clipper, rect2, PolyType.CLIP)) {
            // If the content area is not degenerate (and the redaction area is), let's return false:
            // even if they overlaps somehow, we do not consider it as an intersection.
            // If the content area is degenerate, let's process this case specifically
            if (!clipperBridge.addPolygonToClipper(clipper, rect1, PolyType.SUBJECT)) {
                // Clipper fails to process degenerate redaction areas. However, that's vital for pdfAutoSweep,
                // because in some cases (for example, noninvertible cm) the text's area might be degenerate,
                // but we still need to sweep the content.
                // The idea is as follows:
                // a) if the degenerate redaction area represents a point, there is no intersection
                // b) if the degenerate redaction area represents a line, let's check that there the redaction line
                // equals to one of the edges of the content's area. That is implemented in respect to area generation,
                // because the redaction line corresponds to the descent line of the content.
                if (!clipperBridge.addPolylineSubjectToClipper(clipper, rect2)) {
                    return false;
                }
                if (rect1.length != rect2.length) {
                    return false;
                }
                Point startPoint = rect2[0];
                Point endPoint = rect2[0];
                for (int i = 1; i < rect2.length; i++) {
                    if (rect2[i].distance(startPoint) > EPS) {
                        endPoint = rect2[i];
                        break;
                    }
                }
                for (Point point : rect1) {
                    if (isPointOnALineSegment(point, startPoint, endPoint, true)) {
                        return true;
                    }
                }
            }
            return false;
        }
        // According to clipper documentation:
        // The function will return false if the path is invalid for clipping. A path is invalid for clipping when:
        // - it has less than 2 vertices;
        // - it has 2 vertices but is not an open path;
        // - the vertices are all co-linear and it is not an open path.
        // Reference: http://www.angusj.com/delphi/clipper/documentation/Docs/Units/ClipperLib/Classes/ClipperBase/Methods/AddPath.htm
        // If addition returns false, this means that there are less than 3 distinct points, because of rectangle zero width.
        // Let's in this case specify the path as polyline, because we still want to know if redaction area
        // intersects even with zero-width rectangles.
        boolean intersectionSubjectAdded = clipperBridge.addPolygonToClipper(clipper, rect1, PolyType.SUBJECT);
        if (intersectionSubjectAdded) {
            // working with paths is considered to be a bit faster in terms of performance.
            Paths paths = new Paths();
            clipper.execute(ClipType.INTERSECTION, paths, PolyFillType.NON_ZERO, PolyFillType.NON_ZERO);
            return checkIfIntersectionOccurs(paths, rect1, false, clipperBridge);
        }
        intersectionSubjectAdded = clipperBridge.addPolylineSubjectToClipper(clipper, rect1);
        if (!intersectionSubjectAdded) {
            // According to the comment above,
            // this could have happened only if all four passed points are actually the same point.
            // Adding here a point really close to the original point, to make sure it's not covered by the
            // intersecting rectangle.
            final double SMALL_DIFF = 0.01;
            final Point[] expandedRect1 = new Point[rect1.length + 1];
            System.arraycopy(rect1, 0, expandedRect1, 0, rect1.length);
            expandedRect1[rect1.length] = new Point(rect1[0].getX() + SMALL_DIFF, rect1[0].getY());
            rect1 = expandedRect1;

            intersectionSubjectAdded = clipperBridge.addPolylineSubjectToClipper(clipper, rect1);
            assert intersectionSubjectAdded;
        }
        PolyTree polyTree = new PolyTree();
        clipper.execute(ClipType.INTERSECTION, polyTree, PolyFillType.NON_ZERO, PolyFillType.NON_ZERO);
        return checkIfIntersectionOccurs(Paths.makePolyTreeToPaths(polyTree), rect1, true, clipperBridge);
    }

    private boolean checkIfIntersectionOccurs(Paths paths, Point[] rect1, boolean isDegenerate,
                                              ClipperBridge clipperBridge) {
        if (paths.isEmpty()) {
            return false;
        }
        final LongRect intersectionRectangle = paths.getBounds();
        // If the user defines a overlappingRatio we use this to calculate whether it intersects enough
        // To pass as an intersection
        if (properties.getOverlapRatio() == null) {
            return !checkIfIntersectionRectangleDegenerate(intersectionRectangle, isDegenerate, clipperBridge);
        }
        final double overlappedArea = CleanUpHelperUtil.calculatePolygonArea(rect1);
        final double intersectionArea = clipperBridge.longRectCalculateHeight(intersectionRectangle) *
                clipperBridge.longRectCalculateWidth(intersectionRectangle);
        final double percentageOfOverlapping = intersectionArea / overlappedArea;
        final float SMALL_VALUE_FOR_ROUNDING_ERRORS = 1e-5f;
        return percentageOfOverlapping + SMALL_VALUE_FOR_ROUNDING_ERRORS > properties.getOverlapRatio();
    }

    /**
     * Filter a TextRenderInfo object.
     *
     * @param text the TextRenderInfo to be filtered
     * @return a {@link FilterResult} object with filtered text.
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
                textArray.add(new PdfNumber(FontProgram.convertGlyphSpaceToTextSpace(-ri.getUnscaledWidth()) /
                        (text.getFontSize() * text.getHorizontalScaling() / FontProgram.HORIZONTAL_SCALING_FACTOR)
                ));
            }
        }

        return new FilterResult<PdfArray>(true, textArray);
    }

    /**
     * Filter an ImageRenderInfo object.
     *
     * @param image the ImageRenderInfo object to be filtered
     * @return an {@link FilterResult} object with filtered image data.filterStrokePath
     */
    FilterResult<ImageData> filterImage(ImageRenderInfo image) {
        return filterImage(image.getImage(), getImageAreasToBeCleaned(image.getImageCtm()));
    }

    FilterResult<ImageData> filterImage(FilteredImagesCache.FilteredImageKey imageKey) {
        return filterImage(imageKey.getImageXObject(), imageKey.getCleanedAreas());
    }

    /**
     * Filter a PathRenderInfo object.
     *
     * @param path the PathRenderInfo object to be filtered
     * @return a filtered {@link com.itextpdf.kernel.geom.Path} object.
     */
    com.itextpdf.kernel.geom.Path filterStrokePath(PathRenderInfo path) {
        PdfArray dashPattern = path.getLineDashPattern();
        LineDashPattern lineDashPattern = new LineDashPattern(dashPattern.getAsArray(0), dashPattern.getAsNumber(1).floatValue());

        return filterStrokePath(path.getPath(), path.getCtm(), path.getLineWidth(), path.getLineCapStyle(),
                path.getLineJoinStyle(), path.getMiterLimit(), lineDashPattern);
    }

    /**
     * Filter a PathRenderInfo object.
     *
     * @param path        the PathRenderInfo object to be filtered
     * @param fillingRule an integer parameter, specifying whether the subpath is contour.
     *                    If the subpath is contour, pass any value.
     * @return a filtered {@link com.itextpdf.kernel.geom.Path} object.
     */
    com.itextpdf.kernel.geom.Path filterFillPath(PathRenderInfo path, int fillingRule) {
        return filterFillPath(path.getPath(), path.getCtm(), fillingRule);
    }

    FilteredImagesCache.FilteredImageKey createFilteredImageKey(PdfImageXObject image, Matrix imageCtm, PdfDocument document) {
        return FilteredImagesCache.createFilteredImageKey(image, getImageAreasToBeCleaned(imageCtm), document);
    }

    /**
     * Note: this method will close all unclosed subpaths of the passed path.
     *
     * @param path        the PathRenderInfo object to be filtered.
     * @param ctm         a {@link com.itextpdf.kernel.geom.Path} transformation matrix.
     * @param fillingRule If the subpath is contour, pass any value.
     * @return a filtered {@link com.itextpdf.kernel.geom.Path} object.
     */
    private com.itextpdf.kernel.geom.Path filterFillPath(com.itextpdf.kernel.geom.Path path,
                                                         Matrix ctm, int fillingRule) {
        path.closeAllSubpaths();

        List<Point[]> transfRectVerticesList = new ArrayList<>();
        for (Rectangle rectangle : regions) {
            try {
                transfRectVerticesList.add(transformPoints(ctm, true, getRectangleVertices(rectangle)));
            } catch (PdfException e) {
                if (!(e.getCause() instanceof NoninvertibleTransformException)) {
                    throw e;
                } else {
                    logger.error(MessageFormatUtil.format(CleanUpLogMessageConstant.FAILED_TO_PROCESS_A_TRANSFORMATION_MATRIX));
                }
            }

        }

        IClipper clipper = new DefaultClipper();

        ClipperBridge clipperBridge = properties.getOffsetProperties().calculateOffsetMultiplierDynamically() ?
                getClipperBridge(path, transfRectVerticesList) : new ClipperBridge();
        clipperBridge.addPath(clipper, path, PolyType.SUBJECT);

        for (Point[] transfRectVertices : transfRectVerticesList) {
            clipperBridge.addPolygonToClipper(clipper, transfRectVertices, PolyType.CLIP);
        }

        PolyFillType fillType = PolyFillType.NON_ZERO;

        if (fillingRule == PdfCanvasConstants.FillingRule.EVEN_ODD) {
            fillType = PolyFillType.EVEN_ODD;
        }

        PolyTree resultTree = new PolyTree();
        clipper.execute(ClipType.DIFFERENCE, resultTree, fillType, PolyFillType.NON_ZERO);

        return clipperBridge.convertToPath(resultTree);
    }

    /**
     * Calculates intersection of the image and the render filter region in the coordinate system relative to the image.
     *
     * @return {@code null} if the image is fully covered and therefore is completely cleaned, {@link java.util.List} of
     * {@link Rectangle} objects otherwise.
     */
    private List<Rectangle> getImageAreasToBeCleaned(Matrix imageCtm) {
        Rectangle imageRect = calcImageRect(imageCtm);
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

                areasToBeCleaned.add(transformRectIntoImageCoordinates(intersectionRect, imageCtm));
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

        if (lineDashPattern != null && !lineDashPattern.isSolid()) {
            path = LineDashPattern.applyDashPattern(path, lineDashPattern);
        }

        ClipperBridge clipperBridge = properties.getOffsetProperties().calculateOffsetMultiplierDynamically() ?
                new ClipperBridge(path) : new ClipperBridge();
        ClipperOffset offset = new ClipperOffset(miterLimit, properties.getOffsetProperties().getArcTolerance() *
                clipperBridge.getFloatMultiplier());
        List<Subpath> degenerateSubpaths = clipperBridge.addPath(offset, path, joinType, endType);

        PolyTree resultTree = new PolyTree();
        offset.execute(resultTree, lineWidth * clipperBridge.getFloatMultiplier() / 2);
        Path offsetedPath = clipperBridge.convertToPath(resultTree);

        if (!degenerateSubpaths.isEmpty()) {
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
     * Returns whether the given TextRenderInfo object needs to be cleaned up.
     *
     * @param renderInfo the input TextRenderInfo object
     */
    private boolean isTextNotToBeCleaned(TextRenderInfo renderInfo) {
        Point[] textRect = getTextRectangle(renderInfo);

        for (Rectangle region : regions) {
            Point[] redactRect = getRectangleVertices(region);

            // Text rectangle might be rotated, hence we are using precise polygon intersection checker and not
            // just intersecting two rectangles that are parallel to the x and y coordinate vectors
            if (checkIfRectanglesIntersect(textRect, redactRect)) {
                return false;
            }
        }

        return true;
    }

    private static FilterResult<ImageData> filterImage(PdfImageXObject image, List<Rectangle> imageAreasToBeCleaned) {
        if (imageAreasToBeCleaned == null) {
            return new FilterResult<>(true, null);
        } else if (imageAreasToBeCleaned.isEmpty()) {
            return new FilterResult<>(false, null);
        }

        byte[] filteredImageBytes;
        if (imageSupportsDirectCleanup(image)) {
            byte[] imageStreamBytes = processImageDirectly(image, imageAreasToBeCleaned);
            // Creating imageXObject clone in order to avoid modification of the original XObject in the document.
            // We require to set filtered image bytes to the image XObject only for the sake of simplifying code:
            // in this method we return ImageData, so in order to convert PDF image to the common image format we
            // reuse PdfImageXObject#getImageBytes method.
            // I think this is acceptable here, because monochrome and grayscale images are not very common,
            // so the overhead would be not that big. But anyway, this should be refactored in future if this
            // direct image bytes cleaning approach would be found useful and will be preserved in future.
            PdfImageXObject tempImageClone = new PdfImageXObject((PdfStream) image.getPdfObject().clone());
            tempImageClone.getPdfObject().setData(imageStreamBytes);
            filteredImageBytes = tempImageClone.getImageBytes();
        } else {
            byte[] originalImageBytes = image.getImageBytes();
            filteredImageBytes = CleanUpImageUtil.cleanUpImage(originalImageBytes, imageAreasToBeCleaned);
        }
        return new FilterResult<>(true, ImageDataFactory.create(filteredImageBytes));
    }

    /**
     * Checks if the input intersection rectangle is degenerate.
     * In case of intersection subject is degenerate (isIntersectSubjectDegenerate
     * is true) and it is included into intersecting rectangle, this method returns false,
     * despite the intersection rectangle is degenerate.
     *
     * @param rect                         intersection rectangle
     * @param isIntersectSubjectDegenerate value, specifying if the intersection subject
     *                                     is degenerate.
     * @return true - if the intersection rectangle is degenerate.
     */
    private static boolean checkIfIntersectionRectangleDegenerate(LongRect rect, boolean isIntersectSubjectDegenerate,
                                                                  ClipperBridge clipperBridge) {
        final float width = clipperBridge.longRectCalculateWidth(rect);
        final float height = clipperBridge.longRectCalculateHeight(rect);
        return isIntersectSubjectDegenerate ? (width < EPS && height < EPS) : (width < EPS || height < EPS);
    }

    private static boolean isPointOnALineSegment(Point currPoint, Point linePoint1, Point linePoint2, boolean isBetweenLinePoints) {
        double dxc = currPoint.getX() - linePoint1.getX();
        double dyc = currPoint.getY() - linePoint1.getY();

        double dxl = linePoint2.getX() - linePoint1.getX();
        double dyl = linePoint2.getY() - linePoint1.getY();

        double cross = dxc * dyl - dyc * dxl;

        // if point is on a line, let's check whether it's between provided line points
        if (Math.abs(cross) <= EPS) {
            if (isBetweenLinePoints) {
                if (Math.abs(dxl) >= Math.abs(dyl)) {
                    return dxl > 0 ?
                            linePoint1.getX() - EPS <= currPoint.getX() && currPoint.getX() <= linePoint2.getX() + EPS :
                            linePoint2.getX() - EPS <= currPoint.getX() && currPoint.getX() <= linePoint1.getX() + EPS;
                } else {
                    return dyl > 0 ?
                            linePoint1.getY() - EPS <= currPoint.getY() && currPoint.getY() <= linePoint2.getY() + EPS :
                            linePoint2.getY() - EPS <= currPoint.getY() && currPoint.getY() <= linePoint1.getY() + EPS;
                }
            } else {
                return true;
            }
        }

        return false;
    }

    private static boolean isSupportedFilterForDirectImageCleanup(PdfObject filter) {
        if (filter == null) {
            return true;
        }
        if (filter.isName()) {
            return !NOT_SUPPORTED_FILTERS_FOR_DIRECT_CLEANUP.contains((PdfName) filter);
        } else if (filter.isArray()) {
            PdfArray filterArray = (PdfArray) filter;
            for (int i = 0; i < filterArray.size(); ++i) {
                if (NOT_SUPPORTED_FILTERS_FOR_DIRECT_CLEANUP.contains(filterArray.getAsName(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @return Image boundary rectangle in device space.
     */
    private static Rectangle calcImageRect(Matrix imageCtm) {
        if (imageCtm == null) {
            return null;
        }

        Point[] points = transformPoints(imageCtm, false,
                new Point(0d, 0d), new Point(0d, 1d),
                new Point(1d, 0d), new Point(1d, 1d));

        return Rectangle.calculateBBox(Arrays.asList(points));
    }

    /**
     * Transforms the given Rectangle into the image coordinate system which is [0,1]x[0,1] by default.
     */
    private static Rectangle transformRectIntoImageCoordinates(Rectangle rect, Matrix imageCtm) {
        Point[] points = transformPoints(imageCtm, true, new Point(rect.getLeft(), rect.getBottom()),
                new Point(rect.getLeft(), rect.getTop()),
                new Point(rect.getRight(), rect.getBottom()),
                new Point(rect.getRight(), rect.getTop()));
        return Rectangle.calculateBBox(Arrays.asList(points));
    }

    /**
     * Filters image content using direct manipulation over PDF image samples stream. Implemented according to ISO 32000-2,
     * "8.9.3 Sample representation".
     *
     * @param image                 image XObject which will be filtered
     * @param imageAreasToBeCleaned list of rectangle areas for clean up with coordinates in (0,1)x(0,1) space
     * @return raw bytes of the PDF image samples stream which is already cleaned.
     */
    private static byte[] processImageDirectly(PdfImageXObject image, List<Rectangle> imageAreasToBeCleaned) {
        int X = 0;
        int Y = 1;
        int W = 2;
        int H = 3;

        byte[] originalImageBytes = image.getPdfObject().getBytes();

        PdfNumber bpcVal = image.getPdfObject().getAsNumber(PdfName.BitsPerComponent);
        if (bpcVal == null) {
            throw new IllegalArgumentException("/BitsPerComponent entry is required for image dictionaries.");
        }
        int bpc = bpcVal.intValue();
        if (bpc != 1 && bpc != 2 && bpc != 4 && bpc != 8 && bpc != 16) {
            throw new IllegalArgumentException("/BitsPerComponent only allowed values are: 1, 2, 4, 8 and 16.");
        }

        double bytesInComponent = (double) bpc / 8;
        int firstComponentInByte = 0;
        if (bpc < 16) {
            for (int i = 0; i < bpc; ++i) {
                firstComponentInByte += (int) Math.pow(2, 7 - i);
            }
        }

        double width = image.getWidth();
        double height = image.getHeight();
        int rowPadding = 0;
        if ((width * bpc) % 8 > 0) {
            rowPadding = (int) (8 - (width * bpc) % 8);
        }
        for (Rectangle rect : imageAreasToBeCleaned) {
            int[] cleanImgRect = CleanUpHelperUtil.getImageRectToClean(rect, (int) width, (int) height);
            for (int j = cleanImgRect[Y]; j < cleanImgRect[Y] + cleanImgRect[H]; ++j) {
                for (int i = cleanImgRect[X]; i < cleanImgRect[X] + cleanImgRect[W]; ++i) {
                    // based on assumption that numOfComponents always equals 1, because this method is only for monochrome and grayscale images
                    double pixelPos = j * ((width * bpc + rowPadding) / 8) + i * bytesInComponent;
                    int pixelByteInd = (int) pixelPos;
                    byte byteWithSample = originalImageBytes[pixelByteInd];

                    if (bpc == 16) {
                        originalImageBytes[pixelByteInd] = 0;
                        originalImageBytes[pixelByteInd + 1] = 0;
                    } else {
                        int reset = ~(firstComponentInByte >> (int) ((pixelPos - pixelByteInd) * 8)) & 0xFF;
                        originalImageBytes[pixelByteInd] = (byte) (byteWithSample & reset);
                    }
                }
            }
        }

        return originalImageBytes;
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
            return Collections.<Subpath>emptyList();
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
     * Approximates a given Path with a List of Point objects.
     *
     * @param path input path
     */
    private static List<Point> getPathApproximation(com.itextpdf.kernel.geom.Path path) {
        ApproxPointList<Point> approx = new ApproxPointList<Point>();
        for (Subpath subpath : path.getSubpaths()) {
            approx.addAllPoints(subpath.getPiecewiseLinearApproximation());
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
     * Approximate a circle with 4 Bezier curves (one for each 90 degrees sector).
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
                throw new PdfException(CleanupExceptionMessageConstant.NONINVERTIBLE_MATRIX_CANNOT_BE_PROCESSED, e);
            }
        }

        t.transform(points, 0, transformed, 0, points.length);

        return transformed;
    }

    /**
     * Get the bounding box of a TextRenderInfo object.
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
        return new Point[]{
                new Point(rect.getLeft(), rect.getBottom()),
                new Point(rect.getRight(), rect.getBottom()),
                new Point(rect.getRight(), rect.getTop()),
                new Point(rect.getLeft(), rect.getTop())
        };
    }

    /**
     * Calculate the intersection of 2 Rectangles.
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

    private static ClipperBridge getClipperBridge(Path path, List<Point[]> transfRectVerticesList) {
        List<Point> pointsList = new ArrayList<>();
        for (Subpath subpath : path.getSubpaths()) {
            if (!subpath.isSinglePointClosed() && !subpath.isSinglePointOpen()) {
                pointsList.addAll(subpath.getPiecewiseLinearApproximation());
            }
        }
        for (Point[] transfRectVertices : transfRectVerticesList) {
            pointsList.addAll(Arrays.asList(transfRectVertices));
        }
        return new ClipperBridge(pointsList.toArray(new Point[0]));
    }

    /**
     * Generic class representing the result of filtering an object of type T.
     */
    static class FilterResult<T> {
        private final boolean isModified;
        private final T filterResult;

        public FilterResult(boolean isModified, T filterResult) {
            this.isModified = isModified;
            this.filterResult = filterResult;
        }

        /**
         * Get whether the object was modified or not.
         *
         * @return true if the object was modified, false otherwise
         */
        boolean isModified() {
            return isModified;
        }

        /**
         * Get the result after filtering
         *
         * @return the result of filtering an object of type T.
         */
        T getFilterResult() {
            return filterResult;
        }
    }

    private static class ApproxPointList<T> extends ArrayList<Point> {

        public ApproxPointList() {
            super();
        }

        public boolean addAllPoints(Collection<Point> c) {
            Point prevPoint = (size() - 1 < 0 ? null : get(size() - 1));

            for (Point pt : c) {
                if (!pt.equals(prevPoint)) {
                    add(pt);
                    prevPoint = pt;
                }
            }

            return true;
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
