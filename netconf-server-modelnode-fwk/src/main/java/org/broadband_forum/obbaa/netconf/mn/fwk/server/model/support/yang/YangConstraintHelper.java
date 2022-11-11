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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaMountRegistryProvider;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConstraintHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.HelperDrivenModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;

import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MandatoryAware;

public abstract class YangConstraintHelper implements ConstraintHelper{
	
	private DataSchemaNode m_dataSchemaNode;

	public YangConstraintHelper(DataSchemaNode dataSchemaNode) {
		m_dataSchemaNode = dataSchemaNode;
	}
	
	@Override
	public boolean isMandatory() {
	    if (m_dataSchemaNode instanceof MandatoryAware) {
	        return ((MandatoryAware) m_dataSchemaNode).isMandatory();
	    }
		return false;
	}

    protected ModelNodeWithAttributes getNewModelNode(ModelNode parentNode, ModelNodeDataStoreManager dsm) {
        ModelNodeWithAttributes newNode;
        ModelNodeId parentNodeId = parentNode.getModelNodeId();
        if (parentNode.hasSchemaMount()) {
            SchemaMountRegistryProvider provider = (SchemaMountRegistryProvider) RequestScope.getCurrentScope()
                    .getFromCache(SchemaRegistryUtil.MOUNT_CONTEXT_PROVIDER);
            newNode = new ModelNodeWithAttributes(m_dataSchemaNode.getPath(), parentNodeId,
                    provider.getModelNodeHelperRegistry(parentNodeId), provider.getSubSystemRegistry(parentNodeId),
                    provider.getSchemaRegistry(parentNodeId), dsm);
            newNode.setSchemaMountChild(true);
            newNode.setParentMountPath(parentNode.getModelNodeSchemaPath());
        } else {
            newNode = new ModelNodeWithAttributes(m_dataSchemaNode.getPath(), parentNodeId,
                    ((HelperDrivenModelNode)parentNode).getModelNodeHelperRegistry(),((HelperDrivenModelNode)parentNode).getSubSystemRegistry(),
                    ((HelperDrivenModelNode)parentNode).getSchemaRegistry(), dsm);
        }
        return newNode;
    }


}
