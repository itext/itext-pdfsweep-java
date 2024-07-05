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