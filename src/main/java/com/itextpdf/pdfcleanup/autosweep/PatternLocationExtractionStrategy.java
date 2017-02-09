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

import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.geom.LineSegment;
import com.itextpdf.kernel.geom.Matrix;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is the default implementation of {@code ILocationExtractionStrategy}.
 * It enables autoSweep to mark Rectangles for redaction based on regular expressions.
 */
public class PatternLocationExtractionStrategy implements ILocationExtractionStrategy {

    // parsing state
    private List<CharacterRenderInfo> parseResult = new ArrayList<>();

    // redaction properties
    private Pattern pattern;
    private Color redactionColor = Color.BLACK;
    private boolean useActualText = true;

    public PatternLocationExtractionStrategy(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public PatternLocationExtractionStrategy(Pattern regex) { this.pattern = regex; }

    @Override
    public String getResultantText() {
        return null;
    }

    @Override
    public Set<EventType> getSupportedEvents() {
        return null;
    }

    @Override
    public Color getColor(Rectangle rect) {
        return redactionColor;
    }

    @Override
    public Collection<Rectangle> getLocations() {

        // align characters in "logical" order
        Collections.sort(parseResult);

        // process parse results
        List<Rectangle> rectangles = new ArrayList<>();

        CharacterRenderInfo.StringConversionInfo txt = CharacterRenderInfo.toString(parseResult);

        Matcher mat = pattern.matcher(txt.text);
        while (mat.find()) {
            int startIndex = txt.indexMap.get(mat.start());
            int endIndex = txt.indexMap.get(mat.end());
            for (Rectangle r : toRectangles(parseResult.subList(startIndex, endIndex))) {
                rectangles.add(r);
            }
        }

        // sort
        java.util.Collections.sort(rectangles, new Comparator<Rectangle>() {
            @Override
            public int compare(Rectangle o1, Rectangle o2) {
                if (o1.getY() == o2.getY()) {
                    return o1.getX() == o2.getX() ? 0 : (o1.getX() < o2.getX() ? -1 : 1);
                } else {
                    return o1.getY() < o2.getY() ? -1 : 1;
                }
            }
        });

        return rectangles;
    }

    /**
     * Converts {@code CharacterRenderInfo} objects to {@code Rectangles}
     * This method is public and not final so that custom implementations can choose to override it.
     * E.g. other implementations may choose to add padding/margin to the Rectangles.
     * This method also offers a convenient access point to the mapping of {@code CharacterRenderInfo} to {@code Rectangle}.
     * This mapping enables (custom implementations) to match color of text in redacted Rectangles,
     * or match color of background, by the mere virtue of offering access to the {@code CharacterRenderInfo} objects
     * that generated the {@code Rectangle}.
     *
     * @param cris
     * @return
     */
    public List<Rectangle> toRectangles(List<CharacterRenderInfo> cris) {
        List<Rectangle> retval = new ArrayList<>();
        if (cris.isEmpty())
            return retval;

        int prev = 0;
        int curr = 0;
        while (curr < cris.size()) {
            while (curr < cris.size() && cris.get(curr).sameLine(cris.get(prev))) {
                curr++;
            }
            float x = cris.get(prev).getRectangle().getX();
            float y = cris.get(prev).getRectangle().getY();
            float w = cris.get(curr - 1).getRectangle().getX() - cris.get(prev).getRectangle().getX() + cris.get(curr - 1).getRectangle().getWidth();
            float h = 0f;
            for (CharacterRenderInfo cri : cris.subList(prev, curr)) {
                h = Math.max(h, cri.getRectangle().getHeight());
            }
            retval.add(new Rectangle(x, y, w, h));
            prev = curr;
        }

        // return
        return retval;
    }

    /*
     * FLUENT interface
     */

    public PatternLocationExtractionStrategy setRedactionColor(Color redactionColor) {
        this.redactionColor = redactionColor;
        return this;
    }

    public PatternLocationExtractionStrategy setUseActualText(boolean useActualText) {
        this.useActualText = useActualText;
        return this;
    }

    /*
     * TEXT RENDER INFO PROCESSING
     */

    /**
     * Convert {@code TextRenderInfo} to {@code CharacterRenderInfo}
     * This method is public and not final so that custom implementations can choose to override it.
     * Other implementations of {@code CharacterRenderInfo} may choose to store different properties than
     * merely the {@code Rectangle} describing the bounding box. E.g. a custom implementation might choose to
     * store {@code Color} information as well, to better match the content surrounding the redaction {@code Rectangle}.
     *
     * @param tri
     * @return
     */
    public List<CharacterRenderInfo> toCRI(TextRenderInfo tri) {
        List<CharacterRenderInfo> cris = new ArrayList<>();
        for (TextRenderInfo subTri : tri.getCharacterRenderInfos()) {
            cris.add(new CharacterRenderInfo(subTri));
        }
        return cris;
    }

    @Override
    public void eventOccurred(IEventData data, EventType type) {

        // we are only interested in events that render text
        if (!type.equals(EventType.RENDER_TEXT))
            return;

        TextRenderInfo renderInfo = (TextRenderInfo) data;

        LineSegment segment = renderInfo.getBaseline();

        // handle subscript and subscript
        if (renderInfo.getRise() != 0) {
            Matrix riseOffsetTransform = new Matrix(0, -renderInfo.getRise());
            segment = segment.transformBy(riseOffsetTransform);
        }

        parseResult.addAll(toCRI(renderInfo));
    }

    @Override
    public void clear() {
        parseResult.clear();
    }

}
