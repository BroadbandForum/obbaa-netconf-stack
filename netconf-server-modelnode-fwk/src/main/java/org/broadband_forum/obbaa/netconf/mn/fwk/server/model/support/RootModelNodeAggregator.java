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

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ActionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CopyConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetConfigContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NotificationContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.messages.ActionRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;

/**
 * An aggregator for multiple Root level ModelNodes.
 * The calls to RootModelNodeAggregator is redirected to the aggregated ModelNodes.
 * Created by keshava on 7/22/15.
 */
public interface RootModelNodeAggregator {
    RootModelNodeAggregator addModelServiceRoot(String componentId, ModelNode modelNode);

    void addModelServiceRoots(String componentId, List<ModelNode> modelNodes);

    List<ModelNode> getModelServiceRoots();

    List<Element> get(GetContext getContext, NetconfQueryParams params) throws GetException;

    List<Element> action(ActionRequest actionRequest) throws ActionException;

    List<Element> getConfig(GetConfigContext getConfigContext, NetconfQueryParams params)
            throws GetException;

    List<EditContainmentNode> editConfig(EditConfigRequest request, NotificationContext notificationContext)
            throws EditConfigException;

    void copyConfig(List<Element> configElements) throws CopyConfigException;

    void addModelServiceRootHelper(SchemaPath rootNodeSchemaPath, ChildContainerHelper rootNodeHelper);

    void addModelServiceRootHelper(SchemaPath rootNodeSchemaPath, ChildListHelper rootNodeHelper);

    void removeModelServiceRootHelpers(SchemaPath rootSchemaPath);

    void removeModelServiceRoot(String componentId);
}
