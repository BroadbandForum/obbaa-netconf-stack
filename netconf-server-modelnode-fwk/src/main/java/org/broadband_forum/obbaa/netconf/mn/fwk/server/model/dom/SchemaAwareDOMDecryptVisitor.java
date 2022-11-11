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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.dom;

import java.util.List;

import org.broadband_forum.obbaa.netconf.api.logger.NetconfExtensions;
import org.broadband_forum.obbaa.netconf.api.util.CryptUtil2;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.w3c.dom.Element;

public class SchemaAwareDOMDecryptVisitor extends SchemaAwareDOMVisitor {
    public SchemaAwareDOMDecryptVisitor(Element root, SchemaRegistry schemaRegistry) {
        super(root, schemaRegistry);
    }

    @Override
    public void visit(DataSchemaNode schemaNode, List<Element> elementOfType) {
        if(EncryptDecryptUtil.isPassword(schemaNode, m_schemaRegistry)){
            elementOfType
                    .stream()
                    .forEach(element -> {
                        element.setTextContent(decrypt(element));
                        element.removeAttribute(NetconfExtensions.IS_PASSWORD.getModuleName());
                    });
        }
    }
    protected String decrypt(Element element) {
        return CryptUtil2.decrypt(element.getTextContent());
    }
}
