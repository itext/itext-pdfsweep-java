package com.itextpdf.pdfcleanup;

import com.itextpdf.pdfcleanup.exceptions.CleanupExceptionMessageConstant;
import com.itextpdf.test.ExtendedITextTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.*;

@Tag("UnitTest")
public class CleanUpPropertiesUnitTest extends ExtendedITextTest {


    @Test
    public void nePropsAspectRatioReturnsNull(){
        CleanUpProperties properties = new CleanUpProperties();
        assertNull(properties.getOverlapRatio());
    }

    @Test
    public void setAspectRatioWithValue0IsOk(){
        CleanUpProperties properties = new CleanUpProperties();
        Exception e = assertThrows(IllegalArgumentException.class, () -> properties.setOverlapRatio(0d));
        assertEquals(CleanupExceptionMessageConstant.OVERLAP_RATIO_SHOULD_BE_IN_RANGE, e.getMessage());
    }

    @Test
    public void setAspectRatioWithValue1IsOk(){
        CleanUpProperties properties = new CleanUpProperties();
        properties.setOverlapRatio(1.0);
        assertEquals(1.0, properties.getOverlapRatio());
    }

    @Test
    public void setAspectRatioWithValueGreaterThan1ThrowsException(){
        CleanUpProperties properties = new CleanUpProperties();
        Exception e = assertThrows(IllegalArgumentException.class, () -> properties.setOverlapRatio(1.1));
        assertEquals(CleanupExceptionMessageConstant.OVERLAP_RATIO_SHOULD_BE_IN_RANGE, e.getMessage());
    }

    @Test
    public void setAspectRatioWithValueLessThan0ThrowsException(){
        CleanUpProperties properties = new CleanUpProperties();
        Exception e = assertThrows(IllegalArgumentException.class, () -> properties.setOverlapRatio(-0.1));
        assertEquals(CleanupExceptionMessageConstant.OVERLAP_RATIO_SHOULD_BE_IN_RANGE, e.getMessage());
    }

    @Test
    public void setAspectRatioWithValue0_5IsOk(){
        CleanUpProperties properties = new CleanUpProperties();
        properties.setOverlapRatio(0.5);
        assertEquals(0.5, properties.getOverlapRatio());
    }

    @Test
    public void settingAspectRatioToNullIsOk(){
        CleanUpProperties properties = new CleanUpProperties();
        properties.setOverlapRatio(0.5);
        properties.setOverlapRatio(null);
        assertNull(properties.getOverlapRatio());
    }
}