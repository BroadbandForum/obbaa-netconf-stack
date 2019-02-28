package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.model.SomeList;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.model.TestSlicing;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.EntityRegistryBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootEntityContainerModelNodeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.model.SomeInnerList;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.model.TestSlice;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.model.TestVno;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.model.TestVnos;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.model.Validation;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

@SuppressWarnings("deprecation")
public class FansYangXmlSubTreeValidationTest extends AbstractRootModelTest {

	private static final String FANS_VALIDATION_TEST_REVISION = "2017-07-11";
	private static final String FANS_VALIDATION_TEST_NS = "urn:opendaylight:fans-validation-test";
	private static final String COMPONENT_ID_VALIDATION = "datastore-validator-test";
	private static final String COMPONENT_ID_FANS_VALIDATION = "fans-validation-test";
	protected static final QName TEST_VNOS_QNAME = QName.create(FANS_VALIDATION_TEST_NS, FANS_VALIDATION_TEST_REVISION,
			"test-vnos");
	protected static final QName TEST_VNO_QNAME = QName.create(FANS_VALIDATION_TEST_NS, FANS_VALIDATION_TEST_REVISION,
			"test-vno");
	protected static final QName TEST_SLICING_QNAME = QName.create(FANS_VALIDATION_TEST_NS,
			FANS_VALIDATION_TEST_REVISION, "test-slicing");
	protected static final QName TEST_SLICE_QNAME = QName.create(FANS_VALIDATION_TEST_NS, FANS_VALIDATION_TEST_REVISION,
			"test-slice");
	
	protected static final SchemaPath TEST_VNOS_SCHEMA_PATH = buildSchemaPath(VALIDATION_SCHEMA_PATH, TEST_VNOS_QNAME);
	protected static final SchemaPath TEST_VNO_SCHEMA_PATH = buildSchemaPath(TEST_VNOS_SCHEMA_PATH, TEST_VNO_QNAME);
	protected static final SchemaPath TEST_SLICING_SCHEMA_PATH = buildSchemaPath(SOME_INNER_LIST_SCHEMA_PATH,
			TEST_SLICING_QNAME);
	protected static final SchemaPath TEST_SLICE_SCHEMA_PATH = buildSchemaPath(TEST_SLICING_SCHEMA_PATH,
			TEST_SLICE_QNAME);

	@Before
	public void setup() throws SchemaBuildException, AnnotationAnalysisException, ModelNodeFactoryException {
		super.setup();
		initializeEntityRegistry();
		loadXmlDataIntoServer();
		m_rootModelNode = m_rootModelNodeAggregator.getModelServiceRoots().get(0);
	}

	protected void getSchemaRegistry(List<YangTextSchemaSource> yangFiles) throws SchemaBuildException {
		m_schemaRegistry = new SchemaRegistryImpl(yangFiles, Collections.emptySet(), Collections.emptyMap(), new NoLockService());
	}

	protected List<YangTextSchemaSource> getYangs() {
		return TestUtil.getByteSources(getYangFiles());
	}

	protected List<String> getYangFiles() {
		List<String> fileNames = new LinkedList<String>();
		fileNames.add("/datastorevalidatortest/yangs/dummy-extension.yang");
		fileNames.add("/datastorevalidatortest/yangs/ietf-yang-schema-mount@2017-10-09.yang");
		fileNames.add("/datastorevalidatortest/yangs/datastore-validator-test2.yang");
		fileNames.add("/datastorevalidatortest/yangs/fans-validation-test.yang");
		return fileNames;
	}

	@SuppressWarnings("rawtypes")
	protected void initializeEntityRegistry() throws AnnotationAnalysisException {
		List<Class> classes = new ArrayList<>();
		classes.add(Validation.class);
		classes.add(SomeList.class);
		classes.add(SomeInnerList.class);
		EntityRegistryBuilder.updateEntityRegistry(COMPONENT_ID_VALIDATION, classes, m_entityRegistry, m_schemaRegistry,
				m_persistenceManagerUtil.getEntityDataStoreManager(), m_modelNodeDSMRegistry);
		m_modelNodeDSMRegistry.register(COMPONENT_ID_VALIDATION, VALIDATION_SCHEMA_PATH, m_xmlSubtreeDSM);

		classes.clear();
		classes.add(TestVnos.class);
		classes.add(TestVno.class);
		classes.add(TestSlicing.class);
		classes.add(TestSlice.class);
		EntityRegistryBuilder.updateEntityRegistry(COMPONENT_ID_FANS_VALIDATION, classes, m_entityRegistry,
				m_schemaRegistry, m_persistenceManagerUtil.getEntityDataStoreManager(), m_modelNodeDSMRegistry);
		m_modelNodeDSMRegistry.register(COMPONENT_ID_FANS_VALIDATION, TEST_VNOS_SCHEMA_PATH, m_xmlSubtreeDSM);
	}

	protected void addRootNodeHelpers() {
		ContainerSchemaNode schemaNode = (ContainerSchemaNode) m_schemaRegistry
				.getDataSchemaNode(VALIDATION_SCHEMA_PATH);
		ChildContainerHelper containerHelper = new RootEntityContainerModelNodeHelper(schemaNode,
				m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_aggregatedDSM);
		m_rootModelNodeAggregator.addModelServiceRootHelper(VALIDATION_SCHEMA_PATH, containerHelper);
	}

	@Test
	public void testDeleteListWithSlice() throws Exception {
		// create two vno lists
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation:validation xmlns:validation= \"urn:org:bbf:pma:validation\">"
				+ " <test-vnos xmlns=\"urn:opendaylight:fans-validation-test\">" 
				+ "  <test-vno>"
				+ "   <name>vno1</name>" 
				+ "  </test-vno>" 
				+ "  <test-vno>" 
				+ "   <name>vno2</name>" 
				+ "  </test-vno>"
				+ " </test-vnos>" 
				+ "</validation:validation>";
		editConfig(requestXml);

		// create someList and someInnerList
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation:validation xmlns:validation= \"urn:org:bbf:pma:validation\">"
				+ " <validation:someList>" 
				+ "  <validation:someKey>TestFANS</validation:someKey>"
				+ "   <validation:someInnerList>" 
				+ "    <validation:someKey>OLT1.ONT1</validation:someKey>"
				+ "   </validation:someInnerList>" 
				+ " </validation:someList>" 
				+ "</validation:validation>";
		editConfig(requestXml);

		// create slice
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation:validation xmlns:validation= \"urn:org:bbf:pma:validation\">"
				+ " <validation:someList>" 
				+ "  <validation:someKey>TestFANS</validation:someKey>"
				+ "   <validation:someInnerList>" 
				+ "    <validation:someKey>OLT1.ONT1</validation:someKey>"
				+ "    <fanstest:test-slicing xmlns:fanstest=\"urn:opendaylight:fans-validation-test\">"
				+ "	    <fanstest:test-slice>"
				+ "	     <fanstest:virtual-device-id>virtualID</fanstest:virtual-device-id>"
				+ "	     <fanstest:vno>vno1</fanstest:vno>" 
				+ "	    </fanstest:test-slice>"
				+ "    </fanstest:test-slicing>" 
				+ "   </validation:someInnerList>" 
				+ " </validation:someList>"
				+ "</validation:validation>";
		editConfig(requestXml);

		// delete vno1 list
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation:validation xmlns:validation= \"urn:org:bbf:pma:validation\">"
				+ " <test-vnos xmlns=\"urn:opendaylight:fans-validation-test\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ "  <test-vno  xc:operation='delete'>" 
				+ "   <name>vno1</name>" 
				+ "  </test-vno>" 
				+ " </test-vnos>"
				+ "</validation:validation>";
		m_persistenceManagerUtil.getEntityDataStoreManager().beginTransaction();
		NetConfResponse ncResponse = editConfigAsFalse(requestXml);
		m_persistenceManagerUtil.getEntityDataStoreManager().rollbackTransaction();
		assertEquals("Dependency violated, 'vno1' must exist", ncResponse.getErrors().get(0).getErrorMessage());
		assertEquals(
				"/validation:validation/validation:someList[validation:someKey='TestFANS']/validation:someInnerList[validation:someKey='OLT1.ONT1']/fanstest:test-slicing/fanstest:test-slice[fanstest:virtual-device-id='virtualID']/fanstest:vno",
				ncResponse.getErrors().get(0).getErrorPath());
		assertEquals("instance-required", ncResponse.getErrors().get(0).getErrorAppTag());
	}

	@Test
	public void testDeleteListWithOutSlice() throws Exception {
		// create two vno lists
		String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation:validation xmlns:validation= \"urn:org:bbf:pma:validation\">"
				+ " <test-vnos xmlns=\"urn:opendaylight:fans-validation-test\">" + "  <test-vno>"
				+ "   <name>vno1</name>" 
				+ "  </test-vno>" 
				+ "  <test-vno>" 
				+ "   <name>vno2</name>" 
				+ "  </test-vno>"
				+ " </test-vnos>" + "</validation:validation>";
		editConfig(requestXml);

		// delete vno1 and vno2 list
		requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
				+ "<validation:validation xmlns:validation= \"urn:org:bbf:pma:validation\">"
				+ " <test-vnos xmlns=\"urn:opendaylight:fans-validation-test\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
				+ "  <test-vno  xc:operation='delete'>" 
				+ "   <name>vno1</name>" 
				+ "  </test-vno>"
				+ "  <test-vno  xc:operation='delete'>" 
				+ "   <name>vno2</name>" 
				+ "  </test-vno>" 
				+ " </test-vnos>"
				+ "</validation:validation>";
		editConfig(requestXml);
	}
}
