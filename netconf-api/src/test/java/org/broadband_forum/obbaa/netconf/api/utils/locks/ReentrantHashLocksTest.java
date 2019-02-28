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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

public class ReentrantHashLocksTest extends TestCase {
	 
    /**
     * Checks whether lock & unlock runs smoothly without any exception
     * @throws InterruptedException 
     */
	public void testSingleLockUnlock() throws InterruptedException {
		ReentrantHashLocks<Object> locks = new ReentrantHashLocks<Object>();
		locks.lockOn(this);
		locks.unlock(this);
	}

    /**
     * Checks whether two locks can be acquired based on two different objects
     * @throws InterruptedException 
     */
	public void testMultipleDifferentLocks() throws InterruptedException {
		final ReentrantHashLocks<String> locks = new ReentrantHashLocks<String>();
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicInteger i = new AtomicInteger();
		locks.lockOn("1");
		i.incrementAndGet();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					locks.lockOn("2");
				} catch (InterruptedException e) {
					
				}
				i.incrementAndGet();
				latch.countDown();
				locks.unlock("2");
			}
		}).start();
		try {
			latch.await();
		} catch (InterruptedException e) {
		}
		try {
			assertEquals(2, i.get());
		} finally {
			locks.unlock("1");
		}
	}

    /**
     * Checks whether only one lock can be acquired at a time based on one
     * object - Using two threads/requests.
     */
	public void testMultipleSameLocks() {
		final String lockOn = "lockString";
		final ReentrantHashLocks<String> locks = new ReentrantHashLocks<String>();
		final CountDownLatch startTest = new CountDownLatch(1);
		final CountDownLatch assertValue = new CountDownLatch(2);
		final AtomicBoolean criticalArea = new AtomicBoolean(false);
		final AtomicInteger i = new AtomicInteger(0);
		Runnable testAndSetValue = new Runnable() {
			@Override
			public void run() {
				boolean resetCA = false;
				try {
					startTest.await(); // Wait to start testing
				} catch (InterruptedException e) {
				}
				try {
					locks.lockOn(lockOn);
					if (Math.random() < 0.5) {
						try {
							Thread.sleep(500); // Just some breathing time to
							// test in case if the lock
							// fails
						} catch (InterruptedException e) {
						}
					}
					if (criticalArea.get() == false) {
						criticalArea.set(true);
						resetCA = true;
						i.incrementAndGet();
					}
				} catch (InterruptedException e1) {
					
				} finally {
					if (resetCA)
						criticalArea.set(false);
					locks.unlock(lockOn);
					assertValue.countDown();
				}
			}
		};
		new Thread(testAndSetValue).start();
		new Thread(testAndSetValue).start();
		startTest.countDown();
		try {
			assertValue.await();
		} catch (InterruptedException e) {
		}
		assertEquals(2, i.get());
	}

    /**
     * Checks whether a lock on an object can be obtained, when there is a lock
     * request waiting based on different object
     * @throws InterruptedException 
     */
	public void testMultipleLocksWhileOnWait() throws InterruptedException {
		final ReentrantHashLocks<String> locks = new ReentrantHashLocks<String>();
		final CountDownLatch startTest = new CountDownLatch(1);
		final AtomicInteger i = new AtomicInteger();
		final String ONE = "1";
		final String TWO = "2";
		locks.lockOn(ONE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					startTest.countDown();
					locks.lockOn(ONE); // Just to have a waiting lock request
					i.set(1);
				} catch (InterruptedException e) {
				
				} finally {
					locks.unlock(ONE);
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
		try {
			locks.lockOn(TWO);
			assertEquals(0, i.get());
		} finally {
			locks.unlock(TWO);
		}
		locks.unlock(ONE);
	}
	/**
     * Checks whether same lock can be acquired at a time based on one
     * object - Using same threads/ multiple requests.
     */
	public void testReentrantLocks() {
		final String lockOn = "lockString";
		final ReentrantHashLocks<String> locks = new ReentrantHashLocks<String>();
		final CountDownLatch startTest = new CountDownLatch(1);
		final CountDownLatch assertValue = new CountDownLatch(2);
		final AtomicBoolean criticalArea = new AtomicBoolean(false);
		final AtomicInteger i = new AtomicInteger(0);
		final AtomicInteger j = new AtomicInteger(0);
		final AtomicBoolean secondCriticalArea = new AtomicBoolean(false);
		Runnable testAndSetValue = new Runnable() {
			@Override
			public void run() {
				try {
					startTest.await(); // Wait to start testing
				} catch (InterruptedException e) {
				}
				try {
					locks.lockOn(lockOn);
					if (Math.random() < 0.5) {
						try {
							Thread.sleep(500); // Just some breathing time to
							// test in case if the lock
							// fails
						} catch (InterruptedException e) {
						}
					}
					if (criticalArea.get() == false) {
						criticalArea.set(true);
						i.incrementAndGet();
					}
					if (secondCriticalArea.get() == false) {
						locks.lockOn(lockOn);
						secondCriticalArea.set(true);
						i.incrementAndGet();
						j.incrementAndGet();
					}
				} catch (InterruptedException e1) {
				
				} finally {
					if (criticalArea.get() == true){
						criticalArea.set(false);
						locks.unlock(lockOn);
					}
					if (secondCriticalArea.get() == true){
						secondCriticalArea.set(false);
						locks.unlock(lockOn);
					}
					assertValue.countDown();
				}
			}
		};
		new Thread(testAndSetValue).start();
		new Thread(testAndSetValue).start();
		startTest.countDown();
		try {
			assertValue.await();
		} catch (InterruptedException e) {
		}
		assertEquals(4, i.get());
		assertEquals(2, j.get());
	}
}