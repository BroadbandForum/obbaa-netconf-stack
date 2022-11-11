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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildLeafListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.opendaylight.yangtools.yang.common.QName;

public class ReplaceMNUtil {

    public static boolean checkModelNodeExistsInEditNode(ModelNode modelNode, EditContainmentNode editNode) {
        Map<QName, String> childKeys = getKeysFromModelNode(modelNode);
        QName modelNodeQName = modelNode.getModelNodeSchemaPath().getLastComponent();
        if(childKeys.isEmpty()){ // container
            for (EditContainmentNode editNodeChild : editNode.getChildren()) {
                if(modelNodeQName.equals(editNodeChild.getQName())){
                    return true;
                }
            }
        }else { // list
            for (EditContainmentNode editNodeChild : editNode.getChildren()) {
                if(modelNodeQName.equals(editNodeChild.getQName())){
                    if(!checkIfThisListInstanceExistsInEditNode(childKeys, editNodeChild)){
                        continue;
                    }else{
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public static boolean checkLeafExistsInEditNode(ModelNode modelNode, QName qName, EditContainmentNode editNode, ConfigAttributeHelper configAttributeHelper) {
        ConfigLeafAttribute leafAttribute;
        try {
            leafAttribute = configAttributeHelper.getValue(modelNode);
            EditChangeNode changedNode = editNode.getChangeNode(qName);
            if (leafAttribute != null && changedNode == null) {
                return false;

            }
        } catch (GetAttributeException e) {
            // FIXME REMOVE THIS EXCEPTION FROM INTERFACE. IT IS NEVER THROWN
            throw new RuntimeException(e);
        }
        return true;
    }

    public static boolean checkExistingLeafListIsPresentInEditNode(ModelNode modelNode, QName qName, EditContainmentNode editNode, ChildLeafListHelper childLeafListHelper) {
        Collection<ConfigLeafAttribute> leafListAttributes;
        try {
            leafListAttributes = childLeafListHelper.getValue(modelNode);
            //If leaf-list is present in dataStore but absent in editNode then return false else return true.
            if(leafListAttributes != null && !leafListAttributes.isEmpty() && editNode.getChangeNode(qName) == null) {
                return false;
            }
        } catch (GetAttributeException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public static Map<QName, String> getKeysFromModelNode(ModelNode modelNode) {
        Map<QName, String> childKeys;
        try {
            childKeys = modelNode.getListKeys();
        } catch (GetAttributeException e) {
            // FIXME REMOVE THIS EXCEPTION FROM INTERFACE. IT IS NEVER THROWN
            throw new RuntimeException(e);
        }
        return childKeys;
    }

    public static Map<QName, ConfigLeafAttribute> getKeyFromEditNode(EditContainmentNode editNode) {
        Map<QName, ConfigLeafAttribute> keyAttrs = new HashMap<>();
        for(EditMatchNode node : editNode.getMatchNodes()){
            keyAttrs.put(node.getQName(), node.getConfigLeafAttribute());
        }
        return keyAttrs;
    }

    private static boolean checkIfThisListInstanceExistsInEditNode(Map<QName, String> listKeys, EditContainmentNode editNode) {
        for(Map.Entry<QName,String> childKey : listKeys.entrySet()){
            if(checkKeyExistsInMatchNodes(childKey.getKey(),childKey.getValue(), editNode.getMatchNodes())){
                continue;
            }else{
                return false;
            }
        }
        return true;
    }

    private static boolean checkKeyExistsInMatchNodes(QName keyQName, String keyValue, List<EditMatchNode> matchNodes) {
        for (EditMatchNode matchNode : matchNodes) {
            if(keyQName.equals(matchNode.getQName()) && keyValue.equals(matchNode.getValue())){
                return true;
            }
        }
        return false;
    }

}
