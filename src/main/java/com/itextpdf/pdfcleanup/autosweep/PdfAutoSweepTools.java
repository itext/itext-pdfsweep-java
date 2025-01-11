/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2025 Apryse Group NV
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
package com.itextpdf.pdfcleanup.autosweep;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;
import com.itextpdf.kernel.pdf.annot.PdfRedactAnnotation;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.parser.PdfDocumentContentParser;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IPdfTextLocation;
import com.itextpdf.pdfcleanup.PdfCleanUpLocation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Class that automatically extracts all regions of interest from a given PdfDocument and redacts them.
 */
public class PdfAutoSweepTools {

    private ICleanupStrategy strategy;
    private int annotationNumber = 1;

    /**
     * Construct a new instance of PdfAutoSweepTools with a given ICleanupStrategy
     *
     * @param strategy the redaction strategy to be used
     */
    public PdfAutoSweepTools(ICleanupStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Highlight areas of interest in a given {@link PdfDocument}
     *
     * @param pdfDocument the {@link PdfDocument} to be highlighted
     */
    public void highlight(PdfDocument pdfDocument) {
        for (int i = 1; i <= pdfDocument.getNumberOfPages(); i++) {
            highlight(pdfDocument.getPage(i));
        }
    }

    /**
     * Highlight areas of interest in a given {@link PdfPage}
     *
     * @param pdfPage the {@link PdfPage} to be highlighted
     */
    public void highlight(PdfPage pdfPage) {
        List<PdfCleanUpLocation> cleanUpLocations = getPdfCleanUpLocations(pdfPage);
        for (PdfCleanUpLocation loc : cleanUpLocations) {
            PdfCanvas canvas = new PdfCanvas(pdfPage);
            canvas.setColor(loc.getCleanUpColor(), true);
            canvas.rectangle(loc.getRegion());
            canvas.fill();
        }
    }

    /**
     * Perform tentative cleanup of areas of interest on a given {@link PdfDocument}
     * This method will add all redaction annotations to the given document, allowing
     * the end-user to choose which redactions to keep or delete.
     *
     * @param pdfDocument the document to clean up
     */
    public void tentativeCleanUp(PdfDocument pdfDocument) {
        annotationNumber = 1;
        for (int i = 1; i <= pdfDocument.getNumberOfPages(); i++) {
            tentativeCleanUp(pdfDocument.getPage(i));
        }
    }

    /**
     * Perform tentative cleanup of areas of interest on a given {@link PdfPage}
     * This method will add all redaction annotations to the given page, allowing
     * the end-user to choose which redactions to keep or delete.
     *
     * @param pdfPage the page to clean up
     */
    public void tentativeCleanUp(PdfPage pdfPage) {
        List<PdfCleanUpLocation> cleanUpLocations = getPdfCleanUpLocations(pdfPage);
        for (PdfCleanUpLocation loc : cleanUpLocations) {
            PdfString title = new PdfString("Annotation:" + annotationNumber);
            annotationNumber++;
            float[] color = loc.getCleanUpColor().getColorValue();

            // convert to annotation
            PdfAnnotation redact = new PdfRedactAnnotation(loc.getRegion())
                    .setDefaultAppearance(new PdfString("Helvetica 12 Tf 0 g"))
                    .setTitle(title)
                    .put(PdfName.Subj, PdfName.Redact)
                    .put(PdfName.IC, new PdfArray(new float[]{0f, 0f, 0f}))
                    .put(PdfName.OC, new PdfArray(color));

            pdfPage.addAnnotation(redact);
        }
    }

    /**
     * Get all {@link PdfCleanUpLocation} objects from a given {@link PdfPage}.
     *
     * @param page the {@link PdfPage} to be processed
     *
     * @return a List of {@link PdfCleanUpLocation} objects
     */
    public List<PdfCleanUpLocation> getPdfCleanUpLocations(PdfPage page) {
        // get document
        PdfDocument doc = page.getDocument();

        // create parser
        PdfDocumentContentParser parser = new PdfDocumentContentParser(doc);

        // get page number
        int pageNr = doc.getPageNumber(page);

        // process document
        List<PdfCleanUpLocation> toClean = new ArrayList<>();
        parser.processContent(pageNr, strategy);
        for (IPdfTextLocation rect : strategy.getResultantLocations()) {
            if (rect != null) {
                toClean.add(new PdfCleanUpLocation(pageNr, rect.getRectangle(), strategy.getRedactionColor(rect)));
            }
        }

        // reset strategy for next iteration
        resetStrategy();

        // return
        return toClean;
    }

    /**
     * Get all {@link PdfCleanUpLocation} objects from a given {@link PdfDocument}.
     *
     * @param doc the {@link PdfDocument} to be processed
     *
     * @return a List of {@link PdfCleanUpLocation} objects
     */
    public List<PdfCleanUpLocation> getPdfCleanUpLocations(PdfDocument doc) {
        PdfDocumentContentParser parser = new PdfDocumentContentParser(doc);
        List<PdfCleanUpLocation> toClean = new ArrayList<>();
        for (int pageNr = 1; pageNr <= doc.getNumberOfPages(); pageNr++) {
            parser.processContent(pageNr, strategy);
            for (IPdfTextLocation rect : strategy.getResultantLocations()) {
                if (rect != null) {
                    toClean.add(new PdfCleanUpLocation(pageNr, rect.getRectangle(), strategy.getRedactionColor(rect)));
                }
            }
            resetStrategy();
        }
        java.util.Collections.sort(toClean, new Comparator<PdfCleanUpLocation>() {
            @Override
            public int compare(PdfCleanUpLocation o1, PdfCleanUpLocation o2) {
                if (o1.getPage() != o2.getPage()) {
                    return o1.getPage() < o2.getPage() ? -1 : 1;
                }
                Rectangle r1 = o1.getRegion();
                Rectangle r2 = o2.getRegion();
                if (r1.getY() == r2.getY()) {
                    return r1.getX() == r2.getX() ? 0 : (r1.getX() < r2.getX() ? -1 : 1);
                } else {
                    return r1.getY() < r2.getY() ? -1 : 1;
                }
            }
        });
        return toClean;
    }

    private void resetStrategy() {
        strategy = strategy.reset();
    }
}
