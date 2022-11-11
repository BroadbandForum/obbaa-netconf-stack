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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.MountContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.MountRegistries;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryTraverser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.support.SchemaMountRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelNodeDSMDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SchemaNodeConstraintValidatorRegistrar;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SchemaPathRegistrar;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SubsystemDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.AddDefaultDataInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.MountedRootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityModelNodeHelperDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.RequestScopeXmlDSMCache;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.AbstractValidationTestSetup;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.SchemaMountUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public abstract class AbstractSchemaMountTest extends AbstractRootModelTest {

	@Mock protected ModelNode m_modelNode;
	protected final static QName SCHEMA_MOUNT = createQName("schemaMount");
    protected final static QName SCHEMA_MOUNT1 = createQName("schemaMount1");
    protected final static QName COTNAINER1_MOUNT = createQName("container1");
    protected final static QName LIST1_MOUNT = createQName("list1");
    protected final static SchemaPath SCHEMA_MOUNT_PATH = AbstractValidationTestSetup.buildSchemaPath(SCHEMA_MOUNT_POINT_PATH, SCHEMA_MOUNT);
    protected AddDefaultDataInterceptor m_addDefaultDataInterceptor;
    protected ModelNodeHelperRegistry m_mountModelNodeHelperRegistry;
    protected SubSystemRegistry m_mountSubSystemRegistry;
    protected SchemaMountRegistryProvider m_provider;
    protected static SchemaRegistry m_mountRegistry;

    static QName createQName(String localName){
        return QName.create("schema-mount-test", "2018-01-03", localName);
    }
    
    @BeforeClass
    public static void initializeOnce() throws SchemaBuildException {
        List<YangTextSchemaSource> yangFiles = TestUtil.getByteSources(getYangFiles());
        m_schemaRegistry = new SchemaRegistryImpl(yangFiles, Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.setName(SchemaRegistry.GLOBAL_SCHEMA_REGISTRY);
        QName moduleQName = QName.create("schema-mount-test", "2018-01-03", "schema-mount-test");
        QName deviationModuleQName = QName.create("schema-mount-deviation-test", "2018-01-03", "schema-mount-deviation-test");

        Set<QName> moduleDeviations = new HashSet<>();
        moduleDeviations.add(deviationModuleQName);

        Map<QName, Set<QName>> supportedDeviations = new java.util.HashMap<>();
        supportedDeviations.put(moduleQName, moduleDeviations);
        
        m_mountRegistry = new SchemaRegistryImpl(Arrays.asList(TestUtil.getByteSource("/datastorevalidatortest/yangs/schema-mount-test.yang"), 
                TestUtil.getByteSource("/datastorevalidatortest/yangs/test-mount-action.yang"), TestUtil.getByteSource("/datastorevalidatortest/yangs/test-interfaces.yang"), 
                TestUtil.getByteSource("/datastorevalidatortest/yangs/bbf-interface-usage.yang"), TestUtil.getByteSource("/datastorevalidatortest/yangs/schema-mount-submodule.yang"), TestUtil.getByteSource("/datastorevalidatortest/yangs/sm-common-data-with-diff-conditions1.yang"),
                TestUtil.getByteSource("/datastorevalidatortest/yangs/interface-ref.yang"), TestUtil.getByteSource("/schemaregistryutiltest/nc-stack-extensions.yang"), TestUtil.getByteSource("/datastorevalidatortest/yangs/schema-mount-deviation-test.yang"),
                TestUtil.getByteSource("/datastorevalidatortest/yangs/copy-config-test.yang"),
                TestUtil.getByteSource("/datastorevalidatortest/yangs/bbf-hardware-types.yang"),
                TestUtil.getByteSource("/datastorevalidatortest/yangs/iana-hardware.yang")),
                Collections.emptySet(), supportedDeviations, false, new NoLockService());
         m_mountRegistry.setName("PLUG-1.0");
    }
    
    @Before
    public void setup() throws SchemaBuildException, AnnotationAnalysisException, ModelNodeFactoryException {
        RequestScopeXmlDSMCache.resetCache();
        super.setup();
        MockitoAnnotations.initMocks(this);
        m_schemaMountRegistry = new SchemaMountRegistryImpl();
        ((SchemaRegistryImpl)m_schemaRegistry).setSchemaMountRegistry(m_schemaMountRegistry);
        m_provider = new DummyMountProvider();
        m_schemaMountRegistry.register(SCHEMA_MOUNT_POINT_PATH, m_provider);
        makeSetup(m_mountRegistry);
    }

    protected void initialiseInterceptor() {
        m_addDefaultDataInterceptor = new AddDefaultDataInterceptor(m_modelNodeHelperRegistry, m_schemaRegistry, m_expValidator);
        m_addDefaultDataInterceptor.init();
    }
    
	protected void makeSetup(SchemaRegistry mountRegistry) throws SchemaBuildException {
		
        m_mountModelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(mountRegistry);
        m_mountSubSystemRegistry = new SubSystemRegistryImpl(); 
        EntityModelNodeHelperDeployer deployer = new EntityModelNodeHelperDeployer(m_mountModelNodeHelperRegistry, mountRegistry, m_xmlSubtreeDSM, m_entityRegistry, m_mountSubSystemRegistry);
        
        Map<SchemaPath, SubSystem> subSystemMap = new HashMap<>();
        ModelNodeDSMDeployer dsmDeployer = new ModelNodeDSMDeployer(m_modelNodeDSMRegistry, m_xmlSubtreeDSM);
        List<SchemaRegistryVisitor> visitors = new ArrayList<SchemaRegistryVisitor>();
        
        visitors.add(new SubsystemDeployer(m_mountSubSystemRegistry, subSystemMap));
        visitors.add(deployer);
        visitors.add(dsmDeployer);
        visitors.add(new SchemaPathRegistrar(mountRegistry, m_mountModelNodeHelperRegistry));
        visitors.add(new SchemaNodeConstraintValidatorRegistrar(mountRegistry, m_mountModelNodeHelperRegistry, m_mountSubSystemRegistry));
        RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_PATH, SCHEMA_MOUNT_POINT_PATH);
        for(ModuleIdentifier identifier:mountRegistry.getAllModuleIdentifiers()) {
            SchemaRegistryTraverser traverser = new SchemaRegistryTraverser("SchemaMount", visitors, mountRegistry, mountRegistry.getModuleByNamespace(identifier.getNamespace().toString()));
            traverser.traverse();
        }
        
        mountRegistry.setMountPath(SCHEMA_MOUNT_POINT_PATH);
        mountRegistry.setParentRegistry(m_schemaRegistry);
        m_expValidator = new DSExpressionValidator(mountRegistry, m_mountModelNodeHelperRegistry, m_mountSubSystemRegistry);
        RootModelNodeAggregator rootModelNodeAggregator = new MountedRootModelNodeAggregator(mountRegistry, m_mountModelNodeHelperRegistry, m_xmlSubtreeDSM, m_subSystemRegistry);
        Collection<SchemaPath> rootPaths = new HashSet<>();
        for ( DataSchemaNode rootNode : mountRegistry.getRootDataSchemaNodes()){
            if ( rootNode.isConfiguration()){
                rootPaths.add(rootNode.getPath());
            }
        }
        SchemaMountUtil.addRootNodeHelpers(mountRegistry, m_mountSubSystemRegistry, m_mountModelNodeHelperRegistry, m_xmlSubtreeDSM, rootPaths , rootModelNodeAggregator);
        ((DummyMountProvider)m_provider).addModelNodeHelperRegistry(mountRegistry.toString(), m_mountModelNodeHelperRegistry);
        ((DummyMountProvider)m_provider).addSchemaRegistry(mountRegistry);
        ((DummyMountProvider)m_provider).addRootModelNodeAggregator(mountRegistry.toString(), rootModelNodeAggregator);
        ((DummyMountProvider)m_provider).m_subSystemRegistry = m_mountSubSystemRegistry;
	}

    class DummyMountProvider implements SchemaMountRegistryProvider {

        Map<String, SchemaRegistry> m_schemaRegistries = new HashMap<>();
        Map<String, RootModelNodeAggregator> m_rootModelNodeAggregators = new HashMap<>();
        Map<String, ModelNodeHelperRegistry> m_helperRegistries = new HashMap<>();
        SubSystemRegistry m_subSystemRegistry;

		public void addRootModelNodeAggregator(String key, RootModelNodeAggregator rootModelNodeAggregator) {
		    m_rootModelNodeAggregators.put(key, rootModelNodeAggregator);            
        }

        @Override
        public List<MountContext> getMountContexts() {
            return null;
        }

        @Override
        public boolean isValidMountPoint(ModelNodeId nodeID) {
            return false;
        }

        @Override
        public SchemaRegistry getSchemaRegistry(ModelNodeId modelNodeId) {
            String type = getType(null, modelNodeId);
            return cacheSRAndReturn(type);
        }

        @Override
        public SchemaRegistry getSchemaRegistry(Element element) {
            String type = getType(null, element);
            return cacheSRAndReturn(type);
        }

        @Override
        public SchemaRegistry getSchemaRegistry(EditContainmentNode editContainmentNode) {
            String type = getType(null, editContainmentNode);
            return cacheSRAndReturn(type);
        }

        public SchemaRegistry cacheSRAndReturn(String type) {
            SchemaRegistry schemaRegistry = m_schemaRegistries.get(type);
            Map<String, Object> contextMap = new HashMap<String, Object>();
            contextMap.put(MOUNT_DSM_REGISTRY, m_xmlSubtreeDSM);
            contextMap.put(MOUNT_HELPER_REGISTRY, m_helperRegistries);
            RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_PATH, SCHEMA_MOUNT_POINT_PATH);
            RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_CONTEXT_SUBSYSTEM_REGISTRY, m_subSystemRegistry);
            RequestScope.getCurrentScope().putInCache(SchemaMountRegistryProvider.MOUNT_CURRENT_SCOPE, contextMap);
            return schemaRegistry;
        }

        @Override
		public SchemaRegistry getSchemaRegistry(Map<String, String> keyValues) {
			return null;
		}

		@Override
		public SchemaMountKey getSchemaMountKey() {
			return null;
		}
		
		
		public void addSchemaRegistry(SchemaRegistry registry){
			m_schemaRegistries.put(registry.toString(), registry);
		}
		
		public void addModelNodeHelperRegistry(String type, ModelNodeHelperRegistry registry){
			m_helperRegistries.put(type, registry);
		}
		
		private String getType(ModelNode modelNode, Object ... objects) {
			try {
				if (objects != null && objects.length > 0) {
		            for (Object object : objects) {
		            	if ( object instanceof Element){
		            		Node xmlSubTreeNode = ((Element)object).getParentNode();
		            		@SuppressWarnings("deprecation")
							Element validationLeaf = DocumentUtils.getChildElement((Element)xmlSubTreeNode, "plugType");
		            		if ( validationLeaf != null && "PLUG-2.0".equals(validationLeaf.getTextContent())){
		            			return "PLUG-2.0";
		            		}
		            	} else if (object instanceof EditContainmentNode) {
		            		EditContainmentNode xmlNode = getXmlSubTreeNode((EditContainmentNode)object);
		            		for (EditChangeNode change : xmlNode.getChangeNodes()){
		            			if ( change.getName().equals("plugType") && change.getValue().equals("PLUG-2.0")){
		            				return "PLUG-2.0";
		            			}
		            		}
		            		for (EditMatchNode match : xmlNode.getMatchNodes()){
		            			if ( match.getName().equals("plugType") && match.getValue().equals("PLUG-2.0")){
		            				return "PLUG-2.0";
		            			}
		            		}
		            	}else if (object instanceof ModelNodeId) {
                            ModelNodeId nodeId = (ModelNodeId)object;
                            return nodeId.getRdnValue("plugType");
                        }
		            }
				} else if ( modelNode != null){
					if ( modelNode.getModelNodeId().getRdnValue("plugType").equals("PLUG-2.0")){
						return "PLUG-2.0";
					}
				}
			} catch (Exception e){}
			
			return "PLUG-1.0";
		}

		private EditContainmentNode getXmlSubTreeNode(EditContainmentNode editNode) {
			if ( editNode.getQName().getLocalName().equals("xml-subtree")){
				return editNode;
			} else if ( editNode.getParent() != null){
				return getXmlSubTreeNode(editNode.getParent());
			}
			return editNode;
		}

		@Override
		public SchemaRegistry getSchemaRegistry(String mountKey) {
			return m_schemaRegistries.get(mountKey);
		}

        @Override
        public ModelNodeHelperRegistry getModelNodeHelperRegistry(ModelNodeId modelNodeId) {
            RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_PATH, SCHEMA_MOUNT_POINT_PATH);
            String type = getType(null, modelNodeId);
            return m_helperRegistries.get(type);
        }

        @Override
        public ModelNodeHelperRegistry getModelNodeHelperRegistry(Element element) {
            RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_PATH, SCHEMA_MOUNT_POINT_PATH);
            String type = getType(null, element);
            return m_helperRegistries.get(type);
        }

        @Override
        public ModelNodeHelperRegistry getModelNodeHelperRegistry(EditContainmentNode editContainmentNode) {
            RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_PATH, SCHEMA_MOUNT_POINT_PATH);
            String type = getType(null, editContainmentNode);
            return m_helperRegistries.get(type);
        }

        @Override
        public SubSystemRegistry getSubSystemRegistry(ModelNodeId modelNodeId) {
            RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_PATH, SCHEMA_MOUNT_POINT_PATH);
            return m_subSystemRegistry;
        }

        @Override
        public void setCorrectPlugMountContextInCache(EditContainmentNode node) {
        }

		@Override
		public MountRegistries getMountRegistries(String mountkey) {
			MountRegistries registries = Mockito.mock(MountRegistries.class);
			when(registries.getRootModelNodeAggregator()).thenReturn(m_rootModelNodeAggregators.get(mountkey));
			when(registries.getSchemaRegistry()).thenReturn(m_schemaRegistries.get(mountkey));
			when(registries.getSubSystemRegistry()).thenReturn(m_subSystemRegistry);
			return registries;
		}

        
    }
    
    protected SubSystemRegistry getSubSystemRegistry(){
    	return m_provider.getSubSystemRegistry(null);
    }
}
