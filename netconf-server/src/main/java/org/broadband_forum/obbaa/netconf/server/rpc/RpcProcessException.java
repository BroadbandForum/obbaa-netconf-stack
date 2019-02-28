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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This exception is thrown by the RPC handlers when custom RPC processing/execution fails
 * 
 *
 */

public class RpcProcessException extends Exception {

    private static final long serialVersionUID = 1L;

    private List<NetconfRpcError> m_rpcErrors;

    public RpcProcessException(NetconfRpcError rpcError) {
        super(rpcError.getErrorMessage());
        this.m_rpcErrors = new ArrayList<NetconfRpcError>();
        this.m_rpcErrors.add(rpcError);
    }

    public RpcProcessException(List<NetconfRpcError> rpcErrors) {
        super(makeMessage(rpcErrors));
        this.m_rpcErrors = rpcErrors;
    }

    public List<NetconfRpcError> getRpcErrors() {
        return m_rpcErrors;
    }
    
    private static String makeMessage(List<NetconfRpcError> rpcErrors) {
        return rpcErrors.stream().map((error) -> error.getErrorMessage()).collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        return "RpcProcessException [rpcError=" + m_rpcErrors + "]";
    }
}
