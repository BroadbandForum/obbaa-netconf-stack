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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBean;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation.AbstractDataStoreValidatorTest;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RequestScopeJunitRunner.class)
public class RetrievedParentModelNodesTest extends AbstractDataStoreValidatorTest {

    @Test
    public void testCachedReferencedModelNode() {
        EditContainmentNode editContainmentNode = mock(EditContainmentNode.class);
        when(editContainmentNode.getName()).thenReturn("interface");
        when(editContainmentNode.getEditOperation()).thenReturn("merge");

        ModelNode parentModelNode = mock(ModelNode.class);
        ModelNodeId parentModelNodeId = mock(ModelNodeId.class);
        when(parentModelNodeId.xPathString()).thenReturn("/interfaces/interface[name='etho']");
        when(parentModelNode.getModelNodeId()).thenReturn(parentModelNodeId);

        DynaBean parentBean = mock(ModelNodeDynaBean.class);
        when(parentBean.get(ModelNodeWithAttributes.MODEL_NODE)).thenReturn(parentModelNode);
        Step[] steps = ((LocationPath)JXPathUtils.getExpression("/interfaces/interface[name=\"eth0\"]")).getSteps();
        // didn't register, return null
        DSValidationContext validationContext = new DSValidationContext();
        List<ModelNode> listNodes = validationContext.getRetrievedParentModelNodes().fetchAlreadyRegisteredParentModelNodes(editContainmentNode, parentBean, steps, true, true, false);
        assertNull(listNodes);

        // registered empty list, retrun empty list
        List<ModelNode> registerList = Collections.emptyList();
        validationContext.getRetrievedParentModelNodes().registerParentModelNodes(registerList, editContainmentNode, parentBean, steps, true, true, false);
        listNodes = validationContext.getRetrievedParentModelNodes().fetchAlreadyRegisteredParentModelNodes(editContainmentNode, parentBean, steps, true, true, false);
        assertEquals(0, listNodes.size());

        // registered list with 3 ModelNode's, return list with 3 ModelNodes's
        registerList = new ArrayList<>();
        registerList.add(mock(ModelNode.class));
        registerList.add(mock(ModelNode.class));
        registerList.add(mock(ModelNode.class));
        validationContext.getRetrievedParentModelNodes().registerParentModelNodes(registerList, editContainmentNode, parentBean, steps, true, true, false );
        listNodes = validationContext.getRetrievedParentModelNodes().fetchAlreadyRegisteredParentModelNodes(editContainmentNode, parentBean, steps, true, true, false);
        assertEquals(3, listNodes.size());
        assertEquals(registerList, listNodes);
    }

}
