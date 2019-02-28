package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

public class StringUtil {
    private static final String EMPTY_STR = "";
	private static final String GET = "get";
	private static final String IS = "is";

	public static String blanks(int spaces) {
		StringBuffer outputBuffer = new StringBuffer(spaces);
		for (int i = 0; i < spaces; i++) {
			outputBuffer.append(" ");
		}
		return outputBuffer.toString();
	}

	public static String removeGet(String name) {
		return name.replace(GET, EMPTY_STR);
	}

	public static String removeIs(String name) {
		return name.replace(IS, EMPTY_STR);
	}

	public static String capitalizeFirstCharacter(String str) {
		if (str == null || str.length() == 0) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}
}
