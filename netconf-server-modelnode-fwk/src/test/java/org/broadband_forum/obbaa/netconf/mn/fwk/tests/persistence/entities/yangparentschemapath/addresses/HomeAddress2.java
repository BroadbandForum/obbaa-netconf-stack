package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.yangparentschemapath.addresses;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

/**
 * Created by keshava on 12/8/15.
 */
@Entity
@Table
@IdClass(AddressPK.class)
@YangList(name = "home-address", namespace = "test:addresses", revision = "2015-12-08")
public class HomeAddress2 {
    public static final SchemaPath SP = SchemaPath.create(true, QName.create("test:addresses", "2015-12-08", "home-address"));
    @Id
    @YangListKey(name = "address-name")
    private String addressName;

    @Column
    @YangAttribute
    private String address;

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
    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getAddressName() {
        return addressName;
    }

    public void setAddressName(String addressName) {
        this.addressName = addressName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
