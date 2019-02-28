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

package org.broadband_forum.obbaa.netconf.api.util;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class SchemaPathUtilTest {
    
    public static final String JB_NS = "http://example.com/ns/example-jukebox";
    public static final String JB_REVISION = "2014-07-03";
    public static final String JUKEBOX_LOCAL_NAME = "jukebox";
    public static final SchemaPath JUKEBOX_SCHEMA_PATH = SchemaPath.create(true, QName.create(JB_NS, JB_REVISION, JUKEBOX_LOCAL_NAME));
    
    public static final String LIBRARY_LOCAL_NAME = "library";
    public static final String ARTIST_LOCAL_NAME = "artist";
    public static final String ARTIST_COUNT_LOCAL_NAME = "artist-count";
    public static final String ALBUM_LOCAL_NAME = "album";
    public static final String SONG_LOCAL_NAME = "song";
    public static final QName NAME_QNAME = QName.create(JB_NS, JB_REVISION, "name");
    
    public static final SchemaPath LIBRARY_SCHEMA_PATH = new SchemaPathBuilder().withParent(JUKEBOX_SCHEMA_PATH).appendLocalName(LIBRARY_LOCAL_NAME).build();
    public static final SchemaPath ARTIST_SCHEMA_PATH = new SchemaPathBuilder().withParent(LIBRARY_SCHEMA_PATH).appendLocalName(ARTIST_LOCAL_NAME).build();
    public static final SchemaPath ALBUM_SCHEMA_PATH = new SchemaPathBuilder().withParent(ARTIST_SCHEMA_PATH).appendLocalName(ALBUM_LOCAL_NAME).build();
    public static final SchemaPath ALBUM_NAME_SCHEMA_PATH = new SchemaPathBuilder().withParent(ARTIST_SCHEMA_PATH).appendQName(NAME_QNAME).build();
    public static final SchemaPath SONG_SCHEMA_PATH = new SchemaPathBuilder().withParent(ALBUM_SCHEMA_PATH).appendLocalName(SONG_LOCAL_NAME).build();
    
    
    @Test
    public void testFromString() throws Exception {
        assertEquals(JUKEBOX_SCHEMA_PATH, SchemaPathUtil.fromString(SchemaPathUtil.toString(JUKEBOX_SCHEMA_PATH)));
        assertEquals(SONG_SCHEMA_PATH, SchemaPathUtil.fromString(SchemaPathUtil.toString(SONG_SCHEMA_PATH)));
        
        String pathStringWithoutRevision = JB_NS + ",," + JUKEBOX_LOCAL_NAME;
        SchemaPath pathWithoutRevision = SchemaPath.create(true, QName.create(new URI(JB_NS), (Revision)null, JUKEBOX_LOCAL_NAME));
        assertEquals(pathWithoutRevision, SchemaPathUtil.fromString(pathStringWithoutRevision));
        
        String invalidInput = "%blabla,,jukebox";
        try {
            SchemaPathUtil.fromString(invalidInput);
            Assert.fail("Exception expected");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }
    
    @Test
    public void testToStringNoRev() {
    	String pathNoRev = SchemaPathUtil.toStringNoRev(JUKEBOX_SCHEMA_PATH);
    	assertEquals("http://example.com/ns/example-jukebox,,jukebox,", pathNoRev);
    }

}
