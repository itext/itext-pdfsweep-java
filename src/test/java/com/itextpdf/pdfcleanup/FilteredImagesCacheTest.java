package com.itextpdf.pdfcleanup;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDictionary;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfObject;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.kernel.utils.CompareTool;
import com.itextpdf.test.ExtendedITextTest;
import com.itextpdf.test.annotations.type.IntegrationTest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class FilteredImagesCacheTest extends ExtendedITextTest {

    private static final String inputPath = "./src/test/resources/com/itextpdf/pdfcleanup/FilteredImagesCacheTest/";
    private static final String outputPath = "./target/test/com/itextpdf/pdfcleanup/FilteredImagesCacheTest/";

    @BeforeClass
    public static void before() {
        createOrClearDestinationFolder(outputPath);
    }

    @Test
    public void filteredImagesCacheTest01() throws IOException, InterruptedException {
        // basic test with reusing of xobjects

        String input = inputPath + "multipleImageXObjectOccurrences.pdf";
        String output = outputPath + "filteredImagesCacheTest01.pdf";
        String cmp = inputPath + "cmp_filteredImagesCacheTest01.pdf";

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<>();

        for (int i = 0; i < pdfDocument.getNumberOfPages(); ++i) {
            cleanUpLocations.add(new PdfCleanUpLocation(i + 1, new Rectangle(150, 300, 300, 150)));
        }

        cleanUp(pdfDocument, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff");
        assertNumberXObjects(output, 1);
    }

    @Test
    public void filteredImagesCacheTest02() throws IOException, InterruptedException {
        // reusing when several clean areas (different on different pages)

        String input = inputPath + "multipleImageXObjectOccurrences.pdf";
        String output = outputPath + "filteredImagesCacheTest02.pdf";
        String cmp = inputPath + "cmp_filteredImagesCacheTest02.pdf";

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<>();

        for (int i = 0; i < pdfDocument.getNumberOfPages(); i += 5) {
            cleanUpLocations.add(new PdfCleanUpLocation(i + 1, new Rectangle(350, 450, 300, 40)));
            cleanUpLocations.add(new PdfCleanUpLocation(i + 1, new Rectangle(300, 400, 50, 150)));
        }

        for (int i = 1; i < pdfDocument.getNumberOfPages(); i += 5) {
            cleanUpLocations.add(new PdfCleanUpLocation(i + 1, new Rectangle(350, 450, 300, 20)));
            cleanUpLocations.add(new PdfCleanUpLocation(i + 1, new Rectangle(350, 490, 300, 20)));
            cleanUpLocations.add(new PdfCleanUpLocation(i + 1, new Rectangle(350, 530, 300, 20)));
        }

        for (int i = 3; i < pdfDocument.getNumberOfPages(); i += 5) {
            cleanUpLocations.add(new PdfCleanUpLocation(i + 1, new Rectangle(300, 400, 50, 150)));
            cleanUpLocations.add(new PdfCleanUpLocation(i + 1, new Rectangle(350, 400, 50, 150)));
        }

        for (int i = 4; i < pdfDocument.getNumberOfPages(); i += 5) {
            cleanUpLocations.add(new PdfCleanUpLocation(i + 1, new Rectangle(350, 450, 300, 20)));
            cleanUpLocations.add(new PdfCleanUpLocation(i + 1, new Rectangle(350, 450, 300, 20)));
            cleanUpLocations.add(new PdfCleanUpLocation(i + 1, new Rectangle(350, 450, 300, 20)));
        }

        cleanUp(pdfDocument, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff");
        assertNumberXObjects(output, 5);
    }

    @Test
    public void filteredImagesCacheTest03() throws IOException, InterruptedException {
        // same areas, different src images
        String input = inputPath + "multipleDifferentImageXObjectOccurrences.pdf";
        String output = outputPath + "filteredImagesCacheTest03.pdf";
        String cmp = inputPath + "cmp_filteredImagesCacheTest03.pdf";

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<>();

        for (int i = 0; i < pdfDocument.getNumberOfPages(); ++i) {
            cleanUpLocations.add(new PdfCleanUpLocation(i + 1, new Rectangle(150, 300, 300, 150)));
        }

        cleanUp(pdfDocument, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff");
        assertNumberXObjects(output, 2);
    }

    @Test
    public void filteredImagesCacheTest04() throws IOException, InterruptedException {
        // same image with different scaling and the same resultant image area
        String input = inputPath + "multipleScaledImageXObjectOccurrences.pdf";
        String output = outputPath + "filteredImagesCacheTest04.pdf";
        String cmp = inputPath + "cmp_filteredImagesCacheTest04.pdf";

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        List<PdfCleanUpLocation> cleanUpLocations = new ArrayList<>();

        float x = 559;
        float y = 600.2f;
        float width = 100;
        float height = 100;
        Rectangle region1 = new Rectangle(x - width, y - height, width, height);
        float scaleFactor = 1.2f;
        width *= scaleFactor;
        height *= scaleFactor;
        Rectangle region2 = new Rectangle(x - width, y - height, width, height);
        for (int i = 0; i < pdfDocument.getNumberOfPages(); i += 2) {
            cleanUpLocations.add(new PdfCleanUpLocation(i + 1, region1));
            cleanUpLocations.add(new PdfCleanUpLocation(i + 2, region2));
        }

        cleanUp(pdfDocument, cleanUpLocations);
        compareByContent(cmp, output, outputPath, "diff");
        assertNumberXObjects(output, 1);
    }

    @Test
    public void filteredImagesCacheFlushingTest01() throws IOException, InterruptedException {
        String input = inputPath + "severalImageXObjectOccurrences.pdf";
        String output = outputPath + "filteredImagesCacheFlushingTest01.pdf";
        String cmp = inputPath + "cmp_filteredImagesCacheFlushingTest01.pdf";

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        PdfCleanUpTool cleanUpTool = new PdfCleanUpTool(pdfDocument);
        cleanUpTool.addCleanupLocation(new PdfCleanUpLocation(1, new Rectangle(150, 300, 300, 150)));
        cleanUpTool.cleanUp();

        PdfImageXObject img = pdfDocument.getPage(2).getResources().getImage(new PdfName("Im1"));
        img.getPdfObject().release();

        cleanUpTool.addCleanupLocation(new PdfCleanUpLocation(2, new Rectangle(150, 300, 300, 150)));
        cleanUpTool.cleanUp();

        cleanUpTool.addCleanupLocation(new PdfCleanUpLocation(3, new Rectangle(150, 300, 300, 150)));
        cleanUpTool.cleanUp();

        pdfDocument.close();

        compareByContent(cmp, output, outputPath, "diff");
        assertNumberXObjects(output, 1);
    }

    @Test
    public void filteredImagesCacheFlushingTest02() throws IOException, InterruptedException {
        String input = inputPath + "severalImageXObjectOccurrences.pdf";
        String output = outputPath + "filteredImagesCacheFlushingTest02.pdf";
        String cmp = inputPath + "cmp_filteredImagesCacheFlushingTest02.pdf";

        PdfDocument pdfDocument = new PdfDocument(new PdfReader(input), new PdfWriter(output));

        PdfCleanUpTool cleanUpTool = new PdfCleanUpTool(pdfDocument);
        cleanUpTool.addCleanupLocation(new PdfCleanUpLocation(1, new Rectangle(150, 300, 300, 150)));
        cleanUpTool.cleanUp();

        PdfImageXObject img = pdfDocument.getPage(1).getResources().getImage(new PdfName("Im1"));
        img.makeIndirect(pdfDocument).flush();

        cleanUpTool.addCleanupLocation(new PdfCleanUpLocation(2, new Rectangle(150, 300, 300, 150)));
        cleanUpTool.cleanUp();

        cleanUpTool.addCleanupLocation(new PdfCleanUpLocation(3, new Rectangle(150, 300, 300, 150)));
        cleanUpTool.cleanUp();

        pdfDocument.close();

        compareByContent(cmp, output, outputPath, "diff");
        assertNumberXObjects(output, 1);
    }

    private void cleanUp(PdfDocument pdfDocument, List<PdfCleanUpLocation> cleanUpLocations) throws IOException {
        new PdfCleanUpTool(pdfDocument, cleanUpLocations).cleanUp();
        pdfDocument.close();
    }

    private void assertNumberXObjects(String output, int n) throws IOException {
        PdfDocument doc = new PdfDocument(new PdfReader(output));
        int xObjCount = 0;
        for (int i = 0; i < doc.getNumberOfPdfObjects(); ++i) {
            PdfObject pdfObject = doc.getPdfObject(i);
            if (pdfObject != null && pdfObject.isStream()) {
                PdfDictionary dict = (PdfDictionary) pdfObject;
                if (PdfName.Image.equals(dict.getAsName(PdfName.Subtype)) && dict.containsKey(PdfName.Width) && dict.containsKey(PdfName.Height)) {
                    ++xObjCount;
                }
            }
        }
        Assert.assertEquals(n, xObjCount);
    }

    private void compareByContent(String cmp, String output, String targetDir, String diffPrefix) throws IOException, InterruptedException {
        CompareTool cmpTool = new CompareTool();
        String errorMessage = cmpTool.compareByContent(output, cmp, targetDir, diffPrefix + "_");

        if (errorMessage != null) {
            Assert.fail(errorMessage);
        }
    }
}
