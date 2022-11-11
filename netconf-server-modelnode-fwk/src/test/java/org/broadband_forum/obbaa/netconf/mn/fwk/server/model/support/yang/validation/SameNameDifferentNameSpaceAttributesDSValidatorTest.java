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

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.AddDefaultDataInterceptor;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(RequestScopeJunitRunner.class)
public class SameNameDifferentNameSpaceAttributesDSValidatorTest extends
        AbstractDataStoreValidatorTest {

    @Test
    public void testContainerWithSameLocalNameDifferentNameSpaceReplicate1() throws Exception {
        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <scheduler-node  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">"
                + "     	<name>child</name>"
                + "     	<scheduling-level>3</scheduling-level>"
                + "    </scheduler-node>"
                + "    <scheduler-node  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">"
                + "     <name>parent</name>"
                + "     <scheduling-level>2</scheduling-level>"
                + "			<child-scheduler-nodes>"
                + " 	        <name>child</name>"
                + "			</child-scheduler-nodes>"
                + "    </scheduler-node>"
                + "  </tm-root>"
                + "</validation-yang11>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        String ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + "		<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                        + "<validation11:tm-root>"
                        + "<validation-augment11-replicate-namespace1:scheduler-node xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">"
                        + "<validation-augment11-replicate-namespace1:name>child</validation-augment11-replicate-namespace1:name>"
                        + "<validation-augment11-replicate-namespace1:scheduling-level>3</validation-augment11-replicate-namespace1:scheduling-level>"
                        + "</validation-augment11-replicate-namespace1:scheduler-node>"
                        + "<validation-augment11-replicate-namespace1:scheduler-node xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">"
                        + "<validation-augment11-replicate-namespace1:child-scheduler-nodes>"
                        + "<validation-augment11-replicate-namespace1:name>child</validation-augment11-replicate-namespace1:name>"
                        + "</validation-augment11-replicate-namespace1:child-scheduler-nodes>"
                        + "<validation-augment11-replicate-namespace1:name>parent</validation-augment11-replicate-namespace1:name>"
                        + "<validation-augment11-replicate-namespace1:scheduling-level>2</validation-augment11-replicate-namespace1:scheduling-level>"
                        + "</validation-augment11-replicate-namespace1:scheduler-node>"
                        + "</validation11:tm-root>"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "</validation11:validation-yang11>"
                        + "</data>"
                        + "</rpc-reply>";
        verifyGet(ncResponse);
    }

    @Test
    public void testContainerWithSameLocalNameDifferentNameSpaceAugmentYangReplicate2()
            throws Exception {
        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <scheduler-node  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
                + "     	<name>child</name>"
                + "     	<scheduling-level>3</scheduling-level>"
                + "    </scheduler-node>"
                + "    <scheduler-node  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
                + "     <name>parent</name>"
                + "     <scheduling-level>2</scheduling-level>"
                + "			<child-scheduler-nodes>"
                + " 	        <name>child</name>"
                + "			</child-scheduler-nodes>"
                + "    </scheduler-node>"
                + "  </tm-root>"
                + "</validation-yang11>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        String ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + "		<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "		<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                        + "			<validation11:tm-root>"
                        + "				<validation-augment11:scheduler-node xmlns:validation-augment11=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
                        + "					<validation-augment11:name>child</validation-augment11:name>"
                        + "					<validation-augment11:scheduling-level>3</validation-augment11:scheduling-level>"
                        + "				</validation-augment11:scheduler-node>"
                        + "				<validation-augment11:scheduler-node xmlns:validation-augment11=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
                        + "					<validation-augment11:name>parent</validation-augment11:name>"
                        + "					<validation-augment11:scheduling-level>2</validation-augment11:scheduling-level>"
                        + "					<validation-augment11:child-scheduler-nodes>"
                        + "						<validation-augment11:name>child</validation-augment11:name>"
                        + "					</validation-augment11:child-scheduler-nodes>"
                        + "				</validation-augment11:scheduler-node>"
                        + "			</validation11:tm-root>"
                        + "                     <validation11:container-with-must>"
                        + "                         <validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "                     </validation11:container-with-must>"
                        + "		</validation11:validation-yang11>"
                        + "	</data>"
                        + "</rpc-reply>";
        verifyGet(ncResponse);
    }

    // NAMESPACE1:LEAF AND NAMESPACE2:LEAF TESTS

    /*
    Creates check-leaf-to-leaf-replicate for both the namespace(urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1
                        , urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2) under tm-root
     */
    public void setUpForTestLeaftoLeafReplicate() throws Exception {

        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaf-to-leaf-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</check-leaf-to-leaf-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        String ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "<validation11:tm-root>"
                        + "<validation-augment11-replicate-namespace1:check-leaf-to-leaf-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</validation-augment11-replicate-namespace1:check-leaf-to-leaf-replicate>"
                        + "</validation11:tm-root>"
                        + "</validation11:validation-yang11>"
                        + "</data>"
                        + "</rpc-reply>";

        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaf-to-leaf-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate2</check-leaf-to-leaf-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "<validation11:tm-root>"
                        + "<validation-augment11-replicate-namespace1:check-leaf-to-leaf-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</validation-augment11-replicate-namespace1:check-leaf-to-leaf-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaf-to-leaf-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate2</validation-augment11-replicate-namespace2:check-leaf-to-leaf-replicate>"
                        + "</validation11:tm-root>"
                        + "</validation11:validation-yang11>"
                        + "</data>"
                        + "</rpc-reply>";

        sendEditConfigAndVerifyGet(requestXml, ncResponse);
    }


    @Test
    public void testLeaftoLeafReplicate1() throws Exception {
        setUpForTestLeaftoLeafReplicate();

        // create leaf check-leaf-to-leaf-replicate-pass-1 with has must constraint
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaf-to-leaf-replicate-pass-1  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">replicate3</check-leaf-to-leaf-replicate-pass-1>"
                + "  </tm-root>"
                + "</validation-yang11>";

        String ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                        + "<validation11:tm-root>"
                        + "<validation-augment11-replicate-namespace1:check-leaf-to-leaf-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</validation-augment11-replicate-namespace1:check-leaf-to-leaf-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaf-to-leaf-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate2</validation-augment11-replicate-namespace2:check-leaf-to-leaf-replicate>"
                        + "<validation-augment11:check-leaf-to-leaf-replicate-pass-1 xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">replicate3</validation-augment11:check-leaf-to-leaf-replicate-pass-1>"
                        + "</validation11:tm-root>"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "</validation11:validation-yang11>"
                        + "</data>"
                        + "</rpc-reply>";
        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        // create leaf check-leaf-to-leaf-replicate-fail-1 with has must constraint - wrong one
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaf-to-leaf-replicate-fail-1  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">replicate4</check-leaf-to-leaf-replicate-fail-1>"
                + "  </tm-root>"
                + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
                "Violate must constraints: ../validation-augment11-replicate-namespace1:check-leaf-to-leaf-replicate='replicate3' and ../validation-augment11-replicate-namespace2:check-leaf-to-leaf-replicate='replicate2'",
                "/validation11:validation-yang11/validation11:tm-root/validation-augment11:check-leaf-to-leaf-replicate-fail-1");


    }


    @Test
    public void testLeaftoLeafReplicate2() throws Exception {
        setUpForTestLeaftoLeafReplicate();

        // create leaf check-leaf-to-leaf-replicate-pass-1 with has must constraint
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaf-to-leaf-replicate-pass-1  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">replicate3</check-leaf-to-leaf-replicate-pass-1>"
                + "  </tm-root>"
                + "</validation-yang11>";

        String ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                        + "<validation11:tm-root>"
                        + "<validation-augment11-replicate-namespace1:check-leaf-to-leaf-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</validation-augment11-replicate-namespace1:check-leaf-to-leaf-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaf-to-leaf-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate2</validation-augment11-replicate-namespace2:check-leaf-to-leaf-replicate>"
                        + "<validation-augment11:check-leaf-to-leaf-replicate-pass-1 xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">replicate3</validation-augment11:check-leaf-to-leaf-replicate-pass-1>"
                        + "</validation11:tm-root>"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "</validation11:validation-yang11>"
                        + "</data>"
                        + "</rpc-reply>";
        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        // modify leaf check-leaf-to-leaf-replicate (in namespace1) to make leaf check-leaf-to-leaf-replicate-pass-1 which has must constraint - to fail in must constraint
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaf-to-leaf-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate5</check-leaf-to-leaf-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
                "Violate must constraints: /validation11:validation-yang11/validation11:tm-root/validation-augment11-replicate-namespace1:check-leaf-to-leaf-replicate='replicate1' and current()/../validation-augment11-replicate-namespace2:check-leaf-to-leaf-replicate='replicate2'",
                "/validation11:validation-yang11/validation11:tm-root/validation-augment11:check-leaf-to-leaf-replicate-pass-1");

    }

    @Test
    public void testLeaftoLeafReplicate3() throws Exception {
        setUpForTestLeaftoLeafReplicate();

        // create leaf check-leaf-to-leaf-replicate-pass-1 with has must constraint
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaf-to-leaf-replicate-pass-1  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">replicate3</check-leaf-to-leaf-replicate-pass-1>"
                + "  </tm-root>"
                + "</validation-yang11>";

        String ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                        + "<validation11:tm-root>"
                        + "<validation-augment11-replicate-namespace1:check-leaf-to-leaf-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</validation-augment11-replicate-namespace1:check-leaf-to-leaf-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaf-to-leaf-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate2</validation-augment11-replicate-namespace2:check-leaf-to-leaf-replicate>"
                        + "<validation-augment11:check-leaf-to-leaf-replicate-pass-1 xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">replicate3</validation-augment11:check-leaf-to-leaf-replicate-pass-1>"
                        + "</validation11:tm-root>"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "</validation11:validation-yang11>"
                        + "</data>"
                        + "</rpc-reply>";
        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        // delete leaf check-leaf-to-leaf-replicate (in namespace1) to make leaf check-leaf-to-leaf-replicate-pass-1 which has must constraint - to fail in must constraint
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaf-to-leaf-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"delete\"/>"
                + "  </tm-root>"
                + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
                "Violate must constraints: /validation11:validation-yang11/validation11:tm-root/validation-augment11-replicate-namespace1:check-leaf-to-leaf-replicate='replicate1' and current()/../validation-augment11-replicate-namespace2:check-leaf-to-leaf-replicate='replicate2'",
                "/validation11:validation-yang11/validation11:tm-root/validation-augment11:check-leaf-to-leaf-replicate-pass-1");

    }

    // NAMESPACE1:LEAF-LIST AND NAMESPACE2:LEAF-LIST TESTS

    /*
    Creates check-leaflist-to-leaflist-replicate for both the namespace(urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1
                        , urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2) under tm-root
     */
    public void setUpForTestLeafListtoLeafListReplicate() throws Exception {

        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaflist-to-leaflist-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</check-leaflist-to-leaflist-replicate>"
                + "    <check-leaflist-to-leaflist-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate2</check-leaflist-to-leaflist-replicate>"
                + "    <check-leaflist-to-leaflist-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate3</check-leaflist-to-leaflist-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        String ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "<validation11:tm-root>"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate2</validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate3</validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate>"
                        + "</validation11:tm-root>"
                        + "</validation11:validation-yang11>"
                        + "</data>"
                        + "</rpc-reply>";

        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaflist-to-leaflist-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate1</check-leaflist-to-leaflist-replicate>"
                + "    <check-leaflist-to-leaflist-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate3</check-leaflist-to-leaflist-replicate>"
                + "    <check-leaflist-to-leaflist-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate5</check-leaflist-to-leaflist-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "<validation11:tm-root>"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate2</validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate3</validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate1</validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate3</validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate5</validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate>"
                        + "</validation11:tm-root>"
                        + "</validation11:validation-yang11>"
                        + "</data>"
                        + "</rpc-reply>";

        sendEditConfigAndVerifyGet(requestXml, ncResponse);
    }

    @Test
    public void testLeafListtoLeafListReplicate1() throws Exception {
        setUpForTestLeafListtoLeafListReplicate();

        // create leaf check-leaflist-to-leaflist-replicate-pass-1 with has must constraint
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaflist-to-leaflist-replicate-pass-1  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">replicate10</check-leaflist-to-leaflist-replicate-pass-1>"
                + "  </tm-root>"
                + "</validation-yang11>";

        String ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "<validation11:tm-root>"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate2</validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate3</validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate1</validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate3</validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate5</validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11:check-leaflist-to-leaflist-replicate-pass-1 xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">replicate10</validation-augment11:check-leaflist-to-leaflist-replicate-pass-1>"
                        + "</validation11:tm-root>"
                        + "</validation11:validation-yang11>"
                        + "</data>"
                        + "</rpc-reply>";
        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        // create leaf check-leaflist-to-leaflist-replicate-fail-1 with has must constraint - wrong one
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaflist-to-leaflist-replicate-fail-1  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">replicate14</check-leaflist-to-leaflist-replicate-fail-1>"
                + "  </tm-root>"
                + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
                "Violate must constraints: ./../validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate='replicate4' and current()/../validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate='replicate5'",
                "/validation11:validation-yang11/validation11:tm-root/validation-augment11:check-leaflist-to-leaflist-replicate-fail-1");

    }

    @Test
    public void testLeafListtoLeafListReplicate2() throws Exception {
        setUpForTestLeafListtoLeafListReplicate();

        // create leaf check-leaflist-to-leaflist-replicate-pass-1 with has must constraint
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaflist-to-leaflist-replicate-pass-1  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">replicate10</check-leaflist-to-leaflist-replicate-pass-1>"
                + "  </tm-root>"
                + "</validation-yang11>";

        String ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "<validation11:tm-root>"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate2</validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate3</validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate1</validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate3</validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate5</validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate>"
                        + "<validation-augment11:check-leaflist-to-leaflist-replicate-pass-1 xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">replicate10</validation-augment11:check-leaflist-to-leaflist-replicate-pass-1>"
                        + "</validation11:tm-root>"
                        + "</validation11:validation-yang11>"
                        + "</data>"
                        + "</rpc-reply>";
        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        // delete leaf check-leaflist-to-leaflist-replicate : replicate2 (in namespace1) to make leaf check-leaflist-to-leaflist-replicate-pass-1 which has must constraint - to fail in must constraint
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaflist-to-leaflist-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"delete\">replicate2</check-leaflist-to-leaflist-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
                "Violate must constraints: ../validation-augment11-replicate-namespace1:check-leaflist-to-leaflist-replicate='replicate2' and current()/../validation-augment11-replicate-namespace2:check-leaflist-to-leaflist-replicate='replicate5'",
                "/validation11:validation-yang11/validation11:tm-root/validation-augment11:check-leaflist-to-leaflist-replicate-pass-1");

    }

    // NAMESPACE1:LEAF AND NAMESPACE2:LEAF-LIST TESTS

    /*
    Creates check-leaf-to-leaflist-replicate for both the namespace(urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1
                        , urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2) under tm-root
     */
    public void setUpForTestLeaftoLeafListReplicate() throws Exception {

        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaf-to-leaflist-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</check-leaf-to-leaflist-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        String ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "<validation11:tm-root>"
                        + "<validation-augment11-replicate-namespace1:check-leaf-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</validation-augment11-replicate-namespace1:check-leaf-to-leaflist-replicate>"
                        + "</validation11:tm-root>"
                        + "</validation11:validation-yang11>"
                        + "</data>"
                        + "</rpc-reply>";

        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaf-to-leaflist-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate1</check-leaf-to-leaflist-replicate>"
                + "    <check-leaf-to-leaflist-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate3</check-leaf-to-leaflist-replicate>"
                + "    <check-leaf-to-leaflist-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate5</check-leaf-to-leaflist-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "<validation11:tm-root>"
                        + "<validation-augment11-replicate-namespace1:check-leaf-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</validation-augment11-replicate-namespace1:check-leaf-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaf-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate1</validation-augment11-replicate-namespace2:check-leaf-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaf-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate3</validation-augment11-replicate-namespace2:check-leaf-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaf-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate5</validation-augment11-replicate-namespace2:check-leaf-to-leaflist-replicate>"
                        + "</validation11:tm-root>"
                        + "</validation11:validation-yang11>"
                        + "</data>"
                        + "</rpc-reply>";

        sendEditConfigAndVerifyGet(requestXml, ncResponse);
    }

    @Test
    public void testLeaftoLeafListReplicate1() throws Exception {
        setUpForTestLeaftoLeafListReplicate();

        // create leaf check-leaf-to-leaflist-replicate-pass-1 AND check-leaf-to-leaflist-replicate-pass-2 with has must constraint
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaf-to-leaflist-replicate-pass-1  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">replicate3</check-leaf-to-leaflist-replicate-pass-1>"
                + "    <check-leaf-to-leaflist-replicate-pass-2  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">replicate3</check-leaf-to-leaflist-replicate-pass-2>"
                + "  </tm-root>"
                + "</validation-yang11>";

        String ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                        + "<validation11:tm-root>"
                        + "<validation-augment11-replicate-namespace1:check-leaf-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</validation-augment11-replicate-namespace1:check-leaf-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaf-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate1</validation-augment11-replicate-namespace2:check-leaf-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaf-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate3</validation-augment11-replicate-namespace2:check-leaf-to-leaflist-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaf-to-leaflist-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">replicate5</validation-augment11-replicate-namespace2:check-leaf-to-leaflist-replicate>"
                        + "<validation-augment11:check-leaf-to-leaflist-replicate-pass-1 xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">replicate3</validation-augment11:check-leaf-to-leaflist-replicate-pass-1>"
                        + "<validation-augment11:check-leaf-to-leaflist-replicate-pass-2 xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">replicate3</validation-augment11:check-leaf-to-leaflist-replicate-pass-2>"
                        + "</validation11:tm-root>"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "</validation11:validation-yang11>"
                        + "</data>"
                        + "</rpc-reply>";
        sendEditConfigAndVerifyGet(requestXml, ncResponse);

		/* delete leaf check-leaf-to-leaflist-replicate : replicate1 (in namespace1) to make leaf check-leaf-to-leaflist-replicate-pass-2 which has must constraint - to fail in must constraint
		check-leaf-to-leaflist-replicate has 3 impact paths (all 3 impact paths from module "datastore-validator-augment-test11-replicate-namespace1")
			1) check-leaf-to-leaflist-replicate-pass-2
			2) check-leaf-to-leaflist-replicate-pass-1
			3) check-leaf-to-leaflist-replicate-fail-1

		this will fail in check-leaf-to-leaflist-replicate-pass-2, since its loaded to impact paths first
		*/
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaf-to-leaflist-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\"  xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"delete\">replicate1</check-leaf-to-leaflist-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
                "Violate must constraints: /validation11:validation-yang11/validation11:tm-root/validation-augment11-replicate-namespace1:check-leaf-to-leaflist-replicate='replicate1' and current()/../validation-augment11-replicate-namespace2:check-leaf-to-leaflist-replicate='replicate3'",
                "/validation11:validation-yang11/validation11:tm-root/validation-augment11:check-leaf-to-leaflist-replicate-pass-1");
    }

    @Test
    public void testLeaftoLeafListReplicate2() throws Exception {
        setUpForTestLeaftoLeafListReplicate();

        // create leaf check-leaf-to-leaflist-replicate-fail-1 with has must constraint
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaf-to-leaflist-replicate-fail-1  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">replicate3</check-leaf-to-leaflist-replicate-fail-1>"
                + "  </tm-root>"
                + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
                "Violate must constraints: ../validation-augment11-replicate-namespace1:check-leaf-to-leaflist-replicate='replicate3' and ../validation-augment11-replicate-namespace2:check-leaf-to-leaflist-replicate='replicate2'",
                "/validation11:validation-yang11/validation11:tm-root/validation-augment11:check-leaf-to-leaflist-replicate-fail-1");
    }

    // NAMESPACE1:LEAF AND NAMESPACE2:LIST TESTS

    /*
    Creates check-leaf-to-list-replicate for both the namespace(urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1
                        , urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2) under tm-root
     */
    public void setUpForTestLeaftoListReplicate() throws Exception {

        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaf-to-list-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate100</check-leaf-to-list-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        String ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "<validation11:tm-root>"
                        + "<validation-augment11-replicate-namespace1:check-leaf-to-list-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate100</validation-augment11-replicate-namespace1:check-leaf-to-list-replicate>"
                        + "</validation11:tm-root>"
                        + "</validation11:validation-yang11>"
                        + "</data>"
                        + "</rpc-reply>";

        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    	<check-leaf-to-list-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
                + "			<iamthekey>key1</iamthekey>"
                + "			<someproperty>10</someproperty>"
                + "		</check-leaf-to-list-replicate>"
                + "    	<check-leaf-to-list-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
                + "			<iamthekey>key2</iamthekey>"
                + "			<someproperty>15</someproperty>"
                + "		</check-leaf-to-list-replicate>"
                + "    	<check-leaf-to-list-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
                + "			<iamthekey>key3</iamthekey>"
                + "			<someproperty>20</someproperty>"
                + "		</check-leaf-to-list-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        ncResponse =
                " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                        + "<data>"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "<validation11:tm-root>"
                        + "<validation-augment11-replicate-namespace1:check-leaf-to-list-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate100</validation-augment11-replicate-namespace1:check-leaf-to-list-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaf-to-list-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
                        + "<validation-augment11-replicate-namespace2:iamthekey>key1</validation-augment11-replicate-namespace2:iamthekey>"
                        + "<validation-augment11-replicate-namespace2:someproperty>10</validation-augment11-replicate-namespace2:someproperty>"
                        + "</validation-augment11-replicate-namespace2:check-leaf-to-list-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaf-to-list-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
                        + "<validation-augment11-replicate-namespace2:iamthekey>key2</validation-augment11-replicate-namespace2:iamthekey>"
                        + "<validation-augment11-replicate-namespace2:someproperty>15</validation-augment11-replicate-namespace2:someproperty>"
                        + "</validation-augment11-replicate-namespace2:check-leaf-to-list-replicate>"
                        + "<validation-augment11-replicate-namespace2:check-leaf-to-list-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
                        + "<validation-augment11-replicate-namespace2:iamthekey>key3</validation-augment11-replicate-namespace2:iamthekey>"
                        + "<validation-augment11-replicate-namespace2:someproperty>20</validation-augment11-replicate-namespace2:someproperty>"
                        + "</validation-augment11-replicate-namespace2:check-leaf-to-list-replicate>"
                        + "</validation11:tm-root>"
                        + "</validation11:validation-yang11>"
                        + "</data>"
                        + "</rpc-reply>";

        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        // create leaf check-leaf-to-list-replicate-pass-1 AND check-leaf-to-list-replicate-pass-2 with has must constraint
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaf-to-list-replicate-pass-1  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">replicate3</check-leaf-to-list-replicate-pass-1>"
                + "    <check-leaf-to-list-replicate-pass-2  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">key1</check-leaf-to-list-replicate-pass-2>"
                + "  </tm-root>"
                + "</validation-yang11>";

        ncResponse =
                " <rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                        + "<data>\n"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "<validation11:tm-root>\n"
                        + "<validation-augment11-replicate-namespace1:check-leaf-to-list-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate100</validation-augment11-replicate-namespace1:check-leaf-to-list-replicate>\n"
                        + "<validation-augment11-replicate-namespace2:check-leaf-to-list-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">\n"
                        + "<validation-augment11-replicate-namespace2:iamthekey>key1</validation-augment11-replicate-namespace2:iamthekey>\n"
                        + "<validation-augment11-replicate-namespace2:someproperty>10</validation-augment11-replicate-namespace2:someproperty>\n"
                        + "</validation-augment11-replicate-namespace2:check-leaf-to-list-replicate>\n"
                        + "<validation-augment11-replicate-namespace2:check-leaf-to-list-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">\n"
                        + "<validation-augment11-replicate-namespace2:iamthekey>key2</validation-augment11-replicate-namespace2:iamthekey>\n"
                        + "<validation-augment11-replicate-namespace2:someproperty>15</validation-augment11-replicate-namespace2:someproperty>\n"
                        + "</validation-augment11-replicate-namespace2:check-leaf-to-list-replicate>\n"
                        + "<validation-augment11-replicate-namespace2:check-leaf-to-list-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">\n"
                        + "<validation-augment11-replicate-namespace2:iamthekey>key3</validation-augment11-replicate-namespace2:iamthekey>\n"
                        + "<validation-augment11-replicate-namespace2:someproperty>20</validation-augment11-replicate-namespace2:someproperty>\n"
                        + "</validation-augment11-replicate-namespace2:check-leaf-to-list-replicate>\n"
                        + "<validation-augment11:check-leaf-to-list-replicate-pass-1 xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">replicate3</validation-augment11:check-leaf-to-list-replicate-pass-1>\n"
                        + "<validation-augment11:check-leaf-to-list-replicate-pass-2 xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">key1</validation-augment11:check-leaf-to-list-replicate-pass-2>\n"
                        + "</validation11:tm-root>\n"
                        + "</validation11:validation-yang11>\n"
                        + "</data>\n"
                        + "</rpc-reply>";
        sendEditConfigAndVerifyGet(requestXml, ncResponse);
    }

    @Test
    public void testLeaftoListReplicate1() throws Exception {
        setUpForTestLeaftoListReplicate();

		/* modify leaf check-leaf-to-list-replicate : key2 (in namespace1) to make leaf check-leaf-to-list-replicate-pass-1 which has must constraint - to fail in must constraint
		check-leaf-to-list-replicate-pass-1 expects some entry in list to have value of someproperty as 15
		*/
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    	<check-leaf-to-list-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
                + "			<iamthekey>key2</iamthekey>"
                + "			<someproperty>20</someproperty>"
                + "		</check-leaf-to-list-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
                "Violate must constraints: /validation11:validation-yang11/validation11:tm-root/validation-augment11-replicate-namespace1:check-leaf-to-list-replicate='replicate100' and current()/../validation-augment11-replicate-namespace2:check-leaf-to-list-replicate/someproperty='15'",
                "/validation11:validation-yang11/validation11:tm-root/validation-augment11:check-leaf-to-list-replicate-pass-1");
    }

    @Test
    public void testLeaftoListReplicate2() throws Exception {
        setUpForTestLeaftoListReplicate();

		/* modify leaf check-leaf-to-list-replicate : key1 (in namespace1) to make leaf check-leaf-to-list-replicate-pass-2 which has must constraint - to fail in must constraint
		check-leaf-to-list-replicate-pass-2 expects key1 to have value of someproperty as 10
		*/
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    	<check-leaf-to-list-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
                + "			<iamthekey>key1</iamthekey>"
                + "			<someproperty>11</someproperty>"
                + "		</check-leaf-to-list-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
                "Violate must constraints: /validation11:validation-yang11/validation11:tm-root/validation-augment11-replicate-namespace1:check-leaf-to-list-replicate='replicate100' and current()/../validation-augment11-replicate-namespace2:check-leaf-to-list-replicate[iamthekey=current()]/someproperty='10'",
                "/validation11:validation-yang11/validation11:tm-root/validation-augment11:check-leaf-to-list-replicate-pass-2");
    }

    @Test
    public void testLeaftoListReplicate3() throws Exception {
        setUpForTestLeaftoListReplicate();

        // create leaf check-leaf-to-list-replicate-fail-1 with has must constraint
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaf-to-list-replicate-fail-1  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">replicate3</check-leaf-to-list-replicate-fail-1>"
                + "  </tm-root>"
                + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
                "Violate must constraints: ../validation-augment11-replicate-namespace1:check-leaf-to-list-replicate='replicate1' and ../validation-augment11-replicate-namespace2:check-leaf-to-list-replicate/someproperty='15'",
                "/validation11:validation-yang11/validation11:tm-root/validation-augment11:check-leaf-to-list-replicate-fail-1");
    }

    // NAMESPACE1:CONTAINER AND NAMESPACE2:CONTAINER TESTS

    /*
    Creates check-container-to-container-replicate for both the namespace(urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1
                        , urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2) under tm-root
     */
    @Test
    public void testContainertoContainerReplicate1() throws Exception {

        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-container-to-container-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">"
                + "         <somePropertyContainerNameSpace1>4</somePropertyContainerNameSpace1>"
                + "    </check-container-to-container-replicate>"
                + "    <check-container-to-container-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
                + "         <somePropertyContainerNameSpace2>18</somePropertyContainerNameSpace2>"
                + "    </check-container-to-container-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        String ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                        + "<data>\n"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                        + "<validation11:tm-root>\n"
                        + "<validation-augment11-replicate-namespace1:check-container-to-container-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">\n"
                        + "<validation-augment11-replicate-namespace1:somePropertyContainerNameSpace1>4</validation-augment11-replicate-namespace1:somePropertyContainerNameSpace1>\n"
                        + "</validation-augment11-replicate-namespace1:check-container-to-container-replicate>\n"
                        + "<validation-augment11-replicate-namespace2:check-container-to-container-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">\n"
                        + "<validation-augment11-replicate-namespace2:somePropertyContainerNameSpace2>18</validation-augment11-replicate-namespace2:somePropertyContainerNameSpace2>\n"
                        + "</validation-augment11-replicate-namespace2:check-container-to-container-replicate>\n"
                        + "</validation11:tm-root>\n"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "</validation11:validation-yang11>\n"
                        + "</data>\n"
                        + "</rpc-reply>";

        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        // create leaf check-container-to-container-replicate with has must constraint
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-container-to-container-replicate  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">replicate3</check-container-to-container-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                        + "<data>\n"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "<validation11:tm-root>\n"
                        + "<validation-augment11-replicate-namespace1:check-container-to-container-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">\n"
                        + "<validation-augment11-replicate-namespace1:somePropertyContainerNameSpace1>4</validation-augment11-replicate-namespace1:somePropertyContainerNameSpace1>\n"
                        + "</validation-augment11-replicate-namespace1:check-container-to-container-replicate>\n"
                        + "<validation-augment11-replicate-namespace2:check-container-to-container-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">\n"
                        + "<validation-augment11-replicate-namespace2:somePropertyContainerNameSpace2>18</validation-augment11-replicate-namespace2:somePropertyContainerNameSpace2>\n"
                        + "</validation-augment11-replicate-namespace2:check-container-to-container-replicate>\n"
                        + "<validation-augment11:check-container-to-container-replicate xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">replicate3</validation-augment11:check-container-to-container-replicate>\n"
                        + "</validation11:tm-root>\n"
                        + "</validation11:validation-yang11>\n"
                        + "</data>\n"
                        + "</rpc-reply>";

        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-container-to-container-replicate-fail1  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">replicate3</check-container-to-container-replicate-fail1>"
                + "  </tm-root>"
                + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
                "Violate must constraints: ../validation-augment11-replicate-namespace1:check-container-to-container-replicate/somePropertyContainerNameSpace1='18' and ../validation-augment11-replicate-namespace2:check-container-to-container-replicate/somePropertyContainerNameSpace2='4'",
                "/validation11:validation-yang11/validation11:tm-root/validation-augment11:check-container-to-container-replicate-fail1");
    }

    public void setUpForTestLeafListToContainerReplicate() throws Exception {

        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaflist-to-container-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</check-leaflist-to-container-replicate>"
                + "    <check-leaflist-to-container-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate2</check-leaflist-to-container-replicate>"
                + "    <check-leaflist-to-container-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate3</check-leaflist-to-container-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        String ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                        + "<data>\n"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "<validation11:tm-root>\n"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate>\n"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate2</validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate>\n"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate3</validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate>\n"
                        + "</validation11:tm-root>\n"
                        + "</validation11:validation-yang11>\n"
                        + "</data>\n"
                        + "</rpc-reply>";

        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaflist-to-container-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
                + "         <someproperty>5</someproperty>"
                + "    </check-leaflist-to-container-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                        + "<data>\n"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "<validation11:tm-root>\n"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate>\n"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate2</validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate>\n"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate3</validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate>\n"
                        + "<validation-augment11-replicate-namespace2:check-leaflist-to-container-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">\n"
                        + "<validation-augment11-replicate-namespace2:someproperty>5</validation-augment11-replicate-namespace2:someproperty>\n"
                        + "</validation-augment11-replicate-namespace2:check-leaflist-to-container-replicate>\n"
                        + "</validation11:tm-root>\n"
                        + "</validation11:validation-yang11>\n"
                        + "</data>\n"
                        + "</rpc-reply>";

        sendEditConfigAndVerifyGet(requestXml, ncResponse);
    }

    @Test
    public void testLeafListtoContainerReplicate() throws Exception {
        setUpForTestLeafListToContainerReplicate();

        // create leaf check-leaflist-to-container-replicate-pass-1 with has must constraint
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaflist-to-container-replicate-pass-1  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">replicate10</check-leaflist-to-container-replicate-pass-1>"
                + "  </tm-root>"
                + "</validation-yang11>";

        String ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                        + "<data>\n"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "<validation11:tm-root>\n"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate1</validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate>\n"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate2</validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate>\n"
                        + "<validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">replicate3</validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate>\n"
                        + "<validation-augment11-replicate-namespace2:check-leaflist-to-container-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">\n"
                        + "<validation-augment11-replicate-namespace2:someproperty>5</validation-augment11-replicate-namespace2:someproperty>\n"
                        + "</validation-augment11-replicate-namespace2:check-leaflist-to-container-replicate>\n"
                        + "<validation-augment11:check-leaflist-to-container-replicate-pass-1 xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">replicate10</validation-augment11:check-leaflist-to-container-replicate-pass-1>\n"
                        + "</validation11:tm-root>\n"
                        + "</validation11:validation-yang11>\n"
                        + "</data>\n"
                        + "</rpc-reply>";
        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        // change value of someproperty to 4, to fail must contraint of check-leaflist-to-container-replicate-pass-1
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaflist-to-container-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
                + "         <someproperty>4</someproperty>"
                + "    </check-leaflist-to-container-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
                "Violate must constraints: ../validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate='replicate1' and ../validation-augment11-replicate-namespace2:check-leaflist-to-container-replicate/someproperty='5'",
                "/validation11:validation-yang11/validation11:tm-root/validation-augment11:check-leaflist-to-container-replicate-pass-1");


    }

    @Test
    public void testLeafListtoContainerReplicate2() throws Exception {
        setUpForTestLeafListToContainerReplicate();

        // create leaf check-leaflist-to-container-replicate-pass-1 with has must constraint
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-leaflist-to-container-replicate-fail-1  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">replicate10</check-leaflist-to-container-replicate-fail-1>"
                + "  </tm-root>"
                + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
                "Violate must constraints: ../validation-augment11-replicate-namespace1:check-leaflist-to-container-replicate='replicate1' and ../validation-augment11-replicate-namespace2:check-leaflist-to-container-replicate/someproperty='4'",
                "/validation11:validation-yang11/validation11:tm-root/validation-augment11:check-leaflist-to-container-replicate-fail-1");


    }

    @Test
    public void testContainerToListReplicateFailBecauseOfMissingPrefix() throws Exception {

        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
            + "  <tm-root>"
            + "    <check-container-to-list-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">"
            + "         <someproperty>4</someproperty>"
            + "    </check-container-to-list-replicate>"
            + "    <check-container-to-list-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
            + "			<iamthekey>key1</iamthekey>"
            + "			<someproperty>10</someproperty>"
            + "    </check-container-to-list-replicate>"
            + "  </tm-root>"
            + "</validation-yang11>";

        String ncResponse =
            "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                + "<data>\n"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                + "<validation11:tm-root>\n"
                + "<validation-augment11-replicate-namespace1:check-container-to-list-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">\n"
                + "<validation-augment11-replicate-namespace1:someproperty>4</validation-augment11-replicate-namespace1:someproperty>\n"
                + "</validation-augment11-replicate-namespace1:check-container-to-list-replicate>\n"
                + "<validation-augment11-replicate-namespace2:check-container-to-list-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">\n"
                + "<validation-augment11-replicate-namespace2:iamthekey>key1</validation-augment11-replicate-namespace2:iamthekey>\n"
                + "<validation-augment11-replicate-namespace2:someproperty>10</validation-augment11-replicate-namespace2:someproperty>\n"
                + "</validation-augment11-replicate-namespace2:check-container-to-list-replicate>\n"
                + "</validation11:tm-root>\n"
                + "<validation11:container-with-must>"
                + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                + "</validation11:container-with-must>"
                + "</validation11:validation-yang11>\n"
                + "</data>\n"
                + "</rpc-reply>";

        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
            + "  <tm-root>"
            + "    <check-container-to-list-replicate-fail-2  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">key1</check-container-to-list-replicate-fail-2>"
            + "  </tm-root>"
            + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
            "Violate must constraints: ../validation-augment11-replicate-namespace1:check-container-to-list-replicate/someproperty='4' and current()/../validation-augment11-replicate-namespace2:check-container-to-list-replicate[iamthekey=current()]/someproperty='10'",
            "/validation11:validation-yang11/validation11:tm-root/validation-augment11:check-container-to-list-replicate-fail-2");
    }

    @Test
    public void testContainerToListReplicate() throws Exception {

        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-container-to-list-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">"
                + "         <someproperty>4</someproperty>"
                + "    </check-container-to-list-replicate>"
                + "    <check-container-to-list-replicate  xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
                + "			<iamthekey>key1</iamthekey>"
                + "			<someproperty>10</someproperty>"
                + "    </check-container-to-list-replicate>"
                + "  </tm-root>"
                + "</validation-yang11>";

        String ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                        + "<data>\n"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                        + "<validation11:tm-root>\n"
                        + "<validation-augment11-replicate-namespace1:check-container-to-list-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">\n"
                        + "<validation-augment11-replicate-namespace1:someproperty>4</validation-augment11-replicate-namespace1:someproperty>\n"
                        + "</validation-augment11-replicate-namespace1:check-container-to-list-replicate>\n"
                        + "<validation-augment11-replicate-namespace2:check-container-to-list-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">\n"
                        + "<validation-augment11-replicate-namespace2:iamthekey>key1</validation-augment11-replicate-namespace2:iamthekey>\n"
                        + "<validation-augment11-replicate-namespace2:someproperty>10</validation-augment11-replicate-namespace2:someproperty>\n"
                        + "</validation-augment11-replicate-namespace2:check-container-to-list-replicate>\n"
                        + "</validation11:tm-root>\n"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "</validation11:validation-yang11>\n"
                        + "</data>\n"
                        + "</rpc-reply>";

        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-container-to-list-replicate-pass-1  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">key1</check-container-to-list-replicate-pass-1>"
                + "  </tm-root>"
                + "</validation-yang11>";

        ncResponse =
                "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                        + "<data>\n"
                        + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                        + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                        + "<validation11:container-with-must>"
                        + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                        + "</validation11:container-with-must>"
                        + "<validation11:tm-root>\n"
                        + "<validation-augment11-replicate-namespace1:check-container-to-list-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">\n"
                        + "<validation-augment11-replicate-namespace1:someproperty>4</validation-augment11-replicate-namespace1:someproperty>\n"
                        + "</validation-augment11-replicate-namespace1:check-container-to-list-replicate>\n"
                        + "<validation-augment11-replicate-namespace2:check-container-to-list-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">\n"
                        + "<validation-augment11-replicate-namespace2:iamthekey>key1</validation-augment11-replicate-namespace2:iamthekey>\n"
                        + "<validation-augment11-replicate-namespace2:someproperty>10</validation-augment11-replicate-namespace2:someproperty>\n"
                        + "</validation-augment11-replicate-namespace2:check-container-to-list-replicate>\n"
                        + "<validation-augment11:check-container-to-list-replicate-pass-1 xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">key1</validation-augment11:check-container-to-list-replicate-pass-1>\n"
                        + "</validation11:tm-root>\n"
                        + "</validation11:validation-yang11>\n"
                        + "</data>\n"
                        + "</rpc-reply>";

        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
                + "  <tm-root>"
                + "    <check-container-to-list-replicate-fail-1  xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">key1</check-container-to-list-replicate-fail-1>"
                + "  </tm-root>"
                + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
                "Violate must constraints: ../validation-augment11-replicate-namespace1:check-container-to-list-replicate/someproperty='18' and current()/../validation-augment11-replicate-namespace2:check-container-to-list-replicate[iamthekey=current()]/someproperty='10'",
                "/validation11:validation-yang11/validation11:tm-root/validation-augment11:check-container-to-list-replicate-fail-1");

    }

    @Test
    public void testCoreFunctionConcatWithTwoLeafAndBoolean()
        throws Exception {
        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
            + "  <tm-root>"
            + "    <leafforcorefunction xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">A</leafforcorefunction>"
            + "    <leafforcorefunction xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">A</leafforcorefunction>"
            + "  </tm-root>"
            + "</validation-yang11>";

        String ncResponse =
            "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                + "<data>\n"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                + "<validation11:container-with-must>"
                + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                + "</validation11:container-with-must>"
                + "<validation11:tm-root>\n"
                + "<validation-augment11-replicate-namespace1:leafforcorefunction xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">A</validation-augment11-replicate-namespace1:leafforcorefunction>\n"
                + "<validation-augment11-replicate-namespace2:leafforcorefunction xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">A</validation-augment11-replicate-namespace2:leafforcorefunction>\n"
                + "</validation11:tm-root>\n"
                + "</validation11:validation-yang11>\n"
                + "</data>\n"
                + "</rpc-reply>";
        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
            + "  <tm-root>"
            + "    <concat-function-leaf xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">AA</concat-function-leaf>"
            + "  </tm-root>"
            + "</validation-yang11>";

        ncResponse =
            "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                + "<data>\n"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                + "<validation11:container-with-must>"
                + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                + "</validation11:container-with-must>"
                + "<validation11:tm-root>\n"
                + "<validation-augment11:concat-function-leaf xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">AA</validation-augment11:concat-function-leaf>\n"
                + "<validation-augment11-replicate-namespace1:leafforcorefunction xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">A</validation-augment11-replicate-namespace1:leafforcorefunction>\n"
                + "<validation-augment11-replicate-namespace2:leafforcorefunction xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">A</validation-augment11-replicate-namespace2:leafforcorefunction>\n"
                + "</validation11:tm-root>\n"
                + "</validation11:validation-yang11>\n"
                + "</data>\n"
                + "</rpc-reply>";
        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
            + "  <tm-root>"
            + "    <bool-function-leaf-namespace1 xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">AA</bool-function-leaf-namespace1>"
            + "    <bool-function-leaf-namespace2 xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">AA</bool-function-leaf-namespace2>"
            + "  </tm-root>"
            + "</validation-yang11>";

        ncResponse =
            "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                + "<data>\n"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                + "<validation11:container-with-must>"
                + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                + "</validation11:container-with-must>"
                + "<validation11:tm-root>\n"
                + "<validation-augment11:bool-function-leaf-namespace1 xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">AA</validation-augment11:bool-function-leaf-namespace1>\n"
                + "<validation-augment11:bool-function-leaf-namespace2 xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">AA</validation-augment11:bool-function-leaf-namespace2>\n"
                + "<validation-augment11:concat-function-leaf xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">AA</validation-augment11:concat-function-leaf>\n"
                + "<validation-augment11-replicate-namespace1:leafforcorefunction xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">A</validation-augment11-replicate-namespace1:leafforcorefunction>\n"
                + "<validation-augment11-replicate-namespace2:leafforcorefunction xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">A</validation-augment11-replicate-namespace2:leafforcorefunction>\n"
                + "</validation11:tm-root>\n"
                + "</validation11:validation-yang11>\n"
                + "</data>\n"
                + "</rpc-reply>";
        sendEditConfigAndVerifyGet(requestXml, ncResponse);
    }

    @Test
    public void testCoreFunction_boolean_fail_for_bool_function_leaf_namespace2()
        throws Exception {
        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
            + "  <tm-root>"
            + "    <leafforcorefunction xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">A</leafforcorefunction>"
            + "  </tm-root>"
            + "</validation-yang11>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        String ncResponse =
            "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                + "<data>\n"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                + "<validation11:container-with-must>"
                + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                + "</validation11:container-with-must>"
                + "<validation11:tm-root>\n"
                + "<validation-augment11-replicate-namespace1:leafforcorefunction xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">A</validation-augment11-replicate-namespace1:leafforcorefunction>\n"
                + "</validation11:tm-root>\n"
                + "</validation11:validation-yang11>\n"
                + "</data>\n"
                + "</rpc-reply>";
        verifyGet(ncResponse);


        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
            + "  <tm-root>"
            + "    <bool-function-leaf-namespace1 xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">AA</bool-function-leaf-namespace1>"
            + "  </tm-root>"
            + "</validation-yang11>";

        ncResponse =
            "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                + "<data>\n"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                + "<validation11:container-with-must>"
                + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                + "</validation11:container-with-must>"
                + "<validation11:tm-root>\n"
                + "<validation-augment11:bool-function-leaf-namespace1 xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">AA</validation-augment11:bool-function-leaf-namespace1>\n"
                + "<validation-augment11-replicate-namespace1:leafforcorefunction xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">A</validation-augment11-replicate-namespace1:leafforcorefunction>\n"
                + "</validation11:tm-root>\n"
                + "</validation11:validation-yang11>\n"
                + "</data>\n"
                + "</rpc-reply>";
        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
            + "  <tm-root>"
            + "    <bool-function-leaf-namespace2 xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">AA</bool-function-leaf-namespace2>"
            + "  </tm-root>"
            + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
            "Violate must constraints: boolean(/validation11:validation-yang11/validation11:tm-root/validation-augment11-replicate-namespace2:leafforcorefunction)",
            "/validation11:validation-yang11/validation11:tm-root/validation-augment11:bool-function-leaf-namespace2");
    }

    @Test
    public void testCoreFunction_boolean_fail_for_bool_function_leaf_namespace1()
        throws Exception {
        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
            + "  <tm-root>"
            + "    <leafforcorefunction xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">A</leafforcorefunction>"
            + "  </tm-root>"
            + "</validation-yang11>";

        String ncResponse =
            "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                + "<data>\n"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                + "<validation11:tm-root>\n"
                + "<validation-augment11-replicate-namespace2:leafforcorefunction xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">A</validation-augment11-replicate-namespace2:leafforcorefunction>\n"
                + "</validation11:tm-root>\n"
                + "<validation11:container-with-must>"
                + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                + "</validation11:container-with-must>"
                + "</validation11:validation-yang11>\n"
                + "</data>\n"
                + "</rpc-reply>";
        sendEditConfigAndVerifyGet(requestXml, ncResponse);


        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
            + "  <tm-root>"
            + "    <bool-function-leaf-namespace2 xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">AA</bool-function-leaf-namespace2>"
            + "  </tm-root>"
            + "</validation-yang11>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        ncResponse =
            "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                + "<data>\n"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                + "<validation11:tm-root>\n"
                + "<validation-augment11:bool-function-leaf-namespace2 xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">AA</validation-augment11:bool-function-leaf-namespace2>\n"
                + "<validation-augment11-replicate-namespace2:leafforcorefunction xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">A</validation-augment11-replicate-namespace2:leafforcorefunction>\n"
                + "</validation11:tm-root>\n"
                + "<validation11:container-with-must>"
                + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                + "</validation11:container-with-must>"
                + "</validation11:validation-yang11>\n"
                + "</data>\n"
                + "</rpc-reply>";
        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
            + "  <tm-root>"
            + "    <bool-function-leaf-namespace1 xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">AA</bool-function-leaf-namespace1>"
            + "  </tm-root>"
            + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
            "Violate must constraints: boolean(current()/../validation-augment11-replicate-namespace1:leafforcorefunction)",
            "/validation11:validation-yang11/validation11:tm-root/validation-augment11:bool-function-leaf-namespace1");
    }

    public void setupCoreFunctionCountBetweenLeafListAndList()
        throws Exception {
        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
            + "  <tm-root>"
            + "    <check-leaflist-to-list-replicate xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">A</check-leaflist-to-list-replicate>"
            + "    <check-leaflist-to-list-replicate xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">B</check-leaflist-to-list-replicate>"
            + "  </tm-root>"
            + "</validation-yang11>";

        String ncResponse =
            "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                + "<data>\n"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                + "<validation11:container-with-must>"
                + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                + "</validation11:container-with-must>"
                + "<validation11:tm-root>\n"
                + "<validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">A</validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate>\n"
                + "<validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">B</validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate>\n"
                + "</validation11:tm-root>\n"
                + "</validation11:validation-yang11>\n"
                + "</data>\n"
                + "</rpc-reply>";
        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
            + "  <tm-root>"
            + "    <check-leaflist-to-list-replicate xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
            + "			<iamthekey>key1</iamthekey>"
            + "			<someproperty>10</someproperty>"
            + "    </check-leaflist-to-list-replicate>"
            + "    <check-leaflist-to-list-replicate xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
            + "			<iamthekey>key2</iamthekey>"
            + "			<someproperty>11</someproperty>"
            + "    </check-leaflist-to-list-replicate>"
            + "    <check-leaflist-to-list-replicate xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">"
            + "			<iamthekey>key3</iamthekey>"
            + "			<someproperty>12</someproperty>"
            + "    </check-leaflist-to-list-replicate>"
            + "  </tm-root>"
            + "</validation-yang11>";

        ncResponse =
            "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                + "<data>\n"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                + "<validation11:container-with-must>"
                + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                + "</validation11:container-with-must>"
                + "<validation11:tm-root>\n"
                + "<validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">A</validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate>\n"
                + "<validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">B</validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate>\n"
                + "<validation-augment11-replicate-namespace2:check-leaflist-to-list-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">\n"
                + "<validation-augment11-replicate-namespace2:iamthekey>key1</validation-augment11-replicate-namespace2:iamthekey>\n"
                + "<validation-augment11-replicate-namespace2:someproperty>10</validation-augment11-replicate-namespace2:someproperty>\n"
                + "</validation-augment11-replicate-namespace2:check-leaflist-to-list-replicate>\n"
                + "<validation-augment11-replicate-namespace2:check-leaflist-to-list-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">\n"
                + "<validation-augment11-replicate-namespace2:iamthekey>key2</validation-augment11-replicate-namespace2:iamthekey>\n"
                + "<validation-augment11-replicate-namespace2:someproperty>11</validation-augment11-replicate-namespace2:someproperty>\n"
                + "</validation-augment11-replicate-namespace2:check-leaflist-to-list-replicate>\n"
                + "<validation-augment11-replicate-namespace2:check-leaflist-to-list-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">\n"
                + "<validation-augment11-replicate-namespace2:iamthekey>key3</validation-augment11-replicate-namespace2:iamthekey>\n"
                + "<validation-augment11-replicate-namespace2:someproperty>12</validation-augment11-replicate-namespace2:someproperty>\n"
                + "</validation-augment11-replicate-namespace2:check-leaflist-to-list-replicate>\n"
                + "</validation11:tm-root>\n"
                + "</validation11:validation-yang11>\n"
                + "</data>\n"
                + "</rpc-reply>";
        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
            + "  <tm-root>"
            + "    <check-leaflist-to-list-replicate-count-namespace1 xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">AA</check-leaflist-to-list-replicate-count-namespace1>"
            + "    <check-leaflist-to-list-replicate-count-namespace2 xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">BB</check-leaflist-to-list-replicate-count-namespace2>"
            + "  </tm-root>"
            + "</validation-yang11>";

        ncResponse =
            "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                + "<data>\n"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                + "<validation11:container-with-must>"
                + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                + "</validation11:container-with-must>"
                + "<validation11:tm-root>\n"
                + "<validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">A</validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate>\n"
                + "<validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">B</validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate>\n"
                + "<validation-augment11-replicate-namespace2:check-leaflist-to-list-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">\n"
                + "<validation-augment11-replicate-namespace2:iamthekey>key1</validation-augment11-replicate-namespace2:iamthekey>\n"
                + "<validation-augment11-replicate-namespace2:someproperty>10</validation-augment11-replicate-namespace2:someproperty>\n"
                + "</validation-augment11-replicate-namespace2:check-leaflist-to-list-replicate>\n"
                + "<validation-augment11-replicate-namespace2:check-leaflist-to-list-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">\n"
                + "<validation-augment11-replicate-namespace2:iamthekey>key2</validation-augment11-replicate-namespace2:iamthekey>\n"
                + "<validation-augment11-replicate-namespace2:someproperty>11</validation-augment11-replicate-namespace2:someproperty>\n"
                + "</validation-augment11-replicate-namespace2:check-leaflist-to-list-replicate>\n"
                + "<validation-augment11-replicate-namespace2:check-leaflist-to-list-replicate xmlns:validation-augment11-replicate-namespace2=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\">\n"
                + "<validation-augment11-replicate-namespace2:iamthekey>key3</validation-augment11-replicate-namespace2:iamthekey>\n"
                + "<validation-augment11-replicate-namespace2:someproperty>12</validation-augment11-replicate-namespace2:someproperty>\n"
                + "</validation-augment11-replicate-namespace2:check-leaflist-to-list-replicate>\n"
                + "<validation-augment11:check-leaflist-to-list-replicate-count-namespace1 xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">AA</validation-augment11:check-leaflist-to-list-replicate-count-namespace1>\n"
                + "<validation-augment11:check-leaflist-to-list-replicate-count-namespace2 xmlns:validation-augment11=\"urn:opendaylight:datastore-validator-augment-test11\">BB</validation-augment11:check-leaflist-to-list-replicate-count-namespace2>\n"
                + "</validation11:tm-root>\n"
                + "</validation11:validation-yang11>\n"
                + "</data>\n"
                + "</rpc-reply>";
        sendEditConfigAndVerifyGet(requestXml, ncResponse);
    }

    @Test
    public void testCoreFunctionCountBetweenLeafListAndListFailCountNameSpace1()
        throws Exception {
        setupCoreFunctionCountBetweenLeafListAndList();

        // delete an entry in namespace1 to make impact validation fail on count
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
            + "  <tm-root>"
            + "    <check-leaflist-to-list-replicate xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"delete\">A</check-leaflist-to-list-replicate>"
            + "  </tm-root>"
            + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
            "Violate must constraints: count(/validation11:validation-yang11/validation11:tm-root/validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate) = 2",
            "/validation11:validation-yang11/validation11:tm-root/validation-augment11:check-leaflist-to-list-replicate-count-namespace1");
    }

    @Test
    public void testCoreFunctionCountBetweenLeafListAndListFailCountNameSpace2()
        throws Exception {
        setupCoreFunctionCountBetweenLeafListAndList();

        // delete an entry in namespace2 to make impact validation fail on count
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
            + "  <tm-root>"
            + "    <check-leaflist-to-list-replicate xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace2\" xmlns:xc=\"urn:ietf:params:xml:ns:netconf:base:1.0\" xc:operation=\"delete\">"
            + "         <iamthekey>key1</iamthekey>"
            + "    </check-leaflist-to-list-replicate>"
            + "  </tm-root>"
            + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
            "Violate must constraints: count(../validation-augment11-replicate-namespace2:check-leaflist-to-list-replicate) = 3",
            "/validation11:validation-yang11/validation11:tm-root/validation-augment11:check-leaflist-to-list-replicate-count-namespace2");
    }

    @Test
    public void testCoreFunctionCountBetweenLeafListAndList()
        throws Exception {
        getModelNode();

        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
            + "  <tm-root>"
            + "    <check-leaflist-to-list-replicate xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">A</check-leaflist-to-list-replicate>"
            + "    <check-leaflist-to-list-replicate xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">B</check-leaflist-to-list-replicate>"
            + "    <check-leaflist-to-list-replicate xmlns=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">C</check-leaflist-to-list-replicate>"
            + "  </tm-root>"
            + "</validation-yang11>";

        String ncResponse =
            "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n"
                + "<data>\n"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n"
                + "<validation11:validation-yang11 xmlns:validation11=\"urn:org:bbf2:pma:validation-yang11\">\n"
                + "<validation11:tm-root>\n"
                + "<validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">A</validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate>\n"
                + "<validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">B</validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate>\n"
                + "<validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate xmlns:validation-augment11-replicate-namespace1=\"urn:org:bbf2:datastore-validator-augment-test11-replicate-namespace1\">C</validation-augment11-replicate-namespace1:check-leaflist-to-list-replicate>\n"
                + "</validation11:tm-root>\n"
                + "<validation11:container-with-must>"
                + "<validation11:remove-mac-addresses-from-port-down>false</validation11:remove-mac-addresses-from-port-down>"
                + "</validation11:container-with-must>"
                + "</validation11:validation-yang11>\n"
                + "</data>\n"
                + "</rpc-reply>";
        sendEditConfigAndVerifyGet(requestXml, ncResponse);

        //namespace1 has 3 entries.. BUT we try to validate count = 3 on namespace1. So this should fail
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
            + "<validation-yang11 xmlns=\"urn:org:bbf2:pma:validation-yang11\">"
            + "  <tm-root>"
            + "    <check-leaflist-to-list-replicate-count-namespace2 xmlns=\"urn:opendaylight:datastore-validator-augment-test11\">BB</check-leaflist-to-list-replicate-count-namespace2>"
            + "  </tm-root>"
            + "</validation-yang11>";

        sendEditConfigAndVerifyFailure(requestXml,
            "Violate must constraints: count(../validation-augment11-replicate-namespace2:check-leaflist-to-list-replicate) = 3",
            "/validation11:validation-yang11/validation11:tm-root/validation-augment11:check-leaflist-to-list-replicate-count-namespace2");
    }

    @Override
    protected void initialiseInterceptor() {
        m_addDefaultDataInterceptor = new AddDefaultDataInterceptor(m_modelNodeHelperRegistry,
                m_schemaRegistry, m_expValidator);
        m_addDefaultDataInterceptor.init();
    }

    @After
    public void teardown() {
        m_dataStore.disableUTSupport();
        m_datastoreValidator.setValidatedChildCacheHitStatus(false);
    }
}


