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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractDataStoreValidatorTest;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

@RunWith(RequestScopeJunitRunner.class)
public class DataStoreValidationUtilTest extends AbstractDataStoreValidatorTest {

    private static final SchemaPath SONG_SP = SchemaPathBuilder.fromString("(urn:org:bbf2:pma:validation-yang11?revision=2015-12-14),validation-yang11,leaf-ref-yang11,album");

    @Test
    public void testSingleLevelLeaf() throws ModelNodeInitException, DataStoreException {

        String requestXml = "/datastorevalidatortest/rangevalidation/defaultxml/schema-path-retrival-test1.xml";
        getModelNode();
        EditConfigRequest request = createRequest(requestXml);
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        assertTrue(response.isOk());

        SchemaPath leafTypeSchemaPath = getSchemaPathFor("when-validation", "leaf-type");
        SchemaPath resultLeafSchemaPath = getSchemaPathFor("when-validation", "result-leaf");
        DataSchemaNode schemaNode = m_schemaRegistry.getDataSchemaNode(leafTypeSchemaPath);
        DataStoreValidationPathBuilder pathBuilder = new DataStoreValidationPathBuilder(m_schemaRegistry, m_modelNodeHelperRegistry);
        RevisionAwareXPath whenCondition = schemaNode.getWhenCondition().orElse(null);
        Map<SchemaPath, ArrayList<String>> paths = null;
        if (whenCondition != null) {
            paths = pathBuilder.getSchemaPathsFromXPath(schemaNode, whenCondition.getOriginalString(), null);
        }
        assertTrue(paths.entrySet().iterator().next().getKey().equals(resultLeafSchemaPath));
    }

    @Test
    public void testRemovePrefixes(){
        String xPath = "/adh:a/adm:b/abc:c";
        assertEquals ("/a/b/c",m_expValidator.removePrefixes(JXPathUtils.getExpression(xPath)).toString());
        xPath = "/abc:z/x/j:z";
        assertEquals ("/z/x/z",m_expValidator.removePrefixes(JXPathUtils.getExpression(xPath)).toString());
        xPath = "abc:z/x/j:z";
        assertEquals ("z/x/z",m_expValidator.removePrefixes(JXPathUtils.getExpression(xPath)).toString());
    }

    @Test
    public void testLeafRef() throws ModelNodeInitException {

        String requestXml = "/datastorevalidatortest/leafrefvalidation/defaultxml/valid-reference-leaf-ref.xml";
        getModelNode();
        EditConfigRequest request = createRequest(requestXml);
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        assertTrue(response.isOk());

        SchemaPath songArtistNamePath = getSchemaPathFor("song", "artist-name");
        SchemaPath artistNamePath = getSchemaPathFor("artist", "name");
        LeafSchemaNode schemaNode = (LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(songArtistNamePath);
        DataStoreValidationPathBuilder pathBuilder = new DataStoreValidationPathBuilder(m_schemaRegistry, m_modelNodeHelperRegistry);
        Map<SchemaPath,ArrayList<String>> paths = pathBuilder.getSchemaPathsFromXPath(schemaNode,
                ((LeafrefTypeDefinition) schemaNode.getType()).getPathStatement().getOriginalString(), null);
        assertTrue(paths.entrySet().iterator().next().getKey().equals(artistNamePath));
    }

    @Test
    public void testLeafRefNotRequired() throws ModelNodeInitException {

        String requestXml = "/datastorevalidatortest/leafrefvalidation/defaultxml/valid-reference-leaf-ref-yang11.xml";
        getModelNode();
        EditConfigRequest request = createRequest(requestXml);
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        assertTrue(response.isOk());

        SchemaPath songArtistNamePath = getSchemaPathFor("song", "artist-name");
        SchemaPath artistNamePath = getSchemaPathFor("artist", "name");
        LeafSchemaNode schemaNode = (LeafSchemaNode) m_schemaRegistry.getDataSchemaNode(songArtistNamePath);
        DataStoreValidationPathBuilder pathBuilder = new DataStoreValidationPathBuilder(m_schemaRegistry, m_modelNodeHelperRegistry);
        Map<SchemaPath, ArrayList<String>> paths = pathBuilder.getSchemaPathsFromXPath(schemaNode,
                ((LeafrefTypeDefinition) schemaNode.getType()).getPathStatement().getOriginalString(), null);
        assertEquals(1, paths.size());
        assertTrue(paths.entrySet().iterator().next().getKey().equals(artistNamePath));
    }

    @Test
    public void testBigListLookupIsDoneEfficiently() throws ModelNodeInitException {
        when(m_modelNodeDsm.isChildTypeBigList(SONG_SP, m_schemaRegistry)).thenReturn(true);
        String requestXml = "/datastorevalidatortest/leafrefvalidation/defaultxml/valid-reference-leaf-ref-yang11.xml";
        getModelNode();
        EditConfigRequest request = createRequest(requestXml);
        request.setMessageId("1");
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, request, response);
        assertTrue(response.isOk());
        ArgumentCaptor<Map> matchCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<ModelNodeId> idCaptor = ArgumentCaptor.forClass(ModelNodeId.class);
        Map<QName, ConfigLeafAttribute> criteria = new HashMap<>();
        criteria.put(VALIDATION1_QNAME.create(YANG11_NS, "2015-12-14", "name"),
                new GenericConfigAttribute("name", YANG11_NS, "Album1"));
        ModelNodeId artistId = new ModelNodeId("/container=validation-yang11/container=leaf-ref-yang11", YANG11_NS);
        verify(m_modelNodeDsm, times(3)).findNodes(eq(SONG_SP), eq(criteria), eq(artistId), eq(m_schemaRegistry));

    }

    private SchemaPath getSchemaPathFor(String parent, String leaf) {
        SchemaPath parentSchemaPath = m_schemaRegistry.getDescendantSchemaPath(m_rootModelNode.getModelNodeSchemaPath(),
                m_schemaRegistry.lookupQName(NAMESPACE, parent));

        if (leaf != null) {
            SchemaPath leafSchemaPath = m_schemaRegistry.getDescendantSchemaPath(parentSchemaPath,
                    m_schemaRegistry.lookupQName(NAMESPACE, leaf));
            return leafSchemaPath;
        } else {
            return parentSchemaPath;
        }
    }
}
