package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.addresses;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

/**
 * Created by keshava on 12/8/15.
 */
@Entity
@Table
@IdClass(value = TelephoneNumberPK.class)
@YangList(name = "telephone-number")
public class TelephoneNumber{
    @Id
    @YangListKey
    private String type;

    @Column
    @YangAttribute
    private String number;

    @Id
    @YangParentId
    private String parentId;

    @YangSchemaPath
    @Column
    String schemaPath;

    public String getSchemaPath() {
        return schemaPath;
    }

    public void setSchemaPath(String schemaPath) {
        this.schemaPath = schemaPath;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getParentId() {
        return parentId;
    }

}
