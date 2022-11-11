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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class SubSystemRegistryImpl implements SubSystemRegistry {
	private ConcurrentHashMap<SchemaPath, SubSystem> m_registry = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, List<SchemaPath>> m_componentMap = new ConcurrentHashMap<>();
	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SubSystemRegistryImpl.class, LogAppNames.NETCONF_STACK);
	private CompositeSubSystem m_globalSubSystem;

	public void register(String componentId, SchemaPath schemaPath, SubSystem subSystem) {
		m_registry.put(schemaPath, subSystem);
		List<SchemaPath> schemaPathList = m_componentMap.get(componentId);
		if(schemaPathList == null){
			schemaPathList = new ArrayList<>();
			m_componentMap.putIfAbsent(componentId, schemaPathList);
			schemaPathList = m_componentMap.get(componentId);
		}
		schemaPathList.add(schemaPath);
		detectAndLogViolation(subSystem);
		LOGGER.debug("Registered a subsystem with schema-path: {}", schemaPath);
	}

	public SubSystem lookupSubsystem(SchemaPath schemaPath) {
		SubSystem system = m_registry.get(schemaPath);
		if (system == null) {
			return NoopSubSystem.c_instance;
		}
		return system;
	}

	@Override
	public void undeploy(String componentId) {

		List<SchemaPath> schemaPathList = m_componentMap.get(componentId);
		if(schemaPathList != null){
			for(SchemaPath schemaPath : schemaPathList){
				SubSystem subSystem = m_registry.remove(schemaPath);
				if(subSystem != null){
					LOGGER.debug("Unregistered a subsystem with schema-path: {}", schemaPath);
				}
			}
		}
		m_componentMap.remove(componentId);
	}

	@Override
	public CompositeSubSystem getCompositeSubSystem() {
		return m_globalSubSystem;
	}

	@Override
	public void setCompositeSubSystem(CompositeSubSystem globalSubSystem) {
		m_globalSubSystem = globalSubSystem;
	}

	@Override
	public SubSystemRegistry unwrap() {
		return this;
	}

	private void detectAndLogViolation(SubSystem subSystem) {
		Method[] declaredMethods = subSystem.getClass().getDeclaredMethods();
		boolean hasNotifyMethodImplimented = false;
		boolean hasNotifyPreCommitMethodImplemented = false;
		boolean hasPreCommitMethodImplemented = false;
		boolean hasPostCommitMethodImplemented = false;
		for (Method declaredMethod : declaredMethods) {
			if(declaredMethod.getName().equals("notifyChanged")){
				hasNotifyMethodImplimented = true;
			}else if(declaredMethod.getName().equals("notifyPreCommitChange")){
				hasNotifyPreCommitMethodImplemented = true;
			}else if(declaredMethod.getName().equals("preCommit")){
				hasPreCommitMethodImplemented = true;
			}else if(declaredMethod.getName().equals("postCommit")){
				hasPostCommitMethodImplemented = true;
			}
		}

		if(hasNotifyMethodImplimented && !hasPostCommitMethodImplemented){
			LOGGER.debug("Post commit not implemented in subsystem {} ", subSystem.getClass().getName());
		}
		if(hasNotifyPreCommitMethodImplemented && !hasPreCommitMethodImplemented){
			LOGGER.debug("Pre commit not implemented in subsystem {} ", subSystem.getClass().getName());
		}
	}
}
