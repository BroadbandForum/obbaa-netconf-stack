package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.yang.AbstractValidationTestSetup;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.MountContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryTraverser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.ModelNodeDSMDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SchemaPathRegistrar;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.SubsystemDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactoryException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityModelNodeHelperDeployer;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractSchemaMountTest extends AbstractRootModelTest {

	@Mock protected ModelNode m_modelNode;
	protected SchemaMountRegistryProvider m_provider;
	protected final static QName SCHEMA_MOUNT = createQName("schemaMount");
    protected final static QName SCHEMA_MOUNT1 = createQName("schemaMount1");
    protected final static QName COTNAINER1_MOUNT = createQName("container1");
    protected final static QName LIST1_MOUNT = createQName("list1");
    protected final static SchemaPath SCHEMA_MOUNT_PATH = AbstractValidationTestSetup.buildSchemaPath(SCHEMA_MOUNT_POINT_PATH, SCHEMA_MOUNT);
    
    static QName createQName(String localName){
        return QName.create("schema-mount-test", "2018-01-03", localName);
    }
    
    @BeforeClass
    public static void classSetup(){
        System.setProperty(SchemaRegistryUtil.ENABLE_MOUNT_POINT, "true");
    }
    
    @AfterClass
    public static void classDestroy() {
        System.setProperty(SchemaRegistryUtil.ENABLE_MOUNT_POINT, "false");
    }
    
    @Before
    public void setup() throws SchemaBuildException, AnnotationAnalysisException, ModelNodeFactoryException {
        super.setup();
        SchemaRegistry mountRegistry = new SchemaRegistryImpl(Arrays.asList(TestUtil.getByteSource("/datastorevalidatortest/yangs/schema-mount-test.yang"), TestUtil.getByteSource("/datastorevalidatortest/yangs/test-mount-action.yang"), TestUtil.getByteSource("/datastorevalidatortest/yangs/test-interfaces.yang"), TestUtil.getByteSource("/datastorevalidatortest/yangs/bbf-interface-usage.yang")), Collections.emptySet(), Collections.emptyMap(), false, new NoLockService());
        mountRegistry.setName("PLUG-1.0");
        m_provider = new DummyMountProvider();
        makeSetup(mountRegistry);
    }

	protected void makeSetup(SchemaRegistry mountRegistry) throws SchemaBuildException {
		
        
        ModelNodeHelperRegistry registry = new ModelNodeHelperRegistryImpl(mountRegistry);
        SubSystemRegistry mountSubSystemRegistry = new SubSystemRegistryImpl(); 
        EntityModelNodeHelperDeployer deployer = new EntityModelNodeHelperDeployer(registry, mountRegistry, m_xmlSubtreeDSM, m_entityRegistry, mountSubSystemRegistry);
        
        Map<SchemaPath, SubSystem> subSystemMap = new HashMap<>();
        ModelNodeDSMDeployer dsmDeployer = new ModelNodeDSMDeployer(m_modelNodeDSMRegistry, m_xmlSubtreeDSM);
        List<SchemaRegistryVisitor> visitors = new ArrayList<SchemaRegistryVisitor>();
        
        visitors.add(new SubsystemDeployer(mountSubSystemRegistry, subSystemMap));
        visitors.add(deployer);
        visitors.add(dsmDeployer);
        visitors.add(new SchemaPathRegistrar(mountRegistry, registry));
        RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_PATH, SCHEMA_MOUNT_POINT_PATH);
        RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_CONTEXT_SCHEMA_REGISTRY, mountRegistry);
        for(ModuleIdentifier identifier:mountRegistry.getAllModuleIdentifiers()) {
            SchemaRegistryTraverser traverser = new SchemaRegistryTraverser("SchemaMount", visitors, mountRegistry, mountRegistry.getModuleByNamespace(identifier.getNamespace().toString()));
            traverser.traverse();
        }
        m_schemaMountRegistry.register(SCHEMA_MOUNT_POINT_PATH, m_provider);
        mountRegistry.setMountPath(SCHEMA_MOUNT_POINT_PATH);
        mountRegistry.setParentRegistry(m_schemaRegistry);
        ((DummyMountProvider)m_provider).addModelNodeHelperRegistry(mountRegistry.toString(), registry);
        ((DummyMountProvider)m_provider).addSchemaRegistry(mountRegistry);
        ((DummyMountProvider)m_provider).m_subSystemRegistry = mountSubSystemRegistry;
	}
    
    @After
    public void teardown(){
    	RequestScope.resetScope();
    }

    class DummyMountProvider implements SchemaMountRegistryProvider {

        Map<String, SchemaRegistry> m_schemaRegistries = new HashMap<>();
        Map<String, ModelNodeHelperRegistry> m_helperRegistries = new HashMap<>();
        SubSystemRegistry m_subSystemRegistry;
        @Override
        public SchemaRegistry getSchemaRegistry(ModelNode modelNode, Object... objects) {
        	String type = getType(modelNode, objects);
        	SchemaRegistry schemaRegistry = m_schemaRegistries.get(type);
            RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_PATH, SCHEMA_MOUNT_POINT_PATH);
            RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_CONTEXT_SCHEMA_REGISTRY, schemaRegistry);
            RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_CONTEXT_SUBSYSTEM_REGISTRY, m_subSystemRegistry);
            return schemaRegistry;
        }

		@Override
        public ModelNodeHelperRegistry getModelNodeHelperRegistry(ModelNode modelNode, Object... objects) {
            RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_PATH, SCHEMA_MOUNT_POINT_PATH);
            String type = getType(modelNode, objects);
            return m_helperRegistries.get(type);
        }

        @Override
        public SubSystemRegistry getSubSystemRegistry(ModelNode modelNode, Object... objects) {
        	RequestScope.getCurrentScope().putInCache(SchemaRegistryUtil.MOUNT_PATH, SCHEMA_MOUNT_POINT_PATH);
            return m_subSystemRegistry;
        }

        @Override
        public ModelNodeDataStoreManager getMountDSM(ModelNode modelNode, Object... objects) {
            return null;
        }

        @Override
        public List<MountContext> getMountContexts() {
            return null;
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
		
		private String getType(ModelNode modelNode, Object[] objects) {
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
			return null;
		}
    }
    
    protected SubSystemRegistry getSubSystemRegistry(){
    	return m_provider.getSubSystemRegistry(null, (Object)null);
    }
}
