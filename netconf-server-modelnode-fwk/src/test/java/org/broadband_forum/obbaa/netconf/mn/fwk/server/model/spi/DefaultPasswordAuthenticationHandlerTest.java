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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.spi;


import org.broadband_forum.obbaa.netconf.server.ssh.auth.KeyAuthHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import io.netty.util.CharsetUtil;

import org.junit.Before;
import org.junit.Test;
import org.broadband_forum.obbaa.netconf.api.server.auth.ClientAuthenticationInfo;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static org.junit.Assert.*;

/**
 * Created by vgotagi on 10/17/16.
 */
public class DefaultPasswordAuthenticationHandlerTest {

    DefaultPasswordAuthenticationHandler m_apa;

    @Before
    public void setUp() {
        m_apa = new DefaultPasswordAuthenticationHandler();
    }

    @Test
    public void testAuthenticate() {

        assertFalse(m_apa.authenticate(new ClientAuthenticationInfo(1, "admin", "admin", null, null)));

        m_apa.setPassword("admin");
        assertFalse(m_apa.authenticate(new ClientAuthenticationInfo(1, "admin", "admin", null, null)));

        m_apa = new DefaultPasswordAuthenticationHandler();
        m_apa.setUsername("admin");
        assertFalse(m_apa.authenticate(new ClientAuthenticationInfo(1, "admin", "admin", null, null)));

        m_apa = new DefaultPasswordAuthenticationHandler();
        m_apa.setUsername("admin");
        m_apa.setPassword("admin");
        assertTrue(m_apa.authenticate(new ClientAuthenticationInfo(1, "admin", "admin", null, null)));
    }


    @Test
    public void testAuthenticatePublicKey() throws Exception {

        String pubKeyStr = "MIIBuDCCASwGByqGSM44BAEwggEfAoGBAOg5MOEOL1Lwe8/38+illEM7LSnDbMbztQj" +
                "/h7T0tgQixxebT6S2jcpLouZEYDOTDAuMb8UKhbxZ7+f+8xdTMhw4elpC/3LWs+WwncZM7LaxWtg66l74R4pp"
                + "WmIIsU1RaXOAgXoThNGr6IsDLaGiv7LQIbh8vzzMzl6lrlDjCcj7AhUAsty8KhNivqCqcCH/kbp+Aj" +
                "+Vwj8CgYEAmytCjwTBuc84G/ibRV2J8QP0NDJ2VDRyJnkPjic4v/HCKbSL/nKjE2E2LlUC36mV"
                +
                "VhQYvCcLp92vnXVlxijOldZxceOAK79dFfbjbwIE4YVeAg4DQnMhcXaKOLAAAHpOvnLbiTgq4JF5kYmo2b63738nZRQmy3CiU5PfDFG5aV8DgYUAAoGBAI9jP8uaqHTJWj4ntCP9s56aF38vea4YkZPa"
                + "tOJPqxg3ZbttTI8Y1QWXabjPyY1c/YWdd/GasegQKrgMGM7ufGhWaNcllI0cC6o0J5K2zacGUufxSWeFZ9Unx2vCKYhIMKR66J" +
                "+aHo5Wq9VF0UmsDKtoCGXFRVqVHFsM+6i8YfBD";
        ByteBuf base64 = Unpooled.copiedBuffer(pubKeyStr, CharsetUtil.US_ASCII);
        KeyFactory dsaKF = KeyFactory.getInstance("DSA");
        ByteBuf der = Base64.decode(base64);
        base64.release();
        PublicKey pubKey = null;
        ByteBuf encodedKeyBuf = der;
        byte[] encodedKey = new byte[der.readableBytes()];
        encodedKeyBuf.readBytes(encodedKey).release();
        X509EncodedKeySpec encodedKeySpec = new X509EncodedKeySpec(encodedKey);

        try {
            KeyFactory rsaKF = KeyFactory.getInstance("RSA");
            pubKey = rsaKF.generatePublic(encodedKeySpec);
        } catch (InvalidKeySpecException ignore) {
            pubKey = dsaKF.generatePublic(encodedKeySpec);
        }
        KeyAuthHandler handler = new KeyAuthHandler("authorized_keys");
        assertTrue(handler.authenticate(pubKey));
        assertFalse(m_apa.authenticate(pubKey));
    }

    @Test
    public void testGetetrAndSetters() {
        m_apa.setUsername("admin");
        m_apa.setPassword("admin");
        assertEquals("admin", m_apa.getUsername());
        assertEquals("admin", m_apa.getPassword());
    }

}
