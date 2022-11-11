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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

public class NotifSwitchUtil {

    public static final String ENABLE_NEW_NOTIF_STRUCTURE = "ENABLE_NEW_NOTIF_STRUCTURE";

    public static boolean setSystemPropertyAndReturnResetValue(String currentValue, String requiredValue) {
        boolean resetSystemProperty = false;
        if(currentValue == null || !currentValue.equals(requiredValue)) {
            System.setProperty(ENABLE_NEW_NOTIF_STRUCTURE, requiredValue);
            resetSystemProperty = true;
        }
        return resetSystemProperty;
    }

    public static void resetSystemProperty(boolean resetSystemProperty, String previousValue) {
        if(resetSystemProperty && previousValue != null) {
            System.setProperty(ENABLE_NEW_NOTIF_STRUCTURE, previousValue);
        }
    }
}
