/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
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

import com.itextpdf.kernel.geom.Matrix;
import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfNumber;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfTextArray;
import com.itextpdf.kernel.pdf.canvas.CanvasGraphicsState;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;

import java.util.ArrayList;
import java.util.List;

class TextPositioning {

    private String prevOperator;
    private Float removedTextShift; // shift in text space units, which is the result of the removed text

    /**
     * Not null only when first pos operator encountered; when concatenation of operators is performed
     * this field is cleaned and positioning info is stored in either tdShift or tmShift fields.
     */
    private List<PdfObject> firstPositioningOperands;

    private float[] tdShift;
    private Matrix tmShift;

    private Float currLeading;

    /**
     * Get the current leading
     */
    float getCurrLeading() {
        if (currLeading != null) {
            return (float) currLeading;
        }
        return 0f;
    }

    void appendPositioningOperator(String operator, List<PdfObject> operands) {
        if (firstPositioningOperands != null) {
            storePositioningInfoInShiftFields();
        }

        if ("TD".equals(operator)) {
            currLeading = -((PdfNumber) operands.get(1)).floatValue();
        } else if ("TL".equals(operator)) {
            currLeading = ((PdfNumber) operands.get(0)).floatValue();
            return;
        }

        removedTextShift = null;

        if (prevOperator == null) {
            firstPositioningOperands = new ArrayList<>(operands);
            prevOperator = operator;
        } else {
            if ("Tm".equals(operator)) {
                clear();
                firstPositioningOperands = new ArrayList<>(operands);
                prevOperator = operator;
            } else {
                float tx;
                float ty;
                if ("T*".equals(operator)) {
                    tx = 0;
                    ty = -getCurrLeading();
                } else {
                    tx = ((PdfNumber) operands.get(0)).floatValue();
                    ty = ((PdfNumber) operands.get(1)).floatValue();
                }
                if ("Tm".equals(prevOperator)) {
                    tmShift = new Matrix(tx, ty).multiply(tmShift);
                    // prevOperator is left as TM here
                } else {
                    tdShift[0] += tx;
                    tdShift[1] += ty;
                    prevOperator = "Td"; // concatenation of two any TD, Td, T* result in Td
                }
            }
        }
    }

    private void storePositioningInfoInShiftFields() {
        if ("Tm".equals(prevOperator)) {
            tmShift = PdfCleanUpProcessor.operandsToMatrix(firstPositioningOperands);
        } else if ("T*".equals(prevOperator)) {
            tdShift = new float[]{0, -getCurrLeading()};
        } else {
            tdShift = new float[2];
            tdShift[0] = ((PdfNumber) firstPositioningOperands.get(0)).floatValue();
            tdShift[1] = ((PdfNumber) firstPositioningOperands.get(1)).floatValue();
        }
        firstPositioningOperands = null;
    }

    void appendTjArrayWithSingleNumber(PdfArray tjArray, float fontSize, float scaling) {
        if (removedTextShift == null) {
            removedTextShift = 0f;
        }

        float shift = tjArray.getAsNumber(0).floatValue();
        removedTextShift += shift * fontSize * (scaling / 100) / 1000;
    }

    /**
     * is performed when text object is ended or text chunk is written
     */
    void clear() {
        // leading is not removed, as it is preserved between different text objects
        firstPositioningOperands = null;
        prevOperator = null;
        removedTextShift = null;

        tdShift = null;
        tmShift = null;
    }

    void writePositionedText(String operator, List<PdfObject> operands, PdfArray cleanedText, PdfCanvas canvas) {
        writePositioningOperator(canvas);
        writeText(operator, operands, cleanedText, canvas);
        clear();
    }

    private void writePositioningOperator(PdfCanvas canvas) {
        if (firstPositioningOperands != null) {
            if ("T*".equals(prevOperator)) {
                float currLead = this.getCurrLeading();
                if (canvas.getGraphicsState().getLeading() != currLead) {
                    canvas.setLeading(currLead);
                }
            }
            PdfCleanUpProcessor.writeOperands(canvas, firstPositioningOperands);
        } else if (tdShift != null) {
            canvas.moveText(tdShift[0], tdShift[1]);
        } else if (tmShift != null) {
            canvas.setTextMatrix(tmShift.get(Matrix.I11), tmShift.get(Matrix.I12),
                    tmShift.get(Matrix.I21), tmShift.get(Matrix.I22), tmShift.get(Matrix.I31), tmShift.get(Matrix.I32));
        }
    }

    private void writeText(String operator, List<PdfObject> operands, PdfArray cleanedText, PdfCanvas canvas) {
        CanvasGraphicsState canvasGs = canvas.getGraphicsState();
        boolean newLineShowText = "'".equals(operator) || "\"".equals(operator);
        if (newLineShowText) {
            float currLead = this.getCurrLeading();
            if (canvasGs.getLeading() != currLead) {
                canvas.setLeading((float) currLead);
            }
            // after new line operator, removed text shift doesn't matter
            removedTextShift = null;
        }
        PdfTextArray tjShiftArray = null;
        if (removedTextShift != null) {
            float tjShift = (float) removedTextShift * 1000 / (canvasGs.getFontSize() * canvasGs.getHorizontalScaling() / 100);
            tjShiftArray = new PdfTextArray();
            tjShiftArray.add(new PdfNumber(tjShift));
        }
        if (cleanedText != null) {
            if (newLineShowText) {
                // char spacing and word spacing are set via writeNotAppliedTextStateParams() method
                canvas.newlineText();
            }
            if (removedTextShift != null) {
                tjShiftArray.addAll(cleanedText);
                cleanedText = tjShiftArray;
            }
            canvas.showText(cleanedText);
        } else {
            if (removedTextShift != null) {
                canvas.showText(tjShiftArray);
            }
            PdfCleanUpProcessor.writeOperands(canvas, operands);
        }
    }
}
