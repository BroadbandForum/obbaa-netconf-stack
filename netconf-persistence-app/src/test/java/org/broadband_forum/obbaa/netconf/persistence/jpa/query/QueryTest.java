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

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;

public class QueryTest {

    /*
     * Query { // Root - L1 
         QueryCondition { // qCon1 
            attributeName = device_holder_name, QueryConditionOperator = EQUALs, value = 'OLT-1'
         }, 
         QueryCondition { //qCon2 
             attributeName = device_id, QueryConditionOperator = EQUALs, value = 'device1' 
         }, 
         QueryCondition { //qCon3
             Query { //qCon3Q 
                 QueryCondition { // qCon3QQCon1 
                     Query { // qCon3QQCon1Q 
                         QueryCondition { // qCon3QQCon1QQCon1 
                             attributeName = version, QueryConditionOperator = EQUALs, value = 1 
                         }, 
                         QueryCondition { // qCon3QQCon1QQCon2 
                             attributeName = username, QueryConditionOperator = EQUALs, value = 'admin' 
                         }, 
                         logicOperator = AND 
                     } 
                 }, 
                 QueryCondition { // qCon3QQCon2 
                     Query { // qCon3QQCon2Q
                         QueryCondition { // qCon3QQCon2QQCon1
                             attributeName = version, QueryConditionOperator = EQUALs, value = 2 
                         }, 
                         QueryCondition { // qCon3QQCon2QQCon2 
                             attributeName = username, QueryConditionOperator = EQUALs, value = 'Robin' 
                         }, 
                         logicOperator = AND 
                     } 
                 },
             logicOperator = OR }
         },
         
         
         QueryCondition { //qCon4
             attributeName = attribute1, QueryConditionOperator = EQUALs, value = ''
         },
         QueryCondition { //qCon5
             attributeName = attribute2, QueryConditionOperator = GREATER_THAN, value = 10
         },
         QueryCondition { //qCon6
             attributeName = attribute3, QueryConditionOperator = LESS_THAN, value = 5
         },
         QueryCondition { //qCon7
             attributeName = attribute4, QueryConditionOperator = NOT_EQUALS, value = ''
         },
         QueryCondition { //qCon8
             attributeName = attribute5, QueryConditionOperator = NOT_EQUALS, value = 'attVal5'
         },
         QueryCondition { //qCon9
             attributeName = attribute6, QueryConditionOperator = LESS_THAN_OR_EQUALS, value = 2
         },
         QueryCondition { //qCon10
             attributeName = attribute7, QueryConditionOperator = GREATER_THAN_OR_EQUALS, value = 3
         },
         QueryCondition { //qCon11
             attributeName = attribute8, QueryConditionOperator = IS_NULL, value = ''
         },
         QueryCondition { //qCon12
             attributeName = attribute9, QueryConditionOperator = IS_NOT_NULL, value = ''
         },
         QueryCondition { //qCon13
             attributeName = attribute10, QueryConditionOperator = LIKE, value = '*attVal*10*'
         },
         QueryCondition { //qCon14
             attributeName = attribute11, QueryConditionOperator = CONTAINS, value = 'attVal11'
         },
         QueryCondition { //qCon15
             attributeName = attribute12, QueryConditionOperator = BEGIN_WITH, value = 'attVal12'
         },
         QueryCondition { //qCon16
             attributeName = attribute13, QueryConditionOperator = END_WITH, value = 'attVal13'
         },
         QueryCondition { //qCon17
             attributeName = attribute14, QueryConditionOperator = NOT_CONTAINS, value = 'attVal14'
         },
         QueryCondition { //qCon18
             attributeName = attribute15, QueryConditionOperator = NOT_BEGIN_WITH, value = 'attVal15'
         },
         QueryCondition { //qCon19
             attributeName = attribute16, QueryConditionOperator = NOT_END_WITH, value = 'attVal16'
         },
         QueryCondition { //qCon20
             attributeName = attribute17, QueryConditionOperator = IN, value = (1, 4, 9)
         },
         QueryCondition { //qCon21
             attributeName = attribute18, QueryConditionOperator = NOT_IN, value = ("a", "b", "c")
         },
         QueryCondition { //qCon22
             attributeName = attribute19, QueryConditionOperator = EQUALS, value = '1988-04-18 11:50:20'
         }
         logicOperator = AND 
     }
     */
    @Test
    public void testCreateQuery() {
        String dhn = "device_holder_name";
        String did = "device_id";
        String version = "version";
        String username = "username";
        Query root = new Query();
        root.setLogicOperator(LogicOperator.AND);
        List<QueryCondition> qCons = new ArrayList<>();
        root.setQueryConditions(qCons);

        QueryCondition qCon1 = new QueryCondition(dhn, QueryConditionOperator.EQUALS, "OLT-1");
        qCons.add(qCon1);
        QueryCondition qCon2 = new QueryCondition(did, QueryConditionOperator.EQUALS, "device1");
        qCons.add(qCon2);
        QueryCondition qCon3 = new QueryCondition();
        qCons.add(qCon3);

        List<QueryCondition> qCon3QQCons = new ArrayList<>();
        Query qCon3Q = new Query(qCon3QQCons, LogicOperator.OR);
        qCon3.setQuery(qCon3Q);

        QueryCondition qCon3QQCon1 = new QueryCondition();
        qCon3QQCons.add(qCon3QQCon1);
        List<QueryCondition> qCon3QQCon1QQCons = new ArrayList<>();
        Query qCon3QQCon1Q = new Query(qCon3QQCon1QQCons, LogicOperator.AND);
        qCon3QQCon1.setQuery(qCon3QQCon1Q);

        QueryCondition qCon3QQCon1QQCon1 = new QueryCondition(version, QueryConditionOperator.EQUALS, 1);
        qCon3QQCon1QQCons.add(qCon3QQCon1QQCon1);
        QueryCondition qCon3QQCon1QQCon2 = new QueryCondition(username, QueryConditionOperator.EQUALS, "admin");
        qCon3QQCon1QQCons.add(qCon3QQCon1QQCon2);
        // //

        List<QueryCondition> qCon3QQCon2QQCons = new ArrayList<>();
        Query qCon3QQCon2Q = new Query(qCon3QQCon2QQCons, LogicOperator.AND);
        QueryCondition qCon3QQCon2 = new QueryCondition(qCon3QQCon2Q);
        qCon3QQCons.add(qCon3QQCon2);

        QueryCondition qCon3QQCon2QQCon1 = new QueryCondition(version, QueryConditionOperator.EQUALS, 2);
        qCon3QQCon2QQCons.add(qCon3QQCon2QQCon1);
        QueryCondition qCon3QQCon2QQCon2 = new QueryCondition(username, QueryConditionOperator.EQUALS, "Robin");
        qCon3QQCon2QQCons.add(qCon3QQCon2QQCon2);
        
        QueryCondition qCon4 = new QueryCondition("attribute1", QueryConditionOperator.EQUALS, "");
        qCons.add(qCon4);
        
        QueryCondition qCon5 = new QueryCondition("attribute2", QueryConditionOperator.GREATER_THAN, 10);
        qCons.add(qCon5);
        
        QueryCondition qCon6 = new QueryCondition("attribute3", QueryConditionOperator.LESS_THAN, 5);
        qCons.add(qCon6);
        
        QueryCondition qCon7 = new QueryCondition("attribute4", QueryConditionOperator.NOT_EQUALS, "");
        qCons.add(qCon7);
        
        QueryCondition qCon8 = new QueryCondition("attribute5", QueryConditionOperator.NOT_EQUALS, "attVal5");
        qCons.add(qCon8);
        
        QueryCondition qCon9 = new QueryCondition("attribute6", QueryConditionOperator.LESS_THAN_OR_EQUALS, 2);
        qCons.add(qCon9);
        
        QueryCondition qCon10 = new QueryCondition("attribute7", QueryConditionOperator.GREATER_THAN_OR_EQUALS, 3);
        qCons.add(qCon10);
        
        QueryCondition qCon11 = new QueryCondition("attribute8", QueryConditionOperator.IS_NULL, "");
        qCons.add(qCon11);
        
        QueryCondition qCon12 = new QueryCondition("attribute9", QueryConditionOperator.IS_NOT_NULL, "");
        qCons.add(qCon12);
        
        QueryCondition qCon13 = new QueryCondition("attribute10", QueryConditionOperator.LIKE, "*attVal*10*");
        qCons.add(qCon13);
        
        QueryCondition qCon14 = new QueryCondition("attribute11", QueryConditionOperator.CONTAINS, "attVal11");
        qCons.add(qCon14);
        
        QueryCondition qCon15 = new QueryCondition("attribute12", QueryConditionOperator.BEGIN_WITH, "attVal12");
        qCons.add(qCon15);
        
        QueryCondition qCon16 = new QueryCondition("attribute13", QueryConditionOperator.END_WITH, "attVal13");
        qCons.add(qCon16);
        
        QueryCondition qCon17 = new QueryCondition("attribute14", QueryConditionOperator.NOT_CONTAINS, "attVal14");
        qCons.add(qCon17);
        
        QueryCondition qCon18 = new QueryCondition("attribute15", QueryConditionOperator.NOT_BEGIN_WITH, "attVal15");
        qCons.add(qCon18);
        
        QueryCondition qCon19 = new QueryCondition("attribute16", QueryConditionOperator.NOT_END_WITH, "attVal16");
        qCons.add(qCon19);
        
        QueryCondition qCon20 = new QueryCondition("attribute17", QueryConditionOperator.IN, Arrays.asList(1, 4, 9));
        qCons.add(qCon20);
        
        QueryCondition qCon21 = new QueryCondition("attribute18", QueryConditionOperator.NOT_IN, Arrays.asList("a", "b", "c"));
        qCons.add(qCon21);
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(1988, 3, 18, 11, 50, 20);
        Date date = calendar.getTime();
        QueryCondition qCon22 = new QueryCondition("attribute19", QueryConditionOperator.EQUALS, date);
        qCons.add(qCon22);

        String expected = "device_holder_name = 'OLT-1' AND device_id = 'device1' AND ((version = 1 AND username = 'admin') OR (version = 2 AND username = 'Robin'))"
                + " AND (attribute1 IS null OR attribute1 = '')"
                + " AND attribute2 > 10"
                + " AND attribute3 < 5"
                + " AND (attribute4 IS not null AND attribute4 != '')"
                + " AND (attribute5 IS null OR attribute5 != 'attVal5')"
                + " AND attribute6 <= 2"
                + " AND attribute7 >= 3"
                + " AND (attribute8 IS null OR attribute8 = '')"
                + " AND (attribute9 IS NOT null AND attribute9 != '')"
                + " AND  LOWER(attribute10)  LIKE  LOWER('%attVal%10%') "
                + " AND  LOWER(attribute11)  LIKE  LOWER('%attVal11%') "
                + " AND  LOWER(attribute12)  LIKE  LOWER('attVal12%') "
                + " AND  LOWER(attribute13)  LIKE  LOWER('%attVal13') "
                + " AND  LOWER(attribute14)  NOT LIKE  LOWER('%attVal14%') "
                + " AND  LOWER(attribute15)  NOT LIKE  LOWER('attVal15%') "
                + " AND  LOWER(attribute16)  NOT LIKE  LOWER('%attVal16') "
                + " AND attribute17 IN (1, 4, 9)"
                + " AND attribute18 NOT IN ('a', 'b', 'c')"
                + " AND attribute19 = '1988-04-18 11:50:20'";
        assertEquals(expected, root.toString());
    }
    
    @Test
    public void testGetConditionQueryString() {
        assertEquals("attribute1 LIKE '%value%1%'", QueryBuilder.getConditionQueryString(QueryConditionOperator.LIKE, "attribute1", "*value*1*", false));
        assertEquals("attribute2 LIKE '%value2%'", QueryBuilder.getConditionQueryString(QueryConditionOperator.CONTAINS, "attribute2", "value2", false));
        assertEquals("attribute3 NOT LIKE 'value3%'", QueryBuilder.getConditionQueryString(QueryConditionOperator.NOT_BEGIN_WITH, "attribute3", "value3", false));
    }
}
