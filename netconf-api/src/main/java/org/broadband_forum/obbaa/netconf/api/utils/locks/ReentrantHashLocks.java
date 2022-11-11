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

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;

public class ReentrantHashLocks<T> {

	private ConcurrentHashMap<T, ReentrantLockWrapper> m_lockedObjects = new ConcurrentHashMap<T, ReentrantLockWrapper>();

	private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ReentrantHashLocks.class,
			LogAppNames.NETCONF_LIB);

	
	protected static class ReentrantLockWrapper {
		protected ReentrantLock lock = new ReentrantLock();
		protected int accessCount = 0;
	}
	
	public <RT> RT executeWithLock(T object, Callable<RT> protectedArea) throws Exception {
		boolean locked = false;
		try {
			lockOn(object);
			locked = true;
			return protectedArea.call();
		} finally {
			if (locked) {
				unlock(object);
			}
		}
	}

	private void lockOn(T object) {
		if (null != object) {
			ReentrantLockWrapper lockObject;
			synchronized (object) {
				lockObject = m_lockedObjects.computeIfAbsent(object, value -> new ReentrantLockWrapper());
				lockObject.accessCount++;
			}
			try {
				lockObject.lock.lock();
			} catch (Exception e) {
				lockObject.accessCount--;
				throw e;
			}
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Thread {} acquired  lockObject {}  with accessCount {} and  lockHash {}   ",
						Thread.currentThread().getName(), object.toString(), lockObject.accessCount,
						lockObject.lock.hashCode());
			}
		}
	}

	private void unlock(T object) {
		if (null != object) {
			synchronized (object) {
				ReentrantLockWrapper reentrantLockWrapper = m_lockedObjects.get(object);
				if (null != reentrantLockWrapper) {
					boolean hasQueuedThreadsBeforeUnlock = reentrantLockWrapper.lock.hasQueuedThreads();
					reentrantLockWrapper.lock.unlock();
					reentrantLockWrapper.accessCount--;
					if (!hasQueuedThreadsBeforeUnlock && reentrantLockWrapper.accessCount <=0 && reentrantLockWrapper.lock.getHoldCount() == 0 && !reentrantLockWrapper.lock.isLocked()) {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug(
									"Remove lock object hasQueuedThreads: {} , islocked: {} , holdCount: {} , accessCount {} ,ThreadName: {} , lockHash {}, lockObject {} ",
									hasQueuedThreadsBeforeUnlock, reentrantLockWrapper.lock.isLocked(),
									reentrantLockWrapper.lock.getHoldCount(), reentrantLockWrapper.accessCount,
									Thread.currentThread().getName(), reentrantLockWrapper.lock.hashCode(),object.toString());
						}
						m_lockedObjects.remove(object);
					}
				}
			}
		}
	}

}
