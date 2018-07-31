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

package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence;

import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn.CONTAINER;
import static org.junit.Assert.assertEquals;

import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.annotation.dao.JukeboxDao;
import org.junit.Before;
import org.junit.Test;

import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Artist;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Jukebox;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Library;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.persistence.jpa.JPAEntityManagerFactory;
import org.broadband_forum.obbaa.netconf.persistence.jpa.ThreadLocalPersistenceManagerUtil;

/**
 * Created by keshava on 12/3/15.
 */
public class EntityDataStoreManagerTest {
    private JukeboxDao m_jukeboxDao;
    private static final ModelNodeId EMPTY_NODE_ID = new ModelNodeId();

    @Before
    public void setUp() {
        m_jukeboxDao = getJukeboxDao();
    }

    private JukeboxDao getJukeboxDao() {
        JPAEntityManagerFactory factory = new JPAEntityManagerFactory("hsql");
        PersistenceManagerUtil util = new ThreadLocalPersistenceManagerUtil(factory);
        return new JukeboxDao(util);
    }

    @Test
    public void testJukeboxTree() {
        Jukebox newJukebox = new Jukebox();
        newJukebox.setParentId(EMPTY_NODE_ID.getModelNodeIdAsString());
        Library library = new Library();
        ModelNodeId jukeboxNodeId = new ModelNodeId().addRdn(new ModelNodeRdn(CONTAINER, JukeboxConstants.JB_NS,
                JukeboxConstants.JUKEBOX_LOCAL_NAME));
        library.setParentId(jukeboxNodeId.getModelNodeIdAsString());
        Artist artist = new Artist();
        ModelNodeId libraryNodeId = new ModelNodeId(jukeboxNodeId).addRdn(new ModelNodeRdn(CONTAINER,
                JukeboxConstants.JB_NS, LIBRARY_LOCAL_NAME));
        artist.setParentId(libraryNodeId.getModelNodeIdAsString());
        artist.setName("keshava");
        library.addArtists(artist);
        newJukebox.setLibrary(library);
        m_jukeboxDao.createAndCommit(newJukebox);
        Jukebox jukebox = m_jukeboxDao.findByIdWithWriteLock(EMPTY_NODE_ID.getModelNodeIdAsString());
        assertEquals("keshava", jukebox.getLibrary().getArtists().get(0).getName());
    }
}
