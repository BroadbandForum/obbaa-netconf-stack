package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.RpcRequestHandlerRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.ModelNodeHelperDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockService;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;

public class StaticSchemaRegistryDeployer extends ModelServiceDeployerImpl {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(StaticSchemaRegistryDeployer.class, LogAppNames.NETCONF_STACK);

    public StaticSchemaRegistryDeployer(ModelNodeDSMRegistry modelNodeDSMRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry, SubSystemRegistry subSystemRegistry, RpcRequestHandlerRegistry rpcRequestHandlerRegistry, ModelNodeHelperDeployer modelNodeHelperDeployer, SchemaRegistry schemaRegistry, ReadWriteLockService lockService) {
        super(modelNodeDSMRegistry, modelNodeHelperRegistry, subSystemRegistry, rpcRequestHandlerRegistry, modelNodeHelperDeployer,
                schemaRegistry, lockService);
    }

    @Override
    protected void loadSchemaContext(ModelService service) throws SchemaBuildException {
        LOGGER.info("Not updating schemaRegistry while deploying model service "+service.getName());
    }

    @Override
    protected void unloadSchemaContext(ModelService service, String componentId) throws ModelServiceDeployerException {
        LOGGER.info("Not updating schemaRegistry while undeploying model service "+service.getName());
    }
}
