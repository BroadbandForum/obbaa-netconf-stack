package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

/**
 * Created by sgs on 2/13/17.
 */
public class InstanceIdentifierAttributeComponent {

    private String m_prefix;
    private String m_value;
    private String m_namespace;

    public InstanceIdentifierAttributeComponent(String namespace, String prefix, String value) {
        this.m_namespace = namespace;
        this.m_prefix = prefix;
        this.m_value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        InstanceIdentifierAttributeComponent that = (InstanceIdentifierAttributeComponent) o;

        if (!m_value.equals(that.m_value))
            return false;
        return m_namespace.equals(that.m_namespace);

    }

    @Override
    public int hashCode() {
        int result = m_value.hashCode();
        result = 31 * result + m_namespace.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "InstanceIdentifierAttributeComponent{" +
                "m_value='" + m_value + '\'' +
                ", m_namespace='" + m_namespace + '\'' +
                '}';
    }
}
