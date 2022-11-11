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

import java.util.Optional;
import java.util.Set;

import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.codec.digest.Sha2Crypt;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ModuleIdentifierImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

public class IanaCryptHashUtils {

    public static final String PLAIN_TEXT_PREFIX = "$0$";
    public static final String CRYPT_HASH = "crypt-hash";
    public static final String IANA_CRYPT_HASH_NS = "urn:ietf:params:xml:ns:yang:iana-crypt-hash";
    public static final String CRYPT_HASH_SHA_512 = "crypt-hash-sha-512";
    public static final String CRYPT_HASH_SHA_256 = "crypt-hash-sha-256";
    public static final String CRYPT_HASH_MD_5 = "crypt-hash-md5";

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(IanaCryptHashUtils.class, LogAppNames.NETCONF_STACK);

    public static boolean isPlainText(String value) {
        return value.startsWith(PLAIN_TEXT_PREFIX);
    }

    private static TypeDefinition getCryptHashTypeDefIfPresent(TypeDefinition typeDefinition) {
        if(typeDefinition == null) {
            return null;
        } else if ((((CRYPT_HASH.equals(typeDefinition.getQName().getLocalName())) && IANA_CRYPT_HASH_NS.equals(typeDefinition.getQName().getNamespace().toString())))) {
            return typeDefinition;
        } else {
            return getCryptHashTypeDefIfPresent(typeDefinition.getBaseType());
        }
    }

    private static Set<QName> getCryptHashFeaturesSupported(SchemaRegistry schemaRegistry, TypeDefinition typeDefinition) {
        QName typeDefinitionQName = typeDefinition.getQName();
        Optional<Module> cryptHashModule= schemaRegistry.findModuleByNamespaceAndRevision(typeDefinitionQName.getNamespace(), typeDefinitionQName.getRevision().orElse(null));
        if (!cryptHashModule.isPresent()) {
            LOGGER.warn("Iana module with namespace {} and revision {} not found", typeDefinitionQName.getNamespace(), typeDefinitionQName.getRevision());
            return null;
        }
        return schemaRegistry.getSupportedFeatures().get(ModuleIdentifierImpl.create(cryptHashModule.get()));
    }

    private static String generateHashedValueForClearText(SchemaRegistry schemaRegistry, TypeDefinition typeDef, String plainText) {
        Set<QName> cryptHashFeatures = getCryptHashFeaturesSupported(schemaRegistry, typeDef);
        if (cryptHashFeatures != null && cryptHashFeatures.size() > 0) {
            plainText = plainText.replace("$0$", "");
            if (cryptHashFeatures.contains(QName.create(typeDef.getQName().getNamespace(), typeDef.getQName().getRevision(), CRYPT_HASH_SHA_512))) {
                return Sha2Crypt.sha512Crypt(plainText.getBytes());
            } else if (cryptHashFeatures.contains(QName.create(typeDef.getQName().getNamespace(), typeDef.getQName().getRevision(), CRYPT_HASH_SHA_256))) {
                return Sha2Crypt.sha256Crypt(plainText.getBytes());
            } else if (cryptHashFeatures.contains(QName.create(typeDef.getQName().getNamespace(), typeDef.getQName().getRevision(), CRYPT_HASH_MD_5))) {
                return Md5Crypt.md5Crypt(plainText.getBytes());
            } else {
                throw new IllegalArgumentException("Crypt-hash algorithm/s not supported");
            }
        }
        LOGGER.warn("Either the IANA module is not found OR the IANA module does not support any of the features for crypt-hash. Hence not converting clear text to hash");
        return plainText;
    }

    public static String generateHashedValueIfTypeDefCryptHash(SchemaRegistry schemaRegistry, TypeDefinition typeDefinition, String attributeValue) {
        TypeDefinition td = getCryptHashTypeDefIfPresent(typeDefinition);
        if(td != null) {
            if(isPlainText(attributeValue)){
                return generateHashedValueForClearText(schemaRegistry, td, attributeValue);
            }
        } return attributeValue;
    }
}
