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

package org.broadband_forum.obbaa.netconf.api.x509certificates;

import org.broadband_forum.obbaa.netconf.api.utils.PemReader;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509KeyManager;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * DynamicX509KeyManagerImpl is a X509ExtendedKeyManager that has capability of reloading the private keys.
 * One can call DynamicX509KeyManagerImpl#initKeyManager() with the new List of certificates and private key to
 * re-align the key manager.
 * Created by keshava on 4/29/15.
 */
public class DynamicX509KeyManagerImpl extends DynamicX509KeyManager {
    public static final String SUN_X509 = "SunX509";
    public static final String JKS = "JKS";
    public static final String SSL_KEY_MANAGER_FACTORY_ALGORITHM = "ssl.KeyManagerFactory.algorithm";
    private KeyManagerFactory m_innerKeyManagerFactory = null;
    private X509KeyManager m_innerKeyManager = null;

    private static final Logger LOGGER = Logger.getLogger(DynamicX509KeyManagerImpl.class);

    public DynamicX509KeyManagerImpl(List<String> keyCertificateChain, PrivateKeyInfo privateKeyInfo) throws
            KeyManagerInitException {
        initKeyManager(keyCertificateChain, privateKeyInfo);
    }

    public DynamicX509KeyManagerImpl(String certificateChainFilePath, String privateKeyFilePath, String privateKeyPass)
            throws KeyManagerInitException {
        initKeyManager(certificateChainFilePath, privateKeyFilePath, privateKeyPass);
    }

    public DynamicX509KeyManagerImpl(File certificateChainFile, File privateKeyFile, String privateKeyPassword)
            throws KeyManagerInitException {

        try {
            List<String> certificates = CertificateUtil.certificateStringsFromFile(certificateChainFile);
            String privateKeyString = PemReader.stripPKDelimiters(FileUtils.readFileToString(privateKeyFile));
            PrivateKeyInfo privateKeyInfo = new PrivateKeyInfo(privateKeyString, privateKeyPassword);
            initKeyManager(certificates, privateKeyInfo);
        } catch (CertificateException | IOException e) {
            throw new KeyManagerInitException(e);
        }

    }

    @Override
    public synchronized void initKeyManager(List<String> certificateChain, PrivateKeyInfo privateKeyInfo) throws
            KeyManagerInitException {
        if (privateKeyInfo != null && !certificateChain.isEmpty()) {
            KeyStore ks = null;
            try {
                String algorithm = Security.getProperty(SSL_KEY_MANAGER_FACTORY_ALGORITHM);
                if (algorithm == null) {
                    algorithm = SUN_X509;
                }
                m_innerKeyManagerFactory = KeyManagerFactory.getInstance(algorithm);
                ks = KeyStore.getInstance(JKS);
                ks.load(null, null);
                PrivateKey key = KeyUtil.getPrivateKey(PemReader.stripPKDelimiters(privateKeyInfo.getPrivateKeyString
                        ()), privateKeyInfo
                        .getPrivateKeyPassword());
                List<X509Certificate> certChain = CertificateUtil.getX509Certificates(CertificateUtil
                        .getByteArrayCertificates(CertificateUtil.stripDelimiters(certificateChain)));
                ks.setKeyEntry("key", key, privateKeyInfo.getPrivateKeyPasswordChars(),
                        certChain.toArray(new java.security.cert.Certificate[certChain.size()]));
                m_innerKeyManagerFactory.init(ks, privateKeyInfo.getPrivateKeyPasswordChars());
                m_innerKeyManager = (X509KeyManager) m_innerKeyManagerFactory.getKeyManagers()[0];
            } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException |
                    UnrecoverableKeyException e) {
                throw new KeyManagerInitException(e);
            } catch (KeyException e) {
                throw new KeyManagerInitException(KeyManagerInitException.INVALID_PRIVATE_KEY, e);
            }
        } else {
            m_innerKeyManager = null;
        }
    }

    @Override
    public void initKeyManager(String certificateChainFilePath, String privateKeyFilePath, String privateKeyPassWord)
            throws KeyManagerInitException {
        try {
            File certificateChainFile = new File(certificateChainFilePath);
            File privateKeyFile = new File(privateKeyFilePath);

            if (certificateChainFile.isFile() && privateKeyFile.isFile()) {
                List<String> certificates = CertificateUtil.certificateStringsFromFile(certificateChainFile);
                String privateKeyString = PemReader.stripPKDelimiters(FileUtils.readFileToString(privateKeyFile));
                PrivateKeyInfo privateKeyInfo = new PrivateKeyInfo(privateKeyString, privateKeyPassWord);
                initKeyManager(certificates, privateKeyInfo);
            } else {
                LOGGER.error("CertificateChain file or PrivateKey file not found " + certificateChainFilePath + " - "
                        + privateKeyFilePath);
            }
        } catch (CertificateException | IOException e) {
            throw new KeyManagerInitException(e);
        }
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        if (m_innerKeyManager == null) {
            // If the key manager is not initialised with key and OR certificates, then we don't have an alias
            return null;
        }
        return m_innerKeyManager.getClientAliases(keyType, issuers);
    }

    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        if (m_innerKeyManager == null) {
            // If the key manager is not initialised with key and OR certificates, then we don't have an alias
            return null;
        }
        return m_innerKeyManager.chooseClientAlias(keyType, issuers, socket);
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        if (m_innerKeyManager == null) {
            // If the key manager is not initialised with key and OR certificates, then we don't have an alias
            return null;
        }
        return m_innerKeyManager.getServerAliases(keyType, issuers);
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        if (m_innerKeyManager == null) {
            // If the key manager is not initialised with key and OR certificates, then we don't have an alias
            return null;
        }
        return m_innerKeyManager.chooseServerAlias(keyType, issuers, socket);
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        if (m_innerKeyManager == null) {
            // If the key manager is not initialised with key and OR certificates, then we don't have a certificate
            // chain
            return null;
        }
        return m_innerKeyManager.getCertificateChain(alias);
    }

    public String chooseEngineClientAlias(String[] keyType, Principal[] issuers, SSLEngine engine) {
        return this.chooseClientAlias(keyType, issuers, (Socket) null);
    }

    public String chooseEngineServerAlias(String keyType, Principal[] issuers, SSLEngine engine) {
        return this.chooseServerAlias(keyType, issuers, (Socket) null);
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        if (m_innerKeyManager == null) {
            // If the key manager is not initialised with key and OR certificates, then we don't have a private key
            // m_logger.error()
            return null;
        }
        return m_innerKeyManager.getPrivateKey(alias);
    }

}
