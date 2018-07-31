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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.load;
import static org.broadband_forum.obbaa.netconf.server.util.TestUtil.loadAsXml;
import static org.junit.Assert.assertEquals;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaBuildException;

public class EditConfigTreeBuildTest extends AbstractEditConfigTestSetup {
    @SuppressWarnings("unused")
    private final static Logger LOGGER = Logger.getLogger(EditConfigTreeBuildTest.class);

    @Before
    public void initServer() throws SchemaBuildException {
        super.setup();
    }


    @Test
    public void testMatchAndEditAttribute() throws EditConfigException {
        EditContainmentNode root = new EditContainmentNode();
        m_model.prepareEditSubTree(root, loadAsXml("/editconfig-simple.xml"));
        assertEquals(load("/editconfig-simple-tree.txt"), root.toString(0, true));
    }

    @Test
    public void testDeleteAlbum() throws EditConfigException {
        EditContainmentNode root = new EditContainmentNode();
        m_model.prepareEditSubTree(root, loadAsXml("/editconfig-deletealbum.xml"));
        assertEquals(load("/editconfig-deletealbum.txt"), root.toString(0, true));
    }

    @Test
    public void testCreateAlbum() throws EditConfigException {
        EditContainmentNode root = new EditContainmentNode();
        m_model.prepareEditSubTree(root, loadAsXml("/editconfig-createalbum.xml"));
        assertEquals(load("/editconfig-createalbum.txt"), root.toString(0, true));
    }

    @Test
    public void testMixedOperations() throws EditConfigException {
        EditContainmentNode root = new EditContainmentNode();
        m_model.prepareEditSubTree(root, loadAsXml("/editconfig-mixedoperation.xml"));
        assertEquals(load("/editconfig-mixedoperation.txt"), root.toString(0, true));
    }

}
