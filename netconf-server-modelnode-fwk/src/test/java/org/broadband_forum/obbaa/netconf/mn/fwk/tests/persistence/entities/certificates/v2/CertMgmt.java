package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.certificates.v2;

import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * Created by keshava on 5/13/16.
 */
@Entity
@Table(name = "CertMgmt")
@YangContainer(name="certificate-mgmt", namespace = "test:certificates", revision="2015-12-08")
public class CertMgmt {
    @Id
    @YangParentId
    private String parentId;

    @YangSchemaPath
    @Column
    String schemaPath;

    @YangChild
    @OneToOne
    PmaCerts pmaCerts;

    @YangChild
    @OneToOne
    TrustedCaCerts caCerts;

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

    public PmaCerts getPmaCerts() {
        return pmaCerts;
    }

    public void setPmaCerts(PmaCerts pmaCerts) {
        this.pmaCerts = pmaCerts;
    }

    public TrustedCaCerts getCaCerts() {
        return caCerts;
    }

    public void setCaCerts(TrustedCaCerts caCerts) {
        this.caCerts = caCerts;
    }
}
