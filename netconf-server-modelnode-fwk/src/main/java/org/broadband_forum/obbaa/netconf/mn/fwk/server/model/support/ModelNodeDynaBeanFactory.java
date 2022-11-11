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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * This class reflects the ModelNode as a Bean dynamically. The Resulting bean is called ModelNodeDynaBean  
 * Each ModelNodeDynaBean contains the leaf, leaf-list, list and containers of the current model node as properties
 */
public class ModelNodeDynaBeanFactory {

    public static final String ATTRIBUTE_CLASS = "class";
    private static final String MODIFIED_ATTRIBUTE_PREFIX = "_NAV";
    private static final String DYNABEAN_CACHE_KEY = "DYNABEAN_CACHE";
    public static final String ATTRIBUTE_LIST = "AttributeList";
    static final String CHILD_REFRESH_LIST = "CHILD_REFRESH_LIST";
    
    private final static AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ModelNodeDynaBeanFactory.class, LogAppNames.NETCONF_STACK);

    private static void logDebug(String message, Object... objects) {
        Boolean debugEnabled = (Boolean) RequestScope.getCurrentScope().getFromCache(ModelNodeDynaBeanFactory.class.getName());
        if (debugEnabled == null) {
            debugEnabled = LOGGER.isDebugEnabled();
            RequestScope.getCurrentScope().putInCache(ModelNodeDynaBeanFactory.class.getName(), debugEnabled);
        }
        if (debugEnabled) {
            LOGGER.debug(message, objects);
        }
    }
    public static String getDynaBeanAttributeName(String name) {
        
        /**
         * JXPath does not read an attribute 'class' inside DynaBean, because class is used to save the class name of DynaClass inside
         * dynaBean. More info: FNMS-10896.  
         */
        switch (name) {
        case ATTRIBUTE_CLASS :
            logDebug("found class in {}", LOGGER.sensitiveData(name));
            return name+MODIFIED_ATTRIBUTE_PREFIX;
        default:
            return name;
        }
    }
    
    public static String getModelNodeAttributeName(String name) {
        if (name == null) {
            return null;
        }
        switch (name) {
        case ATTRIBUTE_CLASS+MODIFIED_ATTRIBUTE_PREFIX:
            logDebug(null, "found dyna attribute class_NAV in {}", LOGGER.sensitiveData(name));
            return ATTRIBUTE_CLASS;
        default:
            return name;
        }
    }
    
    @SuppressWarnings("unchecked")
    private static HashMap<String, Object> getCachedNodes() {
        RequestScope currentScope = RequestScope.getCurrentScope();
        HashMap<String, Object> cachedNodes = (HashMap<String, Object>) currentScope.getFromCache(DYNABEAN_CACHE_KEY);
        if (cachedNodes == null) {
            logDebug("Create new cache for ModelNodeDynaBeanFactory");
            cachedNodes = new HashMap<String, Object>();
            currentScope.putInCache(DYNABEAN_CACHE_KEY, cachedNodes);
        }
        return cachedNodes;
    }

    public static void clearDynaBeanCachedNodes() {
        RequestScope currentScope = RequestScope.getCurrentScope();
        HashMap<String, Object> cachedNodes = (HashMap<String, Object>) currentScope.getFromCache(DYNABEAN_CACHE_KEY);
        if (cachedNodes != null) {
            cachedNodes.clear();
        }
        currentScope.removeFromCache(DYNABEAN_CACHE_KEY);
    }
    
    public static boolean containsBeanForModelNode(ModelNodeId modelNodeId, SchemaRegistry schemaRegistry) {
        return getCachedNodes().containsKey(modelNodeId.xPathString(schemaRegistry, true, true));
    }
    
    public static void removeFromCache(ModelNode modelNode, SchemaRegistry schemaRegistry){
        removeFromCache(modelNode.getModelNodeId(), schemaRegistry);
    }
    
    public static void removeFromCache(ModelNodeId modelNodeId, SchemaRegistry schemaRegistry) {
        HashMap<String, Object> cachedNodes = getCachedNodes();
        cachedNodes.remove(modelNodeId.xPathString(schemaRegistry, true, true));
        logDebug("removed dynaBean for {}", LOGGER.sensitiveData(modelNodeId));
    }
    
    public static void resetCache() {
        RequestScope.getCurrentScope().putInCache(DYNABEAN_CACHE_KEY, null);
    }
    

    public static ModelNodeDynaBean getDynaBean(ModelNode modelNode) {
        return getDynaBean(modelNode, null);
    }
    
    public static ModelNodeDynaBean getDynaBean(ModelNode modelNode, ModelNode parent) {
        HashMap<String, Object> cachedNodes = getCachedNodes();
		ModelNodeId modelNodeId = modelNode.getModelNodeId();
		String modelNodeXPath = modelNodeId.xPathString(modelNode.getSchemaRegistry(), true, true);
		if (cachedNodes.containsKey(modelNodeXPath)) {
			ModelNodeDynaBean dynaBean = (ModelNodeDynaBean) cachedNodes.get(modelNodeXPath);
			String namespace = modelNode.getQName().getNamespace().toString();
			if (dynaBean.get(ModelNodeWithAttributes.NAMESPACE).equals(namespace)) {
				logDebug("DynaBean available in cache {} for modelNodeId", LOGGER.sensitiveData(dynaBean), LOGGER.sensitiveData(modelNodeId));
				return dynaBean;
			} else {
				return getDynaBean(Object.class, (ModelNodeWithAttributes) modelNode, cachedNodes, parent);
			}
		} else {
			return getDynaBean(Object.class, (ModelNodeWithAttributes) modelNode, cachedNodes, parent);
		}
    }
    
	public static DynaBean getDynaBeanForLeaf(ModelNode parentNode, DynaBean parent, String leafName, String leafValue, String ns) {
        try {
            ModelNodeId leafId = new ModelNodeId(parentNode.getModelNodeId());
            leafId.addRdn(leafName, ns, leafValue);
            HashMap<String, Object> cachedNodes = getCachedNodes();
            DynaBean object = (DynaBean) cachedNodes.get(leafId.xPathString(parentNode.getSchemaRegistry(), true, true));
            if (object != null) {
                return object;
            }
            Set<String> attributeList = new TreeSet<String>();
            List<DynaProperty> properties = new ArrayList<DynaProperty>();
            properties.add(new DynaProperty(leafName, String.class));
            properties.add(new DynaProperty(ModelNodeWithAttributes.PARENT, DynaBean.class));
            properties.add(new DynaProperty(ModelNodeWithAttributes.NAMESPACE, String.class));
            properties.add(new DynaProperty(ModelNodeWithAttributes.LEAF_VALUE, String.class));
            properties.add(new DynaProperty(CHILD_REFRESH_LIST, Set.class));
            properties.add(new DynaProperty(ATTRIBUTE_LIST, Set.class));
            properties.add(new DynaProperty(ModelNodeWithAttributes.PARENT_MODELNODE, ModelNode.class));

            DynaClass dynaClass = new BasicDynaClass("LeafBean_" + leafName, null, properties.toArray(new DynaProperty[0]));

            DynaBean bean = dynaClass.newInstance();
            bean.set(leafName, leafValue);
            bean.set(ModelNodeWithAttributes.PARENT, parent);
            bean.set(ModelNodeWithAttributes.NAMESPACE, ns);
            bean.set(ModelNodeWithAttributes.LEAF_VALUE, leafValue);
            bean.set(CHILD_REFRESH_LIST, new TreeSet<String>());
            bean.set(ATTRIBUTE_LIST,attributeList);
            bean.set(ModelNodeWithAttributes.PARENT_MODELNODE, parentNode);
            
            attributeList.add(ModelNodeWithAttributes.PARENT);
            attributeList.add(ModelNodeWithAttributes.NAMESPACE);
            attributeList.add(ModelNodeWithAttributes.LEAF_VALUE);
            attributeList.add(CHILD_REFRESH_LIST);
            attributeList.add(ModelNodeWithAttributes.PARENT_MODELNODE);
            attributeList.add(leafName);
            cachedNodes.put(leafId.xPathString(parentNode.getSchemaRegistry(), true, true), bean);
            return bean;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static void fetchChildSchemaNodes(SchemaRegistry schemaRegistry, SchemaPath parentPath, Collection<DataSchemaNode> children) {
        Collection<DataSchemaNode> childNodes = schemaRegistry.getChildren(parentPath);
        for (DataSchemaNode child : childNodes) {
            if (child instanceof ChoiceSchemaNode) {
                Collection<CaseSchemaNode> caseNodes = ((ChoiceSchemaNode) child).getCases().values();
                for (CaseSchemaNode caseNode : caseNodes) {
                    fetchChildSchemaNodes(schemaRegistry, caseNode.getPath(), children);
                }
            } else {
                children.add(child);
            }
        }
    }

    private static void fetchChildNodes(ModelNode modelNode, Collection<DataSchemaNode> childSchemaNodes) {
        fetchChildSchemaNodes(modelNode.getSchemaRegistry(), modelNode.getModelNodeSchemaPath(), childSchemaNodes);
        if (!modelNode.getSchemaRegistry().equals(modelNode.getMountRegistry())) {
                fetchChildSchemaNodes(modelNode.getMountRegistry(), modelNode.getModelNodeSchemaPath(), childSchemaNodes);
        }
    }
    
    private static ModelNodeDynaBean getDynaBean(Class<?> klass, ModelNodeWithAttributes modelNode, Map<String, Object> cachedNodes, ModelNode parent) {
        try {
            boolean addModelNode = true;
            // add all localNames with different namespace
            Set<String> attributesWithSameLocalNameDifferentNameSpace = new HashSet<>();
            Set<String> attributeList = new TreeSet<String>();
            
            // If the incoming map has an entry, indicates the bean is already built, use the same and 
            // dont build again
            String xpathString = modelNode.getModelNodeId().xPathString(modelNode.getSchemaRegistry(), true, true);
            if (cachedNodes.containsKey(xpathString)) {
				ModelNodeDynaBean dynaBean = (ModelNodeDynaBean) cachedNodes
						.get(xpathString);
				String namespace = modelNode.getQName().getNamespace().toString();
				if (dynaBean.get(ModelNodeWithAttributes.NAMESPACE).equals(namespace)) {
					return (ModelNodeDynaBean) dynaBean;
				}
			}

            // validate if the caller has requested for complete dynabean tree
            if (klass.getName().equals(Object.class.getName())) {
                addModelNode = false;
            }
            
            // Build DynaBean properties
            List<DynaProperty> properties = new ArrayList<>();
            properties.add(new DynaProperty(ModelNodeWithAttributes.ATTRIBUTES_WITH_SAME_LOCAL_NAME, Set.class));
            properties.add(new DynaProperty(ModelNodeWithAttributes.CHILD_LISTS, Set.class));
            properties.add(new DynaProperty(ModelNodeWithAttributes.CHILD_CONTAINERS, Set.class));
            properties.add(new DynaProperty(ModelNodeWithAttributes.LEAF_LISTS, Set.class));
            properties.add(new DynaProperty(ModelNodeWithAttributes.NAMESPACE, String.class));
            properties.add(new DynaProperty(ModelNodeWithAttributes.MODEL_NODE, ModelNodeWithAttributes.class));
            properties.add(new DynaProperty(ModelNodeWithAttributes.ADD_MODEL_NODE, Boolean.class));
            properties.add(new DynaProperty(ModelNodeWithAttributes.LEAF_COUNT, Integer.class));
            properties.add(new DynaProperty(ModelNodeWithAttributes.LEAF_LIST_COUNT, Integer.class));
            properties.add(new DynaProperty(CHILD_REFRESH_LIST, Set.class));
            properties.add(new DynaProperty(ATTRIBUTE_LIST, Set.class));

            ModelNode parentNode = parent;
            if (parentNode == null) {
                parentNode = modelNode.getParent();
            }
            if (parentNode != null) {
                if (addModelNode) {
                    properties.add(new DynaProperty(ModelNodeWithAttributes.PARENT, ModelNode.class));
                } else {
                    properties.add(new DynaProperty(ModelNodeWithAttributes.PARENT, Object.class));
                }
            }

            Set<Entry<QName, ConfigLeafAttribute>> entry = modelNode.getAttributes().entrySet();
            Set<Entry<QName, LinkedHashSet<ConfigLeafAttribute>>> leafListEntry = modelNode.getLeafLists().entrySet();
            Collection<DataSchemaNode> childSchemaNodes = new LinkedList<DataSchemaNode>();
            fetchChildNodes(modelNode, childSchemaNodes);

            SchemaRegistry schemaRegistry = modelNode.getSchemaRegistry();

            if(schemaRegistry.getAttributesWithSameLocalNameDifferentNameSpace(modelNode.getModelNodeSchemaPath()) == null) {

                childSchemaNodes.forEach(schemaNode -> {
                    if (childSchemaNodes.stream()
                            .filter(compareSchemaNode -> compareSchemaNode.getQName().getLocalName()
                                    .equals(schemaNode.getQName().getLocalName())).count() > 1) {
                        attributesWithSameLocalNameDifferentNameSpace
                                .add(schemaNode.getQName().getLocalName());
                    }
                });

                schemaRegistry.registerAttributesWithSameLocalNameDifferentNameSpace(modelNode.getModelNodeSchemaPath(), attributesWithSameLocalNameDifferentNameSpace);
            } else {
                attributesWithSameLocalNameDifferentNameSpace.addAll(schemaRegistry.getAttributesWithSameLocalNameDifferentNameSpace(modelNode.getModelNodeSchemaPath()));
            }

            // add all list names as a property
            Set<String> childListNames = new HashSet<String>();

            // add all child container names as a property
            Set<String> childContainerNames = new HashSet<String>();

            /// add all the attributes/leaf as a property
            for (Entry<QName, ConfigLeafAttribute> item : entry) {
                // TODO FNMS-10121 everywhere here: we should use QNames as keys !!
                DynaProperty property = new DynaProperty(getDynaBeanAttributeName(item.getKey().getLocalName()), String.class);
                properties.add(property);
                boolean isSameNameWithDifferentNameSpace = attributesWithSameLocalNameDifferentNameSpace.contains(item.getKey().getLocalName());
                if(isSameNameWithDifferentNameSpace) {
                    String nameWithQname = item.getKey() + ":" + item.getKey().getLocalName();
                    DynaProperty propertyNameWithQName = new DynaProperty(getDynaBeanAttributeName(nameWithQname), String.class);
                    properties.add(propertyNameWithQName);
                }
            }

            // add all leafList as a property
            for (Entry<QName,LinkedHashSet<ConfigLeafAttribute>> leafList : leafListEntry){
                boolean isSameNameWithDifferentNameSpace = attributesWithSameLocalNameDifferentNameSpace.contains(leafList.getKey().getLocalName());
                DynaProperty property = new DynaProperty(getDynaBeanAttributeName(leafList.getKey().getLocalName()), Object.class);
                properties.add(property);
                if(isSameNameWithDifferentNameSpace) {
                    String nameWithQname = leafList.getKey() + ":" + leafList.getKey().getLocalName();
                    DynaProperty propertyNameWithQName = new DynaProperty(getDynaBeanAttributeName(nameWithQname), Object.class);
                    properties.add(propertyNameWithQName);
                }
            }

            for (DataSchemaNode schemaNode: childSchemaNodes){
                //TODO: FNMS-10121 use qname and not localName
                String name = getDynaBeanAttributeName(schemaNode.getQName().getLocalName());
                boolean isSameNameWithDifferentNameSpace = attributesWithSameLocalNameDifferentNameSpace.contains(name);
                if (schemaNode instanceof ContainerSchemaNode){
                    String nameWithQname = schemaNode.getQName() + ":" + name;
                    childContainerNames.add(name);
                    if (isSameNameWithDifferentNameSpace) {
                        childContainerNames.add(nameWithQname);
                    }
                    if (addModelNode) {
                        addDynaProperties(name, schemaNode, properties, ModelNode.class, schemaRegistry, isSameNameWithDifferentNameSpace);
                        properties.add(new DynaProperty(name, ModelNode.class));
                    } else {
                        addDynaProperties(name, schemaNode, properties, Object.class, schemaRegistry, isSameNameWithDifferentNameSpace);
                        properties.add(new DynaProperty(name, Object.class));
                    }
                } else if (schemaNode instanceof ListSchemaNode) {

                    addDynaProperties(name, schemaNode, properties, List.class, schemaRegistry, isSameNameWithDifferentNameSpace);
                    String nameWithQname = schemaNode.getQName() + ":" + name;
                    childListNames.add(name);
                    properties.add(new DynaProperty(name, List.class));
                    if(isSameNameWithDifferentNameSpace) {
                        childListNames.add(nameWithQname);
                    }
                } else if (schemaNode instanceof LeafSchemaNode) {
                    addDynaProperties(name, schemaNode, properties, String.class, schemaRegistry, isSameNameWithDifferentNameSpace);
                }
            }
            
            // create the dyna class with the built properties and create a new dynabean instance
            DynaClass dynaClass = new ModelNodeDynaClass(modelNode.getQName().getLocalName(), null, properties.toArray(new DynaProperty[0]));
            ModelNodeDynaBean modelNodeBean = (ModelNodeDynaBean)dynaClass.newInstance();
            
            // add the dyna bean against the model node in the cached map
            cachedNodes.put(xpathString, modelNodeBean);

            String nameSpace = modelNode.getQName().getNamespace().toString();
            modelNodeBean.set(ModelNodeWithAttributes.ATTRIBUTES_WITH_SAME_LOCAL_NAME, attributesWithSameLocalNameDifferentNameSpace);
            modelNodeBean.set(ModelNodeWithAttributes.CHILD_LISTS, childListNames);
            modelNodeBean.set(ModelNodeWithAttributes.CHILD_CONTAINERS, childContainerNames);
            modelNodeBean.set(ModelNodeWithAttributes.NAMESPACE, nameSpace);
            modelNodeBean.set(ModelNodeWithAttributes.MODEL_NODE, modelNode);
            modelNodeBean.set(ModelNodeWithAttributes.ADD_MODEL_NODE, addModelNode);
            modelNodeBean.set(ModelNodeWithAttributes.LEAF_COUNT, modelNode.getAttributes().size());
            modelNodeBean.set(ModelNodeWithAttributes.LEAF_LIST_COUNT, modelNode.getLeafLists().size());
            modelNodeBean.set(CHILD_REFRESH_LIST, new TreeSet<String>());
            modelNodeBean.set(ATTRIBUTE_LIST, attributeList);
            // add all the attributes values of the current node to dynabean instance 
            modelNodeBean.refreshAttributes(entry);
            
            // add all leaflist
            modelNodeBean.refreshLeafLists(leafListEntry);
            
            // parent, container children and list children will be lazy loaded in ModelNodeDynaBean

            
            DynaProperty[] classProperties = modelNodeBean.getDynaClass().getDynaProperties();
            for (DynaProperty classProperty:classProperties) {
                attributeList.add(classProperty.getName());
            }
            logDebug("creating new bean {} for modelNode {}", LOGGER.sensitiveData(modelNodeBean), LOGGER.sensitiveData(modelNode));

            return modelNodeBean;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addDynaProperties(String name, SchemaNode schemaNode, List<DynaProperty> properties, Class<?> classArguement, SchemaRegistry schemaRegistry, boolean isSameNameWithDifferentNameSpace) {
        String nameWithQname = schemaNode.getQName() + ":" + name;
        Module module = schemaRegistry.getModuleByNamespace(
            schemaNode.getQName().getModule().getNamespace().toString());
        String moduleName = null;
        if(module != null) {
            moduleName = module.getName();
        }
        if(moduleName != null) {
            properties.add(new DynaProperty(
                moduleName + ModelNodeWithAttributes.MODULE_NAME_LOCAL_NAME_SEPARATOR + name,
                ModelNode.class));
        }
        if (isSameNameWithDifferentNameSpace) {
            properties.add(new DynaProperty(nameWithQname, classArguement));
        }
    }

}
