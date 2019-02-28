package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.Container;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation.AnnotationModelNode;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

@Container(name = "song", namespace = Jukebox.NAMESPACE)
public class Song extends AnnotationModelNode {
    public static final QName QNAME = QName.create("http://example.com/ns/example-jukebox", "2014-07-03", "song");

    private static final long serialVersionUID = 1L;
    private String m_name;
    private String m_location;
    public static final SchemaPath SONG_SCHEMA_PATH = new SchemaPathBuilder().withParent(Album.ALBUM_SCHEMA_PATH)
            .appendQName(QNAME).build();

    public Song(ModelNode parent, ModelNodeId parentNodeId, ModelNodeHelperRegistry helperRegistry, SubSystemRegistry subSystemRegistry,
                SchemaRegistry schemaRegistry) {
		super(parent, parentNodeId, helperRegistry, subSystemRegistry, schemaRegistry);
    }

	@ConfigAttribute(name = "name", namespace = Jukebox.NAMESPACE, isKey=true)
	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}
	
	@ConfigAttribute(name = "location", namespace = Jukebox.NAMESPACE)
	public String getLocation() {
	    return m_location;
	}
	
	public void setLocation(String location) {
	    m_location = location;
	}

	@Override
	public String toString() {
		return "Song [m_name=" + m_name + ", m_location=" + m_location + "]";
	}

    @Override
    public SchemaPath getModelNodeSchemaPath() {
        return SONG_SCHEMA_PATH;
    }
}
