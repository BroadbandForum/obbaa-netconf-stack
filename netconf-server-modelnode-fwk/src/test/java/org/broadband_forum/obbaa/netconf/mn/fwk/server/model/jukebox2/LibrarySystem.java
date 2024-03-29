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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.StateAttributeUtil;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class LibrarySystem extends AbstractSubSystem {

	private static final String SONG_COUNT = "song-count";
	private static final String ALBUM_COUNT = "album-count";
	private static final String ARTIST_COUNT = "artist-count";

	@Override
	protected boolean byPassAuthorization() {
		return true;
	}

	@Override
	public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes) throws GetAttributeException {
		Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
		Document doc = DocumentUtils.createDocument();
		for (Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : attributes.entrySet()) {
			List<QName> stateQNames = entry.getValue().getFirst();
			Map<QName, Object> stateAttributes = new HashMap<>();
			
			for (QName attr : stateQNames) {
				if (attr.getLocalName().equals(ARTIST_COUNT)) {
					stateAttributes.put(attr, 2);
				}
				if (attr.getLocalName().equals(ALBUM_COUNT)) {
					stateAttributes.put(attr, 3);
				}
				if (attr.getLocalName().equals(SONG_COUNT)) {
					stateAttributes.put(attr, 7);
				}
			}
			List<Element> stateElements = StateAttributeUtil.convertToStateElements(stateAttributes, "jbox", doc);
			stateInfo.put(entry.getKey(), stateElements);
		}

		return stateInfo;
	}
}
