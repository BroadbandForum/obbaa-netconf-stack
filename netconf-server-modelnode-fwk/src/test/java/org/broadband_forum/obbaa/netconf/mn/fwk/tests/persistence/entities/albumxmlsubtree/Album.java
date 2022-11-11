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

package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.albumxmlsubtree;

import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.AlbumPK;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangOrderByUser;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangVisibilityController;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangXmlSubtree;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Entity(name = "albumsubtree_album")
@Table(name = "albumsubtree_album")
@IdClass(AlbumPK.class)
@YangList(name="album", namespace = JukeboxConstants.JB_NS, revision=JukeboxConstants.JB_REVISION)
public class Album {
	private static final String YEAR = "year";
	private static final String GENRE = "genre";
	public static final String NAME = "name";

	@Id
	@Column(name=NAME)
	@YangListKey(name="name", namespace = JukeboxConstants.JB_NS, revision= JukeboxConstants.JB_REVISION)
	private String name;

	@Column(name=GENRE)
	@YangAttribute(name=GENRE, namespace = JukeboxConstants.JB_NS, revision= JukeboxConstants.JB_REVISION)
	private String genre;
	
	@Column(name=YEAR)
	@YangAttribute(name=YEAR, namespace = JukeboxConstants.JB_NS, revision= JukeboxConstants.JB_REVISION)
	private String year;

	@Id
	@YangParentId
	String parentId;

	@YangSchemaPath
	@Column(length = 1000)
	String schemaPath;

	@YangVisibilityController
	@Column(name = "visibility")
	private boolean visibility = true;

	@Column(length = 100000)
	@YangXmlSubtree
	private String xmlSubtree;
	
	@Column
    @YangOrderByUser
	private Integer insertOrder;
	public static long c_lastSetXmlSubtreeCall = 0L;
	public static String c_lastSetXmlSubtreeStr;

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

	
	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	
	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public boolean getVisibility() {
		return visibility;
	}

	public void setVisibility(boolean visibility) {
		this.visibility = visibility;
	}

	public String getXmlSubtree() {
		return xmlSubtree;
	}

	public void setXmlSubtree(String xmlSubtree) {
		c_lastSetXmlSubtreeCall = System.currentTimeMillis();
		c_lastSetXmlSubtreeStr = xmlSubtree;
		this.xmlSubtree = xmlSubtree;
	}
	
	public int getInsertOrder() {
		return insertOrder;
	}
	
	public void setInsertOrder(Integer insertOrder) {
		this.insertOrder = insertOrder;
	}
}
