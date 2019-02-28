package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils;

import org.broadband_forum.obbaa.netconf.persistence.jpa.JPAEntityManagerFactory;

/**
 * Created by keshava on 12/3/15.
 */
public class EntityUtils {
    public static JPAEntityManagerFactory getJPAEntityManagerFactory() {
        return new JPAEntityManagerFactory("pma_test");
    }
}
