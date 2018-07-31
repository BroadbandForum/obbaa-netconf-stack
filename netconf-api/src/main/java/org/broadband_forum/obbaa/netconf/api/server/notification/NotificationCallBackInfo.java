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

package org.broadband_forum.obbaa.netconf.api.server.notification;

import java.util.Set;

import org.opendaylight.yangtools.yang.common.QName;

public class NotificationCallBackInfo {
    private Set<String> m_capabilities;
    private Set<QName> m_notificationTypes;
    private NotificationCallBack m_callBack;

    public Set<String> getCapabilities() {
        return m_capabilities;
    }

    public void setCapabilities(Set<String> capabilities) {
        this.m_capabilities = capabilities;
    }

    public Set<QName> getNotificationTypes() {
        return m_notificationTypes;
    }

    public void setNotificationTypes(Set<QName> notificationTypes) {
        this.m_notificationTypes = notificationTypes;
    }

    public NotificationCallBack getCallBack() {
        return m_callBack;
    }

    public void setCallBack(NotificationCallBack callBack) {
        this.m_callBack = callBack;
    }

}
