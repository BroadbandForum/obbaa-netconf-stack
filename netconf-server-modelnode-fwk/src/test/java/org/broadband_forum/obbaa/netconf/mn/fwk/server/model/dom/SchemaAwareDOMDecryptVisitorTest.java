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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.dom;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.YangUtils.loadAsXml;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collections;

import org.broadband_forum.obbaa.netconf.api.util.CryptUtil2;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Element;

@RunWith(RequestScopeJunitRunner.class)
public class SchemaAwareDOMDecryptVisitorTest {

    private SchemaAwareDOMDecryptVisitor m_visitor;
    private SchemaRegistry m_schemaRegistry;
    private Element m_root;

    @Before
    public void setUp() throws Exception {
        String keyFilePath = getClass().getResource("/domvisitortest/keyfile.plain").getPath();
        CryptUtil2 cryptUtil2 = new CryptUtil2();
        cryptUtil2.setKeyFilePathForTest(keyFilePath);
        cryptUtil2.initFile();
        m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.loadSchemaContext(JukeboxConstants.JUKEBOX_LOCAL_NAME, TestUtil.getJukeBoxYangs(), Collections.emptySet(), Collections.emptyMap());
    }

    @Test
    public void testAllPasswordsAreDecrypted() throws Exception {
        traverseAndVerifyDecryptedPasswords("/domvisitortest/encrypted-passwords.xml", "/domvisitortest/decrpyted-passwords.xml", 6);
    }

    private void traverseAndVerifyDecryptedPasswords(String encryptedXmlPath,
                                                     String decryptedXmlPath, int expectedNoOfInvocations) throws Exception {
        String rootXmlPath = TestUtil.class.getResource(encryptedXmlPath).getPath();
        m_root = loadAsXml(rootXmlPath);

        String resultXmlPath = TestUtil.class.getResource(decryptedXmlPath).getPath();
        Element expectedDecryptedXml = loadAsXml(resultXmlPath);

        m_visitor = spy(new SchemaAwareDOMDecryptVisitor(m_root, m_schemaRegistry));
        Element actualDecryptedElement = m_visitor.traverse();
        assertXMLEquals(expectedDecryptedXml, actualDecryptedElement);
        verify(m_visitor,times(expectedNoOfInvocations)).decrypt(any(Element.class));
    }
}
