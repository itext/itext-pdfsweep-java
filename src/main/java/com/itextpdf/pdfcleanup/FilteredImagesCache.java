package com.itextpdf.pdfcleanup;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfStream;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class FilteredImagesCache {
    private Map<PdfIndirectReference, List<FilteredImageKey>> cache = new HashMap<>();

    static FilteredImageKey createFilteredImageKey(ImageRenderInfo image, List<Rectangle> areasToBeCleaned, PdfDocument document) {
        PdfStream imagePdfObject = image.getImage().getPdfObject();
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
        private ImageRenderInfo image;
        private List<Rectangle> cleanedAreas;
        private PdfImageXObject filteredImage;

        FilteredImageKey(ImageRenderInfo imageInfo, List<Rectangle> cleanedAreas) {
            this.image = imageInfo;
            this.cleanedAreas = cleanedAreas;
        }

        List<Rectangle> getCleanedAreas() {
            return cleanedAreas;
        }

        ImageRenderInfo getImageRenderInfo() {
            return image;
        }

        PdfIndirectReference getImageIndRef() {
            return image.getImage().getPdfObject().getIndirectReference();
        }

        PdfImageXObject getFilteredImage() {
            return filteredImage;
        }

        void setFilteredImage(PdfImageXObject filteredImage) {
            this.filteredImage = filteredImage;
        }
    }
}
