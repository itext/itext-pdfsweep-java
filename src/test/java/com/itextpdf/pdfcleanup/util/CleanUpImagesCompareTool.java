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
package com.itextpdf.pdfcleanup.util;

import com.itextpdf.commons.utils.FileUtil;
import com.itextpdf.io.util.GhostscriptHelper;
import com.itextpdf.io.util.ImageMagickHelper;
import com.itextpdf.kernel.exceptions.PdfException;
import com.itextpdf.kernel.geom.AffineTransform;
import com.itextpdf.kernel.geom.Matrix;
import com.itextpdf.kernel.geom.NoninvertibleTransformException;
import com.itextpdf.kernel.geom.Point;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfIndirectReference;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfResources;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.ImageRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.kernel.utils.objectpathitems.ObjectPath;
import com.itextpdf.kernel.utils.objectpathitems.DictPathItem;
import com.itextpdf.kernel.utils.objectpathitems.LocalPathItem;
import com.itextpdf.pdfcleanup.exceptions.CleanupExceptionMessageConstant;
import com.itextpdf.test.ITextTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class CleanUpImagesCompareTool extends CompareTool {
    private Set<ObjectPath> ignoredObjectPaths = new LinkedHashSet<ObjectPath>();
    private Map<Integer, List<Rectangle>> ignoredImagesAreas = new HashMap<>();

    private String defaultFuzzValue = "0";

    private boolean useGs = false;

    private final ImageMagickHelper imageMagickHelper = new ImageMagickHelper();
    private final GhostscriptHelper ghostscriptHelper = new GhostscriptHelper();

    public CleanUpImagesCompareTool() {
    }

    /**
     * Specifies a flag, indicating if the required images to compare should be extracted
     * as pdf page images using GhostScript. This is an alternative to the default approach
     * of extracting image XObject data using iText functionality.
     * It might be required if iText fails to extract the image.
     *
     * @param flag: true - the images will extracted as page images
     */
    public void useGsImageExtracting(boolean flag) {
        this.useGs = flag;
    }

    public Map<Integer, List<Rectangle>> getIgnoredImagesAreas() {
        return ignoredImagesAreas;
    }

    public String extractAndCompareImages(String outputPdf, String cmpPdf, String outputPath)
            throws IOException, InterruptedException {
        return extractAndCompareImages(cmpPdf, outputPdf, outputPath, defaultFuzzValue);
    }

    @Override
    public String compareByContent(String outPdf, String cmpPdf, String outPath, String differenceImagePrefix,
            Map<Integer, List<Rectangle>> ignoredAreas, byte[] outPass, byte[] cmpPass)
            throws IOException, InterruptedException {
        if (ignoredImagesAreas.size() != 0) {
            if (ignoredAreas != null) {
                for (int pageNumber : ignoredAreas.keySet()) {
                    if (ignoredImagesAreas.containsKey(pageNumber)) {
                        ignoredImagesAreas.get(pageNumber).addAll(ignoredAreas.get(pageNumber));
                    } else {
                        ignoredImagesAreas.put(pageNumber, ignoredAreas.get(pageNumber));
                    }
                }
            }

            return super.compareByContent(outPdf, cmpPdf, outPath, differenceImagePrefix, ignoredImagesAreas,
                    outPass, cmpPass);
        } else {
            return super.compareByContent(outPdf, cmpPdf, outPath, differenceImagePrefix, ignoredAreas,
                    outPass, cmpPass);
        }
    }

    public String extractAndCompareImages(String outputPdf, String cmpPdf, String outputPath, String fuzzValue)
            throws IOException, InterruptedException {
        String outImgPath = outputPath + "out/";
        String cmpImgPath = outputPath + "cmp/";
        ITextTest.createOrClearDestinationFolder(outImgPath);
        ITextTest.createOrClearDestinationFolder(cmpImgPath);

        Map<Integer, PageImageObjectsPaths > cmpObjectDatas = extractImagesFromPdf(cmpPdf, cmpImgPath);
        Map<Integer, PageImageObjectsPaths > outObjectDatas = extractImagesFromPdf(outputPdf, outImgPath);
        if (cmpObjectDatas.size() != outObjectDatas.size()) {
            return "Number of pages differs in out and cmp pdf documents:\nout = "
                    + outObjectDatas.size() + "\ncmp = " + cmpObjectDatas.size();
        }

        try {
            for (int page : cmpObjectDatas.keySet()) {
                initializeIgnoredObjectPath(cmpObjectDatas.get(page), outObjectDatas.get(page));
            }
        } catch (Exception e) {
            return e.getMessage();
        }

        String[] cmpImages = FileUtil.listFilesInDirectory(cmpImgPath, true);
        String[] outImages = FileUtil.listFilesInDirectory(outImgPath, true);
        Arrays.sort(cmpImages);
        Arrays.sort(outImages);

        if (cmpImages.length != outImages.length) {
            return "Number of images should be the same!";
        }

        StringBuilder resultErrorMessage = new StringBuilder();
        try {
            for (int i = 0; i < cmpImages.length; i++) {
                String diffImage = outputPath + "diff_" + new File(cmpImages[i]).getName();
                String errorText = compareImages(outImages[i], cmpImages[i], diffImage, fuzzValue);
                if (errorText != null) {
                    resultErrorMessage.append(errorText);
                }
            }
        } catch (Exception e) {
            return e.getMessage();
        }

        return resultErrorMessage.toString();
    }

    @Override
    protected boolean compareObjects(PdfObject outObj, PdfObject cmpObj, ObjectPath currentPath,
            CompareTool.CompareResult compareResult) {
        if (ignoredObjectPaths.contains(currentPath)) {

            // Current objects should not be compared, if its ObjectPath is contained in ignored list
            return true;
        }

        return super.compareObjects(outObj, cmpObj, currentPath, compareResult);
    }

    private String compareImages(String outImage, String cmpImage, String differenceImage, String fuzzValue)
            throws Exception {
        System.out.print("Number of different pixels = ");
        if (!imageMagickHelper.runImageMagickImageCompare(outImage, cmpImage, differenceImage, fuzzValue)) {
            return "Images are visually different! Inspect " + differenceImage +
                    " to see the differences.\n";
        }

        return null;
    }

    private Map<Integer, PageImageObjectsPaths > extractImagesFromPdf(String pdf, String outputPath)
            throws IOException, InterruptedException {
        try (PdfReader readerPdf = new PdfReader(pdf);
                PdfDocument pdfDoc = new PdfDocument(readerPdf)) {
            Map<Integer, PageImageObjectsPaths > imageObjectDatas = new HashMap<>();

            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                PdfPage page = pdfDoc.getPage(i);
                PageImageObjectsPaths  imageObjectData =
                        new PageImageObjectsPaths (page.getPdfObject().getIndirectReference());
                Stack<LocalPathItem> baseLocalPath = new Stack<LocalPathItem>();

                PdfResources pdfResources = page.getResources();
                if (pdfResources.getPdfObject().isIndirect()) {
                    imageObjectData.addIndirectReference(pdfResources.getPdfObject().getIndirectReference());
                } else {
                    baseLocalPath.push(new DictPathItem(PdfName.Resources));
                }

                PdfDictionary xObjects = pdfResources.getResource(PdfName.XObject);
                if (xObjects == null) {
                    continue;
                }

                if (xObjects.isIndirect()) {
                    imageObjectData.addIndirectReference(xObjects.getIndirectReference());
                    baseLocalPath.clear();
                } else {
                    baseLocalPath.push(new DictPathItem(PdfName.XObject));
                }

                boolean isPageToGsExtract = false;
                for (PdfName objectName : xObjects.keySet()) {
                    if (!xObjects.get(objectName).isStream()
                            || !PdfName.Image.equals(xObjects.getAsStream(objectName).getAsName(PdfName.Subtype))) {
                        continue;
                    }

                    PdfImageXObject pdfObject = new PdfImageXObject(xObjects.getAsStream(objectName));
                    baseLocalPath.push(new DictPathItem(objectName));

                    if (!useGs) {
                        String extension = pdfObject.identifyImageFileExtension();
                        String fileName = outputPath + objectName + "_" + i + "." + extension;
                        createImageFromPdfXObject(fileName, pdfObject);
                    } else {
                        isPageToGsExtract = true;
                    }

                    Stack<LocalPathItem> reversedStack = new Stack<>();
                    reversedStack.addAll(baseLocalPath);
                    Stack<LocalPathItem> resultStack = new Stack<>();
                    resultStack.addAll(reversedStack);
                    imageObjectData.addLocalPath(resultStack);
                    baseLocalPath.pop();
                }

                if (useGs && isPageToGsExtract) {
                    String fileName = "Page_" + i;
                    ghostscriptHelper.runGhostScriptImageGeneration(pdf, outputPath, fileName, String.valueOf(i));
                }

                ImageRenderListener listener = new ImageRenderListener();
                PdfCanvasProcessor parser = new PdfCanvasProcessor(listener);
                parser.processPageContent(page);
                ignoredImagesAreas.put(i, listener.getImageRectangles());

                imageObjectDatas.put(i, imageObjectData);
            }
            return imageObjectDatas;
        }
    }

    private void createImageFromPdfXObject(String imageFileName, PdfImageXObject imageObject)
            throws IOException {
        try (FileOutputStream stream = new FileOutputStream(imageFileName)) {
            byte[] image = imageObject.getImageBytes();
            stream.write(image, 0, image.length);
        }
    }

    private void initializeIgnoredObjectPath(PageImageObjectsPaths  cmpPageObjects,
            PageImageObjectsPaths  outPageObjects) {
        try {
            List<PdfIndirectReference> cmpIndirects = cmpPageObjects.getIndirectReferences();
            List<PdfIndirectReference> outIndirects = outPageObjects.getIndirectReferences();
            PdfIndirectReference baseCmpIndirect = cmpIndirects.get(0);
            PdfIndirectReference baseOutIndirect = outIndirects.get(0);
            ObjectPath baseObjectPath = new ObjectPath(baseCmpIndirect, baseCmpIndirect);
            for (int i = 1; i < cmpIndirects.size(); i++) {
                baseObjectPath.resetDirectPath(cmpIndirects.get(i), outIndirects.get(i));
                baseCmpIndirect = cmpIndirects.get(i);
                baseOutIndirect = outIndirects.get(i);
            }

            for (Stack<LocalPathItem> path : cmpPageObjects.getDirectPaths()) {
                ignoredObjectPaths.add(new ObjectPath(baseCmpIndirect, baseOutIndirect,
                        path, baseObjectPath.getIndirectPath()));
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Out and cmp pdf documents have different object structure");
        }
    }

    private static class ImageRenderListener implements IEventListener {
        private List<Rectangle> imageRectangles = new ArrayList<>();

        public void eventOccurred(IEventData data, EventType type) {
            switch (type) {
                case RENDER_IMAGE: {
                    ImageRenderInfo renderInfo = (ImageRenderInfo) data;

                    if (!renderInfo.isInline()) {
                        Rectangle boundingRect = getImageBoundingBox(renderInfo.getImageCtm());
                        imageRectangles.add(boundingRect);
                    }

                    break;
                }

                default: {
                    break;
                }
            }
        }

        public List<Rectangle>  getImageRectangles() {
            return imageRectangles;
        }

        public Set<EventType> getSupportedEvents() {
            return null;
        }

        private Rectangle getImageBoundingBox(Matrix imageConcatMatrix) {
            Point[] points = transformPoints(imageConcatMatrix, false,
                    new Point(0, 0), new Point(0, 1),
                    new Point(1, 0), new Point(1, 1));

            return Rectangle.calculateBBox(Arrays.asList(points));
        }

        private Point[] transformPoints(Matrix transformationMatrix, boolean inverse, Point... points) {
            AffineTransform t = new AffineTransform(
                    transformationMatrix.get(Matrix.I11),
                    transformationMatrix.get(Matrix.I12),
                    transformationMatrix.get(Matrix.I21),
                    transformationMatrix.get(Matrix.I22),
                    transformationMatrix.get(Matrix.I31),
                    transformationMatrix.get(Matrix.I32));
            Point[] transformed = new Point[points.length];
            if (inverse) {
                try {
                    t = t.createInverse();
                } catch (NoninvertibleTransformException e) {
                    throw new PdfException(CleanupExceptionMessageConstant.NONINVERTIBLE_MATRIX_CANNOT_BE_PROCESSED, e);
                }
            }

            t.transform(points, 0, transformed, 0, points.length);
            return transformed;
        }
    }

    private static class PageImageObjectsPaths  {
        private List<PdfIndirectReference> pageIndirectReferencesPathToImageResources = new ArrayList<PdfIndirectReference>();

        private List<Stack<LocalPathItem>> directPaths = new ArrayList<Stack<LocalPathItem>>();

        public PageImageObjectsPaths (PdfIndirectReference baseIndirectReference) {
            this.pageIndirectReferencesPathToImageResources.add(baseIndirectReference);
        }

        public void addLocalPath(Stack<LocalPathItem> path) {
            this.directPaths.add(path);
        }

        public void addIndirectReference(PdfIndirectReference reference) {
            this.pageIndirectReferencesPathToImageResources.add(reference);
        }

        public List<Stack<LocalPathItem>> getDirectPaths() {
            return this.directPaths;
        }

        public List<PdfIndirectReference> getIndirectReferences() {
            return this.pageIndirectReferencesPathToImageResources;
        }
    }
}
