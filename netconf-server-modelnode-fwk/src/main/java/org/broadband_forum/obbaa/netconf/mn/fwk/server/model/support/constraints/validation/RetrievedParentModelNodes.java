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

import java.util.HashMap;
import java.util.List;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;

public class RetrievedParentModelNodes {
    HashMap<String, List<ModelNode>> m_retrievedParentModelNodes = new HashMap<>();

    /*
    generate key with parentBean (modelNodeId), editNode (operation + name), pathSteps, nodeNotDeleted, missingParentNode
    key looks like - /validation/boolean-function-validation-number1/boolean-function-validation/boolean-number-conversion-leaf-mergeboolean-function-validation-true-false
     */
    private String getKey(EditContainmentNode editNode, DynaBean inParentBean, Step[] pathSteps, boolean nodeNotDeleted, boolean missingParentNode, boolean isChoiceOrCaseNode) {

        if(editNode == null || inParentBean == null) {
            return null;
        }

        StringBuilder key = new StringBuilder ();
        // inParentBean addition to key
        String parentModelNodeId = ((ModelNode) inParentBean.get(ModelNodeWithAttributes.MODEL_NODE)).getModelNodeId().xPathString();
        key.append(parentModelNodeId + "-");

        // pathSteps addition to key
        StringBuilder pathStep = new StringBuilder();
        if (pathSteps != null) {
            for(int i = 0; i < pathSteps.length; ++i) {
                if (i > 0) {
                    pathStep.append('/');
                }
                pathStep.append(pathSteps[i]);
            }
        }
        key.append(pathStep + "-");

        // editNode addition to key
        String editNodeKey = editNode.getEditOperation()+editNode.getName();
        key.append(editNodeKey + "-");

        // nodeNotDeleted addition to key
        key.append(nodeNotDeleted + "-");
        key.append("isChoiceOrCaseNode"+isChoiceOrCaseNode+"-");
        
        // missingParentNode addition to key
        key.append(missingParentNode);

        return key.toString();
    }

    public void registerParentModelNodes(List<ModelNode> parentModelNodes, EditContainmentNode editNode, DynaBean inParentBean, Step[] pathSteps, boolean nodeNotDeleted, boolean missingParentNode, boolean isChoiceOrCaseNode) {
        String key = getKey(editNode, inParentBean, pathSteps, nodeNotDeleted, missingParentNode, isChoiceOrCaseNode);
        if(key != null) {
            m_retrievedParentModelNodes.put(key, parentModelNodes);
        }
    }

    public List<ModelNode> fetchAlreadyRegisteredParentModelNodes(EditContainmentNode editNode, DynaBean inParentBean, Step[] pathSteps, boolean nodeNotDeleted, boolean missingParentNode, boolean isChoiceOrCaseNode) {
        String key = getKey(editNode, inParentBean, pathSteps, nodeNotDeleted, missingParentNode, isChoiceOrCaseNode);
        if(key != null) {
            return m_retrievedParentModelNodes.get(key);
        }
        return null;
    }
}
