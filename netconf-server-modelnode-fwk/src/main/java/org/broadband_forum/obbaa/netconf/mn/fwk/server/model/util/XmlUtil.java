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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;

public class XmlUtil {
	public static void addXmlValue(Document document, Element parent, String name, String value) {
		Element element = document.createElement(name);
		element.setTextContent(value);

		parent.appendChild(element);
	}
	
	public static void addXmlValue(Document document, Element parent, String name, Integer value) {
		Element element = document.createElement(name);
		element.setTextContent(value.toString());

		parent.appendChild(element);
	}
	
	public static Collection<ModelNode> createCollection(ListSchemaNode listSchemaNode) {
   		if (!(listSchemaNode.isUserOrdered())) {
   		    /**
   		     * Reason for choosing a linkedList over an arrayList
   		     * 1) Can easily expand if there are really large number of objects vs memory fragments managment and GC
   		     * 2) Quick to add to the list when a lot of objects have to be added. 
   		     */
   			return new LinkedList<>();
   		} 
  		return new ArrayList<>();
	}
	
	public static Collection<ConfigLeafAttribute> createCollection(LeafListSchemaNode leafListSchemaNode) {
   		if (!(leafListSchemaNode.isUserOrdered())) {
   			return new LinkedList<>();
   		} 
  		return new ArrayList<>();
	}
}
