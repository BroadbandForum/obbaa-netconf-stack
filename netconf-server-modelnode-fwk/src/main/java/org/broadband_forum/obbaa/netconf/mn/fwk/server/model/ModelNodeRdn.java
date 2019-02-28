package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil.SINGLE_QUOTE;

import java.io.Serializable;

import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.CharacterCodec;

public class ModelNodeRdn implements Comparable<ModelNodeRdn>, Immutable, Serializable {

	private static final long serialVersionUID = 2403429120956800037L;

	public static final String CONTAINER = "container";
	public static final String NAME = "name";

	private String m_rdnName;
	private String m_namespace;
    private String m_rdnValue;

    public ModelNodeRdn(String rdnName, String namespace, String rdnValue) {
        this.m_rdnName = rdnName;
        this.m_namespace = namespace;
        if (rdnValue == null) {
            rdnValue = "";
        }
        this.m_rdnValue = rdnValue;
    }

    public ModelNodeRdn(QName qname, String value) {
        this(qname.getLocalName(), qname.getNamespace().toString(), value);
    }

    public ModelNodeRdn clone() {
        return new ModelNodeRdn(m_rdnName, m_namespace, m_rdnValue);
    }

    public String getRdnName() {
        return this.m_rdnName;
    }

    public String getNamespace() {
        return this.m_namespace;
    }

    public String getRdnValue() {
        return this.m_rdnValue;
    }

    public String getRdnValueForXPath() {
        StringBuilder builder = new StringBuilder();
        if (!m_rdnValue.startsWith(SINGLE_QUOTE)) {
            builder.append(SINGLE_QUOTE);
        }
        builder.append(m_rdnValue);
        if (!m_rdnValue.endsWith(SINGLE_QUOTE)) {
            builder.append(SINGLE_QUOTE);
        }
        return builder.toString();
    }

    public void setNamespace(String namespace) {
        m_namespace = namespace;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.m_rdnName == null) ? 0 : this.m_rdnName.hashCode());
        result = prime * result + ((this.m_rdnValue == null) ? 0 : this.m_rdnValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ModelNodeRdn other = (ModelNodeRdn) obj;
        if (this.m_rdnName == null) {
            if (other.m_rdnName != null) {
                return false;
            }
        } else if (!this.m_rdnName.equals(other.m_rdnName)) {
            return false;
        }
        if (this.m_rdnValue == null) {
            if (other.m_rdnValue != null) {
                return false;
            }
        } else if (!this.m_rdnValue.equals(other.m_rdnValue)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "/" + this.m_rdnName + "=" + this.m_rdnValue;
    }

    @Override
    public int compareTo(ModelNodeRdn other) {
        int diff = m_rdnName.compareTo(other.getRdnName());
        if (diff != 0) {
            return diff;
        }
        diff = m_namespace.compareTo(other.getNamespace());
        if (diff != 0) {
            return diff;
        }
        return m_rdnValue.compareTo(other.getRdnValue());
    }


    public boolean containerMatches(Element node) {
        if (CONTAINER.equals(getRdnName())) {
            if (getNamespace().equals(node.getNamespaceURI()) && getRdnValue().equals(node.getLocalName())) {
                return true;
            }
        }
        return false;
    }

    public String encodedString() {
        return "/" + this.m_rdnName + "=" + CharacterCodec.encode(this.m_rdnValue);
    }
}
