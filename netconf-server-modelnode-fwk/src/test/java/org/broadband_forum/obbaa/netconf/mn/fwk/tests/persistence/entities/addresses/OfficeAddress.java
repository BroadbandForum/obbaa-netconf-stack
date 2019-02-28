package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.addresses;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangAttribute;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangList;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangListKey;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by keshava on 12/8/15.
 */
@Entity
@Table
@IdClass(AddressPK.class)
@YangList(name = "office-address", namespace = "test:addresses", revision = "2015-12-08")
public class OfficeAddress {
    @Id
    @YangListKey(name = "address-name")
    private String addressName;

    @Column
    @YangAttribute
    private String address;

    @OneToMany(cascade = CascadeType.ALL,orphanRemoval = true)
    @YangChild
    List<TelephoneNumber> telephoneNumbers = new ArrayList<>();

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

    public List<TelephoneNumber> getTelephoneNumbers() {
        return telephoneNumbers;
    }

    public void addTelephoneNumbers(TelephoneNumber telephoneNumber) {
        telephoneNumbers.add(telephoneNumber);
    }

    public void setTelephoneNumbers(List<TelephoneNumber> telephoneNumbers) {
        this.telephoneNumbers = telephoneNumbers;
    }
}
