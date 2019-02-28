package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import static org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil.DELIMITER;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.InvalidArgumentException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.broadband_forum.obbaa.netconf.server.util.TestUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.util.NoLockService;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;

/**
 * Created by keshava on 5/12/16.
 */
public class EMNKeyUtilTest {
    private static final String CC_NS = "test:yang-with-choice-cases";
    private static final String CCA_NS = "test:yang-with-choice-cases-augmenting-module";
    private static final String CC_REV = "2016-05-16";

    private static final SchemaPath ICE_CREAM_SP = SchemaPathUtil.fromString(
            CC_NS + DELIMITER + CC_REV + DELIMITER + "dessert" + DELIMITER +
                    CCA_NS + DELIMITER + CC_REV + DELIMITER + "seasonal-choices" + DELIMITER +
                    CCA_NS + DELIMITER + CC_REV + DELIMITER + "summer" + DELIMITER +
                    CCA_NS + DELIMITER + CC_REV + DELIMITER + "ice-cream" + DELIMITER);

    private static final SchemaPath VANILLA_SYR_SP = SchemaPathUtil.fromString(
            CC_NS + DELIMITER + CC_REV + DELIMITER + "dessert" + DELIMITER +
                    CCA_NS + DELIMITER + CC_REV + DELIMITER + "seasonal-choices" + DELIMITER +
                    CCA_NS + DELIMITER + CC_REV + DELIMITER + "summer" + DELIMITER +
                    CCA_NS + DELIMITER + CC_REV + DELIMITER + "ice-cream" + DELIMITER +
                    CCA_NS + DELIMITER + CC_REV + DELIMITER + "syrup-choices" + DELIMITER +
                    CCA_NS + DELIMITER + CC_REV + DELIMITER + "vanilla" + DELIMITER +
                    CCA_NS + DELIMITER + CC_REV + DELIMITER + "vanilla-syrup" + DELIMITER);
    private SchemaRegistry m_schemaRegistry;

    @Before
    public void setUp() throws SchemaBuildException {
        m_schemaRegistry = new SchemaRegistryImpl(Collections.emptyList(), Collections.emptySet(), Collections
                .emptyMap(), new NoLockService());
        List<YangTextSchemaSource> yangs = TestUtil.getJukeBoxYangs();
        yangs.addAll(TestUtil.getByteSources(Arrays.asList("/referenceyangs/yang-with-choice-cases@2016-05-16.yang",
                "/referenceyangs/yang-with-choice-cases-augmenting-module@2016-05-16.yang")));
        m_schemaRegistry.loadSchemaContext("EMNKeyUtilTest", yangs, Collections.emptySet(), Collections.emptyMap());
    }

    @Test
    public void testPopulateNamespaceInNodeId() throws InvalidArgumentException {
        //for a list
        ModelNodeId nodeId = new ModelNodeId("/container=jukebox/container=library", JukeboxConstants.JB_NS);
        nodeId.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, "invalid-ns", "artist"));
        nodeId.addRdn(new ModelNodeRdn("name", "invalid-ns2", "artist1"));
        ModelNodeId modifiedNodeId = EMNKeyUtil.populateNamespaces(nodeId, JukeboxConstants.ARTIST_SCHEMA_PATH,
                m_schemaRegistry);
        runAsserts(nodeId, modifiedNodeId);

        //for a container
        nodeId = new ModelNodeId("/container=jukebox", JukeboxConstants.JB_NS);
        nodeId.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, "invalid-ns", "library"));
        modifiedNodeId = EMNKeyUtil.populateNamespaces(nodeId, JukeboxConstants.LIBRARY_SCHEMA_PATH, m_schemaRegistry);
        runAsserts(nodeId, modifiedNodeId);

        //for a list within a list
        nodeId = new ModelNodeId("/container=jukebox/container=library/", JukeboxConstants.JB_NS);
        nodeId.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, "invalid-ns", "artist"));
        nodeId.addRdn(new ModelNodeRdn("name", "invalid-ns2", "artist1"));
        nodeId.addRdn(new ModelNodeRdn(ModelNodeRdn.CONTAINER, "invalid-ns", "album"));
        nodeId.addRdn(new ModelNodeRdn("name", "invalid-ns2", "album1"));
        modifiedNodeId = EMNKeyUtil.populateNamespaces(nodeId, JukeboxConstants.ALBUM_SCHEMA_PATH, m_schemaRegistry);
        runAsserts(nodeId, modifiedNodeId);

        //for an empty node id
        nodeId = new ModelNodeId();
        modifiedNodeId = EMNKeyUtil.populateNamespaces(nodeId, JukeboxConstants.JUKEBOX_SCHEMA_PATH.getParent(),
                m_schemaRegistry);
        runAsserts(nodeId, modifiedNodeId);

        //invalid node ids, result in exception
        nodeId = new ModelNodeId("/container=jukebox/container=blah", JukeboxConstants.JB_NS);
        try {
            EMNKeyUtil.populateNamespaces(nodeId, JukeboxConstants.LIBRARY_SCHEMA_PATH, m_schemaRegistry);
            fail("Expected an InvalidArgumentException, but did not get it..");
        } catch (IllegalArgumentException e) {
            //expected
        }

        //nodeId is shorter
        nodeId = new ModelNodeId("/container=jukebox/contain=library", JukeboxConstants.JB_NS);
        try {
            EMNKeyUtil.populateNamespaces(nodeId, JukeboxConstants.ARTIST_SCHEMA_PATH, m_schemaRegistry);
            fail("Expected an InvalidArgumentException, but did not get it..");
        } catch (IllegalArgumentException e) {
            //expected
        }

        //nodeId is longer
        nodeId = new ModelNodeId("/container=jukebox/contain=library", "invalid-ns");
        try {
            EMNKeyUtil.populateNamespaces(nodeId, JukeboxConstants.JUKEBOX_SCHEMA_PATH, m_schemaRegistry);
            fail("Expected an InvalidArgumentException, but did not get it..");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    @Test
    public void testPopulateNamespaceInNodeIdChoiceCaseNodes() throws InvalidArgumentException {
        ModelNodeId nodeId = new ModelNodeId("/container=dessert/container=ice-cream", CC_NS);
        ModelNodeId modifiedNodeId = EMNKeyUtil.populateNamespaces(nodeId, ICE_CREAM_SP, m_schemaRegistry);
        runAssertsChoiceCases(nodeId, modifiedNodeId);

        nodeId = new ModelNodeId("/container=dessert/container=ice-cream/container=vanilla-syrup", CC_NS);
        modifiedNodeId = EMNKeyUtil.populateNamespaces(nodeId, VANILLA_SYR_SP, m_schemaRegistry);
        runAssertsChoiceCases(nodeId, modifiedNodeId);

        //nodeId is longer
        nodeId = new ModelNodeId("/container=dessert/container=ice-cream/container=vanilla-syrup", CC_NS);
        try {
            EMNKeyUtil.populateNamespaces(nodeId, ICE_CREAM_SP, m_schemaRegistry);
            fail("Expected an InvalidArgumentException, but did not get it..");
        } catch (IllegalArgumentException e) {
            //expected
        }

        //nodeId is shorter
        nodeId = new ModelNodeId("/container=dessert/container=ice-cream", CC_NS);
        try {
            EMNKeyUtil.populateNamespaces(nodeId, VANILLA_SYR_SP, m_schemaRegistry);
            fail("Expected an InvalidArgumentException, but did not get it..");
        } catch (IllegalArgumentException e) {
            //expected
        }
    }

    private void runAssertsChoiceCases(ModelNodeId nodeId, ModelNodeId modifiedNodeId) {
        assertEquals(nodeId.getRdns().size(), modifiedNodeId.getRdns().size());
        //first RDN in CC NS
        runAsserts(nodeId, modifiedNodeId, 0, CC_NS);

        //rest RDNs in CCA NS
        for (int i = 1; i < nodeId.getRdns().size(); i++) {
            runAsserts(nodeId, modifiedNodeId, i, CCA_NS);
        }
    }

    private void runAsserts(ModelNodeId nodeId, ModelNodeId modifiedNodeId) {
        assertEquals(nodeId.getRdns().size(), modifiedNodeId.getRdns().size());
        for (int i = 0; i < nodeId.getRdns().size(); i++) {
            runAsserts(nodeId, modifiedNodeId, i, JukeboxConstants.JB_NS);
        }
    }

    private void runAsserts(ModelNodeId nodeId, ModelNodeId modifiedNodeId, int index, String expectedNS) {
        ModelNodeRdn modelNodeRdn = modifiedNodeId.getRdns().get(index);
        //check NS is fixed
        assertEquals(expectedNS, modelNodeRdn.getNamespace());
        ModelNodeRdn ordinalNodeRdn = nodeId.getRdns().get(index);
        //check name and values have not changed
        assertEquals(ordinalNodeRdn.getRdnName(), modelNodeRdn.getRdnName());
        assertEquals(ordinalNodeRdn.getRdnValue(), modelNodeRdn.getRdnValue());
    }

}
