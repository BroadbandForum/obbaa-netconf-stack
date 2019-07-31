package org.broadband_forum.obbaa.netconf.samples.jb.persistence.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangOrderByUser;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangXmlSubtree;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.samples.jb.api.JBConstants;

/**
 * Created by keshava on 1/29/16.
 */
@Entity(name = "playlist")
@Table(name = "jb_playlist")
@IdClass(PlayListPK.class)
@YangList(name="playlist", namespace = JBConstants.JB_NS, revision= JBConstants.JB_REVISION)
public class PlayList {
    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";

    @YangParentSchemaPath
    public static final SchemaPath getParentSchemaPath() {
        return JBConstants.JB_SP;
    }
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

    @Column
    @YangOrderByUser
    Integer insertOrder;

    @Column(length = 1000)
    @YangSchemaPath
    String schemaPath;

    @YangXmlSubtree
    @Column(name = "xmlSubtree", columnDefinition = "LONGTEXT")
    @Lob
    private String xmSubtree;

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

    public String getXmSubtree() {
        return xmSubtree;
    }

    public void setXmSubtree(String xmSubtree) {
        this.xmSubtree = xmSubtree;
    }
}
