/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2017 iText Group NV
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
package com.itextpdf.pdfcleanup.autosweep;

import com.itextpdf.kernel.geom.LineSegment;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.geom.Vector;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Joris Schellekens on 3/23/2017.
 */
public class CharacterRenderInfo implements Comparable<CharacterRenderInfo> {

    private String text;

    private Rectangle rectangle;

    private Vector startLocation;
    private Vector endLocation;

    private Vector orientationVector;
    private int orientationMagnitude;

    private int distPerpendicular;
    private float distParallelStart;
    private float distParallelEnd;

    private float charSpaceWidth;

    public CharacterRenderInfo(TextRenderInfo tri) {
        this(tri.getText(), toRectangle(tri), tri.getSingleSpaceWidth());
    }

    public CharacterRenderInfo(String s, Rectangle r, float charSpaceWidth) {
        this.text = s;
        this.rectangle = r;
        this.charSpaceWidth = charSpaceWidth;

        // set start and end location
        startLocation = new Vector(r.getX(), r.getY(), 0);
        endLocation = new Vector(r.getX() + r.getWidth(), r.getY(), 0);

        // calculate orientation
        Vector oVector = endLocation.subtract(startLocation);
        if (oVector.length() == 0) {
            oVector = new Vector(1, 0, 0);
        }
        orientationVector = oVector.normalize();
        orientationMagnitude = (int) (Math.atan2(orientationVector.get(Vector.I2), orientationVector.get(Vector.I1)) * 1000);

        // see http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
        // the two vectors we are crossing are in the same plane, so the result will be purely
        // in the z-axis (out of plane) direction, so we just take the I3 component of the result
        Vector origin = new Vector(0, 0, 1);
        distPerpendicular = (int) (startLocation.subtract(origin)).cross(orientationVector).get(Vector.I3);

        distParallelStart = orientationVector.dot(startLocation);
        distParallelEnd = orientationVector.dot(endLocation);
    }

    private static Rectangle toRectangle(TextRenderInfo tri) {

        float x0 = tri.getDescentLine().getStartPoint().get(0);
        float y0 = tri.getDescentLine().getStartPoint().get(1);

        float h = tri.getAscentLine().getStartPoint().get(1) - tri.getDescentLine().getStartPoint().get(1);
        float w = java.lang.Math.abs(tri.getBaseline().getStartPoint().get(0) - tri.getBaseline().getEndPoint().get(0));

        return new Rectangle(x0, y0, w, h);
    }

    public static StringConversionInfo toString(List<CharacterRenderInfo> cris) {
        Map<Integer, Integer> indexMap = new HashMap<>();

        StringBuilder sb = new StringBuilder();
        CharacterRenderInfo lastChunk = null;
        for (int i = 0; i < cris.size(); i++) {
            CharacterRenderInfo chunk = cris.get(i);
            if (lastChunk == null) {
                indexMap.put(sb.length(), i);
                sb.append(chunk.getText());
            } else {
                if (chunk.sameLine(lastChunk)) {
                    // we only insert a blank space if the trailing character of the previous string wasn't a space, and the leading character of the current string isn't a space
                    if (lastChunk.isChunkAtWordBoundary(lastChunk, chunk) && !chunk.getText().startsWith(" ") && !chunk.getText().endsWith(" ")) {
                        sb.append(' ');
                    }
                    indexMap.put(sb.length(), i);
                    sb.append(chunk.getText());
                } else {
                    indexMap.put(sb.length(), i);
                    sb.append(chunk.getText());
                }
            }
            lastChunk = chunk;
        }
        StringConversionInfo ret = new StringConversionInfo();
        ret.indexMap = indexMap;
        ret.text = sb.toString();
        return ret;
    }

    public Rectangle getRectangle() {
        return rectangle;
    }

    public String getText() {
        return text;
    }

    @Override
    public int compareTo(CharacterRenderInfo other) {
        if (this == other) return 0; // not really needed, but just in case

        LineSegment mySegment = new LineSegment(startLocation, endLocation);
        LineSegment otherSegment = new LineSegment(other.startLocation, other.endLocation);
        if (other.startLocation.equals(other.endLocation) && mySegment.containsSegment(otherSegment) || startLocation.equals(endLocation) && otherSegment.containsSegment(mySegment)) {
            // Return 0 to save order due to stable sort. This handles situation of mark glyphs that have zero width
            return 0;
        }

        int result;
        result = Integer.compare(orientationMagnitude, other.orientationMagnitude);
        if (result != 0) return result;

        result = Integer.compare(distPerpendicular, other.distPerpendicular);
        if (result != 0) return result;

        return Float.compare(distParallelStart, other.distParallelStart);
    }

    public boolean sameLine(CharacterRenderInfo other) {
        return orientationMagnitude == other.orientationMagnitude && distPerpendicular == other.distPerpendicular;
    }

    private boolean isChunkAtWordBoundary(CharacterRenderInfo c0, CharacterRenderInfo c1) {
        /**
         * Here we handle a very specific case which in PDF may look like:
         * -.232 Tc [( P)-226.2(r)-231.8(e)-230.8(f)-238(a)-238.9(c)-228.9(e)]TJ
         * The font's charSpace width is 0.232 and it's compensated with charSpacing of 0.232.
         * And a resultant TextChunk.charSpaceWidth comes to TextChunk constructor as 0.
         * In this case every chunk is considered as a word boundary and space is added.
         * We should consider charSpaceWidth equal (or close) to zero as a no-space.
         */
        if (charSpaceWidth < 0.1f) {
            return false;
        }

        // In case a text chunk is of zero length, this probably means this is a mark character,
        // and we do not actually want to insert a space in such case
        if (c0.getRectangle().getWidth() == 0 || c1.getRectangle().getWidth() == 0) {
            return false;
        }

        float dist = c1.distParallelStart - c0.distParallelEnd;
        return dist < -charSpaceWidth || dist > charSpaceWidth / 2.0f;
    }

    static class StringConversionInfo {
        public String text = null;
        public Map<Integer, Integer> indexMap = new HashMap<>();
    }
}
