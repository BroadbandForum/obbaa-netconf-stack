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
