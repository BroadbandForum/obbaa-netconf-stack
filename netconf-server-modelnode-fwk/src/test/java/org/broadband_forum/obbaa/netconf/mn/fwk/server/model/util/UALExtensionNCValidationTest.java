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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.transformToElement;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.UALExtensionUtil.valuesToString;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.UALExtensionUtil.getUALValuesForEditConfig;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.UALExtensionUtil.hasUALDisabledExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcRequest;
import org.broadband_forum.obbaa.netconf.api.util.DataPath;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.DataPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractDataStoreValidatorTest;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@RunWith(RequestScopeJunitRunner.class)
public class UALExtensionNCValidationTest extends AbstractDataStoreValidatorTest {
    private  static final String UAL_YANG_NAMESPACE = "urn:org:bbf:anv:ual";
    private  static final String UAL_YANG_REVISION = "2020-09-04";

    protected static final QName DEVICE_MANAGER_QNAME = QName.create("urn:org:bbf:anv:ual", "2020-09-04", "device-manager");
    protected static final SchemaPath DEVICE_MANAGER_SCHEMA_PATH = SchemaPath.create(true, DEVICE_MANAGER_QNAME);
    protected static final QName USERS_QNAME = QName.create("urn:org:bbf:anv:ual", "2020-09-04", "users");
    protected static final SchemaPath USERS_SCHEMA_PATH = SchemaPath.create(true, USERS_QNAME);
    protected static final QName PLATFORM_QNAME = QName.create("urn:org:bbf:anv:ual", "2020-09-04", "platform");
    protected static final SchemaPath PLATFORM_SCHEMA_PATH = SchemaPath.create(true, PLATFORM_QNAME);

    protected void addRootNodeHelpers() {
        super.addRootNodeHelpers();
        addRootContainerNodeHelpers(DEVICE_MANAGER_SCHEMA_PATH);
        addRootContainerNodeHelpers(USERS_SCHEMA_PATH);
        addRootContainerNodeHelpers(PLATFORM_SCHEMA_PATH);
    }

    @Test
    public void testUALExtenstion_ListNode() throws Exception {
        getModelNode();
        String editRequestXml = "<device-manager xmlns=\"urn:org:bbf:anv:ual\">"
                + " <device>"
                + "		<device-id>TestDevice</device-id>"
                + "     <device-specific-data>"
                + "         <forwarders>"
                + "             <forwarder>"
                + "                 <key>forwarder1</key>"
                + "             </forwarder>"
                + "         </forwarders>"
                + "     </device-specific-data>"
                + " </device>"
                + "</device-manager>";

        EditConfigRequest request = createRequestFromString(editRequestXml);
        Set<String> applicationNames = new HashSet<String>();
        List<String> argumentNames = new ArrayList<>();
        getUALValuesForEditConfig(m_schemaRegistry,request.getConfigElement().getXmlElement(), applicationNames,argumentNames );
        Assert.assertEquals(1,applicationNames.size());
        Assert.assertEquals("Forwarder Manager", valuesToString(applicationNames));
        Assert.assertEquals("key=forwarder1", valuesToString(argumentNames));
    }

    @Test
    public void testUALExtenstion_ListNodeWithMultipleEntries() throws Exception {
        getModelNode();
        String editRequestXml = "<device-manager xmlns=\"urn:org:bbf:anv:ual\">"
                + " <device>"
                + "		<device-id>TestDevice</device-id>"
                + "     <device-specific-data>"
                + "         <forwarders>"
                + "             <forwarder>"
                + "                 <key>forwarder1</key>"
                + "             </forwarder>"
                + "             <forwarder>"
                + "                 <key>forwarder2</key>"
                + "             </forwarder>"
                + "         </forwarders>"
                + "     </device-specific-data>"
                + " </device>"
                + "</device-manager>";

        EditConfigRequest request = createRequestFromString(editRequestXml);
        Set<String> applicationNames = new LinkedHashSet<String>();
        applicationNames.add("testApp");
        List<String> argumentNames = new ArrayList<>();
        getUALValuesForEditConfig(m_schemaRegistry,request.getConfigElement().getXmlElement(), applicationNames, argumentNames );
        Assert.assertEquals(2,applicationNames.size());
        Assert.assertEquals("[testApp, Forwarder Manager]", valuesToString(applicationNames));
        Assert.assertEquals("[key=forwarder1, key=forwarder2]", valuesToString(argumentNames));
    }

    @Test
    public void testUALExtenstionOnMultipleNode_SameTreeHierarchy() throws Exception {
        getModelNode();
        String editRequestXml = "<device-manager xmlns=\"urn:org:bbf:anv:ual\">"
                + " <device>"
                + "		<device-id>TestDevice</device-id>"
                + "     <device-specific-data>"
                + "         <interfaces>"
                + "             <interface>"
                + "                 <name>interface1</name>"
                + "             </interface>"
                + "             <interface>"
                + "                 <name>interface2</name>"
                + "             </interface>"
                + "         </interfaces>"
                + "     </device-specific-data>"
                + " </device>"
                + "</device-manager>";

        EditConfigRequest request = createRequestFromString(editRequestXml);
        Set<String> applicationNames = new LinkedHashSet<>();
        applicationNames.add("testApp");
        List<String> argumentNames = new ArrayList<>();
        getUALValuesForEditConfig(m_schemaRegistry,request.getConfigElement().getXmlElement(), applicationNames, argumentNames );
        Assert.assertEquals(3,applicationNames.size());
        Assert.assertEquals("[testApp, Interfaces Manager, Interface Manager]", valuesToString(applicationNames));
        Assert.assertEquals("[container_node=interfaces, name=interface1, name=interface2]", valuesToString(argumentNames));
    }

    @Test
    public void testUALExtensionWithMultipleListNodes_SkipIFDepthExceed() throws Exception {
        getModelNode();
        String editRequestXml = "<device-manager xmlns=\"urn:org:bbf:anv:ual\">"
                + " <device>"
                + "		<device-id>TestDevice</device-id>"
                + "     <device-specific-data>"
                + "         <scheduler-nodes>"
                + "             <scheduler-node>"
                + "                 <node-name>node1</node-name>"
                + "                 <child-scheduler-nodes>"
                + "                     <name>child-node</name>"
                + "                 </child-scheduler-nodes>"
                + "                 <child-scheduler-nodes>"
                + "                     <name>child-node1</name>"
                + "                 </child-scheduler-nodes>"
                + "             </scheduler-node>"
                + "         </scheduler-nodes>"
                + "     </device-specific-data>"
                + " </device>"
                + "</device-manager>";

        EditConfigRequest request = createRequestFromString(editRequestXml);
        Set<String> applicationNames = new HashSet<String>();
        List<String> argumentNames = new ArrayList<>();
        getUALValuesForEditConfig(m_schemaRegistry,request.getConfigElement().getXmlElement(), applicationNames, argumentNames );
        Assert.assertEquals(0,applicationNames.size());
        Assert.assertEquals(0,argumentNames.size());
    }

    @Test
    public void testUALAnnotation_ListWithMultiKeys() throws Exception {
        getModelNode();
        String editRequestXml = "<device-manager xmlns=\"urn:org:bbf:anv:ual\">"
                + " <device-multikey>"
                + "		<device-id>TestDevice</device-id>"
                + "		<hardware-type>GFast</hardware-type>"
                + "		<interface-version>1.0</interface-version>"
                + " </device-multikey>"
                + "</device-manager>";

        EditConfigRequest request = createRequestFromString(editRequestXml);
        Set<String> applicationNames = new LinkedHashSet<>();
        List<String> argumentNames = new ArrayList<>();
        getUALValuesForEditConfig(m_schemaRegistry,request.getConfigElement().getXmlElement(), applicationNames, argumentNames );
        Assert.assertEquals(1,applicationNames.size());
        Assert.assertEquals("Device Manager", valuesToString(applicationNames));
        Assert.assertEquals("[hardware-type=GFast, interface-version=1.0]", valuesToString(argumentNames));

        editRequestXml = "<device-manager xmlns=\"urn:org:bbf:anv:ual\">"
                + " <device-multikey>"
                + "		<device-id>TestDevice</device-id>"
                + "		<hardware-type>GFast</hardware-type>"
                + "		<interface-version>1.0</interface-version>"
                + " </device-multikey>"
                + " <device-multikey>"
                + "		<device-id>TestDevice1</device-id>"
                + "		<hardware-type>FGLT-B</hardware-type>"
                + "		<interface-version>19.06</interface-version>"
                + " </device-multikey>"
                + "</device-manager>";

        request = createRequestFromString(editRequestXml);
        applicationNames = new LinkedHashSet<>();
        applicationNames.add(null);
        applicationNames.add("");
        argumentNames = new ArrayList<>();
        getUALValuesForEditConfig(m_schemaRegistry,request.getConfigElement().getXmlElement(), applicationNames, argumentNames );
        Assert.assertEquals(3,applicationNames.size());
        Assert.assertEquals("Device Manager", valuesToString(applicationNames));
        Assert.assertEquals("[hardware-type=GFast, interface-version=1.0, hardware-type=FGLT-B, ...]", valuesToString(argumentNames));
    }

    @Test
    public void testUALAnnotation_ContainerAndList() throws Exception {
        getModelNode();
        String editRequestXml = "<device-manager xmlns=\"urn:org:bbf:anv:ual\">"
                + " <device-multikey>"
                + "		<device-id>TestDevice</device-id>"
                + "		<hardware-type>GFast</hardware-type>"
                + "		<interface-version>1.0</interface-version>"
                + " </device-multikey>"
                + " <device>"
                + "		<device-id>TestDevice</device-id>"
                + "     <device-specific-data>"
                + "         <forwarders>"
                + "             <forwarder>"
                + "                 <key>forwarder1</key>"
                + "             </forwarder>"
                + "             <forwarder>"
                + "                 <key>forwarder2</key>"
                + "             </forwarder>"
                + "         </forwarders>"
                + "         <interfaces>"
                + "             <interface>"
                + "                 <name>Itf1</name>"
                + "             </interface>"
                + "         </interfaces>"
                + "     </device-specific-data>"
                + " </device>"
                + "</device-manager>";

        EditConfigRequest request = createRequestFromString(editRequestXml);
        Set<String> applicationNames = new LinkedHashSet<>();
        applicationNames.add("testApp");
        List<String> argumentNames = new ArrayList<>();
        getUALValuesForEditConfig(m_schemaRegistry,request.getConfigElement().getXmlElement(), applicationNames,
                argumentNames );
        Assert.assertEquals(5, applicationNames.size());
        Assert.assertEquals("[testApp, Device Manager, Forwarder Manager, ...]", valuesToString(applicationNames));
        Assert.assertEquals("[hardware-type=GFast, interface-version=1.0, key=forwarder1, ...]",
                valuesToString(argumentNames));
    }

    @Test
    public void testUALAnnotation_ContainerAndListAndSkipExceedDepth() throws Exception {
        getModelNode();
        String editRequestXml = "<device-manager xmlns=\"urn:org:bbf:anv:ual\">"
                + " <device>"
                + "		<device-id>TestDevice</device-id>"
                + "     <device-specific-data>"
                + "         <scheduler-nodes>"
                + "             <scheduler-node>"
                + "                 <node-name>node1</node-name>"
                + "                 <child-scheduler-node>"
                + "                     <name>name1</name>"
                + "                 </child-scheduler-node>"
                + "             </scheduler-node>"
                + "         </scheduler-nodes>"
                + "         <forwarders>"
                + "             <forwarder>"
                + "                 <key>forwarder1</key>"
                + "             </forwarder>"
                + "             <forwarder>"
                + "                 <key>forwarder2</key>"
                + "             </forwarder>"
                + "         </forwarders>"
                + "         <itf:interfaces xmlns:itf=\"urn:org:bbf:anv:ual\">"
                + "             <itf:interface>"
                + "                 <itf:name>Itf1</itf:name>"
                + "             </itf:interface>"
                + "         </itf:interfaces>"
                + "     </device-specific-data>"
                + " </device>"
                + " <device-multikey>"
                + "		<device-id>TestDevice</device-id>"
                + "		<hardware-type>GFast</hardware-type>"
                + "		<interface-version>1.0</interface-version>"
                + " </device-multikey>"
                + "</device-manager>";

        EditConfigRequest request = createRequestFromString(editRequestXml);
        Set<String> applicationNames = new LinkedHashSet<>();
        List<String> argumentNames = new ArrayList<>();
        getUALValuesForEditConfig(m_schemaRegistry,request.getConfigElement().getXmlElement(), applicationNames, argumentNames );
        Assert.assertEquals(4, applicationNames.size());
        Assert.assertEquals("[Forwarder Manager, Interfaces Manager, Interface Manager, ...]", valuesToString(applicationNames));
        Assert.assertEquals("[key=forwarder1, key=forwarder2, container_node=interfaces, ...]", valuesToString(argumentNames));
    }

    @Test
    public void testUALAnnotation_WithMultipleContainer() throws Exception {
        getModelNode();
        String editRequestXml = "<config>" +
                "<device-manager xmlns=\"urn:org:bbf:anv:ual\">"
                + " <device>"
                + "		<device-id>TestDevice</device-id>"
                + "     <device-specific-data>"
                + "         <interfaces>"
                + "             <interface>"
                + "                 <name>Itf1</name>"
                + "             </interface>"
                + "         </interfaces>"
                + "     </device-specific-data>"
                + " </device>"
                + "</device-manager>"
                + "<users xmlns=\"urn:org:bbf:anv:ual\">"
                + "     <user>"
                + "         <first-name>bbf</first-name>"
                + "         <last-name>bbf1</last-name>"
                + "     </user>"
                + "</users>"
                + "<platform xmlns=\"urn:org:bbf:anv:ual\">"
                + "     <loggers>"
                + "         <logger-config>"
                + "             <application-name>anv</application-name>"
                + "         </logger-config>"
                + "     </loggers>"
                + "</platform>"
                + "</config>";

        EditConfigRequest request = new EditConfigRequest();
        request.setTargetRunning();
        request.setTestOption(EditConfigTestOptions.SET);
        EditConfigElement configElement = new EditConfigElement().setConfigElementContents(
                DocumentUtils.getChildElements(transformToElement(editRequestXml)));
        request.setConfigElement(configElement);

        Set<String> applicationNames = new LinkedHashSet<>();
        List<String> argumentNames = new ArrayList<>();
        getUALValuesForEditConfig(m_schemaRegistry,request.getConfigElement().getXmlElement(), applicationNames, argumentNames );
        Assert.assertEquals(4, applicationNames.size());
        Assert.assertEquals("[Interfaces Manager, Interface Manager, User Manager, ...]", valuesToString(applicationNames));
        Assert.assertEquals("[container_node=interfaces, name=Itf1, container_node=users, ...]",
                valuesToString(argumentNames));
    }

    @Test
    public void testUALExtensionWithDisabled_RPC() throws Exception {
        String rpcRequest = "<retrieve-devices-info xmlns=\"urn:org:bbf:anv:ual\">"+
                "<device-id>TestDevice</device-id>"+
                "</retrieve-devices-info>";
        NetconfRpcRequest netconfRpcRequest = new NetconfRpcRequest();
        netconfRpcRequest.setRpcInput(DocumentUtils.stringToDocument(rpcRequest).getDocumentElement());
        boolean isDisabled = hasUALDisabledExtension(m_schemaRegistry, netconfRpcRequest);
        Assert.assertTrue(isDisabled);
        String applicationName = UALExtensionUtil.getUalApplicationNameForActionOrRpcRequest(m_schemaRegistry, netconfRpcRequest);
        Assert.assertEquals(null, applicationName);
    }

    @Test
    public void testRPC_WithUALAnnotation() throws Exception {
        String rpcRequest = "<retrieve-devices-summary xmlns=\"urn:org:bbf:anv:ual\">"+
                "<device-id>TestDevice</device-id>"+
                "</retrieve-devices-summary>";
        NetconfRpcRequest netconfRpcRequest = new NetconfRpcRequest();
        netconfRpcRequest.setRpcInput(DocumentUtils.stringToDocument(rpcRequest).getDocumentElement());
        String applicationName = UALExtensionUtil.getUalApplicationNameForActionOrRpcRequest(m_schemaRegistry, netconfRpcRequest);
        Assert.assertEquals("Device Manager",applicationName);
        boolean isDisabled = hasUALDisabledExtension(m_schemaRegistry, netconfRpcRequest);
        Assert.assertFalse(isDisabled);
    }

    @Test
    public void testActionWithUAL() throws Exception {
        String request = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
                + "	<softwares xmlns=\"urn:org:bbf:anv:ual\">"
                + "		<software>"
                + "			<name>one</name>"
                + "         <add>"
                + "            <file>movie</file>"
                + "         </add>"
                + "		</software>"
                + "	</softwares>"
                + "</action>"
                + "</rpc>";

        ActionRequest actionRequest = DocumentToPojoTransformer
                .getAction(DocumentUtils.stringToDocument(request));
        QName softwaresQName = QName.create(UAL_YANG_NAMESPACE, UAL_YANG_REVISION, "softwares"); // skipped the choicePath
        QName softwareQName = QName.create(UAL_YANG_NAMESPACE, UAL_YANG_REVISION, "software");
        QName actionQName = QName.create(UAL_YANG_NAMESPACE, UAL_YANG_REVISION, "add");
        List<QName> qNameList = new ArrayList<>();
        qNameList.add(softwaresQName);
        qNameList.add(softwareQName);
        qNameList.add(actionQName);
        DataPath actionDataPath = DataPathUtil.convertToDataPath(qNameList);
        ActionDefinition actionDefinition =
                m_schemaRegistry.getActionDefinitionNode(actionDataPath);
        actionRequest.setActionDefinition(actionDefinition);
        String applicationName = UALExtensionUtil.getUalApplicationNameForActionOrRpcRequest(m_schemaRegistry, actionRequest);
        Assert.assertEquals("Software Manager", applicationName);
    }
    
    @Test
    public void tesAaction_WithUALdisabled() throws Exception {
        String request = "<rpc message-id=\"1\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"
                + "<action xmlns=\"urn:ietf:params:xml:ns:yang:1\">"
                + "	<softwares xmlns=\"urn:org:bbf:anv:ual\">"
                + "		<software>"
                + "			<name>one</name>"
                + "         <delete>"
                + "            <file>movie</file>"
                + "         </delete>"
                + "		</software>"
                + "	</softwares>"
                + "</action>"
                + "</rpc>";

        ActionRequest actionRequest = DocumentToPojoTransformer
                .getAction(DocumentUtils.stringToDocument(request));

        QName softwaresQName = QName.create(UAL_YANG_NAMESPACE, UAL_YANG_REVISION, "softwares"); // skipped the choicePath
        QName softwareQName = QName.create(UAL_YANG_NAMESPACE, UAL_YANG_REVISION, "software");
        QName actionQName = QName.create(UAL_YANG_NAMESPACE, UAL_YANG_REVISION, "delete");
        List<QName> qNameList = new ArrayList<>();
        qNameList.add(softwaresQName);
        qNameList.add(softwareQName);
        qNameList.add(actionQName);
        DataPath actionDataPath = DataPathUtil.convertToDataPath(qNameList);
        ActionDefinition actionDefinition =
                m_schemaRegistry.getActionDefinitionNode(actionDataPath);
        actionRequest.setActionDefinition(actionDefinition);
        String applicationName = UALExtensionUtil.getUalApplicationNameForActionOrRpcRequest(m_schemaRegistry, actionRequest);
        Assert.assertEquals(null, applicationName);
        boolean ualDisabled = hasUALDisabledExtension(m_schemaRegistry, actionRequest);
        Assert.assertTrue(ualDisabled);
    }
}
