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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.WrappedService;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public interface ModelNodeHelperRegistry extends WrappedService<ModelNodeHelperRegistry> {

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

    public ChildLeafListHelper getConfigLeafListHelper(SchemaPath modelNodeSchemaPath, QName helperName);

	public Map<QName, ChildLeafListHelper> getConfigLeafListHelpers(SchemaPath modelNodeSchemaPath);

	public void registerConfigLeafListHelper(String componentId, SchemaPath modelNodeSchemaPath, QName name, ChildLeafListHelper childLeafListHelper) ;

	public DefaultCapabilityCommandInterceptor getDefaultCapabilityCommandInterceptor();

	public void setDefaultCapabilityCommandInterceptor(DefaultCapabilityCommandInterceptor defaultCapabilityCommandInterceptor);

	void undeploy(String componentId);

	void resetDefaultCapabilityCommandInterceptor();

	void clear();
}
