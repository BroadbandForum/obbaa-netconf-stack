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

package org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangLeafList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangOrderByUser;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

@Entity(name = "song")
@Table(name = "song")
@IdClass(SongPK.class)
@YangList(name = "song")
public class Song {
	public static final String NAME = "name";
	
	@Id 
	@Column(name=NAME)
	@YangListKey(name="name", namespace = JukeboxConstants.JB_NS, revision= JukeboxConstants.JB_REVISION)
	private String name;

	@Column(name="location")
	@YangAttribute(name="location", namespace = JukeboxConstants.JB_NS, revision= JukeboxConstants.JB_REVISION)
	private String location;
	
	@Column(name="format")
	@YangAttribute(name="format", namespace = JukeboxConstants.JB_NS, revision= JukeboxConstants.JB_REVISION)
	private String format;
	
	@Column(name="length")
	@YangAttribute(name="length", namespace = JukeboxConstants.JB_NS, revision= JukeboxConstants.JB_REVISION)
	private String length;
	
	@Column
	@YangOrderByUser
	private Integer insertOrder;

	@Id
	@YangParentId
	String parentId;

	@YangSchemaPath
	@Column(length = 1000)
	String schemaPath;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@YangLeafList(name="singer", namespace = JukeboxConstants.JB_NS, revision= JukeboxConstants.JB_REVISION)
	private Set<Singer> singers = new LinkedHashSet<>();

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
	
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}
	
	public Set<Singer> getSingers() {
		return singers;
	}

	public void setSingers(Set<Singer> singers) {
		this.singers = singers;
	}

	public void addSingers(Singer singer) {
		singers.add(singer);
	}
	
	public int getInsertOrder() {
		return insertOrder;
	}
	
	public void setInsertOrder(Integer insertOrder) {
		this.insertOrder = insertOrder;
	}
}
