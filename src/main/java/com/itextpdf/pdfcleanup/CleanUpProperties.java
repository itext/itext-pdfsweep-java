/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2024 Apryse Group NV
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

/**
 * Contains properties for {@link PdfCleanUpTool} operations.
 */
public class CleanUpProperties {

    private IMetaInfo metaInfo;
    private boolean processAnnotations;

    /**
     * Creates default CleanUpProperties instance.
     */
    public CleanUpProperties() {
        processAnnotations = true;
    }

    /**
     * Returns metaInfo property.
     *
     * @return metaInfo property
     */
    IMetaInfo getMetaInfo() {
        return metaInfo;
    }

    /**
     * Sets additional meta info.
     *
     * @param metaInfo the meta info to set
     */
    public void setMetaInfo(IMetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    /**
     * Check if page annotations will be processed.
     * Default: {@code true}.
     *
     * @return {@code true} if annotations will be processed by the {@link PdfCleanUpTool}
     */
    public boolean isProcessAnnotations() {
        return processAnnotations;
    }

    /**
     * Set if page annotations will be processed.
     * Default processing behaviour: remove annotation if there is overlap with a redaction region.
     *
     * @param processAnnotations is page annotations will be processed
     */
    public void setProcessAnnotations(boolean processAnnotations) {
        this.processAnnotations = processAnnotations;
    }
}
