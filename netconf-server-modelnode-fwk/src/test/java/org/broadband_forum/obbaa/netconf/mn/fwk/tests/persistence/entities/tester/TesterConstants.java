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

package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.tester;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class TesterConstants {
    public static final String TESTER_NS = "http://netconfcentral.org/ns/tester";
    public static final String TESTER_LOCAL_NAME = "state-root";
    public static final String TESTER_ACTION_LOCAL_NAME = "action-root";
    public static final String TESTER_REVISION = "2008-07-14";
    public static final SchemaPath STATE_ROOT_SCHEMA_PATH = SchemaPath.create(true, QName.create(TESTER_NS, TESTER_REVISION, TESTER_LOCAL_NAME));
    public static final SchemaPath ROOT_ACTION_SCHEMA_PATH = SchemaPath.create(true, QName.create(TESTER_NS, TESTER_REVISION, TESTER_ACTION_LOCAL_NAME));
    
    public static final SchemaPath STATE_CONTAINER_SCHEMA_PATH = SchemaPath.create(true, QName.create(TESTER_NS, TESTER_REVISION, "state-container"));


}
