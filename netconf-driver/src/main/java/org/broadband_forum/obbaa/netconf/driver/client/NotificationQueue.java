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

package org.broadband_forum.obbaa.netconf.driver.client;

import org.broadband_forum.obbaa.netconf.api.client.NotificationListener;
import org.broadband_forum.obbaa.netconf.api.messages.Notification;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class as a buffer to provides the methods to handle the received notifications from netconf server 
 * Created by nhtoan on 3/9/16.
 */
public class NotificationQueue implements NotificationListener {

    private Queue<Notification> m_notificationQueue;
    private static final Logger LOGGER = Logger.getLogger(NotificationQueue.class);

    public NotificationQueue() {
        this.m_notificationQueue = new ConcurrentLinkedQueue<>();
    }

    public void notificationReceived(Notification notification) {
        m_notificationQueue.add(notification);
        LOGGER.info("Added a notification into queue: " + notification.notificationToString());
    }

    /**
     * Retrieves and removes the head notification of this queue,
     * 
     * @return the retrieved notification as a string
     */
    public String retrieveNotification() {
        Notification pollNotification = m_notificationQueue.poll();
        if (null == pollNotification) {
            return "";
        }
        return pollNotification.notificationToString();
    }

    public List<String> retrieveAllNotification() {
        List<String> notifications = new ArrayList<>();
        Iterator<Notification> notificationIterator = m_notificationQueue.iterator();
        while (notificationIterator.hasNext()) {
            notifications.add(notificationIterator.next().notificationToString());
        }
        return notifications;
    }

    public void clearAllNotifications() {
        m_notificationQueue.clear();
    }

    public boolean isQueueEmpty() {
        return m_notificationQueue.isEmpty();
    }
}
