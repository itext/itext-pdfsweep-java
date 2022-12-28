/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2023 iText Group NV
    Authors: iText Software.

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
package com.itextpdf.pdfcleanup.util;

import com.itextpdf.kernel.pdf.PdfArray;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.pdfcleanup.logs.CleanUpLogMessageConstant;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Utility class providing methods to check images compatibility.
 */
public final class CleanUpCsCompareUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanUpCsCompareUtil.class);

    private CleanUpCsCompareUtil() {
    }

    /**
     * Check whether the image info of the passed original image and the image info of the cleared
     * image are the same.
     *
     * @param originalImage {@link PdfImageXObject} of the original image
     * @param clearedImage  {@link PdfImageXObject} of the cleared image
     * @return true if the image infos are the same
     */
    public static boolean isOriginalCsCompatible(PdfImageXObject originalImage, PdfImageXObject clearedImage) {
        try {
            ImageInfo cmpInfo = Imaging.getImageInfo(originalImage.getImageBytes());
            ImageInfo toCompareInfo = Imaging.getImageInfo(clearedImage.getImageBytes());
            return (cmpInfo.getColorType() == toCompareInfo.getColorType()
                    && cmpInfo.isTransparent() == toCompareInfo.isTransparent()
                    && cmpInfo.getBitsPerPixel() == toCompareInfo.getBitsPerPixel())
                    || isCSApplicable(originalImage, toCompareInfo);
        } catch (ImageReadException | IOException e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(CleanUpLogMessageConstant.CANNOT_OBTAIN_IMAGE_INFO_AFTER_FILTERING, e);
            }
            return false;
        }
    }

    private static boolean isCSApplicable(PdfImageXObject originalImage, ImageInfo clearedImageInfo) {
        PdfObject pdfColorSpace = originalImage.getPdfObject().get(PdfName.ColorSpace);
        PdfName name;
        if (pdfColorSpace.isArray()) {
            name = ((PdfArray) pdfColorSpace).getAsName(0);
        } else if (pdfColorSpace.isName()) {
            name = (PdfName) pdfColorSpace;
        } else {
            name = new PdfName("");
        }

        // With use of pdf color space we can assume the image colorspace type
        // For Separation and DeviceGray color spaces we need to be sure that
        // the result image is 8 bit grayscale image
        if (PdfName.Separation.equals(name) || PdfName.DeviceGray.equals(name)) {
            return clearedImageInfo.getBitsPerPixel() == 8
                    && clearedImageInfo.getColorType() == ImageInfo.ColorType.GRAYSCALE;
        }
        return false;
    }
}
