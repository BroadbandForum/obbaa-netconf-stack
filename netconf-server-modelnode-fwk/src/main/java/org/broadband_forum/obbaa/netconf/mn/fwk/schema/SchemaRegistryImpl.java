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
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DataPath;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.SchemaNodeConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.dom.YinAnnotationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.listener.YangLibraryChangeNotificationListener;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.LockServiceException;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadLockTemplate;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockService;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockServiceImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.WriteLockTemplate;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
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

import com.google.common.annotations.VisibleForTesting;

/**
 * Created by keshava on 11/18/15.
 */
public class SchemaRegistryImpl implements SchemaRegistry {

    private ReadWriteLockService m_readWriteLockService;
    private DecoratedSchemaRegistryImpl m_innerSchemaRegistry;
    private String m_registryName = GLOBAL_SCHEMA_REGISTRY;

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
        this(coreYangModelFiles, null, null, isYangLibrarySupportedInHelloMessage, readWriteLockService, false);
    }

    public SchemaRegistryImpl(List<YangTextSchemaSource> coreYangModelFiles, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations, ReadWriteLockService readWriteLockService) throws SchemaBuildException {
        this(coreYangModelFiles, supportedFeatures, supportedDeviations, true,readWriteLockService, false);
    }

    public SchemaRegistryImpl(List<YangTextSchemaSource> coreYangModelFiles, Set<QName> supportedFeatures,
                              Map<QName, Set<QName>> supportedDeviations, ReadWriteLockService readWriteLockService,
                              boolean isYangLibraryIgnored) throws SchemaBuildException {
        this(coreYangModelFiles, supportedFeatures, supportedDeviations, true,readWriteLockService, isYangLibraryIgnored);
    }

    public SchemaRegistryImpl(List<YangTextSchemaSource> coreYangModelFiles, Set<QName> supportedFeatures,
                              Map<QName, Set<QName>> supportedDeviations, boolean isYangLibrarySupportedInHelloMessage,
                              ReadWriteLockService readWriteLockService) throws SchemaBuildException {
        this(coreYangModelFiles, supportedFeatures, supportedDeviations, isYangLibrarySupportedInHelloMessage ,readWriteLockService, false);
    }

    public SchemaRegistryImpl(List<YangTextSchemaSource> coreYangModelFiles, Set<QName> supportedFeatures,
                              Map<QName, Set<QName>> supportedDeviations, boolean isYangLibrarySupportedInHelloMessage,
                              ReadWriteLockService readWriteLockService, boolean isYangLibraryIgnored) throws SchemaBuildException {
        RequestScope.withScope(new RequestScope.RsTemplate<Void>() {
            @Override
            protected Void execute() throws RequestScopeExecutionException {
                m_readWriteLockService = readWriteLockService;
                m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Object>() {
                    @Override
                    public Object execute() {
                        try {
                            m_innerSchemaRegistry = new DecoratedSchemaRegistryImpl(coreYangModelFiles, supportedFeatures, supportedDeviations, isYangLibrarySupportedInHelloMessage, isYangLibraryIgnored);
                        } catch (SchemaBuildException e) {
                            throw new LockServiceException(e);
                        }
                        return null;
                    }
                });
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

    @Override
    public SchemaRegistry unwrap() {
        return this;
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
            throw new SchemaBuildException("Exception while building Schema Context", e);
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
        loadSchemaContext(componentId, yangModelByteSources, supportedFeatures, supportedDeviations, true, false);
    }

    @Override
    public synchronized void loadSchemaContext(final String componentId, final List<YangTextSchemaSource> yangModelByteSources,
                                               Set<QName> supportedFeatures, Map<QName,Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported) throws SchemaBuildException {
        loadSchemaContext(componentId, yangModelByteSources, supportedFeatures, supportedDeviations, isYangLibNotificationSupported, false);
    }

    @Override
    public synchronized void loadSchemaContext(final String componentId, final List<YangTextSchemaSource> yangModelByteSources, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported, boolean isYangLibraryIgnored) throws SchemaBuildException {

        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    try {
                        DecoratedSchemaRegistryImpl clonedSchemaRegistry = m_innerSchemaRegistry.deepClone();
                        clonedSchemaRegistry.loadSchemaContext(componentId,yangModelByteSources,supportedFeatures,supportedDeviations, isYangLibNotificationSupported, isYangLibraryIgnored);
                        m_innerSchemaRegistry = clonedSchemaRegistry;
                    } catch (SchemaBuildException e) {
                        throw new LockServiceException(e);
                    }
                    return null;
                }
            });
        }catch (LockServiceException e) {
            throw new SchemaBuildException("Exception while building Schema Context",e);
        }
    }

    @Override
    public synchronized void unloadSchemaContext(final String componentId, Set<QName> supportedFeatures, Map<QName,Set<QName>> supportedDeviations) throws SchemaBuildException {
        unloadSchemaContext(componentId, supportedFeatures, supportedDeviations, true);
    }

    @Override
    public synchronized void unloadSchemaContext(final String componentId, Set<QName> supportedFeatures,
                                                 Map<QName,Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported) throws SchemaBuildException {
        unloadSchemaContext(componentId, supportedFeatures, supportedDeviations, isYangLibNotificationSupported, false);
    }

    @Override
    public synchronized void unloadSchemaContext(final String componentId, Set<QName> supportedFeatures, Map<QName,Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported, boolean isYangLibraryIgnored) throws SchemaBuildException {
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    try{
                        if(isYangLibraryIgnored) {
                            m_innerSchemaRegistry.setYangLibraryIgnored(true);
                        }
                        DecoratedSchemaRegistryImpl clonedSchemaRegistry = m_innerSchemaRegistry.deepClone();
                        clonedSchemaRegistry.unloadSchemaContext(componentId,supportedFeatures,supportedDeviations, isYangLibNotificationSupported, isYangLibraryIgnored);
                        m_innerSchemaRegistry = clonedSchemaRegistry;
                    }catch (SchemaBuildException e){

                    }
                    return null;
                }
            });
        } catch (LockServiceException e) {
            throw new SchemaBuildException("Error while unloading schema Context", e);
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
        try {
            return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Map<SourceIdentifier, YangTextSchemaSource>>() {
                @Override
                public Map<SourceIdentifier, YangTextSchemaSource> execute() {
                    try {
                        return m_innerSchemaRegistry.getAllYangTextSchemaSources();
                    } catch (SchemaBuildException e) {
                        throw new LockServiceException(e);
                    }
                }
            });
        } catch(LockServiceException e) {
            throw new SchemaBuildException("Exception while getting all yang schema sources",e);
        }
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
    public void clear() {
        m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
            @Override
            public Void execute() throws LockServiceException {
                m_innerSchemaRegistry.clear();
                return null;
            }
        });
    }

    @Override
    public Collection<DataSchemaNode> getNonChoiceChildren(final SchemaPath parentSchemaPath) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Collection<DataSchemaNode>>() {
            @Override
            public Collection<DataSchemaNode> execute() {
                return m_innerSchemaRegistry.getNonChoiceChildren(parentSchemaPath);
            }
        });
    }

    @Override
    public DataSchemaNode getNonChoiceChild(SchemaPath parentSchemaPath,QName qName){
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<DataSchemaNode>() {
            @Override
            public DataSchemaNode execute() {
                return m_innerSchemaRegistry.getNonChoiceChild(parentSchemaPath, qName);
            }
        });
    }

    @Override
    public DataSchemaNode getNonChoiceParent(SchemaPath schemaPath) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<DataSchemaNode>() {
            @Override
            public DataSchemaNode execute() {
                return m_innerSchemaRegistry.getNonChoiceParent(schemaPath);
            }
        });
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
    public String getComponentIdByNamespace(final String namespace){
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<String>() {
            @Override
            public String execute() {
                return m_innerSchemaRegistry.getComponentIdByNamespace(namespace);
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
    public void addToChildBigList(SchemaPath schemaPath) {
        m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
            @Override
            public Void execute() throws LockServiceException {
                m_innerSchemaRegistry.addToChildBigList(schemaPath);
                return null;
            }
        });
    }

    @Override
    public boolean isChildBigList(SchemaPath schemaPath) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Boolean>() {
            @Override
            public Boolean execute() {
                return m_innerSchemaRegistry.isChildBigList(schemaPath);
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
    public void registerNodesReferredInConstraints(String componentId, ReferringNode referringNode) {
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    m_innerSchemaRegistry.registerNodesReferredInConstraints(componentId, referringNode);
                    return null;
                }
            });
        } catch (LockServiceException e) {
            LOGGER.error("Error while registering reference nodes for constraints", e);
        }
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
            LOGGER.error("Error while de-registering referenced nodes for constraints", e);
        }
    }

    @Override
    public ActionDefinition getActionDefinitionNode(DataPath actionDataPath){
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<ActionDefinition>() {
            @Override
            public ActionDefinition execute() {
                return m_innerSchemaRegistry.getActionDefinitionNode(actionDataPath);
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
    public ReferringNodes getReferringNodesForSchemaPath(final SchemaPath schemaPath){
        return m_readWriteLockService.executeWithReadLock(() -> m_innerSchemaRegistry.getReferringNodesForSchemaPath(schemaPath));
    }

    @Override
    public ReferringNodes addChildImpactPaths(SchemaPath schemaPath, Set<QName> skipImmediateChildQNames){
        return m_readWriteLockService.executeWithReadLock(() -> m_innerSchemaRegistry.addChildImpactPaths(schemaPath, skipImmediateChildQNames));
    }

    @Override
    public void registerAppAllowedAugmentedPath(String moduleId, String path, SchemaPath schemaPath) {
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    m_innerSchemaRegistry.registerAppAllowedAugmentedPath(moduleId, path, schemaPath);
                    return null;
                }
            });
        } catch (LockServiceException e) {
            LOGGER.error("Error while registering App allowed augmented paths", e);
        }
    }

    @Override
    public void deRegisterAppAllowedAugmentedPath(String path) {
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    m_innerSchemaRegistry.deRegisterAppAllowedAugmentedPath(path);
                    return null;
                }
            });
        } catch (LockServiceException e) {
            LOGGER.error("Error while de-registering app allowed augmented paths", e);
        }
    }

    @Override
    public void registerRelativePath(String augmentedPath, String relativePath, DataSchemaNode dataSchemaNode) {
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    m_innerSchemaRegistry.registerRelativePath(augmentedPath, relativePath, dataSchemaNode);
                    return null;
                }
            });
        } catch (LockServiceException e) {
            LOGGER.error("Error while registering relative paths", e);
        }
    }

    @Override
    public Expression getRelativePath(String augmentedPath, DataSchemaNode dataSchemaNode) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Expression>() {
            @Override
            public Expression execute() {
                return m_innerSchemaRegistry.getRelativePath(augmentedPath, dataSchemaNode);
            }
        });
    }

    @Override
    public String getMatchingPath(String path) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<String>() {
            @Override
            public String execute() {
                return m_innerSchemaRegistry.getMatchingPath(path);
            }
        });
    }

    public boolean isYangLibrarySupportedInHelloMessage() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Boolean>() {
            @Override
            public Boolean execute() {
                return m_innerSchemaRegistry.isYangLibrarySupportedInHelloMessage();
            }
        });
    }

    @VisibleForTesting
    public boolean isYangLibraryIgnored() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Boolean>() {
            @Override
            public Boolean execute() {
                return m_innerSchemaRegistry.isYangLibraryIgnored();
            }
        });
    }

    //only for UT
    public void setYangLibrarySupportInHelloMessage(boolean value){
        m_innerSchemaRegistry.setYangLibrarySupportInHelloMessage(value);
    }

    //only for UT
    public void setYangLibraryIgnored(boolean isYangLibraryIgnored){
        m_innerSchemaRegistry.setYangLibraryIgnored(isYangLibraryIgnored);
    }

    @Override
    public Set<String> getModuleCapabilities(boolean forHello) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Set<String>>() {
            @Override
            public Set<String> execute() {
                return m_innerSchemaRegistry.getModuleCapabilities(forHello);
            }
        });
    }

    @Override
    public Expression getExpressionWithModuleNameInPrefix(SchemaPath schemaPath, Expression expression) {
        return m_innerSchemaRegistry.getExpressionWithModuleNameInPrefix(schemaPath, expression.toString());
    }

    @Override
    public Expression getExpressionWithModuleNameInPrefix(SchemaPath schemaPath, String expression) {
        return m_innerSchemaRegistry.getExpressionWithModuleNameInPrefix(schemaPath, expression);
    }

    @Override
    public void registerExpressionWithModuleNameInPrefix(SchemaPath schemaPath, String expression,
            String expressionWithPrefix) {
        m_innerSchemaRegistry.registerExpressionWithModuleNameInPrefix(schemaPath, expression, expressionWithPrefix);
    }

    @Override
    public void registerExpressionWithModuleNameInPrefix(SchemaPath schemaPath,
            Expression expression, String expressionWithPrefix) {
        m_innerSchemaRegistry.registerExpressionWithModuleNameInPrefix(schemaPath, expression, expressionWithPrefix);
    }

    @Override
    public Set<String> getAttributesWithSameLocalNameDifferentNameSpace(SchemaPath schemaPath) {
        return m_innerSchemaRegistry.getAttributesWithSameLocalNameDifferentNameSpace(schemaPath);
    }

    @Override
    public void registerAttributesWithSameLocalNameDifferentNameSpace(SchemaPath schemaPath,
            Set<String> attributesWithSameLocalNameDifferentNameSpace) {
        m_innerSchemaRegistry.registerAttributesWithSameLocalNameDifferentNameSpace(schemaPath, attributesWithSameLocalNameDifferentNameSpace);
    }

    @Override
    public String getCapability(ModuleIdentifier moduleId) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<String>() {
            @Override
            public String execute() {
                return m_innerSchemaRegistry.getCapability(moduleId);
            }
        });
    }

    @Override
    public String getModuleSetId() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<String>() {
            @Override
            public String execute() {
                return m_innerSchemaRegistry.getModuleSetId();
            }
        });
    }

    /**
     * For UT
     */
    protected Set<String> getRelativePathCollection(){
        return m_innerSchemaRegistry.getRelativePathCollection();
    }

    @Override
    public void registerYangLibraryChangeNotificationListener(YangLibraryChangeNotificationListener listener) {
        m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
            @Override
            public Void execute() throws LockServiceException {
                m_innerSchemaRegistry.registerYangLibraryChangeNotificationListener(listener);
                return null;
            }
        });
    }

    @Override
    public void unregisterYangLibraryChangeNotificationListener() {
        m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
            @Override
            public Void execute() throws LockServiceException {
                m_innerSchemaRegistry.unregisterYangLibraryChangeNotificationListener();
                return null;
            }
        });
    }

    @Override
    public Map<SchemaPath, String> retrieveAppAugmentedPathToComponent() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Map<SchemaPath, String>>() {
            @Override
            public Map<SchemaPath, String> execute() {
                return m_innerSchemaRegistry.retrieveAppAugmentedPathToComponent();
            }
        });
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
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Map<ModuleIdentifier, Set<QName>>>() {
            @Override
            public Map<ModuleIdentifier, Set<QName>> execute() {
                return m_innerSchemaRegistry.getSupportedDeviations();
            }
        });
    }

    @Override
    public Map<ModuleIdentifier, Set<QName>> getSupportedFeatures() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Map<ModuleIdentifier, Set<QName>>>() {
            @Override
            public Map<ModuleIdentifier, Set<QName>> execute() {
                return m_innerSchemaRegistry.getSupportedFeatures();
            }
        });
    }

    @Override
    public DataSchemaNode getRPCInputChildNode(RpcDefinition rpcDef, List<QName> qnames){
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<DataSchemaNode>() {
            @Override
            public DataSchemaNode execute() {
                return m_innerSchemaRegistry.getRPCInputChildNode(rpcDef, qnames);
            }
        });

    }

    @Override
    public SchemaPath stripRevisions(SchemaPath schemaPathWithRevisions) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<SchemaPath>() {
            @Override
            public SchemaPath execute() {
                return m_innerSchemaRegistry.stripRevisions(schemaPathWithRevisions);
            }
        });
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
        m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
            @Override
            public Void execute() throws LockServiceException {
                m_innerSchemaRegistry.registerMountPointSchemaPath(componentId, schemaNode);
                return null;
            }
        });
    }

    @Override
    public void unregisterMountPointSchemaPath(String componentId) {
        m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
            @Override
            public Void execute() throws LockServiceException {
                m_innerSchemaRegistry.unregisterMountPointSchemaPath(componentId);
                return null;
            }
        });
    }

    @Override
    public Set<QName> retrieveAllMountPointsPath() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Set<QName>>() {
            @Override
            public Set<QName> execute() {
                return m_innerSchemaRegistry.retrieveAllMountPointsPath();
            }
        });
    }

    @Override
    public Collection<DataSchemaNode> retrieveAllNodesWithMountPointExtension() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Collection<DataSchemaNode>>() {
            @Override
            public Collection<DataSchemaNode> execute() {
                return m_innerSchemaRegistry.retrieveAllNodesWithMountPointExtension();
            }
        });
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
    public String getName(){
        return m_registryName;
    }

    @Override
    public void setName(String registryName){
        m_registryName = registryName;
    }

    @Override
    public String toString() {
        return m_registryName;
    }

    @Override
    public ConcurrentHashMap<SchemaPath, TreeImpactNode<ImpactNode>> getImpactNodeMap() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<ConcurrentHashMap<SchemaPath, TreeImpactNode<ImpactNode>>>() {
            @Override
            public ConcurrentHashMap<SchemaPath, TreeImpactNode<ImpactNode>> execute() {
                return m_innerSchemaRegistry.getImpactNodeMap();
            }
        });        
    }

    @Override
    public Map<SchemaPath, DataSchemaNode> getSchemaNodes() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Map<SchemaPath, DataSchemaNode>>() {
            @Override
            public Map<SchemaPath, DataSchemaNode> execute() {
                return m_innerSchemaRegistry.getSchemaNodes();
            }
        });
    }

    @Override
    public void registerWhenReferringNodes(String componentId, SchemaPath referencedSchemaPath, SchemaPath nodeSchemaPath, String accessPath) {
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    m_innerSchemaRegistry.registerWhenReferringNodes(componentId, referencedSchemaPath, nodeSchemaPath, accessPath);
                    return null;
                }
            });
        } catch (LockServiceException e) {
            LOGGER.error("Error while registering when referring nodes", e);
        }

    }

    @Override
    public void registerWhenReferringNodesForAllSchemaNodes(String componentId, SchemaPath referencedSchemaPath, SchemaPath nodeSchemaPath, String accessPath) {
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    m_innerSchemaRegistry.registerWhenReferringNodesForAllSchemaNodes(componentId, referencedSchemaPath, nodeSchemaPath, accessPath);
                    return null;
                }
            });
        } catch (LockServiceException e) {
            LOGGER.error("Error while registering when referring nodes", e);
        }
    }

    @Override
    public Map<SchemaPath, Expression> getWhenReferringNodes(SchemaPath nodeSchemaPath) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Map<SchemaPath, Expression>>() {
            @Override
            public Map<SchemaPath, Expression> execute() {
                return m_innerSchemaRegistry.getWhenReferringNodes(nodeSchemaPath);
            }
        });
    }

    @Override
    public String getShortPath(SchemaPath path) {
        return m_innerSchemaRegistry.getShortPath(path);
    }

    @Override
    public Map<SchemaPath, Expression> getWhenReferringNodes(String componentId, SchemaPath nodeSchemaPath) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Map<SchemaPath, Expression>>() {
            @Override
            public Map<SchemaPath, Expression> execute() {
                return m_innerSchemaRegistry.getWhenReferringNodes(componentId, nodeSchemaPath);
            }
        });
    }

    @Override
    public Map<SchemaPath, Expression> getWhenReferringNodesForAllSchemaNodes(String componentId, SchemaPath nodeSchemaPath) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Map<SchemaPath, Expression>>() {
            @Override
            public Map<SchemaPath, Expression> execute() {
                return m_innerSchemaRegistry.getWhenReferringNodesForAllSchemaNodes(componentId, nodeSchemaPath);
            }
        });
    }

    @Override
    public void registerMustReferringNodesForAllSchemaNodes(String componentId, SchemaPath referencedSchemaPath, SchemaPath nodeSchemaPath, String accessPath) {
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    m_innerSchemaRegistry.registerMustReferringNodesForAllSchemaNodes(componentId, referencedSchemaPath, nodeSchemaPath, accessPath);
                    return null;
                }
            });
        } catch (LockServiceException e) {
            LOGGER.error("Error while registering must referring nodes", e);
        }
    }

    @Override
    public Map<SchemaPath, Expression> getMustReferringNodesForAllSchemaNodes(String componentId, SchemaPath nodeSchemaPath) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Map<SchemaPath, Expression>>() {
            @Override
            public Map<SchemaPath, Expression> execute() {
                return m_innerSchemaRegistry.getMustReferringNodesForAllSchemaNodes(componentId, nodeSchemaPath);
            }
        });
    }

    @Override
    public void addToSkipValidationPaths(SchemaPath schemaPath, String constraintXpath) {
        m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
            @Override
            public Void execute() throws LockServiceException {
                m_innerSchemaRegistry.addToSkipValidationPaths(schemaPath, constraintXpath);
                return null;
            }
        });		
    }

    @Override
    public boolean isSkipValidationBySchemaPathWithConstraintXpath(SchemaPath schemaPath, String constraintXpath) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Boolean>() {
            @Override
            public Boolean execute() {
                return m_innerSchemaRegistry.isSkipValidationBySchemaPathWithConstraintXpath(schemaPath, constraintXpath);
            }
        });
    }

    @Override
    public boolean isSkipValidationPath(SchemaPath schemaPath) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Boolean>() {
            @Override
            public Boolean execute() {
                return m_innerSchemaRegistry.isSkipValidationPath(schemaPath);
            }
        });
    }

    @Override
    public void printReferringNodes() {
        m_innerSchemaRegistry.printReferringNodes();
    }

    @Override
    public void addImpactNodeForChild(SchemaPath schemaPath, ReferringNodes impactedPaths, Set<QName> skipImmediateChildQNames) {
        m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
            @Override
            public Void execute() throws LockServiceException {
                m_innerSchemaRegistry.addImpactNodeForChild(schemaPath, impactedPaths, skipImmediateChildQNames);
                return null;
            }
        });
    }

    @Override
    public Map<SchemaPath, TreeImpactNode<ImpactNode>> getReferringNodes() {
        return m_readWriteLockService.executeWithReadLock(
                (ReadLockTemplate<Map<SchemaPath, TreeImpactNode<ImpactNode>>>) () -> m_innerSchemaRegistry.getReferringNodes());
    }

    @Override
    public void addExpressionsWithoutKeysInList(String fullExpression, LocationPath expressionWithListAtLowerLevel) {
        m_readWriteLockService.executeWithWriteLock((WriteLockTemplate<Void>) () -> {
            m_innerSchemaRegistry.addExpressionsWithoutKeysInList(fullExpression, expressionWithListAtLowerLevel);
            return null;
        });        
    }

    @Override
    public Set<LocationPath> getExpressionsWithoutKeysInList(String fullExpression) {
        return m_readWriteLockService.executeWithReadLock((ReadLockTemplate<Set<LocationPath>>) () ->
        m_innerSchemaRegistry.getExpressionsWithoutKeysInList(fullExpression));
    }

    @Override
    public void registerNodeConstraintDefinedModule(DataSchemaNode schemaNode, String constraint, Module module) {
        m_readWriteLockService.executeWithWriteLock((WriteLockTemplate<Void>) () -> {
            m_innerSchemaRegistry.registerNodeConstraintDefinedModule(schemaNode, constraint, module);
            return null;
        });

    }

    @Override
    public Map<Expression, Module> getNodeConstraintDefinedModule(DataSchemaNode schemaNode) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Map<Expression, Module>>() {
            @Override
            public Map<Expression, Module> execute() {
                return m_innerSchemaRegistry.getNodeConstraintDefinedModule(schemaNode);
            }
        });
    }

    @Override
    public YinAnnotationService getYinAnnotationService() {
        return m_readWriteLockService.executeWithReadLock(() -> m_innerSchemaRegistry.getYinAnnotationService());
    }

    @VisibleForTesting
    public void setYinAnnotationService(YinAnnotationService testService){
        m_innerSchemaRegistry.setYinAnnotationService(testService);
    }

    public static SchemaRegistryImpl newEmptySR() throws SchemaBuildException {
        return new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
    }

    @Override
    public Set<ActionDefinition> getActionDefinitionNodesWithListAndLeafRef() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Set<ActionDefinition>>() {
            @Override
            public Set<ActionDefinition> execute() {
                return m_innerSchemaRegistry.getActionDefinitionNodesWithListAndLeafRef();
            }
        });
    }

}
