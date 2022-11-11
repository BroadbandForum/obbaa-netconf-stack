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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.WritableChangeTreeNode;

public class NotificationContext {

    private WritableChangeTreeNode m_aggregatorCTN;
    List<NotificationInfo> m_notifInfos = new ArrayList<NotificationInfo>();

    public NotificationContext(NotificationContext notificationContext) {
        m_notifInfos = new ArrayList<>(notificationContext.getNotificationInfos());
    }

    public NotificationContext() {
    }

    //Added specific to OBBAA
    public void appendNotificationInfo(NotificationInfo command) {
        //add on top of stack
        //m_notifInfos.add(0, command);
        //FIXME:FNMS-10115 ideally we want the notification to be on top. Since we
        //are building the command recursively.
        //but add on top of list is expensive as list grows.
        //reversing the list while retriving is cheaper than adding on top
        m_notifInfos.add(command);
    }

    //Added specific to OBBAA
    public List<NotificationInfo> getNotificationInfos() {

        //return Collections.emptyList(); commented for OBBAA to work
        List<NotificationInfo> returnList = new ArrayList<NotificationInfo>(m_notifInfos);
        Collections.reverse(returnList);
        return returnList;
    }

    @Override
    public String toString() {
        return "NotificationContext [m_aggregatorCTN=" + m_aggregatorCTN.print() + "]";
    }

    public void addChangeTreeNode(WritableChangeTreeNode aggregatorCTN) {
        m_aggregatorCTN = aggregatorCTN;
    }

    public WritableChangeTreeNode getChangeTree() {
        return m_aggregatorCTN;
    }
}
