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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util;

import static org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder.fromString;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn.CONTAINER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.nio.file.Paths;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImplTest;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;

public class SchemaRegistryUtilTest {

    SchemaRegistry m_registry;

    @Before
    public void setup() throws SchemaBuildException {
        m_registry = new SchemaRegistryImpl(new NoLockService());
        List<YangTextSchemaSource> yangSources = new ArrayList<YangTextSchemaSource>();
        addYangSource(yangSources, "/yangSchemaValidationTest/referenceyangs/ietf-yang-types.yang");
        addYangSource(yangSources, "/yangSchemaValidationTest/referenceyangs/anv-alarms.yang");
        m_registry.buildSchemaContext(yangSources);

    }

    private void addYangSource(List<YangTextSchemaSource> yangSources, String fileName) {
        URL inFile = SchemaRegistryUtilTest.class.getResource(fileName);
        yangSources.add(YangParserUtil.getYangSource(inFile));
    }

    @Test
    public void testGetAllIdentities() {
        Set<IdentitySchemaNode> identities = SchemaRegistryUtil.getAllIdentities(m_registry);
        assertEquals(1, identities.size());
        for (IdentitySchemaNode node : identities) {
            Set<IdentitySchemaNode> identitySchemaNodes = node.getBaseIdentities();
            assertTrue(identitySchemaNodes.isEmpty());
            assertEquals("alarm-identity", node.getQName().getLocalName());

        }
    }

    @Test
    public void testGetAllModulesNamespaceAsString() {
        List<String> namespaces = SchemaRegistryUtil.getAllModulesNamespaceAsString(m_registry);
        assertTrue(namespaces.contains("http://www.test-company.com/solutions/anv-alarms"));
        assertTrue(namespaces.contains("urn:ietf:params:xml:ns:yang:ietf-yang-types"));
    }

    @Test
    public void testGetModuleSubtreeRoots() throws SchemaBuildException, SchemaPathBuilderException {
        m_registry = new SchemaRegistryImpl(SchemaRegistryImplTest.getAnvYangFiles(), new NoLockService());
        List<SchemaPath> subtreeRoots = SchemaRegistryUtil.getModuleSubtreeRoots(m_registry, "alu-dpu-swmgmt",
                "2015-07-14");

        assertEquals(1, subtreeRoots.size());
        SchemaPath swmgmtSubtreeRoot = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device",
                "(urn:org:bbf:pma:alu-dpu-swmgmt?revision=2015-07-14)swmgmt");
        assertEquals(swmgmtSubtreeRoot, subtreeRoots.get(0));

        subtreeRoots = SchemaRegistryUtil.getModuleSubtreeRoots(m_registry, "alu-pma", "2015-07-14");
        assertEquals(1, subtreeRoots.size());
        SchemaPath pmaSubtreeRoot = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma");
        assertEquals(pmaSubtreeRoot, subtreeRoots.get(0));


    }

    @Test
    public void testGetDataParentSchemaPath() throws Exception {
        m_registry = new SchemaRegistryImpl(SchemaRegistryImplTest.getAnvYangFiles(), new NoLockService());
        SchemaPath expectedSP = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device");
        SchemaPath cdpSP = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device," +
                "connection-initiator-params," +
                "pma,configured-device-properties");
        assertEquals(expectedSP, SchemaRegistryUtil.getDataParentSchemaPath(m_registry, cdpSP));

        expectedSP = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder");
        SchemaPath deviceSP = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device");
        assertEquals(expectedSP, SchemaRegistryUtil.getDataParentSchemaPath(m_registry, deviceSP));
    }

    @Test
    public void testGetDataParent() throws Exception {
        m_registry = new SchemaRegistryImpl(SchemaRegistryImplTest.getAnvYangFiles(), new NoLockService());
        SchemaPath sp = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device," +
                "connection-initiator-params," +
                "pma,configured-device-properties");
        assertEquals("device", SchemaRegistryUtil.getDataParent(m_registry, sp).getQName().getLocalName());
        assertEquals("urn:org:bbf:pma", SchemaRegistryUtil.getDataParent(m_registry, sp).getQName().getNamespace
                ().toString());
        assertEquals("2015-07-14", SchemaRegistryUtil.getDataParent(m_registry, sp).getQName().getFormattedRevision());

        sp = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device");
        assertEquals("device-holder", SchemaRegistryUtil.getDataParent(m_registry, sp).getQName().getLocalName());
        assertEquals("urn:org:bbf:pma", SchemaRegistryUtil.getDataParent(m_registry, sp).getQName().getNamespace
                ().toString());
        assertEquals("2015-07-14", SchemaRegistryUtil.getDataParent(m_registry, sp).getQName().getFormattedRevision());
    }

    @Test
    public void testGetChildQname() throws Exception {
        Node dataNode = mock(Node.class);
        DataSchemaNode schemaNode = mock(DataSchemaNode.class);
        SchemaRegistry registry = mock(SchemaRegistry.class);
        assertNull(SchemaRegistryUtil.getChildQname(dataNode, schemaNode, registry));

    }

    @Test
    public void testGetNamespaceFromModule() throws SchemaBuildException {
        SchemaRegistry schemaRegistry = initSchemaRegistry();

        assertEquals("urn:thirdparty-module3", SchemaRegistryUtil.getNamespaceFromModule(schemaRegistry, "module2",
                "tp3"));
        assertEquals("urn:module1", SchemaRegistryUtil.getNamespaceFromModule(schemaRegistry, "module2",
                "module1prefix"));
        assertEquals("urn:thirdparty-module2", SchemaRegistryUtil.getNamespaceFromModule(schemaRegistry, "module1",
                "tp2"));

    }

    private SchemaRegistry initSchemaRegistry() throws SchemaBuildException {
        SchemaRegistry registry = new SchemaRegistryImpl(new NoLockService());
        List<YangTextSchemaSource> yangSources = new ArrayList<>();
        addYangSource(yangSources, "/schemaregistryutiltest/module1@2016-02-10.yang");
        addYangSource(yangSources, "/schemaregistryutiltest/module2@2016-02-10.yang");
        addYangSource(yangSources, "/schemaregistryutiltest/thirdparty-module1@2016-02-10.yang");
        addYangSource(yangSources, "/schemaregistryutiltest/thirdparty-module2@2016-02-10.yang");
        addYangSource(yangSources, "/schemaregistryutiltest/thirdparty-module3@2016-02-10.yang");
        registry.buildSchemaContext(yangSources);
        return registry;
    }

    @Test
    public void testGetChoiceParentSchemaNode() throws SchemaBuildException, SchemaPathBuilderException {
        m_registry = new SchemaRegistryImpl(SchemaRegistryImplTest.getAnvYangFiles(), new NoLockService());
        SchemaPath configuredDevicePropSchemaPath = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma," +
                "device-holder,device,connection-initiator-params,pma,configured-device-properties");
        SchemaPath connectionInitiatorParamsSchemaPath = fromString("(urn:org:bbf:pma?revision=2015-07-14)" +
                "pma,device-holder,device,connection-initiator-params");
        assertEquals(m_registry.getDataSchemaNode(connectionInitiatorParamsSchemaPath),
                SchemaRegistryUtil.getChoiceParentSchemaNode(configuredDevicePropSchemaPath, m_registry));

        SchemaPath deviceSchemaPath = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma," +
                "device-holder,device");

        assertNull(SchemaRegistryUtil.getChoiceParentSchemaNode(deviceSchemaPath, m_registry));
    }

    @Test
    public void testCreateSchemaRegistry() throws SchemaBuildException {
        String resDir = Paths.get(SchemaRegistryUtilTest.class.getResource("/yangSchemaValidationTest/referenceyangs/" +
            "anvyangs/alu-device-plugs@2015-07-14.yang").getPath()).getParent().toString();
        SchemaRegistry schemaRegistry = SchemaRegistryUtil.createSchemaRegistry(Collections.singletonList(resDir), false, new NoLockService());
        assertNotNull(SchemaRegistryUtil.createSchemaRegistry(Collections.singletonList(resDir), false,new NoLockService()));
        Module module = schemaRegistry.getModule("alu-device-plugs");
        assertEquals("alu-device-plugs", module.getName());
        assertEquals("plug", module.getPrefix());
        assertEquals("1", module.getYangVersion());
        assertEquals("urn:org:bbf:pma:alu-device-plugs", module.getNamespace().toString());
    }

    @Test
    public void testGetChildSchemaNode() throws Exception {
        m_registry = new SchemaRegistryImpl(SchemaRegistryImplTest.getAnvYangFiles(), new NoLockService());
        Document document = DocumentUtils.createDocument();
        Element rootElement = document.createElementNS("urn:org:bbf:pma", "pma");
        DataSchemaNode rootNode = SchemaRegistryUtil.getChildSchemaNode(rootElement, SchemaPath.ROOT, m_registry);
        assertEquals(m_registry.getDataSchemaNode(SchemaPathBuilder.fromString("" +
                "(urn:org:bbf:pma?revision=2015-07-14)pma")), rootNode);

        Element nonRootElement = document.createElementNS("urn:org:bbf:pma", "pma-list");
        DataSchemaNode nonRootNode = SchemaRegistryUtil.getChildSchemaNode(nonRootElement, SchemaPathBuilder
                .fromString("(urn:org:bbf:pma?revision=2015-07-14)pma"), m_registry);
        assertEquals(m_registry.getDataSchemaNode(SchemaPathBuilder.fromString("" +
                "(urn:org:bbf:pma?revision=2015-07-14)pma,pma-list")), nonRootNode);

        assertNull(SchemaRegistryUtil.getChildSchemaNode(document.createElementNS("urn:org:bbf:pma", "blah-blah")
                , SchemaPathBuilder.fromString("(urn:org:bbf:pma?revision=2015-07-14)pma"), m_registry));
    }

    @Test
    public void testGetSchemaPathFromNodeId() throws SchemaBuildException, SchemaPathBuilderException {
        SchemaRegistry registry = new SchemaRegistryImpl(new NoLockService());
        List<YangTextSchemaSource> yangSources = new ArrayList<>();
        addYangSource(yangSources, "/referenceyangs/choice-case-module.yang");
        registry.buildSchemaContext(yangSources);
        ModelNodeId nodeId = new ModelNodeId();
        assertEquals(SchemaPath.ROOT, SchemaRegistryUtil.getSchemaPath(registry, nodeId));

        nodeId.addRdn(CONTAINER, "urn:choice-case-test", "device-manager");
        assertEquals(SchemaPathBuilder.fromString("(urn:choice-case-test?revision=2015-12-14)device-manager"),
                SchemaRegistryUtil.getSchemaPath(registry, nodeId));

        nodeId.addRdn(CONTAINER, "urn:choice-case-test", "device-holder");
        assertEquals(SchemaPathBuilder.fromString("(urn:choice-case-test?revision=2015-12-14)device-manager," +
                        "device-holder"),
                SchemaRegistryUtil.getSchemaPath(registry, nodeId));
        nodeId.addRdn("name", "urn:choice-case-test", "OLT1");
        assertEquals(SchemaPathBuilder.fromString("(urn:choice-case-test?revision=2015-12-14)device-manager," +
                        "device-holder"),
                SchemaRegistryUtil.getSchemaPath(registry, nodeId));

        nodeId.addRdn(CONTAINER, "urn:choice-case-test", "device");
        assertEquals(SchemaPathBuilder.fromString("(urn:choice-case-test?revision=2015-12-14)device-manager," +
                        "device-holder,device"),
                SchemaRegistryUtil.getSchemaPath(registry, nodeId));
        nodeId.addRdn("device-id", "urn:choice-case-test", "ONT1");
        assertEquals(SchemaPathBuilder.fromString("(urn:choice-case-test?revision=2015-12-14)device-manager," +
                        "device-holder,device"),
                SchemaRegistryUtil.getSchemaPath(registry, nodeId));
    }

}
