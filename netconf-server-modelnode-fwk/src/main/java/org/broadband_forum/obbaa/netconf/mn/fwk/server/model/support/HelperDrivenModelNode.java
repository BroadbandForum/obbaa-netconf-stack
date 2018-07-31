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

import static org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil.isRootSchemaPath;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.StateAttributeUtil.getFilteredElements;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.EditTreeTransformer.getLocalName;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.EditTreeTransformer.resolveLocalName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CopyConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetConfigContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeChange;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeChangeType;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NoopSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.StateAttributeGetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.AddChildToListCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CreateChildCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.DeleteChildCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.DeleteChildrenUnderRootCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.DeleteLeafCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.DeleteLeafListCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.MergeChildInListCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.RemoveChildFromListCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.ReplaceChildCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.ReplaceChildInListCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.processing.ModelNodeConstraintProcessor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.YangTypeToClassConverter;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeSource;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.NotificationDecorator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.SubSystemValidationDecorator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.InstanceIdentifierTypeDefinition;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.CompositeEditCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.DelegateToChildCommand;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands.SetConfigAttributeCommand;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

/**
 * A Abstract modelNode implementation that uses a set of Helpers to perform its job.
 * For Example ConfigAttributeHelper s help in getting the config attributes related to the node.
 */
public abstract class HelperDrivenModelNode implements ModelNode {

    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(HelperDrivenModelNode.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");

    private static final String MODELNODEID_GET_ERROR = "Error while getting ModelNodeId for ";
    public static final String MANDATORY_ATTRIBUTE_VALIDATION_ENABLED = "mandatoryAttributeValidationEnabled";

    private final ModelNode m_parent;


    private ModelNodeHelperRegistry m_modelNodeHelperRegistry = null;

    private SubSystemRegistry m_subSystemRegistry;
    private ModelNodeId m_parentNodeId;
    private ModelNodeId m_modelNodeId;
    private ModelNodeInterceptor m_interceptor;

    private SchemaRegistry m_schemaRegistry;

    public HelperDrivenModelNode(ModelNode parent, ModelNodeId parentNodeId, ModelNodeHelperRegistry
            modelNodeHelperRegistry,
                                 SubSystemRegistry subSystemRegistry, SchemaRegistry schemaRegistry) {
        this.m_parent = parent;
        this.m_parentNodeId = parentNodeId;
        this.m_modelNodeHelperRegistry = modelNodeHelperRegistry;
        m_subSystemRegistry = subSystemRegistry;
        m_interceptor = ModelNodeInterceptorChain.getInstance().createInterceptor();
        m_schemaRegistry = schemaRegistry;
    }

    public ModelNodeId getParentNodeId() {
        return m_parentNodeId;
    }

    @Override
    public ModelNode getParent() {
        return m_parent;
    }

    @Override
    public SchemaRegistry getSchemaRegistry() {
        return m_schemaRegistry;
    }

    public ModelNodeHelperRegistry getModelNodeHelperRegistry() {
        return m_modelNodeHelperRegistry;
    }

    @Override
    public String getContainerName() {
        return this.getModelNodeSchemaPath().getLastComponent().getLocalName();
    }

    public SchemaPath getSubSystemId() {
        return getModelNodeSchemaPath();
    }

    /**
     * By default there is no subsystem associated, so returning a dummy
     */
    @Override
    public SubSystem getSubSystem() {
        return getSubSystemRegistry().lookupSubsystem(getSubSystemId());
    }

    @Override
    public SubSystem getSubSystem(SchemaPath schemaPath) {
        return getSubSystemRegistry().lookupSubsystem(schemaPath);
    }

    protected void addXmlValue(Document document, Element parent, QName qname, ConfigLeafAttribute value, boolean
            isKeyAttribute) {
        if (!isKeyAttribute && value == null) {
            return;
        }
        String namespace = null;
        if (qname.getNamespace() != null && !qname.getNamespace().toString().isEmpty()) {
            namespace = qname.getNamespace().toString();
        }
        String localName = resolveLocalName(getSchemaRegistry(), namespace, qname.getLocalName());

        SchemaPath leafPath = m_schemaRegistry.getDescendantSchemaPath(getModelNodeSchemaPath(), qname);
        Class<?> attributeType = YangTypeToClassConverter.getClassTypeBySchemaPath(m_schemaRegistry, leafPath);
        if (InstanceIdentifierTypeDefinition.class.equals(attributeType) || QName.class.equals(attributeType)) {
            parent.appendChild(document.importNode(value.getDOMValue(), true));
        } else {
            Element element = document.createElementNS(namespace, localName);
            element.setTextContent(value.getStringValue());
            parent.appendChild(element);
        }
    }


    @Override
    public Element getConfig(GetConfigContext getConfigContext, NetconfQueryParams restCcontext) throws GetException {
        try {
            return createGetResponse(getConfigContext.getDoc(), getConfigContext.getFilter(), getModelNodeId(),
                    false, null, restCcontext);
        } catch (GetAttributeException e) {
            LOGGER.error(MODELNODEID_GET_ERROR + this, e);
            GetException exception = new GetException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId
                    (getModelNodeId(), NetconfRpcErrorTag.OPERATION_FAILED, MODELNODEID_GET_ERROR + this + " " + e.getMessage
                            (), m_schemaRegistry));
            exception.addSuppressed(e);
            throw exception;
        }
    }

    @Override
    public Element get(GetContext getContext, NetconfQueryParams params) throws GetException {
        try {
            return createGetResponse(getContext.getDoc(), getContext.getFilter(), getModelNodeId(), true,
                    getContext.getStateAttributeContext(), params);
        } catch (GetAttributeException e) {
            LOGGER.error(MODELNODEID_GET_ERROR + this, e);
            GetException exception = new GetException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId
                    (getModelNodeId(), NetconfRpcErrorTag.OPERATION_FAILED, MODELNODEID_GET_ERROR + this + " " + e.getMessage
                            (), m_schemaRegistry));
            exception.addSuppressed(e);
            throw exception;
        }

    }

    @Override
    public abstract QName getQName();


    protected Element createGetResponse(Document doc, FilterNode filter, ModelNodeId modelNodeId, boolean includeState,
                                        StateAttributeGetContext stateContext, NetconfQueryParams params) throws
            GetException, DOMException, GetAttributeException {
        // create the container, at this stage we are not sure yet whether that needs to go in
        String localName = null;
        boolean match = false;
        String containerName = getContainerName();

        String namespace = getQName().getNamespace().toString();

        localName = resolveLocalName(getSchemaRegistry(), namespace, containerName);

        Element parent = doc.createElementNS(namespace, localName);
        LOGGER.trace("createGetResponse: " + this + ", filter =\n" + filter);
        DataSchemaNode schemaNode = m_schemaRegistry.getDataSchemaNode(getModelNodeSchemaPath());
        if ((schemaNode != null) && (!schemaNode.isConfiguration()) && (!includeState)) {
            return null;
        }
        // now need to check the filter
        if (filter == null || filter.isEmpty()) {
            LOGGER.trace("no filter, copying everything to output");
            // this means everything ...
            try {
                copyCompletelyToOutput(doc, modelNodeId, parent, includeState, stateContext, params);
            } catch (DOMException | GetAttributeException e) {
                LOGGER.error("cannot create get response", e);
            }
            return parent;
        } else {
            //Has select node for this container, then copy complete
            FilterNode selectNode = filter.getSelectNode(getContainerName(), getQName().getNamespace().toString());
            if (selectNode != null) {
                LOGGER.trace("select node: {}, copying everything to output", selectNode.getNodeName());
                try {
                    copyCompletelyToOutput(doc, modelNodeId, parent, includeState, stateContext, params);
                } catch (DOMException | GetAttributeException e) {
                    LOGGER.debug("cannot create get response", e);
                }
            } else if (filter.hasContainmentNode(getContainerName())) {
                LOGGER.trace("containment node: {}, next level evaluation", getContainerName());
                // process deeper

                List<FilterNode> childFilters = filter.getContainmentNodes(getContainerName());

                for (FilterNode cn : childFilters) {
                    boolean thisMatch = copyFilteredToOutput(doc, parent, modelNodeId, cn, includeState,
                            stateContext, params);
                    match = match || thisMatch;
                }
            }

            if (parent.hasChildNodes() || (selectNode != null && parent.getLocalName().equals(selectNode.getNodeName
                    ())) || match) {
                return parent;
            }
            return null;
        }
    }

    private boolean isAboveDepth(NetconfQueryParams params) {
        return params.getDepth() == NetconfQueryParams.UNBOUNDED || params.getDepth() > m_modelNodeId.getDepth();
    }

    private boolean isAboveDepth(NetconfQueryParams params, ModelNode childNode) {
        if (isAboveDepth(params)) {
            return true;
        } else {
            ModelNodeId childNodeId = childNode.getModelNodeId();
            List<ModelNodeRdn> relativeRdns = childNodeId.getRdns().subList(params.getDepth(), childNodeId.getRdns()
                    .size());
            Map<String, List<QName>> fields = params.getFields();
            if (fields.isEmpty()) {
                return false;
            }
            for (ModelNodeRdn rdn : relativeRdns) {
                if (ModelNodeRdn.CONTAINER.equals(rdn.getRdnName()) && !fields.containsKey(rdn.getRdnValue())) {
                    return false;
                }
            }
            return true;
        }
    }

    private boolean isAboveDepth(NetconfQueryParams params, QName attributeName) {
        if (isAboveDepth(params)) {
            return true;
        } else {
            Map<String, List<QName>> fields = params.getFields();
            if (fields.containsKey(this.getContainerName())) {
                if (fields.get(this.getContainerName()).contains(attributeName)) {
                    return true;
                }
            }
            return false;
        }
    }


    private void filterStateAttributesAboveDepth(NetconfQueryParams params, Map<ModelNodeId, Pair<List<QName>,
            List<FilterNode>>> map) {

        for (Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : map.entrySet()) {
            List<QName> stateAttributes = entry.getValue().getFirst();
            Iterator<QName> iterator = stateAttributes.iterator();
            while (iterator.hasNext()) {
                QName attributeName = iterator.next();
                if (!isAboveDepth(params, attributeName)) {
                    iterator.remove();
                }
            }

        }
    }

    /**
     * This method evaluates get/get-config filters.
     * The algorithm is as follows
     * 1.	Evaluate match conditions at the current node
     * a.	If the match conditions evaluate to true
     * i.	Then evaluate state match conditions and state child filters (only in case of get)
     * 1.	If state conditions evaluate to true
     * a.	Evaluate child filters
     * if child filter evaluation was success, the start populating response
     *
     * @param doc
     * @param parent
     * @param modelNodeId
     * @param filter
     * @param includeState
     * @throws GetException
     * @throws GetAttributeException
     * @throws DOMException
     */
    private boolean copyFilteredToOutput(Document doc, Element parent, ModelNodeId modelNodeId, FilterNode filter,
                                         boolean includeState,
                                         StateAttributeGetContext stateContext, NetconfQueryParams params) throws
            GetException, DOMException,
            GetAttributeException {
        // we are in the right container, first verify the match nodes ....
        LOGGER.trace("copyFilteredToOutput " + this + ", filter=n" + filter);

        boolean proceedFurther = true;

        List<FilterMatchNode> stateFilterMatchNodes = new ArrayList<>();
        proceedFurther = evaluateMatchNodes(filter, includeState, proceedFurther, stateFilterMatchNodes);

        List<FilterNode> stateSubtreeFilterNodes = new ArrayList<>();
        List<Element> stateElements = new ArrayList<>();

        if (proceedFurther) {
            proceedFurther = evaluateStateConditions(modelNodeId, filter, includeState, stateContext,
                    stateFilterMatchNodes, stateSubtreeFilterNodes, stateElements);
            if (proceedFurther) {
                if ((filter.getSelectNodes().isEmpty()) && (filter.getChildNodes().isEmpty())) {
                    copyCompletelyToOutput(doc, modelNodeId, parent, includeState, stateContext, params);
                } else {
                    LOGGER.trace("copyFilteredToOutput: proceedFurther!");
                    // copy the Key Attribute
                    // all match nodes are to appear in the output
                    List<QName> stateAttributeQNames = new ArrayList<>();

                    boolean childHasMatchCondition = filter.childHasMatchCondition();
                    List<Element> childElements = new ArrayList<>();
                    evaluateChildNodeConditions(doc, filter, includeState, stateContext, params, childElements);

                    //There was child match condition and that match fails, then do not copy the data to response.
                    if (childHasMatchCondition && childElements.isEmpty() && stateElements.isEmpty()) {
                        return false;
                    }
                    populateResponse(doc, parent, modelNodeId, filter, includeState, stateContext, params,
                            stateSubtreeFilterNodes, stateAttributeQNames, childElements);

                }

            } else {
                LOGGER.trace("copyFilteredToOutput: NO match!");
            }
        }

        return proceedFurther;
    }

    private void populateResponse(Document doc, Element parent, ModelNodeId modelNodeId, FilterNode filter, boolean
            includeState, StateAttributeGetContext stateContext, NetconfQueryParams params, List<FilterNode>
            stateSubtreeFilterNodes,
                                  List<QName> stateAttributeQNames, List<Element> childElements) throws
            GetAttributeException, GetException {
        ArrayList<String> keyValues = new ArrayList<String>();
        //copy keys first refer RFC 6020 section 7.8.5
        copyKeyAttribute(doc, parent, modelNodeId, keyValues, params);

        //Copy the match conditions also to output
        for (FilterMatchNode fmn : filter.getMatchNodes()) {
            if (!(keyValues.contains(fmn.getNodeName()))) {
                QName qName = m_schemaRegistry.lookupQName(fmn.getNamespace(), fmn.getNodeName());
                if (params.isIncludeConfig() && qName != null) {
                    if (m_modelNodeHelperRegistry.getConfigAttributeHelpers(this.getModelNodeSchemaPath())
                            .containsKey(qName)) {
                        copyAttributeToOutput(doc, parent, qName, modelNodeId);
                    } else if (m_modelNodeHelperRegistry.getConfigLeafListHelper(this.getModelNodeSchemaPath(),
                            qName) != null) {
                        copyLeafListFilteredToOutput(doc, parent, qName, fmn.getFilter(), modelNodeId);
                    }
                }
            }
        }
        if (isAboveDepth(params, this)) {
            //copy child elements
            for (Element childElement : childElements) {
                parent.appendChild(childElement);
            }
        }

        // select need to be copied to the output for sure, they can be
        // direct attributes or containment nodes
        for (FilterNode fsn : filter.getSelectNodes()) {
            try {
                if (!(keyValues.contains(fsn.getNodeName()))) {
                    // a containment node can refer both to a child
                    // container or a list
                    QName qName = m_schemaRegistry.lookupQName(fsn.getNamespace(), fsn.getNodeName());
                    if (qName != null) {
                        if (m_modelNodeHelperRegistry.getChildListHelpers(this.getModelNodeSchemaPath()).containsKey
                                (qName)) {
                            Collection<ModelNode> children = getChildModelNodes(qName, fsn.getMatchNodes());
                            for (ModelNode child : children) {
                                Element elemChild = delegateToChild(doc, filter, includeState, child, stateContext,
                                        params);
                                if (elemChild != null) {
                                    parent.appendChild(elemChild);
                                }
                            }
                        } else if (m_modelNodeHelperRegistry.getChildContainerHelpers(this.getModelNodeSchemaPath())
                                .containsKey(qName)) {
                            ModelNode child = getChildModelNode(qName);
                            if (child != null) {
                                Element elemChild = delegateToChild(doc, filter, includeState, child, stateContext,
                                        params);
                                if (elemChild != null) {
                                    parent.appendChild(elemChild);
                                }
                            }
                        } else if (includeState && SchemaRegistryUtil.getStateChildLists(m_schemaRegistry,
                                getModelNodeSchemaPath()).contains(qName)) {
                            stateSubtreeFilterNodes.add(fsn);
                        } else if (includeState && SchemaRegistryUtil.getStateChildContainers(m_schemaRegistry,
                                getModelNodeSchemaPath()).contains(qName)) {
                            stateSubtreeFilterNodes.add(fsn);
                        } else if (params.isIncludeConfig() && m_modelNodeHelperRegistry.getConfigAttributeHelpers
                                (this.getModelNodeSchemaPath()).containsKey(qName)) {
                            copyAttributeToOutput(doc, parent, qName, modelNodeId);
                        } else if (includeState && getStateAttributes().contains(qName)) {
                            if (!isKeyAttribute(qName)) {
                                stateAttributeQNames.add(qName);
                            }
                        } else if (params.isIncludeConfig() && m_modelNodeHelperRegistry.getConfigLeafListHelpers
                                (this.getModelNodeSchemaPath()).containsKey(qName)) {
                            copyLeafListToOutput(doc, parent, qName, modelNodeId);
                        } else if (includeState && getStateLeafListAttributes().contains(qName)) {
                            stateAttributeQNames.add(qName);
                        } else {
                            LOGGER.debug("the node {} is not a config node", fsn.getNodeName());
                        }
                    }
                }
            } catch (ModelNodeGetException | GetAttributeException e) {
                LOGGER.error("could not retrieve children {}", fsn.getNodeName(), e);
            }
        }

        if (!stateAttributeQNames.isEmpty() || !stateSubtreeFilterNodes.isEmpty()) {
            Collection<DataSchemaNode> childrenSchemaNodes = m_schemaRegistry.getChildren(getModelNodeSchemaPath());
            for (DataSchemaNode schemaNode : childrenSchemaNodes) {
                addStateAttributesIfNotExist(stateContext, getSubSystem(schemaNode.getPath()), modelNodeId,
                        new ArrayList<>(stateAttributeQNames), stateSubtreeFilterNodes, params);
            }
            addStateAttributesIfNotExist(stateContext, getSubSystem(), modelNodeId, stateAttributeQNames,
                    stateSubtreeFilterNodes, params);
        }
    }

    private void evaluateChildNodeConditions(Document doc, FilterNode filter, boolean includeState,
                                             StateAttributeGetContext stateContext, NetconfQueryParams params,
                                             List<Element> childElements) throws GetException {
        // containment nodes need further evaluation
        for (FilterNode childFilter : filter.getChildNodes()) {
            try {
                // a containment node can refer both to a child
                // container or a list
                QName qName = m_schemaRegistry.lookupQName(childFilter.getNamespace(), childFilter.getNodeName());
                if (qName != null) {
                    if (m_modelNodeHelperRegistry.getChildListHelpers(this.getModelNodeSchemaPath()).containsKey
                            (qName)) {
                        Collection<ModelNode> children = getChildModelNodes(qName, childFilter.getMatchNodes());
                        for (ModelNode child : children) {
                            Element elemChild = delegateToChild(doc, filter, includeState, child, stateContext, params);
                            if (elemChild != null) {
                                childElements.add(elemChild);
                            }
                        }
                    } else if (m_modelNodeHelperRegistry.getChildContainerHelpers(this.getModelNodeSchemaPath())
                            .containsKey(qName)) {
                        ModelNode child = getChildModelNode(qName);
                        if (child != null) {
                            Element elemChild = delegateToChild(doc, filter, includeState, child,
                                    stateContext, params);
                            if (elemChild != null) {
                                childElements.add(elemChild);
                            }
                        }
                    } else {
                        LOGGER.debug("the node {} is not a config node", childFilter.getNodeName());
                    }
                }
            } catch (DOMException | ModelNodeGetException e) {
                LOGGER.error("could not retrieve children {}", childFilter.getNodeName(), e);
            }
        }
    }

    /**
     * we should evaluate state conditions if atleast one of the following is true
     * 1. if there is a state leaf match condition (that is stateFilterMatchNodes is not empty)
     * 2. if there is atleast one stateSubtreeFilterNode which has a child match condition
     *
     * @param modelNodeId
     * @param filter
     * @param includeState
     * @param stateContext
     * @param stateFilterMatchNodes
     * @param stateSubtreeFilterNodes
     * @param stateElements
     * @return
     * @throws GetAttributeException
     */
    private boolean evaluateStateConditions(ModelNodeId modelNodeId, FilterNode filter, boolean includeState,
                                            StateAttributeGetContext stateContext,
                                            List<FilterMatchNode> stateFilterMatchNodes, List<FilterNode>
                                                    stateSubtreeFilterNodes, List<Element> stateElements) throws
            GetAttributeException {
        boolean allChildFiltersAreState = true;
        for (FilterNode childFilter : filter.getChildNodes()) {
            QName qName = m_schemaRegistry.lookupQName(childFilter.getNamespace(), childFilter.getNodeName());
            if (includeState && SchemaRegistryUtil.getStateChildContainers(m_schemaRegistry, getModelNodeSchemaPath()
            ).contains(qName)) {
                stateSubtreeFilterNodes.add(childFilter);
            } else if (includeState && SchemaRegistryUtil.getStateChildLists(m_schemaRegistry, getModelNodeSchemaPath
                    ()).contains(qName)) {
                stateSubtreeFilterNodes.add(childFilter);
            } else {
                allChildFiltersAreState = false;
            }
        }

        boolean stateSubtreeFilterHasMatchCondition = false;
        for (FilterNode stateSubtreeFilterNode : stateSubtreeFilterNodes) {
            if (stateSubtreeFilterNode.hasMatchCondition()) {
                stateSubtreeFilterHasMatchCondition = true;
                break;
            }
        }
        // state leaf/leaf-list
        if ((!stateFilterMatchNodes.isEmpty() || (!stateSubtreeFilterNodes.isEmpty() &&
                stateSubtreeFilterHasMatchCondition))) {
            stateElements.addAll(FilterUtil.checkAndGetStateFilterElements(stateFilterMatchNodes,
                    stateSubtreeFilterNodes, modelNodeId, this.getModelNodeSchemaPath(), m_schemaRegistry,
                    m_subSystemRegistry));
            if (!stateElements.isEmpty()) {
                if ((filter.getSelectNodes().isEmpty()) && (filter.getChildNodes().isEmpty())) {
                    stateContext.getStateMatchNodes().put(modelNodeId, stateElements);
                } else {
                    // need to get filtered elements based on FilterMatchNodes (for leaf-list)
                    List<Element> filteredElements = getFilteredElements(stateFilterMatchNodes, stateElements);
                    stateContext.getStateMatchNodes().put(modelNodeId, filteredElements);
                }
            } else if (allChildFiltersAreState) {
                //state match conditions returned false and all child subtree filters are state.
                return false;
            }
        }
        return true;
    }

    private boolean evaluateMatchNodes(FilterNode filter, boolean includeState, boolean proceedFurther,
                                       List<FilterMatchNode> stateFilterMatchNodes) {
        if (!filter.getMatchNodes().isEmpty()) {
            for (FilterMatchNode fmn : filter.getMatchNodes()) {
                LOGGER.trace("evaluating " + fmn);
                try {
                    QName fmnQname = m_schemaRegistry.lookupQName(fmn.getNamespace(), fmn.getNodeName());
                    ChildLeafListHelper childLeafListhelper = m_modelNodeHelperRegistry.getConfigLeafListHelper(this
                            .getModelNodeSchemaPath(), fmnQname);
                    if (childLeafListhelper != null) {
                        Collection<ConfigLeafAttribute> leafListValues = childLeafListhelper.getValue(this);
                        if (!leafListMatchesFilter(leafListValues, fmn.getFilter())) {
                            proceedFurther = false;
                            // this tree needs to be ignored as is without
                            // further checking
                            break;
                        }
                    } else {
                        ConfigAttributeHelper configHelper = m_modelNodeHelperRegistry.getConfigAttributeHelper(this
                                .getModelNodeSchemaPath(), fmnQname);
                        if (configHelper != null) {
                            ConfigLeafAttribute attributeValue = configHelper.getValue(this);
                            if (attributeValue != null && !fmn.getFilter().equals(attributeValue.getStringValue())) {
                                proceedFurther = false;
                                // this tree needs to be ignored as is without further checking
                                break;
                            }
                        } else if (getStateLeafListAttributes().contains(fmnQname) || getStateAttributes().contains
                                (fmnQname)) {
                            if (!includeState) {
                                // state match condition in get-config is considered as false
                                proceedFurther = false;
                                break;
                            }
                            stateFilterMatchNodes.add(fmn);
                        } else {
                            LOGGER.error("Attribute {} not found", fmnQname);
                            proceedFurther = false;
                            break;
                        }
                    }

                } catch (GetAttributeException e) {
                    LOGGER.error("could not retrieve attribute value, condition is false", e);
                    proceedFurther = false;
                    break;
                }
            }
        }
        return proceedFurther;
    }

    private boolean leafListMatchesFilter(Collection<ConfigLeafAttribute> leafListValues, String filter) {
        Iterator<ConfigLeafAttribute> leafListIterator = leafListValues.iterator();
        while (leafListIterator.hasNext()) {
            ConfigLeafAttribute configLeafAttribute = leafListIterator.next();
            if (filter.equals(configLeafAttribute.getStringValue())) {
                return true;
            }
        }
        return false;
    }


    private Set<QName> getStateLeafListAttributes() {
        Set<QName> stateAttrs = new TreeSet<>();
        Collection<DataSchemaNode> children = m_schemaRegistry.getChildren(getModelNodeSchemaPath());
        for (DataSchemaNode child : children) {
            if (!child.isConfiguration() && (child instanceof LeafListSchemaNode)) {
                stateAttrs.add(child.getQName());
            }
        }
        return stateAttrs;
    }

    private boolean copyKeyAttribute(Document doc, Element parent, ModelNodeId modelNodeId, ArrayList<String>
            keyValues, NetconfQueryParams params) throws GetAttributeException {
        Map<QName, ConfigAttributeHelper> naturalKeyHelpers = m_modelNodeHelperRegistry.getNaturalKeyHelpers(this
                .getModelNodeSchemaPath());
        if (!naturalKeyHelpers.isEmpty()) {
            for (QName keyAttribute : naturalKeyHelpers.keySet()) {
                if (isAboveDepth(params, keyAttribute)) {
                    if (!keyValues.contains(keyAttribute)) {
                        addXmlValue(doc, parent, keyAttribute, getAttributeValue(keyAttribute, modelNodeId), true);
                        keyValues.add(keyAttribute.getLocalName());
                        LOGGER.debug("Key Attribute Copied : " + keyAttribute);
                    }
                }
            }

            if (naturalKeyHelpers.size() == keyValues.size()) {
                return true;
            }
        }
        return false;
    }

    private boolean isKeyAttribute(QName qname) {
        Map<QName, ConfigAttributeHelper> naturalKeyHelpers = m_modelNodeHelperRegistry.getNaturalKeyHelpers(this
                .getModelNodeSchemaPath());
        if (!naturalKeyHelpers.isEmpty()) {
            for (QName keyAttribute : naturalKeyHelpers.keySet()) {
                if (qname.equals(keyAttribute)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Element delegateToChild(Document doc, FilterNode filter, boolean includeState, ModelNode child,
                                    StateAttributeGetContext stateContext, NetconfQueryParams params)
            throws GetException {
        Element result;
        if (includeState) {
            result = child.get(new GetContext(doc, filter, stateContext), params);
        } else {
            result = child.getConfig(new GetConfigContext(doc, filter), params);
        }
        return result;
    }

    private ModelNode getChildModelNode(QName nodeName) throws ModelNodeGetException {
        return m_modelNodeHelperRegistry.getChildContainerHelpers(this.getModelNodeSchemaPath()).get(nodeName)
                .getValue(this);
    }

    private Collection<ModelNode> getChildModelNodes(QName nodeName, List<FilterMatchNode> matchNodes) throws
            ModelNodeGetException {
        Map<QName, ConfigLeafAttribute> matchCriteria = new HashMap<>();
        for (FilterMatchNode matchNode : matchNodes) {
            QName qName = m_schemaRegistry.lookupQName(matchNode.getNamespace(), matchNode.getNodeName());
            matchCriteria.put(qName, new GenericConfigAttribute(matchNode.getFilter()));
        }
        return m_modelNodeHelperRegistry.getChildListHelpers(this.getModelNodeSchemaPath()).get(nodeName).getValue
                (this, matchCriteria);
    }

    @SuppressWarnings("unchecked")
    private Collection<ConfigLeafAttribute> getChildLeafListValues(QName nodeName, ModelNodeId modelNodeId) throws
            GetAttributeException {
        ChildLeafListHelper configLeafListHelper = m_modelNodeHelperRegistry.getConfigLeafListHelper(this
                .getModelNodeSchemaPath(), nodeName);
        if (configLeafListHelper != null) {
            return configLeafListHelper.getValue(this);
        }
        if (getStateLeafListAttributes().contains(nodeName)) {
            Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateAttrs = new HashMap<>();
            List<QName> nodes = new ArrayList<>();
            nodes.add(nodeName);
            stateAttrs.put(modelNodeId, new Pair<>(nodes, Collections.EMPTY_LIST));
            Map<ModelNodeId, List<Element>> result = getSubSystem().retrieveStateAttributes(stateAttrs,
                    NetconfQueryParams.NO_PARAMS);
            List<Element> stateAttrPair = result.get(modelNodeId);
            Collection<ConfigLeafAttribute> data = new ArrayList<>();
            for (Element e : stateAttrPair) {
                try {
                    data.add(ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, this.getModelNodeSchemaPath
                            (), nodeName, e));
                } catch (InvalidIdentityRefException e1) {
                    throw new GetAttributeException(e1.getRpcError());
                }
            }
            return data;
        }
        LOGGER.error("Attribute " + nodeName + " not found");
        throw new GetAttributeException("LeafList " + nodeName + " not found");

    }

    private ConfigLeafAttribute getAttributeValue(QName nodeName, ModelNodeId modelNodeId) throws
            GetAttributeException {
        ConfigAttributeHelper configHelper = m_modelNodeHelperRegistry.getConfigAttributeHelper(this
                .getModelNodeSchemaPath(), nodeName);
        if (configHelper != null) {
            return configHelper.getValue(this);
        }
        if (getStateAttributes().contains(nodeName)) {
            List<QName> nodes = new ArrayList<>();
            nodes.add(nodeName);
            Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> stateAttrs = new HashMap<>();
            stateAttrs.put(modelNodeId, new Pair<List<QName>, List<FilterNode>>(nodes, Collections
                    .<FilterNode>emptyList()));
            Map<ModelNodeId, List<Element>> result = getSubSystem().retrieveStateAttributes(stateAttrs,
                    NetconfQueryParams.NO_PARAMS);
            List<Element> stateAttrPair = result.get(modelNodeId);
            if (stateAttrPair != null && !stateAttrPair.isEmpty()) {
                String stateAttribute = stateAttrPair.get(0).getTextContent();
                if (stateAttribute != null) {
                    try {
                        return ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, this
                                .getModelNodeSchemaPath(), nodeName, stateAttrPair.get
                                (0));
                    } catch (InvalidIdentityRefException e) {
                        throw new GetAttributeException(e.getRpcError());
                    }
                }
            } else {
                LOGGER.error("Couldn't fetch state attributes from SubSystem");
            }
            return null;
        }
        LOGGER.error("Attribute " + nodeName + " not found");
        throw new GetAttributeException("Attribute " + nodeName + " not found");
    }

    private void copyAttributeToOutput(Document doc, Element parent, QName nodeName, ModelNodeId modelNodeId) {
        try {
            if (!isKeyAttribute(nodeName)) {
                addXmlValue(doc, parent, nodeName, getAttributeValue(nodeName, modelNodeId), false);
            }
        } catch (GetAttributeException e) {
            LOGGER.error("Attribute " + nodeName.getLocalName() + " value could not be retrieved", e);
        }
    }

    private void copyLeafListFilteredToOutput(Document doc, Element parent, QName nodeName, String filterValue,
                                              ModelNodeId modelNodeId) {
        try {
            Collection<ConfigLeafAttribute> leafListValues = getChildLeafListValues(nodeName, modelNodeId);
            for (ConfigLeafAttribute value : leafListValues) {
                if (value != null && value.getStringValue().equals(filterValue)) {
                    addXmlValue(doc, parent, nodeName, value, false);
                }
            }
        } catch (GetAttributeException e) {
            LOGGER.error("LeafList " + nodeName + " value could not be retrieved", e);
        }
    }

    private void copyLeafListToOutput(Document doc, Element parent, QName nodeName, ModelNodeId modelNodeId) throws
            GetAttributeException {
        Collection<ConfigLeafAttribute> leafListValues = getChildLeafListValues(nodeName, modelNodeId);
        for (ConfigLeafAttribute value : leafListValues) {
            addXmlValue(doc, parent, nodeName, value, false);
        }
    }


    private void copyCompletelyToOutput(Document doc, ModelNodeId modelNodeId, Element parent, boolean includeState,
                                        StateAttributeGetContext stateAttributeContext, NetconfQueryParams params)
            throws DOMException,
            GetException, GetAttributeException {
        // copy keys first
        ArrayList<String> keyValues = new ArrayList<>();
        copyKeyAttribute(doc, parent, modelNodeId, keyValues, params);
        if (includeState) {
            storeAllStateAttributes(modelNodeId, stateAttributeContext, params);
        }
        if (params.isIncludeConfig()) {
            // get config values
            copyAllConfigAttributesToOutput(doc, parent, keyValues, params);
            // get config leaf list values
            copyAllConfigLeafListToOutput(doc, parent, keyValues, params);
        }
        // add the child containers lists
        copyAllChildListToOutput(doc, parent, includeState, stateAttributeContext, params);
        // add the child containers
        copyAllContainerToOutput(doc, parent, includeState, stateAttributeContext, params);
    }


    private void storeAllStateAttributes(ModelNodeId modelNodeId, StateAttributeGetContext stateContext,
                                         NetconfQueryParams params) {
        Set<QName> stateAttributes = getStateAttributes();
        if (!stateAttributes.isEmpty()) {
            List<Element> elements = stateContext.getStateMatchNodes().get(modelNodeId);
            if (elements != null) {
                for (Element e : elements) {
                    QName fmnQname = m_schemaRegistry.lookupQName(e.getNamespaceURI(), e.getLocalName());
                    stateAttributes.remove(fmnQname);
                }
            }
        }
        List<FilterNode> filterList = new ArrayList<FilterNode>();
        if (isAboveDepth(params)) {
            addContainerAndListStateNodes(filterList);
        }

        if (!stateAttributes.isEmpty() || !filterList.isEmpty()) {
            Collection<DataSchemaNode> childrenSchemaNodes = m_schemaRegistry.getChildren(getModelNodeSchemaPath());
            for (DataSchemaNode schemaNode : childrenSchemaNodes) {
                addStateAttributesIfNotExist(stateContext, getSubSystem(schemaNode.getPath()), modelNodeId,
                        new ArrayList<>(stateAttributes), filterList, params);
            }

            SubSystem subSystem = getSubSystem(getModelNodeSchemaPath());
            if (subSystem instanceof NoopSubSystem) {
                subSystem = getSubSystem();
            }
            addStateAttributesIfNotExist(stateContext, subSystem, modelNodeId, new ArrayList<>(stateAttributes),
                    filterList, params);
        }
    }

    private void addContainerAndListStateNodes(List<FilterNode> filterList) {
        DataSchemaNode schemaNode = m_schemaRegistry.getDataSchemaNode(getModelNodeSchemaPath());
        if (schemaNode instanceof DataNodeContainer) {
            DataNodeContainer dataNodeContainer = (DataNodeContainer) schemaNode;
            for (DataSchemaNode child : dataNodeContainer.getChildNodes()) {
                if (!child.isConfiguration() && (child instanceof ListSchemaNode || child instanceof
                        ContainerSchemaNode)) {
                    FilterNode filterNode = new FilterNode(child.getQName());
                    filterList.add(filterNode);
                }
                if (child instanceof ChoiceSchemaNode) {
                    Set<ChoiceCaseNode> cases = ((ChoiceSchemaNode) child).getCases();
                    List<DataSchemaNode> schemaNodes = ChoiceCaseNodeUtil.getAllNodesFromCases(cases);
                    for (DataSchemaNode node : schemaNodes) {
                        if (!node.isConfiguration() && (node instanceof ListSchemaNode || node instanceof
                                ContainerSchemaNode)) {
                            FilterNode filterNode = new FilterNode(node.getQName());
                            filterList.add(filterNode);
                        }
                    }
                }
            }
        }
    }

    private void addStateAttributesIfNotExist(StateAttributeGetContext stateContext, SubSystem system,
                                              ModelNodeId modelNodeId, List<QName> stateAttributes, List<FilterNode>
                                                      stateSubtrees, NetconfQueryParams params) {
        for (Entry<SubSystem, Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>>> entry : stateContext
                .getSubSystems().entrySet()) {
            SubSystem subSystem = entry.getKey();
            if (subSystem.getClass().getName().equals(system.getClass().getName())) {
                Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> map = entry.getValue();
                if (!map.containsKey(modelNodeId)) {
                    Pair<List<QName>, List<FilterNode>> pair = new Pair<List<QName>, List<FilterNode>>
                            (stateAttributes, stateSubtrees);
                    map.put(modelNodeId, pair);
                }
                filterStateAttributesAboveDepth(params, map);
                return;
            }
        }

        Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> map = new HashMap<>();
        map.put(modelNodeId, new Pair<List<QName>, List<FilterNode>>(stateAttributes, stateSubtrees));
        filterStateAttributesAboveDepth(params, map);
        stateContext.updateSubSystems(system, map);
    }

    private Set<QName> getStateAttributes() {
        Set<QName> stateAttrs = new TreeSet<>();
        Collection<DataSchemaNode> children = m_schemaRegistry.getChildren(getModelNodeSchemaPath());
        for (DataSchemaNode child : children) {
            if (!child.isConfiguration() && !(child instanceof DataNodeContainer)) {
                stateAttrs.add(child.getQName());
            }
        }
        return stateAttrs;
    }

    /**
     * @param doc
     * @param parent
     */
    private void copyAllConfigAttributesToOutput(Document doc, Element parent, ArrayList<String> keyValues,
                                                 NetconfQueryParams params) {
        for (Map.Entry<QName, ConfigAttributeHelper> helperEntry : m_modelNodeHelperRegistry
                .getConfigAttributeHelpers(this.getModelNodeSchemaPath()).entrySet()) {
            QName configAttribute = helperEntry.getKey();
            if (isAboveDepth(params, configAttribute)) {
                if (keyValues.contains(configAttribute.getLocalName())) {
                    continue;
                }
                ConfigAttributeHelper configAttributeHelper = helperEntry.getValue();
                try {
                    if (configAttributeHelper.isMandatory()) {
                        addXmlValue(doc, parent, configAttribute, configAttributeHelper.getValue(this), false);
                    } else if (configAttributeHelper.isChildSet(this)) {
                        addXmlValue(doc, parent, configAttribute, configAttributeHelper.getValue(this), false);
                    }
                } catch (GetAttributeException e) {
                    LOGGER.error("failed to add attribute to output " + configAttribute, e);
                }
            }
        }
    }

    /**
     * @param doc
     * @param parent
     */
    private void copyAllConfigLeafListToOutput(Document doc, Element parent, ArrayList<String> keyValues,
                                               NetconfQueryParams params) {
        for (Map.Entry<QName, ChildLeafListHelper> helperEntry : m_modelNodeHelperRegistry.getConfigLeafListHelpers(this
                .getModelNodeSchemaPath()).entrySet()) {
            try {
                ChildLeafListHelper leafListHelper = helperEntry.getValue();
                if (leafListHelper.isConfiguration()) {
                    QName leafListName = helperEntry.getKey();
                    if (isAboveDepth(params, leafListName)) {
                        if (keyValues.contains(leafListName.getLocalName())) {
                            continue;
                        }
                        Collection<ConfigLeafAttribute> result = leafListHelper.getValue(this);
                        for (ConfigLeafAttribute value : result) {
                            addXmlValue(doc, parent, leafListName, value, false);
                        }
                    }
                }
            } catch (IllegalArgumentException | GetAttributeException e) {
                LOGGER.error("failed to add container list to output " + helperEntry, e);
            }
        }
    }

    /**
     * @param doc
     * @param parent
     * @param includeState
     * @throws GetException
     */
    private void copyAllChildListToOutput(Document doc, Element parent, boolean includeState,
                                          StateAttributeGetContext stateContext, NetconfQueryParams params) throws GetException {
        for (ChildListHelper helper : m_modelNodeHelperRegistry.getChildListHelpers(this.getModelNodeSchemaPath())
                .values()) {
            try {
                Collection<ModelNode> result = helper.getValue(this, Collections.<QName, ConfigLeafAttribute>emptyMap
                        ());
                for (ModelNode child : result) {
                    if (isAboveDepth(params, child)) {
                        if (includeState) {
                            parent.appendChild(child.get(new GetContext(doc, null, stateContext), params));
                        } else if (params.isIncludeConfig()) {
                            parent.appendChild(child.getConfig(new GetConfigContext(doc, null), params));
                        }
                    }
                }

            } catch (IllegalArgumentException | ModelNodeGetException e) {
                LOGGER.error("failed to add container list to output " + helper, e);
            }
        }
    }

    /**
     * @param doc
     * @param parent
     * @param includeState
     * @throws GetException
     */
    private void copyAllContainerToOutput(Document doc, Element parent, boolean includeState,
                                          StateAttributeGetContext stateContext, NetconfQueryParams params) throws
            GetException {
        for (ChildContainerHelper helper : m_modelNodeHelperRegistry.getChildContainerHelpers(this
                .getModelNodeSchemaPath()).values()) {
            try {
                if (helper.isMandatory() || helper.isChildSet(this)) {
                    ModelNode result = helper.getValue(this);
                    if (result != null) {
                        if (isAboveDepth(params, result)) {
                            if (includeState) {
                                parent.appendChild(result.get(new GetContext(doc, null, stateContext), params));
                            } else if (params.isIncludeConfig()) {
                                parent.appendChild(result.getConfig(new GetConfigContext(doc, null), params));
                            }
                        }
                    }
                }

            } catch (ModelNodeGetException e) {
                LOGGER.error("failed to add container to output " + helper, e);
            }
        }
    }

    @Override
    public void prepareEditSubTree(EditContainmentNode root, Element configElementContent) throws EditConfigException {
        new EditTreeBuilder().prepareEditSubTree(root, configElementContent, this.getModelNodeSchemaPath(),
                m_schemaRegistry,
                m_modelNodeHelperRegistry, this.getModelNodeId());
        EditContainmentNode.setParentForEditContainmentNode(root, null);
    }


    @Override
    public void editConfig(EditContext editContext) throws EditConfigException {
        m_interceptor.interceptEditConfig(this, editContext);
    }

    public void editNode(EditContext editContext) throws EditConfigException {
        try {
            //Prepare commands using just the  editTree for now (and test,error option going forward)
            CompositeEditCommand command = getEditCommand(editContext);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Executing command : " + command);
            }
            command.execute();
            if (command.isError()) {
                NetconfRpcError error = NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),
                        NetconfRpcErrorTag.OPERATION_FAILED,
                        "Following errors occured while executing edit config : " + command.getErrorString(),
                        m_schemaRegistry);
                throw new EditConfigException(error);
            }
        } catch (CommandExecutionException e) {
            if (e.getCause() instanceof EditConfigException) {
                EditConfigException innerException = (EditConfigException) e.getCause();
                throw innerException;
            }
            LOGGER.debug("Error while executing edit config : ", e);
            EditConfigException exception = null;
            if (e.getRpcError() != null) {
                NetconfRpcError error = e.getRpcError();
                if (getModelNodeId() != null) {
                    error.setErrorPath(getModelNodeId().xPathString(m_schemaRegistry), getModelNodeId()
                            .xPathStringNsByPrefix(m_schemaRegistry));
                }
                exception = new EditConfigException(error);
            } else {
                exception = new EditConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId
                        (getModelNodeId(),
                        NetconfRpcErrorTag.OPERATION_FAILED, e.getMessage(), m_schemaRegistry));
            }
            exception.addSuppressed(e);
            throw exception;
        }

    }

    public CompositeEditCommand getEditCommand(EditContext editContext) throws EditConfigException {
        CompositeEditCommand compositeEdit = new CompositeEditCommand().setErrorOption(editContext.getErrorOption());
        EditContainmentNode editNode = editContext.getEditNode();
        if (!EditConfigOperations.CREATE.equals(editNode.getEditOperation())) {
            //check if it is the right instance
            boolean thisInstance = isThisInstance(editNode);
            if (!thisInstance) {
                return null;
            }
        }

        if (null == this.getParent() && editNode.getEditOperation().equals(EditConfigOperations.REPLACE) &&
                isThisInstance(editNode)) {
            DeleteChildrenUnderRootCommand cmd = new DeleteChildrenUnderRootCommand(m_modelNodeHelperRegistry, this
                    .getModelNodeSchemaPath(), this);
            decorateAndAppend(editContext, ModelNodeChangeType.delete, compositeEdit, cmd);
        }

        createDefaultCaseNodes(editContext);
        addCommandsForCurrentNodeChanges(editContext, compositeEdit);
        addCommandsForChildNodeChanges(editContext, compositeEdit);
        return compositeEdit;
    }

    /**
     * Updates the current Edit Tree to instantiate default case nodes when the other case node is being deleted.
     *
     * @param editContext
     */
    private void createDefaultCaseNodes(EditContext editContext) {
        DefaultCapabilityCommandInterceptor interceptor = m_modelNodeHelperRegistry
                .getDefaultCapabilityCommandInterceptor();
        List<ChoiceCaseNode> defaultCaseNodes = new ArrayList<>();
        EditContainmentNode editNode = editContext.getEditNode();
        EditContainmentNode newParentEditNode = editContext.getEditNode();
        SchemaPath modelNodeSchemaPath = this.getModelNodeSchemaPath();
        for (EditContainmentNode childEditNode : editNode.getChildren()) {
            if (EditConfigOperations.REMOVE.equals(childEditNode.getEditOperation()) ||
                    EditConfigOperations.DELETE.equals(childEditNode.getEditOperation())) {
                ChildContainerHelper childContainerHelper = m_modelNodeHelperRegistry.getChildContainerHelper
                        (modelNodeSchemaPath, childEditNode.getQName());
                if (childContainerHelper != null) {
                    // handle choice case node
                    getDefaultCaseNodes(defaultCaseNodes, childContainerHelper.getChildModelNodeSchemaPath());
                } else {
                    ChildListHelper childListHelper = m_modelNodeHelperRegistry.getChildListHelper(modelNodeSchemaPath,
                            childEditNode.getQName());
                    ChoiceSchemaNode choiceParentSchemaNode = SchemaRegistryUtil.getChoiceParentSchemaNode
                            (childListHelper.getChildModelNodeSchemaPath(), m_schemaRegistry);
                    if (choiceParentSchemaNode != null) {
                        String defaultCaseName = choiceParentSchemaNode.getDefaultCase();
                        if (defaultCaseName != null) {
                            try {
                                Map<QName, ConfigLeafAttribute> matchCriteria = ModelNodeConstraintProcessor
                                        .getMatchCriteria(childEditNode);
                                Collection<ModelNode> children = childListHelper.getValue(this, matchCriteria);
                                //if there are no more list entries, default needs to be created.
                                if (children.size() == 1 && childListHelper.getValue(this, Collections.emptyMap())
                                        .size() == 1) {
                                    defaultCaseNodes.add(choiceParentSchemaNode.getCaseNodeByName(defaultCaseName));
                                }
                            } catch (ModelNodeGetException e) {
                                //we should not be getting this exception, so throwing as RT exception
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        }
        for (EditChangeNode editChangeNode : editNode.getChangeNodes()) {
            if (EditConfigOperations.REMOVE.equals(editChangeNode.getOperation()) ||
                    EditConfigOperations.DELETE.equals(editChangeNode.getOperation())) {
                QName childQName = editChangeNode.getQName();
                ChildLeafListHelper childLeafListHelper = m_modelNodeHelperRegistry.getConfigLeafListHelper
                        (modelNodeSchemaPath, childQName);
                if (childLeafListHelper != null) {
                    // if there are no more leaf-list entries, default needs to be created.
                    if (areAllLeafListValuesBeingDeleted(editNode, childLeafListHelper)) {
                        SchemaPath childSchemaPath = m_schemaRegistry.getDescendantSchemaPath(modelNodeSchemaPath,
                                childQName);
                        getDefaultCaseNodes(defaultCaseNodes, childSchemaPath);
                    }
                } else {
                    ConfigAttributeHelper configAttributeHelper = m_modelNodeHelperRegistry.getConfigAttributeHelper
                            (modelNodeSchemaPath, childQName);
                    if (configAttributeHelper != null) {
                        SchemaPath childSchemaPath = m_schemaRegistry.getDescendantSchemaPath(modelNodeSchemaPath,
                                childQName);
                        getDefaultCaseNodes(defaultCaseNodes, childSchemaPath);
                    }
                }
            }
        }
        //check if there are default cases to be created,iterate through that new list and call interceptor.
        for (ChoiceCaseNode choiceCaseNode : defaultCaseNodes) {
            interceptor.populateChoiceCase(newParentEditNode, choiceCaseNode, this, true);
        }

    }

    private void getDefaultCaseNodes(List<ChoiceCaseNode> defaultCaseNodes, SchemaPath schemaPath) {
        ChoiceSchemaNode choiceParentSchemaNode = SchemaRegistryUtil.getChoiceParentSchemaNode(schemaPath,
                m_schemaRegistry);
        if (choiceParentSchemaNode != null) {
            String defaultCaseName = choiceParentSchemaNode.getDefaultCase();
            if (defaultCaseName != null) {
                defaultCaseNodes.add(choiceParentSchemaNode.getCaseNodeByName(defaultCaseName));
            }
        }
    }

    private boolean areAllLeafListValuesBeingDeleted(EditContainmentNode editNode, ChildLeafListHelper
            childLeafListHelper) {
        Collection<ConfigLeafAttribute> leafListValues;
        try {
            leafListValues = childLeafListHelper.getValue(this);
        } catch (GetAttributeException e) {
            //we should not be getting this exception, so throwing as RT exception
            throw new RuntimeException(e);
        }
        if (leafListValues != null && leafListValues.size() == 1) {
            // Last leaf-list being deleted.
            return true;
        } else {
            List<EditChangeNode> editChangeNodes = editNode.getChangeNodes();
            for (ConfigLeafAttribute value : leafListValues) {
                boolean found = false;
                for (EditChangeNode editChangeNode : editChangeNodes) {
                    if (value.equals(editChangeNode.getConfigLeafAttribute())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addCommandsForChildNodeChanges(EditContext parentEditContext, CompositeEditCommand
            parentCompositeEdit) throws EditConfigException {
        EditContainmentNode parentEditNode = parentEditContext.getEditNode();
        for (EditContainmentNode childEditNode : parentEditNode.getChildren()) {
            EditContext childEditContext = new EditContext(childEditNode, parentEditContext.getNotificationContext(),
                    parentEditContext.getErrorOption(), parentEditContext.getClientInfo());
            childEditContext.setParentContext(parentEditContext);
            if (EditConfigOperations.CREATE.equals(childEditNode.getEditOperation())) {
                addCreateCommands(childEditContext, parentCompositeEdit);
            } else if (EditConfigOperations.MERGE.equals(childEditNode.getEditOperation())) {
                addMergeCommands(childEditContext, parentCompositeEdit);
            } else if (EditConfigOperations.DELETE.equals(childEditNode.getEditOperation())) {
                addDeleteCommands(childEditContext, parentCompositeEdit);
            } else if (EditConfigOperations.REMOVE.equals(childEditNode.getEditOperation())) {
                addRemoveCommands(childEditContext, parentCompositeEdit);
            } else if (EditConfigOperations.REPLACE.equals(childEditNode.getEditOperation())) {
                addReplaceCommands(childEditContext, parentCompositeEdit);
            } else {
                addNoneCommands(childEditContext, parentCompositeEdit);
            }
        }

    }

    private void addReplaceCommands(EditContext editContext, CompositeEditCommand parentCompositeEdit) throws
            EditConfigException {
        EditContainmentNode childEditNode = editContext.getEditNode();
        ChildContainerHelper childContainerHelper = m_modelNodeHelperRegistry.getChildContainerHelper(this
                .getModelNodeSchemaPath(), childEditNode.getQName());
        if (childContainerHelper != null) {
            ReplaceChildCommand replaceCommand = new ReplaceChildCommand(editContext, m_modelNodeHelperRegistry
                    .getDefaultCapabilityCommandInterceptor()).addReplaceInfo(childContainerHelper, this);
            decorateAndAppend(editContext, ModelNodeChangeType.replace, parentCompositeEdit, replaceCommand);
        } else {
            ChildListHelper childListHelper = m_modelNodeHelperRegistry.getChildListHelper(this
                    .getModelNodeSchemaPath(), childEditNode.getQName());
            try {
                ModelNode child = ModelNodeConstraintProcessor.getChildNode(childListHelper,
                        this, childEditNode);
                ReplaceChildInListCommand addCommand = new ReplaceChildInListCommand(editContext,
                        m_modelNodeHelperRegistry.getDefaultCapabilityCommandInterceptor())
                        .addReplaceInfo(childListHelper, this, child);
                decorateAndAppend(editContext, ModelNodeChangeType.replace, parentCompositeEdit, addCommand);
            } catch (EditConfigException e) {
                // create if the node not found
                String errorMessage = e.getRpcError().getErrorMessage();
                if (errorMessage.contains(ModelNodeConstraintProcessor.DATA_DOES_NOT_EXIST) || errorMessage.contains
                        (ModelNodeConstraintProcessor.MODEL_NODE_NOT_FOUND)) {
                    ReplaceChildInListCommand addCommand = new ReplaceChildInListCommand(editContext,
                            m_modelNodeHelperRegistry.getDefaultCapabilityCommandInterceptor())
                            .addReplaceInfo(childListHelper, this, null);
                    decorateAndAppend(editContext, ModelNodeChangeType.replace, parentCompositeEdit, addCommand);
                } else {
                    throw e;
                }
            }
        }
    }

    private void addRemoveCommands(EditContext editContext, CompositeEditCommand parentCompositeEdit) throws
            EditConfigException {
        EditContainmentNode childEditNode = editContext.getEditNode();
        ChildContainerHelper childContainerHelper = m_modelNodeHelperRegistry.getChildContainerHelper(this
                .getModelNodeSchemaPath(), childEditNode.getQName());

        if (childContainerHelper != null) {
            ModelNode child = null;
            try {
                child = childContainerHelper.getValue(this);
            } catch (ModelNodeGetException e) {
                //ignore as this is remove command
            }
            DeleteChildCommand deleteCommand = new DeleteChildCommand().addDeleteInfo(childContainerHelper, this, child
            );
            decorateAndAppend(editContext, ModelNodeChangeType.remove, parentCompositeEdit, deleteCommand);
        } else {
            ChildListHelper childListHelper = m_modelNodeHelperRegistry.getChildListHelper(this
                    .getModelNodeSchemaPath(), childEditNode.getQName());
            try {
                ModelNode child = ModelNodeConstraintProcessor
                        .getChildNode(childListHelper, this, childEditNode);
                Command removeCommand = new RemoveChildFromListCommand().addRemoveInfo(childListHelper, this, child
                );
                decorateAndAppend(editContext, ModelNodeChangeType.remove, parentCompositeEdit, removeCommand);
            } catch (EditConfigException e) {
                String errorMessage = e.getRpcError().getErrorMessage();
                if (errorMessage.contains(ModelNodeConstraintProcessor.DATA_DOES_NOT_EXIST) || errorMessage.contains
                        (ModelNodeConstraintProcessor.MODEL_NODE_NOT_FOUND)) {
                    // ignore, "remove" unlike "delete" should ignore if the item is not present
                } else {
                    throw e;
                }
            }
        }
    }

    protected void testMerge(EditContext editContext, CompositeEditCommand parentCompositeEdit) throws
            EditConfigException {
        addMergeCommands(editContext, parentCompositeEdit);
    }

    private void addMergeCommands(EditContext editContext, CompositeEditCommand parentCompositeEdit) throws
            EditConfigException {
        EditContainmentNode childEditNode = editContext.getEditNode();

        ChildContainerHelper childContainerHelper = m_modelNodeHelperRegistry.getChildContainerHelper(this
                .getModelNodeSchemaPath(), childEditNode.getQName());
        if (childContainerHelper != null) {
            try {
                ModelNode value = childContainerHelper.getValue(this);
                if (value == null) {
                    // empty child container case. create child container
                    addCreateCommands(editContext, parentCompositeEdit);
                } else {
                    Command childCommand = new DelegateToChildCommand().addInfo(editContext, value);
                    if (childCommand == null) {
                        NetconfRpcError error = NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId()
                                , NetconfRpcErrorTag.DATA_MISSING,
                                "Cannot find the node where the change is to be made :" + childEditNode,
                                m_schemaRegistry);
                        throw new EditConfigException(error);
                    }
                    parentCompositeEdit.appendCommand(childCommand);
                }
            } catch (ModelNodeGetException e) {
                LOGGER.error("Could not get child containers ", e);
                EditConfigException exception = new EditConfigException(NetconfRpcErrorUtil
                        .getNetconfRpcErrorForModelNodeId(getModelNodeId(), NetconfRpcErrorTag.DATA_MISSING,
                        "Could not get child containers " + e.getMessage(), m_schemaRegistry));
                exception.addSuppressed(e);
                throw exception;
            }
        } else {
            ChildListHelper childListHelper = m_modelNodeHelperRegistry.getChildListHelper(this
                    .getModelNodeSchemaPath(), childEditNode.getQName());
            try {
                ModelNode child = ModelNodeConstraintProcessor
                        .getChildNode(childListHelper, this, childEditNode);
                MergeChildInListCommand mergeCommand = new MergeChildInListCommand();
                child.getSubSystem().testChange(editContext, ModelNodeChangeType.merge, child,
                        m_modelNodeHelperRegistry);
                mergeCommand.addAddInfo(childListHelper, this, editContext, child);
                parentCompositeEdit.appendCommand(mergeCommand);

            } catch (EditConfigException e) {
                String errorMessage = e.getRpcError().getErrorMessage();
                if (errorMessage.contains(ModelNodeConstraintProcessor.DATA_DOES_NOT_EXIST) || errorMessage.contains
                        (ModelNodeConstraintProcessor.MODEL_NODE_NOT_FOUND)) {
                    addCreateCommands(editContext, parentCompositeEdit);
                } else {
                    throw e;
                }
            } catch (SubSystemValidationException exception) {
                NetconfRpcError error = new NetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED, NetconfRpcErrorType
                        .Application, NetconfRpcErrorSeverity.Error, exception.getMessage());
                throw new EditConfigException(error);
            }
        }
    }

    private void addDeleteAttributeCommandForChangedNodes(EditContext editContext, CompositeEditCommand
            compositeEditCommand,
                                                          ConfigAttributeHelper helper, EditChangeNode changeNode)
            throws EditConfigException {
        DeleteLeafCommand cmd = new DeleteLeafCommand().addDeleteInfo(getSchemaRegistry(), editContext, helper, this,
                changeNode.getQName(), true);
        decorateAndAppend(editContext, ModelNodeChangeType.merge, compositeEditCommand, cmd);
    }

    private void addDeleteCommandsForContainerChoiceCaseNode(ChildContainerHelper childContainerHelper, EditContext
            editContext, CompositeEditCommand parentCompositeEdit) throws EditConfigException {
        ModelNode value = null;
        try {
            value = childContainerHelper.getValue(this);
            if (value == null) {
                return;
            }
        } catch (ModelNodeGetException e) {
            LOGGER.error("Could not get child containers ", e);
            EditConfigException exception = new EditConfigException(NetconfRpcErrorUtil
                    .getNetconfRpcErrorForModelNodeId(getModelNodeId(), NetconfRpcErrorTag.DATA_MISSING, "Could not get child" +
                            " containers " + e.getMessage(), m_schemaRegistry));
            exception.addSuppressed(e);
            exception.addSuppressed(e);
            throw exception;
        }
        DeleteChildCommand deleteCommand = new DeleteChildCommand().addDeleteInfo(childContainerHelper, this, value
        );
        decorateAndAppend(editContext, ModelNodeChangeType.delete, parentCompositeEdit, deleteCommand);
    }

    private void addDeleteCommandsForListChoiceCaseNode(ChildListHelper childListHelper, EditContext editContext,
                                                        CompositeEditCommand parentCompositeEdit) throws EditConfigException {
        Collection<ModelNode> children;
        try {
            children = childListHelper.getValue(this, Collections.<QName, ConfigLeafAttribute>emptyMap());
            if (children.isEmpty()) {
                return;
            } else {
                for (ModelNode child : children) {
                    Command removeCommand = new RemoveChildFromListCommand().addRemoveInfo(childListHelper, this, child
                    );
                    decorateAndAppend(editContext, ModelNodeChangeType.delete, parentCompositeEdit, removeCommand);
                }
            }
        } catch (ModelNodeGetException e) {
            LOGGER.error("Could not get child containers ", e);
            NetconfRpcError error = NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),
                    NetconfRpcErrorTag.DATA_MISSING, "Could not get child containers", m_schemaRegistry);
            throw new EditConfigException(error);
        }
    }


    private void deleteOtherCaseSiblings(EditContext editContext, CompositeEditCommand parentCompositeEdit) throws
            EditConfigException {
        EditContainmentNode editNode = editContext.getEditNode();
        if (EditChangeSource.user.equals(editNode.getChangeSource())) {
            SchemaPath descendantSchemaPath = m_schemaRegistry.getDescendantSchemaPath(this.getModelNodeSchemaPath(),
                    editNode.getQName());
            if (descendantSchemaPath != null) {
                SchemaPath schemaPath = descendantSchemaPath.getParent();
                if (schemaPath != null) {
                    deleteOtherCaseNodes(editContext, parentCompositeEdit, schemaPath);
                }
            }
        }
    }

    private void deleteOtherCaseNodes(EditContext editContext, CompositeEditCommand compositeEdit, SchemaPath
            schemaPath) throws EditConfigException {
        EditContext parentContext = editContext.getParent();
        Set<ChoiceCaseNode> choiceCaseNodes = ChoiceCaseNodeUtil.checkIsCaseNodeAndReturnAllOtherCases
                (getSchemaRegistry(), schemaPath);
        if (choiceCaseNodes != null && !choiceCaseNodes.isEmpty()) {
            SchemaPath choiceCaseParentPath = SchemaRegistryUtil.getDataParentSchemaPath(getSchemaRegistry(),
                    schemaPath);
            if (parentContext == null && !editContext.getEditNode().getQName().equals(choiceCaseParentPath
                    .getLastComponent())) {
                return;
            }
            List<DataSchemaNode> schemaNodes = ChoiceCaseNodeUtil.getAllNodesFromCases(choiceCaseNodes);
            for (DataSchemaNode dataSchemaNode : schemaNodes) {
                QName qName = dataSchemaNode.getQName();
                EditContainmentNode editContainmentNode = null;
                EditContext context = null;

                if (parentContext != null) {
                    editContainmentNode = parentContext.getEditNode();
                    context = parentContext;
                } else {
                    editContainmentNode = new EditContainmentNode(choiceCaseParentPath.getLastComponent(),
                            EditConfigOperations.MERGE);
                    context = new EditContext(editContainmentNode, editContext.getNotificationContext(),
                            editContext.getErrorOption(), editContext.getClientInfo());
                }
                EditChangeNode choiceCaseNodeToBeDelete = new EditChangeNode(qName, createConfigAttribute
                        (dataSchemaNode));
                choiceCaseNodeToBeDelete.setOperation(EditConfigOperations.DELETE);
                try {
                    if (!editContainmentNode.getChangeNodes().contains(choiceCaseNodeToBeDelete)) {
                        SchemaPath modelNodeSchemaPath = this.getModelNodeSchemaPath();
                        ChildContainerHelper childContainerHelper = m_modelNodeHelperRegistry.getChildContainerHelper
                                (modelNodeSchemaPath, qName);
                        if (childContainerHelper != null && childContainerHelper.getValue(this) != null) {
                            editContainmentNode.addChangeNode(choiceCaseNodeToBeDelete);
                            addDeleteCommandsForContainerChoiceCaseNode(childContainerHelper, context, compositeEdit);
                        } else {
                            ChildListHelper childListHelper = m_modelNodeHelperRegistry.getChildListHelper
                                    (modelNodeSchemaPath, qName);
                            if (childListHelper != null) {
                                Collection<ModelNode> modelNodes = childListHelper.getValue(this, Collections
                                        .emptyMap());
                                if (modelNodes != null && !modelNodes.isEmpty()) {
                                    editContainmentNode.addChangeNode(choiceCaseNodeToBeDelete);
                                    addDeleteCommandsForListChoiceCaseNode(childListHelper, context, compositeEdit);
                                }
                            } else {
                                ConfigAttributeHelper configAttributeHelper = m_modelNodeHelperRegistry
                                        .getConfigAttributeHelper(modelNodeSchemaPath, qName);
                                if (configAttributeHelper != null && configAttributeHelper.getValue(this) != null) {
                                    editContainmentNode.addChangeNode(choiceCaseNodeToBeDelete);
                                    addDeleteCommandForLeafChoiceCaseNode(context, compositeEdit, qName,
                                            configAttributeHelper);
                                } else {
                                    ChildLeafListHelper configLeafListHelper = m_modelNodeHelperRegistry.
                                            getConfigLeafListHelper(modelNodeSchemaPath, qName);
                                    if (configLeafListHelper != null) {
                                        Collection<ConfigLeafAttribute> leafLists = configLeafListHelper.getValue(this);
                                        if (leafLists != null && !leafLists.isEmpty()) {
                                            editContainmentNode.addChangeNode(choiceCaseNodeToBeDelete);
                                            addDeleteCommandForLeafListChoiceCaseNode(compositeEdit,
                                                    configLeafListHelper);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (GetAttributeException e) {
                    EditConfigException exception = new EditConfigException(e.getRpcError());
                    exception.addSuppressed(e);
                    throw exception;
                } catch (ModelNodeGetException e) {
                    NetconfRpcError error = new NetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED,
                            NetconfRpcErrorType.Application, NetconfRpcErrorSeverity.Error, e.getMessage());
                    EditConfigException exception = new EditConfigException(error);
                    exception.addSuppressed(e);
                    throw exception;
                }

            }
        }
    }

    private ConfigLeafAttribute createConfigAttribute(DataSchemaNode dataSchemaNode) {
        if (dataSchemaNode instanceof LeafSchemaNode) {
            return ConfigAttributeFactory.getConfigLeafAttribute(getSchemaRegistry(), (LeafSchemaNode)
                    dataSchemaNode, "");
        }
        return null;
    }

    private void addDeleteCommandForLeafChoiceCaseNode(EditContext editContext, CompositeEditCommand
            parentCompositeEdit,
                                                       QName qName, ConfigAttributeHelper configAttributeHelper) throws
            EditConfigException {
        try {
            if (configAttributeHelper.getValue(this) != null) {
                DeleteLeafCommand cmd = new DeleteLeafCommand()
                        .addDeleteInfo(getSchemaRegistry(), editContext, configAttributeHelper, this, qName, false);
                decorateAndAppend(editContext, ModelNodeChangeType.delete, parentCompositeEdit, cmd, true);
            }
        } catch (GetAttributeException e) {
            LOGGER.error("Could not get leaf value", e);
            NetconfRpcError error = NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),
                    NetconfRpcErrorTag.DATA_MISSING, "Could not get leaf value", m_schemaRegistry);
            throw new EditConfigException(error);
        }
    }

    private void addDeleteCommandForLeafListChoiceCaseNode(CompositeEditCommand parentCompositeEdit,
                                                           ChildLeafListHelper childLeafListHelper) throws
            EditConfigException {
        try {
            Collection<ConfigLeafAttribute> leafListValues = childLeafListHelper.getValue(this);
            if (leafListValues != null && !leafListValues.isEmpty()) {
                DeleteLeafListCommand deleteLeafListCommand = new DeleteLeafListCommand().addDeleteInfo
                        (childLeafListHelper, this);
                parentCompositeEdit.appendCommand(deleteLeafListCommand);
            }
        } catch (GetAttributeException e) {
            LOGGER.error("Could not get leaf-list values", e);
            NetconfRpcError error = NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(),
                    NetconfRpcErrorTag.DATA_MISSING, "Could not get leaf-list values", m_schemaRegistry);
            throw new EditConfigException(error);
        }
    }


    private void addDeleteCommands(EditContext editContext, CompositeEditCommand parentCompositeEdit) throws
            EditConfigException {
        EditContainmentNode childEditNode = editContext.getEditNode();
        ChildContainerHelper childContainerHelper = m_modelNodeHelperRegistry.getChildContainerHelper(this
                        .getModelNodeSchemaPath(),
                childEditNode.getQName());

        if (childContainerHelper != null) {
            ModelNode child = ModelNodeConstraintProcessor.getChildNode(childContainerHelper, this, childEditNode);
            DeleteChildCommand deleteCommand = new DeleteChildCommand().addDeleteInfo(childContainerHelper, this, child
            );
            decorateAndAppend(editContext, ModelNodeChangeType.delete, parentCompositeEdit, deleteCommand);
        } else {
            ChildListHelper childListHelper = m_modelNodeHelperRegistry.getChildListHelper(this
                    .getModelNodeSchemaPath(), childEditNode
                    .getQName());
            ModelNode child = ModelNodeConstraintProcessor.getChildNode(childListHelper, this, childEditNode);
            Command removeCommand = new RemoveChildFromListCommand().addRemoveInfo(childListHelper, this, child
            );
            decorateAndAppend(editContext, ModelNodeChangeType.delete, parentCompositeEdit, removeCommand);

        }
    }

    private void addCreateCommands(EditContext editContext, CompositeEditCommand parentCompositeEdit) throws
            EditConfigException {

        EditContainmentNode childEditNode = editContext.getEditNode();

        ChildContainerHelper childContainerHelper = m_modelNodeHelperRegistry.getChildContainerHelper(this
                .getModelNodeSchemaPath(), childEditNode.getQName());

        if (childContainerHelper != null) {
            // handle choice case node

            deleteOtherCaseSiblings(editContext, parentCompositeEdit);
            ModelNodeConstraintProcessor.validateExistentContainer(childContainerHelper, this, childEditNode);
            //Create command takes care of all the edits in the create subtree
            CreateChildCommand createCommand = new CreateChildCommand(editContext, m_modelNodeHelperRegistry
                    .getDefaultCapabilityCommandInterceptor()).addCreateInfo(childContainerHelper, this);
            decorateAndAppend(editContext, ModelNodeChangeType.create, parentCompositeEdit, createCommand);

        } else {
            ChildListHelper childListHelper = m_modelNodeHelperRegistry.getChildListHelper(this
                    .getModelNodeSchemaPath(), childEditNode.getQName());
            // handle choice case node
            deleteOtherCaseSiblings(editContext, parentCompositeEdit);
            // check if the child already exists ...
            ModelNodeConstraintProcessor.validateExistentList(childListHelper, this, childEditNode);

            // Add command takes care of all the edits in the create subtree
            AddChildToListCommand addCommand = new AddChildToListCommand(editContext, m_modelNodeHelperRegistry
                    .getDefaultCapabilityCommandInterceptor()).addAddInfo(childListHelper, this);
            decorateAndAppend(editContext, ModelNodeChangeType.create, parentCompositeEdit, addCommand);
        }
    }

    private void addNoneCommands(EditContext editContext, CompositeEditCommand parentCompositeEdit) throws
            EditConfigException {
        EditContainmentNode childEditNode = editContext.getEditNode();

        ChildContainerHelper childContainerHelper = m_modelNodeHelperRegistry.getChildContainerHelper(this
                .getModelNodeSchemaPath(), childEditNode.getQName());
        if (childContainerHelper != null) {
            ModelNode child = ModelNodeConstraintProcessor.getChildNode(childContainerHelper, this, childEditNode);
            Command childCommand = new DelegateToChildCommand().addInfo(editContext, child);
            parentCompositeEdit.appendCommand(childCommand);
        } else {
            ChildListHelper childListHelper = m_modelNodeHelperRegistry.getChildListHelper(this
                    .getModelNodeSchemaPath(), childEditNode.getQName());
            ModelNode child = ModelNodeConstraintProcessor.getChildNode(childListHelper, this,
                    childEditNode);
            DelegateToChildCommand childCommand = new DelegateToChildCommand().addInfo(
                    new EditContext(childEditNode, editContext.getNotificationContext(), editContext
                            .getErrorOption(), editContext.getClientInfo()), child);
            parentCompositeEdit.appendCommand(childCommand);
        }
    }

    private void decorateAndAppend(EditContext editContext, ModelNodeChangeType changeType, CompositeEditCommand
            parentCompositeEdit, Command innerCommand, boolean isImpliedChange) throws EditConfigException {
        ModelNodeChange change = new ModelNodeChange(changeType, editContext.getEditNode());
        SubSystemValidationDecorator subSystemValidationDecorator = new SubSystemValidationDecorator(innerCommand,
                getSubSystem(), editContext, changeType, this, m_modelNodeHelperRegistry);
        NotificationDecorator decoratedCommand = new NotificationDecorator(subSystemValidationDecorator, this, change,
                editContext.getNotificationContext(), editContext.getClientInfo(), isImpliedChange);
        parentCompositeEdit.appendCommand(decoratedCommand);
    }

    private void decorateAndAppend(EditContext editContext, ModelNodeChangeType changeType, CompositeEditCommand
            parentCompositeEdit, Command innerCommand) throws EditConfigException {
        decorateAndAppend(editContext, changeType, parentCompositeEdit, innerCommand, false);
    }

    private void addCommandsForCurrentNodeChanges(EditContext editContext, CompositeEditCommand compositeEdit) throws
            EditConfigException {
        //for replace operation, we need to set attribute because we removed the node before calling this function
        if (EditConfigOperations.REPLACE.equals(editContext.getEditNode().getEditOperation()) || hasDeltaChanges
                (editContext, this, compositeEdit)) {
            SetConfigAttributeCommand setCommand = new SetConfigAttributeCommand().addSetInfo(getSchemaRegistry(),
                    m_modelNodeHelperRegistry.getConfigAttributeHelpers(this.getModelNodeSchemaPath()),
                    m_modelNodeHelperRegistry.getConfigLeafListHelpers(this.getModelNodeSchemaPath()), this,
                    editContext);
            decorateAndAppend(editContext, ModelNodeChangeType.merge, compositeEdit, setCommand);
            for (EditChangeNode changeNode : editContext.getEditNode().getChangeNodes()) {
                SchemaPath descendantSchemaPath = m_schemaRegistry.getDescendantSchemaPath(this
                                .getModelNodeSchemaPath(),
                        changeNode.getQName());
                if (EditChangeSource.user.equals(changeNode.getChangeSource())) {
                    SchemaPath parentSchemaPath = descendantSchemaPath.getParent();
                    if (parentSchemaPath != null) {
                        deleteOtherCaseNodes(editContext, compositeEdit, parentSchemaPath);
                    }
                }
            }

        }
    }

    private boolean hasDeltaChanges(EditContext editContext, ModelNode changeModeNode, CompositeEditCommand
            compositeEdit) throws EditConfigException {
        List<EditChangeNode> changeNodes = editContext.getEditNode().getChangeNodes();
        boolean hasDeltaChanges = false;
        if (null != changeNodes && !changeNodes.isEmpty()) {
            Iterator<EditChangeNode> changeNodesItr = changeNodes.iterator();
            while (changeNodesItr.hasNext()) {
                EditChangeNode editChangeNode = changeNodesItr.next();
                QName qName = editChangeNode.getQName();
                ConfigAttributeHelper configAttributeHelper = m_modelNodeHelperRegistry.getConfigAttributeHelper
                        (changeModeNode.getModelNodeSchemaPath(),
                        qName);
                try {
                    LOGGER.debug("Compare current value and changing value of: " + qName.toString());
                    if (null != configAttributeHelper) {
                        ConfigLeafAttribute currentValue = configAttributeHelper.getValue(changeModeNode);
                        String changingValue = editChangeNode.getValue();
                        boolean isDeleteOperation = EditConfigOperations.DELETE.equals(editChangeNode.getOperation())
                                || EditConfigOperations.REMOVE.equals(editChangeNode.getOperation());
                        if (null != currentValue && currentValue.getStringValue().equals(changingValue)) {
                            if (!isDeleteOperation) {
                                changeNodesItr.remove();
                                LOGGER.debug("The values are same: " + currentValue + " and " + changingValue
                                        + ". Removed from editContext !");
                            }
                        } else if (editChangeNode.getValue().isEmpty() && isDeleteOperation) {
                            // delete operation does not need the current value in the request
                            // ref. https://www.ietf.org/mail-archive/web/netconf/current/msg11126.html
                            // hasDeltaChanges should not be set
                        } else {
                            hasDeltaChanges = true;
                        }
                        if (isDeleteOperation) {
                            if (!hasDeltaChanges) {
                                try {
                                    addDeleteAttributeCommandForChangedNodes(editContext, compositeEdit,
                                            configAttributeHelper, editChangeNode);
                                } catch (EditConfigException e) {
                                    LOGGER.error(String.format("Could not add the delete command for attribute %s at " +
                                            "%s", changingValue, qName), e);
                                }
                            } else if (EditConfigOperations.DELETE.equals(editChangeNode.getOperation())) {
                                LOGGER.error(String.format("Could not find the value %s of %s", changingValue,
                                        editChangeNode));
                                throw new EditConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId
                                        (getModelNodeId(), NetconfRpcErrorTag.DATA_MISSING,
                                        "Could not find the value: " + changingValue + " of node " + editChangeNode,
                                                m_schemaRegistry));
                            }
                        }
                    } else {
                        // if it is not a leaf, it should be a leaf-list
                        hasDeltaChanges = true; // Let the SetConfigAttributeCommand handle the behavior of leaf-list
                    }
                } catch (GetAttributeException e) {
                    LOGGER.error("Could not fetch current value of  " + qName, e);
                    return false;
                }
            }
        }
        return hasDeltaChanges;
    }

    public boolean isThisInstance(EditContainmentNode editNode) throws EditConfigException {
        boolean thisInstance = true;
        for (QName key : m_modelNodeHelperRegistry.getNaturalKeyHelpers(this.getModelNodeSchemaPath()).keySet()) {
            for (EditMatchNode matchNode : editNode.getMatchNodes()) {
                if (matchNode.getQName().equals(key)) {
                    try {
                        if (!matchNode.getValue().equals(m_modelNodeHelperRegistry.getNaturalKeyHelper(this
                                        .getModelNodeSchemaPath(),
                                key).getValue(this).getStringValue())) {
                            thisInstance = false;
                            break;
                        }
                    } catch (GetAttributeException e) {
                        LOGGER.error("Could get value from ModelNode", e);
                        EditConfigException exception = new EditConfigException(NetconfRpcErrorUtil
                                .getNetconfRpcErrorForModelNodeId(getModelNodeId(), NetconfRpcErrorTag.DATA_MISSING, "Could " +
                                        "get value from ModelNode" + e.getMessage(), m_schemaRegistry));
                        exception.addSuppressed(e);
                        throw exception;
                    }
                }
            }
        }
        return thisInstance;
    }

    public void setKeyAttributes(Map<QName, ConfigLeafAttribute> constructorArgs) throws SetAttributeException {
        for (Map.Entry<QName, ConfigLeafAttribute> qNameConfigLeafAttributeEntry : constructorArgs.entrySet()) {
            ConfigAttributeHelper keyAttributeHelper = m_modelNodeHelperRegistry.getNaturalKeyHelpers(this
                    .getModelNodeSchemaPath()).get
                    (qNameConfigLeafAttributeEntry.getKey());
            keyAttributeHelper.setValue(this, qNameConfigLeafAttributeEntry.getValue());
        }

    }

    @Override
    public ModelNodeId getModelNodeId() {
        if (m_modelNodeId == null) {
            synchronized (this) {
                if (m_modelNodeId == null) {
                    m_modelNodeId = new ModelNodeId(m_parentNodeId);
                    m_modelNodeId.addRdn(ModelNodeRdn.CONTAINER, getQName().getNamespace().toString(),
                            getContainerName());
                    Map<QName, ConfigAttributeHelper> naturalKeyHelpers = m_modelNodeHelperRegistry
                            .getNaturalKeyHelpers(this
                            .getModelNodeSchemaPath());
                    for (QName helperName : naturalKeyHelpers.keySet()) {
                        try {
                            m_modelNodeId.addRdn(helperName.getLocalName(), helperName.getNamespace().toString(),
                                    naturalKeyHelpers.get(helperName).getValue(this).getStringValue());
                        } catch (GetAttributeException e) {
                            throw new RuntimeException("Could not get key attributes ", e);
                        }
                    }
                }
            }
        }
        return m_modelNodeId;
    }

    /**
     * Mostly used by tests
     *
     * @param modelNodeId
     */
    public void setModelNodeId(ModelNodeId modelNodeId) {
        m_modelNodeId = modelNodeId;
    }

    @Override
    public void copyConfig(Element config) throws CopyConfigException {
        String namespace = config.getNamespaceURI();
        if (namespace != null && !getSchemaRegistry().isKnownNamespace(namespace)) {
            LOGGER.error("An unexpected namespace " + namespace + " is present in " + getLocalName(config));
            NetconfRpcError rpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.UNKNOWN_NAMESPACE,
                    "An unexpected namespace is present");
            rpcError.addErrorInfoElement(NetconfRpcErrorInfo.BadElement, getLocalName(config));
            rpcError.addErrorInfoElement(NetconfRpcErrorInfo.BadNamespace, namespace);
            throw new CopyConfigException(rpcError);
        }
        copyConfigAttributes(config);
        copyConfigLeafLists(config);
        copyChildContainers(config);
        copyChildLists(config);
    }

    private void copyChildLists(Element config) throws CopyConfigException {
        Map<QName, ChildListHelper> childListHelpers = m_modelNodeHelperRegistry.getChildListHelpers(this
                .getModelNodeSchemaPath());
        for (Entry<QName, ChildListHelper> childListHelperEntry : childListHelpers.entrySet()) {
            NodeList childNodes = config.getChildNodes();
            boolean found = false;
            ChildListHelper helper = childListHelperEntry.getValue();

            Set<QName> alreadyProcessed = new HashSet<QName>();

            for (int i = 0; i < childNodes.getLength(); i++) {
                Node iItem = childNodes.item(i);
                if (iItem.getNodeType() == Node.ELEMENT_NODE) {
                    QName iITemQName = getQName(iItem);
                    if (!alreadyProcessed.contains(iITemQName)) {
                        QName helperName = childListHelperEntry.getKey();
                        if (helperName.equals(iITemQName)) {
                            found = true;
                            try {
                                //remove all children
                                Collection<ModelNode> children = helper.getValue(this, Collections.<QName,
                                        ConfigLeafAttribute>emptyMap());
                                if (children != null && !children.isEmpty()) {
                                    helper.removeAllChild(this);
                                }

                                //recreate from copy request
                                // get modelNodeUris and form key and config attributes
                                Map<QName, ConfigLeafAttribute> keyAttrs = new HashMap<>();
                                Map<QName, ConfigLeafAttribute> configAttrs = new HashMap<>();
                                SchemaPath uri = helper.getChildModelNodeSchemaPath();
                                QName containerQName = uri.getLastComponent();
                                if (iITemQName.equals(containerQName)) {
                                    //Get key attributes
                                    Map<QName, ConfigAttributeHelper> keyAttrHelper = m_modelNodeHelperRegistry
                                            .getNaturalKeyHelpers(uri);
                                    for (Entry<QName, ConfigAttributeHelper> configAttributeHelperEntry :
                                            keyAttrHelper.entrySet()) {

                                        NodeList nodeList = iItem.getChildNodes();
                                        for (int j = 0; j < nodeList.getLength(); j++) {
                                            Node node = nodeList.item(j);
                                            if ((node instanceof Element) && configAttributeHelperEntry.getKey()
                                                    .getLocalName().equals(node.getLocalName())) {
                                                keyAttrs.put(configAttributeHelperEntry.getKey(), ConfigAttributeFactory
                                                        .getConfigAttribute(m_schemaRegistry, uri,
                                                                configAttributeHelperEntry
                                                                .getKey(), node));
                                            }
                                        }
                                    }
                                    //Get config attributes
                                    Map<QName, ConfigAttributeHelper> configAttrHelper = m_modelNodeHelperRegistry
                                            .getConfigAttributeHelpers(uri);
                                    for (Entry<QName, ConfigAttributeHelper> configAttributeHelperEntry :
                                            configAttrHelper.entrySet()) {
                                        NodeList nodeList = iItem.getChildNodes();
                                        for (int j = 0; j < nodeList.getLength(); j++) {
                                            Node node = nodeList.item(j);
                                            if ((node instanceof Element) && configAttributeHelperEntry.getKey()
                                                    .getLocalName().equals(node.getLocalName())) {
                                                configAttrs.put(configAttributeHelperEntry.getKey(),
                                                        ConfigAttributeFactory
                                                        .getConfigAttribute(m_schemaRegistry, uri,
                                                                configAttributeHelperEntry.getKey(), node));
                                            }
                                        }
                                    }
                                }

                                ModelNode child = helper.addChild(this, iItem.getLocalName(), keyAttrs, configAttrs);
                                child.copyConfig((Element) iItem);

                                alreadyProcessed.add(helperName);
                                //process all other nodes of the same type
                                for (int j = i + 1; j < childNodes.getLength(); j++) {
                                    Node jItem = childNodes.item(j);
                                    if (jItem.getNodeType() == Node.ELEMENT_NODE) {
                                        QName jItemQName = getQName(jItem);
                                        if (helperName.equals(jItemQName)) {
                                            Map<QName, ConfigLeafAttribute> keyAttributes = new HashMap<QName,
                                                    ConfigLeafAttribute>();
                                            Map<QName, ConfigLeafAttribute> configAttributes = new HashMap<QName,
                                                    ConfigLeafAttribute>();
                                            //Get key attributes
                                            Map<QName, ConfigAttributeHelper> keyAttrHelper =
                                                    m_modelNodeHelperRegistry.getNaturalKeyHelpers(uri);
                                            for (Entry<QName, ConfigAttributeHelper> configAttributeHelperEntry :
                                                    keyAttrHelper.entrySet()) {

                                                NodeList jNodeList = jItem.getChildNodes();
                                                for (int k = 0; k < jNodeList.getLength(); k++) {
                                                    Node kNode = jNodeList.item(k);
                                                    if ((kNode instanceof Element) && configAttributeHelperEntry
                                                            .getKey().getLocalName().equals(kNode.getLocalName())) {
                                                        keyAttributes.put(configAttributeHelperEntry.getKey(), ConfigAttributeFactory
                                                                .getConfigAttribute(m_schemaRegistry, uri, configAttributeHelperEntry
                                                                        .getKey(), kNode));
                                                    }
                                                }
                                            }
                                            //Get config attributes
                                            Map<QName, ConfigAttributeHelper> configAttrHelper = m_modelNodeHelperRegistry.getConfigAttributeHelpers(uri);
                                            for (Entry<QName, ConfigAttributeHelper> configAttributeHelperEntry : configAttrHelper.entrySet()) {
                                                NodeList jNodeList = jItem.getChildNodes();
                                                for (int k = 0; k < jNodeList.getLength(); k++) {
                                                    Node kNode = jNodeList.item(k);
                                                    if ((kNode instanceof Element) && configAttributeHelperEntry.getKey().getLocalName().equals(kNode.getLocalName())) {
                                                        configAttributes.put(configAttributeHelperEntry.getKey(), ConfigAttributeFactory
                                                                .getConfigAttribute(m_schemaRegistry, uri, configAttributeHelperEntry
                                                                        .getKey(), jNodeList.item(k)));
                                                    }
                                                }
                                            }
                                            child = helper.addChild(this, jItem.getLocalName(), keyAttributes, configAttributes);
                                            child.copyConfig((Element) jItem);
                                        }
                                    }//ignore otherwise

                                }
                            } catch (ModelNodeGetException | ModelNodeCreateException | ModelNodeDeleteException e) {
                                LOGGER.error("Failed to set child ", e);
                                CopyConfigException exception = new CopyConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(), NetconfRpcErrorTag.OPERATION_FAILED, "Failed to set child ", m_schemaRegistry));
                                exception.addSuppressed(e);
                                throw exception;
                            } catch (InvalidIdentityRefException e) {
                                CopyConfigException exception = new CopyConfigException(e.getRpcError());
                                throw exception;
                            }
                            break;
                        }
                    }
                }//else ignore it
            }
            if (!found) {
                try {
                    //reset if there is no configuration
                    helper.removeAllChild(this);
                } catch (ModelNodeDeleteException e) {
                    LOGGER.error("Failed to re-set child ", e);
                    CopyConfigException exception = new CopyConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(), NetconfRpcErrorTag.OPERATION_FAILED, "Failed to re-set child ", m_schemaRegistry));
                    exception.addSuppressed(e);
                    throw exception;
                }
            }
        }
    }

    private QName getQName(Node iItem) {
        QName qname = null;
        String namespace = iItem.getNamespaceURI();
        String localName = iItem.getLocalName();
        qname = m_schemaRegistry.lookupQName(namespace, localName);
        return qname;
    }

    private void copyChildContainers(Element config) throws CopyConfigException {
        Map<QName, ChildContainerHelper> annotationChildContainerHelpers = m_modelNodeHelperRegistry.getChildContainerHelpers(this.getModelNodeSchemaPath());
        for (Entry<QName, ChildContainerHelper> childContainerHelperEntry : annotationChildContainerHelpers.entrySet()) {
            NodeList childNodes = config.getChildNodes();
            boolean found = false;
            ChildContainerHelper helper = childContainerHelperEntry.getValue();
            for (int i = 0; i < childNodes.getLength(); i++) {
                QName helperName = childContainerHelperEntry.getKey();
                Node iItem = childNodes.item(i);
                if (iItem.getNodeType() == Node.ELEMENT_NODE) {
                    QName iITemQName = getQName(iItem);
                    if (helperName.equals(iITemQName)) {
                        found = true;
                        try {
                            ModelNode child = helper.getValue(this);
                            if (child == null) {
                                child = helper.createChild(this, new HashMap<>());
                            }
                            child.copyConfig((Element) iItem);
                        } catch (ModelNodeCreateException | ModelNodeGetException e) {
                            LOGGER.error("Failed to set child ", e);
                            CopyConfigException exception = new CopyConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(), NetconfRpcErrorTag.OPERATION_FAILED, "Failed to set child ", m_schemaRegistry));
                            exception.addSuppressed(e);
                            throw exception;
                        }
                        break;
                    }
                }//else ignore it

            }
            if (!found) {
                try {
                    //reset if there is no configuration
                    helper.deleteChild(this);
                } catch (ModelNodeDeleteException e) {
                    LOGGER.error("Failed to re-set child ", e);
                    CopyConfigException exception = new CopyConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(), NetconfRpcErrorTag.OPERATION_FAILED, "Failed to re-set child ", m_schemaRegistry));
                    exception.addSuppressed(e);
                    throw exception;
                }
            }

        }
    }

    private void copyConfigAttributes(Element config) throws CopyConfigException {
        Map<QName, ConfigAttributeHelper> configAttributeHelpers = m_modelNodeHelperRegistry.getConfigAttributeHelpers(this.getModelNodeSchemaPath());
        for (Entry<QName, ConfigAttributeHelper> configAttributeHelperEntry : configAttributeHelpers.entrySet()) {
            NodeList childNodes = config.getChildNodes();
            boolean found = false;
            ConfigAttributeHelper helper = configAttributeHelperEntry.getValue();
            for (int i = 0; i < childNodes.getLength(); i++) {
                QName helperName = configAttributeHelperEntry.getKey();
                Node iItem = childNodes.item(i);

                if (iItem.getNodeType() == Node.ELEMENT_NODE) {
                    QName iITemQName = getQName(iItem);

                    if (helperName.equals(iITemQName)) {
                        found = true;
                        try {
                            helper.setValue(this, ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, this.getModelNodeSchemaPath()
                                    , configAttributeHelperEntry.getKey(), iItem));
                        } catch (SetAttributeException e) {
                            LOGGER.error("Failed to set attribute ", e);
                            CopyConfigException exception = new CopyConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(), NetconfRpcErrorTag.OPERATION_FAILED, "Failed to set attribute ", m_schemaRegistry));
                            exception.addSuppressed(e);
                            throw exception;
                        } catch (InvalidIdentityRefException e) {
                            CopyConfigException exception = new CopyConfigException(e.getRpcError());
                            throw exception;
                        }
                        break;
                    }
                }//else ignore it

            }
            if (!found) {
                try {
                    if (helper.isMandatory()) {
                        //reset if there is no configuration
                        helper.setValue(this, null);
                    }
                } catch (SetAttributeException e) {
                    LOGGER.error("Failed to re-set attribute ", e);
                    CopyConfigException exception = new CopyConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(), NetconfRpcErrorTag.OPERATION_FAILED, "Failed to re-set attribute ", m_schemaRegistry));
                    exception.addSuppressed(e);
                    throw exception;
                }
            }

        }
    }

    private void copyConfigLeafLists(Element config) throws CopyConfigException {
        Map<QName, ChildLeafListHelper> childLeafListHelpers = m_modelNodeHelperRegistry.getConfigLeafListHelpers(this.getModelNodeSchemaPath());
        for (Entry<QName, ChildLeafListHelper> childLeafListHelperEntry : childLeafListHelpers.entrySet()) {
            NodeList childNodes = config.getChildNodes();
            boolean found = false;
            ChildLeafListHelper helper = childLeafListHelperEntry.getValue();
            if (helper.isConfiguration()) {
                for (int i = 0; i < childNodes.getLength(); i++) {
                    QName helperName = childLeafListHelperEntry.getKey();
                    Node iItem = childNodes.item(i);
                    if (iItem.getNodeType() == Node.ELEMENT_NODE) {
                        QName iITemQName = getQName(iItem);

                        if (helperName.equals(iITemQName)) {
                            found = true;
                            try {
                                helper.addChild(this, ConfigAttributeFactory.getConfigAttribute(m_schemaRegistry, this
                                        .getModelNodeSchemaPath(), childLeafListHelperEntry.getKey(), iItem));
                            } catch (DOMException | SetAttributeException e) {
                                LOGGER.error("Failed to add LeafList attribute ", e);
                                CopyConfigException exception = new CopyConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(), NetconfRpcErrorTag.OPERATION_FAILED, "Failed to add LeafList attribute ", m_schemaRegistry));
                                exception.addSuppressed(e);
                                throw exception;
                            } catch (InvalidIdentityRefException e) {
                                CopyConfigException exception = new CopyConfigException(e.getRpcError());
                                throw exception;
                            }
                        }
                    }//ignore otherwise
                }
                if (!found) {
                    try {
                        if (helper.isMandatory()) {
                            //reset if there is no configuration
                            helper.addChild(this, null);
                        }
                    } catch (SetAttributeException e) {
                        LOGGER.error("Failed to re-set Leaf List attribute ", e);
                        CopyConfigException exception = new CopyConfigException(NetconfRpcErrorUtil.getNetconfRpcErrorForModelNodeId(getModelNodeId(), NetconfRpcErrorTag.OPERATION_FAILED, "Failed to re-set Leaf List attribute ", m_schemaRegistry));
                        exception.addSuppressed(e);
                        throw exception;
                    }
                }
            }
        }
    }

    public SubSystemRegistry getSubSystemRegistry() {
        return m_subSystemRegistry;
    }

    @Override
    public Map<QName, String> getListKeys() throws GetAttributeException {
        Map<QName, String> keyValues = new HashMap<>();
        for (Entry<QName, ConfigAttributeHelper> keyEntry : m_modelNodeHelperRegistry.getNaturalKeyHelpers(getModelNodeSchemaPath()).entrySet()) {
            ConfigAttributeHelper helper = keyEntry.getValue();
            QName keyQname = keyEntry.getKey();
            keyValues.put(keyQname, helper.getValue(this).getStringValue());
        }
        return keyValues;
    }

    @Override
    public void setValue(Object value) {
    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public String toString() {
        return "HelperDrivenModelNode [m_modelNodeId=" + m_modelNodeId + "]";
    }

    @Override
    public boolean isRoot() {
        return isRootSchemaPath(getModelNodeSchemaPath());
    }
}