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
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.ConstraintValidatorFactoryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.listener.YangLibraryChangeNotificationListener;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.LockServiceException;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.WriteLockTemplate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.util.ModuleIdentifierImpl;
import org.opendaylight.yangtools.yang.parser.util.ASTSchemaSource;
import org.opendaylight.yangtools.yang.parser.util.TextToASTTransformer;

import org.broadband_forum.obbaa.netconf.api.NetconfCapability;
import org.broadband_forum.obbaa.netconf.api.parser.SettableSchemaProvider;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;

import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.DefaultConcurrentHashMap;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadLockTemplate;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.ReadWriteLockService;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

/**
 * Created by keshava on 11/18/15.
 */
public class SchemaRegistryImpl implements SchemaRegistry {
    private static final String COMMA = ",";
    public static final String VNO = "VNO";
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(SchemaRegistryImpl.class, "netconf-stack",
            "DEBUG", "GLOBAL");
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern(YYYY_MM_DD);
    private SchemaContext m_schemaContext;
    private Map<SchemaPath, RpcDefinition> m_allRpcDefinitions = new LinkedHashMap<>();
    private Map<String, List<YangTextSchemaSource>> m_componentModules = new LinkedHashMap<>();
    private Map<SchemaPath, DataSchemaNode> m_schemaNodes = new LinkedHashMap<>();
    private Map<SchemaPath, DataSchemaNode> m_rootSchemaNodes = new LinkedHashMap<>();
    private Map<String, Module> m_modules = new LinkedHashMap<>();
    private String m_repoName = SchemaRegistryImpl.class.getName();
    private DefaultConcurrentHashMap<SchemaPath, Map<SchemaPath, Expression>> m_impactedNodesForConstraints = new
            DefaultConcurrentHashMap<>(new HashMap<SchemaPath, Expression>(), true);
    private DefaultConcurrentHashMap<String, HashSet<SchemaPath>> m_componentIdSchemaPathMap = new
            DefaultConcurrentHashMap<>(new HashSet<SchemaPath>(), true);
    private Map<SchemaPath, String> m_appAugmentedPathToComponentIdMap = new HashMap<>();
    private Map<String, SchemaPath> m_appAugmentedPathToSchemaPathMap = new HashMap<>();

    private DefaultConcurrentHashMap<SchemaPath, HashSet<ActionDefinition>> m_schemapathToActionDefinitions = new
            DefaultConcurrentHashMap<>(new HashSet<ActionDefinition>(), true);
    private DefaultConcurrentHashMap<String, HashSet<SchemaPath>> m_componentIdToActionRootSchemaPath = new
            DefaultConcurrentHashMap<>(new HashSet<SchemaPath>(), true);

    private final Set<String> m_relativePath = Collections.synchronizedSet(new HashSet<String>());
    private final Map<String, Map<DataSchemaNode, Expression>> m_relativePaths = new DefaultConcurrentHashMap<>(new
            HashMap<DataSchemaNode, Expression>(), true);

    private final DefaultConcurrentHashMap<String, HashSet<String>> m_componentIdAbsSchemaPaths =
            new DefaultConcurrentHashMap<>(new HashSet<String>(), true);

    private ReadWriteLockService m_readWriteLockService;
    private boolean m_isYangLibrarySupportedInHelloMessage;
    private YangLibraryChangeNotificationListener m_yangLibraryChangeNotificationListener;
    private static final String YANG_LIBRARY_CAP_FORMAT =
            "urn:ietf:params:netconf:capability:yang-library:1.0?revision=2016-04-09&module-set-id=%s";
    private final Map<ModuleIdentifier, Set<QName>> m_supportedFeatures = new HashMap<>();
    private final Map<ModuleIdentifier, Set<QName>> m_supportedDeviations = new HashMap<>();
    private final Map<String, Set<ModuleIdentifier>> m_componentIdModuleIdentifiersMap = new HashMap<>();
    private String m_moduleSetId;
    private ConcurrentHashMap<QName, Set<QName>> m_supportedPlugDeviations = new ConcurrentHashMap<>();

    private DataSchemaNode getDataSchemaNodeFromCache(SchemaPath schemaPath) {
        RequestScope scope = RequestScope.getCurrentScope();
        Map<SchemaPath, DataSchemaNode> schemaNodePathMap = (Map<SchemaPath, DataSchemaNode>) scope.getFromCache
                (SCHEMAPATH_SCHEMANODE_CACHE);
        if (schemaNodePathMap != null && schemaPath != null) {
            return schemaNodePathMap.get(schemaPath);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void addDataSchemaNodeToCache(SchemaPath schemaPath, DataSchemaNode dataSchemaNode) {
        RequestScope scope = RequestScope.getCurrentScope();
        Map<SchemaPath, DataSchemaNode> schemaNodePathMap = (Map<SchemaPath, DataSchemaNode>) scope.getFromCache
                (SCHEMAPATH_SCHEMANODE_CACHE);
        if (schemaNodePathMap == null) {
            schemaNodePathMap = new HashMap<>(1000);
            scope.putInCache(SCHEMAPATH_SCHEMANODE_CACHE, schemaNodePathMap);
        }
        schemaNodePathMap.putIfAbsent(schemaPath, dataSchemaNode);
    }

    @SuppressWarnings("unchecked")
    private Map<SchemaPath, Map<QName, DataSchemaNode>> getIndexedCache() {
        RequestScope scope = RequestScope.getCurrentScope();
        Map<SchemaPath, Map<QName, DataSchemaNode>> cache = (Map<SchemaPath, Map<QName, DataSchemaNode>>) scope
                .getFromCache(CHILD_NODE_INDEX_CACHE);
        if (cache == null) {
            cache = new HashMap<>();
            scope.putInCache(CHILD_NODE_INDEX_CACHE, cache);
            cache = (Map<SchemaPath, Map<QName, DataSchemaNode>>) scope.getFromCache(CHILD_NODE_INDEX_CACHE);
        }
        return cache;

    }

    @SuppressWarnings("unchecked")
    @VisibleForTesting
    Collection<DataSchemaNode> getFromCache(SchemaPath parentPath) {
        RequestScope scope = RequestScope.getCurrentScope();
        Map<SchemaPath, Collection<DataSchemaNode>> cache = (Map<SchemaPath, Collection<DataSchemaNode>>) scope
                .getFromCache(CHILD_NODE_CACHE);
        if (cache == null) {
            cache = new HashMap<>();
            scope.putInCache(CHILD_NODE_CACHE, cache);
            cache = (Map<SchemaPath, Collection<DataSchemaNode>>) scope.getFromCache(CHILD_NODE_CACHE);
        }
        return cache.get(parentPath);
    }

    @SuppressWarnings("unchecked")
    void addToCache(SchemaPath parentPath, Collection<DataSchemaNode> childList) {
        RequestScope scope = RequestScope.getCurrentScope();
        Map<SchemaPath, Collection<DataSchemaNode>> cache = (Map<SchemaPath, Collection<DataSchemaNode>>) scope
                .getFromCache(CHILD_NODE_CACHE);
        if (cache == null) {
            cache = new HashMap<>();
            scope.putInCache(CHILD_NODE_CACHE, cache);
            cache = (Map<SchemaPath, Collection<DataSchemaNode>>) scope.getFromCache(CHILD_NODE_CACHE);
        }
        Collection<DataSchemaNode> children = cache.get(parentPath);
        if (children == null) {
            Collection<DataSchemaNode> newChildList = new ArrayList<DataSchemaNode>(childList);
            cache.put(parentPath, newChildList);
        } else {
            children.addAll(childList);
        }
    }

    private void addToCache(SchemaPath parentPath, Map<QName, DataSchemaNode> children) {
        Map<SchemaPath, Map<QName, DataSchemaNode>> cache = getIndexedCache();
        Map<QName, DataSchemaNode> cacheChildren = cache.get(parentPath);
        if (cacheChildren == null) {
            cacheChildren = new HashMap<>(children.size());
            cache.put(parentPath, cacheChildren);
            cacheChildren = cache.get(parentPath);
        }
        cacheChildren.putAll(children);
    }

    private Map<QName, DataSchemaNode> getFromIndexedCache(SchemaPath parentPath) {
        Map<SchemaPath, Map<QName, DataSchemaNode>> cache = getIndexedCache();
        return cache.get(parentPath);
    }

    public SchemaRegistryImpl(ReadWriteLockService readWriteLockService) throws SchemaBuildException {
        this(Collections.<YangTextSchemaSource>emptyList(), true, readWriteLockService);
    }

    public SchemaRegistryImpl(boolean isYangLibrarySupportedInHelloMessage, ReadWriteLockService
            readWriteLockService) throws SchemaBuildException {
        this(Collections.<YangTextSchemaSource>emptyList(), isYangLibrarySupportedInHelloMessage, readWriteLockService);
    }

    public SchemaRegistryImpl(List<YangTextSchemaSource> coreYangModelFiles, ReadWriteLockService
            readWriteLockService) throws SchemaBuildException {
        this(coreYangModelFiles, true, readWriteLockService);
    }

    public SchemaRegistryImpl(List<YangTextSchemaSource> coreYangModelFiles, boolean
            isYangLibrarySupportedInHelloMessage, ReadWriteLockService readWriteLockService) throws
            SchemaBuildException {
        this(coreYangModelFiles, null, null, isYangLibrarySupportedInHelloMessage, readWriteLockService);
    }

    public SchemaRegistryImpl(List<YangTextSchemaSource> coreYangModelFiles, Set<QName> supportedFeatures, Map<QName,
            Set<QName>> supportedDeviations, ReadWriteLockService readWriteLockService) throws SchemaBuildException {
        this(coreYangModelFiles, supportedFeatures, supportedDeviations, true, readWriteLockService);
    }

    public SchemaRegistryImpl(List<YangTextSchemaSource> coreYangModelFiles, Set<QName> supportedFeatures, Map<QName,
            Set<QName>> supportedDeviations, boolean isYangLibrarySupportedInHelloMessage, ReadWriteLockService
            readWriteLockService) throws SchemaBuildException {
        m_readWriteLockService = readWriteLockService;
        m_isYangLibrarySupportedInHelloMessage = isYangLibrarySupportedInHelloMessage;
        buildSchemaContext(coreYangModelFiles, supportedFeatures, supportedDeviations);
    }

    @Override
    public SchemaContext getSchemaContext() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<SchemaContext>() {
            @Override
            public SchemaContext execute() {
                LOGGER.debug("getSchemaContext called");
                return m_schemaContext;
            }
        });
    }

    @Override
    public void buildSchemaContext(final List<YangTextSchemaSource> coreYangModelByteSources) throws
            SchemaBuildException {
        buildSchemaContext(coreYangModelByteSources, null, null);
    }

    @Override
    public void buildSchemaContext(final List<YangTextSchemaSource> coreYangModelByteSources, Set<QName>
            supportedFeatures, Map<QName, Set<QName>> supportedDeviations) throws SchemaBuildException {
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    LOGGER.info("buildSchemaContext called with coreYangModelByteSources : {}",
                            coreYangModelByteSources);
                    try {
                        m_componentModules.clear();
                        ConstraintValidatorFactoryImpl.getInstance().clearCache();
                        SchemaRegistryUtil.resetCache();
                        m_schemaContext = YangParserUtil.parseSchemaSources(m_repoName, coreYangModelByteSources,
                                supportedFeatures, supportedDeviations);
                        m_componentModules.put(CORE_COMPONENT_ID, coreYangModelByteSources);
                        updateIndexes();
                        updateCapabilities(null, supportedFeatures, supportedDeviations, true);
                    } catch (Exception e) {
                        LOGGER.error("Error while building schema context", e);
                        throw new LockServiceException("Error while building schema context", e);
                    }
                    return null;
                }
            });
        } catch (LockServiceException e) {
            throw new SchemaBuildException(e.getCause());
        }
    }

    private void addAllSupportedFeatures(String componentId) {
        Set<Module> modules = m_schemaContext.getModules();
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();
        for (Module module : modules) {
            ModuleIdentifier moduleIdentifier = ModuleIdentifierImpl.create(module.getName(), Optional.of(module
                    .getNamespace()), Optional.of(module.getRevision()));
            Set<QName> featureQnames = m_supportedFeatures.get(moduleIdentifier);
            if (featureQnames == null) {
                featureQnames = new HashSet<>();
                m_supportedFeatures.put(moduleIdentifier, featureQnames);
                moduleIdentifiers.add(moduleIdentifier);
            }
            for (FeatureDefinition feature : module.getFeatures()) {
                featureQnames.add(feature.getQName());
            }
        }
        m_componentIdModuleIdentifiersMap.put(componentId, moduleIdentifiers);
    }

    private void addAllSupportedDeviations(String componentId) {
        Set<Module> modules = m_schemaContext.getModules();
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();
        for (Module module : modules) {
            ModuleIdentifier moduleIdentifier = ModuleIdentifierImpl.create(module.getName(), Optional.of(module
                    .getNamespace()), Optional.of(module.getRevision()));
            Set<QName> deviationQnames = m_supportedDeviations.get(moduleIdentifier);
            if (deviationQnames == null) {
                deviationQnames = new HashSet<>();
                m_supportedDeviations.put(moduleIdentifier, deviationQnames);
                moduleIdentifiers.add(moduleIdentifier);
            }
        }
        m_componentIdModuleIdentifiersMap.put(componentId, moduleIdentifiers);
    }

    private void updateSupportedFeatures(String componentId, Set<QName> supportedFeatures) {
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();
        for (QName supportedFeature : supportedFeatures) {
            Module module = getModuleByNamespace(supportedFeature.getModule().getNamespace().toString());
            ModuleIdentifier moduleIdentifier = ModuleIdentifierImpl.create(module.getName(), Optional.of(module
                    .getNamespace()), Optional.of(module.getRevision()));
            Set<QName> supportedFeaturesPerModule = m_supportedFeatures.get(moduleIdentifier);
            if (supportedFeaturesPerModule == null) {
                supportedFeaturesPerModule = new HashSet<>();
                m_supportedFeatures.put(moduleIdentifier, supportedFeaturesPerModule);
                moduleIdentifiers.add(moduleIdentifier);
            }
            supportedFeaturesPerModule.add(supportedFeature);
        }
        m_componentIdModuleIdentifiersMap.put(componentId, moduleIdentifiers);
    }

    private void updateSupportedDeviations(String componentId, Map<QName, Set<QName>> supportedDeviations) {
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();
        for (Map.Entry<QName, Set<QName>> entry : supportedDeviations.entrySet()) {
            for (QName deviation : entry.getValue()) {
                Module module = getModuleByNamespace(entry.getKey().getNamespace().toString());
                ModuleIdentifier moduleIdentifier = ModuleIdentifierImpl.create(module.getName(), Optional.of(module
                        .getNamespace()), Optional.of(module.getRevision()));
                Set<QName> supportedDeviationsPerModule = m_supportedDeviations.get(moduleIdentifier);
                if (supportedDeviationsPerModule == null) {
                    supportedDeviationsPerModule = new HashSet<>();
                    m_supportedDeviations.put(moduleIdentifier, supportedDeviationsPerModule);
                    moduleIdentifiers.add(moduleIdentifier);
                }
                supportedDeviationsPerModule.add(deviation);
            }
        }
        m_componentIdModuleIdentifiersMap.put(componentId, moduleIdentifiers);
    }

    @Override
    public Collection<DataSchemaNode> getRootDataSchemaNodes() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Collection<DataSchemaNode>>() {
            @Override
            public Collection<DataSchemaNode> execute() {
                return m_rootSchemaNodes.values();
            }
        });
    }

    @Override
    public Module getModule(final String name, final String revision) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Module>() {
            @Override
            public Module execute() {
                Date date;
                if (revision == null || revision.isEmpty()) {
                    date = SimpleDateFormatUtil.DEFAULT_DATE_REV;
                } else {
                    date = DATE_TIME_FORMATTER.parseDateTime(revision).toDate();
                }
                return getModule(name, date);
            }
        });
    }

    @Override
    public Module getModule(final String name) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Module>() {
            @Override
            public Module execute() {
                return getModule(name, (Date) null);
            }
        });
    }

    @Override
    public Module getModule(final String name, final Date revision) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Module>() {
            @Override
            public Module execute() {
                return m_schemaContext.findModuleByName(name, revision);
            }
        });
    }

    @Override
    public Collection<RpcDefinition> getRpcDefinitions() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Collection<RpcDefinition>>() {
            @Override
            public Collection<RpcDefinition> execute() {
                return m_allRpcDefinitions.values();
            }
        });
    }

    @Override
    public RpcDefinition getRpcDefinition(final SchemaPath schemaPath) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<RpcDefinition>() {
            @Override
            public RpcDefinition execute() {
                return m_allRpcDefinitions.get(schemaPath);
            }
        });
    }

    @Override
    public synchronized void loadSchemaContext(final String componentId, final List<YangTextSchemaSource>
            yangModelByteSources, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations) throws
            SchemaBuildException {
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    LOGGER.info("loading YANG modules from component {}", componentId);
                    if (m_componentModules.get(componentId) != null) {
                        throw new LockServiceException("Schema Registry already contains modules of component: " +
                                componentId);
                    }
                    try {
                        if (!yangModelByteSources.isEmpty()) {
                            m_componentModules.put(componentId, yangModelByteSources);
                            if (supportedDeviations != null) {
                                m_supportedPlugDeviations.putAll(supportedDeviations);
                            }
                            rebuildFromSource();
                            updateIndexes();
                        }
                        updateCapabilities(componentId, supportedFeatures, supportedDeviations, true);
                    } catch (Exception e) {
                        LOGGER.error("Error while updating schema context", e);
                        m_componentModules.remove(componentId);
                        throw new LockServiceException("Error while updating schema context", e);
                    }
                    LOGGER.info("loading YANG modules from component {} complete", componentId);
                    return null;
                }
            });
            SchemaRegistryUtil.resetCache();
        } catch (LockServiceException e) {
            throw new SchemaBuildException(e.getCause());
        }
    }

    @Override
    public synchronized void unloadSchemaContext(final String componentId, Map<QName, Set<QName>>
            supportedDeviations) throws SchemaBuildException {
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    LOGGER.info("unloadSchemaContext called for componentId : {}", componentId);
                    if (m_componentModules.get(componentId) != null) {
                        m_componentModules.remove(componentId);
                        try {
                            if (supportedDeviations != null) {
                                m_supportedPlugDeviations.remove(supportedDeviations);
                            }
                            rebuildFromSource();
                            updateIndexes();
                        } catch (SchemaBuildException e) {
                            throw new LockServiceException(e);
                        }
                        updateCapabilities(componentId, null, null, false);
                    }
                    return null;
                }
            });
        } catch (LockServiceException e) {
            throw new SchemaBuildException(e.getCause());
        }
    }

    private void removeSupportedFeatures(String componentId) {
        Set<ModuleIdentifier> moduleIdentifiers = m_componentIdModuleIdentifiersMap.get(componentId);
        if (moduleIdentifiers != null) {
            for (ModuleIdentifier moduleIdentifier : moduleIdentifiers) {
                m_supportedFeatures.remove(moduleIdentifier);
            }
        }
    }

    private void removeSupportedDeviations(String componentId) {
        Set<ModuleIdentifier> moduleIdentifiers = m_componentIdModuleIdentifiersMap.get(componentId);
        if (moduleIdentifiers != null) {
            for (ModuleIdentifier moduleIdentifier : moduleIdentifiers) {
                m_supportedDeviations.remove(moduleIdentifier);
            }
        }
    }

    @Override
    public Set<ModuleIdentifier> getAllModuleAndSubmoduleIdentifiers() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Set<ModuleIdentifier>>() {
            @Override
            public Set<ModuleIdentifier> execute() {
                return m_schemaContext.getAllModuleIdentifiers();
            }
        });
    }

    @Override
    public Set<Module> getAllModules() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Set<Module>>() {
            @Override
            public Set<Module> execute() {
                return m_schemaContext.getModules();
            }
        });
    }

    @Override
    public Map<SourceIdentifier, YangTextSchemaSource> getAllYangTextSchemaSources() throws SchemaBuildException {
        Map<SourceIdentifier, YangTextSchemaSource> yangTextSchemaSources = new HashMap<>();
        try {
            for (List<YangTextSchemaSource> byteSources : m_componentModules.values()) {
                for (YangTextSchemaSource schemaSource : byteSources) {
                    CheckedFuture<ASTSchemaSource, SchemaSourceException> aSTSchemaSource = Futures
                            .immediateCheckedFuture(TextToASTTransformer.transformText(schemaSource));
                    SettableSchemaProvider<ASTSchemaSource> schemaProvider = SettableSchemaProvider.createImmediate(
                            aSTSchemaSource.get(), ASTSchemaSource.class);
                    SourceIdentifier id = schemaProvider.getId();
                    if (!yangTextSchemaSources.containsKey(id)) {
                        yangTextSchemaSources.put(id, schemaSource);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error parsing files", e);
            throw new SchemaBuildException("Error while getting yang schema context", e);
        }
        return yangTextSchemaSources;
    }

    private void rebuildFromSource() throws SchemaBuildException {
        List<YangTextSchemaSource> allYangSources = new ArrayList<>();
        for (List<YangTextSchemaSource> byteSources : m_componentModules.values()) {
            allYangSources.addAll(byteSources);
        }
        try {
            LOGGER.info("rebuilding schemaContext");
            m_schemaContext = YangParserUtil.parseSchemaSources(SchemaRegistryImpl.class.getName(), allYangSources,
                    null, m_supportedPlugDeviations);
            LOGGER.info("rebuilding schemaContext done");
        } catch (Exception e) {
            LOGGER.error("Error while reloading schema context", e);
            throw new SchemaBuildException("Error while reloading schema context", e);
        }
    }

    @Override
    public DataSchemaNode getDataSchemaNode(final SchemaPath dataNodeSchemaPath) {

        DataSchemaNode schemaNode = getDataSchemaNodeFromCache(dataNodeSchemaPath);
        if (schemaNode != null) {
            return schemaNode;
        }
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<DataSchemaNode>() {
            @Override
            public DataSchemaNode execute() {
                DataSchemaNode schemaNode = m_schemaNodes.get(dataNodeSchemaPath);

                if (schemaNode != null) {
                    addDataSchemaNodeToCache(dataNodeSchemaPath, schemaNode);
                }
                return schemaNode;
            }
        });
    }

    @Override
    public Collection<DataSchemaNode> getChildren(final SchemaPath parentSchemaPath) {
        //TODO: This addition of cache, fails the startup of anv. Need to investigate and add if possible
//        Collection<DataSchemaNode> cacheChildren = getFromCache(parentSchemaPath);
//        if (cacheChildren != null) {
//            return cacheChildren;
//        }
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Collection<DataSchemaNode>>() {
            @Override
            public Collection<DataSchemaNode> execute() {
                DataSchemaNode parentNode = getDataSchemaNode(parentSchemaPath);
                if (parentNode != null && parentNode instanceof DataNodeContainer) {
                    Collection<DataSchemaNode> returnList = ((DataNodeContainer) parentNode).getChildNodes();
//                    addToCache(parentSchemaPath, returnList);
                    return returnList;
                }
                return Collections.emptySet();
            }
        });
    }

    @Override
    public DataSchemaNode getChild(final SchemaPath parentSchemaPath, final QName childQName) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<DataSchemaNode>() {
            @Override
            public DataSchemaNode execute() {
                DataSchemaNode parentNode = getDataSchemaNode(parentSchemaPath);
                if (parentNode != null && parentNode instanceof DataNodeContainer) {
                    return ((DataNodeContainer) parentNode).getDataChildByName(childQName);
                }
                return null;
            }
        });
    }

    private void addChildCaseNodes(ChoiceSchemaNode choiceNode, Map<QName, DataSchemaNode> childList) {
        Collection<ChoiceCaseNode> cases = choiceNode.getCases();
        for (ChoiceCaseNode caseNode : cases) {
            Collection<DataSchemaNode> children = caseNode.getChildNodes();
            for (DataSchemaNode child : children) {
                if (child instanceof ChoiceSchemaNode) {
                    addChildCaseNodes((ChoiceSchemaNode) child, childList);
                } else {
                    childList.put(child.getQName(), child);
                }
            }
        }
    }

    @Override
    public Map<QName, DataSchemaNode> getIndexedChildren(final SchemaPath parentSchemaPath) {
        Map<QName, DataSchemaNode> cacheList = getFromIndexedCache(parentSchemaPath);
        if (cacheList != null) {
            return cacheList;
        }

        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Map<QName, DataSchemaNode>>() {
            @Override
            public Map<QName, DataSchemaNode> execute() {
                Map<QName, DataSchemaNode> returnList = new HashMap<QName, DataSchemaNode>(1000);
                DataSchemaNode parentNode = getDataSchemaNode(parentSchemaPath);
                if (parentNode != null && parentNode instanceof DataNodeContainer) {
                    Collection<DataSchemaNode> childNodes = ((DataNodeContainer) parentNode).getChildNodes();
                    for (DataSchemaNode child : childNodes) {
                        if (child instanceof ChoiceSchemaNode) {
                            addChildCaseNodes((ChoiceSchemaNode) child, returnList);
                        } else {
                            returnList.put(child.getQName(), child);
                        }
                    }
                }
                addToCache(parentSchemaPath, returnList);
                return returnList;
            }
        });
    }

    public DataSchemaNode getDataSchemaNode(final List<QName> qNames) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<DataSchemaNode>() {
            @Override
            public DataSchemaNode execute() {
                DataSchemaNode currentNode = getSchemaContext();
                for (QName qname : qNames) {
                    currentNode = findChild(currentNode, qname);
                }
                return currentNode;
            }
        });
    }

    public DataSchemaNode findChild(final DataSchemaNode currentNode, final QName qname) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<DataSchemaNode>() {
            @Override
            public DataSchemaNode execute() {
                if (currentNode == null) {
                    SchemaPath schemaPath = SchemaPath.create(true, qname);
                    return m_schemaNodes.get(schemaPath);
                }
                if (currentNode instanceof DataNodeContainer) {
                    DataNodeContainer dnc = (DataNodeContainer) currentNode;
                    Collection<DataSchemaNode> childNodes = dnc.getChildNodes();
                    for (DataSchemaNode childNode : childNodes) {
                        if (childNode.getQName().equals(qname)) {
                            return childNode;
                        } else if (childNode instanceof ChoiceSchemaNode) {
                            ChoiceSchemaNode choiceNode = (ChoiceSchemaNode) childNode;
                            for (ChoiceCaseNode caseNode : choiceNode.getCases()) {
                                for (DataSchemaNode caseChild : caseNode.getChildNodes()) {
                                    if (caseChild.getQName().equals(qname)) {
                                        return caseChild;
                                    }
                                }
                            }
                        }
                    }
                }
                throw new RuntimeException("Could not find child " + qname + " in parent " + currentNode.getPath());
            }
        });
    }

    @Override
    public Collection<DataSchemaNode> getNonChoiceChildren(final SchemaPath parentSchemaPath) {

        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Collection<DataSchemaNode>>() {
            @Override
            public Collection<DataSchemaNode> execute() {
                Collection<DataSchemaNode> effectiveChildSchemaNode = new HashSet<DataSchemaNode>();
                Collection<DataSchemaNode> childrenSchemaNodes = getChildren(parentSchemaPath);
                for (DataSchemaNode dataSchemaNode : childrenSchemaNodes) {
                    if (dataSchemaNode instanceof ChoiceSchemaNode) {
                        addNonChoiceChildren(effectiveChildSchemaNode, (ChoiceSchemaNode) dataSchemaNode);
                    } else {
                        effectiveChildSchemaNode.add(dataSchemaNode);
                    }
                }
                return effectiveChildSchemaNode;
            }
        });

    }

    @Override
    public DataSchemaNode getNonChoiceChild(SchemaPath parentSchemaPath, QName qName) {
        Map<QName, DataSchemaNode> children = getIndexedChildren(parentSchemaPath);
        return children.get(qName);
    }

    private void addNonChoiceChildren(Collection<DataSchemaNode> effectiveChildSchemaNode, ChoiceSchemaNode
            choiceSchemaNode) {
        Set<ChoiceCaseNode> caseNodeSet = ((ChoiceSchemaNode) choiceSchemaNode).getCases();
        for (ChoiceCaseNode caseNode : caseNodeSet) {
            Collection<DataSchemaNode> childNodes = caseNode.getChildNodes();
            for (DataSchemaNode child : childNodes) {
                if (child instanceof ChoiceSchemaNode) {
                    addNonChoiceChildren(effectiveChildSchemaNode, (ChoiceSchemaNode) child);
                } else {
                    effectiveChildSchemaNode.add(child);
                }
            }
        }
    }

    @Override
    public DataSchemaNode getNonChoiceParent(SchemaPath schemaPath) {
        return SchemaRegistryUtil.getDataParent(this, schemaPath);
    }

    @Override
    public boolean isKnownNamespace(final String namespaceURI) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Boolean>() {
            @Override
            public Boolean execute() {
                return m_modules.keySet().contains(namespaceURI);
            }
        });
    }

    @Override
    public Set<ModuleIdentifier> getAllModuleIdentifiers() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Set<ModuleIdentifier>>() {
            @Override
            public Set<ModuleIdentifier> execute() {
                Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();
                for (Module module : m_schemaContext.getModules()) {
                    moduleIdentifiers.add(ModuleIdentifierImpl.create(module.getName(), Optional.of(module
                            .getNamespace()), Optional.of(module.getRevision())));
                }
                return moduleIdentifiers;
            }
        });
    }

    @Override
    public List<YangTextSchemaSource> getYangModelByteSourcesOfAPlugin(String componentId) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<List<YangTextSchemaSource>>() {
            @Override
            public List<YangTextSchemaSource> execute() {

                List<YangTextSchemaSource> yangModelByteSources = m_componentModules.get(componentId);
                return yangModelByteSources;
            }
        });
    }


    @Override
    public QName lookupQName(final String namespace, final String localName) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<QName>() {
            @Override
            public QName execute() {
                ModuleIdentifier module = m_modules.get(namespace);
                if (module != null) {
                    QName qName = QName.create(module.getQNameModule(), localName);
                    return qName;
                }

                return null;
            }
        });

    }

    @Override
    public String getPrefix(final String namespace) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<String>() {
            @Override
            public String execute() {
                Module module = m_modules.get(namespace);
                if (module != null) {
                    return module.getPrefix();
                }
                return null;
            }
        });
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<String>() {
            @Override
            public String execute() {
                for (Module module : m_modules.values()) {
                    if (module.getPrefix().equals(prefix)) {
                        return module.getNamespace().toString();
                    }
                }
                return null;
            }
        });
    }

    @Override
    public String getModuleNameByNamespace(final String namespace) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<String>() {
            @Override
            public String execute() {
                Module module = m_modules.get(namespace);
                return module != null ? module.getName() : null;
            }
        });

    }

    @Override
    public String getNamespaceOfModule(final String moduleName) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<String>() {
            @Override
            public String execute() {
                for (Module module : m_modules.values()) {
                    if (module.getName().equals(moduleName)) {
                        return module.getNamespace().toString();
                    }
                }
                return null;
            }
        });
    }


    @Override
    public Iterator<?> getPrefixes(final String namespace) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Iterator<?>>() {
            @Override
            public Iterator<?> execute() {
                List<String> prefixes = Collections.emptyList();
                String prefix = getPrefix(namespace);
                if (prefix != null) {
                    prefixes = new ArrayList<>();
                    prefixes.add(prefix);
                }
                return prefixes.iterator();
            }
        });
    }

    @Override
    public Set<SchemaPath> getRootSchemaPaths() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Set<SchemaPath>>() {
            @Override
            public Set<SchemaPath> execute() {
                return m_rootSchemaNodes.keySet();
            }
        });
    }

    private void updateIndexes() throws SchemaBuildException {
        indexRpcDefinitions();
        indexDataSchemaNodes();
        indexRootSchemaNodes();
        indexNamespaces();

    }

    public void updateCapabilities(String componentId, Set<QName> supportedFeatures,
                                   Map<QName, Set<QName>> supportedDeviations, boolean isDeploy) {
        if (!isDeploy) {
            removeSupportedFeatures(componentId);
            removeSupportedDeviations(componentId);
        } else {
            if (supportedFeatures != null) {
                updateSupportedFeatures(componentId, supportedFeatures);
            } else {
                addAllSupportedFeatures(componentId);
            }
            if (supportedDeviations != null) {
                updateSupportedDeviations(componentId, supportedDeviations);
            } else {
                addAllSupportedDeviations(componentId);
            }
        }
        String newModuleSetId = computeModuleSetId();
        LOGGER.info("Module-set-id {} generated after deploy status {} for component-id {}", newModuleSetId,
                isDeploy, componentId);
        if (isYangLibraryChangeNotificationNeeded(newModuleSetId)) {
            m_moduleSetId = newModuleSetId;
            if (m_yangLibraryChangeNotificationListener != null) {
                m_yangLibraryChangeNotificationListener.sendYangLibraryChangeNotification(m_moduleSetId);
            }
        }
        LOGGER.info("re-indexing schema node maps done");
    }

    private boolean isYangLibraryChangeNotificationNeeded(String newModuleSetId) {
        return !newModuleSetId.equals(m_moduleSetId);
    }

    private void indexNamespaces() {
        m_modules.clear();
        for (Module module : m_schemaContext.getModules()) {
            m_modules.put(module.getNamespace().toString(), module);
        }
    }

    private void indexRootSchemaNodes() {
        m_rootSchemaNodes.clear();
        for (DataSchemaNode rootSchemaNode : m_schemaContext.getDataDefinitions()) {
            m_rootSchemaNodes.put(rootSchemaNode.getPath(), rootSchemaNode);
        }
    }

    private void indexDataSchemaNodes() {
        m_schemaNodes.clear();
        DataNodeContainerTraverser.traverse(m_schemaContext, new DataSchemaNodeIndexBuilder(m_schemaNodes));
    }

    private void indexRpcDefinitions() {
        m_allRpcDefinitions.clear();
        Map<SchemaPath, RpcDefinition> rpcs = new HashMap<>();
        for (RpcDefinition rpc : m_schemaContext.getOperations()) {
            rpcs.put(rpc.getPath(), rpc);
        }

        m_allRpcDefinitions.putAll(rpcs);
    }

    public static SchemaRegistry buildSchemaRegistry(List<String> coreYangModelFilesPaths, ReadWriteLockService
            readWriteLockService) throws SchemaBuildException {
        return buildSchemaRegistry(coreYangModelFilesPaths, null, null, true, readWriteLockService);
    }

    public static SchemaRegistry buildSchemaRegistry(List<String> coreYangModelFilesPaths, boolean
            isYangLibrarySupportedInHelloMessage, ReadWriteLockService readWriteLockService) throws
            SchemaBuildException {
        return buildSchemaRegistry(coreYangModelFilesPaths, null, null, isYangLibrarySupportedInHelloMessage,
                readWriteLockService);
    }

    public static SchemaRegistry buildSchemaRegistry(List<String> coreYangModelFilesPaths,
                                                     Map<String, Set<String>> deviations, ReadWriteLockService
                                                             readWriteLockService) throws SchemaBuildException {
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

    public static SchemaRegistry buildSchemaRegistry(List<String> coreYangModelFilesPaths,
                                                     Map<String, Set<String>> deviations, boolean
                                                             isYangLibrarySupportedInHelloMessage,
                                                     ReadWriteLockService readWriteLockService) throws
            SchemaBuildException {
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
        return buildSchemaRegistry(coreYangModelFilesPaths, null, supportedDeviations,
                isYangLibrarySupportedInHelloMessage, readWriteLockService);
    }

    public static SchemaRegistry buildSchemaRegistry(List<String> coreYangModelFilesPaths, Set<String> features,
                                                     Map<String, Set<String>> deviations, ReadWriteLockService
                                                             readWriteLockService) throws SchemaBuildException {

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
        return buildSchemaRegistry(coreYangModelFilesPaths, supportedFeatures, supportedDeviations, true,
                readWriteLockService);
    }

    public static SchemaRegistry buildSchemaRegistry(List<String> coreYangModelFilesPaths, Set<QName>
            supportedFeatures, Map<QName, Set<QName>> supportedDeviations, boolean
            isYangLibrarySupportedInHelloMessage, ReadWriteLockService readWriteLockService) throws
            SchemaBuildException {
        List<YangTextSchemaSource> byteSources = new ArrayList<>();
        for (String coreYangModuleFilePath : coreYangModelFilesPaths) {
            byteSources.add(YangParserUtil.getYangSource(SchemaRegistryImpl.class.getResource(coreYangModuleFilePath)));
        }
        return new SchemaRegistryImpl(byteSources, supportedFeatures, supportedDeviations,
                isYangLibrarySupportedInHelloMessage, readWriteLockService);
    }

    @Override
    public SchemaPath getDescendantSchemaPath(final SchemaPath parentSchemaPath, final QName qname) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<SchemaPath>() {
            @Override
            public SchemaPath execute() {
                DataSchemaNode parentSchemaNode = getDataSchemaNode(parentSchemaPath);
                if (parentSchemaNode instanceof DataNodeContainer) {
                    DataNodeContainer containerSchemaNode = (DataNodeContainer) parentSchemaNode;
                    DataSchemaNode childDataSchemaNode = containerSchemaNode.getDataChildByName(qname);
                    if (childDataSchemaNode != null) {
                        return childDataSchemaNode.getPath();
                    }
                    for (DataSchemaNode child : containerSchemaNode.getChildNodes()) {
                        SchemaPath childPath = getDescendantSchemaPath(child.getPath(), qname);
                        if (childPath != null) {
                            return childPath;
                        }
                    }
                } else if (parentSchemaNode instanceof ChoiceSchemaNode) {
                    ChoiceSchemaNode choiceSchemaNode = (ChoiceSchemaNode) parentSchemaNode;
                    for (DataSchemaNode child : choiceSchemaNode.getCases()) {
                        SchemaPath childPath = getDescendantSchemaPath(child.getPath(), qname);
                        if (childPath != null) {
                            return childPath;
                        }
                    }
                }

                return null;
            }
        });
    }

    @Override
    public Module getModuleByNamespace(final String namespace) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Module>() {
            @Override
            public Module execute() {
                return m_modules.get(namespace);
            }
        });
    }

    @Override
    public Module findModuleByNamespaceAndRevision(final URI namespace, final Date revision) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Module>() {
            @Override
            public Module execute() {
                return m_schemaContext.findModuleByNamespaceAndRevision(namespace, revision);
            }
        });
    }

    @Override
    public void registerNodesReferencedInConstraints(String componentId, SchemaPath referencedSchemaPath, SchemaPath
            nodeSchemaPath, String accessPath) {
        /**
         * container validation {
         *    leaf validation{
         *        type int8;
         *    }
         *
         *    leaf validation1 {
         *        when "../validation < 1";
         *    }
         * }
         *
         * Here when a modification on leaf validation happens, the node (validation1) on which it is referenced as a
         * constraint
         * must also be validated. 
         *
         * So here for the nodeSchemaPath (validation), referencedSchemaPath(validation1) must also be validated 
         * when leaf validation undergoes a change. 
         */
        Map<SchemaPath, Expression> impactedNodes = m_impactedNodesForConstraints.get(nodeSchemaPath);
        impactedNodes.put(referencedSchemaPath, JXPathUtils.getExpression(accessPath));

        Collection<SchemaPath> schemaPathList = m_componentIdSchemaPathMap.get(componentId);
        schemaPathList.add(nodeSchemaPath);
    }

    @Override
    public void deRegisterNodesReferencedInConstraints(final String componentId) {
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    Collection<SchemaPath> schemaPathList = m_componentIdSchemaPathMap.get(componentId);
                    for (SchemaPath schemaPath : schemaPathList) {
                        m_impactedNodesForConstraints.remove(schemaPath);
                    }
                    m_componentIdSchemaPathMap.remove(componentId);

                    Collection<String> absPaths = m_componentIdAbsSchemaPaths.get(componentId);
                    for (String absPath : absPaths) {
                        deRegisterAppAllowedAugmentedPath(absPath);
                    }

                    m_componentIdAbsSchemaPaths.remove(componentId);

                    return null;
                }
            });
        } catch (LockServiceException e) {
            LOGGER.error("Error while unloading impacted nodes for constraints", e);
        }
    }

    @Override
    public SchemaNode getActionDefinitionNode(List<QName> paths) {
        SchemaPath actionPath = SchemaPath.create(paths, true);
        SchemaPath parentPath = actionPath.getParent();
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<SchemaNode>() {
            @Override
            public SchemaNode execute() {
                Collection<ActionDefinition> actionNodes = m_schemapathToActionDefinitions.get(parentPath);
                if (actionNodes != null) {
                    for (ActionDefinition node : actionNodes) {
                        if (node.getPath().equals(actionPath)) {
                            return node;
                        }
                    }
                }
                return null;
            }
        });

    }

    @Override
    public void registerActionSchemaNode(String componentId, SchemaPath nodeSchemaPath,
                                         Set<ActionDefinition> actionDefinitions) {

        Collection<ActionDefinition> actionDefs = m_schemapathToActionDefinitions.get(nodeSchemaPath);
        actionDefs.addAll(actionDefinitions);

        Collection<SchemaPath> schemaPathList = m_componentIdToActionRootSchemaPath.get(componentId);
        schemaPathList.add(nodeSchemaPath);
    }

    @Override
    public void deRegisterActionSchemaNodes(String componentId) {
        try {
            m_readWriteLockService.executeWithWriteLock(new WriteLockTemplate<Void>() {
                @Override
                public Void execute() throws LockServiceException {
                    Collection<SchemaPath> schemaPathList = m_componentIdToActionRootSchemaPath.get(componentId);
                    for (SchemaPath schemaPath : schemaPathList) {
                        m_schemapathToActionDefinitions.remove(schemaPath);
                    }
                    m_componentIdToActionRootSchemaPath.remove(componentId);

                    return null;
                }
            });
        } catch (LockServiceException e) {
            LOGGER.error("Error while unloading schemapath for action nodes", e);
        }
    }

    @Override
    public Collection<SchemaPath> getSchemaPathsForComponent(final String componentId) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Collection<SchemaPath>>() {
            @Override
            public Collection<SchemaPath> execute() {
                return m_componentIdSchemaPathMap.get(componentId);
            }
        });
    }

    @Override
    public Map<SchemaPath, Expression> getReferencedNodesForSchemaPaths(final SchemaPath schemaPath) {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Map<SchemaPath, Expression>>() {
            @Override
            public Map<SchemaPath, Expression> execute() {
                return m_impactedNodesForConstraints.get(schemaPath);
            }
        });
    }

    @Override
    public void registerAppAllowedAugmentedPath(String moduleId, String path, SchemaPath schemaPath) {
        LOGGER.debug("registering app relative path {}", path);
        if (m_relativePath.contains(path)) {
            LOGGER.error("Augmented path {} is already registered", path);
            throw new RuntimeException("Augmented path " + path + " is already registered");
        } else {
            m_relativePath.add(path);
            m_appAugmentedPathToSchemaPathMap.put(path, schemaPath);
        }
        if (moduleId != null) {
            Set<String> augPaths = m_componentIdAbsSchemaPaths.get(moduleId);
            augPaths.add(path);
            m_appAugmentedPathToComponentIdMap.put(schemaPath, moduleId);
        }
    }

    @Override
    public void deRegisterAppAllowedAugmentedPath(String path) {
        LOGGER.debug("removing app relative path {}", path);
        m_appAugmentedPathToComponentIdMap.remove(m_appAugmentedPathToSchemaPathMap.get(path));
        m_appAugmentedPathToSchemaPathMap.remove(path);
        m_relativePath.remove(path);
    }

    @Override
    public void registerRelativePath(String augmentedPath, String relativePath, DataSchemaNode dataSchemaNode) {
        LOGGER.debug("Registering relative path: {} for augmentedPath: {} and dataSchemaNode:{}", relativePath,
                augmentedPath, dataSchemaNode);
        Map<DataSchemaNode, Expression> relativePaths = m_relativePaths.get(augmentedPath);
        relativePaths.putIfAbsent(dataSchemaNode, JXPathUtils.getExpression(relativePath));
    }

    @Override
    public Expression getRelativePath(String augmentedPath, DataSchemaNode dataSchemaNode) {
        Expression relativePath = m_relativePaths.get(augmentedPath).get(dataSchemaNode);
        LOGGER.debug("Relative path is {} for augmentedPath:{}, dataSchemaNode:{}", relativePath, augmentedPath,
                dataSchemaNode);
        return relativePath;
    }

    @Override
    public String getMatchingPath(String path) {
        for (String augmentedPath : m_relativePath) {
            if (path.startsWith(augmentedPath)) {
                LOGGER.debug("path {} starts with augmentation {}", path, augmentedPath);
                return augmentedPath;
            }
        }
        return null;
    }

    public boolean isYangLibrarySupportedInHelloMessage() {
        return m_isYangLibrarySupportedInHelloMessage;
    }

    //only for UT
    public void setYangLibrarySupportInHelloMessage(boolean value) {
        m_isYangLibrarySupportedInHelloMessage = value;
    }

    @Override
    public Set<String> getModuleCapabilities(boolean forHello) {
        Set<String> caps = new HashSet<>();

        if (forHello && isYangLibrarySupportedInHelloMessage()) {
            return getFilteredCapsForHelloMessage();
        } else {
            for (ModuleIdentifier moduleId : getAllModuleIdentifiers()) {
                String capability = getCapability(moduleId);
                caps.add(capability);
            }
            return caps;
        }
    }

    @Override
    public String getCapability(ModuleIdentifier moduleId) {
        Set<QName> features = m_supportedFeatures.get(moduleId);
        Set<QName> deviations = m_supportedDeviations.get(moduleId);
        String revisionDate = DATE_TIME_FORMATTER.print(moduleId.getRevision().getTime());
        String url = moduleId.getNamespace().toString();
        String moduleName = moduleId.getName();
        if ((features != null && !features.isEmpty()) || (deviations != null && !deviations.isEmpty())) {
            String supportedFeatures = null;
            String supportedDeviations = null;
            if (features != null && !features.isEmpty()) {
                supportedFeatures = getCommaSeparatedFeatures(features);
            }
            if (deviations != null && !deviations.isEmpty()) {
                supportedDeviations = getCommaSeparatedDeviations(deviations);
            }
            return getCapability(url, revisionDate, moduleName, supportedFeatures, supportedDeviations);
        } else {
            return getCapability(url, revisionDate, moduleName);
        }
    }

    private String getCapability(String url, String revisionDate,
                                 String moduleName, String supportedFeatures, String supportedDeviations) {
        return (new NetconfCapability(url, moduleName, revisionDate, supportedFeatures, supportedDeviations))
                .toString();
    }

    private Set<String> getFilteredCapsForHelloMessage() {
        Set<String> filteredCaps = new HashSet<>();
        for (Module module : getAllModules()) {
            if (module.getYangVersion().equals("1")) {
                filteredCaps.add(getCapability(module));
            }
        }
        String yangLibraryCapString = String.format(YANG_LIBRARY_CAP_FORMAT, m_moduleSetId);
        filteredCaps.add(yangLibraryCapString);
        return filteredCaps;
    }

    private String getCapability(String url, String revisionDate,
                                 String moduleName) {
        return (new NetconfCapability(url, moduleName, revisionDate)).toString();
    }

    private String getCommaSeparatedFeatures(Set<QName> features) {

        List<String> featureLocalNames = new ArrayList<>();
        for (QName qName : features) {
            featureLocalNames.add(qName.getLocalName());
        }
        Collections.sort(featureLocalNames);
        String supportedFeatures = StringUtils.join(featureLocalNames, COMMA);
        return supportedFeatures;
    }

    private String getCommaSeparatedDeviations(Set<QName> deviations) {
        List<String> deviationNames = new ArrayList<>();
        for (QName qName : deviations) {
            deviationNames.add(qName.getLocalName());
        }
        String supportedDeviations = StringUtils.join(deviationNames, COMMA);
        return supportedDeviations;
    }

    private String computeModuleSetId() {
        Set<String> caps = getModuleCapabilities(false);
        List<String> capsList = new ArrayList<>(caps);
        Collections.sort(capsList);
        StringBuffer sb = new StringBuffer();
        for (String capability : capsList) {
            sb.append(capability);
            sb.append(System.lineSeparator());
        }
        String capsString = sb.toString();
        String hashString = getHashedString(capsString);
        LOGGER.info(null, "Module-set-id {} generated for the available yang modules {}", hashString, capsString);
        return hashString;
    }

    private String getHashedString(String originalString) {
        String hashedString = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(originalString.getBytes("UTF-8"));
            String hexString = DatatypeConverter.printHexBinary(hash);
            return hexString.toLowerCase();
        } catch (Exception e) {
            LOGGER.error("Error while computing hashed string for {}", originalString, e);
        }
        return hashedString;
    }

    @Override
    public String getModuleSetId() {
        return m_moduleSetId;
    }

    /**
     * For UT
     */
    protected Set<String> getRelativePathCollection() {
        return new HashSet<>(m_relativePath);
    }

    @Override
    public void registerYangLibraryChangeNotificationListener(YangLibraryChangeNotificationListener listener) {
        m_yangLibraryChangeNotificationListener = listener;
    }

    @Override
    public void unregisterYangLibraryChangeNotificationListener() {
        m_yangLibraryChangeNotificationListener = null;
    }

    @Override
    public Map<SchemaPath, String> retrieveAppAugmentedPathToComponent() {
        return m_appAugmentedPathToComponentIdMap;
    }

    public Set<ActionDefinition> retrieveAllActionDefinitions() {
        return m_readWriteLockService.executeWithReadLock(new ReadLockTemplate<Set<ActionDefinition>>() {
            @Override
            public Set<ActionDefinition> execute() {
                Set<ActionDefinition> actionDefsList = new HashSet<>();
                for (SchemaPath path : m_schemapathToActionDefinitions.keySet()) {
                    actionDefsList.addAll(m_schemapathToActionDefinitions.get(path));
                }
                return actionDefsList;
            }
        });

    }

    public Map<ModuleIdentifier, Set<QName>> getSupportedDeviations() {
        return m_supportedDeviations;
    }

    @Override
    public DataSchemaNode getRPCInputChildNode(RpcDefinition rpcDef, List<QName> qnames) {
        DataNodeContainer node = rpcDef.getInput();
        for (QName qname : qnames) {
            DataSchemaNode innerNode = node.getDataChildByName(qname);
            if (innerNode != null && innerNode instanceof DataNodeContainer) {
                node = (DataNodeContainer) innerNode;
            }
        }
        return (DataSchemaNode) node;
    }

    /**
     * For UTs only
     *
     * @param readWriteLockService
     * @param <T>
     */
    protected <T> void setReadWriteLockService(ReadWriteLockService readWriteLockService) {
        m_readWriteLockService = readWriteLockService;
    }
}