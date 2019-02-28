package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.w3c.dom.Document;

public class GetConfigContext {
    private Document m_doc;
    private FilterNode m_filter;

    public GetConfigContext(Document doc, FilterNode filter) {
        m_doc = doc;
        m_filter = filter;
    }

    public Document getDoc() {
        return m_doc;
    }

    public void setDoc(Document doc) {
        m_doc = doc;
    }

    public FilterNode getFilter() {
        return m_filter;
    }

    public void setFilter(FilterNode root) {
        m_filter = root;
    }

}