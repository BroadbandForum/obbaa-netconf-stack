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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public final class ModelNodeDynaBeanContext {

    private final Map<QName, ConfigLeafAttribute> m_matchCriteria;
    private Map<QName, Map<QName, ConfigLeafAttribute>> m_matchCriteriaAcrossXPath;
    private final List<SchemaPath> m_schemaPathsInOrder;
    private final String m_leafNameWithModuleNameInPrefix;
    private final boolean m_moduleNameAppendedWithLocalName;

    private ModelNodeDynaBeanContext(Map<QName, ConfigLeafAttribute> matchCriteria,
                                     Map<QName, Map<QName, ConfigLeafAttribute>> matchCriteriaAcrossXPath, List<SchemaPath> schemaPathsInOrder, String leafNameWithModuleNameInPrefix,
                                     Boolean moduleNameAppendedWithLocalName) {
        this.m_matchCriteria = matchCriteria;
        this.m_matchCriteriaAcrossXPath = matchCriteriaAcrossXPath;
        this.m_schemaPathsInOrder = schemaPathsInOrder;
        this.m_leafNameWithModuleNameInPrefix = leafNameWithModuleNameInPrefix;
        this.m_moduleNameAppendedWithLocalName = moduleNameAppendedWithLocalName;
    }

    public Map<QName, ConfigLeafAttribute> getMatchCriteria() {
        return m_matchCriteria;
    }

    public Map<QName, Map<QName, ConfigLeafAttribute>> getMatchCriteriaAcrossXPath() {
        return m_matchCriteriaAcrossXPath;
    }

    public List<SchemaPath> getSchemaPathsInOrder() {
        return this.m_schemaPathsInOrder;
    }

    public String getLeafNameWithModuleNameInPrefix() {
        return m_leafNameWithModuleNameInPrefix;
    }

    public boolean isModuleNameAppendedWithLocalName() {
        return this.m_moduleNameAppendedWithLocalName;
    }

    public static class ModelNodeDynaBeanContextBuilder {

        private Map<QName,ConfigLeafAttribute> m_matchCriteria;
        private List<SchemaPath> m_schemaPathsInOrder;
        private String m_leafNameWithModuleNameInPrefix;
        private boolean m_moduleNameAppendedWithLocalName;
        private Map<QName, Map<QName, ConfigLeafAttribute>> m_matchCriteriaAcrossXPath;

        public ModelNodeDynaBeanContextBuilder() {

        }

        public ModelNodeDynaBeanContextBuilder setMatchCriteria(Map<QName,ConfigLeafAttribute> matchCriteria) {
            this.m_matchCriteria = matchCriteria;
            return this;
        }


        public ModelNodeDynaBeanContextBuilder setSchemaPathsInOrder(List<SchemaPath> schemaPathsInOrder) {
            this.m_schemaPathsInOrder = schemaPathsInOrder;
            return this;
        }

        public ModelNodeDynaBeanContextBuilder setLeafNameWithModuleNameInPrefix(String leafNameWithModuleNameInPrefix) {
            this.m_leafNameWithModuleNameInPrefix = leafNameWithModuleNameInPrefix;
            return this;
        }

        public ModelNodeDynaBeanContextBuilder setModuleNameAppendedWithLocalName(boolean moduleNameAppendedWithLocalName) {
            this.m_moduleNameAppendedWithLocalName = moduleNameAppendedWithLocalName;
            return this;
        }

        public ModelNodeDynaBeanContext build() {
            if(this.m_matchCriteria == null) {
                this.m_matchCriteria  = Collections.EMPTY_MAP;
            }
            if(this.m_schemaPathsInOrder == null) {
                this.m_schemaPathsInOrder = Collections.EMPTY_LIST;
            }
            if(this.m_matchCriteriaAcrossXPath == null) {
                this.m_matchCriteriaAcrossXPath = Collections.EMPTY_MAP;
            }
            return new ModelNodeDynaBeanContext(this.m_matchCriteria, this.m_matchCriteriaAcrossXPath, this.m_schemaPathsInOrder,
                    this.m_leafNameWithModuleNameInPrefix, this.m_moduleNameAppendedWithLocalName);
        }

        public void setMatchCriteriaAcrossXPath(Map<QName, Map<QName, ConfigLeafAttribute>> matchCriteriaAcrossXPath) {
            m_matchCriteriaAcrossXPath = matchCriteriaAcrossXPath;
        }

        public Map<QName, Map<QName, ConfigLeafAttribute>> getMatchCriteriaAcrossXPath() {
            return m_matchCriteriaAcrossXPath;
        }
    }
}
