package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by keshava on 12/28/15.
 */
public class SchemaPathUtilTest {
    @Test
    public void testFromString(){
        assertEquals(JukeboxConstants.JUKEBOX_SCHEMA_PATH, SchemaPathUtil.fromString(SchemaPathUtil.toString(JukeboxConstants.JUKEBOX_SCHEMA_PATH)));
        assertEquals(JukeboxConstants.SONG_SCHEMA_PATH, SchemaPathUtil.fromString(SchemaPathUtil.toString(JukeboxConstants.SONG_SCHEMA_PATH)));
    }

}
