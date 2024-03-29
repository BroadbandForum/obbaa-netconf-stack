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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeInterceptor;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.TimingLogger;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public final class ModelNodeInterceptorChain implements ModelNodeInterceptor{

	/** Map of list of interceptor against a shcema path. */
	private ConcurrentHashMap<SchemaPath, List<ModelNodeInterceptor>> m_modelNodeInterceptors
			= new ConcurrentHashMap<SchemaPath,List<ModelNodeInterceptor>>();
	
	/** The Constant INSTANCE. */
	private static final ModelNodeInterceptorChain INSTANCE = new ModelNodeInterceptorChain(); 

	/**
	 * Instantiates a new model node interceptor impl.
	 */
	private ModelNodeInterceptorChain() {
	}
	
	/**
	 * Gets the single instance of ModelNodeInterceptorImpl.
	 *
	 * @return single instance of ModelNodeInterceptorImpl
	 */
	public static ModelNodeInterceptorChain getInstance(){
		return INSTANCE;
	}
	
	public ModelNodeInterceptor createInterceptor(){
		return this;
	}
	
	/**
	 * Intercept the edit config.
	 *
	 * @param modelNode the model node
	 * @param editContext the edit context
	 * @throws EditConfigException the edit config exception
	 */
	public void interceptEditConfig(HelperDrivenModelNode modelNode, EditContext editContext, WritableChangeTreeNode changeTreeNode) throws EditConfigException {
		List<ModelNodeInterceptor> interceptors = m_modelNodeInterceptors.get(modelNode.getModelNodeSchemaPath());
		if (interceptors!=null){
			for (ModelNodeInterceptor modelNodeInterceptor:interceptors){
				TimingLogger.startPhase("createEditTree.makeChangesInDataStore."+modelNodeInterceptor.getClass().getName());
				modelNodeInterceptor.interceptEditConfig(modelNode, editContext, changeTreeNode);
				TimingLogger.endPhase("createEditTree.makeChangesInDataStore."+modelNodeInterceptor.getClass().getName(), false);
			}
		}
	}

	/**
	 * Register a new interceptor for a given schema path
	 *
	 * @param modelNodeId the model node id
	 * @param interceptor the interceptor
	 */
	public void registerInterceptor(SchemaPath modelNodeId, ModelNodeInterceptor interceptor){
		List<ModelNodeInterceptor> interceptorList = m_modelNodeInterceptors.get(modelNodeId);
		if (interceptorList==null){
			interceptorList = new ArrayList<ModelNodeInterceptor>();
			m_modelNodeInterceptors.put(modelNodeId,interceptorList);
		}
		interceptorList.add(interceptor);
	}

	public List<ModelNodeInterceptor> getMNInterceptorsForSP(SchemaPath schemaPath) {
		return m_modelNodeInterceptors.get(schemaPath);
	}

	public void unregisterInterceptor(SchemaPath path, ModelNodeInterceptor mnInterceptor) {
		m_modelNodeInterceptors.get(path).remove(mnInterceptor);
		if(m_modelNodeInterceptors.get(path).isEmpty()){
			m_modelNodeInterceptors.remove(path);
		}
	}
}
