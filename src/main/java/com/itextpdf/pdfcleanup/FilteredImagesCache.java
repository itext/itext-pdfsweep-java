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

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class FilteredImagesCache {
    private Map<PdfIndirectReference, List<FilteredImageKey>> cache = new HashMap<>();

    static FilteredImageKey createFilteredImageKey(PdfImageXObject image, List<Rectangle> areasToBeCleaned, PdfDocument document) {
        PdfStream imagePdfObject = image.getPdfObject();
        if (imagePdfObject.getIndirectReference() == null) {
            imagePdfObject.makeIndirect(document);
        }
        return new FilteredImageKey(image, areasToBeCleaned);
    }

    /**
     * Retrieves saved result of image filtering based on given set of cleaning areas.
     * This won't handle the case when same filtering result is produced by different sets of areas,
     * e.g. if one set is { (0, 0, 50, 100), (50, 0, 50, 100)} and another one is {(0, 0, 100, 100)},
     * even though filtering results are essentially the same, current {@link FilteredImagesCache}
     * will treat this two cases as different filtering results.
     *
     * @param imageKey the defining filtering case
     * @return result of image filtering based on given set of cleaning areas if such was already processed and saved,
     * null otherwise.
     */
    PdfImageXObject get(FilteredImageKey imageKey) {
        List<FilteredImageKey> cachedFilteredImageKeys = cache.get(imageKey.getImageIndRef());
        if (cachedFilteredImageKeys != null) {
            for (FilteredImageKey cacheKey : cachedFilteredImageKeys) {
                if (rectanglesEqualWithEps(cacheKey.getCleanedAreas(), imageKey.getCleanedAreas())) {
                    return cacheKey.getFilteredImage();
                }
            }
        }
        return null;
    }

    void put(FilteredImageKey imageKey, PdfImageXObject filteredImage) {
        if (imageKey.getCleanedAreas() == null || imageKey.getCleanedAreas().isEmpty()) {
            return;
        }
        List<FilteredImageKey> filteredImageKeys = cache.get(imageKey.getImageIndRef());
        if (filteredImageKeys == null) {
            cache.put(imageKey.getImageIndRef(), filteredImageKeys = new ArrayList<>());
        }
        filteredImageKeys.add(imageKey);
        imageKey.setFilteredImage(filteredImage);
    }

    private boolean rectanglesEqualWithEps(List<Rectangle> cacheRects, List<Rectangle> keyRects) {
        if (keyRects == null || cacheRects.size() != keyRects.size()) {
            return false;
        }

        Set<Rectangle> cacheRectsSet = new LinkedHashSet<>(cacheRects);
        for (Rectangle keyArea : keyRects) {
            boolean found = false;
            for (Rectangle cacheArea : cacheRectsSet) {
                if (keyArea.equalsWithEpsilon(cacheArea)) {
                    found = true;
                    cacheRectsSet.remove(cacheArea);
                    break;
                }
            }
            if (!found) {
                break;
            }
        }
        return cacheRectsSet.isEmpty();
    }

    static class FilteredImageKey {
        private PdfImageXObject image;
        private List<Rectangle> cleanedAreas;
        private PdfImageXObject filteredImage;

        FilteredImageKey(PdfImageXObject image, List<Rectangle> cleanedAreas) {
            this.image = image;
            this.cleanedAreas = cleanedAreas;
        }

        List<Rectangle> getCleanedAreas() {
            return cleanedAreas;
        }

        PdfImageXObject getImageXObject() {
            return image;
        }

        PdfIndirectReference getImageIndRef() {
            return image.getPdfObject().getIndirectReference();
        }

        PdfImageXObject getFilteredImage() {
            return filteredImage;
        }

        void setFilteredImage(PdfImageXObject filteredImage) {
            this.filteredImage = filteredImage;
        }
    }
}
