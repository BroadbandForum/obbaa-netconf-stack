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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import java.util.HashSet;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.AugmentationTarget;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UsesNode;

import com.google.common.collect.Iterables;

public class SchemaContextUtil {

	private static GroupingDefinition findGroupingSchemaNode(SchemaRegistry schemaRegistry, UsesNode usesNode) {
		SchemaPath groupingPath = usesNode.getGroupingPath();
		QName groupingModuleQName = groupingPath.getPathFromRoot().iterator().next();
		Module module = schemaRegistry.getModuleByNamespace(groupingModuleQName.getNamespace().toString());
		GroupingDefinition groupingSchemaNode = findGroupingInDataNodeContainer(module, groupingPath.getPathFromRoot());
		if (groupingSchemaNode == null) {
			throw new RuntimeException("Grouping schema node not found for grouping path " + groupingPath);
		}
		return groupingSchemaNode;
	}

	private static GroupingDefinition findGroupingInDataNodeContainer(DataNodeContainer container, Iterable<QName> path) {
		if (path.iterator().hasNext()) {
			QName currentQName = path.iterator().next();
			for (GroupingDefinition definedGrouping: container.getGroupings()) {
				if (definedGrouping.getQName().equals(currentQName)) return definedGrouping;
			}
			Iterable<QName> nextPath = Iterables.skip(path, 1);
			for (DataSchemaNode childNode : container.getChildNodes()) {
				if (childNode instanceof DataNodeContainer && childNode.getQName().equals(currentQName)) {
					GroupingDefinition foundGrouping = findGroupingInDataNodeContainer((DataNodeContainer)childNode, nextPath);
					if (foundGrouping != null) {
						return foundGrouping;
					}
				}
			}
			for (GroupingDefinition grouping: container.getGroupings()) {
				GroupingDefinition foundGrouping = findGroupingInDataNodeContainer(grouping, nextPath);
				if (foundGrouping != null) {
					return foundGrouping;
				}
			}
			if (container instanceof Module) {
				Module module = (Module) container;
				for (NotificationDefinition notification: module.getNotifications()) {
					GroupingDefinition foundGrouping = findGroupingInDataNodeContainer(notification, nextPath);
					if (foundGrouping != null) {
						return foundGrouping;                    
					}
				}
				for (RpcDefinition rpc: module.getRpcs()) {
					GroupingDefinition foundGrouping = findGroupingInRpc(rpc, nextPath);
					if (foundGrouping != null) {
						return foundGrouping;                                                            
					}
				}
			}
		}
		return null;
	}

	private static GroupingDefinition findGroupingInRpc(RpcDefinition rpc, Iterable<QName> path) {
		if (path.iterator().hasNext()) {
			QName currentQName = path.iterator().next();
			for (GroupingDefinition definedGrouping: rpc.getGroupings()) {
				if (definedGrouping.getQName().equals(currentQName)) return definedGrouping;
			}
			Iterable<QName> nextPath = Iterables.skip(path, 1);
			{
				GroupingDefinition foundGrouping = findGroupingInDataNodeContainer(rpc.getInput(), nextPath);
				if (foundGrouping != null) {
					return foundGrouping;
				}
			}
			{
				GroupingDefinition foundGrouping = findGroupingInDataNodeContainer(rpc.getOutput(), nextPath);
				if (foundGrouping != null) {
					return foundGrouping;
				}
			}
			for (GroupingDefinition grouping: rpc.getGroupings()) {
				GroupingDefinition foundGrouping = findGroupingInDataNodeContainer(grouping, nextPath);
				if (foundGrouping != null) {
					return foundGrouping;
				}
			}
		}
		return null;
	}

	public static Pair<UsesNode,SchemaPath> getUsesSchema(SchemaRegistry schemaRegistry, DataSchemaNode parentSchemaNode, DataSchemaNode child) {
		if (parentSchemaNode instanceof DataNodeContainer) {
			Set<UsesNode> usesAndAugUsesNodes = new HashSet<>();
			Set<UsesNode> usesNodes = ((DataNodeContainer) parentSchemaNode).getUses();
			usesAndAugUsesNodes.addAll(usesNodes);
			Pair<AugmentationSchemaNode,SchemaPath> augsNodePair = DataStoreValidationUtil.getAugmentationSchema(schemaRegistry, parentSchemaNode, child);
			if(augsNodePair != null){
				AugmentationSchemaNode augSchema = augsNodePair.getFirst();
				usesAndAugUsesNodes.addAll(augSchema.getUses());
			}
			UsesNode matchedUses = getMatchedUsesSchema(usesAndAugUsesNodes, child, schemaRegistry);
			if(matchedUses == null){
				DataSchemaNode parentNode = schemaRegistry.getNonChoiceParent(parentSchemaNode.getPath());
				return getUsesSchema(schemaRegistry, parentNode, parentSchemaNode);
			}
			if(ChoiceCaseNodeUtil.isChoiceOrCaseNode(parentSchemaNode)){
				parentSchemaNode = schemaRegistry.getNonChoiceParent(parentSchemaNode.getPath());
			}
			return new Pair<UsesNode, SchemaPath>(matchedUses, parentSchemaNode.getPath());
		}
		return null;
	}

	private static UsesNode getMatchedUsesSchema(Set<UsesNode> usesNodes, DataSchemaNode child, SchemaRegistry schemaRegistry){
		for (UsesNode uses : usesNodes) {
			GroupingDefinition groupingSchemaNode = findGroupingSchemaNode(schemaRegistry, uses);
			Set<UsesNode> innerUsesNodes = groupingSchemaNode.getUses();
			boolean isMatchedFound = false;
			if(!innerUsesNodes.isEmpty()){
				UsesNode matchedInnerUsesNode = getMatchedUsesSchema(innerUsesNodes, child, schemaRegistry);
				if(matchedInnerUsesNode != null){
					isMatchedFound = true;
					return matchedInnerUsesNode;
				}
			}
			if(!isMatchedFound) {
				for(DataSchemaNode childNode: groupingSchemaNode.getChildNodes()){
					if(childNode.getQName().equals(child.getQName())) {
						return uses;
					}
				}
			}
		}
		return null;
	}
}
