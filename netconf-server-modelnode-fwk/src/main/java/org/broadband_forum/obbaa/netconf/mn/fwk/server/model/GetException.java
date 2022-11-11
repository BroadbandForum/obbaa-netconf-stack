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

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;

public class GetException extends RuntimeException {
    private NetconfRpcError m_rpcError;

    public GetException(NetconfRpcError rpcError) {
        super(rpcError.getErrorMessage());
        this.m_rpcError = rpcError;
    }

    public GetException(String message, Throwable cause) {
		super(message, cause);
		
	}

	public NetconfRpcError getRpcError() {
        return m_rpcError;
    }

    @Override
    public String toString() {
        return "GetException [rpcError=" + m_rpcError + "]";
    }

    private static final long serialVersionUID = 1L;
}
