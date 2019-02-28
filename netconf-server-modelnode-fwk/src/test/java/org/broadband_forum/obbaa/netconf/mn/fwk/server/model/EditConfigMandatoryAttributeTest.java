package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.getByteSources;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.RpcRequestConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.AddDefaultDataInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.DefaultCapabilityCommandInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreValidatorImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service.DataStoreIntegrityServiceImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.inmemory.InMemoryDSM;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.AbstractYangValidationTestSetup;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

@RunWith(Parameterized.class)
public class EditConfigMandatoryAttributeTest extends AbstractYangValidationTestSetup {

    private static final String REQUEST_FILE_PATH = "src/test/resources/editconfigmandatoryattributes/request_create-policy.xml";
    private static final String REQUEST_WITH_CHOICE_CASE_FILE_PATH = "src/test/resources/editconfigmandatoryattributes/request_create-policy-without-choice.xml";
    private static final String REQUEST_WITH_OPERATION_FILE_PATH = "src/test/resources/editconfigmandatoryattributes/request_create-policy-with-operation.xml";
    private static final String RESPONSE_FILE_PATH = "/editconfigmandatoryattributes/response_missing-mandatory-attribute.xml";
    private static final String RESPONSE_WITH_OPERATION_FILE_PATH = "/editconfigmandatoryattributes/response_missing-mandatory-attribute-with-operation.xml";
    private static final String RESPONSE_WITH_CHOICE_CASE_FILE_PATH = "/editconfigmandatoryattributes/response_for_choice_case.xml";
    private static final String POLICY_ENGINE_YANG_FILE_PATH = "/editconfigmandatoryattributes/policy-engine.yang";
    private static final String POLICY_ENGINE_YANG_WITH_CHICE_DEFINED_FILE_PATH = "/editconfigmandatoryattributes/policy-engine-with-choice.yang";
    private static final String IETF_YANG_FILE_PATH = "/editconfigmandatoryattributes/ietf-yang-types.yang";

    private NetConfServerImpl m_server;
    private SubSystemRegistry m_subSystemRegistry = new SubSystemRegistryImpl();
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private SchemaRegistry m_schemaRegistry;
    private ModelNodeWithAttributes m_modelWithAttributes;
    private InMemoryDSM m_modelNodeDsm;
    private ModelNodeHelperRegistry m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
    private DataStoreIntegrityServiceImpl m_integrityService;
    private DataStoreValidatorImpl m_datastoreValidator;
    private EditConfigRequest m_request;
    private String m_yangPath;
    private String m_expectedPath;

    @Before
    public void initServer() throws SchemaBuildException, ModelNodeInitException {
        List<String> yangFiles = new ArrayList<>();
        yangFiles.add(IETF_YANG_FILE_PATH);
        yangFiles.add(m_yangPath);
        m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.loadSchemaContext("policyEngine", getByteSources(yangFiles), Collections.emptySet(), Collections.emptyMap());
        ModelNodeDataStoreManager modelNodeDsm = mock(ModelNodeDataStoreManager.class);
        DSExpressionValidator expValidator = new DSExpressionValidator(m_schemaRegistry, mock(ModelNodeHelperRegistry.class), mock(SubSystemRegistry.class));
        DefaultCapabilityCommandInterceptor intersepter = new AddDefaultDataInterceptor(m_modelNodeHelperRegistry, m_schemaRegistry, expValidator);
        m_modelNodeHelperRegistry.setDefaultCapabilityCommandInterceptor(intersepter);
        RpcRequestConstraintParser parser = new RpcRequestConstraintParser(m_schemaRegistry, modelNodeDsm, expValidator);
        m_server = new NetConfServerImpl(m_schemaRegistry, /*mock(RpcPayloadConstraintParser.class)*/ parser);
        m_modelNodeDsm = new InMemoryDSM(m_schemaRegistry);
        m_modelWithAttributes = YangUtils.createInMemoryModelNode(getClass().getResource(m_yangPath).getPath(), new LocalSubSystem(),
                m_modelNodeHelperRegistry, m_subSystemRegistry, m_schemaRegistry, m_modelNodeDsm);
        m_rootModelNodeAggregator = new RootModelNodeAggregatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_modelNodeDsm, m_subSystemRegistry)
                .addModelServiceRoot("policyEngine", m_modelWithAttributes);
        m_integrityService = new DataStoreIntegrityServiceImpl(m_server);
        m_datastoreValidator = new DataStoreValidatorImpl(m_schemaRegistry, m_modelNodeHelperRegistry, m_modelNodeDsm, m_integrityService, m_expValidator);
        DataStore dataStore = new DataStore(StandardDataStores.RUNNING, m_rootModelNodeAggregator, m_subSystemRegistry, m_datastoreValidator);
        NbiNotificationHelper nbiNotificationHelper = mock(NbiNotificationHelper.class);
        dataStore.setNbiNotificationHelper(nbiNotificationHelper);
        m_server.setRunningDataStore(dataStore);
    }

    public EditConfigMandatoryAttributeTest(final EditConfigRequest request, final String expectedPath, final String yangPath) {
        this.m_request = request;
        this.m_expectedPath = expectedPath;
        this.m_yangPath = yangPath;
    }

    @Test
    public void testCreateWhenMandatoryAttributeIsMissingInTheRequest() throws Exception {
        NetConfResponse response = new NetConfResponse().setMessageId("1");
        m_server.onEditConfig(m_clientInfo, m_request, response);
        TestUtil.assertXMLEquals(m_expectedPath, response);
    }

    @Parameterized.Parameters(name = "{index}: testCreateWhenMandatoryAttributeIsMissingInTheRequest - {1}")
    public static Collection editConfigRequests() throws NetconfMessageBuilderException {
        EditConfigRequest request1 = DocumentToPojoTransformer.getEditConfig(DocumentUtils.getDocFromFile(new File(REQUEST_FILE_PATH)));
        EditConfigRequest request2 = DocumentToPojoTransformer.getEditConfig(DocumentUtils.getDocFromFile(new File(REQUEST_WITH_OPERATION_FILE_PATH)));
        EditConfigRequest request3 = DocumentToPojoTransformer.getEditConfig(DocumentUtils.getDocFromFile(new File(REQUEST_WITH_CHOICE_CASE_FILE_PATH)));
        return Arrays.asList(new Object[][]{
                {request1, RESPONSE_FILE_PATH, POLICY_ENGINE_YANG_FILE_PATH},
                {request2, RESPONSE_WITH_OPERATION_FILE_PATH, POLICY_ENGINE_YANG_FILE_PATH},
                {request3, RESPONSE_WITH_CHOICE_CASE_FILE_PATH, POLICY_ENGINE_YANG_WITH_CHICE_DEFINED_FILE_PATH}
        });
    }
}