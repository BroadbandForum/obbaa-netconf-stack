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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.YangLibraryChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.listener.YangLibraryChangeNotificationListener;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.LockServiceException;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.util.ModuleIdentifierImpl;

import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;

/**
 * Created by keshava on 11/18/15.
 */
public class SchemaRegistryImplTest {
    public static final String TOASTER_COMPONENT = "toaster-component";
    SchemaRegistryImpl m_schemaRegistry;
    static YangTextSchemaSource c_unprefixedpathmoduleYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest
            .class.getResource("/yangSchemaValidationTest/referenceyangs/unprefixedpathmodule.yang"));
    static YangTextSchemaSource c_jukeboxYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
            .getResource("/yangSchemaValidationTest/referenceyangs/example-jukebox@2014-07-03.yang"));
    static YangTextSchemaSource c_testYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
            .getResource("/yangSchemaValidationTest/referenceyangs/example-test@2017-14-02.yang"));
    static YangTextSchemaSource c_testSubmoduleYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
            .getResource("/yangSchemaValidationTest/referenceyangs/example-test-submodule@2017-14-02.yang"));
    static YangTextSchemaSource c_shortenedJukeboxYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest
            .class.getResource("/yangSchemaValidationTest/referenceyangs/shortened-example-jukebox@2014-07-03.yang"));
    static YangTextSchemaSource c_ietfInetTypesFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
            .getResource("/yangSchemaValidationTest/referenceyangs/ietf-inet-types.yang"));
    static YangTextSchemaSource c_ietfRestconfFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
            .getResource("/yangSchemaValidationTest/referenceyangs/ietf-restconf.yang"));
    static YangTextSchemaSource c_ietfYangTypesFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
            .getResource("/yangSchemaValidationTest/referenceyangs/ietf-yang-types.yang"));
    static YangTextSchemaSource c_toasterYangTypesFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
            .getResource("/yangSchemaValidationTest/referenceyangs/toaster@2009-11-20.yang"));
    static YangTextSchemaSource c_jukeboxPlugYangTypesFile = YangParserUtil.getYangSource(SchemaRegistryImplTest
            .class.getResource("/yangSchemaValidationTest/referenceyangs/jukebox-plug@2015-07-03.yang"));
    static YangTextSchemaSource c_jukeboxPlug1YangTypesFile = YangParserUtil.getYangSource(SchemaRegistryImplTest
            .class.getResource("/yangSchemaValidationTest/referenceyangs/jukebox-plug1@2015-07-03.yang"));
    static YangTextSchemaSource c_jukeboxPlug2YangTypesFile = YangParserUtil.getYangSource(SchemaRegistryImplTest
            .class.getResource("/yangSchemaValidationTest/referenceyangs/jukebox-plug2@2015-07-03.yang"));
    static YangTextSchemaSource c_testYang11File = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
            .getResource("/yangSchemaValidationTest/referenceyangs/example-test-11.yang"));
    public static final SchemaPath MAKE_TOAST_PATH = SchemaPath.create(true, QName.create("http://netconfcentral" +
            ".org/ns/toaster", "2009-11-20", "make-toast"));
    public static final String JB_NS = "http://example.com/ns/example-jukebox";
    private static final String IETF_INET_TYPES_NS = "urn:ietf:params:xml:ns:yang:ietf-inet-types";
    public static final String JB_REVISION = "2014-07-03";
    public static final SchemaPath JUKEBOX_PATH = SchemaPath.create(true, QName.create(JB_NS, JB_REVISION, "jukebox"));
    public static final SchemaPath PLAY_PATH = SchemaPath.create(true, QName.create(JB_NS, JB_REVISION, "play"));
    private static final String MODULE_SET_ID = "ebb54120fc40d0001558bb6c6c738396045ea4ddcac4202808c9c1aa0528bc25";
    private static final String EXAMPLE_YANG11_FILE_CAPS = "http://example" +
            ".com/ns/example-test-11?module=example-test-11&revision=2018-02-02";
    private static final String YANG_LIBRARY_CAP_WITH_MODULESETID =
            "urn:ietf:params:netconf:capability:yang-library:1.0?revision=2016-04-09&module-set-id" +
                    "=71715043fbc8a2480915d36d9a1383029f41c5431aacb97d85350f7b04da6074";
    private static Logger LOGGER = Logger.getLogger(SchemaRegistry.class);
    private YangLibraryChangeNotificationListener m_listener = mock(YangLibraryChangeNotificationListener.class);
    private static final String COMPONENT_ID1 = "component-id1";
    private static final String AUGMENTED_PATH = "/schemapath1";

    @Before
    public void setUp() throws SchemaBuildException {
        m_schemaRegistry = Mockito.spy(new SchemaRegistryImpl(Collections.<YangTextSchemaSource>emptyList(), false,
                new ReadWriteLockServiceImpl()));
        RequestScope.resetScope();
    }

    @Test
    public void testLookupQName() throws SchemaBuildException, LockServiceException {
        ReadWriteLockServiceImpl readWriteLockService = spy(new ReadWriteLockServiceImpl());
        m_schemaRegistry.setReadWriteLockService(readWriteLockService);
        m_schemaRegistry.buildSchemaContext(getYangFiles());
        verify(readWriteLockService).executeWithWriteLock(Mockito.any());
        verify(readWriteLockService).executeWithReadLock(Mockito.any());

        assertEquals(JUKEBOX_PATH.getLastComponent(), m_schemaRegistry.lookupQName(JB_NS, "jukebox"));
        assertEquals("artist", m_schemaRegistry.lookupQName(JB_NS, "artist").getLocalName());
        assertEquals(JB_NS, m_schemaRegistry.lookupQName(JB_NS, "artist").getNamespace().toString());
        assertEquals(JB_REVISION, m_schemaRegistry.lookupQName(JB_NS, "artist").getFormattedRevision());
        verify(readWriteLockService, times(5)).executeWithReadLock(Mockito.any());
    }

    private List<YangTextSchemaSource> getYangFiles() {
        return Arrays.asList(c_jukeboxYangFile, c_ietfInetTypesFile, c_ietfRestconfFile, c_ietfYangTypesFile);
    }

    private List<YangTextSchemaSource> getDeviationYangFiles() {
        YangTextSchemaSource a = YangParserUtil
                .getYangSource(SchemaRegistryImplTest.class.getResource("/deviationTestYangs/bbf-if-type.yang"));
        YangTextSchemaSource b = YangParserUtil
                .getYangSource(SchemaRegistryImplTest.class.getResource("/deviationTestYangs/bbf-xdsl.yang"));
        YangTextSchemaSource c = YangParserUtil
                .getYangSource(SchemaRegistryImplTest.class.getResource("/deviationTestYangs/ietf-interfaces.yang"));
        YangTextSchemaSource d = YangParserUtil
                .getYangSource(SchemaRegistryImplTest.class.getResource("/deviationTestYangs/ietf-yang-types.yang"));
        YangTextSchemaSource e = YangParserUtil
                .getYangSource(SchemaRegistryImplTest.class.getResource("/deviationTestYangs/test-xdsl-dev.yang"));

        List<YangTextSchemaSource> schemaSources = new ArrayList<>();

        schemaSources.add(a);
        schemaSources.add(b);
        schemaSources.add(c);
        schemaSources.add(d);
        schemaSources.add(e);

        return schemaSources;
    }

    @Test
    public void testGetAllYangTextSchemaSources_NoDuplicateYangSources() throws SchemaBuildException {
        assertEquals(0, m_schemaRegistry.getRootDataSchemaNodes().size());

        List<YangTextSchemaSource> schemaSources = new ArrayList<>();
        schemaSources.add(c_jukeboxYangFile);
        schemaSources.add(c_jukeboxYangFile);
        schemaSources.add(c_jukeboxYangFile);
        schemaSources.add(c_ietfInetTypesFile);

        m_schemaRegistry.buildSchemaContext(schemaSources);
        Map<SourceIdentifier, YangTextSchemaSource> data = m_schemaRegistry.getAllYangTextSchemaSources();
        assertEquals(2, data.size());
        for (Entry<SourceIdentifier, YangTextSchemaSource> entry : data.entrySet()) {
            if (entry.getKey().getName().equals("example-jukebox")) {
                assertEquals("2014-07-03", entry.getKey().getRevision());
            }
            if (entry.getKey().getName().equals("ietf-inet-types")) {
                assertEquals("2013-07-15", entry.getKey().getRevision());
            }
        }
    }

    @Test
    public void testGetAllModuleAndSubmoduleIdentifiers() throws SchemaBuildException, URISyntaxException,
            ParseException {
        assertEquals(0, m_schemaRegistry.getRootDataSchemaNodes().size());

        List<YangTextSchemaSource> schemaSources = new ArrayList<>();
        schemaSources.add(c_testYangFile);
        schemaSources.add(c_testSubmoduleYangFile);
        schemaSources.add(c_ietfInetTypesFile);

        m_schemaRegistry.buildSchemaContext(schemaSources);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        ModuleIdentifier exampleTestId = ModuleIdentifierImpl.create("example-test-submodule", Optional.of(new URI
                        ("http://example.com/ns/example-test")),
                Optional.of(sdf.parse("2017-14-02")));
        Set<ModuleIdentifier> ids = m_schemaRegistry.getAllModuleIdentifiers();
        // not including submodules
        assertEquals(2, ids.size());
        assertFalse(ids.contains(exampleTestId));

        ids = m_schemaRegistry.getAllModuleAndSubmoduleIdentifiers();
        // including submodules
        assertEquals(3, ids.size());
        assertTrue(ids.contains(exampleTestId));
    }

    @Test
    public void testGetAllModules() throws SchemaBuildException {
        List<YangTextSchemaSource> schemaSources = new ArrayList<>();
        schemaSources.add(c_testYangFile);
        schemaSources.add(c_testSubmoduleYangFile);
        schemaSources.add(c_ietfInetTypesFile);

        m_schemaRegistry.buildSchemaContext(schemaSources);
        Set<Module> modules = m_schemaRegistry.getAllModules();
        //Not including submodules
        assertEquals(2, modules.size());
    }

    @Test
    public void testBuildSchemaContext() throws SchemaBuildException {
        assertEquals(0, m_schemaRegistry.getRootDataSchemaNodes().size());

        m_schemaRegistry.buildSchemaContext(getYangFiles());
        assertEquals(1, m_schemaRegistry.getRootDataSchemaNodes().size());
        assertEquals(JUKEBOX_PATH.getLastComponent(), m_schemaRegistry.getRootDataSchemaNodes().iterator().next()
                .getQName());
    }

    @Test
    public void testBuildSchemaContextWithUnPrefixedPathInGrouping() throws SchemaBuildException {
        assertEquals(0, m_schemaRegistry.getRootDataSchemaNodes().size());

        try {
            m_schemaRegistry.buildSchemaContext(getYangFilesWithUnPrefixedPath());
            fail("Excepted SchemaBuildException was not thrown");
        } catch (Exception e) {
            String actualErrorMessage = e.getCause().getMessage();
            String expectedErrorMessage = "Unprefixed Path : upmod:test-container-one/name-one present in the node : " +
                    "groupContainerLeaf1-a-a-a "
                    + "of the grouping : (http://example.com/ns/unprefixedpathmodule?revision=2017-06-15)group1-a-a";
            assertEquals(expectedErrorMessage, actualErrorMessage);
        }
        assertEquals(0, m_schemaRegistry.getRootDataSchemaNodes().size());
    }

    private List<YangTextSchemaSource> getYangFilesWithUnPrefixedPath() {
        return Arrays.asList(c_unprefixedpathmoduleYangFile);
    }

    @Test
    public void testBuildSchemaContextWithPredicateOfAllFeatures() throws SchemaBuildException {
        QName test_contA = QName.create("feature-testNS", "1970-01-01", "test-container-a");
        YangTextSchemaSource featuretest_yang = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource
                ("/yangSchemaValidationTest/referenceyangs/feature-test.yang"));
        SchemaRegistry schemaRegistry = Mockito.spy(new SchemaRegistryImpl(Arrays.asList(featuretest_yang), null,
                null, new ReadWriteLockServiceImpl()));
        assertEquals(1, schemaRegistry.getRootDataSchemaNodes().size());
        assertEquals(test_contA, schemaRegistry.getRootDataSchemaNodes().iterator().next().getQName());
    }

    @Test
    public void testBuildSchemaContextOfFeatures() throws SchemaBuildException {
        QName test_contA = QName.create("feature-testNS", "1970-01-01", "test-container-a");
        QName test_feature1 = QName.create("feature-testNS", "1970-01-01", "test-feature-1");
        QName test_feature2 = QName.create("feature-testNS", "1970-01-01", "test-feature-2");
        Set<QName> supportedQNames = new HashSet<>();
        supportedQNames.add(test_feature2);

        YangTextSchemaSource featuretest_yang = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource
                ("/yangSchemaValidationTest/referenceyangs/feature-test.yang"));
        SchemaRegistry schemaRegistry = Mockito.spy(new SchemaRegistryImpl(Arrays.asList(featuretest_yang),
                supportedQNames, Collections.emptyMap(), new ReadWriteLockServiceImpl()));
        assertTrue(schemaRegistry.getRootDataSchemaNodes().isEmpty());

        supportedQNames.add(test_feature1);
        schemaRegistry = Mockito.spy(new SchemaRegistryImpl(Arrays.asList(featuretest_yang), supportedQNames,
                Collections.emptyMap(), new ReadWriteLockServiceImpl()));
        assertEquals(1, schemaRegistry.getRootDataSchemaNodes().size());
        assertEquals(test_contA, schemaRegistry.getRootDataSchemaNodes().iterator().next().getQName());

    }

    @Test
    public void testBuildSchemaRegistryWithPredicateOfAllFeatures() throws SchemaBuildException {
        QName test_contA = QName.create("feature-testNS", "1970-01-01", "test-container-a");
        String test_feature1 = "(feature-testNS?revision=1970-01-01)test-feature-1";
        String test_feature2 = "(feature-testNS?revision=1970-01-01)test-feature-2";
        Set<String> supportedFeatures = new HashSet<>();
        supportedFeatures.add(test_feature2);
        SchemaRegistry schemaRegistry = SchemaRegistryImpl.buildSchemaRegistry(Arrays.asList
                ("/yangSchemaValidationTest/referenceyangs/feature-test.yang"), supportedFeatures, Collections
                .emptyMap(), new ReadWriteLockServiceImpl());
        assertTrue(schemaRegistry.getRootDataSchemaNodes().isEmpty());

        supportedFeatures.add(test_feature1);
        schemaRegistry = SchemaRegistryImpl.buildSchemaRegistry(Arrays.asList
                ("/yangSchemaValidationTest/referenceyangs/feature-test.yang"), supportedFeatures, Collections
                .emptyMap(), new ReadWriteLockServiceImpl());
        assertEquals(1, schemaRegistry.getRootDataSchemaNodes().size());
        assertEquals(test_contA, schemaRegistry.getRootDataSchemaNodes().iterator().next().getQName());

        supportedFeatures = null;
        schemaRegistry = SchemaRegistryImpl.buildSchemaRegistry(Arrays.asList
                ("/yangSchemaValidationTest/referenceyangs/feature-test.yang"), supportedFeatures, Collections
                .emptyMap(), new ReadWriteLockServiceImpl());
        assertEquals(1, schemaRegistry.getRootDataSchemaNodes().size());
        assertEquals(test_contA, schemaRegistry.getRootDataSchemaNodes().iterator().next().getQName());
    }

    @Test
    public void testGetRpcDefinitions() throws SchemaBuildException {
        m_schemaRegistry.buildSchemaContext(getYangFiles());

        assertEquals(1, m_schemaRegistry.getRpcDefinitions().size());
        assertEquals("play", m_schemaRegistry.getRpcDefinitions().iterator().next().getQName().getLocalName());
    }

    @Test
    public void testGetRpcDefinitionByQName() throws SchemaBuildException {
        m_schemaRegistry.buildSchemaContext(getYangFiles());

        assertEquals(1, m_schemaRegistry.getRpcDefinitions().size());
        makeSurePlayRpcExists();
    }

    public void makeSurePlayRpcExists() {
        RpcDefinition playRpcDefinition = m_schemaRegistry.getRpcDefinition(PLAY_PATH);
        assertEquals(PLAY_PATH.getLastComponent(), playRpcDefinition.getQName());
        assertEquals("input", playRpcDefinition.getInput().getQName().getLocalName());
        assertEquals(2, playRpcDefinition.getInput().getChildNodes().size());
    }

    @Test
    public void testLoadSchemaContextWithoutAugmentation() throws SchemaBuildException {
        m_schemaRegistry.buildSchemaContext(getYangFiles());
        assertEquals(1, m_schemaRegistry.getRootDataSchemaNodes().size());

        m_schemaRegistry.loadSchemaContext(TOASTER_COMPONENT, Arrays.asList(c_toasterYangTypesFile), null,
                Collections.emptyMap());
        //there should toaster now
        assertEquals(2, m_schemaRegistry.getRootDataSchemaNodes().size());
        makeSurePlayRpcExists();
        makeSureMakeToastRpcExists();

    }

    @Test
    public void testUnloadSchemaContextWithoutAugmentation() throws SchemaBuildException {
        m_schemaRegistry.buildSchemaContext(getYangFiles());
        m_schemaRegistry.loadSchemaContext(TOASTER_COMPONENT, Arrays.asList(c_toasterYangTypesFile), null,
                Collections.emptyMap());
        assertEquals(2, m_schemaRegistry.getRootDataSchemaNodes().size());

        try {
            //try to load it again with same component id
            m_schemaRegistry.loadSchemaContext(TOASTER_COMPONENT, Arrays.asList(c_toasterYangTypesFile), null, null);
            fail("Excepted SchemaBuildException was not thrown");
        } catch (SchemaBuildException e) {
            //ok. good
        }
        m_schemaRegistry.unloadSchemaContext(TOASTER_COMPONENT, Collections.emptyMap());
        assertEquals(1, m_schemaRegistry.getRootDataSchemaNodes().size());


    }

    @Test
    public void testLoadSchemaContextWithAugmentation() throws SchemaBuildException {
        m_schemaRegistry.buildSchemaContext(getYangFiles());
        SchemaPath playerPath = SchemaPath.create(true, QName.create(JB_NS, JB_REVISION, "jukebox"),
                QName.create(JB_NS, JB_REVISION, "player"));

        //make sure no augmentation before
        assertEquals(1, ((DataNodeContainer) m_schemaRegistry.getDataSchemaNode(playerPath)).getChildNodes().size());
        m_schemaRegistry.loadSchemaContext("jukebox-plug", Arrays.asList(c_jukeboxPlugYangTypesFile), null,
                Collections.emptyMap());
        assertEquals(1, m_schemaRegistry.getRootDataSchemaNodes().size());
        //make sure it is augmented
        assertEquals(2, ((DataNodeContainer) m_schemaRegistry.getDataSchemaNode(playerPath)).getChildNodes().size());

    }

    @Test
    public void testLoadSchemaContextWithDeviations() throws SchemaBuildException {

        List<YangTextSchemaSource> schemaSources = new ArrayList<>();
        QName line = QName.create("urn:broadband-forum-org:yang:bbf-xdsl", "2016-01-25", "line");
        QName supported_node = QName.create("urn:broadband-forum-org:yang:bbf-xdsl", "2016-01-25", "supported-mode");
        QName interfaces = QName.create("urn:ietf:params:xml:ns:yang:ietf-interfaces", "2014-05-08", "interfaces");
        QName interfaceQName = QName.create("urn:ietf:params:xml:ns:yang:ietf-interfaces", "2014-05-08", "interface");

        schemaSources.addAll(getYangFiles());
        schemaSources.addAll(getDeviationYangFiles());
        m_schemaRegistry.buildSchemaContext(schemaSources);
        SchemaPath supportedNodePath = SchemaPath.create(true, interfaces, interfaceQName, line, supported_node);

        QName moduleQName = QName.create("urn:broadband-forum-org:yang:bbf-xdsl", "2016-01-25", "bbf-xdsl");
        QName deviationModuleQName = QName.create("urn:xxxxx-org:yang:test-xdsl-dev", "2017-07-05", "test-xdsl-dev");

        Set<QName> moduleDeviations = new HashSet<>();
        moduleDeviations.add(deviationModuleQName);

        Map<QName, Set<QName>> supportedDeviations = new java.util.HashMap<>();
        supportedDeviations.put(moduleQName, moduleDeviations);

        m_schemaRegistry.loadSchemaContext("jukebox-plug", Arrays.asList(c_jukeboxPlugYangTypesFile), null,
                supportedDeviations);
        LeafListSchemaNode leafListSchemaNode = ((LeafListSchemaNode) (m_schemaRegistry
                .getDataSchemaNode(supportedNodePath)));

        // Once deviation is applied , maxElements value becomes 2 , else it is null
        assertEquals((Integer) 2, leafListSchemaNode.getConstraints().getMaxElements());
    }

    @Test
    public void testLoadSchemaContextWithMultiPlugDeviations() throws SchemaBuildException {

        List<YangTextSchemaSource> schemaSources = new ArrayList<>();
        QName line = QName.create("urn:broadband-forum-org:yang:bbf-xdsl", "2016-01-25", "line");
        QName supported_node = QName.create("urn:broadband-forum-org:yang:bbf-xdsl", "2016-01-25", "supported-mode");
        QName interfaces = QName.create("urn:ietf:params:xml:ns:yang:ietf-interfaces", "2014-05-08", "interfaces");
        QName interfaceQName = QName.create("urn:ietf:params:xml:ns:yang:ietf-interfaces", "2014-05-08", "interface");

        schemaSources.addAll(getYangFiles());
        schemaSources.addAll(getDeviationYangFiles());

        SchemaPath supportedNodePath = SchemaPath.create(true, interfaces, interfaceQName, line, supported_node);

        QName moduleQName = QName.create("urn:broadband-forum-org:yang:bbf-xdsl", "2016-01-25", "bbf-xdsl");
        QName deviationModuleQName = QName.create("urn:xxxxx-org:yang:test-xdsl-dev", "2017-07-05", "test-xdsl-dev");

        Set<QName> moduleDeviations = new HashSet<>();
        moduleDeviations.add(deviationModuleQName);

        Map<QName, Set<QName>> supportedDeviations = new java.util.HashMap<>();
        supportedDeviations.put(moduleQName, moduleDeviations);

        m_schemaRegistry.buildSchemaContext(schemaSources);
        LeafListSchemaNode leafListSchemaNode = null;

        // plug1 is updating the schemaRegistry
        m_schemaRegistry.loadSchemaContext("jukebox-plug1",
                Arrays.asList(c_jukeboxPlug1YangTypesFile), null, supportedDeviations);
        leafListSchemaNode = ((LeafListSchemaNode) (m_schemaRegistry.getDataSchemaNode(supportedNodePath)));

		/*
         * Once deviation is applied , maxElements value becomes 2 , else it is
		 * null Deviations are applied by plug1
		 */
        assertEquals((Integer) 2, leafListSchemaNode.getConstraints().getMaxElements());

        Map<QName, Set<QName>> plug2SupportedDeviations = new java.util.HashMap<>();

        // plug2 is updating the schemaRegistry

        // Deviations are not getting altered by plug2
        m_schemaRegistry.loadSchemaContext("jukebox-plug2",
                Arrays.asList(c_jukeboxPlug2YangTypesFile), null, plug2SupportedDeviations);
        leafListSchemaNode = ((LeafListSchemaNode) (m_schemaRegistry.getDataSchemaNode(supportedNodePath)));

        // Deviations applied via plug1 are not altered by plug2
        assertEquals((Integer) 2, leafListSchemaNode.getConstraints().getMaxElements());
    }

    @Test
    public void testFindChildWithDataSchemaNode() throws SchemaBuildException {
        DataSchemaNode m_dataSchemaNode = mock(DataSchemaNode.class);
        QName qname = QName.create("test", "test2");
        m_schemaRegistry.buildSchemaContext(getAnvYangFiles());
        m_schemaRegistry.loadSchemaContext("G.fast-plug", getGfastYangFiles(), null, Collections.emptyMap());
        SchemaPath deviceSchemaPath = new SchemaPathBuilder().withNamespace("urn:org:bbf:pma").withRevision
                ("2015-07-14")
                .appendLocalName("pma").appendLocalName("device-holder").appendLocalName("device").build();
        assertNotNull(m_schemaRegistry.getDataSchemaNode(deviceSchemaPath));
        DataSchemaNode dataSchemaNode = mock(DataSchemaNode.class, withSettings().extraInterfaces(DataNodeContainer
                .class));
        Collection<DataSchemaNode> childNodes = new ArrayList<>();
        m_dataSchemaNode = mock(DataSchemaNode.class);
        childNodes.add(m_dataSchemaNode);
        when(((DataNodeContainer) dataSchemaNode).getChildNodes()).thenReturn(childNodes);
        when(m_dataSchemaNode.getQName()).thenReturn(qname);
        assertNotNull(m_schemaRegistry.findChild(dataSchemaNode, qname));
    }

    @Test
    public void testLoadSchemaContextWithMultipleAugmentation() throws SchemaBuildException {
        m_schemaRegistry.buildSchemaContext(Arrays.asList(c_shortenedJukeboxYangFile));
        SchemaPath playerPath = SchemaPath.create(true, QName.create(JB_NS, JB_REVISION, "jukebox"),
                QName.create(JB_NS, JB_REVISION, "player"));
        //make sure no augmentation before
        assertEquals(1, ((DataNodeContainer) m_schemaRegistry.getDataSchemaNode(playerPath)).getChildNodes().size());
        m_schemaRegistry.loadSchemaContext("jukebox-plug1", Arrays.asList(c_jukeboxPlug1YangTypesFile), null,
                Collections.emptyMap());
        assertEquals(1, m_schemaRegistry.getRootDataSchemaNodes().size());

        //make sure it is augmented
        SchemaPath fadeEffect1 = new SchemaPathBuilder().withParent(playerPath)
                .withNamespace("http://example.com/ns/example-jukebox-plug1")
                .withRevision("2015-07-03")
                .appendLocalName("fad-effect-container1")
                .build();
        assertNotNull(m_schemaRegistry.getDataSchemaNode(fadeEffect1));

        //make sure both augmentations are present
        SchemaPath fadeEffect2 = new SchemaPathBuilder().withParent(playerPath)
                .withNamespace("http://example.com/ns/example-jukebox-plug2")
                .withRevision("2015-07-03")
                .appendLocalName("fad-effect-container2")
                .build();
        m_schemaRegistry.loadSchemaContext("jukebox-plug2", Arrays.asList(c_jukeboxPlug2YangTypesFile), null,
                Collections.emptyMap());
        assertNotNull(m_schemaRegistry.getDataSchemaNode(fadeEffect2));
        assertNotNull(m_schemaRegistry.getDataSchemaNode(fadeEffect1));

    }

    @Test
    public void testUnloadSchemaContextWithAugmentation() throws SchemaBuildException {
        m_schemaRegistry.buildSchemaContext(getYangFiles());
        SchemaPath playerPath = SchemaPath.create(true, QName.create(JB_NS, JB_REVISION, "jukebox"),
                QName.create(JB_NS, JB_REVISION, "player"));
        //make sure no augmentation before
        assertEquals(1, ((DataNodeContainer) m_schemaRegistry.getDataSchemaNode(playerPath)).getChildNodes().size());
        m_schemaRegistry.loadSchemaContext("jukebox-plug", Arrays.asList(c_jukeboxPlugYangTypesFile), null,
                Collections.emptyMap());
        m_schemaRegistry.unloadSchemaContext("jukebox-plug", Collections.emptyMap());
        assertEquals(1, ((DataNodeContainer) m_schemaRegistry.getDataSchemaNode(playerPath)).getChildNodes().size());
    }

    @Test
    public void testMoreYangs() throws SchemaBuildException {
        SchemaPath deviceSchemaPath = new SchemaPathBuilder()
                .withNamespace("urn:org:bbf:pma")
                .withRevision("2015-07-14")
                .appendLocalName("pma")
                .appendLocalName("device-holder")
                .appendLocalName("device")
                .build();
        SchemaPath configsPath = new SchemaPathBuilder().withNamespace("urn:ietf:params:xml:ns:yang:bbf-fast-vop")
                .withRevision("2015-02-27")
                .withParent(deviceSchemaPath).appendLocalName("configs").build();

        m_schemaRegistry.buildSchemaContext(getAnvYangFiles());
        assertNotNull(m_schemaRegistry.getDataSchemaNode(deviceSchemaPath));

        m_schemaRegistry.loadSchemaContext("G.fast-plug", getGfastYangFiles(), null, Collections.emptyMap());
        assertNotNull(m_schemaRegistry.getDataSchemaNode(configsPath));
        LOGGER.info(m_schemaRegistry.getChildren(deviceSchemaPath));

        SchemaPath jukeboxPath = new SchemaPathBuilder().withNamespace(JB_NS)
                .withRevision(JB_REVISION)
                .withParent(deviceSchemaPath).appendLocalName("jukebox").build();
        m_schemaRegistry.loadSchemaContext("Jukebox-plug", getJukeboxYangFiles(), null, Collections.emptyMap());

        LOGGER.info(m_schemaRegistry.getChildren(deviceSchemaPath));

        assertNotNull(m_schemaRegistry.getDataSchemaNode(jukeboxPath));
        assertNotNull(m_schemaRegistry.getDataSchemaNode(configsPath));
    }

    @Test
    public void testGetChildren() throws SchemaBuildException {
        ReadWriteLockServiceImpl readWriteLockService = spy(new ReadWriteLockServiceImpl());
        m_schemaRegistry.setReadWriteLockService(readWriteLockService);
        m_schemaRegistry.buildSchemaContext(getAnvYangFiles());
        m_schemaRegistry.loadSchemaContext("G.fast-plug", getGfastYangFiles(), null, Collections.emptyMap());
        SchemaPath pmaSchemaPath = new SchemaPathBuilder()
                .withNamespace("urn:org:bbf:pma")
                .withRevision("2015-07-14")
                .appendLocalName("pma")
                .build();
        assertEquals(8, m_schemaRegistry.getChildren(pmaSchemaPath).size());
        verify(readWriteLockService, atLeastOnce()).executeWithReadLock(Mockito.any());

        SchemaPath usersCountSchemaPath = new SchemaPathBuilder()
                .withNamespace("urn:org:bbf:pma")
                .withRevision("2015-07-14")
                .appendLocalName("pma")
                .appendLocalName("users")
                .appendLocalName("user-count")
                .build();
        assertEquals(0, m_schemaRegistry.getChildren(usersCountSchemaPath).size());

        usersCountSchemaPath = new SchemaPathBuilder()
                .withNamespace("urn:org:bbf:pma")
                .withRevision("2015-07-14")
                .appendLocalName("pma")
                .appendLocalName("users")
                .appendLocalName("invalid-name")
                .build();
        assertEquals(0, m_schemaRegistry.getChildren(usersCountSchemaPath).size());
    }

    @Test
    public void testGetNonChoiceChildren() throws SchemaBuildException {
        ReadWriteLockServiceImpl readWriteLockService = spy(new ReadWriteLockServiceImpl());
        m_schemaRegistry.setReadWriteLockService(readWriteLockService);
        //prepare schema registry
        m_schemaRegistry.buildSchemaContext(getAnvYangFiles());
        SchemaPath deviceSchemaPath = new SchemaPathBuilder()
                .withNamespace("urn:org:bbf:pma")
                .withRevision("2015-07-14")
                .appendLocalName("pma")
                .appendLocalName("device-holder")
                .appendLocalName("device")
                .build();

        //test Non Choice children
        m_schemaRegistry.setReadWriteLockService(spy(new ReadWriteLockServiceImpl()));
        Collection<DataSchemaNode> effectiveDeviceChidlren = m_schemaRegistry.getNonChoiceChildren(deviceSchemaPath);
        verify(readWriteLockService, atLeastOnce()).executeWithReadLock(Mockito.any());
        assertEquals(10, effectiveDeviceChidlren.size());

        //build expected device children
        Collection<DataSchemaNode> expectedEffectiveChilren = buildExpectedDeviceChildren(deviceSchemaPath);

        assertEquals(expectedEffectiveChilren, effectiveDeviceChidlren);
    }

    private Collection<DataSchemaNode> buildExpectedDeviceChildren(SchemaPath deviceSchemaPath) {
        Collection<DataSchemaNode> expectedDeviceChildren = new HashSet<>();
        DataNodeContainer deviceSchemaNode = (DataNodeContainer) m_schemaRegistry.getDataSchemaNode(deviceSchemaPath);
        for (DataSchemaNode child : deviceSchemaNode.getChildNodes()) {
            if (!(child instanceof ChoiceSchemaNode)) {
                expectedDeviceChildren.add(child);
            }
        }

        SchemaPath discoveredPropertiesSchemaPath = new SchemaPathBuilder()
                .withNamespace("urn:org:bbf:pma")
                .withRevision("2015-07-14")
                .appendLocalName("pma")
                .appendLocalName("device-holder")
                .appendLocalName("device")
                .appendLocalName("connection-initiator-params")
                .appendLocalName("device")
                .appendLocalName("discovered-device-properties")
                .build();
        DataSchemaNode discoveredSchemaNode = m_schemaRegistry.getDataSchemaNode(discoveredPropertiesSchemaPath);
        expectedDeviceChildren.add(discoveredSchemaNode);

        SchemaPath configuredPropertiesSchemaPath = new SchemaPathBuilder()
                .withNamespace("urn:org:bbf:pma")
                .withRevision("2015-07-14")
                .appendLocalName("pma")
                .appendLocalName("device-holder")
                .appendLocalName("device")
                .appendLocalName("connection-initiator-params")
                .appendLocalName("pma")
                .appendLocalName("configured-device-properties")
                .build();

        DataSchemaNode configuredSchemaNode = m_schemaRegistry.getDataSchemaNode(configuredPropertiesSchemaPath);
        expectedDeviceChildren.add(configuredSchemaNode);
        return expectedDeviceChildren;
    }

    @Test
    public void testGetNonChoiceChildrenWhenNestedChoiceCase() throws SchemaBuildException {
        ReadWriteLockServiceImpl readWriteLockService = spy(new ReadWriteLockServiceImpl());
        m_schemaRegistry.setReadWriteLockService(readWriteLockService);
        //prepare schema registry
        m_schemaRegistry.buildSchemaContext(Arrays.asList(c_jukeboxYangFile));
        SchemaPath albumSchemaPath = new SchemaPathBuilder()
                .withNamespace(JB_NS)
                .withRevision(JB_REVISION)
                .appendLocalName("jukebox")
                .appendLocalName("library")
                .appendLocalName("artist")
                .appendLocalName("album")
                .build();

        //test Non Choice children
        m_schemaRegistry.setReadWriteLockService(spy(new ReadWriteLockServiceImpl()));
        Collection<DataSchemaNode> effectiveDeviceChidlren = m_schemaRegistry.getNonChoiceChildren(albumSchemaPath);
        assertEquals(8, effectiveDeviceChidlren.size());
        verify(readWriteLockService, atLeastOnce()).executeWithReadLock(Mockito.any());

        //build expected album children
        Collection<DataSchemaNode> expectedAlbumChilren = buildExpectedAlbumChildren(albumSchemaPath);

        assertEquals(expectedAlbumChilren, effectiveDeviceChidlren);
    }

    @Test
    public void testGetNonChoiceChild() throws SchemaBuildException {
        //prepare schema registry
        m_schemaRegistry.buildSchemaContext(Arrays.asList(c_jukeboxYangFile));
        SchemaPath albumSchemaPath = new SchemaPathBuilder()
                .withNamespace(JB_NS)
                .withRevision(JB_REVISION)
                .appendLocalName("jukebox")
                .appendLocalName("library")
                .appendLocalName("artist")
                .appendLocalName("album")
                .build();

        SchemaPath cdSchemaPath = new SchemaPathBuilder()
                .withNamespace(JB_NS)
                .withRevision(JB_REVISION)
                .appendLocalName("jukebox")
                .appendLocalName("library")
                .appendLocalName("artist")
                .appendLocalName("album")
                .appendLocalName("release-type")
                .appendLocalName("optical-disk")
                .appendLocalName("optical-disk-choice")
                .appendLocalName("cd-type")
                .appendLocalName("cd")
                .build();

        DataSchemaNode expectedDataSchemaNode = m_schemaRegistry.getDataSchemaNode(cdSchemaPath);
        assertEquals(expectedDataSchemaNode, m_schemaRegistry.getNonChoiceChild(albumSchemaPath, QName.create(JB_NS,
                JB_REVISION, "cd")));

        assertNull(m_schemaRegistry.getNonChoiceChild(albumSchemaPath, QName.create(JB_NS, JB_REVISION,
                "unknown-qname")));

    }

    private Collection<DataSchemaNode> buildExpectedAlbumChildren(SchemaPath deviceSchemaPath) {
        Collection<DataSchemaNode> expectedAlbumChildren = new HashSet<>();
        DataNodeContainer deviceSchemaNode = (DataNodeContainer) m_schemaRegistry.getDataSchemaNode(deviceSchemaPath);
        for (DataSchemaNode child : deviceSchemaNode.getChildNodes()) {
            if (!(child instanceof ChoiceSchemaNode)) {
                expectedAlbumChildren.add(child);
            }
        }

        SchemaPath disketteSchemaPath = new SchemaPathBuilder()
                .withNamespace(JB_NS)
                .withRevision(JB_REVISION)
                .appendLocalName("jukebox")
                .appendLocalName("library")
                .appendLocalName("artist")
                .appendLocalName("album")
                .appendLocalName("release-type")
                .appendLocalName("magnetic-disk")
                .appendLocalName("diskette")
                .build();
        DataSchemaNode disketteSchemaNode = m_schemaRegistry.getDataSchemaNode(disketteSchemaPath);
        expectedAlbumChildren.add(disketteSchemaNode);

        SchemaPath cdSchemaPath = new SchemaPathBuilder()
                .withNamespace(JB_NS)
                .withRevision(JB_REVISION)
                .appendLocalName("jukebox")
                .appendLocalName("library")
                .appendLocalName("artist")
                .appendLocalName("album")
                .appendLocalName("release-type")
                .appendLocalName("optical-disk")
                .appendLocalName("optical-disk-choice")
                .appendLocalName("cd-type")
                .appendLocalName("cd")
                .build();

        DataSchemaNode cdSchemaNode = m_schemaRegistry.getDataSchemaNode(cdSchemaPath);
        expectedAlbumChildren.add(cdSchemaNode);

        SchemaPath dvdSchemaPath = new SchemaPathBuilder()
                .withNamespace(JB_NS)
                .withRevision(JB_REVISION)
                .appendLocalName("jukebox")
                .appendLocalName("library")
                .appendLocalName("artist")
                .appendLocalName("album")
                .appendLocalName("release-type")
                .appendLocalName("optical-disk")
                .appendLocalName("optical-disk-choice")
                .appendLocalName("dvd-type")
                .appendLocalName("dvd")
                .build();

        DataSchemaNode dvdSchemaNode = m_schemaRegistry.getDataSchemaNode(dvdSchemaPath);
        expectedAlbumChildren.add(dvdSchemaNode);
        return expectedAlbumChildren;
    }

    @Test
    public void testLookupPrefix() throws SchemaBuildException {
        ReadWriteLockServiceImpl readWriteLockService = spy(new ReadWriteLockServiceImpl());
        m_schemaRegistry.setReadWriteLockService(readWriteLockService);
        m_schemaRegistry.buildSchemaContext(getYangFiles());
        verify(readWriteLockService).executeWithReadLock(Mockito.any());
        assertEquals("jbox", m_schemaRegistry.getPrefix(JB_NS));
        verify(readWriteLockService, times(2)).executeWithReadLock(Mockito.any());
        assertEquals("inet", m_schemaRegistry.getPrefix(IETF_INET_TYPES_NS));
        verify(readWriteLockService, times(3)).executeWithReadLock(Mockito.any());
    }

    @Test
    public void testLookupNamespace() throws SchemaBuildException {
        ReadWriteLockServiceImpl readWriteLockService = spy(new ReadWriteLockServiceImpl());
        m_schemaRegistry.setReadWriteLockService(readWriteLockService);
        m_schemaRegistry.buildSchemaContext(getYangFiles());
        verify(readWriteLockService).executeWithReadLock(Mockito.any());
        assertEquals(JB_NS, m_schemaRegistry.getNamespaceURI("jbox"));
        verify(readWriteLockService, times(2)).executeWithReadLock(Mockito.any());
        assertEquals(IETF_INET_TYPES_NS, m_schemaRegistry.getNamespaceURI("inet"));
        verify(readWriteLockService, times(3)).executeWithReadLock(Mockito.any());
    }

    @Test
    public void testGetModuleSetIdWithAndWithoutYangLibrary() throws SchemaBuildException {
        ReadWriteLockServiceImpl readWriteLockService = spy(new ReadWriteLockServiceImpl());
        m_schemaRegistry.setReadWriteLockService(readWriteLockService);
        m_schemaRegistry.buildSchemaContext(getYangFiles());
        verify(readWriteLockService, times(1)).executeWithReadLock(Mockito.any());
        String moduleSetId = m_schemaRegistry.getModuleSetId();
        assertEquals(MODULE_SET_ID, moduleSetId);

        m_schemaRegistry.setYangLibrarySupportInHelloMessage(true);
        m_schemaRegistry.buildSchemaContext(getYangFiles());
        moduleSetId = m_schemaRegistry.getModuleSetId();
        assertEquals(MODULE_SET_ID, moduleSetId);

        assertEquals(JB_NS, m_schemaRegistry.getNamespaceURI("jbox"));
        verify(readWriteLockService, times(3)).executeWithReadLock(Mockito.any());
    }

    @Test
    public void testGetModuleSetIdWithDeployAndUndeployPlugs() throws SchemaBuildException {
        Module module = mock(Module.class);
        when(m_schemaRegistry.getModuleByNamespace(YangLibraryChangeNotification.IETF_YANG_LIBRARY_NS)).thenReturn
                (module);
        when(module.getPrefix()).thenReturn("yanglib");

        m_schemaRegistry.setYangLibrarySupportInHelloMessage(true);
        m_schemaRegistry.registerYangLibraryChangeNotificationListener(m_listener);
        m_schemaRegistry.buildSchemaContext(getYangFiles());
        String moduleSetId = m_schemaRegistry.getModuleSetId();
        assertEquals(MODULE_SET_ID, moduleSetId);
        verify(m_listener).sendYangLibraryChangeNotification(moduleSetId);

        m_schemaRegistry.loadSchemaContext("anv", getAnvYangFiles(), null, Collections.emptyMap());
        String moduleSetIdAfterAnv = m_schemaRegistry.getModuleSetId();
        assertNotEquals(MODULE_SET_ID, moduleSetIdAfterAnv);
        assertNotEquals(moduleSetId, moduleSetIdAfterAnv);
        verify(m_listener).sendYangLibraryChangeNotification(moduleSetIdAfterAnv);

        m_schemaRegistry.loadSchemaContext("G.fast-plug", getGfastYangFiles(), null, Collections.emptyMap());
        String moduleSetIdAfterGfast = m_schemaRegistry.getModuleSetId();
        assertNotEquals(MODULE_SET_ID, moduleSetIdAfterGfast);
        assertNotEquals(moduleSetId, moduleSetIdAfterGfast);
        verify(m_listener).sendYangLibraryChangeNotification(moduleSetIdAfterGfast);

        m_schemaRegistry.unloadSchemaContext("G.fast-plug", Collections.emptyMap());
        String moduleSetIdAfterGfastUnload = m_schemaRegistry.getModuleSetId();
        assertEquals(moduleSetIdAfterAnv, moduleSetIdAfterGfastUnload);
        verify(m_listener, times(2)).sendYangLibraryChangeNotification(moduleSetIdAfterGfastUnload);

        m_schemaRegistry.unloadSchemaContext("anv", Collections.emptyMap());
        String moduleSetIdAfterAnvUnload = m_schemaRegistry.getModuleSetId();
        assertEquals(MODULE_SET_ID, moduleSetIdAfterAnvUnload);
        verify(m_listener, times(2)).sendYangLibraryChangeNotification(moduleSetIdAfterAnvUnload);
    }

    @Test
    public void testLookupNamespaceOfModule() throws SchemaBuildException {
        ReadWriteLockServiceImpl readWriteLockService = spy(new ReadWriteLockServiceImpl());
        m_schemaRegistry.setReadWriteLockService(readWriteLockService);
        m_schemaRegistry.buildSchemaContext(getYangFiles());
        verify(readWriteLockService).executeWithReadLock(Mockito.any());
        assertEquals(JB_NS, m_schemaRegistry.getNamespaceOfModule("example-jukebox"));
        verify(readWriteLockService, times(2)).executeWithReadLock(Mockito.any());
    }

    @Test
    public void testFindModuleByNamespaceAndRevision() throws URISyntaxException, SchemaBuildException,
            LockServiceException {
        ReadWriteLockServiceImpl readWriteLockService = spy(new ReadWriteLockServiceImpl());
        m_schemaRegistry.setReadWriteLockService(readWriteLockService);
        m_schemaRegistry.buildSchemaContext(getYangFiles());
        verify(readWriteLockService).executeWithWriteLock(Mockito.any());
        verify(readWriteLockService).executeWithReadLock(Mockito.any());

        Module module = m_schemaRegistry.getModuleByNamespace(JB_NS);
        verify(readWriteLockService, times(2)).executeWithReadLock(Mockito.any());
        module = m_schemaRegistry.findModuleByNamespaceAndRevision(new URI(JB_NS), module.getRevision());
        assertEquals("example-jukebox", module.getName());
        verify(readWriteLockService, times(3)).executeWithReadLock(Mockito.any());

    }

    public static List<YangTextSchemaSource> getGfastYangFiles() {
        YangTextSchemaSource bbfFastYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource
                ("/yangSchemaValidationTest/referenceyangs/anvyangs/bbf-fast@2015-02-27.yang"));
        YangTextSchemaSource ianaTypeYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource
                ("/yangSchemaValidationTest/referenceyangs/anvyangs/iana-if-type@2014-05-08.yang"));
        YangTextSchemaSource ietfNetconfYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
                .getResource("/yangSchemaValidationTest/referenceyangs/anvyangs/ietf-netconf@2011-03-08.yang"));
        YangTextSchemaSource yumaAppCommonYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
                .getResource("/yangSchemaValidationTest/referenceyangs/anvyangs/yuma-app-common@2012-08-16.yang"));
        YangTextSchemaSource yumaArpYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource
                ("/yangSchemaValidationTest/referenceyangs/anvyangs/yuma-arp@2012-01-13.yang"));
        YangTextSchemaSource yumaInterfacesYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
                .getResource("/yangSchemaValidationTest/referenceyangs/anvyangs/yuma-interfaces@2012-01-13.yang"));
        YangTextSchemaSource yumaNacmYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource
                ("/yangSchemaValidationTest/referenceyangs/anvyangs/yuma-nacm@2012-10-05.yang"));
        YangTextSchemaSource yumaProcYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource
                ("/yangSchemaValidationTest/referenceyangs/anvyangs/yuma-proc@2012-10-10.yang"));
        YangTextSchemaSource yumaSystemYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
                .getResource("/yangSchemaValidationTest/referenceyangs/anvyangs/yuma-system@2012-10-05.yang"));
        YangTextSchemaSource yumaTypesYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
                .getResource("/yangSchemaValidationTest/referenceyangs/anvyangs/yuma-types@2012-06-01.yang"));

        return Arrays.asList(bbfFastYangFile, ianaTypeYangFile, yumaAppCommonYangFile, yumaArpYangFile,
                yumaInterfacesYangFile,
                ietfNetconfYangFile, yumaNacmYangFile, yumaProcYangFile, yumaSystemYangFile, yumaTypesYangFile);
    }

    public static List<YangTextSchemaSource> getAnvYangFiles() {
        YangTextSchemaSource aluDeviceYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
                .getResource("/yangSchemaValidationTest/referenceyangs/anvyangs/alu-device-plugs@2015-07-14.yang"));
        YangTextSchemaSource aluDpuSwmgmtYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
                .getResource("/yangSchemaValidationTest/referenceyangs/anvyangs/alu-dpu-swmgmt@2015-07-14.yang"));
        YangTextSchemaSource aluPmaYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource
                ("/yangSchemaValidationTest/referenceyangs/anvyangs/alu-pma@2015-07-14.yang"));
        YangTextSchemaSource aluCertificatesYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
                .getResource("/yangSchemaValidationTest/referenceyangs/anvyangs/alu-pma-certificates@2015-07-14.yang"));
        YangTextSchemaSource aluStatsYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource
                ("/yangSchemaValidationTest/referenceyangs/anvyangs/alu-pma-statistics@2015-07-14.yang"));
        YangTextSchemaSource aluPmaSwmgmtYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
                .getResource("/yangSchemaValidationTest/referenceyangs/anvyangs/alu-pma-swmgmt@2015-07-14.yang"));
        YangTextSchemaSource aluPmaTypesYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
                .getResource("/yangSchemaValidationTest/referenceyangs/anvyangs/alu-pma-types@2015-08-13.yang"));
        YangTextSchemaSource aluPmaUsersYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
                .getResource("/yangSchemaValidationTest/referenceyangs/anvyangs/alu-pma-users@2015-07-14.yang"));
        YangTextSchemaSource ietfYangTypesYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
                .getResource("/yangSchemaValidationTest/referenceyangs/anvyangs/ietf-yang-types@2010-09-24.yang"));
        YangTextSchemaSource ietfInetYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource
                ("/yangSchemaValidationTest/referenceyangs/anvyangs/ietf-inet-types@2010-09-24.yang"));
        YangTextSchemaSource ietfInterfacesYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class
                .getResource("/yangSchemaValidationTest/referenceyangs/anvyangs/ietf-interfaces@2014-05-08.yang"));

        return Arrays.asList(aluDeviceYangFile, aluDpuSwmgmtYangFile, aluPmaYangFile,
                aluCertificatesYangFile, aluStatsYangFile, aluPmaSwmgmtYangFile, aluPmaTypesYangFile,
                aluPmaUsersYangFile,
                ietfYangTypesYangFile, ietfInetYangFile, ietfInterfacesYangFile);
    }

    private void makeSureMakeToastRpcExists() {
        RpcDefinition makeToastRpcDefinition = m_schemaRegistry.getRpcDefinition(MAKE_TOAST_PATH);
        assertEquals(MAKE_TOAST_PATH.getLastComponent(), makeToastRpcDefinition.getQName());
        assertEquals("input", makeToastRpcDefinition.getInput().getQName().getLocalName());
        assertEquals(2, makeToastRpcDefinition.getInput().getChildNodes().size());
    }

    public List<YangTextSchemaSource> getJukeboxYangFiles() {
        YangTextSchemaSource jukeboxYangFile = YangParserUtil.getYangSource(SchemaRegistryImplTest.class.getResource
                ("/yangSchemaValidationTest/referenceyangs/anvyangs/example-jukebox@2014-07-03.yang"));

        return Arrays.asList(jukeboxYangFile);
    }

    @Test
    public void testLoadUnloadImpactedNodesForConstraints() {
        SchemaPath constraintSchemaPath = new SchemaPathBuilder()
                .withNamespace(JB_NS)
                .withRevision(JB_REVISION)
                .appendLocalName("jukebox")
                .appendLocalName("library")
                .appendLocalName("artist")
                .appendLocalName("album")
                .appendLocalName("release-type")
                .appendLocalName("optical-disk")
                .appendLocalName("optical-disk-choice")
                .appendLocalName("cd-type")
                .appendLocalName("cd")
                .build();

        SchemaPath nodeSchemaPath = new SchemaPathBuilder()
                .withNamespace(JB_NS)
                .withRevision(JB_REVISION)
                .appendLocalName("jukebox")
                .appendLocalName("library")
                .appendLocalName("artist")
                .appendLocalName("album")
                .appendLocalName("release-type")
                .appendLocalName("optical-disk")
                .appendLocalName("optical-disk-choice")
                .appendLocalName("dvd-type")
                .appendLocalName("dvd")
                .build();

        m_schemaRegistry.registerNodesReferencedInConstraints("G.Fast-1.1", constraintSchemaPath, nodeSchemaPath, null);
        assertTrue(m_schemaRegistry.getSchemaPathsForComponent("G.Fast-1.1").size() == 1);
        m_schemaRegistry.deRegisterNodesReferencedInConstraints("G.Fast-1.1");
        assertTrue(m_schemaRegistry.getSchemaPathsForComponent("G.Fast-1.1").isEmpty());

    }

    @Test
    public void testGetSupportedModuleCap() throws URISyntaxException, SchemaBuildException {
        SchemaRegistry schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), new
                ReadWriteLockServiceImpl());
        Set<String> expectedCaps = new HashSet<>();
        expectedCaps.add("http://example.com/ns/example-jukebox?module=example-jukebox&revision=2014-07-03");
        expectedCaps.add("http://example.com/ns/example-jukebox?revision=2014-07-03&module=example-jukebox");
        String capabilityWithOnlyModuleInfo = "http://example" +
                ".com/ns/example-jukebox?module=example-jukebox&revision=2014-07-03";
        String capabilityWithFeatureDeviationInfo = "http://example" +
                ".com/ns/example-jukebox?module=example-jukebox&revision=2014-07-03&features=jukebox-test-feature" +
                "&deviations=test-example-jukebox-dev";
        expectedCaps.add(capabilityWithFeatureDeviationInfo);

        expectedCaps.add("urn:ietf:params:xml:ns:yang:ietf-restconf?module=ietf-restconf&revision=2014-07-03");
        expectedCaps.add("urn:ietf:params:xml:ns:yang:ietf-restconf?revision=2014-07-03&module=ietf-restconf");

        expectedCaps.add("urn:ietf:params:xml:ns:yang:ietf-inet-types?module=ietf-inet-types&revision=2013-07-15");
        expectedCaps.add("urn:ietf:params:xml:ns:yang:ietf-inet-types?revision=2013-07-15&module=ietf-inet-types");

        expectedCaps.add("urn:ietf:params:xml:ns:yang:ietf-yang-types?module=ietf-yang-types&revision=2013-07-15");
        expectedCaps.add("urn:ietf:params:xml:ns:yang:ietf-yang-types?revision=2013-07-15&module=ietf-yang-types");

        Set<String> actualCaps = schemaRegistry.getModuleCapabilities(false);
        assertFalse(actualCaps.contains(capabilityWithOnlyModuleInfo));
        for (String actualCap : actualCaps) {
            assertNotNull(expectedCaps.remove(actualCap));
        }
        QName feature = QName.create("http://example.com/ns/example-jukebox", "2014-07-03", "jukebox-test-feature");
        Set<QName> features = new HashSet<>();
        features.add(feature);

        Map<QName, Set<QName>> deviationMap = new HashMap<>();
        QName moduleQName = QName.create("http://example.com/ns/example-jukebox", "2014-07-03", "example-jukebox");
        QName deviationQName = QName.create("http://example.com/ns/example-jukebox-dev", "2014-08-03",
                "test-example-jukebox-dev");
        Set<QName> deviations = new HashSet<>();
        deviations.add(deviationQName);
        deviationMap.put(moduleQName, deviations);

        schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), features, deviationMap, new
                ReadWriteLockServiceImpl());
        actualCaps = schemaRegistry.getModuleCapabilities(false);
        assertTrue(actualCaps.contains(capabilityWithFeatureDeviationInfo));
        assertFalse(actualCaps.contains(capabilityWithOnlyModuleInfo));

        m_schemaRegistry.loadSchemaContext("jukebox-plug", TestUtil.getJukeBoxYangs(), features, deviationMap);
        actualCaps = m_schemaRegistry.getModuleCapabilities(false);
        assertTrue(actualCaps.contains(capabilityWithFeatureDeviationInfo));
        assertFalse(actualCaps.contains(capabilityWithOnlyModuleInfo));
    }

    @Test
    public void testUpdateCapabilitesForDeviations() throws URISyntaxException, SchemaBuildException, ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        QName feature = QName.create("http://example.com/ns/example-jukebox", "2014-07-03", "jukebox-test-feature");
        Set<QName> features = new HashSet<>();
        features.add(feature);

        Map<QName, Set<QName>> deviationMap = new HashMap<>();
        QName moduleQName = QName.create("http://example.com/ns/example-jukebox", "2014-07-03", "example-jukebox");
        QName deviationQName = QName.create("http://example.com/ns/example-jukebox-dev", "2014-08-03",
                "test-example-jukebox-dev");
        Set<QName> deviations = new HashSet<>();
        deviations.add(deviationQName);
        deviationMap.put(moduleQName, deviations);

        SchemaRegistry schemaRegistry = new SchemaRegistryImpl(TestUtil.getJukeBoxYangs(), null, deviationMap, new
                ReadWriteLockServiceImpl());
        Map<ModuleIdentifier, Set<QName>> supportedDeviations = schemaRegistry.getSupportedDeviations();

        URI namespaceUri = new URI("http://example.com/ns/example-jukebox");
        Date revisionDate = sdf.parse("2014-07-03");
        Optional<URI> namespace = Optional.of(namespaceUri);
        Optional<Date> revision = Optional.of(revisionDate);
        ModuleIdentifier moduleIdentifier = ModuleIdentifierImpl.create("example-jukebox", namespace, revision);
        assertNotNull(supportedDeviations);
        assertTrue(supportedDeviations.get(moduleIdentifier).contains(deviationQName));
    }

    @Test
    public void testGetModuleCapsForHello() throws SchemaBuildException {
        List<YangTextSchemaSource> yangFiles = Arrays.asList(c_jukeboxYangFile, c_ietfInetTypesFile,
                c_ietfYangTypesFile, c_testYang11File);
        assertEquals(4, yangFiles.size());
        m_schemaRegistry.buildSchemaContext(yangFiles);
        assertFalse(m_schemaRegistry.isYangLibrarySupportedInHelloMessage());
        Set<String> moduleCapabilities = m_schemaRegistry.getModuleCapabilities(false);
        assertEquals(yangFiles.size(), moduleCapabilities.size());
        assertTrue(moduleCapabilities.contains(EXAMPLE_YANG11_FILE_CAPS));
        moduleCapabilities = m_schemaRegistry.getModuleCapabilities(true);
        assertEquals(yangFiles.size(), moduleCapabilities.size());
        assertTrue(moduleCapabilities.contains(EXAMPLE_YANG11_FILE_CAPS));

        m_schemaRegistry.setYangLibrarySupportInHelloMessage(true);
        moduleCapabilities = m_schemaRegistry.getModuleCapabilities(true);
        assertEquals(4, moduleCapabilities.size());
        assertFalse(moduleCapabilities.contains(EXAMPLE_YANG11_FILE_CAPS));
        assertTrue(moduleCapabilities.contains(YANG_LIBRARY_CAP_WITH_MODULESETID));
    }

    @Test
    public void testRegisterAndUnregisterAppAllowedAugmentPath() {
        Map<SchemaPath, String> expectedEntry = new HashMap<>();
        expectedEntry.put(JUKEBOX_PATH, COMPONENT_ID1);
        m_schemaRegistry.registerAppAllowedAugmentedPath(COMPONENT_ID1, "/schemapath1", JUKEBOX_PATH);
        Map<SchemaPath, String> actualEntry = m_schemaRegistry.retrieveAppAugmentedPathToComponent();
        assertEquals(expectedEntry, actualEntry);
        assertEquals(1, actualEntry.size());

        m_schemaRegistry.deRegisterAppAllowedAugmentedPath(AUGMENTED_PATH);
        actualEntry = m_schemaRegistry.retrieveAppAugmentedPathToComponent();
        assertTrue(actualEntry.isEmpty());
    }
}
