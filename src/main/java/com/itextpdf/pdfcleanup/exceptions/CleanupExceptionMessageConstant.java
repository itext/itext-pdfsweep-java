/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2021 iText Group NV
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
package com.itextpdf.pdfcleanup.exceptions;

/**
 * Class that bundles all the error message templates as constants.
 */
public final class CleanupExceptionMessageConstant {
    public static final String DEFAULT_APPEARANCE_NOT_FOUND = "DefaultAppearance is required but not found";
    public static final String NONINVERTIBLE_MATRIX_CANNOT_BE_PROCESSED = "A noninvertible matrix has been parsed. "
            + "The behaviour is unpredictable.";
    public static final String PDF_DOCUMENT_MUST_BE_OPENED_IN_STAMPING_MODE = "PdfDocument must be opened in stamping "
            + "mode.";

    private CleanupExceptionMessageConstant(){}
}
