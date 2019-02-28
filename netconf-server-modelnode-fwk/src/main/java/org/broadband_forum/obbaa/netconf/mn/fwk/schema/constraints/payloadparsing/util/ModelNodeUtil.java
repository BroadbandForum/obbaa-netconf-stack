package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util;

import java.util.Collection;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;

public class ModelNodeUtil {
	
    /**
     * Get the ModelNode of schema mount path from input request.
     * @param request 
     * @param globalRegistry
     * @param modelNodeDSManager
     * @return
     */
	public static ModelNode getParentModelNode(Element request, SchemaRegistry globalRegistry,
			ModelNodeDataStoreManager modelNodeDSManager) {
		if (request != null) {
			QName rootQname = globalRegistry.lookupQName(request.getNamespaceURI(), request.getLocalName());
			Collection<DataSchemaNode> rootNodes = globalRegistry.getRootDataSchemaNodes();
			SchemaPath mountPath = null;
			DataSchemaNode rootDsn = null;
			for (DataSchemaNode rootNode : rootNodes) {
				if (rootNode.getQName().getNamespace().equals(rootQname.getNamespace())
						&& rootNode.getQName().getLocalName().equals(rootQname.getLocalName())) {
					rootDsn = rootNode;
					break;
				}
			}
			if (SchemaRegistryUtil.getMountRegistry() != null && SchemaRegistryUtil.getMountRegistry().getMountPath() !=null){
				mountPath = SchemaRegistryUtil.getMountRegistry().getMountPath();
			}
			if (rootDsn == null || mountPath == null) {
				return null;
			}
			//Get the root modelNode
			List<ModelNode> rootModelNodes = modelNodeDSManager.listChildNodes(rootDsn.getPath(), new ModelNodeId());
			List<ModelNode> modelNodeList = getParentModelNode(request, rootDsn, rootModelNodes,
					modelNodeDSManager, mountPath);
			if (modelNodeList != null && !modelNodeList.isEmpty()) {
				return modelNodeList.get(0);
			}
		}
		return null;
	}
    
	/**
	 * Itreate the request elements until mounted path along with schema nodes and called recursively until get modelnode of mountedpath
	 */
	private static List<ModelNode> getParentModelNode(Node request, DataSchemaNode parentSchemaNode,
			List<ModelNode> parentModelNodeList, ModelNodeDataStoreManager manager,
			SchemaPath mountPath) {
		NodeList childNodes = request.getChildNodes();
		for (int index = 0; index < childNodes.getLength(); index++) {
			Node childNode = childNodes.item(index);
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {
				if (parentSchemaNode instanceof DataNodeContainer) {
					for (DataSchemaNode childSchemaNode : ((DataNodeContainer) parentSchemaNode).getChildNodes()) {
						if (childSchemaNode.getQName().getNamespace().toString()
								.equals(childNode.getNamespaceURI().toString())
								&& childSchemaNode.getQName().getLocalName().equals(childNode.getLocalName())) {
							if (childSchemaNode instanceof DataNodeContainer) {
								List<ModelNode> modelNode = manager.listChildNodes(childSchemaNode.getPath(),
										parentModelNodeList.get(0).getModelNodeId());
								// If reached is mountpath, then return the modelnode, otherwise call recursively
								if (childSchemaNode.getPath().equals(mountPath)) {
									return modelNode;
								} else {
									return getParentModelNode(childNode, childSchemaNode, modelNode, manager, mountPath);
								}
							} else if (childSchemaNode instanceof LeafSchemaNode) {
								ModelNode matchedModelNode = null;
								// validate device-id key with list of modelnodes
								for (ModelNode modelnode : parentModelNodeList) {
									if (modelnode.getModelNodeId().getRdnValue(childNode.getLocalName())
											.equals(childNode.getTextContent())) {
										matchedModelNode = modelnode;
										break;
									}
								}
								parentModelNodeList.clear();
								if (matchedModelNode != null) {
									parentModelNodeList.add(matchedModelNode);
								}
							}
							break; // break the inner loop if any childschema node processed
						}
					}

				}
			}
		}
		return parentModelNodeList;
	}

}
