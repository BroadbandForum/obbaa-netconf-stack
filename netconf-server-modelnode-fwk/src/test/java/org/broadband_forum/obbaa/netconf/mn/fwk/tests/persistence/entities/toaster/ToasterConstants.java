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

package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.toaster;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Created by pgorai on 4/4/16.
 */
public class ToasterConstants {
    public static final String TOASTER_NS = "http://netconfcentral.org/ns/toaster";
    public static final String TOASTER_LOCAL_NAME = "toaster";
    public static final String TOASTER_REVISION = "2009-11-20";
    public static final SchemaPath TOASTER_SCHEMA_PATH = SchemaPath.create(true, QName.create(TOASTER_NS,
            TOASTER_REVISION, TOASTER_LOCAL_NAME));
}
