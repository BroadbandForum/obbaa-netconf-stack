/*
 * Copyright 2021 Broadband Forum
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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.UniqueConstraint;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Relative;
import org.opendaylight.yangtools.yang.model.api.type.LeafrefTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;

import com.google.common.annotations.VisibleForTesting;

public class SchemaVerifierImpl implements SchemaVerifier {

    private static final boolean ENABLE_SCHEMA_VERIFICATION = Boolean.valueOf(SystemPropertyUtils.getInstance().
            getFromEnvOrSysProperty("ENABLE_SCHEMA_VERIFICATION", "TRUE"));
    private Boolean m_schemaVerificationEnabledForTest = null;

    @Override
    public void verify(SchemaRegistry schemaRegistry) throws SchemaBuildException {
        Set<String> errorList = new HashSet<String>();
        if(isSchemaVerificationEnabled()){
            checkCircularDependencyforLeafrefs(schemaRegistry, errorList);
            checkUniqueConstraints(schemaRegistry, errorList);
        }

        if (!errorList.isEmpty()) {
            throw new SchemaBuildException(StringUtils.join(errorList, "\n"));
        }
    }

    @VisibleForTesting
    protected boolean isSchemaVerificationEnabled(){
        return m_schemaVerificationEnabledForTest != null ? m_schemaVerificationEnabledForTest : ENABLE_SCHEMA_VERIFICATION;
    }

    public void setIsSchemaVerificationEnabledForTest(boolean testValue){
        m_schemaVerificationEnabledForTest = testValue;
    }

    @VisibleForTesting
    protected void checkCircularDependencyforLeafrefs(SchemaRegistry schemaRegistry, Set<String> errorList) {
        ConcurrentHashMap<SchemaPath, TreeImpactNode<ImpactNode>> schemaPathToTreeImpactNode = schemaRegistry
                .getImpactNodeMap();
        Map<SchemaPath, DataSchemaNode> schemaNodeMap = schemaRegistry.getSchemaNodes();
        for (Map.Entry<SchemaPath, TreeImpactNode<ImpactNode>> treeImpactNode : schemaPathToTreeImpactNode.entrySet()) {
            SchemaPath nodeSchemaPath = treeImpactNode.getKey();
            DataSchemaNode dataSchemaNode = schemaNodeMap.get(nodeSchemaPath);

            if (dataSchemaNode instanceof TypedDataSchemaNode) {
                TypeDefinition refDefinition = ((TypedDataSchemaNode) dataSchemaNode).getType();
                if (refDefinition instanceof LeafrefTypeDefinition) {
                    checkCircularDependencyonImpactNode(dataSchemaNode.getPath(), errorList,
                            schemaPathToTreeImpactNode);

                } else if (refDefinition instanceof UnionTypeDefinition) {
                    for (TypeDefinition<?> typeDefinition : ((UnionTypeDefinition) refDefinition).getTypes()) {
                        if (typeDefinition instanceof LeafrefTypeDefinition) {
                            checkCircularDependencyonImpactNode(dataSchemaNode.getPath(), errorList,
                                    schemaPathToTreeImpactNode);
                        }
                    }
                }
            }
        }
    }

    private void checkCircularDependencyonImpactNode(SchemaPath nodeSchemaPath, Set<String> errorList,
            ConcurrentHashMap<SchemaPath, TreeImpactNode<ImpactNode>> schemaPathToTreeImpactNode) {
        TreeImpactNode<ImpactNode> impactNode = schemaPathToTreeImpactNode.get(nodeSchemaPath);
        for (Map.Entry<SchemaPath, Set<ReferringNode>> impactMapValue : impactNode.getData().getImpactNodes().entrySet()) {
            if (schemaPathToTreeImpactNode.get(impactMapValue.getKey()) != null
                    && schemaPathToTreeImpactNode.get(impactMapValue.getKey()).getData() != null) {
                checkForChildrenDependency(impactMapValue.getKey(), nodeSchemaPath, errorList,
                        schemaPathToTreeImpactNode);
                ReferringNodes impactMap = schemaPathToTreeImpactNode.get(impactMapValue.getKey())
                        .getData().getImpactNodes();
                addToErrorList(nodeSchemaPath, impactMap, errorList);
            }
        }
    }

    private void checkForChildrenDependency(SchemaPath impactSchemaPath, SchemaPath nodeSchemaPath,
            Set<String> errorList,
            ConcurrentHashMap<SchemaPath, TreeImpactNode<ImpactNode>> schemaPathToTreeImpactNode) {
        Set<SchemaPath> childPaths = new HashSet<SchemaPath>();
        childPaths.addAll(schemaPathToTreeImpactNode.keySet());
        for (SchemaPath childPath : childPaths) {
            SchemaPath parentPath = childPath.getParent();
            while (parentPath.getLastComponent() != null) {
                if (parentPath.equals(impactSchemaPath)) {
                    Set<ReferringNode> referringNodes = schemaPathToTreeImpactNode.get(childPath).getData().getImpactNodes().get(nodeSchemaPath);
                    if (schemaPathToTreeImpactNode.get(childPath).getData() != null && referringNodes != null) {
                        if(referringNodes.stream().filter(referringNode -> referringNode != null).count() > 0 && isLeafRefReferringNodes(referringNodes)) {
                            errorList.add("Leafref with circular dependency: "+ nodeSchemaPath.getLastComponent().getLocalName());
                        }
                    }
                }
                parentPath = parentPath.getParent();
            }
        }
    }

    private boolean isLeafRefReferringNodes(Collection<ReferringNode> referringNodes) {
        for ( ReferringNode referringNode : referringNodes){
            if (referringNode.getReferenceType().equals(ReferringNode.ReferenceType.LEAFREF)){
                return true;
            }
        }
        return false;
    }

    @VisibleForTesting
    protected void checkUniqueConstraints(SchemaRegistry schemaRegistry, Set<String> errorList) {
        Map<SchemaPath, DataSchemaNode> schemaNodeMap = schemaRegistry.getSchemaNodes();
        for (DataSchemaNode node: schemaNodeMap.values()) {
            if (node instanceof ListSchemaNode) {
                SchemaPath basePath = node.getPath();
                ListSchemaNode listnode = (ListSchemaNode)node;
                for (UniqueConstraint uniqueConstraint : listnode.getUniqueConstraints()) {
                    for (Relative relative : uniqueConstraint.getTag()) {
                        SchemaPath path = relative.asSchemaPath();
                        SchemaPath fullPath = basePath.createChild(path);
                        if (! schemaNodeMap.containsKey(fullPath)) {
                            errorList.add("Nodes in unique constraint of '" + node.getQName().getLocalName() + "' don't exist (maybe wrong namespace?): " + relative);
                        }
                    }
                }
            }
        }
    }

    private void addToErrorList(SchemaPath impactingNodeSP, ReferringNodes referringNodes,
            Set<String> errorList) {
        if(!referringNodes.isEmpty() && referringNodes.containsKey(impactingNodeSP)) {
            for (ReferringNode referringNode : referringNodes.get(impactingNodeSP)) {
                if (referringNode != null && !referringNode.getReferredSP().equals(referringNode.getReferringSP()) && isLeafRefReferringNodes(Arrays.asList(referringNode))) {
                    errorList.add("Leafref with circular dependency: " + impactingNodeSP.getLastComponent().getLocalName());
                }
            }
        }
    }

}
