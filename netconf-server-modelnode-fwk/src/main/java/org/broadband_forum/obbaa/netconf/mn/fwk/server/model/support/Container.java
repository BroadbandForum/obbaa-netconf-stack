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
@Target(ElementType.TYPE)
public @interface Container {

	public String name();

	public String namespace();

    public String revision() default "";

}
