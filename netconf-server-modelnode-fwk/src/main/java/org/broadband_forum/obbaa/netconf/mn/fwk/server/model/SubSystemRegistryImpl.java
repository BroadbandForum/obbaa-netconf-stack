package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

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

	public void register(String componentId, SchemaPath schemaPath, SubSystem subSystem) {
		m_registry.put(schemaPath, subSystem);
		List<SchemaPath> schemaPathList = m_componentMap.get(componentId);
		if(schemaPathList == null){
			schemaPathList = new ArrayList<>();
			m_componentMap.putIfAbsent(componentId, schemaPathList);
			schemaPathList = m_componentMap.get(componentId);
		}
		schemaPathList.add(schemaPath);

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
}
