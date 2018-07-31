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

import java.util.Arrays;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.parser.YangParserUtil;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;

public class SubtreeFilterUtilTest {

    private static final String FULL_DS_STR = "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
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
            "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
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
            "    <world xmlns=\"http://test-company.test/country-universe\">\n" +
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
        m_schemaRegistry = new SchemaRegistryImpl(new NoLockService());
        m_schemaRegistry.buildSchemaContext(getTestYangFiles());
        m_util = new SubtreeFilterUtil(m_schemaRegistry);
    }

    private static List<YangTextSchemaSource> getTestYangFiles() {
        YangTextSchemaSource rpcTestYangFile = YangParserUtil.getYangSource(SubtreeFilterUtilTest.class.getResource
                ("/subtreefilterutiltest/country-universe@2017-06-14.yang"));
        return Arrays.asList(rpcTestYangFile);
    }

    @Test
    public void testSelectFilteringOnContainerNodes() throws Exception {
        Element unfilteredXml = getFullDsXml();
        Element filter = getElement(
                "<filter type=\"subtree\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company.test/country-universe\"/>\n" +
                        "</filter>");
        Element filteredXml = m_util.filter(unfilteredXml, filter);
        String expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <world xmlns=\"http://test-company.test/country-universe\">\n" +
                        "        <country/>\n" +
                        "    </world>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <world xmlns=\"http://test-company.test/country-universe\">\n" +
                        "        <country>\n" +
                        "            <country-name/>\n" +
                        "            <no-of-states/>\n" +
                        "        </country>\n" +
                        "    </world>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <world xmlns=\"http://test-company.test/country-universe\">\n" +
                        "        <country>\n" +
                        "            <country-name/>\n" +
                        "        </country>\n" +
                        "    </world>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company.test/country-universe\">\n" +
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
    public void testSelectFilteringOnListNodes() throws Exception {
        Element unfilteredXml = getFullDsXml();
        Element filter = getElement(
                "<filter type=\"subtree\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company.test/country-universe\"/>\n" +
                        "</filter>");
        Element filteredXml = m_util.filter(unfilteredXml, filter);
        String expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
                        "        <galaxy/>\n" +
                        "    </universe>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
                        "        <galaxy>\n" +
                        "            <name/>\n" +
                        "            <pet-name/>\n" +
                        "        </galaxy>\n" +
                        "    </universe>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
                        "        <galaxy>\n" +
                        "            <pet-name/>\n" +
                        "        </galaxy>\n" +
                        "    </universe>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <world xmlns=\"http://test-company.test/country-universe\">\n" +
                        "        <country>\n" +
                        "            <country-name>India</country-name>\n" +
                        "        </country>\n" +
                        "    </world>\n" +
                        "</filter>");
        Element filteredXml = m_util.filter(unfilteredXml, filter);
        String expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <world xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <world xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <world xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <world xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <world xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <world xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
                        "        <name>parallel universe</name>\n" +
                        "    </universe>\n" +
                        "</filter>");
        Element filteredXml = m_util.filter(unfilteredXml, filter);
        String expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
                        "        <name>universe 1</name>\n" +
                        "        <galaxy>\n" +
                        "            <pet-name>My home galaxy</pet-name>\n" +
                        "        </galaxy>\n" +
                        "    </universe>\n" +
                        "</filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <ut:universe xmlns:ut=\"http://test-company.test/country-universe\">\n" +
                        "        <ut:name>universe 1</ut:name>\n" +
                        "        <galaxy xmlns=\"http://test-company.test/country-universe\">\n" +
                        "            <name>Milky way</name>\n" +
                        "            <pet-name>My home galaxy</pet-name>\n" +
                        "            <planetary-system/>\n" +
                        "        </galaxy>\n" +
                        "    </ut:universe>\n" +
                        "</nc:filter>");
        filteredXml = m_util.filter(unfilteredXml, filter);
        expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <world xmlns=\"http://test-company.test/country-universe\">\n" +
                        "        <country>\n" +
                        "            <state>\n" +
                        "                <district>\n" +
                        "                    <name>Mangalore</name>\n" +
                        "                </district>\n" +
                        "            </state>\n" +
                        "        </country>\n" +
                        "    </world>" +
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
                        "        <name>parallel universe</name>\n" +
                        "    </universe>\n" +
                        "</filter>");
        Element filteredXml = m_util.filter(unfilteredXml, filter);
        String expectedXmlStr =
                "<data xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
                        "<world xmlns=\"http://test-company.test/country-universe\">\n" +
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
                        "    <universe xmlns=\"http://test-company.test/country-universe\">\n" +
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
        assertXMLEquals(DocumentUtils.stringToDocument(FULL_DS_STR).getDocumentElement(), filteredXml);
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

}
