package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.w3c.dom.Document;


public class GetContext extends GetConfigContext {
    private StateAttributeGetContext m_stateAttributeContext;

    public GetContext(Document doc, FilterNode filter, StateAttributeGetContext getStateAttributeContext) {
        super(doc, filter);
        m_stateAttributeContext = getStateAttributeContext;
    }

    public StateAttributeGetContext getStateAttributeContext() {
        return m_stateAttributeContext;
    }

    public void setStateAttributeContext(StateAttributeGetContext stateAttributeContext) {
        this.m_stateAttributeContext = stateAttributeContext;
    }

}