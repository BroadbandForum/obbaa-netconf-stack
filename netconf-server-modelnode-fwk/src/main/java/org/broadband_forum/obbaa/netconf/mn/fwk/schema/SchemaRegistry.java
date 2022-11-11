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
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.NamespaceContext;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.broadband_forum.obbaa.netconf.api.util.DataPath;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.SchemaNodeConstraintParser;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.typevalidators.TypeValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.dom.YinAnnotationService;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.notification.listener.YangLibraryChangeNotificationListener;
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
 * A registry responsible for maintaining SchemaContext of entire application.
 * Provides methods to
 * 1. update schema context
 * 2. Retrieve root level nodes
 * 3. retrieve schema nodes with a Schema Path.
 *
 * Created by keshava on 11/18/15.
 */
public interface SchemaRegistry extends NamespaceContext, WrappedService<SchemaRegistry> {
    String CORE_COMPONENT_ID = "core-component";
    public final static String GLOBAL_SCHEMA_REGISTRY = "GLOBAL-GLABAL";
    public final static String CHILD_NODE_INDEX_CACHE = "ChildNodeIndexCache";
    public final static String CHILD_NODE_CACHE = "ChilNodeCache";
    public final static String SCHEMAPATH_SCHEMANODE_CACHE = "SchemaPathSchemaNodeCache";

    /**
     * (Re) Builds schema context in the registry if any with the schema context built using the YANG yangModulefiles supplied.
     * @param coreYangModelFiles - list of core YANG Model files (along with dependent modules) which is to be loaded into schema registry.
     *                             use YangParserUtil.getYangSource to construct a YangTextSchemaSource object
     * @throws SchemaBuildException
     * @deprecated Use the method that passes the supported features and supported deviations from now on.
     */
    @Deprecated
    void buildSchemaContext(List<YangTextSchemaSource> coreYangModelFiles) throws SchemaBuildException;

    void buildSchemaContext(List<YangTextSchemaSource> coreYangModelFiles, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations) throws SchemaBuildException;

    void buildSchemaContext(List<YangTextSchemaSource> coreYangModelFiles, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported) throws SchemaBuildException;

    /**
     * Return the root level DataSchemaNode s from the current schema context.
     * @return
     */
    Collection<DataSchemaNode> getRootDataSchemaNodes();

    /**
     * Get  a Module by name and revision.
     * @param name
     * @param revision
     * @return
     */
    Optional<Module> getModule(String name, Revision revision);
    
    /**
     * Get  a Module by name
     * @param name
     * @return
     */
    Optional<Module> getModule(String name);

    /**
     * Return the schema paths of all root nodes.
     * @return
     */
    Set<SchemaPath> getRootSchemaPaths();

    /**
     * Return the entire collection of RPCs currently available in Schema Registyr.
     * @return
     */
    Collection<RpcDefinition> getRpcDefinitions() ;

    /**
     * Get RPC definition by Schema Path.
     * @param schemaPath
     * @return
     */
    RpcDefinition getRpcDefinition(SchemaPath schemaPath);

    /**
     * Updates the existing schema context with content new modules.
     * Use YangParserUtil.getYangSource to construct a YangTextSchemaSource object
     * @param componentId
     * @param yangModelFiles
     */
    void loadSchemaContext(String componentId, List<YangTextSchemaSource> yangModelFiles, Set<QName> supportedFeatures, Map<QName,Set<QName>> supportedDeviations) throws SchemaBuildException;

    void loadSchemaContext(String componentId, List<YangTextSchemaSource> yangModelFiles, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported) throws SchemaBuildException;

    void loadSchemaContext(String componentId, List<YangTextSchemaSource> yangModelFiles, Set<QName> supportedFeatures, Map<QName, Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported, boolean isYangLibraryIgnored) throws SchemaBuildException;

    void unloadSchemaContext(String componentId, Set<QName> supportedFeatures, Map<QName,Set<QName>> supportedDeviations) throws SchemaBuildException;

    void unloadSchemaContext(String componentId, Set<QName> supportedFeatures, Map<QName,Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported) throws SchemaBuildException;

    void unloadSchemaContext(String componentId, Set<QName> supportedFeatures, Map<QName,Set<QName>> supportedDeviations, boolean isYangLibNotificationSupported, boolean isYangLibraryIgnored) throws SchemaBuildException;

    Map<SchemaPath, DataSchemaNode> getSchemaNodes();
    
    ConcurrentHashMap<SchemaPath, TreeImpactNode<ImpactNode>> getImpactNodeMap();
    
    /**
     * Get Data schema node by Schema Path.
     * @param dataNodeSchemaPath
     * @return
     */
    DataSchemaNode getDataSchemaNode(SchemaPath dataNodeSchemaPath);
    
    /**
     * Get ActionSchema node (which is Data schema node) by Path.
     * @param action node data path
     * @return
     */
    ActionDefinition getActionDefinitionNode(DataPath actionDataPath);
    
    /**
     * Get Notification node (which is Data schema node) by Path.
     * @param dataNodeSchemaPath
     * @return
     */
    NotificationDefinition getNotificationDefinitionNode(List<QName> path);

    /**
     * Get the children of a Schema Path if any.
     *
     * @param parentSchemaPath
     * @return
     * 
     * Note: For efficiency, if the child QName is available use getChild(SchemaPath, childQName)
     */
    Collection<DataSchemaNode> getChildren(SchemaPath parentSchemaPath);
    
    /**
     * Get the child DataSchemaNode for the given schemaPath and childQname if the child exists. 
     * @param pathSchemaPath
     * @param childQName
     * @return
     */
    DataSchemaNode getChild(SchemaPath pathSchemaPath, QName childQName);
    /**
     * Provides the Non Choice children of Schema Path by replacing
     *  logical (choice case) schema node with actual descendant data schema node
     *
     * @param parentSchemaPath
     * @return
     */
    Collection<DataSchemaNode> getNonChoiceChildren(SchemaPath parentSchemaPath);

    DataSchemaNode getNonChoiceChild(SchemaPath parentSchemaPath,QName qName);

    SchemaPath getDescendantSchemaPath(SchemaPath parentSchemaPath, QName qname);

    DataSchemaNode getNonChoiceParent(SchemaPath schemaPath);

    boolean isKnownNamespace(String namespaceURI);

    Set<ModuleIdentifier> getAllModuleIdentifiers();

    /**
     * Get the QName with the deployed Module revision.
     * @param namespace
     * @param localName
     * @return
     */
    QName lookupQName(String namespace, String localName);

    @Deprecated
    SchemaContext getSchemaContext();
    
    String getModuleNameByNamespace(String namespace);
    
    String getComponentIdByNamespace(String namespace);

    String getNamespaceOfModule(String moduleName);

    Module getModuleByNamespace(String namespace);

    Optional<Module> findModuleByNamespaceAndRevision(URI namespace, Revision revision);
    
    /**
     * Helps to build a map of impacted nodes for each constraint/ref nodes present in yang. Here a ref node can be either a leaf-ref or an
     * instance-identifier 
     * 
     * Example: leaf test when ../test-key='true' && ../test-value='1'
     * 
     * constraintSchemaPaths: test-key and test-value referredSP: test
     * 
     * It also builds a map of constraintSchemaPaths corresponding to a particular component
     *
     * @param referringNode
     */
    void registerNodesReferredInConstraints(String componentId, ReferringNode referringNode);
    
    void deRegisterNodesReferencedInConstraints(String componentId);
    
    Collection<SchemaPath> getSchemaPathsForComponent(String componentId);

    ReferringNodes getReferringNodesForSchemaPath(SchemaPath schemaPath);

    DataSchemaNode getDataSchemaNode(List<QName> paths);
    
    Map<SourceIdentifier, YangTextSchemaSource> getAllYangTextSchemaSources() throws SchemaBuildException;
    
    Set<ModuleIdentifier> getAllModuleAndSubmoduleIdentifiers();
    
    /**
     * Register an app specific augmented abs path from Root 
     */
    void registerAppAllowedAugmentedPath(String componentId, String path, SchemaPath schemaPath);

    void deRegisterAppAllowedAugmentedPath(String path);

    String getMatchingPath(String path);

    void addToChildBigList(SchemaPath schemaPath);

    boolean isChildBigList(SchemaPath schemaPath);

    /**
     * for a absolute path augmented with app specific path, register the equivalent local path
     * eg : 
     *    augmentedPath --> /device-manager/device-holder/device/device-specific-data/interfaces/interface/name
     *    relativePath --> ../interface/name
     */
    void registerRelativePath(String augmentedPath, String relativePath, DataSchemaNode schemaNode);
    
    /**
     * given a augment path, get the relative path
     */
    Expression getRelativePath(String augmentPath, DataSchemaNode dataSchemaNode);    
    
    boolean isYangLibrarySupportedInHelloMessage();
    
	public Set<String> getModuleCapabilities(boolean forHello);

	public Expression getExpressionWithModuleNameInPrefix(SchemaPath schemaPath, Expression expression);

	public Expression getExpressionWithModuleNameInPrefix(SchemaPath schemaPath, String expression);

	public void registerExpressionWithModuleNameInPrefix(SchemaPath schemaPath, String expression, String expressionWithPrefix);

	public void registerExpressionWithModuleNameInPrefix(SchemaPath schemaPath, Expression expression, String expressionWithPrefix);

	public Set<String> getAttributesWithSameLocalNameDifferentNameSpace(SchemaPath schemaPath);

	public void registerAttributesWithSameLocalNameDifferentNameSpace(SchemaPath schemaPath, Set<String> attributesWithSameLocalNameDifferentNameSpace);

	public String getCapability(ModuleIdentifier moduleId);
	
	public String getModuleSetId();
	
	public Set<Module> getAllModules();
	
    public void registerYangLibraryChangeNotificationListener(YangLibraryChangeNotificationListener listener);
    
    public void unregisterYangLibraryChangeNotificationListener();
    
    public Map<SchemaPath, String> retrieveAppAugmentedPathToComponent();

    public List<YangTextSchemaSource> getYangModelByteSourcesOfAPlugin(String componentId);

    Set<ActionDefinition> retrieveAllActionDefinitions();
    
    Set<NotificationDefinition> retrieveAllNotificationDefinitions();
    
    Map<ModuleIdentifier, Set<QName>> getSupportedDeviations();
    
    Map<ModuleIdentifier, Set<QName>> getSupportedFeatures();
    
	public DataSchemaNode getRPCInputChildNode(RpcDefinition rpcDef, List<QName> qnames);

    Map<QName, DataSchemaNode> getIndexedChildren(SchemaPath parentSchemaPath);
    
    SchemaMountRegistry getMountRegistry();
    
    void setMountPath(SchemaPath mountPath);
    
    SchemaPath getMountPath();

    SchemaPath stripRevisions(SchemaPath schemaPath);
    
    SchemaPath addRevisions(SchemaPath schemaPath);
    
    void registerMountPointSchemaPath(String componentId, DataSchemaNode schemaNode);

    void unregisterMountPointSchemaPath(String componentId);

    public Set<QName> retrieveAllMountPointsPath();

	public Collection<DataSchemaNode> retrieveAllNodesWithMountPointExtension();
	
	public void setParentRegistry(SchemaRegistry parent);
	
	public SchemaRegistry getParentRegistry();
	
	public void putValidator(TypeDefinition<?> type,TypeValidator TypeValidator);
    
     public TypeValidator getValidator(TypeDefinition<?> type);
     
     public SchemaNodeConstraintParser getSchemaNodeConstraintParser(DataSchemaNode dataSchemaNode);
     
     public void putSchemaNodeConstraintParser(DataSchemaNode dataSchemaNode, SchemaNodeConstraintParser schemaNodeConstraintParser);
     public void setName(String schemaRegistryName);
     public String getName();

    ReferringNodes addChildImpactPaths(SchemaPath schemaPath, Set<QName> skipImmediateChildQNames);

    void registerWhenReferringNodes(String componentId, SchemaPath referencedSchemaPath, SchemaPath nodeSchemaPath, String accessPath);

    Map<SchemaPath, Expression> getWhenReferringNodes(SchemaPath nodeSchemaPath);
    
    Map<SchemaPath, Expression> getWhenReferringNodes(String componentId, SchemaPath nodeSchemaPath);

    void registerWhenReferringNodesForAllSchemaNodes(String componentId, SchemaPath referencedSchemaPath, SchemaPath nodeSchemaPath, String accessPath);

    Map<SchemaPath, Expression> getWhenReferringNodesForAllSchemaNodes(String componentId, SchemaPath nodeSchemaPath);

    void registerMustReferringNodesForAllSchemaNodes(String componentId, SchemaPath referencedSchemaPath, SchemaPath nodeSchemaPath, String accessPath);

    Map<SchemaPath, Expression> getMustReferringNodesForAllSchemaNodes(String componentId, SchemaPath nodeSchemaPath);

    String getShortPath(SchemaPath path);

    void addToSkipValidationPaths(SchemaPath schemaPath, String constraintXpath);

    boolean isSkipValidationBySchemaPathWithConstraintXpath(SchemaPath schemaPath, String constraintXpath);

    boolean isSkipValidationPath(SchemaPath schemaPath);
    
    void addExpressionsWithoutKeysInList(String fullExpression, LocationPath expressionWithListAtLowerLevel);
    
    Set<LocationPath> getExpressionsWithoutKeysInList(String fullExpression);

    void printReferringNodes();
    
    public void addImpactNodeForChild(SchemaPath schemaPath, ReferringNodes impactedPaths, Set<QName> skipImmediateChildQNames);

    Map<SchemaPath, TreeImpactNode<ImpactNode>> getReferringNodes();
    
    public void registerNodeConstraintDefinedModule(DataSchemaNode schemaNode, String constraint, Module module);
    
    public Map<Expression, Module> getNodeConstraintDefinedModule(DataSchemaNode schemaNode);

    YinAnnotationService getYinAnnotationService();

    DataSchemaNode findChild(final DataSchemaNode currentNode, final QName qname);

    void clear();


    Set<ActionDefinition> getActionDefinitionNodesWithListAndLeafRef();
}