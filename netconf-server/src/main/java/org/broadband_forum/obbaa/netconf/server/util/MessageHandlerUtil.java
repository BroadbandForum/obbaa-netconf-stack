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

package org.broadband_forum.obbaa.netconf.server.util;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.AbstractNetconfRequest;
import org.broadband_forum.obbaa.netconf.api.server.notification.NotificationService;
import org.broadband_forum.obbaa.netconf.server.AggregateContext;
import org.broadband_forum.obbaa.netconf.server.RequestCategory;
import org.broadband_forum.obbaa.netconf.server.RequestContext;
import org.broadband_forum.obbaa.netconf.server.RequestTask;
import org.broadband_forum.obbaa.netconf.server.UserContext;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.ual.UALLogger;

public class MessageHandlerUtil {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(MessageHandlerUtil.class, LogAppNames.NETCONF_LIB);

    public static void updateRequestTask(NetconfClientInfo clientInfo, AbstractNetconfRequest request,
                                         RequestTask requestTask, NotificationService notificationService, UALLogger ualLogger) {
        // Adding request context to Request Task
        RequestCategory requestCategoryFromCache = RequestContext.getRequestCategoryTL();

        // Make the default request category as NBI, if no category is setting in
        // RequestScope
        RequestCategory requestCategory = requestCategoryFromCache == null ? RequestCategory.NBI
                : requestCategoryFromCache;
        RequestContext requestContext = new RequestContext(requestCategory);
        requestContext.setLoggedInUserCtxt(getLoggedInUserCtxt(clientInfo));
        requestContext.setAdditionalUserCtxt(getAdditionalUserContext(request));

        requestContext.setApplication(getApplication(request));
        requestContext.setAggregateContext(getAggregateContext());
        requestTask.setNotificationService(notificationService);
        requestTask.setRequestContext(requestContext);
        requestTask.setCanLog(ualLogger.canLog());
    }

    protected static UserContext getLoggedInUserCtxt(NetconfClientInfo clientInfo) {
        if (RequestContext.getLoggedInUserCtxtTL() != null) {
            // intra JVM calls should go here
            return RequestContext.getLoggedInUserCtxtTL().copy();
        } else if (clientInfo.getClientContextSetter() != null
                && clientInfo.getClientContextSetter().getSessionId() != null
                && !clientInfo.getClientContextSetter().getSessionId().toString().isEmpty()) {
            // RESConf, REST calls go here
            return new UserContext(clientInfo.getUsername(),
                    clientInfo.getClientContextSetter().getSessionId().toString());
        } else if (clientInfo.getClientSessionId() != null) {
            // Netconf channel flows here
            return new UserContext(clientInfo.getUsername(), clientInfo.getClientSessionId().toString());
        }
        return null;
    }

    protected static UserContext getAdditionalUserContext(AbstractNetconfRequest request) {
        String username = request.getAdditionalUserContext();
        if(username!= null){
            String sessionId = request.getAdditionalUserSessionId();
            return new UserContext(username, sessionId);
        }
        return null;
    }

    protected static String getApplication(AbstractNetconfRequest request) {
        String application = request.getApplication();
        String applicationTL = RequestContext.getApplicationTL();
        LOGGER.debug("Request application: '{}', request context thread local application: '{}'", application,
                applicationTL);
        return application != null ? application : applicationTL != null ? applicationTL : null;
    }

    protected static AggregateContext getAggregateContext() {
        AggregateContext aggregateContextTL = RequestContext.getAggregateContextTL();
        return aggregateContextTL != null ? aggregateContextTL : null;
    }
}
