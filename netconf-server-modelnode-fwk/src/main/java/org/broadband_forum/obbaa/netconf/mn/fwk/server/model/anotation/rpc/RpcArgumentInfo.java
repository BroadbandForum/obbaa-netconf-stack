package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc;

/**
 * Created by kbhatk on 8/8/16.
 */
public class RpcArgumentInfo {
    private final Class m_type;
    private final String m_argName;
    private final String m_namespace;

    public RpcArgumentInfo(Class type, String argName, String namespace) {
        m_type = type;
        m_argName = argName;
        m_namespace = namespace;
    }

    public Class getType() {
        return m_type;
    }

    public String getArgName() {
        return m_argName;
    }

    public String getNamespace() {
        return m_namespace;
    }
}
