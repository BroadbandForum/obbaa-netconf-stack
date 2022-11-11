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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.logger.NetconfExtensions;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.w3c.dom.Element;

public class EncryptDecryptUtil {

    public static Boolean isPassword(DataSchemaNode schemaNode, SchemaRegistry schemaRegistry){
        return schemaNode != null ?
                NetconfExtensions.IS_PASSWORD.isExtensionIn(schemaNode) || isPasswordYinAnnotationPresent(schemaNode, schemaRegistry)
                : false;
    }

    private static Boolean isPasswordYinAnnotationPresent(DataSchemaNode schemaNode, SchemaRegistry schemaRegistry) {
        return schemaRegistry.getYinAnnotationService() != null ?
                schemaRegistry.getYinAnnotationService().isPassword(schemaNode, schemaRegistry.getName()) : false;
    }

    public static Map<QName, List<Element>> getChildrenOfType(Element currentNode) {
        Map<QName, List<Element>> result = new HashMap<>();
        List<Element> childNodes = DocumentUtils.getChildElements(currentNode);
        childNodes.forEach(child ->{
            QName childQName = QName.create(child.getNamespaceURI(), child.getLocalName());
            List<Element> existingList = result.get(childQName);
            if(existingList == null){
                existingList = new ArrayList<>();
                result.put(childQName,existingList);
            }
            existingList.add(child);
        });
        return result;
    }
}
