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

import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;

public interface ModelNode {

    public QName getQName();

    /**
     * Return the parent node of the this node or null if this is a root node.
     *
     * @return
     */
    public ModelNode getParent();

    public SchemaPath getModelNodeSchemaPath();


    /**
     * Unique Id for this node.
     *
     * @return
     */
    public ModelNodeId getModelNodeId();

    public String getContainerName();

    /**
     * Returns the subtree until N level.
     *
     * @param restconfContext.depth : N (mentioned above)
     * @param restconfContext.level : recursive argument (temporary argument to compute logic)
     * @param getConfigContext      : context
     */
    public Element getConfig(GetConfigContext getConfigContext, NetconfQueryParams restconfContext) throws GetException;

    /**
     * Returns the subtree until N level.
     *
     * @param restconfContext.depth : N (mentioned above)
     * @param restconfContext.level : recursive argument (temporary argument to compute logic)
     * @param parameterObject       : context
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

    public void editConfig(EditContext editContext) throws EditConfigException;

    public void copyConfig(Element config) throws CopyConfigException;

    public SchemaRegistry getSchemaRegistry();

    /**
     * Get the key values of the List node.
     * In case the node represents a Container node, an empty map will be returned.
     *
     * @return
     */
    Map<QName, String> getListKeys() throws GetAttributeException;

    public Object getValue();

    public void setValue(Object value);

    boolean isRoot();
}
