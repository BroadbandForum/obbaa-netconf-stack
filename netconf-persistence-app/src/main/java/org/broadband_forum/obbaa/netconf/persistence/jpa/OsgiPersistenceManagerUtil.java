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

package org.broadband_forum.obbaa.netconf.persistence.jpa;

import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PersistenceManagerUtil;

/**
 * Created by keshava on 2/19/16.
 */
public class OsgiPersistenceManagerUtil implements PersistenceManagerUtil{

    private EntityDataStoreManager m_entityDataStoreManager;

    @Override
    public EntityDataStoreManager getEntityDataStoreManager() {
        return m_entityDataStoreManager;
    }

    @Override
    public void closePersistenceManager() {
        //container takes care
    }

    public void setEntityDataStoreManager(EntityDataStoreManager entityDataStoreManager) {
        m_entityDataStoreManager = entityDataStoreManager;
    }
}
