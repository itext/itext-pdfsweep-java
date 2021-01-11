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

import com.itextpdf.kernel.Version;

/**
 * Utility class for handling operation related to reflections.
 */
final class ReflectionUtils {

    private static final String LICENSEKEY = "com.itextpdf.licensekey.LicenseKey";
    private static final String LICENSEKEY_PRODUCT = "com.itextpdf.licensekey.LicenseKeyProduct";
    private static final String CHECK_LICENSEKEY_METHOD = "scheduledCheck";

    private ReflectionUtils() {
    }

    /**
     * Performs a scheduled license check.
     */
    static void scheduledLicenseCheck() {
        try {
            Class licenseKeyProductClass = getClass(LICENSEKEY_PRODUCT);
            Class[] params = new Class[] {
                    String.class, String.class, String.class
            };
            Object licenseKeyProductObject = licenseKeyProductClass.getConstructor(params).newInstance(
                    PdfCleanupProductInfo.PRODUCT_NAME, String.valueOf(PdfCleanupProductInfo.MAJOR_VERSION),
                    String.valueOf(PdfCleanupProductInfo.MINOR_VERSION)
            );
            getClass(LICENSEKEY).getMethod(CHECK_LICENSEKEY_METHOD, licenseKeyProductClass)
                    .invoke(null, licenseKeyProductObject);
        } catch (Exception e) {
            if (!Version.isAGPLVersion()) {
                throw new RuntimeException(e.getCause());
            }
        }
    }

    private static Class<?> getClass(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }
}
