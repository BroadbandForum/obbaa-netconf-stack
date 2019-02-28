package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.artistsubtreewithconfigattr;

import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.ArtistPK;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangXmlSubtree;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity(name = "artistsubtreewithconfigattr_artist")
@Table(name = "artistsubtreewithconfigattr_artist")
@IdClass(ArtistPK.class)
@YangList(name="artist", namespace = JukeboxConstants.JB_NS, revision= JukeboxConstants.JB_REVISION)
public class Artist {
	public static final String NAME = "name";

	@Id
	@Column(name=NAME)
	@YangListKey(name="name", namespace = JukeboxConstants.JB_NS, revision= JukeboxConstants.JB_REVISION)
	private String name;

	@Id
	@YangParentId
	String parentId;

	@YangSchemaPath
	@Column(length = 1000)
	String schemaPath;

	@Column(length = 100000)
	@YangXmlSubtree
	private String xmlSubtree;

	public String getXmlSubtree() {
		return xmlSubtree;
	}

	public void setXmlSubtree(String xmlSubtree) {
		this.xmlSubtree = xmlSubtree;
	}

	public String getSchemaPath() {
		return schemaPath;
	}

	public void setSchemaPath(String schemaPath) {
		this.schemaPath = schemaPath;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
