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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.jukebox2;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.EditException;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNodeId;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.SubSystemRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ConfigAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.Container;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ContainerList;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation.AnnotationModelNode;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.ArrayList;
import java.util.List;

@Container(name = "artist", namespace = Jukebox.NAMESPACE)
public class Artist extends AnnotationModelNode {
    public static final QName QNAME = QName.create("http://example.com/ns/example-jukebox", "2014-07-03", "artist");
    private static final long serialVersionUID = 1L;
    private String m_name;
    private List<Album> m_albums = new ArrayList<Album>();
    public static final SchemaPath ARTIST_SCHEMA_PATH = new SchemaPathBuilder().withParent(Library.LIBRARY_SCHEMA_PATH)
            .appendQName(QNAME).build();


    public Artist(ModelNode parent, ModelNodeId parentNodeId, ModelNodeHelperRegistry helperRegistry,
                  SubSystemRegistry subSystemRegistry,
                  SchemaRegistry schemaRegistry) {
        super(parent, parentNodeId, helperRegistry, subSystemRegistry, schemaRegistry);
    }

    @ConfigAttribute(name = "name", namespace = Jukebox.NAMESPACE, isKey = true)
    public String getName() {
        return m_name;
    }

    public void setName(String name) throws EditException {
        m_name = name;
    }

    @ContainerList(name = "album", namespace = Jukebox.NAMESPACE, childClass = Album.class, childFactoryName =
            "AbstractModelNodeFactory")
    public List<Album> getAlbums() {
        return m_albums;
    }

    public Album addAlbum(String name) throws EditException {
        Album album = new Album(this, new ModelNodeId(getModelNodeId()), getModelNodeHelperRegistry(),
                getSubSystemRegistry(), getSchemaRegistry());
        album.setName(name);
        m_albums.add(album);
        return album;
    }

    public int getAlbumCount() {
        return m_albums.size();
    }

    public int getSongCount() {
        int count = 0;
        for (Album a : m_albums) {
            count += a.getSongCount();
        }
        return count;
    }

    @Override
    public String toString() {
        return "Artist [m_name=" + m_name + "]";
    }

    @Override
    public SchemaPath getModelNodeSchemaPath() {
        return ARTIST_SCHEMA_PATH;
    }

}
