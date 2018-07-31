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


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2.Jukebox;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;

/**
 * Created by keshava on 7/7/15.
 */
public class SubsystemNotificationExecutorTest {
    private static final String JUKBOX_NS = "http://example.com/ns/example-jukebox";
    private static final String PMA_NS = "urn:org:bbf:pma";
    SubsystemNotificationExecutor m_notificationExecutor = new SubsystemNotificationExecutor();
    private ModelNodeId m_jukeboxNodeId;
    private ModelNodeId m_libraryNodeId;
    private ModelNodeId m_artistNodeId;
    private ModelNodeId m_album1NodeId;
    private ModelNodeId m_album2NodeId;

    private ModelNode m_jukeBoxNode;
    private ModelNode m_libraryNode;
    private ModelNode m_artistNode;
    private ModelNode m_album1Node;
    private ModelNode m_album2Node;

    private ModelNode m_swmgmtNode;
    private ModelNode m_swverOverrideNode;
    private ModelNodeId m_swmgmtNodeId;
    private ModelNodeId m_swVerOverrideId;
    private SchemaRegistry m_schemaRegistry;
    private EditConfigRequest m_request;

    @Before
    public void setUp() {
        m_jukeboxNodeId = new ModelNodeId("/container=pma/container=device-holder/name=OLT1/device-id=device1" +
                "/container=jukebox", Jukebox.NAMESPACE);
        m_libraryNodeId = new ModelNodeId("/container=pma/container=device-holder/name=OLT1/device-id=device1" +
                "/container=jukebox/container=library", Jukebox.NAMESPACE);
        m_artistNodeId = new ModelNodeId("/container=pma/container=device-holder/name=OLT1/device-id=device1" +
                "/container=jukebox/container=library/container=artist/name=Lenny", Jukebox.NAMESPACE);
        m_album1NodeId = new ModelNodeId("/container=pma/container=device-holder/name=OLT1/device-id=device1" +
                "/container=jukebox/container=library/container=artist/name=Lenny/container=album/name=Greatest " +
                "Hits", Jukebox.NAMESPACE);
        m_album2NodeId = new ModelNodeId("/container=pma/container=device-holder/name=OLT1/device-id=device1" +
                "/container=jukebox/container=library/container=artist/name=Lenny/container=album/name=Strut",
                Jukebox.NAMESPACE);

        m_jukeBoxNode = mock(ModelNode.class);
        when(m_jukeBoxNode.getModelNodeId()).thenReturn(m_jukeboxNodeId);
        m_libraryNode = mock(ModelNode.class);
        when(m_libraryNode.getModelNodeId()).thenReturn(m_libraryNodeId);
        m_artistNode = mock(ModelNode.class);
        when(m_artistNode.getModelNodeId()).thenReturn(m_artistNodeId);
        m_album1Node = mock(ModelNode.class);
        when(m_album1Node.getModelNodeId()).thenReturn(m_album1NodeId);
        m_album2Node = mock(ModelNode.class);
        when(m_album2Node.getModelNodeId()).thenReturn(m_album2NodeId);

        m_swmgmtNodeId = new ModelNodeId("/container=device-manager/container=pma-swmgmt", PMA_NS);
        m_swVerOverrideId = new ModelNodeId("/container=device-manager/container=pma-swmgmt/container=pma-swver" +
                "-override", PMA_NS);
        m_swmgmtNode = mock(ModelNode.class);
        m_swverOverrideNode = mock(ModelNode.class);
        when(m_swmgmtNode.getModelNodeId()).thenReturn(m_swmgmtNodeId);
        when(m_swverOverrideNode.getModelNodeId()).thenReturn(m_swVerOverrideId);
        m_schemaRegistry = mock(SchemaRegistry.class);
        when(m_schemaRegistry.getPrefix(JUKBOX_NS)).thenReturn("jukbox");
        m_request = new EditConfigRequest();
    }

    /**
     * We are trying to simulate the following edit-config request and making sure only one notification is sent to
     * single sub system.
     * {@code
     * <edit-config>
     * <target>
     * <running/>
     * </target>
     * <test-option>set</test-option>
     * <config>
     * <pma xmlns="urn:org:bbf:pma">
     * <device-holder>
     * <name>OLT1</name>
     * <device>
     * <device-id>device1</device-id>
     * <jukebox xmlns="http://example.com/ns/example-jukebox">
     * <library operation="replace">
     * <artist>
     * <name>Lenny</name>
     * </artist>
     * </library>
     * </jukebox>
     * </device>
     * </device-holder>
     * </pma>
     * </config>
     * </edit-config>
     * }
     * So here assuming that the nodes jukebox/library belong to a single subsystem X, we need to send a single
     * notificiation to subsystem X.
     */
    @Test
    public void testNoDuplicateNotificationsAreSent() {
        List<NotificationInfo> listWithDuplicates = new ArrayList<>();

        TestSubsystem subssytemX = new TestSubsystem();
        when(m_jukeBoxNode.getSubSystem()).thenReturn(subssytemX);
        when(m_libraryNode.getSubSystem()).thenReturn(subssytemX);

        EditMatchNode artistMatchNode = new EditMatchNode(QName.create(JUKBOX_NS, "name"), new GenericConfigAttribute
                ("Lenny"));
        EditContainmentNode replaceArtist = new EditContainmentNode(QName.create(JUKBOX_NS, "artist"), "replace")
                .addMatchNode(artistMatchNode);
        EditContainmentNode replaceLib = new EditContainmentNode(QName.create(JUKBOX_NS, "library"), "replace")
                .addChild(replaceArtist);

        ModelNodeChange jukeBoxNodeChange = new ModelNodeChange(ModelNodeChangeType.replace, replaceLib);
        NotificationInfo jukeboxInfo = new NotificationInfo().addNotificationInfo(m_jukeBoxNode, jukeBoxNodeChange,
                null);

        ModelNodeChange libraryNodeChange = new ModelNodeChange(ModelNodeChangeType.replace, replaceArtist);
        NotificationInfo libraryInfo = new NotificationInfo().addNotificationInfo(m_libraryNode, libraryNodeChange,
                null);

        listWithDuplicates.add(jukeboxInfo);
        listWithDuplicates.add(libraryInfo);

        Map<SubSystem, List<ChangeNotification>> subSystemNotificationMap = m_notificationExecutor
                .getSubSystemNotificationMap(StandardDataStores.RUNNING, listWithDuplicates, m_request);
        m_notificationExecutor.sendNotifications(subSystemNotificationMap);

        assertEquals(1, subssytemX.getLastNotifList().size());
        EditConfigChangeNotification editConfigChangeNotification = (EditConfigChangeNotification) subssytemX
                .getLastNotifList().get(0);
        assertEquals(jukeboxInfo.getChange(), editConfigChangeNotification.getChange());
        assertEquals(StandardDataStores.RUNNING, editConfigChangeNotification.getDataStore());
    }

    /**
     * We are trying to simulate the following edit-config request and making sure only one notification is sent to
     * single sub system.
     * {@code
     * <edit-config>
     * <target>
     * <running/>
     * </target>
     * <test-option>set</test-option>
     * <config>
     * <pma xmlns="urn:org:bbf:pma">
     * <device-holder>
     * <name>OLT1</name>
     * <device>
     * <device-id>device1</device-id>
     * <jukebox xmlns="http://example.com/ns/example-jukebox">
     * <library operation="replace">
     * <artist>
     * <name>Lenny</name>
     * </artist>
     * </library>
     * </jukebox>
     * </device>
     * </device-holder>
     * </pma>
     * </config>
     * </edit-config>
     * }
     * So here assuming that the nodes jukebox belongs to a subsystem X and library belongs to subsystem Y,
     * we need to send a single notification to subsystem X and one more to subsystem Y.
     */
    @Test
    public void testNoNotificationsAreMissed() {
        List<NotificationInfo> listWithDuplicates = new ArrayList<>();

        TestSubsystem subssytemX = new TestSubsystem();
        when(m_jukeBoxNode.getSubSystem()).thenReturn(subssytemX);
        TestSubsystem subssytemY = new TestSubsystem();
        when(m_libraryNode.getSubSystem()).thenReturn(subssytemY);

        EditMatchNode artistMatchNode = new EditMatchNode(QName.create(JUKBOX_NS, "name"), new GenericConfigAttribute
                ("Lenny"));
        EditContainmentNode replaceArtist = new EditContainmentNode(QName.create(JUKBOX_NS, "artist"), "replace")
                .addMatchNode(artistMatchNode);
        EditContainmentNode replaceLib = new EditContainmentNode(QName.create(JUKBOX_NS, "library"), "replace")
                .addChild(replaceArtist);

        ModelNodeChange jukeBoxNodeChange = new ModelNodeChange(ModelNodeChangeType.replace, replaceLib);
        NotificationInfo jukeboxInfo = new NotificationInfo().addNotificationInfo(m_jukeBoxNode, jukeBoxNodeChange,
                null);

        ModelNodeChange libraryNodeChange = new ModelNodeChange(ModelNodeChangeType.replace, replaceArtist);
        NotificationInfo libraryInfo = new NotificationInfo().addNotificationInfo(m_libraryNode, libraryNodeChange,
                null);

        listWithDuplicates.add(jukeboxInfo);
        listWithDuplicates.add(libraryInfo);

        Map<SubSystem, List<ChangeNotification>> subSystemNotificationMap = m_notificationExecutor
                .getSubSystemNotificationMap(StandardDataStores.RUNNING, listWithDuplicates, m_request);
        m_notificationExecutor.sendNotifications(subSystemNotificationMap);

        assertEquals(1, subssytemX.getLastNotifList().size());
        EditConfigChangeNotification editConfigChangeNotification = (EditConfigChangeNotification) subssytemX
                .getLastNotifList().get(0);
        assertEquals(jukeboxInfo.getChange(), editConfigChangeNotification.getChange());
        assertEquals(StandardDataStores.RUNNING, editConfigChangeNotification.getDataStore());

        assertEquals(1, subssytemY.getLastNotifList().size());
        editConfigChangeNotification = (EditConfigChangeNotification) subssytemY.getLastNotifList().get(0);
        assertEquals(libraryInfo.getChange(), editConfigChangeNotification.getChange());
        assertEquals(StandardDataStores.RUNNING, editConfigChangeNotification.getDataStore());

    }

    /**
     * We are trying to simulate the following edit-config request and making sure only one notification is sent to
     * single sub system.
     * {@code
     * <edit-config>
     * <target>
     * <running/>
     * </target>
     * <test-option>set</test-option>
     * <config>
     * <pma xmlns="urn:org:bbf:pma">
     * <device-holder>
     * <name>OLT1</name>
     * <device>
     * <device-id>device1</device-id>
     * <jukebox xmlns="http://example.com/ns/example-jukebox">
     * <library>
     * <artist>
     * <name>Lenny</name>
     * <album operation="create">
     * <name>Greatest hits</name>
     * <song>
     * <name>Are you gonne go my way</name>
     * </song>
     * </album>
     * <album operation="create">
     * <name>Strut</name>
     * </album>
     * </artist>
     * </library>
     * </jukebox>
     * </device>
     * </device-holder>
     * </pma>
     * </config>
     * </edit-config>
     * }
     * So here assuming that the album belongs to subsystem Z, it should receive 2 notifications. One for the create
     * album1
     * and another for create album2
     */
    @Test
    public void testNoNotificationsAreMissedSiblingChanges() {
        List<NotificationInfo> notifList = new ArrayList<>();

        TestSubsystem subssytemX = new TestSubsystem();
        when(m_artistNode.getSubSystem()).thenReturn(subssytemX);

        EditMatchNode album1MatchNode = new EditMatchNode(QName.create(JUKBOX_NS, "name"), new GenericConfigAttribute
                ("Strut"));
        EditContainmentNode createAlbum1 = new EditContainmentNode(QName.create(JUKBOX_NS, "album"), "create")
                .addMatchNode(album1MatchNode);


        EditMatchNode songMatchNode = new EditMatchNode(QName.create(JUKBOX_NS, "name"), new GenericConfigAttribute
                ("Are you gonne go my " +
                "way"));
        EditContainmentNode createSong = new EditContainmentNode(QName.create(JUKBOX_NS, "song"), "create")
                .addMatchNode(songMatchNode);
        EditMatchNode album2MatchNode = new EditMatchNode(QName.create(JUKBOX_NS, "name"), new GenericConfigAttribute
                ("Greatest Hits"));
        EditContainmentNode createAlbum2 = new EditContainmentNode(QName.create(JUKBOX_NS, "album"), "create")
                .addMatchNode(album2MatchNode).addChild(createSong);


        ModelNodeChange createAlbum1Change = new ModelNodeChange(ModelNodeChangeType.create, createAlbum1);
        NotificationInfo createAlbum1Info = new NotificationInfo().addNotificationInfo(m_artistNode,
                createAlbum1Change, null);

        ModelNodeChange createAlbum2Change = new ModelNodeChange(ModelNodeChangeType.create, createAlbum2);
        NotificationInfo createAlbum2Info = new NotificationInfo().addNotificationInfo(m_artistNode,
                createAlbum2Change, null);

        ModelNodeChange createSongChange = new ModelNodeChange(ModelNodeChangeType.create, createSong);
        NotificationInfo createSongInfo = new NotificationInfo().addNotificationInfo(m_album2Node, createSongChange,
                null);

        notifList.add(createAlbum1Info);
        notifList.add(createAlbum2Info);
        notifList.add(createSongInfo);


        Map<SubSystem, List<ChangeNotification>> subSystemNotificationMap = m_notificationExecutor
                .getSubSystemNotificationMap(StandardDataStores.RUNNING, notifList, m_request);
        m_notificationExecutor.sendNotifications(subSystemNotificationMap);

        assertEquals(2, subssytemX.getLastNotifList().size());
        EditConfigChangeNotification editConfigChangeNotification = (EditConfigChangeNotification) subssytemX
                .getLastNotifList().get(0);
        assertEquals(createAlbum1Info.getChange(), editConfigChangeNotification.getChange());
        assertEquals(StandardDataStores.RUNNING, editConfigChangeNotification.getDataStore());

        editConfigChangeNotification = (EditConfigChangeNotification) subssytemX.getLastNotifList().get(1);
        assertEquals(createAlbum2Info.getChange(), editConfigChangeNotification.getChange());
        assertEquals(StandardDataStores.RUNNING, editConfigChangeNotification.getDataStore());


    }

    @Test
    public void testNotificationIsSentToChidSussystem() {

        TestSubsystem jukeboxSubsystem = new TestSubsystem();
        when(m_jukeBoxNode.getSubSystem()).thenReturn(jukeboxSubsystem);
        TestSubsystem librarySubsystem = new TestSubsystem();
        SchemaPath jukeboxSchemaPath = new SchemaPathBuilder().withNamespace(JUKBOX_NS)
                .appendLocalName("jukebox").build();
        when(m_jukeBoxNode.getModelNodeSchemaPath()).thenReturn(jukeboxSchemaPath);
        SchemaPath librarySchemaPath = new SchemaPathBuilder().withNamespace(JUKBOX_NS)
                .appendLocalName("jukebox").appendLocalName("library").build();
        when(m_jukeBoxNode.getSubSystem(librarySchemaPath)).thenReturn(librarySubsystem);

        List<NotificationInfo> deletingNotifList = new ArrayList<>();
        EditContainmentNode deletingLibrary = new EditContainmentNode(QName.create(JUKBOX_NS, "library"), "delete");
        ModelNodeChange jukeBoxNodeChange = new ModelNodeChange(ModelNodeChangeType.delete, deletingLibrary);
        NotificationInfo deletingJukeBoxInfo = new NotificationInfo().addNotificationInfo(m_jukeBoxNode,
                jukeBoxNodeChange, null);
        deletingNotifList.add(deletingJukeBoxInfo);

        Map<SubSystem, List<ChangeNotification>> subSystemNotificationMap = m_notificationExecutor
                .getSubSystemNotificationMap(StandardDataStores.RUNNING, deletingNotifList, m_request);
        m_notificationExecutor.sendNotifications(subSystemNotificationMap);
        //child subsystem is also sent the notification
        assertEquals(1, librarySubsystem.getLastNotifList().size());
    }


    /**
     * We are trying to simulate the following edit-config request.
     * {@code
     * <edit-config>
     * <target>
     * <running/>
     * </target>
     * <test-option>set</test-option>
     * <config>
     * <pma xmlns="urn:org:bbf:pma" xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0">
     * <pma-swmgmt>
     * <pma-swver-override>
     * <pma-dpu-sw-version-override xc:operation="replace">
     * <dpu-id>OLT-1.R1.S1.LT1.P1.ONT4</dpu-id>
     * <planned-sw-version-url>ftp://swversionurl/demo2/SW002</planned-sw-version-url>
     * <download-sw-version-url>ftp://swversionurl/demo2/SW002</download-sw-version-url>
     * <delayed-activate>false</delayed-activate>
     * </pma-dpu-sw-version-override>
     * </pma-swver-override>
     * <pma-dpu-sw-control xc:operation="replace">
     * <max-num-devices>10</max-num-devices>
     * <time-interval>1</time-interval>
     * </pma-dpu-sw-control>
     * </pma-swmgmt>
     * </pma>
     * </config>
     * </edit-config>
     * }
     * So here assuming that the nodes swver-override/dpu-sw-control belong to a single subsystem X, we need to send
     * a 2 notificiation to subsystem X.
     */
    @Test
    public void testParentNotifNotContainDataOfChildNotif() {
        List<NotificationInfo> notifInfos = new ArrayList<>();

        TestSubsystem subssytemX = new TestSubsystem();
        when(m_swmgmtNode.getSubSystem()).thenReturn(subssytemX);
        when(m_swverOverrideNode.getSubSystem()).thenReturn(subssytemX);

        EditChangeNode changeNode1 = new EditChangeNode(QName.create(PMA_NS, "max-num-devices"), new
                GenericConfigAttribute("10"));
        EditChangeNode changeNode2 = new EditChangeNode(QName.create(PMA_NS, "time-interval"), new
                GenericConfigAttribute("2"));

        EditContainmentNode replaceDpuSwControl = new EditContainmentNode(QName.create("urn:org:bbf:pma",
                "pma-dpu-sw-control"), "replace");
        replaceDpuSwControl.addChangeNode(changeNode1);
        replaceDpuSwControl.addChangeNode(changeNode2);

        EditChangeNode changeNodeOverride1 = new EditChangeNode(QName.create(PMA_NS, "planned-sw-version-url"),
                new GenericConfigAttribute("ftp://swversionurl/demo2/SW002"));
        EditChangeNode changeNodeOverride2 = new EditChangeNode(QName.create(PMA_NS, "download-sw-version-url"),
                new GenericConfigAttribute("ftp://swversionurl/demo2/SW002"));

        EditContainmentNode replaceSwOverrideControl = new EditContainmentNode(QName.create(PMA_NS,
                "pma-dpu-sw-control"), "replace");
        replaceSwOverrideControl.addChangeNode(changeNodeOverride1);
        replaceSwOverrideControl.addChangeNode(changeNodeOverride2);
        replaceSwOverrideControl.addMatchNode(new EditMatchNode(QName.create(PMA_NS, "dpu-id"), new
                GenericConfigAttribute("OLT-1.R1.S1" +
                ".LT1.P1.ONT4")));


        ModelNodeChange swmgmtNodeChange = new ModelNodeChange(ModelNodeChangeType.replace, replaceDpuSwControl);
        NotificationInfo swmgmtInfo = new NotificationInfo().addNotificationInfo(m_swmgmtNode, swmgmtNodeChange, null);

        ModelNodeChange dpuSwVersionOveride = new ModelNodeChange(ModelNodeChangeType.replace,
                replaceSwOverrideControl);
        NotificationInfo swVersionOverrideInfo = new NotificationInfo().addNotificationInfo(m_swverOverrideNode,
                dpuSwVersionOveride, null);

        notifInfos.add(swmgmtInfo);
        notifInfos.add(swVersionOverrideInfo);

        Map<SubSystem, List<ChangeNotification>> subSystemNotificationMap = m_notificationExecutor
                .getSubSystemNotificationMap(StandardDataStores.RUNNING, notifInfos, m_request);
        m_notificationExecutor.sendNotifications(subSystemNotificationMap);

        assertEquals(2, subssytemX.getLastNotifList().size());

        EditConfigChangeNotification editConfigChangeNotif1 = (EditConfigChangeNotification) subssytemX
                .getLastNotifList().get(0);
        EditConfigChangeNotification editConfigChangeNotif2 = (EditConfigChangeNotification) subssytemX
                .getLastNotifList().get(1);

        assertEquals(swmgmtInfo.getChange(), editConfigChangeNotif1.getChange());
        assertEquals(swVersionOverrideInfo.getChange(), editConfigChangeNotif2.getChange());

        assertEquals(StandardDataStores.RUNNING, editConfigChangeNotif1.getDataStore());
    }

    @Test
    public void testRefineNetconfConfigChangeNotification() {
        NetconfConfigChangeNotification netconfConfigChangeNotification = new NetconfConfigChangeNotification();
        EditInfo editInfo = new EditInfo();
        editInfo.setTarget("/prefix1:container1/prefix2:container2");
        netconfConfigChangeNotification.setEditList(editInfo);
        m_notificationExecutor.refineNetconfConfigChangeNotification(netconfConfigChangeNotification);

        assertEquals("replace", netconfConfigChangeNotification.getEditList().get(0).getOperation());
    }

    private class TestSubsystem extends AbstractSubSystem {
        private List<ChangeNotification> m_lastNotifList;

        public List<ChangeNotification> getLastNotifList() {
            return m_lastNotifList;
        }

        @Override
        public Map<ModelNodeId, List<Element>> retrieveStateAttributes(
                Map<ModelNodeId, Pair<List<QName>, List<FilterNode>>> attributes) throws GetAttributeException {
            return null;
        }

        @Override
        public void notifyChanged(List<ChangeNotification> changeNotificationList) {
            m_lastNotifList = changeNotificationList;
        }

    }


}
