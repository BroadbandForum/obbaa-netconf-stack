package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.toaster;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.server.NetconfQueryParams;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigLeafAttribute;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CopyConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContainmentNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetConfigContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NoopSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.Jukebox;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.XmlUtil;

public class Toaster implements ModelNode {
	
	private static final long serialVersionUID = 1L;

	public static final QName QNAME = QName.create("http://example.com/ns/example-jukebox", "toaster");
	
	private String m_toasterModelNumber = "123-1231-44";
	private String m_toasterManufacturer  ="ALU";
	private ToasterStatus m_toasterStatus = ToasterStatus.down;
	private ModelNodeId m_modelNodeId = new ModelNodeId().addRdn(new ModelNodeRdn("container", Jukebox.NAMESPACE, "toaster"));

	public String getToasterModelNumber() {
		return m_toasterModelNumber;
	}

	public void setToasterModelNumber(String toasterModelNumber) {
		m_toasterModelNumber = toasterModelNumber;
	}

	public String getToasterManufacturer() {
		return m_toasterManufacturer;
	}

	public void setToasterManufacturer(String toasterManufacturer) {
		m_toasterManufacturer = toasterManufacturer;
	}

	public ToasterStatus getToasterStatus() {
		return m_toasterStatus;
	}

	public void setToasterStatus(ToasterStatus toasterStatus) {
		m_toasterStatus = toasterStatus;
	}

    @Override
	public Element get(GetContext parameterObject, NetconfQueryParams context) {
		Element element = parameterObject.getDoc().createElement("toaster");
		XmlUtil.addXmlValue(parameterObject.getDoc(), element, "toasterModelNumber", getToasterModelNumber());
		XmlUtil.addXmlValue(parameterObject.getDoc(), element, "toasterManufacturer", getToasterManufacturer());
		XmlUtil.addXmlValue(parameterObject.getDoc(), element, "toasterStatus", getToasterStatus().name());
		return element;
	}

    @Override
	public Element getConfig(GetConfigContext parameterObject, NetconfQueryParams context) {
		Element element = parameterObject.getDoc().createElement("toaster");
		return element;
	}
	
	@Override
    public void prepareEditSubTree(EditContainmentNode root, Element configElementContents) throws EditConfigException {
        throw new RuntimeException("not supported");
    }


	@Override
	public SubSystem getSubSystem() {
		return NoopSubSystem.c_instance;
	}

    @Override
    public SubSystem getSubSystem(SchemaPath schemaPath) {
        return null;
    }

    @Override
    public void editConfig(EditContext editData) throws EditConfigException {

    }

    @Override
    public void copyConfig(Element config) throws CopyConfigException {

    }

	@Override
	public SchemaPath getModelNodeSchemaPath() {
		return null;
	}

	@Override
	public ModelNodeId getModelNodeId() {
		return m_modelNodeId;
	}

	@Override
	public String getContainerName() {
		return null;
	}

	@Override
	public QName getQName() {
		return QNAME;
	}

	@Override
	public ModelNode getParent() {
		//root node
		return null;
	}

	@Override
	public SchemaRegistry getSchemaRegistry() {
		return null;
	}

	@Override
	public Map<QName, String> getListKeys() throws GetAttributeException {
		return Collections.emptyMap();
	}
	
	@Override
	public Object getValue() {
		return null;
	}

	@Override
	public void setValue(Object value) {
	}

	@Override
	public boolean isRoot() {
		return true;
	}

    @Override
    public SchemaRegistry getMountRegistry() {
        return null;
    }

    @Override
    public ModelNodeHelperRegistry getMountModelNodeHelperRegistry() {
        return null;
    }

    @Override
    public SubSystemRegistry getMountSubSystemRegistry() {
        return null;
    }

    @Override
    public boolean hasSchemaMount() {
        return false;
    }

    @Override
    public boolean isSchemaMountImmediateChild() {
        return false;
    }

    @Override
    public SchemaPath getParentMountPath() {
        return null;
    }

    @Override
    public ModelNodeDataStoreManager getModelNodeDSM() {
        return null;
    }

	@Override
	public Set<ConfigLeafAttribute> getLeafList(QName qName) {
		return null;
	}

	@Override
	public Map<QName, LinkedHashSet<ConfigLeafAttribute>> getLeafLists() {
		return null;
	}
}
