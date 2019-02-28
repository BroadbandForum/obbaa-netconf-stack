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

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangOrderByUser;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keshava on 1/29/16.
 */
@Entity(name = "playlist")
@Table(name = "playlist")
@IdClass(PlayListPK.class)
@YangList(name="playlist", namespace = JukeboxConstants.JB_NS, revision= JukeboxConstants.JB_REVISION)
public class PlayList {
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";

    @Id
    @Column(name=NAME)
    @YangListKey(name="name")
    String name;

    @Id
    @YangParentId
    String parentId;

    @Id
    @Column(name=DESCRIPTION)
    @YangAttribute(name="description")
    String description;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @YangChild
    List<PlayListSong> songs = new ArrayList<>();
    
    @Column
    @YangOrderByUser
    Integer insertOrder;

    @Column(length = 1000)
    @YangSchemaPath
    String schemaPath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PlayListSong> getSongs() {
        return songs;
    }

    public void setSongs(List<PlayListSong> songs) {
        this.songs = songs;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }
    
    public Integer getInsertOrder() {
		return insertOrder;
	}
	
	public void setInsertOrder(Integer insertOrder) {
		this.insertOrder = insertOrder;
	}
}
