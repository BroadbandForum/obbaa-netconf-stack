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

package org.broadband_forum.obbaa.netconf.server.ssh;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A ThreadFactory that creates named pool of threads.
 */
public class NamedThreadFactory implements ThreadFactory {
    private static final String THREAD_NAME = "%s-Thread-%d";

    private final String m_poolName;
    private AtomicInteger m_threadNumber = new AtomicInteger(0);

    public NamedThreadFactory(String poolName) {
        this.m_poolName = poolName;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread t = new Thread(runnable); // NOSONAR
        t.setName(String.format(THREAD_NAME, m_poolName, m_threadNumber.incrementAndGet()));
        return t;
    }
}
