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

package org.broadband_forum.obbaa.netconf.server.ssh.auth;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.utils.PemReader;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.PublicKey;

/**
 * This class provides the method for the validation of key.
 * 
 *
 * 
 */
public class SshFileKeyValidataion implements SshKeyValidataion {

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(SshFileKeyValidataion.class, LogAppNames.NETCONF_LIB);

    /**
     * This method will compare the input public key with the public keys in the authorized key file and if it is present it will return
     * true else false.
     */
    @Override
    public boolean isValidPublicKey(String filePathAuthKey, PublicKey pubKey) {
        FileInputStream fis = null;
        try {

            fis = new FileInputStream(filePathAuthKey);
            if (PemReader.readPublicKey(fis).contains(pubKey)) {
                return true;
            }
        } catch (Exception e) {
            LOGGER.error("Exception :", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    LOGGER.error("Error in closing the Key File Stream", e);
                }
            }
        }
        return false;
    }

}
