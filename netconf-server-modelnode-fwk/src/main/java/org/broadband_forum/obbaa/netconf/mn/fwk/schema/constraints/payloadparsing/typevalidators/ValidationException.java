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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcValidationException;

public class ValidationException extends RpcValidationException {

	private static final long serialVersionUID = 1L;

	private List<NetconfRpcError> m_rpcErrors = new LinkedList<NetconfRpcError>();
	
	public ValidationException() {
        super();
    }

	public ValidationException(NetconfRpcError rpcError) {
		super(rpcError);
		this.m_rpcErrors.add(rpcError);
	}
	
	public NetconfRpcError getRpcError() {
	    if (m_rpcErrors==null || m_rpcErrors.isEmpty()) {
	        return null;
	    } else {
	        return m_rpcErrors.get(0);
	    }
	}
	
	public List<NetconfRpcError> getRpcErrors() {
	    if (m_rpcErrors != null) {
	        return new LinkedList<NetconfRpcError>(m_rpcErrors);
	    } else {
	        return Collections.emptyList();
	    }
	}
	
	public void addNetconfRpcError(NetconfRpcError error) {
	    m_rpcErrors.add(error);
	}
	
    public ValidationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }
    
    public String toString(){
        return "ValidationException [rpcError=" + m_rpcErrors + "]";
    }
}
