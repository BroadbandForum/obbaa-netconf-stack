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

package org.broadband_forum.obbaa.netconf.nc.stack.examples;

import org.apache.log4j.Logger;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientSessionListener;

public class LoggingClientSessionListener implements NetconfClientSessionListener {
    private static final Logger LOGGER = Logger.getLogger(LoggingClientSessionListener.class);

    @Override
    public void sessionClosed(int sessionId) {
        LOGGER.info("Session with id :" + sessionId + " is closed");
    }
}
