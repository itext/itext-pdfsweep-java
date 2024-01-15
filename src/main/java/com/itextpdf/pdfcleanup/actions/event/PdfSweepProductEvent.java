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
package com.itextpdf.pdfcleanup.actions.event;

import com.itextpdf.commons.actions.AbstractProductProcessITextEvent;
import com.itextpdf.commons.actions.confirmations.EventConfirmationType;
import com.itextpdf.commons.actions.contexts.IMetaInfo;
import com.itextpdf.commons.actions.sequence.SequenceId;
import com.itextpdf.pdfcleanup.actions.data.PdfSweepProductData;

/**
 * Class represents events registered in iText cleanup module.
 */
public class PdfSweepProductEvent extends AbstractProductProcessITextEvent {
    /**
     * Cleanup event type description.
     */
    public static final String CLEANUP_PDF = "cleanup-pdf";

    private final String eventType;

    /**
     * Creates an event associated with a general identifier and additional meta data.
     *
     * @param sequenceId is an identifier associated with the event
     * @param metaInfo is an additional meta info
     * @param eventType is a string description of the event
     */
    private PdfSweepProductEvent(SequenceId sequenceId, IMetaInfo metaInfo, String eventType) {
        super(sequenceId, PdfSweepProductData.getInstance(), metaInfo, EventConfirmationType.ON_CLOSE);
        this.eventType = eventType;
    }

    /**
     * Creates a cleanup-pdf event which associated with a general identifier and additional meta data.
     *
     * @param sequenceId is an identifier associated with the event
     * @param metaInfo is an additional meta info
     *
     * @return the cleanup-pdf event
     */
    public static PdfSweepProductEvent createCleanupPdfEvent(SequenceId sequenceId, IMetaInfo metaInfo) {
        return new PdfSweepProductEvent(sequenceId, metaInfo, CLEANUP_PDF);
    }

    @Override
    public String getEventType() {
        return eventType;
    }
}
