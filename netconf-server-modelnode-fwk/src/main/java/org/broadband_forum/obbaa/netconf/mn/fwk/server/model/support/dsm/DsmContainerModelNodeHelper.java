package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKeyBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeSetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.YangConstraintHelper;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.Map;

/**
 * Created by keshava on 21/12/15.
 */
public class DsmContainerModelNodeHelper extends YangConstraintHelper implements ChildContainerHelper {
    protected final ContainerSchemaNode m_schemaNode;
    protected final ModelNodeDataStoreManager m_modelNodeDSM;
    /**
     * @param schemaNode
     * @param modelNodeDSM
     */
    public DsmContainerModelNodeHelper(ContainerSchemaNode schemaNode,ModelNodeDataStoreManager modelNodeDSM) {
        super(schemaNode);
        m_schemaNode = schemaNode;
        m_modelNodeDSM = modelNodeDSM;
    }

    @Override
    public ModelNode getValue(ModelNode node) throws ModelNodeGetException {
        if (m_schemaNode != null){
            ModelNodeId parentNodeId;
            if(node != null) {
                parentNodeId = node.getModelNodeId();
            }else {
                parentNodeId = new ModelNodeId();
            }
            return m_modelNodeDSM.findNode(m_schemaNode.getPath(), new ModelNodeKeyBuilder().build(), parentNodeId);
        }
        return null;
    }

    @Override
    public SchemaPath getChildModelNodeSchemaPath() {
        return m_schemaNode.getPath();
    }

    @Override
    public DataSchemaNode getSchemaNode(){
    	return (DataSchemaNode)m_schemaNode;
    }

    @Override
    public ModelNode createChild(ModelNode parentNode, Map<QName, ConfigLeafAttribute> keyAttrs) throws ModelNodeCreateException {
        ModelNodeWithAttributes newNode = getNewModelNode(parentNode, m_modelNodeDSM);
        newNode.setAttributes(keyAttrs);
        m_modelNodeDSM.createNode(newNode, parentNode.getModelNodeId());
        return newNode;
    }

    @Override
    public ModelNode setValue(ModelNode parentNode, ModelNode childNode) throws ModelNodeSetException {
        ModelNodeId parentNodeId = null;
        if(parentNode != null) {
            parentNodeId = parentNode.getModelNodeId();
        }else {
            parentNodeId = new ModelNodeId();
        }
        m_modelNodeDSM.createNode(childNode, parentNodeId);
        return childNode;
    }

    @Override
    public void deleteChild(ModelNode parentNode) throws ModelNodeDeleteException {
        try{
            if(parentNode !=null){
                ModelNode value = getValue(parentNode);
                if(value != null) {
                    m_modelNodeDSM.removeNode(value, parentNode.getModelNodeId());
                }
            }
        } catch (ModelNodeGetException e) {
            throw new ModelNodeDeleteException(e);
        }
    }

    @Override
    public boolean isChildSet(ModelNode node) {
        return true;
    }
}
