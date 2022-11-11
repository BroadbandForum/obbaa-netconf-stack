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

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;

public class SystemPropertyUtilsUserTest {
    public SystemPropertyUtils m_systemPropertyUtils;
    private SystemPropertyUtils m_originalUtil;

    @Before
    public void setUpMockUtils(){
        m_systemPropertyUtils = mock(SystemPropertyUtils.class);
        m_originalUtil = SystemPropertyUtils.getInstance();
        SystemPropertyUtils.setInstance(m_systemPropertyUtils);
    }

    public void mockPropertyUtils(String key, String value) {
        when(m_systemPropertyUtils.getFromEnvOrSysProperty(key)).thenReturn(value);
        when(m_systemPropertyUtils.getFromEnvOrSysProperty(eq(key), anyString())).thenReturn(value);
        when(m_systemPropertyUtils.readFromEnvOrSysProperty(key)).thenReturn(value);
    }

    @After
    public void tearDownMockUtils(){
        SystemPropertyUtils.setInstance(m_originalUtil);
    }
}
