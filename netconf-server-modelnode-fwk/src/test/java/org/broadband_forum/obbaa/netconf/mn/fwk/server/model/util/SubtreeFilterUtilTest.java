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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.assertXMLEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterMatchNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@RunWith(RequestScopeJunitRunner.class)
public class SubtreeFilterUtilTest {

    private static final String INTF_NS = "urn:ietf:params:xml:ns:yang:ietf-interfaces";
    private static final String DEVICE_HOLDER_NS = "http://www.test-company.com/solutions/anv-device-holders";

    public static final String UNIVERSE1 = "    <universe xmlns=\"http://test-company/country-universe\">\n" +
            "        <name>universe 1</name>\n" +
            "        <galaxy>\n" +
            "            <name>Milky way</name>\n" +
            "            <pet-name>My home galaxy</pet-name>\n" +
            "            <planetary-system>\n" +
            "                <system-name>Solar System</system-name>\n" +
            "                <planets>\n" +
            "                    <no-of-planets>1</no-of-planets>\n" +
            "                    <planet>\n" +
            "                        <planet-name>Mother Earth</planet-name>\n" +
            "                        <another-planet-key>Mother Earth Key2</another-planet-key>\n" +
            "                        <satellite>\n" +
            "                            <container>weird key named container</container>\n" +
            "                            <another-satellite-key>Moon</another-satellite-key>\n" +
            "                        </satellite>\n" +
            "                    </planet>\n" +
            "                </planets>\n" +
            "            </planetary-system>\n" +
            "            <planetary-system>\n" +
            "                <system-name>Solar System 2</system-name>\n" +
            "                <planets>\n" +
            "                    <planet>\n" +
            "                        <planet-name>Mother Earth 2</planet-name>\n" +
            "                        <another-planet-key>Mother Earth 2 Key2</another-planet-key>\n" +
            "                        <satellite>\n" +
            "                            <container>weird key named container 2</container>\n" +
            "                            <another-satellite-key>Moon 2</another-satellite-key>\n" +
            "                        </satellite>\n" +
            "                    </planet>\n" +
            "                </planets>\n" +
            "            </planetary-system>\n" +
            "        </galaxy>\n" +
            "        <galaxy>\n" +
            "            <name>Milky way 2</name>\n" +
            "            <pet-name>Not My home galaxy</pet-name>\n" +
            "            <planetary-system>\n" +
            "                <system-name>Solar System 3</system-name>\n" +
            "                <planets>\n" +
            "                    <planet>\n" +
            "                        <planet-name>Mother Earth 3</planet-name>\n" +
            "                        <another-planet-key>Mother Earth 3 Key2</another-planet-key>\n" +
            "                        <satellite>\n" +
            "                            <container>weird key named container 3</container>\n" +
            "                            <another-satellite-key>Moon</another-satellite-key>\n" +
            "                        </satellite>\n" +
            "                    </planet>\n" +
            "                </planets>\n" +
            "            </planetary-system>\n" +
            "            <planetary-system>\n" +
            "                <system-name>Solar System 4</system-name>\n" +
            "                <planets>\n" +
            "                    <no-of-planets>2</no-of-planets>\n" +
            "                    <planet>\n" +
            "                        <planet-name>Mother Earth 4</planet-name>\n" +
            "                        <another-planet-key>Mother Earth 4 Key2</another-planet-key>\n" +
            "                        <satellite>\n" +
            "                            <container>weird key named container 4</container>\n" +
            "                            <another-satellite-key>Moon 4</another-satellite-key>\n" +
            "                        </satellite>\n" +
            "                    </planet>\n" +
            "                    <planet>\n" +
            "                        <planet-name>Mother Earth 5</planet-name>\n" +
            "                        <another-planet-key>Mother Earth 5 Key2</another-planet-key>\n" +
            "                        <satellite>\n" +
            "                            <container>weird key named container 5</container>\n" +
            "                            <another-satellite-key>Moon 5</another-satellite-key>\n" +
            "                        </satellite>\n" +
            "                    </planet>\n" +
            "                </planets>\n" +
            "            </planetary-system>\n" +
            "        </galaxy>\n" +
            "    </universe>\n";
    
    private static final String SPACE_TIME = "<space-time xmlns=\"http://test-company/country-universe\">\n"+
                                            "<type>black-hole</type>"+
                                            "<mass>10000</mass>"+
                                            "<angular-momentum>600000</angular-momentum>"+
                                            "</space-time>"; 
    
    private static final String SPACE_TIME2 = "<space-time xmlns=\"http://test-company/country-universe\">\n"+
            "<type>black-hole</type>"+
            "<mass>10000</mass>"+
            "<angular-momentum>600000</angular-momentum>"+
            "<charge>"+
            "<type>negative</type>"+
            "</charge>"+            
            "</space-time>";
    
    private static final String SPACE_TIME3 = "<space-time xmlns=\"http://test-company/country-universe\">\n"+
            "<type>black-hole</type>"+            
            "<charge>"+
            "<type>negative</type>"+
            "</charge>"+
            "<gravity/>"+
            "</space-time>";
    
    
    private static final String FULL_DS_STR = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +             
            UNIVERSE1 +
            "    <universe xmlns=\"http://test-company/country-universe\">\n" +
            "        <name>parallel universe</name>\n" +
            "        <galaxy>\n" +
            "            <name>Milky way</name>\n" +
            "            <pet-name>My home galaxy</pet-name>\n" +
            "            <planetary-system>\n" +
            "                <system-name>Solar System</system-name>\n" +
            "                <planets>\n" +
            "                    <no-of-planets>1</no-of-planets>\n" +
            "                    <planet>\n" +
            "                        <planet-name>Mother Earth</planet-name>\n" +
            "                        <another-planet-key>Mother Earth Key2</another-planet-key>\n" +
            "                        <satellite>\n" +
            "                            <container>weird key named container</container>\n" +
            "                            <another-satellite-key>Moon</another-satellite-key>\n" +
            "                        </satellite>\n" +
            "                    </planet>\n" +
            "                </planets>\n" +
            "            </planetary-system>\n" +
            "            <planetary-system>\n" +
            "                <system-name>Solar System 2</system-name>\n" +
            "                <planets>\n" +
            "                    <planet>\n" +
            "                        <planet-name>Mother Earth 2</planet-name>\n" +
            "                        <another-planet-key>Mother Earth 2 Key2</another-planet-key>\n" +
            "                        <satellite>\n" +
            "                            <container>weird key named container 2</container>\n" +
            "                            <another-satellite-key>Moon 2</another-satellite-key>\n" +
            "                        </satellite>\n" +
            "                    </planet>\n" +
            "                </planets>\n" +
            "            </planetary-system>\n" +
            "        </galaxy>\n" +
            "    </universe>\n" +
            "    <world xmlns=\"http://test-company/country-universe\">\n" +
            "        <no-of-countires>2</no-of-countires>\n" +
            "        <country>\n" +
            "            <country-name>India</country-name>\n" +
            "            <no-of-states>2</no-of-states>\n" +
            "            <state>\n" +
            "                <name>Karnataka</name>\n" +
            "                <chief-name>Siddu</chief-name>\n" +
            "                <short-name>KA</short-name>\n" +
            "                <no-of-districts>2</no-of-districts>\n" +
            "                <district>\n" +
            "                    <name>Bangalore</name>\n" +
            "                    <mayor-name>Padmavathi</mayor-name>\n" +
            "                </district>\n" +
            "                <district>\n" +
            "                    <name>Mangalore</name>\n" +
            "                    <mayor-name>Kavita Sanil</mayor-name>\n" +
            "                </district>\n" +
            "            </state>\n" +
            "            <state>\n" +
            "                <name>Tamilandu</name>\n" +
            "                <no-of-districts>1</no-of-districts>\n" +
            "                <chief-name>Palani</chief-name>\n" +
            "                <short-name>TN</short-name>\n" +
            "                <district>\n" +
            "                    <name>Chennai</name>\n" +
            "                    <mayor-name>Duraisamy</mayor-name>\n" +
            "                </district>\n" +
            "            </state>\n" +
            "        </country>\n" +
            "        <country>\n" +
            "            <country-name>India 2</country-name>\n" +
            "            <no-of-states>1</no-of-states>\n" +
            "            <state>\n" +
            "                <name>Tamilandu</name>\n" +
            "                <chief-name>Palani</chief-name>\n" +
            "                <short-name>TN</short-name>\n" +
            "                <district>\n" +
            "                    <name>Chennai</name>\n" +
            "                    <mayor-name>Duraisamy</mayor-name>\n" +
            "                </district>\n" +
            "            </state>\n" +
            "        </country>\n" +
            "    </world>\n" +
            "</data>";
    private static SubtreeFilterUtil m_util;
    private static SchemaRegistry m_schemaRegistry;

    @BeforeClass
    public static void setUp() throws Exception {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections.emptyMap(), new NoLockService());
        m_schemaRegistry.buildSchemaContext(getTestYangFiles(), Collections.emptySet(), Collections.emptyMap());
        m_util = new SubtreeFilterUtil(m_schemaRegistry);
    }

    private static List<YangTextSchemaSource> getTestYangFiles() {
        YangTextSchemaSource rpcTestYangFile1 = YangParserUtil.getYangSource(SubtreeFilterUtilTest.class.getResource("/subtreefilterutiltest/country-universe@2017-06-14.yang"));
        YangTextSchemaSource rpcTestYangFile2 = YangParserUtil.getYangSource(SubtreeFilterUtilTest.class.getResource("/subtreefilterutiltest/test-logging-app.yang"));
        List<YangTextSchemaSource> ytss = new ArrayList<>();
        ytss.add(rpcTestYangFile1);
        ytss.add(rpcTestYangFile2);

        return ytss;
    }

    @Test
    public void testSelectFilteringOnContainerNodes() throws Exception {
        Element unfilteredXml = getFullDsXml();
        Element filter = getElement(
                "<filter type=\"subtree\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company/country-universe\"/>\n" +
                        "</filter>");
        Element filteredXml = m_util.filter(unfilteredXml, filter);
        String expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company/country-universe\">\n" +
                        "        <no-of-countires>2</no-of-countires>\n" +
                        "        <country>\n" +
                        "            <country-name>India</country-name>\n" +
                        "            <no-of-states>2</no-of-states>\n" +
                        "            <state>\n" +
                        "                <name>Karnataka</name>\n" +
                        "                <chief-name>Siddu</chief-name>\n" +
                        "                <short-name>KA</short-name>\n" +
                        "                <no-of-districts>2</no-of-districts>\n" +
                        "                <district>\n" +
                        "                    <name>Bangalore</name>\n" +
                        "                    <mayor-name>Padmavathi</mayor-name>\n" +
                        "                </district>\n" +
                        "                <district>\n" +
                        "                    <name>Mangalore</name>\n" +
                        "                    <mayor-name>Kavita Sanil</mayor-name>\n" +
                        "                </district>\n" +
                        "            </state>\n" +
                        "            <state>\n" +
                        "                <name>Tamilandu</name>\n" +
                        "                <no-of-districts>1</no-of-districts>\n" +
                        "                <chief-name>Palani</chief-name>\n" +
                        "                <short-name>TN</short-name>\n" +
                        "                <district>\n" +
                        "                    <name>Chennai</name>\n" +
                        "                    <mayor-name>Duraisamy</mayor-name>\n" +
                        "                </district>\n" +
                        "            </state>\n" +
                        "        </country>\n" +
                        "        <country>\n" +
                        "            <country-name>India 2</country-name>\n" +
                        "            <no-of-states>1</no-of-states>\n" +
                        "            <state>\n" +
                        "                <name>Tamilandu</name>\n" +
                        "                <chief-name>Palani</chief-name>\n" +
                        "                <short-name>TN</short-name>\n" +
                        "                <district>\n" +
                        "                    <name>Chennai</name>\n" +
                        "                    <mayor-name>Duraisamy</mayor-name>\n" +
                        "                </district>\n" +
                        "            </state>\n" +
                        "        </country>\n" +
                        "    </world>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);

        filter = getElement(
                "<filter type=\"subtree\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"unknown:ns\"/>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);

        filter = getElement(
                "<filter type=\"subtree\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company/country-universe\">\n" +
                        "        <country/>\n" +
                        "    </world>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company/country-universe\">\n" +
                        "        <country>\n" +
                        "            <country-name>India</country-name>\n" +
                        "            <no-of-states>2</no-of-states>\n" +
                        "            <state>\n" +
                        "                <name>Karnataka</name>\n" +
                        "                <chief-name>Siddu</chief-name>\n" +
                        "                <short-name>KA</short-name>\n" +
                        "                <no-of-districts>2</no-of-districts>\n" +
                        "                <district>\n" +
                        "                    <name>Bangalore</name>\n" +
                        "                    <mayor-name>Padmavathi</mayor-name>\n" +
                        "                </district>\n" +
                        "                <district>\n" +
                        "                    <name>Mangalore</name>\n" +
                        "                    <mayor-name>Kavita Sanil</mayor-name>\n" +
                        "                </district>\n" +
                        "            </state>\n" +
                        "            <state>\n" +
                        "                <name>Tamilandu</name>\n" +
                        "                <no-of-districts>1</no-of-districts>\n" +
                        "                <chief-name>Palani</chief-name>\n" +
                        "                <short-name>TN</short-name>\n" +
                        "                <district>\n" +
                        "                    <name>Chennai</name>\n" +
                        "                    <mayor-name>Duraisamy</mayor-name>\n" +
                        "                </district>\n" +
                        "            </state>\n" +
                        "        </country>\n" +
                        "        <country>\n" +
                        "            <country-name>India 2</country-name>\n" +
                        "            <no-of-states>1</no-of-states>\n" +
                        "            <state>\n" +
                        "                <name>Tamilandu</name>\n" +
                        "                <chief-name>Palani</chief-name>\n" +
                        "                <short-name>TN</short-name>\n" +
                        "                <district>\n" +
                        "                    <name>Chennai</name>\n" +
                        "                    <mayor-name>Duraisamy</mayor-name>\n" +
                        "                </district>\n" +
                        "            </state>\n" +
                        "        </country>\n" +
                        "    </world>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);

        filter = getElement(
                "<filter type=\"subtree\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company/country-universe\">\n" +
                        "        <country>\n" +
                        "            <country-name/>\n" +
                        "            <no-of-states/>\n" +
                        "        </country>\n" +
                        "    </world>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company/country-universe\">\n" +
                        "        <country>\n" +
                        "            <country-name>India</country-name>\n" +
                        "            <no-of-states>2</no-of-states>\n" +
                        "        </country>\n" +
                        "        <country>\n" +
                        "            <country-name>India 2</country-name>\n" +
                        "            <no-of-states>1</no-of-states>\n" +
                        "        </country>\n" +
                        "    </world>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);

        filter = getElement(
                "<filter type=\"subtree\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company/country-universe\">\n" +
                        "        <country>\n" +
                        "            <country-name/>\n" +
                        "        </country>\n" +
                        "    </world>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company/country-universe\">\n" +
                        "        <country>\n" +
                        "            <country-name>India</country-name>\n" +
                        "        </country>\n" +
                        "        <country>\n" +
                        "            <country-name>India 2</country-name>\n" +
                        "        </country>\n" +
                        "    </world>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);
    }

    @Test 
    public void testSelectFilterWhenListDoesNotExist() throws DOMException, Exception{
        
        /*Case 1: This is the actual case for FNMS-31214 
         * The filter element has the child list and other nodes.
         * However the response does not contain the child List. In this case, the other select nodes 
         * alone must be returned.*/
        Element unfilteredXml = getFullDsXml();        
        
        Element childElement = (Element) unfilteredXml.getOwnerDocument().importNode(TestUtil.parseXml(SPACE_TIME), true);
        unfilteredXml.appendChild(childElement);        
        Element filter = getElement(
                "<filter type=\"subtree\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <space-time xmlns=\"http://test-company/country-universe\">\n" +
                        "<charge>"+
                        "<type/>"+
                        "</charge>"+
                        "<type/>"+                                                  
                        "<angular-momentum/>" +
                        "<mass/>"+
                        "</space-time>"+
                        "</filter>");
        
        String expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
        "<space-time xmlns=\"http://test-company/country-universe\">"+
        "<type>black-hole</type>"+
        "<angular-momentum>600000</angular-momentum>"+
        "<mass>10000</mass>"+
        "</space-time>"+
        "</data>";
        
        Element filteredXml = m_util.filter(unfilteredXml, filter);
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);        
    }
    
    @Test 
    public void testSelectFilterWhenListExists() throws DOMException, Exception{
        /*Case 2: In this case the response contains the child list and other nodes. 
         * However the filter only contains the child list. 
         * In this case the child list alone must be returned.*/
        
        Element unfilteredXml = getFullDsXml();
        
        Element childElement = (Element) unfilteredXml.getOwnerDocument().importNode(TestUtil.parseXml(SPACE_TIME2), true);
        unfilteredXml.appendChild(childElement); 
        
        Element filter = getElement(
                "<filter type=\"subtree\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <space-time xmlns=\"http://test-company/country-universe\">\n" +
                        "<charge>"+
                        "<type/>"+
                        "</charge>"+
                        "<type/>"+             
                        "</space-time>"+
                        "</filter>");
        
        String expectedXmlStr = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
                "<space-time xmlns=\"http://test-company/country-universe\">"+
                "<type>black-hole</type>"+
                "<charge>"+
                "<type>negative</type>"+
                "</charge>"+
                "</space-time>"+
                "</data>";
        
        Element filteredXml = m_util.filter(unfilteredXml, filter);
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);     
    }
    
    @Test 
    public void testSelectFilterWhenOuterListElementsDontExist() throws DOMException, Exception{
        /*Case 3: In this case the filter consists of child list and other select nodes. 
         * However the response only contains the child list. 
         * In this case too, the child list alone should be returned*/
        
        Element unfilteredXml = getFullDsXml();       
        
        Element childElement = (Element) unfilteredXml.getOwnerDocument().importNode(TestUtil.parseXml(SPACE_TIME3), true);
        unfilteredXml.appendChild(childElement);        
        Element filter = getElement(
                "<filter type=\"subtree\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <space-time xmlns=\"http://test-company/country-universe\">\n" +
                        "<charge>"+
                        "<type/>"+
                        "</charge>"+
                        "<type/>"+                                                  
                        "<angular-momentum/>" +
                        "<mass/>"+
                        "<gravity/>"+
                        "</space-time>"+
                        "</filter>");       
        
        String expectedXmlStr = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">"+
                "<space-time xmlns=\"http://test-company/country-universe\">"+
                "<type>black-hole</type>"+
                "<charge>"+
                "<type>negative</type>"+
                "</charge>"+
                "<gravity/>"+
                "</space-time>"+                
                "</data>";
        
        Element filteredXml = m_util.filter(unfilteredXml, filter);
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);
    }
    
    @Test
    public void testSelectFilteringOnListNodes() throws Exception {
        Element unfilteredXml = getFullDsXml();
        Element filter = getElement(
                "<filter type=\"subtree\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company/country-universe\"/>\n" +
                        "</filter>");
        Element filteredXml = m_util.filter(unfilteredXml, filter);
        String expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <name>universe 1</name>\n" +
                        "        <galaxy>\n" +
                        "            <name>Milky way</name>\n" +
                        "            <pet-name>My home galaxy</pet-name>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System</system-name>\n" +
                        "                <planets>\n" +
                        "                    <no-of-planets>1</no-of-planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container</container>\n" +
                        "                            <another-satellite-key>Moon</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System 2</system-name>\n" +
                        "                <planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth 2</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth 2 Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container 2</container>\n" +
                        "                            <another-satellite-key>Moon 2</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "        </galaxy>\n" +
                        "        <galaxy>\n" +
                        "            <name>Milky way 2</name>\n" +
                        "            <pet-name>Not My home galaxy</pet-name>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System 3</system-name>\n" +
                        "                <planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth 3</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth 3 Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container 3</container>\n" +
                        "                            <another-satellite-key>Moon</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System 4</system-name>\n" +
                        "                <planets>\n" +
                        "                    <no-of-planets>2</no-of-planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth 4</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth 4 Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container 4</container>\n" +
                        "                            <another-satellite-key>Moon 4</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth 5</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth 5 Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container 5</container>\n" +
                        "                            <another-satellite-key>Moon 5</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "        </galaxy>\n" +
                        "    </universe>\n" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <name>parallel universe</name>\n" +
                        "        <galaxy>\n" +
                        "            <name>Milky way</name>\n" +
                        "            <pet-name>My home galaxy</pet-name>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System</system-name>\n" +
                        "                <planets>\n" +
                        "                    <no-of-planets>1</no-of-planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container</container>\n" +
                        "                            <another-satellite-key>Moon</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System 2</system-name>\n" +
                        "                <planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth 2</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth 2 Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container 2</container>\n" +
                        "                            <another-satellite-key>Moon 2</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "        </galaxy>\n" +
                        "    </universe>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);

        filter = getElement(
                "<filter type=\"subtree\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"unknown:ns2\"/>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);

        filter = getElement(
                "<filter type=\"subtree\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <galaxy/>\n" +
                        "    </universe>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <name>universe 1</name>\n" +
                        "        <galaxy>\n" +
                        "            <name>Milky way</name>\n" +
                        "            <pet-name>My home galaxy</pet-name>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System</system-name>\n" +
                        "                <planets>\n" +
                        "                    <no-of-planets>1</no-of-planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container</container>\n" +
                        "                            <another-satellite-key>Moon</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System 2</system-name>\n" +
                        "                <planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth 2</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth 2 Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container 2</container>\n" +
                        "                            <another-satellite-key>Moon 2</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "        </galaxy>\n" +
                        "        <galaxy>\n" +
                        "            <name>Milky way 2</name>\n" +
                        "            <pet-name>Not My home galaxy</pet-name>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System 3</system-name>\n" +
                        "                <planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth 3</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth 3 Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container 3</container>\n" +
                        "                            <another-satellite-key>Moon</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System 4</system-name>\n" +
                        "                <planets>\n" +
                        "                    <no-of-planets>2</no-of-planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth 4</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth 4 Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container 4</container>\n" +
                        "                            <another-satellite-key>Moon 4</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth 5</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth 5 Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container 5</container>\n" +
                        "                            <another-satellite-key>Moon 5</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "        </galaxy>\n" +
                        "    </universe>\n" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <name>parallel universe</name>\n" +
                        "        <galaxy>\n" +
                        "            <name>Milky way</name>\n" +
                        "            <pet-name>My home galaxy</pet-name>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System</system-name>\n" +
                        "                <planets>\n" +
                        "                    <no-of-planets>1</no-of-planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container</container>\n" +
                        "                            <another-satellite-key>Moon</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System 2</system-name>\n" +
                        "                <planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth 2</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth 2 Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container 2</container>\n" +
                        "                            <another-satellite-key>Moon 2</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "        </galaxy>\n" +
                        "    </universe>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);

        filter = getElement(
                "<filter type=\"subtree\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <galaxy>\n" +
                        "            <name/>\n" +
                        "            <pet-name/>\n" +
                        "        </galaxy>\n" +
                        "    </universe>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <galaxy>\n" +
                        "            <name>Milky way</name>\n" +
                        "            <pet-name>My home galaxy</pet-name>\n" +
                        "        </galaxy>\n" +
                        "        <galaxy>\n" +
                        "            <name>Milky way 2</name>\n" +
                        "            <pet-name>Not My home galaxy</pet-name>\n" +
                        "        </galaxy>\n" +
                        "        <name>universe 1</name>\n" +
                        "    </universe>\n" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <galaxy>\n" +
                        "            <name>Milky way</name>\n" +
                        "            <pet-name>My home galaxy</pet-name>\n" +
                        "        </galaxy>\n" +
                        "        <name>parallel universe</name>\n" +
                        "    </universe>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);

        filter = getElement(
                "<filter type=\"subtree\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <galaxy>\n" +
                        "            <pet-name/>\n" +
                        "        </galaxy>\n" +
                        "    </universe>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <galaxy>\n" +
                        "            <name>Milky way</name>\n" +
                        "            <pet-name>My home galaxy</pet-name>\n" +
                        "        </galaxy>\n" +
                        "        <galaxy>\n" +
                        "            <name>Milky way 2</name>\n" +
                        "            <pet-name>Not My home galaxy</pet-name>\n" +
                        "        </galaxy>\n" +
                        "        <name>universe 1</name>\n" +
                        "    </universe>\n" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <galaxy>\n" +
                        "            <name>Milky way</name>\n" +
                        "            <pet-name>My home galaxy</pet-name>\n" +
                        "        </galaxy>\n" +
                        "        <name>parallel universe</name>\n" +
                        "    </universe>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);

    }

    @Test
    public void testMatchFilteringOnContainerNodes() throws Exception {
        Element unfilteredXml = getFullDsXml();
        Element filter = getElement(
                "<filter type=\"subtree\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company/country-universe\">\n" +
                        "        <country>\n" +
                        "            <country-name>India</country-name>\n" +
                        "        </country>\n" +
                        "    </world>\n" +
                        "</filter>");
        Element filteredXml = m_util.filter(unfilteredXml, filter);
        String expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company/country-universe\">\n" +
                        "        <country>\n" +
                        "            <country-name>India</country-name>\n" +
                        "            <no-of-states>2</no-of-states>\n" +
                        "            <state>\n" +
                        "                <name>Karnataka</name>\n" +
                        "                <chief-name>Siddu</chief-name>\n" +
                        "                <short-name>KA</short-name>\n" +
                        "                <no-of-districts>2</no-of-districts>\n" +
                        "                <district>\n" +
                        "                    <name>Bangalore</name>\n" +
                        "                    <mayor-name>Padmavathi</mayor-name>\n" +
                        "                </district>\n" +
                        "                <district>\n" +
                        "                    <name>Mangalore</name>\n" +
                        "                    <mayor-name>Kavita Sanil</mayor-name>\n" +
                        "                </district>\n" +
                        "            </state>\n" +
                        "            <state>\n" +
                        "                <name>Tamilandu</name>\n" +
                        "                <no-of-districts>1</no-of-districts>\n" +
                        "                <chief-name>Palani</chief-name>\n" +
                        "                <short-name>TN</short-name>\n" +
                        "                <district>\n" +
                        "                    <name>Chennai</name>\n" +
                        "                    <mayor-name>Duraisamy</mayor-name>\n" +
                        "                </district>\n" +
                        "            </state>\n" +
                        "        </country>\n" +
                        "    </world>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);

        filter = getElement(
                "<filter xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" type=\"subtree\">\n" +
                        "    <world xmlns=\"http://test-company/country-universe\">\n" +
                        "        <country>\n" +
                        "            <state>\n" +
                        "                <district>\n" +
                        "                    <name>Mangalore</name>\n" +
                        "                </district>\n" +
                        "            </state>\n" +
                        "        </country>\n" +
                        "    </world>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company/country-universe\">\n" +
                        "        <country>\n" +
                        "            <country-name>India</country-name>\n" +
                        "            <state>\n" +
                        "                <name>Karnataka</name>\n" +
                        "                <district>\n" +
                        "                    <name>Mangalore</name>\n" +
                        "                    <mayor-name>Kavita Sanil</mayor-name>\n" +
                        "                </district>\n" +
                        "            </state>\n" +
                        "        </country>\n" +
                        "    </world>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);

        filter = getElement(
                "<filter xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" type=\"subtree\">\n" +
                        "    <world xmlns=\"http://test-company/country-universe\">\n" +
                        "        <country>\n" +
                        "            <state>\n" +
                        "                <district>\n" +
                        "                    <name>Chennai</name>\n" +
                        "                </district>\n" +
                        "            </state>\n" +
                        "        </country>\n" +
                        "    </world>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company/country-universe\">\n" +
                        "        <country>\n" +
                        "            <country-name>India</country-name>\n" +
                        "            <state>\n" +
                        "                <name>Tamilandu</name>\n" +
                        "                <district>\n" +
                        "                    <name>Chennai</name>\n" +
                        "                    <mayor-name>Duraisamy</mayor-name>\n" +
                        "                </district>\n" +
                        "            </state>\n" +
                        "        </country>\n" +
                        "        <country>\n" +
                        "            <country-name>India 2</country-name>\n" +
                        "            <state>\n" +
                        "                <name>Tamilandu</name>\n" +
                        "                <district>\n" +
                        "                    <name>Chennai</name>\n" +
                        "                    <mayor-name>Duraisamy</mayor-name>\n" +
                        "                </district>\n" +
                        "            </state>\n" +
                        "        </country>\n" +
                        "    </world>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);

        filter = getElement(
                "<filter xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\" type=\"subtree\">\n" +
                        "    <world xmlns=\"http://test-company/country-universe\">\n" +
                        "        <country>\n" +
                        "            <state>\n" +
                        "                <district>\n" +
                        "                    <name>Nellore</name>\n" +
                        "                </district>\n" +
                        "            </state>\n" +
                        "        </country>\n" +
                        "    </world>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"/>\n";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);
    }

    @Test
    public void testMatchFilteringOnListNodes() throws Exception {
        Element unfilteredXml = getFullDsXml();
        Element filter = getElement(
                "<filter xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <name>parallel universe</name>\n" +
                        "    </universe>\n" +
                        "</filter>");
        Element filteredXml = m_util.filter(unfilteredXml, filter);
        String expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <name>parallel universe</name>\n" +
                        "        <galaxy>\n" +
                        "            <name>Milky way</name>\n" +
                        "            <pet-name>My home galaxy</pet-name>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System</system-name>\n" +
                        "                <planets>\n" +
                        "                    <no-of-planets>1</no-of-planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container</container>\n" +
                        "                            <another-satellite-key>Moon</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System 2</system-name>\n" +
                        "                <planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth 2</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth 2 Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container 2</container>\n" +
                        "                            <another-satellite-key>Moon 2</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "        </galaxy>\n" +
                        "    </universe>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);

        filter = getElement(
                "<filter xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <name>universe 1</name>\n" +
                        "        <galaxy>\n" +
                        "            <pet-name>My home galaxy</pet-name>\n" +
                        "        </galaxy>\n" +
                        "    </universe>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <name>universe 1</name>\n" +
                        "        <galaxy>\n" +
                        "            <name>Milky way</name>\n" +
                        "            <pet-name>My home galaxy</pet-name>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System</system-name>\n" +
                        "                <planets>\n" +
                        "                    <no-of-planets>1</no-of-planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container</container>\n" +
                        "                            <another-satellite-key>Moon</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System 2</system-name>\n" +
                        "                <planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth 2</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth 2 Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container 2</container>\n" +
                        "                            <another-satellite-key>Moon 2</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "        </galaxy>\n" +
                        "    </universe>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);

        filter = getElement(
                "<nc:filter xmlns:nc=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <ut:universe xmlns:ut=\"http://test-company/country-universe\">\n" +
                        "        <ut:name>universe 1</ut:name>\n" +
                        "        <galaxy xmlns=\"http://test-company/country-universe\">\n" +
                        "            <name>Milky way</name>\n" +
                        "            <pet-name>My home galaxy</pet-name>\n" +
                        "            <planetary-system/>\n" +
                        "        </galaxy>\n" +
                        "    </ut:universe>\n" +
                        "</nc:filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <name>universe 1</name>\n" +
                        "        <galaxy>\n" +
                        "            <name>Milky way</name>\n" +
                        "            <pet-name>My home galaxy</pet-name>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System</system-name>\n" +
                        "                <planets>\n" +
                        "                    <no-of-planets>1</no-of-planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container</container>\n" +
                        "                            <another-satellite-key>Moon</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System 2</system-name>\n" +
                        "                <planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth 2</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth 2 Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container 2</container>\n" +
                        "                            <another-satellite-key>Moon 2</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "        </galaxy>\n" +
                        "    </universe>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);

        filter = getElement(
                "<filter xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <name>universe 1</name>\n" +
                        "        <galaxy>\n" +
                        "            <name>Milky way</name>\n" +
                        "            <pet-name>My home galaxy</pet-name>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Lunar System</system-name>\n" +
                        "            </planetary-system>\n" +
                        "        </galaxy>\n" +
                        "    </universe>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"/>\n";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);
    }

    @Test
    public void testFilterMixed() throws Exception {
        Element unfilteredXml = getFullDsXml();
        Element filter = getElement(
                "<filter xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company/country-universe\">\n" +
                        "        <country>\n" +
                        "            <state>\n" +
                        "                <district>\n" +
                        "                    <name>Mangalore</name>\n" +
                        "                </district>\n" +
                        "            </state>\n" +
                        "        </country>\n" +
                        "    </world>" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <name>parallel universe</name>\n" +
                        "    </universe>\n" +
                        "</filter>");
        Element filteredXml = m_util.filter(unfilteredXml, filter);
        String expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "<world xmlns=\"http://test-company/country-universe\">\n" +
                        "    <country>\n" +
                        "        <country-name>India</country-name>\n" +
                        "        <state>\n" +
                        "            <district>\n" +
                        "                <mayor-name>Kavita Sanil</mayor-name>\n" +
                        "                <name>Mangalore</name>\n" +
                        "            </district>\n" +
                        "            <name>Karnataka</name>\n" +
                        "        </state>\n" +
                        "    </country>\n" +
                        "</world>" +
                        "    <universe xmlns=\"http://test-company/country-universe\">\n" +
                        "        <name>parallel universe</name>\n" +
                        "        <galaxy>\n" +
                        "            <name>Milky way</name>\n" +
                        "            <pet-name>My home galaxy</pet-name>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System</system-name>\n" +
                        "                <planets>\n" +
                        "                    <no-of-planets>1</no-of-planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container</container>\n" +
                        "                            <another-satellite-key>Moon</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "            <planetary-system>\n" +
                        "                <system-name>Solar System 2</system-name>\n" +
                        "                <planets>\n" +
                        "                    <planet>\n" +
                        "                        <planet-name>Mother Earth 2</planet-name>\n" +
                        "                        <another-planet-key>Mother Earth 2 Key2</another-planet-key>\n" +
                        "                        <satellite>\n" +
                        "                            <container>weird key named container 2</container>\n" +
                        "                            <another-satellite-key>Moon 2</another-satellite-key>\n" +
                        "                        </satellite>\n" +
                        "                    </planet>\n" +
                        "                </planets>\n" +
                        "            </planetary-system>\n" +
                        "        </galaxy>\n" +
                        "    </universe>\n" +
                        "</data>";
        assertXMLEquals(getElement(expectedXmlStr), filteredXml);
    }

    @Test
    public void testEmptyFilter() throws Exception {
        Element unfilteredXml = getFullDsXml();
        Element filter = getElement(
                "<filter xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "</filter>");
        Element filteredXml = m_util.filter(unfilteredXml, filter);
        String expectedResponse = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"/>";
        assertXMLEquals(DocumentUtils.stringToDocument(expectedResponse).getDocumentElement(), filteredXml);
    }

    @Test
    public void testDoFilterFilters() throws Exception {
        Element unfilteredXml = getElement(UNIVERSE1);
        Element filter = getElement(
                "<filter xmlns=\"my:own:ns\">\n" +
                        "</filter>");
        FilterNode filterNode = new FilterNode();
        FilterUtil.processFilter(filterNode, DocumentUtils.getChildElements(filter), m_schemaRegistry);
        SchemaPath universeSp = SchemaPathBuilder.fromString("(http://test-company/country-universe?revision=2017-06-14)universe");
        Document document = DocumentUtils.getNewDocument();
        Element outputNode = document.createElementNS("http://test-company/country-universe",
                "universe");
        m_util.doFilter(document, filterNode, m_schemaRegistry.getDataSchemaNode(universeSp), unfilteredXml, outputNode);
        String expectedResponse = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"/>";
        assertXMLEquals(getElement(UNIVERSE1), outputNode);
    }

    private Element getFullDsXml() {
        return getElement(FULL_DS_STR);
    }

    private Element getElement(String xmlStr) {
        try {
            return DocumentUtils.stringToDocument(xmlStr).getDocumentElement();
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testMatchNodeFilter() {
        String filterXml = "<anv:device-manager xmlns:anv=\"http://www.test-company.com/solutions/anv\">" +
                "<adh:device-holder xmlns:adh=\"http://www.test-company.com/solutions/anv-device-holders\">" +
                "<adh:name>TestANV</adh:name>" +
                "<adh:device>" +
                "<adh:device-id>R1.S1.LT1.PON1.ONT1</adh:device-id>" +
                "<adh:device-specific-data>" +
                "<if:interfaces xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">" +
                "<if:interface>" +
                "<if:name>int1</if:name>" +
                "</if:interface>" +
                "</if:interfaces>" +
                "</adh:device-specific-data>" +
                "</adh:device>" +
                "</adh:device-holder>" +
                "</anv:device-manager>";
        Element filter = getElement(filterXml);
        List<Element> filterXmlElements = new ArrayList<>();
        filterXmlElements.add(filter);
        FilterNode filterNode = new FilterNode();
        Set<QName> qnames = new HashSet<>();
        qnames.add(QName.create(DEVICE_HOLDER_NS, "device-specific-data"));
        SchemaRegistry registry = mock(SchemaRegistry.class);
        when(registry.retrieveAllMountPointsPath()).thenReturn(qnames);
        FilterUtil.processFilter(filterNode, filterXmlElements, registry);
        FilterNode deviceMgrNode = filterNode.getContainmentNodes("device-manager").get(0);
        FilterNode deviceHolderNode = deviceMgrNode.getContainmentNodes("device-holder").get(0);
        FilterNode deviceNode = deviceHolderNode.getContainmentNodes("device").get(0);
        FilterNode deviceSpecificDataNode = deviceNode.getContainmentNodes("device-specific-data").get(0);
        assertFalse(deviceSpecificDataNode.isMountPointImmediateChild());
        FilterNode interfacesNode = deviceSpecificDataNode.getContainmentNodes("interfaces").get(0);
        assertTrue(interfacesNode.isMountPointImmediateChild());
        FilterNode interfaceNode = interfacesNode.getContainmentNodes("interface").get(0);
        FilterMatchNode interfaceNameMatchNode = interfaceNode.getMatchNodes().get(0);
        QName interfaceNameQName = QName.create(INTF_NS, "name");
        assertTrue(interfaceNameMatchNode.isSameQName(interfaceNameQName));
    }

    @Test
    public void testSelectNodeFilter() {
        String filterXml = "<anv:device-manager xmlns:anv=\"http://www.test-company.com/solutions/anv\">" +
                "<adh:device-holder xmlns:adh=\"http://www.test-company.com/solutions/anv-device-holders\">" +
                "<adh:name>TestANV</adh:name>" +
                "<adh:device>" +
                "<adh:device-id>R1.S1.LT1.PON1.ONT1</adh:device-id>" +
                "<adh:device-specific-data>" +
                "<if:interfaces xmlns:if=\"urn:ietf:params:xml:ns:yang:ietf-interfaces\">" +
                "</if:interfaces>" +
                "</adh:device-specific-data>" +
                "</adh:device>" +
                "</adh:device-holder>" +
                "</anv:device-manager>";
        Element filter = getElement(filterXml);
        List<Element> filterXmlElements = new ArrayList<>();
        filterXmlElements.add(filter);
        FilterNode filterNode = new FilterNode();
        Set<QName> qnames = new HashSet<>();
        qnames.add(QName.create(DEVICE_HOLDER_NS, "device-specific-data"));
        SchemaRegistry registry = mock(SchemaRegistry.class);
        when(registry.retrieveAllMountPointsPath()).thenReturn(qnames);
        FilterUtil.processFilter(filterNode, filterXmlElements, registry);
        FilterNode deviceMgrNode = filterNode.getContainmentNodes("device-manager").get(0);
        FilterNode deviceHolderNode = deviceMgrNode.getContainmentNodes("device-holder").get(0);
        FilterNode deviceNode = deviceHolderNode.getContainmentNodes("device").get(0);
        FilterNode deviceSpecificDataNode = deviceNode.getContainmentNodes("device-specific-data").get(0);
        assertFalse(deviceSpecificDataNode.isMountPointImmediateChild());
        FilterNode interfacesNode = deviceSpecificDataNode.getSelectNode("interfaces", INTF_NS);
        assertTrue(interfacesNode.isMountPointImmediateChild());
        ;
    }

    @Test
    public void testFilterWithoutConfigTagInStore() throws IOException, SAXException {

        String documentString = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "    <log-app:applications xmlns:log-app=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "        <active-applications xmlns=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "            <app-name>Stack</app-name>\n" +
                "            <modules>\n" +
                "                <mod-name>rpc</mod-name>\n" +
                "                <level>error</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>notification</mod-name>\n" +
                "                <level>critical</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>access-control</mod-name>\n" +
                "                <level>info</level>\n" +
                "            </modules>\n" +
                "        </active-applications>\n" +
                "        <active-applications xmlns=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "            <app-name>ONT</app-name>\n" +
                "            <modules>\n" +
                "                <mod-name>software</mod-name>\n" +
                "                <level>debug</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>ranging</mod-name>\n" +
                "                <level>warning</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>configuration</mod-name>\n" +
                "                <level>info</level>\n" +
                "            </modules>\n" +
                "        </active-applications>\n" +
                "    </log-app:applications>\n" +
                "</data>\n";

        String filterString = "<filter xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "    <log-app:applications xmlns:log-app=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "        <log-app:app-tag>dummy</log-app:app-tag>\n" +
                "        <log-app:active-applications>\n" +
                "            <log-app:app-name>ONT</log-app:app-name>\n" +
                "        </log-app:active-applications>\n" +
                "    </log-app:applications>\n" +
                "</filter>";

        Element documentElement = getElement(documentString);
        Element filterElement = getElement(filterString);
        Element filteredElement = m_util.filter(documentElement, filterElement);
        String expectedString = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"/>";
        assertXMLEquals(getElement(expectedString), filteredElement);
    }

    @Test
    public void testFilterWithoutMatchingConfigTagValueInStore() throws IOException, SAXException {

        String documentString = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "    <log-app:applications xmlns:log-app=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "        <log-app:app-tag>DIFFERENT</log-app:app-tag>\n" +
                "        <active-applications xmlns=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "            <app-name>Stack</app-name>\n" +
                "            <modules>\n" +
                "                <mod-name>rpc</mod-name>\n" +
                "                <level>error</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>notification</mod-name>\n" +
                "                <level>critical</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>access-control</mod-name>\n" +
                "                <level>info</level>\n" +
                "            </modules>\n" +
                "        </active-applications>\n" +
                "        <active-applications xmlns=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "            <app-name>ONT</app-name>\n" +
                "            <modules>\n" +
                "                <mod-name>software</mod-name>\n" +
                "                <level>debug</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>ranging</mod-name>\n" +
                "                <level>warning</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>configuration</mod-name>\n" +
                "                <level>info</level>\n" +
                "            </modules>\n" +
                "        </active-applications>\n" +
                "    </log-app:applications>\n" +
                "</data>\n";

        String filterString = "<filter xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "    <log-app:applications xmlns:log-app=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "        <log-app:app-tag>dummy</log-app:app-tag>\n" +
                "        <log-app:active-applications>\n" +
                "            <log-app:app-name>ONT</log-app:app-name>\n" +
                "        </log-app:active-applications>\n" +
                "    </log-app:applications>\n" +
                "</filter>";

        Element documentElement = getElement(documentString);
        Element filterElement = getElement(filterString);
        Element filteredElement = m_util.filter(documentElement, filterElement);
        String expectedString = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\"/>";
        assertXMLEquals(getElement(expectedString), filteredElement);
    }

    @Test
    public void testFilterWithMatchingConfigTagValueInStore() throws IOException, SAXException {

        String documentString = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "    <log-app:applications xmlns:log-app=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "        <log-app:app-tag>dummy</log-app:app-tag>\n" +
                "        <active-applications xmlns=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "            <app-name>Stack</app-name>\n" +
                "            <modules>\n" +
                "                <mod-name>rpc</mod-name>\n" +
                "                <level>error</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>notification</mod-name>\n" +
                "                <level>critical</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>access-control</mod-name>\n" +
                "                <level>info</level>\n" +
                "            </modules>\n" +
                "        </active-applications>\n" +
                "        <active-applications xmlns=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "            <app-name>ONT</app-name>\n" +
                "            <modules>\n" +
                "                <mod-name>software</mod-name>\n" +
                "                <level>debug</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>ranging</mod-name>\n" +
                "                <level>warning</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>configuration</mod-name>\n" +
                "                <level>info</level>\n" +
                "            </modules>\n" +
                "        </active-applications>\n" +
                "    </log-app:applications>\n" +
                "</data>\n";

        String filterString = "<filter xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "    <log-app:applications xmlns:log-app=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "        <log-app:app-tag>dummy</log-app:app-tag>\n" +
                "        <log-app:active-applications>\n" +
                "            <log-app:app-name>ONT</log-app:app-name>\n" +
                "        </log-app:active-applications>\n" +
                "    </log-app:applications>\n" +
                "</filter>";

        Element documentElement = getElement(documentString);
        Element filterElement = getElement(filterString);
        Element filteredElement = m_util.filter(documentElement, filterElement);
        String expectedString = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "    <applications xmlns=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "        <log-app:app-tag xmlns:log-app=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">dummy</log-app:app-tag>\n" +
                "        <active-applications>\n" +
                "            <app-name>ONT</app-name>\n" +
                "            <modules>\n" +
                "                <mod-name>software</mod-name>\n" +
                "                <level>debug</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>ranging</mod-name>\n" +
                "                <level>warning</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>configuration</mod-name>\n" +
                "                <level>info</level>\n" +
                "            </modules>\n" +
                "        </active-applications>\n" +
                "    </applications>\n" +
                "</data>";

        assertXMLEquals(getElement(expectedString), filteredElement);
    }

    @Test
    public void testFilterWithConfigListHavingNonMatchingLeafValueInStore() throws IOException, SAXException {

        String documentString = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "    <log-app:applications xmlns:log-app=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "        <log-app:config-app>\n" +
                "            <log-app:app-id>DIFFERENT</log-app:app-id>\n" +
                "        </log-app:config-app>\n" +
                "        <active-applications xmlns=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "            <app-name>Stack</app-name>\n" +
                "            <modules>\n" +
                "                <mod-name>rpc</mod-name>\n" +
                "                <level>error</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>notification</mod-name>\n" +
                "                <level>critical</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>access-control</mod-name>\n" +
                "                <level>info</level>\n" +
                "            </modules>\n" +
                "        </active-applications>\n" +
                "        <active-applications xmlns=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "            <app-name>ONT</app-name>\n" +
                "            <modules>\n" +
                "                <mod-name>software</mod-name>\n" +
                "                <level>debug</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>ranging</mod-name>\n" +
                "                <level>warning</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>configuration</mod-name>\n" +
                "                <level>info</level>\n" +
                "            </modules>\n" +
                "        </active-applications>\n" +
                "    </log-app:applications>\n" +
                "</data>\n";

        String filterString = "<filter xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "    <log-app:applications xmlns:log-app=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "        <log-app:config-app>\n" +
                "            <log-app:app-id>dummy</log-app:app-id>\n" +
                "        </log-app:config-app>\n" +
                "        <log-app:active-applications>\n" +
                "            <log-app:app-name>ONT</log-app:app-name>\n" +
                "        </log-app:active-applications>\n" +
                "    </log-app:applications>\n" +
                "</filter>";

        Element documentElement = getElement(documentString);
        Element filterElement = getElement(filterString);
        Element filteredElement = m_util.filter(documentElement, filterElement);
        String expectedString = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "    <applications xmlns=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "        <active-applications>\n" +
                "            <app-name>ONT</app-name>\n" +
                "            <modules>\n" +
                "                <mod-name>software</mod-name>\n" +
                "                <level>debug</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>ranging</mod-name>\n" +
                "                <level>warning</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>configuration</mod-name>\n" +
                "                <level>info</level>\n" +
                "            </modules>\n" +
                "        </active-applications>\n" +
                "    </applications>\n" +
                "</data>";
        assertXMLEquals(getElement(expectedString), filteredElement);
    }

    @Test
    public void testFilterWithConfigListHavingMatchingLeafValueInStore() throws IOException, SAXException {

        String documentString = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "    <log-app:applications xmlns:log-app=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "        <log-app:config-app>\n" +
                "            <log-app:app-id>dummy</log-app:app-id>\n" +
                "        </log-app:config-app>\n" +
                "        <active-applications xmlns=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "            <app-name>Stack</app-name>\n" +
                "            <modules>\n" +
                "                <mod-name>rpc</mod-name>\n" +
                "                <level>error</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>notification</mod-name>\n" +
                "                <level>critical</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>access-control</mod-name>\n" +
                "                <level>info</level>\n" +
                "            </modules>\n" +
                "        </active-applications>\n" +
                "        <active-applications xmlns=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "            <app-name>ONT</app-name>\n" +
                "            <modules>\n" +
                "                <mod-name>software</mod-name>\n" +
                "                <level>debug</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>ranging</mod-name>\n" +
                "                <level>warning</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>configuration</mod-name>\n" +
                "                <level>info</level>\n" +
                "            </modules>\n" +
                "        </active-applications>\n" +
                "    </log-app:applications>\n" +
                "</data>\n";

        String filterString = "<filter xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "    <log-app:applications xmlns:log-app=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "        <log-app:config-app>\n" +
                "            <log-app:app-id>dummy</log-app:app-id>\n" +
                "        </log-app:config-app>\n" +
                "        <log-app:active-applications>\n" +
                "            <log-app:app-name>ONT</log-app:app-name>\n" +
                "        </log-app:active-applications>\n" +
                "    </log-app:applications>\n" +
                "</filter>";

        Element documentElement = getElement(documentString);
        Element filterElement = getElement(filterString);
        Element filteredElement = m_util.filter(documentElement, filterElement);
        String expectedString = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                "    <applications xmlns=\"http://www.test-company.com/Fixed-Networks/BBA/yang/test-logging-app\">\n" +
                "        <config-app>\n" +
                "            <app-id>dummy</app-id>\n" +
                "        </config-app>\n" +
                "        <active-applications>\n" +
                "            <app-name>ONT</app-name>\n" +
                "            <modules>\n" +
                "                <mod-name>software</mod-name>\n" +
                "                <level>debug</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>ranging</mod-name>\n" +
                "                <level>warning</level>\n" +
                "            </modules>\n" +
                "            <modules>\n" +
                "                <mod-name>configuration</mod-name>\n" +
                "                <level>info</level>\n" +
                "            </modules>\n" +
                "        </active-applications>\n" +
                "    </applications>\n" +
                "</data>";
        assertXMLEquals(getElement(expectedString), filteredElement);
    }
}
