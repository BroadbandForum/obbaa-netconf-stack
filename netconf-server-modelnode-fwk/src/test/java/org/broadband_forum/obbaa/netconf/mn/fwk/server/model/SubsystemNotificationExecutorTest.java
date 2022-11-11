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


import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubsystemNotificationExecutor.CONTEXT_MAP_CLIENT_INFO_KEY;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubsystemNotificationExecutor.CONTEXT_MAP_UPLOAD_REQUEST_KEY;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.ARTIST_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JB_NS;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.JUKEBOX_SCHEMA_PATH;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_QNAME;
import static org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants.LIBRARY_SCHEMA_PATH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.broadband_forum.obbaa.netconf.api.client.NetconfClientInfo;
import org.broadband_forum.obbaa.netconf.api.messages.EditConfigRequest;
import org.broadband_forum.obbaa.netconf.api.messages.EditInfo;
import org.broadband_forum.obbaa.netconf.api.messages.NetconfConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.api.util.NetconfResources;
import org.broadband_forum.obbaa.netconf.api.util.Pair;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.GenericConfigAttribute;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Element;

/**
 * Created by keshava on 7/7/15.
 */
@RunWith(RequestScopeJunitRunner.class)
public class SubsystemNotificationExecutorTest {
    private static final String JUKBOX_NS = "http://example.com/ns/example-jukebox";
    private static final String PMA_NS = "urn:org:bbf2:pma";
    public static final String NAME = "name";
    public static final String MAX_NUM_DEVICES = "max-num-devices";
    public static final String TIME_INTERVAL = "time-interval";
    public static final String PLANNED_SW_VERSION_URL = "planned-sw-version-url";
    public static final String DOWNLOAD_SW_VERSION_URL = "download-sw-version-url";
    SubsystemNotificationExecutor m_notificationExecutor;
    public NetconfClientInfo m_netconfClientInfo;
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
    private WritableChangeTreeNode m_aggregatorCTN;
    private SubSystemRegistry m_subSystemRegistry;

    @Before
    public void setUp(){
    	m_jukeboxNodeId = new ModelNodeId("/container=pma/container=device-holder/name=OLT1/device-id=device1/container=jukebox", JB_NS);
        m_libraryNodeId = new ModelNodeId("/container=pma/container=device-holder/name=OLT1/device-id=device1/container=jukebox/container=library", JB_NS);
        m_artistNodeId = new ModelNodeId("/container=pma/container=device-holder/name=OLT1/device-id=device1/container=jukebox/container=library/container=artist/name=Lenny", JB_NS);
        m_album1NodeId = new ModelNodeId("/container=pma/container=device-holder/name=OLT1/device-id=device1/container=jukebox/container=library/container=artist/name=Lenny/container=album/name=Greatest Hits", JB_NS);
        m_album2NodeId = new ModelNodeId("/container=pma/container=device-holder/name=OLT1/device-id=device1/container=jukebox/container=library/container=artist/name=Lenny/container=album/name=Strut", JB_NS);

        m_jukeBoxNode = mock(ModelNode.class);
        when(m_jukeBoxNode.getModelNodeId()).thenReturn(m_jukeboxNodeId);
        m_libraryNode = mock(ModelNode.class);
        when(m_libraryNode.getModelNodeId()).thenReturn(m_libraryNodeId);
        m_artistNode = mock(ModelNode.class);
        when(m_artistNode.getModelNodeId()).thenReturn(m_artistNodeId);
        m_album1Node = mock(ModelNode.class);
        when(m_album1Node.getModelNodeId()).thenReturn(m_album1NodeId);
        m_album2Node= mock(ModelNode.class);
        when(m_album2Node.getModelNodeId()).thenReturn(m_album2NodeId);

    	m_swmgmtNodeId = new ModelNodeId("/container=device-manager/container=pma-swmgmt", PMA_NS);
    	m_swVerOverrideId = new ModelNodeId("/container=device-manager/container=pma-swmgmt/container=pma-swver-override", PMA_NS);
    	m_swmgmtNode = mock(ModelNode.class);
    	m_swverOverrideNode = mock(ModelNode.class);
    	when(m_swmgmtNode.getModelNodeId()).thenReturn(m_swmgmtNodeId);
    	when(m_swverOverrideNode.getModelNodeId()).thenReturn(m_swVerOverrideId);
    	m_schemaRegistry = mock(SchemaRegistry.class);
    	when(m_schemaRegistry.getPrefix(JUKBOX_NS)).thenReturn("jukbox");
        m_request = new EditConfigRequest();
        m_netconfClientInfo = new NetconfClientInfo("test-client",1);
        m_notificationExecutor = new SubsystemNotificationExecutor(new SubSystemRegistryImpl());
    }

    /**
     * We are trying to simulate the following edit-config request and making sure only one notification is sent to single sub system.
     * {@code
     * <edit-config>
     * 	<target>
     * 		<running/>
     * 	</target>
     * 	<test-option>set</test-option>
     * 	<config>
     * 		<pma xmlns="urn:org:bbf2:pma">
     * 			<device-holder>
     * 				<name>OLT1</name>
     * 				<device>
     * 					<device-id>device1</device-id>
     * 					<jukebox xmlns="http://example.com/ns/example-jukebox">
     * 						<library operation="replace">
     * 							<artist>
     * 								<name>Lenny</name>
     * 							</artist>
     * 						</library>
     * 					</jukebox>
     * 				</device>
     * 			</device-holder>
     * 		</pma>
     * 	</config>
     * </edit-config>
     * }
     * So here assuming that the nodes jukebox/library belong to a single subsystem X, we need to send a single notificiation to subsystem X.
     */
    @Test
    public void testNoDuplicateNotificationsAreSent() {
        List<NotificationInfo> listWithDuplicates = new ArrayList<>();

        TestSubsystem subssytemX = new TestSubsystem();
        when(m_jukeBoxNode.getSubSystem()).thenReturn(subssytemX);
        when(m_libraryNode.getSubSystem()).thenReturn(subssytemX);

        EditMatchNode artistMatchNode = new EditMatchNode(QName.create(JUKBOX_NS, NAME), new GenericConfigAttribute(NAME, JUKBOX_NS, "Lenny"));
        EditContainmentNode replaceArtist = new TestEditContainmentNode(QName.create(JUKBOX_NS, "artist"), "replace", m_schemaRegistry).addMatchNode(artistMatchNode);
        EditContainmentNode replaceLib = new TestEditContainmentNode(QName.create(JUKBOX_NS, "library"), "replace", m_schemaRegistry).addChild(replaceArtist);

        ModelNodeChange jukeBoxNodeChange = new ModelNodeChange(ModelNodeChangeType.replace, replaceLib);
        NotificationInfo jukeboxInfo = new NotificationInfo().addNotificationInfo(m_jukeBoxNode, jukeBoxNodeChange, null);

        ModelNodeChange libraryNodeChange = new ModelNodeChange(ModelNodeChangeType.replace, replaceArtist);
        NotificationInfo libraryInfo = new NotificationInfo().addNotificationInfo(m_libraryNode, libraryNodeChange, null);

        listWithDuplicates.add(jukeboxInfo);
        listWithDuplicates.add(libraryInfo);

        Map<SubSystem, IndexedNotifList> subSystemNotificationMap = m_notificationExecutor
        		.getSubSystemNotificationMap(StandardDataStores.RUNNING, listWithDuplicates, m_request);
        m_notificationExecutor.sendNotifications(subSystemNotificationMap);

        assertEquals(1, subssytemX.getLastNotifList().size());
        EditConfigChangeNotification editConfigChangeNotification = (EditConfigChangeNotification) subssytemX.getLastNotifList().get(0);
        assertEquals(jukeboxInfo.getChange(), editConfigChangeNotification.getChange());
        assertEquals(StandardDataStores.RUNNING, editConfigChangeNotification.getDataStore());
    }

    /**
     * We are trying to simulate the following edit-config request and making sure only one notification is sent to single sub system.
     * {@code
     * <edit-config>
     * 	<target>
     * 		<running/>
     * 	</target>
     * 	<test-option>set</test-option>
     * 	<config>
     * 		<pma xmlns="urn:org:bbf2:pma">
     * 			<device-holder>
     * 				<name>OLT1</name>
     * 				<device>
     * 					<device-id>device1</device-id>
     * 					<jukebox xmlns="http://example.com/ns/example-jukebox">
     * 						<library operation="replace">
     * 							<artist>
     * 								<name>Lenny</name>
     * 							</artist>
     * 						</library>
     * 					</jukebox>
     * 				</device>
     * 			</device-holder>
     * 		</pma>
     * 	</config>
     * </edit-config>
     * }
     * So here assuming that the nodes jukebox belongs to a subsystem X and library belongs to subsystem Y,
     * we need to send a single notification to subsystem X and one more to subsystem Y.
     */
    @Test
    public void testNoNotificationsAreMissed(){
        List<NotificationInfo> listWithDuplicates = new ArrayList<>();

        TestSubsystem subssytemX = new TestSubsystem();
        when(m_jukeBoxNode.getSubSystem()).thenReturn(subssytemX);
        TestSubsystem subssytemY = new TestSubsystem();
        when(m_libraryNode.getSubSystem()).thenReturn(subssytemY);

        EditMatchNode artistMatchNode = new EditMatchNode(QName.create(JUKBOX_NS, NAME),new GenericConfigAttribute(NAME, JUKBOX_NS, "Lenny"));
        EditContainmentNode replaceArtist = new TestEditContainmentNode(QName.create(JUKBOX_NS, "artist"), "replace", m_schemaRegistry).addMatchNode(artistMatchNode);
        EditContainmentNode replaceLib = new TestEditContainmentNode(QName.create(JUKBOX_NS, "library"), "replace", m_schemaRegistry).addChild(replaceArtist);

        ModelNodeChange jukeBoxNodeChange = new ModelNodeChange(ModelNodeChangeType.replace, replaceLib);
        NotificationInfo jukeboxInfo = new NotificationInfo().addNotificationInfo(m_jukeBoxNode, jukeBoxNodeChange, null);

        ModelNodeChange libraryNodeChange = new ModelNodeChange(ModelNodeChangeType.replace, replaceArtist);
        NotificationInfo libraryInfo = new NotificationInfo().addNotificationInfo(m_libraryNode, libraryNodeChange, null);

        listWithDuplicates.add(jukeboxInfo);
        listWithDuplicates.add(libraryInfo);

        Map<SubSystem, IndexedNotifList> subSystemNotificationMap = m_notificationExecutor
        		.getSubSystemNotificationMap(StandardDataStores.RUNNING, listWithDuplicates, m_request);
        m_notificationExecutor.sendNotifications(subSystemNotificationMap);

        assertEquals(1, subssytemX.getLastNotifList().size());
        EditConfigChangeNotification editConfigChangeNotification = (EditConfigChangeNotification) subssytemX.getLastNotifList().get(0);
        assertEquals(jukeboxInfo.getChange(), editConfigChangeNotification.getChange());
        assertEquals(StandardDataStores.RUNNING, editConfigChangeNotification.getDataStore());

        assertEquals(1, subssytemY.getLastNotifList().size());
        editConfigChangeNotification = (EditConfigChangeNotification) subssytemY.getLastNotifList().get(0);
        assertEquals(libraryInfo.getChange(), editConfigChangeNotification.getChange());
        assertEquals(StandardDataStores.RUNNING, editConfigChangeNotification.getDataStore());

    }

    /**
     * We are trying to simulate the following edit-config request and making sure only one notification is sent to single sub system.
     * {@code
     * <edit-config>
     * 	<target>
     * 		<running/>
     * 	</target>
     * 	<test-option>set</test-option>
     * 	<config>
     * 		<pma xmlns="urn:org:bbf2:pma">
     * 			<device-holder>
     * 				<name>OLT1</name>
     * 				<device>
     * 					<device-id>device1</device-id>
     * 					<jukebox xmlns="http://example.com/ns/example-jukebox">
     * 						<library>
     * 							<artist>
     * 								<name>Lenny</name>
     * 								<album operation="create">
     * 									<name>Greatest hits</name>
     * 									<song>
     * 										<name>Are you gonne go my way</name>
     * 									</song>
     * 								</album>
     * 								<album operation="create">
     * 									<name>Strut</name>
     * 								</album>
     * 							</artist>
     * 						</library>
     * 					</jukebox>
     * 				</device>
     * 			</device-holder>
     * 		</pma>
     * 	</config>
     * </edit-config>
     * }
     * So here assuming that the album belongs to subsystem Z, it should receive 2 notifications. One for the create album1
     * and another for create album2
     */
    @Test
    public void testNoNotificationsAreMissedSiblingChanges(){
        List<NotificationInfo> notifList = new ArrayList<>();

        TestSubsystem subssytemX = new TestSubsystem();
        when(m_artistNode.getSubSystem()).thenReturn(subssytemX);

        EditMatchNode album1MatchNode = new EditMatchNode(QName.create(JUKBOX_NS, NAME), new GenericConfigAttribute(NAME, JUKBOX_NS, "Strut"));
        EditContainmentNode createAlbum1 = new TestEditContainmentNode(QName.create(JUKBOX_NS, "album"), "create", m_schemaRegistry).addMatchNode(album1MatchNode);


        EditMatchNode songMatchNode = new EditMatchNode(QName.create(JUKBOX_NS, NAME), new GenericConfigAttribute(NAME, JUKBOX_NS, "Are you gonne go my " +
                "way"));
        EditContainmentNode createSong = new TestEditContainmentNode(QName.create(JUKBOX_NS, "song"), "create", m_schemaRegistry).addMatchNode(songMatchNode);
        EditMatchNode album2MatchNode = new EditMatchNode(QName.create(JUKBOX_NS, NAME), new GenericConfigAttribute(NAME, JUKBOX_NS, "Greatest Hits"));
        EditContainmentNode createAlbum2 = new TestEditContainmentNode(QName.create(JUKBOX_NS, "album"), "create", m_schemaRegistry).addMatchNode(album2MatchNode).addChild(createSong);


        ModelNodeChange createAlbum1Change = new ModelNodeChange(ModelNodeChangeType.create, createAlbum1);
        NotificationInfo createAlbum1Info = new NotificationInfo().addNotificationInfo(m_artistNode, createAlbum1Change, null);

        ModelNodeChange createAlbum2Change = new ModelNodeChange(ModelNodeChangeType.create, createAlbum2);
        NotificationInfo createAlbum2Info = new NotificationInfo().addNotificationInfo(m_artistNode, createAlbum2Change, null);

        ModelNodeChange createSongChange = new ModelNodeChange(ModelNodeChangeType.create, createSong);
        NotificationInfo createSongInfo = new NotificationInfo().addNotificationInfo(m_album2Node, createSongChange, null);

        notifList.add(createAlbum1Info);
        notifList.add(createAlbum2Info);
        notifList.add(createSongInfo);


        Map<SubSystem, IndexedNotifList> subSystemNotificationMap = m_notificationExecutor
        		.getSubSystemNotificationMap(StandardDataStores.RUNNING, notifList, m_request);
        m_notificationExecutor.sendNotifications(subSystemNotificationMap);

        assertEquals(2, subssytemX.getLastNotifList().size());
        EditConfigChangeNotification editConfigChangeNotification = (EditConfigChangeNotification) subssytemX.getLastNotifList().get(0);
        assertEquals(createAlbum1Info.getChange(), editConfigChangeNotification.getChange());
        assertEquals(StandardDataStores.RUNNING, editConfigChangeNotification.getDataStore());

        editConfigChangeNotification = (EditConfigChangeNotification) subssytemX.getLastNotifList().get(1);
        assertEquals(createAlbum2Info.getChange(), editConfigChangeNotification.getChange());
        assertEquals(StandardDataStores.RUNNING, editConfigChangeNotification.getDataStore());


    }

    @Test
    public void testNotificationIsSentToChidSussystem(){

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
		EditContainmentNode deletingLibrary = new TestEditContainmentNode(QName.create(JUKBOX_NS, "library"), "delete", m_schemaRegistry);
		ModelNodeChange jukeBoxNodeChange = new ModelNodeChange(ModelNodeChangeType.delete, deletingLibrary);
		NotificationInfo deletingJukeBoxInfo = new NotificationInfo().addNotificationInfo(m_jukeBoxNode, jukeBoxNodeChange, null);
		deletingNotifList.add(deletingJukeBoxInfo);

        Map<SubSystem, IndexedNotifList> subSystemNotificationMap = m_notificationExecutor
        		.getSubSystemNotificationMap(StandardDataStores.RUNNING, deletingNotifList, m_request);
        m_notificationExecutor.sendNotifications(subSystemNotificationMap);
		//child subsystem is also sent the notification
		assertEquals(1, librarySubsystem.getLastNotifList().size());
    }


    /**
     * We are trying to simulate the following edit-config request.
     * {@code
     * <edit-config>
     * 	<target>
     * 		<running/>
     * 	</target>
     * 	<test-option>set</test-option>
     * 	<config>
     *  	<pma xmlns="urn:org:bbf2:pma" xmlns:xc="urn:ietf:params:xml:ns:netconf:base:1.0">
     *   		<pma-swmgmt>
     *    			<pma-swver-override>
     *      			<pma-dpu-sw-version-override xc:operation="replace">
     *        				<dpu-id>OLT-1.R1.S1.LT1.P1.ONT4</dpu-id>
     *        				<planned-sw-version-url>ftp://swversionurl/demo2/SW002</planned-sw-version-url>
     *        				<download-sw-version-url>ftp://swversionurl/demo2/SW002</download-sw-version-url>
     *        				<delayed-activate>false</delayed-activate>
     *      			</pma-dpu-sw-version-override>
     *      		</pma-swver-override>
     *    			<pma-dpu-sw-control xc:operation="replace">
     *      			<max-num-devices>10</max-num-devices>
     *      			<time-interval>1</time-interval>
     *    			</pma-dpu-sw-control>
     *  		</pma-swmgmt>
     *		</pma>
     *	</config>
     * </edit-config>
     * }
     * So here assuming that the nodes swver-override/dpu-sw-control belong to a single subsystem X, we need to send a 2 notificiation to subsystem X.
     */
    @Test
    public void testParentNotifNotContainDataOfChildNotif() {
        List<NotificationInfo> notifInfos = new ArrayList<>();

        TestSubsystem subssytemX = new TestSubsystem();
        when(m_swmgmtNode.getSubSystem()).thenReturn(subssytemX);
        when(m_swverOverrideNode.getSubSystem()).thenReturn(subssytemX);

        EditChangeNode changeNode1 = new EditChangeNode(QName.create(PMA_NS, MAX_NUM_DEVICES), new GenericConfigAttribute(MAX_NUM_DEVICES, PMA_NS, "10"));
        EditChangeNode changeNode2 = new EditChangeNode(QName.create(PMA_NS, TIME_INTERVAL), new GenericConfigAttribute(TIME_INTERVAL, PMA_NS, "2"));

        EditContainmentNode replaceDpuSwControl = new TestEditContainmentNode(QName.create("urn:org:bbf2:pma", "pma-dpu-sw-control"), "replace", m_schemaRegistry);
        replaceDpuSwControl.addChangeNode(changeNode1);
        replaceDpuSwControl.addChangeNode(changeNode2);

        EditChangeNode changeNodeOverride1 = new EditChangeNode(QName.create(PMA_NS, PLANNED_SW_VERSION_URL),
                new GenericConfigAttribute(PLANNED_SW_VERSION_URL, PMA_NS, "ftp://swversionurl/demo2/SW002"));
        EditChangeNode changeNodeOverride2 = new EditChangeNode(QName.create(PMA_NS, DOWNLOAD_SW_VERSION_URL),
                new GenericConfigAttribute(DOWNLOAD_SW_VERSION_URL, PMA_NS, "ftp://swversionurl/demo2/SW002"));

        EditContainmentNode replaceSwOverrideControl = new TestEditContainmentNode(QName.create(PMA_NS, "pma-dpu-sw-control"), "replace", m_schemaRegistry);
        replaceSwOverrideControl.addChangeNode(changeNodeOverride1);
        replaceSwOverrideControl.addChangeNode(changeNodeOverride2);
        replaceSwOverrideControl.addMatchNode(new EditMatchNode(QName.create(PMA_NS, "dpu-id"),new GenericConfigAttribute("dpu-id", PMA_NS, "OLT-1.R1.S1" +
                ".LT1.P1.ONT4")));


        ModelNodeChange swmgmtNodeChange = new ModelNodeChange(ModelNodeChangeType.replace, replaceDpuSwControl);
        NotificationInfo swmgmtInfo = new NotificationInfo().addNotificationInfo(m_swmgmtNode, swmgmtNodeChange, null);

        ModelNodeChange dpuSwVersionOveride = new ModelNodeChange(ModelNodeChangeType.replace, replaceSwOverrideControl);
        NotificationInfo swVersionOverrideInfo = new NotificationInfo().addNotificationInfo(m_swverOverrideNode, dpuSwVersionOveride, null);

        notifInfos.add(swmgmtInfo);
        notifInfos.add(swVersionOverrideInfo);

        Map<SubSystem, IndexedNotifList> subSystemNotificationMap = m_notificationExecutor
        		.getSubSystemNotificationMap(StandardDataStores.RUNNING, notifInfos, m_request);
        m_notificationExecutor.sendNotifications(subSystemNotificationMap);

        assertEquals(2, subssytemX.getLastNotifList().size());

        EditConfigChangeNotification editConfigChangeNotif1 = (EditConfigChangeNotification) subssytemX.getLastNotifList().get(0);
        EditConfigChangeNotification editConfigChangeNotif2 = (EditConfigChangeNotification) subssytemX.getLastNotifList().get(1);

        assertEquals(swmgmtInfo.getChange(), editConfigChangeNotif1.getChange());
        assertEquals(swVersionOverrideInfo.getChange(), editConfigChangeNotif2.getChange());

        assertEquals(StandardDataStores.RUNNING, editConfigChangeNotif1.getDataStore());
    }

    @Test
    public void testRefineNetconfConfigChangeNotification(){
    	NetconfConfigChangeNotification netconfConfigChangeNotification = new NetconfConfigChangeNotification();
    	EditInfo editInfo = new EditInfo();
    	editInfo.setTarget("/prefix1:container1/prefix2:container2");
    	netconfConfigChangeNotification.setEditList(editInfo);
    	m_notificationExecutor.refineNetconfConfigChangeNotification(netconfConfigChangeNotification);

    	assertEquals("replace", netconfConfigChangeNotification.getEditList().get(0).getOperation());
    }

    @Test
    public void testExceptionHandlingInSendNotifications(){
        Map<SubSystem, IndexedNotifList> subSystemNotificationMap = new LinkedHashMap<>();

        TestSubsystem subsystemX = mock(TestSubsystem.class);
        ChangeNotification changeNotificationX = mock(ChangeNotification.class);
        IndexedNotifList changeNotificationsX = getNotifList(changeNotificationX);
        subSystemNotificationMap.put(subsystemX, changeNotificationsX);

        TestSubsystem subsystemY = mock(TestSubsystem.class);
        ChangeNotification changeNotificationY = mock(ChangeNotification.class);
        IndexedNotifList changeNotificationsY = getNotifList(changeNotificationY);
        subSystemNotificationMap.put(subsystemY, changeNotificationsY);

        // Case: Exception while sending notification to subsystem X , verify notification is sent to subsystem Y
        doThrow(new RuntimeException("Something bad")).when(subsystemX).notifyChanged(changeNotificationsX.list());

        m_notificationExecutor.sendNotifications((subSystemNotificationMap));
        verify(subsystemY).notifyChanged(changeNotificationsY.list());
    }

    @Test
    public void testGetSubSystemChangeTreeNodeMapForNoChanges() {

        WritableChangeTreeNode aggregatorCTN = mock(WritableChangeTreeNode.class);
        SubSystemRegistry subSystemRegistry = new SubSystemRegistryImpl();
        when(aggregatorCTN.getChangedNodeTypes()).thenReturn(Collections.emptySet());

        m_notificationExecutor = new SubsystemNotificationExecutor(subSystemRegistry);
        Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> subSystemChangeTreeNodeMap =
                m_notificationExecutor.getSubSystemChangeTreeNodeMap(aggregatorCTN, false, m_netconfClientInfo);
        assertEquals(0, subSystemChangeTreeNodeMap.size());
    }

    @Test
    public void testSendPreCommitNotificationsV2() throws SubSystemValidationException {
        SubSystem subSystem = mock(SubSystem.class);
        SchemaPath schemaPath = mock(SchemaPath.class);
        ChangeTreeNode changeTreeNode = mock(ChangeTreeNode.class);
        Map<SchemaPath, List<ChangeTreeNode>> changesMap = new HashMap<>();
        changesMap.put(schemaPath, Collections.singletonList(changeTreeNode));
        Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> subSystemCTNMap = new HashMap<>();
        subSystemCTNMap.put(subSystem, changesMap);
        m_notificationExecutor.sendPreCommitNotificationsV2(subSystemCTNMap);
        verify(subSystem).preCommit(changesMap);
    }

    @Test
    public void testSendNotificationsV2() {
        SubSystem subSystem = mock(SubSystem.class);
        SchemaPath schemaPath = mock(SchemaPath.class);
        ChangeTreeNode changeTreeNode = mock(ChangeTreeNode.class);
        Map<SchemaPath, List<ChangeTreeNode>> changesMap = new HashMap<>();
        changesMap.put(schemaPath, Collections.singletonList(changeTreeNode));
        Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> subSystemCTNMap = new HashMap<>();
        subSystemCTNMap.put(subSystem, changesMap);
        m_notificationExecutor.sendNotificationsV2(subSystemCTNMap);
        verify(subSystem).postCommit(changesMap);
    }
    
    @Test
    public void testSendNcExtensionNotifications_SingleSubsystem() {
        SubSystem subSystem = mock(SubSystem.class);
        SchemaPath schemaPath = mock(SchemaPath.class);
        ChangeTreeNode changeTreeNode = mock(ChangeTreeNode.class);
        Map<SchemaPath, List<ChangeTreeNode>> changesMap = new HashMap<>();
        changesMap.put(schemaPath, Collections.singletonList(changeTreeNode));
        Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> subSystemCTNMap = new HashMap<>();
        subSystemCTNMap.put(subSystem, changesMap);
        Element ncExtensionResponse = mock(Element.class);
        when(subSystem.handleNcExtensions(changesMap, NetconfResources.TRIGGER_SYNC_UPON_SUCCESS_QNAME)).thenReturn(ncExtensionResponse);
        List<Element> ncExtensionsReponsesFromAllSubsytems = m_notificationExecutor.sendNcExtensionNotifications(subSystemCTNMap, NetconfResources.TRIGGER_SYNC_UPON_SUCCESS_QNAME);
        verify(subSystem).handleNcExtensions(changesMap, NetconfResources.TRIGGER_SYNC_UPON_SUCCESS_QNAME);
        assertTrue(ncExtensionsReponsesFromAllSubsytems.size() == 1);
        assertEquals(ncExtensionResponse, ncExtensionsReponsesFromAllSubsytems.get(0));
    }
    
    @Test
    public void testSendNcExtensionNotifications_MultipleSubsystems() {
        SubSystem subSystem = mock(SubSystem.class);
        SchemaPath schemaPath = mock(SchemaPath.class);
        ChangeTreeNode changeTreeNode = mock(ChangeTreeNode.class);
        Map<SchemaPath, List<ChangeTreeNode>> changesMap = new HashMap<>();
        changesMap.put(schemaPath, Collections.singletonList(changeTreeNode));
        Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> subSystemCTNMap = new HashMap<>();
        subSystemCTNMap.put(subSystem, changesMap);
        SubSystem subSystem2 = mock(SubSystem.class);
        subSystemCTNMap.put(subSystem2, changesMap);
        
        Element ncExtensionResponse = mock(Element.class);
        when(subSystem.handleNcExtensions(changesMap, NetconfResources.TRIGGER_SYNC_UPON_SUCCESS_QNAME)).thenReturn(ncExtensionResponse);
        when(subSystem2.handleNcExtensions(changesMap, NetconfResources.TRIGGER_SYNC_UPON_SUCCESS_QNAME)).thenReturn(ncExtensionResponse);
        
        List<Element> ncExtensionsReponsesFromAllSubsytems = m_notificationExecutor.sendNcExtensionNotifications(subSystemCTNMap, NetconfResources.TRIGGER_SYNC_UPON_SUCCESS_QNAME);
        verify(subSystem).handleNcExtensions(changesMap, NetconfResources.TRIGGER_SYNC_UPON_SUCCESS_QNAME);
        verify(subSystem2).handleNcExtensions(changesMap, NetconfResources.TRIGGER_SYNC_UPON_SUCCESS_QNAME);
        assertTrue(ncExtensionsReponsesFromAllSubsytems.size() == 2);
        assertEquals(ncExtensionResponse, ncExtensionsReponsesFromAllSubsytems.get(0));
        assertEquals(ncExtensionResponse, ncExtensionsReponsesFromAllSubsytems.get(1));
    }

    @Test
    public void testGetSubSystemChangeTreeNodeMapForActualChanges() {

        initializePrerequisites();

        //changeTreeNodes for corresponding nodes
        ChangeTreeNode jukeboxCTN = mock(ChangeTreeNode.class);
        ChangeTreeNode libraryCTN = mock(ChangeTreeNode.class);
        ChangeTreeNode artistCTN = mock(ChangeTreeNode.class);

        //Create 2 subsystems by registering 1st subsystem to Jukebox and Artist and 2nd subsystem to Library
        SubSystem subSystem1 = mock(SubSystem.class);
        SubSystem subSystem2 = mock(SubSystem.class);
        m_subSystemRegistry.register(JUKEBOX_QNAME.toString(), JUKEBOX_SCHEMA_PATH, subSystem1);
        m_subSystemRegistry.register(LIBRARY_QNAME.toString(), LIBRARY_SCHEMA_PATH, subSystem2);
        m_subSystemRegistry.register(ARTIST_QNAME.toString(), ARTIST_SCHEMA_PATH, subSystem1);

        when(m_aggregatorCTN.getNodesOfType(JUKEBOX_SCHEMA_PATH)).thenReturn(Collections.singleton(jukeboxCTN));
        when(m_aggregatorCTN.getNodesOfType(LIBRARY_SCHEMA_PATH)).thenReturn(Collections.singleton(libraryCTN));
        when(m_aggregatorCTN.getNodesOfType(ARTIST_SCHEMA_PATH)).thenReturn(Collections.singleton(artistCTN));

        m_notificationExecutor = new SubsystemNotificationExecutor(m_subSystemRegistry);
        Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> subSystemChangeTreeNodeMap =
                m_notificationExecutor.getSubSystemChangeTreeNodeMap(m_aggregatorCTN, false, m_netconfClientInfo);

        Map<SchemaPath, List<ChangeTreeNode>> subSystem1ChangesMap = subSystemChangeTreeNodeMap.get(subSystem1);
        Map<SchemaPath, List<ChangeTreeNode>> subSystem2ChangesMap = subSystemChangeTreeNodeMap.get(subSystem2);

        assertEquals(2, subSystemChangeTreeNodeMap.size());
        assertEquals(2, subSystem1ChangesMap.size());
        assertEquals(1, subSystem2ChangesMap.size());
        assertEquals(Collections.singletonList(jukeboxCTN), subSystem1ChangesMap.get(JUKEBOX_SCHEMA_PATH));
        assertEquals(Collections.singletonList(libraryCTN), subSystem2ChangesMap.get(LIBRARY_SCHEMA_PATH));
        assertEquals(Collections.singletonList(artistCTN), subSystem1ChangesMap.get(ARTIST_SCHEMA_PATH));

        verify(m_aggregatorCTN).addContextValue(CONTEXT_MAP_UPLOAD_REQUEST_KEY, false);
        verify(m_aggregatorCTN).addContextValue(CONTEXT_MAP_CLIENT_INFO_KEY, m_netconfClientInfo);
    }

    @Test
    public void testGetSubSystemChangeTreeNodeMapForChangeWithoutHavingAnySubSystemRegistered() {

        initializePrerequisites();

        when(m_aggregatorCTN.getNodesOfType(JUKEBOX_SCHEMA_PATH)).thenReturn(Collections.singleton(mock(ChangeTreeNode.class)));
        when(m_aggregatorCTN.getNodesOfType(LIBRARY_SCHEMA_PATH)).thenReturn(Collections.singleton(mock(ChangeTreeNode.class)));
        when(m_aggregatorCTN.getNodesOfType(ARTIST_SCHEMA_PATH)).thenReturn(Collections.singleton(mock(ChangeTreeNode.class)));

        m_notificationExecutor = new SubsystemNotificationExecutor(m_subSystemRegistry);
        Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> subSystemChangeTreeNodeMap =
                m_notificationExecutor.getSubSystemChangeTreeNodeMap(m_aggregatorCTN, false, m_netconfClientInfo);

        //Map entry is created for NoopSubSystem since no SubSystem is registered for changed SchemaPaths
        assertEquals(1, subSystemChangeTreeNodeMap.size());
        assertTrue(subSystemChangeTreeNodeMap.entrySet().iterator().next().getKey() instanceof NoopSubSystem);
    }

    @Test
    public void testGetSubSystemChangeTreeNodeMapForSchemaPathHavingMultipleCTNs() {

        initializePrerequisites();

        //Create 2 subsystems by registering 1st subsystem to Jukebox and Artist and 2nd subsystem to Library
        SubSystem subSystem1 = mock(SubSystem.class);
        SubSystem subSystem2 = mock(SubSystem.class);
        m_subSystemRegistry.register(JUKEBOX_QNAME.toString(), JUKEBOX_SCHEMA_PATH, subSystem1);
        m_subSystemRegistry.register(LIBRARY_QNAME.toString(), LIBRARY_SCHEMA_PATH, subSystem2);
        m_subSystemRegistry.register(ARTIST_QNAME.toString(), ARTIST_SCHEMA_PATH, subSystem1);

        //changeTreeNodes for corresponding nodes
        ChangeTreeNode jukeboxCTN = mock(ChangeTreeNode.class);
        ChangeTreeNode libraryCTN = mock(ChangeTreeNode.class);
        ChangeTreeNode firstArtistCTN = mock(ChangeTreeNode.class);
        ChangeTreeNode secondArtistCTN = mock(ChangeTreeNode.class);

        List<ChangeTreeNode> artistCTNs = new ArrayList<>();
        artistCTNs.add(firstArtistCTN);
        artistCTNs.add(secondArtistCTN);

        //Jukebox schema path has 2 change tree nodes
        when(m_aggregatorCTN.getNodesOfType(JUKEBOX_SCHEMA_PATH)).thenReturn(Collections.singleton(jukeboxCTN));
        when(m_aggregatorCTN.getNodesOfType(LIBRARY_SCHEMA_PATH)).thenReturn(Collections.singleton(libraryCTN));
        when(m_aggregatorCTN.getNodesOfType(ARTIST_SCHEMA_PATH)).thenReturn(artistCTNs);

        m_notificationExecutor = new SubsystemNotificationExecutor(m_subSystemRegistry);
        Map<SubSystem, Map<SchemaPath, List<ChangeTreeNode>>> subSystemChangeTreeNodeMap =
                m_notificationExecutor.getSubSystemChangeTreeNodeMap(m_aggregatorCTN, false, m_netconfClientInfo);
        assertEquals(2, subSystemChangeTreeNodeMap.size());

        Map<SchemaPath, List<ChangeTreeNode>> subSystem1ChangesMap = subSystemChangeTreeNodeMap.get(subSystem1);
        Map<SchemaPath, List<ChangeTreeNode>> subSystem2ChangesMap = subSystemChangeTreeNodeMap.get(subSystem2);

        assertEquals(2, subSystem1ChangesMap.size());
        assertEquals(1, subSystem2ChangesMap.size());
        assertEquals(1, subSystem1ChangesMap.get(JUKEBOX_SCHEMA_PATH).size());
        assertEquals(1, subSystem2ChangesMap.get(LIBRARY_SCHEMA_PATH).size());
        assertEquals(2, subSystem1ChangesMap.get(ARTIST_SCHEMA_PATH).size());
        assertEquals(Collections.singletonList(jukeboxCTN), subSystem1ChangesMap.get(JUKEBOX_SCHEMA_PATH));
        assertEquals(Collections.singletonList(libraryCTN), subSystem2ChangesMap.get(LIBRARY_SCHEMA_PATH));
        assertEquals(artistCTNs, subSystem1ChangesMap.get(ARTIST_SCHEMA_PATH));
    }

    private void initializePrerequisites() {
        m_aggregatorCTN = mock(WritableChangeTreeNode.class);
        m_subSystemRegistry = new SubSystemRegistryImpl();

        //Assuming below schema paths as changed schema paths
        Set<SchemaPath> changedSchemaPaths = new HashSet<>();
        changedSchemaPaths.add(JUKEBOX_SCHEMA_PATH);
        changedSchemaPaths.add(LIBRARY_SCHEMA_PATH);
        changedSchemaPaths.add(ARTIST_SCHEMA_PATH);

        when(m_aggregatorCTN.getChangedNodeTypes()).thenReturn(changedSchemaPaths);
    }

    private IndexedNotifList getNotifList(ChangeNotification changeNotification) {
        IndexedNotifList list = new IndexedNotifList();
        list.add(changeNotification);
        return list;
    }

    private class TestSubsystem extends AbstractSubSystem {
        private List<ChangeNotification> m_lastNotifList;

        public List<ChangeNotification> getLastNotifList() {
            return m_lastNotifList;
        }

        @Override
        protected boolean byPassAuthorization() {
            return true;
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
