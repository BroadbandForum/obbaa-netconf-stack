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

package org.broadband_forum.obbaa.netconf.api.util;

public enum HashAlgorithm {

    MD5("1", "MD5"), SHA_256("5", "SHA-256"), SHA_512("6", "SHA-512");

    private String m_id;
    private String m_algorithmName;

    private HashAlgorithm(String id, String name) {
        this.m_id = id;
        this.m_algorithmName = name;
    }

    public String getId() {
        return m_id;
    }

    public String getName() {
        return m_algorithmName;
    }

}
