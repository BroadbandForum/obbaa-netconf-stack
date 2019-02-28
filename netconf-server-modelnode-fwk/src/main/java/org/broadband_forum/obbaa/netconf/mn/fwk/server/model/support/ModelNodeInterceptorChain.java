package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditContext;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeInterceptor;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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
	public void interceptEditConfig(HelperDrivenModelNode modelNode, EditContext editContext) throws EditConfigException {
		List<ModelNodeInterceptor> interceptors = m_modelNodeInterceptors.get(modelNode.getModelNodeSchemaPath());
		if (interceptors!=null){
			for (ModelNodeInterceptor modelNodeInterceptor:interceptors){
				modelNodeInterceptor.interceptEditConfig(modelNode, editContext);
			}
		}
		modelNode.editNode(editContext);

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

}
