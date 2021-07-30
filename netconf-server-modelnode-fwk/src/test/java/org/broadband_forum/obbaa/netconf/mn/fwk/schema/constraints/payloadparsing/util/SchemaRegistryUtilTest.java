package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util;

import static org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImplTest.getAnvYangFiles;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn.CONTAINER;
import static org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder.fromString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.broadband_forum.obbaa.netconf.api.parser.FileYangSource;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.support.SchemaMountRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.MountProviderInfo;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public class SchemaRegistryUtilTest {

    public static final String NESTED_CHOICE_NS_REVISION = "urn:nested-choice-case-test?revision=2020-01-20";
    public static final String NESTED_CHOICE_NS = "urn:nested-choice-case-test";
    public static final String NESTED_CHOICE_REVISION = "2020-01-20";
    private static SchemaRegistry c_anvSchemaRegistry;

    @BeforeClass
    public static void setup() throws SchemaBuildException {

        c_anvSchemaRegistry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        List<YangTextSchemaSource> yangSources = new ArrayList<YangTextSchemaSource>();
        addYangSource(yangSources, "/yangSchemaValidationTest/referenceyangs/ietf-yang-types.yang");
        addYangSource(yangSources, "/yangSchemaValidationTest/referenceyangs/anv-alarms.yang");
        addYangSource(yangSources, "/schemaregistryutiltest/test-module@2018-08-23.yang");
        addYangSource(yangSources, "/schemaregistryutiltest/nested-choice-with-leaf-list-and-list@2020-01-20.yang");
        c_anvSchemaRegistry.buildSchemaContext(yangSources, Collections.emptySet(), Collections.emptyMap());
    }

    private static void addYangSource(List<YangTextSchemaSource> yangSources, String fileName) {

        URL inFile = SchemaRegistryUtilTest.class.getResource(fileName);
        yangSources.add(YangParserUtil.getYangSource(inFile));
        yangSources.addAll(getAnvYangFiles());
    }

    @Test
    public void testGetAllIdentities() {

        String[] expectedIdentityNames = {"alarm-identity", "interface-type"};
        String[] actualIdentityNames = new String[2];
        Set<IdentitySchemaNode> identities = SchemaRegistryUtil.getAllIdentities(c_anvSchemaRegistry);
        assertEquals(2, identities.size());
        int i = 0;
        for (IdentitySchemaNode node : identities) {

            Set<IdentitySchemaNode> identitySchemaNodes = node.getBaseIdentities();
            assertTrue(identitySchemaNodes.isEmpty());
            actualIdentityNames[i++] = node.getQName().getLocalName();
        }
        assertEquals(expectedIdentityNames[0], actualIdentityNames[0]);
        assertEquals(expectedIdentityNames[1], actualIdentityNames[1]);
    }

    @Test
    public void testGetAllModulesNamespaceAsString() {

        List<String> namespaces = SchemaRegistryUtil.getAllModulesNamespaceAsString(c_anvSchemaRegistry);
        assertTrue(namespaces.contains("http://www.test-company.com/solutions/anv-alarms"));
        assertTrue(namespaces.contains("urn:ietf:params:xml:ns:yang:ietf-yang-types"));
    }

    @Test
    public void testGetModuleSubtreeRoots() throws SchemaBuildException, SchemaPathBuilderException {

        c_anvSchemaRegistry = new SchemaRegistryImpl(getAnvYangFiles(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        List<SchemaPath> subtreeRoots = SchemaRegistryUtil.getModuleSubtreeRoots(c_anvSchemaRegistry, "alu-dpu-swmgmt",
                "2015-07-14");

        assertEquals(1, subtreeRoots.size());
        SchemaPath swmgmtSubtreeRoot = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device",
                "(urn:org:bbf:pma:alu-dpu-swmgmt?revision=2015-07-14)swmgmt");
        assertEquals(swmgmtSubtreeRoot, subtreeRoots.get(0));

        subtreeRoots = SchemaRegistryUtil.getModuleSubtreeRoots(c_anvSchemaRegistry, "alu-pma", "2015-07-14");
        assertEquals(1, subtreeRoots.size());
        SchemaPath pmaSubtreeRoot = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma");
        assertEquals(pmaSubtreeRoot, subtreeRoots.get(0));
    }

    @Test
    public void testGetDataParentSchemaPath() {

        SchemaPath expectedSP = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device");
        SchemaPath cdpSP = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device,connection-initiator-params," +
                "pma,configured-device-properties");
        assertEquals(expectedSP, SchemaRegistryUtil.getDataParentSchemaPath(c_anvSchemaRegistry, cdpSP));

        expectedSP = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder");
        SchemaPath deviceSP = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device");
        assertEquals(expectedSP, SchemaRegistryUtil.getDataParentSchemaPath(c_anvSchemaRegistry, deviceSP));
    }

    @Test
    public void testGetDataParent() throws NullPointerException {

        SchemaPath sp = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device,connection-initiator-params," +
                "pma,configured-device-properties");
        assertEquals("device", SchemaRegistryUtil.getDataParent(c_anvSchemaRegistry, sp).getQName().getLocalName());
        assertEquals("urn:org:bbf:pma", SchemaRegistryUtil.getDataParent(c_anvSchemaRegistry, sp).getQName().getNamespace().toString());
        assertEquals("2015-07-14", SchemaRegistryUtil.getDataParent(c_anvSchemaRegistry, sp).getQName().getRevision().get().toString());

        sp = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device");
        assertEquals("device-holder", SchemaRegistryUtil.getDataParent(c_anvSchemaRegistry, sp).getQName().getLocalName());
        assertEquals("urn:org:bbf:pma", SchemaRegistryUtil.getDataParent(c_anvSchemaRegistry, sp).getQName().getNamespace().toString());
        assertEquals("2015-07-14", SchemaRegistryUtil.getDataParent(c_anvSchemaRegistry, sp).getQName().getRevision().get().toString());
    }

    @Test
    public void testGetChildQname() {

        Node dataNode = mock(Node.class);
        DataSchemaNode schemaNode = mock(DataSchemaNode.class);
        SchemaRegistry registry = mock(SchemaRegistry.class);
        assertNull(SchemaRegistryUtil.getChildQname(dataNode, schemaNode, registry));

    }

    @Test
    public void testGetNamespaceFromModule() throws SchemaBuildException {

        SchemaRegistry schemaRegistry = initSchemaRegistry();

        assertEquals("urn:thirdparty-module3", SchemaRegistryUtil.getNamespaceFromModule(schemaRegistry, "module2", "tp3"));
        assertEquals("urn:module1", SchemaRegistryUtil.getNamespaceFromModule(schemaRegistry, "module2", "module1prefix"));
        assertEquals("urn:thirdparty-module2", SchemaRegistryUtil.getNamespaceFromModule(schemaRegistry, "module1", "tp2"));
    }

    private SchemaRegistry initSchemaRegistry() throws SchemaBuildException {

        SchemaRegistry registry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        List<YangTextSchemaSource> yangSources = new ArrayList<>();
        addYangSource(yangSources, "/schemaregistryutiltest/module1@2016-02-10.yang");
        addYangSource(yangSources, "/schemaregistryutiltest/module2@2016-02-10.yang");
        addYangSource(yangSources, "/schemaregistryutiltest/thirdparty-module1@2016-02-10.yang");
        addYangSource(yangSources, "/schemaregistryutiltest/thirdparty-module2@2016-02-10.yang");
        addYangSource(yangSources, "/schemaregistryutiltest/thirdparty-module3@2016-02-10.yang");
        registry.buildSchemaContext(yangSources, Collections.emptySet(), Collections.emptyMap());
        return registry;
    }

    @Test
    public void testGetChoiceParentSchemaNode() throws SchemaPathBuilderException {

        SchemaPath configuredDevicePropSchemaPath = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma," +
                "device-holder,device,connection-initiator-params,pma,configured-device-properties");
        SchemaPath connectionInitiatorParamsSchemaPath = fromString("(urn:org:bbf:pma?revision=2015-07-14)" +
                "pma,device-holder,device,connection-initiator-params");
        assertEquals(c_anvSchemaRegistry.getDataSchemaNode(connectionInitiatorParamsSchemaPath),
                SchemaRegistryUtil.getChoiceParentSchemaNode(configuredDevicePropSchemaPath, c_anvSchemaRegistry));

        SchemaPath deviceSchemaPath = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma," +
                "device-holder,device");

        assertNull(SchemaRegistryUtil.getChoiceParentSchemaNode(deviceSchemaPath, c_anvSchemaRegistry));
    }

    @Test
    public void testGetChildSchemaNode() {

        Document document = DocumentUtils.createDocument();
        Element rootElement = document.createElementNS("urn:org:bbf:pma", "pma");
        DataSchemaNode rootNode = SchemaRegistryUtil.getChildSchemaNode(rootElement, SchemaPath.ROOT, c_anvSchemaRegistry);
        assertEquals(c_anvSchemaRegistry.getDataSchemaNode(fromString("(urn:org:bbf:pma?revision=2015-07-14)pma")), rootNode);

        Element nonRootElement = document.createElementNS("urn:org:bbf:pma", "pma-list");
        DataSchemaNode nonRootNode = SchemaRegistryUtil.getChildSchemaNode(nonRootElement, fromString("(urn:org:bbf:pma?revision=2015-07-14)pma"), c_anvSchemaRegistry);
        assertEquals(c_anvSchemaRegistry.getDataSchemaNode(fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,pma-list")), nonRootNode);

        assertNull(SchemaRegistryUtil.getChildSchemaNode(document.createElementNS("urn:org:bbf:pma", "blah-blah"), fromString("(urn:org:bbf:pma?revision=2015-07-14)pma"), c_anvSchemaRegistry));
        DataSchemaNode actualNode = SchemaRegistryUtil.getChildSchemaNode("urn:test-module", "innerContainer", fromString("(urn:test-module?revision=2018-08-23)testContainer"), c_anvSchemaRegistry);
        assertEquals("innerContainer", actualNode.getQName().getLocalName());
    }

    @Test
    public void testGetSchemaPathFromNodeId() throws SchemaBuildException, SchemaPathBuilderException {

        SchemaRegistry registry = new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        List<YangTextSchemaSource> yangSources = new ArrayList<>();
        addYangSource(yangSources, "/referenceyangs/choice-case-module.yang");
        registry.buildSchemaContext(yangSources, Collections.emptySet(), Collections.emptyMap());
        ModelNodeId nodeId = new ModelNodeId();
        assertEquals(SchemaPath.ROOT, SchemaRegistryUtil.getSchemaPath(registry, nodeId));

        nodeId.addRdn(CONTAINER, "urn:choice-case-test", "device-manager");
        assertEquals(fromString("(urn:choice-case-test?revision=2015-12-14)device-manager"),
                SchemaRegistryUtil.getSchemaPath(registry, nodeId));

        nodeId.addRdn(CONTAINER, "urn:choice-case-test", "device-holder");
        assertEquals(fromString("(urn:choice-case-test?revision=2015-12-14)device-manager,device-holder"),
                SchemaRegistryUtil.getSchemaPath(registry, nodeId));
        nodeId.addRdn("name", "urn:choice-case-test", "OLT1");
        assertEquals(fromString("(urn:choice-case-test?revision=2015-12-14)device-manager,device-holder"),
                SchemaRegistryUtil.getSchemaPath(registry, nodeId));

        nodeId.addRdn(CONTAINER, "urn:choice-case-test", "device");
        assertEquals(fromString("(urn:choice-case-test?revision=2015-12-14)device-manager,device-holder,device"),
                SchemaRegistryUtil.getSchemaPath(registry, nodeId));
        nodeId.addRdn("device-id", "urn:choice-case-test", "ONT1");
        assertEquals(fromString("(urn:choice-case-test?revision=2015-12-14)device-manager,device-holder,device"),
                SchemaRegistryUtil.getSchemaPath(registry, nodeId));
    }

    @Test
    public void testGetSchemaPathsForNodes() {

        Document document = DocumentUtils.createDocument();
        Element rootElement = document.createElementNS("urn:org:bbf:pma", "pma");
        DataSchemaNode rootNode = SchemaRegistryUtil.getChildSchemaNode(rootElement, SchemaPath.ROOT, c_anvSchemaRegistry);
        Element nonRootElement = document.createElementNS("urn:org:bbf:pma", "pma-list");
        DataSchemaNode nonRootNode = SchemaRegistryUtil.getChildSchemaNode(nonRootElement,
                fromString("(urn:org:bbf:pma?revision=2015-07-14)pma"), c_anvSchemaRegistry);

        List<DataSchemaNode> dataSchemaNodeList = new ArrayList<>();
        dataSchemaNodeList.add(rootNode);
        dataSchemaNodeList.add(nonRootNode);

        List<SchemaPath> expectedSchemaPaths = new ArrayList<>();
        SchemaPath rootNodeSchemaPath = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma");
        SchemaPath nonRootNodeSchemaPath = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,pma-list");
        expectedSchemaPaths.add(rootNodeSchemaPath);
        expectedSchemaPaths.add(nonRootNodeSchemaPath);

        List<SchemaPath> actualSchemaPaths = (List<SchemaPath>) SchemaRegistryUtil.getSchemaPathsForNodes(dataSchemaNodeList);
        assertEquals(expectedSchemaPaths, actualSchemaPaths);
    }

    @Test
    public void testGetEffectiveParentNode() {

        SchemaPath caseSchemaPath = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device,connection-initiator-params,pma");
        DataSchemaNode dataSchemaNodePma = c_anvSchemaRegistry.getDataSchemaNode(caseSchemaPath);
        SchemaPath parentSchemaPathOfPma = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device");
        DataSchemaNode expectedDataSchemaNodeOfPma = c_anvSchemaRegistry.getDataSchemaNode(parentSchemaPathOfPma);
        DataSchemaNode actualDataSchemaNodeOfPma = SchemaRegistryUtil.getEffectiveParentNode(dataSchemaNodePma, c_anvSchemaRegistry);
        assertEquals(expectedDataSchemaNodeOfPma, actualDataSchemaNodeOfPma);

        SchemaPath nonCaseSchemaPath = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder");
        DataSchemaNode dataSchemaNodeDeviceHolder = c_anvSchemaRegistry.getDataSchemaNode(nonCaseSchemaPath);
        SchemaPath parentSchemaPathOfDeviceHolder = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma");
        DataSchemaNode expectedDataSchemaNodeOfDeviceHolder = c_anvSchemaRegistry.getDataSchemaNode(parentSchemaPathOfDeviceHolder);
        DataSchemaNode actualDataSchemaNodeOfDeviceHolder = SchemaRegistryUtil.getEffectiveParentNode(dataSchemaNodeDeviceHolder, c_anvSchemaRegistry);
        assertEquals(expectedDataSchemaNodeOfDeviceHolder, actualDataSchemaNodeOfDeviceHolder);

        SchemaPath schemaPath = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma");
        DataSchemaNode dataSchemaNode = c_anvSchemaRegistry.getDataSchemaNode(schemaPath);
        actualDataSchemaNodeOfPma = SchemaRegistryUtil.getEffectiveParentNode(dataSchemaNode, c_anvSchemaRegistry);
        assertNull(actualDataSchemaNodeOfPma);
    }

    @Test
    public void testHasDefaults() {

        SchemaPath schemaPathOfDefaultNode = fromString("(urn:test-module?revision=2018-08-23)testContainer,nodeWithDefault");
        DataSchemaNode dataSchemaNodeOfDefaultNode = c_anvSchemaRegistry.getDataSchemaNode(schemaPathOfDefaultNode);
        boolean resultOfDefaultNode = SchemaRegistryUtil.hasDefaults(dataSchemaNodeOfDefaultNode);
        assertTrue(resultOfDefaultNode);

        SchemaPath schemaPathOfNonDefaultNode = fromString("(urn:test-module?revision=2018-08-23)testContainer,nodeWithoutDefault");
        DataSchemaNode dataSchemaNodeOfNonDefaultNode = c_anvSchemaRegistry.getDataSchemaNode(schemaPathOfNonDefaultNode);
        boolean resultOfNonDefaultNode = SchemaRegistryUtil.hasDefaults(dataSchemaNodeOfNonDefaultNode);
        assertFalse(resultOfNonDefaultNode);
    }

    @Test
    public void testContainsWhen() {

        SchemaPath schemaPathOfPma = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device,connection-initiator-params,pma");
        DataSchemaNode dataSchemaNodeOfPma = c_anvSchemaRegistry.getDataSchemaNode(schemaPathOfPma);
        boolean resultOfPma = SchemaRegistryUtil.containsWhen(dataSchemaNodeOfPma);
        assertTrue(resultOfPma);

        SchemaPath schemaPathOfDeviceId = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device,device-id");
        DataSchemaNode dataSchemaNodeOfDeviceId = c_anvSchemaRegistry.getDataSchemaNode(schemaPathOfDeviceId);
        boolean resultOfDeviceId = SchemaRegistryUtil.containsWhen(dataSchemaNodeOfDeviceId);
        assertFalse(resultOfDeviceId);
    }

    @Test
    public void testGetYtss() {

        File firstFile = new File("/yangSchemaValidationTest/referenceyangs/ietf-yang-types.yang");
        File secondFile = new File("/yangSchemaValidationTest/referenceyangs/anv-alarms.yang");

        List<YangTextSchemaSource> expectedYangSources = new ArrayList<YangTextSchemaSource>();
        expectedYangSources.add(new FileYangSource(firstFile));
        expectedYangSources.add(new FileYangSource(secondFile));
        Set<File> files = new HashSet<>();
        files.add(firstFile);
        files.add(secondFile);
        List<YangTextSchemaSource> actualYangSources = SchemaRegistryUtil.getYtss(files);
        assertEquals(expectedYangSources.get(0).getIdentifier(), actualYangSources.get(0).getIdentifier());
        assertEquals(expectedYangSources.get(0).getType(), actualYangSources.get(0).getType());
        assertEquals(expectedYangSources.get(1).getIdentifier(), actualYangSources.get(1).getIdentifier());
        assertEquals(expectedYangSources.get(1).getType(), actualYangSources.get(1).getType());
    }

    @Test
    public void testGetSchemaPathForElement() {

        Document document = DocumentUtils.createDocument();
        Element pmaElement = document.createElementNS("urn:org:bbf:pma", "pma");

        List<SchemaPath> schemaPaths = new ArrayList<>();
        schemaPaths.add(fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder"));
        schemaPaths.add(fromString("(urn:org:bbf:pma?revision=2015-07-14)pma"));
        schemaPaths.add(fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device,device-id"));

        SchemaPath expectedSchemaPath = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma");
        SchemaPath actualSchemaPath = SchemaRegistryUtil.getSchemaPathForElement(pmaElement, schemaPaths);
        assertEquals(expectedSchemaPath, actualSchemaPath);

        Element pmaListElement = document.createElementNS("urn:org:bbf:pma", "pma-list");
        actualSchemaPath = SchemaRegistryUtil.getSchemaPathForElement(pmaListElement, schemaPaths);
        assertNull(actualSchemaPath);

        Element dummyElement = document.createElementNS("urn:org:bbf:pma", "dummy");
        actualSchemaPath = SchemaRegistryUtil.getSchemaPathForElement(dummyElement, schemaPaths);
        assertNull(actualSchemaPath);
    }

    @Test
    public void testGetSchemaPathFromCases() {

        List<SchemaPath> schemaPaths = new ArrayList<>();
        schemaPaths.add(fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device,connection-initiator-params"));
        schemaPaths.add(fromString("(urn:org:bbf:pma?revision=2015-07-14)pma"));

        List<DataSchemaNode> dataSchemaNodes = new ArrayList<>();
        dataSchemaNodes.add(c_anvSchemaRegistry.getDataSchemaNode(schemaPaths.get(0)));
        dataSchemaNodes.add(c_anvSchemaRegistry.getDataSchemaNode(schemaPaths.get(1)));

        List<SchemaPath> expectedSchemaPaths = new ArrayList<>();
        expectedSchemaPaths.add(fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device,connection-initiator-params,device,discovered-device-properties"));
        expectedSchemaPaths.add(fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device,connection-initiator-params,pma,configured-device-properties"));

        List<SchemaPath> actualSchemaPaths = SchemaRegistryUtil.getSchemaPathFromCases(dataSchemaNodes);
        assertEquals(expectedSchemaPaths, actualSchemaPaths);

        dataSchemaNodes.remove(0);
        actualSchemaPaths = SchemaRegistryUtil.getSchemaPathFromCases(dataSchemaNodes);
        assertEquals(0, actualSchemaPaths.size());
    }

    @Test
    public void testGetStateChildCases() {

        SchemaPath choiceSchemaPath = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device,connection-initiator-params");
        ChoiceSchemaNode choiceSchemaNode = (ChoiceSchemaNode) c_anvSchemaRegistry.getDataSchemaNode(choiceSchemaPath);

        SchemaPath caseSchemaPath = fromString("(urn:org:bbf:pma?revision=2015-07-14)pma,device-holder,device,connection-initiator-params,device,discovered-device-properties");
        DataSchemaNode caseSchemaNode = c_anvSchemaRegistry.getDataSchemaNode(caseSchemaPath);

        List<QName> expectedQNames = new ArrayList<>();
        expectedQNames.add(caseSchemaNode.getQName());

        List<QName> actualQNames = SchemaRegistryUtil.getStateChildCases(c_anvSchemaRegistry, choiceSchemaNode);
        assertEquals(expectedQNames, actualQNames);
    }

    @Test
    public void testGetStateLeafListAttributes() {

        SchemaPath nodeSchemaPath = fromString("(urn:test-module?revision=2018-08-23)testContainer");

        SchemaPath schemaPath = fromString("(urn:test-module?revision=2018-08-23)testContainer,state-leaf-list");
        DataSchemaNode leafListSchemaNode = c_anvSchemaRegistry.getDataSchemaNode(schemaPath);

        Set<QName> expectedStateLeafListAttributes = new HashSet<>();
        expectedStateLeafListAttributes.add(leafListSchemaNode.getQName());

        Set<QName> actualStateLeafListAttributes = SchemaRegistryUtil.getStateLeafListAttributes(nodeSchemaPath, c_anvSchemaRegistry);
        assertEquals(expectedStateLeafListAttributes, actualStateLeafListAttributes);
    }

    @Test
    public void testGetStateAttributes() {

        SchemaPath nodeSchemaPath = fromString("(urn:test-module?revision=2018-08-23)testContainer");

        List<SchemaPath> schemaPaths = new ArrayList<>();
        schemaPaths.add(fromString("(urn:test-module?revision=2018-08-23)testContainer,state-leaf-list"));
        schemaPaths.add(fromString("(urn:test-module?revision=2018-08-23)testContainer,nodeWithoutDefault"));

        List<DataSchemaNode> dataSchemaNodes = new ArrayList<>();
        dataSchemaNodes.add(c_anvSchemaRegistry.getDataSchemaNode(schemaPaths.get(0)));
        dataSchemaNodes.add(c_anvSchemaRegistry.getDataSchemaNode(schemaPaths.get(1)));

        Set<QName> expectedStateLeafListAttributes = new HashSet<>();
        expectedStateLeafListAttributes.add(dataSchemaNodes.get(0).getQName());
        expectedStateLeafListAttributes.add(dataSchemaNodes.get(1).getQName());

        Set<QName> actualStateLeafListAttributes = SchemaRegistryUtil.getStateAttributes(nodeSchemaPath, c_anvSchemaRegistry);
        assertEquals(expectedStateLeafListAttributes, actualStateLeafListAttributes);
    }

    @Test
    public void testGetStateChildLists() {

        SchemaPath nodeSchemaPath = fromString("(urn:test-module?revision=2018-08-23)testContainer");

        SchemaPath stateListNodeSchemaPath = fromString("(urn:test-module?revision=2018-08-23)testContainer,state-list");
        DataSchemaNode stateListSchemaNode = c_anvSchemaRegistry.getDataSchemaNode(stateListNodeSchemaPath);

        List<QName> expectedQNames = new ArrayList<>();
        expectedQNames.add(stateListSchemaNode.getQName());

        List<QName> actualQNames = SchemaRegistryUtil.getStateChildLists(c_anvSchemaRegistry, nodeSchemaPath);
        assertEquals(expectedQNames, actualQNames);
    }

    @Test
    public void testGetStateChildContainers() {

        SchemaPath nodeSchemaPath = fromString("(urn:test-module?revision=2018-08-23)testContainer");

        List<SchemaPath> stateContainerNodeSchemaPaths = new ArrayList<>();
        stateContainerNodeSchemaPaths.add(fromString("(urn:test-module?revision=2018-08-23)testContainer,stateContainer"));
        stateContainerNodeSchemaPaths.add(fromString("(urn:test-module?revision=2018-08-23)testContainer,connection,direct,directContainer"));

        List<DataSchemaNode> stateContainerSchemaNodes = new ArrayList<>();
        stateContainerSchemaNodes.add(c_anvSchemaRegistry.getDataSchemaNode(stateContainerNodeSchemaPaths.get(0)));
        stateContainerSchemaNodes.add(c_anvSchemaRegistry.getDataSchemaNode(stateContainerNodeSchemaPaths.get(1)));

        List<QName> expectedQNames = new ArrayList<>();
        expectedQNames.add(stateContainerSchemaNodes.get(0).getQName());
        expectedQNames.add(stateContainerSchemaNodes.get(1).getQName());

        List<QName> actualQNames = SchemaRegistryUtil.getStateChildContainers(c_anvSchemaRegistry, nodeSchemaPath);
        assertEquals(expectedQNames, actualQNames);
    }

    @Test
    public void testGetSchemaPathFromXpathExpression(){
        SchemaPath parentSchemaPath = fromString("(urn:test-module?revision=2018-08-23)testContainer");
        Expression ex = JXPathUtils.getExpression("state-list/name");
        LocationPath newPath = (LocationPath) ex;
        DataSchemaNode node = SchemaRegistryUtil.getSchemaNodeForXpath(c_anvSchemaRegistry, parentSchemaPath, newPath);
        assertNotNull(node);  
        assertFalse(node.isConfiguration());
        
        ex = JXPathUtils.getExpression("/nodeWithDefault");
        newPath = (LocationPath) ex;
        node = SchemaRegistryUtil.getSchemaNodeForXpath(c_anvSchemaRegistry, parentSchemaPath, newPath);
        assertNotNull(node);  
        assertTrue(node.isConfiguration());
        
        ex = JXPathUtils.getExpression("/nodeWithoutDefault");
        newPath = (LocationPath) ex;
        node = SchemaRegistryUtil.getSchemaNodeForXpath(c_anvSchemaRegistry, parentSchemaPath, newPath);
        assertNotNull(node);  
        assertFalse(node.isConfiguration());        
        
        ex = JXPathUtils.getExpression("/config-list/item");
        newPath = (LocationPath) ex;
        node = SchemaRegistryUtil.getSchemaNodeForXpath(c_anvSchemaRegistry, parentSchemaPath, newPath);
        assertNotNull(node);  
        assertTrue(node.isConfiguration());
        
        ex = JXPathUtils.getExpression("/config-list[item='test']");
        newPath = (LocationPath) ex;
        node = SchemaRegistryUtil.getSchemaNodeForXpath(c_anvSchemaRegistry, parentSchemaPath, newPath);
        assertNotNull(node);  
    }
    
    @Test
    public void testGetAbsolutePath() {

        String elementXML = "<testContainer xmlns=\"urn:test-module\"> \n" +
                "    <nodeWithDefault>defaultNode</nodeWithDefault>\n" +
                "    <state-list>\n" +
                "        <name>connection</name>\n" +
                "    </state-list>\n" +
                "</testContainer>";

        Node node = TestUtil.transformToElement(elementXML);
        SchemaPath schemaPath = fromString("(urn:test-module?revision=2018-08-23)testContainer,nodeWithDefault");
        String actualAbsolutePath = SchemaRegistryUtil.getAbsolutePath(node.getChildNodes().item(1), c_anvSchemaRegistry.getDataSchemaNode(schemaPath), c_anvSchemaRegistry);
        String expectedAbsolutePath = "/test:testContainer/test:nodeWithDefault";
        assertEquals(expectedAbsolutePath, actualAbsolutePath);

        schemaPath = fromString("(urn:test-module?revision=2018-08-23)testContainer");
        actualAbsolutePath = SchemaRegistryUtil.getAbsolutePath(node, c_anvSchemaRegistry.getDataSchemaNode(schemaPath), c_anvSchemaRegistry);
        expectedAbsolutePath = "/test:testContainer";
        assertEquals(expectedAbsolutePath, actualAbsolutePath);

        schemaPath = fromString("(urn:test-module?revision=2018-08-23)testContainer,state-list");
        actualAbsolutePath = SchemaRegistryUtil.getAbsolutePath(node.getChildNodes().item(3), c_anvSchemaRegistry.getDataSchemaNode(schemaPath), c_anvSchemaRegistry);
        expectedAbsolutePath = "/test:testContainer/test:state-list[test:name = 'connection']";
        assertEquals(expectedAbsolutePath, actualAbsolutePath);

        actualAbsolutePath = SchemaRegistryUtil.getAbsolutePath(node, null, c_anvSchemaRegistry);
        assertEquals("", actualAbsolutePath);
    }

    @Test
    public void testCreateSchemaRegistry() throws SchemaBuildException {

        List<String> resourceDir = new ArrayList<>();
        resourceDir.add(Paths.get(SchemaRegistryUtilTest.class.getResource("/yangSchemaValidationTest/referenceyangs/anvyangs/" +
                "alu-device-plugs@2015-07-14.yang").getPath()).getParent().toString());
        SchemaRegistry actualSchemaRegistry = SchemaRegistryUtil.createSchemaRegistry(resourceDir, Collections.emptySet(),
                Collections.emptyMap(), true, new NoLockService());
        assertEquals(23, actualSchemaRegistry.getAllModules().size());

        resourceDir.add(Paths.get(SchemaRegistryUtilTest.class.getResource("/schemaregistryutiltest/test-module@2018-08-23.yang")
                .getPath()).getParent().toString());
        actualSchemaRegistry = SchemaRegistryUtil.createSchemaRegistry(resourceDir, Collections.emptySet(), Collections.emptyMap(),
                true, new NoLockService());
        assertEquals(30, actualSchemaRegistry.getAllModules().size());
        assertEquals(Optional.empty(), actualSchemaRegistry.getModule("dummy"));
    }

    @Test
    public void testGetErrorPath() {

        String elementXML = "<testContainer xmlns=\"urn:test-module\">\n" +
                "    <nodeWithDefault>defaultNode</nodeWithDefault>\n" +
                "    <nodeWithoutDefault>defaultNode</nodeWithoutDefault>\n" +
                "    <state-list>\n" +
                "        <name>connection</name>\n" +
                "    </state-list>\n" +
                "</testContainer>";
        Node element = TestUtil.transformToElement(elementXML);
        String childElementXML = "<nodeWithDefault>defaultNode</nodeWithDefault>";
        Element childElement = TestUtil.transformToElement(childElementXML);
        SchemaPath schemaPath = fromString("(urn:test-module?revision=2018-08-23)testContainer");
        Map<String, String> prefixToNSMap = new HashMap<>();
        prefixToNSMap.put("test", "urn:test-module");
        Pair<String, Map<String, String>> expectedErrorPath = new Pair<String, Map<String, String>>("/test:testContainer/nodeWithDefault", prefixToNSMap);
        Pair<String, Map<String, String>> actualErrorPath = SchemaRegistryUtil.getErrorPath(element, c_anvSchemaRegistry.getDataSchemaNode(schemaPath),
                c_anvSchemaRegistry, childElement);
        assertEquals(expectedErrorPath, actualErrorPath);

        childElementXML = "<name>GFast</name>";
        childElement = TestUtil.transformToElement(childElementXML);
        schemaPath = fromString("(urn:test-module?revision=2018-08-23)testContainer,state-list");
        expectedErrorPath = new Pair<String, Map<String, String>>("/test:testContainer/test:state-list[test:name='connection']/name", prefixToNSMap);
        actualErrorPath = SchemaRegistryUtil.getErrorPath(element.getChildNodes().item(5), c_anvSchemaRegistry.getDataSchemaNode(schemaPath),
                c_anvSchemaRegistry, childElement);
        assertEquals(expectedErrorPath, actualErrorPath);

        expectedErrorPath = new Pair<String, Map<String, String>>("/test:testContainer/test:nodeWithDefault", prefixToNSMap);
        schemaPath = fromString("(urn:test-module?revision=2018-08-23)testContainer,nodeWithDefault");
        actualErrorPath = SchemaRegistryUtil.getErrorPath("/test:testContainer", element, c_anvSchemaRegistry.getDataSchemaNode(schemaPath), c_anvSchemaRegistry);
        assertEquals(expectedErrorPath, actualErrorPath);
    }

    @Test
    public void testIsMountPointEnabled() {

        assertTrue(SchemaRegistryUtil.isMountPointEnabled());
    }

    @Test
    public void testGetSchemaRegistry() {

        ModelNode modelNode = mock(ModelNode.class);
        SchemaRegistry registry = mock(SchemaRegistry.class);
        SchemaRegistry anotherRegistry = mock(SchemaRegistry.class);
        when(modelNode.hasSchemaMount()).thenReturn(true);
        when(modelNode.getMountRegistry()).thenReturn(anotherRegistry);
        SchemaRegistry actualRegistry = SchemaRegistryUtil.getSchemaRegistry(modelNode, registry);
        assertEquals(anotherRegistry, actualRegistry);

        when(modelNode.hasSchemaMount()).thenReturn(false);
        when(modelNode.getParent()).thenReturn(mock(ModelNode.class));
        when(modelNode.getParent().hasSchemaMount()).thenReturn(true);
        actualRegistry = SchemaRegistryUtil.getSchemaRegistry(modelNode, registry);
        assertEquals(anotherRegistry, actualRegistry);

        when(modelNode.hasSchemaMount()).thenReturn(false);
        when(modelNode.getParent()).thenReturn(mock(ModelNode.class));
        when(modelNode.getParent().hasSchemaMount()).thenReturn(false);
        actualRegistry = SchemaRegistryUtil.getSchemaRegistry(modelNode, registry);
        assertEquals(registry, actualRegistry);
    }

    @Test
    public void testGetSchemaPath() {

        String xPath = "/testContainer/nodeWithDefault";
        String defaultNs = "urn:test-module";
        String moduleName = "test-module";
        SchemaPath expectedSchemaPath = fromString("(urn:test-module?revision=2018-08-23)testContainer, nodeWithDefault");
        SchemaPath actualSchemaPath = SchemaRegistryUtil.getSchemaPath(c_anvSchemaRegistry, xPath, defaultNs, moduleName);
        assertEquals(expectedSchemaPath, actualSchemaPath);

        xPath = "/test:testContainer/test:nodeWithoutDefault";
        expectedSchemaPath = fromString("(urn:test-module?revision=2018-08-23)testContainer, nodeWithoutDefault");
        actualSchemaPath = SchemaRegistryUtil.getSchemaPath(c_anvSchemaRegistry, xPath, defaultNs, moduleName);
        assertEquals(expectedSchemaPath, actualSchemaPath);

        xPath = "/testContainer/current()";
        actualSchemaPath = SchemaRegistryUtil.getSchemaPath(c_anvSchemaRegistry, xPath, defaultNs, moduleName);
        assertNull(actualSchemaPath);
    }

    @Test
    public void testGetMountProviderInfo() {
        
        SchemaMountRegistry schemaMountRegistry = new SchemaMountRegistryImpl();
        ((SchemaRegistryImpl) c_anvSchemaRegistry).setSchemaMountRegistry(schemaMountRegistry);
        String elementXML = "<testContainer xmlns=\"urn:test-module\">\n" +
                "    <nodeWithDefault>defaultNode</nodeWithDefault>\n" +
                "    <nodeWithoutDefault>defaultNode</nodeWithoutDefault>\n" +
                "</testContainer>";
        Element request = TestUtil.transformToElement(elementXML);
        MountProviderInfo actualValue = SchemaRegistryUtil.getMountProviderInfo(request, c_anvSchemaRegistry);
        assertNull(actualValue);
    }

    @Test
    public void testIsLeafListOrderedByUser(){
        SchemaPath container1ListSP = fromString("(" + NESTED_CHOICE_NS_REVISION + ")" +
                "root-container");

        assertFalse(SchemaRegistryUtil.isLeafListOrderedByUser(QName.create(NESTED_CHOICE_NS, NESTED_CHOICE_REVISION,"level1-leaf-list1"),
                container1ListSP,c_anvSchemaRegistry));

        assertTrue(SchemaRegistryUtil.isLeafListOrderedByUser(QName.create(NESTED_CHOICE_NS, NESTED_CHOICE_REVISION,"level1-leaf-list2"),
                container1ListSP,c_anvSchemaRegistry));

    }
}
