package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.annotation.dao;

import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.Jukebox;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.persistence.jpa.dao.AbstractDao;

/**
 * Created by keshava on 12/3/15.
 */
public class JukeboxDao extends AbstractDao<Jukebox, String> {
    public JukeboxDao(PersistenceManagerUtil persistenceManagerUtil) {
        super(persistenceManagerUtil, Jukebox.class);
    }

    public void createAndCommit(Jukebox newJukebox) {
        getPersistenceManager().beginTransaction();
        try{
            getPersistenceManager().create(newJukebox);
            getPersistenceManager().commitTransaction();
        }catch (Exception e){
            getPersistenceManager().rollbackTransaction();
            throw e;
        }
    }
}
