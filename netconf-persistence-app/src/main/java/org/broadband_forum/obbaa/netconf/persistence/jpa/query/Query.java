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

package org.broadband_forum.obbaa.netconf.persistence.jpa.query;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;

public class Query {
    
    public static final String EMPTY = "";

    private List<QueryCondition> m_queryConditions = new ArrayList<>();
    
    private LogicOperator m_logicOperator;
    
    public Query() {
        
    }
    
    public Query (List<QueryCondition> queryConditions, LogicOperator logicOperator) {
        this.m_queryConditions = queryConditions;
        this.m_logicOperator = logicOperator;
    }

    public List<QueryCondition> getQueryConditions() {
        return m_queryConditions;
    }

    public void setQueryConditions(List<QueryCondition> queryConditions) {
        m_queryConditions = queryConditions;
    }

    public LogicOperator getLogicOperator() {
        return m_logicOperator;
    }

    public void setLogicOperator(LogicOperator logicOperator) {
        m_logicOperator = logicOperator;
    }
    
    public void addQueryCondition(QueryCondition qCondition) {
        m_queryConditions.add(qCondition);
    }
    
    public void addQueryConditions(List<QueryCondition>  qConditions) {
        m_queryConditions.addAll(qConditions);
    }
    
    @Override
    public String toString() {
        if (m_queryConditions == null || m_queryConditions.isEmpty()) {
            return "";
        }
        return Joiner.on(" " + m_logicOperator.toString() + " ").join(m_queryConditions);
    }
}
