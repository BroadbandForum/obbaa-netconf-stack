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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;


public class YangModelFactory {

    private static final String[] IETF_YANG_PATH = {"/yangs/ietf/ietf-restconf.yang",
            "/yangs/ietf/ietf-inet-types.yang",
                                                     /*"/yangs/ietf/ietf-interfaces.yang",*/
            "/yangs/ietf/ietf-yang-types.yang"};


    private List<URL> m_ietfYangModels = new ArrayList<URL>();

    private Map<String, Module> m_yangModuleMap = Collections.synchronizedMap(new HashMap<String, Module>());

    private static YangModelFactory c_instance = new YangModelFactory();


    private YangModelFactory() {
        for (String fileName : IETF_YANG_PATH) {
            URL resource = YangModelFactory.class.getResource(fileName);
            m_ietfYangModels.add(resource);
        }
    }

    public static YangModelFactory getInstance() {
        return c_instance;
    }

    public Module loadModule(String yangFilePath) {
        if (m_yangModuleMap.containsKey(yangFilePath)) {
            return m_yangModuleMap.get(yangFilePath);
        } else {
            List<YangTextSchemaSource> yangSources = new ArrayList<>();
            yangSources.add(YangParserUtil.getYangSource(yangFilePath));
            /**
             * FIXME: I think we should let the caller supply the IETF yang files, not take it on our own
             */
            for (String path : IETF_YANG_PATH) {
                yangSources.add(YangParserUtil.getYangSource(YangModelFactory.class.getResource(path)));
            }
            SchemaContext context = YangParserUtil.parseSchemaSources(YangModelFactory.class.getName(), yangSources);
            Module module = YangParserUtil.getParsedModule(context, yangFilePath);
            m_yangModuleMap.put(yangFilePath, module);
            return module;
        }
    }

    public SchemaContext loadSchemaContext(List<String> yangFileNames) {
        List<YangTextSchemaSource> byteSourceList = new ArrayList<>();
        for (String importFileName : yangFileNames) {
            URL yangURL = YangModelFactory.class.getResource(importFileName);
            YangTextSchemaSource byteSource = YangParserUtil.getYangSource(yangURL);
            byteSourceList.add(byteSource);
        }
        return YangParserUtil.parseSchemaSources(YangModelFactory.class.getName(), byteSourceList);
    }


}
