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


import com.itextpdf.kernel.PdfException;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.PathRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PdfCleanUpEventListener implements IEventListener {
    private static final String textDataExpected = "Text data expected.";
    private static final String imageDataExpected = "Image data expected.";
    private static final String pathDataExpected = "Path data expected.";

    private List<IEventData> content = new ArrayList<>();

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

    public List<TextRenderInfo> getEncounteredText() {
        if (content.isEmpty()) {
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

    public ImageRenderInfo getEncounteredImage() {
        if (content.isEmpty()) {
            throw new PdfException(imageDataExpected);
        }

        IEventData eventData = content.get(0);
        if (!(eventData instanceof ImageRenderInfo)) {
            throw new PdfException(imageDataExpected);
        }
        content.clear();
        return (ImageRenderInfo) eventData;
    }

    public PathRenderInfo getEncounteredPath() {
        if (content.isEmpty()) {
            throw new PdfException(pathDataExpected);
        }

        IEventData eventData = content.get(0);
        if (!(eventData instanceof PathRenderInfo)) {
            throw new PdfException(pathDataExpected);
        }
        content.clear();
        return (PathRenderInfo) eventData;

    }

    @Override
    public Set<EventType> getSupportedEvents() {
        return null;
    }
}
