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

public class BooleanTypeValidatorTest extends AbstractTypeValidatorTest {

    @Test
    public void testPass() throws NetconfMessageBuilderException {
        /*
		 * "true"
		 */
        testPass("booleanvalidator/valid-boolean-1.xml");
		/*
		 * "false"
		 */
        testPass("booleanvalidator/valid-boolean-2.xml");
    }

    @Test
    public void testFail() throws NetconfMessageBuilderException {
		/*
		 * "True"
		 */
        testFail("booleanvalidator/invalid-boolean-1.xml", "Invalid value. It should be \"true\" or \"false\" instead" +
                        " of \"True\"",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:boolean-type", null);
		/*
		 * "False"
		 */
        testFail("booleanvalidator/invalid-boolean-2.xml", "Invalid value. It should be \"true\" or \"false\" instead" +
                        " of \"False\"",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:boolean-type", null);
		/*
		 * "on"
		 */
        testFail("booleanvalidator/invalid-boolean-3.xml", "Invalid value. It should be \"true\" or \"false\" instead" +
                        " of \"on\"",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:boolean-type", null);
    }
}
