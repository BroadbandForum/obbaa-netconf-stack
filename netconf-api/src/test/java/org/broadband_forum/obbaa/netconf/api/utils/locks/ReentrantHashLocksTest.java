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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;

import junit.framework.TestCase;

public class ReentrantHashLocksTest extends TestCase {
	 
    /**
     * Checks whether lock & unlock runs smoothly without any exception
     * @throws Exception 
     */
	public void testSingleLockUnlock() throws Exception {
		ReentrantHashLocks<Object> locks = new ReentrantHashLocks<Object>();
		lockObject(locks,this);
	}

	private void lockObject(ReentrantHashLocks<Object> locks, Object object) throws Exception {
		locks.executeWithLock(object, new Callable<String>() {
			@Override
			public String call() throws Exception {
				return "localString";
			}
		});
	}
	
	private void lockStringObject(ReentrantHashLocks<String> locks, String object) throws Exception {
		locks.executeWithLock(object, new Callable<String>() {
			@Override
			public String call() throws Exception {
				return "localString";
			}
		});
	}

    /**
     * Checks whether two locks can be acquired based on two different objects
     * @throws Exception 
     */
	public void testMultipleDifferentLocks() throws Exception {
		final ReentrantHashLocks<String> locks = new ReentrantHashLocks<String>();
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicInteger i = new AtomicInteger();
		lockStringObject(locks,"1");
		i.incrementAndGet();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					lockStringObject(locks,"2");
				} catch (Exception e) {
					e.printStackTrace();
				}
				i.incrementAndGet();
				latch.countDown();
			}
		}).start();
		try {
			latch.await();
		} catch (InterruptedException e) {
		}
        assertEquals(2, i.get());
		
	}


    /**
     * Checks whether a lock on an object can be obtained, when there is a lock
     * request waiting based on different object
     * @throws Exception 
     */
	public void testMultipleLocksWhileOnWait() throws Exception {
		final ReentrantHashLocks<String> locks = new ReentrantHashLocks<String>();
		final CountDownLatch startTest = new CountDownLatch(1);
		final AtomicInteger atmoicInteger = new AtomicInteger();
		final String ONE = "1";
		final String TWO = "2";
		lockStringObject(locks,ONE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					startTest.countDown();
					lockStringObject(locks,ONE); // Just to have a waiting lock request
					atmoicInteger.set(1);
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		}).start();
		try {
			startTest.await();
		} catch (InterruptedException e) {
		}
		try {
			Thread.sleep(500); // Breathing time for the lock request to get
			// added on to Q
		} catch (InterruptedException e) {
		}
		
		 assertEquals(1, atmoicInteger.get());

	}
	
	
	
	
}