package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;

/**
 * Created by pgorai on 2/4/16.
 */
public class ModelNodeHelperRegistryTest {
    public static final String COMPONENT_1 = "component1";
    public static final String COMPONENT_2 = "component2";
    ModelNodeHelperRegistry m_modelNodeHelperRegistry ;

    private SchemaPath m_schemaPath1 = JukeboxConstants.JUKEBOX_SCHEMA_PATH;
    private QName m_childContainer1 = QName.create(JukeboxConstants.JB_NS, JukeboxConstants.JB_REVISION, "childContainer1");
    private ChildContainerHelper m_childContainer1Helper = mock(ChildContainerHelper.class);

    private SchemaPath m_schemaPath2 = JukeboxConstants.LIBRARY_SCHEMA_PATH;
    private QName m_childContainer2 = QName.create(JukeboxConstants.JB_NS, JukeboxConstants.JB_REVISION, "childContainer2");
    private ChildContainerHelper m_childContainer2Helper= mock(ChildContainerHelper.class);

    private SchemaPath m_schemaPath3 = JukeboxConstants.ARTIST_SCHEMA_PATH;
    private QName m_childContainer3 = QName.create(JukeboxConstants.JB_NS, JukeboxConstants.JB_REVISION, "childContainer3");
    private ChildContainerHelper m_childContainer3Helper = mock(ChildContainerHelper.class);

    private ChildListHelper m_childList1Helper = mock(ChildListHelper.class);
    private ChildListHelper m_childList2Helper = mock(ChildListHelper.class);
    private ChildListHelper m_childList3Helper = mock(ChildListHelper.class);

    private ConfigAttributeHelper m_configAttributeHelper1 = mock(ConfigAttributeHelper.class);
    private ConfigAttributeHelper m_configAttributeHelper2 = mock(ConfigAttributeHelper.class);
    private ConfigAttributeHelper m_configAttributeHelper3 = mock(ConfigAttributeHelper.class);
    private ChildLeafListHelper m_childLeafListHelper1 = mock(ChildLeafListHelper.class);
    private ChildLeafListHelper m_childLeafListHelper2 = mock(ChildLeafListHelper.class);
    private ChildLeafListHelper m_childLeafListHelper3 = mock(ChildLeafListHelper.class);
    private SchemaRegistry m_schemaRegistry;
    
    @Before
    public void setUp() throws SchemaBuildException{
    	m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(m_schemaRegistry);
    }
    
    @Test
    public void testUndeploy(){

        /*Register Child container Helper*/

        when(m_childContainer1Helper.getChildModelNodeSchemaPath()).thenReturn(m_schemaPath1);
        when(m_childContainer2Helper.getChildModelNodeSchemaPath()).thenReturn(m_schemaPath2);
        when(m_childContainer3Helper.getChildModelNodeSchemaPath()).thenReturn(m_schemaPath3);

        m_modelNodeHelperRegistry.registerChildContainerHelper(COMPONENT_1, m_schemaPath1, m_childContainer1, m_childContainer1Helper);
        m_modelNodeHelperRegistry.registerChildContainerHelper(COMPONENT_2, m_schemaPath2, m_childContainer2, m_childContainer2Helper);
        m_modelNodeHelperRegistry.registerChildContainerHelper(COMPONENT_2, m_schemaPath3, m_childContainer3, m_childContainer3Helper);
        assertEquals(m_childContainer1Helper, m_modelNodeHelperRegistry.getChildContainerHelper(m_schemaPath1, m_childContainer1));
        assertEquals(m_childContainer2Helper, m_modelNodeHelperRegistry.getChildContainerHelper(m_schemaPath2, m_childContainer2));
        m_modelNodeHelperRegistry.undeploy(COMPONENT_1);
        assertNull(m_modelNodeHelperRegistry.getChildContainerHelper(m_schemaPath1, m_childContainer1));
        assertNull(m_modelNodeHelperRegistry.getChildContainerHelper(m_schemaPath1, m_childContainer2));
        assertEquals(m_childContainer3Helper,m_modelNodeHelperRegistry.getChildContainerHelper(m_schemaPath3,m_childContainer3));

        /*Register Child List Helper*/
        when(m_childList1Helper.getChildModelNodeSchemaPath()).thenReturn(m_schemaPath1);
        when(m_childList2Helper.getChildModelNodeSchemaPath()).thenReturn(m_schemaPath2);
        when(m_childList3Helper.getChildModelNodeSchemaPath()).thenReturn(m_schemaPath3);

        m_modelNodeHelperRegistry.registerChildListHelper(COMPONENT_1, m_schemaPath1, m_childContainer1, m_childList1Helper);
        m_modelNodeHelperRegistry.registerChildListHelper(COMPONENT_1, m_schemaPath2, m_childContainer2, m_childList2Helper);
        m_modelNodeHelperRegistry.registerChildListHelper(COMPONENT_2, m_schemaPath3, m_childContainer3, m_childList3Helper);
        assertEquals(m_childList1Helper,m_modelNodeHelperRegistry.getChildListHelper(m_schemaPath1,m_childContainer1));
        assertEquals(m_childList3Helper,m_modelNodeHelperRegistry.getChildListHelper(m_schemaPath3,m_childContainer3));
        m_modelNodeHelperRegistry.undeploy(COMPONENT_1);
        assertNull(m_modelNodeHelperRegistry.getChildListHelper(m_schemaPath1, m_childContainer1));
        assertNull(m_modelNodeHelperRegistry.getChildListHelper(m_schemaPath2, m_childContainer2));

        /*Register NaturalKey Helper*/
        when(m_configAttributeHelper1.getChildModelNodeSchemaPath()).thenReturn(m_schemaPath1);
        when(m_configAttributeHelper2.getChildModelNodeSchemaPath()).thenReturn(m_schemaPath2);
        when(m_configAttributeHelper3.getChildModelNodeSchemaPath()).thenReturn(m_schemaPath3);

        m_modelNodeHelperRegistry.registerNaturalKeyHelper(COMPONENT_1, m_schemaPath1, m_childContainer1, m_configAttributeHelper1);
        m_modelNodeHelperRegistry.registerNaturalKeyHelper(COMPONENT_1, m_schemaPath2, m_childContainer2, m_configAttributeHelper2);
        m_modelNodeHelperRegistry.registerNaturalKeyHelper(COMPONENT_2, m_schemaPath3, m_childContainer3, m_configAttributeHelper3);
        assertEquals(m_configAttributeHelper1,m_modelNodeHelperRegistry.getNaturalKeyHelper(m_schemaPath1,m_childContainer1));
        assertEquals(m_configAttributeHelper3,m_modelNodeHelperRegistry.getNaturalKeyHelper(m_schemaPath3,m_childContainer3));
        m_modelNodeHelperRegistry.undeploy(COMPONENT_1);
        assertNull(m_modelNodeHelperRegistry.getNaturalKeyHelper(m_schemaPath1, m_childContainer1));
        assertNull(m_modelNodeHelperRegistry.getNaturalKeyHelper(m_schemaPath2, m_childContainer2));
        assertEquals(m_configAttributeHelper3,m_modelNodeHelperRegistry.getNaturalKeyHelper(m_schemaPath3, m_childContainer3));

        /*Register ConfigAttribute Helper */
        m_modelNodeHelperRegistry.registerConfigAttributeHelper(COMPONENT_1,m_schemaPath1, m_childContainer1, m_configAttributeHelper1);
        m_modelNodeHelperRegistry.registerConfigAttributeHelper(COMPONENT_1, m_schemaPath2, m_childContainer2, m_configAttributeHelper2);
        m_modelNodeHelperRegistry.registerConfigAttributeHelper(COMPONENT_2, m_schemaPath3, m_childContainer3, m_configAttributeHelper3);
        assertEquals(m_configAttributeHelper1,m_modelNodeHelperRegistry.getConfigAttributeHelper(m_schemaPath1,m_childContainer1));
        assertEquals(m_configAttributeHelper3,m_modelNodeHelperRegistry.getConfigAttributeHelper(m_schemaPath3,m_childContainer3));
        m_modelNodeHelperRegistry.undeploy(COMPONENT_1);
        assertNull(m_modelNodeHelperRegistry.getConfigAttributeHelper(m_schemaPath1, m_childContainer1));
        assertNull(m_modelNodeHelperRegistry.getConfigAttributeHelper(m_schemaPath2, m_childContainer2));
        assertEquals(m_configAttributeHelper3,m_modelNodeHelperRegistry.getConfigAttributeHelper(m_schemaPath3, m_childContainer3));

        /*Register ConfigLeafList Helper */
        when(m_childLeafListHelper1.getChildModelNodeSchemaPath()).thenReturn(m_schemaPath1);
        when(m_childLeafListHelper2.getChildModelNodeSchemaPath()).thenReturn(m_schemaPath2);
        when(m_childLeafListHelper3.getChildModelNodeSchemaPath()).thenReturn(m_schemaPath3);

        m_modelNodeHelperRegistry.registerConfigLeafListHelper(COMPONENT_1,m_schemaPath1, m_childContainer1, m_childLeafListHelper1);
        m_modelNodeHelperRegistry.registerConfigLeafListHelper(COMPONENT_1, m_schemaPath2, m_childContainer2, m_childLeafListHelper2);
        m_modelNodeHelperRegistry.registerConfigLeafListHelper(COMPONENT_2, m_schemaPath3, m_childContainer3, m_childLeafListHelper3);
        assertEquals(m_childLeafListHelper1,m_modelNodeHelperRegistry.getConfigLeafListHelper(m_schemaPath1,m_childContainer1));
        assertEquals(m_childLeafListHelper3,m_modelNodeHelperRegistry.getConfigLeafListHelper(m_schemaPath3,m_childContainer3));
        m_modelNodeHelperRegistry.undeploy(COMPONENT_1);
        assertNull(m_modelNodeHelperRegistry.getConfigLeafListHelper(m_schemaPath1, m_childContainer1));
        assertNull(m_modelNodeHelperRegistry.getConfigLeafListHelper(m_schemaPath2, m_childContainer2));
        assertEquals(m_childLeafListHelper3,m_modelNodeHelperRegistry.getConfigLeafListHelper(m_schemaPath3, m_childContainer3));
    }
    
    @Test
    public void testMultiplePlugsUndeploy(){
        when(m_childContainer1Helper.getChildModelNodeSchemaPath()).thenReturn(m_schemaPath1);
        when(m_childContainer2Helper.getChildModelNodeSchemaPath()).thenReturn(m_schemaPath2);

        m_modelNodeHelperRegistry.registerChildContainerHelper(COMPONENT_1, m_schemaPath1, m_childContainer1, m_childContainer1Helper);
        m_modelNodeHelperRegistry.registerChildContainerHelper(COMPONENT_2, m_schemaPath2, m_childContainer2, m_childContainer2Helper);
        m_modelNodeHelperRegistry.registerChildContainerHelper(COMPONENT_2, m_schemaPath1, m_childContainer2, m_childContainer1Helper);

        m_schemaRegistry.registerAppAllowedAugmentedPath(COMPONENT_1, "/schemapath1", m_schemaPath1);
        m_modelNodeHelperRegistry.undeploy(COMPONENT_2);
        assertNotNull(m_modelNodeHelperRegistry.getChildContainerHelper(m_schemaPath1, m_childContainer1));
        assertNotNull(m_modelNodeHelperRegistry.getChildContainerHelper(m_schemaPath1, m_childContainer2));
        assertNull(m_modelNodeHelperRegistry.getChildContainerHelper(m_schemaPath2, m_childContainer2));

        m_modelNodeHelperRegistry.undeploy(COMPONENT_1);
        assertNull(m_modelNodeHelperRegistry.getChildContainerHelper(m_schemaPath1, m_childContainer1));
        assertNull(m_modelNodeHelperRegistry.getChildContainerHelper(m_schemaPath1, m_childContainer2));
    }

    @Test
    public void testUnDeployModelService_WithAugmentation(){

        SchemaRegistry schemaRegistry = mock(SchemaRegistry.class);
        m_modelNodeHelperRegistry = new ModelNodeHelperRegistryImpl(schemaRegistry);

        // Service is the Root container which contains "configuration" container to which service are augmented.
        SchemaPath serviceSchemaPath = SchemaPath.create(true, QName.create("example-service-namespace", "2016-06-20", "service"));
        SchemaPath configurationSchemaPath = new SchemaPathBuilder().withParent(serviceSchemaPath).appendLocalName("configuration").build();
        ChildContainerHelper configurationContainerHelper = mock(ChildContainerHelper.class);
        when(configurationContainerHelper.getChildModelNodeSchemaPath()).thenReturn(configurationSchemaPath);
        m_modelNodeHelperRegistry.registerChildContainerHelper("configurationComponent",serviceSchemaPath,configurationSchemaPath.getLastComponent(),configurationContainerHelper);

        // Case 1 : Deploy Service A and Service B (Both are containers)
        SchemaPath serviceTypeA = new SchemaPathBuilder().withParent(configurationSchemaPath).appendLocalName("serviceTypeA").build();
        ChildContainerHelper serviceTypeAContainerHelper = mock(ChildContainerHelper.class);
        when(serviceTypeAContainerHelper.getChildModelNodeSchemaPath()).thenReturn(serviceTypeA);
        m_modelNodeHelperRegistry.registerChildContainerHelper("serviceTypeAComponent",configurationSchemaPath,serviceTypeA.getLastComponent(),serviceTypeAContainerHelper);

        SchemaPath serviceTypeAConfigAttribute = new SchemaPathBuilder().withParent(serviceTypeA).appendLocalName("serviceTypeA-configAttribute").build();
        ConfigAttributeHelper serviceTypeAConfigAttrHelper = mock(ConfigAttributeHelper.class);
        when(serviceTypeAConfigAttrHelper.getChildModelNodeSchemaPath()).thenReturn(serviceTypeAConfigAttribute);
        m_modelNodeHelperRegistry.registerConfigAttributeHelper("serviceTypeAComponent",serviceTypeA, serviceTypeAConfigAttribute.getLastComponent(),serviceTypeAConfigAttrHelper);

        SchemaPath serviceTypeB = new SchemaPathBuilder().withParent(configurationSchemaPath).appendLocalName("serviceTypeB").build();
        ChildContainerHelper serviceTypeBContainerHelper = mock(ChildContainerHelper.class);
        when(serviceTypeBContainerHelper.getChildModelNodeSchemaPath()).thenReturn(serviceTypeB);
        m_modelNodeHelperRegistry.registerChildContainerHelper("serviceTypeBComponent",configurationSchemaPath,serviceTypeB.getLastComponent(),serviceTypeBContainerHelper);

        SchemaPath serviceTypeBConfigAttribute = new SchemaPathBuilder().withParent(serviceTypeB).appendLocalName("serviceTypeB-configAttribute").build();
        ConfigAttributeHelper serviceTypeBConfigAttrHelper = mock(ConfigAttributeHelper.class);
        when(serviceTypeBConfigAttrHelper.getChildModelNodeSchemaPath()).thenReturn(serviceTypeBConfigAttribute);
        m_modelNodeHelperRegistry.registerConfigAttributeHelper("serviceTypeBComponent",serviceTypeB, serviceTypeBConfigAttribute.getLastComponent(),serviceTypeBConfigAttrHelper);

        DataSchemaNode configSchemaNode = mock(DataSchemaNode.class);
        when(schemaRegistry.getNonChoiceParent(serviceTypeA)).thenReturn(configSchemaNode);
        when(configSchemaNode.getPath()).thenReturn(configurationSchemaPath);
        when(schemaRegistry.getDataSchemaNode(configurationSchemaPath)).thenReturn(configSchemaNode);

        DataSchemaNode serviceTypeASchemaNode = mock(DataSchemaNode.class);
        when(schemaRegistry.getNonChoiceParent(serviceTypeAConfigAttribute)).thenReturn(serviceTypeASchemaNode);
        when(serviceTypeASchemaNode.getPath()).thenReturn(serviceTypeA);
        when(schemaRegistry.getDataSchemaNode(serviceTypeA)).thenReturn(serviceTypeASchemaNode);

        // Undeploy Service A and check Service B still exists
        m_modelNodeHelperRegistry.undeploy("serviceTypeAComponent");
        assertNotNull(m_modelNodeHelperRegistry.getChildContainerHelper(serviceSchemaPath,configurationSchemaPath.getLastComponent()));
        assertNotNull(m_modelNodeHelperRegistry.getChildContainerHelper(configurationSchemaPath,serviceTypeB.getLastComponent()));
        assertNotNull(m_modelNodeHelperRegistry.getConfigAttributeHelper(serviceTypeB,serviceTypeBConfigAttribute.getLastComponent()));
        assertNull(m_modelNodeHelperRegistry.getChildContainerHelper(configurationSchemaPath,serviceTypeA.getLastComponent()));
        assertNull(m_modelNodeHelperRegistry.getConfigAttributeHelper(serviceTypeA,serviceTypeAConfigAttribute.getLastComponent()));
        assertEquals(1,m_modelNodeHelperRegistry.getChildContainerHelpers(configurationSchemaPath).size());

        // Case 2 : Deploy Service C (list with a key and attribute) along with existing Service B
        SchemaPath serviceTypeC = new SchemaPathBuilder().withParent(configurationSchemaPath).appendLocalName("serviceTypeC").build();
        ChildListHelper serviceTypeCListHelper = mock(ChildListHelper.class);
        when(serviceTypeCListHelper.getChildModelNodeSchemaPath()).thenReturn(serviceTypeC);
        m_modelNodeHelperRegistry.registerChildListHelper("serviceTypeCComponent",configurationSchemaPath,serviceTypeC.getLastComponent(),serviceTypeCListHelper);

        SchemaPath serviceTypeCNaturalKey = new SchemaPathBuilder().withParent(serviceTypeC).appendLocalName("serviceTypeC-Key").build();
        ConfigAttributeHelper serviceTypeCNaturalKeyHelper = mock(ConfigAttributeHelper.class);
        when(serviceTypeCNaturalKeyHelper.getChildModelNodeSchemaPath()).thenReturn(serviceTypeCNaturalKey);
        m_modelNodeHelperRegistry.registerNaturalKeyHelper("serviceTypeCComponent",serviceTypeC, serviceTypeCNaturalKey.getLastComponent(),serviceTypeCNaturalKeyHelper);

        SchemaPath serviceTypeCConfigAttribute = new SchemaPathBuilder().withParent(serviceTypeC).appendLocalName("serviceTypeC-configAttribute").build();
        ConfigAttributeHelper serviceTypeCConfigAttrHelper = mock(ConfigAttributeHelper.class);
        when(serviceTypeCConfigAttrHelper.getChildModelNodeSchemaPath()).thenReturn(serviceTypeCConfigAttribute);
        m_modelNodeHelperRegistry.registerConfigAttributeHelper("serviceTypeCComponent", serviceTypeC, serviceTypeCConfigAttribute.getLastComponent(),serviceTypeCConfigAttrHelper);

        DataSchemaNode serviceTypeCSchemaNode = mock(DataSchemaNode.class);
        when(schemaRegistry.getNonChoiceParent(serviceTypeCConfigAttribute)).thenReturn(serviceTypeCSchemaNode);
        when(schemaRegistry.getNonChoiceParent(serviceTypeCNaturalKey)).thenReturn(serviceTypeCSchemaNode);
        when(serviceTypeCSchemaNode.getPath()).thenReturn(serviceTypeC);
        when(schemaRegistry.getDataSchemaNode(serviceTypeC)).thenReturn(serviceTypeCSchemaNode);

        when(schemaRegistry.getNonChoiceParent(serviceTypeC)).thenReturn(configSchemaNode);

        assertEquals(1,m_modelNodeHelperRegistry.getChildListHelpers(configurationSchemaPath).size());

        // Undeploy Service C
        m_modelNodeHelperRegistry.undeploy("serviceTypeCComponent");
        assertNotNull(m_modelNodeHelperRegistry.getChildContainerHelper(serviceSchemaPath,configurationSchemaPath.getLastComponent()));
        assertNotNull(m_modelNodeHelperRegistry.getChildContainerHelper(configurationSchemaPath,serviceTypeB.getLastComponent()));
        assertNotNull(m_modelNodeHelperRegistry.getConfigAttributeHelper(serviceTypeB,serviceTypeBConfigAttribute.getLastComponent()));

        assertNull(m_modelNodeHelperRegistry.getChildListHelper(configurationSchemaPath,serviceTypeC.getLastComponent()));
        assertNull(m_modelNodeHelperRegistry.getConfigAttributeHelper(serviceTypeC,serviceTypeCConfigAttribute.getLastComponent()));
        assertNull(m_modelNodeHelperRegistry.getNaturalKeyHelper(serviceTypeC,serviceTypeCNaturalKey.getLastComponent()));

        assertEquals(1,m_modelNodeHelperRegistry.getChildContainerHelpers(configurationSchemaPath).size());
        assertEquals(0,m_modelNodeHelperRegistry.getChildListHelpers(configurationSchemaPath).size());

        // Case 3 : Deploy Service D with only a leaf
        SchemaPath serviceTypeD = new SchemaPathBuilder().withParent(configurationSchemaPath).appendLocalName("serviceTypeD-configAttribute").build();
        ConfigAttributeHelper serviceTypeDConfigAttrHelper = mock(ConfigAttributeHelper.class);
        when(serviceTypeDConfigAttrHelper.getChildModelNodeSchemaPath()).thenReturn(serviceTypeD);
        m_modelNodeHelperRegistry.registerConfigAttributeHelper("serviceTypeDComponent",configurationSchemaPath,serviceTypeD.getLastComponent(),serviceTypeDConfigAttrHelper);

        assertEquals(1,m_modelNodeHelperRegistry.getConfigAttributeHelpers(configurationSchemaPath).size());

        when(schemaRegistry.getNonChoiceParent(serviceTypeD)).thenReturn(configSchemaNode);
        m_modelNodeHelperRegistry.undeploy("serviceTypeDComponent");
        assertEquals(0,m_modelNodeHelperRegistry.getConfigAttributeHelpers(configurationSchemaPath).size());

        // Case 4 : Deploy Service E with only a leaf-list augmented to configuration
        SchemaPath serviceTypeE = new SchemaPathBuilder().withParent(configurationSchemaPath).appendLocalName("serviceTypeD-configAttribute").build();
        ChildLeafListHelper serviceTypeELeafListHelper = mock(ChildLeafListHelper.class);
        when(serviceTypeELeafListHelper.getChildModelNodeSchemaPath()).thenReturn(serviceTypeE);
        m_modelNodeHelperRegistry.registerConfigLeafListHelper("serviceTypeEComponent",configurationSchemaPath,serviceTypeE.getLastComponent(),serviceTypeELeafListHelper);

        assertEquals(1,m_modelNodeHelperRegistry.getConfigLeafListHelpers(configurationSchemaPath).size());

        when(schemaRegistry.getNonChoiceParent(serviceTypeE)).thenReturn(configSchemaNode);
        m_modelNodeHelperRegistry.undeploy("serviceTypeEComponent");
        assertEquals(0,m_modelNodeHelperRegistry.getConfigLeafListHelpers(configurationSchemaPath).size());


    }
}
