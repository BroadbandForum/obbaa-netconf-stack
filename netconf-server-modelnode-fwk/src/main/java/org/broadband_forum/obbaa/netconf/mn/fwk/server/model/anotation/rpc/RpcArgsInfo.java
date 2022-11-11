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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kbhatk on 8/8/16.
 */
public class RpcArgsInfo {
    private List<RpcArgumentInfo> m_rpcArgsInfo = new ArrayList<>();

    public RpcArgumentInfo getRpcArgument(int index) {
        if(index >= m_rpcArgsInfo.size()){
            return null;
        }
        return m_rpcArgsInfo.get(index);
    }

    public void setRpcArgsInfo(List<RpcArgumentInfo> rpcArgs) {
        m_rpcArgsInfo = rpcArgs;
    }

    public List<RpcArgumentInfo> getRpcArguments() {
        return m_rpcArgsInfo;
    }
}
