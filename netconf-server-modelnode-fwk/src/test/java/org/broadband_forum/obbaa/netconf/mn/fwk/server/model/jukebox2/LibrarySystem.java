package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.yang.util.StateAttributeUtil;

public class LibrarySystem extends AbstractSubSystem {

	private static final String SONG_COUNT = "song-count";
	private static final String ALBUM_COUNT = "album-count";
	private static final String ARTIST_COUNT = "artist-count";

	private ModelNode m_rootModelNode;
	
	public void setRootModelNode(ModelNode rootNode) {
		this.m_rootModelNode = rootNode;
	}
	
	public ModelNode getLibraryNode() {
		return ((Jukebox)m_rootModelNode).getLibrary();
	}
	
	@Override
	public Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes) throws GetAttributeException {
		Map<ModelNodeId, List<Element>> stateInfo = new HashMap<>();
		Document doc = DocumentUtils.createDocument();
		for (Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> entry : attributes.entrySet()) {
			List<QName> stateQNames = entry.getValue().getFirst();
			Map<QName, Object> stateAttributes = new HashMap<>();
			
			Library library = (Library) getLibraryNode();
			for (QName attr : stateQNames) {
				if (attr.getLocalName().equals(ARTIST_COUNT)) {
					stateAttributes.put(attr, library.getArtistCount());
				}
				if (attr.getLocalName().equals(ALBUM_COUNT)) {
					stateAttributes.put(attr, library.getAlbumCount());
				}
				if (attr.getLocalName().equals(SONG_COUNT)) {
					stateAttributes.put(attr, library.getSongCount());
				}
			}
			List<Element> stateElements = StateAttributeUtil.convertToStateElements(stateAttributes, "jbox", doc);
			stateInfo.put(entry.getKey(), stateElements);
		}

		return stateInfo;
	}
}
