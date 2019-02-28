package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import static junit.framework.TestCase.assertTrue;
import static org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity.Error;
import static org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag.DATA_MISSING;
import static org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType.Application;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ExtensionDefinition;
import org.opendaylight.yangtools.yang.model.api.IdentitySchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.UnknownSchemaNode;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.IdentityrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.Int64TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;

@RunWith(MockitoJUnitRunner.class)
public class AddDefaultDataInterceptorTest {

    private static final String BBF_FAST_NAMESPACE = "urn:broadband-forum-org:yang:bbf-fast";
    private static final String DEVICE_HOLDER_NAMESPACE = "http://www.test-company.com/solutions/anv-device-holders";
    private static final String DEVICE_SPECIFIC_DATA = "device-specific-data";
    private static final String DOWN_STREAM_PROFILE = "down-stream-profile";
    private static final String NAME = "name";
    private static final String DATA_RATE = "data-rate";
    private static final String CHOICE_NODE = "choice-node";
    private static final String CASE_NODE = "case-node";
    private static final String LEAF_WITH_EMPTY_TYPE = "leaf-with-empty-type";

    private static final String BBF_SUB_INTERFACE_TAGGING = "urn:bbf:yang:bbf-sub-interface-tagging";
    private static final String MATCH_CRITERIA = "match-criteria";
    private static final String IPV4_MULTICAST_ADDRESS = "ipv4-multicast-address";
    private static final String ANY_FRAME = "any-frame";
    private static final QName MATCH_CRITERIA_QNAME = QName.create(BBF_SUB_INTERFACE_TAGGING, MATCH_CRITERIA);
    private static final SchemaPath MATCH_CRITERIA_SCHEMAPATH = SchemaPath.create(true,MATCH_CRITERIA_QNAME);
    private static final QName IP4V_MULTICAST_ADDR_QNAME = QName.create(BBF_SUB_INTERFACE_TAGGING, IPV4_MULTICAST_ADDRESS);
    private static final QName ANY_FRAME_QNAME = QName.create(BBF_SUB_INTERFACE_TAGGING, ANY_FRAME);
    private static final SchemaPath ANY_FRAME_SCHEMAPATH = new SchemaPathBuilder().withParent(MATCH_CRITERIA_SCHEMAPATH).appendLocalName(ANY_FRAME).build();
    private static final QName DEST_MAC_ADD_QNAME = QName.create(BBF_SUB_INTERFACE_TAGGING, "dest-mac-add");
    private static final QName ANY_MULTICAST_QNAME = QName.create(BBF_SUB_INTERFACE_TAGGING, "any");
    private static final QName IPV4_MULTICAST_QNAME = QName.create(BBF_SUB_INTERFACE_TAGGING, "ipv4");

    @Mock private SchemaRegistry m_schemaRegistry;
    @Mock private SchemaContext m_schemaContext;
    @Mock private ModelNodeHelperRegistry m_modelNodeHelperRegistry;
    @Mock private ModelNode m_modelNode;
    @Mock private LeafSchemaNode m_dataRateLeafNode;
    @Mock private LeafSchemaNode m_nameLeafNode;
    @Mock private ChoiceSchemaNode m_choiceSchemaNode;
    @Mock private CaseSchemaNode m_choiceCaseNode;
    @Mock private LeafSchemaNode m_leafWithEmptyType;
    @Mock private ContainerSchemaNode m_dsProfileSchemaNode;
    @Mock private ContainerSchemaNode m_deviceSpecificDataSchemaNode;
    @Mock private DSExpressionValidator m_expValidator;
    @Mock private UnknownSchemaNode m_unKnownSchemaNode; 
    @Mock private ExtensionDefinition m_extensionDef;
    @InjectMocks private AddDefaultDataInterceptor m_addDefaultDataInterceptor;
    
    @Rule
    public ExpectedException m_expectedException = ExpectedException.none();
    
    private static QName qname(String localName) {
        return QName.create(BBF_FAST_NAMESPACE, localName);
    }

    @Before
    public void setUp() throws Exception {
        m_addDefaultDataInterceptor = new AddDefaultDataInterceptor(m_modelNodeHelperRegistry , m_schemaRegistry , m_expValidator);
        m_addDefaultDataInterceptor.init();
        when(m_schemaRegistry.getSchemaContext()).thenReturn(m_schemaContext);
        
        initNode(m_dsProfileSchemaNode, null, DOWN_STREAM_PROFILE, null, true);
        initNode(m_nameLeafNode, m_dsProfileSchemaNode, NAME, null, false);
        initNode(m_dataRateLeafNode, m_dsProfileSchemaNode, DATA_RATE, null, true);
        
        initNode(m_choiceSchemaNode, m_dsProfileSchemaNode, CHOICE_NODE, null, true);
        when(m_choiceSchemaNode.getDefaultCase()).thenReturn(Optional.of(m_choiceCaseNode));
        initNode(m_choiceCaseNode, m_choiceSchemaNode, CASE_NODE, null, true);
        initNode(m_leafWithEmptyType, m_choiceCaseNode, LEAF_WITH_EMPTY_TYPE, mock(EmptyTypeDefinition.class), true);
        
        SchemaPath path = m_dsProfileSchemaNode.getPath();
        when(m_modelNode.getModelNodeSchemaPath()).thenReturn(path);
        when(m_expValidator.validateWhenConditionOnModule(m_modelNode, m_leafWithEmptyType)).thenReturn(true);
        AnvExtensions ignoreDefaultExt = AnvExtensions.IGNORE_DEFAULT;
        QName ignoreDefault = QName.create(URI.create(ignoreDefaultExt.getExtensionNamespace()), ignoreDefaultExt.getRevision(), ignoreDefaultExt.getName());
        when(m_extensionDef.getQName()).thenReturn(ignoreDefault );
        when(m_unKnownSchemaNode.getExtensionDefinition()).thenReturn(m_extensionDef);
        
    }
    
    private void initNode(DataSchemaNode node, DataSchemaNode parent, String localName, TypeDefinition type, boolean isConfiguration) {
        when(node.getQName()).thenReturn(qname(localName));
        when(node.isConfiguration()).thenReturn(isConfiguration);
        SchemaPath path = null;
        if (parent == null) {
            path = SchemaPath.create(true, qname(localName));
        }
        else {
            path = parent.getPath().createChild(qname(localName));
            if (parent instanceof DataNodeContainer) {
                Collection<DataSchemaNode> childNodes = ((DataNodeContainer)parent).getChildNodes();
                childNodes.add(node);                
            }
            else if (parent instanceof ChoiceSchemaNode && node instanceof CaseSchemaNode) {
                ChoiceSchemaNode choiceNode = (ChoiceSchemaNode)parent;
                SortedMap<QName,CaseSchemaNode> cases = choiceNode.getCases();
                cases.put(qname(localName), (CaseSchemaNode)node); 
            }
        }
        when(node.getPath()).thenReturn(path);
        when(m_schemaRegistry.getDataSchemaNode(path)).thenReturn(node);
        
        if (node instanceof DataNodeContainer) {
            when(((DataNodeContainer)node).getChildNodes()).thenReturn(new ArrayList<DataSchemaNode>());            
        }
        if (node instanceof ChoiceSchemaNode) {
            when(((ChoiceSchemaNode)node).getCases()).thenReturn(new TreeMap<QName, CaseSchemaNode>());            
        }
        if (node instanceof LeafSchemaNode) {
            when(((LeafSchemaNode)node).getType()).thenReturn(type);                        
        }
    }

    /**
     * Test that when creating down-stream-profile:
     *  - default value for data-rate is added
     *  - the empty node for leaf-with-empty-type is added
     */
    @Test
    public void testProcessMissingDataWhenCreate() {
        EditContainmentNode editContainmentNode = new EditContainmentNode(qname(DOWN_STREAM_PROFILE), EditConfigOperations.CREATE);
        editContainmentNode.addMatchNode(qname(NAME),new GenericConfigAttribute(NAME, BBF_FAST_NAMESPACE, "ds_profile1"));
        //set default value in schema
        TypeDefinition type = mock(Int64TypeDefinition.class);
        when(type.getBaseType()).thenReturn(BaseTypes.int64Type());
        when(type.getDefaultValue()).thenReturn(Optional.of("0xFFFFFFFF"));
        when(m_dataRateLeafNode.getType()).thenReturn(type);
        
        EditContainmentNode interceptedContainmentNode = m_addDefaultDataInterceptor.processMissingData(editContainmentNode, m_modelNode);
        
        assertEquals(2, interceptedContainmentNode.getChangeNodes().size());
        assertEquals("4294967295", interceptedContainmentNode.getChangeNode(qname(DATA_RATE)).getValue());
        assertEquals("", interceptedContainmentNode.getChangeNode(qname(LEAF_WITH_EMPTY_TYPE)).getValue());
    }
    
    @Test
    public void testProcessMissingData_DisabledNodes() {
        EditContainmentNode editContainmentNode = new EditContainmentNode(qname(DOWN_STREAM_PROFILE), EditConfigOperations.CREATE);
        editContainmentNode.addMatchNode(qname(NAME),new GenericConfigAttribute(NAME, BBF_FAST_NAMESPACE, "ds_profile1"));
        editContainmentNode.addDisabledDefaultCreationNode(qname(DATA_RATE));
        //set default value in schema
        TypeDefinition type = mock(Int64TypeDefinition.class);
        when(type.getBaseType()).thenReturn(BaseTypes.int64Type());
        when(type.getDefaultValue()).thenReturn(Optional.of("0xFFFFFFFF"));
        when(m_dataRateLeafNode.getType()).thenReturn(type);
        
        EditContainmentNode interceptedContainmentNode = m_addDefaultDataInterceptor.processMissingData(editContainmentNode, m_modelNode);
        
        assertEquals(1, interceptedContainmentNode.getChangeNodes().size());
        assertNull(interceptedContainmentNode.getChangeNode(qname(DATA_RATE)));
        assertEquals("", interceptedContainmentNode.getChangeNode(qname(LEAF_WITH_EMPTY_TYPE)).getValue());
    }
    
    @SuppressWarnings("unchecked")
	@Test
    public void testProcessMissingDataWhenTypeDefIsIdentityRef() {
        EditContainmentNode editContainmentNode = new EditContainmentNode(qname(DOWN_STREAM_PROFILE), EditConfigOperations.CREATE);
        editContainmentNode.addMatchNode(qname(NAME), new GenericConfigAttribute(NAME, BBF_FAST_NAMESPACE, "ds_profile1"));
        //set default value in schema
        TypeDefinition identityrefTypeDefinition1 = mock(IdentityrefTypeDefinition.class);
        IdentitySchemaNode identitySchemaNode = mock(IdentitySchemaNode.class);
        when(m_dataRateLeafNode.getType()).thenReturn((TypeDefinition)identityrefTypeDefinition1);
        Set<IdentitySchemaNode> identitySchemaNodes = new HashSet<>();
        identitySchemaNodes.add(identitySchemaNode);
        when(((IdentityrefTypeDefinition)identityrefTypeDefinition1).getIdentities()).thenReturn(identitySchemaNodes);
        when(identitySchemaNode.getQName()).thenReturn(qname(DATA_RATE));
        
        //default value with prefix
        when(identityrefTypeDefinition1.getDefaultValue()).thenReturn(Optional.of("pf:dValue"));
        EditContainmentNode interceptedContainmentNode = m_addDefaultDataInterceptor.processMissingData(editContainmentNode, m_modelNode);
        
        assertEquals(2, interceptedContainmentNode.getChangeNodes().size());
        assertEquals("pf:dValue", interceptedContainmentNode.getChangeNode(qname(DATA_RATE)).getValue());
        assertEquals(BBF_FAST_NAMESPACE, interceptedContainmentNode.getChangeNode(qname(DATA_RATE)).getNamespace());
        assertEquals("", interceptedContainmentNode.getChangeNode(qname(LEAF_WITH_EMPTY_TYPE)).getValue());
        
        //default value without prefix
        when(identityrefTypeDefinition1.getDefaultValue()).thenReturn(Optional.of("dValue"));
        when(m_schemaRegistry.getPrefix(BBF_FAST_NAMESPACE)).thenReturn("pf");
        interceptedContainmentNode = m_addDefaultDataInterceptor.processMissingData(editContainmentNode, m_modelNode);
        
        assertEquals(2, interceptedContainmentNode.getChangeNodes().size());
        assertEquals("pf:dValue", interceptedContainmentNode.getChangeNode(qname(DATA_RATE)).getValue());
        ConfigLeafAttribute configLeafAttribute = interceptedContainmentNode.getChangeNode(qname(DATA_RATE)).getConfigLeafAttribute();
        assertTrue(configLeafAttribute instanceof IdentityRefConfigAttribute);
        assertEquals(BBF_FAST_NAMESPACE, configLeafAttribute.getDOMValue().lookupNamespaceURI("pf"));
        assertEquals(BBF_FAST_NAMESPACE, interceptedContainmentNode.getChangeNode(qname(DATA_RATE)).getNamespace());
        assertEquals("", interceptedContainmentNode.getChangeNode(qname(LEAF_WITH_EMPTY_TYPE)).getValue());
        
        //Default ignored
        when(m_dataRateLeafNode.getUnknownSchemaNodes()).thenReturn(Arrays.asList(m_unKnownSchemaNode));
        when(m_choiceSchemaNode.getUnknownSchemaNodes()).thenReturn(Arrays.asList(m_unKnownSchemaNode));
        interceptedContainmentNode = m_addDefaultDataInterceptor.processMissingData(editContainmentNode, m_modelNode);
        assertNull(interceptedContainmentNode.getChangeNode(qname(DATA_RATE)));
        assertNull(interceptedContainmentNode.getChangeNode(qname(LEAF_WITH_EMPTY_TYPE)));
    }

    /**
     *  container match-criteria -> choice frame-filter -> case any-frame (Default case) -> leaf any-frame
     *                                                  -> case destination-mac-address -> case ipv4-multicast-address + 7 other cases.
     */
    @Test
    public void testProcessMissingDataNestedChoiceCaseNodes(){
        EditContainmentNode oldEditData = new EditContainmentNode(MATCH_CRITERIA_QNAME, EditConfigOperations.MERGE);
        EditChangeNode ipv4EditData = new EditChangeNode(IP4V_MULTICAST_ADDR_QNAME, new GenericConfigAttribute(IPV4_MULTICAST_ADDRESS, BBF_SUB_INTERFACE_TAGGING,""));
        oldEditData.addChangeNode(ipv4EditData);

        mockChoiceCaseNodes();

        EditContainmentNode interceptedContainmentNode = m_addDefaultDataInterceptor.processMissingData(oldEditData, m_modelNode);

        assertEquals(1, interceptedContainmentNode.getChangeNodes().size());
        assertEquals("", interceptedContainmentNode.getChangeNode(IP4V_MULTICAST_ADDR_QNAME).getValue());
        assertEquals(BBF_SUB_INTERFACE_TAGGING, interceptedContainmentNode.getChangeNode(IP4V_MULTICAST_ADDR_QNAME).getNamespace());

        // Case 2: No cases under frame-filter. Default case needs to be created
        oldEditData = new EditContainmentNode(MATCH_CRITERIA_QNAME, EditConfigOperations.MERGE);

        mockChoiceCaseNodes();

        interceptedContainmentNode = m_addDefaultDataInterceptor.processMissingData(oldEditData, m_modelNode);

        assertEquals(1, interceptedContainmentNode.getChangeNodes().size());
        assertEquals("", interceptedContainmentNode.getChangeNode(ANY_FRAME_QNAME).getValue());
        assertEquals(BBF_SUB_INTERFACE_TAGGING, interceptedContainmentNode.getChangeNode(ANY_FRAME_QNAME).getNamespace());
    }

    private void mockChoiceCaseNodes() {
        when(m_modelNode.getModelNodeSchemaPath()).thenReturn(MATCH_CRITERIA_SCHEMAPATH);

        // Match-criteria
        ContainerSchemaNode matchCriteriaSchemaNode = mock(ContainerSchemaNode.class);
        when(m_schemaRegistry.getDataSchemaNode(MATCH_CRITERIA_SCHEMAPATH)).thenReturn(matchCriteriaSchemaNode);

        // Frame-filter
        ChoiceSchemaNode frameFilterChoiceCaseNode = mock(ChoiceSchemaNode.class);
        when(frameFilterChoiceCaseNode.isConfiguration()).thenReturn(true);
        when(matchCriteriaSchemaNode.getChildNodes()).thenReturn(Collections.singleton(frameFilterChoiceCaseNode));

        // Frame-filter cases - any filter and dest-mac-address
        SortedMap<QName, CaseSchemaNode> frameFilterCases = new TreeMap<>();
        CaseSchemaNode anyFilterCaseNode = mock(CaseSchemaNode.class);
        CaseSchemaNode destMacAddCaseNode = mock(CaseSchemaNode.class);
        frameFilterCases.put(ANY_FRAME_QNAME, anyFilterCaseNode);
        frameFilterCases.put(DEST_MAC_ADD_QNAME, destMacAddCaseNode);

        // any-filter is the default case
        when(frameFilterChoiceCaseNode.getCases()).thenReturn(frameFilterCases);
        when(frameFilterChoiceCaseNode.getDefaultCase()).thenReturn(Optional.of(anyFilterCaseNode));

        LeafSchemaNode anyFilterLeafNode = mock(LeafSchemaNode.class);
        when(anyFilterCaseNode.getChildNodes()).thenReturn(Collections.singleton(anyFilterLeafNode));
        when(anyFilterLeafNode.isConfiguration()).thenReturn(true);
        when(anyFilterLeafNode.getPath()).thenReturn(ANY_FRAME_SCHEMAPATH);
        when(m_schemaRegistry.getDataSchemaNode(ANY_FRAME_SCHEMAPATH)).thenReturn(anyFilterLeafNode);
        TypeDefinition emptyType = mock(EmptyTypeDefinition.class);
        when(anyFilterLeafNode.getType()).thenReturn(emptyType);

        // dest-mac-address has ipv4-multicast-address + other cases
        ChoiceSchemaNode macAddrChoiceSchemaNode = mock(ChoiceSchemaNode.class);
        when(destMacAddCaseNode.getChildNodes()).thenReturn(Collections.singleton(macAddrChoiceSchemaNode));

        CaseSchemaNode anyMulticastMacCaseNode = mock(CaseSchemaNode.class);
        LeafSchemaNode anyMulticastMacLeafNode = mock(LeafSchemaNode.class);
        when(anyMulticastMacCaseNode.getChildNodes()).thenReturn(Collections.singleton(anyMulticastMacLeafNode));

        CaseSchemaNode ipv4MulticastMacCaseNode = mock(CaseSchemaNode.class);
        LeafSchemaNode ipv4MulticastMacLeafNode = mock(LeafSchemaNode.class);
        when(ipv4MulticastMacLeafNode.getQName()).thenReturn(IP4V_MULTICAST_ADDR_QNAME);
        when(ipv4MulticastMacCaseNode.getChildNodes()).thenReturn(Collections.singleton(ipv4MulticastMacLeafNode));
        SortedMap<QName, CaseSchemaNode> macAddrCases = new TreeMap<>();
        macAddrCases.put(ANY_MULTICAST_QNAME, anyMulticastMacCaseNode);
        macAddrCases.put(IPV4_MULTICAST_QNAME, ipv4MulticastMacCaseNode);
        when(macAddrChoiceSchemaNode.getCases()).thenReturn(macAddrCases);

        when(m_expValidator.validateWhenConditionOnModule(m_modelNode, anyFilterLeafNode)).thenReturn(true);
        when(anyFilterLeafNode.getQName()).thenReturn(ANY_FRAME_QNAME);
    }
    
    @Test
    public void testProcessMissingDataWhenThrowEditConfigException() {
        System.setProperty(SchemaRegistryUtil.ENABLE_MOUNT_POINT, "true");
        EditContainmentNode editContainmentNode = new EditContainmentNode(QName.create(DEVICE_HOLDER_NAMESPACE,DEVICE_SPECIFIC_DATA), 
                EditConfigOperations.CREATE);
        
        SchemaPath path = SchemaPath.create(true, QName.create(DEVICE_HOLDER_NAMESPACE, DEVICE_SPECIFIC_DATA));
        
        when(m_schemaRegistry.getDataSchemaNode(path)).thenReturn(m_deviceSpecificDataSchemaNode);
        when(m_modelNode.getModelNodeSchemaPath()).thenReturn(path);
        when(m_modelNode.hasSchemaMount()).thenReturn(true);
        NetconfRpcError error = new NetconfRpcError(DATA_MISSING, Application, Error, "Mandatory leaf hardware-type is missing");
        GetException getException = new GetException(error);
        when(m_modelNode.getMountRegistry()).thenThrow(getException);
        
        
        NetconfRpcError rpcError = new NetconfRpcError(DATA_MISSING, Application, Error, "Mandatory leaf hardware-type is missing");
        EditConfigException editConfigException = new EditConfigException(rpcError);
        m_expectedException.expect(new BaseMatcher<EditConfigException>(){
            
            @Override
            public boolean matches(Object item) {
                return item instanceof EditConfigException && ((EditConfigException)item).getRpcError().equals(rpcError);
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(editConfigException);
            }});
        
        m_addDefaultDataInterceptor.processMissingData(editContainmentNode, m_modelNode);
    }
    
    

    @After
    public void tearDown() {
        m_addDefaultDataInterceptor.destroy();
        System.setProperty(SchemaRegistryUtil.ENABLE_MOUNT_POINT, "false");
    }
}
