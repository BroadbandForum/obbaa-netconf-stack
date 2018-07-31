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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotEquals;

import org.junit.Before;
import org.junit.Test;

public class EditInfoTest {
    private EditInfo m_editInfo;
    private EditInfo m_editInfo2;
    private ChangedLeafInfo m_changedLeafInfo;
    private List<ChangedLeafInfo> m_changedLeafNodes;

    @Before
    public void initialize() {
        m_editInfo = new EditInfo();
        m_editInfo2 = new EditInfo();
        m_changedLeafInfo = new ChangedLeafInfo();
        m_changedLeafNodes = new ArrayList<>();
    }

    @Test
    public void testHashCode() {
        m_changedLeafNodes.clear();
        m_editInfo2.setChangedLeafInfos(m_changedLeafNodes);
        assertEquals(m_editInfo.hashCode(), m_editInfo2.hashCode());
        m_changedLeafNodes.add(m_changedLeafInfo);
        m_editInfo2.setChangedLeafInfos(m_changedLeafNodes);
        assertNotEquals(m_editInfo.hashCode(), m_editInfo2.hashCode());
    }

    @Test
    public void testEquals() {
        m_changedLeafNodes.clear();
        m_editInfo2.setChangedLeafInfos(m_changedLeafNodes);
        assertTrue(m_editInfo.equals(m_editInfo2));
        m_changedLeafNodes.add(m_changedLeafInfo);
        m_editInfo2.setChangedLeafInfos(m_changedLeafNodes);
        assertFalse(m_editInfo.equals(m_editInfo2));
    }

}
