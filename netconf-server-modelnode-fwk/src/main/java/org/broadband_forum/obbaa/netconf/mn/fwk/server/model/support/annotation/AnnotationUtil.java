package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AnnotationUtil {


	private static final String SET = "set";
	private static final String GET = "get";

	public static String getSetMethodName(Method m) {
		String setMethodStr;
		if (!m.getReturnType().equals(boolean.class)) {
			setMethodStr = SET + StringUtil.removeGet(m.getName());
		} else {
			setMethodStr = SET + StringUtil.removeIs(m.getName());
		}
		return setMethodStr;
	}

	public static Method getSetMethod(Class klass, Field field) throws NoSuchMethodException {
		String fieldName = field.getName();
		String setMethodName = SET + StringUtil.capitalizeFirstCharacter(fieldName);
		return klass.getDeclaredMethod(setMethodName, field.getType());
	}

	public static Method getGetMethod(Class klass, Field field) throws NoSuchMethodException {
		String fieldName = field.getName();
		String getMethodName = GET + StringUtil.capitalizeFirstCharacter(fieldName);
		return klass.getDeclaredMethod(getMethodName);
	}
}
