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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

/*
 * flag containing the boolean value whether a new node is created or existing deleted for current netconf request
 * called from  restconf 'put' operation, in  order to send correct status code to the client
 */
public class FlagForRestPutOperations {

    public static ThreadLocal<Boolean> m_instanceReplace = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }

        ;
    };

    public static boolean getInstanceReplaceFlag() {
        return m_instanceReplace.get();
    }

    public static void setInstanceReplaceFlag() {
        m_instanceReplace.set(true);
    }

    public static void resetInstanceReplaceFlag() {
        m_instanceReplace.set(false);
    }
}
