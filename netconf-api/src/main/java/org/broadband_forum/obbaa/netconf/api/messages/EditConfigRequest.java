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

import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.EDIT_CONFIG;

import java.util.HashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Netconf request to perform {@code <edit-config> } operation.
 * 
 * @see {@link EditConfigElement}, {@link EditConfigDefaultOperations}, {@link EditConfigErrorOptions}, {@link EditConfigOperations},
 *      {@link EditConfigTestOptions} for more info.
 * 
 * 
 */
public class EditConfigRequest extends AbstractNetconfRequest {
    private String m_target = StandardDataStores.RUNNING;
    // TODO: FNMS-10109 when advertises the :validate:1.1 capability change defaults to TEST_THEN_SET
    private String m_testOption = EditConfigTestOptions.SET;
    private String m_errorOption = EditConfigErrorOptions.STOP_ON_ERROR;

    private EditConfigElement m_configElement;
    private String m_defaultOperation = EditConfigDefaultOperations.MERGE;
    private boolean m_uploadToPmaRequest = false;
    private boolean m_withTransactionId = false;
    private boolean m_triggerSyncUponSuccess = false;
    private final Map<String, Object> m_context = new HashMap<>();
    private String m_reqXmlStrCopy = null;

    public String getTarget() {
        return m_target;
    }

    public EditConfigRequest setTarget(String target) {
        this.m_target = target;
        return this;
    }

    public EditConfigRequest setTargetRunning() {
        setTarget(StandardDataStores.RUNNING);
        return this;
    }

    public String getTestOption() {
        return m_testOption;
    }

    public EditConfigRequest setTestOption(String testOption) {
        this.m_testOption = testOption;
        return this;
    }

    public String getErrorOption() {
        return m_errorOption;
    }

    public EditConfigRequest setErrorOption(String errorOption) {
        this.m_errorOption = errorOption;
        return this;
    }

    public EditConfigElement getConfigElement() {
        return m_configElement;
    }

    public EditConfigRequest setConfigElement(EditConfigElement configElement) {
        this.m_configElement = configElement;
        return this;
    }

    @Override
    public Document getRequestDocumentInternal() throws NetconfMessageBuilderException {
        Document doc = new PojoToDocumentTransformer().newNetconfRpcDocument(m_messageId)
                .addEditConfigElement(m_withTransactionId, m_target, m_defaultOperation, m_testOption, m_errorOption, m_withDelay, m_configElement, m_userContext, m_contextSessionId).build();
        return doc;
    }

    @Override
    public String getRpcType() {
        return EDIT_CONFIG;
    }

    public EditConfigRequest setDefaultOperation(String defaultOperation) {
        this.m_defaultOperation = defaultOperation;
        return this;
    }

    public String getDefaultOperation() {
        return this.m_defaultOperation;
    }

    /**
     * Indicate this edit-config request is internally generated towards PMA upload
     */
    public void setUploadToPmaRequest() {
        m_uploadToPmaRequest = true;
    }

    public void setUploadToPmaRequest(boolean uploadToPmaRequest) {
        m_uploadToPmaRequest = uploadToPmaRequest;
    }

    /**
     * Is this a PMA Upload request?
     * 
     * @return true if pmaUpload request
     */
    public boolean isUploadToPmaRequest() {
        return m_uploadToPmaRequest;
    }

    public void setTransactionId(boolean withTransactionId) {
        m_withTransactionId = withTransactionId;
    }

    public boolean getWithTransactionId() {
        return m_withTransactionId;
    }

    public boolean isTriggerSyncUponSuccess() {
		return m_triggerSyncUponSuccess;
	}

	public void setTriggerSyncUponSuccess(boolean triggerSyncUponSuccess) {
		this.m_triggerSyncUponSuccess = triggerSyncUponSuccess;
	}

    public Object getContextValue(String key) {
        return m_context.get(key);
    }

    public Map<String, Object> getContextMap() {
        return m_context;
    }

    public void addContextValue(String key, Object value) {
        m_context.put(key, value);
    }

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_configElement == null) ? 0 : m_configElement.hashCode());
        result = prime * result + ((m_reqXmlStrCopy == null) ? 0 : m_reqXmlStrCopy.hashCode());
        result = prime * result + ((m_defaultOperation == null) ? 0 : m_defaultOperation.hashCode());
        result = prime * result + ((m_errorOption == null) ? 0 : m_errorOption.hashCode());
        result = prime * result + ((m_target == null) ? 0 : m_target.hashCode());
        result = prime * result + ((m_testOption == null) ? 0 : m_testOption.hashCode());
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
        EditConfigRequest other = (EditConfigRequest) obj;
        if (m_configElement == null) {
            if (other.m_configElement != null)
                return false;
        } else if (!m_configElement.equals(other.m_configElement))
            return false;
        if (m_reqXmlStrCopy == null) {
            if (other.m_reqXmlStrCopy != null)
                return false;
        } else if (!m_reqXmlStrCopy.equals(other.m_reqXmlStrCopy))
            return false;
        if (m_defaultOperation == null) {
            if (other.m_defaultOperation != null)
                return false;
        } else if (!m_defaultOperation.equals(other.m_defaultOperation))
            return false;
        if (m_errorOption == null) {
            if (other.m_errorOption != null)
                return false;
        } else if (!m_errorOption.equals(other.m_errorOption))
            return false;
        if (m_target == null) {
            if (other.m_target != null)
                return false;
        } else if (!m_target.equals(other.m_target))
            return false;
        if (m_testOption == null) {
            if (other.m_testOption != null)
                return false;
        } else if (!m_testOption.equals(other.m_testOption))
            return false;
        return true;
    }

    public void unsetConfigElement() {
        m_configElement = null;
    }

    public void setConfigElement() throws NetconfMessageBuilderException {
        Element configElement = DocumentUtils.stringToDocument(m_reqXmlStrCopy).getDocumentElement();
        m_configElement = new EditConfigElement()
                .setConfigElementContents(DocumentUtils.getChildElements(configElement));
    }

    public String getReqXmlStrCopy(){
        return m_reqXmlStrCopy;
    }

    public void setReqXmlStrCopy() throws NetconfMessageBuilderException {
        m_reqXmlStrCopy = DocumentUtils.documentToString(getConfigElement().getXmlElement());
        unsetConfigElement();
    }
}
