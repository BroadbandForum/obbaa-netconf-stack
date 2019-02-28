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

package org.broadband_forum.obbaa.netconf.api.messages;

/**
 * Created by keshava on 11/23/15.
 */
public class NetconfRpcErrorMessages {
    public static final String AN_UNEXPECTED_ELEMENT_S_IS_PRESENT = "An unexpected element %s is present";

    public static final String AN_UNEXPECTED_NAMESPACE_S_IS_PRESENT = "An unexpected namespace %s is present";

    public static final String MANDATORY_LEAF_MISSING = "Mandatory leaf %s is missing";

    public static final String MANDATORY_CHOICE_MISSING = "Mandatory choice %s is missing";

    public static final String MANDATORY_ANYXML_MISSING = "Mandatory anyxml %s is missing";

    public static final String EXPECTED_ELEMENTS_IS_MISSING = "Expected element(s) %s is missing";

    public static final String EXPECTED_KEYS_IS_MISPLACED = "Expected list key(s) %s is not placed in the proper location in the message";

    public static final String EXPECTED_KEYS_IS_MISSING = "Expected list key(s) %s is missing";

    public static final String DUPLICATE_KEYS_IS_PRESENT = "Duplicate list key(s) %s is present";

    public static final String RANGE_CONSTRAINT = "Value \"%s\" does not meet the range constraints. Expected range of value: %s to %s";

    public static final String LENGTH_CONSTRAINT = "Value \"%s\" does not meet the length constraints. Expected length is: %s to %s";

    public static final String USER_IS_NOT_AUTHORIZED_ERROR_MESSAGE = "User is not authorized to invoke the protocol operation";

}
