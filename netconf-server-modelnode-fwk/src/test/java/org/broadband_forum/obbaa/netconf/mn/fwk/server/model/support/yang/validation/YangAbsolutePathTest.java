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

import static org.mockito.Mockito.mock;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@RunWith(RequestScopeJunitRunner.class)
public class YangAbsolutePathTest extends YangXmlSubtreeValidationTest{
    
    protected SchemaRegistry getSchemaRegistry() throws SchemaBuildException{
        SchemaRegistry schemaRegistry = super.getSchemaRegistry();
        schemaRegistry.registerAppAllowedAugmentedPath("datastore-validator-test", "/validation/someList/someInnerList/childContainer", mock(SchemaPath.class));
        schemaRegistry.registerAppAllowedAugmentedPath("datastore-validator-test", "/validation:validation/validation:someList/validation:someInnerList/validation:childContainer", mock(SchemaPath.class));
        return schemaRegistry;
    }
    
    @Test
    public void testAbsolutePath() throws Exception {
        String requestXml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>                    " +
                "<validation xmlns=\"urn:org:bbf2:pma:validation\">" +
                "   <someList>"+
                "    <someKey>key1</someKey>" +
                "    <someInnerList>" +
                "      <someKey>skey2</someKey>" +
                "      <childContainer>"+
                "        <multiContainer> " +
                "          <otherContainerList>" +
                "            <key1>11</key1>" +
                "            <ref>12</ref>" +
                "          </otherContainerList>" +
                "          <multiContainerList>" +
                "            <key1>12</key1>" +
                "            <ref>11</ref>"+
                "          </multiContainerList>" +
                "        </multiContainer>" +
                "      </childContainer>" +
                "   </someInnerList>" +
                "  </someList>" +
                "</validation>"
                ;
       editConfig(requestXml2); 
       
       String response = 
               "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
               + " <data>"
               + "  <ctr:CrossTest xmlns:ctr=\"urn:org:bbf:yang:test:cross:tree:reference\"/>"
               + "  <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\">"
               + "   <validation:someList>"
               + "    <validation:someInnerList>"
               + "     <validation:childContainer>"
               + "      <validation:multiContainer>"
               + "       <validation:multiContainerList>"
               + "        <validation:key1>12</validation:key1>"
               + "        <validation:ref>11</validation:ref>"
               + "       </validation:multiContainerList>"
               + "       <validation:otherContainerList>"
               + "        <validation:key1>11</validation:key1>"
               + "        <validation:ref>12</validation:ref>"
               + "       </validation:otherContainerList>"
               + "      </validation:multiContainer>"
               + "     </validation:childContainer>"
               + "     <validation:someKey>skey2</validation:someKey>"
               + "    </validation:someInnerList>"
               + "    <validation:someKey>key1</validation:someKey>"
               + "   </validation:someList>"
               + "  </validation:validation>"
               + " </data>"
               + "</rpc-reply>"
               ;
       
       verifyGet(m_server, getClientInfo(), response);
    }

}
