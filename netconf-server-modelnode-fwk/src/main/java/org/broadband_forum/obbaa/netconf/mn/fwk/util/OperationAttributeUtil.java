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

package org.broadband_forum.obbaa.netconf.mn.fwk.util;

import static org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations.DELETE;
import static org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations.REMOVE;
import static org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations.REPLACE;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Created by sgs on 4/20/17.
 */
public class OperationAttributeUtil {
    private static Map<String, String> c_map = new HashMap<>();
    private static SimpleNamespaceContext c_namespaceContext = new SimpleNamespaceContext(c_map);

    static {
        c_map.put("nc", NetconfResources.NETCONF_RPC_NS_1_0);
    }

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(OperationAttributeUtil.class, LogAppNames.NETCONF_STACK);
    ;
    public static final String NC_DEFAULT_OPERATION_XPATH_STR = "/nc:rpc/nc:edit-config/nc:default-operation";

    public static String getOperationAttribute(Element element) {
        Attr operAttrOnNode = getOperationAttributeOnNode(element);
        if (operAttrOnNode != null) {
            return operAttrOnNode.getValue();
        }
        while (element.getParentNode() != null) {
            operAttrOnNode = getOperationAttributeOnNode(element);
            if (operAttrOnNode != null) {
                return operAttrOnNode.getValue();
            }
            Node parentNode = element.getParentNode();
            if (parentNode instanceof Element) {
                element = (Element) parentNode;
            } else {
                break;
            }
        }

        return DataStoreValidationUtil.getDefaultEditConfigOperationInCache();
    }

    public static Attr getOperationAttributeOnNode(Element element) {
        return element.getAttributeNodeNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources
                .OPERATION);
    }

    public static boolean isAllowedOperation(String choiceCaseOper1, String choiceCaseOper2) {
        switch (choiceCaseOper1){
            case EditConfigOperations.CREATE:
                switch (choiceCaseOper2){
                    case EditConfigOperations.CREATE: return false;
                    case EditConfigOperations.REPLACE: return false;
                    case EditConfigOperations.DELETE: return true;
                    case EditConfigOperations.REMOVE: return true;
                    case EditConfigOperations.MERGE: return false;
                }
                break;
            case EditConfigOperations.REPLACE:
                switch (choiceCaseOper2){
                    case EditConfigOperations.CREATE: return false;
                    case EditConfigOperations.REPLACE: return false;
                    case EditConfigOperations.DELETE: return true;
                    case EditConfigOperations.REMOVE: return true;
                    case EditConfigOperations.MERGE: return false;
                }
                break;
            case EditConfigOperations.DELETE:
                switch (choiceCaseOper2){
                    case EditConfigOperations.CREATE: return true;
                    case EditConfigOperations.REPLACE: return true;
                    case EditConfigOperations.DELETE: return true;
                    case EditConfigOperations.REMOVE: return true;
                    case EditConfigOperations.MERGE: return true;
                }
                break;
            case EditConfigOperations.REMOVE:
                switch (choiceCaseOper2){
                    case EditConfigOperations.CREATE: return true;
                    case EditConfigOperations.REPLACE: return true;
                    case EditConfigOperations.DELETE: return true;
                    case EditConfigOperations.REMOVE: return true;
                    case EditConfigOperations.MERGE: return true;
                }
                break;
            case EditConfigOperations.MERGE:
                switch (choiceCaseOper2){
                    case EditConfigOperations.CREATE: return false;
                    case EditConfigOperations.REPLACE: return false;
                    case EditConfigOperations.DELETE: return true;
                    case EditConfigOperations.REMOVE: return true;
                    case EditConfigOperations.MERGE: return false;
                }
                break;
        }
        return false;
    }

    public static boolean isRemoveOperation(Element element) {
        String operation = getOperationAttribute(element);
        return isRemoveOperation(operation);
    }

    private static boolean isRemoveOperation(String operation) {
        return operation.equals(REMOVE) || operation.equals(DELETE);
    }

    public static boolean isReplaceOperation(Element element) {
        String operation = getOperationAttribute(element);
        return isReplaceOperation(operation);
    }

    private static boolean isReplaceOperation(String operation) {
        return operation.equals(REPLACE);
    }

    public static boolean isRemoveOrReplaceOperation(Element element) {
        String operation = getOperationAttribute(element);
        return isRemoveOperation(operation)  || isReplaceOperation(operation);
    }
    
    public static boolean isAggregatedEditOperationAllowed(String operation1, String operation2) {
        switch (operation1){
            case EditConfigOperations.CREATE:
                switch (operation2){
                    case EditConfigOperations.CREATE: return false;
                    case EditConfigOperations.REPLACE: return false;
                    case EditConfigOperations.DELETE: return false;
                    case EditConfigOperations.REMOVE: return false;
                    case EditConfigOperations.MERGE: return false;
                }
                break;
            case EditConfigOperations.REPLACE:
                switch (operation2){
                    case EditConfigOperations.CREATE: return false;
                    case EditConfigOperations.REPLACE: return true;
                    case EditConfigOperations.DELETE: return false;
                    case EditConfigOperations.REMOVE: return true;
                    case EditConfigOperations.MERGE: return false;
                }
                break;
            case EditConfigOperations.DELETE:
                switch (operation2){
                    case EditConfigOperations.CREATE: return false;
                    case EditConfigOperations.REPLACE: return false;
                    case EditConfigOperations.DELETE: return false;
                    case EditConfigOperations.REMOVE: return false;
                    case EditConfigOperations.MERGE: return false;
                }
                break;
            case EditConfigOperations.REMOVE:
                switch (operation2){
                    case EditConfigOperations.CREATE: return false;
                    case EditConfigOperations.REPLACE: return true;
                    case EditConfigOperations.DELETE: return false;
                    case EditConfigOperations.REMOVE: return true;
                    case EditConfigOperations.MERGE: return false;
                }
                break;
            case EditConfigOperations.MERGE:
                switch (operation2){
                    case EditConfigOperations.CREATE: return false;
                    case EditConfigOperations.REPLACE: return false;
                    case EditConfigOperations.DELETE: return false;
                    case EditConfigOperations.REMOVE: return true;
                    case EditConfigOperations.MERGE: return true;
                }
                break;
        }
        return false;
    }
    
    private static class SimpleNamespaceContext implements NamespaceContext {
        private final Map<String, String> m_map;

        public SimpleNamespaceContext(Map<String, String> map) {
            m_map = map;
        }

        @Override
        public String getNamespaceURI(String s) {
            return m_map.get(s);
        }

        @Override
        public String getPrefix(String s) {
            for (Map.Entry<String, String> entry : m_map.entrySet()) {
                if (entry.getValue().equals(s)) {
                    return entry.getKey();
                }
            }
            return null;
        }

        @Override
        public Iterator getPrefixes(String s) {
            return Arrays.asList(getPrefix(s)).iterator();
        }
    }
}
