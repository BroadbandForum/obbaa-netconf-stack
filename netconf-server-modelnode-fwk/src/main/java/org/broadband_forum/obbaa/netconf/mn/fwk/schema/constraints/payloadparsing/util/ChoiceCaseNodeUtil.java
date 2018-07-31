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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.CaseEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.ChoiceEffectiveStatementImpl;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;


public class ChoiceCaseNodeUtil {

    public static Set<ChoiceCaseNode> checkIsCaseNodeAndReturnAllOtherCases(SchemaRegistry schemaRegistry, SchemaPath
            schemaPath) {
        ChoiceSchemaNode choiceSchemaNode = getChoiceSchemaNodeFromCaseNodeSchemaPath(schemaRegistry, schemaPath);
        Set<ChoiceCaseNode> remainChoiceCaseNodes = new HashSet<>();
        if (choiceSchemaNode != null) {
            Set<ChoiceCaseNode> allChoiceSchemaNodes = choiceSchemaNode.getCases();
            remainChoiceCaseNodes.addAll(allChoiceSchemaNodes);
            for (ChoiceCaseNode caseNode : allChoiceSchemaNodes) {
                if (caseNode.getPath().equals(schemaPath)) {
                    remainChoiceCaseNodes.remove(caseNode);
                    return remainChoiceCaseNodes;
                }
            }
        }
        return null;
    }

    public static ChoiceSchemaNode getChoiceSchemaNodeFromCaseNodeSchemaPath(SchemaRegistry schemaRegistry,
                                                                             SchemaPath schemaPath) {
        DataSchemaNode schemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
        if (schemaNode != null && schemaNode instanceof ChoiceCaseNode) {
            SchemaPath choiceSchemaPath = schemaNode.getPath().getParent();
            return (ChoiceSchemaNode) schemaRegistry.getDataSchemaNode(choiceSchemaPath);
        }

        return null;
    }

    public static List<DataSchemaNode> getAllNodesFromCases(Set<ChoiceCaseNode> choiceCaseNodes) {
        List<DataSchemaNode> schemaNodes = new ArrayList<>();
        for (ChoiceCaseNode caseNode : choiceCaseNodes) {
            schemaNodes.addAll(caseNode.getChildNodes());
        }
        return schemaNodes;
    }

    public static List<DataSchemaNode> getChildrenUnderChoice(SchemaRegistry schemaRegistry, SchemaPath schemaPath) {
        List<DataSchemaNode> childrenNodes = new ArrayList<>();
        DataSchemaNode node = schemaRegistry.getDataSchemaNode(schemaPath);
        if (node != null && node instanceof DataNodeContainer) {
            for (DataSchemaNode child : ((DataNodeContainer) node).getChildNodes()) {
                if (child instanceof ChoiceSchemaNode) {
                    Set<ChoiceCaseNode> cases = ((ChoiceSchemaNode) child).getCases();
                    for (ChoiceCaseNode caseNode : cases) {
                        childrenNodes.addAll(caseNode.getChildNodes());
                    }
                } else {
                    childrenNodes.add(child);
                }
            }
        }
        return childrenNodes;
    }

    public static boolean isChoiceSchemaPath(SchemaRegistry schemaRegistry, SchemaPath schemaPath) {
        DataSchemaNode currentSchemaNode = schemaRegistry.getDataSchemaNode(schemaPath);
        if (currentSchemaNode != null && currentSchemaNode instanceof ChoiceSchemaNode) {
            return true;
        }
        return false;
    }

    public static SchemaPath getChoiceCaseNodeSchemaPath(SchemaRegistry schemaRegistry, SchemaPath currentNodePath) {
        if (currentNodePath == null) {
            return null;
        }
        DataSchemaNode currentSchemaNode = schemaRegistry.getDataSchemaNode(currentNodePath.getParent());
        if (currentSchemaNode instanceof ChoiceCaseNode) {
            return currentSchemaNode.getPath();
        }
        return getChoiceCaseNodeSchemaPath(schemaRegistry, currentNodePath.getParent());
    }

    public static boolean isChoiceOrCaseNode(DataSchemaNode schemaNode) {
        if (schemaNode instanceof ChoiceEffectiveStatementImpl || schemaNode instanceof CaseEffectiveStatementImpl) {
            return true;
        }
        return false;
    }

}
