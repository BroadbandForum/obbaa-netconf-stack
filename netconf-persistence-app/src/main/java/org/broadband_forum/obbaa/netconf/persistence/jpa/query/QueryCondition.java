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

public class QueryCondition {

    private String m_attributeName;
    
    private QueryConditionOperator m_operator;
    
    private Object m_value;
    
    private Query m_query;
    
    public QueryCondition() {
        
    }
    
    public QueryCondition(Query query) {
        m_query = query;
    }
    
    public QueryCondition (String attributeName, QueryConditionOperator operator, Object value) {
        this.m_attributeName = attributeName;
        this.m_operator = operator;
        this.m_value = value;
    }

    public String getAttributeName() {
        return m_attributeName;
    }

    public void setAttributeName(String attributeName) {
        m_attributeName = attributeName;
    }

    public QueryConditionOperator getOperator() {
        return m_operator;
    }

    public void setOperator(QueryConditionOperator operator) {
        m_operator = operator;
    }

    public Object getValues() {
        return m_value;
    }

    public void setValues(Object value) {
        m_value = value;
    }

    public Query getQuery() {
        return m_query;
    }

    public void setQuery(Query query) {
        m_query = query;
    }
    
    @Override
    public String toString () {
        StringBuilder str = new StringBuilder();
        if (m_attributeName != null && m_operator != null && m_value != null) {
            str.append(QueryBuilder.getConditionQueryString(m_operator, m_attributeName, m_value, true));
        } else if (m_query != null) {
            str.append("(").append(m_query.toString()).append(")");
        }
        return str.toString();
    }
}
