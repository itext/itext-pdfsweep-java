/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 Apryse Group NV
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

import com.itextpdf.commons.actions.EventManager;
import com.itextpdf.io.source.PdfTokenizer;
import com.itextpdf.io.source.RandomAccessFileOrArray;
import com.itextpdf.io.source.RandomAccessSourceFactory;
import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceCmyk;
import com.itextpdf.kernel.colors.DeviceGray;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfBoolean;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;
import com.itextpdf.kernel.pdf.annot.PdfPopupAnnotation;
import com.itextpdf.kernel.pdf.annot.PdfRedactAnnotation;
import com.itextpdf.kernel.pdf.canvas.CanvasArtifact;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.layout.LayoutArea;
import com.itextpdf.layout.properties.Property;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.pdfcleanup.actions.event.PdfSweepProductEvent;
import com.itextpdf.pdfcleanup.exceptions.CleanupExceptionMessageConstant;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
     * When a document with line arts is being cleaned up, there are a lot of
     * calculations with floating point numbers. All of them are translated
     * into fixed point numbers by multiplying by this coefficient. Vary it
     * to adjust the preciseness of the calculations.
     * @deprecated
     */
    @Deprecated
    //TODO DEVSIX-5770 make this constant a single non-static configuration
    public static double floatMultiplier = Math.pow(10, 14);

    /**
     * Used as the criterion of a good approximation of rounded line joins
     * and line caps.
     * @deprecated
     */
    @Deprecated
    //TODO DEVSIX-5770 make this constant a single non-static configuration
    public static double arcTolerance = 0.0025;

    private PdfDocument pdfDocument;

    private boolean processAnnotations;

    /**
     * Key - page number, value - list of locations related to the page.
     */
    private Map<Integer, List<PdfCleanUpLocation>> pdfCleanUpLocations;

    /**
     * Keys - redact annotations to be removed from the document after clean up,
     * values - list of regions defined by redact annotation.
     */
    private Map<PdfRedactAnnotation, List<Rectangle>> redactAnnotations;

    private FilteredImagesCache filteredImagesCache;

    /**
     * Creates a {@link PdfCleanUpTool} object. No regions for erasing are specified.
     * Use {@link PdfCleanUpTool#addCleanupLocation(PdfCleanUpLocation)} method
     * to set regions to be erased from the document.
     *
     * @param pdfDocument A {@link PdfDocument} object representing the document to which redaction applies.
     */
    public PdfCleanUpTool(PdfDocument pdfDocument) {
        this(pdfDocument, false, new CleanUpProperties());
    }

    /**
     * Creates a {@link PdfCleanUpTool} object. If {@code cleanRedactAnnotations} is true,
     * regions to be erased are extracted from the redact annotations contained inside the given document.
     * Those redact annotations will be removed from the resultant document. If {@code cleanRedactAnnotations} is false,
     * then no regions for erasing are specified. In that case use {@link PdfCleanUpTool#addCleanupLocation(PdfCleanUpLocation)}
     * method to set regions to be erased from the document.
     *
     * @param pdfDocument A {@link PdfDocument} object representing the document to which redaction applies.
     * @param cleanRedactAnnotations if true - regions to be erased are extracted from the redact annotations contained
     * @param properties additional properties for clean-up process
     * inside the given document.
     */
    public PdfCleanUpTool(PdfDocument pdfDocument, boolean cleanRedactAnnotations, CleanUpProperties properties) {
        EventManager.getInstance().onEvent(PdfSweepProductEvent.createCleanupPdfEvent(
                pdfDocument.getDocumentIdWrapper(), properties.getMetaInfo()));

        if (pdfDocument.getReader() == null || pdfDocument.getWriter() == null) {
            throw new PdfException(CleanupExceptionMessageConstant.PDF_DOCUMENT_MUST_BE_OPENED_IN_STAMPING_MODE);
        }
        this.pdfDocument = pdfDocument;
        this.pdfCleanUpLocations = new HashMap<>();
        this.filteredImagesCache = new FilteredImagesCache();

        if (cleanRedactAnnotations) {
            addCleanUpLocationsBasedOnRedactAnnotations();
        }
        processAnnotations = properties.isProcessAnnotations();
    }

    /**
     * Creates a {@link PdfCleanUpTool} object based on the given {@link java.util.List}
     * of {@link PdfCleanUpLocation}s representing regions to be erased from the document.
     *
     * @param cleanUpLocations list of locations to be cleaned up {@link PdfCleanUpLocation}
     * @param pdfDocument a {@link PdfDocument} object representing the document to which redaction applies.
     * @param properties additional properties for clean-up process
     */
    public PdfCleanUpTool(PdfDocument pdfDocument, List<PdfCleanUpLocation> cleanUpLocations,
            CleanUpProperties properties) {
        this(pdfDocument, false, properties);
        for (PdfCleanUpLocation location : cleanUpLocations) {
            addCleanupLocation(location);
        }
    }

    /**
     * Adds a {@link PdfCleanUpLocation} to be cleaned up.
     *
     * @param cleanUpLocation a {@link PdfCleanUpLocation} to be cleaned up
     *
     * @return this {@link PdfCleanUpTool}
     */
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
     * Cleans the document by erasing all the areas which are provided or extracted from redaction annotations.
     *
     * @throws IOException IOException
     */
    public void cleanUp() throws IOException {
        for (Map.Entry<Integer, List<PdfCleanUpLocation>> entry : pdfCleanUpLocations.entrySet()) {
            cleanUpPage(entry.getKey(), entry.getValue());
        }

        if (redactAnnotations != null) { // if it isn't null, then we are in "extract locations from redact annots" mode
            removeRedactAnnots();
        }
        pdfCleanUpLocations.clear();
    }

    /**
     * Cleans a page from the document by erasing all the areas which
     * are provided or extracted from redaction annotations.
     *
     * @param pageNumber the page to be cleaned up
     * @param cleanUpLocations the locations to be cleaned up
     */
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
        cleanUpProcessor.setFilteredImagesCache(filteredImagesCache);
        cleanUpProcessor.processPageContent(page);
        if (processAnnotations) {
            cleanUpProcessor.processPageAnnotations(page, regions, redactAnnotations != null);
        }

        PdfCanvas pageCleanedContents = cleanUpProcessor.popCleanedCanvas();
        page.put(PdfName.Contents, pageCleanedContents.getContentStream());
        page.setResources(pageCleanedContents.getResources());

        colorCleanedLocations(pageCleanedContents, cleanUpLocations);
    }

    /**
     * Draws colored rectangles on the PdfCanvas corresponding to the PdfCleanUpLocation objects.
     *
     * @param canvas the PdfCanvas on which to draw
     * @param cleanUpLocations the PdfCleanUpLocations
     */
    private void colorCleanedLocations(PdfCanvas canvas, List<PdfCleanUpLocation> cleanUpLocations) {
        for (PdfCleanUpLocation location : cleanUpLocations) {
            if (location.getCleanUpColor() != null) {
                addColoredRectangle(canvas, location);
            }
        }
    }

    /**
     * Draws a colored rectangle on the PdfCanvas correponding to a PdfCleanUpLocation.
     *
     * @param canvas the PdfCanvas on which to draw
     * @param location the PdfCleanUpLocation
     */
    private void addColoredRectangle(PdfCanvas canvas, PdfCleanUpLocation location) {
        if (pdfDocument.isTagged()) {
            canvas.openTag(new CanvasArtifact());
        }

        // To avoid the float calculation precision differences in Java and .Net,
        // the values of rectangles to be drawn are rounded
        float x = (float)(Math.floor(location.getRegion().getX() * 2.0) / 2.0);
        float y = (float)(Math.floor(location.getRegion().getY() * 2.0) / 2.0);
        float width = (float)(Math.floor(location.getRegion().getWidth() * 2.0) / 2.0);
        float height = (float)(Math.floor(location.getRegion().getHeight() * 2.0) / 2.0);
        Rectangle rect = new Rectangle(x, y, width, height);

        canvas
                .saveState()
                .setFillColor(location.getCleanUpColor())
                .rectangle(rect)
                .fill()
                .restoreState();

        if (pdfDocument.isTagged()) {
            canvas.closeTag();
        }
    }

    /**
     * Adds clean up locations to be erased by extracting regions from the redact annotations
     * contained inside the given document. Those redact annotations will be removed from the resultant document.
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
        PdfArray quadPoints = redactAnnotation.getQuadPoints();
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

    /**
     * Convert a PdfArray of floats into a List of Rectangle objects.
     *
     * @param quadPoints input PdfArray
     */
    private List<Rectangle> translateQuadPointsToRectangles(PdfArray quadPoints) {
        List<Rectangle> rectangles = new ArrayList<Rectangle>();

        for (int i = 0; i < quadPoints.size(); i += 8) {
            float x = quadPoints.getAsNumber(i + 4).floatValue();
            float y = quadPoints.getAsNumber(i + 5).floatValue();
            float width = quadPoints.getAsNumber(i + 2).floatValue() - x;
            float height = quadPoints.getAsNumber(i + 3).floatValue() - y;
            rectangles.add(new Rectangle(x, // QuadPoints in redact annotations have "Z" order
                    y,
                    width,
                    height));
        }

        return rectangles;
    }

    /**
     * Remove the redaction annotations.
     * This method is called after the annotations are processed.
     *
     * @throws IOException signals that an I/O exception has occurred during redaction.
     */
    private void removeRedactAnnots() throws IOException {
        for (PdfRedactAnnotation annotation : redactAnnotations.keySet()) {
            PdfPage page = annotation.getPage();
            if (page != null) {
                page.removeAnnotation(annotation);

                PdfPopupAnnotation popup = annotation.getPopup();
                if (popup != null) {
                    page.removeAnnotation(popup);
                }
            }

            PdfCanvas canvas = new PdfCanvas(page);
            PdfStream redactRolloverAppearance = annotation.getRedactRolloverAppearance();
            PdfString overlayText = annotation.getOverlayText();
            Rectangle annotRect = annotation.getRectangle().toRectangle();

            if (redactRolloverAppearance != null) {
                drawRolloverAppearance(canvas, redactRolloverAppearance, annotRect, redactAnnotations.get(annotation));
            } else if (overlayText != null && !overlayText.toUnicodeString().isEmpty()) {
                drawOverlayText(canvas, overlayText.toUnicodeString(), annotRect, annotation.getRepeat(),
                        annotation.getDefaultAppearance(), annotation.getJustification());
            }
        }
    }

    private void drawRolloverAppearance(PdfCanvas canvas, PdfStream redactRolloverAppearance, Rectangle annotRect,
            List<Rectangle> cleanedRegions) {
        if (pdfDocument.isTagged()) {
            canvas.openTag(new CanvasArtifact());
        }

        canvas.saveState();

        for (Rectangle rect : cleanedRegions) {
            canvas.rectangle(rect.getLeft(), rect.getBottom(), rect.getWidth(), rect.getHeight());
        }
        canvas.clip().endPath();

        PdfFormXObject formXObject = new PdfFormXObject(redactRolloverAppearance);
        canvas.addXObjectWithTransformationMatrix(formXObject, 1, 0, 0, 1, annotRect.getLeft(), annotRect.getBottom());
        canvas.restoreState();

        if (pdfDocument.isTagged()) {
            canvas.closeTag();
        }
    }

    private void drawOverlayText(PdfCanvas canvas, String overlayText, Rectangle annotRect, PdfBoolean repeat,
            PdfString defaultAppearance, int justification) throws IOException {
        Map<String, List> parsedDA;
        try {
            parsedDA = parseDAParam(defaultAppearance);
        }catch (NullPointerException npe){
            throw new PdfException(CleanupExceptionMessageConstant.DEFAULT_APPEARANCE_NOT_FOUND);
        }
        PdfFont font;
        float fontSize = 12;
        List fontArgs = parsedDA.get("Tf");
        PdfDictionary formDictionary = pdfDocument.getCatalog().getPdfObject().getAsDictionary(PdfName.AcroForm);
        if (fontArgs != null && formDictionary != null) {
            font = getFontFromAcroForm((PdfName) fontArgs.get(0));
            fontSize = ((PdfNumber) fontArgs.get(1)).floatValue();
        } else {
            font = PdfFontFactory.createFont();
        }

        if (pdfDocument.isTagged()) {
            canvas.openTag(new CanvasArtifact());
        }

        Canvas modelCanvas = new Canvas(canvas, annotRect, false);

        Paragraph p = new Paragraph(overlayText).setFont(font).setFontSize(fontSize).setMargin(0);
        TextAlignment textAlignment = TextAlignment.LEFT;
        switch (justification) {
            case 1:
                textAlignment = TextAlignment.CENTER;
                break;
            case 2:
                textAlignment = TextAlignment.RIGHT;
                break;
            default:
        }
        p.setTextAlignment(textAlignment);
        List strokeColorArgs = parsedDA.get("StrokeColor");
        if (strokeColorArgs != null) {
            p.setStrokeColor(getColor(strokeColorArgs));
        }
        List fillColorArgs = parsedDA.get("FillColor");
        if (fillColorArgs != null) {
            p.setFontColor(getColor(fillColorArgs));
        }

        modelCanvas.add(p);
        if (repeat != null && repeat.getValue()) {
            boolean hasFull = modelCanvas.getRenderer().hasProperty(Property.FULL);
            boolean isFull = hasFull ? (boolean) modelCanvas.getRenderer().getPropertyAsBoolean(Property.FULL) : false;
            while (!isFull) {
                p.add(overlayText);
                LayoutArea previousArea = modelCanvas.getRenderer().getCurrentArea().clone();
                modelCanvas.relayout();
                if (modelCanvas.getRenderer().getCurrentArea().equals(previousArea)) {
                    // Avoid infinite loop. This might be caused by the fact that the font does not support the text we want to show
                    break;
                }
                hasFull = modelCanvas.getRenderer().hasProperty(Property.FULL);
                isFull = hasFull ? (boolean) modelCanvas.getRenderer().getPropertyAsBoolean(Property.FULL) : false;
            }
        }
        modelCanvas.getRenderer().flush();

        if (pdfDocument.isTagged()) {
            canvas.closeTag();
        }
    }

    private Map<String, List> parseDAParam(PdfString DA) throws IOException {
        Map<String, List> commandArguments = new HashMap<String, List>();

        PdfTokenizer tokeniser = new PdfTokenizer(
                new RandomAccessFileOrArray(
                        new RandomAccessSourceFactory().createSource(
                                DA.toUnicodeString().getBytes(StandardCharsets.UTF_8)
                        )
                )
        );
        List currentArguments = new ArrayList();

        while (tokeniser.nextToken()) {
            if (tokeniser.getTokenType() == PdfTokenizer.TokenType.Other) {
                String key = tokeniser.getStringValue();

                if ("RG".equals(key) || "G".equals(key) || "K".equals(key)) {
                    key = "StrokeColor";
                } else if ("rg".equals(key) || "g".equals(key) || "k".equals(key)) {
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
                color = new DeviceGray(((PdfNumber) colorArgs.get(0)).floatValue());
                break;

            case 3:
                color = new DeviceRgb(((PdfNumber) colorArgs.get(0)).floatValue(),
                        ((PdfNumber) colorArgs.get(1)).floatValue(),
                        ((PdfNumber) colorArgs.get(2)).floatValue());
                break;

            case 4:
                color = new DeviceCmyk(((PdfNumber) colorArgs.get(0)).floatValue(),
                        ((PdfNumber) colorArgs.get(1)).floatValue(),
                        ((PdfNumber) colorArgs.get(2)).floatValue(),
                        ((PdfNumber) colorArgs.get(3)).floatValue());
                break;
        }
        return color;
    }

}
