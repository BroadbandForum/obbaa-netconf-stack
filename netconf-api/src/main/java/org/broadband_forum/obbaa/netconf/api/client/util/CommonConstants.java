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

package org.broadband_forum.obbaa.netconf.api.client.util;

import org.opendaylight.yangtools.yang.common.QName;

public class CommonConstants {
    public static final long DEFAULT_MESSAGE_TIMEOUT = 100000;
    public static final String DEVIATION = "deviation";
    public static final String FEATURE = "feature";
    public static final String IETF_YANG_LIBRARY_NS = "urn:ietf:params:xml:ns:yang:ietf-yang-library";
    public static final String NAMESPACE_PARAM = "namespace";
    public static final String MODULE_PARAM = "module";
    public static final String MODULES_STATE = "modules-state";
    public static final String MODULE_SET_ID = "module-set-id";
    public static final String REVISION_PARAM = "revision";
    public static final String SUB_TREE = "subtree";
    public static final String NAME_PARAM = "name";
    public static final String IETF_NETCONF_NOTIFICATIONS = "urn:ietf:params:xml:ns:yang:ietf-netconf-notifications";
    public static final String NETCONF_CAPABILITY_CHANGE = "netconf-capability-change";
    public static final String YANG_LIBRARY = "yang-library";
    public static final QName CAPS_CHANGE_NOTIFICATION_TYPE = QName.create(IETF_NETCONF_NOTIFICATIONS, NETCONF_CAPABILITY_CHANGE);
    public static final QName QNAME_YANG_LIBRARY = QName.create(IETF_YANG_LIBRARY_NS, MODULE_PARAM);
}
