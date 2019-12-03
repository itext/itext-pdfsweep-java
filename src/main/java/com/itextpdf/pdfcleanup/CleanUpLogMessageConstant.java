package com.itextpdf.pdfcleanup;

public class CleanUpLogMessageConstant {

    /** The Constant CANNOT_OBTAIN_IMAGE_INFO_AFTER_FILTERING. */
    public static final String CANNOT_OBTAIN_IMAGE_INFO_AFTER_FILTERING = "Cannot obtain image info after filtering.";
    /** The Constant FAILED_TO_PROCESS_A_TRANSFORMATION_MATRIX. */
    public static final String FAILED_TO_PROCESS_A_TRANSFORMATION_MATRIX = "Failed to process a transformation matrix which is noninvertible. Some content may be placed not as expected.";
    /** The Constant IMAGE_MASK_CLEAN_UP_NOT_SUPPORTED. */
    public static final String IMAGE_MASK_CLEAN_UP_NOT_SUPPORTED = "Partial clean up of transparent images with mask encoded with one of the following filters is not supported: JBIG2Decode, DCTDecode, JPXDecode. Image will become non-transparent.";
    /** The Constant REDACTION_OF_ANNOTATION_TYPE_WATERMARK_IS_NOT_SUPPORTED. */
    public static final String REDACTION_OF_ANNOTATION_TYPE_WATERMARK_IS_NOT_SUPPORTED = "Redaction of annotation subtype /Watermark is not supported";

    private CleanUpLogMessageConstant() {
    }
}
