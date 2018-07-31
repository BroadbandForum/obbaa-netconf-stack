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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import java.util.Collection;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;

/**
 * Helper used to retrieve/create/delete/update a list node.
 */
public interface ChildListHelper extends ConstraintHelper {

    /**
     * Returns all children modelNode instances which matches all criteria.
     *
     * @param parentNode
     * @param matchCriteria
     * @return
     * @throws ModelNodeGetException
     */
    public Collection<ModelNode> getValue(ModelNode parentNode, Map<QName, ConfigLeafAttribute> matchCriteria) throws
            ModelNodeGetException;

    public ModelNode addChild(ModelNode instance, String childName, Map<QName, ConfigLeafAttribute> keyAttrs,
                              Map<QName, ConfigLeafAttribute> configAttrs) throws
            ModelNodeCreateException;

    public void removeChild(ModelNode instance, ModelNode item) throws ModelNodeDeleteException;

    public void removeAllChild(ModelNode instance) throws ModelNodeDeleteException;

    public ModelNode createModelNode(ModelNode parentNode, Map<QName, ConfigLeafAttribute> keyAttrs) throws
            ModelNodeCreateException;

    public ModelNode addChild(ModelNode parentNode, ModelNode childNode) throws ModelNodeSetException;

    SchemaPath getChildModelNodeSchemaPath();

    public ModelNode addChildByUserOrder(ModelNode instance, Map<QName, ConfigLeafAttribute> keyAttrs, Map<QName,
            ConfigLeafAttribute> configAttrs, InsertOperation insertOperation, ModelNode insertValue) throws
            ModelNodeCreateException;

    public ModelNode addChildByUserOrder(ModelNode instance, ModelNode childNode, ModelNode indexNode,
                                         InsertOperation insertOperation) throws ModelNodeCreateException;
}