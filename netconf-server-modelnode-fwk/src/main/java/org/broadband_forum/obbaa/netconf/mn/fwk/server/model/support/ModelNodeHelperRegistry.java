package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.Map;

public interface ModelNodeHelperRegistry {

	public boolean isRegistered(SchemaPath modelNodeSchemaPath);

    public boolean registrationComplete(SchemaPath modelNodeSchemaPath);

    public Map<QName, ChildListHelper> getChildListHelpers(SchemaPath modelNodeSchemaPath);

    public void registerChildListHelper(String componentId, SchemaPath modelNodeSchemaPath, QName name, ChildListHelper helper);

	public ChildListHelper getChildListHelper(SchemaPath modelNodeSchemaPath, QName helperName);

	public void registerChildContainerHelper(String componentId, SchemaPath modelNodeSchemaPath, QName name, ChildContainerHelper helper);

	public ChildContainerHelper getChildContainerHelper(SchemaPath modelNodeSchemaPath, QName helperName);

	public Map<QName, ChildContainerHelper> getChildContainerHelpers(SchemaPath modelNodeSchemaPath);

	public void registerNaturalKeyHelper(String componentId, SchemaPath modelNodeSchemaPath, QName name, ConfigAttributeHelper helper);

	public ConfigAttributeHelper getNaturalKeyHelper(SchemaPath modelNodeSchemaPath, QName helperName);

	public void registerConfigAttributeHelper(String componentId, SchemaPath modelNodeSchemaPath, QName name, ConfigAttributeHelper helper);

	public ConfigAttributeHelper getConfigAttributeHelper(SchemaPath modelNodeSchemaPath, QName helperName);

    public Map<QName, ConfigAttributeHelper> getConfigAttributeHelpers(SchemaPath modelNodeSchemaPath);

    public Map<QName, ConfigAttributeHelper> getNaturalKeyHelpers(SchemaPath modelNodeSchemaPath);

    public void registerModelNodeFactory(String factoryName, ModelNodeFactory modelNodeFactory) throws ModelNodeFactoryException;

    public ModelNodeFactory getCreateFactory(String factoryName);

    public ChildLeafListHelper getConfigLeafListHelper(SchemaPath modelNodeSchemaPath, QName helperName);

	public Map<QName, ChildLeafListHelper> getConfigLeafListHelpers(SchemaPath modelNodeSchemaPath);

	public void registerConfigLeafListHelper(String componentId, SchemaPath modelNodeSchemaPath, QName name, ChildLeafListHelper childLeafListHelper) ;

	public DefaultCapabilityCommandInterceptor getDefaultCapabilityCommandInterceptor();

	public void setDefaultCapabilityCommandInterceptor(DefaultCapabilityCommandInterceptor defaultCapabilityCommandInterceptor);

	void undeploy(String componentId);

	void resetDefaultCapabilityCommandInterceptor();
}
