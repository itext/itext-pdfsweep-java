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

import com.itextpdf.kernel.geom.Rectangle;
import org.apache.commons.imaging.ImageFormat;
import org.apache.commons.imaging.ImageFormats;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.png.PngConstants;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class providing methods to handle images and work with graphics.
 */
public final class CleanUpImageUtil {
    private static final Color CLEANED_AREA_FILL_COLOR = Color.WHITE;

    private CleanUpImageUtil() {
    }

    /**
     * Clean up an image using a List of Rectangles that need to be redacted.
     *
     * @param imageBytes       the image to be cleaned up
     * @param areasToBeCleaned the List of Rectangles that need to be redacted out of the image
     * @return an array of bytes of the image with cleaned areas.
     */
    public static byte[] cleanUpImage(byte[] imageBytes, List<Rectangle> areasToBeCleaned) {
        if (areasToBeCleaned.isEmpty()) {
            return imageBytes;
        }

        try {
            final ImageInfo imageInfo = Imaging.getImageInfo(imageBytes);
            BufferedImage image = getBuffer(imageBytes, imageInfo.getFormat());
            cleanImage(image, areasToBeCleaned);
            return writeImage(image, imageInfo);
        } catch (ImageReadException | ImageWriteException | IOException e) {
            throw new CleanupImageHandlingUtilException(e.getMessage(), e);
        }
    }

    /**
     * Clean up a BufferedImage using a List of Rectangles that need to be redacted.
     *
     * @param image            the image to be cleaned up
     * @param areasToBeCleaned the List of Rectangles that need to be redacted out of the image
     */
    private static void cleanImage(BufferedImage image, List<Rectangle> areasToBeCleaned) {
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(CLEANED_AREA_FILL_COLOR);

        // A rectangle in the areasToBeCleaned list is treated to be in standard [0,1]x[0,1] image space
        // (y varies from bottom to top and x from left to right), so we should scale the rectangle and also
        // invert and shear the y axe.
        for (Rectangle rect : areasToBeCleaned) {
            int imgHeight = image.getHeight();
            int imgWidth = image.getWidth();
            int[] scaledRectToClean = CleanUpHelperUtil.getImageRectToClean(rect, imgWidth, imgHeight);

            graphics.fillRect(scaledRectToClean[0], scaledRectToClean[1], scaledRectToClean[2], scaledRectToClean[3]);
        }

        graphics.dispose();
    }

    /**
     * Reads the image bytes into a {@link BufferedImage}.
     * Isolates and catches known Apache Commons Imaging bug for JPEG:
     * https://issues.apache.org/jira/browse/IMAGING-97
     *
     * @param imageBytes the image to be read, as a byte array
     * @return a BufferedImage, independent of the reading strategy
     */
    private static BufferedImage getBuffer(byte[] imageBytes, ImageFormat imageFormat) throws IOException {
        try {
            if (imageFormat != ImageFormats.JPEG) {
                return Imaging.getBufferedImage(imageBytes);
            }
        } catch (ImageReadException ire) {
            // No actions required in the catch block, because the default return should be performed.
        }

        return ImageIO.read(new ByteArrayInputStream(imageBytes));
    }

    private static byte[] writeImage(BufferedImage imageToWrite, ImageInfo originalImageInfo)
            throws IOException, ImageWriteException {
        // Apache can only read JPEG, so we should use awt for writing in this format
        if (originalImageInfo.getFormat() == ImageFormats.JPEG) {
            return getJPGBytes(imageToWrite);
        } else {
            Map<String, Object> params = new ConcurrentHashMap<>();

            // At least for PNG images if the resulted image can be grayscale, then Imaging makes
            // it grayscale. As we do not want to change image format at all, then we need to
            // force true color (if the image is not grayscale, then Imaging always uses true color).
            if (originalImageInfo.getFormat() == ImageFormats.PNG
                    && originalImageInfo.getColorType() != ImageInfo.ColorType.GRAYSCALE) {
                params.put(PngConstants.PARAM_KEY_PNG_FORCE_TRUE_COLOR, true);
            }

            return Imaging.writeImageToBytes(imageToWrite, originalImageInfo.getFormat(), params);
        }
    }

    /**
     * Get the bytes of the BufferedImage (in JPG format).
     *
     * @param image input image
     */
    private static byte[] getJPGBytes(BufferedImage image) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(1.0f);

            jpgWriter.setOutput(new MemoryCacheImageOutputStream(outputStream));
            IIOImage outputImage = new IIOImage(image, null, null);

            jpgWriter.write(null, outputImage, jpgWriteParam);
            jpgWriter.dispose();
            outputStream.flush();

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new CleanupImageHandlingUtilException(e.getMessage(), e);
        }
    }

    /**
     * Exception is thrown when errors occur during handling and cleaning up images.
     */
    public static class CleanupImageHandlingUtilException extends RuntimeException {
        /**
         * Creates a new {@link CleanUpImageUtil.CleanupImageHandlingUtilException}.
         *
         * @param msg the detail message.
         * @param e the exception to wrap.
         */
        public CleanupImageHandlingUtilException(String msg, Exception e) {
            super(msg, e);
        }
    }

}
