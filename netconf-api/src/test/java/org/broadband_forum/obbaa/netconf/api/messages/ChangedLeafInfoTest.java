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

package org.broadband_forum.obbaa.netconf.api.messages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;

import org.junit.Before;
import org.junit.Test;

public class ChangedLeafInfoTest {

    private static final String TEST_PREFIX = "testPrefix";
    private static final String TEST_NAMESPACE = "testNamespace";
    private static final String TEST_CHANGED_VALUE = "testChangedValue";
    private static final String TEST_NAME = "testName";
    private static final String TEST_NAME2 = "testName2";
    private ChangedLeafInfo m_changedLeafInfo;
    private ChangedLeafInfo m_changedLeafInfo2;

    @Before
    public void initialize() {
        m_changedLeafInfo = new ChangedLeafInfo();
        m_changedLeafInfo.setName(TEST_NAME);
        m_changedLeafInfo.setChangedValue(TEST_CHANGED_VALUE);
        m_changedLeafInfo.setNamespace(TEST_NAMESPACE);
        m_changedLeafInfo.setPrefix(TEST_PREFIX);
        m_changedLeafInfo2 = new ChangedLeafInfo(TEST_NAME, TEST_CHANGED_VALUE, TEST_NAMESPACE, TEST_PREFIX);
    }

    @Test
    public void testEquals() {
        m_changedLeafInfo.setName(TEST_NAME);
        assertTrue(m_changedLeafInfo.equals(m_changedLeafInfo2));
        m_changedLeafInfo.setName(TEST_NAME2);
        assertFalse(m_changedLeafInfo.equals(m_changedLeafInfo2));
    }

    @Test
    public void testHashCode() {
        m_changedLeafInfo.setName(TEST_NAME);
        assertEquals(m_changedLeafInfo.hashCode(), m_changedLeafInfo2.hashCode());
        m_changedLeafInfo.setName(TEST_NAME2);
        assertNotEquals(m_changedLeafInfo.hashCode(), m_changedLeafInfo2.hashCode());
    }

}
