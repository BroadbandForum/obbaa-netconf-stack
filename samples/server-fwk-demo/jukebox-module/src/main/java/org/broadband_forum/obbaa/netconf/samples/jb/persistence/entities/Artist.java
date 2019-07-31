package org.broadband_forum.obbaa.netconf.samples.jb.persistence.entities;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangXmlSubtree;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.w3c.dom.Document;

import org.broadband_forum.obbaa.netconf.samples.jb.api.JBConstants;

@YangContainer(name = "artist", namespace = JBConstants.JB_NS, revision = JBConstants.JB_REVISION)
@Entity(name="artist")
@Table(name="jb_artist")
@IdClass(ArtistPK.class)
public class Artist {

    @Id
    @YangParentId
    @Column(name = YangParentId.PARENT_ID_FIELD_NAME)
    private String parentId;

    @Id
    @YangAttribute(name = "name")
    @Column(name = "name")
    private String name;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @YangXmlSubtree
    @Column(name = "xmlSubtree", columnDefinition = "LONGTEXT")
    @Lob
    private String xmSubtree;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getXmSubtree() {
        return xmSubtree;
    }

    public void setXmSubtree(String xmSubtree) {
        this.xmSubtree = xmSubtree;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artist artist = (Artist) o;
        return Objects.equals(parentId, artist.parentId) &&
                Objects.equals(name, artist.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentId, name);
    }

    public Document getXmSubtreeDoc() {
        try {
            return DocumentUtils.stringToDocument(getXmSubtree());
        } catch (NetconfMessageBuilderException e) {
            throw new RuntimeException(e);
        }
    }
}
