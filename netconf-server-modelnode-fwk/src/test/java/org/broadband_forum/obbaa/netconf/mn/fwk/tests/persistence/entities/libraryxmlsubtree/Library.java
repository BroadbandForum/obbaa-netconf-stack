package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.libraryxmlsubtree;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3.JukeboxConstants;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangXmlSubtree;

@Entity(name = "librarySubtree_library")
@Table(name = "librarySubtree_library")
@YangContainer(name="library", namespace = JukeboxConstants.JB_NS, revision= JukeboxConstants.JB_REVISION)
public class Library {
    @Id
    @YangParentId
    String parentId;
    
    @YangSchemaPath
    @Column(length = 1000)
    String schemaPath;

    @Column(length=100000)
    @YangXmlSubtree
    private String xmlSubtree;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

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
}
