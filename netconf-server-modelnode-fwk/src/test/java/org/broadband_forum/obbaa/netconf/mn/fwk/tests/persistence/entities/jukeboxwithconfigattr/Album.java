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

package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.jukeboxwithconfigattr;

import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.AlbumPK;
import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.AttributeType;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttributeNS;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangLeafList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity(name = "jukeboxwithconfigattr_album")
@Table(name = "jukeboxwithconfigattr_album")
@IdClass(AlbumPK.class)
@YangList(name = "album", namespace = JukeboxConstants.JB_NS, revision = JukeboxConstants.JB_REVISION)
public class Album {
    private static final String YEAR = "year";
    private static final String GENRE = "genre";
    public static final String NAME = "name";
    public static final String LABEL = "label";
    public static final String RESOURCE = "resource";

    @Id
    @Column(name = NAME)
    @YangListKey(name = NAME, namespace = JukeboxConstants.JB_NS, revision = JukeboxConstants.JB_REVISION)
    private String name;

    @Column(name = YEAR)
    @YangAttribute(name = YEAR, namespace = JukeboxConstants.JB_NS, revision = JukeboxConstants.JB_REVISION)
    private String year;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @YangLeafList(name = "singer")
    private Set<Singer> singers = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @YangLeafList(name = "dummy-leaflist-id-ref")
    private Set<DummyLeaflistIdRef> dummyLeaflistIdRef = new HashSet<>();

    @Column(name = LABEL)
    @YangAttribute(name = LABEL, namespace = JukeboxConstants.JB_NS, revision = JukeboxConstants.JB_REVISION,
            attributeType = AttributeType.IDENTITY_REF_CONFIG_ATTRIBUTE)
    private String label;

    @Column(name = "labelNS")
    @YangAttributeNS(belongsToAttribute = LABEL, attributeNamespace = JukeboxConstants.JB_NS,
            attributeRevision = JukeboxConstants.JB_REVISION)
    private String labelNS;

    @Column(name = GENRE)
    @YangAttribute(name = GENRE, namespace = JukeboxConstants.JB_NS, revision = JukeboxConstants.JB_REVISION,
            attributeType = AttributeType.IDENTITY_REF_CONFIG_ATTRIBUTE)
    private String genre;

    @Column(name = "genreNS")
    @YangAttributeNS(belongsToAttribute = GENRE, attributeNamespace = JukeboxConstants.JB_NS,
            attributeRevision = JukeboxConstants.JB_REVISION)
    private String genreNS;

    @Id
    @YangParentId
    String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    String schemaPath;

    @Column(name = RESOURCE)
    @YangAttribute(name = RESOURCE, attributeType = AttributeType.INSTANCE_IDENTIFIER_CONFIG_ATTRIBUTE)
    private String resource;

    @Column(name = "resourceNS")
    @YangAttributeNS(belongsToAttribute = RESOURCE, attributeNamespace = JukeboxConstants.JB_NS, attributeRevision =
            JukeboxConstants.JB_REVISION)
    private String resourceNS;

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

    public String getLabelNS() {
        return labelNS;
    }

    public void setLabelNS(String labelNS) {
        this.labelNS = labelNS;
    }

    public String getGenreNS() {
        return genreNS;
    }

    public void setGenreNS(String genreNS) {
        this.genreNS = genreNS;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getResourceNS() {
        return resourceNS;
    }

    public void setResourceNS(String resourceNS) {
        this.resourceNS = resourceNS;
    }

    public Set<Singer> getSingers() {
        return singers;
    }

    public void setSingers(Set<Singer> singers) {
        this.singers = singers;
    }

    public Set<DummyLeaflistIdRef> getDummyLeaflistIdRef() {
        return dummyLeaflistIdRef;
    }

    public void setDummyLeaflistIdRef(Set<DummyLeaflistIdRef> dummyLeaflistIdRef) {
        this.dummyLeaflistIdRef = dummyLeaflistIdRef;
    }
}
