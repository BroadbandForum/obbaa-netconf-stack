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

package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.restaurant;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Created by pgorai on 4/6/16.
 */
public class RestaurantConstants {
    public static final String RESTAURANT_NS = "http://example.com/ns/example-restaurant";
    public static final String RESTAURANT_LOCAL_NAME = "restaurant";
    public static final String RESTAURANT_REVISION = "2014-07-03";
    public static final QName MODULE_QNAME = QName.create(RESTAURANT_NS, "dummy", (Revision)null);
    public static final SchemaPath RESTAURANT_SCHEMA_PATH = SchemaPath.create(true, QName.create(RESTAURANT_NS,
            RESTAURANT_REVISION, RESTAURANT_LOCAL_NAME));

}
