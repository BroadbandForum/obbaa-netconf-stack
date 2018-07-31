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

public enum WithDefaults {

    REPORT_ALL("report-all"),

    REPORT_ALL_TAGGED("report-all-tagged"),

    WITH_DEFAULT("with-defaults"),

    EXPLICIT("explicit");

    private String m_value;

    private WithDefaults(String value) {
        this.m_value = value;
    }

    public String getValue() {
        return m_value;
    }
}
