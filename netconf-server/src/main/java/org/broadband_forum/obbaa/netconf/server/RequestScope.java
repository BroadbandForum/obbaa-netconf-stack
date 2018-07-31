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

package org.broadband_forum.obbaa.netconf.server;

import java.util.HashMap;
import java.util.Map;

public class RequestScope {

    private RequestScope() {

    }

    private static ThreadLocal<RequestScope> m_requestScope = new ThreadLocal<RequestScope>() {
        @Override
        protected RequestScope initialValue() {
            return new RequestScope();
        }
    };

    private static boolean enableThreadLocalInUT = false;

    private static boolean m_useActualRequestScope = false;

    public static void setEnableThreadLocalInUT(boolean newValue) {
        enableThreadLocalInUT = newValue;
        resetScope();
    }

    public static RequestScope getCurrentScope() {
        // For UT in Dev & Build Boxes
        // SelfUT is to exhibit actual behaviour. In other cases; there is no threadLocal behaviour.
        if (!m_useActualRequestScope) {
            if (!enableThreadLocalInUT && System.getenv("DB_HOST") != null) {
                //if DB_HOST is present and Thread local is absent. Means we are starting runkaraf.sh
                m_useActualRequestScope = true;
                return m_requestScope.get();
            } else if (!enableThreadLocalInUT && (System.getenv("MAVEN_HOME") != null || System.getenv
                    ("JENKINS_HOME") != null)) {
                return new RequestScope();
            }
        }
        return m_requestScope.get();
    }

    public static void resetScope() {
        m_requestScope.remove();
    }

    private final Map<String, Object> m_cache = new HashMap<>();

    public void putInCache(String key, Object value) {
        m_cache.put(key, value);
    }

    public Object getFromCache(String key) {
        return m_cache.get(key);
    }

}
