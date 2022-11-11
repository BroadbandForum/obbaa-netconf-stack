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

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.messages.NetConfResponse;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@RunWith(RequestScopeJunitRunner.class)
public class BitsTypeLeafDSValidatorTest extends AbstractDataStoreValidatorTest {
    private static final QName VALIDATION_FEATURE_BIT_QNAME = QName.create("test-interfaces", "test-feature");

    @BeforeClass
    public static void initializeOnce() throws SchemaBuildException {
        List<YangTextSchemaSource> yangFiles = TestUtil.getByteSources(getYang());
        Set<QName> feature_set = new HashSet<QName>();
        feature_set.add(VALIDATION_FEATURE_BIT_QNAME);
        m_schemaRegistry = new SchemaRegistryImpl(yangFiles, feature_set, Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.setName(SchemaRegistry.GLOBAL_SCHEMA_REGISTRY);
    } 
    
    @Test
    public void testBitLeafwithFeature() throws Exception {
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " 
                + "<interfaces xmlns=\"test-interfaces\">" 
                + "    <interface>"
                + "     <name>intName</name>" 
                + "     <type>ptm</type>" 
                + "     <featurebitleaf1>test</featurebitleaf1>"
                + "    </interface>" 
                + "  </interfaces>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">" 
                + "<data>"
                + "<if:interfaces xmlns:if=\"test-interfaces\">" 
                + "<if:interface>" 
                + "<if:featurebitleaf1>test</if:featurebitleaf1>"
                + "<if:mybits>fourthBit</if:mybits>" 
                + "<if:mybitsoverridden>fourthBit</if:mybitsoverridden>"
                + "<if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>" 
                + "<if:name>intName</if:name>" 
                + "<if:type>if:ptm</if:type>"
                + "</if:interface>" 
                + "</if:interfaces>" 
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>" 
                + "</rpc-reply>";
        verifyGet(response);
    }

    @Test
    public void testFailurecaseBitLeafwithFeature() throws Exception {
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> " 
                + "<interfaces xmlns=\"test-interfaces\">" 
                + "    <interface>"
                + "     <name>intName</name>" 
                + "     <type>ptm</type>" 
                + "     <featurebitleaf2>green</featurebitleaf2>"
                + "    </interface>" 
                + "  </interfaces>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals("Value \"green\" does not meet the bits type constraints. Valid bits are: \"yellow\"", error.getErrorMessage());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
        assertEquals("/if:interfaces/if:interface[if:name='intName']/if:featurebitleaf2", response.getErrors().get(0).getErrorPath());
    }

    @Test
    public void testBitLeafOrderUnderContainer() throws Exception {
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<interfaces xmlns=\"test-interfaces\">"
                + "    <testList>"
                + "     <name>intName</name>"
                + "     <testContainer>"
                + "         <bitsUnderContainer>secondBit thirdBit firstBit</bitsUnderContainer>"
                + "     </testContainer>"
                + "    </testList>"
                + "  </interfaces>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <data>\n" +
                "      <if:interfaces xmlns:if=\"test-interfaces\">\n" +
                "         <if:testList>\n" +
                "            <if:name>intName</if:name>\n" +
                "            <if:testContainer>\n" +
                "               <if:bitsUnderContainer>firstBit secondBit thirdBit</if:bitsUnderContainer>\n" +
                "            </if:testContainer>\n" +
                "         </if:testList>\n" +
                "      </if:interfaces>\n" +
                "      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "   </data>\n" +
                "</rpc-reply>";
        verifyGet(response);

        response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <data>\n" +
                "      <if:interfaces xmlns:if=\"test-interfaces\">\n" +
                "         <if:testList>\n" +
                "            <if:name>intName</if:name>\n" +
                "         </if:testList>\n" +
                "      </if:interfaces>\n" +
                "   </data>\n" +
                "</rpc-reply>";
        // Check match value
        String filter = "<if:interfaces xmlns:if=\"test-interfaces\">"
                + "<if:testList>"
                + "  <if:testContainer>"
                + "      <if:bitsUnderContainer>secondBit thirdBit firstBit</if:bitsUnderContainer>"
                + "  </if:testContainer>"
                + "<if:name/>"
                + "</if:testList>"
                + "</if:interfaces>";
        verifyGetConfigWithFilter(m_server, m_clientInfo, filter, response);
    }

    @Test
    public void testBitLeafInUnion() throws Exception {
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<interfaces xmlns=\"test-interfaces\">"
                + "    <testList>"
                + "     <name>intName</name>"
                + "     <testContainer>"
                + "         <unionBits>secondBit thirdBit firstBit</unionBits>"
                + "         <unionTypeWithBitsFirst>secondBit firstBit thirdBit</unionTypeWithBitsFirst>" // It should not be considered as string. So it should be ordered
                + "     </testContainer>"
                + "    </testList>"
                + "  </interfaces>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        String response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <data>\n" +
                "      <if:interfaces xmlns:if=\"test-interfaces\">\n" +
                "         <if:testList>\n" +
                "            <if:name>intName</if:name>\n" +
                "            <if:testContainer>\n" +
                "               <if:bitsUnderContainer>fourthBit</if:bitsUnderContainer>\n"+
                "               <if:unionTypeWithBitsFirst>firstBit secondBit thirdBit</if:unionTypeWithBitsFirst>\n" +
                "               <if:unionBits>firstBit secondBit thirdBit</if:unionBits>\n" +
                "            </if:testContainer>\n" +
                "         </if:testList>\n" +
                "      </if:interfaces>\n" +
                "      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "   </data>\n" +
                "</rpc-reply>";
        verifyGet(response);

        // Setting the other value of union (*)
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<interfaces xmlns=\"test-interfaces\">"
                + "    <testList>"
                + "     <name>intName</name>"
                + "     <testContainer>"
                + "         <unionBits>*</unionBits>" // now consider this as string not bits but with pattern matching
                + "         <unionTypeWithBitsFirst>xyz</unionTypeWithBitsFirst>" // now consider this as string not bits, of course no pattern
                + "     </testContainer>"
                + "    </testList>"
                + "  </interfaces>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <data>\n" +
                "      <if:interfaces xmlns:if=\"test-interfaces\">\n" +
                "         <if:testList>\n" +
                "            <if:name>intName</if:name>\n" +
                "            <if:testContainer>\n" +
                "               <if:bitsUnderContainer>fourthBit</if:bitsUnderContainer>\n"+
                "               <if:unionBits>*</if:unionBits>\n" +
                "               <if:unionTypeWithBitsFirst>xyz</if:unionTypeWithBitsFirst>\n" +
                "            </if:testContainer>\n" +
                "         </if:testList>\n" +
                "      </if:interfaces>\n" +
                "      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "   </data>\n" +
                "</rpc-reply>";
        verifyGet(response);
    }

    @Test
    public void testBitLeafOrderUnderList() throws Exception {
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<interfaces xmlns=\"test-interfaces\">"
                + "    <interface>"
                + "     <name>intName</name>"
                + "     <type>ptm</type>"
                + "     <mybits>secondBit thirdBit firstBit</mybits>"
                + "    </interface>"
                + "  </interfaces>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        String response ="<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<if:interfaces xmlns:if=\"test-interfaces\">"
                + "<if:interface>"
                + "<if:mybits>firstBit secondBit thirdBit</if:mybits>" // Should be in order
                + "<if:mybitsoverridden>fourthBit</if:mybitsoverridden>"
                + "<if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>"
                + "<if:name>intName</if:name>"
                + "<if:type>if:ptm</if:type>"
                + "</if:interface>"
                + "</if:interfaces>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";
        verifyGet(response);

        response = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <data>\n" +
                "      <if:interfaces xmlns:if=\"test-interfaces\">\n" +
                "         <if:interface>\n" +
                "            <if:mybits>firstBit secondBit thirdBit</if:mybits>\n" +
                "            <if:name>intName</if:name>\n" +
                "         </if:interface>\n" +
                "      </if:interfaces>\n" +
                "   </data>\n" +
                "</rpc-reply>";
        // Check match value
        String filter = "<if:interfaces xmlns:if=\"test-interfaces\">"
                + "<if:interface>"
                + "<if:mybits>firstBit secondBit thirdBit</if:mybits>"
                + "<if:name/>"
                + "</if:interface>"
                + "</if:interfaces>";
        verifyGetConfigWithFilter(m_server, m_clientInfo, filter, response);

        filter = "<if:interfaces xmlns:if=\"test-interfaces\">"
                + "<if:interface>"
                + "<if:mybits>secondBit thirdBit firstBit</if:mybits>" // Should be in order
                + "<if:name/>"
                + "</if:interface>"
                + "</if:interfaces>";
        verifyGetConfigWithFilter(m_server, m_clientInfo, filter, response);
    }
    
    @Test
    public void testBitLeafwithDefaultValue() throws Exception {
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<interfaces xmlns=\"test-interfaces\">"
                + "    <interface>"
                + "     <name>intName</name>"
                + "     <type>ptm</type>"
                + "     <mybits>secondBit</mybits>"
                + "    </interface>"
                + "  </interfaces>";
        editConfig(m_server, m_clientInfo, requestXml, true);  
        String response ="<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<if:interfaces xmlns:if=\"test-interfaces\">"
                + "<if:interface>"
                + "<if:mybits>secondBit</if:mybits>"
                + "<if:mybitsoverridden>fourthBit</if:mybitsoverridden>"
                + "<if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>"
                + "<if:name>intName</if:name>"
                + "<if:type>if:ptm</if:type>"
                + "</if:interface>"
                + "</if:interfaces>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";
        verifyGet(response);
    }
    
    @Test
    public void testBitIsSetFunctionNegativecase() throws Exception {
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<interfaces xmlns=\"test-interfaces\">"
                + "    <interface>"
                + "     <name>intName</name>"
                + "     <type>ptm</type>"
                + "     <mybits>secondBit thirdBit</mybits>"
                + "     <testBitss>shouldPass</testBitss>"
                + "    </interface>"
                + "  </interfaces>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        NetconfRpcError error = response.getErrors().get(0);        
        assertEquals("Violate when constraints: bit-is-set(../mybits, 'firstBit')", error.getErrorMessage());
        assertEquals("when-violation", error.getErrorAppTag());
    }
    
    @Test
    public void testBitIsSetFunction() throws Exception {
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<interfaces xmlns=\"test-interfaces\">"
                + "    <interface>"
                + "     <name>intName</name>"
                + "     <type>ptm</type>"
                + "     <mybits>firstBit secondBit</mybits>"
                + "     <testBitss>shouldPass</testBitss>"
                + "    </interface>"
                + "  </interfaces>";
        editConfig(m_server, m_clientInfo, requestXml, true);  
        String response ="<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<if:interfaces xmlns:if=\"test-interfaces\">"
                + "<if:interface>"
                + "<if:mybits>firstBit secondBit</if:mybits>"
                + "<if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>"
                + "<if:mybitsoverridden>fourthBit</if:mybitsoverridden>"
                + "<if:testBitss>shouldPass</if:testBitss>"
                + "<if:name>intName</if:name>"
                + "<if:type>if:ptm</if:type>"
                + "</if:interface>"
                + "</if:interfaces>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";
        verifyGet(response);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<interfaces xmlns=\"test-interfaces\">"
                + "    <interface>"
                + "     <name>intName</name>"
                + "     <type>ptm</type>"
                + "     <mybits>thirdBit</mybits>"
                + "    </interface>"
                + "  </interfaces>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        
        response ="<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<if:interfaces xmlns:if=\"test-interfaces\">"
                + "<if:interface>"
                + "<if:mybits>thirdBit</if:mybits>"
                + "<if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>"
                + "<if:mybitsoverridden>fourthBit</if:mybitsoverridden>"
                + "<if:name>intName</if:name>"
                + "<if:type>if:ptm</if:type>"
                + "</if:interface>"
                + "</if:interfaces>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";
        verifyGet(response);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<interfaces xmlns=\"test-interfaces\">"
                + "    <interface>"
                + "     <name>name1</name>"
                + "     <type>ptm</type>"
                + "     <mybits>secondBit</mybits>"
                + "     <testBitss>shouldNotPass</testBitss>"
                + "    </interface>"
                + "  </interfaces>";
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml, false);  
        assertEquals("Violate when constraints: bit-is-set(../mybits, 'firstBit')", ncResponse.getErrors().get(0).getErrorMessage());
    }
    
    @Test
    public void testBitIsSetWithLeafListNegativecase1() throws Exception {
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<interfaces xmlns=\"test-interfaces\">"
                + "    <interface>"
                + "     <name>intName</name>"
                + "     <type>ptm</type>"
                + "     <myBitsOfLeafList>thirdBit secondBit</myBitsOfLeafList>"
                + "     <myBitsOfLeafList>fourthBit secondBit</myBitsOfLeafList>"
                + "     <testBitssOfLeafList>shouldPass</testBitssOfLeafList>"
                + "    </interface>"
                + "  </interfaces>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);  
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals("Violate when constraints: bit-is-set(../myBitsOfLeafList, 'firstBit')", error.getErrorMessage());
        assertEquals("when-violation", error.getErrorAppTag());
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<interfaces xmlns=\"test-interfaces\">"
                + "    <interface>"
                + "     <name>intName1</name>"
                + "     <type>ptm</type>"
                + "     <myBitsOfLeafList>thirdBit firstBit</myBitsOfLeafList>"  // leaf list given unordered
                + "     <myBitsOfLeafList>secondBit</myBitsOfLeafList>"
                + "     <testBitssOfLeafList>shouldPass</testBitssOfLeafList>"
                + "    </interface>"
                + "  </interfaces>";
        editConfig(m_server, m_clientInfo, requestXml, true);

        String responseStr = "<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">\n" +
                "   <data>\n" +
                "      <if:interfaces xmlns:if=\"test-interfaces\">\n" +
                "         <if:interface>\n" +
                "            <if:myBitsOfLeafList>secondBit thirdBit</if:myBitsOfLeafList>\n" +  // leaf list ordered
                "            <if:myBitsOfLeafList>secondBit fourthBit</if:myBitsOfLeafList>\n" +  // leaf list ordered
                "            <if:name>intName</if:name>\n" +
                "            <if:testBitssOfLeafList>shouldPass</if:testBitssOfLeafList>\n" +
                "            <if:type>if:ptm</if:type>\n" +
                "         </if:interface>\n" +
                "         <if:interface>\n" +
                "            <if:myBitsOfLeafList>firstBit thirdBit</if:myBitsOfLeafList>\n" +  // leaf list ordered
                "            <if:myBitsOfLeafList>secondBit</if:myBitsOfLeafList>\n" +          // leaf list ordered
                "            <if:mybits>fourthBit</if:mybits>\n" +
                "            <if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>\n" +
                "            <if:mybitsoverridden>fourthBit</if:mybitsoverridden>\n" +
                "            <if:name>intName1</if:name>\n" +
                "            <if:testBitssOfLeafList>shouldPass</if:testBitssOfLeafList>\n" +
                "            <if:type>if:ptm</if:type>\n" +
                "         </if:interface>\n" +
                "      </if:interfaces>\n" +
                "      <validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>\n" +
                "   </data>\n" +
                "</rpc-reply>";

        verifyGet(responseStr);
    }
    
    @Test
    public void testBitIsSetWithLeafList() throws Exception {
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<interfaces xmlns=\"test-interfaces\">"
                + "    <interface>"
                + "     <name>intName</name>"
                + "     <type>ptm</type>"
                + "     <myBitsOfLeafList>firstBit fourthBit</myBitsOfLeafList>"
                + "     <myBitsOfLeafList>thirdBit secondBit</myBitsOfLeafList>"
                + "     <testBitssOfLeafList>shouldPass</testBitssOfLeafList>"
                + "    </interface>"
                + "  </interfaces>";
        editConfig(m_server, m_clientInfo, requestXml, true);  
        String response ="<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<if:interfaces xmlns:if=\"test-interfaces\">"
                + "<if:interface>"
                + "<if:myBitsOfLeafList>firstBit fourthBit</if:myBitsOfLeafList>"
                + "<if:myBitsOfLeafList>secondBit thirdBit</if:myBitsOfLeafList>"
                + "<if:mybits>fourthBit</if:mybits>"
                + "<if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>"
                + "<if:mybitsoverridden>fourthBit</if:mybitsoverridden>"
                + "<if:testBitssOfLeafList>shouldPass</if:testBitssOfLeafList>"
                + "<if:name>intName</if:name>"
                + "<if:type>if:ptm</if:type>"
                + "</if:interface>"
                + "</if:interfaces>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";
        verifyGet(response);
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<interfaces xmlns=\"test-interfaces\">"
                + "    <interface>"
                + "     <name>name1</name>"
                + "     <type>ptm</type>"
                + "     <myBitsOfLeafList>secondBit thirdBit</myBitsOfLeafList>"
                + "     <myBitsOfLeafList>secondBit</myBitsOfLeafList>"
                + "     <testBitssOfLeafList>shouldNotPass</testBitssOfLeafList>"
                + "    </interface>"
                + "  </interfaces>";
        NetConfResponse ncResponse = editConfig(m_server, m_clientInfo, requestXml, false);  
        assertEquals("Violate when constraints: bit-is-set(../myBitsOfLeafList, 'firstBit')", ncResponse.getErrors().get(0).getErrorMessage());
    }
    
    @Test
    public void testBitIsSetWithCountFunction() throws Exception {
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<interfaces xmlns=\"test-interfaces\">"
                + "    <interface>"
                + "     <name>intName</name>"
                + "     <type>ptm</type>"
                + "     <mybits>firstBit secondBit</mybits>"
                + "     <testBitss>shouldPass</testBitss>"
                + "    </interface>"
                + "    <interfaceCountWithBitIsSet>"
                + "     <name>test</name>"
                + "     <testBitIsSetWithCount>againTest</testBitIsSetWithCount>"
                + "    </interfaceCountWithBitIsSet>"
                + "  </interfaces>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);
        assertEquals("Violate when constraints: count(/if:interfaces/if:interface[bit-is-set(mybits, 'firstBit')]) > 2", response.getErrors().get(0).getErrorMessage());
        
        requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<interfaces xmlns=\"test-interfaces\">"
                + "    <interface>"
                + "     <name>name1</name>"
                + "     <type>ptm</type>"
                + "     <mybits>firstBit secondBit</mybits>"
                + "    </interface>"
                + "    <interface>"
                + "     <name>name2</name>"
                + "     <type>ptm</type>"
                + "     <mybits>firstBit</mybits>"
                + "    </interface>"
                + "    <interface>"
                + "     <name>name3</name>"
                + "     <type>ptm</type>"
                + "     <mybits>firstBit secondBit</mybits>"
                + "    </interface>"
                + "    <interfaceCountWithBitIsSet>"
                + "     <name>test</name>"
                + "     <testBitIsSetWithCount>againTest</testBitIsSetWithCount>"
                + "    </interfaceCountWithBitIsSet>"
                + "  </interfaces>";
        editConfig(m_server, m_clientInfo, requestXml, true);
    }
    

    @Test
    public void testFailurecaseOfOverriddenBitValue() throws Exception {
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<interfaces xmlns=\"test-interfaces\">"
                + "    <interface>"
                + "     <name>intName</name>"
                + "     <type>ptm</type>"
                + "     <mybitsoverridden>fifthBit</mybitsoverridden>"
                + "    </interface>"
                + "  </interfaces>";
        NetConfResponse response = editConfig(m_server, m_clientInfo, requestXml, false);  
        NetconfRpcError error = response.getErrors().get(0);
        assertEquals("Value \"fifthBit\" does not meet the bits type constraints. Valid bits are: \"thirdBit, fourthBit\"", error.getErrorMessage());
        assertEquals("/if:interfaces/if:interface[if:name='intName']/if:mybitsoverridden",error.getErrorPath());
        assertEquals(NetconfRpcErrorType.Application, response.getErrors().get(0).getErrorType());
    }
    
    @Test
    public void testOverriddenOfDefaultBitValue() throws Exception {
        getModelNode();
        String requestXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> "
                + "<interfaces xmlns=\"test-interfaces\">"
                + "    <interface>"
                + "     <name>intName</name>"
                + "     <type>ptm</type>"
                + "    </interface>"
                + "  </interfaces>";
        editConfig(m_server, m_clientInfo, requestXml, true);
        String response ="<rpc-reply xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" message-id=\"1\">"
                + "<data>"
                + "<if:interfaces xmlns:if=\"test-interfaces\">"
                + "<if:interface>"
                + "<if:mybits>fourthBit</if:mybits>"
                + "<if:mybitsdefaultValue>thirdBit</if:mybitsdefaultValue>"
                + "<if:mybitsoverridden>fourthBit</if:mybitsoverridden>"
                + "<if:name>intName</if:name>"
                + "<if:type>if:ptm</if:type>"
                + "</if:interface>"
                + "</if:interfaces>"
                + "<validation:validation xmlns:validation=\"urn:org:bbf2:pma:validation\"/>"
                + "</data>"
                + "</rpc-reply>";                
        verifyGet(response);      
    }

}
