package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.certificates.v3;


import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangChild;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangContainer;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangParentId;
import org.broadband_forum.obbaa.netconf.stack.api.annotations.YangSchemaPath;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@YangContainer(name = "certificate-mgmt", namespace = "test:v3-certificates", revision = "2015-12-08")
@Entity
@Table(name="V3CertificateMgmt")
public class V3CertificateMgmt {

    @Id
    @YangParentId
    @Column
    private String parentId;

    @YangSchemaPath
    @Column(length = 1000)
    private String schemaPath;

    @YangChild
    @OneToOne(cascade = {CascadeType.ALL}, fetch = FetchType.LAZY, orphanRemoval = true)
    private V3PmaCerts pmaCerts;

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

    public V3PmaCerts getPmaCerts() {
        return pmaCerts;
    }

    public void setPmaCerts(V3PmaCerts pmaCerts) {
        this.pmaCerts = pmaCerts;
    }

}
