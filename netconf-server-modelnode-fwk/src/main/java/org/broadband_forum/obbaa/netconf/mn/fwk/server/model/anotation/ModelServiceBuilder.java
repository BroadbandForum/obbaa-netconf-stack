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

import org.broadband_forum.obbaa.netconf.api.messages.RpcName;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc.AnnotatedRpcRequestHandler;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc.InvocationType;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc.RpcArgsInfo;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.anotation.rpc.RpcArgumentInfo;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service.BundleContextAwareModelService;
import org.broadband_forum.obbaa.netconf.server.rpc.RpcRequestHandler;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.NcSubsystem;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.Rpc;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.RpcArg;
import org.osgi.framework.BundleContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds ModelService based on the NCY annotations.
 * Created by kbhatk on 7/26/16.
 */
public class ModelServiceBuilder {
    public static final Pattern MODULE_NAME_AND_REVISION_PATTERN = Pattern.compile("(.+)@([0-9-]+)");


    public static BundleContextAwareModelService buildModelService(Object bean, BundleContext bundleContext, SchemaRegistry schemaRegistry) throws ModelServiceBuilderException {
        BundleContextAwareModelService modelService = new BundleContextAwareModelService();
        analyseNCSubsystemAnnotation(bean, bundleContext, modelService);
        analyseRPCAnnotation(bean, modelService, schemaRegistry);
        return modelService ;
    }

    private static void analyseRPCAnnotation(Object bean, BundleContextAwareModelService modelService,
                                             SchemaRegistry schemaRegistry) throws ModelServiceBuilderException {
        Set<RpcRequestHandler> rpcRequestHandlers = new HashSet<>();
        for(Method method : bean.getClass().getDeclaredMethods()){
            Rpc annotation = method.getAnnotation(Rpc.class);
            if(annotation != null){
                RpcName rpcName = getRpcName(method, schemaRegistry);
                AnnotatedRpcRequestHandler rpcRequestHandler = new AnnotatedRpcRequestHandler(rpcName);
                rpcRequestHandler.setRpcMethod(method);
                rpcRequestHandler.setBean(bean);
                RpcArgsInfo argsInfo = new RpcArgsInfo();
                rpcRequestHandler.setRpcArgsInfo(argsInfo);

                List<RpcArgumentInfo> argumentsInfo = new ArrayList<>();
                argsInfo.setRpcArgsInfo(argumentsInfo);
                int parameterIndex = 0;
                Annotation allParameterAnnotations[][] = method.getParameterAnnotations();
                for(Class parameterType : method.getParameterTypes()){

                    Annotation parameterAnnotations [] = allParameterAnnotations[parameterIndex];
                    RpcArg rpcArgAnnotation = getRpcArgAnnotation(parameterAnnotations);
                    if(rpcArgAnnotation != null) {
                        String argNamespace = rpcArgAnnotation.namespace().trim();
                        if("".equals(argNamespace)){
                            argNamespace = rpcName.getNamespace();
                        }
                        RpcArgumentInfo argInfo = new RpcArgumentInfo(parameterType, rpcArgAnnotation.value(), argNamespace);
                        argumentsInfo.add(argInfo);
                        rpcRequestHandler.setInvocationType(InvocationType.ANNOTATED_ARGS);
                    }else {
                        if(parameterIndex > 0){
                            throw new ModelServiceBuilderException(String.format(
                                    "RpcArg annotation not found for all method parameters of the method: %s", method.getName()));
                        }
                        rpcRequestHandler.setInvocationType(InvocationType.NC_REQ_RES);
                        break;
                    }
                    parameterIndex++;
                }
                rpcRequestHandlers.add(rpcRequestHandler);
            }
        }
        modelService.setRpcRequestHandlers(rpcRequestHandlers);
    }

    private static RpcArg getRpcArgAnnotation(Annotation[] parameterAnnotations) {
        for(Annotation annotation : parameterAnnotations){
            if(annotation instanceof RpcArg){
                return (RpcArg) annotation;
            }
        }
        return null;
    }

    private static RpcName getRpcName(Method method, SchemaRegistry schemaRegistry) throws ModelServiceBuilderException {
        RpcName rpcName = null;
        Rpc rpcAnnotation = method.getAnnotation(Rpc.class);
        String namespace = rpcAnnotation.namespace();
        if(namespace.trim().isEmpty()){
            //check the NcSubsystem annotation
            NcSubsystem subsystemAnnotation = method.getDeclaringClass().getAnnotation(NcSubsystem.class);
            if(subsystemAnnotation == null){
                throw new ModelServiceBuilderException(String.format("Cannot determine the namespace for RPC on method %s",
                        method.toGenericString()));
            }
            Pair<String, String> moduleNameAndRevisionPair = getModuleNameAndRevision(subsystemAnnotation.yangModule());
            namespace = schemaRegistry.getNamespaceOfModule(moduleNameAndRevisionPair.getFirst());
            if(namespace == null){
                throw new ModelServiceBuilderException(String.format(
                        "Cannot determine the namespace for RPC on method %s, YANG modules may not be loaded for namespace %s", method
                                .toGenericString(), moduleNameAndRevisionPair.getFirst()));
            }
        }
        if(!schemaRegistry.isKnownNamespace(namespace)){
            throw new ModelServiceBuilderException(String.format(
                    "Cannot determine the namespace for RPC on method %s, YANG modules may not be loaded for namespace %s", method
                            .toGenericString(), namespace));
        }
        rpcName = new RpcName(namespace, rpcAnnotation.value());
        return rpcName;
    }

    private static void analyseNCSubsystemAnnotation(Object bean, BundleContext bundleContext,
                                                                               BundleContextAwareModelService modelService) throws ModelServiceBuilderException {
        modelService.setBundleContext(bundleContext);
        NcSubsystem annotation = bean.getClass().getAnnotation(NcSubsystem.class);
        if(annotation != null){
            String defaultXmlPath = annotation.defaultXMLFilePath();
            if(defaultXmlPath!=null && !defaultXmlPath.isEmpty()) {
                modelService.setDefaultXmlPath(defaultXmlPath);
            }
            modelService.setYangFilePaths(Arrays.asList(annotation.yangFilePaths()));
            String moduleNameAndVersion = annotation.yangModule();
            Pair<String, String> moduleNameAndRevisionPair = getModuleNameAndRevision(moduleNameAndVersion);
            modelService.setModuleName(moduleNameAndRevisionPair.getFirst());
            modelService.setModuleRevision(moduleNameAndRevisionPair.getSecond());

        }
    }

    private static Pair<String, String> getModuleNameAndRevision(String moduleNameAndVersion) throws ModelServiceBuilderException {
        Matcher matcher = MODULE_NAME_AND_REVISION_PATTERN.matcher(moduleNameAndVersion);
        if(matcher.matches()){
            return  new Pair<>(matcher.group(1), matcher.group(2));
        }else {
            throw new ModelServiceBuilderException("Module name and revision could not be parsed, it should be on the following " +
                    "format: module-name@YYYY-MM-DD");
        }
    }
}
