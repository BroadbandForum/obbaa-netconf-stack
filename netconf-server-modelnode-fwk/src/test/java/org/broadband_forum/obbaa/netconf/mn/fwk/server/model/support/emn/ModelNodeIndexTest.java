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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static junit.framework.TestCase.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.IndexedList;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;

public class ModelNodeIndexTest {
    ModelNodeIndex m_index;

    @Mock
    private XmlModelNodeImpl m_libraryNode;
    @Mock
    private XmlModelNodeImpl m_artist1Node;
    @Mock
    private XmlModelNodeImpl m_artist2Node;
    @Mock
    private XmlModelNodeImpl m_album1Node;
    @Mock
    private XmlModelNodeImpl m_album2Node;
    @Mock
    private SchemaRegistryImpl m_schemaRegistry;

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        m_index = new ModelNodeIndex();
        setupNode(m_libraryNode, "/container=jukebox/container=library");
        setupNode(m_artist1Node, "/container=jukebox/container=library/container=artist/name=lenny");
        setupNode(m_artist2Node, "/container=jukebox/container=library/container=artist/name=lenny2");
        setupNode(m_album1Node, "/container=jukebox/container=library/container=artist/name=lenny/container=album/name=greatest hits");
        setupNode(m_album2Node, "/container=jukebox/container=library/container=artist/name=lenny/container=album/name=greatest hits2");

        setupAttrs(m_artist1Node, "name, lenny");
        setupAttrs(m_artist2Node, "name, lenny2");
        setupAttrs(m_album1Node, "name, greatest hits", "year,2000");
        setupAttrs(m_album2Node, "name, greatest hits2", "year,2000");

        setupChildren(m_libraryNode, m_artist1Node, m_artist2Node);
        setupChildren(m_artist1Node, m_album1Node, m_album2Node);
    }

    private void setupChildren(XmlModelNodeImpl parentNode, XmlModelNodeImpl ... children) {
        Map<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> childrenMap = mock(Map.class);
        when(parentNode.getChildren()).thenReturn(childrenMap);
        List<IndexedList<ModelNodeId, XmlModelNodeImpl>> childList = new ArrayList<>();
        childList.addAll(getIndexedList(children));
        when(childrenMap.values()).thenReturn(childList);
    }

    private List<IndexedList<ModelNodeId, XmlModelNodeImpl>> getIndexedList(XmlModelNodeImpl[] children) {
        List<IndexedList<ModelNodeId, XmlModelNodeImpl>> list = new ArrayList<>();
        IndexedList<ModelNodeId, XmlModelNodeImpl> innerList = new IndexedList<>();
        list.add(innerList);
        for(XmlModelNodeImpl child : children){
            innerList.add( child);
        }
        return list;
    }

    private void setupAttrs(XmlModelNodeImpl node, String ... attrs) {
        Map<QName, ConfigLeafAttribute> attrsMap = new HashMap<>();
        when(node.getAttributes()).thenReturn(attrsMap);

        for(String attr : attrs) {
            ConfigLeafAttribute leafAttribute = mock(ConfigLeafAttribute.class);
            String[] parts = attr.split(",");
            attrsMap.put(QName.create("test:ns", parts[0].trim()), leafAttribute);
            when(leafAttribute.getStringValue()).thenReturn(parts[1].trim());
            doAnswer((invocationOnMock) -> invocationOnMock.getArguments()[1] +"/"+parts[0].trim()).when(leafAttribute).xPathString(any(SchemaRegistry.class), any(String.class));
            doAnswer(invocationOnMock -> parts[0].trim()+"-value").when(leafAttribute).toString();
            String attrXPath = leafAttribute.xPathString(m_schemaRegistry, node.getIndexNodeId().xPathString(m_schemaRegistry, true, true));
            node.getNodeIndex().addAttrIndex(attrXPath, leafAttribute);
            node.getNodeIndex().addAttrTypeIndex(attrXPath, node.getIndexNodeId().xPathString(m_schemaRegistry, false, false) +"/"+parts[0].trim());
        }
    }

    private void setupNode(XmlModelNodeImpl node, String strNodeId) {
        ModelNodeId nodeId = new ModelNodeId(strNodeId, "test:ns");
        when(node.getIndexNodeId()).thenReturn(nodeId);
        when(node.getModelNodeId()).thenReturn(nodeId);
        NodeIndex nodeIndex = new NodeIndex();
        nodeIndex.addNodeIndex(node.getIndexNodeId().xPathString(m_schemaRegistry, true, true));
        when(node.getNodeIndex()).thenReturn(nodeIndex);
        when(node.getSchemaRegistry()).thenReturn(m_schemaRegistry);
    }

    @Test
    public void testIndexCreationAndDeletion (){
        m_index.addToIndex(m_libraryNode, false);
        m_index.addToIndex(m_artist2Node, false);

        assertEquals("Nodes {\n" +
                "  /jukebox/library\n" +
                "  /jukebox/library/artist[name = 'lenny2']\n" +
                "}\n" +
                "Attributes {\n" +
                "  /jukebox/library/artist[name = 'lenny2']/name --> lenny2\n" +
                "}\n" +
                "AttributesOfType {\n" +
                "  /jukebox/library/artist/name --> {\n" +
                "    lenny2 --> [name-value]\n" +
                "  }\n" +
                "}\n", m_index.print());

        m_index.addToIndex(m_album1Node, false);
        m_index.addToIndex(m_album2Node, false);
        m_index.addToIndex(m_artist1Node, false);

        assertEquals("Nodes {\n" +
                "  /jukebox/library\n" +
                "  /jukebox/library/artist[name = 'lenny2']\n" +
                "  /jukebox/library/artist[name = 'lenny']/album[name = 'greatest hits']\n" +
                "  /jukebox/library/artist[name = 'lenny']/album[name = 'greatest hits2']\n" +
                "  /jukebox/library/artist[name = 'lenny']\n" +
                "}\n" +
                "Attributes {\n" +
                "  /jukebox/library/artist[name = 'lenny2']/name --> lenny2\n" +
                "  /jukebox/library/artist[name = 'lenny']/album[name = 'greatest hits']/year --> 2000\n" +
                "  /jukebox/library/artist[name = 'lenny']/album[name = 'greatest hits']/name --> greatest hits\n" +
                "  /jukebox/library/artist[name = 'lenny']/album[name = 'greatest hits2']/year --> 2000\n" +
                "  /jukebox/library/artist[name = 'lenny']/album[name = 'greatest hits2']/name --> greatest hits2\n" +
                "  /jukebox/library/artist[name = 'lenny']/name --> lenny\n" +
                "}\n" +
                "AttributesOfType {\n" +
                "  /jukebox/library/artist/name --> {\n" +
                "    lenny --> [name-value]\n" +
                "    lenny2 --> [name-value]\n" +
                "  }\n" +
                "  /jukebox/library/artist/album/year --> {\n" +
                "    2000 --> [year-value, year-value]\n" +
                "  }\n" +
                "  /jukebox/library/artist/album/name --> {\n" +
                "    greatest hits2 --> [name-value]\n" +
                "    greatest hits --> [name-value]\n" +
                "  }\n" +
                "}\n", m_index.print());

        m_index.removeNode(m_artist1Node);

        assertEquals("Nodes {\n" +
                "  /jukebox/library\n" +
                "  /jukebox/library/artist[name = 'lenny2']\n" +
                "}\n" +
                "Attributes {\n" +
                "  /jukebox/library/artist[name = 'lenny2']/name --> lenny2\n" +
                "}\n" +
                "AttributesOfType {\n" +
                "  /jukebox/library/artist/name --> {\n" +
                "    lenny2 --> [name-value]\n" +
                "  }\n" +
                "}\n", m_index.print());

    }
}
