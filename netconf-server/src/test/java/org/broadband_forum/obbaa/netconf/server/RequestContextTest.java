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

package org.broadband_forum.obbaa.netconf.server;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RequestContextTest {

    private RequestContext m_requestContext;
    
    @Before
    public void init() {
        m_requestContext = new RequestContext(RequestCategory.BACKGROUND);
    }
    
    @Test
    public void testGetSetRequestCategory() {
        assertEquals(RequestCategory.BACKGROUND, m_requestContext.getRequestCategory());
        m_requestContext.setRequestCategory(RequestCategory.GUI);
        assertEquals(RequestCategory.GUI, m_requestContext.getRequestCategory());
        m_requestContext.setRequestCategory(RequestCategory.NBI);
        assertEquals(RequestCategory.NBI, m_requestContext.getRequestCategory());
    }
    
    @After
    public void destroy() {
        m_requestContext = null;
    }
}
