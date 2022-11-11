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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.libraryxmlsubtreewithartist.Library;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Album;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Artist;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Jukebox;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Song;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
/**
 * Created by pgorai on 2/8/16.
 */
public class EntityRegistryTest {

    private EntityRegistryImpl m_entityRegistry;
    private ModelNodeDSMRegistry m_modelNodeDSMRegistry;
    private Class m_klass = Jukebox.class;

    private Map<QName, Method> m_configAttributeGetters = new HashMap<>();
    private Map<QName, String> m_fieldNames = new HashMap<>();
    private String m_componentId1 = "componentId1";
    private String m_componentId2 = "componentId2";
    private Map<QName, Method> m_configAttributeSetters = new HashMap<>();

    @Before
    public void setup() throws AnnotationAnalysisException {
        m_entityRegistry = new EntityRegistryImpl();
        m_modelNodeDSMRegistry = new ModelNodeDSMRegistryImpl();

    }

    @Test
    public void testUpdateRegistryAndUndeploy() throws AnnotationAnalysisException {
        Map<SchemaPath, Class> map = new HashMap<>();
        map.put(JukeboxConstants.JUKEBOX_SCHEMA_PATH, m_klass);
        m_entityRegistry.addSchemaPaths("test-component", map);

        assertEquals(m_klass, m_entityRegistry.getEntityClass(JukeboxConstants.JUKEBOX_SCHEMA_PATH));
        assertEquals(JukeboxConstants.JUKEBOX_SCHEMA_PATH.getLastComponent(), m_entityRegistry.getQName(m_klass));
        m_entityRegistry.undeploy("test-component");
        assertEquals(null, m_entityRegistry.getEntityClass(JukeboxConstants.JUKEBOX_SCHEMA_PATH));
        assertEquals(null, m_entityRegistry.getQName(m_klass));
    }

    @Test
    public void undeployTest() throws NoSuchFieldException, NoSuchMethodException {
        m_entityRegistry.addComponentClass(m_componentId1, m_klass);
        m_entityRegistry.addConfigAttributeGetters(m_klass, m_configAttributeGetters, m_fieldNames);
        m_entityRegistry.addClassWithYangParentSchemaPathAnnotation(m_componentId1, m_klass);
        assertEquals(m_configAttributeGetters, m_entityRegistry.getAttributeGetters(m_klass));
        assertTrue(m_entityRegistry.classHasYangParentSchemaPathAnnotation(m_klass));

        m_entityRegistry.undeploy(m_componentId1);
        assertNull(m_entityRegistry.getAttributeGetters(m_klass));
        assertFalse(m_entityRegistry.classHasYangParentSchemaPathAnnotation(m_klass));

        m_entityRegistry.addComponentClass(m_componentId2, m_klass);
        m_entityRegistry.addConfigAttributeSetters(m_klass, m_configAttributeSetters, m_klass.getDeclaredMethod("getParentId"));
        assertEquals(m_configAttributeSetters, m_entityRegistry.getAttributeSetters(m_klass));
        m_entityRegistry.undeploy(m_componentId2);
        assertNull(m_entityRegistry.getAttributeSetters(m_klass));

        m_entityRegistry.addComponentClass(m_componentId2, m_klass);
        m_entityRegistry.addYangChildGetters(m_klass, m_configAttributeGetters);
        assertEquals(m_configAttributeGetters, m_entityRegistry.getYangChildGetters(m_klass));
        m_entityRegistry.undeploy(m_componentId2);
        assertNull(m_entityRegistry.getYangChildGetters(m_klass));

        m_entityRegistry.addComponentClass(m_componentId1, m_klass);
        m_entityRegistry.addYangChildSetters(m_klass, m_configAttributeSetters);
        assertEquals(m_configAttributeSetters, m_entityRegistry.getYangChildSetters(m_klass));
        m_entityRegistry.undeploy(m_componentId1);
        assertNull(m_entityRegistry.getYangChildSetters(m_klass));

        // Adding dummy method
        m_entityRegistry.addComponentClass(m_componentId1, m_klass);
        m_entityRegistry.addYangXmlSubtreeGetter(m_klass, m_klass.getDeclaredMethod("getParentId"));
        assertEquals("getParentId", m_entityRegistry.getYangXmlSubtreeGetter(m_klass).getName());
        m_entityRegistry.undeploy(m_componentId1);
        assertNull(m_entityRegistry.getYangXmlSubtreeGetter(m_klass));

        // Adding dummy method
        m_entityRegistry.addComponentClass(m_componentId1, m_klass);
        m_entityRegistry.addYangXmlSubtreeSetter(m_klass, m_klass.getDeclaredMethod("getParentId"));
        assertEquals("getParentId", m_entityRegistry.getYangXmlSubtreeSetter(m_klass).getName());
        m_entityRegistry.undeploy(m_componentId1);
        assertNull(m_entityRegistry.getYangXmlSubtreeSetter(m_klass));

        // Adding dummy method
        m_entityRegistry.addComponentClass(m_componentId1, m_klass);
        m_entityRegistry.addYangSchemaPathSetter(m_klass,m_klass.getDeclaredMethod("getParentId"));
        assertEquals("getParentId",m_entityRegistry.getSchemaPathSetter(m_klass).getName());
        m_entityRegistry.undeploy(m_componentId1);
        assertNull(m_entityRegistry.getSchemaPathSetter(m_klass));
        
        // Adding dummy method
        m_entityRegistry.addComponentClass(m_componentId1, m_klass);
        m_entityRegistry.addOrderByUserGetter(m_klass,m_klass.getDeclaredMethod("getParentId"));
        assertEquals("getParentId",m_entityRegistry.getOrderByUserGetter(m_klass).getName());
        m_entityRegistry.undeploy(m_componentId1);
        assertNull(m_entityRegistry.getOrderByUserGetter(m_klass));
        
        // Adding dummy method
        m_entityRegistry.addComponentClass(m_componentId1, m_klass);
        m_entityRegistry.addOrderByUserSetter(m_klass,m_klass.getDeclaredMethod("getParentId"));
        assertEquals("getParentId",m_entityRegistry.getOrderByUserSetter(m_klass).getName());
        m_entityRegistry.undeploy(m_componentId1);
        assertNull(m_entityRegistry.getOrderByUserSetter(m_klass));

        // Adding dummy method
        m_entityRegistry.addComponentClass(m_componentId1, m_klass);
        m_entityRegistry.addYangVisibilityControllerGetter(m_klass,m_klass.getDeclaredMethod("getParentId"));
        assertEquals("getParentId",m_entityRegistry.getYangVisibilityControllerGetter(m_klass).getName());
        m_entityRegistry.undeploy(m_componentId1);
        assertNull(m_entityRegistry.getYangVisibilityControllerGetter(m_klass));

        // Adding dummy method
        m_entityRegistry.addComponentClass(m_componentId1, m_klass);
        m_entityRegistry.addYangVisibilityControllerSetter(m_klass,m_klass.getDeclaredMethod("getParentId"));
        assertEquals("getParentId",m_entityRegistry.getYangVisibilityControllerSetter(m_klass).getName());
        m_entityRegistry.undeploy(m_componentId1);
        assertNull(m_entityRegistry.getYangVisibilityControllerSetter(m_klass));
    }

    @Test
    public void testDeploy() throws AnnotationAnalysisException {
        Class<org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.libraryxmlsubtreewithartist.Jukebox> jukeboxClass = org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.libraryxmlsubtreewithartist.Jukebox.class;
        Class<Library> libraryClass = Library.class;
        Class<Artist> artistClass = Artist.class;
        Class<Album> albumClass = Album.class;
        Class<Song> songClass = Song.class;
        SchemaRegistry schemaRegistry = mock(SchemaRegistry.class);
        m_entityRegistry.updateRegistry("Jukebox", Collections.<Class>singletonList(jukeboxClass), schemaRegistry, mock(EntityDataStoreManager.class), m_modelNodeDSMRegistry);

        assertTrue(m_entityRegistry.getAttributeGetters(jukeboxClass).isEmpty());
        assertEquals("getParentId",m_entityRegistry.getParentIdGetter(jukeboxClass).getName());
        assertEquals("getSchemaPath",m_entityRegistry.getSchemaPathGetter(jukeboxClass).getName());
        assertEquals(1, m_entityRegistry.getYangChildGetters(jukeboxClass).size());

        assertTrue(m_entityRegistry.getAttributeGetters(libraryClass).isEmpty());
        assertEquals("getParentId",m_entityRegistry.getParentIdGetter(libraryClass).getName());
        assertEquals("getSchemaPath",m_entityRegistry.getSchemaPathGetter(libraryClass).getName());
        assertNotNull(m_entityRegistry.getYangXmlSubtreeGetter(libraryClass));
        
        assertEquals("getInsertOrder",m_entityRegistry.getOrderByUserGetter(artistClass).getName());
        assertEquals("setInsertOrder",m_entityRegistry.getOrderByUserSetter(artistClass).getName());
        assertEquals("insertOrder",m_entityRegistry.getOrderByFieldName(artistClass));
        
        assertEquals("getInsertOrder",m_entityRegistry.getOrderByUserGetter(albumClass).getName());
        assertEquals("setInsertOrder",m_entityRegistry.getOrderByUserSetter(albumClass).getName());
        assertEquals("insertOrder",m_entityRegistry.getOrderByFieldName(albumClass));

        assertEquals("getVisibility",m_entityRegistry.getYangVisibilityControllerGetter(albumClass).getName());
        assertEquals("setVisibility",m_entityRegistry.getYangVisibilityControllerSetter(albumClass).getName());

        assertEquals("getInsertOrder",m_entityRegistry.getOrderByUserGetter(songClass).getName());
        assertEquals("setInsertOrder",m_entityRegistry.getOrderByUserSetter(songClass).getName());
        assertEquals("insertOrder",m_entityRegistry.getOrderByFieldName(songClass));
        Map<QName, Method> yangLeafListGetter = m_entityRegistry.getYangLeafListGetters(songClass); 
        for (QName qname : yangLeafListGetter.keySet()) {
        	assertEquals("getSingers", yangLeafListGetter.get(qname).getName());
        }
        Map<QName, Method> yangLeafListSetter = m_entityRegistry.getYangLeafListSetters(songClass); 
        for (QName qname : yangLeafListSetter.keySet()) {
        	assertEquals("setSingers", yangLeafListSetter.get(qname).getName());
        }

        m_entityRegistry.undeploy("Jukebox");
        assertNull(m_entityRegistry.getParentIdGetter(jukeboxClass));
        assertNull(m_entityRegistry.getSchemaPathGetter(jukeboxClass));
        assertNull(m_entityRegistry.getYangChildGetters(jukeboxClass));

        assertNull(m_entityRegistry.getParentIdGetter(libraryClass));
        assertNull(m_entityRegistry.getSchemaPathGetter(libraryClass));
        assertNull(m_entityRegistry.getYangXmlSubtreeGetter(libraryClass));
        
        assertNull(m_entityRegistry.getOrderByUserGetter(artistClass));
        assertNull(m_entityRegistry.getOrderByUserSetter(artistClass));
        
        assertNull(m_entityRegistry.getOrderByUserGetter(albumClass));
        assertNull(m_entityRegistry.getOrderByUserSetter(albumClass));
        
        assertNull(m_entityRegistry.getOrderByUserGetter(songClass));
        assertNull(m_entityRegistry.getOrderByUserSetter(songClass));
        assertNull(m_entityRegistry.getYangLeafListGetters(songClass));
        assertNull(m_entityRegistry.getYangLeafListSetters(songClass));

    }

    @Test
    public void testAddClassWithYangParentSchemaPathAnnotation(){
        Class mockClass1 = Jukebox.class;
        Class mockClass2 = Library.class;
        Class mockClass3 = Artist.class;
        m_entityRegistry.addClassWithYangParentSchemaPathAnnotation("component1", mockClass1);
        m_entityRegistry.addClassWithYangParentSchemaPathAnnotation("component1", mockClass2);

        assertTrue(m_entityRegistry.classHasYangParentSchemaPathAnnotation(mockClass1));
        assertTrue(m_entityRegistry.classHasYangParentSchemaPathAnnotation(mockClass2));
        assertFalse(m_entityRegistry.classHasYangParentSchemaPathAnnotation(mockClass3));
    }
}
