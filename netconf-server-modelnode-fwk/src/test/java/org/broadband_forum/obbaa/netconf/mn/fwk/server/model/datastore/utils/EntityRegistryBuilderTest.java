package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistryImpl;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.billboard.Billboard;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistryImpl;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Album;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Artist;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Jukebox;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Library;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Song;

/**
 * Created by keshava on 4/12/15.
 */
public class EntityRegistryBuilderTest {

    private EntityRegistry m_entityRegistry = new EntityRegistryImpl();
    private ModelNodeDSMRegistry m_modelNodeDSMRegistry = new ModelNodeDSMRegistryImpl();

    @Test
    public void testAnalysisHappyCase() throws AnnotationAnalysisException {
        SchemaRegistry schemaRegistry = mock(SchemaRegistry.class);
        List<Class> classes = new ArrayList<>();
        classes.add(Jukebox.class);
        classes.add(Library.class);
        classes.add(Artist.class);
        classes.add(Album.class);
        classes.add(Song.class);
        SchemaPath jukeboxSchemaPath = SchemaPath.create(true, QName.create("http://example.com/ns/example-jukebox", "2014-07-03", "jukebox"));
        SchemaPath librarySchemaPath = new SchemaPathBuilder().withParent(jukeboxSchemaPath).appendLocalName("library").build();
        SchemaPath artistSchemaPath = new SchemaPathBuilder().withParent(librarySchemaPath).appendLocalName("artist").build();
        SchemaPath albumSchemaPath = new SchemaPathBuilder().withParent(artistSchemaPath).appendLocalName("album").build();
        SchemaPath songSchemaPath = new SchemaPathBuilder().withParent(albumSchemaPath).appendLocalName("song").build();

        EntityRegistryBuilder.updateEntityRegistry("Jukebox", classes, m_entityRegistry, schemaRegistry, mock(EntityDataStoreManager.class), m_modelNodeDSMRegistry);

        assertEquals(m_entityRegistry.getEntityClass(jukeboxSchemaPath), Jukebox.class);
        assertEquals(m_entityRegistry.getEntityClass(librarySchemaPath), Library.class);
        assertEquals(m_entityRegistry.getEntityClass(artistSchemaPath), Artist.class);
        assertEquals(m_entityRegistry.getEntityClass(albumSchemaPath), Album.class);
        assertEquals(m_entityRegistry.getEntityClass(songSchemaPath), Song.class);

        assertNotNull(m_entityRegistry.getAttributeGetters(Artist.class).get(QName.create("http://example.com/ns/example-jukebox", "2014-07-03", "name")));
    }
    
    @Test
    public void testChoiceStatement() throws SchemaBuildException, AnnotationAnalysisException {
        List<YangTextSchemaSource> yangFiles = TestUtil.getJukeBoxDeps();
        yangFiles.add(TestUtil.getByteSource("/referenceyangs/jukebox/example-billboard@2014-07-03.yang"));
        SchemaRegistry schemaRegistry = new SchemaRegistryImpl(yangFiles, Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        
        List<Class> classes = new ArrayList<>();
        classes.add(Billboard.class);
        EntityRegistryBuilder.updateEntityRegistry("Jukebox", classes, m_entityRegistry, schemaRegistry, mock(EntityDataStoreManager.class), m_modelNodeDSMRegistry);
        
        SchemaPath billboardSchemaPath = SchemaPath.create(true, QName.create("http://example.com/ns/example-jukebox", "2014-07-03", "billboard"));
        SchemaPath choiceSchemaPath = new SchemaPathBuilder().withParent(billboardSchemaPath).appendLocalName("arward-type").build();
        SchemaPath albumCaseSchemaPath = new SchemaPathBuilder().withParent(choiceSchemaPath).appendLocalName("album-case").build();
        SchemaPath songCaseSchemaPath = new SchemaPathBuilder().withParent(choiceSchemaPath).appendLocalName("single-case").build();
        SchemaPath albumSchemaPath = new SchemaPathBuilder().withParent(albumCaseSchemaPath).appendLocalName("album").build();
        SchemaPath songSchemaPath = new SchemaPathBuilder().withParent(songCaseSchemaPath).appendLocalName("song").build();
        
        assertEquals(Billboard.class, m_entityRegistry.getEntityClass(billboardSchemaPath));
        assertEquals(Album.class, m_entityRegistry.getEntityClass(albumSchemaPath));
        assertEquals(Song.class, m_entityRegistry.getEntityClass(songSchemaPath));
    }
}
