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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox4;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.emn.XmlSubtreeDSMLeafListTest.NS;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangLeafList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangXmlSubtree;

@Entity(name = "song2")
@Table(name = "song2")
@IdClass(SongPK.class)
@YangList(name = "song", namespace = NS, revision= JukeboxConstants.JB_REVISION)
public class Song {

    public static final String NAME = "name";
    public static final String AWARDS = "awards";
    public static final String VERSION = "version";


    @Id
    @Column(name=NAME)
    @YangListKey(name=NAME, namespace = NS, revision= JukeboxConstants.JB_REVISION)
    private String name;

    @Id
    @Column(name=VERSION)
    @YangListKey(name=VERSION, namespace = NS, revision= JukeboxConstants.JB_REVISION)
    private String version;

    @Id
    @YangParentId
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @YangLeafList(name="singer", namespace = NS, revision= JukeboxConstants.JB_REVISION)
    private Set<Singer> singers = new LinkedHashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @YangLeafList(name="genre", namespace = NS, revision= JukeboxConstants.JB_REVISION)
    private Set<Genre> genres = new LinkedHashSet<>();

    @YangXmlSubtree(namespace = NS, revision= JukeboxConstants.JB_REVISION)
    @Column(name = AWARDS)
    @Lob
    private String awards;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Set<Singer> getSingers() {
        return singers;
    }

    public void setSingers(Set<Singer> singers) {
        this.singers = singers;
    }

    public void addSinger(Singer singer) {
        singers.add(singer);
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setAwards(String subtree) {
        this.awards = subtree;
    }

    public String getAwards() {
        return awards;
    }

    public Set<Genre> getGenres() {
        return genres;
    }

    public void setGenres(Set<Genre> genres) {
        this.genres = genres;
    }

    public void addGenre(Genre genre) {
        genres.add(genre);
    }
}
