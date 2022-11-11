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

import java.util.List;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.ChangeTreeNode;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class DetectSubSystemViolatorsTest {

    @Test
    public void testViolator() {
        SubSystemRegistry registry = new SubSystemRegistryImpl();

        registry.register("ut", SchemaPath.ROOT, new PreCommitPostCommitNotImplemented());
        registry.register("ut",SchemaPath.ROOT, new PreCommitNotImplemented());
        registry.register("ut",SchemaPath.ROOT, new PostCommitNotImplemented());
        registry.register("ut",SchemaPath.ROOT, new PreCommitPostCommitImplemented());
        registry.register("ut",SchemaPath.ROOT, new DefaultNoImpl());

    }

    public class PreCommitPostCommitNotImplemented extends AbstractSubSystem {
        @Override
        protected boolean byPassAuthorization() {
            return true;
        }

        @Override
        public void notifyChanged(List<ChangeNotification> changeNotificationList) {
            super.notifyChanged(changeNotificationList);
        }

        @Override
        public void notifyPreCommitChange(List<ChangeNotification> changeNotificationList) throws SubSystemValidationException {
            super.notifyPreCommitChange(changeNotificationList);
        }
    }

    private class PreCommitNotImplemented extends AbstractSubSystem {
        @Override
        protected boolean byPassAuthorization() {
            return true;
        }

        @Override
        public void notifyChanged(List<ChangeNotification> changeNotificationList) {
            super.notifyChanged(changeNotificationList);
        }

        @Override
        public void postCommit(Map<SchemaPath, List<ChangeTreeNode>> changesMap) {
            super.postCommit(changesMap);
        }

        @Override
        public void notifyPreCommitChange(List<ChangeNotification> changeNotificationList) throws SubSystemValidationException {
            super.notifyPreCommitChange(changeNotificationList);
        }
    }

    private class PostCommitNotImplemented extends AbstractSubSystem {
        @Override
        protected boolean byPassAuthorization() {
            return true;
        }

        @Override
        public void notifyChanged(List<ChangeNotification> changeNotificationList) {
            super.notifyChanged(changeNotificationList);
        }

        @Override
        public void notifyPreCommitChange(List<ChangeNotification> changeNotificationList) throws SubSystemValidationException {
            super.notifyPreCommitChange(changeNotificationList);
        }

        @Override
        public void preCommit(Map<SchemaPath, List<ChangeTreeNode>> changesMap) throws SubSystemValidationException {
            super.preCommit(changesMap);
        }
    }

    private class PreCommitPostCommitImplemented extends AbstractSubSystem {
        @Override
        protected boolean byPassAuthorization() {
            return true;
        }

        @Override
        public void notifyChanged(List<ChangeNotification> changeNotificationList) {
            super.notifyChanged(changeNotificationList);
        }

        @Override
        public void notifyPreCommitChange(List<ChangeNotification> changeNotificationList) throws SubSystemValidationException {
            super.notifyPreCommitChange(changeNotificationList);
        }

        @Override
        public void preCommit(Map<SchemaPath, List<ChangeTreeNode>> changesMap) throws SubSystemValidationException {
            super.preCommit(changesMap);
        }

        @Override
        public void postCommit(Map<SchemaPath, List<ChangeTreeNode>> changesMap) {
            super.postCommit(changesMap);
        }
    }

    private class DefaultNoImpl extends AbstractSubSystem {
        @Override
        protected boolean byPassAuthorization() {
            return true;
        }

    }

}
