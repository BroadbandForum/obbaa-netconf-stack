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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.hibernate.jpa.HibernatePersistenceProvider;

import org.broadband_forum.obbaa.netconf.persistence.EMFactory;

import java.util.Map;

@Deprecated
public class JPAEntityManagerFactory implements EMFactory{
	private EntityManagerFactory m_factory;
	
	public JPAEntityManagerFactory(){
		
	}
	
	public JPAEntityManagerFactory(String persistenceUnitName, Map<String, String> properties) {
	    initEntityMgr(persistenceUnitName, properties);
	}
	
	public JPAEntityManagerFactory(String dbName){
		initEntityMgr(dbName, null);
	}

	private void initEntityMgr(String dbName, Map<String, String> properties) {
	    //Persistence.createEntityManagerFacotry creates deprecated HibernatePersistence.
	    //So, instead using HibernatePersistenceProvider
	    HibernatePersistenceProvider persistenceProvider = new HibernatePersistenceProvider();
		m_factory = persistenceProvider.createEntityManagerFactory(dbName, properties);
	}
	
	@Override
	public EntityManager createNewEntityManager() {
		return m_factory.createEntityManager();
	}

}
