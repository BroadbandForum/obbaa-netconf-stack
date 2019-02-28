package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.embeddedchoicecase;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangXmlSubtree;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "embeddedchoicecase_root")
@Table(name = "embeddedchoicecase_root")
@YangContainer(name="root-container", namespace = "urn:embedded-choice-case-test", revision= "2015-12-14")
public class RootContainer {

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

    @Override
    public String toString() {
        return "RootContainer{" +
                "parentId='" + parentId + '\'' +
                ", schemaPath='" + schemaPath + '\'' +
                ", xmlSubtree='" + xmlSubtree + '\'' +
                '}';
    }
}
