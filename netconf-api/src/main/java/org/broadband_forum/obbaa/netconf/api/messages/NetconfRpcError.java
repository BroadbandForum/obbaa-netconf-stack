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

package org.broadband_forum.obbaa.netconf.api.messages;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NetconfRpcError {

    private static final Logger LOGGER = Logger.getLogger(NetconfRpcError.class);
    private NetconfRpcErrorType m_errorType;
    private NetconfRpcErrorTag m_errorTag;
    private NetconfRpcErrorSeverity m_errorSeverity;
    private String m_errorAppTag;
    private Map<String, String> m_errorPathNsByPrefix;
    private String m_errorPath;
    private Element m_errorPathElement;
    private String m_errorMessage;
    private Element m_errorInfo;
    private Document m_document;
    private List<Pair<Object, String>> m_errorInfoElements = new LinkedList<Pair<Object, String>>();

    /**
     * The ErrorTag, ErrorType,ErrorSeverity are mandatory parameters. User facing message is also made mandatory.
     *
     * @param errorTag
     * @param errorType
     * @param errorSeverity
     * @param errorMessage
     */
    public NetconfRpcError(NetconfRpcErrorTag errorTag, NetconfRpcErrorType errorType, NetconfRpcErrorSeverity
            errorSeverity,
                           String errorMessage) {
        this.m_errorTag = errorTag;
        this.m_errorType = errorType;
        this.m_errorSeverity = errorSeverity;
        this.m_errorMessage = errorMessage;
    }

    public NetconfRpcError setErrorAppTag(String errorAppTag) {
        this.m_errorAppTag = errorAppTag;
        return this;
    }

    /**
     * @deprecated use {@link #setErrorPath(String, Map<String, String>)} or {@link #setErrorPathElement(Element)}
     * instead.
     */
    @Deprecated
    public NetconfRpcError setErrorPath(String errorPath) {
        this.m_errorPath = errorPath;
        return this;
    }

    /**
     * Either errorPath or errorPathElement should be set
     */
    public NetconfRpcError setErrorPath(String errorPath, Map<String, String> errorPathNsByPrefix) {
        this.m_errorPath = errorPath;
        this.m_errorPathNsByPrefix = errorPathNsByPrefix;
        return this;
    }

    /**
     * Either errorPath or errorPathElement should be set
     */
    public NetconfRpcError setErrorPathElement(Element errorPathElement) {
        this.m_errorPathElement = errorPathElement;
        return this;
    }

    public NetconfRpcError setErrorMessage(String errorMessage) {
        this.m_errorMessage = errorMessage;
        return this;
    }

    public NetconfRpcErrorType getErrorType() {
        return m_errorType;
    }

    public NetconfRpcErrorTag getErrorTag() {
        return m_errorTag;
    }

    public NetconfRpcErrorSeverity getErrorSeverity() {
        return m_errorSeverity;
    }

    public String getErrorAppTag() {
        return m_errorAppTag;
    }

    public String getErrorPath() {
        return m_errorPath;
    }

    public Map<String, String> getErrorPathNsByPrefix() {
        return m_errorPathNsByPrefix;
    }

    /**
     * @deprecated use {@link #setErrorPath(String, Map<String, String>)} or {@link #setErrorPathElement(Element)}
     * instead.
     */
    @Deprecated
    public void setErrorPathNsByPrefix(Map<String, String> errorPathNsByPrefix) {
        this.m_errorPathNsByPrefix = errorPathNsByPrefix;
    }

    public Element getErrorPathElement() {
        return m_errorPathElement;
    }

    public String getErrorMessage() {
        return m_errorMessage;
    }

    public Element getErrorInfo() {
        if (m_errorInfo == null && !m_errorInfoElements.isEmpty()) {
            if (m_document == null) {
                try {
                    m_document = DocumentUtils.getNewDocument();

                } catch (ParserConfigurationException e) {
                    LOGGER.error("could not get document", e);
                }
            }

            if (m_document != null) {
                if (m_errorInfo == null) {
                    m_errorInfo = m_document.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, NetconfResources
                            .RPC_ERROR_INFO);
                }

                for (Pair<Object, String> element : m_errorInfoElements) {
                    Element errorInfoContent = null;
                    Object first = element.getFirst();
                    if (first instanceof String) {
                        errorInfoContent = m_document.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0, (String)
                                first);
                    } else if (first instanceof NetconfRpcErrorInfo) {
                        errorInfoContent = m_document.createElementNS(NetconfResources.NETCONF_RPC_NS_1_0,
                                ((NetconfRpcErrorInfo) first).value());
                    }
                    errorInfoContent.setTextContent(element.getSecond());
                    m_errorInfo.appendChild(errorInfoContent);
                }
            }
        }
        return m_errorInfo;
    }

    @Override
    public String toString() {
        if (m_errorInfo == null && !m_errorInfoElements.isEmpty()) {
            getErrorInfo();
        }
        return "NetconfRpcError [errorType=" + m_errorType.value() + ", errorTag=" + m_errorTag.value() + ", " +
                "errorSeverity="
                + m_errorSeverity.value() + ", errorAppTag=" + m_errorAppTag + ", errorPath=" + m_errorPath + ", " +
                "errorMessage="
                + m_errorMessage + ", errorInfoContent=" + DocumentUtils.getErrorInfoContents(m_errorInfo) + "]";
    }

    public NetconfRpcError addErrorInfoElement(NetconfRpcErrorInfo elementName, String elementTextContent) {
        m_errorInfoElements.add(new Pair<Object, String>(elementName, elementTextContent));
        return this;
    }

    public NetconfRpcError addErrorInfoElements(List<Pair<String, String>> errorInfoElements) {
        for (Pair<String, String> element : errorInfoElements) {
            m_errorInfoElements.add(new Pair<Object, String>(element.getFirst(), element.getSecond()));
        }
        return this;
    }

    public NetconfRpcError setErrorInfo(Element errorInfo) {
        this.m_errorInfo = errorInfo;
        return this;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_errorAppTag == null) ? 0 : m_errorAppTag.hashCode());
        result = prime * result + ((m_errorInfoElements == null) ? 0 : m_errorInfoElements.hashCode());
        result = prime * result + ((m_errorMessage == null) ? 0 : m_errorMessage.hashCode());
        result = prime * result + ((m_errorPath == null) ? 0 : m_errorPath.hashCode());
        result = prime * result + ((m_errorPathNsByPrefix == null) ? 0 : m_errorPathNsByPrefix.hashCode());
        result = prime * result + ((m_errorSeverity == null) ? 0 : m_errorSeverity.hashCode());
        result = prime * result + ((m_errorTag == null) ? 0 : m_errorTag.hashCode());
        result = prime * result + ((m_errorType == null) ? 0 : m_errorType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NetconfRpcError other = (NetconfRpcError) obj;
        if (m_errorAppTag == null) {
            if (other.m_errorAppTag != null)
                return false;
        } else if (!m_errorAppTag.equals(other.m_errorAppTag))
            return false;
        if (m_errorInfoElements == null) {
            if (other.m_errorInfoElements != null)
                return false;
        } else if (!m_errorInfoElements.equals(other.m_errorInfoElements))
            return false;
        if (m_errorMessage == null) {
            if (other.m_errorMessage != null)
                return false;
        } else if (!m_errorMessage.equals(other.m_errorMessage))
            return false;
        if (m_errorPath == null) {
            if (other.m_errorPath != null)
                return false;
        } else if (!m_errorPath.equals(other.m_errorPath))
            return false;
        if (m_errorPathNsByPrefix == null) {
            if (other.m_errorPathNsByPrefix != null)
                return false;
        } else if (!m_errorPathNsByPrefix.equals(other.m_errorPathNsByPrefix))
            return false;
        if (m_errorSeverity != other.m_errorSeverity)
            return false;
        if (m_errorTag != other.m_errorTag)
            return false;
        if (m_errorType != other.m_errorType)
            return false;
        return true;
    }

    public static NetconfRpcError getUnknownElementError(String unknownElementName, NetconfRpcErrorType errorType) {
        return new NetconfRpcError(NetconfRpcErrorTag.UNKNOWN_ELEMENT, errorType, NetconfRpcErrorSeverity.Error,
                String.format(
                NetconfRpcErrorMessages.AN_UNEXPECTED_ELEMENT_S_IS_PRESENT, unknownElementName)).addErrorInfoElement(
                NetconfRpcErrorInfo.BadElement, unknownElementName);
    }

    public static NetconfRpcError getBadElementError(String badElementName, NetconfRpcErrorType errorType) {
        return new NetconfRpcError(NetconfRpcErrorTag.BAD_ELEMENT, errorType, NetconfRpcErrorSeverity.Error, String
                .format(
                NetconfRpcErrorMessages.AN_UNEXPECTED_ELEMENT_S_IS_PRESENT, badElementName)).addErrorInfoElement(
                NetconfRpcErrorInfo.BadElement, badElementName);
    }

    public static NetconfRpcError getUnknownNamespaceError(String badNamespace, String badElement,
                                                           NetconfRpcErrorType errorType) {
        return new NetconfRpcError(NetconfRpcErrorTag.UNKNOWN_NAMESPACE, errorType, NetconfRpcErrorSeverity.Error,
                String.format(
                NetconfRpcErrorMessages.AN_UNEXPECTED_NAMESPACE_S_IS_PRESENT, badNamespace)).addErrorInfoElement(
                NetconfRpcErrorInfo.BadNamespace, badNamespace).addErrorInfoElement(NetconfRpcErrorInfo.BadElement,
                badElement);
    }

    public static NetconfRpcError getMissingElementError(List<String> missingAttributes, NetconfRpcErrorType
            errorType) {
        NetconfRpcError error = new NetconfRpcError(NetconfRpcErrorTag.MISSING_ELEMENT, errorType,
                NetconfRpcErrorSeverity.Error,
                String.format(NetconfRpcErrorMessages.EXPECTED_ELEMENTS_IS_MISSING, missingAttributes));
        for (String missingAtt : missingAttributes) {
            error.addErrorInfoElement(NetconfRpcErrorInfo.BadElement, missingAtt);
        }
        return error;
    }

    public static NetconfRpcError getMisplacedKeyError(List<String> misplacedKeys, NetconfRpcErrorType errorType) {
        NetconfRpcError error = new NetconfRpcError(NetconfRpcErrorTag.MISSING_ELEMENT, errorType,
                NetconfRpcErrorSeverity.Error,
                String.format(NetconfRpcErrorMessages.EXPECTED_KEYS_IS_MISPLACED, misplacedKeys));
        for (String misplacedKey : misplacedKeys) {
            error.addErrorInfoElement(NetconfRpcErrorInfo.BadElement, misplacedKey);
        }
        return error;
    }

    public static NetconfRpcError getMissingKeyError(List<String> missingKeys, NetconfRpcErrorType errorType) {
        NetconfRpcError error = new NetconfRpcError(NetconfRpcErrorTag.MISSING_ELEMENT, errorType,
                NetconfRpcErrorSeverity.Error,
                String.format(NetconfRpcErrorMessages.EXPECTED_KEYS_IS_MISSING, missingKeys));
        for (String missingKey : missingKeys) {
            error.addErrorInfoElement(NetconfRpcErrorInfo.BadElement, missingKey);
        }
        return error;
    }

    public static NetconfRpcError getBadAttributeError(String badAttributeName, NetconfRpcErrorType errorType, String
            rfcMessageError) {
        return new NetconfRpcError(NetconfRpcErrorTag.BAD_ATTRIBUTE, NetconfRpcErrorType.Application,
                NetconfRpcErrorSeverity.Error,
                rfcMessageError).addErrorInfoElement(NetconfRpcErrorInfo.BadAttribute, badAttributeName);
    }

    public static NetconfRpcError getApplicationError(String errorMsg) {
        return new NetconfRpcError(NetconfRpcErrorTag.OPERATION_FAILED, NetconfRpcErrorType.Application,
                NetconfRpcErrorSeverity.Error,
                errorMsg);
    }
}
