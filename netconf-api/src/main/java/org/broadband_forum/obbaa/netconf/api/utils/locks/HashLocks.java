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

import java.util.HashSet;
import java.util.Set;

/**
 * Multi-lock based on Object Hash. "Generics" just to have compile-time safety
 * when multiple instances of this class are in use to protect different
 * contexts.
 *
 */
public class HashLocks<T> {

    // Set of objects on which the locks are currently granted
    private Set<T> m_lockedObjects = new HashSet<T>();

    /**
     * Grants lock if the {@code object} is not already present in locked
     * Objects list.
     *
     * @param object
     *            Object on which the lock is sought
     */
    public void lockOn(T object) {
        synchronized (this) {
            while (m_lockedObjects.contains(object)) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    // Ignoring Interrupt
                }
            }
            m_lockedObjects.add(object);
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
            if (m_lockedObjects.remove(object)) {
                this.notifyAll();
            }
        }
    }
    
	/**
	 * Returns true if the {@code object} is not already present in locked objects
	 * list and acquires a lock on the given object else returns false
	 *
	 * @param object Object on which the lock is sought
	 * @return {@code true} if the lock was acquired and {@code false} otherwise
	 */
	public boolean tryLock(T object) {
		synchronized (this) {
			if (m_lockedObjects.contains(object)) {
				return false;
			} else {
				m_lockedObjects.add(object);
				return true;
			}
		}
	}


}
