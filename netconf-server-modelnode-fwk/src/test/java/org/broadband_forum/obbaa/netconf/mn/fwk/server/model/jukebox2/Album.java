package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.Container;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ContainerChild;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ContainerList;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation.AnnotationModelNode;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.ArrayList;
import java.util.List;

@Container(name = "album", namespace = Jukebox.NAMESPACE)
public class Album extends AnnotationModelNode {
    public static final QName QNAME = QName.create("http://example.com/ns/example-jukebox","2014-07-03",  "album");
	private static final long serialVersionUID = 1L;
	private String m_name;
	private int m_year;
	private AlbumImage m_albumImage;
	
	private List<Song> m_songs = new ArrayList<Song>();
    public static final SchemaPath ALBUM_SCHEMA_PATH = new SchemaPathBuilder().withParent(Artist.ARTIST_SCHEMA_PATH)
            .appendQName(QNAME).build();

    public Album(ModelNode parent, ModelNodeId parentNodeId, ModelNodeHelperRegistry helperRegistry, SubSystemRegistry subSystemRegistry,
                 SchemaRegistry schemaRegistry) {
		super(parent, parentNodeId, helperRegistry, subSystemRegistry, schemaRegistry);
	}

	@ConfigAttribute(name = "name", namespace = Jukebox.NAMESPACE, isKey=true)
	public String getName() {
		return m_name;
	}

	public void setName(String name) throws EditException {
	    if("".equals(name)){
	        throw new EditException("Album name cannot be empty.");
	    }
		m_name = name;
	}

	@ConfigAttribute(name = "year", namespace = Jukebox.NAMESPACE)
	public int getYear() {
		return m_year;
	}

	@ContainerChild(name ="albumimage", namespace=Jukebox.NAMESPACE, factoryName = "AbstractModelNodeFactory" )
	public AlbumImage getAlbumImage() {
        return m_albumImage;
    }

    public void setAlbumImage(AlbumImage albumImage) {
        m_albumImage = albumImage;
    }

    public Album setYear(int year) throws EditException {
	    //some way to create a edit-config error
	    if(year < 0){
	        throw new EditException("year cannot be less than zero.");
	    }
		m_year = year;
		return this;
	}

	@ContainerList(name ="song", namespace=Jukebox.NAMESPACE, childClass= Song.class, childFactoryName = "AbstractModelNodeFactory")
	public List<Song> getSongs() {
		return m_songs;
	}

	public Album addSong(String name) {
		Song song = null;
		song = new Song(this, new ModelNodeId(getModelNodeId()), getModelNodeHelperRegistry(), getSubSystemRegistry(), getSchemaRegistry());
		song.setName(name);
		m_songs.add(song);
		return this;
	}

	public int getSongCount() {
		return m_songs.size();
	}

	@Override
	public String toString() {
		return "Album [m_name=" + m_name + "]";
	}

    @Override
    public SchemaPath getModelNodeSchemaPath() {
        return ALBUM_SCHEMA_PATH;
    }
}
