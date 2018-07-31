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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath;

import org.apache.commons.jxpath.ri.JXPathCompiledExpression;
import org.apache.commons.jxpath.ri.compiler.CoreOperationEqual;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


/**
 * Created by vgotagi on 10/18/16.
 */
public class JXPathUtilsTest {

    @Test
    public void testGetExpression() {
        Expression[] expressions = new Expression[2];
        Expression exp = new CoreOperationEqual(expressions[0], expressions[1]);
        JXPathCompiledExpression jcExp = new JXPathCompiledExpression("xPath", exp);
        assertNotNull(JXPathUtils.getExpression(jcExp));
    }

    @Test
    public void testNull() {
        assertNull(JXPathUtils.getExpression((JXPathCompiledExpression) null));
    }
}
