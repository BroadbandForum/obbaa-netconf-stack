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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by keshava on 11/20/15.
 */
public class DataNodeContainerTraverser {
    public static void traverse(DataNodeContainer dataNodeContainer, DataSchemaNodeVisitor visitor) {
        DataNodeContainerTraverser dataNodeContainerTraverser = new DataNodeContainerTraverser(dataNodeContainer);
        Iterator<DataSchemaNode> iterator = dataNodeContainerTraverser.getIterator();
        while (iterator.hasNext()) {
            DataSchemaNode dataSchemaNode = iterator.next();
            visitor.visit(dataSchemaNode);
        }
    }

    private final DataNodeContainer m_container;
    private final List<ListSchemaNode> m_allLists;
    private final List<ContainerSchemaNode> m_allContainers;
    private final List<ChoiceSchemaNode> m_allChoices;
    private final List<DataSchemaNode> m_allChildren;
    private final List<GroupingDefinition> m_allGroupings;
    private final List<TypeDefinition<?>> m_allTypedefs;

    /*
        almost similar to org.opendaylight.yangtools.yang.model.util.DataNodeIterator, but org.opendaylight.yangtools
        .yang.model.util.DataNodeIterator iterator is buggy.

     */
    private DataNodeContainerTraverser(final DataNodeContainer container) {
        if (container == null) {
            throw new IllegalArgumentException("Data Node Container MUST be specified and cannot be NULL!");
        }

        this.m_allContainers = new ArrayList<>();
        this.m_allLists = new ArrayList<>();
        this.m_allChildren = new ArrayList<>();
        this.m_allChoices = new ArrayList<>();
        this.m_allGroupings = new ArrayList<>();
        this.m_allTypedefs = new ArrayList<>();

        this.m_container = container;
        traverse(this.m_container);
    }

    /**
     * Returns list all containers present in subtree.
     *
     * @return Returns list all containers present in subtree.
     */
    public List<ContainerSchemaNode> allContainers() {
        return m_allContainers;
    }

    /**
     * Returns list all lists present in subtree.
     *
     * @return Returns list all containers present in subtree.
     */
    public List<ListSchemaNode> allLists() {
        return m_allLists;
    }

    /**
     * Returns list all choices present in subtree.
     *
     * @return Returns list all containers present in subtree.
     */
    public List<ChoiceSchemaNode> allChoices() {
        return m_allChoices;
    }

    /**
     * Returns list all groupings present in subtree.
     *
     * @return Returns list all containers present in subtree.
     */
    public List<GroupingDefinition> allGroupings() {
        return m_allGroupings;
    }

    /**
     * Returns list all typedefs present in subtree.
     *
     * @return Returns list all containers present in subtree.
     */
    public List<TypeDefinition<?>> allTypedefs() {
        return m_allTypedefs;
    }

    private void traverse(final DataNodeContainer dataNode) {
        if (dataNode == null) {
            return;
        }

        final Iterable<DataSchemaNode> childNodes = dataNode.getChildNodes();
        if (childNodes != null) {
            for (DataSchemaNode childNode : childNodes) {
                /*
                    Not sure why org.opendaylight.yangtools.yang.model.util.DataNodeIterator ignores walking over
                    augmenting nodes
                if (childNode.isAugmenting()) {
                    continue;
                }*/
                m_allChildren.add(childNode);
                if (childNode instanceof ContainerSchemaNode) {
                    final ContainerSchemaNode containerNode = (ContainerSchemaNode) childNode;
                    m_allContainers.add(containerNode);
                    for (ActionDefinition action : containerNode.getActions()) {
                        m_allChildren.add(action.getInput());
                        m_allChildren.add(action.getOutput());
                        traverse(action.getInput());
                        traverse(action.getOutput());
                    }
                    traverse(containerNode);
                } else if (childNode instanceof ListSchemaNode) {
                    final ListSchemaNode list = (ListSchemaNode) childNode;
                    m_allLists.add(list);
                    for (ActionDefinition action : list.getActions()) {
                        m_allChildren.add(action.getInput());
                        m_allChildren.add(action.getOutput());
                        traverse(action.getInput());
                        traverse(action.getOutput());
                    }
                    traverse(list);
                } else if (childNode instanceof ChoiceSchemaNode) {
                    final ChoiceSchemaNode choiceNode = (ChoiceSchemaNode) childNode;
                    m_allChoices.add(choiceNode);
                    final Set<ChoiceCaseNode> cases = choiceNode.getCases();
                    if (cases != null) {
                        for (final ChoiceCaseNode caseNode : cases) {
                            m_allChildren.add(caseNode);
                            traverse(caseNode);
                        }
                    }
                }
            }
        }

        this.m_allTypedefs.addAll(dataNode.getTypeDefinitions());
        traverseModule(dataNode);
        traverseGroupings(dataNode);

    }

    private void checkGrouping(GroupingDefinition grouping) {
        Iterable<DataSchemaNode> childNodes = grouping.getChildNodes();
        for (DataSchemaNode dataSchemaNode : childNodes) {
            checkGroupingNode(dataSchemaNode, grouping);
        }
    }

    private void checkGroupingNode(DataSchemaNode childNode, GroupingDefinition grouping) {
        String xPath = null;
        if (childNode instanceof LeafSchemaNode) {
            LeafSchemaNode leafSchemaNode = (LeafSchemaNode) childNode;
            if (leafSchemaNode.getType() instanceof LeafrefTypeDefinition) {
                LeafrefTypeDefinition type = (LeafrefTypeDefinition) leafSchemaNode.getType();
                xPath = type.getPathStatement().toString();
                checkForUnPrefixedPath(xPath, childNode, grouping);
            }
        } else if (childNode instanceof LeafListSchemaNode) {
            LeafListSchemaNode leafListSchemaNode = (LeafListSchemaNode) childNode;
            if (leafListSchemaNode.getType() instanceof LeafrefTypeDefinition) {
                LeafrefTypeDefinition type = (LeafrefTypeDefinition) leafListSchemaNode.getType();
                xPath = type.getPathStatement().toString();
                checkForUnPrefixedPath(xPath, childNode, grouping);
            }
        } else if (childNode instanceof ContainerSchemaNode) {
            ContainerSchemaNode containerNode = (ContainerSchemaNode) childNode;
            Iterable<DataSchemaNode> childNodes = containerNode.getChildNodes();
            for (DataSchemaNode dataSchemaNode : childNodes) {
                checkGroupingNode(dataSchemaNode, grouping);
            }
        } else if (childNode instanceof ListSchemaNode) {
            ListSchemaNode listSchemaNode = (ListSchemaNode) childNode;
            Iterable<DataSchemaNode> childNodes = listSchemaNode.getChildNodes();
            for (DataSchemaNode dataSchemaNode : childNodes) {
                checkGroupingNode(dataSchemaNode, grouping);
            }
        } else if (childNode instanceof ChoiceSchemaNode) {
            ChoiceSchemaNode choiceNode = (ChoiceSchemaNode) childNode;
            Set<ChoiceCaseNode> cases = choiceNode.getCases();
            for (ChoiceCaseNode caseNode : cases) {
                checkGroupingNode(caseNode, grouping);
            }
        }
    }

    private void checkForUnPrefixedPath(String xPath, DataSchemaNode dataSchemaNode, GroupingDefinition grouping) {
        LocationPath path = (LocationPath) JXPathUtils.getExpression(xPath);
        for (Step step : path.getSteps()) {
            if (step.getNodeTest() instanceof NodeNameTest) {
                NodeNameTest node = (NodeNameTest) step.getNodeTest();
                String prefix = node.getNodeName().getPrefix();
                if (prefix == null || prefix.isEmpty()) {
                    QName groupingQName = grouping.getPath().getLastComponent();
                    QName nodeQName = dataSchemaNode.getPath().getLastComponent();
                    String errorMessage = "Unprefixed Path : " + xPath + " present in the node : "
                            + nodeQName.getLocalName() + " of the grouping : " + groupingQName;
                    throw new RuntimeException(errorMessage);
                }
            }
        }
    }

    private void traverseModule(final DataNodeContainer dataNode) {
        final Module module;
        if (dataNode instanceof Module) {
            module = (Module) dataNode;
            final Set<NotificationDefinition> notifications = module.getNotifications();
            for (NotificationDefinition notificationDefinition : notifications) {
                traverse(notificationDefinition);
            }
        } else if (dataNode instanceof SchemaContext) {


            final Set<RpcDefinition> rpcs = ((SchemaContext) dataNode).getOperations();
            for (RpcDefinition rpcDefinition : rpcs) {
                this.m_allTypedefs.addAll(rpcDefinition.getTypeDefinitions());
                ContainerSchemaNode input = rpcDefinition.getInput();
                if (input != null) {
                    m_allChildren.add(input);
                    traverse(input);
                }
                ContainerSchemaNode output = rpcDefinition.getOutput();
                if (output != null) {
                    m_allChildren.add(output);
                    traverse(output);
                }
            }
        }
    }

    private void traverseGroupings(final DataNodeContainer dataNode) {
        final Set<GroupingDefinition> groupings = dataNode.getGroupings();
        if (groupings != null) {
            for (GroupingDefinition grouping : groupings) {
                m_allGroupings.add(grouping);
                checkGrouping(grouping);
                traverse(grouping);
            }
        }
    }

    public Iterator<DataSchemaNode> getIterator() {
        return m_allChildren.iterator();
    }

}
