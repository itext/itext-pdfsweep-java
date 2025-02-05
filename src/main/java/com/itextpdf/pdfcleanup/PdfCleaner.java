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

import com.itextpdf.commons.actions.contexts.IMetaInfo;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.pdfcleanup.autosweep.ICleanupStrategy;
import com.itextpdf.pdfcleanup.autosweep.PdfAutoSweepTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

/**
 * Main entry point for cleaning a PDF document.
 * This class contains a series of static methods that accept PDF file as a {@link InputStream}
 * or already opened {@link PdfDocument} and performs erasing of data in regions specified by input
 * arguments. The output can either be preserved in passed {@link PdfDocument} with possibility to
 * post-process the document, or in an {@link OutputStream} in a form of a complete PDF file.
 *
 * <p>
 * The important difference between overloads with InputStream/OutputStream parameters and
 * {@link PdfDocument} parameter is in the consumption of product license limits.
 */
public final class PdfCleaner {

    private PdfCleaner() {
        // this class is designed to be used with static methods only
    }

    /**
     * Cleans the document by erasing all the areas which are provided.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param inputPdf         the pdf document InputStream to which cleaned up applies
     * @param outputPdf        the cleaned up pdf document OutputStream
     * @param cleanUpLocations list of locations to be cleaned up
     *
     * @throws IOException if an I/O error occurs
     */
    public static void cleanUp(InputStream inputPdf, OutputStream outputPdf, List<PdfCleanUpLocation> cleanUpLocations)
            throws IOException {
        cleanUp(inputPdf, outputPdf, cleanUpLocations, new CleanUpProperties());
    }

    /**
     * Cleans the document by erasing all the areas which are provided.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param inputPdf         the pdf document InputStream to which cleaned up applies
     * @param outputPdf        the cleaned up pdf document OutputStream
     * @param cleanUpLocations list of locations to be cleaned up
     * @param properties       additional properties for cleanUp
     *
     * @throws IOException if an I/O error occurs
     */
    public static void cleanUp(InputStream inputPdf, OutputStream outputPdf, List<PdfCleanUpLocation> cleanUpLocations,
            CleanUpProperties properties) throws IOException {
        StampingProperties stampingProperties = new StampingProperties();
        IMetaInfo propertiesMetaInfo = properties.getMetaInfo();
        stampingProperties
                .setEventCountingMetaInfo(propertiesMetaInfo == null ? new CleanUpToolMetaInfo() : propertiesMetaInfo);

        try (
                PdfReader reader = new PdfReader(inputPdf);
                PdfWriter writer = new PdfWriter(outputPdf);
                PdfDocument pdfDocument = new PdfDocument(reader, writer, stampingProperties)
        ) {
            cleanUp(pdfDocument, cleanUpLocations, properties);
        }
    }

    /**
     * Cleans the document by erasing all the areas which are provided.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param pdfDocument      a document to which cleaned up applies
     * @param cleanUpLocations list of locations to be cleaned up
     *
     * @throws IOException if an I/O error occurs
     */
    public static void cleanUp(PdfDocument pdfDocument, List<PdfCleanUpLocation> cleanUpLocations) throws IOException {
        cleanUp(pdfDocument, cleanUpLocations, new CleanUpProperties());
    }

    /**
     * Cleans the document by erasing all the areas which are provided.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param pdfDocument      a document to which cleaned up applies
     * @param cleanUpLocations list of locations to be cleaned up
     * @param properties       additional properties for cleanUp
     *
     * @throws IOException if an I/O error occurs
     */
    public static void cleanUp(PdfDocument pdfDocument, List<PdfCleanUpLocation> cleanUpLocations,
            CleanUpProperties properties) throws IOException {
        PdfCleanUpTool cleanUpTool = new PdfCleanUpTool(pdfDocument, cleanUpLocations, properties);
        cleanUpTool.cleanUp();
    }

    /**
     * Perform cleanup of areas of interest based on a given cleanup strategy.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param inputPdf  the pdf document InputStream to which cleaned up applies
     * @param outputPdf the cleaned up pdf document OutputStream
     * @param strategy  cleanup strategy to be used
     *
     * @throws IOException if an I/O error occurs
     */
    public static void autoSweepCleanUp(InputStream inputPdf, OutputStream outputPdf, ICleanupStrategy strategy)
            throws IOException {
        autoSweepCleanUp(inputPdf, outputPdf, strategy, new CleanUpProperties());
    }

    /**
     * Perform cleanup of areas of interest based on a given cleanup strategy.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param inputPdf                   the pdf document InputStream to which cleaned up applies
     * @param outputPdf                  the cleaned up pdf document OutputStream
     * @param strategy                   cleanup strategy to be used
     * @param additionalCleanUpLocations list of additional locations to be cleaned up
     *
     * @throws IOException if an I/O error occurs
     */
    public static void autoSweepCleanUp(InputStream inputPdf, OutputStream outputPdf, ICleanupStrategy strategy,
            List<PdfCleanUpLocation> additionalCleanUpLocations) throws IOException {
        autoSweepCleanUp(inputPdf, outputPdf, strategy, additionalCleanUpLocations, new CleanUpProperties());
    }

    /**
     * Perform cleanup of areas of interest based on a given cleanup strategy.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param inputPdf   the pdf document InputStream to which cleaned up applies
     * @param outputPdf  the cleaned up pdf document OutputStream
     * @param strategy   cleanup strategy to be used
     * @param properties additional properties for cleanUp
     *
     * @throws IOException if an I/O error occurs
     */
    public static void autoSweepCleanUp(InputStream inputPdf, OutputStream outputPdf, ICleanupStrategy strategy,
            CleanUpProperties properties) throws IOException {
        autoSweepCleanUp(inputPdf, outputPdf, strategy, Collections.<PdfCleanUpLocation>emptyList(), properties);
    }

    /**
     * Perform cleanup of areas of interest based on a given cleanup strategy.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param inputPdf                   the pdf document InputStream to which cleaned up applies
     * @param outputPdf                  the cleaned up pdf document OutputStream
     * @param strategy                   cleanup strategy to be used
     * @param additionalCleanUpLocations list of additional locations to be cleaned up
     * @param properties                 additional properties for cleanUp
     *
     * @throws IOException if an I/O error occurs
     */
    public static void autoSweepCleanUp(InputStream inputPdf, OutputStream outputPdf, ICleanupStrategy strategy,
            List<PdfCleanUpLocation> additionalCleanUpLocations, CleanUpProperties properties) throws IOException {
        StampingProperties stampingProperties = new StampingProperties();
        IMetaInfo propertiesMetaInfo = properties.getMetaInfo();
        stampingProperties
                .setEventCountingMetaInfo(propertiesMetaInfo == null ? new CleanUpToolMetaInfo() : propertiesMetaInfo);

        try (
                PdfReader reader = new PdfReader(inputPdf);
                PdfWriter writer = new PdfWriter(outputPdf);
                PdfDocument pdfDocument = new PdfDocument(reader, writer, stampingProperties)
        ) {
            autoSweepCleanUp(pdfDocument, strategy, additionalCleanUpLocations, properties);
        }
    }

    /**
     * Perform cleanup of areas of interest based on a given cleanup strategy.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param pdfDocument a document to which cleaned up applies
     * @param strategy    cleanup strategy to be used
     *
     * @throws IOException if an I/O error occurs
     */
    public static void autoSweepCleanUp(PdfDocument pdfDocument, ICleanupStrategy strategy) throws IOException {
        autoSweepCleanUp(pdfDocument, strategy, new CleanUpProperties());
    }

    /**
     * Perform cleanup of areas of interest based on a given cleanup strategy.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param pdfDocument a document to which cleaned up applies
     * @param strategy    cleanup strategy to be used
     * @param properties  additional properties for cleanUp
     *
     * @throws IOException if an I/O error occurs
     */
    public static void autoSweepCleanUp(PdfDocument pdfDocument, ICleanupStrategy strategy,
            CleanUpProperties properties) throws IOException {
        List<PdfCleanUpLocation> cleanUpLocations = new PdfAutoSweepTools(strategy).getPdfCleanUpLocations(pdfDocument);
        cleanUp(pdfDocument, cleanUpLocations, properties);
    }

    /**
     * Perform cleanup of areas of interest based on a given cleanup strategy.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param pdfDocument                a document to which cleaned up applies
     * @param strategy                   cleanup strategy to be used
     * @param additionalCleanUpLocations list of additional locations to be cleaned up
     *
     * @throws IOException if an I/O error occurs
     */
    public static void autoSweepCleanUp(PdfDocument pdfDocument, ICleanupStrategy strategy,
            List<PdfCleanUpLocation> additionalCleanUpLocations) throws IOException {
        List<PdfCleanUpLocation> cleanUpLocations = new PdfAutoSweepTools(strategy).getPdfCleanUpLocations(pdfDocument);
        cleanUpLocations.addAll(additionalCleanUpLocations);
        cleanUp(pdfDocument, cleanUpLocations, new CleanUpProperties());
    }

    /**
     * Perform cleanup of areas of interest based on a given cleanup strategy.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param pdfDocument                a document to which cleaned up applies
     * @param strategy                   cleanup strategy to be used
     * @param additionalCleanUpLocations list of additional locations to be cleaned up
     * @param properties                 additional properties for cleanUp
     *
     * @throws IOException if an I/O error occurs
     */
    public static void autoSweepCleanUp(PdfDocument pdfDocument, ICleanupStrategy strategy,
            List<PdfCleanUpLocation> additionalCleanUpLocations, CleanUpProperties properties) throws IOException {
        List<PdfCleanUpLocation> cleanUpLocations = new PdfAutoSweepTools(strategy).getPdfCleanUpLocations(pdfDocument);
        cleanUpLocations.addAll(additionalCleanUpLocations);
        cleanUp(pdfDocument, cleanUpLocations, properties);
    }

    /**
     * Perform cleanup of areas of interest based on a given cleanup strategy.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param pdfPage  the {@link PdfPage} to which cleaned up applies
     * @param strategy cleanup strategy to be used
     *
     * @throws IOException if an I/O error occurs
     */
    public static void autoSweepCleanUp(PdfPage pdfPage, ICleanupStrategy strategy) throws IOException {
        autoSweepCleanUp(pdfPage, strategy, new CleanUpProperties());
    }

    /**
     * Perform cleanup of areas of interest based on a given cleanup strategy.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param pdfPage                    the {@link PdfPage} to which cleaned up applies
     * @param strategy                   cleanup strategy to be used
     * @param additionalCleanUpLocations list of additional locations to be cleaned up
     *
     * @throws IOException if an I/O error occurs
     */
    public static void autoSweepCleanUp(PdfPage pdfPage, ICleanupStrategy strategy,
            List<PdfCleanUpLocation> additionalCleanUpLocations) throws IOException {
        List<PdfCleanUpLocation> cleanUpLocations = new PdfAutoSweepTools(strategy).getPdfCleanUpLocations(pdfPage);
        cleanUpLocations.addAll(additionalCleanUpLocations);
        cleanUp(pdfPage.getDocument(), cleanUpLocations, new CleanUpProperties());
    }

    /**
     * Perform cleanup of areas of interest based on a given cleanup strategy.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param pdfPage    the {@link PdfPage} to which cleaned up applies
     * @param strategy   cleanup strategy to be used
     * @param properties additional properties for cleanUp
     *
     * @throws IOException if an I/O error occurs
     */
    public static void autoSweepCleanUp(PdfPage pdfPage, ICleanupStrategy strategy, CleanUpProperties properties)
            throws IOException {
        List<PdfCleanUpLocation> cleanUpLocations = new PdfAutoSweepTools(strategy).getPdfCleanUpLocations(pdfPage);
        cleanUp(pdfPage.getDocument(), cleanUpLocations, properties);
    }

    /**
     * Perform cleanup of areas of interest based on a given cleanup strategy.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param pdfPage                    the {@link PdfPage} to which cleaned up applies
     * @param strategy                   cleanup strategy to be used
     * @param additionalCleanUpLocations list of additional locations to be cleaned up
     * @param properties                 additional properties for cleanUp
     *
     * @throws IOException if an I/O error occurs
     */
    public static void autoSweepCleanUp(PdfPage pdfPage, ICleanupStrategy strategy,
            List<PdfCleanUpLocation> additionalCleanUpLocations, CleanUpProperties properties) throws IOException {
        List<PdfCleanUpLocation> cleanUpLocations = new PdfAutoSweepTools(strategy).getPdfCleanUpLocations(pdfPage);
        cleanUpLocations.addAll(additionalCleanUpLocations);
        cleanUp(pdfPage.getDocument(), cleanUpLocations, properties);
    }

    /**
     * Cleans the document by erasing regions defined by redact annotations inside the document.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param inputPdf  the pdf document InputStream to which cleaned up applies
     * @param outputPdf the cleaned up pdf document OutputStream
     *
     * @throws IOException if an I/O error occurs
     */
    public static void cleanUpRedactAnnotations(InputStream inputPdf, OutputStream outputPdf) throws IOException {
        cleanUpRedactAnnotations(inputPdf, outputPdf, new CleanUpProperties());
    }

    /**
     * Cleans the document by erasing regions defined by redact annotations inside the document.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param inputPdf   the pdf document InputStream to which cleaned up applies
     * @param outputPdf  the cleaned up pdf document OutputStream
     * @param properties additional properties for cleanUp
     *
     * @throws IOException if an I/O error occurs
     */
    public static void cleanUpRedactAnnotations(InputStream inputPdf, OutputStream outputPdf,
            CleanUpProperties properties)
            throws IOException {
        cleanUpRedactAnnotations(inputPdf, outputPdf, null, properties);
    }

    /**
     * Cleans the document by erasing regions defined by redact annotations inside the document.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param pdfDocument a document to which cleaned up applies
     *
     * @throws IOException if an I/O error occurs
     */
    public static void cleanUpRedactAnnotations(PdfDocument pdfDocument) throws IOException {
        cleanUpRedactAnnotations(pdfDocument, null, new CleanUpProperties());
    }

    /**
     * Cleans the document by erasing regions defined by redact annotations inside the document.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param pdfDocument a document to which cleaned up applies
     * @param properties  additional properties for cleanUp
     *
     * @throws IOException if an I/O error occurs
     */
    public static void cleanUpRedactAnnotations(PdfDocument pdfDocument, CleanUpProperties properties)
            throws IOException {
        cleanUpRedactAnnotations(pdfDocument, null, properties);
    }

    /**
     * Cleans the document by erasing regions defined by redact annotations and additional cleanup locations inside the
     * document.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param inputPdf                   the pdf document InputStream to which cleaned up applies
     * @param outputPdf                  the cleaned up pdf document OutputStream
     * @param additionalCleanUpLocations list of locations to be cleaned up
     *
     * @throws IOException if an I/O error occurs
     */
    public static void cleanUpRedactAnnotations(InputStream inputPdf, OutputStream outputPdf,
            List<PdfCleanUpLocation> additionalCleanUpLocations) throws IOException {
        cleanUpRedactAnnotations(inputPdf, outputPdf, additionalCleanUpLocations, new CleanUpProperties());
    }

    /**
     * Cleans the document by erasing regions defined by redact annotations and additional cleanup locations inside the
     * document.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param pdfDocument                a document to which cleaned up applies
     * @param additionalCleanUpLocations list of locations to be cleaned up
     *
     * @throws IOException if an I/O error occurs
     */
    public static void cleanUpRedactAnnotations(PdfDocument pdfDocument,
            List<PdfCleanUpLocation> additionalCleanUpLocations) throws IOException {
        cleanUpRedactAnnotations(pdfDocument, additionalCleanUpLocations, new CleanUpProperties());
    }

    /**
     * Cleans the document by erasing regions defined by redact annotations inside the document.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param inputPdf                   the pdf document InputStream to which cleaned up applies
     * @param outputPdf                  the cleaned up pdf document OutputStream
     * @param additionalCleanUpLocations list of locations to be cleaned up
     * @param properties                 additional properties for cleanUp
     *
     * @throws IOException if an I/O error occurs
     */
    public static void cleanUpRedactAnnotations(InputStream inputPdf, OutputStream outputPdf,
            List<PdfCleanUpLocation> additionalCleanUpLocations, CleanUpProperties properties) throws IOException {
        StampingProperties stampingProperties = new StampingProperties();
        IMetaInfo propertiesMetaInfo = properties.getMetaInfo();
        stampingProperties
                .setEventCountingMetaInfo(propertiesMetaInfo == null ? new CleanUpToolMetaInfo() : propertiesMetaInfo);

        try (
                PdfReader reader = new PdfReader(inputPdf);
                PdfWriter writer = new PdfWriter(outputPdf);
                PdfDocument pdfDocument = new PdfDocument(reader, writer, stampingProperties)
        ) {
            cleanUpRedactAnnotations(pdfDocument, additionalCleanUpLocations, properties);
        }
    }

    /**
     * Cleans the document by erasing regions defined by redact annotations inside the document.
     * Note, use methods with InputStream/OutputStream params if you don't want to consume itext-core product license
     * limits.
     *
     * @param pdfDocument                a document to which cleaned up applies
     * @param additionalCleanUpLocations list of locations to be cleaned up
     * @param properties                 additional properties for cleanUp
     *
     * @throws IOException if an I/O error occurs
     */
    public static void cleanUpRedactAnnotations(PdfDocument pdfDocument,
            List<PdfCleanUpLocation> additionalCleanUpLocations,
            CleanUpProperties properties) throws IOException {
        PdfCleanUpTool cleanUpTool = new PdfCleanUpTool(pdfDocument, true, properties);
        if (additionalCleanUpLocations != null) {
            for (PdfCleanUpLocation cleanUpLocation : additionalCleanUpLocations) {
                cleanUpTool.addCleanupLocation(cleanUpLocation);
            }
        }
        cleanUpTool.cleanUp();
    }

    static class CleanUpToolMetaInfo implements IMetaInfo {

    }
}
