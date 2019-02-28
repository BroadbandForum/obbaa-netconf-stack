package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation.AnnotationChildContainerHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation.AnnotationChildListHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation.AnnotationConfigAttributeHelper;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation.AnnotationModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation.AnnotationUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * A registrar is an official keeper of records made in a deployHelpers.
 * ModelNodeRegistrar is record keeper in {@link ModelNodeHelperRegistry}
 *
 *
 */
public class ModelNodeRegistrar {
	
	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ModelNodeRegistrar.class, LogAppNames.NETCONF_STACK);

	
    public static void registerAnnotationModelNodeClass(String componentId, SchemaPath modelNodeSchemaPath, Class<? extends AnnotationModelNode> class1,
														ModelNodeHelperRegistry modelNodeHelperRegistry) throws ModelNodeInitException{
        ModelNodeRegistrar.register(componentId, modelNodeSchemaPath, class1, modelNodeHelperRegistry);
    }

	//in future other types of classes can also get help from this registrar

    public static void registerModelNodeFactory(String factoryName, ModelNodeFactory modelNodeFactory, ModelNodeHelperRegistry modelNodeHelperRegistry) throws ModelNodeFactoryException {
    	modelNodeHelperRegistry.registerModelNodeFactory(factoryName, modelNodeFactory);

    }

	public static void register(String componentId, SchemaPath modelNodeSchemaPath, Class<? extends ModelNode> instanceClass, ModelNodeHelperRegistry modelNodeHelperRegistry) throws ModelNodeInitException{
	    if (!modelNodeHelperRegistry.isRegistered(modelNodeSchemaPath)) {
	        synchronized (instanceClass) {
	            if (!modelNodeHelperRegistry.isRegistered(modelNodeSchemaPath)) {
	                updateRegistry(componentId, modelNodeSchemaPath, instanceClass, modelNodeHelperRegistry);
	                modelNodeHelperRegistry.registrationComplete(modelNodeSchemaPath);
	            }
	        }
	    }
	}

	public static void updateRegistry(String componentId, SchemaPath modelNodeSchemaPath, Class<? extends ModelNode> instanceClass, ModelNodeHelperRegistry modelNodeHelperRegistry) throws ModelNodeInitException {
		// find all the methods for attributes
		for (Method m : instanceClass.getMethods()) {
            SchemaPath childSchemaPath;
            if (m.isAnnotationPresent(ConfigAttribute.class)) {
		        AnnotationConfigAttributeHelper attributeHelper = null;
		        ConfigAttribute annotation = m.getAnnotation(ConfigAttribute.class);
		        String setMethodStr = AnnotationUtil.getSetMethodName(m);
		        try {
		            Method setter = instanceClass.getMethod(setMethodStr, m.getReturnType());
		            attributeHelper = new AnnotationConfigAttributeHelper(m, setter);
		        } catch (NoSuchMethodException | SecurityException e) {
		            throw new ModelNodeInitException("Cannot find setter for : " + annotation.name(), e);
		        }
		        String namespace = annotation.namespace();
            	String name = annotation.name();
                Optional<Revision> revision = getRevision(annotation.revision());
                if(!revision.isPresent()){
                    revision = modelNodeSchemaPath.getLastComponent().getRevision();
                }
                QName qname = getQName(namespace, name, revision.orElse(null));
		        if (annotation.isKey()) {
		            modelNodeHelperRegistry.registerNaturalKeyHelper(componentId, modelNodeSchemaPath, qname, attributeHelper);
		        }
		        modelNodeHelperRegistry.registerConfigAttributeHelper(componentId, modelNodeSchemaPath, qname, attributeHelper);
		    } else if (m.isAnnotationPresent(ContainerList.class)) {
		        ContainerList annotation = m.getAnnotation(ContainerList.class);
		        String namespace = annotation.namespace();
            	String name = annotation.name();
                Optional<Revision> revision = getRevision(annotation.revision());
                if(!revision.isPresent()){
                    revision = modelNodeSchemaPath.getLastComponent().getRevision();
                }
                QName qname = getQName(namespace, name, revision.orElse(null));
                childSchemaPath = new SchemaPathBuilder().withParent(modelNodeSchemaPath).appendQName(qname).build();
		        modelNodeHelperRegistry.registerChildListHelper(componentId, modelNodeSchemaPath, qname,
                        new AnnotationChildListHelper(m, childSchemaPath, annotation.childClass(), annotation.childCreateStrategy(), annotation.childFactoryName(), modelNodeHelperRegistry));
                registerAnnotationModelNodeClass(componentId, childSchemaPath, (Class<? extends AnnotationModelNode>)  annotation.childClass(), modelNodeHelperRegistry);

		    } else if (m.isAnnotationPresent(ContainerChild.class)) {
		        ContainerChild annotation = m.getAnnotation(ContainerChild.class);
		        String setMethodStr = AnnotationUtil.getSetMethodName(m);
		        AnnotationChildContainerHelper containerHelper = null;

                String namespace = annotation.namespace();
                String name = annotation.name();
                Optional<Revision> revision = getRevision(annotation.revision());
                if(!revision.isPresent()){
                    revision = modelNodeSchemaPath.getLastComponent().getRevision();
                }
                QName qname = getQName(namespace, name, revision.orElse(null));
                childSchemaPath = new SchemaPathBuilder().withParent(modelNodeSchemaPath).appendQName(qname).build();
                try {
		            Method setter = instanceClass.getMethod(setMethodStr, m.getReturnType());
		            CreateStrategy createStrategy = annotation.createStrategy();
		            containerHelper = new AnnotationChildContainerHelper(annotation.name(), createStrategy, annotation.factoryName() , m, setter, childSchemaPath, modelNodeHelperRegistry);
                    registerAnnotationModelNodeClass(componentId, childSchemaPath, (Class<? extends AnnotationModelNode>) setter.getParameterTypes()[0], modelNodeHelperRegistry);
		        } catch (NoSuchMethodException | SecurityException e) {
		            LOGGER.info("Cannot find setter for : {} maybe there is no setter" ,annotation.name(), e);
		            containerHelper = new AnnotationChildContainerHelper(m);
		        }

		        modelNodeHelperRegistry.registerChildContainerHelper(componentId, modelNodeSchemaPath, qname, containerHelper);

		    }
		    
		}
	}

    private static Optional<Revision> getRevision(String annotationRevision) {
        Optional<Revision> revision = Optional.empty();
        if (annotationRevision != null && ! annotationRevision.isEmpty()) {
            revision = Revision.ofNullable(annotationRevision);
        }
        return revision;
    }
	
	private static QName getQName(String namespace, String name, Revision revision) {
        QName qname = null;
        try {
            qname = QName.create(new URI(namespace), revision, name);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("namespace is not a valid URI: " + namespace);
        }
        return qname;
    }
}
