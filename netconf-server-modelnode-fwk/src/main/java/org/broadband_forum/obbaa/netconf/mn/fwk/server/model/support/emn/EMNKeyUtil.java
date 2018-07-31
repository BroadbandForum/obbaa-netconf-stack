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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.IdClass;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Type;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.DataStoreException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.StringToObjectTransformer;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeKey;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.TransformerException;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

/**
 * Created by keshava on 12/28/15.
 */
public class EMNKeyUtil {
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(EMNKeyUtil.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");

    static Object buildPrimaryKey(Class klass, ModelNodeId parentNodeId,
                                  ModelNodeKey keys, EntityRegistry entityRegistry, EntityDataStoreManager
                                          entityDataStoreManager) throws DataStoreException {
        EntityType entityType = entityDataStoreManager.getMetaModel().entity(klass);
        Type type = entityType.getIdType();
        Object idObject = null;
        Class idClass = null;
        try {
            if (type != null) {
                idClass = type.getJavaType();
                if (idClass.equals(String.class)) {
                    idObject = parentNodeId.getModelNodeIdAsString();
                } else {
                    LOGGER.debug("Java idType {} is nto supported", type);
                }
            } else {
                //FIXME: FNMS-10111 ugly needs to be fixed by using metamodel
                idClass = ((IdClass) klass.getAnnotation(IdClass.class)).value();
                idObject = idClass.newInstance();
                if (!(idObject instanceof String || idObject instanceof String)) {
                    for (Map.Entry<QName, String> keyEntry : keys.entrySet()) {
                        String fieldName = entityRegistry.getFieldName(klass, keyEntry.getKey());
                        setFieldValue(idClass, idObject, fieldName, keyEntry.getValue());

                    }
                    String parentIdFieldName = entityRegistry.getYangParentIdFieldName(klass);
                    if (parentIdFieldName == null || parentIdFieldName.isEmpty()) {
                        parentIdFieldName = YangParentId.PARENT_ID_FIELD_NAME;
                    }
                    setFieldValue(idClass, idObject, parentIdFieldName, parentNodeId.getModelNodeIdAsString());
                } else {
                    try {
                        idObject = StringToObjectTransformer.transform(keys.getKeys().entrySet().iterator().next()
                                        .getValue(),
                                type.getJavaType());
                    } catch (TransformerException e) {
                        throw new DataStoreException(e);
                    }
                }
            }
            return idObject;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new DataStoreException(e);
        }


    }

    private static void setFieldValue(Class idClass, Object idObject, String fieldName, Object value) throws
            IllegalAccessException, DataStoreException {
        try {
            Field declaredField = idClass.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            declaredField.set(idObject, value);
        } catch (NoSuchFieldException e) {
            throw new DataStoreException(e);
        } finally {
            try {
                idClass.getDeclaredField(fieldName).setAccessible(false);
            } catch (NoSuchFieldException e) {
                //Let the previous exception be thrown
            }
        }
    }

    /**
     * Return the ParentId given a schemaPath and its ModelNodeId.
     *
     * @param schemaRegistry
     * @param nodePath
     * @param nodeId
     * @return
     */
    public static ModelNodeId getParentId(SchemaRegistry schemaRegistry, SchemaPath nodePath, ModelNodeId nodeId) {
        DataSchemaNode schemaNode = schemaRegistry.getDataSchemaNode(nodePath);
        return getParentId(schemaNode, nodeId);
    }

    /**
     * Return the ParentId given a schemaNode and its ModelNodeId.
     *
     * @param nodeId
     * @param schemaNode
     * @return
     */
    public static ModelNodeId getParentId(DataSchemaNode schemaNode, ModelNodeId nodeId) {
        List<ModelNodeRdn> parentKeyRdns = new ArrayList<>(nodeId.getRdns());
        if (schemaNode instanceof ListSchemaNode) {
            //remove the key rdns
            parentKeyRdns = parentKeyRdns.subList(0, parentKeyRdns.size() - ((ListSchemaNode) schemaNode)
                    .getKeyDefinition().size());
        }
        //it is empty if the parent node is root node
        if (!parentKeyRdns.isEmpty()) {
            parentKeyRdns = parentKeyRdns.subList(0, parentKeyRdns.size() - 1);
        }

        return new ModelNodeId(parentKeyRdns);
    }

    /**
     * Prepare modelNodeId given parent's id and ModelNodeKey.
     *
     * @param key
     * @param parentId
     * @param schemaPath
     * @return
     */
    public static ModelNodeId getModelNodeId(ModelNodeKey key, ModelNodeId parentId, SchemaPath schemaPath) {
        ModelNodeId modelNodeId = new ModelNodeId(parentId);
        QName qname = schemaPath.getLastComponent();
        modelNodeId.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, qname.getNamespace().toString(), qname
                .getLocalName()));

        for (Map.Entry<QName, String> keyEntry : key.entrySet()) {
            modelNodeId.addRdn(new ModelNodeRdn(keyEntry.getKey(), keyEntry.getValue()));
        }
        return modelNodeId;
    }

    /**
     * A example super node id is as follows
     * /container=jukebox/container=library/container=artist/name=Artist1/container=album/name=album1 is a super node
     * id of
     * 1. /container=jukebox
     * 2. /container=jukebox/container=library
     * 3. /container=jukebox/container=library/container=artist/name=artist1
     * <p>
     * Given a super node id the method reduces the nodeId to the nodeType level.
     *
     * @param schemaRegistry
     * @param nodeType
     * @param superNodeId
     * @return
     */

    public static ModelNodeId scopeModelNodeId(SchemaRegistry schemaRegistry, SchemaPath nodeType, ModelNodeId
            superNodeId) {
        ModelNodeId scopedId = new ModelNodeId();
        int count = 0;
        while (schemaRegistry.getDataSchemaNode(nodeType) != null) {
            DataSchemaNode schemaNode = schemaRegistry.getDataSchemaNode(nodeType);
            if (schemaNode instanceof ContainerSchemaNode) {
                count++;
            } else if (schemaNode instanceof ListSchemaNode) {
                count += (1 + ((ListSchemaNode) schemaNode).getKeyDefinition().size());
            }
            nodeType = nodeType.getParent();
        }

        List<ModelNodeRdn> superNodeIdRdns = superNodeId.getRdns();
        for (int i = 0; i < count && i < superNodeIdRdns.size(); i++) {
            ModelNodeRdn rdn = superNodeIdRdns.get(i);
            scopedId.addRdn(rdn);
        }

        return scopedId;
    }

    public static ModelNodeId getParentIdFromSchemaPath(SchemaPath schemaPath) {
        ModelNodeId parentId = null;
        SchemaPath parentPath = schemaPath.getParent();
        Iterator<QName> parentPathIter = parentPath.getPathFromRoot().iterator();
        List<ModelNodeRdn> rdns = new ArrayList<>();
        while (parentPathIter.hasNext()) {
            QName next = parentPathIter.next();
            rdns.add(new ModelNodeRdn(ModelNodeRdn.CONTAINER, next.getNamespace().toString(), next.getLocalName()));
        }
        parentId = new ModelNodeId(rdns);
        return parentId;
    }

    /**
     * Given a ModelNodeId with RDNs that don't have namespaces in them, this method will add namespaces by going
     * through SchemaRegistry.
     * This will also correct namespaces if they are incorrect in the RDNs.
     * The RDN values and names however are not changed.
     *
     * @param nodeId         - ModelNodeId to be fixed.
     * @param schemaPath     - SchemaPath of the node whose nodeId is to be fixed.
     * @param schemaRegistry
     * @return
     * @throws IllegalArgumentException
     */
    public static ModelNodeId populateNamespaces(ModelNodeId nodeId, SchemaPath schemaPath, SchemaRegistry
            schemaRegistry) throws
            IllegalArgumentException {
        ModelNodeId modifiedNodeId = new ModelNodeId();
        List<ModelNodeRdn> rdns = nodeId.getRdns();
        if (rdns.size() < 1) {
            return modifiedNodeId;
        }

        /*
            We will go backwards from the last RDN to the first.
            Fixing the namespace for each RDN as we go and appending the RDN at the beginning of the new Node Id.
            In case of a choice case node, the ModelNodeId will not contain the choice/case information. It only
            contains the info
            related to the XML representation of a node. Which is OK.
            But a SchemaPath contains choice/case nodes, so we will have to skip choice case nodes.

            But why do we do this backwards?
            Ans: if we traverse from root towards the node in question and we have chocie case nodes in the path,
            then determining the
            exact case node is hard, but this is easier if we traverse the schema path backwards.

         */
        DataSchemaNode currentNode = schemaRegistry.getDataSchemaNode(schemaPath);
        if (currentNode == null) {
            throw new IllegalArgumentException("Argument SchemaPath is invalid.");
        }
        //get the last RDN
        int i = rdns.size() - 1;
        while (currentNode != null) {
            if (currentNode instanceof ListSchemaNode) {
                //add key RDNs, we can rely on the order of KeyDefinition here
                List<QName> keyDefinition = new ArrayList(((ListSchemaNode) currentNode).getKeyDefinition());
                Collections.reverse(keyDefinition);
                for (QName keyQname : keyDefinition) {
                    ModelNodeRdn rdn = getRdn(rdns, i--);
                    modifiedNodeId.addRdn(0, new ModelNodeRdn(keyQname, rdn.getRdnValue()));
                }
            }
            ModelNodeRdn rdn = getRdn(rdns, i--);
            if (!currentNode.getQName().getLocalName().equals(rdn.getRdnValue())) {
                throw new IllegalArgumentException("ModelNodeId does not match the supplied SchemaPath, ModelNodeId " +
                        "is invalid");
            }
            modifiedNodeId.addRdn(0, new ModelNodeRdn(ModelNodeRdn.CONTAINER, currentNode.getQName().getNamespace()
                    .toString(),
                    rdn.getRdnValue()));
            currentNode = schemaRegistry.getNonChoiceParent(currentNode.getPath());
        }
        //check if we are still left with RDNs
        if (i > -1) {
            throw new IllegalArgumentException("ModelNodeId does not represent supplied SchemaPath, ModelNodeId might" +
                    " represent a child " +
                    "SchemaPath");
        }

        return modifiedNodeId;
    }

    private static ModelNodeRdn getRdn(List<ModelNodeRdn> rdns, int pos) throws IllegalArgumentException {
        if (pos < 0) {
            throw new IllegalArgumentException("ModelNodeId does not represent supplied SchemaPath, ModelNodeId might" +
                    " represent a parent " +
                    "SchemaPath");
        }
        return rdns.get(pos);
    }
}
