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

package org.broadband_forum.obbaa.netconf.server.ssh;

import org.broadband_forum.obbaa.netconf.server.ssh.auth.SshFileKeyValidataion;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.util.CharsetUtil;

import org.junit.Before;
import org.junit.Test;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static org.junit.Assert.assertTrue;

public class SshFileKeyValidataionTest {

    private SshFileKeyValidataion m_authenticator;

    @Before
    public void initServer() {
        m_authenticator = new SshFileKeyValidataion();
    }

    @Test
    public void testisValidPublicKey() throws Exception {

        String pubKeyStr = "MIIBuDCCASwGByqGSM44BAEwggEfAoGBAOg5MOEOL1Lwe8/38+illEM7LSnDbMbztQj" +
                "/h7T0tgQixxebT6S2jcpLouZEYDOTDAuMb8UKhbxZ7+f+8xdTMhw4elpC/3LWs+WwncZM7LaxWtg66l74R4pp"
                + "WmIIsU1RaXOAgXoThNGr6IsDLaGiv7LQIbh8vzzMzl6lrlDjCcj7AhUAsty8KhNivqCqcCH/kbp+Aj" +
                "+Vwj8CgYEAmytCjwTBuc84G/ibRV2J8QP0NDJ2VDRyJnkPjic4v/HCKbSL/nKjE2E2LlUC36mV"
                +
                "VhQYvCcLp92vnXVlxijOldZxceOAK79dFfbjbwIE4YVeAg4DQnMhcXaKOLAAAHpOvnLbiTgq4JF5kYmo2b63738nZRQmy3CiU5PfDFG5aV8DgYUAAoGBAI9jP8uaqHTJWj4ntCP9s56aF38vea4YkZPa"
                + "tOJPqxg3ZbttTI8Y1QWXabjPyY1c/YWdd/GasegQKrgMGM7ufGhWaNcllI0cC6o0J5K2zacGUufxSWeFZ9Unx2vCKYhIMKR66J" +
                "+aHo5Wq9VF0UmsDKtoCGXFRVqVHFsM+6i8YfBD";

        ByteBuf base64 = Unpooled.copiedBuffer(pubKeyStr, CharsetUtil.US_ASCII);
        ByteBuf der = Base64.decode(base64);
        base64.release();

        ByteBuf encodedKeyBuf = der;
        byte[] encodedKey = new byte[der.readableBytes()];
        encodedKeyBuf.readBytes(encodedKey).release();

        KeyFactory rsaKF = KeyFactory.getInstance("RSA");
        KeyFactory dsaKF = KeyFactory.getInstance("DSA");

        X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(encodedKey);
        PublicKey pubKey;
        try {
            pubKey = rsaKF.generatePublic(encodedKeySpec);
        } catch (InvalidKeySpecException ignore) {
            pubKey = dsaKF.generatePublic(encodedKeySpec);
        }

        String filePathAuthKey = Thread.currentThread().getContextClassLoader().getResource("authorized_keys")
                .getPath();

        boolean auth = m_authenticator.isValidPublicKey(filePathAuthKey, pubKey);

        assertTrue(auth);
    }
}
