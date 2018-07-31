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

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.Map;

/**
 * Helper used to retrieve/create/delete/update a container node.
 */
public interface ChildContainerHelper extends ConstraintHelper {

    ModelNode getValue(ModelNode node) throws ModelNodeGetException;

    SchemaPath getChildModelNodeSchemaPath();

    void deleteChild(ModelNode parentNode) throws ModelNodeDeleteException;

    ModelNode createChild(ModelNode parentNode, Map<QName, ConfigLeafAttribute> keyAttrs) throws
            ModelNodeCreateException;

    ModelNode setValue(ModelNode parentNode, ModelNode childNode) throws ModelNodeSetException;

    public DataSchemaNode getSchemaNode();

}