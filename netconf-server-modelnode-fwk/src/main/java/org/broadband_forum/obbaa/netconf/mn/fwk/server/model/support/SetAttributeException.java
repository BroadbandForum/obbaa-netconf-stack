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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;

public class SetAttributeException extends Exception {
    private static final long serialVersionUID = 1L;

    private NetconfRpcError m_rpcError;
    
    public SetAttributeException() {
        super();
    }
    
    public SetAttributeException(NetconfRpcError rpcError, Throwable cause) {
    	super(cause.getMessage(), cause);
        this.m_rpcError = rpcError;
    }
    
    public SetAttributeException(NetconfRpcError rpcError) {
    	super(rpcError.getErrorMessage());
    	this.m_rpcError = rpcError;
    }

    public NetconfRpcError getRpcError() {
        return m_rpcError;
    }


    public SetAttributeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public SetAttributeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SetAttributeException(String message) {
        super(message);
    }

    public SetAttributeException(Throwable cause) {
        super(cause);
    }

}
