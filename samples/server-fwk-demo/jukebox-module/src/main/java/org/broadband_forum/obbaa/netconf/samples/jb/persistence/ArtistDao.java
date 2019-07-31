package org.broadband_forum.obbaa.netconf.samples.jb.persistence;

import org.broadband_forum.obbaa.netconf.persistence.jpa.dao.EntityDAO;
import org.broadband_forum.obbaa.netconf.samples.jb.persistence.entities.Artist;
import org.broadband_forum.obbaa.netconf.samples.jb.persistence.entities.ArtistPK;

public interface ArtistDao extends EntityDAO<Artist, ArtistPK> {
}
