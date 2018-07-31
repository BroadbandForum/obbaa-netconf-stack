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

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Test;


public class BinaryTypeValidatorTest extends AbstractTypeValidatorTest {
    @Test
    public void testBinary() throws NetconfMessageBuilderException {
        testPass("binaryvalidator/valid-binary.xml");

        testFail("binaryvalidator/invalid-binary-1.xml",
                "The argument is out of bounds <1, 1> | The argument is out of bounds <3, 4>",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:binary-type",
                "length-out-of-specified-bounds | length-out-of-specified-bounds");

        testFail("binaryvalidator/invalid-binary-2.xml",
                "The argument is out of bounds <1, 1> | The argument is out of bounds <3, 4>",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:binary-type",
                "length-out-of-specified-bounds | length-out-of-specified-bounds");

        testFail("binaryvalidator/invalid-binary-3.xml",
                "The argument is out of bounds <1, 1> | The argument is out of bounds <3, 4>",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:binary-type",
                "length-out-of-specified-bounds | length-out-of-specified-bounds");

    }

    @Test
    public void testCustomBinary() throws NetconfMessageBuilderException {
        String validCustomReq1 = "<custom-binary-type>11111111</custom-binary-type>";
        testCustomPass(formRequestString(validCustomReq1));

        String validCustomReq2 = "<custom-binary-type>111111111111111111111111</custom-binary-type>";
        testCustomPass(formRequestString(validCustomReq2));

        String invalidCustomReq1 = "<custom-binary-type>11</custom-binary-type>";
        testCustomFail(formRequestString(invalidCustomReq1),
                "length constraint error-app-message | length constraint error-app-message",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:custom-binary-type",
                "length constraint error-app-tag | length constraint error-app-tag");

        String invalidCustomReq2 = "<custom-binary-type>1111111111111111</custom-binary-type>";
        testCustomFail(formRequestString(invalidCustomReq2),
                "length constraint error-app-message | length constraint error-app-message",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:custom-binary-type", "length constraint error-app-tag | length constraint error-app-tag");
    }
}
