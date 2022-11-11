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

import java.util.LinkedList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;

public class ActionException extends Exception {
    private List<NetconfRpcError> m_rpcErrors = new LinkedList<NetconfRpcError>();

	public ActionException(NetconfRpcError rpcError) {
	    super(rpcError.getErrorMessage());
        this.m_rpcErrors.add(rpcError);
	}

	public ActionException(String message, Throwable cause) {
		super(message, cause);

	}

	public List<NetconfRpcError> getRpcErrors() {
		return m_rpcErrors;
	}

	@Override
	public String toString() {
		return "ActionException [rpcError=" + m_rpcErrors + "]";
	}

	private static final long serialVersionUID = 1L;
}
