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

package org.broadband_forum.obbaa.netconf.api.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;
import org.opendaylight.yangtools.yang.model.api.type.Uint32TypeDefinition;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;

public class YangParserUtilTest {

	private static final String[] IETF_YANG_PATH_LIST = { "/yangs/ietf/ietf-inet-types.yang",
			"/yangs/ietf/ietf-yang-types.yang" };

	private static final String IETF_YANG_PATH = "/yangs/ietf/ietf-yang-types.yang";
    private static final String IETF_YANG_FILE_NAME = "ietf-yang-types.yang";
	
	private List<YangTextSchemaSource> m_yangschemaSources = new ArrayList<YangTextSchemaSource>();
	private List<File> m_yangFileList = new ArrayList<File>();

	@Before
	public void init() {
		for (String yangPath : IETF_YANG_PATH_LIST) {
			m_yangFileList.add(new File(YangParserUtilTest.class.getResource(yangPath).getPath()));
			m_yangschemaSources.add(YangParserUtil.getYangSource(YangParserUtilTest.class.getResource(yangPath)));
		}
	}
	
	// for ODL testing purposes
    public static void main(String[] args) {
        if (args.length != 0) {
            String directory = args[0];
            Collection<File> files = FileUtils.listFiles(new File(directory), FileFilterUtils.suffixFileFilter(".yang"), TrueFileFilter.INSTANCE);
            List<File> fileList = new ArrayList<File>(files);
            SchemaContext context = YangParserUtil.parseFiles("test", fileList);
            System.out.println("Context is: "+ context);
        }
    }	

	@Test
	public void testParseSchemaSources() {
		SchemaContext context = YangParserUtil.parseSchemaSources(YangParserUtilTest.class.getName(),
				m_yangschemaSources);
		assertNotNull(context);
	}

	@Test
	public void testGetYangSource() throws IOException {
		YangTextSchemaSource schemaSource = YangParserUtil.getYangSource(
				YangParserUtilTest.class.getResource(IETF_YANG_PATH),
				new ByteArrayInputStream(IETF_YANG_PATH.getBytes()));
		assertNotNull(schemaSource);
		assertEquals(IETF_YANG_FILE_NAME, schemaSource.getSymbolicName().get());
	}

	@Test
	public void testGetYangSource_URL() {

		YangTextSchemaSource schemaSource = YangParserUtil
				.getYangSource(YangParserUtilTest.class.getResource(IETF_YANG_PATH));
		assertNotNull(schemaSource);
	}

	@Test
	public void testGetYangSource_Filename() {
		YangTextSchemaSource schemaSource = YangParserUtil.getYangSource(IETF_YANG_PATH);
		assertNotNull(schemaSource);
	}

	@Test
	public void testParseFiles() {
		SchemaContext context = YangParserUtil.parseFiles(YangParserUtilTest.class.getName(), m_yangFileList);
		assertNotNull(context);
	}
	
	@Test
	public void testGetParsedModule() {
		SchemaContext context = YangParserUtil.parseSchemaSources(YangParserUtilTest.class.getName(),
				m_yangschemaSources);
		assertNotNull(context);

		Module module = YangParserUtil.getParsedModule(context,
				YangParserUtilTest.class.getResource(IETF_YANG_PATH).getPath());
		assertNotNull(module);
	}
	
	@Test
	public void testApplicationOfDeviationsOnYangs() throws IOException {

		QName line = QName.create("urn:broadband-forum-org:yang:bbf-xdsl", "2016-01-25", "line");
		QName supported_node = QName.create("urn:broadband-forum-org:yang:bbf-xdsl", "2016-01-25", "supported-mode");
		QName minimum_expected_throughput = QName.create("urn:broadband-forum-org:yang:bbf-xdsl", "2016-01-25", "minimum-expected-throughput");
		QName maximum_expected_throughput = QName.create("urn:broadband-forum-org:yang:bbf-xdsl", "2016-01-25", "maximum-expected-throughput");
		QName interfaces = QName.create("urn:ietf:params:xml:ns:yang:ietf-interfaces", "2014-05-08", "interfaces");
		QName interfaceQName = QName.create("urn:ietf:params:xml:ns:yang:ietf-interfaces", "2014-05-08", "interface");

		YangTextSchemaSource a = YangParserUtil
				.getYangSource(YangParserUtilTest.class.getResource("/deviationTestYangs/bbf-if-type.yang"));
		YangTextSchemaSource b = YangParserUtil
				.getYangSource(YangParserUtilTest.class.getResource("/deviationTestYangs/bbf-xdsl.yang"));
		YangTextSchemaSource c = YangParserUtil
				.getYangSource(YangParserUtilTest.class.getResource("/deviationTestYangs/ietf-interfaces.yang"));
		YangTextSchemaSource d = YangParserUtil
				.getYangSource(YangParserUtilTest.class.getResource("/deviationTestYangs/ietf-yang-types.yang"));
		YangTextSchemaSource e = YangParserUtil
				.getYangSource(YangParserUtil.class.getResource("/deviationTestYangs/test-xdsl-dev.yang"));

		List<YangTextSchemaSource> schemaSources = new ArrayList<>();

		// adding deviation folder yangs
		schemaSources.add(a);
		schemaSources.add(b);
		schemaSources.add(c);
		schemaSources.add(d);
		schemaSources.add(e);

		QName moduleQName = QName.create("urn:broadband-forum-org:yang:bbf-xdsl", "2016-01-25", "bbf-xdsl");
		QName deviationModuleQName = QName.create("urn:xxxxx-org:yang:test-xdsl-dev", "2017-07-05", "test-xdsl-dev");

		Set<QName> moduleDeviations = new HashSet<>();
		moduleDeviations.add(deviationModuleQName);

		Map<QName, Set<QName>> supportedDeviations = new java.util.HashMap<>();
		supportedDeviations.put(moduleQName, moduleDeviations);

		LeafListSchemaNode leafListSchemaNode = null;
		LeafSchemaNode leafSchemaNodeMinimumExpectedThroughput = null;
		LeafSchemaNode leafSchemaNodeMaximumExpectedThroughput = null;
		RangeConstraint minimumExpectedThroughputRangeConstraint = null;
		RangeConstraint maximumExpectedThroughputRangeConstraint = null;

				// deviations are not applied
		SchemaContext context = YangParserUtil.parseSchemaSources(YangParserUtil.class.getName(), schemaSources, null, null);
		leafListSchemaNode = (LeafListSchemaNode) SchemaContextUtil.findDataSchemaNode(context,
				SchemaPath.create(true, interfaces, interfaceQName, line, supported_node));
		assertEquals((Integer) 1, leafListSchemaNode.getElementCountConstraint().get().getMinElements());


		// but the module is parsed and present
		assertTrue(context.findModule("test-xdsl-dev", Revision.of("2017-07-05")).isPresent());

		// passing empty map
		context = YangParserUtil.parseSchemaSources(YangParserUtil.class.getName(), schemaSources, null,
				Collections.emptyMap());
		leafListSchemaNode = (LeafListSchemaNode) SchemaContextUtil.findDataSchemaNode(context,
				SchemaPath.create(true, interfaces, interfaceQName, line, supported_node));
		assertEquals((Integer) 1, leafListSchemaNode.getElementCountConstraint().get().getMinElements());
        // but the module is parsed and present
        assertTrue(context.findModule("test-xdsl-dev", Revision.of("2017-07-05")).isPresent());

		// passing additional deviations in map
		QName testModule = QName.create("urn:broadband-forum-org:yang:bbf-test", "2016-01-25", "bbf-test");
		QName testDeviationModule = QName.create("urn:xxxxx-org:yang:test-test-dev", "2017-07-05", "test-test-dev");
		moduleDeviations.clear();
		moduleDeviations.add(testDeviationModule);
		supportedDeviations.put(testModule, moduleDeviations);
		context = YangParserUtil.parseSchemaSources(YangParserUtil.class.getName(), schemaSources, null,
				supportedDeviations);
		leafListSchemaNode = (LeafListSchemaNode) SchemaContextUtil.findDataSchemaNode(context,
				SchemaPath.create(true, interfaces, interfaceQName, line, supported_node));
		assertEquals((Integer) 1, leafListSchemaNode.getElementCountConstraint().get().getMinElements());

		leafSchemaNodeMinimumExpectedThroughput = (LeafSchemaNode) SchemaContextUtil.findDataSchemaNode(context,
				SchemaPath.create(true, interfaces, interfaceQName, line, minimum_expected_throughput));
		minimumExpectedThroughputRangeConstraint = ((Uint32TypeDefinition)(leafSchemaNodeMinimumExpectedThroughput.getType())).getRangeConstraint().orElse(null);
		if(minimumExpectedThroughputRangeConstraint!=null){
			RangeSet rangeSet = minimumExpectedThroughputRangeConstraint.getAllowedRanges();
			Set<Range> ranges = rangeSet.asRanges();
			for (Range range: ranges) {
				assertEquals(true,range.test(4L));
				assertEquals(true,range.test(5L));
				assertEquals(true,range.test(10L));
				assertEquals(true,range.test(11L));
			}
		}
		leafSchemaNodeMaximumExpectedThroughput = (LeafSchemaNode) SchemaContextUtil.findDataSchemaNode(context,
				SchemaPath.create(true, interfaces, interfaceQName, line, maximum_expected_throughput));
		maximumExpectedThroughputRangeConstraint = ((Uint32TypeDefinition)(leafSchemaNodeMaximumExpectedThroughput.getType())).getRangeConstraint().orElse(null);
		if(maximumExpectedThroughputRangeConstraint!=null){
			RangeSet rangeSet = maximumExpectedThroughputRangeConstraint.getAllowedRanges();
			Set<Range> ranges = rangeSet.asRanges();
			for (Range range: ranges) {
				assertEquals(true,range.test(3L));
				assertEquals(true,range.test(5L));
				assertEquals(true,range.test(7L));
			}
		}
        // the module is parsed and present
        assertTrue(context.findModule("test-xdsl-dev", Revision.of("2017-07-05")).isPresent());
        // this one not, because it is not in the schema sources
        assertFalse(context.findModule("test-test-dev", Revision.of("2017-07-05")).isPresent());

		// deviations are provided
		moduleDeviations.clear();
		moduleDeviations.add(deviationModuleQName);
		supportedDeviations.clear();
		supportedDeviations.put(moduleQName, moduleDeviations);
		context = YangParserUtil.parseSchemaSources(YangParserUtil.class.getName(), schemaSources, null,
				supportedDeviations);
		leafListSchemaNode = (LeafListSchemaNode) SchemaContextUtil.findDataSchemaNode(context,
				SchemaPath.create(true, interfaces, interfaceQName, line, supported_node));
		assertEquals((Integer) 2, leafListSchemaNode.getElementCountConstraint().get().getMinElements());
		leafSchemaNodeMinimumExpectedThroughput = (LeafSchemaNode) SchemaContextUtil.findDataSchemaNode(context,
				SchemaPath.create(true, interfaces, interfaceQName, line, minimum_expected_throughput));
		minimumExpectedThroughputRangeConstraint = ((Uint32TypeDefinition)(leafSchemaNodeMinimumExpectedThroughput.getType())).getRangeConstraint().orElse(null);
		if(minimumExpectedThroughputRangeConstraint!=null){
			RangeSet rangeSet = minimumExpectedThroughputRangeConstraint.getAllowedRanges();
			Set<Range> ranges = rangeSet.asRanges();
			for (Range range: ranges) {
				assertEquals(false,range.test(4L));
				assertEquals(true,range.test(5L));
				assertEquals(true,range.test(10L));
				assertEquals(false,range.test(11L));
			}
		}
		leafSchemaNodeMaximumExpectedThroughput = (LeafSchemaNode) SchemaContextUtil.findDataSchemaNode(context,
				SchemaPath.create(true, interfaces, interfaceQName, line, maximum_expected_throughput));
		maximumExpectedThroughputRangeConstraint = ((Uint32TypeDefinition)(leafSchemaNodeMaximumExpectedThroughput.getType())).getRangeConstraint().orElse(null);
		if(maximumExpectedThroughputRangeConstraint!=null){
			RangeSet rangeSet = maximumExpectedThroughputRangeConstraint.getAllowedRanges();
			Set<Range> ranges = rangeSet.asRanges();
			for (Range range: ranges) {
				assertEquals(false,range.test(3L));
				assertEquals(true,range.test(5L));
				assertEquals(false,range.test(7L));
			}
		}
        // the module is parsed and present
        assertTrue(context.findModule("test-xdsl-dev", Revision.of("2017-07-05")).isPresent());
	}

}
