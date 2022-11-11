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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CompositeEditCommand;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

public interface ModelNode {
	
	public QName getQName();

	Map<QName, ConfigLeafAttribute> getAttributes();

	ConfigLeafAttribute getAttribute(QName qname);

	Set<ConfigLeafAttribute> getLeafList(QName qName);

	Map<QName, LinkedHashSet<ConfigLeafAttribute>> getLeafLists();

	/**
	 * Return the parent node of the this node or null if this is a root node.
	 * @return
	 */
	public ModelNode getParent();
	
	public SchemaPath getModelNodeSchemaPath();

	CompositeEditCommand getEditCommand(EditContext editContext, WritableChangeTreeNode parentChangeTreeNode) throws EditConfigException;

	/**
	 * Unique Id for this node.
	 * @return
	 */
	public ModelNodeId getModelNodeId();

	public String getContainerName();

	/**
	 * Returns the subtree until N level.
	 * @param restconfContext.depth : N (mentioned above)
	 * @param restconfContext.level : recursive argument (temporary argument to compute logic)
	 * @param getConfigContext : context
	 */
    public Element getConfig(GetConfigContext getConfigContext, NetconfQueryParams restconfContext) throws GetException;

    /**
	 * Returns the subtree until N level.
	 * @param restconfContext.depth : N (mentioned above)
	 * @param restconfContext.level : recursive argument (temporary argument to compute logic)
	 * @param parameterObject : context
	 */
    public Element get(GetContext parameterObject, NetconfQueryParams restconfContext) throws GetException;

	public void prepareEditSubTree(EditContainmentNode root, Element configElementContents) throws EditConfigException;

	/**
	 * Returns the subsystem which is responsible for the business logic associated with the management model.
	 * 
	 * @return
	 */
	public SubSystem getSubSystem();
	
	public SubSystem getSubSystem(SchemaPath schemaPath);

	public void editConfig(EditContext editContext, WritableChangeTreeNode changeTreeNode) throws EditConfigException;

    public void copyConfig(Element config) throws CopyConfigException;

	public SchemaRegistry getSchemaRegistry();

	public SchemaRegistry getSchemaRegistryForCurrentNode();

	/**
	 * Get the key values of the List node.
	 * In case the node represents a Container node, an empty map will be returned.
	 * @return
     */
	Map<QName,String> getListKeys() throws GetAttributeException;

	public Object getValue();
	
	public void setValue(Object value);

    boolean isRoot();
    
    public SchemaRegistry getMountRegistry() throws GetException;
    
    public ModelNodeHelperRegistry getMountModelNodeHelperRegistry();
    
    public SubSystemRegistry getMountSubSystemRegistry();
    
    /**
     * Returns true, if the SchemaNode of this ModelNode has the Schema Mount Extension. 
     * @return
     */
    public boolean hasSchemaMount();
    
    /**
     * Returns true, if this is an immediate child of a Schema Mount ModelNode.
     * @return
     */
    public boolean isSchemaMountImmediateChild();
    
    public SchemaPath getParentMountPath();

    default boolean isChildBigList(DataSchemaNode childSchemaNode){
    	return false;
    }
    
    public ModelNodeDataStoreManager getModelNodeDSM();

    ModelNode getChildModelNode(QName qName);

    Collection<ModelNode> getChildModelNodes(QName qName, List<FilterMatchNode> matchCriteria);

    boolean isVisible();

	void interceptEditConfig(EditContext editContext, WritableChangeTreeNode changeTreeNode);
}
