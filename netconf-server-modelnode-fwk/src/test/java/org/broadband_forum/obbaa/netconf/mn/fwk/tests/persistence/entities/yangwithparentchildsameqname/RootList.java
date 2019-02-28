package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.yangwithparentchildsameqname;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangXmlSubtree;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "yangwithparentchildsameqname_root_list")
@Table(name = "yangwithparentchildsameqname_root_list")
@YangList(name="root-list", namespace = "urn:yang-with-parent-child-same-qname", revision= "2016-07-11")
public class RootList {
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

    @Override public String toString() {
        return "RootList{" +
                "parentId='" + parentId + '\'' +
                ", schemaPath='" + schemaPath + '\'' +
                '}';
    }
}
