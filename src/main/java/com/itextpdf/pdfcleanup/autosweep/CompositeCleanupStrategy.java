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
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.listener.ILocationExtractionStrategy;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IPdfTextLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This class is a composite pattern for {@link ICleanupStrategy}.
 * It allows users to have multiple ICleanupStrategy implementations and bundle them as one.
 */
public class CompositeCleanupStrategy implements ICleanupStrategy {

    private Map<Integer, Set<IPdfTextLocation>> locations = new HashMap<>();
    private List<ICleanupStrategy> strategies = new ArrayList<>();

    /**
     * Creates a {@link CompositeCleanupStrategy composite pattern} for {@link ICleanupStrategy cleanup strategies}.
     */
    public CompositeCleanupStrategy() {
    }

    /**
     * Adds a {@link ICleanupStrategy cleanup strategy} to this {@link CompositeCleanupStrategy composite pattern}.
     *
     * @param strategy a {@link ICleanupStrategy cleanup strategy} to be added to this
     *     {@link CompositeCleanupStrategy composite pattern}.
     */
    public void add(ICleanupStrategy strategy) {
        strategies.add(strategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<IPdfTextLocation> getResultantLocations() {
        locations.clear();

        // build return value
        Set<IPdfTextLocation> retval = new LinkedHashSet<>();
        for (int i = 0; i < strategies.size(); i++) {
            ILocationExtractionStrategy s = strategies.get(i);
            Collection<IPdfTextLocation> rects = s.getResultantLocations();
            retval.addAll(rects);
            locations.put(i, new HashSet<>(rects));
        }

        List<IPdfTextLocation> rectangles = new ArrayList<>(retval);
        java.util.Collections.sort(rectangles, new Comparator<IPdfTextLocation>() {
            @Override
            public int compare(IPdfTextLocation l1, IPdfTextLocation l2) {
                Rectangle r1 = l1.getRectangle();
                Rectangle r2 = l2.getRectangle();
                if (r1.getY() == r2.getY()) {
                    return r1.getX() == r2.getX() ? 0 : (r1.getX() < r2.getX() ? -1 : 1);
                } else {
                    return r1.getY() < r2.getY() ? -1 : 1;
                }
            }
        });

        return rectangles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getRedactionColor(IPdfTextLocation location) {
        for (int i = 0; i < strategies.size(); i++) {
            if (locations.get(i).contains(location)) {
                return strategies.get(i).getRedactionColor(location);
            }
        }
        return ColorConstants.BLACK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void eventOccurred(IEventData data, EventType type) {
        for (ILocationExtractionStrategy s : strategies) {
            s.eventOccurred(data, type);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<EventType> getSupportedEvents() {
        Set<EventType> evts = new HashSet<>();
        for (ILocationExtractionStrategy s : strategies) {
            Set<EventType> se = s.getSupportedEvents();
            if (se != null)
                evts.addAll(se);
        }
        return evts.isEmpty() ? null : evts;
    }

    /**
     * Returns a {@link ICleanupStrategy cleanup strategy} which represents
     * a reset {@link CompositeCleanupStrategy composite cleanup strategy}.
     *
     * <p>
     * Note that all the inner {@link ICleanupStrategy strategies} will be reset as well.
     *
     * @return a reset {@link CompositeCleanupStrategy composite strategy}
     */
    public ICleanupStrategy reset() {
        CompositeCleanupStrategy resetCompositeStrategy = new CompositeCleanupStrategy();
        for(ICleanupStrategy s : strategies) {
            resetCompositeStrategy.add(s.reset());
        }
        return resetCompositeStrategy;
    }
}
