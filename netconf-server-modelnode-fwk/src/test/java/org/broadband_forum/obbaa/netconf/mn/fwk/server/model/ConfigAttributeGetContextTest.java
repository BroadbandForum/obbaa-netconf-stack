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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.LibrarySystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.StateAttributeUtil;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ConfigAttributeGetContextTest {

    private ConfigAttributeGetContext attributeGetContext = new ConfigAttributeGetContext();

    @Test
    public void testGetAndAddSubSystem() {
        attributeGetContext.addAuthorizedSubSystem(Mockito.mock(LibrarySystem.class));
        attributeGetContext.addAuthorizedSubSystem(Mockito.mock(AlbumSubsystem.class));
        Set<SubSystem> subsystems = attributeGetContext.getCorrespondingSubSystem();
        Assert.assertEquals(subsystems.size(), 2);
    }

    private class AlbumSubsystem extends AbstractSubSystem {

        @Override
        protected boolean byPassAuthorization() {
            return true;
        }

        @Override
        public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> mapAttributes) throws GetAttributeException {
            Document doc = DocumentUtils.createDocument();
            Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
            for (Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : mapAttributes.entrySet()) {
                Map<QName, Object> stateAttributes = new LinkedHashMap<QName, Object>();
                ModelNodeId modelNodeId = entry.getKey();
                List<QName> attributes = entry.getValue().getFirst();
                for (QName attribute : attributes) {
                    // dummy logic
                }
                List<Element> stateElements = StateAttributeUtil.convertToStateElements(stateAttributes, "jbox", doc);
                stateInfo.put(modelNodeId, stateElements);
            }
            return stateInfo;
        }
    }

}
