package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystem;
import org.broadband_forum.obbaa.netconf.server.rpc.MultiRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcRequestHandler;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Element;

/**
 * A ModelService to be used in the OSGi Environment.
 * Created by keshava on 2/8/16.
 */
public class BundleContextAwareModelService extends ModelService {
    BundleContext m_bundleContext;
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(BundleContextAwareModelService.class, LogAppNames.NETCONF_STACK);

    public BundleContextAwareModelService(){
        super();
    }
    public BundleContextAwareModelService(String moduleName, String moduleRevision, String defaultXmlPath,
                                          Map<SchemaPath, SubSystem> subSystems, Set<RpcRequestHandler> rpcRequestHandlers,Set<MultiRpcRequestHandler> multiRpcRequestHandlers,
                                          List<String> yangFilePaths) {
        super(moduleName, moduleRevision, defaultXmlPath, subSystems, rpcRequestHandlers,multiRpcRequestHandlers, yangFilePaths);
    }

    public BundleContextAwareModelService(String moduleName, String moduleRevision, String defaultXmlPath,
                                          SubSystem defaultSubsystem, Set<RpcRequestHandler> rpcRequestHandlers,Set<MultiRpcRequestHandler> multiRpcRequestHandlers,
                                          List<String> yangFilePaths) {
        super(moduleName, moduleRevision, defaultXmlPath,defaultSubsystem, rpcRequestHandlers,multiRpcRequestHandlers, yangFilePaths);
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
        for(String file : getYangFilePaths()){
            YangTextSchemaSource byteSource = null;
            if(m_bundleContext.getBundle().getEntry(file) != null) {
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
        if(m_defaultXmlPath != null) {
            InputStream inputStream = null;
            try {
                inputStream = m_bundleContext.getBundle().getEntry(m_defaultXmlPath).openStream();
                return Collections.singletonList(DocumentUtils.loadXmlDocument(inputStream).getDocumentElement());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }finally {
                try {
                    //can we change the language level and use try(resource) syntax?
                    if(inputStream!=null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    LOGGER.error("Error while closing stream",e);
                }
            }

        }
        return null;
    }
}
