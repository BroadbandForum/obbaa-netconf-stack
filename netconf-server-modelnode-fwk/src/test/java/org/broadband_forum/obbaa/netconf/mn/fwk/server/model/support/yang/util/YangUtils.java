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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.DocumentBuilderFactoryWithoutDTD;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryTraverser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaVerifierImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ValidationHint;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.HintDetails;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetConfServerImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelNodeDSMDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SchemaNodeConstraintValidatorRegistrar;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SchemaPathRegistrar;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SubsystemDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm.DsmModelNodeHelperDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.LocalSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.YangModelFactory;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.annotations.VisibleForTesting;

public class YangUtils {

    private static SchemaVerifierImpl m_verifier = new SchemaVerifierImpl();
    private static final String COMPONENT_ID_FORMAT = "%s?revision=%s";

    public static ModelNodeWithAttributes createInMemoryModelNode(String yangFilePath, SubSystem subSystem,
                                                                  ModelNodeHelperRegistry modelNodeHelperRegistry,
                                                                  SubSystemRegistry subSystemRegistry,
                                                                  SchemaRegistry schemaRegistry, ModelNodeDataStoreManager modelNodeDsm) throws ModelNodeInitException{

        try {
            Module module = deployInMemoryHelpers(yangFilePath, subSystem, modelNodeHelperRegistry, subSystemRegistry, schemaRegistry, modelNodeDsm);
            Collection<DataSchemaNode> containers = module.getChildNodes();
            for (DataSchemaNode node : containers) {
                if (node instanceof ContainerSchemaNode) {
                    ModelNodeWithAttributes modelNodeWithAttributes = new ModelNodeWithAttributes(node.getPath(),new ModelNodeId(), modelNodeHelperRegistry, subSystemRegistry, schemaRegistry, modelNodeDsm);
                    modelNodeDsm.createNode(modelNodeWithAttributes, new ModelNodeId());
                    return modelNodeWithAttributes;
                }
            }
        } catch (Exception e) {
            throw new ModelNodeInitException("could not deploy YangModelNodeFactory", e);
        }
        return null;
    }

    public static Module deployInMemoryHelpers(String yangFilePath, SubSystem subSystem,
                                               ModelNodeHelperRegistry modelNodeHelperRegistry,
                                               SubSystemRegistry subSystemRegistry,
                                               SchemaRegistry schemaRegistry, ModelNodeDataStoreManager modelNodeDsm) throws ModelNodeFactoryException, SchemaBuildException {
        return deployInMemoryHelpers(yangFilePath, subSystem, modelNodeHelperRegistry, subSystemRegistry, schemaRegistry, modelNodeDsm,
                new HashMap<>(), new HashMap<>());
    }

    public static Module deployInMemoryHelpers(String yangFilePath, SubSystem subSystem,
                                               ModelNodeHelperRegistry modelNodeHelperRegistry,
                                               SubSystemRegistry subSystemRegistry,
                                               SchemaRegistry schemaRegistry, ModelNodeDataStoreManager modelNodeDsm,
                                               Map<SchemaPath, ValidationHint> deviceHints, Map<SchemaPath, HintDetails> globalHints) throws ModelNodeFactoryException, SchemaBuildException {
        Module module = YangModelFactory.getInstance().loadModule(yangFilePath);
        traverseModule(subSystem, modelNodeHelperRegistry, subSystemRegistry,
				schemaRegistry, modelNodeDsm, module, deviceHints, globalHints, null);
        m_verifier.verify(schemaRegistry);
        return module;
    }

	private static void traverseModule(SubSystem subSystem,
                                       ModelNodeHelperRegistry modelNodeHelperRegistry,
                                       SubSystemRegistry subSystemRegistry, SchemaRegistry schemaRegistry,
                                       ModelNodeDataStoreManager modelNodeDsm, Module module, Map<SchemaPath, ValidationHint> deviceHints, Map<SchemaPath, HintDetails> globalHints, String componentId) {
		if(componentId == null) {
	        componentId = module.getRevision().isPresent() ? String.format(COMPONENT_ID_FORMAT, module.getName(),module.getRevision().get()):module.getName();
		}
        //deployHelpers subSystem
        Map<SchemaPath, SubSystem> subSystemMap = new HashMap<>();
        for(DataSchemaNode child : module.getChildNodes()){
            subSystemMap.put(child.getPath(), subSystem);
        }
        
        List<SchemaRegistryVisitor> visitors = new ArrayList<>();
        visitors.add(new SubsystemDeployer(subSystemRegistry, subSystemMap));
        visitors.add(new DsmModelNodeHelperDeployer(schemaRegistry, modelNodeDsm,
                modelNodeHelperRegistry, subSystemRegistry));
        visitors.add(new ModelNodeDSMDeployer(new ModelNodeDSMRegistryImpl(),
                modelNodeDsm));
        visitors.add(new SchemaPathRegistrar(schemaRegistry, modelNodeHelperRegistry, deviceHints, globalHints));
        visitors.add(new SchemaNodeConstraintValidatorRegistrar(schemaRegistry, modelNodeHelperRegistry, subSystemRegistry));
        SchemaRegistryTraverser traverser = new SchemaRegistryTraverser(componentId, visitors, schemaRegistry, module);
        traverser.traverse();
	}
    
    public static ModelNode createInMemoryModelNode(List<String> yangFilePaths, SubSystem subSystem,
            ModelNodeHelperRegistry modelNodeHelperRegistry,
            SubSystemRegistry subSystemRegistry,
            SchemaRegistry schemaRegistry, ModelNodeDataStoreManager modelNodeDsm) throws ModelNodeInitException, SchemaBuildException{

    	try {
    		SchemaContext context = deployInMemoryHelpers(yangFilePaths, subSystem, modelNodeHelperRegistry, subSystemRegistry, schemaRegistry, modelNodeDsm, null, null);
    		for (DataSchemaNode node : context.getChildNodes()) {
    			if (node instanceof ContainerSchemaNode) {
    				ModelNodeWithAttributes modelNodeWithAttributes = new ModelNodeWithAttributes(node.getPath(),new ModelNodeId(), modelNodeHelperRegistry, subSystemRegistry, schemaRegistry,modelNodeDsm);
    				return modelNodeWithAttributes;
    			}
    		}
    	} catch (ModelNodeFactoryException e) {
    		throw new ModelNodeInitException("could not deploy YangModelNodeFactory", e);
    	}
    	return null;
    }
    
    public static SchemaContext deployInMemoryHelpers(List<String> yangFileNames, SubSystem subSystem,
            ModelNodeHelperRegistry modelNodeHelperRegistry,
            SubSystemRegistry subSystemRegistry,
            SchemaRegistry schemaRegistry, ModelNodeDataStoreManager modelNodeDsm,
            Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations)
            throws ModelNodeFactoryException, SchemaBuildException {
        return deployInMemoryHelpers(yangFileNames, subSystem, modelNodeHelperRegistry, subSystemRegistry, schemaRegistry, modelNodeDsm,
                supportedFeatures, supportedDeviations, new HashMap<>(), new HashMap<>());
    }
    
    public static SchemaContext deployInMemoryHelpers(List<String> yangFileNames, SubSystem subSystem,
            ModelNodeHelperRegistry modelNodeHelperRegistry,
            SubSystemRegistry subSystemRegistry,
            SchemaRegistry schemaRegistry, ModelNodeDataStoreManager modelNodeDsm,
            Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations, String componentId)
            throws ModelNodeFactoryException, SchemaBuildException {
        return deployInMemoryHelpers(yangFileNames, subSystem, modelNodeHelperRegistry, subSystemRegistry, schemaRegistry, modelNodeDsm,
                supportedFeatures, supportedDeviations, new HashMap<>(), new HashMap<>(), componentId);
    }

    @VisibleForTesting
    public static SchemaContext deployInMemoryHelpers(List<String> yangFileNames, SubSystem subSystem,
            ModelNodeHelperRegistry modelNodeHelperRegistry,
            SubSystemRegistry subSystemRegistry,
            SchemaRegistry schemaRegistry, ModelNodeDataStoreManager modelNodeDsm,
            Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations, boolean isSchemaVerificationEnabled)
            throws ModelNodeFactoryException, SchemaBuildException {
        if (isSchemaVerificationEnabled) {
            m_verifier.setIsSchemaVerificationEnabledForTest(true);
        }
        return deployInMemoryHelpers(yangFileNames, subSystem,
                modelNodeHelperRegistry, subSystemRegistry, schemaRegistry, modelNodeDsm, supportedFeatures, null);

    }

    public static SchemaContext deployInMemoryHelpers(List<String> yangFileNames, SubSystem subSystem,
                                                      ModelNodeHelperRegistry modelNodeHelperRegistry,
                                                      SubSystemRegistry subSystemRegistry,
                                                      SchemaRegistry schemaRegistry, ModelNodeDataStoreManager modelNodeDsm,
                                                      Set<QName> supportedFeatures, Map<QName,Set<QName>> supportedDeviations,
                                                      Map<SchemaPath, ValidationHint> deviceHints, Map<SchemaPath, HintDetails> globalHints) throws ModelNodeFactoryException, SchemaBuildException {
        SchemaContext context = schemaRegistry.getSchemaContext();
        for(Module module : context.getModules()) {
	        traverseModule(subSystem, modelNodeHelperRegistry, subSystemRegistry,
					schemaRegistry, modelNodeDsm, module, deviceHints, globalHints, null);
        }
        m_verifier.verify(schemaRegistry);
        return context;
    }
    
    public static SchemaContext deployInMemoryHelpers(List<String> yangFileNames, SubSystem subSystem,
            ModelNodeHelperRegistry modelNodeHelperRegistry,
            SubSystemRegistry subSystemRegistry,
            SchemaRegistry schemaRegistry, ModelNodeDataStoreManager modelNodeDsm,
            Set<QName> supportedFeatures, Map<QName,Set<QName>> supportedDeviations,
            Map<SchemaPath, ValidationHint> deviceHints, Map<SchemaPath, HintDetails> globalHints, String componentId) throws ModelNodeFactoryException, SchemaBuildException {
			SchemaContext context = schemaRegistry.getSchemaContext();
			for(Module module : context.getModules()) {
				traverseModule(subSystem, modelNodeHelperRegistry, subSystemRegistry,schemaRegistry, modelNodeDsm, module, deviceHints, globalHints, componentId);
			}
			m_verifier.verify(schemaRegistry);
			return context;
	}

    public static Element loadAsXml(String filePath) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactoryWithoutDTD.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new File(filePath));
            return (Element) doc.getChildNodes().item(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void loadXmlDataIntoServer(NetConfServerImpl server, String xmlFilePath) {
        loadXmlDataIntoServer(server, xmlFilePath, StandardDataStores.RUNNING);
    }

    public static void loadXmlDataIntoServer(NetConfServerImpl server, String xmlFilePath, String dataStore) {
        Element rootElement = loadAsXml(xmlFilePath);
        EditConfigElement editConfigElement = new EditConfigElement();
        editConfigElement.getConfigElementContents().add(rootElement);
        EditConfigRequest request = (EditConfigRequest) new EditConfigRequest().setConfigElement(editConfigElement).setMessageId("1");
        request.setTarget(dataStore);
        NetConfResponse response = new NetConfResponse();
        server.onEditConfig(new NetconfClientInfo("test",1), request, response);
        if(!response.isOk()){
            throw new RuntimeException("edit failed: "+ response.responseToString());
        }
    }

    
    
}
