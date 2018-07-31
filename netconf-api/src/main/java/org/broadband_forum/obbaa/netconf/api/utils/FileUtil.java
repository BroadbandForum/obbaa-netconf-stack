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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by keshava on 8/13/15.
 */
public class FileUtil {
    public static String loadAsString(String resourceName) {
        StringBuffer sb = new StringBuffer();

        InputStream resourceStream = FileUtil.class.getResourceAsStream(resourceName);
        return loadStreamAsString(sb, resourceStream);
    }

    public static String loadStreamAsString(StringBuffer sb, InputStream resourceStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceStream))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(System.getProperty("line.separator"));
            }
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String loadFileAsString(String filePath) throws FileNotFoundException {
        StringBuffer sb = new StringBuffer();

        InputStream resourceStream = new FileInputStream(filePath);
        return loadStreamAsString(sb, resourceStream);
    }
}
