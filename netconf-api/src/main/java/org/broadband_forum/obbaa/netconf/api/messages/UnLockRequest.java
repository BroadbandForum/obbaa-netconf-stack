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

package org.broadband_forum.obbaa.netconf.api.messages;

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.UNLOCK;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.w3c.dom.Document;

/**
 * {@code
 * Netconf <unlock> request.
 * }
 * 
 * 
 * 
 */
public class UnLockRequest extends AbstractNetconfRequest {
    private String m_target;

    @Override
    public Document getRequestDocumentInternal() throws NetconfMessageBuilderException {
        Document doc = new PojoToDocumentTransformer().newNetconfRpcDocument(m_messageId).addUnLockElement(m_target).build();

        return doc;
    }

    /**
     * Set the target data store to unlock.
     * 
     * @param target
     * @return Modified instance of {@link UnLockRequest}
     */
    public UnLockRequest setTarget(String target) {
        this.m_target = target;
        return this;
    }

    /**
     * Set the target data store to unlock as "running".
     * 
     * @return Modified instance of {@link UnLockRequest}
     */
    public UnLockRequest setTargetRunning() {
        this.m_target = StandardDataStores.RUNNING;
        return this;
    }

    public String getTarget() {
        return m_target;
    }

    @Override
    public String toString() {
        return "UnLockRequest [target=" + m_target + "]";
    }

    @Override
    public String getRpcType() {
        return UNLOCK;
    }

}
