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

package org.broadband_forum.obbaa.netconf.stack;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultNcNotificationCounterService implements NcNotificationCounterService {
    private ConcurrentHashMap<Integer, AtomicLong> m_outNotificationsMap = new ConcurrentHashMap<>();
    private Map<String,AtomicLong> m_numberOfNotificationsForUsers = new HashMap<>();

    @Override
    public long getNumberOfNotifications() {
        long result = 0l;
        for(AtomicLong count :m_numberOfNotificationsForUsers.values()) {
            result+=count.get();
        }
        return  result;
    }

    public Map<String,AtomicLong> getNumberOfNotificationsMapForUsers() {
          return  m_numberOfNotificationsForUsers;
    }

    @Override
    public void increaseNumberOfNotificationsForUsers(String userName) {
        if(m_numberOfNotificationsForUsers.get(userName) == null) {
            m_numberOfNotificationsForUsers.put(userName, new AtomicLong(0));
        }
        m_numberOfNotificationsForUsers.get(userName).incrementAndGet();
    }

    @Override
    public long getOutNotifications(Integer sessionId) {
        ensureEntryExistsForSession(sessionId);
        return m_outNotificationsMap.get(sessionId).get();
    }

    private void ensureEntryExistsForSession(Integer sessionId) {
        if (!m_outNotificationsMap.containsKey(sessionId)) {
            m_outNotificationsMap.putIfAbsent(sessionId, new AtomicLong(0));
        }
    }

    @Override
    public void increaseOutNotifications(Integer sessionId) {
        ensureEntryExistsForSession(sessionId);
        m_outNotificationsMap.get(sessionId).incrementAndGet();
    }
}
