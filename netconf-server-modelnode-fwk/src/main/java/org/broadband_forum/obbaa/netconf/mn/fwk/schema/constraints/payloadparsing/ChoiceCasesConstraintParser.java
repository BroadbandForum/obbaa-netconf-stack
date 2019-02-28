package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.ValidationException;
import org.dom4j.dom.DOMNodeHelper;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.NetconfRpcErrorUtil;

public class ChoiceCasesConstraintParser {

    public static void checkIfMultipleChoiceCaseElementExists(Collection<DataSchemaNode> dataNodeChildren,
            DataSchemaNode dataSchemaNode, Element dataNode, SchemaRegistry schemaRegistry) throws ValidationException {
        NodeList childNodes = (dataNode == null ? DOMNodeHelper.EMPTY_NODE_LIST : dataNode.getChildNodes());
        if (childNodes.getLength() > 0) {
            Pair<String, Map<String, String>> errorPathPair = SchemaRegistryUtil.getErrorPath(dataNode, dataSchemaNode,
                    schemaRegistry, (Element) null);
            for (DataSchemaNode childNode : dataNodeChildren) {
                if (childNode instanceof ChoiceSchemaNode) {
                    checkIfMultipleChoiceCaseElementExists((ChoiceSchemaNode) childNode, childNodes, dataNode,
                            errorPathPair, schemaRegistry);
                }
            }
        }
    }

    private static void checkIfMultipleChoiceCaseElementExists(ChoiceSchemaNode choiceNode, NodeList childNodes,
            Element element, Pair<String, Map<String, String>> existingErrorPathPair, SchemaRegistry schemaRegistry)
            throws ValidationException {
        int count = 0;
        for (CaseSchemaNode caseNode : choiceNode.getCases().values()) {
            Set<QName> caseChildNodes = new HashSet<>();
            for (DataSchemaNode caseChildNode : caseNode.getChildNodes()) {
                caseChildNodes.add(caseChildNode.getQName());
            }
            if (ChoiceCaseNodeUtil.isDataNodeSuperSet(childNodes, caseChildNodes)) {
                count++;
            }
            if (count > 1) {
                // dataNode contains elements from more than one case.
                Pair<String, Map<String, String>> errorPathPair = SchemaRegistryUtil.getErrorPath(existingErrorPathPair,
                        element, choiceNode, schemaRegistry);
                throw new ValidationException(getBadElementRpcError(choiceNode, errorPathPair));
            }
        }
    }

    private static NetconfRpcError getBadElementRpcError(ChoiceSchemaNode schemaNode,
            Pair<String, Map<String, String>> errorPathPair) {
        String localName = schemaNode.getQName().getLocalName();
        NetconfRpcError netconfRpcError = NetconfRpcErrorUtil.getApplicationError(NetconfRpcErrorTag.BAD_ELEMENT,
                String.format("Invalid element in choice node ", localName));
        netconfRpcError.setErrorPath(errorPathPair.getFirst(), errorPathPair.getSecond());
        return netconfRpcError;
    }
}
