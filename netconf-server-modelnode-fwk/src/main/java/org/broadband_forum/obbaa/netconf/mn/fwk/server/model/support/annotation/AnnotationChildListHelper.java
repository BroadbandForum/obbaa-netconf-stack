package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetConfigContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.CreateStrategy;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeSetException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class AnnotationChildListHelper extends AnnotationConstraintHelper implements ChildListHelper {
	private Method m_getterMethod;
    private Class<? extends ModelNode> m_childClass;
    @SuppressWarnings("unused")
    private CreateStrategy m_createStrategy;
    private String m_factoryName;
    private SchemaPath m_childSchemaPath;
	private ModelNodeHelperRegistry m_modelNodeHelperRegistry;

    public AnnotationChildListHelper(Method getterMethod, SchemaPath childSchemaPath, Class<? extends ModelNode> childClass, CreateStrategy createStrategy, String factoryName, ModelNodeHelperRegistry modelNodeHelperRegistry) {
        m_getterMethod = getterMethod;
        m_childClass = childClass;
        m_createStrategy  = createStrategy;
        m_factoryName = factoryName;
        m_childSchemaPath = childSchemaPath;
        m_modelNodeHelperRegistry = modelNodeHelperRegistry;
    }

	@Override
	public Collection<ModelNode> getValue(ModelNode parent, Map<QName, ConfigLeafAttribute> matchCriteria) throws ModelNodeGetException {
		try {
			List<ModelNode> matchedNodes = new ArrayList<>();
			Collection<ModelNode> modelNodes = (Collection<ModelNode>) m_getterMethod.invoke(parent);
			Iterator<ModelNode> modelNodesIter = modelNodes.iterator();
			while(modelNodesIter.hasNext()){
				ModelNode node = modelNodesIter.next();
				if(isMatch(matchCriteria, node)){
					//match success
					matchedNodes.add(node);
				}
			}
			return matchedNodes;
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | GetException e) {
			throw new ModelNodeGetException("could not get child node list from ModelNode:" + parent, e);
		}
	}

	/* (non-Javadoc)
     * @see ChildListHelper#addChild(org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode, java.util.Map)
     */
    @Override
	public ModelNode addChild(ModelNode parent, String childUri, Map<QName, ConfigLeafAttribute> keyAttrs, Map<QName, ConfigLeafAttribute> configAttrs) throws ModelNodeCreateException {
        ModelNode newNode = null;
        newNode = getNewModelNode(parent, keyAttrs);
        try {
			getValue(parent).add(newNode);
		} catch (ModelNodeGetException e) {
			throw new ModelNodeCreateException("could not get child node list from parent node to add child", e);
		}
        return newNode;
    }

	private  Collection<ModelNode> getValue(ModelNode parent) throws ModelNodeGetException {
		try {
			return (Collection<ModelNode>) m_getterMethod.invoke(parent);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new ModelNodeGetException("could not get child node list from ModelNode:" + parent, e);
		}
	}

	/* (non-Javadoc)
     * @see ChildListHelper#removeChild(org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode, org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode)
     */
    @Override
	public void removeChild(ModelNode instance, ModelNode item) throws ModelNodeDeleteException {
        try {
			getValue(instance).remove(item);
		} catch (ModelNodeGetException e) {
			throw new ModelNodeDeleteException("could not get child node list from parent node to remove child", e);
		}
        
    }
/*
	@Override
	public SchemaPath getChildSchemaPath() {
        return m_childSchemaPath;
	}*/

	@Override
	public void removeAllChild(ModelNode instance) throws ModelNodeDeleteException {
		try {
			getValue(instance).clear();
		} catch (ModelNodeGetException e) {
			throw new ModelNodeDeleteException("could not get child node list from parent node to remove child", e);
		}
	}

	@Override
	public ModelNode createModelNode(ModelNode parent, Map<QName, ConfigLeafAttribute> keyAttrs) throws ModelNodeCreateException {
		QName qname = parent.getQName();
		ModelNode newNode = getNewModelNode(parent, keyAttrs, qname);
		return newNode;
	}

	@Override
	public ModelNode addChild(ModelNode parentNode, ModelNode childNode) throws ModelNodeSetException {
		try {
			getValue(parentNode).add(childNode);
		} catch (ModelNodeGetException e) {
			throw new ModelNodeSetException("could not get child node list from parent node to add child", e);
		}
        return childNode;
	}

    @Override
    public SchemaPath getChildModelNodeSchemaPath() {
        return m_childSchemaPath;
    }
    
	
	@Override
	public ModelNode addChildByUserOrder(ModelNode instance, Map<QName, ConfigLeafAttribute> keyAttrs, Map<QName, ConfigLeafAttribute> configAttrs,
										 InsertOperation insertOperation, ModelNode insertValue) throws ModelNodeCreateException {
	    ModelNode newNode = getNewModelNode(instance, keyAttrs);
		try {
			getValue(instance).add(newNode);
		} catch (ModelNodeGetException e) {
			throw new ModelNodeCreateException("could not get child node list from parent node to add child", e);
		}
        return newNode;
	}
	
	@Override
	public ModelNode addChildByUserOrder(ModelNode instance, ModelNode childNode, ModelNode indexNode,
										 InsertOperation insertOperation) {
		return null;
	}

	@Override
	public ListSchemaNode getSchemaNode() {
		return null;
	}

	private static boolean isMatch(Map<QName, ConfigLeafAttribute> matchCriteria, ModelNode node) throws GetException {
		Element getConfig = node.getConfig(new GetConfigContext(DocumentUtils.createDocument(), null),
				NetconfQueryParams.NO_PARAMS);
		for(Map.Entry<QName, ConfigLeafAttribute> criteriaEntry : matchCriteria.entrySet()){
			Node matchNode = DocumentUtils.getChildNodeByName(getConfig, criteriaEntry.getKey().getLocalName(), criteriaEntry
					.getKey().getNamespace().toString());
			if(matchNode != null){
				if(!criteriaEntry.getValue().getStringValue().equals(matchNode.getTextContent())){
					return false;
				}
			}
		}
		return true;
	}

    protected ModelNode getNewModelNode(ModelNode parent, Map<QName, ConfigLeafAttribute> keyAttrs, Object...objects) throws ModelNodeCreateException {
        ModelNode newNode;
        ModelNodeFactory factory = m_modelNodeHelperRegistry.getCreateFactory(m_factoryName);
        if (parent.hasSchemaMount()) {
            newNode = factory.getModelNode(m_childClass, parent, new ModelNodeId(parent.getModelNodeId()), parent.getMountModelNodeHelperRegistry(), parent.getMountSubSystemRegistry(), parent.getMountRegistry(), keyAttrs, objects);
        } else {
            newNode = factory.getModelNode(m_childClass, parent, new ModelNodeId(parent.getModelNodeId()), ((HelperDrivenModelNode)parent).getModelNodeHelperRegistry(), ((HelperDrivenModelNode)parent).getSubSystemRegistry(), ((HelperDrivenModelNode)parent).getSchemaRegistry(), keyAttrs, objects);
        }
        return newNode;
    }

}
