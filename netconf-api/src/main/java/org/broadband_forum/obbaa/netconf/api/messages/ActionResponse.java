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

import java.util.ArrayList;
import java.util.List;

import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;

public class ActionResponse extends NetConfResponse {

	private List<Element> m_outputElements = new ArrayList<>();

	private ActionDefinition m_actionDefinition;

	public ActionDefinition getActionDefinition() {
		return m_actionDefinition;
	}

	public void setActionDefinition(ActionDefinition actionDefinition) {
		this.m_actionDefinition = actionDefinition;
	}
	
	public List<Element> getActionOutputElements() {
		return m_outputElements;
	}

	public void setActionOutputElements(List<Element> outputElements){
		m_outputElements = outputElements;
	}
	
	@Override
    public Document getResponseDocument() throws NetconfMessageBuilderException {
        synchronized (this){
            PojoToDocumentTransformer responseBuilder = new PojoToDocumentTransformer()
                    .newNetconfRpcReplyDocument(getMessageId(),
                            getOtherRpcAttributes()).addRpcErrors(getErrors());
            if (! m_outputElements.isEmpty()){
            	for (Element outputElement: m_outputElements) {
                    responseBuilder.addRpcElement(outputElement);
                }
            } else if (isOk()) {
                responseBuilder.addOk();
            }
            return responseBuilder.build();
        }
    }

}
