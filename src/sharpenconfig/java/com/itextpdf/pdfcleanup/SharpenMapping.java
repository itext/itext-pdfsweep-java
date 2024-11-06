package com.itextpdf.pdfcleanup;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import sharpen.config.MappingConfiguration;
import sharpen.config.MappingConfigurator;
import sharpen.config.ModuleOption;
import sharpen.config.ModulesConfigurator;
import sharpen.config.OptionsConfigurator;

public class SharpenMapping implements MappingConfiguration {


    @Override
    public int getMappingPriority() {
        // leaf node in dependency tree
        return 0;
    }

    @Override
    public String getModuleName() {
        return "cleanup";
    }

    @Override
    public void applyMappingConfiguration(MappingConfigurator configurator) {
        configurator.addFullName("iText.PdfCleanup.PdfCleanUpTool");
        configurator.addFullName("iText.PdfCleanup.PdfCleanUpLocation");

        configurator.addCustomUsingDeclaration("com.itextpdf.pdfcleanup.PdfCleanUpFilter", Arrays.asList("Paths = System.Collections.Generic.List<System.Collections.Generic.List<iText.Kernel.Pdf.Canvas.Parser.ClipperLib.IntPoint>>"));
        // TODO DEVSIX-1617: remove on ticket completion
        configurator.addAdditionalAttributes("com.itextpdf.pdfcleanup.PdfCleanUpToolWithInlineImagesTest.cleanUpTest28()", Arrays.asList("Ignore(\"DEVSIX-1617: System.Drawing.Image creates a Bitmap image object with fixed pixel format. If you try to get Graphics from such an image you'll get an exception.\" )"));
        // TODO DEVSIX-1617: remove on ticket completion
        configurator.addAdditionalAttributes("com.itextpdf.pdfcleanup.PdfCleanUpToolWithInlineImagesTest.cleanUpTest29()", Arrays.asList("Ignore(\"DEVSIX-1617: System.Drawing.Image creates a Bitmap image object with fixed pixel format. If you try to get Graphics from such an image you'll get an exception.\" )"));
        // TODO DEVSIX-1617: remove on ticket completion
        configurator.addCustomUsingDeclaration("com.itextpdf.pdfcleanup.PdfCleanUpToolWithInlineImagesTest", Arrays.asList("NUnit.Framework"));


        configurator.mapType("com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpLocation",
                "iTextSharp.xtra.iTextSharp.text.pdf.pdfcleanup.PdfCleanUpLocation");
        configurator.mapType("com.itextpdf.text.pdf.pdfcleanup.PdfCleanUpProcessor",
                "iTextSharp.xtra.iTextSharp.text.pdf.pdfcleanup.PdfCleanUpProcessor");

    }

    @Override
    public void applySharpenOptions(OptionsConfigurator configurator) {

    }

    @Override
    public void applyConfigModuleSettings(ModulesConfigurator configurator) {

    }

    @Override
    public Collection<ModuleOption> getAvailableModuleSettings() {
        return Collections.EMPTY_SET;
    }

    @Override
    public Collection<String> getDependencies() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<String> getIgnoredSourceFiles() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Collection<String> getIgnoredResources() {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List<SimpleImmutableEntry<String, String>> getOverwrittenResources() {
        return Collections.EMPTY_LIST;
    }
}