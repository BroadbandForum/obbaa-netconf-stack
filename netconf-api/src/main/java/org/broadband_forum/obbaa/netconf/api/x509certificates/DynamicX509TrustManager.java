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

import javax.net.ssl.X509TrustManager;
import java.util.List;

/**
 * Created by keshava on 2/16/16.
 */
public interface DynamicX509TrustManager extends X509TrustManager{
    void initTrustManager(List<String> trustedCaCertificates) throws TrustManagerInitException;

    void initTrustManager(String caCertificateFilePath) throws TrustManagerInitException;
}
