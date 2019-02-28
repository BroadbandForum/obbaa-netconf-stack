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

package org.broadband_forum.obbaa.netconf.api.utils.locks;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.broadband_forum.obbaa.netconf.api.util.Pair;

public class ReentrantHashLocks<T> {
		
	// Set of objects on which the locks are currently granted
	private Map<T,Pair<Long, Integer>> m_lockedObjects = new HashMap<T,Pair<Long, Integer>>();
	
	 private static final Logger LOGGER = Logger.getLogger(ReentrantHashLocks.class);

	/**
	 * Grants lock 
	 * 		if the {@code object} is not already present in locked  Objects list.
	 *		if the {@code object} is already present in locked  Objects list and currently owned by same thread (reentrant request)
	 * @param object
	 *            Object on which the lock is sought
	 * @throws InterruptedException 
	 */
	 public void lockOn(T object) throws InterruptedException {
		 synchronized (this) {
			 if(null != object){
				 Long id=Thread.currentThread().getId();
				 while (m_lockedObjects.containsKey(object)) {
					 Pair<Long, Integer> lockedThread=m_lockedObjects.get(object);
					 if(lockedThread.getFirst().equals(id)){
						 lockedThread.setSecond(lockedThread.getSecond()+1);
						 return;
					 }
					 try {
						 this.wait();
					 } catch (InterruptedException e) {
						 LOGGER.error("Thread got interuppted while waiting for Lock "+id, e);
						 throw e;
					 }
				 }

				 Pair<Long, Integer> newOwnedThread=new Pair<Long, Integer>(id, 1);
				 m_lockedObjects.put(object,newOwnedThread);
			 }
		 }
	 }

	/**
	 * Revokes the lock, if it was granted in past.
	 *
	 * @param object
	 *            Object on which the lock was acquired.
	 */
	 public void unlock(T object) {
		 synchronized (this) {
			 if(null != object){
				 if (m_lockedObjects.containsKey(object)) {
					 Long id=Thread.currentThread().getId();
					 Pair<Long, Integer> lockedThread=m_lockedObjects.get(object);
					 if(lockedThread.getFirst().equals(id)){
						 Integer lockCount=lockedThread.getSecond();
						 lockCount--;
						 if(lockCount==0){
							 m_lockedObjects.remove(object);
							 this.notifyAll();
						 }else{
							 lockedThread.setSecond(lockCount);
							 return;
						 }
					 }else{
						 throw new IllegalMonitorStateException();
					 }
				 }
			 }
		 }
	 }
}

