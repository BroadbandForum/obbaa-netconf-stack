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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils;

import static org.apache.commons.codec.digest.Crypt.crypt;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.IanaCryptHashUtils.CRYPT_HASH;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.IanaCryptHashUtils.CRYPT_HASH_MD_5;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.IanaCryptHashUtils.CRYPT_HASH_SHA_256;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.IanaCryptHashUtils.CRYPT_HASH_SHA_512;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.IanaCryptHashUtils.IANA_CRYPT_HASH_NS;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.IanaCryptHashUtils.PLAIN_TEXT_PREFIX;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.utils.IanaCryptHashUtils.generateHashedValueIfTypeDefCryptHash;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifier;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifierImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

public class IanaCryptHashUtilsTest {

    public static final String PLAINTEXT = "Plaintext";
    public static final String PLAIN_TEXT_WITH_PREFIX = "$0$" + PLAINTEXT;
    public static final String SHA_512_PREFIX = "$6$";
    public static final String SHA_256_PREFIX = "$5$";
    public static final String MD5_PREFIX = "$1$";
    public static final String REVISION = "2014-08-06";
    private QName m_cryptHash;
    @Mock
    private SchemaRegistry m_schemaRegistry;
    private QName m_nonCryptHashIanaNS;
    private QName m_nonCryptHashDiffNS;
    @Mock
    private Module m_module;
    @Mock
    private TypeDefinition m_typeDef;
    private Set<QName> m_ianaFeatures;
    private Map<ModuleIdentifier, Set<QName>> m_supportedFeatures;
    private ModuleIdentifier m_moduleIdentifier;
    private QName m_cryptHashDiffNS;

    @Before
    public void setUp() throws URISyntaxException {
        initMocks(this);
        when(m_module.getName()).thenReturn("iana-crypt-hash");
        when(m_module.getNamespace()).thenReturn(new URI(IANA_CRYPT_HASH_NS));
        when(m_module.getRevision()).thenReturn(Optional.of(Revision.of(REVISION)));
        m_moduleIdentifier = ModuleIdentifierImpl.create(m_module);
        m_ianaFeatures = new HashSet<>();
        m_ianaFeatures.add(QName.create(IANA_CRYPT_HASH_NS, REVISION, CRYPT_HASH_SHA_512));
        m_ianaFeatures.add(QName.create(IANA_CRYPT_HASH_NS, REVISION, CRYPT_HASH_SHA_256));
        m_ianaFeatures.add(QName.create(IANA_CRYPT_HASH_NS, REVISION, CRYPT_HASH_MD_5));
        m_supportedFeatures = new HashMap<>();
        m_supportedFeatures.put(m_moduleIdentifier, m_ianaFeatures);
        m_cryptHash = QName.create(IANA_CRYPT_HASH_NS, REVISION, CRYPT_HASH);
        m_cryptHashDiffNS = QName.create("urn:ietf:params:xml:ns:yang:diff-namespace", "2013-06-05", CRYPT_HASH);
        m_nonCryptHashIanaNS = QName.create(IANA_CRYPT_HASH_NS, REVISION, "non-crypt-hash");
        m_nonCryptHashDiffNS = QName.create("urn:ietf:params:xml:ns:yang:diff-namespace", "2013-06-05", "non-crypt-hash");
        when(m_typeDef.getQName()).thenReturn(m_cryptHash);
        when(m_schemaRegistry.findModuleByNamespaceAndRevision(m_cryptHash.getNamespace(), m_cryptHash.getRevision().get())).thenReturn(Optional.of(m_module));
        when(m_schemaRegistry.getSupportedFeatures()).thenReturn(m_supportedFeatures);
    }

    @Test
    public void testGenerateHashIfCryptHashAndClearText() {
        //all 3 crypt-hash algo supported
        verifyClearTextIsHashed(SHA_512_PREFIX);

        //SHA-256 and MD5 supported
        m_ianaFeatures.remove(QName.create(IANA_CRYPT_HASH_NS, REVISION, CRYPT_HASH_SHA_512));
        verifyClearTextIsHashed(SHA_256_PREFIX);

        //only MD5 supported
        m_ianaFeatures.remove(QName.create(IANA_CRYPT_HASH_NS, REVISION, CRYPT_HASH_SHA_256));
        verifyClearTextIsHashed(MD5_PREFIX);

        //SHA-512 and SHA-256 supported
        m_ianaFeatures.clear();
        m_ianaFeatures.add(QName.create(IANA_CRYPT_HASH_NS, REVISION, CRYPT_HASH_SHA_512));
        m_ianaFeatures.add(QName.create(IANA_CRYPT_HASH_NS, REVISION, CRYPT_HASH_SHA_256));
        verifyClearTextIsHashed(SHA_512_PREFIX);

        //SHA-512 and MD5 supported
        m_ianaFeatures.clear();
        m_ianaFeatures.add(QName.create(IANA_CRYPT_HASH_NS, REVISION, CRYPT_HASH_SHA_512));
        m_ianaFeatures.add(QName.create(IANA_CRYPT_HASH_NS, REVISION, CRYPT_HASH_MD_5));
        verifyClearTextIsHashed(SHA_512_PREFIX);

        //SHA-512 only supported
        m_ianaFeatures.clear();
        m_ianaFeatures.add(QName.create(IANA_CRYPT_HASH_NS, REVISION, CRYPT_HASH_SHA_512));
        verifyClearTextIsHashed(SHA_512_PREFIX);

        //SHA-256 only supported
        m_ianaFeatures.clear();
        m_ianaFeatures.add(QName.create(IANA_CRYPT_HASH_NS, REVISION, CRYPT_HASH_SHA_256));
        verifyClearTextIsHashed(SHA_256_PREFIX);

        //None of the Algorithms supported
        m_ianaFeatures.clear();
        String hashedString = IanaCryptHashUtils.generateHashedValueIfTypeDefCryptHash(m_schemaRegistry, m_typeDef, PLAIN_TEXT_WITH_PREFIX);
        assertTrue(hashedString.startsWith(PLAIN_TEXT_PREFIX));
        assertEquals(PLAIN_TEXT_WITH_PREFIX, hashedString);

        //Crypt-hash algorithm other than SHA-512, SHA-256 or MD5
        m_ianaFeatures.clear();
        m_ianaFeatures.add(QName.create(IANA_CRYPT_HASH_NS, REVISION, "crypt-hash-unknown"));
        try {
            IanaCryptHashUtils.generateHashedValueIfTypeDefCryptHash(m_schemaRegistry, m_typeDef, PLAIN_TEXT_WITH_PREFIX);
            fail("Test should have failed");
        } catch (IllegalArgumentException e) {
            assertEquals("Crypt-hash algorithm/s not supported", e.getMessage());
        }
    }

    @Test
    public void testGenerateCryptHashForAlreadyHashedString() {
        //sending hashed password hashed with SHA-512
        String value = generateHashedValueIfTypeDefCryptHash(m_schemaRegistry, m_typeDef, "$6$5qE8Go66$reLR5vUdT7Go9t.JzI70ns7wQnaaTKCvaDyldcAhFsvBS9C6g8sNwB2g/oNbLo1n5ZfzmtL.56lEE8m3ffFYD0");
        assertEquals("$6$5qE8Go66$reLR5vUdT7Go9t.JzI70ns7wQnaaTKCvaDyldcAhFsvBS9C6g8sNwB2g/oNbLo1n5ZfzmtL.56lEE8m3ffFYD0", value);
        verifyZeroInteractions(m_schemaRegistry);

        //sending hashed password hashed with SHA-256
        value = generateHashedValueIfTypeDefCryptHash(m_schemaRegistry, m_typeDef, "$5$sWcVIVAQ$Gykrmqg703/quWDwDw/JtrtOfv8TYkTASftPdEPulv/");
        assertEquals("$5$sWcVIVAQ$Gykrmqg703/quWDwDw/JtrtOfv8TYkTASftPdEPulv/", value);
        verifyZeroInteractions(m_schemaRegistry);

        //sending hashed password hashed with MD5
        value = generateHashedValueIfTypeDefCryptHash(m_schemaRegistry, m_typeDef, "$1$gAK41fQX$zqinfQx4VIRAPU/cdicn3/");
        assertEquals("$1$gAK41fQX$zqinfQx4VIRAPU/cdicn3/", value);
        verifyZeroInteractions(m_schemaRegistry);
    }

    @Test
    public void testGenerateCryptHashForNonCryptHashType() {
        //when typedef is from iana-crypt-hash module but the type is not crypt-hash
        when(m_typeDef.getQName()).thenReturn(m_nonCryptHashIanaNS);
        String attributeValue = generateHashedValueIfTypeDefCryptHash(m_schemaRegistry, m_typeDef, PLAIN_TEXT_WITH_PREFIX);
        assertEquals(PLAIN_TEXT_WITH_PREFIX, attributeValue);
        verifyZeroInteractions(m_schemaRegistry);

        attributeValue = generateHashedValueIfTypeDefCryptHash(m_schemaRegistry, m_typeDef, "$6$5qE8Go66$reLR5vUdT7Go9t.JzI70ns7wQnaaTKCvaDyldcAhFsvBS9C6g8sNwB2g/oNbLo1n5ZfzmtL.56lEE8m3ffFYD0");
        assertEquals("$6$5qE8Go66$reLR5vUdT7Go9t.JzI70ns7wQnaaTKCvaDyldcAhFsvBS9C6g8sNwB2g/oNbLo1n5ZfzmtL.56lEE8m3ffFYD0", attributeValue);
        verifyZeroInteractions(m_schemaRegistry);

        attributeValue = generateHashedValueIfTypeDefCryptHash(m_schemaRegistry, m_typeDef, "non-crypt-hash-value iana module");
        assertEquals("non-crypt-hash-value iana module", attributeValue);
        verifyZeroInteractions(m_schemaRegistry);

        //when typedef not crypt-hash and non-iana-crypt-hash module
        when(m_typeDef.getQName()).thenReturn(m_nonCryptHashDiffNS);
        attributeValue = generateHashedValueIfTypeDefCryptHash(m_schemaRegistry, m_typeDef, PLAIN_TEXT_WITH_PREFIX);
        assertEquals(PLAIN_TEXT_WITH_PREFIX, attributeValue);
        verifyZeroInteractions(m_schemaRegistry);

        attributeValue = generateHashedValueIfTypeDefCryptHash(m_schemaRegistry, m_typeDef, "$5$sWcVIVAQ$Gykrmqg703/quWDwDw/JtrtOfv8TYkTASftPdEPulv/");
        assertEquals("$5$sWcVIVAQ$Gykrmqg703/quWDwDw/JtrtOfv8TYkTASftPdEPulv/", attributeValue);
        verifyZeroInteractions(m_schemaRegistry);

        attributeValue = generateHashedValueIfTypeDefCryptHash(m_schemaRegistry, m_typeDef, "non-crypt-hash-value diff module");
        assertEquals("non-crypt-hash-value diff module", attributeValue);
        verifyZeroInteractions(m_schemaRegistry);

        //when typedef crypt-hash but from non-iana-crypt-hash module
        when(m_typeDef.getQName()).thenReturn(m_cryptHashDiffNS);
        attributeValue = generateHashedValueIfTypeDefCryptHash(m_schemaRegistry, m_typeDef, PLAIN_TEXT_WITH_PREFIX);
        assertEquals(PLAIN_TEXT_WITH_PREFIX, attributeValue);
        verifyZeroInteractions(m_schemaRegistry);

        attributeValue = generateHashedValueIfTypeDefCryptHash(m_schemaRegistry, m_typeDef, "$1$gAK41fQX$zqinfQx4VIRAPU/cdicn3/");
        assertEquals("$1$gAK41fQX$zqinfQx4VIRAPU/cdicn3/", attributeValue);
        verifyZeroInteractions(m_schemaRegistry);

        attributeValue = generateHashedValueIfTypeDefCryptHash(m_schemaRegistry, m_typeDef, "crypt-hash-value diff NS");
        assertEquals("crypt-hash-value diff NS", attributeValue);
        verifyZeroInteractions(m_schemaRegistry);
    }

    @Test
    public void testWhenLeafHasCryptHashInMultiLayer() {
        TypeDefinition baseType = mock(TypeDefinition.class);
        when(m_typeDef.getQName()).thenReturn(m_nonCryptHashDiffNS);
        when(m_typeDef.getBaseType()).thenReturn(baseType);
        when(baseType.getQName()).thenReturn(m_cryptHash);
        String hashedString = generateHashedValueIfTypeDefCryptHash(m_schemaRegistry, m_typeDef, PLAIN_TEXT_WITH_PREFIX);
        assertTrue(hashedString.startsWith(SHA_512_PREFIX));
        assertEquals(crypt(PLAINTEXT, hashedString.substring(0, hashedString.lastIndexOf('$'))), hashedString);
    }

    @Test
    public void testWhenCryptHashModuleAbsent() {
        when(m_schemaRegistry.findModuleByNamespaceAndRevision(m_cryptHash.getNamespace(), m_cryptHash.getRevision().get())).thenReturn(Optional.empty());
        String hashedString = IanaCryptHashUtils.generateHashedValueIfTypeDefCryptHash(m_schemaRegistry, m_typeDef, PLAIN_TEXT_WITH_PREFIX);
        assertTrue(hashedString.startsWith(PLAIN_TEXT_PREFIX));
        assertEquals(PLAIN_TEXT_WITH_PREFIX, hashedString);
    }

    private void verifyClearTextIsHashed(String expectedPrefix) {
        String hashedString = IanaCryptHashUtils.generateHashedValueIfTypeDefCryptHash(m_schemaRegistry, m_typeDef, PLAIN_TEXT_WITH_PREFIX);
        assertTrue(hashedString.startsWith(expectedPrefix));
        assertEquals(crypt(PLAINTEXT, hashedString.substring(0, hashedString.lastIndexOf('$'))), hashedString);
    }
}