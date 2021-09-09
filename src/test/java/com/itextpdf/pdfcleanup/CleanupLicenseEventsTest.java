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
package com.itextpdf.pdfcleanup;

import com.itextpdf.commons.actions.EventManager;
import com.itextpdf.commons.actions.IBaseEvent;
import com.itextpdf.commons.actions.IBaseEventHandler;
import com.itextpdf.commons.actions.ProductNameConstant;
import com.itextpdf.commons.actions.confirmations.ConfirmEvent;
import com.itextpdf.commons.actions.confirmations.ConfirmedEventWrapper;
import com.itextpdf.commons.actions.confirmations.EventConfirmationType;
import com.itextpdf.commons.actions.processors.DefaultITextProductEventProcessor;
import com.itextpdf.commons.actions.producer.ProducerBuilder;
import com.itextpdf.commons.actions.sequence.SequenceId;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.counter.event.ITextCoreProductEvent;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.pdfcleanup.actions.event.PdfSweepProductEvent;
import com.itextpdf.pdfcleanup.autosweep.CompositeCleanupStrategy;
import com.itextpdf.pdfcleanup.autosweep.PdfAutoSweepTools;
import com.itextpdf.pdfcleanup.autosweep.RegexBasedCleanupStrategy;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class CleanupLicenseEventsTest extends ExtendedITextTest {

    private static final String INPUT_PATH = "./src/test/resources/com/itextpdf/pdfcleanup/PdfCleanUpToolTest/";
    private static final String OUTPUT_PATH = "./target/test/com/itextpdf/pdfcleanup/PdfCleanUpToolTest/";
    private StoreEventsHandler handler;

    @BeforeClass
    public static void beforeClass() {
        createOrClearDestinationFolder(OUTPUT_PATH);
    }

    @Before
    public void setStoredEventHandler() {
        handler = new StoreEventsHandler();
        EventManager.getInstance().register(handler);
    }

    @After
    public void resetHandler() {
        EventManager.getInstance().unregister(handler);
        handler = null;
    }

    @Test
    public void cleanUpRedactAnnotationsSendsCoreAndCleanUpEventTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfDocument document = new PdfDocument(new PdfReader(INPUT_PATH + "absentICentry.pdf"), new PdfWriter(baos));

        String oldProducer = document.getDocumentInfo().getProducer();

        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
        PdfCleanUpLocation lineLoc = new PdfCleanUpLocation(1, new Rectangle(100, 560, 200, 30), ColorConstants.GREEN);
        cleanUpLocations.add(lineLoc);

        PdfCleaner.cleanUpRedactAnnotations(document, new CleanUpProperties());

        document.close();

        List<ConfirmEvent> events = handler.getEvents();

        Assert.assertEquals(2, events.size());
        Assert.assertEquals(ITextCoreProductEvent.PROCESS_PDF, events.get(0).getEvent().getEventType());
        Assert.assertEquals(PdfSweepProductEvent.CLEANUP_PDF, events.get(1).getEvent().getEventType());

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(new ByteArrayInputStream(baos.toByteArray())))) {
            String expectedProdLine = createExpectedProducerLine(
                    new ConfirmedEventWrapper[] {getCoreEvent(), getCleanUpEvent()}, oldProducer);
            Assert.assertEquals(expectedProdLine, pdfDocument.getDocumentInfo().getProducer());
        }
    }

    @Test
    public void cleanUpRedactAnnotationsWithAdditionalLocationSendsCoreAndCleanUpEventTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfDocument document = new PdfDocument(new PdfReader(INPUT_PATH + "absentICentry.pdf"), new PdfWriter(baos));

        String oldProducer = document.getDocumentInfo().getProducer();

        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
        PdfCleanUpLocation lineLoc = new PdfCleanUpLocation(1, new Rectangle(100, 560, 200, 30), ColorConstants.GREEN);
        cleanUpLocations.add(lineLoc);

        PdfCleaner.cleanUpRedactAnnotations(document, cleanUpLocations);

        document.close();

        List<ConfirmEvent> events = handler.getEvents();

        Assert.assertEquals(2, events.size());
        Assert.assertEquals(ITextCoreProductEvent.PROCESS_PDF, events.get(0).getEvent().getEventType());
        Assert.assertEquals(PdfSweepProductEvent.CLEANUP_PDF, events.get(1).getEvent().getEventType());

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(new ByteArrayInputStream(baos.toByteArray())))) {
            String expectedProdLine = createExpectedProducerLine(
                    new ConfirmedEventWrapper[] {getCoreEvent(), getCleanUpEvent()}, oldProducer);
            Assert.assertEquals(expectedProdLine, pdfDocument.getDocumentInfo().getProducer());
        }
    }

    @Test
    public void cleanUpRedactAnnotationsWithNullLocationSendsCoreAndCleanUpEventTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfDocument document = new PdfDocument(new PdfReader(INPUT_PATH + "absentICentry.pdf"), new PdfWriter(baos));

        String oldProducer = document.getDocumentInfo().getProducer();

        List<PdfCleanUpLocation> cleanUpLocations = null;
        PdfCleaner.cleanUpRedactAnnotations(document, cleanUpLocations, new CleanUpProperties());

        document.close();

        List<ConfirmEvent> events = handler.getEvents();

        Assert.assertEquals(2, events.size());
        Assert.assertEquals(ITextCoreProductEvent.PROCESS_PDF, events.get(0).getEvent().getEventType());
        Assert.assertEquals(PdfSweepProductEvent.CLEANUP_PDF, events.get(1).getEvent().getEventType());

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(new ByteArrayInputStream(baos.toByteArray())))) {
            String expectedProdLine = createExpectedProducerLine(
                    new ConfirmedEventWrapper[] {getCoreEvent(), getCleanUpEvent()}, oldProducer);
            Assert.assertEquals(expectedProdLine, pdfDocument.getDocumentInfo().getProducer());
        }
    }

    @Test
    public void cleanUpRedactAnnotationsWithSteamPramsSendsCleanUpEventTest() throws Exception {
        InputStream inputStream = new FileInputStream(INPUT_PATH + "absentICentry.pdf");
        OutputStream outputStream = new FileOutputStream(OUTPUT_PATH + "cleanUpRedactAnnotationsWithSteamPramsSendsCleanUpEventTest.pdf");

        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
        PdfCleanUpLocation lineLoc = new PdfCleanUpLocation(1, new Rectangle(100, 560, 200, 30), ColorConstants.GREEN);
        cleanUpLocations.add(lineLoc);

        PdfCleaner.cleanUpRedactAnnotations(inputStream, outputStream, cleanUpLocations);

        List<ConfirmEvent> events = handler.getEvents();

        Assert.assertEquals(1, events.size());
        Assert.assertEquals(PdfSweepProductEvent.CLEANUP_PDF, events.get(0).getEvent().getEventType());
    }

    @Test
    public void cleanUpRedactAnnotationsWithStreamArgumentsSendsCleanUpEventTest() throws Exception {
        String in = INPUT_PATH + "absentICentry.pdf";
        String out = OUTPUT_PATH + "cleanUpRedactAnnotationsWithStreamArgumentTest.pdf";
        InputStream file = new FileInputStream(in);
        OutputStream output = new FileOutputStream(out);
        PdfCleaner.cleanUpRedactAnnotations(file, output);

        List<ConfirmEvent> events = handler.getEvents();

        Assert.assertEquals(1, events.size());
        Assert.assertEquals(PdfSweepProductEvent.CLEANUP_PDF, events.get(0).getEvent().getEventType());

        try (
                PdfDocument pdfDocument = new PdfDocument(new PdfReader(out));
                PdfDocument inputDoc =  new PdfDocument(new PdfReader(in))
        ) {
            String expectedProdLine = createExpectedProducerLine(
                    new ConfirmedEventWrapper[] {getCleanUpEvent()}, inputDoc.getDocumentInfo().getProducer());
            Assert.assertEquals(expectedProdLine, pdfDocument.getDocumentInfo().getProducer());
        }
    }

    @Test
    public void cleanUpWithStreamArgumentsSendsCleanUpEventTest() throws Exception {
        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
        PdfCleanUpLocation lineLoc = new PdfCleanUpLocation(1, new Rectangle(100, 560, 200, 30));
        cleanUpLocations.add(lineLoc);

        String in = INPUT_PATH + "page229.pdf";
        String out = OUTPUT_PATH + "cleanUpWithStreamArgumentTest.pdf";
        InputStream file = new FileInputStream(in);
        OutputStream output = new FileOutputStream(out);
        PdfCleaner.cleanUp(file, output, cleanUpLocations, new CleanUpProperties());

        List<ConfirmEvent> events = handler.getEvents();

        Assert.assertEquals(1, events.size());
        Assert.assertEquals(PdfSweepProductEvent.CLEANUP_PDF, events.get(0).getEvent().getEventType());

        try (
                PdfDocument pdfDocument = new PdfDocument(new PdfReader(out));
                PdfDocument inputDoc =  new PdfDocument(new PdfReader(in))
        ) {
            String expectedProdLine = createExpectedProducerLine(
                    new ConfirmedEventWrapper[] {getCleanUpEvent()}, inputDoc.getDocumentInfo().getProducer());
            Assert.assertEquals(expectedProdLine, pdfDocument.getDocumentInfo().getProducer());
        }
    }

    @Test
    public void openDocumentAndCleanUpSendsCoreAndCleanUpEventsTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfDocument document = new PdfDocument(new PdfReader(INPUT_PATH + "page229.pdf"), new PdfWriter(baos));

        String oldProducer = document.getDocumentInfo().getProducer();

        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<PdfCleanUpLocation>();
        PdfCleanUpLocation lineLoc = new PdfCleanUpLocation(1, new Rectangle(100, 560, 200, 30), ColorConstants.GREEN);
        cleanUpLocations.add(lineLoc);

        PdfCleaner.cleanUp(document, cleanUpLocations, new CleanUpProperties());

        document.close();

        List<ConfirmEvent> events = handler.getEvents();

        Assert.assertEquals(2, events.size());
        Assert.assertEquals(ITextCoreProductEvent.PROCESS_PDF, events.get(0).getEvent().getEventType());
        Assert.assertEquals(PdfSweepProductEvent.CLEANUP_PDF, events.get(1).getEvent().getEventType());

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(new ByteArrayInputStream(baos.toByteArray())))) {
            String expectedProdLine = createExpectedProducerLine(
                    new ConfirmedEventWrapper[] {getCoreEvent(), getCleanUpEvent()}, oldProducer);
            Assert.assertEquals(expectedProdLine, pdfDocument.getDocumentInfo().getProducer());
        }
    }

    @Test
    public void autoSweepHighlightSendsCoreEventsTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfDocument document = new PdfDocument(new PdfReader(INPUT_PATH + "fontCleanup.pdf"), new PdfWriter(baos));

        String oldProducer = document.getDocumentInfo().getProducer();

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("leonard"));
        PdfAutoSweepTools autoSweep = new PdfAutoSweepTools(strategy);
        autoSweep.highlight(document);

        document.close();

        List<ConfirmEvent> events = handler.getEvents();

        Assert.assertEquals(1, events.size());
        Assert.assertEquals(ITextCoreProductEvent.PROCESS_PDF, events.get(0).getEvent().getEventType());

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(new ByteArrayInputStream(baos.toByteArray())))) {
            String expectedProdLine = createExpectedProducerLine(
                    new ConfirmedEventWrapper[] {getCoreEvent()}, oldProducer);
            Assert.assertEquals(expectedProdLine, pdfDocument.getDocumentInfo().getProducer());
        }
    }

    @Test
    public void autoSweepCleanUpSendsCoreAndCleanUpEventsTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfDocument document = new PdfDocument(new PdfReader(INPUT_PATH + "fontCleanup.pdf"), new PdfWriter(baos));

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("leonard"));
        PdfCleaner.autoSweepCleanUp(document, strategy);

        String oldProducer = document.getDocumentInfo().getProducer();

        document.close();

        List<ConfirmEvent> events = handler.getEvents();

        Assert.assertEquals(2, events.size());
        Assert.assertEquals(ITextCoreProductEvent.PROCESS_PDF, events.get(0).getEvent().getEventType());
        Assert.assertEquals(PdfSweepProductEvent.CLEANUP_PDF, events.get(1).getEvent().getEventType());

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(new ByteArrayInputStream(baos.toByteArray())))) {
            String expectedProdLine = createExpectedProducerLine(
                    new ConfirmedEventWrapper[] {getCoreEvent(), getCleanUpEvent()}, oldProducer);
            Assert.assertEquals(expectedProdLine, pdfDocument.getDocumentInfo().getProducer());
        }
    }

    @Test
    public void autoCleanWithStreamParamsSendsCleanUpEventTest() throws Exception {
        String input = INPUT_PATH + "fontCleanup.pdf";
        String output = OUTPUT_PATH + "autoCleanWithStreamParamsSendsCleanUpEventTest.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("leonard"));
        PdfCleaner.autoSweepCleanUp(new FileInputStream(input), new FileOutputStream(output), strategy);

        List<ConfirmEvent> events = handler.getEvents();
        Assert.assertEquals(1, events.size());
        Assert.assertEquals(PdfSweepProductEvent.CLEANUP_PDF, events.get(0).getEvent().getEventType());

        try (
                PdfDocument outDoc = new PdfDocument(new PdfReader(output));
                PdfDocument inputDoc =  new PdfDocument(new PdfReader(input))
        ) {
            String expectedProdLine = createExpectedProducerLine(
                    new ConfirmedEventWrapper[] {getCleanUpEvent()}, inputDoc.getDocumentInfo().getProducer());
            Assert.assertEquals(expectedProdLine, outDoc.getDocumentInfo().getProducer());
        }
    }

    @Test
    public void autoCleanWithLocationAndStreamParamsSendsCleanUpEventTest() throws Exception {
        String input = INPUT_PATH + "fontCleanup.pdf";
        String output = OUTPUT_PATH + "autoCleanWithLocationAndStreamParamsSendsCleanUpEventTest.pdf";

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("leonard"));
        List<PdfCleanUpLocation> additionalLocation = Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(150, 650, 0, 0)));
        PdfCleaner
                .autoSweepCleanUp(new FileInputStream(input), new FileOutputStream(output), strategy, additionalLocation);

        List<ConfirmEvent> events = handler.getEvents();
        Assert.assertEquals(1, events.size());
        Assert.assertEquals(PdfSweepProductEvent.CLEANUP_PDF, events.get(0).getEvent().getEventType());

        try (
                PdfDocument outDoc = new PdfDocument(new PdfReader(output));
                PdfDocument inputDoc =  new PdfDocument(new PdfReader(input))
        ) {
            String expectedProdLine = createExpectedProducerLine(
                    new ConfirmedEventWrapper[] {getCleanUpEvent()}, inputDoc.getDocumentInfo().getProducer());
            Assert.assertEquals(expectedProdLine, outDoc.getDocumentInfo().getProducer());
        }
    }

    @Test
    public void autoSweepTentativeCleanUpSendsCoreEventTest() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfDocument document = new PdfDocument(new PdfReader(INPUT_PATH + "fontCleanup.pdf"), new PdfWriter(baos));

        CompositeCleanupStrategy strategy = new CompositeCleanupStrategy();
        strategy.add(new RegexBasedCleanupStrategy("leonard"));
        PdfAutoSweepTools autoSweep = new PdfAutoSweepTools(strategy);
        autoSweep.tentativeCleanUp(document);

        String oldProducer = document.getDocumentInfo().getProducer();

        document.close();

        List<ConfirmEvent> events = handler.getEvents();

        Assert.assertEquals(1, events.size());
        Assert.assertEquals(ITextCoreProductEvent.PROCESS_PDF, events.get(0).getEvent().getEventType());

        try (PdfDocument pdfDocument = new PdfDocument(new PdfReader(new ByteArrayInputStream(baos.toByteArray())))) {
            String expectedProdLine = createExpectedProducerLine(
                    new ConfirmedEventWrapper[] {getCoreEvent()}, oldProducer);
            Assert.assertEquals(expectedProdLine, pdfDocument.getDocumentInfo().getProducer());
        }
    }

    private static String createExpectedProducerLine(ConfirmedEventWrapper[] expectedEvents, String oldProducer) {
        List<ConfirmedEventWrapper> listEvents = Arrays.asList(expectedEvents);
        return ProducerBuilder.modifyProducer(listEvents, oldProducer);
    }

    private static ConfirmedEventWrapper getCoreEvent() {
        DefaultITextProductEventProcessor processor = new DefaultITextProductEventProcessor(
                ProductNameConstant.ITEXT_CORE);
        return new ConfirmedEventWrapper(
                ITextCoreProductEvent.createProcessPdfEvent(new SequenceId(), null, EventConfirmationType.ON_CLOSE),
                processor.getUsageType(),
                processor.getProducer());
    }

    private static ConfirmedEventWrapper getCleanUpEvent() {
        DefaultITextProductEventProcessor processor = new DefaultITextProductEventProcessor(
                ProductNameConstant.PDF_SWEEP);
        return new ConfirmedEventWrapper(
                PdfSweepProductEvent.createCleanupPdfEvent(new SequenceId(), null),
                processor.getUsageType(),
                processor.getProducer());
    }

    private static class StoreEventsHandler implements IBaseEventHandler {
        private final List<ConfirmEvent> events = new ArrayList<>();

        public List<ConfirmEvent> getEvents() {
            return events;
        }

        @Override
        public void onEvent(IBaseEvent event) {
            if (event instanceof ConfirmEvent) {
                events.add((ConfirmEvent) event);
            }
        }
    }
}
