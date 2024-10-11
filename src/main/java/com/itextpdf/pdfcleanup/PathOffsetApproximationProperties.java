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

import com.itextpdf.kernel.pdf.canvas.parser.clipper.ClipperBridge;

/**
 * Contains properties for {@link com.itextpdf.kernel.pdf.canvas.parser.clipper.ClipperOffset} operations.
 */
public class PathOffsetApproximationProperties {
    private double arcTolerance = 0.0025;
    private boolean calculateOffsetMultiplierDynamically = false;

    /**
     * Creates new {@link PathOffsetApproximationProperties} instance.
     */
    public PathOffsetApproximationProperties() {
        // Empty constructor.
    }

    /**
     * Specifies if floatMultiplier should be calculated dynamically. Default value is {@code false}.
     *
     * <p>
     * When a document with line arts is being cleaned up, there are a lot of calculations with floating point numbers.
     * All of them are translated into fixed point numbers by multiplying by this floatMultiplier coefficient.
     * It is possible to dynamically adjust the preciseness of the calculations.
     *
     * @param calculateDynamically {@code true} if floatMultiplier should be calculated dynamically,
     *                             {@code false} for default value specified by {@link ClipperBridge#ClipperBridge()}
     *
     * @return this {@link PathOffsetApproximationProperties} instance
     */
    public PathOffsetApproximationProperties calculateOffsetMultiplierDynamically(boolean calculateDynamically) {
        this.calculateOffsetMultiplierDynamically = calculateDynamically;
        return this;
    }

    /**
     * Checks whether floatMultiplier should be calculated dynamically.
     *
     * <p>
     * When a document with line arts is being cleaned up, there are a lot of calculations with floating point numbers.
     * All of them are translated into fixed point numbers by multiplying by this floatMultiplier coefficient.
     * It is possible to dynamically adjust the preciseness of the calculations.
     *
     * @return {@code true} if floatMultiplier should be calculated dynamically, {@code false} for default value
     */
    public boolean calculateOffsetMultiplierDynamically() {
        return this.calculateOffsetMultiplierDynamically;
    }

    /**
     * Gets arc tolerance which is the maximum difference between the true and the faceted representation of curves
     * (arcs) in units. Used as the criterion of a good approximation of rounded line joins and line caps.
     *
     * <p>
     * Since flattened paths can never perfectly represent arcs, this field/property specifies a maximum acceptable
     * imprecision (tolerance) when arcs are approximated in an offsetting operation. Smaller values will increase
     * smoothness up to a point though at a cost of performance and in creating more vertices to construct the arc.
     *
     * @return arc tolerance specifying maximum difference between the true and the faceted representation of arcs
     */
    public double getArcTolerance() {
        return arcTolerance;
    }

    /**
     * Sets arc tolerance which is the maximum difference between the true and the faceted representation of curves
     * (arcs) in units. Used as the criterion of a good approximation of rounded line joins and line caps.
     *
     * <p>
     * Since flattened paths can never perfectly represent arcs, this field/property specifies a maximum acceptable
     * imprecision (tolerance) when arcs are approximated in an offsetting operation. Smaller values will increase
     * smoothness up to a point though at a cost of performance and in creating more vertices to construct the arc.
     *
     * @param arcTolerance maximum difference between the true and the faceted representation of arcs
     *
     * @return this {@link PathOffsetApproximationProperties} instance
     */
    public PathOffsetApproximationProperties setArcTolerance(double arcTolerance) {
        this.arcTolerance = arcTolerance;
        return this;
    }
}
