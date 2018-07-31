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

import java.util.Collection;
import java.util.Date;
import java.util.List;

public class QueryBuilder {

    public static String getConditionQueryString(QueryConditionOperator conditionOperator, String attribute, Object
            value,
                                                 boolean isCaseInSensitive) {
        value = getValueQueryString(value, conditionOperator);
        String queryStr = "";
        switch (conditionOperator) {
            case EQUALS:
                queryStr += attribute + " = " + value;
                if (!Collection.class.isAssignableFrom(value.getClass())) {
                    if ("''".equals(value) || "".equals(value)) {
                        queryStr = "(" + attribute + " IS null OR " + queryStr + ")";
                    }
                }
                break;
            case GREATER_THAN:
                queryStr += attribute + " > " + value;
                break;
            case LESS_THAN:
                queryStr += attribute + " < " + value;
                break;
            case NOT_EQUALS:
                if ("''".equals(value)) {
                    queryStr += "(" + attribute + " IS not null AND " + attribute + " != " + value + ")";
                } else {
                    queryStr += "(" + attribute + " IS null OR " + attribute + " != " + value + ")";
                }
                break;
            case LESS_THAN_OR_EQUALS:
                queryStr += attribute + " <= " + value;
                break;
            case GREATER_THAN_OR_EQUALS:
                queryStr += attribute + " >= " + value;
                break;
            case IS_NULL:
                queryStr += "(" + attribute + " IS null OR " + attribute + " = '')";
                break;
            case IS_NOT_NULL:
                queryStr += "(" + attribute + " IS NOT null AND " + attribute + " != '')";
                break;
            case LIKE:
                if (isCaseInSensitive)
                    queryStr += convertToLowerInDB(attribute) + " LIKE " + convertToLowerInDB(value.toString()
                            .replace("*", "%"));
                else
                    queryStr += attribute + " LIKE " + value.toString().replace("*", "%");
                break;
            case CONTAINS:
            case BEGIN_WITH:
            case END_WITH:
                if (isCaseInSensitive)
                    queryStr += convertToLowerInDB(attribute) + " LIKE " + convertToLowerInDB(value.toString());
                else
                    queryStr += attribute + " LIKE " + value;
                break;
            case NOT_CONTAINS:
            case NOT_BEGIN_WITH:
            case NOT_END_WITH:
                if (isCaseInSensitive)
                    queryStr += convertToLowerInDB(attribute) + " NOT LIKE " + convertToLowerInDB(value.toString());
                else
                    queryStr += attribute + " NOT LIKE " + value;
                break;
            case IN:
                queryStr += attribute + " IN " + value;
                break;
            case NOT_IN:
                queryStr += attribute + " NOT IN " + value;
                break;
            default:
                break;
        }

        return queryStr;
    }

    public static String getValueQueryString(Object value, QueryConditionOperator conditionOperator) {
        StringBuilder valueQueryStr = new StringBuilder();
        final String none = "";
        final String quote = "'";
        final String wild = "%";
        String begin = none;
        String end = none;
        String start;
        String finish;
        QueryConditionOperator listOperator;

        if ((conditionOperator.equals(QueryConditionOperator.BEGIN_WITH))
                || (conditionOperator.equals(QueryConditionOperator.NOT_BEGIN_WITH))
                || (conditionOperator.equals(QueryConditionOperator.CONTAINS))
                || (conditionOperator.equals(QueryConditionOperator.NOT_CONTAINS))) {
            end = wild;
        }
        if ((conditionOperator.equals(QueryConditionOperator.END_WITH)) || (conditionOperator.equals
                (QueryConditionOperator.NOT_END_WITH))
                || (conditionOperator.equals(QueryConditionOperator.CONTAINS))
                || (conditionOperator.equals(QueryConditionOperator.NOT_CONTAINS))) {
            begin = wild;
        }

        if ((conditionOperator.equals(QueryConditionOperator.IN)) || (conditionOperator.equals(QueryConditionOperator
                .NOT_IN))) {
            start = none;
            finish = none;
            listOperator = QueryConditionOperator.EQUALS;
        } else {
            start = quote + begin;
            finish = end + quote;
            listOperator = conditionOperator;
        }

        if (value == null) {
            valueQueryStr.append(none);
        } else if (value instanceof Date) {
            Date date = (Date) value;
            valueQueryStr.append(quote).append(new java.sql.Date(date.getTime())).append(" ").append(new java.sql
                    .Time(date.getTime()))
                    .append(quote);
        } else if (value instanceof String) {
            String temp = value.toString();
            valueQueryStr.append(start).append(temp).append(finish);
        } else if ((!begin.equals(none)) || (!end.equals(none))) {
            valueQueryStr.append(quote).append(begin).append(value.toString()).append(end).append(quote);
        } else if (value instanceof java.util.List) {
            valueQueryStr.append("(");
            List list = (List) value;
            if (0 == list.size()) {
                return none;
            }
            int i = 0;
            for (Object tmp : list) {
                if (0 == i) {
                    valueQueryStr.append(getValueQueryString(list.get(0), listOperator));
                } else {
                    valueQueryStr.append(", ").append(getValueQueryString(tmp, listOperator));
                }
                i++;
            }
            valueQueryStr.append(")");
        } else {
            valueQueryStr.append(value.toString());
        }

        return valueQueryStr.toString();
    }

    private static String convertToLowerInDB(String string) {
        return " LOWER(" + string + ") ";
    }
}
