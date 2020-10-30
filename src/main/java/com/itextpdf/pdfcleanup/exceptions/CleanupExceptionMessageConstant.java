package com.itextpdf.pdfcleanup.exceptions;

/**
 * Class that bundles all the error message templates as constants.
 */
public final class CleanupExceptionMessageConstant {
    public static final String DEFAULT_APPEARANCE_NOT_FOUND = "DefaultAppearance is required but not found";
    public static final String NONINVERTIBLE_MATRIX_CANNOT_BE_PROCESSED = "A noninvertible matrix has been parsed. The behaviour is unpredictable.";
    public static final String PDF_DOCUMENT_MUST_BE_OPENED_IN_STAMPING_MODE = "PdfDocument must be opened in stamping mode.";
}
