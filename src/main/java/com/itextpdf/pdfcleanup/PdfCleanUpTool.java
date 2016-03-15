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


import com.itextpdf.io.source.PdfTokenizer;
import com.itextpdf.io.source.RandomAccessFileOrArray;
import com.itextpdf.io.source.RandomAccessSourceFactory;
import com.itextpdf.kernel.PdfException;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceCmyk;
import com.itextpdf.kernel.color.DeviceGray;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;
import com.itextpdf.kernel.pdf.annot.PdfPopupAnnotation;
import com.itextpdf.kernel.pdf.annot.PdfRedactAnnotation;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Property;
import com.itextpdf.layout.element.Paragraph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the main mechanism for cleaning a PDF document.
 */
public class PdfCleanUpTool {

    /**
     * When a document with line arts is being cleaned up, there are lot of
     * calculations with floating point numbers. All of them are translated
     * into fixed point numbers by multiplying by this coefficient. Vary it
     * to adjust the preciseness of the calculations.
     */
    public static double floatMultiplier = Math.pow(10, 14);

    /**
     * Used as the criterion of a good approximation of rounded line joins
     * and line caps.
     */
    public static double arcTolerance = 0.0025;

    private PdfDocument pdfDocument;

    /**
     * Key - page number, value - list of locations related to the page.
     */
    private Map<Integer, List<PdfCleanUpLocation>> pdfCleanUpLocations;

    /**
     * Keys - redact annotations to be removed from the document after clean up,
     * Values - list of regions defined by redact annotation
     */
    private Map<PdfRedactAnnotation, List<Rectangle>> redactAnnotations;

    /**
     * Creates a {@link PdfCleanUpTool} object. No regions for erasing are specified.
     * Use {@link PdfCleanUpTool#addCleanupLocation(PdfCleanUpLocation)} method
     * to set regions to be erased from the document.
     *
     * @param pdfDocument A{@link com.itextpdf.kernel.pdf.PdfDocument} object representing the document
     *                    to which redaction applies.
     */
    public PdfCleanUpTool(PdfDocument pdfDocument) {
        this(pdfDocument, false);
    }

    /**
     * Creates a {@link PdfCleanUpTool} object. If {@param cleanRedactAnnotations} is true,
     * regions to be erased are extracted from the redact annotations contained inside the given document.
     * Those redact annotations will be removed from the resultant document. If {@param cleanRedactAnnotations} is false,
     * then no regions for erasing are specified. In that case use {@link PdfCleanUpTool#addCleanupLocation(PdfCleanUpLocation)}
     * method to set regions to be erased from the document.
     *
     * @param pdfDocument A{@link com.itextpdf.kernel.pdf.PdfDocument} object representing the document
     *                    to which redaction applies.
     * @param cleanRedactAnnotations if true - regions to be erased are extracted from the redact annotations contained
     *                               inside the given document.
     */
    public PdfCleanUpTool(PdfDocument pdfDocument, boolean cleanRedactAnnotations) {
        if (pdfDocument.getReader() == null || pdfDocument.getWriter() == null) {
            throw new PdfException(PdfException.PdfDocumentMustBeOpenedInStampingMode);
        }
        this.pdfDocument = pdfDocument;
        this.pdfCleanUpLocations = new HashMap<>();

        if (cleanRedactAnnotations) {
            addCleanUpLocationsBasedOnRedactAnnotations();
        }
    }

    /**
     * Creates a {@link PdfCleanUpTool} object based on the given {@link java.util.List}
     * of {@link PdfCleanUpLocation}s representing regions to be erased from the document.
     *
     * @param cleanUpLocations list of locations to be cleaned up {@see PdfCleanUpLocation}
     * @param pdfDocument      A{@link com.itextpdf.kernel.pdf.PdfDocument} object representing the document
     *                         to which redaction applies.
     */
    public PdfCleanUpTool(PdfDocument pdfDocument, List<PdfCleanUpLocation> cleanUpLocations) {
        this(pdfDocument);
        for (PdfCleanUpLocation location : cleanUpLocations) {
            addCleanupLocation(location);
        }
    }

    public PdfCleanUpTool addCleanupLocation(PdfCleanUpLocation cleanUpLocation) {
        List<PdfCleanUpLocation> pgLocations = this.pdfCleanUpLocations.get(cleanUpLocation.getPage());
        if (pgLocations == null) {
            pgLocations = new ArrayList<>();
            this.pdfCleanUpLocations.put(cleanUpLocation.getPage(), pgLocations);
        }
        pgLocations.add(cleanUpLocation);

        return this;
    }

    /**
     * Cleans the document by erasing all the areas which are either provided or
     * extracted from redaction annotations.
     */
    public void cleanUp() throws IOException {
        for (Map.Entry<Integer, List<PdfCleanUpLocation>> entry : pdfCleanUpLocations.entrySet()) {
            cleanUpPage(entry.getKey(), entry.getValue());
        }

        if (redactAnnotations != null) { // if it isn't null, then we are in "extract locations from redact annots" mode
            removeRedactAnnots();
        }
    }

    private void cleanUpPage(int pageNumber, List<PdfCleanUpLocation> cleanUpLocations) {
        if (cleanUpLocations.size() == 0) {
            return;
        }

        List<Rectangle> regions = new ArrayList<>();
        for (PdfCleanUpLocation cleanUpLocation : cleanUpLocations) {
            regions.add(cleanUpLocation.getRegion());
        }

        PdfPage page = pdfDocument.getPage(pageNumber);
        PdfCleanUpProcessor cleanUpProcessor = new PdfCleanUpProcessor(regions, pdfDocument);
        cleanUpProcessor.processPageContent(page);

        PdfCanvas pageCleanedContents = cleanUpProcessor.popCleanedCanvas();
        page.put(PdfName.Contents, pageCleanedContents.getContentStream());

        colorCleanedLocations(pageCleanedContents, cleanUpLocations);
    }

    private void colorCleanedLocations(PdfCanvas canvas, List<PdfCleanUpLocation> cleanUpLocations) {
        for (PdfCleanUpLocation location : cleanUpLocations) {
            if (location.getCleanUpColor() != null) {
                addColoredRectangle(canvas, location);
            }
        }
    }

    private void addColoredRectangle(PdfCanvas canvas, PdfCleanUpLocation location) {
        canvas
                .saveState()
                .setFillColor(location.getCleanUpColor())
                .rectangle(location.getRegion())
                .fill()
                .restoreState();
    }

    /**
     * Adds clean up locations to be erased by extracting regions from the redact annotations
     * contained inside the given document. Those redact annotations will be removed from the resultant document.
     *
     * @return current {@link PdfCleanUpTool} instance.
     */
    private void addCleanUpLocationsBasedOnRedactAnnotations() {
        redactAnnotations = new LinkedHashMap<>();
        for (int i = 1; i <= pdfDocument.getNumberOfPages(); ++i) {
            extractLocationsFromRedactAnnotations(pdfDocument.getPage(i));
        }
    }

    private void extractLocationsFromRedactAnnotations(PdfPage page) {
        List<PdfAnnotation> annotations = page.getAnnotations();
        for (PdfAnnotation annotation : annotations) {
            if (PdfName.Redact.equals(annotation.getSubtype())) {
                extractLocationsFromSingleRedactAnnotation((PdfRedactAnnotation) annotation);
            }
        }
    }

    /**
     * Note: annotation can consist not only of one area specified by the RECT entry, but also of multiple areas specified
     * by the QuadPoints entry in the annotation dictionary.
     */
    private void extractLocationsFromSingleRedactAnnotation(PdfRedactAnnotation redactAnnotation) {
        List<Rectangle> regions;
        PdfArray quadPoints =  redactAnnotation.getQuadPoints();
        if (quadPoints != null && !quadPoints.isEmpty()) {
            regions = translateQuadPointsToRectangles(quadPoints);
        } else {
            regions = new ArrayList<>();
            regions.add(redactAnnotation.getRectangle().toRectangle());
        }

        redactAnnotations.put(redactAnnotation, regions);

        int page = pdfDocument.getPageNumber(redactAnnotation.getPage());
        Color cleanUpColor = redactAnnotation.getInteriorColor();

        PdfDictionary ro = redactAnnotation.getRedactRolloverAppearance();
        if (ro != null) {
            cleanUpColor = null;
        }

        for (Rectangle region : regions) {
            addCleanupLocation(new PdfCleanUpLocation(page, region, cleanUpColor));
        }
    }

    private List<Rectangle> translateQuadPointsToRectangles(PdfArray quadPoints) {
        List<Rectangle> rectangles = new ArrayList<Rectangle>();

        for (int i = 0; i < quadPoints.size(); i += 8) {
            Float x = quadPoints.getAsFloat(i + 4);
            Float y = quadPoints.getAsFloat(i + 5);
            Float width = quadPoints.getAsFloat(i + 2) - x;
            Float height = quadPoints.getAsFloat(i + 3) - y;
            rectangles.add(new Rectangle(x, // QuadPoints have "Z" order
                    y,
                    width,
                    height));
        }

        return rectangles;
    }

    private void removeRedactAnnots() throws IOException {
        for (PdfRedactAnnotation annotation : redactAnnotations.keySet()) {
            PdfPage page = annotation.getPage();
            page.removeAnnotation(annotation);
            PdfPopupAnnotation popup = annotation.getPopup();
            if (popup != null) {
                page.removeAnnotation(popup);
            }

            PdfCanvas canvas = new PdfCanvas(page);
            PdfStream redactRolloverAppearance = annotation.getRedactRolloverAppearance();
            PdfString overlayText = annotation.getOverlayText();
            Rectangle annotRect = annotation.getRectangle().toRectangle();

            if (redactRolloverAppearance != null) {
                drawRolloverAppearance(canvas, redactRolloverAppearance, annotRect, redactAnnotations.get(annotation));
            } else if (overlayText != null && !overlayText.toUnicodeString().isEmpty()) {
                drawOverlayText(canvas, overlayText.toUnicodeString(), annotRect, annotation.getRepeat(), annotation.getDefaultAppearance(), annotation.getJustification());
            }
        }
    }

    private void drawRolloverAppearance(PdfCanvas canvas, PdfStream redactRolloverAppearance, Rectangle annotRect, List<Rectangle> cleanedRegions) {
        canvas.saveState();

        for (Rectangle rect : cleanedRegions) {
            canvas.rectangle(rect.getLeft(), rect.getBottom(), rect.getWidth(), rect.getHeight());
        }
        canvas.clip().newPath();

        PdfFormXObject formXObject = new PdfFormXObject(redactRolloverAppearance);
        canvas.addXObject(formXObject, 1, 0, 0, 1, annotRect.getLeft(), annotRect.getBottom());
        canvas.restoreState();
    }

    private void drawOverlayText(PdfCanvas canvas, String overlayText, Rectangle annotRect, PdfBoolean repeat, PdfString defaultAppearance, int justification) throws IOException {
        Map<String, List> parsedDA = parseDAParam(defaultAppearance);
        PdfFont font;
        float fontSize = 12;
        List fontArgs = parsedDA.get("Tf");
        if (fontArgs != null) {
            font = getFontFromAcroForm((PdfName) fontArgs.get(0));
            fontSize = ((PdfNumber) fontArgs.get(1)).getFloatValue();
        } else {
            font = PdfFontFactory.createFont();
        }

        Canvas modelCanvas = new Canvas(canvas, pdfDocument, annotRect, false);

        Paragraph p = new Paragraph(overlayText).setFont(font).setFontSize(fontSize).setMargin(0);
        Property.TextAlignment textAlignment = Property.TextAlignment.LEFT;
        switch (justification) {
            case 1:
                textAlignment = Property.TextAlignment.CENTER;
                break;
            case 2:
                textAlignment = Property.TextAlignment.RIGHT;
                break;
            default:
        }
        p.setTextAlignment(textAlignment);
        List<PdfObject> strokeColorArgs = parsedDA.get("StrokeColor");
        if (strokeColorArgs != null) {
            p.setStrokeColor(getColor(strokeColorArgs));
        }
        List<PdfObject> fillColorArgs = parsedDA.get("FillColor");
        if (fillColorArgs != null) {
            p.setFontColor(getColor(fillColorArgs));
        }

        modelCanvas.add(p);
        if (repeat != null && repeat.getValue()) {
            Boolean isFull = modelCanvas.getRenderer().getPropertyAsBoolean(Property.FULL);
            while(isFull == null || !isFull) {
                p.add(overlayText);
                modelCanvas.relayout();
                isFull = modelCanvas.getRenderer().getPropertyAsBoolean(Property.FULL);
            }
        }
        modelCanvas.getRenderer().flush();
    }

    private Map<String, List> parseDAParam(PdfString DA) throws IOException {
        Map<String, List> commandArguments = new HashMap<String, List>();

        PdfTokenizer tokeniser = new PdfTokenizer(new RandomAccessFileOrArray(new RandomAccessSourceFactory().createSource(DA.toUnicodeString().getBytes())));
        List currentArguments = new ArrayList();

        while (tokeniser.nextToken()) {
            if (tokeniser.getTokenType() == PdfTokenizer.TokenType.Other) {
                String key = tokeniser.getStringValue();

                if (key.equals("RG") || key.equals("G") || key.equals("K")) {
                    key = "StrokeColor";
                } else if (key.equals("rg") || key.equals("g") || key.equals("k")) {
                    key = "FillColor";
                }

                commandArguments.put(key, currentArguments);
                currentArguments = new ArrayList();
            } else {
                switch (tokeniser.getTokenType()) {
                    case Number:
                        currentArguments.add(new PdfNumber(new Float(tokeniser.getStringValue())));
                        break;
                    case Name:
                        currentArguments.add(new PdfName(tokeniser.getStringValue()));
                        break;
                    default:
                        currentArguments.add(tokeniser.getStringValue());
                }
            }
        }

        return commandArguments;
    }

    private PdfFont getFontFromAcroForm(PdfName fontName) {
        PdfDictionary formDictionary = pdfDocument.getCatalog().getPdfObject().getAsDictionary(PdfName.AcroForm);
        PdfDictionary resources = formDictionary.getAsDictionary(PdfName.DR);
        PdfDictionary fonts = resources.getAsDictionary(PdfName.Font);

        return PdfFontFactory.createFont(fonts.getAsDictionary(fontName));
    }

    private Color getColor(List colorArgs) {
        Color color = null;
        switch (colorArgs.size()) {
            case 1:
                color = new DeviceGray(((PdfNumber) colorArgs.get(0)).getFloatValue());
                break;

            case 3:
                color = new DeviceRgb(((PdfNumber) colorArgs.get(0)).getFloatValue(),
                        ((PdfNumber) colorArgs.get(1)).getFloatValue(),
                        ((PdfNumber) colorArgs.get(2)).getFloatValue());
                break;

            case 4:
                color = new DeviceCmyk(((PdfNumber) colorArgs.get(0)).getFloatValue(),
                        ((PdfNumber) colorArgs.get(1)).getFloatValue(),
                        ((PdfNumber) colorArgs.get(2)).getFloatValue(),
                        ((PdfNumber) colorArgs.get(3)).getFloatValue());
                break;
        }
        return color;
    }
}
