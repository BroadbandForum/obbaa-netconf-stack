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

package org.broadband_forum.obbaa.netconf.client.ssh.auth;

import org.broadband_forum.obbaa.netconf.api.client.authentication.LoginKey;
import org.broadband_forum.obbaa.netconf.api.utils.PemReader;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.util.CharsetUtil;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

/**
 * This class gives the API to read and write the Keypair in PEM format.
 * 
 *
 * 
 */
public class SshHostKeyUtil {

    /**
     * This method reads the public and private key from the file(PEM format).
     * 
     * @param loginKey - This object should contain the public and private key file name.
     * 
     */
    public static KeyPair doReadKeyPair(LoginKey loginKey) throws Exception {

        URL filePathPubKey = Thread.currentThread().getContextClassLoader().getResource(loginKey.getPubKeyFile());
        FileInputStream fisPub = new FileInputStream(filePathPubKey.getPath());
        List<PublicKey> pubKey = PemReader.readPublicKey(fisPub);

        URL filePathPrvKey = Thread.currentThread().getContextClassLoader().getResource(loginKey.getPrivKeyFile());
        FileInputStream fisPri = new FileInputStream(filePathPrvKey.getPath());
        PrivateKey privKey = PemReader.readPrivateKey(fisPri);

        return new KeyPair(pubKey.get(0), privKey);
    }

    public List<PublicKey> readPublicKey(InputStream is) throws Exception {
        return PemReader.readPublicKey(is);
    }

    public PrivateKey readPrivateKey(InputStream is) throws Exception {
        return PemReader.readPrivateKey(is);
    }

    /**
     * This method writes the public and private key to the file in PEM format.
     */
    public static void doWriteKeyPair(KeyPair paramKeyPair, OutputStream paramOutputStream) throws Exception {

        String pubKeyText = "-----BEGIN PUBLIC KEY-----\n"
                + Base64.encode(Unpooled.wrappedBuffer(paramKeyPair.getPublic().getEncoded()), true).toString(CharsetUtil.US_ASCII)
                + "\n-----END PUBLIC KEY-----\n";

        paramOutputStream.write(pubKeyText.getBytes(CharsetUtil.US_ASCII));

        String privKeyText = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.encode(Unpooled.wrappedBuffer(paramKeyPair.getPrivate().getEncoded()), true).toString(CharsetUtil.US_ASCII)
                + "\n-----END PRIVATE KEY-----\n";

        paramOutputStream.write(privKeyText.getBytes(CharsetUtil.US_ASCII));

    }

}
