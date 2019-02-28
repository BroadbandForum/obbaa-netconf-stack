package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.ModelNodeDSMRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.datastore.utils.AnnotationAnalysisException;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;

/**
 * Created by keshava on 6/12/15.
 */
public interface EntityRegistry {

    QName getQName(Class aClass);

    void addSchemaPaths(String componentId, Map<SchemaPath, Class> schemaPaths);
    
    void addComponentClass(String componentId, Class klass);

    Class getEntityClass(SchemaPath schemaPath);

    void addConfigAttributeGetters(Class klass, Map<QName, Method> configAttributeGetters, Map<QName, String> fieldNames);
    Map<QName, Method> getAttributeGetters(Class aClass);
    String getFieldName(Class klass, QName qName);

    void addConfigAttributeSetters(Class klass, Map<QName, Method> configAttributeSetters, Method parentIdSetter);
    Map<QName, Method> getAttributeSetters(Class aClass);
    Method getParentIdSetter(Class klass);

    void addParentIdGetter(Class subrootClass, Method parentIdGetter);
    Method getParentIdGetter(Class klass);

    void addYangSchemaPathGetter(Class subrootClass, Method schemaPathGetter);
    Method getSchemaPathGetter(Class klass);

    void addYangSchemaPathSetter(Class subrootClass, Method schemaPathSetter);
    Method getSchemaPathSetter(Class klass);

    void addYangChildGetters(Class subrootClass, Map<QName, Method> yangChildGetters);
    Map<QName,Method> getYangChildGetters(Class<?> aClass);

    void addYangChildSetters(Class subrootClass, Map<QName, Method> yangChildSetters);
    Map<QName,Method> getYangChildSetters(Class klass);

    void addYangXmlSubtreeGetter(Class klass, Method yangXmlSubtreeGetter);
    Method getYangXmlSubtreeGetter(Class klass);

    void addYangXmlSubtreeSetter(Class klass, Method yangXmlSubtreeSetter);
    Method getYangXmlSubtreeSetter(Class klass);

    void addYangLeafListGetters(Class subrootClass, Map<QName, Method> yangLeafListGetters);
    Map<QName, Method> getYangLeafListGetters(Class klass);

    void addYangLeafListSetters(Class subrootClass, Map<QName, Method> yangLeafListSetters);
    Map<QName, Method> getYangLeafListSetters(Class klass);
    
    void addBigListType(Class klass, boolean bigListType);
    boolean getBigListType(Class klass);

    void undeploy(String componentId);

    void updateRegistry(String componentId, List<Class> classes, SchemaRegistry schemaRegistry, EntityDataStoreManager entityDSM, ModelNodeDSMRegistry modelNodeDSMRegistry) throws AnnotationAnalysisException;

	void addOrderByUserGetter(Class subrootClass, Method orderByUserGetter);
	Method getOrderByUserGetter(Class klass);

	void addOrderByUserSetter(Class subrootClass, Method orderByUserSetter);
	Method getOrderByUserSetter(Class klass);

	String getOrderByFieldName(Class entityClass);

    String getYangParentIdFieldName(Class entityClass);

    void addYangAttributeNSGetters(Class klass, Map<QName, Method> yangAttributeNSGetters);
    Map<QName, Method> getYangAttributeNSGetters(Class klass);

    void addYangAttributeNSSetters(Class klass, Map<QName, Method> yangAttributeNSSetters);
    Map<QName, Method> getYangAttributeNSSetters(Class klass);

    void addJpaAttributesInfo(Class klass, Map<QName, String> attributesInfo);
    Map<QName, String> getJpaAttributesInfo(Class klass);

    void addClassWithYangParentSchemaPathAnnotation(String componentId, Class klass);

    boolean classHasYangParentSchemaPathAnnotation(Class<?> klass);
}
