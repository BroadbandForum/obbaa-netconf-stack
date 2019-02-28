package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Responsible for conversion of a given Xml String into XmlModelNodeImpl and vice versa.
 * Does lazy conversion (Only given node attributes are set, children are lazily initialised upon XmlModelNodeImpl#getChildren() call).
 *
 */
public interface XmlModelNodeToXmlMapper {

    static boolean nodesMatch(Node nodeXml, QName nodeQName) {
        if(nodeXml == null){
            return false;
        }
        String childElementLocalName = nodeXml.getLocalName();
        String childElementNamespace = nodeXml.getNamespaceURI();
        if (nodeQName.getLocalName().equals(childElementLocalName) && nodeQName.getNamespace().toString().equals(childElementNamespace)) {
            return true;
        }
        return false;
    }

    XmlModelNodeImpl getModelNodeFromParentSchemaPath(Element nodeXml, SchemaPath parentSchemaPath, ModelNodeId parentId, XmlModelNodeImpl parentModelNode, ModelNodeDataStoreManager modelNodeDsm);

    List<XmlModelNodeImpl> getModelNodeFromNodeSchemaPath(Element nodeXml, Map<QName, ConfigLeafAttribute> configAttrsFromEntity, SchemaPath nodeSchemaPath, ModelNodeId parentId, XmlModelNodeImpl parentModelNode, ModelNodeDataStoreManager modelNodeDsm);

    XmlModelNodeImpl getModelNode(Object entity, ModelNodeDataStoreManager modelNodeDSM);

    Element getXmlValue(XmlModelNodeImpl xmlModelNode);

    XmlModelNodeImpl getRootXmlModelNode(ModelNodeWithAttributes modelNode, ModelNodeDataStoreManager dsm);
}
