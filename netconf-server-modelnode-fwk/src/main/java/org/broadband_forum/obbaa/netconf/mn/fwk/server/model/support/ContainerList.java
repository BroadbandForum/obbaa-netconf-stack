package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be put on a getter method for an attribute
 *
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ContainerList {
	String name();
	String namespace();
	Class<? extends ModelNode> childClass();
	CreateStrategy childCreateStrategy() default CreateStrategy.factory;
    String childFactoryName();

    String revision()default "";
}
