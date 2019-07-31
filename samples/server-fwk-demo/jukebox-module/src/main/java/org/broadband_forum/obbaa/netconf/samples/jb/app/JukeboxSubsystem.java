package org.broadband_forum.obbaa.netconf.samples.jb.app;

import static org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorSeverity.Error;
import static org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorTag.OPERATION_FAILED;
import static org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcErrorType.RPC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.broadband_forum.obbaa.netconf.api.messages.NetconfRpcError;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.AbstractSubSystem;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.FilterNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.GetAttributeException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemValidationException;
import org.broadband_forum.obbaa.netconf.samples.jb.api.JBConstants;
import org.broadband_forum.obbaa.netconf.samples.jb.persistence.ArtistDao;
import org.broadband_forum.obbaa.netconf.samples.jb.persistence.entities.Artist;
import org.broadband_forum.obbaa.netconf.samples.jb.persistence.entities.ArtistPK;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class JukeboxSubsystem extends AbstractSubSystem {
    private static final ModelNodeId LIBRARY_TEMPLATE = new ModelNodeId("/container=jukebox/container=library", JBConstants.JB_NS);
    private final ArtistDao m_dao;
    private static final AdvancedLogger LOGGER = LoggerFactory.getLogger(JukeboxSubsystem.class, "jukebox", "CUSTOMER", "GLOBAL");

    public JukeboxSubsystem(ArtistDao dao) {
        m_dao = dao;
    }

    @Override
    public void notifyPreCommitChange(List<ChangeNotification> changeNotificationList) throws SubSystemValidationException {
        //A random veto !, you cannot add an artist named Blah, no logic here, but it explains the concept
        Artist forbiddenArtist = m_dao.findById(new ArtistPK("/container=jukebox/container=library", "Blah"));
        if (forbiddenArtist != null) {
            throw new SubSystemValidationException(new NetconfRpcError(OPERATION_FAILED, RPC, Error, "Cannot create an artist named Blah"));
        }
        super.notifyPreCommitChange(changeNotificationList);
    }

    @Override
    public void notifyChanged(List<ChangeNotification> changeNotificationList) {
        LOGGER.info("Subsystem notified of changes: {}", changeNotificationList);
        super.notifyChanged(changeNotificationList);
    }

    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = {RuntimeException.class, Exception.class})
    @Override
    protected Map<ModelNodeId, List<Element>> retrieveStateAttributes(Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes) throws GetAttributeException {
        LOGGER.info("Subsystem being queried for state attributes: {}", attributes);
        Map<ModelNodeId, List<Element>> returnValues = new HashMap<>();
        for (Map.Entry<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attrEntry : attributes.entrySet()) {
            if (attrEntry.getKey().beginsWithTemplate(LIBRARY_TEMPLATE)) {
                try {
                    Document document = DocumentUtils.getNewDocument();
                    List<Element> stateValues = new ArrayList<>();
                    returnValues.put(attrEntry.getKey(), stateValues);
                    List<Artist> artists = m_dao.findAll();
                    for (QName stateAttr : attrEntry.getValue().getFirst()) {
                        if (stateAttr.getLocalName().equals("artist-count")) {
                            Element artistCount = document.createElementNS(JBConstants.JB_NS, "artist-count");
                            stateValues.add(artistCount);
                            artistCount.setTextContent(String.valueOf(artists.size()));
                        } else if (stateAttr.getLocalName().equals("album-count")) {
                            Element albumCount = document.createElementNS(JBConstants.JB_NS, "album-count");
                            stateValues.add(albumCount);
                            albumCount.setTextContent(String.valueOf(countAlbums(artists)));
                        } else if (stateAttr.getLocalName().equals("song-count")) {
                            Element songCount = document.createElementNS(JBConstants.JB_NS, "song-count");
                            stateValues.add(songCount);
                            songCount.setTextContent(String.valueOf(countSongs(artists)));
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return returnValues;
    }

    private Long countSongs(List<Artist> artists) {
        long totalAlbums = 0;
        for (Artist artis : artists) {
            Document doc = artis.getXmSubtreeDoc();
            NodeList albums = doc.getElementsByTagName("song");
            totalAlbums += albums.getLength();
        }
        return totalAlbums;
    }

    private Long countAlbums(List<Artist> artists) {
        long totalAlbums = 0;
        for (Artist artis : artists) {
            Document doc = artis.getXmSubtreeDoc();
            NodeList albums = doc.getElementsByTagName("album");
            totalAlbums += albums.getLength();
        }
        return totalAlbums;
    }
}

