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

package org.broadband_forum.obbaa.netconf.api.messages;

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;
import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

public class NetconfConfigChangeNotification extends NetconfNotification {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(NetconfConfigChangeNotification.class, LogAppNames.NETCONF_LIB);
    public static final QName TYPE = QName.create(NetconfResources.IETF_NOTIFICATION_NS, "netconf-config-change");
    private String m_datastore = StandardDataStores.RUNNING;
    private List<EditInfo> m_editList = new ArrayList<EditInfo>();
    private ChangedByParams m_changedByParams;

    public String getDataStore() {
        return m_datastore;
    }

    public NetconfConfigChangeNotification setDataStore(String datastore) {
        this.m_datastore = datastore;
        return this;
    }

    public NetconfConfigChangeNotification setDataStore(int datastore) {
        switch (datastore) {
        case 0:
            this.m_datastore = StandardDataStores.RUNNING;
            break;
        case 1:
            this.m_datastore = StandardDataStores.STARTUP;
            break;
        }
        return this;
    }

    public List<EditInfo> getEditList() {
        return m_editList;
    }

    public NetconfConfigChangeNotification setEditList(List<EditInfo> editList) {
        this.m_editList = editList;
        return this;
    }

    public NetconfConfigChangeNotification setEditList(EditInfo editInfo) {
        if (editInfo != null && !m_editList.contains(editInfo)) {
            this.m_editList.add(editInfo);
        }
        return this;
    }

    public ChangedByParams getChangedByParams() {
        return m_changedByParams;
    }

    public void setChangedByParams(ChangedByParams changedByParams) {
        this.m_changedByParams = changedByParams;
    }
    
    public Element getNetconfConfigChangeNotificationElement() throws NetconfMessageBuilderException {
        PojoToDocumentTransformer transformer = new PojoToDocumentTransformer();
        return transformer.getConfigChangeNotificationElement(m_datastore, m_editList, m_changedByParams);
    }
    
    @Override
    public Element getNotificationElement() {
        try {
            Element netconfConfigChangeNotiticationElement = super.getNotificationElement();
            if (netconfConfigChangeNotiticationElement == null) {
                netconfConfigChangeNotiticationElement = getNetconfConfigChangeNotificationElement();
                setNotificationElement(netconfConfigChangeNotiticationElement);
            }
            return netconfConfigChangeNotiticationElement;
        } catch (NetconfMessageBuilderException e) {
            LOGGER.error("Error while getting netconf-config-change notification element. ", e);
        }
        return null;
    }
    
    @Override
    public QName getType() {
        return TYPE;
    }
}
