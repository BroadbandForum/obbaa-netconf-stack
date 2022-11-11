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

package org.broadband_forum.obbaa.netconf.mn.fwk.util;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigOperations;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * Created by sgs on 4/20/17.
 */
@RunWith(RequestScopeJunitRunner.class)
public class OperationAttributeUtilTest {

    private static final String DOC = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <edit-config>\n" +
            "    <target>\n" +
            "      <running/>\n" +
            "    </target>\n" +
            "    <default-operation>merge</default-operation>\n" +
            "    <test-option>set</test-option>\n" +
            "    <error-option>stop-on-error</error-option>\n" +
            "    <config>\n" +
            "      <if:interfaces xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
            "        <if:interface>\n" +
            "          <if:name>sinband1</if:name>\n" +
            "          <bbf-subif:frame-processing xmlns:bbf-subif=\"urn:bbf:yang:bbf-sub-interfaces\">\n" +
            "            <bbf-subif:ingress-rule>\n" +
            "              <bbf-subif:rule>\n" +
            "                <bbf-subif:name>single_tagged_vlan</bbf-subif:name>\n" +
            "                <bbf-subif:flexible-match>\n" +
            "                  <bbf-subif-tag:match-criteria xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
            "ns0:operation=\"merge\" xmlns:bbf-subif-tag=\"urn:bbf:yang:bbf-sub-interface-tagging\">\n" +
            "                    <bbf-subif-tag:match-all/>\n" +
            "                    <bbf-subif-tag:tag ns0:operation=\"remove\">\n" +
            "                      <bbf-subif-tag:index>0</bbf-subif-tag:index>\n" +
            "                    </bbf-subif-tag:tag>\n" +
            "                  </bbf-subif-tag:match-criteria>\n" +
            "                </bbf-subif:flexible-match>\n" +
            "              </bbf-subif:rule>\n" +
            "            </bbf-subif:ingress-rule>\n" +
            "          </bbf-subif:frame-processing>\n" +
            "        </if:interface>\n" +
            "      </if:interfaces>\n" +
            "    </config>\n" +
            "  </edit-config>\n" +
            "</rpc>";

    private static final String DOC2 = "<rpc xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "  <edit-config>\n" +
            "    <target>\n" +
            "      <running/>\n" +
            "    </target>\n" +
            "    <test-option>set</test-option>\n" +
            "    <error-option>stop-on-error</error-option>\n" +
            "    <config>\n" +
            "      <if:interfaces xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">\n" +
            "        <if:interface>\n" +
            "          <if:name>sinband1</if:name>\n" +
            "          <bbf-subif:frame-processing xmlns:bbf-subif=\"urn:bbf:yang:bbf-sub-interfaces\">\n" +
            "            <bbf-subif:ingress-rule>\n" +
            "              <bbf-subif:rule>\n" +
            "                <bbf-subif:name>single_tagged_vlan</bbf-subif:name>\n" +
            "                <bbf-subif:flexible-match>\n" +
            "                  <bbf-subif-tag:match-criteria xmlns:ns0=\"urn:ietf:params:xml:ns:netconf:base:1.0\" " +
            "ns0:operation=\"merge\" xmlns:bbf-subif-tag=\"urn:bbf:yang:bbf-sub-interface-tagging\">\n" +
            "                    <bbf-subif-tag:match-all/>\n" +
            "                    <bbf-subif-tag:tag ns0:operation=\"create\">\n" +
            "                      <bbf-subif-tag:index>0</bbf-subif-tag:index>\n" +
            "                    </bbf-subif-tag:tag>\n" +
            "                  </bbf-subif-tag:match-criteria>\n" +
            "                </bbf-subif:flexible-match>\n" +
            "              </bbf-subif:rule>\n" +
            "            </bbf-subif:ingress-rule>\n" +
            "          </bbf-subif:frame-processing>\n" +
            "        </if:interface>\n" +
            "      </if:interfaces>\n" +
            "    </config>\n" +
            "  </edit-config>\n" +
            "</rpc>";

    @Test
    public void testGetOperation() throws NetconfMessageBuilderException {
        Document document = DocumentUtils.stringToDocument(DOC);
        Element element = DocumentUtils.getChildElements(document.getDocumentElement(), "if:interfaces").get(0) ;
        assertEquals("merge", OperationAttributeUtil.getOperationAttribute(element));

        element = DocumentUtils.getChildElements(document.getDocumentElement(), "bbf-subif-tag:tag").get(0) ;
        assertEquals("remove", OperationAttributeUtil.getOperationAttribute(element));

        element = DocumentUtils.getChildElements(document.getDocumentElement(), "bbf-subif-tag:match-all").get(0) ;
        assertEquals("merge", OperationAttributeUtil.getOperationAttribute(element));

        document = DocumentUtils.stringToDocument(DOC2);
        element = DocumentUtils.getChildElements(document.getDocumentElement(), "if:interfaces").get(0) ;
        assertEquals("merge", OperationAttributeUtil.getOperationAttribute(element));

        element = DocumentUtils.getChildElements(document.getDocumentElement(), "bbf-subif-tag:tag").get(0) ;
        assertEquals("create", OperationAttributeUtil.getOperationAttribute(element));

        element = DocumentUtils.getChildElements(document.getDocumentElement(), "bbf-subif-tag:match-all").get(0) ;
        assertEquals("merge", OperationAttributeUtil.getOperationAttribute(element));
    }

    @Test
    public void testIsOppositeOperation(){
        assertTrue(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.CREATE, EditConfigOperations.DELETE));
        assertTrue(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.CREATE, EditConfigOperations.REMOVE));
        assertFalse(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.CREATE, EditConfigOperations.REPLACE));
        assertFalse(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.CREATE, EditConfigOperations.MERGE));
        assertFalse(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.CREATE, EditConfigOperations.CREATE));
        assertFalse(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.CREATE, "blah"));

        assertTrue(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.DELETE, EditConfigOperations.CREATE));
        assertTrue(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.DELETE, EditConfigOperations.MERGE));
        assertTrue(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.DELETE, EditConfigOperations.REPLACE));
        assertTrue(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.DELETE, EditConfigOperations.REMOVE));
        assertTrue(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.DELETE, EditConfigOperations.DELETE));
        assertFalse(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.DELETE, "blah"));

        assertTrue(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.REMOVE, EditConfigOperations.CREATE));
        assertTrue(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.REMOVE, EditConfigOperations.MERGE));
        assertTrue(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.REMOVE, EditConfigOperations.REPLACE));
        assertTrue(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.REMOVE, EditConfigOperations.DELETE));
        assertTrue(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.REMOVE, EditConfigOperations.REMOVE));
        assertFalse(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.REMOVE, "blah"));

        assertFalse(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.REPLACE, EditConfigOperations.CREATE));
        assertFalse(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.REPLACE, EditConfigOperations.MERGE));
        assertTrue(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.REPLACE, EditConfigOperations.DELETE));
        assertTrue(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.REPLACE, EditConfigOperations.REMOVE));
        assertFalse(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.REPLACE, EditConfigOperations.REPLACE));
        assertFalse(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.REPLACE, "blah"));

        assertFalse(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.MERGE, EditConfigOperations.CREATE));
        assertTrue(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.MERGE, EditConfigOperations.REMOVE));
        assertTrue(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.MERGE, EditConfigOperations.DELETE));
        assertFalse(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.MERGE, EditConfigOperations.REPLACE));
        assertFalse(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.MERGE, EditConfigOperations.MERGE));
        assertFalse(OperationAttributeUtil.isAllowedOperation(EditConfigOperations.MERGE, "blah"));

        assertFalse(OperationAttributeUtil.isAllowedOperation("blah", "blah"));
    }

}
