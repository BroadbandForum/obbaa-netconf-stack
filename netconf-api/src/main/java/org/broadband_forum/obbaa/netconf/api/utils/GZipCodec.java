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

package org.broadband_forum.obbaa.netconf.api.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;

public class GZipCodec {
    public static final String CHAR_ENCODING = "UTF-8";
    public static byte [] encode(String str){
        if(str != null) {
            int length = str.length();
            if (length > 0) {
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream(length);
                     GZIPOutputStream os = new GZIPOutputStream(baos, length)) {
                    os.write(str.getBytes(CHAR_ENCODING));
                    //explicitly finish OS to make it write
                    os.finish();
                    return baos.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException("Could not write compressed data", e);
                }
            }
        }
        return new byte[0];
    }

    public static String decode(byte[] bytes) {
        if (bytes.length > 0) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                 GZIPInputStream is = new GZIPInputStream(bais)) {
                return IOUtils.toString(is, CHAR_ENCODING);
            } catch (IOException e) {
                throw new RuntimeException("Could not read compressed data", e);
            }
        } else {
            return "";
        }
    }
}
