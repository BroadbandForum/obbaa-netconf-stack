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

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 *  Helper used to retrieve/update configuration attributes of a node.
 */
public interface ConfigAttributeHelper extends ConstraintHelper {

	SchemaPath getChildModelNodeSchemaPath();

	public String getDefault();

	public ConfigLeafAttribute getValue(ModelNode modelNode) throws GetAttributeException;

	public void setValue(ModelNode abstractModelNode,ConfigLeafAttribute attr) throws SetAttributeException;

	public void removeAttribute(ModelNode abstractModelNode);

	LeafSchemaNode getLeafSchemaNode();
}
