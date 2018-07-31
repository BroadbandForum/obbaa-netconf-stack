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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.validation;

import org.junit.Test;

public class LocalNameClassTest extends AbstractDataStoreValidatorTest {

    @Test
    public void testLeafCaseName() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <class-validation>"
                + "  <class>class</class>"
                + " </class-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:class-validation>"
                        + "    <validation:class>class</validation:class>"
                        + "    <validation:class1>1</validation:class1>"
                        + "   </validation:class-validation>"
                        + "  </validation:validation>"
                        + " </data>"
                        + "</rpc-reply>";
        verifyGet(response);

    }

    @Test
    public void testClassWithCurrent() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <class-validation>"
                + "  <class>class</class>"
                + "  <caseList>"
                + "    <class>class</class>"
                + "    <classContainer>"
                + "      <class>class</class>"
                + "    </classContainer>"
                + "  </caseList>"
                + " </class-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:class-validation>"
                        + "    <validation:caseList>"
                        + "     <validation:class>class</validation:class>"
                        + "     <validation:classContainer>"
                        + "      <validation:class>class</validation:class>"
                        + "     </validation:classContainer>"
                        + "    </validation:caseList>"
                        + "    <validation:class>class</validation:class>"
                        + "    <validation:class1>1</validation:class1>"
                        + "   </validation:class-validation>"
                        + "  </validation:validation>"
                        + " </data>"
                        + "</rpc-reply>";
        verifyGet(response);
        requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <class-validation>"
                + "  <class3>class</class3>"
                + "  <class2>class</class2>"
                + " </class-validation>"
                + "</validation>"
        ;
        editConfig(m_server, m_clientInfo, requestXml1, true);

        response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:class-validation>"
                        + "    <validation:caseList>"
                        + "     <validation:class>class</validation:class>"
                        + "     <validation:classContainer>"
                        + "      <validation:class>class</validation:class>"
                        + "     </validation:classContainer>"
                        + "    </validation:caseList>"
                        + "    <validation:class>class</validation:class>"
                        + "    <validation:class1>1</validation:class1>"
                        + "    <validation:class3>class</validation:class3>"
                        + "    <validation:class2>class</validation:class2>"
                        + "   </validation:class-validation>"
                        + "  </validation:validation>"
                        + " </data>"
                        + "</rpc-reply>"
        ;
        verifyGet(response);
    }

    @Test
    public void testMultiLevelParentName() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <parent-validation>"
                + "  <parent>parent</parent>"
                + "  <parentList>"
                + "    <parent>parent</parent>"
                + "    <parentContainer>"
                + "     <parent>"
                + "      <parent>"
                + "        <parent>parent</parent>"
                + "      </parent>"
                + "     </parent>"
                + "    </parentContainer>"
                + "  </parentList>"
                + " </parent-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + " <data>"
                        + "  <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "   <validation:parent-validation>"
                        + "    <validation:parent>parent</validation:parent>"
                        + "     <validation:parentList>"
                        + "      <validation:parent>parent</validation:parent>"
                        + "       <validation:parentContainer>"
                        + "        <validation:parent>"
                        + "         <validation:parent>"
                        + "          <validation:parent>parent</validation:parent>"
                        + "         </validation:parent>"
                        + "        </validation:parent>"
                        + "       </validation:parentContainer>"
                        + "      </validation:parentList>"
                        + "     </validation:parent-validation>"
                        + "    </validation:validation>"
                        + "   </data>"
                        + "  </rpc-reply>";
        verifyGet(response);
    }

    @Test
    public void testMultiLevelClassName() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <class-validation>"
                + "  <class>class</class>"
                + "  <caseList>"
                + "    <class>class</class>"
                + "    <innerClass>"
                + "    <class>"
                + "      <class>class</class>"
                + "      <innerClass>"
                + "        <class>"
                + "          <class>"
                + "            <class>class</class>"
                + "          </class>"
                + "        </class>"
                + "      </innerClass>"
                + "    </class>"
                + "    </innerClass>"
                + "    <refClass1>class</refClass1>"
                + "  </caseList>"
                + " </class-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + " <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "  <validation:class-validation>"
                        + "   <validation:caseList>"
                        + "    <validation:class>class</validation:class>"
                        + "    <validation:innerClass>"
                        + "     <validation:class>"
                        + "      <validation:class>class</validation:class>"
                        + "      <validation:innerClass>"
                        + "       <validation:class>"
                        + "        <validation:class>"
                        + "         <validation:class>class</validation:class>"
                        + "        </validation:class>"
                        + "       </validation:class>"
                        + "      </validation:innerClass>"
                        + "     </validation:class>"
                        + "    </validation:innerClass>"
                        + "    <validation:refClass1>class</validation:refClass1>"
                        + "   </validation:caseList>"
                        + "   <validation:class>class</validation:class>"
                        + "   <validation:class1>1</validation:class1>"
                        + "  </validation:class-validation>"
                        + " </validation:validation>"
                        + "</data>"
                        + "</rpc-reply>";
        verifyGet(response);
    }

    @Test
    public void testLeafRefClassName() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
                "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <class-validation>"
                + "  <class>class</class>"
                + "  <caseList>"
                + "    <class>class</class>"
                + "    <innerClass>"
                + "    <class>"
                + "      <class>class</class>"
                + "      <innerClass>"
                + "        <class>"
                + "          <class>"
                + "            <class>class</class>"
                + "          </class>"
                + "        </class>"
                + "      </innerClass>"
                + "    </class>"
                + "    </innerClass>"
                + "    <refClass1>class</refClass1>"
                + "    <refClass2>class</refClass2>"
                + "  </caseList>"
                + " </class-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);

        String response =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + " <validation:validation xmlns:validation=\"urn:org:bbf:pma:validation\">"
                        + "  <validation:class-validation>"
                        + "   <validation:caseList>"
                        + "    <validation:class>class</validation:class>"
                        + "    <validation:innerClass>"
                        + "     <validation:class>"
                        + "      <validation:class>class</validation:class>"
                        + "      <validation:innerClass>"
                        + "       <validation:class>"
                        + "        <validation:class>"
                        + "         <validation:class>class</validation:class>"
                        + "        </validation:class>"
                        + "       </validation:class>"
                        + "      </validation:innerClass>"
                        + "     </validation:class>"
                        + "    </validation:innerClass>"
                        + "    <validation:refClass1>class</validation:refClass1>"
                        + "    <validation:refClass2>class</validation:refClass2>"
                        + "   </validation:caseList>"
                        + "   <validation:class>class</validation:class>"
                        + "   <validation:class1>1</validation:class1>"
                        + "  </validation:class-validation>"
                        + " </validation:validation>"
                        + "</data>"
                        + "</rpc-reply>";
        verifyGet(response);
    }

    @Test
    public void testCurentWithMultipleParent() throws Exception {
        getModelNode();
        String requestXml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation xmlns=\"urn:org:bbf:pma:validation\">"
                + " <class-validation>"
                + "  <class>class</class>"
                + "  <someClass>"
                + "    <someLeafRef>class</someLeafRef>"
                + "  </someClass>"
                + " </class-validation>"
                + "</validation>";
        editConfig(m_server, m_clientInfo, requestXml1, true);
    }
}
