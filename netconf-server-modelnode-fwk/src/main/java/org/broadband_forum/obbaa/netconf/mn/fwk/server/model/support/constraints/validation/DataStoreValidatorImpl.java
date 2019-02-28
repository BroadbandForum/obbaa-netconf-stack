package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import static org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations.DELETE;
import static org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations.REMOVE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.jxpath.ri.compiler.Step;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AnvExtensions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditChangeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBean;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeDynaBeanFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service.DSAugmentValidation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service.DSConstraintValidation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationErrors;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.MNKeyUtil;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraintAware;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class DataStoreValidatorImpl implements DataStoreValidator {

	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DataStoreValidatorImpl.class, LogAppNames.NETCONF_STACK);

	private static final Long VALIDATION_TIME_FORCE_PRINT_CUTOFF = 2000L;

	private final SchemaRegistry m_schemaRegistry;
	private final ModelNodeHelperRegistry m_modelNodeHelperRegistry;
	private final ModelNodeDataStoreManager m_modelNodeDSM;
	private final DataStoreIntegrityService m_integrityService;
	private final ChoiceMandatoryChildrenValidator m_choiceCaseValidator;
	private final DSExpressionValidator m_expValidator;
	private final DataStoreValidationPathBuilder m_dsPathTraverser;
	private final DSValidation m_augmentValidator;
	private final DSValidation m_constraintValidator;

	//For UT
	private boolean m_validatedChildCacheHitStatus = false;

	private void logDebug(String message, Object... objects) {
		Boolean isDebugEnabled = (Boolean) RequestScope.getCurrentScope().getFromCache(DataStoreValidatorImpl.class.getName());
		if (isDebugEnabled == null) {
			isDebugEnabled = LOGGER.isDebugEnabled();
			RequestScope.getCurrentScope().putInCache(DataStoreValidatorImpl.class.getName(), isDebugEnabled);
		}

		if (isDebugEnabled) {
			LOGGER.debug(message, objects);
		}
	}

	public DataStoreValidatorImpl(SchemaRegistry schemaRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry,
								  ModelNodeDataStoreManager modelNodeDSM, DataStoreIntegrityService integrityService, DSExpressionValidator expValidator) {
		m_schemaRegistry = schemaRegistry;
		m_modelNodeHelperRegistry = modelNodeHelperRegistry;
		m_modelNodeDSM = modelNodeDSM;
		m_integrityService = integrityService;
		m_expValidator = expValidator;
		m_choiceCaseValidator = new ChoiceMandatoryChildrenValidator(m_schemaRegistry, m_modelNodeHelperRegistry, this);
		m_dsPathTraverser = new DataStoreValidationPathBuilder(m_schemaRegistry, m_modelNodeHelperRegistry);
		m_augmentValidator = new DSAugmentValidation(expValidator, schemaRegistry);
		m_constraintValidator = new DSConstraintValidation(expValidator);
	}

	private void initializeCache(){
		DataStoreValidationUtil.resetValidationContext();
	}

	private void addToDeletedCache(EditContainmentNode node, QName attribute) {
		String modelNodeId = node.getModelNodeId().pathString();
		if (attribute != null) {
			// Indicates only leaf is deleted
			getValidationContext().recrodDeletedChangeAttribute(modelNodeId, attribute);
		} else {
			// indicates the entire edit containment is deleted
			getValidationContext().recordDeletedChangeNode(modelNodeId, node);
		}
	}

	private boolean isNodeDeleted(EditContainmentNode changedNode) {
		EditContainmentNode node =  getValidationContext().getDeletedNode(changedNode.getModelNodeId().pathString());
		return node == null ? false : true;
	}

	private boolean isNodeDeleted(EditContainmentNode changedNode, DataSchemaNode changeNode) {
		if (isNodeDeleted(changedNode)){
			return true;
		}
		QName qname = getValidationContext().getDeletedAttribute(changedNode.getModelNodeId().pathString());
		if(qname != null && changeNode.getQName().equals(qname)){
			return true;
		}
		return false;
	}

	private void cacheDeleteChangNode(EditContainmentNode changedNode) {
		ModelNodeId id = changedNode.getModelNodeId();

		if (getValidationContext().getDeletedNode(id.pathString()) == null) {
			// if the node is not already identified as deleted.
			EditContainmentNode parentEditContainement = changedNode.getParent();
			if (parentEditContainement!=null) {
				if (getValidationContext().getDeletedNode(parentEditContainement.getModelNodeId().pathString()) != null) {
					// if its parent node is deleted, so is the child
					addToDeletedCache(changedNode, null);
					return;
				}
			}

			String operation = changedNode.getEditOperation();
			if (operation.equals(EditConfigOperations.DELETE) || operation.equals(EditConfigOperations.REMOVE)){
				// if the node is deleted/removed add to cache
				addToDeletedCache(changedNode, null);
				return;
			}

			for (EditChangeNode changeNode:changedNode.getChangeNodes()) {
				operation = changeNode.getOperation();
				if (operation.equals(EditConfigOperations.DELETE) || operation.equals(EditConfigOperations.REMOVE)) {
					// add the attribute that is deleted/removed
					addToDeletedCache(changedNode, changeNode.getQName());
				}
			}
		}
	}

	@Override
	public List<EditConfigRequest> validate(RootModelNodeAggregator aggregator, EditContainmentNode editTree,EditConfigRequest request,
											NetconfClientInfo clientInfo) throws ValidationException {
		ValidationTimingLogger.start();
		ValidationTimingLogger.startPhase("init");
		Set<SchemaPath> changeNodes = new LinkedHashSet<SchemaPath>();
		Map<SchemaPath,Collection<EditContainmentNode>> changeNodeMap = new HashMap<SchemaPath,Collection<EditContainmentNode>>();
		Map<SchemaPath, SchemaRegistry> pathToRegistryMap = new HashMap<>();
		Map<EditContainmentNode, Map<String, Object>> editNodeToRegistryMap = new HashMap<>();
		List<ModelNode> rootNodes = aggregator.getModelServiceRootsForEdit(request);
		ModelNode rootModelNode = null;
		SchemaPath rootSchemaPath = null;
		ValidationTimingLogger.endPhase("init");

		try {
			try {
				ValidationTimingLogger.startPhase("initCache");
				initializeCache();
				DataStoreValidationUtil.getValidationContext().setRootNodeAggregator(aggregator);
				for (ModelNode rootNode : rootNodes) {
					/*
					 * every editTree will start from a root node. Find the right root node for the edit tree and
					 * validate
					 */
					if (editTree.getQName().equals(rootNode.getQName())) {
						rootSchemaPath = rootNode.getModelNodeSchemaPath();
						rootModelNode = rootNode;
						getValidationContext().addToModelNodeCache(rootSchemaPath, rootModelNode);
						break;
					}
				}
				ValidationTimingLogger.endPhase("initCache");
				ValidationTimingLogger.startPhase("buildAllChangeSchemaPaths");
				buildAllChangeSchemaPaths(rootSchemaPath, editTree, changeNodes, changeNodeMap, m_schemaRegistry, pathToRegistryMap, editNodeToRegistryMap, new HashMap<>());
				ValidationTimingLogger.endPhase("buildAllChangeSchemaPaths");
				// timing logged inside the method
				validateChanges(editTree, changeNodes, changeNodeMap, rootModelNode, pathToRegistryMap, editNodeToRegistryMap);
				ValidationTimingLogger.startPhase("checkForDeletion");
				checkForNonPresenceContainerDeletion();
				ValidationTimingLogger.endPhase("checkForDeletion");
			} finally {
				// Validation timing should be printed before createOrDeleteNodes
				// to avoid nested info
				try {
					ValidationTimingLogger.finish();
					Long totalTime = ValidationTimingLogger.getTotalTime();
					boolean forcePrint = totalTime > VALIDATION_TIME_FORCE_PRINT_CUTOFF;

					if (LOGGER.isDebugEnabled() || forcePrint) {
						String timingsString = "Validation time details: " + ValidationTimingLogger.getTimingsString();
						if (forcePrint) {
							LOGGER.warn(timingsString);
						}
						else {
							LOGGER.debug(timingsString);
						}
					}
				} catch (Exception e) {
					LOGGER.warn("Problem in logging timing info", e);
				} finally {
					ValidationTimingLogger.close();
				}
			}
			return m_integrityService.createInternalEditRequests(request, clientInfo);
		} catch (GetAttributeException | ModelNodeGetException e) {
			throw new ValidationException(e);
		}
	}

	private void checkForNonPresenceContainerDeletion() throws ModelNodeGetException {
		Map<ModelNode, Collection<QName>> deleteNodes = new HashMap<ModelNode, Collection<QName>>(getValidationContext().getDeleteList());

		for (Map.Entry<ModelNode, Collection<QName>> deleteList : deleteNodes.entrySet()) {
			if (deleteList.getKey() instanceof ModelNodeWithAttributes) {
				for (QName qnameInList : deleteList.getValue()) {
					checkForDeletion(deleteList.getKey(), qnameInList);
				}
			}
		}
	}

	private boolean hasChild(Map<ModelNode, Collection<QName>> nodeMap, ModelNode parentNode, QName childName) {
		Collection<QName> childList = nodeMap.get(parentNode);
		if (childList != null) {
			return childList.contains(childName);
		}
		return false;
	}

	private void checkForDeletion(ModelNode node, QName childName) throws ModelNodeGetException {
		if (node.getParent() == null) {
			// delete/remove on root node not allowed
			return;
		}

		if (node instanceof ModelNodeWithAttributes) {
			if (hasNoOtherChildren((ModelNodeWithAttributes) node, childName)) {
				ModelNode parentNode = node.getParent();
				if (parentNode != null) {
					if (!getValidationContext().isNodeForCreate(node)) {
						getValidationContext().removeFromDelete(node);
						getValidationContext().removeFromMerge(node);
						getValidationContext().recordForDelete(parentNode, node.getQName());
						checkForDeletion(parentNode, node.getQName());
					}
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private boolean hasNoOtherChildren(ModelNodeWithAttributes modelNode, QName exception) throws ModelNodeGetException {
		SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
		DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(modelNode.getSchemaRegistry(), schemaPath);
		SchemaRegistry schemaRegistry = context.getSchemaRegistry();
		DataSchemaNode schemaNode = context.getDataSchemaNode();
		// check if it is a container
		if (!(schemaNode instanceof ContainerSchemaNode)) {
			return false;
		}

		//check if it is a presence container
		if (((ContainerSchemaNode)schemaNode).isPresenceContainer()) {
			return false;
		}

		SchemaPath childSchemaPath = schemaRegistry.getDescendantSchemaPath(schemaPath, exception);
		DataSchemaNode childSchemaNode = SchemaRegistryUtil.getDataSchemaNode(schemaRegistry, childSchemaPath).getDataSchemaNode();

		Map leafLists = modelNode.getLeafLists();
		Map attributes = modelNode.getAttributes();
		// check if there are more than 1 leafList then return false
		// if there is only one leafList and if it is not exception, return false
		if (leafLists.size() > 1) {
			return false;
		} else if (leafLists.size() == 1 && leafLists.get(exception) == null && !(childSchemaNode instanceof LeafListSchemaNode)) {
			return false;
		}

		// if there are more than one attribute return false
		// if there is only one attribute and it is not exception, return false
		if (attributes.size() > 1) {
			return false;
		} else if (attributes.size() == 1 && attributes.get(exception) == null && !(childSchemaNode instanceof LeafSchemaNode)) {
			return false;
		}

		//if there are any childContainers and that is not exception return false
		Map<QName, ChildContainerHelper> containers = m_modelNodeHelperRegistry.getChildContainerHelpers(schemaPath);
		for (Map.Entry<QName,ChildContainerHelper> helper:containers.entrySet()) {
			if (!helper.getKey().equals(exception) && helper.getValue() != null && helper.getValue().getValue(modelNode) != null) {
				return false;
			}
		}

		//if there are any childList and that is not exception, return false
		Map<QName, ChildListHelper> lists = m_modelNodeHelperRegistry.getChildListHelpers(schemaPath);
		for (Map.Entry<QName, ChildListHelper> helper:lists.entrySet()) {
			if (!helper.getKey().equals(exception) && helper.getValue() != null && !helper.getValue().getValue(modelNode, Collections.emptyMap()).isEmpty()) {
				return false;
			}
		}

		// if there are any state nodes, return false
		Collection<DataSchemaNode> children = schemaRegistry.getChildren(schemaPath);
		for (DataSchemaNode child:children) {
			if (!child.isConfiguration()) {
				return false;
			}
		}
		return true;
	}

	private List<ModelNode> getParentWithAccessPath(EditContainmentNode editNode, DynaBean parentBean, LocationPath accessPath, List<ModelNode> parentNodes, boolean nodeNotDeleted) {
		Step[] accessPathSteps = accessPath.getSteps();
		if (accessPath.isAbsolute()) {
			DataSchemaNode rootSchemaNode = DataStoreValidationUtil.getRootSchemaNode(m_schemaRegistry, accessPathSteps[0]);
			if (rootSchemaNode != null) {
				parentBean = DataStoreValidationUtil.getRootModelNode(SchemaRegistryUtil.getSchemaRegistry(m_schemaRegistry), rootSchemaNode);
				if (parentBean == null) {
					// impact node does not exists. return
					return parentNodes;
				}

				Step[] newAccessPathSteps = new Step[accessPathSteps.length - 1];
				System.arraycopy(accessPathSteps, 1, newAccessPathSteps, 0, newAccessPathSteps.length);
				accessPathSteps = newAccessPathSteps;

			}
			// if this is a abs access path, then it does not matter if the node is deleted or not, since the access path is from root
			parentNodes = m_dsPathTraverser.getParentModelNodeWithAccessPath(editNode,parentBean, accessPathSteps, true);
		} else {
			parentNodes = m_dsPathTraverser.getParentModelNodeWithAccessPath(editNode,parentBean, accessPathSteps, nodeNotDeleted);
		}
		return parentNodes;
	}

	private void validateImpactedNode(ModelNode rootModelNode, ModelNode parentNode, EditContainmentNode changedEditNode,
									  DataSchemaNode changeSchemaNode,SchemaPath referencedSchemaPath, LocationPath accessPath, boolean nodeNotDeleted) {
		List<ModelNode> parentNodes = new ArrayList<ModelNode>();
		Step[] accessPathSteps = accessPath == null ? null : accessPath.getSteps();
		if (parentNode == null){
			parentNodes = getParentModelNode(changeSchemaNode, changedEditNode, referencedSchemaPath, accessPathSteps);
		} else {
			if (accessPath == null || accessPath.getSteps().length == 0) {
				accessPath = buildAccessPath(referencedSchemaPath);
				accessPathSteps = accessPath.getSteps();
			}
			DynaBean parentBean = (DynaBean) parentNode.getValue();
			DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(m_schemaRegistry, referencedSchemaPath);
			DataSchemaNode referenceNode = context.getDataSchemaNode();
			parentNodes = getParentWithAccessPath(changedEditNode, parentBean, accessPath, parentNodes, nodeNotDeleted);
			if (parentNodes.isEmpty()) {
				if (SchemaRegistryUtil.hasDefaults(referenceNode)) {
					// might be a parent Node missing for a when condition leaf inside a non-presence container.
					parentNodes = m_dsPathTraverser.getMissingParentNodeWithAccessPath(changedEditNode,parentBean,
							accessPathSteps, nodeNotDeleted);
				} else if (referenceNode instanceof ContainerSchemaNode
						&& DataStoreValidationUtil.containerHasDefaultLeafs(context.getSchemaRegistry(), (ContainerSchemaNode) referenceNode)) {
					// might be a parent Node missing for a when condition container with default leafs.
					parentNodes = m_dsPathTraverser.getMissingParentNodeWithAccessPath(changedEditNode, parentBean,
							accessPathSteps, nodeNotDeleted);
				}
			}
		}

		if (parentNodes.isEmpty() && accessPath != null) {
			//could be abs path
			parentNodes = getParentWithAccessPath(changedEditNode,(DynaBean) rootModelNode.getValue(), accessPath, parentNodes, nodeNotDeleted);
		}
		DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(m_schemaRegistry, referencedSchemaPath);
		DataSchemaNode schemaNode = context.getDataSchemaNode();
		for (ModelNode parentModelNode:parentNodes){
			if (isParent(parentModelNode,referencedSchemaPath)) {
				validateChildIfRequired(parentModelNode, schemaNode);
			}
		}


	}

	private boolean isParent(ModelNode modelNode, SchemaPath childSchemaPath) {
		/*
		 * This method checks whether the childSchemaPath is present in the
		 * modelNode or not. Basically this check is needed so that
		 * childSchemaPath is validated under the correct parentModelNode
		 *
		 */
		DynaBean dynaBean = (DynaBean) modelNode.getValue();
		String localName = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(childSchemaPath.getLastComponent().getLocalName());

		DynaProperty childProperty = dynaBean.getDynaClass().getDynaProperty(localName);
		if (childProperty != null) {
			{
				Object children = dynaBean.get(localName);
				if (children != null) {
					if (children instanceof ModelNodeDynaBean) {
						ModelNode childNode = (ModelNode) ((DynaBean) children).get(ModelNodeWithAttributes.MODEL_NODE);
						if (!(childNode.getModelNodeSchemaPath().equals(childSchemaPath))) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}


	private List<ModelNode> getParentModelNode(DataSchemaNode changeSchemaNode, EditContainmentNode changeEditNode,
											   SchemaPath referencePath, Step[] accessPathSteps) {
		List<ModelNode> returnValue = new ArrayList<ModelNode>();
		boolean newAccessPath = false;
		if (accessPathSteps == null || accessPathSteps.length == 0) {
			accessPathSteps = buildAccessPath(referencePath).getSteps();
			newAccessPath = true;
		}
		if (accessPathSteps != null && accessPathSteps[0] != null){
			Step[] steps = null;
			String nodeName = DataStoreValidationUtil.getLocalName(accessPathSteps[0]);
			String operation  = changeEditNode.getEditOperation();
			boolean deleteNode = false;
			if (!newAccessPath && (operation.equals(DELETE) || operation.equals(REMOVE))){
				deleteNode = true;
			}

			EditContainmentNode nextNode = null;
			if (changeEditNode.getQName().getLocalName().equals(nodeName) && deleteNode){
				nextNode = changeEditNode.getParent();
				steps = new Step[accessPathSteps.length -1 ];
				System.arraycopy(accessPathSteps, 1, steps, 0, steps.length);
			}
			/**
			 * if nextNode!=null --> indicates we have reached next step of the path. Continue to find the next path
			 * if nextNode == null && deleteNode is true --> indicates the modelNode is deleted and we need to skip the step and proceed
			 * if nextNode == null && deleteNode is false, we might have hit the end node. 
			 * 		if schemaNode is not null here (indicating we have not exhausted the steps and could not find the node) find
			 *      the modelNode with the remaning steps in accessPath and the current schemaNode
			 */
			if (nextNode != null && changeSchemaNode != null){
				DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(m_schemaRegistry, changeSchemaNode.getPath().getParent());
				DataSchemaNode parentSchemaNode = context.getDataSchemaNode();
				returnValue.addAll(getParentModelNode(parentSchemaNode, nextNode, referencePath, steps));
			} else if (deleteNode && nextNode == null && changeSchemaNode != null){
				// could be a attribute in a deleted parent node, that is not part of the change
				steps = new Step[accessPathSteps.length -1 ];
				System.arraycopy(accessPathSteps, 1, steps, 0, steps.length);
				DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(m_schemaRegistry, changeSchemaNode.getPath().getParent());
				DataSchemaNode parentSchemaNode = context.getDataSchemaNode();
				returnValue.addAll(getParentModelNode(parentSchemaNode, changeEditNode, referencePath, steps));
			} else if (changeSchemaNode != null){
				ModelNode parentModelNode = getParentNode(changeSchemaNode.getPath(), changeEditNode);
				if (parentModelNode != null){
					returnValue.addAll(m_dsPathTraverser.getParentModelNodeWithAccessPath(changeEditNode, (DynaBean)parentModelNode.getValue(), accessPathSteps, true));
				}
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Reached the rootNode therefore returning the existing ModelNode list as changeSchemaNode will be null now");
				}
			}

		}
		return returnValue;
	}

	private LocationPath buildAccessPath(SchemaPath referencePath) {
		LocationPath accessPath;
		List<Step> inputPath = new LinkedList<Step>();
		DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(m_schemaRegistry, referencePath);
		DataStoreValidationUtil.buildAbsAccessPath(context.getSchemaRegistry(), context.getDataSchemaNode(), inputPath);
		accessPath = new LocationPath(true, inputPath.toArray(new Step[0]));
		return accessPath;
	}

	private void validateChanges(EditContainmentNode editTree, Set<SchemaPath> changeNodes, Map<SchemaPath, Collection<EditContainmentNode>> changeNodeMap, ModelNode rootModelNode,
								 Map<SchemaPath, SchemaRegistry> pathToRegistryMap, Map<EditContainmentNode, Map<String, Object>> editNodeToRegistryMap)
			throws ValidationException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Validating changes for {}", editTree);
		}
		Set<SchemaPath> changeNodesSet = new HashSet<SchemaPath>(changeNodes);
		Collection<SchemaPath> schemaPathToDelete = getValidationContext().getSchemaPathsToDelete();
		for (SchemaPath changePath:changeNodes){
			SchemaRegistry schemaRegistry = pathToRegistryMap.get(changePath);
			if ( schemaRegistry != null){
				SchemaRegistryUtil.setMountRegistry(schemaRegistry);
			}
			DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(m_schemaRegistry, changePath);
			DataSchemaNode changeSchemaNode = context.getDataSchemaNode();
			if (changeSchemaNode == null) {
				throw new ValidationException(String.format("DataSchemaNode is null for schemaPath %s", changePath));
			}
			int nodesToCreateCount = getValidationContext().nodesToCreateCount();
			for (EditContainmentNode changedNode:changeNodeMap.get(changeSchemaNode.getPath())){
				Map<String, Object> currentScope = editNodeToRegistryMap.get(changedNode);
				if ( currentScope != null && !currentScope.isEmpty()){
					SchemaRegistryUtil.setMountCurrentScope(currentScope);
				}

				// for each new expression evaluation, reset the single step count
				RequestScope.getCurrentScope().putInCache(DataStoreValidationUtil.SINGLE_STEP_COUNT, null);

				ValidationTimingLogger.startPhase("getParent");
				ModelNode parentNode = null;
				SchemaPath childPath = changeSchemaNode.getPath();
				parentNode = getParentNode(childPath, changedNode);

				boolean nodeNotDeleted = true;
				if (isNodeDeleted(changedNode, changeSchemaNode)){
					nodeNotDeleted = false;
				}

				if (parentNode == null){
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("parentNode is null for dataSchemaNode:{} and path:{}. is node not deleted?:{}",changeSchemaNode, changePath, nodeNotDeleted);
					}
					if (changeSchemaNode.getQName().equals(rootModelNode.getQName())) {
						parentNode = rootModelNode;
					}
				}
				ValidationTimingLogger.endPhase("getParent");

				ValidationTimingLogger.startPhase("validateChild");
				if (nodeNotDeleted) {
				    /*
				     * we dont want to validate a deleted node. 
				     */
					validateChildIfRequired(parentNode, changeSchemaNode);
				}
				ValidationTimingLogger.endPhase("validateChild");

				ValidationTimingLogger.startPhase("validateImpact");
				parentNode = validateImpact(changeNodeMap, rootModelNode, changeNodesSet, schemaPathToDelete, changePath, changeSchemaNode,
						nodesToCreateCount, changedNode, nodeNotDeleted, parentNode);
				ValidationTimingLogger.endPhase("validateImpact");

				ValidationTimingLogger.startPhase("checkForMandatoryNodes");
				if (!isNodeDeleted(changedNode)) {
					if (parentNode == null) {
						parentNode = rootModelNode;
					}
					// after validating all impact nodes for creation/deletion check for mandatory node existence
					checkForMandatoryNodes(parentNode, changeSchemaNode, getMatchCriteria(changeSchemaNode, changedNode), changeNodesSet);
				}
				ValidationTimingLogger.endPhase("checkForMandatoryNodes");

			}

		}
	}

	private ModelNode validateImpact(Map<SchemaPath, Collection<EditContainmentNode>> changeNodeMap, ModelNode rootModelNode,
									 Set<SchemaPath> changeNodesSet, Collection<SchemaPath> schemaPathToDelete, SchemaPath changePath,
									 DataSchemaNode changeSchemaNode, int nodesToCreateCount, EditContainmentNode changedNode, boolean nodeNotDeleted,
									 ModelNode parentNode) {
		try {
			DataStoreValidationUtil.getValidationContext().setImpactValidation(true);
			/**
			 * validate all the referenced nodes that are impacted by
			 * the changeNode
			 */
			Map<SchemaPath, Expression> impactedPaths = SchemaRegistryUtil.getSchemaRegistry(m_schemaRegistry).getReferencedNodesForSchemaPaths(changePath);
			if (schemaPathToDelete != null) {
				schemaPathToDelete.addAll(impactedPaths.keySet());
			}
			if (EditConfigOperations.REMOVE.equals(changedNode.getEditOperation())
					|| EditConfigOperations.DELETE.equals(changedNode.getEditOperation())) {
				if (changeSchemaNode instanceof ListSchemaNode || changeSchemaNode instanceof ContainerSchemaNode) {
					Map<SchemaPath, Expression> childImpactPaths = SchemaRegistryUtil
							.getSchemaRegistry(m_schemaRegistry).addChildImpactPaths(changePath);
					impactedPaths.putAll(childImpactPaths);
				}
			}
			for (Map.Entry<SchemaPath, Expression> impactedPath : impactedPaths.entrySet()) {
				boolean validate = false;
				SchemaPath key = impactedPath.getKey();
				boolean containsImpactPath = changeNodesSet.contains(key);
				if (containsImpactPath && isDeletedList(changeNodeMap, key)) {
					// one of the instances of the list is deleted, not necessarily all
					validate = true;
				} else if (!containsImpactPath) {
					validate = true;
				}
				if (validate) {
					DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(m_schemaRegistry, impactedPath.getKey());
					DataSchemaNode constraintNode = context.getDataSchemaNode();
					if (constraintNode != null) {
						if (constraintNode instanceof LeafSchemaNode) {
							Optional<?> defaultValue = ((LeafSchemaNode)constraintNode).getType().getDefaultValue();
							if (defaultValue.isPresent()) {
								// only if the leaf has a default, it is OK to create the leaf on when becoming true
								DataStoreValidationUtil.getValidationContext().getSchemaPathsToCreate().add(impactedPath.getKey());
							}
						}
						if (constraintNode instanceof ContainerSchemaNode) {
							Boolean nodeExists = false;
							if (!((ContainerSchemaNode) constraintNode).isPresenceContainer() && !nodeExists){
								// only if it is a non-presence container, it is
								// OK to create the container on when becoming true
								DataStoreValidationUtil.getValidationContext().getSchemaPathsToCreate().add(impactedPath.getKey());
							}
						}

						// For leaf-list, no default are defined
						// For list, a default key is never mostly defined.
					}
					if (parentNode == null) {
						parentNode = getParentNode(changeSchemaNode.getPath(), changedNode);
					}
					validateImpactedNode(rootModelNode, parentNode, changedNode, changeSchemaNode, key,
							(LocationPath) impactedPath.getValue(), nodeNotDeleted);
				}
			}
		} finally {
			schemaPathToDelete.clear();
			DataStoreValidationUtil.getValidationContext().setImpactValidation(false);
			DataStoreValidationUtil.getValidationContext().getSchemaPathsToCreate().clear();
		}
		return parentNode;
	}


	private boolean isDeletedList(Map<SchemaPath, Collection<EditContainmentNode>> changeNodeMap, SchemaPath referencePath) {
		if (changeNodeMap.containsKey(referencePath)) {
			DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(m_schemaRegistry, referencePath);
			DataSchemaNode schemaNode = context.getDataSchemaNode();
			if (schemaNode instanceof ListSchemaNode) {
				Collection<EditContainmentNode> editNodes = changeNodeMap.get(referencePath);
				for (EditContainmentNode editNode:editNodes) {
					if (!isNodeDeleted(editNode)){
						return false;
					}
				}
				return true;
			} else {
				// is its parent a list and deleted?
				context = SchemaRegistryUtil.getDataSchemaNode(m_schemaRegistry, referencePath.getParent());
				schemaNode = context.getDataSchemaNode();
				if (schemaNode instanceof ListSchemaNode) {
					Collection<EditContainmentNode> editNodes = changeNodeMap.get(referencePath);
					for (EditContainmentNode editNode:editNodes) {
						if (!isNodeDeleted(editNode)){
							return false;
						}
					}
					return true;
				}
			}
		}
		return false;
	}
	private Map<QName,ConfigLeafAttribute> getMatchCriteria(DataSchemaNode schemaNode, EditContainmentNode editNode){
		if (schemaNode instanceof ListSchemaNode) {
			// if it id a list, build a hashmap of all keys and values from EditContainmentNode
			List<EditMatchNode> matchNodes = editNode.getMatchNodes();
			SchemaRegistry schemaRegistry = m_schemaRegistry;
			Map<QName,ConfigLeafAttribute> matchCriteria = new HashMap<>();
			for (EditMatchNode matchNode:matchNodes) {
				SchemaPath childPath = schemaRegistry.getDescendantSchemaPath(schemaNode.getPath(), matchNode.getQName());
				if (childPath == null) {
					SchemaRegistry mountRegistry = SchemaRegistryUtil.getMountRegistry();
					schemaRegistry = mountRegistry != null ? mountRegistry : schemaRegistry;
					childPath = schemaRegistry.getDescendantSchemaPath(schemaNode.getPath(), matchNode.getQName());
				}
				LeafSchemaNode childNode = (LeafSchemaNode) schemaRegistry.getDataSchemaNode(childPath);
				ConfigLeafAttribute attribute = ConfigAttributeFactory.getConfigLeafAttribute(schemaRegistry, childNode , matchNode.getValue());
				matchCriteria.put(matchNode.getQName(), attribute);
			}

			return matchCriteria;
		}

		return Collections.emptyMap();
	}

	private void buildAllChangeSchemaPaths(SchemaPath parentPath, EditContainmentNode editTree, Set<SchemaPath> changeNodesPath, Map<SchemaPath, Collection<EditContainmentNode>> changeNodeMap,
										   SchemaRegistry schemaRegistry, Map<SchemaPath, SchemaRegistry> pathToRegistryMap, Map<EditContainmentNode, Map<String, Object>> mountCurrentScopeMap, Map<String, Object> mountCurrentScope) throws DataStoreException, GetAttributeException{
		// cache if the node is deleted
		cacheDeleteChangNode(editTree);

		boolean nodeAdded = false;
		boolean isMountPath = false;
		/**
		 * For every changeNode in a EditContainmentNode, record the schemaPath for the change and also the parent edit node.
		 * We need the parent Edit node to identify the right list/leaf list model nodes. 
		 */
		for (EditChangeNode changeNode:editTree.getChangeNodes()){
			DSValidationMountContext mountContext = DataStoreValidationUtil.populateChildSchemaPath(schemaRegistry, parentPath,
					changeNode.getQName());
			schemaRegistry = mountContext.getSchemaRegistry();
			SchemaPath childPath = mountContext.getSchemaPath();
			pathToRegistryMap.put(childPath, schemaRegistry);
			addToChangeList(editTree, changeNodesPath, changeNodeMap, childPath);
			nodeAdded = true;
		}

		for (EditContainmentNode childNode: editTree.getChildren()){
			DSValidationMountContext mountContext = DataStoreValidationUtil.populateChildSchemaPath(schemaRegistry, parentPath,
					childNode);
			if (! schemaRegistry.equals(mountContext.getSchemaRegistry())){
				isMountPath = true;
			}
			schemaRegistry = mountContext.getSchemaRegistry();
			if ( mountContext.getMountCurrentScope() != null){
				mountCurrentScope = mountContext.getMountCurrentScope();
			}
			SchemaPath childPath = mountContext.getSchemaPath();
			pathToRegistryMap.put(childPath, schemaRegistry);
			mountCurrentScopeMap.put(childNode, mountCurrentScope);
			buildAllChangeSchemaPaths(childPath, childNode, changeNodesPath, changeNodeMap, schemaRegistry, pathToRegistryMap, mountCurrentScopeMap, mountCurrentScope);
		}

		if (nodeAdded || !editTree.getEditOperation().equalsIgnoreCase(EditConfigOperations.NONE)) {
			if ( isMountPath && schemaRegistry.getParentRegistry() != null){ // Resetting schema registry to parent registry (for mount node, global registry need to be taken
				schemaRegistry = schemaRegistry.getParentRegistry();
			}
			// if there is already a node added, we will validate the entire editNode
			// if no node is added so far, if there is an operation on the edit node itself,
			// then add the match node and container
			for (EditMatchNode matchNode:editTree.getMatchNodes()){
				DSValidationMountContext mountContext = DataStoreValidationUtil.populateChildSchemaPath(schemaRegistry, parentPath,
						matchNode.getQName());
				schemaRegistry = mountContext.getSchemaRegistry();
				SchemaPath childPath = mountContext.getSchemaPath();
				addToChangeList(editTree, changeNodesPath, changeNodeMap, childPath);
			}

			/**
			 * Record the schemaPath of EditContainmentNode. We need to check for the integrity at every container/list
			 * level after the change if this has undergone a change
			 */
			DSValidationMountContext mountContext = DataStoreValidationUtil.populateChildSchemaPath(schemaRegistry, parentPath.getParent(),
					editTree.getQName());
			SchemaPath editNodePath = mountContext.getSchemaPath();
			if (editNodePath != null){
				addToChangeList(editTree, changeNodesPath, changeNodeMap, editNodePath);
			} else if (parentPath.getParent().getLastComponent() == null){
				// indicates it is a root node
				addToChangeList(editTree, changeNodesPath, changeNodeMap, parentPath);
			}
		}
	}

	private Map<String,ModelNode> getFromModelNodeCache(SchemaPath schemaPath) {
		return getValidationContext().getModelNodes(schemaPath);
	}


	private void addToModelNodeCache(Collection<ModelNode> modelNodes, SchemaPath schemaPath) {
		for (ModelNode modelNode:modelNodes) {
			getValidationContext().addToModelNodeCache(schemaPath, modelNode);
		}
	}

	private void addToMountPathModelNodeIdCache(SchemaPath schemaPath, Collection<ModelNode> modelNodes) {
		for (ModelNode modelNode:modelNodes) {
			getValidationContext().addToMountPathModelNodeIdCache(schemaPath, modelNode.getModelNodeId());
		}
	}

	private void addToChangeList(EditContainmentNode editTree, Set<SchemaPath> changeNodesPath, Map<SchemaPath, Collection<EditContainmentNode>> changeNodeMap, SchemaPath childPath) {
		changeNodesPath.add(childPath);
		Collection<EditContainmentNode> containmentNodes = changeNodeMap.get(childPath);
		if (containmentNodes == null){
			containmentNodes = new HashSet<EditContainmentNode>();
			changeNodeMap.put(childPath, containmentNodes);
		}
		containmentNodes.add(editTree);
	}

	@Override
	public boolean validateChild(ModelNode parentModelNode, DataSchemaNode childSchemaNode) throws ValidationException {
		boolean validation = true;
		// first augmentation if any has to be validated
		validation = m_augmentValidator.evaluate(parentModelNode, childSchemaNode);
		if (validation) {
			// evaluate node constraints if any -- must/when
			validation = m_constraintValidator.evaluate(parentModelNode, childSchemaNode);

			// evaluate type specific validation
			if (validation) {
				DataStoreConstraintValidator validator = DataStoreConstraintValidatorFactory.getInstance().getValidator(childSchemaNode,
						m_schemaRegistry, m_modelNodeHelperRegistry, m_expValidator);
				if (validator != null && parentModelNode != null) {
					validator.validate(parentModelNode);
				}
			}
		}
		return validation;
	}

	private void checkForMandatoryNodes(ModelNode parentModelNode, DataSchemaNode childSchemaNode, Map<QName,ConfigLeafAttribute> matchCriteria, Collection<SchemaPath> changeNodeSet) {
		try{
			DataStoreValidationUtil.getValidationContext().setAsMandatoryNodesCheck(true);
			if (childSchemaNode instanceof ListSchemaNode) {
				// We only have a matchCriteria and a schemaNode.
				// So we need to fetch the right modelNode that was modified as part of
				// edit request and then validate for missing mandatory nodes

				// Since the modelNode is part of the Validation context, the model node and all impacted children will
				// be already available in DynaBean cache. We have to validate only those that were impacted in this request.
				Collection<ModelNode> modelNodes = DataStoreValidationUtil.getChildListModelNodes(parentModelNode, childSchemaNode, matchCriteria);
				for (ModelNode modelNode:modelNodes) {
					boolean match = true;
					for (Map.Entry<QName,ConfigLeafAttribute> entry:matchCriteria.entrySet()) {
						ConfigLeafAttribute nodeAttribute = ((ModelNodeWithAttributes)modelNode).getAttribute(entry.getKey());
						if (!nodeAttribute.equals(entry.getValue())) {
							match = false;
							break;
						}
					}

					if (match) {
						validateMissingChildren((ModelNodeWithAttributes) modelNode, changeNodeSet);
						break;
					}
				}
			} else if (childSchemaNode instanceof ContainerSchemaNode) {
				ModelNode modelNode = DataStoreValidationUtil.getChildContainerModelNode(parentModelNode, childSchemaNode);
				if (modelNode == null && childSchemaNode.getPath().getParent().getLastComponent() == null) {
					//indicate it ia root node
					QName qName = childSchemaNode.getQName();
					Collection<ModelNode> rootNodes = getValidationContext().getRootNodesOfType(qName.getNamespace().toString(), qName.getLocalName());
					for (ModelNode rootNode:rootNodes) {
						if (rootNode.getModelNodeSchemaPath().equals(childSchemaNode.getPath())) {
							modelNode = rootNode;
							break;
						}
					}

				}
				if(modelNode != null) {
					validateMissingChildren((ModelNodeWithAttributes) modelNode, changeNodeSet);
				}
			} else {
				// not a list or container
				if (changeNodeSet.contains(childSchemaNode.getPath())) {
					validateMissingChild((ModelNodeWithAttributes) parentModelNode, childSchemaNode, changeNodeSet, m_schemaRegistry);
				}

			}
		} catch (ModelNodeGetException e) {
			throw new ValidationException(e);
		}
		finally {
			DataStoreValidationUtil.getValidationContext().setAsMandatoryNodesCheck(false);
		}
	}

	private boolean isNodeForCreateOrDelete(ModelNode modelNode, DataSchemaNode child) {
		Map<ModelNode, Collection<QName>> createNodes = getValidationContext().getMergeList();
		Map<ModelNode, Collection<QName>> deleteNodes = getValidationContext().getDeleteList();

		for (Map.Entry<ModelNode, Collection<QName>> createEntry : createNodes.entrySet()) {
			ModelNode entryModelNode = createEntry.getKey();
			if (entryModelNode.getModelNodeId().equals(modelNode.getModelNodeId())
					|| entryModelNode.getModelNodeSchemaPath().equals(child.getPath())) {
				for (QName qnameInList : createEntry.getValue()) {
					if (qnameInList.equals(child.getQName())) {
						return true;
					} else if (qnameInList.equals(modelNode.getQName())) {
						return true;
					}
				}
			}
		}

		for (Map.Entry<ModelNode, Collection<QName>> deleteEntry : deleteNodes.entrySet()) {
			ModelNode entryModelNode = deleteEntry.getKey();
			if (entryModelNode.getModelNodeId().equals(modelNode.getModelNodeId())
					|| entryModelNode.getModelNodeSchemaPath().equals(child.getPath())) {
				for (QName qnameInList : deleteEntry.getValue()) {
					if (qnameInList.equals(child.getQName())) {
						return true;
					} else if (qnameInList.equals(modelNode.getQName())) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean isNonPresenceContainerWithDefaults(ModelNode modelNode) {
		SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
		DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(m_schemaRegistry, schemaPath);
		ContainerSchemaNode schemaNode = (ContainerSchemaNode) context.getDataSchemaNode();
		if (!schemaNode.isPresenceContainer()) {
			Collection<DataSchemaNode> children = context.getSchemaRegistry().getChildren(schemaPath);
			for (DataSchemaNode child : children) {
				if (SchemaRegistryUtil.hasDefaults(child)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void validateMissingChildren(ModelNodeWithAttributes modelNode, Collection<SchemaPath> changeNodeSet) throws ModelNodeGetException {
		SchemaPath schemaPath = modelNode.getModelNodeSchemaPath();
		SchemaRegistry schemaRegistry = m_schemaRegistry;
		Collection<DataSchemaNode> children = m_schemaRegistry.getChildren(schemaPath);
		if (m_schemaRegistry.getDataSchemaNode(schemaPath) == null & SchemaRegistryUtil.isMountPointEnabled()) {
			if (SchemaRegistryUtil.getMountRegistry() != null) {
				schemaRegistry = SchemaRegistryUtil.getMountRegistry();
				children = schemaRegistry.getChildren(schemaPath);
			}
		}
		for (DataSchemaNode child : children) {
			if (!isNodeForCreateOrDelete(modelNode, child)) {
				// If it is not already identified for create/delete dont validate it,
				// it will be validated in the next cycle when it is created.
				// We dont want to validate a delete container for mandatory node

				boolean shouldCheckForMissingChild = true;
				if ((child instanceof ContainerSchemaNode || child instanceof ListSchemaNode) && changeNodeSet.contains(child.getPath())) {
					shouldCheckForMissingChild = false;
				}

				if (shouldCheckForMissingChild) {
					validateMissingChild(modelNode, child, changeNodeSet, schemaRegistry);
				}
			}
		}

	}

	@SuppressWarnings( "rawtypes" )
	protected void validateMissingChild(ModelNodeWithAttributes modelNode, DataSchemaNode child, Collection<SchemaPath> changeNodeSet, SchemaRegistry schemaRegistry) throws ModelNodeGetException {
		try{
			DataStoreValidationUtil.getValidationContext().getSchemaPathsToDelete().add(child.getPath());
			QName childQName = child.getQName();
			boolean proxyModelNode = modelNode instanceof ProxyValidationModelNode;
			if (!child.isConfiguration()) {
				return;
			} else if (child instanceof ContainerSchemaNode) {
				ModelNode childModelNode = DataStoreValidationUtil.getChildContainerModelNode(modelNode, child);

				if (((ContainerSchemaNode) child).isPresenceContainer() && childModelNode != null && !proxyModelNode) {
					validateMissingChildren((ModelNodeWithAttributes) childModelNode, changeNodeSet);
				} else if (!((ContainerSchemaNode) child).isPresenceContainer()) {
					if (childModelNode == null) {
						boolean valid = true;
						try{
							valid = validateChild(modelNode,child);
						} catch (ValidationException e) {
							valid = false;
							logDebug(null, "Mandatory validation on child {} for modelNode{} will not be processed further. ProxyModelNode {}",
									child, modelNode, proxyModelNode);
						}

						// if the container is not created yet and it is not a presence container,
						// create a validation container and check for any mandatory nodes inside it
						if (valid) {
							// it is ok to validate and expect further validation on this non-presence container
							childModelNode = new ProxyValidationModelNode(modelNode, m_modelNodeHelperRegistry, child.getPath());
							validateMissingChildren((ModelNodeWithAttributes) childModelNode, changeNodeSet);
						}
					} else if (isNonPresenceContainerWithDefaults(childModelNode)) {
						// indicates this is a non presence container with default leafs
						// and created when the parent container was created. Not necessarily was created with all mandatory nodes
						validateMissingChildren((ModelNodeWithAttributes) childModelNode, changeNodeSet);
					} else {

						logDebug("modelNode {} already exists. It is already validated when it was last modified",
								childModelNode.getModelNodeId());
					}
				}

			} else {
				logDebug("Not a container node so go through validataion for modelNode {} child {}", modelNode.getModelNodeId(),
						child.getQName());
				boolean mustWhen = DataStoreValidationUtil.containsMustWhen(schemaRegistry, child);

				if (child instanceof LeafSchemaNode) {
					// if it is a leaf node, and constraint is mandatory
					// if it has a when/must validate and check for its existance
					if (((LeafSchemaNode)child).isMandatory()) {
						if (mustWhen) {
							boolean validateMustWhen = validateChildIfRequired(modelNode, child);
							// If mandatory leaf is missing then thrown an exception
							if(validateMustWhen && modelNode.getAttribute(childQName) == null){
								DataStoreValidationErrors.throwDataMissingException(schemaRegistry, modelNode, childQName);
							}
						} else {
							if (modelNode.getAttribute(childQName) == null) {
								DataStoreValidationErrors.throwDataMissingException(schemaRegistry, modelNode, childQName);
							}
						}
					} else if (((LeafSchemaNode) child).getType().getDefaultValue().isPresent() && !AnvExtensions.IGNORE_DEFAULT.isExtensionIn(child)) {
						// if this leaf has defaults, is missing and not identified for create
						// throw exception
						if (mustWhen && modelNode.getAttribute(childQName) == null) {
							Map<ModelNode, Collection<QName>> nodesToCreate = getValidationContext().getMergeList();
							if (!hasChild(nodesToCreate, modelNode, childQName)) {
								// in case if the constraints on the node becomes true and it has a default, create it.
								Collection<SchemaPath> schemaPathsToCreate = DataStoreValidationUtil.getValidationContext().getSchemaPathsToCreate();
								boolean addPathToCreate = true;
								try{
									validateChildIfRequired(modelNode, child);
								}catch(ValidationException e) {
									logDebug("child {} cannot exists under modelNode {}", child, modelNode);
									addPathToCreate = false;
								}
								if (addPathToCreate) {
									schemaPathsToCreate.add(child.getPath());
								}
							}
						} else if (modelNode.getAttribute(childQName) == null) {

							Map<ModelNode, Collection<QName>> nodesToCreate = getValidationContext().getMergeList();
							Collection<QName> childQNames = nodesToCreate.get(modelNode);
							if (childQNames == null) {
								childQNames = new LinkedHashSet<QName>();
								nodesToCreate.put(modelNode, childQNames);
							}
							childQNames.add(childQName);
							getValidationContext().recordDefaultValue(child.getPath(), modelNode, ((LeafSchemaNode) child).getType().getDefaultValue().orElse(null));
						}
					}
				} else if (child instanceof ChoiceSchemaNode) {
					m_choiceCaseValidator.validateMandatoryChoiceChildren(modelNode, (ChoiceSchemaNode) child);
				} else {
					ElementCountConstraint elementCountConstraint = null;
					if (child instanceof ElementCountConstraintAware) {
						Optional<ElementCountConstraint> optElementCountConstraint = ((ElementCountConstraintAware) child).getElementCountConstraint();
						if (optElementCountConstraint.isPresent()) {
							elementCountConstraint = optElementCountConstraint.get();
						}
					}
					if (elementCountConstraint != null && elementCountConstraint.getMinElements() != null && elementCountConstraint.getMinElements() > 0) {
						int minElements = elementCountConstraint.getMinElements();
						if (!mustWhen) {
							if (child instanceof LeafListSchemaNode) {
								Set leafLists = modelNode.getLeafList(childQName);
								if (leafLists == null || leafLists.isEmpty() || leafLists.size() < minElements) {
									ValidationException exception = DataStoreValidationErrors.getViolateMinElementException(childQName.getLocalName(), minElements);
									ModelNodeId errorId = new ModelNodeId(modelNode.getModelNodeId());
									errorId.addRdn(ModelNodeRdn.CONTAINER, childQName.getNamespace().toString(), childQName.getLocalName());
									exception.getRpcError().setErrorPath(errorId.xPathString(schemaRegistry), errorId.xPathStringNsByPrefix(schemaRegistry));
									throw exception;
								}
							} else if (child instanceof ListSchemaNode) {
								Collection listNodes = DataStoreValidationUtil.getChildListModelNodes(modelNode, child);

								if (listNodes == null || listNodes.isEmpty() || listNodes.size() < minElements) {
									ValidationException exception = DataStoreValidationErrors.getViolateMinElementException(childQName.getLocalName(), elementCountConstraint.getMinElements());
									ModelNodeId errorId = new ModelNodeId(modelNode.getModelNodeId());
									errorId.addRdn(ModelNodeRdn.CONTAINER, childQName.getNamespace().toString(), childQName.getLocalName());
									exception.getRpcError().setErrorPath(errorId.xPathString(schemaRegistry), errorId.xPathStringNsByPrefix(schemaRegistry));
									throw exception;
								}
							}
						} else {
							if (!proxyModelNode) {
								boolean validateWhen = validateChildIfRequired(modelNode, child);
								if(validateWhen && child instanceof LeafListSchemaNode){
									Set leafLists = modelNode.getLeafList(childQName);
									// If min-elements leaf list is missing, then thrown an exception
									if (leafLists == null || leafLists.isEmpty() || leafLists.size() < minElements) {
										ValidationException exception = DataStoreValidationErrors.getViolateMinElementException(childQName.getLocalName(), minElements);
										ModelNodeId errorId = new ModelNodeId(modelNode.getModelNodeId());
										errorId.addRdn(ModelNodeRdn.CONTAINER, childQName.getNamespace().toString(), childQName.getLocalName());
										exception.getRpcError().setErrorPath(errorId.xPathString(schemaRegistry), errorId.xPathStringNsByPrefix(schemaRegistry));
										throw exception;
									}
								}
							} else {
								DataStoreValidationErrors.throwDataMissingException(schemaRegistry, modelNode, childQName);
							}
						}
					}
				}

			}
		}finally {
			DataStoreValidationUtil.getValidationContext().getSchemaPathsToDelete().clear();
		}
	}

	private ModelNode getParentNode(SchemaPath childPath, EditContainmentNode childEditNode)  {
		ModelNode returnValue = null;
		DSValidationMountContext context = SchemaRegistryUtil.getDataSchemaNode(m_schemaRegistry, childPath);
		DataSchemaNode childNode = context.getDataSchemaNode();
		DataSchemaNode parentNode = SchemaRegistryUtil.getEffectiveParentNode(childNode, m_schemaRegistry);
		if (parentNode != null){
			Map<String,ModelNode> possibleParentNodes = getFromModelNodeCache(parentNode.getPath());
			if (possibleParentNodes != null && !possibleParentNodes.isEmpty()) {
				EditContainmentNode parentEditNode = null;
				if (childNode instanceof LeafSchemaNode || childNode instanceof LeafListSchemaNode){
					parentEditNode = childEditNode;
				} else {
					parentEditNode = childEditNode.getParent();
				}

				returnValue = possibleParentNodes.get(parentEditNode.getModelNodeId().xPathString());
			}

			if (returnValue == null ){
				if (childNode instanceof LeafSchemaNode || childNode instanceof LeafListSchemaNode) {
					/**
					 * What do we get here?
					 * container c {
					 *    list a {
					 *      leaf b;
					 *    }
					 * }
					 *
					 * If childPath is 'leaf b', we will have EditContainmentNode of 'list a'.
					 *
					 * So we need to find the ModelNode of 'container c' and then get the ModelNode of 'list a' from 'container c'
					 */
					SchemaPath parentPath = SchemaRegistryUtil.getEffectiveParentNode(childNode, m_schemaRegistry).getPath();
					ModelNode grandParentModelNode = getParentNode(parentPath, childEditNode);
					if (grandParentModelNode != null && grandParentModelNode.getModelNodeId().equals(childEditNode.getParent().getModelNodeId())){
						Map<String,ModelNode> modelNodesMap = getFromModelNodeCache(parentPath);
						returnValue = matchModelNode(modelNodesMap, childEditNode);
						if (returnValue == null) {
							if (m_modelNodeDSM.isChildTypeBigList(parentPath)) {
								ModelNodeKey modelNodeKey = MNKeyUtil.getModelNodeKey(m_schemaRegistry, parentPath, childEditNode.getModelNodeId());
								returnValue = m_modelNodeDSM.findNode(parentPath, modelNodeKey, grandParentModelNode.getModelNodeId());
							} else {
								List<ModelNode> modelNodes = new ArrayList<>();
								modelNodes = m_modelNodeDSM.listChildNodes(parentPath, grandParentModelNode.getModelNodeId());
								if (modelNodes!=null && !modelNodes.isEmpty()) {
									addToModelNodeCache(modelNodes, modelNodes.get(0).getModelNodeSchemaPath());
								}
								if (modelNodes!=null && !modelNodes.isEmpty() && isNodeDeleted(childEditNode)) {
									// we are here when the actual child is deleted which impacts any other nodes of same type
									// instances of same type - yang list. So every such node will have
									// same path, lets validate one of them.
									returnValue = modelNodes.iterator().next();
								} else {
									modelNodesMap = getFromModelNodeCache(parentPath);
									returnValue = matchModelNode(modelNodesMap, childEditNode);
								}
							}
						}
					}

				} else {
					SchemaPath parentPath = parentNode.getPath();
					ModelNode parentModelNode = getParentNode(parentPath, childEditNode.getParent());
					if (parentModelNode != null) {
						if (!parentModelNode.getModelNodeId().equals(childEditNode.getParent().getModelNodeId())
								&& parentModelNode.getModelNodeId().equals(childEditNode.getParent().getParent().getModelNodeId())) {
							if (m_modelNodeDSM.isChildTypeBigList(parentPath)) {
								ModelNodeKey modelNodeKey = MNKeyUtil.getModelNodeKey(m_schemaRegistry, parentPath, childEditNode.getParent().getModelNodeId());
								returnValue = m_modelNodeDSM.findNode(parentPath, modelNodeKey, parentModelNode.getModelNodeId());
							}
							else {
								List<ModelNode> parentModelNodes = m_modelNodeDSM.listChildNodes(parentPath, parentModelNode.getModelNodeId());
								returnValue = matchModelNode(parentModelNodes, childEditNode.getParent());
								if (!parentModelNodes.isEmpty()) {
									addToModelNodeCache(parentModelNodes, parentModelNodes.get(0).getModelNodeSchemaPath());
									// Cache mountpath and it's modelNode Id
									if (SchemaRegistryUtil.isMountPointEnabled()
											&& SchemaRegistryUtil.getMountRegistry() != null
											&& SchemaRegistryUtil.getMountRegistry().getMountPath().equals(parentModelNodes.get(0).getModelNodeSchemaPath())) {
										addToMountPathModelNodeIdCache(parentModelNodes.get(0).getModelNodeSchemaPath(), parentModelNodes);
									}
								}
							}
						} else if (parentModelNode.getModelNodeId().equals(childEditNode.getParent().getModelNodeId())) {
							returnValue = parentModelNode;
						}
					}
				}

			}
		}
		return returnValue;

	}

	private ModelNode matchModelNode(Map<String,ModelNode> modelNodes, EditContainmentNode editNode){
		if (modelNodes != null){
			ModelNode returnValue = modelNodes.get(editNode.getModelNodeId().xPathString());
			if (returnValue != null) {
				return returnValue;
			}
		}

		return null;
	}

	private ModelNode matchModelNode(List<ModelNode> modelNodes, EditContainmentNode editNode){
		if (modelNodes != null){
			for (ModelNode modelNode:modelNodes){
				if (modelNode.getModelNodeId().xPathString().equals(editNode.getModelNodeId().xPathString())){
					return modelNode;
				}
			}
		}
		return null;
	}

	/*
	This method prevents duplicate calls to validate same leaf node under a parent Model node
     */
	private boolean validateChildIfRequired(ModelNode modelNode, DataSchemaNode childSchemaNode) {
		boolean result = false;
		if (childSchemaNode instanceof LeafSchemaNode) {
			Pair<ModelNode, DataSchemaNode> pairModelAndDataNode = new Pair<ModelNode, DataSchemaNode>(modelNode, childSchemaNode);
			/**
			 * Here we should check the cache whether childSchemaNode is already validated or not.
			 *
			 * If it is not validated already,
			 * 		then we should validate the childSchemaNode and cache it along with validation result.
			 * Else
			 * 		we should get validation result of childSchemaNode from cache and return it.
			 */
			boolean isAlreadyValidated = DataStoreValidationUtil.getValidationContext().getValidatedChilds().containsKey(pairModelAndDataNode);
			if (!isAlreadyValidated) {
				result = validateChild(modelNode, childSchemaNode);
				// Add child schemanode and it's validation result in cache
				DataStoreValidationUtil.getValidationContext().getValidatedChilds().put(pairModelAndDataNode, result);
			} else {
				// we should return the result of already validated child
				result = DataStoreValidationUtil.getValidationContext().getValidatedChilds().get(pairModelAndDataNode);
				//only for UT
				m_validatedChildCacheHitStatus = true;
			}
		} else {
			result = validateChild(modelNode, childSchemaNode);
		}
		return result;
	}

	@Override
	public DSExpressionValidator getValidator() {
		return m_expValidator;
	}

	private DSValidationContext getValidationContext() {
		return DataStoreValidationUtil.getValidationContext();
	}

	//For UT only
	public boolean getValidatedChildCacheHitStatus() {
		return m_validatedChildCacheHitStatus;
	}

	public void setValidatedChildCacheHitStatus(boolean validatedChildCacheHitStatus) {
		this.m_validatedChildCacheHitStatus = validatedChildCacheHitStatus;
	}
}
