/**
 * 
 */
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

import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;

/**
 * NotificationComplete is a notification message used inform netconf client that notification subscription is stopped as stop time
 * specified in subscription requrest expires.
 * 
 *
 * 
 */
public class NotificationComplete extends NetconfNotification {
    
    public static final QName TYPE = QName.create(NetconfResources.NC_NOTIFICATION_NS, "notificationComplete");

    /**
     * @throws NetconfMessageBuilderException
     */
    public NotificationComplete() throws NetconfMessageBuilderException {
        init();
    }

    private void init() throws NetconfMessageBuilderException {
        Element replayComplete = new PojoToDocumentTransformer().newNotificationCompleteElement();
        setNotificationElement(replayComplete);
    }
    
    @Override
    public QName getType() {
        return TYPE;
    }
}
