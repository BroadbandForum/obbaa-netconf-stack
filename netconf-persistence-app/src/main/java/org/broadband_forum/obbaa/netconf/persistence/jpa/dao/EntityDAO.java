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

package org.broadband_forum.obbaa.netconf.persistence.jpa.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.persistence.PagingInput;

public interface EntityDAO<E, PK extends Serializable> {
	
	@Deprecated
	/**
	 * Use findByWithWriteLock() or findByWithReadLock()
	 * @param primaryKey
	 * @return
	 */
	E findById(PK primaryKey);
	
	boolean merge(E entity);

    E findByIdWithWriteLock(PK primaryKey);
    
    E findByIdWithReadLock(PK primaryKey);

    public List<E> findAll();

    public void create(E entity);

    public void delete(E entity);
    
    public int deleteAll();
    
    boolean deleteById(PK id);
    
    List<E> findWithPaging(PagingInput pagingInput);
    
    public List<E> findWithPagingAndOrderByColumn(PagingInput pagingInput, Map<String, Object> matchValues,
            String orderByColumn, Boolean isDesc);
}
