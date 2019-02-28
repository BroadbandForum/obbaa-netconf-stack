package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeCreateException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttributeFactory;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;

/**
 * Created by keshava on 5/6/16.
 */
public class CommandUtils {
    static ListSchemaNode getListSchemaNode(QName qName, SchemaRegistry schemaRegistry, SchemaPath parentNodeSchemaPath) throws
            ModelNodeCreateException {
        Collection<DataSchemaNode> schemaNodes = schemaRegistry.getChildren(parentNodeSchemaPath);
        ListSchemaNode listSchemaNode = null;
        for (DataSchemaNode dataSchemaNode : schemaNodes) {
            if (dataSchemaNode.getQName().equals(qName)) {
                if (dataSchemaNode instanceof ListSchemaNode) {
                    listSchemaNode = (ListSchemaNode)dataSchemaNode;
                    break;
                }
            }
            if (dataSchemaNode instanceof ChoiceSchemaNode) {
                Collection<CaseSchemaNode> schemaChoiceCases = ((ChoiceSchemaNode)dataSchemaNode).getCases().values();
                for (CaseSchemaNode choiceCaseNode : schemaChoiceCases) {
                    if (choiceCaseNode.getDataChildByName(qName) != null && (choiceCaseNode.getDataChildByName(qName)) instanceof ListSchemaNode) {
                        listSchemaNode = (ListSchemaNode)choiceCaseNode.getDataChildByName(qName);
                        break;
                    }
                }
            }
        }
        if (listSchemaNode == null) {
            throw new ModelNodeCreateException(String.format("Cannot get the schema node for '%s'", qName));
        }
        return listSchemaNode;
    }

    public static ModelNode getExistingNode(QName qName, String keyAttribute, ChildListHelper childListHelper, ModelNode parentNode,
                                            ModelNodeHelperRegistry modelNodeHelperRegistry, SchemaRegistry schemaRegistry) throws ModelNodeGetException, ModelNodeCreateException {
        Map<QName, ConfigLeafAttribute> keyPredicates = getKeyPredicates(qName, keyAttribute, schemaRegistry, parentNode.getModelNodeSchemaPath());
        ModelNode existingNode = null;
        int countMatchKeys = 0;
        try {
            Collection<ModelNode> currentListChilds = childListHelper.getValue(parentNode, keyPredicates);
            for (ModelNode child : currentListChilds) {
                Map<QName, ConfigAttributeHelper> naturalKeyHelpers = modelNodeHelperRegistry.getNaturalKeyHelpers(child.getModelNodeSchemaPath());
                for (QName key : modelNodeHelperRegistry.getNaturalKeyHelpers(child.getModelNodeSchemaPath()).keySet()) {
                    for (QName keyQName : keyPredicates.keySet()) {
                        if (key.equals(keyQName)) {
                            ConfigLeafAttribute keyPredicateValue = keyPredicates.get(keyQName);
                            if (keyPredicateValue.equals(modelNodeHelperRegistry.getNaturalKeyHelper(child.getModelNodeSchemaPath(), key)
                                    .getValue(child))) {
                                countMatchKeys++;
                            }
                        }
                    }
                }
                if (countMatchKeys == naturalKeyHelpers.size()) {
                    existingNode = child;
                    break;
                }
                countMatchKeys = 0; // reset
            }
        } catch (GetAttributeException e) {
            ModelNodeGetException exception = new ModelNodeGetException("could not get value from ModelNode." + e.getMessage(), e);
            throw exception;
        }

        return existingNode;
    }

    private static Map<QName, ConfigLeafAttribute> getKeyPredicates(QName qName, String keyAttribute, SchemaRegistry schemaRegistry, SchemaPath
            parentNodeSchemaPath) throws ModelNodeCreateException  {
        Map<QName, ConfigLeafAttribute> keyPairs = new HashMap<>();
        String regex = "\\]\\["; // split keys
        String keyAttributeFix = keyAttribute.substring(1,keyAttribute.length() - 1); // remove '[', ']'
        List<String> strKeys = Arrays.asList(keyAttributeFix.split(regex));
        ListSchemaNode listSchemaNode = getListSchemaNode(qName,  schemaRegistry, parentNodeSchemaPath);
        List<QName> keys = listSchemaNode.getKeyDefinition();
        for (String strKey : strKeys) {
            String key = "";

            String value = "";
            if (strKey.indexOf(":") >= 0) {// contains prefix
                key = strKey.substring(strKey.indexOf(":") + 1, strKey.indexOf("=")).trim();
            } else {
                key = strKey.substring(0, strKey.indexOf("="));
            }
            value = strKey.substring(strKey.indexOf("=") + 1, strKey.length()).trim();
            value = value.substring(value.indexOf("'") + 1,value.lastIndexOf("'"));
            QName keyQName = getKeyQname(keys, key);
            SchemaPath keySchemaPath = new SchemaPathBuilder().withParent(listSchemaNode.getPath()).appendQName(keyQName).build();
            LeafSchemaNode keyLeaf = (LeafSchemaNode) schemaRegistry.getDataSchemaNode(keySchemaPath);
            keyPairs.put(keyQName, ConfigAttributeFactory.getConfigLeafAttribute(schemaRegistry, keyLeaf, value));
        }

        return keyPairs; // must return a map of full key/value within the list
    }

    private static QName getKeyQname(List<QName> keyDefinition, String keyName) {
        QName keyDefined = null;
        for (QName key : keyDefinition) {
            if (key.getLocalName().equals(keyName)) {
                keyDefined = key;
                break;
            }
        }
        return keyDefined;
    }
}
