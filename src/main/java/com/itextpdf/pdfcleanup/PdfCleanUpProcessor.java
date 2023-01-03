/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
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
import com.itextpdf.io.source.ByteUtils;
import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.BezierCurve;
import com.itextpdf.kernel.geom.IShape;
import com.itextpdf.kernel.geom.Matrix;
import com.itextpdf.kernel.geom.Path;
import com.itextpdf.kernel.geom.Point;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.geom.Subpath;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfLiteral;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfResources;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfTextArray;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;
import com.itextpdf.kernel.pdf.annot.PdfLineAnnotation;
import com.itextpdf.kernel.pdf.annot.PdfLinkAnnotation;
import com.itextpdf.kernel.pdf.annot.PdfMarkupAnnotation;
import com.itextpdf.kernel.pdf.annot.PdfPopupAnnotation;
import com.itextpdf.kernel.pdf.annot.PdfTextMarkupAnnotation;
import com.itextpdf.kernel.pdf.canvas.CanvasGraphicsState;
import com.itextpdf.kernel.pdf.canvas.CanvasTag;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.PdfCanvasConstants;
import com.itextpdf.kernel.pdf.canvas.PdfCanvasConstants.FillingRule;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.PathRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.itextpdf.kernel.pdf.colorspace.PdfShading;
import com.itextpdf.kernel.pdf.tagutils.TagTreePointer;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.itextpdf.pdfcleanup.logs.CleanUpLogMessageConstant;
import com.itextpdf.pdfcleanup.util.CleanUpCsCompareUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PDF content stream processor, which filters content to be cleaned up.
 */
public class PdfCleanUpProcessor extends PdfCanvasProcessor {

    private static final Set<String> TEXT_SHOWING_OPERATORS = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("TJ", "Tj", "'", "\"")));
    private static final Set<String> PATH_CONSTRUCTION_OPERATORS = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("m", "l", "c", "v", "y", "h", "re")));
    private static final Set<String> STROKE_OPERATORS = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("S", "s", "B", "B*", "b", "b*")));
    private static final Set<String> NW_FILL_OPERATORS = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("f", "F", "B", "b")));
    private static final Set<String> EO_FILL_OPERATORS = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("f*", "B*", "b*")));
    private static final Set<String> PATH_PAINTING_OPERATORS;
    private static final Set<String> CLIPPING_PATH_OPERATORS = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("W", "W*")));
    private static final Set<String> LINE_STYLE_OPERATORS = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("w", "J", "j", "M", "d")));
    private static final Set<String> STROKE_COLOR_OPERATORS = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("CS", "SC", "SCN", "G", "RG", "K")));
    private static final Set<String> FILL_COLOR_OPERATORS = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("cs", "sc", "scn", "g", "rg", "k")));

    // TL actually is not a text positioning operator, but we need to process it with them
    private static final Set<String> TEXT_POSITIONING_OPERATORS = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("Td", "TD", "Tm", "T*", "TL")));

    // these operators are processed via PdfCanvasProcessor graphics state and event listener
    private static final Set<String> IGNORED_OPERATORS;

    static {
        // HashSet is required in order to autoport correctly in .Net
        HashSet<String> tempSet = new HashSet<>();
        tempSet.addAll(STROKE_OPERATORS);
        tempSet.addAll(NW_FILL_OPERATORS);
        tempSet.addAll(EO_FILL_OPERATORS);
        tempSet.add("n");
        PATH_PAINTING_OPERATORS = Collections.unmodifiableSet(tempSet);

        tempSet = new HashSet<>();
        tempSet.addAll(PATH_CONSTRUCTION_OPERATORS);
        tempSet.addAll(CLIPPING_PATH_OPERATORS);
        tempSet.addAll(LINE_STYLE_OPERATORS);
        tempSet.addAll(Arrays.asList("Tc", "Tw", "Tz", "Tf", "Tr", "Ts"));
        tempSet.addAll(Arrays.asList("BMC", "BDC"));
        IGNORED_OPERATORS = Collections.unmodifiableSet(tempSet);
    }

    private PdfDocument document;
    private PdfPage currentPage;
    private PdfCleanUpFilter filter;
    private Stack<PdfCanvas> canvasStack;

    private boolean removeAnnotIfPartOverlap = true;

    /**
     * In {@code notAppliedGsParams} field not written graphics state params are stored.
     * Stack represents gs params on different levels of the q/Q nesting (see {@link NotAppliedGsParams}).
     * On "q" operator new {@code NotAppliedGsParams} is pushed to the stack and on "Q" it is popped.
     * <p>
     * When operators are applied, they are written from the outer to inner nesting level, separated by "q".
     * After being written the stack is cleared.
     * <p>
     * Graphics state parameters are applied in two ways:
     * <ul>
     * <li>
     * first - right before writing text content, text state in current gs is compare to the text state of the text
     * render info gs and difference is applied to current gs;
     * <li>
     * second - through list of the not applied gs params. Right before writing some content, this list is checked,
     * and if something affecting content is stored in this list it will be applied.
     * </ul>
     */
    private Deque<NotAppliedGsParams> notAppliedGsParams;
    private Deque<CanvasTag> notWrittenTags;
    private int numOfOpenedTagsInsideText;
    private boolean btEncountered;
    private boolean isInText;
    private TextPositioning textPositioning;
    private FilteredImagesCache filteredImagesCache;

    PdfCleanUpProcessor(List<Rectangle> cleanUpRegions, PdfDocument document) {
        super(new PdfCleanUpEventListener());
        this.document = document;
        this.filter = new PdfCleanUpFilter(cleanUpRegions);
        this.canvasStack = new Stack<>();
        this.notAppliedGsParams = new ArrayDeque<>();
        this.notAppliedGsParams.push(new NotAppliedGsParams());
        this.notWrittenTags = new ArrayDeque<>();
        this.numOfOpenedTagsInsideText = 0;
        this.btEncountered = false;
        this.isInText = false;
        this.textPositioning = new TextPositioning();
    }

    @Override
    public void processPageContent(PdfPage page) {
        currentPage = page;
        super.processPageContent(page);
    }

    /**
     * Process the annotations of a page.
     * Default process behaviour is to remove the annotation if there is (partial) overlap with a redaction region
     *
     * @param page    the page to process
     * @param regions a list of redaction regions
     * @param redactRedactAnnotations true if annotation with subtype /Redact should also be removed
     */
    public void processPageAnnotations(PdfPage page, List<Rectangle> regions, boolean redactRedactAnnotations) {
        // Iterate over annotations
        for (PdfAnnotation annot : page.getAnnotations()) {
            PdfName annotSubtype = annot.getSubtype();
            if (PdfName.Popup.equals(annotSubtype)) {
                // we handle popup annots together with PdfMarkupAnnotation annots only
                continue;
            }
            if (!redactRedactAnnotations && PdfName.Redact.equals(annotSubtype)) {
                continue;
            }
            // Check against regions
            for (Rectangle region : regions) {
                if (annotationIsToBeRedacted(annot, region)) {
                    if (annot instanceof PdfMarkupAnnotation) {
                        PdfPopupAnnotation popup = ((PdfMarkupAnnotation) annot).getPopup();
                        if (popup != null) {
                            page.removeAnnotation(popup);
                        }
                    }
                    page.removeAnnotation(annot);
                    break;
                }
            }
        }
    }

    void setFilteredImagesCache(FilteredImagesCache cache) {
        this.filteredImagesCache = cache;
    }

    /**
     * @param contentBytes the bytes of a content stream
     * @param resources    the resources of the content stream. Must not be null.
     */
    @Override
    public void processContent(byte[] contentBytes, PdfResources resources) {
        canvasStack.push(new PdfCanvas(new PdfStream(), new PdfResources(), document));
        if (canvasStack.size() == 1) {
            // If it is the first canvas, we begin to wrap it with q
            getCanvas().saveState();
        }

        super.processContent(contentBytes, resources);
        // Here we don't pop() canvases by intent. It is the responsibility of the one who utilizes the canvas data
    }

    @Override
    public IEventListener getEventListener() {
        return eventListener;
    }

    PdfCanvas popCleanedCanvas() {
        // If it is the last canvas, we finish to wrap it with Q
        if (canvasStack.size() == 1) {
            getCanvas().restoreState();
        }
        return canvasStack.pop();
    }

    @Override
    protected void invokeOperator(PdfLiteral operator, List<PdfObject> operands) {
        String operatorString = operator.toString();

        writeGsParamsIfFormXObject(operatorString, operands);
        super.invokeOperator(operator, operands);
        popCanvasIfFormXObject(operatorString, operands);

        filterContent(operatorString, operands);
    }

    @Override
    protected void beginMarkedContent(PdfName tag, PdfDictionary dict) {
        super.beginMarkedContent(tag, dict);
        notWrittenTags.push(new CanvasTag(tag).setProperties(dict));
        if (btEncountered) {
            ++numOfOpenedTagsInsideText;
        }
    }

    static void writeOperands(PdfCanvas canvas, List<PdfObject> operands) {
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

    static Matrix operandsToMatrix(List<PdfObject> operands) {
        float a = ((PdfNumber) operands.get(0)).floatValue();
        float b = ((PdfNumber) operands.get(1)).floatValue();
        float c = ((PdfNumber) operands.get(2)).floatValue();
        float d = ((PdfNumber) operands.get(3)).floatValue();
        float e = ((PdfNumber) operands.get(4)).floatValue();
        float f = ((PdfNumber) operands.get(5)).floatValue();
        return new Matrix(a, b, c, d, e, f);
    }

    @Override
    protected void eventOccurred(IEventData data, EventType type) {
        if (supportedEvents == null || supportedEvents.contains(type)) {
            eventListener.eventOccurred(data, type);
        }
    }

    /**
     * Returns the last canvas without removing it.
     *
     * @return the last canvas in canvasStack.
     */
    PdfCanvas getCanvas() {
        return canvasStack.peek();
    }

    /**
     * Adds tag to the deque of not written tags.
     *
     * @param tag tag to be added.
     */
    void addNotWrittenTag(CanvasTag tag) {
        notWrittenTags.push(tag);
    }

    /**
     * Opens all tags from deque of not written tags. Should be called before some content is drawn.
     */
    void openNotWrittenTags() {
        CanvasTag tag = notWrittenTags.pollLast();
        while (tag != null) {
            getCanvas().openTag(tag);
            tag = notWrittenTags.pollLast();
        }
    }

    private boolean annotationIsToBeRedacted(PdfAnnotation annotation, Rectangle redactRegion) {
        // TODO(DEVSIX-1605,DEVSIX-1606,DEVSIX-1607,DEVSIX-1608,DEVSIX-1609)
        removeAnnotIfPartOverlap = true;

        PdfName annotationType = annotation.getPdfObject().getAsName(PdfName.Subtype);
        if (annotationType.equals(PdfName.Watermark)) {
            // TODO /FixedPrint entry effect is not fully investigated: DEVSIX-2471
            Logger logger = LoggerFactory.getLogger(PdfCleanUpProcessor.class);
            logger.warn(CleanUpLogMessageConstant.REDACTION_OF_ANNOTATION_TYPE_WATERMARK_IS_NOT_SUPPORTED);
        }

        PdfArray rectAsArray = annotation.getRectangle();
        Rectangle rect = null;
        if (rectAsArray != null) {
            rect = rectAsArray.toRectangle();
        }

        boolean annotationIsToBeRedacted = processAnnotationRectangle(redactRegion, rect);

        // Special processing for some types of annotations.
        if (PdfName.Link.equals(annotationType)) {
            PdfArray quadPoints = ((PdfLinkAnnotation) annotation).getQuadPoints();
            if (quadPointsForLinkAnnotationAreValid(rect, quadPoints)) {
                annotationIsToBeRedacted = processAnnotationQuadPoints(redactRegion, quadPoints);
            }
        } else if (annotationType.equals(PdfName.Highlight) || annotationType.equals(PdfName.Underline)
                || annotationType.equals(PdfName.Squiggly) || annotationType.equals(PdfName.StrikeOut)) {
            PdfArray quadPoints = ((PdfTextMarkupAnnotation) annotation).getQuadPoints();
            // The annotation dictionary’s AP entry, if present, shall take precedence over QuadPoints.
            if (quadPoints != null && annotation.getAppearanceDictionary() == null) {
                try {
                    annotationIsToBeRedacted = processAnnotationQuadPoints(redactRegion, quadPoints);
                } catch (PdfException ignored) {
                    // if quad points array cannot be processed, simply ignore it
                }
            }
        } else if (annotationType.equals(PdfName.Line)) {
            PdfArray line = ((PdfLineAnnotation) annotation).getLine();
            if (line != null) {
                Rectangle drawnLineRectangle = line.toRectangle();
                // Line annotation might contain line leaders, so let's double check overlapping with /Rect area, for simplicity.
                // TODO DEVSIX-1607
                annotationIsToBeRedacted = annotationIsToBeRedacted || processAnnotationRectangle(redactRegion, drawnLineRectangle);
            }
        }

        return annotationIsToBeRedacted;
    }

    private boolean processAnnotationQuadPoints(Rectangle redactRegion, PdfArray quadPoints) {
        List<Rectangle> boundingRectangles = Rectangle.createBoundingRectanglesFromQuadPoint(quadPoints);
        boolean bboxOverlapped = false;
        for (Rectangle bbox : boundingRectangles) {
            bboxOverlapped = bboxOverlapped || processAnnotationRectangle(redactRegion, bbox);
        }
        return bboxOverlapped;
    }

    private boolean processAnnotationRectangle(Rectangle redactRegion, Rectangle annotationRect) {
        if (annotationRect == null) {
            return false;
        }
        // 3 possible situations: full overlap, partial overlap, no overlap
        if (redactRegion.overlaps(annotationRect)) {
            if (redactRegion.contains(annotationRect)) {
                // full overlap
                return true;
            }

            Rectangle intersectionRect = redactRegion.getIntersection(annotationRect);
            if (intersectionRect != null) {
                // partial overlap
                if (removeAnnotIfPartOverlap) {
                    return true;
                } else {
                    //TODO (DEVSIX-1605,DEVSIX-1606,DEVSIX-1609)
                }
            }
        }
        // No overlap, do nothing
        return false;
    }

    /**
     * For a link annotation, a quadpoints array can be specified
     * but it will be ignored in favour of the rectangle
     * if one of the points is located outside the rectangle's boundaries
     *
     * @param rect       rectangle entry of the link annotation
     * @param quadPoints An array of 8 × n numbers specifying the coordinates of n quadrilaterals
     *                   in default user space that comprise the region in which the link should be activated.
     * @return true if the quad points are valid, false if the quadpoint array should be used
     */
    private boolean quadPointsForLinkAnnotationAreValid(Rectangle rect, PdfArray quadPoints) {
        if (quadPoints == null || quadPoints.isEmpty() || quadPoints.size() % 8 != 0) {
            return false;
        }
        for (int i = 0; i < quadPoints.size(); i += 8) {
            for (int j = 0; j < 8; j += 2) {
                PdfNumber pointX = quadPoints.getAsNumber(i + j);
                PdfNumber pointY = quadPoints.getAsNumber(i + j + 1);
                if (pointX == null || pointY == null) {
                    return false;
                }
                float x = pointX.floatValue();
                float y = pointY.floatValue();
                if (rect != null && !rect.contains(new Rectangle(x, y, 0, 0))) {
                    return false;
                }
            }
        }
        return true;
    }

    private void writeGsParamsIfFormXObject(String operator, List<PdfObject> operands) {
        if ("Do".equals(operator)) {
            PdfStream formStream = getXObjectStream((PdfName) operands.get(0));
            if (PdfName.Form.equals(formStream.getAsName(PdfName.Subtype))) {
                writeNotAppliedGsParams(true, true);
                openNotWrittenTags();
            }
        }
    }

    private void popCanvasIfFormXObject(String operator, List<PdfObject> operands) {
        if ("Do".equals(operator)) {
            PdfStream formStream = getXObjectStream((PdfName) operands.get(0));
            if (PdfName.Form.equals(formStream.getAsName(PdfName.Subtype))) {
                PdfCanvas cleanedCanvas = popCleanedCanvas();

                PdfFormXObject newFormXObject = new PdfFormXObject((Rectangle) null);
                newFormXObject.getPdfObject().putAll(formStream);
                if (formStream.containsKey(PdfName.Resources)) {
                    newFormXObject.put(PdfName.Resources, cleanedCanvas.getResources().getPdfObject());
                }
                newFormXObject.getPdfObject().setData(cleanedCanvas.getContentStream().getBytes());

                PdfName name = getCanvas().getResources().addForm(newFormXObject);
                getCanvas().getContentStream().getOutputStream().write(name).writeSpace().writeBytes(ByteUtils.getIsoBytes("Do\n"));
            }
        }
    }

    private void filterContent(String operator, List<PdfObject> operands) {
        if (TEXT_SHOWING_OPERATORS.contains(operator)) {
            cleanText(operator, operands);
        } else if ("Do".equals(operator)) {
            checkIfImageAndClean(operands);
        } else if ("EI".equals(operator)) {
            cleanInlineImage();
        } else if (PATH_PAINTING_OPERATORS.contains(operator)) {
            writePath();
        } else if ("q".equals(operator)) {
            notAppliedGsParams.push(new NotAppliedGsParams());
        } else if ("Q".equals(operator)) {
            notAppliedGsParams.pop();
            if (notAppliedGsParams.size() == 0) {
                getCanvas().restoreState();
                notAppliedGsParams.push(new NotAppliedGsParams());
            }
        } else if ("BT".equals(operator)) {
            btEncountered = true;
        } else if ("ET".equals(operator)) {
            if (isInText) {
                writeOperands(getCanvas(), operands);
                isInText = false;
            }
            btEncountered = false;
            textPositioning.clear();
        } else if (TEXT_POSITIONING_OPERATORS.contains(operator)) {
            textPositioning.appendPositioningOperator(operator, operands);
        } else if ("EMC".equals(operator)) { // BMC and BDC are handled with BeginMarkedContent method
            removeOrCloseTag();
        } else if (LINE_STYLE_OPERATORS.contains(operator)) {
            notAppliedGsParams.peek().lineStyleOperators.put(operator, new ArrayList<>(operands));
        } else if ("gs".equals(operator)) {
            notAppliedGsParams.peek().extGStates.add(getResources().getResource(PdfName.ExtGState).getAsDictionary((PdfName) operands.get(0)));
        } else if ("cm".equals(operator)) {
            notAppliedGsParams.peek().ctms.add(new ArrayList<>(operands));
        } else if (STROKE_COLOR_OPERATORS.contains(operator)) {
            notAppliedGsParams.peek().strokeColor = getGraphicsState().getStrokeColor();
        } else if (FILL_COLOR_OPERATORS.contains(operator)) {
            notAppliedGsParams.peek().fillColor = getGraphicsState().getFillColor();
        } else if ("sh".equals(operator)) {
            PdfShading shading = getResources().getShading((PdfName) operands.get(0));
            getCanvas().paintShading(shading);
        } else if (!IGNORED_OPERATORS.contains(operator)) {
            writeOperands(getCanvas(), operands);
        }
    }

    private void cleanText(String operator, List<PdfObject> operands) {
        List<TextRenderInfo> textChunks = null;
        PdfArray cleanedText = null;
        if ("TJ".equals(operator)) {
            PdfArray originalTJ = (PdfArray) operands.get(0);
            if (originalTJ.isEmpty()) {
                // empty TJ neither shows any text nor affects text positioning
                // we can safely ignore it
                return;
            }
            int i = 0; // text chunk index in original TJ
            PdfTextArray newTJ = new PdfTextArray();
            for (PdfObject e : originalTJ) {
                if (e.isString()) {
                    if (null == textChunks) {
                        textChunks = ((PdfCleanUpEventListener) getEventListener()).getEncounteredText();
                    }
                    PdfArray filteredText = filter.filterText(textChunks.get(i++)).getFilterResult();
                    newTJ.addAll(filteredText);
                } else {
                    newTJ.add(e);
                }
            }

            cleanedText = newTJ;
        } else { // if operator is Tj or ' or "
            textChunks = ((PdfCleanUpEventListener) getEventListener()).getEncounteredText();
            PdfCleanUpFilter.FilterResult<PdfArray> filterResult = filter.filterText(textChunks.get(0));
            if (filterResult.isModified()) {
                cleanedText = filterResult.getFilterResult();
            }
        }
        // if text wasn't modified cleanedText is null
        if (cleanedText == null || cleanedText.size() != 1 || !cleanedText.get(0).isNumber()) {
            if (null == textChunks) {
                textChunks = ((PdfCleanUpEventListener) getEventListener()).getEncounteredText();
            }
            TextRenderInfo text = textChunks.get(0); // all text chunks even in case of TJ have the same graphics state
            writeNotAppliedGsParamsForText(text);
            beginTextObjectAndOpenNotWrittenTags();

            writeNotAppliedTextStateParams(text);
            textPositioning.writePositionedText(operator, operands, cleanedText, getCanvas());
        } else { // cleaned text is tj array with single number - it means that the whole text chunk was removed
            CanvasGraphicsState gs = getCanvas().getGraphicsState();
            // process new lines if necessary
            if ("'".equals(operator) || "\"".equals(operator)) {
                List<PdfObject> newLineList = new ArrayList<>();
                newLineList.add(new PdfLiteral("T*"));
                textPositioning.appendPositioningOperator("T*", newLineList);
            }
            textPositioning.appendTjArrayWithSingleNumber(cleanedText, gs.getFontSize(), gs.getHorizontalScaling());
        }

    }

    private void beginTextObjectAndOpenNotWrittenTags() {
        if (!isInText) {
            int numOfTagsBeforeBT = notWrittenTags.size() - numOfOpenedTagsInsideText;
            CanvasTag tag;
            for (int i = 0; i < numOfTagsBeforeBT; ++i) {
                tag = notWrittenTags.pollLast();
                getCanvas().openTag(tag);
            }

            getCanvas().beginText();
            isInText = true;

            openNotWrittenTags();
        } else {
            openNotWrittenTags();
        }
    }

    private void writeNotAppliedTextStateParams(TextRenderInfo text) {
        PdfCanvas canvas = getCanvas();
        CanvasGraphicsState currGs = canvas.getGraphicsState();
        if (currGs.getCharSpacing() != text.getCharSpacing()) {
            canvas.setCharacterSpacing(text.getCharSpacing());
        }
        if (currGs.getWordSpacing() != text.getWordSpacing()) {
            canvas.setWordSpacing(text.getWordSpacing());
        }
        if (currGs.getHorizontalScaling() != text.getHorizontalScaling()) {
            canvas.setHorizontalScaling(text.getHorizontalScaling());
        }

        // not writing leading here, it is processed along with positioning operators

        PdfFont currFont = currGs.getFont();
        if (currFont == null || currFont.getPdfObject() != text.getFont().getPdfObject()
                || currGs.getFontSize() != text.getFontSize()) {
            canvas.setFontAndSize(text.getFont(), text.getFontSize());
        }
        if (currGs.getTextRenderingMode() != text.getTextRenderMode()) {
            canvas.setTextRenderingMode(text.getTextRenderMode());
        }
        if (currGs.getTextRise() != text.getRise()) {
            canvas.setTextRise(text.getRise());
        }
    }

    private void writeNotAppliedGsParamsForText(TextRenderInfo textRenderInfo) {
        boolean stroke = false;
        boolean fill = false;
        switch (textRenderInfo.getTextRenderMode()) {
            case PdfCanvasConstants.TextRenderingMode.STROKE:
            case PdfCanvasConstants.TextRenderingMode.STROKE_CLIP:
                stroke = true;
                break;
            case PdfCanvasConstants.TextRenderingMode.FILL:
            case PdfCanvasConstants.TextRenderingMode.FILL_CLIP:
                fill = true;
                break;
            case PdfCanvasConstants.TextRenderingMode.FILL_STROKE:
            case PdfCanvasConstants.TextRenderingMode.FILL_STROKE_CLIP:
                stroke = true;
                fill = true;
                break;
        }
        writeNotAppliedGsParams(fill, stroke);
    }

    private void checkIfImageAndClean(List<PdfObject> operands) {
        PdfStream imageStream = getXObjectStream((PdfName) operands.get(0));
        if (PdfName.Image.equals(imageStream.getAsName(PdfName.Subtype))) {
            ImageRenderInfo encounteredImage = ((PdfCleanUpEventListener) getEventListener()).getEncounteredImage();

            FilteredImagesCache.FilteredImageKey key = filter.createFilteredImageKey(encounteredImage.getImage(), encounteredImage.getImageCtm(), document);
            PdfImageXObject imageToWrite = getFilteredImage(key, encounteredImage.getImageCtm());

            if (imageToWrite != null) {
                float[] ctm = pollNotAppliedCtm();
                writeNotAppliedGsParams(false, false);
                openNotWrittenTags();
                getCanvas().addXObjectWithTransformationMatrix(imageToWrite, ctm[0], ctm[1], ctm[2], ctm[3], ctm[4], ctm[5]);
            }
        }
    }

    private PdfImageXObject getFilteredImage(FilteredImagesCache.FilteredImageKey filteredImageKey, Matrix ctmForMasksFiltering) {
        PdfImageXObject originalImage = filteredImageKey.getImageXObject();
        PdfImageXObject imageToWrite = getFilteredImagesCache().get(filteredImageKey);

        if (imageToWrite == null) {
            PdfCleanUpFilter.FilterResult<ImageData> imageFilterResult = filter.filterImage(filteredImageKey);
            if (imageFilterResult.isModified()) {
                ImageData filteredImageData = imageFilterResult.getFilterResult();
                if (Boolean.TRUE.equals(originalImage.getPdfObject().getAsBool(PdfName.ImageMask))) {
                    if (!PdfCleanUpFilter.imageSupportsDirectCleanup(originalImage)) {
                        Logger logger = LoggerFactory.getLogger(PdfCleanUpProcessor.class);
                        logger.error(CleanUpLogMessageConstant.IMAGE_MASK_CLEAN_UP_NOT_SUPPORTED);
                    } else {
                        filteredImageData.makeMask();
                    }
                }
                if (filteredImageData != null) {
                    imageToWrite = new PdfImageXObject(filteredImageData);
                    getFilteredImagesCache().put(filteredImageKey, imageToWrite);

                    // While having been processed with java libraries, only the number of components mattered.
                    // However now we should put the correct color space dictionary as an image's resource,
                    // because it'd be have been considered by pdf browsers before rendering it.
                    // Additional checks required as if an image format has been changed,
                    // then the old colorspace may produce an error with the new image data.
                    if (areColorSpacesDifferent(originalImage, imageToWrite)
                            && CleanUpCsCompareUtil.isOriginalCsCompatible(originalImage, imageToWrite)) {
                        PdfObject originalCS = originalImage.getPdfObject().get(PdfName.ColorSpace);
                        if (originalCS != null) {
                            imageToWrite.put(PdfName.ColorSpace, originalCS);
                        }
                    }

                    if (ctmForMasksFiltering != null && !filteredImageData.isMask()) {
                        filterImageMask(originalImage, PdfName.SMask, ctmForMasksFiltering, imageToWrite);
                        filterImageMask(originalImage, PdfName.Mask, ctmForMasksFiltering, imageToWrite);

                        PdfArray colourKeyMaskingArr = originalImage.getPdfObject().getAsArray(PdfName.Mask);
                        if (colourKeyMaskingArr != null) {
                            // In general we should be careful about images that might have changed their color space
                            // or have been converted to lossy format during filtering.
                            // However we have been copying Mask entry non-conditionally before and also I'm not sure
                            // that cases described above indeed take place.
                            imageToWrite.put(PdfName.Mask, colourKeyMaskingArr);
                        }

                        if (originalImage.getPdfObject().containsKey(PdfName.SMaskInData)) {
                            // This entry will likely lose meaning after image conversion to bitmap and back again, but let's leave as is for now.
                            imageToWrite.put(PdfName.SMaskInData, originalImage.getPdfObject().get(PdfName.SMaskInData));
                        }
                    }
                }
            } else {
                imageToWrite = originalImage;
            }
        }
        return imageToWrite;
    }

    private void filterImageMask(PdfImageXObject originalImage, PdfName maskKey, Matrix ctmForMasksFiltering, PdfImageXObject imageToWrite) {
        PdfStream maskStream = originalImage.getPdfObject().getAsStream(maskKey);
        if (maskStream == null || ctmForMasksFiltering == null) {
            return;
        }
        PdfImageXObject maskImageXObject = new PdfImageXObject(maskStream);
        if (!PdfCleanUpFilter.imageSupportsDirectCleanup(maskImageXObject)) {
            Logger logger = LoggerFactory.getLogger(PdfCleanUpProcessor.class);
            logger.error(CleanUpLogMessageConstant.IMAGE_MASK_CLEAN_UP_NOT_SUPPORTED);
            return;
        }
        FilteredImagesCache.FilteredImageKey k = filter.createFilteredImageKey(maskImageXObject, ctmForMasksFiltering, document);
        PdfImageXObject maskToWrite = getFilteredImage(k, null);
        if (maskToWrite != null) {
            imageToWrite.getPdfObject().put(maskKey, maskToWrite.getPdfObject());
        }
    }

    private FilteredImagesCache getFilteredImagesCache() {
        return filteredImagesCache != null ? filteredImagesCache : new FilteredImagesCache();
    }

    private void cleanInlineImage() {
        ImageRenderInfo encounteredImage = ((PdfCleanUpEventListener) getEventListener()).getEncounteredImage();
        PdfCleanUpFilter.FilterResult<ImageData> imageFilterResult = filter.filterImage(encounteredImage);
        ImageData filteredImage;
        if (imageFilterResult.isModified()) {
            filteredImage = imageFilterResult.getFilterResult();
        } else {
            filteredImage = ImageDataFactory.create(encounteredImage.getImage().getImageBytes());
        }
        if (filteredImage != null) {
            Boolean imageMaskFlag = encounteredImage.getImage().getPdfObject().getAsBool(PdfName.ImageMask);
            if (imageMaskFlag != null && (boolean) imageMaskFlag) {
                filteredImage.makeMask();
            }

            float[] ctm = pollNotAppliedCtm();
            writeNotAppliedGsParams(false, false);
            openNotWrittenTags();

            getCanvas().addImageWithTransformationMatrix(filteredImage, ctm[0], ctm[1], ctm[2], ctm[3], ctm[4], ctm[5], true);
        }

        // TODO
        // PdfCanvas doesn't have a method that writes inline image using pdf stream, and only have method which
        // accepts Image as parameter. That's why we can't write image just as it was in original file, we convert it to Image.

        // IMPORTANT: If writing of pdf stream of not changed inline image will be implemented, don't forget to ensure that
        // inline image color space is present in new resources if necessary.
    }

    private void writePath() {
        PathRenderInfo path = ((PdfCleanUpEventListener) getEventListener()).getEncounteredPath();

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
        PdfCanvas canvas = getCanvas();
        if (fill) {
            fillPath = filter.filterFillPath(path, path.getRule());
            if (!fillPath.isEmpty()) {
                writeNotAppliedGsParams(true, false);
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
                // we pass stroke here as false, because stroke is transformed into fill. we don't need to set stroke color
                writeNotAppliedGsParams(false, false);
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
                writeNotAppliedGsParams(false, false);
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
                writeNotAppliedGsParams(false, false); // we still need to open all q operators
                canvas.moveTo(0, 0).clip();
            }
            canvas.endPath();
        }
    }

    private void writePath(Path path) {
        PdfCanvas canvas = getCanvas();
        for (Subpath subpath : path.getSubpaths()) {
            canvas.moveTo((float) subpath.getStartPoint().getX(), (float) subpath.getStartPoint().getY());

            for (IShape segment : subpath.getSegments()) {
                if (segment instanceof BezierCurve) {

                    List<Point> basePoints = segment.getBasePoints();
                    Point p2 = basePoints.get(1);
                    Point p3 = basePoints.get(2);
                    Point p4 = basePoints.get(3);
                    canvas.curveTo((float) p2.getX(), (float) p2.getY(),
                            (float) p3.getX(), (float) p3.getY(),
                            (float) p4.getX(), (float) p4.getY());

                } else { // segment is Line

                    Point destination = segment.getBasePoints().get(1);
                    canvas.lineTo((float) destination.getX(), (float) destination.getY());

                }
            }

            if (subpath.isClosed()) {
                canvas.closePath();
            }
        }
    }

    private void writeStrokePath(Path strokePath, Color strokeColor) {
        PdfCanvas canvas = getCanvas();
        // As we transformed stroke to fill, we set stroke color for filling here
        canvas.saveState().setFillColor(strokeColor);
        writePath(strokePath);
        canvas.fill().restoreState();
    }

    private void removeOrCloseTag() {
        if (notWrittenTags.size() > 0) {
            CanvasTag tag = notWrittenTags.pop();
            if (tag.hasMcid() && document.isTagged()) {
                TagTreePointer pointer = document.getTagStructureContext().removeContentItem(currentPage, tag.getMcid());
                if (pointer != null) {
                    while (pointer.getKidsRoles().size() == 0) {
                        pointer.removeTag();
                    }
                }
            }
        } else {
            getCanvas().endMarkedContent();
        }
        if (btEncountered) {
            --numOfOpenedTagsInsideText;
        }
    }

    /**
     * To add images and formXObjects to canvas we pass ctm. Here we try to find last not applied ctm in order to pass it to
     * PdfCanvas method later. Returned ctm is written right before the image, that's why we care only for not applied ctms of
     * the current (the "deepest") q/Q nesting level.
     * If such ctm wasn't found identity ctm is returned.
     */
    private float[] pollNotAppliedCtm() {
        List<List<PdfObject>> ctms = notAppliedGsParams.peek().ctms;
        if (ctms.size() == 0) {
            return new float[]{1, 0, 0, 1, 0, 0};
        }
        List<PdfObject> lastCtm = ctms.remove(ctms.size() - 1);

        float[] ctm = new float[6];
        ctm[0] = ((PdfNumber) lastCtm.get(0)).floatValue();
        ctm[1] = ((PdfNumber) lastCtm.get(1)).floatValue();
        ctm[2] = ((PdfNumber) lastCtm.get(2)).floatValue();
        ctm[3] = ((PdfNumber) lastCtm.get(3)).floatValue();
        ctm[4] = ((PdfNumber) lastCtm.get(4)).floatValue();
        ctm[5] = ((PdfNumber) lastCtm.get(5)).floatValue();

        return ctm;
    }

    private void writeNotAppliedGsParams(boolean fill, boolean stroke) {
        if (notAppliedGsParams.size() > 0) {
            while (notAppliedGsParams.size() != 1) {
                NotAppliedGsParams gsParams = notAppliedGsParams.pollLast();
                // We want to apply graphics state params of outer q/Q nesting level on it's level and not on the inner
                // q/Q nesting level. Because of that we write all gs params for the outer q/Q, just in case it will be needed
                // later (if we don't write it now, there will be no possibility to write it in the outer q/Q later).
                applyGsParams(true, true, gsParams);
                getCanvas().saveState();
            }
            applyGsParams(fill, stroke, notAppliedGsParams.peek());
        }
    }

    private void applyGsParams(boolean fill, boolean stroke, NotAppliedGsParams gsParams) {
        for (PdfDictionary extGState : gsParams.extGStates) {
            getCanvas().setExtGState(extGState);
        }
        gsParams.extGStates.clear();

        if (gsParams.ctms.size() > 0) {
            Matrix m = new Matrix();
            for (List<PdfObject> ctm : gsParams.ctms) {

                m = operandsToMatrix(ctm).multiply(m);
            }
            getCanvas().concatMatrix(m.get(Matrix.I11), m.get(Matrix.I12),
                    m.get(Matrix.I21), m.get(Matrix.I22), m.get(Matrix.I31), m.get(Matrix.I32));

            gsParams.ctms.clear();
        }

        if (stroke) {
            for (List<PdfObject> strokeState : gsParams.lineStyleOperators.values()) {
                writeOperands(getCanvas(), strokeState);
            }
            gsParams.lineStyleOperators.clear();
        }

        if (fill) {
            if (gsParams.fillColor != null) {
                getCanvas().setFillColor(gsParams.fillColor);
            }
            gsParams.fillColor = null;
        }
        if (stroke) {
            if (gsParams.strokeColor != null) {
                getCanvas().setStrokeColor(gsParams.strokeColor);
            }
            gsParams.strokeColor = null;
        }
    }

    static boolean areColorSpacesDifferent(PdfImageXObject originalImage, PdfImageXObject clearedImage) {
        PdfObject originalImageCS = originalImage.getPdfObject().get(PdfName.ColorSpace);
        PdfObject clearedImageCS = clearedImage.getPdfObject().get(PdfName.ColorSpace);

        if (originalImageCS == clearedImageCS) {
            return false;
        } else if (originalImageCS == null || clearedImageCS == null) {
            return true;
        } else if (originalImageCS.equals(clearedImageCS)) {
            return false;
        } else if (originalImageCS.isArray() && clearedImageCS.isArray()) {
            PdfArray originalCSArray = (PdfArray) originalImageCS;
            PdfArray clearedCSArray = (PdfArray) clearedImageCS;
            if (originalCSArray.size() != clearedCSArray.size()) {
                return true;
            }
            for (int i = 0; i < originalCSArray.size(); ++i) {
                PdfObject objectFromOriginal = originalCSArray.get(i);
                PdfObject objectFromCleared = clearedCSArray.get(i);
                if (!objectFromOriginal.equals(objectFromCleared)) {
                    return true;
                }
            }
            return false;
        }

        return true;
    }

    /**
     * Single instance of this class represents not applied graphics state params of the single q/Q nesting level.
     * For example:
     * <p>
     * 0 g
     * 1 0 0 1 25 50 cm
     * <p>
     * q
     * <p>
     * 5 w
     * /Gs1 gs
     * 13 g
     * <p>
     * Q
     * <p>
     * 1 0 0 RG
     * <p>
     * Operators "0 g", "1 0 0 1 25 50 cm" and "1 0 0 RG" belong to the outer q/Q nesting level;
     * Operators "5 w", "/Gs1 gs", "13 g" belong to the inner q/Q nesting level.
     * Operators of every level of the q/Q nesting are stored in different instances of this class.
     */
    static class NotAppliedGsParams {
        List<PdfDictionary> extGStates = new ArrayList<>();
        List<List<PdfObject>> ctms = new ArrayList<>(); // list of operator statements
        Color fillColor;
        Color strokeColor;
        Map<String, List<PdfObject>> lineStyleOperators = new LinkedHashMap<>(); // operator and it's operands
    }
}
