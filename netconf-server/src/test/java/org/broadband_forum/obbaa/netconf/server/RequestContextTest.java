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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RequestContextTest {

    private RequestContext m_requestContext;
    private UserContext m_loggedInUser;
    private UserContext m_additionalUser;

    @Before
    public void init() {
        m_requestContext = new RequestContext(RequestCategory.BACKGROUND);
        RequestContext.clearUserCtxtTLs();
        assertUserContextsNotSet();
        m_loggedInUser = new UserContext("Kuncha", "session1");
        m_additionalUser = new UserContext("Dhooma", "session2");
    }
    
    @Test
    public void testGetSetRequestCategory() {
        assertEquals(RequestCategory.BACKGROUND, m_requestContext.getRequestCategory());
        m_requestContext.setRequestCategory(RequestCategory.GUI);
        assertEquals(RequestCategory.GUI, m_requestContext.getRequestCategory());
        m_requestContext.setRequestCategory(RequestCategory.NBI);
        assertEquals(RequestCategory.NBI, m_requestContext.getRequestCategory());
    }

    @Test
    public void testUserContextTLs(){
        assertUserContextsNotSet();

        RequestContext.setLoggedInUserCtxtTL(m_loggedInUser);
        assertEquals(new UserContext("Kuncha", "session1"), RequestContext.getLoggedInUserCtxtTL());
        assertNotEquals(new UserContext("Kuncha2", "session1"), RequestContext.getLoggedInUserCtxtTL());
        assertNotEquals(new UserContext("Kuncha", "session2"), RequestContext.getLoggedInUserCtxtTL());
        assertNull(RequestContext.getAdditionalUserCtxtTL());

        RequestContext.setAdditionalUserCtxtTL(m_additionalUser);
        assertEquals(m_loggedInUser, RequestContext.getLoggedInUserCtxtTL());
        assertEquals(m_additionalUser, RequestContext.getAdditionalUserCtxtTL());

        RequestContext.clearUserCtxtTLs();
        assertUserContextsNotSet();
    }

    @Test
    public void testByPassedContext() {
        assertFalse(RequestContext.isByPassed());
        RequestContext.enableIsByPass();
        assertTrue(RequestContext.isByPassed());

        assertNull(RequestContext.getByPassPermissions());
        RequestContext.setByPassPermissions(Sets.newHashSet("permission"));
        assertEquals(1, RequestContext.getByPassPermissions().size());
    }

    private void assertUserContextsNotSet() {
        assertNull(RequestContext.getLoggedInUserCtxtTL());
        assertNull(RequestContext.getAdditionalUserCtxtTL());
    }

    @After
    public void destroy() {
        m_requestContext = null;
        RequestContext.clearUserCtxtTLs();
        RequestContext.reset();
    }
}
