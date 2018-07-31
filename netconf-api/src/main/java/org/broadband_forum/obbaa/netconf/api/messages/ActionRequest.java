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

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ActionRequest extends AbstractNetconfRequest {
    private Element m_actionTreeElement;
    private SchemaPath m_actionTargetpath;
    private QName m_actionQName;

    public QName getActionQName() {
        return m_actionQName;
    }

    public void setActionQName(QName actionQName) {
        this.m_actionQName = actionQName;
    }

    public Element getActionTreeElement() {
        return m_actionTreeElement;
    }

    public void setActionTreeElement(Element actionTreeElement) {
        this.m_actionTreeElement = actionTreeElement;
    }

    public SchemaPath getActionTargetpath() {
        return m_actionTargetpath;
    }

    public void setActionTargetpath(SchemaPath actionTargetpath) {
        this.m_actionTargetpath = actionTargetpath;
    }

    @Override
    public Document getRequestDocumentInternal() throws NetconfMessageBuilderException {
        Document doc = new PojoToDocumentTransformer().newNetconfRpcDocument(m_messageId).addActionElement(m_actionTreeElement).build();
        return doc;
    }

}
