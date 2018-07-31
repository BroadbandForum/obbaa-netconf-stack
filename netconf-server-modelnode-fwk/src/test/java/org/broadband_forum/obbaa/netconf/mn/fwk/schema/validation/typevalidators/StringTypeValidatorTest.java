/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.validation.typevalidators;

import org.junit.Test;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

public class StringTypeValidatorTest extends AbstractTypeValidatorTest {

    @Test
    public void testString() throws NetconfMessageBuilderException {
        testPass("stringvalidator/valid-string.xml");

        testFail("stringvalidator/invalid-string-1.xml",
                "The argument is out of bounds <1, 3>",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:string-type",
                "length-out-of-specified-bounds");

        testFail("stringvalidator/invalid-string-2.xml",
                "The argument is out of bounds <1, 3>",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:string-type",
                "length-out-of-specified-bounds");

        testFail("stringvalidator/invalid-string-3.xml",
                "Supplied value does not match the regular expression ^[a-z]$. or Supplied value does not match the " +
                        "regular expression ^[0-9]{2}$. or Supplied value does not match the regular expression ^abc$.",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:string-type",
                "invalid-regular-expression or invalid-regular-expression or invalid-regular-expression");

        testFail("stringvalidator/invalid-string-4.xml",
                "Supplied value does not match the regular expression ^[a-z]$. or Supplied value does not match the " +
                        "regular expression ^[0-9]{2}$. or Supplied value does not match the regular expression ^abc$.",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:string-type",
                "invalid-regular-expression or invalid-regular-expression or invalid-regular-expression");

        testFail("stringvalidator/invalid-string-5.xml",
                "Supplied value does not match the regular expression ^[a-z]$. or Supplied value does not match the " +
                        "regular expression ^[0-9]{2}$. or Supplied value does not match the regular expression ^abc$.",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:string-type",
                "invalid-regular-expression or invalid-regular-expression or invalid-regular-expression");

    }

    @Test
    public void testCustomString() throws NetconfMessageBuilderException {
        String validCustomReq = "<custom-string-type>abc</custom-string-type>";
        testCustomPass(formRequestString(validCustomReq));

        String invalidCustomReq1 = "<custom-string-type>12</custom-string-type>";
        testCustomFail(formRequestString(invalidCustomReq1),
                "pattern constraint error-app-message",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:custom-string-type",
                "pattern constraint error-app-tag");

        String invalidCustomReq2 = "<custom-string-type>a</custom-string-type>";
        testCustomFail(formRequestString(invalidCustomReq2),
                "length constraint error-app-message",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:custom-string-type",
                "length constraint error-app-tag");
    }

    @Test
    public void testRegExpPassWithDollarEscape() throws Exception {
        testPass("stringvalidator/valid-string-2.xml");
    }

    @Test
    public void testRegExpFailWithDollarEscape() throws Exception {
        testFail("stringvalidator/invalid-string-6.xml", "Supplied value does not match the regular expression ^\\$\\d\\$[a-zA-Z0-9^]+\\$$.", "/validation:validation/validation:type-validation[validation:id='1']/validation:string-type-with-dollar", "invalid-regular-expression");
    }

}
