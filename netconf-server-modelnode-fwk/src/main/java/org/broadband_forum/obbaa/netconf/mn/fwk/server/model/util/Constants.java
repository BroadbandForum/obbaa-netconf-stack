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

public class Constants {
    public static final char EQUAL_TO_CHAR = '=';
    public static final char FORWARD_SLASH_CHAR = '/';
    public static final char BACKWARD_SLASH_CHAR = '\\';
    public static final String BACKWARD_SLASH_ENCODER = "\\\\";
    public static final String EQUAL_TO = "=";
    public static final String EQUAL_TO_ENCODER = "\\=";
    public static final String FORWARD_SLASH = "/";
    public static final String FORWARD_SLASH_ENCODER = "\\/";
    public static final String PLUS = "+";
    public static final String REGEX_FORWARD_SLASH_WITH_NO_SLASH_PREFIX = "(?<!\\\\)/";
    public static final String REGEX_EQUAL_TO_WITH_NO_SLASH_PREFIX = "(?<!\\\\)=";
}
