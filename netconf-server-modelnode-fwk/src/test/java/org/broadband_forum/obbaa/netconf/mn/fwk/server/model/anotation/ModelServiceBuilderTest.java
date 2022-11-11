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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc.AnnotatedRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc.InvocationType;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc.RpcArgsInfo;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.BundleContextAwareModelService;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcRequestHandler;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.NcSubsystem;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.Rpc;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.RpcArg;

/**
 * Created by kbhatk on 7/26/16.
 */
public class ModelServiceBuilderTest {

    public static final String YANG_FILE1 = "/modelservicebuildertest/test-yang1.yang";
    public static final String YANG_FILE2 = "/modelservicebuildertest/test-yang2@2015-02-27.yang";
    public static final String DEFAULT_XML_FILE = "/modelservicebuildertest/default.xml";
    public static final String TEST_YANG_MODULE = "test-yang-module";
    public static final String YANG_MODULE_AND_REVISION = TEST_YANG_MODULE + "@2015-02-27";
    public static final String TEST_YANG_MODULE_NS = "urn:" + TEST_YANG_MODULE;
    @Mock
    private BundleContext m_bundleContext;
    @Mock
    private Document m_defaultXmlDoc;
    @Mock
    private SchemaRegistry m_schemaRegistry;

    private final TestSubsystem1 m_subsystem1 = new TestSubsystem1();
    private final SubsystemWithoutRpcs m_subsystem2 = new SubsystemWithoutRpcs();
    private List<YangTextSchemaSource> m_yangByteSources;
    private List<String> m_yangFilePaths;


    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        when(m_schemaRegistry.getNamespaceOfModule(TEST_YANG_MODULE)).thenReturn(TEST_YANG_MODULE_NS);
        when(m_schemaRegistry.isKnownNamespace(TEST_YANG_MODULE_NS)).thenReturn(true);

        Bundle bundle = mock(Bundle.class);
        when(m_bundleContext.getBundle()).thenReturn(bundle);
        m_defaultXmlDoc = DocumentUtils.loadXmlDocument(getClass().getResource(DEFAULT_XML_FILE).openStream());
        when(bundle.getEntry(DEFAULT_XML_FILE)).thenReturn(getClass().getResource(DEFAULT_XML_FILE));
        when(bundle.getEntry(YANG_FILE1)).thenReturn(getClass().getResource(YANG_FILE1));
        when(bundle.getEntry(YANG_FILE2)).thenReturn(getClass().getResource(YANG_FILE2));

        m_yangByteSources = new ArrayList<>();
        m_yangByteSources.add(YangParserUtil.getYangSource(getClass().getResource(YANG_FILE1)));
        m_yangByteSources.add(YangParserUtil.getYangSource(getClass().getResource(YANG_FILE2)));
        m_yangFilePaths = new ArrayList<>();
        m_yangFilePaths.add(YANG_FILE1);
        m_yangFilePaths.add(YANG_FILE2);
    }

    @Test
    public void testBuildModelServices_NcSubsystemAnnotation() throws ModelServiceBuilderException, NetconfMessageBuilderException {
        BundleContextAwareModelService modelService;
        modelService = ModelServiceBuilder.buildModelService(m_subsystem1, m_bundleContext, m_schemaRegistry);
        assertEquals(m_bundleContext, modelService.getBundleContext());
        assertEquals(DocumentUtils.documentToString(m_defaultXmlDoc),DocumentUtils.documentToString(modelService.getDefaultSubtreeRootNodes().get(0)));
        assertEquals(m_yangFilePaths, modelService.getYangFilePaths());
        assertEquals(2, modelService.getYangModuleByteSources().size());
        assertEquals("test-yang1", modelService.getYangModuleByteSources().get(0).getIdentifier().getName());
        //There seems to be bug in org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource.identifierFromFilename() which
        // ignores revision
        assertEquals("test-yang2", modelService.getYangModuleByteSources().get(1).getIdentifier().getName());
        assertEquals(TEST_YANG_MODULE, modelService.getModuleName());
        assertEquals("2015-02-27", modelService.getModuleRevision());

        modelService = ModelServiceBuilder.buildModelService(m_subsystem2, m_bundleContext, m_schemaRegistry);
        assertEquals(m_bundleContext, modelService.getBundleContext());
        //there can be model services without any default xml
        assertEquals(null, modelService.getDefaultSubtreeRootNodes());
        assertEquals(Collections.<String>emptyList(), modelService.getYangFilePaths());
        assertEquals(0, modelService.getYangModuleByteSources().size());
        assertEquals(TEST_YANG_MODULE, modelService.getModuleName());
        assertEquals("2015-02-27", modelService.getModuleRevision());
    }

    @Test
    public void testBuildModelServices_RpcAnnotation() throws ModelServiceBuilderException{
        BundleContextAwareModelService modelService;
        modelService = ModelServiceBuilder.buildModelService(m_subsystem1, m_bundleContext, m_schemaRegistry);

        assertEquals(2, modelService.getRpcRequestHandlers().size());

        //iterate and evaluate
        for(RpcRequestHandler handler: modelService.getRpcRequestHandlers()){
            assertTrue(handler instanceof AnnotatedRpcRequestHandler);
            AnnotatedRpcRequestHandler annotatedRpcRequestHandler = (AnnotatedRpcRequestHandler) handler;
            assertEquals(TEST_YANG_MODULE_NS, annotatedRpcRequestHandler.getRpcQName().getNamespace());
            assertNotNull(annotatedRpcRequestHandler.getRpcMethod());
            assertEquals(m_subsystem1, annotatedRpcRequestHandler.getBean());

            if(annotatedRpcRequestHandler.getRpcQName().getName().equals("play")){
                testPlayRpcWithArgs(annotatedRpcRequestHandler);

            }else if(annotatedRpcRequestHandler.getRpcQName().getName().equals("pause")){
                testPauseRpcWithNoArgs(annotatedRpcRequestHandler);

            }else {
                fail("Found a unexpected RPC handler with name: "+annotatedRpcRequestHandler.getRpcQName());
            }
        }

    }

    @Test
    public void testBuildModelServices_With_SubsystemWithOnlyRpc() throws ModelServiceBuilderException{
        BundleContextAwareModelService modelService;
        SubsystemWithOnlyRpcs bean = new SubsystemWithOnlyRpcs();
        modelService = ModelServiceBuilder.buildModelService(bean, m_bundleContext, m_schemaRegistry);

        assertEquals(2, modelService.getRpcRequestHandlers().size());

        //iterate and evaluate
        for(RpcRequestHandler handler: modelService.getRpcRequestHandlers()){
            assertTrue(handler instanceof AnnotatedRpcRequestHandler);
            AnnotatedRpcRequestHandler annotatedRpcRequestHandler = (AnnotatedRpcRequestHandler) handler;
            assertEquals(TEST_YANG_MODULE_NS, annotatedRpcRequestHandler.getRpcQName().getNamespace());
            assertNotNull(annotatedRpcRequestHandler.getRpcMethod());
            assertEquals(bean, annotatedRpcRequestHandler.getBean());

            if(annotatedRpcRequestHandler.getRpcQName().getName().equals("play")){
                testPlayRpcWithArgs(annotatedRpcRequestHandler);

            }else if(annotatedRpcRequestHandler.getRpcQName().getName().equals("pause")){
                testPauseRpcWithNoArgs(annotatedRpcRequestHandler);

            }else {
                fail("Found a unexpected RPC handler with name: "+annotatedRpcRequestHandler.getRpcQName());
            }
        }

    }

    @Test
    public void testBuildModelServices_With_SubsystemWithInvalidRpc() throws ModelServiceBuilderException {
        try {
            ModelServiceBuilder.buildModelService(new SubsystemWithInvalidRpc(), m_bundleContext, m_schemaRegistry);
            fail("expected an exception here");
        } catch (ModelServiceBuilderException e) {
            assertTrue(e.getMessage().contains("Cannot determine the namespace for RPC on method"));
            assertTrue(e.getMessage().contains("playRpc"));
        }
    }

    @Test
    public void testBuildModelServices_With_SubsystemWithInvalidRpc2() throws ModelServiceBuilderException {
        try {
            ModelServiceBuilder.buildModelService(new SubsystemWithInvalidRpc2(), m_bundleContext, m_schemaRegistry);
            fail("expected an exception here");
        } catch (ModelServiceBuilderException e) {
            assertTrue(e.getMessage().contains("RpcArg annotation not found for all method parameters of the method"));
            assertTrue(e.getMessage().contains("playRpc"));
        }
    }

    @Test
    public void testBuildModelServices_With_SubsystemWithOnlyRpcsButInvalidNS() throws ModelServiceBuilderException {
        try {
            ModelServiceBuilder.buildModelService(new SubsystemWithOnlyRpcsButInvalidNS(), m_bundleContext, m_schemaRegistry);
            fail("expected an exception here");
        } catch (ModelServiceBuilderException e) {
            assertTrue(e.getMessage().contains("Cannot determine the namespace for RPC on method "));
            assertTrue(e.getMessage().contains("playRpc"));
            assertTrue(e.getMessage().contains("YANG modules may not be loaded"));
            assertTrue(e.getMessage().contains("unknown-namespace"));
        }
    }

    @Test
    public void testBuildModelServices_With_SubsystemWithOnlyRpcsButInvalidNS2() throws ModelServiceBuilderException {
        try {
            ModelServiceBuilder.buildModelService(new SubsystemWithOnlyRpcsButInvalidNS2(), m_bundleContext, m_schemaRegistry);
            fail("expected an exception here");
        } catch (ModelServiceBuilderException e) {
            assertTrue(e.getMessage().contains("Cannot determine the namespace for RPC on method "));
            assertTrue(e.getMessage().contains("playRpc"));
            assertTrue(e.getMessage().contains("YANG modules may not be loaded"));
            assertTrue(e.getMessage().contains("unknown-namespace"));
        }
    }


    private void testPauseRpcWithNoArgs(AnnotatedRpcRequestHandler annotatedRpcRequestHandler) {
        //this should not have Rpc Args info
        RpcArgsInfo info = annotatedRpcRequestHandler.getRpcArgsInfo();
        assertEquals(0, info.getRpcArguments().size());
    }

    private void testPlayRpcWithArgs(AnnotatedRpcRequestHandler annotatedRpcRequestHandler) {
        assertNotNull(annotatedRpcRequestHandler.getRpcMethod());

        //this should have Rpc Args info
        RpcArgsInfo info = annotatedRpcRequestHandler.getRpcArgsInfo();
        assertEquals(InvocationType.ANNOTATED_ARGS, annotatedRpcRequestHandler.getInvocationType());
        assertEquals(String.class, info.getRpcArgument(0).getType());
        assertEquals("playlist", info.getRpcArgument(0).getArgName());

        assertEquals(InvocationType.ANNOTATED_ARGS, annotatedRpcRequestHandler.getInvocationType());
        assertEquals(Integer.class, info.getRpcArgument(1).getType());
        assertEquals("song-number", info.getRpcArgument(1).getArgName());
    }

    @NcSubsystem(yangFilePaths = {YANG_FILE1, YANG_FILE2},
            defaultXMLFilePath = DEFAULT_XML_FILE, yangModule = YANG_MODULE_AND_REVISION)
    class TestSubsystem1 {
        @Rpc("play")
        public NetConfResponse playRpc(@RpcArg("playlist") String playlist, @RpcArg("song-number") Integer songNumber){
            return null;
        }

        @Rpc(value = "pause", namespace = TEST_YANG_MODULE_NS)
        public NetConfResponse pauseRpc(){
            return null;
        }

        public NetConfResponse methodWithoutRpcAnnotation(){
            return null;
        }
    }

    @NcSubsystem(yangModule = YANG_MODULE_AND_REVISION)
    class SubsystemWithoutRpcs {

    }

    //A subsystem with only RPCs, YANGs are already loaded
    class SubsystemWithOnlyRpcs {
        @Rpc(value = "play", namespace = TEST_YANG_MODULE_NS)
        public NetConfResponse playRpc(@RpcArg("playlist") String playlist, @RpcArg("song-number") Integer songNumber){
            return null;
        }

        @Rpc(value = "pause", namespace = TEST_YANG_MODULE_NS)
        public NetConfResponse pauseRpc(){
            return null;
        }

        public NetConfResponse methodWithoutRpcAnnotation(){
            return null;
        }

    }

    //A subsystem with only RPCs YANGs not loaded
    class SubsystemWithOnlyRpcsButInvalidNS {
        @Rpc(value = "play", namespace = "unknown-namespace")
        public NetConfResponse playRpc(@RpcArg("playlist") String playlist, @RpcArg("song-number") Integer songNumber){
            return null;
        }
    }


    //Namespace is defined but not found in schema registry
    @NcSubsystem(yangModule = "unknown-namespace@2015-02-27")
    private class SubsystemWithOnlyRpcsButInvalidNS2 {
        @Rpc(value = "play")
        public NetConfResponse playRpc(@RpcArg("playlist") String playlist, @RpcArg("song-number") Integer songNumber){
            return null;
        }
    }

    //Namespace is not defined for RPCs
    class SubsystemWithInvalidRpc {
        @Rpc("play")
        //no way to get the namespace of the RPC
        public NetConfResponse playRpc(@RpcArg("playlist") String playlist, @RpcArg("song-number") Integer songNumber){
            return null;
        }
    }

    class SubsystemWithInvalidRpc2 {

        @Rpc(value = "play", namespace = TEST_YANG_MODULE_NS)
        //stack wont know how to map the songNumber
        public NetConfResponse playRpc(@RpcArg("playlist") String playlist, Integer songNumber){
            return null;
        }

    }

}
