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

package org.broadband_forum.obbaa.netconf.api.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;

import org.junit.Test;

public class FileUtilTest {

	private static final String IETF_YANG_PATH = "/yangs/ietf/ietf-yang-types.yang";

	private static String m_loadAsString = null;
	private static String m_loadFileString = null;

	@Test
	public void testLoadFileAsString() throws FileNotFoundException {
		m_loadFileString = FileUtil.loadFileAsString(FileUtilTest.class.getResource(IETF_YANG_PATH).getPath());
		assertNotNull(m_loadFileString);
	}

	@Test
	public void testLoadAsString() {
		m_loadAsString = FileUtil.loadAsString(IETF_YANG_PATH);
		assertNotNull(m_loadAsString);
		assertEquals(m_loadFileString,m_loadAsString);
	}

}
