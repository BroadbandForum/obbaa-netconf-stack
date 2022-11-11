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
