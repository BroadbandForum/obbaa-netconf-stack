package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import org.broadband_forum.obbaa.netconf.persistence.DataStoreMetaProvider;

/**
 * Created by keshava on 12/18/15.
 */
public class DummyDataStoreMetaProvider implements DataStoreMetaProvider{
    @Override
    public long getDataStoreVersion(String moduleId) {
        return 0;
    }

    @Override
    public void updateDataStoreVersion(String name, long newVersion) {
        //dummy could not care less !
    }
}
