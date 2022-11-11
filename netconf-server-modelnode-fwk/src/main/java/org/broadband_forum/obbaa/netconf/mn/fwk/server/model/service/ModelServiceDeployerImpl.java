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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.map.HashedMap;
import org.broadband_forum.obbaa.netconf.api.client.InternalNetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigElement;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigTestOptions;
import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryTraverser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryVisitor;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaVerifierImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.DataStore;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigTestFailedException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.LockedByOtherSessionException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.NetconfServer;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.RpcRequestHandlerRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.PersistenceException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeGetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeInitException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeSetException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.RootModelNodeAggregator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.EntityRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.ModelNodeHelperDeployer;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.LockServiceException;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockService;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.SchemaMountUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.WriteLockTemplate;
import org.broadband_forum.obbaa.netconf.persistence.DataStoreMetaProvider;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.server.rpc.MultiRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcRequestHandler;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Element;

public class ModelServiceDeployerImpl implements ModelServiceDeployer {

    protected final SchemaRegistry m_schemaRegistry;

    protected ModelNodeDSMRegistry m_modelNodeDSMRegistry;
	
	protected ModelNodeHelperRegistry m_modelNodeHelperRegistry;
	
	protected SubSystemRegistry m_subSystemRegistry;
	
	private RpcRequestHandlerRegistry m_rpcRequestHandlerRegistry;

    protected ModelNodeHelperDeployer m_modelNodeHelperDeployer;

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ModelServiceDeployerImpl.class, LogAppNames.NETCONF_STACK);
    private RootModelNodeAggregator m_rootModelNodeAggregator;
    private EntityRegistry m_entityRegistry;

    protected Set<DeployListener> m_deployListener = new ConcurrentHashMap<>().newKeySet();
    private DataStoreMetaProvider m_dataStoreMetadataProvider;
    private NetconfServer m_netconfServer;
    protected ReadWriteLockService m_readWriteLockService;

    /**
	 * construct ModelServiceDeployer with sysSystemRegistry, RpcRequestHandlerRegistry
     * @param subSystemRegistry
     * @param rpcRequestHandlerRegistry
     * @param modelNodeHelperDeployer
     * @param readWriteLockService
     */
	public ModelServiceDeployerImpl(ModelNodeDSMRegistry modelNodeDSMRegistry, ModelNodeHelperRegistry modelNodeHelperRegistry,
                                    SubSystemRegistry subSystemRegistry, RpcRequestHandlerRegistry rpcRequestHandlerRegistry,
                                    ModelNodeHelperDeployer modelNodeHelperDeployer,
                                    SchemaRegistry schemaRegistry, ReadWriteLockService readWriteLockService) {
		super();
		this.m_modelNodeDSMRegistry = modelNodeDSMRegistry;
		this.m_modelNodeHelperRegistry = modelNodeHelperRegistry;
		this.m_subSystemRegistry = subSystemRegistry;
		this.m_rpcRequestHandlerRegistry = rpcRequestHandlerRegistry;
		m_modelNodeHelperDeployer = modelNodeHelperDeployer;
        // deployHelpers NETCONF base namespace by default
        m_schemaRegistry = schemaRegistry;
        m_readWriteLockService = readWriteLockService;
	}

    @Override
    public void postStartup(ModelService service)  throws ModelServiceDeployerException {
        LOGGER.info("performing postStartup steps for modelservice {}", service);
        if (m_dataStoreMetadataProvider != null) {
            long dsVersion = m_dataStoreMetadataProvider.getDataStoreVersion(service.getName());
            if (dsVersion < 1) {
                try {
                    RequestScope.withScope(new RequestScope.RsTemplate<Void>() {
                        @Override
                        protected Void execute() throws RequestScopeExecutionException {
                            List<Element> configElements = service.getDefaultSubtreeRootNodes();
                            if (configElements != null && !configElements.isEmpty()) {
                                EditConfigRequest request = new EditConfigRequest()
                                        .setTargetRunning()
                                        .setTestOption(EditConfigTestOptions.SET)
                                        .setErrorOption(EditConfigErrorOptions.STOP_ON_ERROR)
                                        .setConfigElement(new EditConfigElement()
                                                .setConfigElementContents(configElements));
                                request.setMessageId("1");
                                DataStore store = m_netconfServer.getDataStore(request.getTarget());
                                try {
                                    LOGGER.info("sending default XML edit for modelservice {}, edit-request {}", service, request.requestToString());
                                    NetconfClientInfo clientInfo = new InternalNetconfClientInfo(NetconfResources.IMPLICATION_CHANGE, 0);
                                    NetConfResponse response = new NetConfResponse().setMessageId(request.getMessageId());
                                    store.edit(request, response, clientInfo);
                                    m_dataStoreMetadataProvider.updateDataStoreVersion(service.getName(), 1);
                                } catch (EditConfigException | PersistenceException | EditConfigTestFailedException | LockedByOtherSessionException e) {
                                    throw new RequestScopeExecutionException(new ModelServiceDeployerException("Error while deploying application " + service.getName(), e));
                                }
                            } else {
                                LOGGER.info("config elements is null or empty for service {}", service.getName());
                            }
                            return null;
                        }
                    });
                } catch (RequestScope.RsTemplate.RequestScopeExecutionException e) {
                    Throwable cause = e.getCause();
                    if (cause instanceof ModelServiceDeployerException) {
                        throw (ModelServiceDeployerException) cause;
                    }
                } finally {
                    LOGGER.debug("The contents of Datastore for db_version table post startup are: {}", m_dataStoreMetadataProvider.getAllDataStoreVersions());
                }
            }
        }
    }

    public ModelNodeDSMRegistry getModelNodeDSMRegistry() {
		return m_modelNodeDSMRegistry;
	}

	public void setModelNodeDSMRegistry(ModelNodeDSMRegistry modelNodeDSMRegistry) {
		m_modelNodeDSMRegistry = modelNodeDSMRegistry;
	}

	public ModelNodeHelperRegistry getModelNodeHelperRegistry() {
		return m_modelNodeHelperRegistry;
	}

	public void setModelNodeHelperRegistry(ModelNodeHelperRegistry modelNodeHelperRegistry) {
		m_modelNodeHelperRegistry = modelNodeHelperRegistry;
	}

	public SubSystemRegistry getSubSystemRegistry() {
		return m_subSystemRegistry;
	}

	public void setSubSystemRegistry(SubSystemRegistry subSystemRegistry) {
		this.m_subSystemRegistry = subSystemRegistry;
	}

	public RpcRequestHandlerRegistry getRpcRequestHandlerRegistry() {
		return m_rpcRequestHandlerRegistry;
	}

	public void setRpcRequestHandlerRegistry(RpcRequestHandlerRegistry rpcRequestHandlerRegistry) {
		this.m_rpcRequestHandlerRegistry = rpcRequestHandlerRegistry;
	}

    @Override
    public synchronized void deploy(List<ModelService> services) throws ModelServiceDeployerException {
	    try{
            RequestScope.withScope(new RequestScope.RsTemplate<Void>(){
                @Override
                protected Void execute() throws RequestScopeExecutionException {
                    LOGGER.info("deploying modelservices {}, deployer hashcode: {}", services, hashCode());
                    try {
                        m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                            @Override
                            public Void execute() throws LockServiceException {
                                try {
                                    // Trigger listeners to perform pre-deploy actions
                                    for (DeployListener listener : m_deployListener) {
                                        listener.preDeploy();
                                    }
                                    for(ModelService service : services){
                                        deploy(service);
                                    }
                                    // Trigger listeners to perform post-deploy actions
                                    for (DeployListener listener : m_deployListener) {
                                        listener.postDeploy();
                                    }
                                    for(ModelService service : services){
                                        addRootNodeHelpers(service);
                                    }
                                    for(ModelService service : services){
                                        postStartup(service);
                                    }
                                    for(ModelService service : services){
                                        for(SubSystem ss : service.getSubSystems().values()){
                                            ss.appDeployed();
                                        }
                                        SubSystem defaultSubsystem = service.getDefaultSubsystem();
                                        if(defaultSubsystem != null){
                                            defaultSubsystem.appDeployed();
                                        }
                                    }

                                } catch (Exception e) {
                                    throw new LockServiceException(e);
                                }
                                return null;
                            }
                        });
                    } catch (LockServiceException e) {
                        LOGGER.error("Deploy failed, trying to undeploy model services {} to clear partially deployed services", services, e);
                        undeploy(services);
                        throw e;
                    }
                    LOGGER.info("deploying modelservices {} done, deployer hashcode: {}", services, hashCode());
                    return null;
                }
            });
        }catch (LockServiceException e) {
            if(e.getCause() instanceof ModelServiceDeployerException){
                throw (ModelServiceDeployerException) e.getCause();
            }
            throw e;
        }
    }

    /**
	 * deployHelpers following modelservice SubSystem, ModelMode  RpcRequestHandlers of model service to registry
	 * @param service
	 * @throws ModelNodeInitException
	 * @throws ModelNodeGetException
	 * @throws ModelNodeSetException 
	 * @throws PersistenceException 
	 */
    protected void deploy(ModelService service) throws ModelServiceDeployerException {
        LOGGER.info("updating registries for modelservice {}",service);
        try {

            Map<String, SchemaPath> appAugmentedPaths = service.getAppAugmentedPaths();
        	for(String xpath : appAugmentedPaths.keySet()){
        		m_schemaRegistry.registerAppAllowedAugmentedPath(service.getModuleName(), xpath, appAugmentedPaths.get(xpath));
        	}

           loadSchemaContext(service);

        	//deploy subsystems
            Optional<Module> deployedModule = m_schemaRegistry.getModule(service.getModuleName(), Revision.of(service.getModuleRevision()));

            if(deployedModule.isPresent()){
                updateRegistries(service, deployedModule.get());
                deployRpcHandlers(service, deployedModule.get());
            }
            else {
                LOGGER.warn("No YANG module defined by ModelService {}", service.getName());
            }

        } catch (SchemaBuildException | AnnotationAnalysisException e) {
            LOGGER.error("Error while deploying service {}",service.getName(), e);
            throw new ModelServiceDeployerException(e);
        }

    }

    protected void loadSchemaContext(ModelService service) throws SchemaBuildException {
        if(service.updateSchema()) {
            List<YangTextSchemaSource> yangByteSources = service.getYangModuleByteSources();
            m_schemaRegistry.loadSchemaContext(service.getName(), yangByteSources, service.getSupportedFeatures(),
                    service.getSupportedDeviations());
        }
    }

    public void addRootNodeHelpers(ModelService service) {
        LOGGER.info("adding root node helpers for modelservice {}",service);
        List<SchemaPath> subtreeRootPaths = new ArrayList<>();
        
        List<SchemaPath> rootNodes = SchemaRegistryUtil.getModuleSubtreeRoots(m_schemaRegistry, service.getModuleName(), service.getModuleRevision());
        for ( SchemaPath rootNodePath : rootNodes){
            DataSchemaNode rootNode = m_schemaRegistry.getDataSchemaNode(rootNodePath);
            if(!(rootNode instanceof ListSchemaNode && !rootNode.isConfiguration())){ // We just need to omit the state list nodes. 
                subtreeRootPaths.add(rootNodePath);
            }
        }
        SchemaMountUtil.addRootNodeHelpers(m_schemaRegistry, m_subSystemRegistry, m_modelNodeHelperRegistry, service.getModelNodeDSM(), subtreeRootPaths, m_rootModelNodeAggregator);
    }

    private void undeploy(ModelService service) throws ModelServiceDeployerException {

		String componentId = service.getName();

		m_modelNodeDSMRegistry.undeploy(componentId);
		m_modelNodeHelperRegistry.undeploy(componentId);
		m_subSystemRegistry.undeploy(componentId);
		m_rpcRequestHandlerRegistry.undeploy(componentId);
		Set<MultiRpcRequestHandler> multiRpcRequestHandlers = service.getMultiRpcRequestHandlers();
        for (MultiRpcRequestHandler multiRpcHandler : multiRpcRequestHandlers) {
            m_rpcRequestHandlerRegistry.undeployMultiRpcRequestHandler(multiRpcHandler);
        }
        m_entityRegistry.undeploy(componentId);
        m_schemaRegistry.deRegisterNodesReferencedInConstraints(componentId);
        unloadSchemaContext(service, componentId);
        Map<String, SchemaPath> appAugmentedPaths = service.getAppAugmentedPaths();
		for(String xpath : appAugmentedPaths.keySet()){
			m_schemaRegistry.deRegisterAppAllowedAugmentedPath(xpath);
		}
		m_schemaRegistry.unregisterMountPointSchemaPath(componentId);
    }

    protected void unloadSchemaContext(ModelService service, String componentId) throws ModelServiceDeployerException {
        try {
            m_schemaRegistry.unloadSchemaContext(componentId, service.getSupportedFeatures(), service.getSupportedDeviations());
        } catch (SchemaBuildException e) {
            LOGGER.error("Error while undeploying service {}",service.getName(), e);
            throw new ModelServiceDeployerException("Error while undeploying service "+service.getName(),e);
        }
    }

    @Override
    public synchronized void undeploy(List<ModelService> services) {
        RequestScope.withScope(new RequestScope.RsTemplate<Void>() {

            @Override
            protected Void execute() throws RequestScopeExecutionException {
                LOGGER.info("un-deploying modelservices {}", services);
                List<ModelService> servicesInReverseOrder = new ArrayList<ModelService>(services);
                Collections.reverse(servicesInReverseOrder);
                for(ModelService service : servicesInReverseOrder){
                    removeRootNodeHelpers(service);
                }
                // Trigger listeners to perform pre-undeploy actions
                for (DeployListener listener : m_deployListener) {
                    try {
                        listener.preUndeploy();
                    } catch (Exception e) {
                        LOGGER.warn("Pre-undeploy for the listener [{}] failed!! ", listener, e);
                    }
                }
                for(ModelService service : servicesInReverseOrder){
                    try {
                        undeploy(service);
                    } catch (Exception e) {
                        LOGGER.error("Un-deploying modelservice [{}] failed!! ", service.getName(), e);
                    }
                }
                // Trigger listeners to perform post-undeploy actions
                for (DeployListener listener : m_deployListener) {
                    try {
                        listener.postUndeploy();
                    } catch (Exception e){
                        LOGGER.warn("Post-undeploy for the listener [{}] failed!! ", listener, e);
                    }
                }

                for (ModelService service : services) {
                    for (SubSystem ss : service.getSubSystems().values()) {
                        try {
                            ss.appUndeployed();
                        } catch (Exception e) {
                            LOGGER.warn("Un-deploy for [{}] failed!! ", ss, e);
                        }
                    }
                    SubSystem defaultSubsystem = service.getDefaultSubsystem();
                    if (defaultSubsystem != null) {
                        try {
                            defaultSubsystem.appUndeployed();
                        } catch (Exception e) {
                            LOGGER.warn("Un-deploy for [{}] failed!! ", defaultSubsystem, e);
                        }
                    }
                }
                LOGGER.info("un-deploying modelservices {} done", services);
                return null;
            }
        });
    }

    private void removeRootNodeHelpers(ModelService service) {
        List<SchemaPath> subtreeRootPaths = new ArrayList<>();
        subtreeRootPaths.addAll(SchemaRegistryUtil.getModuleSubtreeRoots(m_schemaRegistry, service.getModuleName(),
                service.getModuleRevision()));
        for(SchemaPath rootSchemaPath : subtreeRootPaths){
            m_rootModelNodeAggregator.removeModelServiceRootHelpers(rootSchemaPath);
        }
    }

    protected void updateRegistries(ModelService service, Module deployedModule) throws AnnotationAnalysisException, SchemaBuildException {
        //the order is important here.
        if(m_entityRegistry !=null && service.getEntityClasses() != null && !service.getEntityClasses().isEmpty()){
            m_entityRegistry.updateRegistry(service.getName(), service.getEntityClasses(), m_schemaRegistry, service.getEntityDSM(), m_modelNodeDSMRegistry);
        }

        //register the module revision
		String componentId = service.getName();
        List<SchemaRegistryVisitor> visitors = getRegistryVisitors(service);

        SchemaRegistryTraverser traverser = new SchemaRegistryTraverser(componentId, visitors, m_schemaRegistry, deployedModule);
        traverser.traverse();
        SchemaVerifierImpl verifier = new SchemaVerifierImpl();
        verifier.verify(m_schemaRegistry);

    }

    protected List<SchemaRegistryVisitor> getRegistryVisitors(ModelService service) {
        Map<SchemaPath, SubSystem> subSystems = getSubSystems(service);
        ModelNodeDataStoreManager dsmToUse = service.getModelNodeDSM();

        List<SchemaRegistryVisitor> visitors = new ArrayList<>();
        visitors.add(new SubsystemDeployer(m_subSystemRegistry, subSystems));
        visitors.add(new ModelNodeDSMDeployer(m_modelNodeDSMRegistry, dsmToUse));
        visitors.add(m_modelNodeHelperDeployer);
        visitors.add(new SchemaPathRegistrar(m_schemaRegistry, m_modelNodeHelperRegistry));
        return visitors;
    }

    protected Map<SchemaPath, SubSystem> getSubSystems(ModelService service) {
        Map<SchemaPath, SubSystem> subsystems = new HashedMap();
        if(service.getDefaultSubsystem() != null){
            List<SchemaPath> moduleSubtreeRoots = SchemaRegistryUtil.getModuleSubtreeRoots(m_schemaRegistry, service.getModuleName(),
                    service.getModuleRevision());
            for(SchemaPath subtreeRootPath : moduleSubtreeRoots){
                subsystems.put(subtreeRootPath, service.getDefaultSubsystem());
            }
        }
        //override specific subsystems
        subsystems.putAll(service.getSubSystems());
        return subsystems;
    }

    protected void deployRpcHandlers(ModelService service, Module deployedModule) {
        if (deployedModule == null)
            return;

        //deployHelpers rpcRequestHanders of model service
        Map<RpcName, RpcDefinition> rpcDefinitionMap = new HashMap<RpcName, RpcDefinition>();
		String componentId;
        Set<RpcDefinition> rpcDefinitions = deployedModule.getRpcs();
        for (RpcDefinition rpcDefinition : rpcDefinitions) {
            rpcDefinitionMap.put(new RpcName(rpcDefinition.getQName().getNamespace().toString(), rpcDefinition.getQName().getLocalName()), rpcDefinition);
        }

        // Get the RPC yang definition which is defined in Yang Modules
        Map<String, String> rpcYangModuleNames =  service.getRpcModuleNames();
        rpcYangModuleNames.forEach((key,value)->{
        	Optional<Module> module = m_schemaRegistry.getModule(key, Revision.of(value));
        	if(module.isPresent()) {
        		Set<RpcDefinition> rpcs = module.get().getRpcs();
        		for (RpcDefinition rpcDefinition : rpcs) {
        			rpcDefinitionMap.put(new RpcName(rpcDefinition.getQName().getNamespace().toString(), rpcDefinition.getQName().getLocalName()), rpcDefinition);
        		}
        	}
        });

        Set<RpcRequestHandler> rpcRequestHandlers = service.getRpcRequestHandlers();
        for (RpcRequestHandler handler : rpcRequestHandlers) {
            RpcDefinition rpcDefinition = rpcDefinitionMap.get(handler.getRpcQName());
            handler.setRpcDefinition(rpcDefinition);
			componentId = service.getName();
            m_rpcRequestHandlerRegistry.register(componentId,handler.getRpcQName(), handler);
        }
        
        Set<MultiRpcRequestHandler> multiRpcRequestHandlers = service.getMultiRpcRequestHandlers();
        for (MultiRpcRequestHandler multiRpcHandler : multiRpcRequestHandlers) {
            m_rpcRequestHandlerRegistry.registerMultiRpcRequestHandler(multiRpcHandler);
        }
    }

    public RootModelNodeAggregator getRootModelNodeAggregator() {
        return m_rootModelNodeAggregator;
    }

    public void setRootModelNodeAggregator(RootModelNodeAggregator rootModelNodeAggregator) {
        m_rootModelNodeAggregator = rootModelNodeAggregator;
    }

    public EntityRegistry getEntityRegistry() {
        return m_entityRegistry;
    }

    public void setEntityRegistry(EntityRegistry entityRegistry) {
        m_entityRegistry = entityRegistry;
    }

    @Override
    public void addDeployListener(DeployListener listener) {
        if (!m_deployListener.contains(listener)) {
            m_deployListener.add(listener);
        }
    }
    
    @Override
    public void removeDeployListener(DeployListener listener) {
        if (m_deployListener.contains(listener)) {
            m_deployListener.remove(listener);
        }
    }

    public DataStoreMetaProvider getDataStoreMetadataProvider() {
        return m_dataStoreMetadataProvider;
    }

    public void setDataStoreMetadataProvider(DataStoreMetaProvider dataStoreMetadataProvider) {
        m_dataStoreMetadataProvider = dataStoreMetadataProvider;
    }

    public NetconfServer getNetconfServer() {
        return m_netconfServer;
    }

    public void setNetconfServer(NetconfServer netconfServer) {
        m_netconfServer = netconfServer;
    }

    /**
     * For UT
     * @return
     */
    protected ReadWriteLockService getReadWriteLockService() {
        return m_readWriteLockService;
    }
}
