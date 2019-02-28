package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * Created by kbhatk on 7/12/16.
 */
public class ModelServiceTest {

    @Test
    public void testGetName(){
        ModelService service = new ModelService();
        service.setModuleName("my-module");
        service.setModuleRevision("1988-03-27");
        assertEquals("my-module?revision=1988-03-27", service.getName());
    }

	@Test
	public void testGetDefaultXmlDocument() throws Exception {
		ModelService testObj = new ModelService();
		QName name1 = new QName("ns1", "name1");
		QName name2 = new QName("ns1", "name2");
		QName name3 = new QName("ns2", "name3");
		testObj.setRootElemQName(Arrays.asList(name1, name2, name3));
		List<Element> actualConfigElements = testObj.getDefaultSubtreeRootNodes();
		assertNotNull(actualConfigElements);
		assertEquals(3,actualConfigElements.size());

		List<String> expectedConfigElements = new ArrayList<>();
		String element1 = "<name1 xmlns=\"ns1\"/>";
		String element2 = "<name2 xmlns=\"ns1\"/>";
		String element3 = "<name3 xmlns=\"ns2\"/>";

		expectedConfigElements.add(element1);
		expectedConfigElements.add(element2);
		expectedConfigElements.add(element3);

		for(Element element: actualConfigElements){
			assertTrue(expectedConfigElements.contains(TestUtil.xmlToString(element)));
		}
	}
}
