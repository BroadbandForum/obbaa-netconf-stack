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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.SetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.StringToObjectTransformer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.TransformerException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URI;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by vgotagi on 10/19/16.
 */
public class AnnotationConfigAttributeHelperTest {
    private Method m_method;
    private ModelNode m_node = mock(ModelNode.class);
    private URI m_uri = URI.create("");
    private QName m_qName = new QName(m_uri, "en");

    @Before
    public void setUp() throws Exception {
        m_method = Class.forName(ModelNode.class.getName()).getMethod("getModelNodeId");
    }

    @Test(expected = GetAttributeException.class)
    public void testException() throws Exception {
        when(m_node.getModelNodeId()).thenThrow(new IllegalArgumentException("test"));
        AnnotationConfigAttributeHelper helper = new AnnotationConfigAttributeHelper(m_method);
        helper.getValue(m_node);
    }


    @Test(expected = SetAttributeException.class)
    public void testSetAttrException() throws Exception {
        AnnotationConfigAttributeHelper helper = new AnnotationConfigAttributeHelper(m_method);
        helper.setValue(m_node, new GenericConfigAttribute("value"));
    }

    @Test(expected = SetAttributeException.class)
    public void testSetValue() throws Exception {
        Method getterMethod = Class.forName(ModelNode.class.getName()).getMethod("getValue");
        Method setterMethod = Class.forName(ModelNode.class.getName()).getMethod("setValue", Object.class);
        AnnotationConfigAttributeHelper helper = new AnnotationConfigAttributeHelper(getterMethod, setterMethod);
        helper.setValue(m_node, new GenericConfigAttribute("value"));
    }

    @Test
    public void testDefault() throws Exception {
        AnnotationConfigAttributeHelper helper = new AnnotationConfigAttributeHelper(m_method);
        assertNull(helper.getDefault());
    }

    @Test(expected = TransformerException.class)
    public void testTransform() throws Exception {
        StringToObjectTransformer.transform(m_qName, "value", Object.class);
    }

    @Test(expected = TransformerException.class)
    public void testTransformBigIntegr() throws Exception {
        StringToObjectTransformer.transform(m_qName, "value", BigInteger.class);
    }


}
