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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidationContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DataStoreIntegrityService;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

@RunWith(RequestScopeJunitRunner.class)
public class DataStoreIntegrityServiceTest {

    @Mock
    private SchemaRegistry m_registry;
    @Mock
    private NetconfClientInfo m_info;
    @Mock
    private NetconfServer m_server;
    @Mock
    private ModelNodeHelperRegistry m_mnHelperRegistry;
    @Mock
    private ConfigAttributeHelper m_attributeHelper;
    private DataStoreIntegrityService m_service;
    private EditConfigRequest m_request;
    private DSValidationContext m_context;

    @Before
    public void before() throws GetAttributeException {
        MockitoAnnotations.initMocks(this);
        String dmNS = "http://www.test-company.com/solutions/anv";
        String dhNS = "http://www.test-company.com/solutions/anv-device-holders";
        String interfaceNS = "urn:ietf:params:xml:ns:yang:ietf-interfaces";
        String subInterfaceNS = "urn:bbf:yang:bbf-sub-interface-tagging";

        QName inlineFrameProcessingQName = QName.create(interfaceNS, "inline-frame-processing");
        QName egressRewriteQName = QName.create(interfaceNS, "egress-rewrite");
        QName popTagsQName = QName.create(subInterfaceNS, "pop-tags");
        QName interfaceNameQName = QName.create(interfaceNS, "name");
        QName interfaceQName = QName.create(interfaceNS, "interface");
        QName interfacesQName = QName.create(interfaceNS, "interfaces");
        QName dsdQName = QName.create(dhNS, "device-specific-data");
        QName deviceQName = QName.create(dhNS, "device");
        QName deviceIdQName = QName.create(dhNS, "device-id");
        QName deviceManagerQName = QName.create(dmNS, "device-manager");

        DataSchemaNode interfacesNode = null;
        DataSchemaNode dsdNode = null;
        DataSchemaNode deviceNode = null;
        DataSchemaNode dmNode = null;
        DataSchemaNode inlineFPNode = mock(ContainerSchemaNode.class);

        List<ModelNodeRdn> rdns = new ArrayList<>();
        rdns.add(new ModelNodeRdn(ModelNodeRdn.CONTAINER, dmNS, "device-manager"));
        rdns.add(new ModelNodeRdn(ModelNodeRdn.CONTAINER, dhNS, "device"));
        rdns.add(new ModelNodeRdn(deviceIdQName, "device1"));
        rdns.add(new ModelNodeRdn(ModelNodeRdn.CONTAINER, dhNS, "device-specific-data"));
        rdns.add(new ModelNodeRdn(ModelNodeRdn.CONTAINER, interfaceNS, "interfaces"));
        rdns.add(new ModelNodeRdn(ModelNodeRdn.CONTAINER, interfaceNS, "interface"));
        rdns.add(new ModelNodeRdn(interfaceNameQName, "interface1"));
        rdns.add(new ModelNodeRdn(ModelNodeRdn.CONTAINER, interfaceNS, "inline-frame-processing"));
        rdns.add(new ModelNodeRdn(ModelNodeRdn.CONTAINER, interfaceNS, "egress-rewrite"));
        rdns.add(new ModelNodeRdn(ModelNodeRdn.NAME, subInterfaceNS, "pop-tags"));

        ModelNodeId popTagsId = new ModelNodeId(rdns);
        ModelNodeId egressRewriteId = popTagsId.getParentId();
        ModelNodeId inlineFrameProcessingId = egressRewriteId.getParentId();
        ModelNodeId nameId = inlineFrameProcessingId.getParentId();
        ModelNodeId interfaceId = nameId.getParentId();
        ModelNodeId interfacesId = interfaceId.getParentId();
        ModelNodeId dsdId = interfacesId.getParentId();
        ModelNodeId deviceId = dsdId.getParentId();
        ModelNodeId dmId = deviceId.getParentId();

        SchemaPath popTagsPath = SchemaPath.create(true,deviceManagerQName, deviceQName, dsdQName, interfacesQName, interfaceQName, inlineFrameProcessingQName, egressRewriteQName, popTagsQName);
        SchemaPath egressRewritePath = popTagsPath.getParent(); 
        SchemaPath inlineFrameProcessingPath = egressRewritePath.getParent(); 
        SchemaPath interfacePath = SchemaPath.create(true, deviceManagerQName, deviceQName, dsdQName, interfacesQName, interfaceQName);
        SchemaPath interfacesPath = SchemaPath.create(true, deviceManagerQName, deviceQName, dsdQName, interfacesQName);
        SchemaPath dsdPath = interfacesPath.getParent();
        SchemaPath devicePath = dsdPath.getParent();
        SchemaPath dmPath = devicePath.getParent();

        when(m_registry.getDataSchemaNode(inlineFrameProcessingPath)).thenReturn(inlineFPNode);
        when(m_registry.getDataSchemaNode(interfacesPath)).thenReturn(interfacesNode);
        when(m_registry.getDataSchemaNode(dsdPath)).thenReturn(dsdNode);
        when(m_registry.getDataSchemaNode(devicePath)).thenReturn(deviceNode);
        when(m_registry.getDataSchemaNode(dmPath)).thenReturn(dmNode);
        when(m_registry.getDescendantSchemaPath(egressRewritePath, popTagsQName)).thenReturn(popTagsPath);
        when(m_registry.getDescendantSchemaPath(interfacePath, inlineFrameProcessingQName)).thenReturn(inlineFrameProcessingPath);
        when(m_registry.getDescendantSchemaPath(interfacesPath, interfaceQName)).thenReturn(interfacePath);
        when(m_registry.getDescendantSchemaPath(devicePath, dsdQName)).thenReturn(dsdPath);

        m_context = new DSValidationContext();

        ModelNode interfaceModelNode = mock(ModelNodeWithAttributes.class);
        ModelNode inlineFrameProcessingModelNode = mock(ModelNodeWithAttributes.class);
        ModelNode egressRewriteModelNode = mock(ModelNodeWithAttributes.class);
        ModelNodeWithAttributes interfacesModelNode = mock(ModelNodeWithAttributes.class);
        ModelNode deviceSpecificDataModelNode = mock(ModelNode.class);
        ModelNodeWithAttributes deviceModelNode = mock(ModelNodeWithAttributes.class);
        ModelNode deviceManagerModelNode = mock(ModelNode.class);
        when((inlineFrameProcessingModelNode.getQName())).thenReturn(inlineFrameProcessingQName);
        when((interfaceModelNode.getQName())).thenReturn(interfaceQName);
        when((egressRewriteModelNode.getQName())).thenReturn(egressRewriteQName);
        when((interfacesModelNode.getQName())).thenReturn(interfacesQName);
        when((deviceSpecificDataModelNode.getQName())).thenReturn(dsdQName);
        when((deviceModelNode.getQName())).thenReturn(deviceQName);
        when((deviceManagerModelNode.getQName())).thenReturn(deviceManagerQName);

        when(egressRewriteModelNode.getParent()).thenReturn(inlineFrameProcessingModelNode);
        when(inlineFrameProcessingModelNode.getParent()).thenReturn(interfaceModelNode);
        when(interfaceModelNode.getParent()).thenReturn(interfacesModelNode);
        when(interfacesModelNode.getParent()).thenReturn(deviceSpecificDataModelNode);
        when(deviceSpecificDataModelNode.getParent()).thenReturn(deviceModelNode);
        when(deviceModelNode.getParent()).thenReturn(deviceManagerModelNode);

        when(interfaceModelNode.getSchemaRegistry()).thenReturn(m_registry);
        when(inlineFrameProcessingModelNode.getSchemaRegistry()).thenReturn(m_registry);
        when(egressRewriteModelNode.getSchemaRegistry()).thenReturn(m_registry);
        when(interfacesModelNode.getSchemaRegistry()).thenReturn(m_registry);
        when(deviceSpecificDataModelNode.getSchemaRegistry()).thenReturn(m_registry);
        when(deviceModelNode.getSchemaRegistry()).thenReturn(m_registry);
        when(deviceManagerModelNode.getSchemaRegistry()).thenReturn(m_registry);

        when(egressRewriteModelNode.getModelNodeSchemaPath()).thenReturn(egressRewritePath);
        when(inlineFrameProcessingModelNode.getModelNodeSchemaPath()).thenReturn(inlineFrameProcessingPath);
        when(interfaceModelNode.getModelNodeSchemaPath()).thenReturn(interfacePath);
        when(interfacesModelNode.getModelNodeSchemaPath()).thenReturn(interfacesPath);
        when(deviceSpecificDataModelNode.getModelNodeSchemaPath()).thenReturn(dsdPath);
        when(deviceModelNode.getModelNodeSchemaPath()).thenReturn(devicePath);
        when(deviceManagerModelNode.getModelNodeSchemaPath()).thenReturn(dmPath);

        when(egressRewriteModelNode.getModelNodeId()).thenReturn(egressRewriteId);
        when(inlineFrameProcessingModelNode.getModelNodeId()).thenReturn(inlineFrameProcessingId);
        when(interfaceModelNode.getModelNodeId()).thenReturn(interfaceId);
        when(interfacesModelNode.getModelNodeId()).thenReturn(interfacesId);
        when(deviceSpecificDataModelNode.getModelNodeId()).thenReturn(dsdId);
        when(deviceModelNode.getModelNodeId()).thenReturn(deviceId);
        when(deviceManagerModelNode.getModelNodeId()).thenReturn(dmId);

        when(egressRewriteModelNode.getMountModelNodeHelperRegistry()).thenReturn(m_mnHelperRegistry);
        when(inlineFrameProcessingModelNode.getMountModelNodeHelperRegistry()).thenReturn(m_mnHelperRegistry);
        when(interfaceModelNode.getMountModelNodeHelperRegistry()).thenReturn(m_mnHelperRegistry);
        when(interfacesModelNode.getMountModelNodeHelperRegistry()).thenReturn(m_mnHelperRegistry);
        when(deviceSpecificDataModelNode.getMountModelNodeHelperRegistry()).thenReturn(m_mnHelperRegistry);
        when(deviceModelNode.getMountModelNodeHelperRegistry()).thenReturn(m_mnHelperRegistry);
        when(deviceManagerModelNode.getMountModelNodeHelperRegistry()).thenReturn(m_mnHelperRegistry);

        Map<QName, ConfigAttributeHelper> deviceIdKeys = new HashMap<>();
        deviceIdKeys.put(deviceIdQName, m_attributeHelper);
        GenericConfigAttribute value = new GenericConfigAttribute("device-id", dhNS, "device1");
        when(m_attributeHelper.getValue(deviceModelNode)).thenReturn(value);
        
        Map<QName, ConfigAttributeHelper> interfaceNameKey = new HashMap<>();
        interfaceNameKey.put(interfaceNameQName, m_attributeHelper);
        GenericConfigAttribute nameValue = new GenericConfigAttribute("name", dhNS, "interface1");
        when(m_attributeHelper.getValue(interfaceModelNode)).thenReturn(nameValue);

        when(m_mnHelperRegistry.getNaturalKeyHelpers(interfacePath)).thenReturn(interfaceNameKey);
        when(m_mnHelperRegistry.getNaturalKeyHelpers(interfacesPath)).thenReturn(Collections.emptyMap());
        when(m_mnHelperRegistry.getNaturalKeyHelpers(dsdPath)).thenReturn(Collections.emptyMap());
        when(m_mnHelperRegistry.getNaturalKeyHelpers(devicePath)).thenReturn(deviceIdKeys);
        when(m_mnHelperRegistry.getNaturalKeyHelpers(dmPath)).thenReturn(Collections.emptyMap());

        m_context.recordForCreate(interfacesModelNode, interfaceQName);
        m_context.recordForMerge(deviceModelNode, dsdQName);
        m_context.recordForMerge(egressRewriteModelNode, popTagsQName);
        m_context.recordForMerge(interfaceModelNode, inlineFrameProcessingQName);
        m_context.recordDefaultValue(inlineFrameProcessingPath, interfaceModelNode, null);
        m_context.recordDefaultValue(popTagsPath, egressRewriteModelNode, null);
        m_context.recordDefaultValue(dsdPath, deviceModelNode, null);
        m_service = new DataStoreIntegrityServiceImpl(m_server);
        m_request = new EditConfigRequest();
    }

    @Test
    public void testCreateOrDeleteNodes() throws Exception {
        List<EditConfigRequest> internalRequests = m_service.createInternalEditRequests(m_request, m_info, m_context);
        assertEquals(false, internalRequests.get(0).isUploadToPmaRequest());
        Element actual = internalRequests.get(0).getConfigElement().getXmlElement();
        TestUtil.assertXMLEquals(getExpected(), actual);
    }

    @Test
    public void testCreateOrDeleteNodesWhenUploadConfigTrue() throws Exception {
        m_request.setUploadToPmaRequest();
        List<EditConfigRequest> internalRequests = m_service.createInternalEditRequests(m_request, m_info, m_context);
        assertEquals(true, internalRequests.get(0).isUploadToPmaRequest());
        Element actual = internalRequests.get(0).getConfigElement().getXmlElement();
        TestUtil.assertXMLEquals(getExpected(), actual);
    }

    private Element getExpected() throws NetconfMessageBuilderException {
        String exp = "<config xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<device-manager xmlns=\"http://www.test-company.com/solutions/anv\">"
                + "<device xmlns=\"http://www.test-company.com/solutions/anv-device-holders\">"
                + "<device-id>device1</device-id><device-specific-data xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" ns0:operation=\"merge\">"
                + "<interfaces xmlns=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">"
                + "<interface>"
                + "<inline-frame-processing ns0:operation=\"merge\">"
                + "<egress-rewrite>"
                + "<pop-tags ns0:operation=\"merge\" xmlns=\"urn:bbf:yang:bbf-sub-interface-tagging\"/>"
                + "</egress-rewrite>"
                + "</inline-frame-processing>"
                + "<name>interface1</name>"
                + "</interface>"
                + "</interfaces>"
                + "</device-specific-data>"
                + "</device>"
                + "</device-manager>"
                + "</config>";
        return DocumentUtils.stringToDocument(exp).getDocumentElement();
    }

}
