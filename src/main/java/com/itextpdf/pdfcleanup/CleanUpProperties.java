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
import com.itextpdf.pdfcleanup.exceptions.CleanupExceptionMessageConstant;

/**
 * Contains properties for {@link PdfCleanUpTool} operations.
 */
public class CleanUpProperties {
//test comment for sharpen
    private IMetaInfo metaInfo;
    private boolean processAnnotations;
    private Double overlapRatio;
    private PathOffsetApproximationProperties offsetProperties = new PathOffsetApproximationProperties();

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
     *
     * @return this {@link CleanUpProperties} instance
     */
    public CleanUpProperties setMetaInfo(IMetaInfo metaInfo) {
        this.metaInfo = metaInfo;
        return this;
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
     *
     * @return this {@link CleanUpProperties} instance
     */
    public CleanUpProperties setProcessAnnotations(boolean processAnnotations) {
        this.processAnnotations = processAnnotations;
        return this;
    }

    /**
     * Gets the overlap ratio.
     * This is a value between 0 and 1 that indicates how much the content region should overlap with the redaction
     * area to be removed.
     *
     * @return the overlap ratio or {@code null} if it has not been set.
     */
    public Double getOverlapRatio() {
        return overlapRatio;
    }

    /**
     * Sets the overlap ratio.
     * This is a value between 0 and 1 that indicates how much the content region should overlap with the
     * redaction area to be removed.
     * <p>
     * Example: if the overlap ratio is set to 0.3, the content region will be removed if it overlaps with
     * the redaction area by at least 30%.
     *
     * @param overlapRatio the overlap ratio to set
     *
     * @return this {@link CleanUpProperties} instance
     */
    public CleanUpProperties setOverlapRatio(Double overlapRatio) {
        if (overlapRatio == null) {
            this.overlapRatio = null;
            return this;
        }
        if (overlapRatio <= 0 || overlapRatio > 1) {
            throw new IllegalArgumentException(CleanupExceptionMessageConstant.OVERLAP_RATIO_SHOULD_BE_IN_RANGE);
        }
        this.overlapRatio = overlapRatio;
        return this;
    }

    /**
     * Get {@link PathOffsetApproximationProperties} specifying approximation parameters for
     * {@link com.itextpdf.kernel.pdf.canvas.parser.clipper.ClipperOffset} operations.
     *
     * @return {@link PathOffsetApproximationProperties} parameters
     */
    public PathOffsetApproximationProperties getOffsetProperties() {
        return offsetProperties;
    }

    /**
     * Set {@link PathOffsetApproximationProperties} specifying approximation parameters for
     * {@link com.itextpdf.kernel.pdf.canvas.parser.clipper.ClipperOffset} operations.
     *
     * @param offsetProperties {@link PathOffsetApproximationProperties} to set
     *
     * @return this {@link CleanUpProperties} instance
     */
    public CleanUpProperties setOffsetProperties(PathOffsetApproximationProperties offsetProperties) {
        this.offsetProperties = offsetProperties;
        return this;
    }
}
