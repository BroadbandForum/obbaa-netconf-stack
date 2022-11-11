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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeChangeType;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

/**
 * Used only for UTs
 */
public class LocalSubSystem extends AbstractSubSystem {

	private Map<SchemaPath, List<ChangeTreeNode>> m_changesMap = new HashMap<>();

	private EditContainmentNode m_editContainmentNode;
	private Map<String, ModelNodeId> modelNodeIdMap =  new HashMap<>();
	public LocalSubSystem(){

	}

	@Override
	protected boolean byPassAuthorization() {
		return true;
	}

	@Override
	public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes) throws GetAttributeException {
		return new HashMap<>();
	}

	@Override
	public void postCommit(Map<SchemaPath, List<ChangeTreeNode>> changesMap) {
		clearChanges();
		m_changesMap.putAll(changesMap);
	}

	public void clearChanges() {
		m_changesMap.clear();
	}

	public Map<SchemaPath, List<ChangeTreeNode>> getChangesMap(){
		return m_changesMap;
	}

	public void assertChangeTreeNodeForGivenSchemaPath(SchemaPath schemaPath, List<String> changeTreeNodes) {
		boolean changeFound;
		List<ChangeTreeNode> changeTreeNodeList = m_changesMap.get(schemaPath);
		if (changeTreeNodeList != null && !changeTreeNodeList.isEmpty()) {
			for (String ctn : changeTreeNodes) {
				changeFound = false;
				List<String> changeTreeNodePrints = new ArrayList<>();
				for (ChangeTreeNode changeTreeNode : changeTreeNodeList) {
					changeTreeNodePrints.add(changeTreeNode.print());
					if (changeTreeNode.print().equals(ctn)) {
						changeFound = true;
						break;
					}
				}
				if (!changeFound) {
					fail("Specified ChangeTreeNodes Not found.\n\nExpected ChangeTreeNodes:\n\n" + changeTreeNodes.toString() + "\n\nActual ChangeTreeNodes:\n\n" + changeTreeNodePrints.toString());
				}
			}
		} else {
			throw new AssertCTNException("ChangeTreeNodes not found for specified schema path", schemaPath);
		}
		clearChanges();
	}

	public void assertChangeTreeNodeWithoutClearingChanges(SchemaPath schemaPath, List<String> changeTreeNodes) {
		boolean changeFound;
		List<ChangeTreeNode> changeTreeNodeList = m_changesMap.get(schemaPath);
		if (changeTreeNodeList != null && !changeTreeNodeList.isEmpty()) {
			for (String ctn : changeTreeNodes) {
				changeFound = false;
				List<String> changeTreeNodePrints = new ArrayList<>();
				for (ChangeTreeNode changeTreeNode : changeTreeNodeList) {
					changeTreeNodePrints.add(changeTreeNode.print());
					if (changeTreeNode.print().equals(ctn)) {
						changeFound = true;
						break;
					}
				}
				if (!changeFound) {
					fail("Specified ChangeTreeNodes Not found.\n\nExpected ChangeTreeNodes:\n\n" + changeTreeNodes.toString() + "\n\nActual ChangeTreeNodes:\n\n" + changeTreeNodePrints.toString());
				}
			}
		} else {
			throw new AssertCTNException("ChangeTreeNodes not found for specified schema path", schemaPath);
		}
	}

	public void assertChangeTreeNodeForGivenSchemaPathIsAbsent(SchemaPath schemaPath) {
		List<ChangeTreeNode> changeTreeNodeList = m_changesMap.get(schemaPath);
		if (changeTreeNodeList != null && !changeTreeNodeList.isEmpty()) {
			throw new AssertCTNException("ChangeTreeNodes not found for specified schema path", schemaPath);
		}
		clearChanges();
	}

	public static class AssertCTNException extends RuntimeException {

		public SchemaPath m_schemaPath;

		public AssertCTNException(String s, SchemaPath schemaPath) {
			super(s);
			m_schemaPath = schemaPath;
		}

		public SchemaPath getSchemaPath() {
			return m_schemaPath;
		}

		@Override
		public String getMessage() {
			return "ChangeTreeNodes not found for specified schema path -> " + m_schemaPath.toString();
		}
	}

	@Override
	public boolean handleChanges(EditContext editContext, ModelNodeChangeType changeType, ModelNode changedNode) {
		m_editContainmentNode = editContext.getEditNode();
		populateModelNodeIdMap(m_editContainmentNode);
		return false;
	}

	private void populateModelNodeIdMap(EditContainmentNode editContainmentNode) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(editContainmentNode.getName());
		for(EditMatchNode matchNode : editContainmentNode.getMatchNodes()) {
			stringBuilder.append("/").append(matchNode.getName()).append("=").append(matchNode.getValue());
		}
		String key = stringBuilder.toString();
		modelNodeIdMap.put(key, editContainmentNode.getModelNodeId());
		for(EditContainmentNode childNode : editContainmentNode.getChildren()) {
			populateModelNodeIdMap(childNode);
		}
	}

	public void assertModelNodeId(String key, String expectedMNId) {
		ModelNodeId actualMNId = modelNodeIdMap.get(key);
		if(actualMNId != null) {
			assertEquals(expectedMNId, actualMNId.toString());
		}
		else {
			fail("ModelNodeId does not exist for " + key + " node.");
		}
	}
}
