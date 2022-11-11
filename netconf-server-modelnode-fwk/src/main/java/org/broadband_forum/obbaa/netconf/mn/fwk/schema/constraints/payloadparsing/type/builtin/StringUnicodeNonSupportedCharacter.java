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

package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.type.builtin;

import java.util.ArrayList;

import org.w3c.dom.Element;

public class StringUnicodeNonSupportedCharacter {

	public static ArrayList<String> UNICODE_NON_CHARACTER_LIST = new ArrayList<String>();
	
	/**
	 * As per RFC section 14-Yang-String, List of Excluded non-characters that are not allowed in YANG.
	 * 
	 * This means that the below list consists of characters that are valid in XML but invalid for YANG
	 * since the XML parser will only block characters that are not allowed in XML.
	 */
	
	static {
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDD0))); 
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDD1)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDD2)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDD3)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDD4)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDD5)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDD6)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDD7)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDD8)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDD9)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDDA)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDDB)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDDC)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDDD)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDDE)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDDF)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDE0)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDE1)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDE2)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDE3)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDE4)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDE5)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDE6)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDE7)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDE8)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDE9)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDEA)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDEB)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDEC)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDED)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDEE)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0xFDEF)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x1FFFE)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x1FFFF)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x2FFFE)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x2FFFF)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x3FFFE)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x3FFFF)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x4FFFE)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x4FFFF)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x5FFFE)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x5FFFF)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x6FFFE)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x6FFFF)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x7FFFE)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x7FFFF)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x8FFFE)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x8FFFF)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x9FFFE)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x9FFFF)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x10FFFE)));
		UNICODE_NON_CHARACTER_LIST.add(new String(Character.toChars(0x10FFFF)));
	}

	public static boolean containForbiddenCharacter(Element element) {
		String xmlString = element.getTextContent();
		for (String unicodeNoncharacter : UNICODE_NON_CHARACTER_LIST) {
			if (xmlString.contains(unicodeNoncharacter)) {
				return true;
			}
		}
		return false;
	}	
}
