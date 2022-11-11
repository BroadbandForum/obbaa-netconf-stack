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

import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.IndexedList;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlModelNodeImpl;
import org.opendaylight.yangtools.yang.common.QName;

public interface ModelNodeWithIndex {

    Map<String, List<ConfigLeafAttribute>> getAttrsOfType(String attrTypeXPath);

    Object getIndexedValue(String s);

    ModelNodeId getSchemaRootId();

    boolean isNewNode();

    boolean isNewNode(String nodeXPath);
    
    Map<QName, IndexedList<ModelNodeId, XmlModelNodeImpl>> getChildren();
}
