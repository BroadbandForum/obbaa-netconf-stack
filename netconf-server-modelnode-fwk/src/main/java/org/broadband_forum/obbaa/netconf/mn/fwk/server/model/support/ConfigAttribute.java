package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support;

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
public @interface ConfigAttribute {
	String name();
	String namespace();
    String revision()default "";
	boolean isKey() default false;
}
