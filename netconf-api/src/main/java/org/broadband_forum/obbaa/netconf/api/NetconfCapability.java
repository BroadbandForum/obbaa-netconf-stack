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

package org.broadband_forum.obbaa.netconf.api;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class NetconfCapability {

    public static final String MODULE_PARAM = "module";
    public static final String FEATURES_PARAM = "features";
    public static final String DEVIATIONS_PARAM = "deviations";
    public static final String REVISION_PARAM = "revision";
    private static final String QUESTION_MARK = "?";
    public static final String AMPERSAND = "&";
    public static final String EQUAL = "=";
    public static final String COMMA = ",";
    private final String m_uri;
    private final Map<String, String> m_parameters = new LinkedHashMap<>();

    public NetconfCapability(String capabilityString) {
        String[] split1 = capabilityString.split("\\" + QUESTION_MARK);
        m_uri = split1[0];
        if (split1.length > 1) {
            String parameterList = split1[1];
            String[] split2 = parameterList.split(AMPERSAND);
            for (String parameterEntry : split2) {
                String[] split3 = parameterEntry.split(EQUAL, -1);
                m_parameters.put(split3[0], split3[1]);
            }
        }
    }

    public NetconfCapability(String uri, String module, String revision) {
        m_uri = uri;
        m_parameters.put(MODULE_PARAM, module);
        if (revision != null && !revision.isEmpty()) {
            m_parameters.put(REVISION_PARAM, revision);
        }
    }

    public NetconfCapability(String uri, String module, String revision, String supportedFeatures,
                             String supportedDeviations) {
        this(uri, module, revision);
        if (supportedFeatures != null && !supportedFeatures.isEmpty()) {
            m_parameters.put(FEATURES_PARAM, supportedFeatures);
        }
        if (supportedDeviations != null && !supportedDeviations.isEmpty()) {
            m_parameters.put(DEVIATIONS_PARAM, supportedDeviations);
        }
    }

    public String getUri() {
        return m_uri;
    }

    public Map<String, String> getParameters() {
        return m_parameters;
    }

    public String getParameter(String paramKey) {
        return m_parameters.get(paramKey);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + m_parameters.hashCode();
        result = prime * result + ((m_uri == null) ? 0 : m_uri.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NetconfCapability other = (NetconfCapability) obj;
        if (!m_parameters.equals(other.m_parameters))
            return false;
        if (m_uri == null) {
            if (other.m_uri != null)
                return false;
        } else if (!m_uri.equals(other.m_uri))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(m_uri);
        if (!m_parameters.isEmpty()) {
            builder.append(QUESTION_MARK);
            for (Entry<String, String> parameter : m_parameters.entrySet()) {
                builder.append(parameter.getKey()).append(EQUAL).append(parameter.getValue()).append(AMPERSAND);
            }
            builder.deleteCharAt(builder.toString().length() - 1);
        }
        return builder.toString();
    }

    public boolean identical(final NetconfCapability capability) {
        if (this == capability)
            return true;
        if (capability == null)
            return false;
        if (!isParameterMatch(capability.m_parameters))
            return false;
        if (m_uri == null) {
            if (capability.m_uri != null)
                return false;
        } else if (!m_uri.equals(capability.m_uri))
            return false;
        return true;
    }

    private boolean isParameterMatch(final Map<String, String> parameters) {
        if (!parameters.keySet().equals(this.m_parameters.keySet())) {
            return false;
        }
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            final String parameterValue = entry.getValue();
            final String storedValue = this.m_parameters.get(entry.getKey());
            if ((parameterValue == null && storedValue != null) || (storedValue == null && parameterValue != null)) {
                return false;
            }
            if (parameterValue.contains(COMMA) && storedValue.contains(COMMA)) {
                final Set<String> parameterValueSet = new HashSet<>(Arrays.asList(parameterValue.split(COMMA)));
                final Set<String> storedValueSet = new HashSet<>(Arrays.asList(storedValue.split(COMMA)));
                if (!parameterValueSet.equals(storedValueSet)) {
                    return false;
                }
            } else if (!parameterValue.equals(storedValue)) {
                return false;
            }
        }
        return true;
    }

}
