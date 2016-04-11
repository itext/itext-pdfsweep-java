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
package com.itextpdf.pdfcleanup;


import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.geom.BezierCurve;
import com.itextpdf.kernel.geom.Path;
import com.itextpdf.kernel.geom.Point2D;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.geom.Shape;
import com.itextpdf.kernel.geom.Subpath;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.CanvasTag;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.PathRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfLiteral;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfResources;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfTextArray;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.PdfCanvasConstants.FillingRule;
import com.itextpdf.kernel.pdf.tagutils.TagTreePointer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

public class PdfCleanUpProcessor extends PdfCanvasProcessor {

    private static final Set<String> textShowingOperators = new HashSet<String>(Arrays.asList("TJ", "Tj", "'", "\""));
    private static final Set<String> pathConstructionOperators = new HashSet<String>(Arrays.asList("m", "l", "c", "v", "y", "h", "re"));
    private static final Set<String> strokeOperators = new HashSet<String>(Arrays.asList("S", "s", "B", "B*", "b", "b*"));
    private static final Set<String> nwFillOperators = new HashSet<String>(Arrays.asList("f", "F", "B", "b"));
    private static final Set<String> eoFillOperators = new HashSet<String>(Arrays.asList("f*", "B*", "b*"));
    private static final Set<String> pathPaintingOperators = new HashSet<String>() {{
        addAll(strokeOperators);
        addAll(nwFillOperators);
        addAll(eoFillOperators);
        add("n");
    }};
    private static final Set<String> clippingPathOperators = new HashSet<String>(Arrays.asList("W", "W*"));
    private static final Set<String> lineStyleOperators = new HashSet<String>(Arrays.asList("w", "J", "j", "M", "d"));
    private static final Set<String> markedContentOperators = new HashSet<String>(Arrays.asList("BMC", "BDC", "EMC"));

    private PdfDocument document;
    private PdfPage currentPage;
    private PdfCleanUpFilter filter;
    private Stack<PdfCanvas> canvasStack;

    private Deque<CanvasTag> notWrittenTags;

    public PdfCleanUpProcessor(List<Rectangle> cleanUpRegions, PdfDocument document) {
        super(new PdfCleanUpEventListener());
        this.document = document;
        this.filter = new PdfCleanUpFilter(cleanUpRegions);
        this.canvasStack = new Stack<>();
        this.notWrittenTags = new ArrayDeque<>();
    }

    @Override
    public void processPageContent(PdfPage page) {
        currentPage = page;
        super.processPageContent(page);
    }

    /**
     * @param contentBytes the bytes of a content stream
     * @param resources    the resources of the content stream. Must not be null.
     */
    @Override
    public void processContent(byte[] contentBytes, PdfResources resources) {
        canvasStack.push(new PdfCanvas(new PdfStream(), resources, document));
        if (canvasStack.size() == 1) {
            // If it is the first canvas, we begin to wrap it with q
            canvasStack.peek().saveState();
        }

        super.processContent(contentBytes, resources);
        // Here we don't pop() canvases by intent. It is the responsibility of the one who utilizes the canvas data
    }

    @Override
    public PdfCleanUpEventListener getEventListener() {
        return (PdfCleanUpEventListener) eventListener;
    }

    public PdfCanvas popCleanedCanvas() {
        // If it is the last canvas, we finish to wrap it with Q
        if (canvasStack.size() == 1) {
            canvasStack.peek().restoreState();
        }
        return canvasStack.pop();
    }

    @Override
    protected void invokeOperator(PdfLiteral operator, List<PdfObject> operands) {
        super.invokeOperator(operator, operands);
        popCanvasIfFormXObject(operator.toString(), operands);

        boolean disableOutput = cleanContent(operator.toString(), operands);
        if (!disableOutput) {
            writeOperands(canvasStack.peek(), operands);
        }
    }

    @Override
    protected void beginMarkedContent(PdfName tag, PdfDictionary dict) {
        super.beginMarkedContent(tag, dict);
        notWrittenTags.push(new CanvasTag(tag).addProperties(dict));
    }

    private void popCanvasIfFormXObject(String operator, List<PdfObject> operands) {
        if ("Do".equals(operator)) {
            PdfStream formStream = getXObjectStream((PdfName) operands.get(0));
            if (PdfName.Form.equals(formStream.getAsName(PdfName.Subtype))) {
                formStream.setData(popCleanedCanvas().getContentStream().getBytes());
            }
        }
    }

    private boolean cleanContent(String operator, List<PdfObject> operands) {
        boolean disableOutput = false;

        if (textShowingOperators.contains(operator)) {
            cleanText(operator, operands);
            disableOutput = true;
        } else if ("Do".equals(operator)) { // TODO inline image case
            disableOutput = checkIfImageAndClean(operands);
        } else if (pathPaintingOperators.contains(operator)) {
            writePath();
            disableOutput = true;
        } else if (pathConstructionOperators.contains(operator) || clippingPathOperators.contains(operator)
                || lineStyleOperators.contains(operator)) {
            disableOutput = true;
        } else if (markedContentOperators.contains(operator)) {
            if ("EMC".equals(operator)) {
                removeTagIfNotWritten();
            }
            disableOutput = true;
        }

        return disableOutput;
    }

    private void cleanText(String operator, List<PdfObject> operands) {
        PdfCanvas canvas = canvasStack.peek();
        if ("'".equals(operator)) {
            canvas.newlineText();
        } else if ("\"".equals(operator)) {
            PdfNumber wordSpacing = (PdfNumber) operands.get(0);
            PdfNumber charSpacing = (PdfNumber) operands.get(1);
            canvas.setWordSpacing(wordSpacing.getFloatValue())
                    .setCharacterSpacing(charSpacing.getFloatValue())
                    .newlineText();
        }

        List<TextRenderInfo> textChunks = getEventListener().getEncounteredText();
        PdfArray cleanedText;
        if ("TJ".equals(operator)) {
            PdfArray originalTJ = (PdfArray)operands.get(0);
            int i = 0; // text chunk index in original TJ
            PdfTextArray newTJ = new PdfTextArray();
            for (PdfObject e : originalTJ) {
                if (e.isString()) {
                    PdfArray filteredText = filter.filterText(textChunks.get(i++));
                    newTJ.addAll(filteredText);
                } else {
                    newTJ.add(e);
                }
            }

            cleanedText = newTJ;
        } else { // if operator is Tj or ' or "
            cleanedText = filter.filterText(textChunks.get(0));
        }

        if (!cleanedText.isEmpty()) {
            // TODO Here we apply a hack. Review this later.
            // The problem is, that in PdfCleanUpProcessor we write most of the operators directly to output stream, and
            // not with PdfCanvas. But in some cases (as here) we need PdfCanvas to write changed operators. PdfCanvas
            // keeps track of the current graphics state and sometimes PdfCanvas behaviour depends on the current
            // graphics state. For example, PdfCanvas forbids using text showing operators if in graphics state Font
            // isn't set. As "Tf" is one of the operators which we write directly, PdfCanvas doesn't know that it is used.
            // Similar thing happens when we write colors using PdfCanvas: if color is the current color in graphics state,
            // color operator is not written. However, most of the color operators in PdfCleanUpProcessor are written
            // directly, therefore PdfCanvas doesn't know which color is current.
            //
            // As a workaround we modify current graphics state here, so it would not throw an exception.
            canvas.getGraphicsState().setFont(textChunks.get(0).getFont());
            canvas.getGraphicsState().setFontSize(textChunks.get(0).getFontSize());

            if (cleanedText.size() != 1 || !cleanedText.get(0).isNumber()) {
                openNotWrittenTags();
            }

            canvas.showText(cleanedText);
        }
    }

    /**
     * @return true - if image was completely removed, otherwise - false
     */
    private boolean checkIfImageAndClean(List<PdfObject> operands) {
        PdfStream imageStream = getXObjectStream((PdfName) operands.get(0));
        if (PdfName.Image.equals(imageStream.getAsName(PdfName.Subtype))) {
            ImageRenderInfo encounteredImage = getEventListener().getEncounteredImage();
            PdfStream filteredImage = filter.filterImage(encounteredImage);
            if (filteredImage != null) {
                imageStream.clear();
                imageStream.setData(filteredImage.getBytes(false));
                imageStream.putAll(filteredImage);

                openNotWrittenTags();
            } else {
                return true;
            }
        }
        return false;
    }

    private void writePath() {
        PathRenderInfo path = getEventListener().getEncounteredPath();

        boolean stroke = (path.getOperation() & PathRenderInfo.STROKE) == PathRenderInfo.STROKE;
        boolean fill = (path.getOperation() & PathRenderInfo.FILL) == PathRenderInfo.FILL;
        boolean clip = path.isPathModifiesClippingPath();

        // Here we intentionally draw all three paths separately and not combining them in any way:

        // First of all, stroke converted to fill paths, therefore it could not be combined with fill (if it is
        // stroke-fill operation) or clip paths, and also it should be drawn after the fill, because in case it's
        // stroke-fill operation stroke should be "on top" of the filled area.

        // Secondly, current clipping path modifying happens AFTER the path painting. So if it is drawn separately, clip
        // path should be the last one.

        // So consider the situation when it is stroke-fill operation and also this path is marked as clip path.
        // And here we have it: fill path is the first, stroke path is the second and clip path is the last. And
        // stroke path could not be combined with neither fill nor clip paths.

        // Some improved logic could be applied to distinguish the cases when some paths actually could be drawn as one,
        // but this is the only generic solution.

        Path fillPath = null;
        PdfCanvas canvas = canvasStack.peek();
        if (fill) {
            fillPath = filter.filterFillPath(path, path.getRule());
            if (!fillPath.isEmpty()) {
                openNotWrittenTags();
                writePath(fillPath);
                if (path.getRule() == FillingRule.NONZERO_WINDING) {
                    canvas.fill();
                } else { // FillingRule.EVEN_ODD
                    canvas.eoFill();
                }
            }
        }

        if (stroke) {
            Path strokePath = filter.filterStrokePath(path);
            if (!strokePath.isEmpty()) {
                openNotWrittenTags();
                writeStrokePath(strokePath, path.getStrokeColor());
            }
        }

        if (clip) {
            Path clippingPath;
            if (fill && path.getClippingRule() == path.getRule()) {
                clippingPath = fillPath;
            } else {
                clippingPath = filter.filterFillPath(path, path.getClippingRule());
            }
            if (!clippingPath.isEmpty()) {
                openNotWrittenTags();
                writePath(clippingPath);
                if (path.getClippingRule() == FillingRule.NONZERO_WINDING) {
                    canvas.clip();
                } else { // FillingRule.EVEN_ODD
                    canvas.eoClip();
                }
            } else {
                // If the clipping path from the source document is cleaned (it happens when reduction
                // area covers the path completely), then you should treat it as an empty set (no points
                // are included in the path). Then the current clipping path (which is the intersection
                // between previous clipping path and the new one) is also empty set, which means that
                // there is no visible content at all. But at the same time as we removed the clipping
                // path, the invisible content would become visible. So, to emulate the correct result,
                // we would simply put a degenerate clipping path which consists of a single point at (0, 0).
                canvas.moveTo(0, 0).clip();
            }
            canvas.newPath();
        }
    }

    private void writePath(Path path) {
        PdfCanvas canvas = canvasStack.peek();
        for (Subpath subpath : path.getSubpaths()) {
            canvas.moveTo((float)subpath.getStartPoint().getX(), (float) subpath.getStartPoint().getY());

            for (Shape segment : subpath.getSegments()) {
                if (segment instanceof BezierCurve) {

                    List<Point2D> basePoints = segment.getBasePoints();
                    Point2D p2 = basePoints.get(1);
                    Point2D p3 = basePoints.get(2);
                    Point2D p4 = basePoints.get(3);
                    canvas.curveTo((float) p2.getX(), (float) p2.getY(),
                            (float) p3.getX(), (float) p3.getY(),
                            (float) p4.getX(), (float) p4.getY());

                } else { // segment is Line

                    Point2D destination = segment.getBasePoints().get(1);
                    canvas.lineTo((float)destination.getX(), (float) destination.getY());

                }
            }

            if (subpath.isClosed()) {
                canvas.closePath();
            }
        }
    }

    private void writeStrokePath(Path strokePath, Color strokeColor) {
        PdfCanvas canvas = canvasStack.peek();
        // As we transformed stroke to fill, we set stroke color for filling here
        // TODO as we set color with canvas - it could be lost if we previously set this color. See TODOs in cleanText method
        canvas.saveState().setColor(strokeColor, true);
        writePath(strokePath);
        canvas.fill().restoreState();
    }

    private void writeOperands(PdfCanvas canvas, List<PdfObject> operands) {
        int index = 0;

        for (PdfObject obj : operands) {
            canvas.getContentStream().getOutputStream().write(obj);
            if (operands.size() > ++index) {
                canvas.getContentStream().getOutputStream().writeSpace();
            } else {
                canvas.getContentStream().getOutputStream().writeNewLine();
            }
        }
    }

    // should be called before some content is drawn
    private void openNotWrittenTags() {
        CanvasTag tag = notWrittenTags.pollLast();
        while (tag != null) {
            canvasStack.peek().openTag(tag);
            tag = notWrittenTags.pollLast();
        }
    }

    private void removeTagIfNotWritten() {
        if (!notWrittenTags.isEmpty()) {
            CanvasTag tag = notWrittenTags.pop();
            if (tag.hasMcid() && document.isTagged()) {
                TagTreePointer pointer = document.getTagStructureContext().removeContentItem(currentPage, tag.getMcid());
                if (pointer != null) {
                    while (pointer.getKidsRoles().isEmpty()) {
                        pointer.removeTag();
                    }
                }
            }
        } else {
            canvasStack.peek().endMarkedContent();
        }
    }
}
