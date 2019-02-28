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

package org.broadband_forum.obbaa.netconf.persistence.jpa;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.junit.Before;
import org.junit.Test;

import org.broadband_forum.obbaa.netconf.persistence.EMFactory;
import org.broadband_forum.obbaa.netconf.persistence.EntityDataStoreManager;
import org.broadband_forum.obbaa.netconf.persistence.PagingInput;

/**
 * Created by Keshava on 8/11/15.
 */
public class JPAEntityDataStoreManagerTest {

    JPAEntityDataStoreManager m_jpaPersistenceManager = null;
    @Before
    public void setUp(){
    	EMFactory emf = new JPAEntityManagerFactory("pma_test");
        m_jpaPersistenceManager = new JPAEntityDataStoreManager(emf);
        System.out.println("Setup");
    }
    
    @Test
    public void testUniqueKeys(){
        Employee emp1 = new Employee();
        emp1.setEmpId("1A");
        emp1.setName("name1");
        
        Employee emp2 = new Employee();
        emp2.setEmpId("2A");
        emp2.setName("name2");
        
        EntityDataStoreManager manager = m_jpaPersistenceManager;
        manager.beginTransaction();
        manager.create(emp1);
        manager.create(emp2);
        manager.commitTransaction();
        
        manager.beginTransaction();
        HashMap<String,Object> keys = new HashMap();
        keys.put("empId", "1A");
        Employee emp = manager.findByUniqueKeys(Employee.class, keys);
        assertEquals(emp, emp1);
        
        keys.clear();
        keys.put("name", "name2");
        emp = manager.findByUniqueKeys(Employee.class, keys);
        assertEquals(emp, emp2);
        keys.clear();
        keys.put("name", "name2");
        keys.put("empId", "1A");
        emp = manager.findByUniqueKeys(Employee.class, keys);
        assertNull(emp);
        manager.commitTransaction();
        manager.close();
    }

    @Test
    public void testMatchValues(){
        m_jpaPersistenceManager.beginTransaction();

        Person personWhoLikesRed = buildPersonWhoLikesRed();
        m_jpaPersistenceManager.create(personWhoLikesRed);
        Person personWhoLikesGreen = buildPersonWhoLikesGreen();
        m_jpaPersistenceManager.create(personWhoLikesGreen);
        Person personWhoLikesPurple = buildPersonWhoLikesPurple();
        m_jpaPersistenceManager.create(personWhoLikesPurple);

        m_jpaPersistenceManager.commitTransaction();

        m_jpaPersistenceManager.beginTransaction();

        assertEquals(new Integer(3), new Integer(m_jpaPersistenceManager.findAll(Person.class).size()));

        Map<String, List<Object>> matchValues = new HashMap<>();
        List<Object> firstNames = new ArrayList<>();
        firstNames.add("Alice");
        firstNames.add("Bob");
        firstNames.add("Charles");
        matchValues.put("firstName", firstNames);

        Map<String, List<Object>> nonMatchValues = new HashMap<>();
        List<Object> notSoGoodColors = new ArrayList<>();
        notSoGoodColors.add("Purple");
        nonMatchValues.put("favoriteColor", notSoGoodColors);

        /**
         * Find Persons whose name is either "Alice" Or "Bob" Or "Charles" and who don't like Purple.
         */
        List<Person> personsWhoDontLikePurple = m_jpaPersistenceManager.findByMatchAndNotMatchValues(Person.class, matchValues, nonMatchValues);
        List<Person> expectedPersons = new ArrayList<>();
        personWhoLikesRed.setId(1L);
        expectedPersons.add(personWhoLikesRed);
        personWhoLikesGreen.setId(2L);
        expectedPersons.add(personWhoLikesGreen);

        assertEquals(expectedPersons, personsWhoDontLikePurple);
        m_jpaPersistenceManager.commitTransaction();
        m_jpaPersistenceManager.close();
    }
    
    @Test
    public void testLikeValues(){
    	EntityDataStoreManager manager = m_jpaPersistenceManager;
    	manager.beginTransaction();
        Person personWhoLikesOrange = buildPersonWhoLikesOrange();
        m_jpaPersistenceManager.create(personWhoLikesOrange);
        manager.commitTransaction();
        
        Map<String,String> matchValue = new HashMap();
        matchValue.put("firstName", "Al");
    	manager.beginTransaction();
        List<Person> person = manager.findLike(Person.class, matchValue);
        assertEquals(1, person.size());
        assertEquals(personWhoLikesOrange, person.get(0));
        
        //test delete
        manager.deleteLike(Person.class, matchValue);
        person = manager.findLike(Person.class, matchValue);
        manager.commitTransaction();
        assertEquals(0,person.size());
        manager.close();
    }
    
    private List<Person> prepareDatas() {
        Person person1 =  new Person()
        .setFirstName("Vagrant")
        .setPhoneNumber("12345")
        .setFavoriteColor("Orange");
        
        Person person2 =  new Person()
        .setFirstName("Job")
        .setPhoneNumber("6789")
        .setFavoriteColor("Orange");
        
        Person person3 =  new Person()
        .setFirstName("Alice")
        .setPhoneNumber("12345")
        .setFavoriteColor("Blue");
        
        Person person4 =  new Person()
        .setFirstName("Nexus")
        .setPhoneNumber("6789")
        .setFavoriteColor("Orange");
        
        Person person5 =  new Person()
        .setFirstName("Touch")
        .setPhoneNumber("12347")
        .setFavoriteColor("Green");
        
        List<Person> persons = new ArrayList<>();
        persons.add(person1);
        persons.add(person2);
        persons.add(person3);
        persons.add(person4);
        persons.add(person5);
        return persons;
    }
    
    @Test
    public void testLikeValuesWithoutMatchValues(){
        EntityDataStoreManager manager = m_jpaPersistenceManager;
        manager.beginTransaction();
        
        for (Person person : prepareDatas()) {
            m_jpaPersistenceManager.create(person);
        }
        
        manager.commitTransaction();
        
        Map<String,String> likeValues = new HashMap();
        likeValues.put("firstName", "li");
        likeValues.put("phoneNumber", "78");
        
        
        manager.beginTransaction();
        List<Person> person = manager.findLikeWithPagingInput(Person.class, null, likeValues, null);
        manager.commitTransaction();
        assertEquals(3, person.size());
        assertEquals("Job", person.get(0).getFirstName());
        assertEquals("Alice", person.get(1).getFirstName());
        assertEquals("Nexus", person.get(2).getFirstName());
        
        manager.beginTransaction();
        PagingInput input = new PagingInput(0, 2);
        person = manager.findLikeWithPagingInput(Person.class, null, likeValues, input);
        manager.commitTransaction();
        assertEquals(2, person.size());
        assertEquals("Job", person.get(0).getFirstName());
        assertEquals("Alice", person.get(1).getFirstName());
        
        manager.close();
    }
    
    @Test
    public void testLikeValuesWithMatchValues(){
        EntityDataStoreManager manager = m_jpaPersistenceManager;
        manager.beginTransaction();
        
        for (Person person : prepareDatas()) {
            m_jpaPersistenceManager.create(person);
        }
        
        manager.commitTransaction();
        
        Map<String,String> likeValues = new HashMap();
        likeValues.put("firstName", "li");
        
        Map<String,String> matchValues = new HashMap();
        matchValues.put("favoriteColor", "Blue");
        matchValues.put("phoneNumber", "12345");
        
        // (phoneNumber=12345 ) and ( favoriteColor=Blue ) and ( firstName like '%li%' )
        manager.beginTransaction();
        List<Person> person = manager.findLikeWithPagingInput(Person.class, matchValues, likeValues, null);
        manager.commitTransaction();
        assertEquals(1, person.size());
        assertEquals("Alice", person.get(0).getFirstName());
        
        
        // (phoneNumber like '%23%' ) or ( firstName  like '%li%' )
        manager.beginTransaction();
        likeValues = new HashMap();
        likeValues.put("firstName", "li");
        likeValues.put("phoneNumber", "23");
        
        person = manager.findLikeWithPagingInput(Person.class, null, likeValues, null);
        manager.commitTransaction();
        assertEquals(3, person.size());
        assertEquals("Vagrant", person.get(0).getFirstName());
        assertEquals("Alice", person.get(1).getFirstName());
        assertEquals("Touch", person.get(2).getFirstName());
        
        
        // ((phoneNumber = '%23%' ) or ( firstName  like '%li%' )) AND (favoriteColor = Blue)
        manager.beginTransaction();
        likeValues = new HashMap();
        likeValues.put("firstName", "li");
        likeValues.put("phoneNumber", "23");
        
        matchValues = new HashMap();
        matchValues.put("favoriteColor", "Blue");
        
        person = manager.findLikeWithPagingInput(Person.class, matchValues, likeValues, null);
        manager.commitTransaction();
        assertEquals(1, person.size());
        assertEquals("Alice", person.get(0).getFirstName());
        
        manager.close();
    }
    
    @Test
    public void testFindByMatchMultiValues(){
        EntityDataStoreManager manager = m_jpaPersistenceManager;
        manager.beginTransaction();
        
        for (Person person : prepareDatas()) {
            m_jpaPersistenceManager.create(person);
        }
        PagingInput paging = new PagingInput(0, 4);
        manager.commitTransaction();
        
        // ( firstName like '%li%' ) and ( phoneNumber like '%23%' )
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String,Object> nameCondition = new HashMap();
        nameCondition.put("firstName", "li");
        
        Map<String,Object> phoneCondition = new HashMap();
        phoneCondition.put("phoneNumber", "23");
        conditions.add(nameCondition);
        conditions.add(phoneCondition);

        manager.beginTransaction();
        List<Person> person = manager.findByMatchMultiConditions(Person.class, conditions, paging);
        manager.commitTransaction();
        assertEquals(1, person.size());
        assertEquals("Alice", person.get(0).getFirstName());
        
        // ((firstName like '%Alice%' ) or (firstName like '%John%' )) and (phoneNumber like '%23%')
        manager.beginTransaction();
        
        conditions = new ArrayList<>();
        nameCondition = new HashMap();
        List<String> listName = new ArrayList<>();
        listName.add("Alice");
        listName.add("John");
        nameCondition.put("firstName", listName);
        
        phoneCondition = new HashMap();
        phoneCondition.put("phoneNumber", "23");
        
        conditions.add(nameCondition);
        conditions.add(phoneCondition);
        
        person = manager.findByMatchMultiConditions(Person.class, conditions, paging);
        manager.commitTransaction();
        assertEquals(1, person.size());
        assertEquals("Alice", person.get(0).getFirstName());
        
        manager.close();
    }

    @Test
    public void testRollBack(){
        System.out.println("testRollBack");
        Employee emp1 = new Employee();
        emp1.setEmpId("1A");
        emp1.setName("name1");

        Employee emp2 = new Employee();
        emp2.setEmpId("2A");
        emp2.setName("name2");

        EntityDataStoreManager manager = m_jpaPersistenceManager;
        manager.beginTransaction();
        manager.create(emp1);
        manager.commitTransaction();
        manager.beginTransaction();
        manager.create(emp2);
        manager.rollbackTransaction();

        HashMap<String,Object> keys = new HashMap();
        keys.put("empId", "1A");
        manager.beginTransaction();
        Employee emp = manager.findByUniqueKeys(Employee.class, keys);
        assertEquals(emp, emp1);

        keys.clear();
        keys.put("name", "name2");
        emp  = manager.findByUniqueKeys(Employee.class, keys);
        assertNull(emp);
        manager.commitTransaction();
        manager.close();
    }

    
    private Person buildPersonWhoLikesPurple() {
        return new Person()
                .setFirstName("Charles")
                .setPhoneNumber("12345")
                .setFavoriteColor("Purple");
    }


    private Person buildPersonWhoLikesGreen() {
        return new Person()
                .setFirstName("Bob")
                .setPhoneNumber("12346")
                .setFavoriteColor("Green");
    }

    private Person buildPersonWhoLikesRed() {
        return new Person()
                            .setFirstName("Alice")
                            .setPhoneNumber("12347")
                            .setFavoriteColor("Red");
    }

    private Person buildPersonWhoLikesOrange() {
        return new Person()
                            .setFirstName("Alice")
                            .setPhoneNumber("12347")
                            .setFavoriteColor("Orange");
    }

}
