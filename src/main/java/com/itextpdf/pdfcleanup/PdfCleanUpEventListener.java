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


import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.PathRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * An event listener which handles cleanup related events.
 */
public class PdfCleanUpEventListener implements IEventListener {
    private static final String textDataExpected = "Text data expected.";
    private static final String imageDataExpected = "Image data expected.";
    private static final String pathDataExpected = "Path data expected.";

    private List<IEventData> content = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void eventOccurred(IEventData data, EventType type) {
        switch (type) {
            case RENDER_TEXT:
            case RENDER_IMAGE:
            case RENDER_PATH:
                content.add(data);
                break;
            default:
                break;
        }
    }

    /**
     * Get the last encountered TextRenderInfo objects, then clears the internal buffer
     *
     * @return the TextRenderInfo objects that were encountered when processing the last text rendering operation
     */
    List<TextRenderInfo> getEncounteredText() {
        if (content.size() == 0) {
            throw new PdfException(textDataExpected);
        }
        ArrayList<TextRenderInfo> text = new ArrayList<>(content.size());
        for (IEventData data : content) {
            if (data instanceof TextRenderInfo) {
                text.add((TextRenderInfo) data);
            } else {
                throw new PdfException(textDataExpected);
            }
        }

        content.clear();
        return text;
    }

    /**
     * Get the last encountered ImageRenderInfo, then clears the internal buffer
     *
     * @return the ImageRenderInfo object that was encountered when processing the last image rendering operation
     */
    ImageRenderInfo getEncounteredImage() {
        if (content.size() == 0) {
            throw new PdfException(imageDataExpected);
        }

        IEventData eventData = content.get(0);
        if (!(eventData instanceof ImageRenderInfo)) {
            throw new PdfException(imageDataExpected);
        }
        content.clear();
        return (ImageRenderInfo) eventData;
    }

    /**
     * Get the last encountered PathRenderInfo, then clears the internal buffer
     *
     * @return the PathRenderInfo object that was encountered when processing the last path rendering operation
     */
    PathRenderInfo getEncounteredPath() {
        if (content.size() == 0) {
            throw new PdfException(pathDataExpected);
        }

        IEventData eventData = content.get(0);
        if (!(eventData instanceof PathRenderInfo)) {
            throw new PdfException(pathDataExpected);
        }
        content.clear();
        return (PathRenderInfo) eventData;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<EventType> getSupportedEvents() {
        return null;
    }
}
