package org.broadband_forum.obbaa.netconf.mn.fwk.schema;


import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.SchemaNodeConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.listener.YangLibraryChangeNotificationListener;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.LockServiceException;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadLockTemplate;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockService;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockServiceImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.WriteLockTemplate;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

/**
 * Created by keshava on 11/18/15.
 */
public class SchemaRegistryImpl implements SchemaRegistry {

    private ReadWriteLockService m_readWriteLockService;
    private DecoratedSchemaRegistryImpl m_innerSchemaRegistry;
    private String m_registryName = "GLOBAL-GLOABAL";

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SchemaRegistryImpl.class, LogAppNames.NETCONF_STACK);

    /*
     * @deprecated You have to use a constructor that passes the yang models, features and deviations from now onwards.
     */
    @Deprecated
    public SchemaRegistryImpl() throws SchemaBuildException {
        this(new ReadWriteLockServiceImpl());
    }

    /*
     * @deprecated You have to use a constructor that passes the yang models, features and deviations from now onwards.
     */
    @Deprecated
    public SchemaRegistryImpl(ReadWriteLockService readWriteLockService) throws SchemaBuildException {
        this(Collections.<YangTextSchemaSource>emptyList(), true, readWriteLockService);
    }

    /*
     * @deprecated You have to use a constructor that passes the yang models, features and deviations from now onwards.
     */
    @Deprecated
    public SchemaRegistryImpl(boolean isYangLibrarySupportedInHelloMessage, ReadWriteLockService readWriteLockService) throws SchemaBuildException {
        this(Collections.<YangTextSchemaSource>emptyList(), isYangLibrarySupportedInHelloMessage,readWriteLockService);
    }

    /*
     * @deprecated You have to use a constructor that passes the yang models, features and deviations from now onwards.
     */
    @Deprecated
    public SchemaRegistryImpl(List<YangTextSchemaSource> coreYangModelFiles, ReadWriteLockService readWriteLockService) throws SchemaBuildException {
        this(coreYangModelFiles, true,readWriteLockService);
    }

    /*
     * @deprecated You have to use a constructor that passes the yang models, features and deviations from now onwards.
     */
    @Deprecated
    public SchemaRegistryImpl(List<YangTextSchemaSource> coreYangModelFiles, boolean isYangLibrarySupportedInHelloMessage, ReadWriteLockService readWriteLockService) throws SchemaBuildException {
        this(coreYangModelFiles, null, null, isYangLibrarySupportedInHelloMessage, readWriteLockService);
    }

    public SchemaRegistryImpl(List<YangTextSchemaSource> coreYangModelFiles, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations, ReadWriteLockService readWriteLockService) throws SchemaBuildException {
        this(coreYangModelFiles, supportedFeatures, supportedDeviations, true,readWriteLockService);
    }

    public SchemaRegistryImpl(List<YangTextSchemaSource> coreYangModelFiles, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations, boolean isYangLibrarySupportedInHelloMessage, ReadWriteLockService readWriteLockService) throws SchemaBuildException {
        m_readWriteLockService = readWriteLockService;
        m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Object>() {
            @Override
            public Object execute() {
                try {
                    m_innerSchemaRegistry =  new DecoratedSchemaRegistryImpl(coreYangModelFiles, supportedFeatures, supportedDeviations, isYangLibrarySupportedInHelloMessage);
                } catch (SchemaBuildException e) {
                    throw new LockServiceException(e);
                }
                return null;
            }
        });

    }

    @Override
    public SchemaContext getSchemaContext() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<SchemaContext>() {
            @Override
            public SchemaContext execute() {
                return m_innerSchemaRegistry.getSchemaContext();
            }
        });
    }

    /*
     * @deprecated You have to use the method that passes the yang models, features and deviations from now onwards.
     */
    @Override
    @Deprecated
    public void buildSchemaContext(final List<YangTextSchemaSource> coreYangModelByteSources) throws SchemaBuildException {
        buildSchemaContext(coreYangModelByteSources, null, null);
    }

    @Override
    public void buildSchemaContext(final List<YangTextSchemaSource> coreYangModelByteSources, Set<QName> supportedFeatures, Map<QName,Set<QName>> supportedDeviations) throws SchemaBuildException {
        buildSchemaContext(coreYangModelByteSources, supportedFeatures, supportedDeviations, true);
    }

    @Override
    public void buildSchemaContext(final List<YangTextSchemaSource> coreYangModelByteSources, Set<QName> supportedFeatures, Map<QName,Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported) throws SchemaBuildException {
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    try {
                        DecoratedSchemaRegistryImpl clonedSchemaRegistryImpl = m_innerSchemaRegistry.deepClone();
                        clonedSchemaRegistryImpl.buildSchemaContext(coreYangModelByteSources,supportedFeatures,supportedDeviations, isYangLibNotificationSupported);
                        m_innerSchemaRegistry = clonedSchemaRegistryImpl;
                    } catch (SchemaBuildException e) {
                        throw new LockServiceException(e);
                    }

                    return null;
                }
            });
        } catch (LockServiceException e) {
            LOGGER.error("Exception while building Schema Context",e);
            throw new SchemaBuildException(e.getCause());
        }
    }
    
    @Override
    public Collection<DataSchemaNode> getRootDataSchemaNodes() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Collection<DataSchemaNode>>() {
            @Override
            public Collection<DataSchemaNode> execute() {
                return m_innerSchemaRegistry.getRootDataSchemaNodes();
            }
        });
    }

    @Override
    public Optional<Module> getModule(final String name){
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Optional<Module>>() {
            @Override
            public Optional<Module> execute() {
                return m_innerSchemaRegistry.getModule(name);
            }
        });
    }

    @Override
    public Optional<Module> getModule(final String name, final Revision revision) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Optional<Module>>() {
            @Override
            public Optional<Module> execute() {
                return m_innerSchemaRegistry.getModule(name, revision);
            }
        });
    }

    @Override
    public Collection<RpcDefinition> getRpcDefinitions() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Collection<RpcDefinition>>() {
            @Override
            public Collection<RpcDefinition> execute() {
                return m_innerSchemaRegistry.getRpcDefinitions();
            }
        });
    }

    @Override
    public RpcDefinition getRpcDefinition(final SchemaPath schemaPath) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<RpcDefinition>() {
            @Override
            public RpcDefinition execute() {
                return m_innerSchemaRegistry.getRpcDefinition(schemaPath);
            }
        });
    }

    @Override
    public synchronized void loadSchemaContext(final String componentId, final List<YangTextSchemaSource> yangModelByteSources, Set<QName> supportedFeatures, Map<QName,Set<QName>> supportedDeviations) throws SchemaBuildException {
        loadSchemaContext(componentId, yangModelByteSources, supportedFeatures, supportedDeviations, true);
    }

    @Override
    public synchronized void loadSchemaContext(final String componentId, final List<YangTextSchemaSource> yangModelByteSources, Set<QName> supportedFeatures, Map<QName,Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported) throws SchemaBuildException {

        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    try {
                        DecoratedSchemaRegistryImpl clonedSchemaRegistry = m_innerSchemaRegistry.deepClone();
                        clonedSchemaRegistry.loadSchemaContext(componentId,yangModelByteSources,supportedFeatures,supportedDeviations, isYangLibNotificationSupported);
                        m_innerSchemaRegistry = clonedSchemaRegistry;
                    } catch (SchemaBuildException e) {
                        throw new LockServiceException(e);
                    }
                    return null;
                }
            });
        }catch (LockServiceException e) {
            LOGGER.error("Exception while building Schema Context",e);
            throw new SchemaBuildException(e);
        }
    }
    
    @Override
    public synchronized void unloadSchemaContext(final String componentId, Set<QName> supportedFeatures, Map<QName,Set<QName>> supportedDeviations) throws SchemaBuildException {
        unloadSchemaContext(componentId, supportedFeatures, supportedDeviations, true);
    }

    @Override
    public synchronized void unloadSchemaContext(final String componentId, Set<QName> supportedFeatures, Map<QName,Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported) throws SchemaBuildException {
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    try{
                        DecoratedSchemaRegistryImpl clonedSchemaRegistry = m_innerSchemaRegistry.deepClone();
                        clonedSchemaRegistry.unloadSchemaContext(componentId,supportedFeatures,supportedDeviations, isYangLibNotificationSupported);
                        m_innerSchemaRegistry = clonedSchemaRegistry;
                    }catch (SchemaBuildException e){

                    }
                    return null;
                }
            });
        } catch (LockServiceException e) {
            LOGGER.error("Error while unloading schema Context", e);
            throw new SchemaBuildException(e.getCause());
        }
    }
    

    @Override
    public Set<ModuleIdentifier> getAllModuleAndSubmoduleIdentifiers() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Set<ModuleIdentifier>>() {
            @Override
            public Set<ModuleIdentifier> execute() {
                return m_innerSchemaRegistry.getAllModuleAndSubmoduleIdentifiers();
            }
        });
    }

    @Override
    public Set<Module> getAllModules() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Set<Module>>() {
            @Override
            public Set<Module> execute() {
                return m_innerSchemaRegistry.getAllModules();
            }
        });
    }

    @Override
    public Map<SourceIdentifier, YangTextSchemaSource> getAllYangTextSchemaSources() throws SchemaBuildException {        
        return m_innerSchemaRegistry.getAllYangTextSchemaSources();
    }


    @Override
    public DataSchemaNode getDataSchemaNode(final SchemaPath dataNodeSchemaPath) {

        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<DataSchemaNode>() {
            @Override
            public DataSchemaNode execute() {
                return m_innerSchemaRegistry.getDataSchemaNode(dataNodeSchemaPath);
            }
        });
    }

    @Override
    public Collection<DataSchemaNode> getChildren(final SchemaPath parentSchemaPath) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Collection<DataSchemaNode>>() {
            @Override
            public Collection<DataSchemaNode> execute() {
                return m_innerSchemaRegistry.getChildren(parentSchemaPath);
            }
        });
    }

    @Override
    public DataSchemaNode getChild(final SchemaPath parentSchemaPath, final QName childQName) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<DataSchemaNode>() {
            @Override
            public DataSchemaNode execute() {
                return m_innerSchemaRegistry.getChild(parentSchemaPath, childQName);
            }
        });
    }

    @Override
    public Map<QName,DataSchemaNode> getIndexedChildren(final SchemaPath parentSchemaPath) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Map<QName,DataSchemaNode>>() {
            @Override
            public Map<QName,DataSchemaNode> execute() {
                return m_innerSchemaRegistry.getIndexedChildren(parentSchemaPath);
            }
        });
    }

    public DataSchemaNode getDataSchemaNode(final List<QName> qNames) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<DataSchemaNode>() {
            @Override
            public DataSchemaNode execute() {
                return m_innerSchemaRegistry.getDataSchemaNode(qNames);
            }
        });
    }

    public DataSchemaNode findChild(final DataSchemaNode currentNode, final QName qname) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<DataSchemaNode>() {
            @Override
            public DataSchemaNode execute() {
                return m_innerSchemaRegistry.findChild(currentNode, qname);
            }
        });
    }

    @Override
    public Collection<DataSchemaNode> getNonChoiceChildren(final SchemaPath parentSchemaPath) {
        return m_innerSchemaRegistry.getNonChoiceChildren(parentSchemaPath);
    }

    @Override
    public DataSchemaNode getNonChoiceChild(SchemaPath parentSchemaPath,QName qName){
       return m_innerSchemaRegistry.getNonChoiceChild(parentSchemaPath, qName);
    }

    @Override
    public DataSchemaNode getNonChoiceParent(SchemaPath schemaPath) {
        return m_innerSchemaRegistry.getNonChoiceParent(schemaPath);
    }

    @Override
    public boolean isKnownNamespace(final String namespaceURI) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Boolean>() {
            @Override
            public Boolean execute() {
                return m_innerSchemaRegistry.isKnownNamespace(namespaceURI);
            }
        });
    }

    @Override
    public Set<ModuleIdentifier> getAllModuleIdentifiers() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Set<ModuleIdentifier>>() {
            @Override
            public Set<ModuleIdentifier> execute() {
                return m_innerSchemaRegistry.getAllModuleIdentifiers();
            }
        });
    }

    @Override
    public List<YangTextSchemaSource> getYangModelByteSourcesOfAPlugin(String componentId) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<List<YangTextSchemaSource>>() {
            @Override
            public List<YangTextSchemaSource> execute() {
                return m_innerSchemaRegistry.getYangModelByteSourcesOfAPlugin(componentId) ;
            }
        });
    }


    @Override
    public QName lookupQName(final String namespace, final String localName) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<QName>() {
            @Override
            public QName execute() {
                return m_innerSchemaRegistry.lookupQName(namespace,localName);
            }
        });

    }

    @Override
    public String getPrefix(final String namespace) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<String>() {
            @Override
            public String execute() {
                return m_innerSchemaRegistry.getPrefix(namespace);
            }
        });
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<String>() {
            @Override
            public String execute() {
                return m_innerSchemaRegistry.getNamespaceURI(prefix);
            }
        });
    }

    @Override
    public String getModuleNameByNamespace(final String namespace){
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<String>() {
            @Override
            public String execute() {
                return m_innerSchemaRegistry.getModuleNameByNamespace(namespace);
            }
        });

    }

    @Override
    public String getNamespaceOfModule(final String moduleName){
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<String>() {
            @Override
            public String execute() {
                return m_innerSchemaRegistry.getNamespaceOfModule(moduleName);
            }
        });
    }


    @Override
    public Iterator<?> getPrefixes(final String namespace) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Iterator<?>>() {
            @Override
            public Iterator<?> execute() {
                return m_innerSchemaRegistry.getPrefixes(namespace);
            }
        });
    }

    @Override
    public Set<SchemaPath> getRootSchemaPaths() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Set<SchemaPath>>() {
            @Override
            public Set<SchemaPath> execute() {
                return m_innerSchemaRegistry.getRootSchemaPaths();
            }
        });
    }

    /*
     * @deprecated You have to use the method that passes the yang models, features and deviations from now onwards.
     */
    @Deprecated
    public static SchemaRegistry buildSchemaRegistry(List<String> coreYangModelFilesPaths, ReadWriteLockService readWriteLockService) throws SchemaBuildException {
        return buildSchemaRegistry(coreYangModelFilesPaths, null, null, true, readWriteLockService);
    }

    /*
     * @deprecated You have to use the method that passes the yang models, features and deviations from now onwards.
     */
    @Deprecated
    public static SchemaRegistry buildSchemaRegistry(List<String> coreYangModelFilesPaths, boolean isYangLibrarySupportedInHelloMessage,ReadWriteLockService readWriteLockService) throws SchemaBuildException {
        return buildSchemaRegistry(coreYangModelFilesPaths, null, null, isYangLibrarySupportedInHelloMessage, readWriteLockService);
    }

    /*
     * @deprecated You have to use the method that passes the yang models, features and deviations from now onwards.
     */
    @Deprecated
    public static SchemaRegistry buildSchemaRegistry(List<String> coreYangModelFilesPaths,
                                                     Map<String, Set<String>> deviations, ReadWriteLockService readWriteLockService) throws SchemaBuildException {
        Map<QName, Set<QName>> supportedDeviations = null;
        if (deviations != null) {
            supportedDeviations = new HashMap<>();
            for (Map.Entry<String, Set<String>> entry : deviations.entrySet()) {
                Set<QName> moduleDeviations = new HashSet<>();
                QName module = QName.create(entry.getKey());
                for (String deviation : entry.getValue()) {
                    moduleDeviations.add(QName.create(deviation));
                }
                supportedDeviations.put(module, moduleDeviations);
            }
        }
        return buildSchemaRegistry(coreYangModelFilesPaths, null, supportedDeviations, true, readWriteLockService);
    }

    /*
     * @deprecated You have to use the method that passes the yang models, features and deviations from now onwards.
     */
    @Deprecated
    public static SchemaRegistry buildSchemaRegistry(List<String> coreYangModelFilesPaths,
                                                     Map<String, Set<String>> deviations, boolean isYangLibrarySupportedInHelloMessage, ReadWriteLockService readWriteLockService) throws SchemaBuildException {
        Map<QName, Set<QName>> supportedDeviations = null;
        if (deviations != null) {
            supportedDeviations = new HashMap<>();
            for (Map.Entry<String, Set<String>> entry : deviations.entrySet()) {
                Set<QName> moduleDeviations = new HashSet<>();
                QName module = QName.create(entry.getKey());
                for (String deviation : entry.getValue()) {
                    moduleDeviations.add(QName.create(deviation));
                }
                supportedDeviations.put(module, moduleDeviations);
            }
        }
        return buildSchemaRegistry(coreYangModelFilesPaths, null, supportedDeviations, isYangLibrarySupportedInHelloMessage, readWriteLockService);
    }

    public static SchemaRegistry buildSchemaRegistry(List<String> coreYangModelFilesPaths, Set<String> features,
                                                     Map<String, Set<String>> deviations,ReadWriteLockService readWriteLockService) throws SchemaBuildException {

        Set<QName> supportedFeatures = null;
        Map<QName, Set<QName>> supportedDeviations = null;
        if (features != null) {
            supportedFeatures = new HashSet<>();
            for (String feature : features) {
                supportedFeatures.add(QName.create(feature));
            }
        }

        if (deviations != null) {
            supportedDeviations = new HashMap<>();
            for (Map.Entry<String, Set<String>> entry : deviations.entrySet()) {
                Set<QName> moduleDeviations = new HashSet<>();
                QName module = QName.create(entry.getKey());
                for (String deviation : entry.getValue()) {
                    moduleDeviations.add(QName.create(deviation));
                }
                supportedDeviations.put(module, moduleDeviations);
            }
        }
        return buildSchemaRegistry(coreYangModelFilesPaths, supportedFeatures, supportedDeviations, true, readWriteLockService);
    }

    public static SchemaRegistry buildSchemaRegistry(List<String> coreYangModelFilesPaths, Set<QName> supportedFeatures, Map<QName,Set<QName>> supportedDeviations, boolean isYangLibrarySupportedInHelloMessage,ReadWriteLockService readWriteLockService) throws SchemaBuildException {
        List<YangTextSchemaSource> byteSources = new ArrayList<>();
        for(String coreYangModuleFilePath : coreYangModelFilesPaths){
            byteSources.add(YangParserUtil.getYangSource(SchemaRegistryImpl.class.getResource(coreYangModuleFilePath)));
        }
        return new SchemaRegistryImpl(byteSources, supportedFeatures, supportedDeviations, isYangLibrarySupportedInHelloMessage, readWriteLockService);
    }

    @Override
    public SchemaPath getDescendantSchemaPath(final SchemaPath parentSchemaPath, final QName qname) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<SchemaPath>() {
            @Override
            public SchemaPath execute() {
                return m_innerSchemaRegistry.getDescendantSchemaPath(parentSchemaPath,qname);
            }
        });
    }

    @Override
    public Module getModuleByNamespace(final String namespace) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Module>() {
            @Override
            public Module execute() {
                return m_innerSchemaRegistry.getModuleByNamespace(namespace);
            }
        });
    }

    @Override
    public Optional<Module> findModuleByNamespaceAndRevision(final URI namespace, final Revision revision) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Optional<Module>>() {
            @Override
            public Optional<Module> execute() {
                return m_innerSchemaRegistry.findModuleByNamespaceAndRevision(namespace, revision);
            }
        });
    }

    @Override
    public void registerNodesReferencedInConstraints(String componentId, SchemaPath referencedSchemaPath, SchemaPath nodeSchemaPath, String accessPath) {
        m_innerSchemaRegistry.registerNodesReferencedInConstraints(componentId,referencedSchemaPath,nodeSchemaPath,accessPath);
    }

    @Override
    public void deRegisterNodesReferencedInConstraints(final String componentId) {
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    try {
                        DecoratedSchemaRegistryImpl clonedSchemaRegistry = m_innerSchemaRegistry.deepClone();
                        clonedSchemaRegistry.deRegisterNodesReferencedInConstraints(componentId);
                        m_innerSchemaRegistry = clonedSchemaRegistry;
                    } catch (SchemaBuildException e) {
                        throw new LockServiceException(e);
                    }
                    return null;
                }
            });
        } catch (LockServiceException e) {
            LOGGER.error("Error while unloading impacted nodes for constraints", e);
        }
    }

    @Override
    public ActionDefinition getActionDefinitionNode(List<QName> paths){        
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<ActionDefinition>() {
            @Override
            public ActionDefinition execute() {
                return m_innerSchemaRegistry.getActionDefinitionNode(paths);
            }
        });
    }

    @Override
    public NotificationDefinition getNotificationDefinitionNode(List<QName> paths){
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<NotificationDefinition>() {
            @Override
            public NotificationDefinition execute() {
                return m_innerSchemaRegistry.getNotificationDefinitionNode(paths);
            }
        });
    }

    @Override
    public Collection<SchemaPath> getSchemaPathsForComponent(final String componentId){
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Collection<SchemaPath>>() {
            @Override
            public Collection<SchemaPath> execute() {
                return m_innerSchemaRegistry.getSchemaPathsForComponent(componentId);
            }
        });
    }

    @Override
    public Map<SchemaPath,Expression> getReferencedNodesForSchemaPaths(final SchemaPath schemaPath){
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Map<SchemaPath,Expression>>() {
            @Override
            public Map<SchemaPath,Expression> execute() {
                return m_innerSchemaRegistry.getReferencedNodesForSchemaPaths(schemaPath);
            }
        });
    }   
    
    @Override
    public Map<SchemaPath, Expression> addChildImpactPaths(SchemaPath schemaPath){
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Map<SchemaPath,Expression>>() {
            @Override
            public Map<SchemaPath,Expression> execute() {
                return m_innerSchemaRegistry.addChildImpactPaths(schemaPath);
            }
        });        
    }

    @Override
    public void registerAppAllowedAugmentedPath(String moduleId, String path, SchemaPath schemaPath) {
        m_innerSchemaRegistry.registerAppAllowedAugmentedPath(moduleId,path,schemaPath);
    }
    @Override
    public void deRegisterAppAllowedAugmentedPath(String path) {
        m_innerSchemaRegistry.deRegisterAppAllowedAugmentedPath(path);
    }
    @Override
    public void registerRelativePath(String augmentedPath, String relativePath, DataSchemaNode dataSchemaNode) {
        m_innerSchemaRegistry.registerRelativePath(augmentedPath,relativePath,dataSchemaNode);
    }

    @Override
    public Expression getRelativePath(String augmentedPath, DataSchemaNode dataSchemaNode){
        return m_innerSchemaRegistry.getRelativePath(augmentedPath,dataSchemaNode);
    }

    @Override
    public String getMatchingPath(String path) {
        return m_innerSchemaRegistry.getMatchingPath(path);
    }

    public boolean isYangLibrarySupportedInHelloMessage(){
        return m_innerSchemaRegistry.isYangLibrarySupportedInHelloMessage();
    }

    //only for UT
    public void setYangLibrarySupportInHelloMessage(boolean value){
        m_innerSchemaRegistry.setYangLibrarySupportInHelloMessage(value);
    }

    @Override
    public Set<String> getModuleCapabilities(boolean forHello) {
        return m_innerSchemaRegistry.getModuleCapabilities(forHello);
    }

    @Override
    public String getCapability(ModuleIdentifier moduleId){
        return m_innerSchemaRegistry.getCapability(moduleId);
    }


    @Override
    public String getModuleSetId(){
        return m_innerSchemaRegistry.getModuleSetId();
    }

    /**
     * For UT
     */
    protected Set<String> getRelativePathCollection(){
        return m_innerSchemaRegistry.getRelativePathCollection();
    }

    @Override
    public void registerYangLibraryChangeNotificationListener(YangLibraryChangeNotificationListener listener) {
        m_innerSchemaRegistry.registerYangLibraryChangeNotificationListener(listener);
    }

    @Override
    public void unregisterYangLibraryChangeNotificationListener() {
        m_innerSchemaRegistry.unregisterYangLibraryChangeNotificationListener();
    }

    @Override
    public Map<SchemaPath, String> retrieveAppAugmentedPathToComponent() {
        return m_innerSchemaRegistry.retrieveAppAugmentedPathToComponent();
    }

    @Override
    public Set<ActionDefinition> retrieveAllActionDefinitions() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Set<ActionDefinition>>() {
            @Override
            public Set<ActionDefinition> execute() {
                return m_innerSchemaRegistry.retrieveAllActionDefinitions();
            }
        });
    }

    @Override
    public Set<NotificationDefinition> retrieveAllNotificationDefinitions() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Set<NotificationDefinition>>() {
            @Override
            public Set<NotificationDefinition> execute() {
                return m_innerSchemaRegistry.retrieveAllNotificationDefinitions();
            }
        });
    }

    public Map<ModuleIdentifier, Set<QName>> getSupportedDeviations() {
        return m_innerSchemaRegistry.getSupportedDeviations();
    }

    @Override
    public Map<ModuleIdentifier, Set<QName>> getSupportedFeatures() {
        return m_innerSchemaRegistry.getSupportedFeatures();
    }

    @Override
    public DataSchemaNode getRPCInputChildNode(RpcDefinition rpcDef, List<QName> qnames){
        return m_innerSchemaRegistry.getRPCInputChildNode(rpcDef, qnames);
    }

    @Override
    public SchemaPath stripRevisions(SchemaPath schemaPathWithRevisions) {
        return m_innerSchemaRegistry.stripRevisions(schemaPathWithRevisions);
    }


    @Override
    public SchemaPath addRevisions(SchemaPath schemaPathWithoutRevisions) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<SchemaPath>() {
            @Override
            public SchemaPath execute() {
                return m_innerSchemaRegistry.addRevisions(schemaPathWithoutRevisions);
            }
        });
    }

    /**
     * For UTs only
     * @param readWriteLockService
     * @param <T>
     */
    protected  <T> void setReadWriteLockService(ReadWriteLockService readWriteLockService) {
        m_readWriteLockService = readWriteLockService;
    }

    public void setSchemaMountRegistry(SchemaMountRegistry schemaMountRegistry) {
        m_innerSchemaRegistry.setSchemaMountRegistry(schemaMountRegistry);
    }

    @Override
    public SchemaMountRegistry getMountRegistry() {
        return m_innerSchemaRegistry.getMountRegistry();
    }

    public void setMountPath(SchemaPath mountPath) {
        m_innerSchemaRegistry.setMountPath(mountPath);
    }

    public SchemaPath getMountPath() {
        return m_innerSchemaRegistry.getMountPath();
    };

    @Override
    public void registerMountPointSchemaPath(String componentId, DataSchemaNode schemaNode) {
        m_innerSchemaRegistry.registerMountPointSchemaPath(componentId,schemaNode);
    }
    @Override
    public void unregisterMountPointSchemaPath(String componentId) {
        m_innerSchemaRegistry.unregisterMountPointSchemaPath(componentId);
    }

    @Override
    public Set<QName> retrieveAllMountPointsPath() {
        return m_innerSchemaRegistry.retrieveAllMountPointsPath();
    }

    @Override
    public Collection<DataSchemaNode> retrieveAllNodesWithMountPointExtension() {
        return m_innerSchemaRegistry.retrieveAllNodesWithMountPointExtension();
    }

    public void setParentRegistry(SchemaRegistry parent){
        m_innerSchemaRegistry.setParentRegistry(parent);
    }

    public SchemaRegistry getParentRegistry(){
        return m_innerSchemaRegistry.getParentRegistry();
    }

	@Override
    public void putValidator(TypeDefinition<?> type, TypeValidator typeValidator) {
        m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
            @Override
            public Void execute() {
                m_innerSchemaRegistry.putValidator(type, typeValidator);
                return null;
            }
        });
    }

	@Override
	public TypeValidator getValidator(TypeDefinition<?> type) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<TypeValidator>() {
            @Override
            public TypeValidator execute() {
                return m_innerSchemaRegistry.getValidator(type);
            }
        });
	}

	@Override
	public SchemaNodeConstraintParser getSchemaNodeConstraintParser(DataSchemaNode dataSchemaNode) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<SchemaNodeConstraintParser>() {
            @Override
            public SchemaNodeConstraintParser execute() {
                return m_innerSchemaRegistry.getSchemaNodeConstraintParser(dataSchemaNode);
            }
        });
	}

	@Override
	public void putSchemaNodeConstraintParser(DataSchemaNode dataSchemaNode,
			SchemaNodeConstraintParser schemaNodeConstraintParser) {
        m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
            @Override
            public Void execute() {
                m_innerSchemaRegistry.putSchemaNodeConstraintParser(dataSchemaNode, schemaNodeConstraintParser);
                return null;
            }
        });
	}
	
	@Override
	public void setName(String registryName){
		m_registryName = registryName;
	}
	
	@Override
	public String toString() {
		return m_registryName;
	}
}
