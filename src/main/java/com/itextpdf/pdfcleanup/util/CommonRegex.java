/*
    This file is part of the iText (R) project.
    Copyright (c) 1998-2017 iText Group NV
    Authors: iText Software.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.pdfcleanup.util;

import java.util.regex.Pattern;

/**
 * Created by Joris Schellekens on 3/13/2017.
 */
public enum CommonRegex {

    CREDIT_CARD(""),

    CURRENCY(""),

    DATE_ALPHANUMERIC(""),
    DATE_NUMERIC_PERIOD_SEPARATED(""),
    DATE_NUMERIC_SPACE_SEPARATED(""),
    DATE_NUMERIC_SLASH_SEPARATED(""),

    EMAIL(""),

    ISBN_10(""),
    ISBN_13(""),

    POSTAL_CODE_AFGHANISTAN("(([123][0123456789])|(4[0123]))((0[123456789])|([123456789][0123456789]))"),   // https://en.wikipedia.org/wiki/Postal_codes_in_Afghanistan
    POSTAL_CODE_ALBANIA(""),                                                                                // https://en.wikipedia.org/wiki/Postal_codes_in_Albania
    POSTAL_CODE_ALGERIA(""),                                                                                // https://en.wikipedia.org/wiki/Postal_codes_in_Algeria
    POSTAL_CODE_ANDORRA("AD[1234567]0[01]"),                                                                // https://en.wikipedia.org/wiki/Postal_codes_in_Andorra
    POSTAL_CODE_ANGOLA(""),
    POSTAL_CODE_ANGUILLA(""),
    POSTAL_CODE_ANTIGUA_AND_BARBUDA(""),
    POSTAL_CODE_ARGENTINA(""),
    POSTAL_CODE_ARMENIA(""),
    POSTAL_CODE_AUSTRALIA(""),
    POSTAL_CODE_AUSTRIA(""),

    SOCIAL_SECURITY_NUMBER(""),

    TELEPHONE_NUMBER_AUSTRALIA(""),

    URL("");

    private final Pattern pattern;

    private CommonRegex(final String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public Pattern getPattern() { return this.pattern; }

}
