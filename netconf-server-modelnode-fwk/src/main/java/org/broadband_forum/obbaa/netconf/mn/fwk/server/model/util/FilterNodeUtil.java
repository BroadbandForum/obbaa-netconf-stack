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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractFilterNode;

import java.util.Map;

import static org.broadband_forum.obbaa.netconf.api.messages.PojoToDocumentTransformer.XMLNS_NAMESPACE;

public class FilterNodeUtil {

    /**
     * Given an element, gets all its attributes and adds them to filterNode
     * @param filterNode
     * @param filterXmlElement
     */
    public static void xmlElementToFilterNode(AbstractFilterNode filterNode, Element filterXmlElement) {
        if(filterXmlElement.hasAttributes()){
            NamedNodeMap attributes = filterXmlElement.getAttributes();

            for(int i=0; i<attributes.getLength(); i++){
                Attr attr = (Attr) attributes.item(i);
                filterNode.addAttribute(attr.getName(),attr.getValue());
            }

        }
    }

    /**
     * Given a filterNode, adds the attributes to xml element
     * @param filterNode
     * @param filterXmlElement
     */
    public static void filterNodeToXmlElement(AbstractFilterNode filterNode, Element filterXmlElement){
        Map<String, String> attributes = filterNode.getAttributes();

        for(Map.Entry<String,String> mapEntry : attributes.entrySet()){
            filterXmlElement.setAttributeNS(XMLNS_NAMESPACE,mapEntry.getKey(),mapEntry.getValue());
        }
    }
}
