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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.itextpdf.pdfcleanup.autosweep;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;
import com.itextpdf.kernel.pdf.annot.PdfRedactAnnotation;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.canvas.parser.PdfDocumentContentParser;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IPdfTextLocation;
import com.itextpdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.pdfcleanup.PdfCleanUpTool;

import java.io.IOException;
import java.util.*;

/**
 * Class that automatically extracts all regions of interest from a given PdfDocument and redacts them.
 */
public class PdfAutoSweep {

    private ICleanupStrategy strategy;

    /**
     * Construct a new instance of PdfAutoSweep with a given ICleanupStrategy
     *
     * @param strategy the redaction strategy to be used
     */
    public PdfAutoSweep(ICleanupStrategy strategy) {

        this.strategy = strategy;
    }

    private void resetStrategy() {
        strategy = strategy.reset();
    }

    /**
     * Get all {@code PdfCleanupLocation} objects from a given {@code PdfPage}
     *
     * @param page the {@code PdfPage} to be processed
     * @return
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
            if (rect != null)
                toClean.add(new PdfCleanUpLocation(pageNr, rect.getRectangle(), strategy.getRedactionColor(rect)));
        }

        // reset strategy for next iteration
        resetStrategy();

        // return
        return toClean;
    }

    /**
     * Get all {@code PdfCleanupLocation} objects from a given {@code PdfDocument}
     *
     * @param doc the {@code PdfDocument} to be processed
     * @return
     */
    public List<PdfCleanUpLocation> getPdfCleanUpLocations(PdfDocument doc) {
        PdfDocumentContentParser parser = new PdfDocumentContentParser(doc);
        List<PdfCleanUpLocation> toClean = new ArrayList<>();
        for (int pageNr = 1; pageNr <= doc.getNumberOfPages(); pageNr++) {
            parser.processContent(pageNr, strategy);
            for (IPdfTextLocation rect : strategy.getResultantLocations()) {
                if (rect != null)
                    toClean.add(new PdfCleanUpLocation(pageNr, rect.getRectangle(), strategy.getRedactionColor(rect)));
            }
            resetStrategy();
        }
        java.util.Collections.sort(toClean, new Comparator<PdfCleanUpLocation>() {
            @Override
            public int compare(PdfCleanUpLocation o1, PdfCleanUpLocation o2) {
                if(o1.getPage() != o2.getPage())
                    return o1.getPage() < o2.getPage() ? -1 : 1;
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

    /**
     * Highlight areas of interest in a given {@code PdfDocument}
     *
     * @param pdfDocument the {@code PdfDocument} to be highlighted
     */
    public void highlight(PdfDocument pdfDocument) {
        for (int i = 1; i <= pdfDocument.getNumberOfPages(); i++)
            highlight(pdfDocument.getPage(i));
    }

    /**
     * Highlight areas of interest in a given {@code PdfPage}
     *
     * @param pdfPage the {@code PdfPage} to be highlighted
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
     * Perform cleanup of areas of interest on a given {@code PdfDocument}
     *
     * @param pdfDocument the {@code PdfDocument} to be redacted
     * @throws IOException
     */
    public void cleanUp(PdfDocument pdfDocument) throws IOException {
        List<PdfCleanUpLocation> cleanUpLocations = getPdfCleanUpLocations(pdfDocument);
        PdfCleanUpTool cleaner = (cleanUpLocations == null)
                ? new PdfCleanUpTool(pdfDocument, true)
                : new PdfCleanUpTool(pdfDocument, cleanUpLocations);
        cleaner.cleanUp();
    }

    /**
     * Perform cleanup of areas of interest on a given {@code PdfPage}
     *
     * @param pdfPage the {@code PdfPage} to be redacted
     * @throws IOException
     */
    public void cleanUp(PdfPage pdfPage) throws IOException {
        List<PdfCleanUpLocation> cleanUpLocations = getPdfCleanUpLocations(pdfPage);
        PdfCleanUpTool cleaner = (cleanUpLocations == null)
                ? new PdfCleanUpTool(pdfPage.getDocument(), true)
                : new PdfCleanUpTool(pdfPage.getDocument(), cleanUpLocations);
        cleaner.cleanUp();
    }

    /**
     * Perform tentative cleanup of areas of interest on a given {@Code PdfDocument}
     * This method will add all redaction annotations to the given document, allowing
     * the end-user to choose which redactions to keep or delete.
     *
     * @param pdfDocument
     */
    public void tentativeCleanUp(PdfDocument pdfDocument) {
        for (int i = 1; i <= pdfDocument.getNumberOfPages(); i++)
            tentativeCleanUp(pdfDocument.getPage(i));
    }

    /**
     * Perform tentative cleanup of areas of interest on a given {@Code PdfPage}
     * This method will add all redaction annotations to the given page, allowing
     * the end-user to choose which redactions to keep or delete.
     *
     * @param pdfPage
     */
    public void tentativeCleanUp(PdfPage pdfPage) {
        List<PdfCleanUpLocation> cleanUpLocations = getPdfCleanUpLocations(pdfPage);

        // random title generation
        Random rnd = new Random(System.currentTimeMillis());
        Set<String> usedTitles = new HashSet<>();

        for (PdfCleanUpLocation loc : cleanUpLocations) {

            float[] color = loc.getCleanUpColor().getColorValue();

            // generate random annotation title
            String title = "Annotation:" + rnd.nextInt();
            while (usedTitles.contains(title))
                title = "Annotation:" + rnd.nextInt();

            // convert to annotation
            PdfAnnotation redact = new PdfRedactAnnotation(loc.getRegion())
                    .setDefaultAppearance(new PdfString("Helvetica 12 Tf 0 g"))
                    .setTitle(new PdfString(title))
                    .put(PdfName.Subj, PdfName.Redact)
                    .put(PdfName.IC, new PdfArray(new float[]{0f, 0f, 0f}))
                    .put(PdfName.OC, new PdfArray(color));

            pdfPage.addAnnotation(redact);
        }
    }
}
