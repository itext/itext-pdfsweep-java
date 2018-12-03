package com.itextpdf.pdfcleanup.text;

import com.itextpdf.io.LogMessageConstant;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.pdfcleanup.PdfCleanUpLocation;
import com.itextpdf.pdfcleanup.PdfCleanUpTool;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.LogMessage;
import com.itextpdf.test.annotations.LogMessages;
import com.itextpdf.test.annotations.type.IntegrationTest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class CleanUpTextTest extends ExtendedITextTest{
    private static final String inputPath = "./src/test/resources/com/itextpdf/pdfcleanup/text/CleanUpTextTest/";
    private static final String outputPath = "./target/test/com/itextpdf/pdfcleanup/text/CleanUpTextTest/";

    @BeforeClass
    public static void before() {
        createOrClearDestinationFolder(outputPath);
    }

    @Test
    @LogMessages(messages = {
            @LogMessage(messageTemplate = LogMessageConstant.FONT_DICTIONARY_WITH_NO_FONT_DESCRIPTOR),
            @LogMessage(messageTemplate = LogMessageConstant.FONT_DICTIONARY_WITH_NO_WIDTHS)})
    public void cleanZeroWidthTextInvalidFont() throws IOException, InterruptedException {
        String input = inputPath + "cleanZeroWidthTextInvalidFont.pdf";
        String output = outputPath + "cleanZeroWidthTextInvalidFont.pdf";
        String cmp = inputPath + "cmp_cleanZeroWidthTextInvalidFont.pdf";

        cleanUp(input, output, Arrays.asList(new PdfCleanUpLocation(1, new Rectangle(50, 50, 500, 500))));
        compareByContent(cmp, output, outputPath);
    }

    private void cleanUp(String input, String output, List<PdfCleanUpLocation> cleanUpLocations) throws IOException {
        PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        PdfCleanUpTool cleaner = (cleanUpLocations == null)
                ? new PdfCleanUpTool(pdfDocument, true)
                : new PdfCleanUpTool(pdfDocument, cleanUpLocations);
        cleaner.cleanUp();

        pdfDocument.close();
    }

    private void compareByContent(String cmp, String output, String targetDir) throws IOException, InterruptedException {
        CompareTool cmpTool = new CompareTool();
        String errorMessage = cmpTool.compareByContent(output, cmp, targetDir);

        if (errorMessage != null) {
            Assert.fail(errorMessage);
        }
    }
}
