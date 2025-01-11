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
package com.itextpdf.pdfcleanup;

import com.itextpdf.io.font.FontProgram;
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

    private float currLeading = 0F;

    /**
     * Get the current leading
     */
    float getCurrLeading() {
        return currLeading;
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
        removedTextShift += FontProgram.convertTextSpaceToGlyphSpace(
                shift * fontSize * (scaling / FontProgram.HORIZONTAL_SCALING_FACTOR));
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
                if (canvas.getGraphicsState().getLeading() != currLeading) {
                    canvas.setLeading(currLeading);
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
            if (canvasGs.getLeading() != currLeading) {
                canvas.setLeading(currLeading);
            }
            // after new line operator, removed text shift doesn't matter
            removedTextShift = null;
        }
        PdfTextArray tjShiftArray = null;
        if (removedTextShift != null) {
            final float tjShift = (float) (FontProgram.convertGlyphSpaceToTextSpace((float)removedTextShift) / (
                    canvasGs.getFontSize() * canvasGs.getHorizontalScaling() / FontProgram.HORIZONTAL_SCALING_FACTOR));
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
