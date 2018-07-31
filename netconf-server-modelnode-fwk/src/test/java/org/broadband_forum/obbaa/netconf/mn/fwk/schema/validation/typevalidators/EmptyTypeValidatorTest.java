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

public class EmptyTypeValidatorTest extends AbstractTypeValidatorTest {

    @Test
    public void testEmpty() throws NetconfMessageBuilderException {
        testPass("emptyvalidator/valid-empty.xml");

        testFail("emptyvalidator/invalid-empty-1.xml",
                "Value \"non-empty\" does not meet the empty type constraints. Element \"empty-type\" should not have" +
                        " any value",
                "/validation:validation/validation:type-validation[validation:id='1']/validation:empty-type", null);
    }
}
