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

package org.broadband_forum.obbaa.netconf.client.ssh;

import org.broadband_forum.obbaa.netconf.api.client.authentication.LoginKey;
import org.broadband_forum.obbaa.netconf.client.ssh.auth.SshHostKeyUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.KeyPair;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SshHostKeyUtilTest {

    KeyPair keyPair = null;
    File temp = null;
    LoginKey loginKey = null;

    @Before
    public void setUp() throws Exception {
        temp = new File("test.tmp");
        loginKey = new LoginKey(null, "hostkey_pub.pem", "hostkey_priv.pem");
    }

    @After
    public void deleteFile() {
        temp.deleteOnExit();
        ;
    }

    @Test
    public void testdoReadKeyPair() throws Exception {
        keyPair = SshHostKeyUtil.doReadKeyPair(loginKey);
        assertNotNull(keyPair);
    }

    @Test
    public void testdoWriteKeyPair() throws Exception {

        OutputStream outStream = new FileOutputStream(temp);
        keyPair = SshHostKeyUtil.doReadKeyPair(loginKey);

        SshHostKeyUtil.doWriteKeyPair(keyPair, outStream);

        outStream.close();
        assertTrue(temp.exists());

    }

}
