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

package org.broadband_forum.obbaa.netconf.server.rpc;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;

/**
 * This class is a Runtime exception to ease in DB rollback when a validation exception happens. Apache Aries honors only 
 * Runtime Exceptions to rollback 
 *
 */
public class RpcValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private NetconfRpcError m_rpcError;
    
    public RpcValidationException() {
        super();
    }

    public RpcValidationException(NetconfRpcError rpcError) {
        super(rpcError.getErrorMessage());
        this.m_rpcError = rpcError;
    }
    
    public RpcValidationException(String message, Throwable cause, NetconfRpcError rpcError, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        m_rpcError = rpcError;
    }

    public RpcValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RpcValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcValidationException(String message) {
        super(message);
    }

    public RpcValidationException(Throwable cause) {
        super(cause);
    }

    public NetconfRpcError getRpcError() {
        return m_rpcError;
    }

    @Override
    public String toString() {
    	if (m_rpcError!=null) {
            return "RpcValidationException [rpcError=" + m_rpcError + "]";
    	}
    	return super.toString();
    }
}
