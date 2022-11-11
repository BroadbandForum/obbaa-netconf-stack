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

package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.billboard;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class BillboardConstants {
    public static final String BB_NS = "http://example.com/ns/example-jukebox";

    public static final String BB_LOCAL_NAME = "billboard";
    
    public static final String ARWARD_CHOICE_LOCAL_NAME = "arward-type";
    
    public static final String SINGLE_CASE_LOCAL_NAME = "single-case";
    
    public static final String SONG_LOCAL_NAME = "song";

    public static final String BB_REVISION = "2014-07-03";

    public static final QName BB_QNAME = QName.create(BB_NS, BB_REVISION, BB_LOCAL_NAME);

    public static final SchemaPath BB_SCHEMA_PATH = SchemaPath.create(true, BB_QNAME);
}
