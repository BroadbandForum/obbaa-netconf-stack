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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.server.RequestScopeJunitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.common.QName;

@RunWith(RequestScopeJunitRunner.class)
public class ModelNodeIdTest {
    public static final String ANV_PLATFORM_NAMESPACE = "http://www.test-company.com/solutions/anv-platform";
    public static final String ANV_PLATFORM_REVISION = "2016-04-28";
    public static final String ANV_PLATFORM_ROOT_CONTAINER_NAME = "platform";
    public static final QName QNAME_PLATFORM_ROOT_CONTAINER = QName.create(ANV_PLATFORM_NAMESPACE,
            ANV_PLATFORM_REVISION, ANV_PLATFORM_ROOT_CONTAINER_NAME);
    private static final String SOMECONTAINER = "SomeContainer";
    private static final String SOMEOTHERLISTCONTAINER = "SomeOtherListContainer";
    private static final String KEYNAME1 = "KeyName1";
    private static final String KEYNAME2 = "KeyName2";
    private static final String KEYNAME3 = "KeyName3";
    private static final String KEYVALUE1 = "KeyValue1";
    private static final String KEYVALUE2 = "KeyValue2";
    private static final String KEYVALUE3 = "KeyValue3";
    private static final String OTHERKEYNAME = "OtherKeyName";
    private static final String OTHERKEYVALUE = "OtherKeyValue";
    private static final String NAMESPACE1 = "namespace1";
    private static final String TEST = "test";
    private static final String TESTOBJ = "testobj";
    private static final String UNKNOWN = "unknown";
    private static final String CONTAINER = "container";
    private static final String PLATFORM = "platform";
    public static final String DEVICE_HOLDER_NAME = "device-holder-name";
    public static final String DEVICE_HOLDER_NAMESPACE = "http://www.test-company.com/solutions/anv-device-holders";
    public static final String ANV_NAMESPACE = "http://www.test-company.com/solutions/anv";
    public static final String DEVICE_ID_LOCAL_NAME = "device-id";

    // @formatter:off
    private final ModelNodeId EMPTY_CONTAINER = new ModelNodeId();

    private static final ModelNodeId ONE_CONTAINER_ONLY = new ModelNodeId(
            Arrays.asList(new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE,
                    ANV_PLATFORM_ROOT_CONTAINER_NAME)));

    private static final ModelNodeId ANOTHER_ONE_CONTAINER_ONLY = new ModelNodeId(
            Arrays.asList(new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE,
                    ANV_PLATFORM_ROOT_CONTAINER_NAME)));

    private static final ModelNodeId TWO_CONTAINERS_ONLY = new ModelNodeId(Arrays.asList(
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE,
                    ANV_PLATFORM_ROOT_CONTAINER_NAME),
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE,
                    SOMECONTAINER)));

    private static final ModelNodeId LIST_WITH_SINGLE_KEY = new ModelNodeId(Arrays.asList(
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE,
                    ANV_PLATFORM_ROOT_CONTAINER_NAME),
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE, SOMECONTAINER),
            new ModelNodeRdn(KEYNAME1, ANV_PLATFORM_NAMESPACE, KEYVALUE1)));

    private static final ModelNodeId LIST_WITH_MULTIPLE_KEYS_ORDER1 = new ModelNodeId(Arrays.asList(
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE,
                    ANV_PLATFORM_ROOT_CONTAINER_NAME),
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE, SOMECONTAINER),
            new ModelNodeRdn(KEYNAME1, ANV_PLATFORM_NAMESPACE, KEYVALUE1),
            new ModelNodeRdn(KEYNAME2, ANV_PLATFORM_NAMESPACE, KEYVALUE2)));

    private static final ModelNodeId LIST_WITH_MULTIPLE_KEYS_ORDER2 = new ModelNodeId(Arrays.asList(
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE,
                    ANV_PLATFORM_ROOT_CONTAINER_NAME),
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE, SOMECONTAINER),
            new ModelNodeRdn(KEYNAME2, ANV_PLATFORM_NAMESPACE, KEYVALUE2),
            new ModelNodeRdn(KEYNAME1, ANV_PLATFORM_NAMESPACE, KEYVALUE1)));

    private static final ModelNodeId LIST_WITH_MULTIPLE_KEYS_MISSINGKEY1 = new ModelNodeId(Arrays.asList(
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE,
                    ANV_PLATFORM_ROOT_CONTAINER_NAME),
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE, SOMECONTAINER),
            new ModelNodeRdn(KEYNAME2, ANV_PLATFORM_NAMESPACE, KEYVALUE2)));

    private static final ModelNodeId LIST_WITH_MULTIPLE_KEYS_MISSINGKEY2 = new ModelNodeId(Arrays.asList(
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE,
                    ANV_PLATFORM_ROOT_CONTAINER_NAME),
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE, SOMECONTAINER),
            new ModelNodeRdn(KEYNAME1, ANV_PLATFORM_NAMESPACE, KEYVALUE1)));

    private static final ModelNodeId NESTED_LIST_WITH_MULTIPLEKEYS_ORDER1 = new ModelNodeId(Arrays.asList(
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE,
                    ANV_PLATFORM_ROOT_CONTAINER_NAME),
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE, SOMECONTAINER),
            new ModelNodeRdn(KEYNAME2, ANV_PLATFORM_NAMESPACE, KEYVALUE2),
            new ModelNodeRdn(KEYNAME1, ANV_PLATFORM_NAMESPACE, KEYVALUE1),
            new ModelNodeRdn(KEYNAME3, ANV_PLATFORM_NAMESPACE, KEYVALUE3),
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE,
                    SOMEOTHERLISTCONTAINER),
            new ModelNodeRdn(OTHERKEYNAME, ANV_PLATFORM_NAMESPACE, OTHERKEYVALUE)));

    private static final ModelNodeId NESTED_LIST_WITH_MULTIPLEKEYS_ORDER2 = new ModelNodeId(Arrays.asList(
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE,
                    ANV_PLATFORM_ROOT_CONTAINER_NAME),
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE, SOMECONTAINER),
            new ModelNodeRdn(KEYNAME1, ANV_PLATFORM_NAMESPACE, KEYVALUE1),
            new ModelNodeRdn(KEYNAME3, ANV_PLATFORM_NAMESPACE, KEYVALUE3),
            new ModelNodeRdn(KEYNAME2, ANV_PLATFORM_NAMESPACE, KEYVALUE2),
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE,
                    SOMEOTHERLISTCONTAINER),
            new ModelNodeRdn(OTHERKEYNAME, ANV_PLATFORM_NAMESPACE, OTHERKEYVALUE)));

    private static final ModelNodeId NESTED_LIST_WITH_MULTIPLEKEYS_ONEKEY_MISSINGLEVEL1 = new ModelNodeId(Arrays.asList(
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE,
                    ANV_PLATFORM_ROOT_CONTAINER_NAME),
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE, SOMECONTAINER),
            new ModelNodeRdn(KEYNAME1, ANV_PLATFORM_NAMESPACE, KEYVALUE1),
            new ModelNodeRdn(KEYNAME2, ANV_PLATFORM_NAMESPACE, KEYVALUE2),
            new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE,
                    SOMEOTHERLISTCONTAINER),
            new ModelNodeRdn(OTHERKEYNAME, ANV_PLATFORM_NAMESPACE, OTHERKEYVALUE)));

    private static final ModelNodeId NUMBER_MODEL_NODE_ID1 = new ModelNodeId(Arrays.asList(new ModelNodeRdn("container",
            ANV_NAMESPACE, "device-manager"), new ModelNodeRdn("container",
            DEVICE_HOLDER_NAMESPACE, "device-holder"), new ModelNodeRdn("name",
            DEVICE_HOLDER_NAMESPACE, "100")));
    private static final ModelNodeId NUMBER_MODEL_NODE_ID2 = new ModelNodeId(Arrays.asList(new ModelNodeRdn("container",
            ANV_NAMESPACE, "device-manager"), new ModelNodeRdn("container",
            DEVICE_HOLDER_NAMESPACE, "device-holder"), new ModelNodeRdn("name",
            DEVICE_HOLDER_NAMESPACE, "90")));

    @Test
    public void testBeginsWithTemplateIgnoreKeyOrder_ContainerOnly() {
        assertTrue(ONE_CONTAINER_ONLY.beginsWithTemplateIgnoreKeyOrder(ONE_CONTAINER_ONLY));
        ModelNodeId objectWithNamespaceDefault = new ModelNodeId(
                Arrays.asList(new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE, ANV_PLATFORM_ROOT_CONTAINER_NAME)));
        ModelNodeId objectWithNamespaceOther = new ModelNodeId(
                Arrays.asList(new ModelNodeRdn(ModelNodeRdn.CONTAINER, NAMESPACE1, ANV_PLATFORM_ROOT_CONTAINER_NAME)));
        ModelNodeId objectWithNamespaceNew = new ModelNodeId(
                Arrays.asList(new ModelNodeRdn(ModelNodeRdn.CONTAINER, NAMESPACE1, CONTAINER)));
        ModelNodeId objectWithNullNameSpace = new ModelNodeId(Arrays.asList(new ModelNodeRdn(ModelNodeRdn.CONTAINER, null, ANV_PLATFORM_ROOT_CONTAINER_NAME)));
        ModelNodeId objectWithNull = new ModelNodeId(Arrays.asList(new ModelNodeRdn(ModelNodeRdn.CONTAINER, null, ANV_PLATFORM_ROOT_CONTAINER_NAME)));
        assertFalse(objectWithNamespaceDefault.beginsWithTemplateIgnoreKeyOrder(objectWithNamespaceNew));
        assertFalse(objectWithNamespaceOther.beginsWithTemplateIgnoreKeyOrder(objectWithNamespaceDefault));
        assertFalse(objectWithNamespaceNew.beginsWithTemplateIgnoreKeyOrder(objectWithNamespaceOther));
        assertTrue(objectWithNullNameSpace.beginsWithTemplateIgnoreKeyOrder(objectWithNull));
    }

    @Test
    public void testBeginsWithTemplateIgnoreKeyOrder_MultipleContainers() {

        assertTrue(TWO_CONTAINERS_ONLY.beginsWithTemplateIgnoreKeyOrder(ONE_CONTAINER_ONLY));
        assertFalse(ONE_CONTAINER_ONLY.beginsWithTemplateIgnoreKeyOrder(TWO_CONTAINERS_ONLY));
        assertTrue(TWO_CONTAINERS_ONLY.beginsWithTemplateIgnoreKeyOrder(TWO_CONTAINERS_ONLY));
    }

    @Test
    public void testBeginsWithTemplateIgnoreKeyOrder_LIST_WITH_SINGLE_KEY() {
        assertTrue(LIST_WITH_SINGLE_KEY.beginsWithTemplateIgnoreKeyOrder(ONE_CONTAINER_ONLY));
        assertTrue(LIST_WITH_SINGLE_KEY.beginsWithTemplateIgnoreKeyOrder(TWO_CONTAINERS_ONLY));
        assertTrue(LIST_WITH_SINGLE_KEY.beginsWithTemplateIgnoreKeyOrder(LIST_WITH_SINGLE_KEY));
    }

    @Test
    public void testBeginsWithTemplateIgnoreKeyOrder_ListWithMultipleKeys() {
        assertTrue(LIST_WITH_MULTIPLE_KEYS_ORDER1.beginsWithTemplateIgnoreKeyOrder(ONE_CONTAINER_ONLY));
        assertTrue(LIST_WITH_MULTIPLE_KEYS_ORDER1.beginsWithTemplateIgnoreKeyOrder(TWO_CONTAINERS_ONLY));
        assertTrue(LIST_WITH_MULTIPLE_KEYS_ORDER1.beginsWithTemplateIgnoreKeyOrder(LIST_WITH_SINGLE_KEY));
        assertTrue(LIST_WITH_MULTIPLE_KEYS_ORDER1.beginsWithTemplateIgnoreKeyOrder(LIST_WITH_MULTIPLE_KEYS_ORDER2));
        assertTrue(LIST_WITH_MULTIPLE_KEYS_ORDER2.beginsWithTemplateIgnoreKeyOrder(LIST_WITH_MULTIPLE_KEYS_ORDER1));
        assertTrue(LIST_WITH_MULTIPLE_KEYS_ORDER1.beginsWithTemplateIgnoreKeyOrder(LIST_WITH_MULTIPLE_KEYS_MISSINGKEY1));
        assertTrue(LIST_WITH_MULTIPLE_KEYS_ORDER1.beginsWithTemplateIgnoreKeyOrder(LIST_WITH_MULTIPLE_KEYS_MISSINGKEY2));
        assertTrue(LIST_WITH_MULTIPLE_KEYS_ORDER2.beginsWithTemplateIgnoreKeyOrder(LIST_WITH_MULTIPLE_KEYS_MISSINGKEY1));
        assertTrue(LIST_WITH_MULTIPLE_KEYS_ORDER2.beginsWithTemplateIgnoreKeyOrder(LIST_WITH_MULTIPLE_KEYS_MISSINGKEY2));
    }

    @Test
    public void testBeginsWithTemplateIgnoreKeyOrder_NestedListWithMultipleKeys() {
        assertTrue(NESTED_LIST_WITH_MULTIPLEKEYS_ORDER1.beginsWithTemplateIgnoreKeyOrder(ONE_CONTAINER_ONLY));
        assertTrue(NESTED_LIST_WITH_MULTIPLEKEYS_ORDER1.beginsWithTemplateIgnoreKeyOrder(TWO_CONTAINERS_ONLY));
        assertTrue(NESTED_LIST_WITH_MULTIPLEKEYS_ORDER1.beginsWithTemplateIgnoreKeyOrder(LIST_WITH_SINGLE_KEY));
        assertTrue(NESTED_LIST_WITH_MULTIPLEKEYS_ORDER1.beginsWithTemplateIgnoreKeyOrder(LIST_WITH_MULTIPLE_KEYS_ORDER2));
        assertTrue(NESTED_LIST_WITH_MULTIPLEKEYS_ORDER1.beginsWithTemplateIgnoreKeyOrder(LIST_WITH_MULTIPLE_KEYS_ORDER1));
        assertFalse(LIST_WITH_MULTIPLE_KEYS_ORDER1.beginsWithTemplateIgnoreKeyOrder(NESTED_LIST_WITH_MULTIPLEKEYS_ORDER1));
        assertTrue(NESTED_LIST_WITH_MULTIPLEKEYS_ORDER1.beginsWithTemplateIgnoreKeyOrder(NESTED_LIST_WITH_MULTIPLEKEYS_ORDER2));
        assertTrue(NESTED_LIST_WITH_MULTIPLEKEYS_ORDER2.beginsWithTemplateIgnoreKeyOrder(NESTED_LIST_WITH_MULTIPLEKEYS_ORDER1));
        assertFalse(NESTED_LIST_WITH_MULTIPLEKEYS_ORDER2.beginsWithTemplateIgnoreKeyOrder(NESTED_LIST_WITH_MULTIPLEKEYS_ONEKEY_MISSINGLEVEL1));
    }

    @Test
    public void testGetRdnValue() {
        assertEquals(PLATFORM, ONE_CONTAINER_ONLY.getRdnValue(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE));
        assertEquals(KEYVALUE1, NESTED_LIST_WITH_MULTIPLEKEYS_ORDER1.getRdnValue(KEYNAME1, ANV_PLATFORM_NAMESPACE));
        assertNull(ONE_CONTAINER_ONLY.getRdnValue(UNKNOWN, ANV_PLATFORM_NAMESPACE));
        assertNull(NESTED_LIST_WITH_MULTIPLEKEYS_ORDER1.getRdnValue(UNKNOWN, ANV_PLATFORM_NAMESPACE));
        assertNull(ONE_CONTAINER_ONLY.getRdnValue(TEST, TEST));
    }

    @Test
    public void testGetRdnValue_Container() {
        assertEquals(PLATFORM, ONE_CONTAINER_ONLY.getRdnValue(ModelNodeRdn.CONTAINER));
        assertNull(ONE_CONTAINER_ONLY.getRdnValue(UNKNOWN));
        assertEquals(PLATFORM, TWO_CONTAINERS_ONLY.getRdnValue(ModelNodeRdn.CONTAINER));
        assertNull(TWO_CONTAINERS_ONLY.getRdnValue(UNKNOWN));
    }

    @Test
    public void testRemoveFirst() {
        ModelNodeId nodeRemove = EMPTY_CONTAINER.clone();
        ModelNodeRdn mnr1 = new ModelNodeRdn(TEST, TEST, TEST);
        nodeRemove.addRdn(mnr1);
        nodeRemove.addRdn(mnr1);
        nodeRemove.addRdn(mnr1);
        nodeRemove.addRdn(mnr1);
        nodeRemove.removeFirst(10);
        int size = nodeRemove.getRdns().size();
        nodeRemove.removeFirst(1);
        assertEquals(size - 1, nodeRemove.getRdns().size());
    }

    @Test
    public void testxPathString() {
        SchemaRegistryImpl schemaImpl = Mockito.mock(SchemaRegistryImpl.class);
        assertEquals("/platform", ONE_CONTAINER_ONLY.xPathString(schemaImpl));
        ModelNodeId emptyContainer = new ModelNodeId();
        assertEquals("/", emptyContainer.xPathString(schemaImpl));
        assertNull(schemaImpl.getPrefix(null));
    }

    @Test
    public void testAppendNameToXPath() {
        ModelNodeId oneContainerClone = ONE_CONTAINER_ONLY.clone();
        ModelNodeId twoContainersClone = TWO_CONTAINERS_ONLY.clone();
        assertEquals("/platform/test", oneContainerClone.appendNameToXPath(TEST));
        assertEquals("/platform/SomeContainer/test", twoContainersClone.appendNameToXPath(TEST));
    }

    @Test
    public void testPathString() {
        ModelNodeId oneContainerClone = ONE_CONTAINER_ONLY.clone();
        ModelNodeId twoContainersClone = TWO_CONTAINERS_ONLY.clone();
        assertEquals("/platform", oneContainerClone.pathString());
        assertEquals("/platform/SomeContainer", twoContainersClone.pathString());
        ModelNodeId emptyContainerClone = EMPTY_CONTAINER.clone();
        assertEquals('/', emptyContainerClone.pathString().charAt(emptyContainerClone.pathString().length() - 1));
    }

    @Test
    public void testEquals() {
        assertTrue(ONE_CONTAINER_ONLY.equals(ANOTHER_ONE_CONTAINER_ONLY));
        assertEquals(ONE_CONTAINER_ONLY, ONE_CONTAINER_ONLY);
        assertFalse(ONE_CONTAINER_ONLY.equals(null));
        assertFalse(ONE_CONTAINER_ONLY.equals(PLATFORM));
        assertFalse(TWO_CONTAINERS_ONLY.equals(PLATFORM));
        assertFalse(EMPTY_CONTAINER.equals(ONE_CONTAINER_ONLY));
        List<ModelNodeRdn> list = null;
        ModelNodeId mnid = new ModelNodeId(list);
        assertFalse(mnid.equals(ONE_CONTAINER_ONLY));
        ModelNodeId othermnid = new ModelNodeId(list);
        assertTrue(mnid.equals(othermnid));
    }

    @Test
    public void testAppendRdns() {
        ModelNodeId OneContainerOnlyClone = ONE_CONTAINER_ONLY.clone();
        int size = OneContainerOnlyClone.getRdns().size();
        OneContainerOnlyClone.appendRdns(ONE_CONTAINER_ONLY.getRdns());
        int expectedSize = size + size;
        assertEquals(expectedSize, OneContainerOnlyClone.getRdns().size());
    }

    @Test
    public void testBeginsWithTemplate() {
        assertFalse(ONE_CONTAINER_ONLY.beginsWithTemplate(TWO_CONTAINERS_ONLY));
        assertTrue(ONE_CONTAINER_ONLY.beginsWithTemplate(ONE_CONTAINER_ONLY));
        assertFalse(LIST_WITH_MULTIPLE_KEYS_ORDER1.beginsWithTemplate(LIST_WITH_MULTIPLE_KEYS_ORDER2));
        ModelNodeId objectWithNamespaceDefault = new ModelNodeId(
                Arrays.asList(new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE, ANV_PLATFORM_ROOT_CONTAINER_NAME)));
        ModelNodeId objectWithNamespaceOther = new ModelNodeId(
                Arrays.asList(new ModelNodeRdn(ModelNodeRdn.CONTAINER, NAMESPACE1, TEST)));
        assertFalse(objectWithNamespaceOther.beginsWithTemplate(objectWithNamespaceDefault));
        ModelNodeId objectWithName = new ModelNodeId(
                Arrays.asList(new ModelNodeRdn(TESTOBJ, ANV_PLATFORM_NAMESPACE, ANV_PLATFORM_ROOT_CONTAINER_NAME)));
        ModelNodeId objectWithNameOther = new ModelNodeId(
                Arrays.asList(new ModelNodeRdn(TESTOBJ, NAMESPACE1, TEST)));
        assertFalse(objectWithName.beginsWithTemplate(objectWithNameOther));
        ModelNodeId objectWithNullNameSpace = new ModelNodeId(
                Arrays.asList(new ModelNodeRdn(TESTOBJ, null, ANV_PLATFORM_ROOT_CONTAINER_NAME)));
        assertFalse(objectWithNameOther.beginsWithTemplate(objectWithNullNameSpace));
        assertTrue(objectWithNullNameSpace.beginsWithTemplate(objectWithNullNameSpace));
    }

    @Test
    public void testMatchesTemplate() {

        assertFalse(ONE_CONTAINER_ONLY.matchesTemplate(TWO_CONTAINERS_ONLY));
        assertTrue(LIST_WITH_MULTIPLE_KEYS_MISSINGKEY1.matchesTemplate(LIST_WITH_MULTIPLE_KEYS_MISSINGKEY1));
    }

    @Test
    public void testAddRdns() {
        int newsize = ONE_CONTAINER_ONLY.getRdns().size();
        EMPTY_CONTAINER.addRdns(ONE_CONTAINER_ONLY.getRdns());
        assertEquals(newsize, EMPTY_CONTAINER.getRdns().size());
    }

    @Test
    public void testGetParentId() {
        ModelNodeId emptyContainer = new ModelNodeId();
        assertEquals(emptyContainer, ONE_CONTAINER_ONLY.getParentId());
        assertEquals(ONE_CONTAINER_ONLY, LIST_WITH_MULTIPLE_KEYS_MISSINGKEY1.getParentId());
    }

    @Test
    public void testCompareTo() {
        ArrayList<ModelNodeId> arrayList = new ArrayList<ModelNodeId>();
        arrayList.add(LIST_WITH_MULTIPLE_KEYS_MISSINGKEY2);
        arrayList.add(LIST_WITH_MULTIPLE_KEYS_MISSINGKEY1);
        arrayList.add(TWO_CONTAINERS_ONLY);
        arrayList.add(ONE_CONTAINER_ONLY);
        Collections.sort(arrayList);
        assertEquals(ONE_CONTAINER_ONLY, arrayList.get(0));
        assertEquals(TWO_CONTAINERS_ONLY, arrayList.get(1));
        assertEquals(LIST_WITH_MULTIPLE_KEYS_MISSINGKEY2, arrayList.get(2));
        assertEquals(LIST_WITH_MULTIPLE_KEYS_MISSINGKEY1, arrayList.get(3));
    }

    @Test
    public void testComparisionForDifferentStrings() {
        int actual = ONE_CONTAINER_ONLY.compareTo(TWO_CONTAINERS_ONLY);
        assertTrue(actual < 0);
    }

    @Test
    public void testComparisionForDifferentStringValues() {
        int actual = LIST_WITH_MULTIPLE_KEYS_MISSINGKEY1.compareTo(LIST_WITH_MULTIPLE_KEYS_MISSINGKEY2);
        assertTrue(actual > 0);
        actual = LIST_WITH_MULTIPLE_KEYS_MISSINGKEY2.compareTo(LIST_WITH_MULTIPLE_KEYS_MISSINGKEY1);
        assertTrue(actual < 0);

    }

    @Test
    public void testComparisionForSameNumbers() {
        int actual = NUMBER_MODEL_NODE_ID1.compareTo(NUMBER_MODEL_NODE_ID1);
        assertTrue(actual == 0);
    }

    @Test
    public void testComparisionForDifferentNumbers() {
        int actual = NUMBER_MODEL_NODE_ID1.compareTo(NUMBER_MODEL_NODE_ID2);
        assertTrue(actual > 0);
    }

    @Test
    public void testComparisionForSameStrings() {
        int actual = ONE_CONTAINER_ONLY.compareTo(ONE_CONTAINER_ONLY);
        assertTrue(actual == 0);

        List<ModelNodeId> nodeList = new ArrayList<>();
        for (int i = 19; i >= 1; i--) {
            ModelNodeId someListNodeId = new ModelNodeId(Arrays.asList(
                    new ModelNodeRdn("container", ANV_NAMESPACE,
                            "device-manager"), new ModelNodeRdn("container",
                            DEVICE_HOLDER_NAMESPACE,
                            "device-holder"), new ModelNodeRdn("name",
                            DEVICE_HOLDER_NAMESPACE, "OLT" + i)));
            nodeList.add(someListNodeId);
        }
        Collections.sort(nodeList);
        for (int i = 0; i < 19; i++) {
            assertTrue(nodeList.get(i).getModelNodeIdAsString().contains("OLT" + (i + 1)));
        }
    }

    @Test
    public void testGetLastRdn() {
        int size = TWO_CONTAINERS_ONLY.getRdns().size() - 1;
        assertEquals(TWO_CONTAINERS_ONLY.getRdns().get(size), TWO_CONTAINERS_ONLY.getLastRdn());
        assertNull(EMPTY_CONTAINER.getLastRdn());
        List<ModelNodeRdn> list = null;
        ModelNodeId mnid = new ModelNodeId(list);
        assertNull(mnid.getLastRdn());
    }

    @Test
    public void testGetFirstRdn() {
        assertNull(EMPTY_CONTAINER.getFirstRdn(ModelNodeRdn.CONTAINER));

        assertEquals(ANV_PLATFORM_ROOT_CONTAINER_NAME, TWO_CONTAINERS_ONLY.getFirstRdn(ModelNodeRdn.CONTAINER).getRdnValue());

        assertEquals(KEYVALUE1, LIST_WITH_SINGLE_KEY.getFirstRdn(KEYNAME1).getRdnValue());
    }

    @Test
    public void testGetNextRdn() {
        ModelNodeRdn rdn1 = new ModelNodeRdn(ModelNodeRdn.CONTAINER, "ns", "level1");
        ModelNodeRdn rdn2 = new ModelNodeRdn(ModelNodeRdn.CONTAINER, "ns", "level2");
        ModelNodeRdn rdn3 = new ModelNodeRdn(ModelNodeRdn.CONTAINER, "ns", "level3");
        ModelNodeRdn rdn4 = new ModelNodeRdn(ModelNodeRdn.CONTAINER, "ns", "level4");
        ModelNodeRdn rdn5 = new ModelNodeRdn(ModelNodeRdn.NAME, "ns", "'key1'");
        ModelNodeRdn rdn6 = new ModelNodeRdn("key2", "ns", "key2");

        ModelNodeId parentId = new ModelNodeId();
        parentId.addRdn(rdn1);
        parentId.addRdn(rdn2);
        ModelNodeId childId = new ModelNodeId();
        childId.addRdn(rdn1).addRdn(rdn2).addRdn(rdn3).addRdn(rdn4);

        ModelNodeRdn rdn = childId.getNextChildRdn(parentId);
        assertEquals(rdn3, rdn);

        ModelNodeId nextId = new ModelNodeId();
        nextId.addRdn(rdn1).addRdn(rdn2).addRdn(rdn3);

        ModelNodeId targetNextId = childId.getNextChildId(parentId);
        assertEquals(nextId, targetNextId);

        ModelNodeId grandChildId = new ModelNodeId();
        grandChildId.addRdn(rdn1).addRdn(rdn2).addRdn(rdn3).addRdn(rdn4).addRdn(rdn5).addRdn(rdn6);

        targetNextId = grandChildId.getNextChildId(childId);
        assertEquals(grandChildId, targetNextId);
        assertEquals("/level1/level2/level3/level4[name='key1'][key2='key2']", targetNextId.xPathString());
    }

    @Test
    public void testGetNextRdnForInvalidKeys() {
        ModelNodeRdn rdn1 = new ModelNodeRdn(ModelNodeRdn.CONTAINER, "ns", "level1");
        ModelNodeRdn rdn2 = new ModelNodeRdn(ModelNodeRdn.CONTAINER, "ns", "level2");
        ModelNodeRdn rdn3 = new ModelNodeRdn(ModelNodeRdn.CONTAINER, "ns", "level3");
        ModelNodeRdn rdn4 = new ModelNodeRdn(ModelNodeRdn.CONTAINER, "ns", "level4");
        ModelNodeRdn rdn5 = new ModelNodeRdn(ModelNodeRdn.NAME, "ns", "key1'");
        ModelNodeRdn rdn6 = new ModelNodeRdn("key2", "ns", "'key2");

        ModelNodeId parentId = new ModelNodeId();
        parentId.addRdn(rdn1);
        parentId.addRdn(rdn2);
        ModelNodeId childId = new ModelNodeId();
        childId.addRdn(rdn1).addRdn(rdn2).addRdn(rdn3).addRdn(rdn4);

        ModelNodeId grandChildId = new ModelNodeId();
        grandChildId.addRdn(rdn1).addRdn(rdn2).addRdn(rdn3).addRdn(rdn4).addRdn(rdn5).addRdn(rdn6);

        ModelNodeId targetNextId = grandChildId.getNextChildId(childId);
        assertEquals(grandChildId, targetNextId);
        assertEquals("/level1/level2/level3/level4[name='key1'][key2='key2']", targetNextId.xPathString());
    }

    @Test
    public void testUrlEncodeDecode() {
        ModelNodeId keyWithSpecialChars = new ModelNodeId(Arrays.asList(
                new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE,
                        ANV_PLATFORM_ROOT_CONTAINER_NAME),
                new ModelNodeRdn(ModelNodeRdn.CONTAINER, ANV_PLATFORM_NAMESPACE, SOMECONTAINER),
                new ModelNodeRdn(KEYNAME1, ANV_PLATFORM_NAMESPACE, KEYVALUE1),
                new ModelNodeRdn(KEYNAME3, ANV_PLATFORM_NAMESPACE, "/funnykey=value><%\\")));
        final String encodedText = keyWithSpecialChars.getModelNodeIdAsString();
        assertEquals("/container=platform/container=SomeContainer/KeyName1=KeyValue1/KeyName3=\\/funnykey\\=value><%\\\\", encodedText);
        assertEquals("/funnykey=value><%\\", keyWithSpecialChars.getLastRdn().getRdnValue());

        ModelNodeId decodedKey = new ModelNodeId(encodedText, ANV_PLATFORM_NAMESPACE);
        assertEquals("/funnykey=value><%\\", decodedKey.getRdnValue(KEYNAME3));
        assertEquals("/container=platform/container=SomeContainer/KeyName1=KeyValue1/KeyName3=\\/funnykey\\=value><%\\\\", decodedKey.getModelNodeIdAsString());
        assertEquals(keyWithSpecialChars, decodedKey);
    }

}
