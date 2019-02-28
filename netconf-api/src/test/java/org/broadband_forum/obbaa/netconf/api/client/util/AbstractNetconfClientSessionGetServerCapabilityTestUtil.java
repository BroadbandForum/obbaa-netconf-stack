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

package org.broadband_forum.obbaa.netconf.api.client.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class AbstractNetconfClientSessionGetServerCapabilityTestUtil {

    public static CompletableFuture<NetConfResponse> getNetConfResponses(final Element response) {
        final NetConfResponse netConfResponse = new NetConfResponse();
        return new CompletableFuture<NetConfResponse>() {
            @Override
            public NetConfResponse get() {
                netConfResponse.setMessageId(response.getAttribute(NetconfResources.MESSAGE_ID));
                //netConfResponse.setOk(true);
                return netConfResponse.setData(response);
            }

            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return true;
            }

            @Override
            public boolean isCancelled() {
                return true;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public NetConfResponse get(long timeout, TimeUnit unit) {
                return null;
            }
        };
    }


    public static Element getYangModulesResponseElement() {
        String getResponse = "<data>\n" +
                "        <yanglib:modules-state xmlns:yanglib=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                "            <yanglib:module-set-id>bf3aaa952a123ba750ef9c3cd5d6bf5503b42223ee48b6eb04d40c63d075fe8c\n" +
                "            </yanglib:module-set-id>\n" +
                "            <yanglib:module>\n" +
                "                <yanglib:name>ietf-inet-types</yanglib:name>\n" +
                "                <yanglib:revision>2013-07-15</yanglib:revision>\n" +
                "                <yanglib:namespace>urn:ietf:params:xml:ns:yang:ietf-inet-types</yanglib:namespace>\n" +
                "                <yanglib:conformance-type>implement</yanglib:conformance-type>\n" +
                "            </yanglib:module>\n" +
                "            <yanglib:module>\n" +
                "                <yanglib:name>ietf-yang-library</yanglib:name>\n" +
                "                <yanglib:revision>2016-06-21</yanglib:revision>\n" +
                "                <yanglib:namespace>urn:ietf:params:xml:ns:yang:ietf-yang-library</yanglib:namespace>\n" +
                "                <yanglib:conformance-type>implement</yanglib:conformance-type>\n" +
                "                <yanglib:submodules>\n" +
                "                    <yanglib:submodule>\n" +
                "                        <yanglib:name>ietf-yang-library-submodule</yanglib:name>\n" +
                "                        <yanglib:revision>2016-06-21</yanglib:revision>\n" +
                "                    </yanglib:submodule>\n" +
                "                </yanglib:submodules>\n" +
                "            </yanglib:module>\n" +
                "            <yanglib:module>\n" +
                "                <yanglib:name>bbf-sub-interfaces</yanglib:name>\n" +
                "                <yanglib:revision>2016-05-30</yanglib:revision>\n" +
                "                <yanglib:namespace>urn:broadband-forum-org:yang:bbf-sub-interfaces</yanglib:namespace>\n" +
                "                <yanglib:conformance-type>implement</yanglib:conformance-type>\n" +
                "                <yanglib:feature>tag-rewrites</yanglib:feature>\n" +
                "            </yanglib:module>\n" +
                "            <yanglib:module>\n" +
                "                <yanglib:name>ietf-entity</yanglib:name>\n" +
                "                <yanglib:revision>2015-10-19</yanglib:revision>\n" +
                "                <yanglib:namespace>urn:ietf:params:xml:ns:yang:ietf-entity</yanglib:namespace>\n" +
                "                <yanglib:conformance-type>implement</yanglib:conformance-type>\n" +
                "                <yanglib:feature>entity-sensor</yanglib:feature>\n" +
                "                <yanglib:feature>entity-mib</yanglib:feature>\n" +
                "            </yanglib:module>\n" +
                "        </yanglib:modules-state>\n" +
                "    </data>\n";

        return transformToElement(getResponse);
    }

    public static Element getYangModulesResponseElementWithDeviations() {
        String getResponse = "<data>\n" +
                "    <modules-state xmlns=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                "        <module-set-id>bf3aaa952a123ba750ef9c3cd5d6bf5503b42223ee48b6eb04d40c63d075fe8c</module-set-id>\n" +
                "        <module>\n" +
                "            <name>ietf-yang-library</name>\n" +
                "            <revision>2016-06-21</revision>\n" +
                "            <namespace>urn:ietf:params:xml:ns:yang:ietf-yang-library</namespace>\n" +
                "            <conformance-type>implement</conformance-type>\n" +
                "            <submodules>\n" +
                "                <submodule>\n" +
                "                    <name>ietf-yang-library-submodule</name>\n" +
                "                    <revision>2016-06-21</revision>\n" +
                "                </submodule>\n" +
                "            </submodules>\n" +
                "        </module>\n" +
                "        <module>\n" +
                "            <name>ietf-interfaces</name>\n" +
                "            <revision>2014-05-08</revision>\n" +
                "            <namespace>urn:ietf:params:xml:ns:yang:ietf-interfaces</namespace>\n" +
                "            <conformance-type>implement</conformance-type>\n" +
                "            <feature>if-mib</feature>\n" +
                "            <deviation>\n" +
                "                <name>test-ietf-interfaces-dev</name>\n" +
                "                <revision>2017-07-05</revision>\n" +
                "            </deviation>\n" +
                "        </module>\n" +
                "        <module>\n" +
                "            <name>test-ietf-interfaces-dev</name>\n" +
                "            <revision>2017-07-05</revision>\n" +
                "            <namespace>urn:xxxxx-org:yang:test-ietf-interfaces-dev</namespace>\n" +
                "            <conformance-type>implement</conformance-type>\n" +
                "        </module>\n" +
                "    </modules-state>\n" +
                "</data>";

        return transformToElement(getResponse);
    }

    public static Element getYangModulesResponseElementWithSimYang() {
        String getResponse = "<data>\n" +
                "    <yanglib:modules-state xmlns:yanglib=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                "        <yanglib:module-set-id>bf3aaa952a123ba750ef9c3cd5d6bf5503b42223ee48b6eb04d40c63d075fe8c\n" +
                "        </yanglib:module-set-id>\n" +
                "        <yanglib:module>\n" +
                "            <yanglib:name>ietf-inet-types</yanglib:name>\n" +
                "            <yanglib:revision>2013-07-15</yanglib:revision>\n" +
                "            <yanglib:namespace>urn:ietf:params:xml:ns:yang:ietf-inet-types</yanglib:namespace>\n" +
                "            <yanglib:conformance-type>implement</yanglib:conformance-type>\n" +
                "        </yanglib:module>\n" +
                "        <yanglib:module>\n" +
                "            <yanglib:name>ietf-yang-library</yanglib:name>\n" +
                "            <yanglib:revision>2016-06-21</yanglib:revision>\n" +
                "            <yanglib:namespace>urn:ietf:params:xml:ns:yang:ietf-yang-library</yanglib:namespace>\n" +
                "            <yanglib:conformance-type>implement</yanglib:conformance-type>\n" +
                "            <yanglib:submodules>\n" +
                "                <yanglib:submodule>\n" +
                "                    <yanglib:name>ietf-yang-library-submodule</yanglib:name>\n" +
                "                    <yanglib:revision>2016-06-21</yanglib:revision>\n" +
                "                </yanglib:submodule>\n" +
                "            </yanglib:submodules>\n" +
                "        </yanglib:module>\n" +
                "        <yanglib:module>\n" +
                "            <yanglib:name>simulator-test-interface</yanglib:name>\n" +
                "            <yanglib:revision>2016-08-18</yanglib:revision>\n" +
                "            <yanglib:namespace>http://www.test-company.com/solutions/simulator-test-interface\n" +
                "            </yanglib:namespace>\n" +
                "            <yanglib:conformance-type>implement</yanglib:conformance-type>\n" +
                "        </yanglib:module>\n" +
                "        <yanglib:module>\n" +
                "            <yanglib:name>bbf-sub-interfaces</yanglib:name>\n" +
                "            <yanglib:revision>2016-05-30</yanglib:revision>\n" +
                "            <yanglib:namespace>urn:broadband-forum-org:yang:bbf-sub-interfaces</yanglib:namespace>\n" +
                "            <yanglib:conformance-type>implement</yanglib:conformance-type>\n" +
                "            <yanglib:feature>tag-rewrites</yanglib:feature>\n" +
                "        </yanglib:module>\n" +
                "        <yanglib:module>\n" +
                "            <yanglib:name>ietf-entity</yanglib:name>\n" +
                "            <yanglib:revision>2015-10-19</yanglib:revision>\n" +
                "            <yanglib:namespace>urn:ietf:params:xml:ns:yang:ietf-entity</yanglib:namespace>\n" +
                "            <yanglib:conformance-type>implement</yanglib:conformance-type>\n" +
                "            <yanglib:feature>entity-sensor</yanglib:feature>\n" +
                "            <yanglib:feature>entity-mib</yanglib:feature>\n" +
                "        </yanglib:module>\n" +
                "    </yanglib:modules-state>\n" +
                "</data>";

        return transformToElement(getResponse);
    }


    public static Element getYangModulesResponseElementWithDeviationsOnModuleWithEmptyRevision() {
        String getResponse =  "    <data>\n" +
                "        <yanglib:modules-state xmlns:yanglib=\"urn:ietf:params:xml:ns:yang:ietf-yang-library\">\n" +
                "            <yanglib:module-set-id>bf3aaa952a123ba750ef9c3cd5d6bf5503b42223ee48b6eb04d40c63d075fe8c\n" +
                "            </yanglib:module-set-id>\n" +
                "            <yanglib:module>\n" +
                "                <yanglib:name>ietf-yang-library</yanglib:name>\n" +
                "                <yanglib:revision>2016-06-21</yanglib:revision>\n" +
                "                <yanglib:namespace>urn:ietf:params:xml:ns:yang:ietf-yang-library</yanglib:namespace>\n" +
                "                <yanglib:conformance-type>implement</yanglib:conformance-type>\n" +
                "                <yanglib:submodules>\n" +
                "                    <yanglib:submodule>\n" +
                "                        <yanglib:name>ietf-yang-library-submodule</yanglib:name>\n" +
                "                        <yanglib:revision>2016-06-21</yanglib:revision>\n" +
                "                    </yanglib:submodule>\n" +
                "                </yanglib:submodules>\n" +
                "            </yanglib:module>\n" +
                "            <yanglib:module>\n" +
                "                <yanglib:name>ietf-interfaces</yanglib:name>\n" +
                "                <yanglib:revision/>\n" +
                "                <yanglib:namespace>urn:ietf:params:xml:ns:yang:ietf-interfaces</yanglib:namespace>\n" +
                "                <yanglib:conformance-type>implement</yanglib:conformance-type>\n" +
                "                <yanglib:feature>if-mib</yanglib:feature>\n" +
                "                <yanglib:deviation>\n" +
                "                    <yanglib:name>test-ietf-interfaces-dev</yanglib:name>\n" +
                "                    <yanglib:revision>2017-07-05</yanglib:revision>\n" +
                "                </yanglib:deviation>\n" +
                "            </yanglib:module>\n" +
                "            <yanglib:module>\n" +
                "                <yanglib:name>ietf-interfaces2</yanglib:name>\n"+
                "                <yanglib:revision>2017-07-05</yanglib:revision>\n" +
                "                <yanglib:namespace>urn:ietf:params:xml:ns:yang:ietf-interfaces</yanglib:namespace>\n" +
                "                <yanglib:conformance-type>implement2</yanglib:conformance-type>\n" +
                "                <yanglib:feature>if-mib2</yanglib:feature>\n" +
                "                <yanglib:deviation>\n" +
                "                    <yanglib:name>test-ietf-interfaces-dev2</yanglib:name>\n" +
                "                    <yanglib:revision>2017-07-05</yanglib:revision>\n" +
                "                </yanglib:deviation>\n" +
                "            </yanglib:module>\n" +
                "            <yanglib:module>\n" +
                "                <yanglib:name>ietf-interfaces3</yanglib:name>\n"+
                "                <yanglib:revision>2017-07-05</yanglib:revision>\n" +
                "                <yanglib:namespace>urn:ietf:params:xml:ns:yang:ietf-interfaces</yanglib:namespace>\n" +
                "                <yanglib:conformance-type>implement3</yanglib:conformance-type>\n" +
                "                <yanglib:feature>if-mib3,test1,test2</yanglib:feature>\n" +
                "                <yanglib:deviation>\n" +
                "                    <yanglib:name>test-ietf-interfaces-dev3,test1,test2</yanglib:name>\n" +
                "                    <yanglib:revision>2017-07-05</yanglib:revision>\n" +
                "                </yanglib:deviation>\n" +
                "            </yanglib:module>\n" +
                "            <yanglib:module>\n" +
                "                <yanglib:name>ietf-interfaces4</yanglib:name>\n"+
                "                <yanglib:revision>2017-07-05</yanglib:revision>\n" +
                "                <yanglib:namespace>urn:ietf:params:xml:ns:yang:ietf-interfaces</yanglib:namespace>\n" +
                "                <yanglib:conformance-type>implement4</yanglib:conformance-type>\n" +
                "                <yanglib:feature>if-mib4,test4,test4</yanglib:feature>\n" +
                "                <yanglib:deviation>\n" +
                "                    <yanglib:name>test-ietf-interfaces-dev4,test1,test2</yanglib:name>\n" +
                "                    <yanglib:revision>2017-07-05</yanglib:revision>\n" +
                "                </yanglib:deviation>\n" +
                "            </yanglib:module>\n" +
                "            <yanglib:module>\n" +
                "                <yanglib:name>test-ietf-interfaces-dev</yanglib:name>\n" +
                "                <yanglib:revision>2017-07-05</yanglib:revision>\n" +
                "                <yanglib:namespace>urn:xxxxx-org:yang:test-ietf-interfaces-dev</yanglib:namespace>\n" +
                "                <yanglib:conformance-type>implement</yanglib:conformance-type>\n" +
                "            </yanglib:module>\n" +
                "        </yanglib:modules-state>\n" +
                "    </data>\n";

        return transformToElement(getResponse);
    }

    private static Element transformToElement(String xmldata) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputStream stream = new ByteArrayInputStream(xmldata.getBytes(StandardCharsets.UTF_8));
            Document doc = dBuilder.parse(stream);
            return doc.getDocumentElement();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
