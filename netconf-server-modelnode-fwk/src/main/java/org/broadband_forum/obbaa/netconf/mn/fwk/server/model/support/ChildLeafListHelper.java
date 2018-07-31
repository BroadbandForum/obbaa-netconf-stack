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

import org.broadband_forum.obbaa.netconf.api.messages.InsertOperation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.Collection;

/**
 * Helper used to retrieve/update leaf-list attributes.
 */
public interface ChildLeafListHelper extends ConstraintHelper {

    public Collection<ConfigLeafAttribute> getValue(ModelNode node) throws GetAttributeException;

    public boolean isConfiguration();

    SchemaPath getChildModelNodeSchemaPath();

    public void addChild(ModelNode instance, ConfigLeafAttribute value) throws SetAttributeException;

    public void removeChild(ModelNode instance, ConfigLeafAttribute value) throws ModelNodeDeleteException;

    public void removeAllChild(ModelNode instance) throws ModelNodeDeleteException;

    public void addChildByUserOrder(ModelNode instance, ConfigLeafAttribute value, String leafOperation,
                                    InsertOperation insertOperation) throws SetAttributeException, GetAttributeException;

}