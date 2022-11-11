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

import static org.broadband_forum.obbaa.netconf.api.util.CryptUtil2.ENCR_STR_PATTERN;

import java.util.List;

import org.broadband_forum.obbaa.netconf.api.logger.NetconfExtensions;
import org.broadband_forum.obbaa.netconf.api.util.CryptUtil2;
import org.w3c.dom.Element;

public class DOMEncryptVisitor extends DOMVisitor {

    public DOMEncryptVisitor(Element root) {
        super(root);
    }

    @Override
    public void visit(List<Element> elementOfType) {
        if(!elementOfType.isEmpty()){
            Element firstElement = elementOfType.get(0);
            String attribute = firstElement.getAttribute(IS_PASSWORD);
            if (attribute == null || attribute.isEmpty()) {
                // If one element does not have is-password attribute,we can skip checking for other elements since all are of same type.
                return;
            }
        }
        elementOfType
                .stream()
                .filter(element -> !ENCR_STR_PATTERN.matcher(element.getTextContent()).matches())
                .forEach(element -> {
                    element.setTextContent(encrypt(element));
                    element.setAttribute(NetconfExtensions.IS_PASSWORD.getModuleName(), "true");
                });
    }

    protected String encrypt(Element element) {
        return CryptUtil2.encrypt(element.getTextContent());
    }
}
