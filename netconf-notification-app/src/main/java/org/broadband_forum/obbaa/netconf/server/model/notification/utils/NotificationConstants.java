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

package org.broadband_forum.obbaa.netconf.server.model.notification.utils;

import org.broadband_forum.obbaa.netconf.api.NetconfCapability;




/**
 * Created by nhtoan on 1/21/16.
 */
public class NotificationConstants {
    public static final String NAMESPACE = "urn:ietf:params:xml:ns:netmod:notification";
    public static final String YANG_REVISION = "2008-07-14";
    public static final String MODULE_NAME = "nc-notifications";
    public static final String CAPABILITY = (new NetconfCapability(NAMESPACE, MODULE_NAME, YANG_REVISION)).toString();
}
