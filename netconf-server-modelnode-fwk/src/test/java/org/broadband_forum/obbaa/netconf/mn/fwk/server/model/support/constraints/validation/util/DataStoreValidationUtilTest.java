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

import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractDataStoreValidatorTest;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;

public class DataStoreValidationUtilTest extends AbstractDataStoreValidatorTest {

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
        DataStoreValidationPathBuilder pathBuilder = new DataStoreValidationPathBuilder(m_schemaRegistry,
                m_modelNodeHelperRegistry);
        Map<SchemaPath, String> paths = pathBuilder.getSchemaPathsFromXPath(schemaNode, schemaNode.getConstraints()
                .getWhenCondition());

        assertTrue(paths.entrySet().iterator().next().getKey().equals(resultLeafSchemaPath));
        assertEquals("result-leaf/when-validation/leaf-type", paths.entrySet().iterator().next().getValue());

    }

    @Test
    public void testRemovePrefixes() {
        String xPath = "/adh:a/adm:b/abc:c";
        assertEquals("/a/b/c", m_expValidator.removePrefixes(JXPathUtils.getExpression(xPath)).toString());
        xPath = "/abc:z/x/j:z";
        assertEquals("/z/x/z", m_expValidator.removePrefixes(JXPathUtils.getExpression(xPath)).toString());
        xPath = "abc:z/x/j:z";
        assertEquals("z/x/z", m_expValidator.removePrefixes(JXPathUtils.getExpression(xPath)).toString());
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
        DataStoreValidationPathBuilder pathBuilder = new DataStoreValidationPathBuilder(m_schemaRegistry,
                m_modelNodeHelperRegistry);
        Map<SchemaPath, String> paths = pathBuilder.getSchemaPathsFromXPath(schemaNode,
                ((LeafrefTypeDefinition) schemaNode.getType()).getPathStatement());
        assertTrue(paths.entrySet().iterator().next().getKey().equals(artistNamePath));
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
