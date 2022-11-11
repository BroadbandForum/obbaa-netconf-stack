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

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

public class RequestScope {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(RequestScope.class,
            LogAppNames.NETCONF_LIB);;
    private static ThreadLocal<Boolean> m_templateIsUsed = ThreadLocal.withInitial(() -> Boolean.FALSE);
    private RequestScope() {

    }

    private static ThreadLocal<RequestScope> m_requestScope = ThreadLocal.withInitial(() -> new RequestScope());

    public static RequestScope getCurrentScope() {
       /* if (!m_templateIsUsed.get()) {
            throw new RuntimeException("Could not get the current scope as it is not being reset using template. Encapsulate the base caller with RequestScope.withScope() template to ensure the resetting of cache.");
        }*/
        return m_requestScope.get();
    }

    public static void setEnableThreadLocalInUT(boolean newValue) {
        enableThreadLocalInUT = newValue;
        resetScope();
    }
    private static boolean enableThreadLocalInUT = false;

    public static void resetScope() {
        m_requestScope.remove();
    }

    private final Map<String, Object> m_cache = new HashMap<>();

    public static void setScope(RequestScope originalScope) {
        m_requestScope.set(originalScope);
    }

    public void putInCache(String key, Object value) {
        m_cache.put(key, value);
    }

    public void removeFromCache(String key) {
        m_cache.remove(key);
    }

    public Object getFromCache(String key) {
        return m_cache.get(key);
    }

    public static <RT> RT withScope(RsTemplate<RT> rsTemplate) throws RsTemplate.RequestScopeExecutionException {
        if (m_templateIsUsed.get()) {
            return rsTemplate.execute();
        }
        return rsTemplate.executeWithReset();
    }

    public static Boolean isTemplateUsed() {
        return m_templateIsUsed.get();
    }

    public static abstract class RsTemplate<RT> {
        protected abstract RT execute() throws RequestScopeExecutionException;
        final RT executeWithReset() throws RequestScopeExecutionException {
            RequestScope.resetScope();
            RequestScope.m_templateIsUsed.set(true);
            try{
                return execute();
            }finally {
                RequestScope.resetScope();
                RequestScope.m_templateIsUsed.remove();
            }
        };

        public static class RequestScopeExecutionException extends RuntimeException {
            public RequestScopeExecutionException(Exception e) {
                super(e);
            }
        }
    }
}
