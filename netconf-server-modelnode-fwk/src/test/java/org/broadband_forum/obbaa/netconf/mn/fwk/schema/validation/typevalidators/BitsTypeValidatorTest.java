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

public class BitsTypeValidatorTest extends AbstractTypeValidatorTest {

    @Test
    public void testBits() throws NetconfMessageBuilderException {
        testPass("bitsvalidator/valid-bits.xml");

        testFail("bitsvalidator/invalid-bits-1.xml",
                "Value \"  bit-1 bit-2\" does not meet the bits type constraints. It shouldn't have any spaces in " +
                        "begin/end value and more than one space between bit values",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:bits-type", null);

        testFail("bitsvalidator/invalid-bits-2.xml",
                "Value \"bit-1 bit-2  \" does not meet the bits type constraints. It shouldn't have any spaces in " +
                        "begin/end value and more than one space between bit values",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:bits-type", null);

        testFail("bitsvalidator/invalid-bits-3.xml",
                "Value \"bit-1   bit-2\" does not meet the bits type constraints. It shouldn't have any spaces in " +
                        "begin/end value and more than one space between bit values",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:bits-type", null);

        testFail("bitsvalidator/invalid-bits-4.xml",
                "Value \"bit-unknown-1, bit-unknown-2, bit-unknown-3\" does not meet the bits type constraints. Valid" +
                        " bits are: \"bit-1, bit-2, bit-3\"",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:bits-type", null);

        testFail("bitsvalidator/invalid-bits-5.xml",
                "Value \"bit-1 bit-2 bit-2 bit-3 bit-3\" does not meet the bits type constraints. A bit value can appear atmost once",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:bits-type", null);
    }
}