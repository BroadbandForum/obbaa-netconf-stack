package org.broadband_forum.obbaa.netconf.samples.jb.persistence.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangLeafList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangOrderByUser;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.samples.jb.api.JBConstants;

@Entity(name = "singer")
@Table(name = "jb_singer")
@IdClass(SingerPK.class)
@YangLeafList(name = "singer", namespace = JBConstants.JB_NS, revision = JBConstants.JB_REVISION)
public class Singer {

    @Id
    @Column
    @YangAttribute(name = "name", namespace = JBConstants.JB_NS, revision = JBConstants.JB_REVISION)
    private String name;

    @Id
    @YangParentId
    String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    String schemaPath;

    @Column
    @YangOrderByUser
    private Integer insertOrder;

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

    public int getInsertOrder() {
        return insertOrder;
    }

    public void setInsertOrder(Integer insertOrder) {
        this.insertOrder = insertOrder;
    }
}
