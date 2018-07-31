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

package org.broadband_forum.obbaa.netconf.api;

/**
 * Both netconf client and server use configuration builders to define their configuration.
 * <p>
 * This exception is thrown by the configuration builders when there is an error in the supplied netconf
 * client/server configuration. For
 * example: when the client/server has not specified a valid transport.
 *
 * @author keshava
 */
public class NetconfConfigurationBuilderException extends Exception {
    private static final long serialVersionUID = 1L;

    public NetconfConfigurationBuilderException() {
    }

    public NetconfConfigurationBuilderException(String message) {
        super(message);
    }

    public NetconfConfigurationBuilderException(Throwable cause) {
        super(cause);
    }

    public NetconfConfigurationBuilderException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetconfConfigurationBuilderException(String message, Throwable cause, boolean enableSuppression, boolean
            writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
