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

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes.ADD_MODEL_NODE;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes.ATTRIBUTES_WITH_SAME_LOCAL_NAME;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes.CHILD_CONTAINERS;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes.CHILD_LISTS;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes.MODEL_NODE;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes.MODULE_NAME_LOCAL_NAME_SEPARATOR;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes.PARENT;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeWithAttributes.PARENT_MODELNODE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.beanutils.PropertyUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.DsmNotRegisteredException;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class ModelNodeDynaBean extends BasicDynaBean {

    public static final String COLON = ":";
    private static ThreadLocal<Map<QName,ConfigLeafAttribute>> c_matchCriteria = ThreadLocal.withInitial((Supplier<Map<QName, ConfigLeafAttribute>>) () -> Collections.EMPTY_MAP);
    private static ThreadLocal<Map<QName,Map<QName,ConfigLeafAttribute>>> c_matchCriteriaAcrossXPath =
            ThreadLocal.withInitial((Supplier<Map<QName,Map<QName, ConfigLeafAttribute>>>) () -> Collections.EMPTY_MAP);
    private static ThreadLocal<List<SchemaPath>> c_schemaPathsInOrder = ThreadLocal.withInitial((Supplier<List<SchemaPath>>) () -> Collections.EMPTY_LIST);
    private static ThreadLocal<String> c_leafNameWithModuleNameInPrefix = ThreadLocal.withInitial((Supplier<String>) () -> null);
    private static ThreadLocal<Boolean> c_isContextSet = ThreadLocal.withInitial((Supplier<Boolean>) () -> Boolean.FALSE);
    private static ThreadLocal<Boolean> c_isModuleNameAppendedWithLocalName = ThreadLocal.withInitial((Supplier<Boolean>) () -> Boolean.FALSE);
    private static ThreadLocal<Map<ModelNodeDynaBean, Set<String>>> c_modelNodeToAttributesLoadedWithMatchCriteriaMap = ThreadLocal.withInitial((Supplier<Map<ModelNodeDynaBean, Set<String>>>) () -> Collections.EMPTY_MAP);

    private static final long serialVersionUID = 1L;

    private Set<String> m_refreshList;

    private Set<String> m_attributeList;

    private Set<String> m_attributeWithSameLocalNameDifferentNameSpace;

    private Boolean m_isLogDebugEnabled;

    private final static AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ModelNodeDynaBean.class, LogAppNames.NETCONF_STACK);

    public static ModelNode getContextModelNode(DynaBean contextBean) {
        if(contextBean instanceof ModelNodeDynaBean){
            return ((ModelNodeDynaBean)contextBean).getModelNode();
        }
        return (ModelNode) contextBean.get(PARENT_MODELNODE);
    }

    private ModelNode getModelNode() {
        return (ModelNode) get(MODEL_NODE);
    }

    private void logDebug(String message, Object... objects) {
        if (m_isLogDebugEnabled == null) {
            m_isLogDebugEnabled = LOGGER.isDebugEnabled();
        }

        if (m_isLogDebugEnabled) {
            LOGGER.debug(message,objects);
        }
    }

    public ModelNodeDynaBean(DynaClass dynaClass) {
        super(dynaClass);
    }

    @Override
    public boolean contains(String name, String key) {
        doLazyLoad(name, null);
        return super.contains(name, key);
    }

    public boolean contains(String name) {
        if (PropertyUtils.isReadable(this, name)) {
            return true;
        }

        return ModelNodeDynaClass.containsProperty(this, name);
    }

    @Override
    public Object get(String name) {
        logDebug("fetch for name {} in bean {}", name, LOGGER.sensitiveData(this));
        Pair<String, String> nameAndNameWithQNamePair = getQNameAwareLocalName(name);
        name = nameAndNameWithQNamePair.getFirst();
        String nameWithQname = nameAndNameWithQNamePair.getSecond();
        doLazyLoad(name, nameWithQname);
        if(nameWithQname != null) {
            return super.get(nameWithQname);
        }
        return super.get(name);
    }

    @Override
    public Object get(String name, int index) {
        logDebug("fetch for name {} index {} in bean {}", name, index, LOGGER.sensitiveData(this));
        Pair<String, String> nameAndNameWithQNamePair = getQNameAwareLocalName(name);
        name = nameAndNameWithQNamePair.getFirst();
        String nameWithQname = nameAndNameWithQNamePair.getSecond();
        doLazyLoad(name, nameWithQname);
        if(nameWithQname != null) {
            return super.get(nameWithQname, index);
        }
        return super.get(name, index);
    }

    @Override
    public Object get(String name, String key) {
        logDebug("fetch for name {} key {} in bean {}", name, key, LOGGER.sensitiveData(this));
        Pair<String, String> nameAndNameWithQNamePair = getQNameAwareLocalName(name);
        name = nameAndNameWithQNamePair.getFirst();
        String nameWithQname = nameAndNameWithQNamePair.getSecond();
        doLazyLoad(name, nameWithQname);
        if(nameWithQname != null) {
            return super.get(nameWithQname, key);
        }
        return super.get(name, key);
    }

    @SuppressWarnings("unchecked")
    private void doLazyLoad(String name, String nameWithQname) {
        Set<String> refreshList = getRefreshList();
        if (refreshList.contains(name)) {
            return;
        }
        logDebug("dolazyload for name {} in bean {}", name, LOGGER.sensitiveData(this));

        if(doesMatchCriteriaExist(name)) {
            if (shouldLazyLoad(name, nameWithQname, CHILD_LISTS)) {
                loadChildList(name, nameWithQname);
                return;
            }
        }

        if (!isAlreadyMaterialized(name, nameWithQname, refreshList)) {
            if (PARENT.equals(name)) {
                loadParent();
            } else if (shouldLazyLoad(name, nameWithQname, CHILD_LISTS)) {
                loadChildList(name, nameWithQname);
            } else if (shouldLazyLoad(name, nameWithQname, CHILD_CONTAINERS)) {
                loadChildContainer(name, nameWithQname);
            }
        }

    }

    private boolean shouldLazyLoad(String name, String nameWithQname, String childType) {
        return (nameWithQname != null && ((Collection<String>) values.get(childType)).contains(nameWithQname)) || (nameWithQname == null && ((Collection<String>) values.get(childType))
                .contains(name));
    }

    private boolean isAlreadyMaterialized(String name, String nameWithQname, Set<String> refreshList) {
        return !((nameWithQname != null && values.get(nameWithQname) == null) || (nameWithQname == null && values.get(name) == null) && !refreshList.contains(name));
    }

    private void loadParent() {
        ModelNodeWithAttributes modelNode = (ModelNodeWithAttributes)values.get(MODEL_NODE);
        Boolean addModelNode = (Boolean)values.get(ADD_MODEL_NODE);
        ModelNode parentNode = modelNode.getParent();
        if (parentNode != null) {
            if (addModelNode) {
                set(PARENT, parentNode);
            } else {
                Object dyna = ModelNodeDynaBeanFactory.getDynaBean(parentNode);
                set(PARENT, dyna);
                logDebug("loading parent {} for {}", LOGGER.sensitiveData(dyna), LOGGER.sensitiveData(this));
            }
        }
    }

    private Pair<String, String> getQNameAwareLocalName(String inName){
        if(c_isModuleNameAppendedWithLocalName.get()) {
            if( !inName.contains(MODULE_NAME_LOCAL_NAME_SEPARATOR)) {
                return new Pair<>(inName, null);
            }
            int indexOfSeparator = inName.indexOf(MODULE_NAME_LOCAL_NAME_SEPARATOR);
            String moduleName = inName.substring(0,indexOfSeparator);
            String localName = inName.substring(indexOfSeparator + MODULE_NAME_LOCAL_NAME_SEPARATOR.length());
            if(isAttributeWithSameLocalNameDifferentNameSpace(localName)) {
                ModelNode modelNode = getModelNode();
                SchemaPath childPath = DataStoreValidationUtil
                    .getChildPathModuleNameAware(modelNode.getSchemaRegistry(),
                        modelNode.getModelNodeSchemaPath(), localName, moduleName);
                if(childPath != null) {
                    return new Pair<>(localName, childPath.getLastComponent() + COLON + localName);
                }
            }
            return new Pair<>(localName, null);
        }
        boolean isSameNameWithDifferentNameSpace = isAttributeWithSameLocalNameDifferentNameSpace(inName);
        if(isSameNameWithDifferentNameSpace) {
            ModelNodeWithAttributes modelNode = (ModelNodeWithAttributes) values.get(MODEL_NODE);
            if(c_schemaPathsInOrder.get().size() > 0) {
                SchemaPath childSchemaPath = getChildSchemaPathWhenSameNameWithDifferentNamespace(
                        modelNode.getModelNodeSchemaPath(), inName);
                if (childSchemaPath != null) {
                    return new Pair<>(inName, childSchemaPath.getLastComponent() + COLON + inName);
                }
            } else if (c_leafNameWithModuleNameInPrefix.get() != null) {
                String leafNameWithModuleNameInPrefix = c_leafNameWithModuleNameInPrefix.get();
                SchemaPath childSchemaPath = DataStoreValidationUtil.getChildPathModuleNameAware(modelNode.getSchemaRegistry(),
                        modelNode.getModelNodeSchemaPath(),
                        DataStoreValidationUtil.getLocalName(leafNameWithModuleNameInPrefix),
                        DataStoreValidationUtil.getPrefix(leafNameWithModuleNameInPrefix));
                if (childSchemaPath != null) {
                    return new Pair<>(inName, childSchemaPath.getLastComponent() + COLON + inName);
                }
            }
        }
        return new Pair<>(inName, null);
    }

    private SchemaPath getChildSchemaPathWhenSameNameWithDifferentNamespace(SchemaPath parentSchemaPath, String childName) {
        boolean isSameNameWithDifferentNameSpace = isAttributeWithSameLocalNameDifferentNameSpace(childName);
        if (isSameNameWithDifferentNameSpace) {
            List<SchemaPath> schemaPaths = c_schemaPathsInOrder.get();
            int index = schemaPaths.indexOf(parentSchemaPath);
            if (index  != -1 && index + 1 < schemaPaths.size()) {
                return schemaPaths.get(index + 1);
            }
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void loadChildList(String inName, String inNameWithQname) {
        try {
            String name = ModelNodeDynaBeanFactory.getModelNodeAttributeName(inName);
            ModelNodeWithAttributes modelNode = (ModelNodeWithAttributes)values.get(MODEL_NODE);
            String childQName = getQNameFromNameWithQName(inNameWithQname);
            Boolean addModelNode = (Boolean)values.get(ADD_MODEL_NODE);
            Map<QName, ChildListHelper> childListHelpers = modelNode.getModelNodeHelperRegistry().getChildListHelpers(modelNode.getModelNodeSchemaPath());
            for (Entry<QName, ChildListHelper> childList : childListHelpers.entrySet()) {
                boolean isChildListMatch = childList.getKey().getLocalName().equals(name);
                if(childQName != null) {
                    isChildListMatch = isChildListMatch && childQName.equals(childList.getKey().toString());
                }
                if (isChildListMatch) {
                    ChildListHelper helper = childList.getValue();
                    Map<QName, ConfigLeafAttribute> matchCriteria = c_matchCriteria.get();
                    if(matchCriteria.isEmpty() && !c_matchCriteriaAcrossXPath.get().isEmpty()) {
                        Map<QName, ConfigLeafAttribute> matchCriteriaAcrossXPath =
                                c_matchCriteriaAcrossXPath.get().get(childList.getKey());
                        if(matchCriteriaAcrossXPath != null && !matchCriteriaAcrossXPath.isEmpty()) {
                            matchCriteria = matchCriteriaAcrossXPath;
                        }
                    }
                    Collection<ModelNode> listNodes = helper.getValue(modelNode, matchCriteria);
                    boolean isLoadedWithMatchCriteria = doesMatchCriteriaExist(name);
                    List childEntries = null;
                    for (ModelNode list : listNodes) {
                        if (addModelNode) {
                            if (childEntries==null){
                                childEntries = new ArrayList<ModelNode>();
                            }
                            childEntries.add(list);
                        } else {
                            if (childEntries==null){
                                childEntries = new ArrayList<Object>();
                            }
                            Object dyna = ModelNodeDynaBeanFactory.getDynaBean(list, modelNode);
                            logDebug("loading child list {} dyna for {}", LOGGER.sensitiveData(dyna), LOGGER.sensitiveData(this));
                            childEntries.add(dyna);
                        }
                    }
                    if (childEntries!=null){
                        if(childQName != null) {
                            set(childQName + COLON + inName, childEntries);
                            if(isLoadedWithMatchCriteria) {
                                addToAttributesLoadedWithMatchCriteria(childQName + COLON + inName);
                            }
                        } else {
                            set(inName, childEntries);
                            if(isLoadedWithMatchCriteria) {
                                addToAttributesLoadedWithMatchCriteria(inName);
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean doesMatchCriteriaExist(String name) {
        if (!c_matchCriteria.get().isEmpty()) {
            return true;
        } else if(!c_matchCriteriaAcrossXPath.get().isEmpty()) {
            for (Entry<QName, Map<QName, ConfigLeafAttribute>> entry : c_matchCriteriaAcrossXPath.get().entrySet()) {
                QName qName = entry.getKey();
                if (qName.getLocalName().equals(name)) {
                    return true;
                }
            }
        }
        return  false;
    }

    private void addToAttributesLoadedWithMatchCriteria(String inName) {
        Set<String> attributesLoadedWithMatchCriteria = c_modelNodeToAttributesLoadedWithMatchCriteriaMap.get().get(this);
        if (attributesLoadedWithMatchCriteria == null) {
            attributesLoadedWithMatchCriteria = new HashSet<>();
            c_modelNodeToAttributesLoadedWithMatchCriteriaMap.get().put(this, attributesLoadedWithMatchCriteria);
        }
        attributesLoadedWithMatchCriteria.add(inName);
    }

    private void loadChildContainer(String inName, String inNameWithQname) {
        try {
            String name = ModelNodeDynaBeanFactory.getModelNodeAttributeName(inName);
            ModelNodeWithAttributes modelNode = (ModelNodeWithAttributes)values.get(MODEL_NODE);
            String childQName = getQNameFromNameWithQName(inNameWithQname);
            Boolean addModelNode = (Boolean)values.get(ADD_MODEL_NODE);
            Map<QName, ChildContainerHelper> childContainers = modelNode.getModelNodeHelperRegistry().getChildContainerHelpers(modelNode.getModelNodeSchemaPath());
            List<Object> containers = new ArrayList<Object>();
            for (Entry<QName, ChildContainerHelper> childContainer: childContainers.entrySet()) {
                boolean isChildListMatch = childContainer.getKey().getLocalName().equals(name);
                if(childQName != null) {
                    isChildListMatch = isChildListMatch && childQName.equals(childContainer.getKey().toString());
                }
                if (isChildListMatch) {
                    ChildContainerHelper containerHelper = childContainer.getValue();
                    ModelNode containerNode = null;
                    try{
                        containerNode = containerHelper.getValue(modelNode);
                    } catch(Exception e) {
                        if (!(e instanceof DsmNotRegisteredException)) {
                            throw e;
                        } else {
                            //We have picked up a wrong node. since we are comparing only name here.
                            //NS addition is a seperate task to be done to enhance support in ModelNodeDynaBean.
                            // lets continue to scan others, if they also want to throw an exception :)
                            // FNMS-6679
                            continue;
                        }
                    }
                    if (containerNode != null) {
                        if (addModelNode) {
                            containers.add(containerNode);
                        } else {
                            Object dyna = ModelNodeDynaBeanFactory.getDynaBean(containerNode);
                            logDebug("loading child container {} dyna for {}", LOGGER.sensitiveData(dyna), LOGGER.sensitiveData(this));
                            containers.add(dyna);
                        }
                        if (containers!=null){
                            if(childQName != null) {
                                set(childQName + COLON + inName, containers);
                            } else {
                                set(inName, containers);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getQNameFromNameWithQName(String nameWithQName) {
        if(nameWithQName != null && nameWithQName.contains(COLON)) {
            return nameWithQName.substring(0, nameWithQName.lastIndexOf(COLON));
        }
        return null;
    }

    public void refreshLeafLists(Set<Entry<QName, LinkedHashSet<ConfigLeafAttribute>>> leafListEntry) {
        for (Entry<QName, LinkedHashSet<ConfigLeafAttribute>> item : leafListEntry) {
            String name = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(item.getKey().getLocalName());
            if(isAttributeWithSameLocalNameDifferentNameSpace(name)) {
                name = item.getKey() + COLON + name;
            }
        	set(name, item.getValue());
        }
    }


    public void refreshAttributes(Set<Entry<QName, ConfigLeafAttribute>> entry) {
        for (Entry<QName, ConfigLeafAttribute> item : entry) {
            String name = ModelNodeDynaBeanFactory.getDynaBeanAttributeName(item.getKey().getLocalName());
            if(isAttributeWithSameLocalNameDifferentNameSpace(name)) {
                name = item.getKey() + COLON + name;
            }
            set(name, item.getValue().getStringValue());
        }
    }

    /**
     * added for debuggin
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public String toString() {
        DynaProperty[] properties = this.dynaClass.getDynaProperties();
        StringBuilder builder = new StringBuilder();
        Collection childNames = new HashSet<>((Collection) super.get(CHILD_LISTS));
        childNames.addAll((Collection) super.get(CHILD_CONTAINERS));
        builder.append(this.dynaClass.getName()).append("[");
        for (DynaProperty property:properties){
            if (property.getName().equals(PARENT)){
                builder.append(property.getName()).append(COLON).append(" ").append('\n');
            }
            else if (!property.getName().contains(MODULE_NAME_LOCAL_NAME_SEPARATOR) && !childNames.contains(property.getName())){
                builder.append(property.getName()).append(COLON).append(super.get(property.getName())).append(" ").append('\n');
            }
        }
        builder.append("]");

        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    private Set<String> getRefreshList() {
        if (m_refreshList == null) {
            m_refreshList =  (Set<String>) super.get(ModelNodeDynaBeanFactory.CHILD_REFRESH_LIST);
        }
        return m_refreshList;
    }

    @SuppressWarnings("unchecked")
    public boolean isReadable(String name) {
        if (m_attributeList == null) {
            m_attributeList = (Set<String>) super.get(ModelNodeDynaBeanFactory.ATTRIBUTE_LIST);
        }
        return m_attributeList.contains(name);
    }

    public boolean isAttributeWithSameLocalNameDifferentNameSpace(String name) {
        if (m_attributeWithSameLocalNameDifferentNameSpace == null) {
            m_attributeWithSameLocalNameDifferentNameSpace = (Set<String>) super.get(ATTRIBUTES_WITH_SAME_LOCAL_NAME);
        }
        return m_attributeWithSameLocalNameDifferentNameSpace.contains(name);
    }

    @Override
    public DynaProperty getDynaProperty(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Property name is missing.");
        } else if (name.contains(ModelNodeWithAttributes.MODULE_NAME_LOCAL_NAME_SEPARATOR)){
            return super.getDynaProperty(name.substring(name.indexOf(ModelNodeWithAttributes.MODULE_NAME_LOCAL_NAME_SEPARATOR) + ModelNodeWithAttributes.MODULE_NAME_LOCAL_NAME_SEPARATOR.length()));
        }
        return super.getDynaProperty(name);
    }

    public static <T> T withContext(ModelNodeDynaBeanContext context, ModelDynaBeanContextTemplate<T> template) {

        Map<QName,ConfigLeafAttribute> oldMatchCriteriaValue = c_matchCriteria.get();
        Map<QName, Map<QName, ConfigLeafAttribute>> oldMatchCriteriaAcrossXPath = c_matchCriteriaAcrossXPath.get();
        List<SchemaPath> oldSchemaPathsInOrderValue = c_schemaPathsInOrder.get();
        String oldLeafWithModuleNameInPrefix = c_leafNameWithModuleNameInPrefix.get();
        Boolean oldIsContextSet = c_isContextSet.get();
        Boolean oldIsModuleNameAppendedWithLocalName = c_isModuleNameAppendedWithLocalName.get();

        try{

            c_matchCriteria.set(context.getMatchCriteria());
            c_matchCriteriaAcrossXPath.set(context.getMatchCriteriaAcrossXPath());
            c_schemaPathsInOrder.set(context.getSchemaPathsInOrder());
            c_leafNameWithModuleNameInPrefix.set(context.getLeafNameWithModuleNameInPrefix());
            c_isContextSet.set(Boolean.TRUE);
            c_isModuleNameAppendedWithLocalName.set(context.isModuleNameAppendedWithLocalName());
            c_modelNodeToAttributesLoadedWithMatchCriteriaMap.set(new HashMap<>());
            return template.run();

        }finally {

            c_matchCriteria.set(oldMatchCriteriaValue);
            c_matchCriteriaAcrossXPath.set(oldMatchCriteriaAcrossXPath);
            c_schemaPathsInOrder.set(oldSchemaPathsInOrderValue);
            c_leafNameWithModuleNameInPrefix.set(oldLeafWithModuleNameInPrefix);
            c_isContextSet.set(oldIsContextSet);
            c_isModuleNameAppendedWithLocalName.set(oldIsModuleNameAppendedWithLocalName);

            c_modelNodeToAttributesLoadedWithMatchCriteriaMap.get().keySet().forEach(dynaBean -> {
                Set<String> attributesLoadedWithMatchCriteria = c_modelNodeToAttributesLoadedWithMatchCriteriaMap.get().get(dynaBean);
                attributesLoadedWithMatchCriteria.forEach(attribute -> dynaBean.set(attribute, null));
            });
            c_modelNodeToAttributesLoadedWithMatchCriteriaMap.set(Collections.EMPTY_MAP);

        }
    }

    public interface ModelDynaBeanContextTemplate<T> {

        T run();
    }

}
