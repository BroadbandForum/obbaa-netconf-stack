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

import static org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder.REVISION_DELIMITER;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDataStoreManager;
import org.broadband_forum.obbaa.netconf.server.rpc.MultiRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcRequestHandler;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;

/**
 * ModelService is wrapper class that binds yang module with ModelNode, default configuration xml
 * and RpcRequestHandler implementation together.
 */
@Deprecated
public class ModelService {

    private SubSystem m_defaultSubsystem;
    private List<String> m_yangFilePaths = new ArrayList<>();
    protected String m_defaultXmlPath;
    private ModelNodeDataStoreManager m_modelNodeDSM;
    private List<Class> m_entityClasses;
    private Map<SchemaPath, SubSystem> m_subSystems = new HashMap();
    private Set<RpcRequestHandler> m_rpcRequestHandlers = new HashSet<RpcRequestHandler>();
    private Set<MultiRpcRequestHandler> m_multiRpcRequestHandlers = new HashSet<MultiRpcRequestHandler>();
    private String m_moduleRevision;
    private String m_moduleName;
    private List<QName> rootElemQNames;
    private Set<org.opendaylight.yangtools.yang.common.QName> m_supportedFeatures = new HashSet<>();
    private Set<String> m_supportedFeatureNames = new HashSet<>();
    private Map<org.opendaylight.yangtools.yang.common.QName, Set<org.opendaylight.yangtools.yang.common.QName>>
            m_supportedDeviations = new HashMap<>();
    private Map<String, Set<String>> m_supportedDeviationNames = new HashMap<String, Set<String>>();
    private Map<String, SchemaPath> m_appAugmentedPaths = new HashMap<String, SchemaPath>();
    private EntityDataStoreManager m_entityDSM;

    public ModelService() {
        super();
    }

    public ModelService(String moduleName, String moduleRevision, String defaultXmlPath, Map<SchemaPath, SubSystem>
            subSystems,
                        Set<RpcRequestHandler> rpcRequestHandlers, Set<MultiRpcRequestHandler>
                                multiRpcRequestHandlers, List<String> yangFilePaths) {
        this();
        m_moduleName = moduleName;
        m_moduleRevision = moduleRevision;
        m_subSystems = subSystems;
        m_defaultXmlPath = defaultXmlPath;
        m_rpcRequestHandlers = rpcRequestHandlers;
        m_multiRpcRequestHandlers = multiRpcRequestHandlers;
        m_yangFilePaths = yangFilePaths;
    }

    public ModelService(String moduleName, String moduleRevision, String defaultXmlPath,
                        SubSystem defaultSubsystem, Set<RpcRequestHandler> rpcRequestHandlers,
                        Set<MultiRpcRequestHandler> multiRpcRequestHandlers, List<String> yangFilePaths) {
        this(moduleName, moduleRevision, defaultXmlPath,
                Collections.<SchemaPath, SubSystem>emptyMap(), rpcRequestHandlers, multiRpcRequestHandlers,
                yangFilePaths);
        m_defaultSubsystem = defaultSubsystem;
    }

    public SubSystem getDefaultSubsystem() {
        return m_defaultSubsystem;
    }

    public void setDefaultSubsystem(SubSystem defaultSubsystem) {
        m_defaultSubsystem = defaultSubsystem;
    }

    public String getDefaultXmlPath() {
        return m_defaultXmlPath;
    }

    /**
     * {@link org.apache.karaf.features.internal.region.Subsystem} to be used if no subsystem is specified explicitly
     * via
     * getSubSystems() for the given node's schemapath or its parent's schemapath.
     *
     * @param defaultXmlPath
     */
    public void setDefaultXmlPath(String defaultXmlPath) {
        this.m_defaultXmlPath = defaultXmlPath;
    }

    public String getName() {
        StringBuilder sb = new StringBuilder();
        sb.append(m_moduleName).append(REVISION_DELIMITER).append(m_moduleRevision);
        return sb.toString();
    }

    public Map<SchemaPath, SubSystem> getSubSystems() {
        return m_subSystems;
    }

    public void setSubSystems(Map<SchemaPath, SubSystem> subSystems) {
        this.m_subSystems = subSystems;
    }

    public ModelNodeDataStoreManager getModelNodeDSM() {
        return m_modelNodeDSM;
    }

    public void setModelNodeDSM(ModelNodeDataStoreManager modelNodeDSM) {
        m_modelNodeDSM = modelNodeDSM;
    }

    public List<Class> getEntityClasses() {
        return m_entityClasses;
    }

    public void setEntityClasses(List<Class> entityClasses) {
        this.m_entityClasses = entityClasses;
    }

    public EntityDataStoreManager getEntityDSM() {
        return m_entityDSM;
    }

    public void setEntityDSM(EntityDataStoreManager entityDSM) {
        m_entityDSM = entityDSM;
    }

    public Set<RpcRequestHandler> getRpcRequestHandlers() {
        return m_rpcRequestHandlers;
    }

    public void setRpcRequestHandlers(Set<RpcRequestHandler> rpcRequestHandlers) {
        this.m_rpcRequestHandlers = rpcRequestHandlers;
    }

    public Set<MultiRpcRequestHandler> getMultiRpcRequestHandlers() {
        return m_multiRpcRequestHandlers;
    }

    public void setMultiRpcRequestHandlers(Set<MultiRpcRequestHandler> multiRpcRequestHandlers) {
        this.m_multiRpcRequestHandlers = multiRpcRequestHandlers;
    }

    public List<QName> getRootElemQName() {
        return rootElemQNames;
    }

    public void setRootElemQName(List<QName> rootElemQName) {
        this.rootElemQNames = rootElemQName;
    }

    public List<Element> getDefaultSubtreeRootNodes() {
        List<Element> configElements = new ArrayList<>();
        if (m_defaultXmlPath != null) {
            InputStream inputStream = this.getClass().getResourceAsStream(m_defaultXmlPath);
            configElements.add(DocumentUtils.loadXmlDocument(inputStream).getDocumentElement());
        }
        if (rootElemQNames != null && rootElemQNames.size() > 0) {
            Document doc = DocumentUtils.createDocument();
            Iterator<QName> qNameIterator = rootElemQNames.iterator();
            while (qNameIterator.hasNext()) {
                QName qName = qNameIterator.next();
                Element rootElem = doc.createElementNS(qName.getNamespaceURI(), qName.getLocalPart());
                configElements.add(rootElem);
            }
        }
        return configElements;
    }

    public List<String> getYangFilePaths() {
        return m_yangFilePaths;
    }

    public void setYangFilePaths(List<String> yangFilePaths) {
        m_yangFilePaths = yangFilePaths;
    }

    public String getModuleRevision() {
        return m_moduleRevision;
    }

    public void setModuleRevision(String moduleRevision) {
        m_moduleRevision = moduleRevision;
    }

    public String getModuleName() {
        return m_moduleName;
    }

    public void setModuleName(String moduleName) {
        m_moduleName = moduleName;
    }

    public Set<org.opendaylight.yangtools.yang.common.QName> getSupportedFeatures() {
        return m_supportedFeatures;
    }

    public void setSupportedFeatures(Set<org.opendaylight.yangtools.yang.common.QName> supportedFeatures) {
        this.m_supportedFeatures = supportedFeatures;
        m_supportedFeatureNames = new HashSet<>();
        for (org.opendaylight.yangtools.yang.common.QName qname : supportedFeatures) {
            m_supportedFeatureNames.add(qname.toString());
        }
    }

    public Set<String> getSupportedFeatureNames() {
        return m_supportedFeatureNames;
    }

    public void setSupportedFeatureNames(Set<String> supportedFeatureNames) {
        m_supportedFeatureNames = supportedFeatureNames;
        m_supportedFeatures = new HashSet<>();
        for (String name : supportedFeatureNames) {
            m_supportedFeatures.add(org.opendaylight.yangtools.yang.common.QName.create(name));
        }
    }

    public Map<org.opendaylight.yangtools.yang.common.QName, Set<org.opendaylight.yangtools.yang.common.QName>>
    getSupportedDeviations() {
        return m_supportedDeviations;

    }

    public void setSupportedDeviations(Map<org.opendaylight.yangtools.yang.common.QName, Set<org.opendaylight
            .yangtools.yang.common.QName>> supportedDeviations) {
        m_supportedDeviations = supportedDeviations;
        m_supportedDeviationNames = new HashMap<>();
        for (Map.Entry<org.opendaylight.yangtools.yang.common.QName, Set<org.opendaylight.yangtools.yang.common
                .QName>> entry : m_supportedDeviations.entrySet()) {
            Set<String> deviations = new HashSet<>();
            String module = entry.getKey().toString();
            for (org.opendaylight.yangtools.yang.common.QName deviation : entry.getValue()) {
                deviations.add(deviation.toString());
            }
            m_supportedDeviationNames.put(module, deviations);
        }
    }

    public Map<String, Set<String>> getSupportedDeviationNames() {
        return m_supportedDeviationNames;
    }

    public void setSupportedDeviationNames(Map<String, Set<String>> supportedDeviationNames) {
        m_supportedDeviationNames = supportedDeviationNames;
        m_supportedDeviations = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : m_supportedDeviationNames.entrySet()) {
            Set<org.opendaylight.yangtools.yang.common.QName> deviations = new HashSet<>();
            org.opendaylight.yangtools.yang.common.QName module = org.opendaylight.yangtools.yang.common.QName.create
                    (entry.getKey());
            for (String deviation : entry.getValue()) {
                deviations.add(org.opendaylight.yangtools.yang.common.QName.create(deviation));
            }
            m_supportedDeviations.put(module, deviations);
        }
    }

    public List<YangTextSchemaSource> getYangModuleByteSources() {
        List<YangTextSchemaSource> byteSourceList = new ArrayList<>();
        for (String file : getYangFilePaths()) {
            YangTextSchemaSource byteSource = YangParserUtil.getYangSource(this.getClass().getResource(file));
            byteSourceList.add(byteSource);
        }
        return byteSourceList;
    }

    @Override
    public String toString() {
        return getName();
    }

    public void setAppAugmentedPaths(Map<String, SchemaPath> appAugmentedPaths) {
        m_appAugmentedPaths = appAugmentedPaths;
    }

    public Map<String, SchemaPath> getAppAugmentedPaths() {
        return new HashMap<String, SchemaPath>(m_appAugmentedPaths);
    }
}
