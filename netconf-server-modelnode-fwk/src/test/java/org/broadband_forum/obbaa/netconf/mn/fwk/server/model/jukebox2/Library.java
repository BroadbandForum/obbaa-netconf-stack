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
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.Container;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ContainerList;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.StateAttribute;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.annotation.AnnotationModelNode;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import java.util.ArrayList;
import java.util.List;

@Container(name = "library", namespace = Jukebox.NAMESPACE)
public class Library extends AnnotationModelNode {
    public static final QName QNAME = QName.create("http://example.com/ns/example-jukebox", "2014-07-03", "library");
    public static final SchemaPath LIBRARY_SCHEMA_PATH = new SchemaPathBuilder().withParent(Jukebox.JUKEBOX_SCHEMA_PATH)
            .appendQName(QNAME).build();
    ;


    public Library(ModelNode parent, ModelNodeId parentNodeId, ModelNodeHelperRegistry helperRegistry,
                   SubSystemRegistry subSystemRegistry,
                   SchemaRegistry schemaRegistry) {
        super(parent, parentNodeId, helperRegistry, subSystemRegistry, schemaRegistry);
    }

    private static final long serialVersionUID = 1L;

    private List<Artist> m_artists = new ArrayList<Artist>();

    /**
     * Non config element
     *
     * @return
     */
    @StateAttribute(name = "artist-count", namespace = Jukebox.NAMESPACE)
    public int getArtistCount() {
        return m_artists.size();
    }

    /**
     * Non config element
     *
     * @return
     */
    @StateAttribute(name = "album-count", namespace = Jukebox.NAMESPACE)
    public int getAlbumCount() {
        int count = 0;
        for (Artist a : m_artists) {
            count += a.getAlbumCount();
        }
        return count;
    }

    /**
     * Non config element
     *
     * @return
     */
    @StateAttribute(name = "song-count", namespace = Jukebox.NAMESPACE)
    public int getSongCount() {
        int count = 0;
        for (Artist a : m_artists) {
            count += a.getSongCount();
        }
        return count;
    }

    @ContainerList(name = "artist", namespace = Jukebox.NAMESPACE, childClass = Artist.class, childFactoryName =
            "AbstractModelNodeFactory")
    public List<Artist> getArtists() {
        return m_artists;
    }

    public Artist addArtist(String name) throws EditException {
        if (name == null || name.equals("")) {
            throw new EditException("Artist name cannot be empty");
        }
        Artist a = null;
        a = new Artist(this, new ModelNodeId(getModelNodeId()), getModelNodeHelperRegistry(), getSubSystemRegistry(),
                getSchemaRegistry());
        a.setName(name);
        m_artists.add(a);
        return a;
    }

    @Override
    public SchemaPath getModelNodeSchemaPath() {
        return LIBRARY_SCHEMA_PATH;
    }

}
