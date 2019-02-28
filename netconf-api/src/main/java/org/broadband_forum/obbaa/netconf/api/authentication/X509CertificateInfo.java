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

package org.broadband_forum.obbaa.netconf.api.authentication;

import java.io.File;

/**
 * This class holds information that is typically needed for TLS peer authentication.
 * 
 *
 * 
 */
public class X509CertificateInfo {
    private final File m_certificateChain;
    private final File m_trustChain;
    private final File m_privateKey;
    private final String m_privateKeyPassword;

    /**
     * Creates a X509CertificateInfo object.
     * 
     * @param certificateChain - The X.509 Certificate chain used to authenticate self.
     * @param trustChain - The CA public key chain used to verify CA signatures on the peer's X.509 certificate.
     * @param privateKey - The private key file.
     * @param privateKeyPassword - The password for the private key file, set to null if the file is not password protected.
     */
    public X509CertificateInfo(File certificateChain, File trustChain, File privateKey, String privateKeyPassword) {
        m_certificateChain = certificateChain;
        m_trustChain = trustChain;
        m_privateKey = privateKey;
        m_privateKeyPassword = privateKeyPassword;
    }

    /**
     * The X.509 Certificate chain used to authenticate self.
     * 
     * @return
     */
    public File getCertificateChain() {
        return m_certificateChain;
    }

    /**
     * The CA public key chain used to verify CA signatures on the peer's X.509 certificate.
     * 
     * @return
     */
    public File getTrustChain() {
        return m_trustChain;
    }

    /**
     * The private key file.
     * 
     * @return
     */
    public File getPrivateKey() {
        return m_privateKey;
    }

    /**
     * The password for the private key file, set to null if the file is not password protected.
     * 
     * @return
     */
    public String getPrivateKeyPassword() {
        return m_privateKeyPassword;
    }

}
