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
package com.itextpdf.pdfcleanup.autosweep;

import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IPdfTextLocation;
import com.itextpdf.kernel.pdf.canvas.parser.listener.RegexBasedLocationExtractionStrategy;

import java.util.regex.Pattern;

/**
 * This class represents a regular expression based cleanup strategy
 */
public class RegexBasedCleanupStrategy extends RegexBasedLocationExtractionStrategy implements ICleanupStrategy {

    private Pattern pattern;
    private Color redactionColor = ColorConstants.BLACK;

    /**
     * Creates an object of regular expression based cleanup strategy.
     *
     * @param regex regular expression on which cleanup strategy will be based
     */
    public RegexBasedCleanupStrategy(String regex) {
        super(regex);
        this.pattern = Pattern.compile(regex);
    }

    /**
     * Creates an object of regular expression based cleanup strategy.
     *
     * @param pattern {@link Pattern} pattern on which cleanup strategy will be based
     */
    public RegexBasedCleanupStrategy(Pattern pattern) {
        super(pattern);
        this.pattern = pattern;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getRedactionColor(IPdfTextLocation location) {
        return redactionColor;
    }

    /**
     * Sets the color in which redaction is to take place.
     *
     * @param color the color in which redaction is to take place
     * 
     * @return this {@link RegexBasedCleanupStrategy strategy}
     */
    public RegexBasedCleanupStrategy setRedactionColor(Color color) {
        this.redactionColor = color;
        return this;
    }

    /**
     * Returns an {@link ICleanupStrategy} object which is set to this regular pattern and redaction color.
     *
     * @return a reset {@link ICleanupStrategy cleanup strategy}
     */
    public ICleanupStrategy reset() {
        return new RegexBasedCleanupStrategy(pattern).setRedactionColor(redactionColor);
    }
}
