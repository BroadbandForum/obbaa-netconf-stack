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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc;

/**
 * Created by kbhatk on 8/8/16.
 */
public class RpcArgumentInfo {
    private final Class m_type;
    private final String m_argName;
    private final String m_namespace;

    public RpcArgumentInfo(Class type, String argName, String namespace) {
        m_type = type;
        m_argName = argName;
        m_namespace = namespace;
    }

    public Class getType() {
        return m_type;
    }

    public String getArgName() {
        return m_argName;
    }

    public String getNamespace() {
        return m_namespace;
    }
}
