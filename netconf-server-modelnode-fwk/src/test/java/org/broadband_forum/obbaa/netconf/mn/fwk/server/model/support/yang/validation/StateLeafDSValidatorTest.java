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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.setUpUnwrap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CompositeSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class StateLeafDSValidatorTest  extends AbstractDataStoreValidatorTest {

    @Test
    public void testStateLeafWithDefault() throws ModelNodeInitException, SAXException, IOException {
        getModelNode();
        initialiseInterceptor();
        String request = "<stateValue1 xmlns=\"urn:org:bbf2:pma:validation\">"
                +    "<child>"
                +"<value2>yes</value2>"
                +    "</child>"
                + "</stateValue1>";
       editConfig(m_server, m_clientInfo, request, true);

       String expectedOutput =  "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
               +"<data>"
               +"<validation:stateValue1 xmlns:validation=\"urn:org:bbf2:pma:validation\">"
               +"<validation:child>"
               +"<child xmlns=\"urn:org:bbf2:pma:validation\">"
               +"<value1>test</value1>"
               +"</child>"
               +"<validation:value2>yes</validation:value2>"
               +"</validation:child>"
               +"</validation:stateValue1>"
               +"<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
               +"</data>"
               +"</rpc-reply>"
               ;
       verifyGet(expectedOutput);
    }

    protected  SubSystemRegistry getSubSystemRegistry() {
        SubSystem subSystem = new LocalSubSystem() {
            @Override
            public Map<ModelNodeId, List<Element>> retrieveStateAttributes(
                    Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes) throws GetAttributeException {
                Map<ModelNodeId, List<Element>> returnValue = new HashMap<ModelNodeId,List<Element>>();
                List<Element> elements = new ArrayList<Element>();
                Document document = DocumentUtils.createDocument();
                Element element = document.createElementNS("urn:org:bbf2:pma:validation", "child");
                elements.add(element);
                Element child = document.createElementNS("urn:org:bbf2:pma:validation", "value1");
                element.appendChild(child);
                child.setTextContent("test");
                returnValue.put(mapAttributes.keySet().iterator().next(), elements);
                return returnValue;
            }
        };
        m_subSystemRegistry = Mockito.mock(SubSystemRegistry.class);
        when(m_subSystemRegistry.lookupSubsystem(VALIDATION11_SCHEMA_PATH)).thenReturn(subSystem);
        QName childQname = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "child");
        SchemaPath childSchemaPath = buildSchemaPath(VALIDATION11_SCHEMA_PATH, childQname);
        QName stateLeafQname = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "value1");
        SchemaPath stateLeafSchemaPath = buildSchemaPath(childSchemaPath,stateLeafQname); 
        QName secondLeafQname = QName.create("urn:org:bbf2:pma:validation", "2015-12-14", "value2");
        SchemaPath secondLeafSchemaPath = buildSchemaPath(childSchemaPath,secondLeafQname);
        when(m_subSystemRegistry.lookupSubsystem(stateLeafSchemaPath)).thenReturn(subSystem);
        when(m_subSystemRegistry.lookupSubsystem(childSchemaPath)).thenReturn(subSystem);
        when(m_subSystemRegistry.lookupSubsystem(secondLeafSchemaPath)).thenReturn(subSystem);
        when(m_subSystemRegistry.getCompositeSubSystem()).thenReturn(mock(CompositeSubSystem.class));
        setUpUnwrap(m_subSystemRegistry);
        return m_subSystemRegistry;
    }
}
