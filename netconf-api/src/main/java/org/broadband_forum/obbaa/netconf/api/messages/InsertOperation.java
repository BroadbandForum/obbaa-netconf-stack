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

public class InsertOperation {

	public static final String FIRST = "first";
	public static final String LAST = "last";
	public static final String BEFORE = "before";
	public static final String AFTER = "after";

    public static final InsertOperation FIRST_OP = new InsertOperation(FIRST, null);
    public static final InsertOperation LAST_OP = new InsertOperation(LAST, null);

    private final String m_value;
    private final String m_name;

    public InsertOperation(String name, String value) {
        this.m_name = name;
        this.m_value = value;
    }

    public static InsertOperation get(String name, String value) {
        if (FIRST.equals(name) ) {
            return FIRST_OP;
        } else if(LAST.equals(name)) {
            return LAST_OP;
        }
        return new InsertOperation(name, value);
    }
    
    public String getValue() {
        return this.m_value;
    }

    public String getName() {
        return this.m_name;
    }
}
