package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn.CONTAINER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditConfigChangeNotification;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeRdn;
import org.junit.Test;

import org.broadband_forum.obbaa.netconf.api.messages.StandardDataStores;
import org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.TestConstants;

public class EditConfigChangeNotificationTest {
    private static final ModelNodeId DEVICE_HOLDER_ID = new ModelNodeId(
            Arrays.asList(new ModelNodeRdn(CONTAINER, TestConstants.ANV_NAMESPACE, "device-manager"),
                    new ModelNodeRdn(CONTAINER, TestConstants.DEVICE_HOLDER_NAMESPACE, "device-holder"),
                    new ModelNodeRdn("name", TestConstants.DEVICE_HOLDER_NAMESPACE, TestConstants.DEVICE_HOLDER_NAME)));

    private static final ModelNodeId DEVICE_HOLDER_ID2 = new ModelNodeId(
            Arrays.asList(new ModelNodeRdn(CONTAINER, TestConstants.ANV_NAMESPACE, "device-manager"),
                    new ModelNodeRdn(CONTAINER, TestConstants.DEVICE_HOLDER_NAMESPACE, "device-holder"),
                    new ModelNodeRdn("name", TestConstants.DEVICE_HOLDER_NAMESPACE, "device-holder")));

    private static final ModelNodeId DEVICE_ID = new ModelNodeId(
            Arrays.asList(new ModelNodeRdn(CONTAINER, TestConstants.ANV_NAMESPACE, "device-manager"),
                    new ModelNodeRdn(CONTAINER, TestConstants.DEVICE_HOLDER_NAMESPACE, "device-holder"),
                    new ModelNodeRdn("name", TestConstants.DEVICE_HOLDER_NAMESPACE, TestConstants.DEVICE_HOLDER_NAME),
                    new ModelNodeRdn(CONTAINER, TestConstants.DEVICE_HOLDER_NAMESPACE, "device"),
                    new ModelNodeRdn(TestConstants.DEVICE_ID_LOCAL_NAME, TestConstants.DEVICE_HOLDER_NAMESPACE, "R1.S1.LT1.PON1.ONT1")));

    private static final ModelNodeId NUMBER_MODEL_NODE_ID1 = new ModelNodeId(Arrays.asList(new ModelNodeRdn("container",
            TestConstants.ANV_NAMESPACE, "device-manager"), new ModelNodeRdn("container",
                    TestConstants.DEVICE_HOLDER_NAMESPACE, "device-holder"), new ModelNodeRdn("name",
                            TestConstants.DEVICE_HOLDER_NAMESPACE, "100")));

    private static final ModelNodeId NUMBER_MODEL_NODE_ID2 = new ModelNodeId(Arrays.asList(new ModelNodeRdn("container",
            TestConstants.ANV_NAMESPACE, "device-manager"), new ModelNodeRdn("container",
                    TestConstants.DEVICE_HOLDER_NAMESPACE, "device-holder"), new ModelNodeRdn("name",
                            TestConstants.DEVICE_HOLDER_NAMESPACE, "90")));

    @Test
    public void testSameValue(){
        ModelNode changedNode = mock(ModelNode.class);
        EditConfigChangeNotification changeNotification = new EditConfigChangeNotification(DEVICE_HOLDER_ID, null,
                StandardDataStores.RUNNING, changedNode);
        int actual = changeNotification.compareTo(changeNotification);
        assertTrue(actual==0);
    }

    @Test
    public void testDifferentValue() {
        ModelNode changedNode = mock(ModelNode.class);
        EditConfigChangeNotification changeNotification = new EditConfigChangeNotification(DEVICE_HOLDER_ID, null,
                StandardDataStores.RUNNING, changedNode);
        EditConfigChangeNotification changeNotification2 = new EditConfigChangeNotification(DEVICE_HOLDER_ID2, null,
                StandardDataStores.RUNNING, changedNode);
        int actual = changeNotification.compareTo(changeNotification2);
        assertTrue(actual > 0);
        actual = changeNotification2.compareTo(changeNotification);
        assertTrue(actual < 0);
    }

    @Test
    public void testList(){
        ModelNode changedNode = mock(ModelNode.class);
        EditConfigChangeNotification changeNotification = new EditConfigChangeNotification(DEVICE_HOLDER_ID, null,
                StandardDataStores.RUNNING, changedNode);
        EditConfigChangeNotification changeNotification2 = new EditConfigChangeNotification(DEVICE_HOLDER_ID2, null,
                StandardDataStores.RUNNING, changedNode);
        EditConfigChangeNotification changeNotification3 = new EditConfigChangeNotification(DEVICE_ID, null,
                StandardDataStores.RUNNING, changedNode);
        ArrayList<EditConfigChangeNotification> arrayList = new ArrayList<>();
        arrayList.add(changeNotification);
        arrayList.add(changeNotification2);
        arrayList.add(changeNotification3);
        Collections.sort(arrayList);
        assertEquals(changeNotification2,arrayList.get(0));
        assertEquals(changeNotification,arrayList.get(1));
        assertEquals(changeNotification3,arrayList.get(2));
    }

    @Test
    public void testComparisionForDifferentNumbers() {
        ModelNode changedNode = mock(ModelNode.class);
        EditConfigChangeNotification changeNotificationNumber1 = new EditConfigChangeNotification(NUMBER_MODEL_NODE_ID1, null,
                StandardDataStores.RUNNING, changedNode);
        EditConfigChangeNotification changeNotificationNumber2 = new EditConfigChangeNotification(NUMBER_MODEL_NODE_ID2, null,
                StandardDataStores.RUNNING, changedNode);
        int actual = changeNotificationNumber1.compareTo(changeNotificationNumber2);
        assertTrue(actual > 0);
        actual = changeNotificationNumber2.compareTo(changeNotificationNumber1);
        assertTrue(actual < 0);
    }

    @Test
    public void testComparisionForSameNumbers() {
        ModelNode changedNode = mock(ModelNode.class);
        EditConfigChangeNotification changeNotificationNumber1 = new EditConfigChangeNotification(NUMBER_MODEL_NODE_ID1, null,
                StandardDataStores.RUNNING, changedNode);
        int actual = changeNotificationNumber1.compareTo(changeNotificationNumber1);
        assertTrue(actual==0);
    }

    @Test
    public void testDifferentModelRdnValues() {
        ModelNode changedNode = mock(ModelNode.class);
        EditConfigChangeNotification changeNotificationNumber1 = new EditConfigChangeNotification(DEVICE_HOLDER_ID, null,
                StandardDataStores.RUNNING, changedNode);
        EditConfigChangeNotification changeNotificationNumber2 = new EditConfigChangeNotification(DEVICE_ID, null,
                StandardDataStores.RUNNING, changedNode);
        int actual = changeNotificationNumber1.compareTo(changeNotificationNumber2);
        assertTrue(actual < 0);
        actual = changeNotificationNumber2.compareTo(changeNotificationNumber1);
        assertTrue(actual > 0);
    }


    @Test
    public void testSameModelRdnValues() {
        ModelNode changedNode = mock(ModelNode.class);
        EditConfigChangeNotification changeNotificationNumber1 = new EditConfigChangeNotification(DEVICE_HOLDER_ID, null,
                StandardDataStores.RUNNING, changedNode);
        int actual = changeNotificationNumber1.compareTo(changeNotificationNumber1);
        assertTrue(actual == 0);
    }

    @Test
    public void testForNullValuesOfModelNodeId() {
        ModelNode changedNode = mock(ModelNode.class);
        EditConfigChangeNotification changeNotificationWithoutNullModelNodeId = new EditConfigChangeNotification(DEVICE_HOLDER_ID, null,
                StandardDataStores.RUNNING, changedNode);
        EditConfigChangeNotification changeNotificationWithNullModelNodeId = new EditConfigChangeNotification(null, null,
                StandardDataStores.RUNNING, changedNode);
        int actual = changeNotificationWithNullModelNodeId.compareTo(changeNotificationWithNullModelNodeId);
        assertTrue(actual == 0);
        actual = changeNotificationWithNullModelNodeId.compareTo(changeNotificationWithoutNullModelNodeId);
        assertTrue(actual == -1);
        actual = changeNotificationWithoutNullModelNodeId.compareTo(changeNotificationWithNullModelNodeId);
        assertTrue(actual == 1);

    }
}
