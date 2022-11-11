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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.apache.commons.lang3.StringUtils;
import org.broadband_forum.obbaa.netconf.api.NetconfCapability;
import org.broadband_forum.obbaa.netconf.api.parser.SettableSchemaProvider;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DataPath;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.ConstraintValidatorFactoryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.SchemaNodeConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.ChoiceCaseNodeUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.dom.YinAnnotationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.listener.YangLibraryChangeNotificationListener;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.DefaultConcurrentHashMap;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.LockServiceException;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchemaNode;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DeviateDefinition;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.FeatureDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.ASTSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToASTTransformer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import javax.xml.bind.DatatypeConverter;

/**
 * Created by keshava on 11/18/15.
 */
public class DecoratedSchemaRegistryImpl implements SchemaRegistry {
    private static final String COMMA = ",";
    public static final String VNO = "VNO";
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DecoratedSchemaRegistryImpl.class, LogAppNames.NETCONF_STACK);
    static final String PATHMAPPINGCACHE = DecoratedSchemaRegistryImpl.class + "_PATHMAPPING";
    public static final String REVISION = "?revision=";

    private SchemaContext m_schemaContext;

    private Map<SchemaPath, RpcDefinition> m_allRpcDefinitions = new LinkedHashMap<>();
    private Map<String, List<YangTextSchemaSource>> m_componentModules = new LinkedHashMap<>();
    private Map<String, Set<LocationPath>> m_expressionsWithoutKeysInList = new HashMap<>();
    private Map<SchemaPath, String> m_shortPaths = new HashMap<>();
    private static ServiceListener c_serviceListener;
    private static YinAnnotationService c_yinAnnotationService;
    private Map<String, List<String>> m_prefixes = new LinkedHashMap<>();

    static {
        c_serviceListener = event -> {
            try {
                ServiceReference<?> ref = event.getServiceReference();
                Bundle bundle = ref.getBundle();
                Object service = bundle.getBundleContext().getService(ref);

                switch (event.getType()) {
                    case ServiceEvent.UNREGISTERING:
                        if (service instanceof YinAnnotationService) {
                            c_yinAnnotationService = null;
                        }
                        break;
                    case ServiceEvent.REGISTERED: {
                        if (service instanceof YinAnnotationService) {
                            c_yinAnnotationService = (YinAnnotationService) service;
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error(null, "Error when registering/unregistering YinAnnotationService ", e);
            }
        };

        registerYinAnnotationService();
    }

    @Override
    public Map<SchemaPath, DataSchemaNode> getSchemaNodes() {
        return m_schemaNodes;
    }

    protected Map<DataPath, ActionDefinition> getActionDefinitions() {
        return m_actionDefinitionsByDataPath;
    }

    protected Map<SchemaPath, NotificationDefinition> getNotificationDefinitions() {
        return m_notificationDefinitions;
    }

    private Map<SchemaPath, DataSchemaNode> m_schemaNodes = new LinkedHashMap<>();
    private Map<SchemaPath, DataSchemaNode> m_rootSchemaNodes = new LinkedHashMap<>();
    private Map<DataPath, ActionDefinition> m_actionDefinitionsByDataPath = new LinkedHashMap<>();
    private Map<SchemaPath, NotificationDefinition> m_notificationDefinitions = new LinkedHashMap<>();
    private Map<String, Module> m_modules = new LinkedHashMap<>();
    private String m_repoName = DecoratedSchemaRegistryImpl.class.getName();
    private ConcurrentHashMap<SchemaPath, TreeImpactNode<ImpactNode>> m_schemaPathToTreeImpactNode = new ConcurrentHashMap<SchemaPath, TreeImpactNode<ImpactNode>>();
    private DefaultConcurrentHashMap<SchemaPath, Map<SchemaPath, Expression>> m_whenReferringNodes = new DefaultConcurrentHashMap<>(new HashMap<SchemaPath, Expression>(), true);
    private ConcurrentHashMap<String, WhenReferringNode> m_componentWhenReferringNodes = new ConcurrentHashMap<String, WhenReferringNode>();
    private ConcurrentHashMap<String, MustReferringNode> m_componentMustReferringNodes = new ConcurrentHashMap<String, MustReferringNode>();

    private ConcurrentHashMap<String, WhenReferringNode> m_componentIdWhenReferringNodesForAllSchemaNodes = new ConcurrentHashMap<String, WhenReferringNode>();
    private ConcurrentHashMap<String, MustReferringNode> m_componentIdMustReferringNodesForAllSchemaNodes = new ConcurrentHashMap<String, MustReferringNode>();
    private DefaultConcurrentHashMap<String, HashSet<SchemaPath>> m_componentIdReferredSchemaPathMap = new DefaultConcurrentHashMap<>(new HashSet<SchemaPath>(), true);
    private DefaultConcurrentHashMap<String, HashSet<SchemaPath>> m_componentIdReferringSchemaPathMap = new DefaultConcurrentHashMap<>(new HashSet<SchemaPath>(), true);
    private Map<SchemaPath, String> m_appAugmentedPathToComponentIdMap = new HashMap<>();
    private Map<String, SchemaPath> m_appAugmentedPathToSchemaPathMap = new HashMap<>();
    private Map<TypeDefinition<?>, TypeValidator> m_validators = new HashMap<TypeDefinition<?>, TypeValidator>();
    private Map<DataSchemaNode, SchemaNodeConstraintParser> m_constraintValidators = new HashMap<>();

    private final Set<String> m_relativePath = Collections.synchronizedSet(new HashSet<String>());
    private final Map<String, Map<DataSchemaNode, Expression>> m_relativePaths = new DefaultConcurrentHashMap<>(new HashMap<DataSchemaNode, Expression>(), true);

    private final Set <SchemaPath> m_childBigListSchemaNodes = Sets.newConcurrentHashSet();
    private Map<SchemaPath, HashMap<String, String>> m_xPathWithPrefixToXPathWithModuleNameInPrefixMap = new ConcurrentHashMap<>();
    private Map<SchemaPath, Set<String>> m_attributesWithSameLocalNameDifferentNameSpace = new ConcurrentHashMap<>();
    private final DefaultConcurrentHashMap<String, HashSet<String>> m_componentIdAbsSchemaPaths =
            new DefaultConcurrentHashMap<>(new HashSet<String>(), true);

    private boolean m_isYangLibrarySupportedInHelloMessage;
    private YangLibraryChangeNotificationListener m_yangLibraryChangeNotificationListener;
    private static final String YANG_LIBRARY_CAP_FORMAT = "urn:ietf:params:netconf:capability:yang-library:1.0?revision=2016-04-09&module-set-id=%s";
    private final Map<ModuleIdentifier, Set<QName>> m_supportedFeatures = new HashMap<>();
    private final Map<ModuleIdentifier, Set<QName>> m_supportedDeviations = new HashMap<>();
    private final Map<String, Set<ModuleIdentifier>> m_componentIdModuleIdentifiersMap = new HashMap<>();
    private String m_moduleSetId;
    private Set<QName> m_supportedPlugFeatures = Sets.newConcurrentHashSet();
    private ConcurrentHashMap<QName, Set<QName>> m_supportedPlugDeviations = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, DataSchemaNode> m_mountPointsSchemaPathMap = new ConcurrentHashMap<>();
    private SchemaMountRegistry m_schemaMountRegistry;
    private SchemaPath m_parentMountPath;
    private SchemaRegistry m_parent;
    private SchemaSupportVerifierImpl m_schemaSupportVerifier = new SchemaSupportVerifierImpl();
    private final ConcurrentHashMap<SchemaPath, Set<String>> m_skipValidationHintSchemaPaths = new ConcurrentHashMap<>();
    private final Map<DataSchemaNode, Map<Expression, Module>> m_nodeConstraintsDefinedModules = new DefaultConcurrentHashMap<>(new HashMap<Expression, Module>(), true);
    private boolean m_isYangLibraryIgnored = false;
    private Set<ActionDefinition> m_actionDefinitionsWithListAndLeafRef = new LinkedHashSet<>();

    @SuppressWarnings("unchecked")
    private Map<SchemaPath, Map<QName, DataSchemaNode>> getIndexedCache() {
        RequestScope scope = RequestScope.getCurrentScope();
        Map<SchemaPath, Map<QName, DataSchemaNode>> cache = (Map<SchemaPath, Map<QName, DataSchemaNode>>) scope.getFromCache(CHILD_NODE_INDEX_CACHE);
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
        Map<SchemaPath, Collection<DataSchemaNode>> cache = (Map<SchemaPath, Collection<DataSchemaNode>>) scope.getFromCache(CHILD_NODE_CACHE);
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
        Map<SchemaPath, Collection<DataSchemaNode>> cache = (Map<SchemaPath, Collection<DataSchemaNode>>) scope.getFromCache(CHILD_NODE_CACHE);
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

    /*
     * @deprecated You have to use a constructor that passes the yang models, features and deviations from now onwards.
     */
    @Deprecated
    public DecoratedSchemaRegistryImpl() throws SchemaBuildException {
        this(Collections.<YangTextSchemaSource>emptyList(), true);
    }


    /*
     * @deprecated You have to use a constructor that passes the yang models, features and deviations from now onwards.
     */
    @Deprecated
    public DecoratedSchemaRegistryImpl(boolean isYangLibrarySupportedInHelloMessage) throws SchemaBuildException {
        this(Collections.<YangTextSchemaSource>emptyList(), isYangLibrarySupportedInHelloMessage);
    }

    /*
     * @deprecated You have to use a constructor that passes the yang models, features and deviations from now onwards.
     */
    @Deprecated
    public DecoratedSchemaRegistryImpl(List<YangTextSchemaSource> coreYangModelFiles) throws SchemaBuildException {
        this(coreYangModelFiles, true);
    }

    /*
     * @deprecated You have to use a constructor that passes the yang models, features and deviations from now onwards.
     */
    @Deprecated
    public DecoratedSchemaRegistryImpl(List<YangTextSchemaSource> coreYangModelFiles, boolean isYangLibrarySupportedInHelloMessage) throws SchemaBuildException {
        this(coreYangModelFiles, null, null, isYangLibrarySupportedInHelloMessage, false);
    }

    public DecoratedSchemaRegistryImpl(List<YangTextSchemaSource> coreYangModelFiles, Set<QName> supportedFeatures, Map<QName,
            Set<QName>> supportedDeviations) throws SchemaBuildException {
        this(coreYangModelFiles, supportedFeatures, supportedDeviations, true, false);
    }

    public DecoratedSchemaRegistryImpl(List<YangTextSchemaSource> coreYangModelFiles, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations, boolean isYangLibraryIgnored) throws SchemaBuildException {
        this(coreYangModelFiles, supportedFeatures, supportedDeviations, true, isYangLibraryIgnored);
    }

    public DecoratedSchemaRegistryImpl(List<YangTextSchemaSource> coreYangModelFiles, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations, boolean isYangLibrarySupportedInHelloMessage, boolean isYangLibraryIgnored) throws SchemaBuildException {
        m_isYangLibrarySupportedInHelloMessage = isYangLibrarySupportedInHelloMessage;
        setYangLibraryIgnored(isYangLibraryIgnored);
        if (isYangLibraryIgnored()) {
            buildSchemaContext(coreYangModelFiles, null, null);
        } else {
            buildSchemaContext(coreYangModelFiles, supportedFeatures, supportedDeviations);
        }
        LOGGER.debug("DecoratedSchemaRegistry created from files {}", coreYangModelFiles, new RuntimeException("Just to get stack trace"));
    }

    @Override
    public SchemaContext getSchemaContext() {
        LOGGER.debug("getSchemaContext called");
        return m_schemaContext;
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
    public void buildSchemaContext(final List<YangTextSchemaSource> coreYangModelByteSources, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations) throws SchemaBuildException {
        buildSchemaContext(coreYangModelByteSources, supportedFeatures, supportedDeviations, true);
    }

    @Override
    public void buildSchemaContext(final List<YangTextSchemaSource> coreYangModelByteSources, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported) throws SchemaBuildException {
        LOGGER.debug("buildSchemaContext called with coreYangModelByteSources : {}", coreYangModelByteSources);
        try {
            m_componentModules.clear();
            ConstraintValidatorFactoryImpl.getInstance().clearCache();
            SchemaRegistryUtil.resetCache();
            if (supportedFeatures != null) {
                m_supportedPlugFeatures.addAll(supportedFeatures);
            } else if (isYangLibraryIgnored()) {
                m_supportedPlugFeatures = null;
                m_supportedPlugDeviations = null;
            } else {
                throw new RuntimeException("Supported features have to be provided for the YANG model");
            }
            if (supportedDeviations != null) {
                m_supportedPlugDeviations.putAll(supportedDeviations);
            }
            m_schemaContext = YangParserUtil.parseSchemaSources(m_repoName, coreYangModelByteSources, m_supportedPlugFeatures, m_supportedPlugDeviations);
            m_schemaSupportVerifier.verify(m_schemaContext);
            m_componentModules.put(CORE_COMPONENT_ID, coreYangModelByteSources);
            updateIndexes();
            updateCapabilities(null, supportedFeatures, supportedDeviations, true, isYangLibNotificationSupported);
        } catch (Exception e) {
            throw new LockServiceException("Error while building schema context", e);
        }
    }

    private void addAllSupportedFeatures(String componentId) {
        Set<Module> modules = m_schemaContext.getModules();
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();
        for (Module module : modules) {
            ModuleIdentifier moduleIdentifier = ModuleIdentifierImpl.create(module.getName(), Optional.of(module.getNamespace()), module.getRevision());
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
            ModuleIdentifier moduleIdentifier = ModuleIdentifierImpl.create(module.getName(), Optional.of(module.getNamespace()), module.getRevision());
            Set<QName> deviationQnames = m_supportedDeviations.get(moduleIdentifier);
            if (deviationQnames == null) {
                deviationQnames = new HashSet<>();
                m_supportedDeviations.put(moduleIdentifier, deviationQnames);
                moduleIdentifiers.add(moduleIdentifier);
            }
        }
        addDeviationsToModule(modules);
        m_componentIdModuleIdentifiersMap.put(componentId, moduleIdentifiers);
    }

    private QName getQName(String name, Optional<Revision> revision, URI namespace) {
        QName module;
        if ( revision.isPresent() ){
            module = QName.create(namespace, revision, name);
        } else {
            module = QName.create(namespace, name);
        }
        return module;
    }

    private void addDeviationsToModule(Set<Module> modules) {
        for(Module module: modules){
            for ( Deviation deviation : module.getDeviations()) {
                QNameModule deviatedQNameModule = deviation.getTargetPath().getLastComponent().getModule();
                URI namespace = deviatedQNameModule.getNamespace();
                String deviatedModule =getModuleNameByNamespace(namespace.toString());
                ModuleIdentifier moduleIdentifier = ModuleIdentifierImpl.create(deviatedModule, Optional.of(namespace),
                        deviatedQNameModule.getRevision());
                Set<QName> deviationQNames = m_supportedDeviations.get(moduleIdentifier);

                deviationQNames.add(getQName(module.getName(), module.getRevision(), module.getNamespace()));
            }
        }
    }

    private void updateSupportedFeatures(String componentId, Set<QName> supportedFeatures) {
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();
        for (QName supportedFeature : supportedFeatures) {
            Module module = getModuleByNamespace(supportedFeature.getModule().getNamespace().toString());
            if (module != null) {
                ModuleIdentifier moduleIdentifier = ModuleIdentifierImpl.create(module.getName(), Optional.of(module.getNamespace()),
                        module.getRevision());
                Set<QName> supportedFeaturesPerModule = m_supportedFeatures.get(moduleIdentifier);
                if (supportedFeaturesPerModule == null) {
                    supportedFeaturesPerModule = new HashSet<>();
                    m_supportedFeatures.put(moduleIdentifier, supportedFeaturesPerModule);
                    moduleIdentifiers.add(moduleIdentifier);
                }
                supportedFeaturesPerModule.add(supportedFeature);
            } else {
                LOGGER.error("Unsupported module entry in the supported features");
            }
        }
        m_componentIdModuleIdentifiersMap.put(componentId, moduleIdentifiers);
    }

    private void updateSupportedDeviations(String componentId, Map<QName, Set<QName>> supportedDeviations) {
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();
        for (Map.Entry<QName, Set<QName>> entry : supportedDeviations.entrySet()) {
            for (QName deviation : entry.getValue()) {
                Module module = getModuleByNamespace(entry.getKey().getNamespace().toString());
                if (module != null) {
                    ModuleIdentifier moduleIdentifier = ModuleIdentifierImpl.create(module.getName(), Optional.of(module.getNamespace()),
                            module.getRevision());
                    Set<QName> supportedDeviationsPerModule = m_supportedDeviations.get(moduleIdentifier);
                    if (supportedDeviationsPerModule == null) {
                        supportedDeviationsPerModule = new HashSet<>();
                        m_supportedDeviations.put(moduleIdentifier, supportedDeviationsPerModule);
                        moduleIdentifiers.add(moduleIdentifier);
                    }
                    supportedDeviationsPerModule.add(deviation);
                }
            }
        }
        m_componentIdModuleIdentifiersMap.put(componentId, moduleIdentifiers);
    }

    @Override
    public Collection<DataSchemaNode> getRootDataSchemaNodes() {
        return m_rootSchemaNodes.values();
    }

    @Override
    public Optional<Module> getModule(final String name) {
        Set<Module> modules = m_schemaContext.findModules(name);
        if (!modules.isEmpty()) {
            return Optional.of(modules.iterator().next());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Module> getModule(final String name, final Revision revision) {
        if (revision == null) {
            return m_schemaContext.findModule(name);
        } else {
            return m_schemaContext.findModule(name, revision);
        }
    }

    @Override
    public Collection<RpcDefinition> getRpcDefinitions() {
        return m_allRpcDefinitions.values();
    }

    @Override
    public RpcDefinition getRpcDefinition(final SchemaPath schemaPath) {
        return m_allRpcDefinitions.get(schemaPath);
    }

    @Override
    public synchronized void loadSchemaContext(final String componentId, final List<YangTextSchemaSource> yangModelByteSources, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations) throws SchemaBuildException {
        loadSchemaContext(componentId, yangModelByteSources, supportedFeatures, supportedDeviations, true, false);
    }

    @Override
    public void loadSchemaContext(String componentId, List<YangTextSchemaSource> yangModelFiles, Set<QName> supportedFeatures, Map<QName,
            Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported) throws SchemaBuildException {
        loadSchemaContext(componentId, yangModelFiles, supportedFeatures, supportedDeviations, isYangLibNotificationSupported, false);
    }

    @Override
    public synchronized void loadSchemaContext(final String componentId, final List<YangTextSchemaSource> yangModelByteSources, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported, boolean isYangLibraryIgnored) throws SchemaBuildException {
        LOGGER.debug("loading YANG modules from component {}", componentId);
        if (m_componentModules.get(componentId) != null) {
            throw new LockServiceException("Schema Registry already contains modules of component: " + componentId);
        }
        try {
            if (!yangModelByteSources.isEmpty()) {
                m_componentModules.put(componentId, yangModelByteSources);
                if (supportedFeatures != null) {
                    m_supportedPlugFeatures.addAll(supportedFeatures);
                } else if (isYangLibraryIgnored) {
                    m_supportedPlugFeatures = null;
                    m_supportedPlugDeviations = null;
                } else {
                    throw new RuntimeException("Supported features have to be provided in the plug");
                }
                if (supportedDeviations != null) {
                    m_supportedPlugDeviations.putAll(supportedDeviations);
                }
                rebuildFromSource();
                updateIndexes();
            }
            updateCapabilities(componentId, supportedFeatures, supportedDeviations, true, isYangLibNotificationSupported);
        } catch (Exception e) {
            m_componentModules.remove(componentId);
            throw new LockServiceException("Error while updating schema context", e);
        }
        LOGGER.debug("loading YANG modules from component {} complete", componentId);
        SchemaRegistryUtil.resetCache();
    }

    @Override
    public synchronized void unloadSchemaContext(final String componentId, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations) throws SchemaBuildException {
        unloadSchemaContext(componentId, supportedFeatures, supportedDeviations, true, false);
    }

    @Override
    public synchronized void unloadSchemaContext(final String componentId, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported) throws SchemaBuildException {
        unloadSchemaContext(componentId, supportedFeatures, supportedDeviations, isYangLibNotificationSupported, false);
    }

    @Override
    public synchronized void unloadSchemaContext(final String componentId, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported, boolean isYangLibraryIgnored) throws SchemaBuildException {
        LOGGER.debug("unloadSchemaContext called for componentId : {}", componentId);
        m_validators.clear();
        clearLazilyLoadedCaches();
        m_constraintValidators.clear();
        setYangLibraryIgnored(isYangLibraryIgnored);
        if (m_componentModules.get(componentId) != null) {
            m_componentModules.remove(componentId);
            try {
                if (supportedFeatures != null) {
                    m_supportedPlugFeatures.remove(supportedFeatures);
                } else if (isYangLibraryIgnored()) {
                    m_supportedPlugFeatures = null;
                    m_supportedPlugDeviations = null;
                } else {
                    throw new RuntimeException("Supported features have to be provided in the plug");
                }
                if (supportedDeviations != null) {
                    m_supportedPlugDeviations.remove(supportedDeviations);
                }
                rebuildFromSource();
                updateIndexes();
            } catch (SchemaBuildException e) {
                throw new LockServiceException(e);
            }
            updateCapabilities(componentId, null, null, false, isYangLibNotificationSupported);
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
        Set<ModuleIdentifier> result = new HashSet<>();
        for (Module module: m_schemaContext.getModules()) {
            result.add(ModuleIdentifierImpl.create(module.getName(), Optional.of(module.getNamespace()), module.getRevision()));
            for (Module submodule : module.getSubmodules()) {
                result.add(ModuleIdentifierImpl.create(submodule.getName(), Optional.of(submodule.getNamespace()), submodule.getRevision()));
            }
        }
        return result;
    }

    @Override
    public Set<Module> getAllModules() {
        return m_schemaContext.getModules();
    }

    @Override
    public Map<SourceIdentifier, YangTextSchemaSource> getAllYangTextSchemaSources() throws SchemaBuildException {
        Map<SourceIdentifier, YangTextSchemaSource> yangTextSchemaSources = new HashMap<>();
        try {
            for (List<YangTextSchemaSource> byteSources : m_componentModules.values()) {
                for (YangTextSchemaSource schemaSource : byteSources) {
                    ListenableFuture<ASTSchemaSource> aSTSchemaSource = Futures
                            .immediateFuture(TextToASTTransformer.transformText(schemaSource));
                    SettableSchemaProvider<ASTSchemaSource> schemaProvider = SettableSchemaProvider.createImmediate(
                            aSTSchemaSource.get(), ASTSchemaSource.class);
                    SourceIdentifier id = schemaProvider.getId();
                    if (!yangTextSchemaSources.containsKey(id)) {
                        yangTextSchemaSources.put(id, schemaSource);
                    }
                }
            }
        } catch (Exception e) {
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
            LOGGER.debug("rebuilding schemaContext");
            m_schemaContext = YangParserUtil.parseSchemaSources(DecoratedSchemaRegistryImpl.class.getName(), allYangSources, m_supportedPlugFeatures, m_supportedPlugDeviations);
            m_schemaSupportVerifier.verify(m_schemaContext);
            LOGGER.debug("rebuilding schemaContext done");
        } catch (Exception e) {
            throw new SchemaBuildException("Error while reloading schema context", e);
        }
    }

    @Override
    public DataSchemaNode getDataSchemaNode(final SchemaPath dataNodeSchemaPath) {

        DataSchemaNode schemaNode = m_schemaNodes.get(dataNodeSchemaPath);
        return schemaNode;
    }

    @Override
    public Collection<DataSchemaNode> getChildren(final SchemaPath parentSchemaPath) {
        if (parentSchemaPath.equals(m_parentMountPath)) {
            return getRootDataSchemaNodes();
        }
        DataSchemaNode parentNode = getDataSchemaNode(parentSchemaPath);
        if (parentNode != null && parentNode instanceof DataNodeContainer) {
            Collection<DataSchemaNode> returnList = ((DataNodeContainer) parentNode).getChildNodes();
            return returnList;
        }
        return Collections.emptySet();
    }

    @Override
    public DataSchemaNode getChild(final SchemaPath parentSchemaPath, final QName childQName) {
        if (parentSchemaPath.equals(m_parentMountPath)) {
            return getDataSchemaNode(getDescendantSchemaPath(parentSchemaPath, childQName));
        }
        DataSchemaNode parentNode = getDataSchemaNode(parentSchemaPath);
        if (parentNode != null && parentNode instanceof DataNodeContainer) {
            return ((DataNodeContainer) parentNode).findDataChildByName(childQName).orElse(null);
        }
        return null;
    }

    private void addChildCaseNodes(ChoiceSchemaNode choiceNode, Map<QName, DataSchemaNode> childList) {
        Collection<CaseSchemaNode> cases = choiceNode.getCases().values();
        for (CaseSchemaNode caseNode : cases) {
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

    public DataSchemaNode getDataSchemaNode(final List<QName> qNames) {
        DataSchemaNode currentNode = getSchemaContext();
        for (QName qname : qNames) {
            currentNode = findChild(currentNode, qname);
        }
        return currentNode;
    }

    public DataSchemaNode findChild(final DataSchemaNode currentNode, final QName qname) {
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
                    for (DataSchemaNode caseChild : ChoiceCaseNodeUtil.getImmediateChildrenOfChoice(choiceNode)) {
                        if (caseChild.getQName().equals(qname)) {
                            return caseChild;
                        }
                    }
                }
            }
        }
        throw new RuntimeException("Could not find child " + qname + " in parent " + currentNode.getPath());
    }

    @Override
    public void clear() {
        m_schemaPathToTreeImpactNode.clear();
        m_whenReferringNodes.clear();
        m_supportedFeatures.clear();
        m_supportedDeviations.clear();
        m_componentIdModuleIdentifiersMap.clear();
        m_supportedPlugDeviations.clear();
        m_relativePaths.clear();
        m_expressionsWithoutKeysInList.clear();
        m_childBigListSchemaNodes.clear();
        m_skipValidationHintSchemaPaths.clear();
        m_nodeConstraintsDefinedModules.clear();
    }

    @Override
    public Collection<DataSchemaNode> getNonChoiceChildren(final SchemaPath parentSchemaPath) {

        Collection<DataSchemaNode> effectiveChildSchemaNode = new LinkedHashSet<>();
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

    @Override
    public DataSchemaNode getNonChoiceChild(SchemaPath parentSchemaPath, QName qName) {
        if (parentSchemaPath.equals(m_parentMountPath)) {
            return getDataSchemaNode(getDescendantSchemaPath(parentSchemaPath, qName));
        }
        Map<QName, DataSchemaNode> children = getIndexedChildren(parentSchemaPath);
        return children.get(qName);
    }

    private void addNonChoiceChildren(Collection<DataSchemaNode> effectiveChildSchemaNode, ChoiceSchemaNode choiceSchemaNode) {
        Collection<CaseSchemaNode> caseNodeSet = ((ChoiceSchemaNode) choiceSchemaNode).getCases().values();
        for (CaseSchemaNode caseNode : caseNodeSet) {
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
        return m_modules.keySet().contains(namespaceURI);
    }

    @Override
    public Set<ModuleIdentifier> getAllModuleIdentifiers() {
        Set<ModuleIdentifier> moduleIdentifiers = new HashSet<>();
        for (Module module : m_schemaContext.getModules()) {
            moduleIdentifiers.add(ModuleIdentifierImpl.create(module.getName(), Optional.of(module.getNamespace()), module.getRevision()));
        }
        return moduleIdentifiers;
    }

    @Override
    public List<YangTextSchemaSource> getYangModelByteSourcesOfAPlugin(String componentId) {
        List<YangTextSchemaSource> yangModelByteSources = m_componentModules.get(componentId);
        return yangModelByteSources;
    }


    @Override
    public QName lookupQName(final String namespace, final String localName) {
        Module module = m_modules.get(namespace);
        if (module != null) {
            QName qName = QName.create(module.getQNameModule(), localName);
            return qName;
        }

        return null;
    }

    @Override
    public String getPrefix(final String namespace) {
        Module module = m_modules.get(namespace);
        if (module != null) {
            return module.getPrefix();
        }
        return null;
    }

    @Override
    public String getNamespaceURI(final String prefix) {
        List<String> namespaces = m_prefixes.get(prefix);
        if(namespaces == null || namespaces.isEmpty()){
            return null;
        }
        return namespaces.get(0);
    }

    @Override
    public String getModuleNameByNamespace(final String namespace) {
        Module module = m_modules.get(namespace);
        return module != null ? module.getName() : null;
    }

    @Override
    public String getComponentIdByNamespace(final String namespace) {
        Module module = m_modules.get(namespace);
        if (module != null) {
            String moduleName = module.getName();
            Optional<Revision> optRevision = module.getRevision();
            return optRevision.isPresent() ? moduleName + REVISION + optRevision.get().toString() : moduleName;
        }
        return null;
    }

    @Override
    public String getNamespaceOfModule(final String moduleName) {
        for (Module module : m_modules.values()) {
            if (module.getName().equals(moduleName)) {
                return module.getNamespace().toString();
            }
        }
        return null;
    }


    @Override
    public Iterator<?> getPrefixes(final String namespace) {
        List<String> prefixes = Collections.emptyList();
        String prefix = getPrefix(namespace);
        if (prefix != null) {
            prefixes = new ArrayList<>();
            prefixes.add(prefix);
        }
        return prefixes.iterator();
    }

    @Override
    public Set<SchemaPath> getRootSchemaPaths() {
        return m_rootSchemaNodes.keySet();
    }

    private void updateIndexes() throws SchemaBuildException {
        clearLazilyLoadedCaches();
        indexRpcDefinitions();
        indexDataSchemaNodes();
        indexRootSchemaNodes();
        indexNamespaces();
        indexDeviationsAndAugmentsYangs();
    }

    private void clearLazilyLoadedCaches(){
        m_xPathWithPrefixToXPathWithModuleNameInPrefixMap.clear();
        m_attributesWithSameLocalNameDifferentNameSpace.clear();
        m_shortPaths.clear();
    }

    public void updateCapabilities(String componentId, Set<QName> supportedFeatures,
            Map<QName, Set<QName>> supportedDeviations, boolean isDeploy, boolean isYangLibNotificationSupported) {
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
        LOGGER.debug("Module-set-id {} generated after deploy status {} for component-id {}", newModuleSetId, isDeploy, componentId);
        if (isYangLibraryChangeNotificationNeeded(newModuleSetId)) {
            m_moduleSetId = newModuleSetId;
            if (isYangLibNotificationSupported && m_yangLibraryChangeNotificationListener != null) {
                m_yangLibraryChangeNotificationListener.sendYangLibraryChangeNotification(m_moduleSetId);
            }
        }
        LOGGER.debug("re-indexing schema node maps done");
    }

    private boolean isYangLibraryChangeNotificationNeeded(String newModuleSetId) {
        return !newModuleSetId.equals(m_moduleSetId);
    }

    private void indexNamespaces() {
        m_modules.clear();
        m_prefixes.clear();
        for (Module module : m_schemaContext.getModules()) {
            m_modules.put(module.getNamespace().toString(), module);
        }
        for(Module module: m_modules.values()){
            String prefix = module.getPrefix();
            List<String> namespaces = m_prefixes.get(prefix);
            if(namespaces == null){
                namespaces = new ArrayList<>();
                m_prefixes.put(prefix, namespaces);
            }
            namespaces.add(module.getNamespace().toString());
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
        m_actionDefinitionsByDataPath.clear();
        m_notificationDefinitions.clear();
        DataNodeContainerTraverser.traverse(m_schemaContext, new SchemaNodeIndexBuilder(m_schemaNodes, m_actionDefinitionsByDataPath,
                m_actionDefinitionsWithListAndLeafRef, m_notificationDefinitions, m_schemaContext));
    }

    private void indexRpcDefinitions() {
        m_allRpcDefinitions.clear();
        Map<SchemaPath, RpcDefinition> rpcs = new HashMap<>();
        for (RpcDefinition rpc : m_schemaContext.getOperations()) {
            rpcs.put(rpc.getPath(), rpc);
        }

        m_allRpcDefinitions.putAll(rpcs);
    }

    private void indexDeviationsAndAugmentsYangs() {
        if (m_supportedPlugDeviations != null) {
            for (Map.Entry<QName, Set<QName>> entry : m_supportedPlugDeviations.entrySet()) {
                for (QName deviationModule : entry.getValue()) {
                    Module module = getModuleByNamespace(deviationModule.getNamespace().toString());
                    if (module != null) {
                        for (Deviation deviation : module.getDeviations()) {
                            for (DeviateDefinition deviateDef : deviation.getDeviates()) {
                                if (deviateDef.getDeviateType().equals(DeviateKind.ADD) || deviateDef.getDeviateType().equals(DeviateKind.REPLACE)) {
                                    DataSchemaNode deviatedNode = getDataSchemaNode(deviation.getTargetPath());
                                    Collection<MustDefinition> mustConstraints = deviateDef.getDeviatedMusts();
                                    if (mustConstraints != null) {
                                        for (MustDefinition mustConstraint : mustConstraints) {
                                            String constraint = mustConstraint.getXpath().getOriginalString();
                                            registerNodeConstraintDefinedModule(deviatedNode, constraint, module);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        for(Module module : getAllModules()) {
            for (AugmentationSchemaNode augmentationSchema : module.getAugmentations()) {
                SchemaPath targetPath = augmentationSchema.getTargetPath();
                DataSchemaNode augmentedNode = getDataSchemaNode(targetPath);
                Optional<RevisionAwareXPath> optWhenCondition = augmentationSchema.getWhenCondition();
                if (augmentedNode != null && optWhenCondition != null && optWhenCondition.isPresent()) {
                    String constraint = optWhenCondition.get().getOriginalString();
                    registerNodeConstraintDefinedModule(augmentedNode, constraint, module);
                }
            }
        }

    }

    /*
     * @deprecated You have to use the method that passes the yang models, features and deviations from now onwards.
     */
    @Deprecated
    public static SchemaRegistry buildSchemaRegistry(List<String> coreYangModelFilesPaths) throws SchemaBuildException {
        return buildSchemaRegistry(coreYangModelFilesPaths, null, null, true);
    }

    /*
     * @deprecated You have to use the method that passes the yang models, features and deviations from now onwards.
     */
    @Deprecated
    public static SchemaRegistry buildSchemaRegistry(List<String> coreYangModelFilesPaths, boolean isYangLibrarySupportedInHelloMessage) throws SchemaBuildException {
        return buildSchemaRegistry(coreYangModelFilesPaths, null, null, isYangLibrarySupportedInHelloMessage);
    }

    /*
     * @deprecated You have to use the method that passes the yang models, features and deviations from now onwards.
     */
    @Deprecated
    public static SchemaRegistry buildSchemaRegistry(List<String> coreYangModelFilesPaths,
            Map<String, Set<String>> deviations) throws SchemaBuildException {
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
        return buildSchemaRegistry(coreYangModelFilesPaths, null, supportedDeviations, true);
    }

    /*
     * @deprecated You have to use the method that passes the yang models, features and deviations from now onwards.
     */
    @Deprecated
    public static SchemaRegistry buildSchemaRegistry(List<String> coreYangModelFilesPaths,
            Map<String, Set<String>> deviations, boolean isYangLibrarySupportedInHelloMessage) throws SchemaBuildException {
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
        return buildSchemaRegistry(coreYangModelFilesPaths, null, supportedDeviations, isYangLibrarySupportedInHelloMessage);
    }

    public static SchemaRegistry buildSchemaRegistry(List<String> coreYangModelFilesPaths, Set<String> features,
            Map<String, Set<String>> deviations) throws SchemaBuildException {

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
        return buildSchemaRegistry(coreYangModelFilesPaths, supportedFeatures, supportedDeviations, true);
    }

    public static SchemaRegistry buildSchemaRegistry(List<String> coreYangModelFilesPaths, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations, boolean isYangLibrarySupportedInHelloMessage) throws SchemaBuildException {
        List<YangTextSchemaSource> byteSources = new ArrayList<>();
        for (String coreYangModuleFilePath : coreYangModelFilesPaths) {
            byteSources.add(YangParserUtil.getYangSource(DecoratedSchemaRegistryImpl.class.getResource(coreYangModuleFilePath)));
        }
        return new DecoratedSchemaRegistryImpl(byteSources, supportedFeatures, supportedDeviations, isYangLibrarySupportedInHelloMessage);
    }

    @Override
    public SchemaPath getDescendantSchemaPath(final SchemaPath parentSchemaPath, final QName qname) {
        if (parentSchemaPath != null && parentSchemaPath.equals(m_parentMountPath)) {
            Collection<SchemaPath> rootPaths = getRootSchemaPaths();
            for (SchemaPath rootPath : rootPaths) {
                if (rootPath.getLastComponent().equals(qname)) {
                    return rootPath;
                }
            }
        }
        DataSchemaNode parentSchemaNode = m_schemaNodes.get(parentSchemaPath);
        if (parentSchemaNode instanceof DataNodeContainer) {
            DataNodeContainer containerSchemaNode = (DataNodeContainer) parentSchemaNode;
            DataSchemaNode childDataSchemaNode = containerSchemaNode.findDataChildByName(qname).orElse(null);
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
            for (DataSchemaNode child : choiceSchemaNode.getCases().values()) {
                SchemaPath childPath = getDescendantSchemaPath(child.getPath(), qname);
                if (childPath != null) {
                    return childPath;
                }
            }
        }

        return null;
    }

    @Override
    public Module getModuleByNamespace(final String namespace) {
        return m_modules.get(namespace);
    }

    @Override
    public Optional<Module> findModuleByNamespaceAndRevision(final URI namespace, final Revision revision) {
        return m_schemaContext.findModule(namespace, revision);
    }

    @Override
    public void registerNodesReferredInConstraints(String componentId, ReferringNode referringNode) {
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
         * Here when a modification on leaf validation happens, the node (validation1) on which it is referenced as a constraint
         * must also be validated.
         *
         * So here for the nodeSchemaPath (validation), referencedSchemaPath(validation1) must also be validated
         * when leaf validation undergoes a change.
         */
        ImpactNode impactNode = getImpactNode(referringNode.getReferredSP());
        impactNode.addImpactNodes(referringNode);
        TreeImpactNode<ImpactNode> node = new TreeImpactNode<ImpactNode>(impactNode);
        m_schemaPathToTreeImpactNode.put(referringNode.getReferredSP(), node);
        SchemaPath currentSchemaPath = referringNode.getReferredSP().getParent();
        while (currentSchemaPath.getLastComponent() != null) {
            if (m_schemaPathToTreeImpactNode.get(currentSchemaPath) != null) {
                TreeImpactNode<ImpactNode> parentNode = m_schemaPathToTreeImpactNode.get(currentSchemaPath);
                parentNode.addChild(impactNode);
                break;

            } else {
                ImpactNode parentImpactNode = new ImpactNode(currentSchemaPath);
                TreeImpactNode<ImpactNode> parentNode = new TreeImpactNode<ImpactNode>(parentImpactNode);
                parentNode.addChild(impactNode);
                impactNode = parentImpactNode;
                m_schemaPathToTreeImpactNode.put(currentSchemaPath, parentNode);
                currentSchemaPath = currentSchemaPath.getParent();
            }
        }

        Collection<SchemaPath> schemaPathList = m_componentIdReferredSchemaPathMap.get(componentId);
        schemaPathList.add(referringNode.getReferredSP());
        Collection<SchemaPath> referringSchemaPathList = m_componentIdReferringSchemaPathMap.get(componentId);
        referringSchemaPathList.add(referringNode.getReferringSP());
    }

    @Override
    public void registerWhenReferringNodes(String componentId, SchemaPath referencedSchemaPath, SchemaPath nodeSchemaPath, String accessPath) {
        Map<SchemaPath, Expression> referringNodes = m_whenReferringNodes.get(nodeSchemaPath);
        referringNodes.put(referencedSchemaPath, JXPathUtils.getExpression(accessPath));

        WhenReferringNode whenReferringNode = m_componentWhenReferringNodes.get(componentId);
        if(whenReferringNode == null){
            whenReferringNode = new WhenReferringNode(componentId) ;
            m_componentWhenReferringNodes.put(componentId, whenReferringNode);
        }
        whenReferringNode.registerWhenReferringNodes(referencedSchemaPath, nodeSchemaPath, accessPath);
    }

    @Override
    public Map<SchemaPath, Expression> getWhenReferringNodes(SchemaPath nodeSchemaPath) {
        return m_whenReferringNodes.get(nodeSchemaPath);
    }

    @Override
    public Map<SchemaPath, Expression> getWhenReferringNodes(String componentId, SchemaPath nodeSchemaPath) {
        WhenReferringNode whenReferringNode = m_componentWhenReferringNodes.get(componentId);
        if(whenReferringNode != null){
            return whenReferringNode.getWhenReferringNodes(nodeSchemaPath);
        }
        return Collections.EMPTY_MAP;
    }

    public void registerWhenReferringNodesForAllSchemaNodes(String componentId, SchemaPath referencedSchemaPath, SchemaPath nodeSchemaPath, String accessPath) {
        WhenReferringNode whenReferringNode = m_componentIdWhenReferringNodesForAllSchemaNodes.get(componentId);
        if(whenReferringNode == null){
            whenReferringNode = new WhenReferringNode(componentId) ;
            m_componentIdWhenReferringNodesForAllSchemaNodes.put(componentId, whenReferringNode);
        }
        whenReferringNode.registerWhenReferringNodes(referencedSchemaPath, nodeSchemaPath, accessPath);
    }

    public Map<SchemaPath, Expression> getWhenReferringNodesForAllSchemaNodes(String componentId, SchemaPath nodeSchemaPath) {
        WhenReferringNode whenReferringNode = m_componentIdWhenReferringNodesForAllSchemaNodes.get(componentId);
        if(whenReferringNode != null){
            return whenReferringNode.getWhenReferringNodes(nodeSchemaPath);
        }
        return Collections.EMPTY_MAP;
    }

    public void registerMustReferringNodesForAllSchemaNodes(String componentId, SchemaPath referencedSchemaPath, SchemaPath nodeSchemaPath, String accessPath) {
        MustReferringNode mustReferringNode = m_componentIdMustReferringNodesForAllSchemaNodes.get(componentId);
        if(mustReferringNode == null){
            mustReferringNode = new MustReferringNode(componentId);
            m_componentIdMustReferringNodesForAllSchemaNodes.put(componentId, mustReferringNode);
        }
        mustReferringNode.registerMustReferringNodes(referencedSchemaPath, nodeSchemaPath, accessPath);
    }

    public Map<SchemaPath, Expression> getMustReferringNodesForAllSchemaNodes(String componentId, SchemaPath nodeSchemaPath) {
        MustReferringNode mustReferringNode = m_componentIdMustReferringNodesForAllSchemaNodes.get(componentId);
        if(mustReferringNode != null){
            return mustReferringNode.getMustReferringNodes(nodeSchemaPath);
        }
        return Collections.EMPTY_MAP;
    }

    @Override
    public String getShortPath(SchemaPath path) {
        String sp = m_shortPaths.get(path);
        if(sp != null){
            return m_shortPaths.get(path);
        }
        StringBuilder sb = new StringBuilder();
        String prevModule = null;
        for (QName qName : path.getPathFromRoot()) {
            String currentModule = getModuleByNamespace(qName.getModule().getNamespace().toString()).getName();
            sb.append("/");
            if(!currentModule.equals(prevModule)){
                sb.append(currentModule).append(":");
            }
            sb.append(qName.getLocalName());
            prevModule = currentModule;
        }
        sp = sb.toString();
        m_shortPaths.put(path, sp);
        return sp;
    }

    private ImpactNode getImpactNode(SchemaPath schemaPath) {
        if (m_schemaPathToTreeImpactNode.get(schemaPath) != null) {
            return m_schemaPathToTreeImpactNode.get(schemaPath).getData();
        } else {
            return new ImpactNode(schemaPath);
        }
    }

    @Override
    public ConcurrentHashMap<SchemaPath, TreeImpactNode<ImpactNode>> getImpactNodeMap(){
        return m_schemaPathToTreeImpactNode;
    }

    @Override
    public void deRegisterNodesReferencedInConstraints(final String componentId) {
        Collection<SchemaPath> schemaPathList = m_componentIdReferredSchemaPathMap.get(componentId);
        for (SchemaPath schemaPath : schemaPathList) {
            TreeImpactNode<ImpactNode> node = m_schemaPathToTreeImpactNode.get(schemaPath);
            Map<SchemaPath, Set<ReferringNode>> schemaPathToReferringNodes = node.getData().getImpactNodes().getReferringNodes();
            Collection<SchemaPath> referringSchemaPathList = m_componentIdReferringSchemaPathMap.get(componentId);
            for(SchemaPath referringpath : referringSchemaPathList){
                schemaPathToReferringNodes.remove(referringpath);
            }
        }
        m_componentIdReferredSchemaPathMap.remove(componentId);
        m_componentIdReferringSchemaPathMap.remove(componentId);

        Collection<String> absPaths = m_componentIdAbsSchemaPaths.get(componentId);
        for (String absPath : absPaths) {
            deRegisterAppAllowedAugmentedPath(absPath);
        }

        m_componentIdAbsSchemaPaths.remove(componentId);
        m_componentWhenReferringNodes.remove(componentId);
        m_componentMustReferringNodes.remove(componentId);
        m_componentIdWhenReferringNodesForAllSchemaNodes.remove(componentId);
        m_componentIdMustReferringNodesForAllSchemaNodes.remove(componentId);
    }

    @Override
    public ActionDefinition getActionDefinitionNode(DataPath actionDataPath) {
        return m_actionDefinitionsByDataPath.get(actionDataPath);
    }

    @Override
    public NotificationDefinition getNotificationDefinitionNode(List<QName> paths) {
        SchemaPath notificationPath = SchemaPath.create(paths, true);
        return m_notificationDefinitions.get(notificationPath);
    }

    @Override
    public Collection<SchemaPath> getSchemaPathsForComponent(final String componentId) {
        return m_componentIdReferredSchemaPathMap.get(componentId);
    }

    @Override
    public ReferringNodes getReferringNodesForSchemaPath(final SchemaPath schemaPath) {
        if(m_schemaPathToTreeImpactNode.get(schemaPath)==null){
            return new ReferringNodes(schemaPath);
        }
        return m_schemaPathToTreeImpactNode.get(schemaPath).getData().getImpactNodes();
    }

    @Override
    public ReferringNodes addChildImpactPaths(SchemaPath changeSchemaPath, Set<QName> skipImmediateChildQNames){
        ReferringNodes impactedPaths = new ReferringNodes(changeSchemaPath);
        addImpactNodeForChild(changeSchemaPath, impactedPaths, skipImmediateChildQNames);
        return impactedPaths;
    }

    @Override
    public void addImpactNodeForChild(SchemaPath schemaPath, ReferringNodes impactedPaths, Set<QName> skipImmediateChildQNames) {
        TreeImpactNode<ImpactNode> parentNode = m_schemaPathToTreeImpactNode.get(schemaPath);
        if (parentNode != null) {
            for (TreeImpactNode<ImpactNode> child : parentNode.getChildren()) {
                if(!skipImmediateChildQNames.contains(child.getData().getNodeSchemaPath().getLastComponent())) {
                    impactedPaths.putAll(child.getData().getImpactNodes());
                }
                DataSchemaNode childNode = getDataSchemaNode(child.getData().getNodeSchemaPath());
                if (childNode instanceof ListSchemaNode || childNode instanceof ContainerSchemaNode) {
                    addImpactNodeForChild(child.getData().getNodeSchemaPath(), impactedPaths, Collections.EMPTY_SET);
                }
            }
        }
    }

    @Override
    public Map<SchemaPath, TreeImpactNode<ImpactNode>> getReferringNodes() {
        return new HashMap<>(m_schemaPathToTreeImpactNode);
    }

    @Override
    public void registerAppAllowedAugmentedPath(String moduleId, String path, SchemaPath schemaPath) {
        LOGGER.debug("registering app relative path {}", path);
        if (m_relativePath.contains(path)) {
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
    public void addToChildBigList(SchemaPath schemaPath) {
        LOGGER.debug("Adding to Child Big List schemaPath path: {}", schemaPath);
        if(schemaPath != null) {
            m_childBigListSchemaNodes.add(schemaPath);
        }
    }

    @Override
    public boolean isChildBigList(SchemaPath schemaPath) {
        if(schemaPath == null) {
            return false;
        }
        return m_childBigListSchemaNodes.contains(schemaPath);
    }

    @Override
    public void registerRelativePath(String augmentedPath, String relativePath, DataSchemaNode dataSchemaNode) {
        LOGGER.debug("Registering relative path: {} for augmentedPath: {} and dataSchemaNode:{}", relativePath, augmentedPath, dataSchemaNode);
        Map<DataSchemaNode, Expression> relativePaths = m_relativePaths.get(augmentedPath);
        relativePaths.putIfAbsent(dataSchemaNode, JXPathUtils.getExpression(relativePath));
    }

    @Override
    public Expression getRelativePath(String augmentedPath, DataSchemaNode dataSchemaNode) {
        Expression relativePath = m_relativePaths.get(augmentedPath).get(dataSchemaNode);
        LOGGER.debug("Relative path is {} for augmentedPath:{}, dataSchemaNode:{}", relativePath, augmentedPath, dataSchemaNode);
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


    public boolean isYangLibraryIgnored() {
        return m_isYangLibraryIgnored;
    }

    public void setYangLibraryIgnored(boolean yangLibraryIgnored) {
        m_isYangLibraryIgnored = yangLibraryIgnored;
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
    public Expression getExpressionWithModuleNameInPrefix(SchemaPath schemaPath, Expression expression) {
        return getExpressionWithModuleNameInPrefix(schemaPath, expression.toString());
    }

    @Override
    public Expression getExpressionWithModuleNameInPrefix(SchemaPath schemaPath, String expression) {
        HashMap <String, String> expressionMap = m_xPathWithPrefixToXPathWithModuleNameInPrefixMap.get(schemaPath);
        if(expressionMap != null && expressionMap.containsKey(expression)) {
            return JXPathUtils.getExpression(expressionMap.get(expression));
        }
        return null;
    }

    @Override
    public void registerExpressionWithModuleNameInPrefix(SchemaPath schemaPath,
            String expression, String expressionWithPrefix) {
        HashMap <String, String> expressionMap = m_xPathWithPrefixToXPathWithModuleNameInPrefixMap.get(schemaPath);
        if(expressionMap == null) {
            expressionMap = new HashMap<>();
            m_xPathWithPrefixToXPathWithModuleNameInPrefixMap.put(schemaPath, expressionMap);
        }
        expressionMap.put(expression, expressionWithPrefix);
    }

    @Override
    public void registerExpressionWithModuleNameInPrefix(SchemaPath schemaPath,
            Expression expression, String expressionWithPrefix) {
        registerExpressionWithModuleNameInPrefix(schemaPath, expression.toString(), expressionWithPrefix);
    }

    /*
    In most of the cases a schemaNode won't have any child with same localName and different namespace.
    In this case the list would be empty. "return empty list"

    If this method return's null, it means we hadn't found if there are any child with same localname.
    So we need to find it out and lazy load into schema registry.
     */
    @Override
    public Set<String> getAttributesWithSameLocalNameDifferentNameSpace(SchemaPath schemaPath) {
        if(m_attributesWithSameLocalNameDifferentNameSpace.containsKey(schemaPath)) {
            return m_attributesWithSameLocalNameDifferentNameSpace.get(schemaPath).stream().collect(
                    Collectors.toSet());
        }
        return null;
    }

    @Override
    public void registerAttributesWithSameLocalNameDifferentNameSpace(SchemaPath schemaPath,
            Set<String> attributesWithSameLocalNameDifferentNameSpace) {
        m_attributesWithSameLocalNameDifferentNameSpace.put(schemaPath, attributesWithSameLocalNameDifferentNameSpace.stream().collect(
                Collectors.toSet()));
    }

    @Override
    public String getCapability(ModuleIdentifier moduleId) {
        Set<QName> features = m_supportedFeatures.get(moduleId);
        Set<QName> deviations = m_supportedDeviations.get(moduleId);
        Optional<Revision> optRevision = moduleId.getRevision();
        String revisionDate = (optRevision.isPresent()? optRevision.get().toString() : null);
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
        return (new NetconfCapability(url, moduleName, revisionDate, supportedFeatures, supportedDeviations)).toString();
    }

    private Set<String> getFilteredCapsForHelloMessage() {
        Set<String> filteredCaps = new HashSet<>();
        for (Module module : getAllModules()) {
            if (module.getYangVersion().equals(YangVersion.VERSION_1)) {
                ModuleIdentifier moduleId = ModuleIdentifierImpl.create(module.getName(), Optional.of(module.getNamespace()), module.getRevision());
                filteredCaps.add(getCapability(moduleId));
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
        LOGGER.debug(null, "Module-set-id {} generated for the available yang modules {}", hashString, capsString);
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

    @Override
    public Set<ActionDefinition> retrieveAllActionDefinitions() {
        return new HashSet<>(m_actionDefinitionsByDataPath.values());
    }

    @Override
    public Set<NotificationDefinition> retrieveAllNotificationDefinitions() {
        return new HashSet<>(m_notificationDefinitions.values());
    }

    public Map<ModuleIdentifier, Set<QName>> getSupportedDeviations() {
        return m_supportedDeviations;
    }

    @Override
    public Map<ModuleIdentifier, Set<QName>> getSupportedFeatures() {
        return m_supportedFeatures;
    }

    @Override
    public DataSchemaNode getRPCInputChildNode(RpcDefinition rpcDef, List<QName> qnames) {
        DataNodeContainer node = rpcDef.getInput();
        for (QName qname : qnames) {
            DataSchemaNode innerNode = node.findDataChildByName(qname).orElse(null);
            if (innerNode != null && innerNode instanceof DataNodeContainer) {
                node = (DataNodeContainer) innerNode;
            }
        }
        return (DataSchemaNode) node;
    }

    @Override
    public SchemaPath stripRevisions(SchemaPath schemaPathWithRevisions) {
        SchemaPath schemaPathWithoutRevisions = getFromRequestScopeCache(schemaPathWithRevisions, true);
        if (schemaPathWithoutRevisions == null) {
            SchemaPathBuilder builder = new SchemaPathBuilder();
            boolean revisionMissing = false;
            for (QName qname : schemaPathWithRevisions.getPathFromRoot()) {
                if (qname.getRevision() == null) {
                    revisionMissing = true;
                }
                builder.appendQName(qname.withoutRevision());
            }
            schemaPathWithoutRevisions = builder.build();
            // make sure that we don't cache wrong info, since we are using a bimap
            if (!revisionMissing) {
                putInRequestScopeCache(schemaPathWithoutRevisions, schemaPathWithRevisions);
            }
        }
        return schemaPathWithoutRevisions;
    }

    private void putInRequestScopeCache(SchemaPath pathWithoutRevisions, SchemaPath pathWithRevisions) {
        @SuppressWarnings("unchecked")
        BiMap<SchemaPath, SchemaPath> map = (BiMap<SchemaPath, SchemaPath>) RequestScope.getCurrentScope().getFromCache(PATHMAPPINGCACHE);
        if (map == null) {
            map = HashBiMap.<SchemaPath, SchemaPath>create();
            RequestScope.getCurrentScope().putInCache(PATHMAPPINGCACHE, map);
        }
        map.put(pathWithoutRevisions, pathWithRevisions);
    }

    private SchemaPath getFromRequestScopeCache(SchemaPath path, boolean inputWithRevisions) {
        SchemaPath result = null;
        @SuppressWarnings("unchecked")
        BiMap<SchemaPath, SchemaPath> map = (BiMap<SchemaPath, SchemaPath>) RequestScope.getCurrentScope().getFromCache(PATHMAPPINGCACHE);
        if (map != null) {
            if (inputWithRevisions) {
                map = map.inverse();
            }
            result = map.get(path);
        }
        return result;
    }

    @Override
    public SchemaPath addRevisions(SchemaPath schemaPathWithoutRevisions) {
        SchemaPath schemaPathWithRevisions = getFromRequestScopeCache(schemaPathWithoutRevisions, false);
        if (schemaPathWithRevisions == null) {
            SchemaPathBuilder builder = new SchemaPathBuilder();
            boolean hasRevision = false;
            for (QName qname : schemaPathWithoutRevisions.getPathFromRoot()) {
                if (qname.getRevision().isPresent()) {
                    // incoming qname has a revision,
                    // but we'll override with the currently known revision anyway to be safe
                    hasRevision = true;
                }
                URI namespace = qname.getNamespace();
                Module module = getModuleByNamespace(namespace.toString());
                if (module != null) {
                    QName qnameWithRevision = QName.create(module.getQNameModule(), qname.getLocalName());
                    builder.appendQName(qnameWithRevision);
                } else {
                    throw new RuntimeException("No module found with namespace " + namespace);
                }
            }
            schemaPathWithRevisions = builder.build();
            // make sure that we don't cache wrong info, since we are using a bimap
            if (!hasRevision) {
                putInRequestScopeCache(schemaPathWithoutRevisions, schemaPathWithRevisions);
            }
        }
        return schemaPathWithRevisions;
    }

    public void setSchemaMountRegistry(SchemaMountRegistry schemaMountRegistry) {
        m_schemaMountRegistry = schemaMountRegistry;
    }

    @Override
    public SchemaMountRegistry getMountRegistry() {
        return m_schemaMountRegistry;
    }

    public void setMountPath(SchemaPath mountPath) {
        m_parentMountPath = mountPath;
    }

    public SchemaPath getMountPath() {
        return m_parentMountPath;
    }

    @Override
    public void registerMountPointSchemaPath(String componentId, DataSchemaNode schemaNode) {
        SchemaPath path = schemaNode.getPath();
        LOGGER.debug("registering mount point schema path {}", path);
        if (!m_mountPointsSchemaPathMap.containsKey(componentId)) {
            m_mountPointsSchemaPathMap.put(componentId, schemaNode);
        }
    }

    @Override
    public void unregisterMountPointSchemaPath(String componentId) {
        LOGGER.debug("removing mount point schema path for componentId {}", componentId);
        m_mountPointsSchemaPathMap.remove(componentId);
    }

    @Override
    public Set<QName> retrieveAllMountPointsPath() {
        Set<QName> qnames = new HashSet<>();
        for (DataSchemaNode node : m_mountPointsSchemaPathMap.values()) {
            qnames.add(node.getQName().withoutRevision());
        }
        return qnames;
    }

    @Override
    public Collection<DataSchemaNode> retrieveAllNodesWithMountPointExtension() {
        return m_mountPointsSchemaPathMap.values();
    }

    public DecoratedSchemaRegistryImpl deepClone() throws SchemaBuildException, LockServiceException {
        DecoratedSchemaRegistryImpl clonedSchemaRegistry = null;
        try {
            clonedSchemaRegistry = new DecoratedSchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), m_isYangLibraryIgnored);
        } catch (SchemaBuildException e) {
            throw new LockServiceException(e);
        }

        clonedSchemaRegistry.m_schemaContext = m_schemaContext;
        clonedSchemaRegistry.m_allRpcDefinitions.putAll(m_allRpcDefinitions);
        clonedSchemaRegistry.m_componentModules.putAll(m_componentModules);
        clonedSchemaRegistry.m_schemaNodes.putAll(m_schemaNodes);
        clonedSchemaRegistry.m_rootSchemaNodes.putAll(m_rootSchemaNodes);
        clonedSchemaRegistry.m_actionDefinitionsByDataPath.putAll(m_actionDefinitionsByDataPath);
        clonedSchemaRegistry.m_notificationDefinitions.putAll(m_notificationDefinitions);
        clonedSchemaRegistry.m_modules.putAll(m_modules);
        clonedSchemaRegistry.m_prefixes.putAll(m_prefixes);
        //clonedSchemaRegistry.m_repoName = m_repoName;
        clonedSchemaRegistry.m_schemaPathToTreeImpactNode.putAll(m_schemaPathToTreeImpactNode);
        clonedSchemaRegistry.m_componentIdReferredSchemaPathMap.putAll(m_componentIdReferredSchemaPathMap);
        clonedSchemaRegistry.m_componentWhenReferringNodes.putAll(m_componentWhenReferringNodes);
        clonedSchemaRegistry.m_componentMustReferringNodes.putAll(m_componentMustReferringNodes);
        clonedSchemaRegistry.m_componentIdWhenReferringNodesForAllSchemaNodes.putAll(m_componentIdWhenReferringNodesForAllSchemaNodes);
        clonedSchemaRegistry.m_componentIdMustReferringNodesForAllSchemaNodes.putAll(m_componentIdMustReferringNodesForAllSchemaNodes);
        clonedSchemaRegistry.m_componentIdReferringSchemaPathMap.putAll(m_componentIdReferringSchemaPathMap);
        clonedSchemaRegistry.m_appAugmentedPathToComponentIdMap.putAll(m_appAugmentedPathToComponentIdMap);
        clonedSchemaRegistry.m_appAugmentedPathToSchemaPathMap.putAll(m_appAugmentedPathToSchemaPathMap);
        clonedSchemaRegistry.m_relativePath.addAll(m_relativePath);
        clonedSchemaRegistry.m_relativePaths.putAll(m_relativePaths);
        clonedSchemaRegistry.m_componentIdAbsSchemaPaths.putAll(m_componentIdAbsSchemaPaths);
        clonedSchemaRegistry.m_isYangLibraryIgnored = m_isYangLibraryIgnored;
        clonedSchemaRegistry.m_isYangLibrarySupportedInHelloMessage = m_isYangLibrarySupportedInHelloMessage;
        clonedSchemaRegistry.m_yangLibraryChangeNotificationListener = m_yangLibraryChangeNotificationListener;
        //clonedSchemaRegistry.YANG_LIBRARY_CAP_FORMAT
        if(!clonedSchemaRegistry.isYangLibraryIgnored()) {
            clonedSchemaRegistry.m_supportedPlugDeviations.putAll(m_supportedPlugDeviations);
        }
        clonedSchemaRegistry.m_supportedFeatures.putAll(m_supportedFeatures);
        clonedSchemaRegistry.m_supportedDeviations.putAll(m_supportedDeviations);
        clonedSchemaRegistry.m_componentIdModuleIdentifiersMap.putAll(m_componentIdModuleIdentifiersMap);
        clonedSchemaRegistry.m_moduleSetId = new String(m_moduleSetId);
        clonedSchemaRegistry.m_mountPointsSchemaPathMap.putAll(m_mountPointsSchemaPathMap);
        clonedSchemaRegistry.m_schemaMountRegistry = m_schemaMountRegistry;
        clonedSchemaRegistry.m_parentMountPath = m_parentMountPath;
        clonedSchemaRegistry.m_validators.putAll(m_validators);
        clonedSchemaRegistry.m_whenReferringNodes.putAll(m_whenReferringNodes);
        clonedSchemaRegistry.m_constraintValidators.putAll(m_constraintValidators);
        clonedSchemaRegistry.c_yinAnnotationService = c_yinAnnotationService;

        return clonedSchemaRegistry;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof DecoratedSchemaRegistryImpl) || other == null) {
            return false;
        }
        DecoratedSchemaRegistryImpl otherSchemaRegistry = (DecoratedSchemaRegistryImpl) other;
        if (otherSchemaRegistry.m_schemaContext.equals(this.m_schemaContext) &&
                otherSchemaRegistry.m_allRpcDefinitions.equals(this.m_allRpcDefinitions) &&
                otherSchemaRegistry.m_componentModules.equals(this.m_componentModules) &&
                otherSchemaRegistry.m_schemaNodes.equals(this.m_schemaNodes) &&
                otherSchemaRegistry.m_rootSchemaNodes.equals(this.m_rootSchemaNodes) &&
                otherSchemaRegistry.m_actionDefinitionsByDataPath.equals(this.m_actionDefinitionsByDataPath) &&
                otherSchemaRegistry.m_notificationDefinitions.equals(this.m_notificationDefinitions) &&
                otherSchemaRegistry.m_modules.equals(this.m_modules) &&
                otherSchemaRegistry.m_prefixes.equals(this.m_prefixes) &&
                otherSchemaRegistry.m_schemaPathToTreeImpactNode.equals(this.m_schemaPathToTreeImpactNode) &&
                otherSchemaRegistry.m_componentIdReferredSchemaPathMap.equals(this.m_componentIdReferredSchemaPathMap) &&
                otherSchemaRegistry.m_componentWhenReferringNodes.equals(this.m_componentWhenReferringNodes) &&
                otherSchemaRegistry.m_componentMustReferringNodes.equals(this.m_componentMustReferringNodes) &&
                otherSchemaRegistry.m_componentIdWhenReferringNodesForAllSchemaNodes.equals(this.m_componentIdWhenReferringNodesForAllSchemaNodes) &&
                otherSchemaRegistry.m_componentIdMustReferringNodesForAllSchemaNodes.equals(this.m_componentIdMustReferringNodesForAllSchemaNodes) &&
                otherSchemaRegistry.m_componentIdReferringSchemaPathMap.equals(this.m_componentIdReferringSchemaPathMap) &&
                otherSchemaRegistry.m_appAugmentedPathToComponentIdMap.equals(this.m_appAugmentedPathToComponentIdMap) &&
                otherSchemaRegistry.m_appAugmentedPathToSchemaPathMap.equals(this.m_appAugmentedPathToSchemaPathMap) &&
                otherSchemaRegistry.m_relativePath.equals(this.m_relativePath) &&
                otherSchemaRegistry.m_relativePaths.equals(this.m_relativePaths) &&
                otherSchemaRegistry.m_componentIdAbsSchemaPaths.equals(this.m_componentIdAbsSchemaPaths) &&
                otherSchemaRegistry.m_isYangLibrarySupportedInHelloMessage == this.m_isYangLibrarySupportedInHelloMessage &&
                otherSchemaRegistry.m_isYangLibraryIgnored == this.m_isYangLibraryIgnored &&
                otherSchemaRegistry.m_yangLibraryChangeNotificationListener == this.m_yangLibraryChangeNotificationListener &&
                otherSchemaRegistry.m_supportedFeatures.equals(this.m_supportedFeatures) &&
                otherSchemaRegistry.m_supportedDeviations.equals(m_supportedDeviations) &&
                otherSchemaRegistry.m_componentIdModuleIdentifiersMap.equals(m_componentIdModuleIdentifiersMap) &&
                otherSchemaRegistry.m_moduleSetId.equals(this.m_moduleSetId) &&
                otherSchemaRegistry.m_supportedPlugDeviations.equals(m_supportedPlugDeviations) &&
                otherSchemaRegistry.m_mountPointsSchemaPathMap.equals(m_mountPointsSchemaPathMap) &&
                otherSchemaRegistry.m_schemaMountRegistry == this.m_schemaMountRegistry &&
                otherSchemaRegistry.m_parentMountPath == this.m_parentMountPath &&
                otherSchemaRegistry.m_whenReferringNodes.equals(this.m_whenReferringNodes) &&
                compareYinAnnotationServices(otherSchemaRegistry.c_yinAnnotationService, this.c_yinAnnotationService) &&
                otherSchemaRegistry.m_supportedFeatures.equals(this.m_supportedFeatures)) {
            return true;
        }

        return false;
    }

    private boolean compareYinAnnotationServices(YinAnnotationService thisService, YinAnnotationService thatService) {
        if(thisService == null && thatService == null){
            return true;
        }else if(thisService == null && thatService != null){
            return false;
        }else if(thisService != null && thatService == null){
            return false;
        }else{
            return thisService.equals(thatService);
        }
    }


    @Override
    //fix for  sonar violation
    public int hashCode() {
        return System.identityHashCode(this);
    }

    public void setParentRegistry(SchemaRegistry parent) {
        m_parent = parent;
    }

    public SchemaRegistry getParentRegistry() {
        return m_parent;
    }

    @Override
    public void putValidator(TypeDefinition<?> type, TypeValidator typeValidator) {
        m_validators.put(type, typeValidator);
    }

    @Override
    public TypeValidator getValidator(TypeDefinition<?> type) {
        return m_validators.get(type);
    }

    @Override
    public SchemaNodeConstraintParser getSchemaNodeConstraintParser(DataSchemaNode dataSchemaNode) {
        return m_constraintValidators.get(dataSchemaNode);
    }

    @Override
    public void putSchemaNodeConstraintParser(DataSchemaNode dataSchemaNode,
            SchemaNodeConstraintParser schemaNodeConstraintParser) {
        m_constraintValidators.put(dataSchemaNode, schemaNodeConstraintParser);
    }

    @Override
    public void setName(String registryName){

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void addToSkipValidationPaths(SchemaPath schemaPath, String constraintXpath) {
        LOGGER.debug("Adding to skip validation schemaPath path: {} - with constraint xpath :{}", schemaPath, constraintXpath);
        if(schemaPath != null) {
            Set<String> xpaths = m_skipValidationHintSchemaPaths.get(schemaPath);
            if(xpaths == null) {
                xpaths = new HashSet<String>();
            }
            xpaths.add(constraintXpath);
            m_skipValidationHintSchemaPaths.put(schemaPath, xpaths);
        }
    }

    @Override
    public boolean isSkipValidationBySchemaPathWithConstraintXpath(SchemaPath schemaPath, String constraintXpath) {
        if(schemaPath == null || m_skipValidationHintSchemaPaths.get(schemaPath) == null) {
            return false;
        }
        return m_skipValidationHintSchemaPaths.get(schemaPath).contains(constraintXpath);
    }

    @Override
    public boolean isSkipValidationPath(SchemaPath schemaPath) {
        if(schemaPath == null) {
            return false;
        }
        return m_skipValidationHintSchemaPaths.containsKey(schemaPath);
    }

    @Override
    public void printReferringNodes() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<SchemaPath, TreeImpactNode<ImpactNode>> schemaPathTreeImpactNodeEntry : m_schemaPathToTreeImpactNode.entrySet()) {
            SchemaPath referrednode = schemaPathTreeImpactNodeEntry.getKey();
            sb.append("\nNode ").append(getSimplePath(
                    getDataSchemaNode(referrednode))).append(" -impacts-> {\n");
            ImpactNode data = schemaPathTreeImpactNodeEntry.getValue().getData();
            Map<SchemaPath, Set<ReferringNode>> referringNodes = data.getImpactNodes().getReferringNodes();
            for (Set<ReferringNode> referringNodeSet : referringNodes.values()) {
                for (ReferringNode referringNode : referringNodeSet) {
                    sb.append("  ");
                    sb.append(getSimplePath(getDataSchemaNode(referringNode.getReferringSP())));
                    if(referringNode.getValidationHint() != null){
                        sb.append(" With Hint: ").append(referringNode.getValidationHint());
                    }
                    sb.append("\n");
                }
            }
            sb.append("}");

        }

        LOGGER.debug("ReferringNodes {}", sb.toString());
    }

    public static String getSimplePath(DataSchemaNode childSchemaNode) {
        StringBuilder sb = new StringBuilder();
        for (QName qName : childSchemaNode.getPath().getPathFromRoot()) {
            sb.append("/").append(qName.getLocalName());
        }
        return sb.toString();
    }

    @Override
    public void addExpressionsWithoutKeysInList(String fullExpression, LocationPath expressionWithListAtLowerLevel) {
        Set<LocationPath> expressionsAtLowerLevel = m_expressionsWithoutKeysInList.get(fullExpression);
        if ( expressionsAtLowerLevel == null) {
            expressionsAtLowerLevel = new HashSet<>();
            m_expressionsWithoutKeysInList.put(fullExpression, expressionsAtLowerLevel);
        }
        expressionsAtLowerLevel.add(expressionWithListAtLowerLevel);
    }

    @Override
    public Set<LocationPath> getExpressionsWithoutKeysInList(String fullExpression) {
        Set<LocationPath> locationPaths = m_expressionsWithoutKeysInList.get(fullExpression);
        if(locationPaths == null) {
            locationPaths = Collections.emptySet();
        }
        return locationPaths;
    }

    @Override
    public void registerNodeConstraintDefinedModule(DataSchemaNode schemaNode, String constraint, Module module) {
        Map<Expression, Module> constraintModules = m_nodeConstraintsDefinedModules.get(schemaNode);
        constraintModules.putIfAbsent(JXPathUtils.getExpression(constraint), module);
    }

    @Override
    public Map<Expression, Module> getNodeConstraintDefinedModule(DataSchemaNode schemaNode) {
        return m_nodeConstraintsDefinedModules.get(schemaNode);
    }

    @Override
    public YinAnnotationService getYinAnnotationService() {
        return c_yinAnnotationService;
    }

    @VisibleForTesting
    public void setYinAnnotationService(YinAnnotationService testService){
        c_yinAnnotationService = testService;
    }

    private static void registerYinAnnotationService(){
        Bundle bundle = FrameworkUtil.getBundle(DecoratedSchemaRegistryImpl.class);
        if(bundle != null) {
            BundleContext bundleContext = bundle.getBundleContext();
            if( bundleContext != null) {
                try {
                    bundleContext.addServiceListener(c_serviceListener, "(" + Constants.OBJECTCLASS + "="
                            + YinAnnotationService.class.getName() + ")");
                } catch (InvalidSyntaxException e) {
                    throw new RuntimeException("Error while registering YinAnnotation Service ");
                }
                ServiceReference<YinAnnotationService> annotationServiceRef = bundleContext.getServiceReference(YinAnnotationService.class);
                if (annotationServiceRef != null) {
                    c_yinAnnotationService = bundleContext.getService(annotationServiceRef);
                }
            } else {
                LOGGER.debug("Error while registering YinAnnotation Service , bundleContext is null");
            }
        } else{
            LOGGER.debug("Error while registering YinAnnotation Service , bundle is null");
        }
    }

    @Override
    public Set<ActionDefinition> getActionDefinitionNodesWithListAndLeafRef() {
        return m_actionDefinitionsWithListAndLeafRef;
    }
}
