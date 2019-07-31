package org.broadband_forum.obbaa.netconf.samples.jb.persistence;

import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;
import org.broadband_forum.obbaa.netconf.persistence.jpa.dao.AbstractDao;
import org.broadband_forum.obbaa.netconf.samples.jb.persistence.entities.Artist;
import org.broadband_forum.obbaa.netconf.samples.jb.persistence.entities.ArtistPK;


public class ArtistDaoImpl extends AbstractDao<Artist, ArtistPK> implements ArtistDao {
    public ArtistDaoImpl(PersistenceManagerUtil persistenceManagerUtil) {
        super(persistenceManagerUtil, Artist.class);
    }
}
