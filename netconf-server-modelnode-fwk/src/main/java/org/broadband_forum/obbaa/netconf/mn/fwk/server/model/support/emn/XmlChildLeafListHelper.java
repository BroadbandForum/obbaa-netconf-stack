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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDeleteException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.SetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.dsm.DsmChildLeafListHelper;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

public class XmlChildLeafListHelper extends DsmChildLeafListHelper {
    public XmlChildLeafListHelper(LeafListSchemaNode leafListSchemaNode, QName qname, ModelNodeDataStoreManager
            modelNodeDSM, SchemaRegistry schemaRegistry) {
        super(leafListSchemaNode, qname, modelNodeDSM, schemaRegistry);
    }

    @Override
    public void addChildByUserOrder(ModelNode parentNode, ConfigLeafAttribute value, String operation,
                                    InsertOperation insertOperation) throws SetAttributeException,
            GetAttributeException {
        ModelNode reloadedNode = XmlDsmUtils.reloadParentNode(parentNode, m_modelNodeDSM, m_schemaRegistry);
        super.addChildByUserOrder(reloadedNode, value, operation, insertOperation);
    }

    @Override
    public void removeChild(ModelNode modelNode, ConfigLeafAttribute value) throws ModelNodeDeleteException {
        ModelNode reloadedNode = XmlDsmUtils.reloadParentNode(modelNode, m_modelNodeDSM, m_schemaRegistry);
        super.removeChild(reloadedNode, value);
    }
}
