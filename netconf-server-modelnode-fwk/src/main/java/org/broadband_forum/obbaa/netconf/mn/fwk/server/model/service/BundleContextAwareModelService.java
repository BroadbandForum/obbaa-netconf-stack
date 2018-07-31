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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.server.rpc.MultiRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcRequestHandler;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

/**
 * A ModelService to be used in the OSGi Environment.
 * Created by keshava on 2/8/16.
 */
public class BundleContextAwareModelService extends ModelService {
    BundleContext m_bundleContext;
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(BundleContextAwareModelService.class,
            "netconf-server-datastore", "DEBUG", "GLOBAL");

    public BundleContextAwareModelService() {
        super();
    }

    public BundleContextAwareModelService(String moduleName, String moduleRevision, String defaultXmlPath,
                                          Map<SchemaPath, SubSystem> subSystems, Set<RpcRequestHandler>
                                                  rpcRequestHandlers, Set<MultiRpcRequestHandler>
                                                  multiRpcRequestHandlers,
                                          List<String> yangFilePaths) {
        super(moduleName, moduleRevision, defaultXmlPath, subSystems, rpcRequestHandlers, multiRpcRequestHandlers,
                yangFilePaths);
    }

    public BundleContextAwareModelService(String moduleName, String moduleRevision, String defaultXmlPath,
                                          SubSystem defaultSubsystem, Set<RpcRequestHandler> rpcRequestHandlers,
                                          Set<MultiRpcRequestHandler> multiRpcRequestHandlers,
                                          List<String> yangFilePaths) {
        super(moduleName, moduleRevision, defaultXmlPath, defaultSubsystem, rpcRequestHandlers,
                multiRpcRequestHandlers, yangFilePaths);
    }

    public BundleContext getBundleContext() {
        return m_bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        m_bundleContext = bundleContext;
    }

    @Override
    public List<YangTextSchemaSource> getYangModuleByteSources() {

        List<YangTextSchemaSource> byteSourceList = new ArrayList<>();
        for (String file : getYangFilePaths()) {
            YangTextSchemaSource byteSource = null;
            if (m_bundleContext.getBundle().getEntry(file) != null) {
                byteSource = YangParserUtil.getYangSource(m_bundleContext.getBundle().getEntry(file));
            } else {
                byteSource = YangParserUtil.getYangSource(file);
            }
            byteSourceList.add(byteSource);
        }
        return byteSourceList;
    }

    @Override
    public List<Element> getDefaultSubtreeRootNodes() {
        if (m_defaultXmlPath != null) {
            InputStream inputStream = null;
            try {
                inputStream = m_bundleContext.getBundle().getEntry(m_defaultXmlPath).openStream();
                return Collections.singletonList(DocumentUtils.loadXmlDocument(inputStream).getDocumentElement());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    //can we change the language level and use try(resource) syntax?
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    LOGGER.error("Error while closing stream", e);
                }
            }

        }
        return null;
    }
}
