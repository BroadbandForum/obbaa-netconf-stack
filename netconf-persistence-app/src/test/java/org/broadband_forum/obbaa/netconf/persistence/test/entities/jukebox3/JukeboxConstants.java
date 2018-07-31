/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.netconf.persistence.test.entities.jukebox3;

import org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil;
import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

import static org.broadband_forum.obbaa.netconf.api.util.SchemaPathUtil.DELIMITER;

/**
 * Created by keshava on 21/12/15.
 */
public class JukeboxConstants {
    public static final String JB_NS = "http://example.com/ns/example-jukebox";
    public static final String JUKEBOX_MODULE_NAME = "example-jukebox";
    public static final String ADDR_NS = "test:addresses";
    public static final String CERT_NS = "test:certificates";
    public static final String JB_REVISION = "2014-07-03";
    public static final String REVISION = "2015-12-08";
    public static final String JUKEBOX_LOCAL_NAME = "jukebox";
    public static final String LIBRARY_LOCAL_NAME = "library";
    public static final String ARTIST_LOCAL_NAME = "artist";
    public static final String ARTIST_COUNT_LOCAL_NAME = "artist-count";
    public static final String ALBUM_LOCAL_NAME = "album";
    public static final String SONG_LOCAL_NAME = "song";
    public static final String RELEASE_TYPE_LOCAL_NAME = "release-type";
    public static final String CD_TYPE_LOCAL_NAME = "cd-type";
    public static final String CD_LOCAL_NAME = "cd";
    public static final String STOCK_LOCAL_NAME = "stock";
    public static final String CERT_MGMT_LOCAL_NAME = "certificate-mgmt";
    public static final String PMA_CERTS_LOCAL_NAME = "pma-certs";
    public static final String TRUSTED_CA_CERTS_LOCAL_NAME = "trusted-ca-certs";
    public static final String CERTIFICATE_LOCAL_NAME = "certificate";
    public static final String TELEPHONE_NUMBER_LOCAL_NAME = "telephone-number";
    public static final String OFFICE_ADDRESS_LOCAL_NAME = "office-address";
    public static final String HOME_ADDRESS_LOCAL_NAME = "home-address";
    public static final String ADDRESS_NAME_LOCAL_NAME = "address-name";
    public static final String SINGER_LOCAL_NAME = "singer";

    public static final String MY_HOME_ADDRESS_KARNATAKA_560092 = "My Home Address, Karnataka 560092";
    public static final String LAND_LINE = "Land line";
    public static final String HOME_ADDRESS = "Home Address";
    public static final String CELL_PHONE = "cell phone";
    public static final String OFFICE_LAND_LINE = "044 3312 8000";
    public static final String OFFICE_CELL = "+91 944 955 4002";
    public static final String HOME_LAND_LINE = "080 1234 5678";
    public static final String HOME_CELL = "+91 944 955 4003";
    public static final String CHENNAI_OFFICE_ADDRESS = "TVH Agnitio Park, 4th Floor, Door No. 141, Kandanchavady Old" +
            " Mahabalipuram, Road, Chennai, Tamil Nadu 600096";
    public static final String OFFICE_ADDRESS = "Office Address";

    public static final QName JUKEBOX_QNAME = QName.create(JB_NS, JB_REVISION, JUKEBOX_LOCAL_NAME);
    public static final QName LIBRARY_QNAME = QName.create(JB_NS, JB_REVISION, LIBRARY_LOCAL_NAME);
    public static final QName ARTIST_QNAME = QName.create(JB_NS, JB_REVISION, ARTIST_LOCAL_NAME);

    public static final QName ARTIST_COUNT_QNAME = QName.create(JB_NS, JB_REVISION, ARTIST_COUNT_LOCAL_NAME);
    public static final QName ALBUM_QNAME = QName.create(JB_NS, JB_REVISION, ALBUM_LOCAL_NAME);
    public static final QName SONG_QNAME = QName.create(JB_NS, JB_REVISION, SONG_LOCAL_NAME);
    public static final QName NAME_QNAME = QName.create(JB_NS, JB_REVISION, "name");
    public static final QName YEAR_QNAME = QName.create(JB_NS, JB_REVISION, "year");
    public static final QName FORMAT_QNAME = QName.create(JB_NS, JB_REVISION, "format");
    public static final QName LABEL_QNAME = QName.create(JB_NS, JB_REVISION, "label");
    public static final QName GENRE_QNAME = QName.create(JB_NS, JB_REVISION, "genre");
    public static final QName RESOURCE_QNAME = QName.create(JB_NS, JB_REVISION, "resource");
    public static final QName LOCATION_QNAME = QName.create(JB_NS, JB_REVISION, "location");
    public static final QName STOCK_QNAME = QName.create(JB_NS, JB_REVISION, STOCK_LOCAL_NAME);
    public static final QName TELEPHONE_TYPE_QNAME = QName.create(ADDR_NS, REVISION, "type");
    public static final QName TELEPHONE_NUMBER_QNAME = QName.create(ADDR_NS, REVISION, "number");
    public static final QName QNAME_CERTIFICATE_ID = QName.create(CERT_NS, REVISION, "id");
    public static final QName ADDRESS_NAME_Q_NAME = QName.create(ADDR_NS, REVISION, ADDRESS_NAME_LOCAL_NAME);
    public static final QName ADDRESS_Q_NAME = QName.create(ADDR_NS, REVISION, "address");
    public static final QName SINGER_QNAME = QName.create(JB_NS, JB_REVISION, SINGER_LOCAL_NAME);

    public static final SchemaPath JUKEBOX_SCHEMA_PATH = SchemaPath.create(true, QName.create(JB_NS, JB_REVISION,
            JUKEBOX_LOCAL_NAME));
    public static final SchemaPath LIBRARY_SCHEMA_PATH = new SchemaPathBuilder().withParent(JUKEBOX_SCHEMA_PATH)
            .appendLocalName(LIBRARY_LOCAL_NAME).build();
    public static final SchemaPath ARTIST_SCHEMA_PATH = new SchemaPathBuilder().withParent(LIBRARY_SCHEMA_PATH)
            .appendLocalName(ARTIST_LOCAL_NAME).build();
    public static final SchemaPath ALBUM_SCHEMA_PATH = new SchemaPathBuilder().withParent(ARTIST_SCHEMA_PATH)
            .appendLocalName(ALBUM_LOCAL_NAME).build();
    public static final SchemaPath ALBUM_NAME_SCHEMA_PATH = new SchemaPathBuilder().withParent(ARTIST_SCHEMA_PATH)
            .appendQName(NAME_QNAME).build();
    public static final SchemaPath SONG_SCHEMA_PATH = new SchemaPathBuilder().withParent(ALBUM_SCHEMA_PATH)
            .appendLocalName(SONG_LOCAL_NAME).build();
    public static final SchemaPath CD_SCHEMA_PATH = new SchemaPathBuilder().withParent(SONG_SCHEMA_PATH)
            .appendLocalName(RELEASE_TYPE_LOCAL_NAME).appendLocalName(CD_TYPE_LOCAL_NAME).appendLocalName
                    (CD_LOCAL_NAME).build();
    public static final SchemaPath SINGER_SCHEMA_PATH = new SchemaPathBuilder().withParent(SONG_SCHEMA_PATH)
            .appendLocalName(SINGER_LOCAL_NAME).build();

    private static final QName CERT_MGMT_QNAME = QName.create(CERT_NS, REVISION, CERT_MGMT_LOCAL_NAME);
    public static final SchemaPath CA_CERT_CONTAINER_SCHEMA_PATH = SchemaPath.create(true, CERT_MGMT_QNAME, QName
            .create(CERT_NS,
            REVISION, TRUSTED_CA_CERTS_LOCAL_NAME));

    public static final SchemaPath PMA_CERT_CONTAINER_SCHEMA_PATH = SchemaPath.create(true, CERT_MGMT_QNAME, QName
            .create(CERT_NS,
            REVISION, PMA_CERTS_LOCAL_NAME));
    public static final SchemaPath PMA_CERT_SCHEMA_PATH = new SchemaPathBuilder().withParent
            (PMA_CERT_CONTAINER_SCHEMA_PATH).appendLocalName(CERTIFICATE_LOCAL_NAME).build();
    public static final SchemaPath CA_CERT_SCHEMA_PATH = new SchemaPathBuilder().withParent
            (CA_CERT_CONTAINER_SCHEMA_PATH).appendLocalName(CERTIFICATE_LOCAL_NAME).build();
    public static final SchemaPath HOME_ADDRESSES_SCHEMA_PATH = SchemaPath.create(true, QName.create(ADDR_NS,
            REVISION, HOME_ADDRESS_LOCAL_NAME));
    public static final SchemaPath OFFICE_ADDRESSES_SCHEMA_PATH = SchemaPath.create(true, QName.create(ADDR_NS,
            REVISION, OFFICE_ADDRESS_LOCAL_NAME));
    public static final SchemaPath HOME_TELPHONE_SCHEMA_PATH = new SchemaPathBuilder().withParent
            (HOME_ADDRESSES_SCHEMA_PATH).appendLocalName(TELEPHONE_NUMBER_LOCAL_NAME).build();
    public static final SchemaPath OFFICE_TELPHONE_SCHEMA_PATH = new SchemaPathBuilder().withParent
            (OFFICE_ADDRESSES_SCHEMA_PATH).appendLocalName(TELEPHONE_NUMBER_LOCAL_NAME).build();

    public static final String V3_PMA_CERT_NS = "test:v3-pma-certificates";
    public static final String V3_CERT_REVISION = REVISION;
    public static final String V3_CERT_NS = "test:v3-certificates";
    public static final SchemaPath V3_PMA_CERT_CONTAINER_SCHEMA_PATH = SchemaPathUtil.fromString(new StringBuilder()
            .append(V3_CERT_NS + DELIMITER).append(V3_CERT_REVISION + DELIMITER).append(CERT_MGMT_LOCAL_NAME +
                    DELIMITER)
            .append(V3_PMA_CERT_NS + DELIMITER).append(V3_CERT_REVISION + DELIMITER).append(PMA_CERTS_LOCAL_NAME +
                    DELIMITER)
            .toString());

    public static final SchemaPath V3_CERT_SCHEMA_PATH = SchemaPathUtil.fromString(new StringBuilder()
            .append(V3_CERT_NS + DELIMITER).append(V3_CERT_REVISION + DELIMITER).append(CERT_MGMT_LOCAL_NAME +
                    DELIMITER)
            .append(V3_PMA_CERT_NS + DELIMITER).append(V3_CERT_REVISION + DELIMITER).append(PMA_CERTS_LOCAL_NAME +
                    DELIMITER)
            .append(V3_PMA_CERT_NS + DELIMITER).append(V3_CERT_REVISION + DELIMITER).append(CERTIFICATE_LOCAL_NAME +
                    DELIMITER)
            .toString());
    public static final QName V3_CERT_BINARY_QNAME = QName.create(V3_PMA_CERT_NS, V3_CERT_REVISION, "cert-binary");
    public static final QName V3_CERT_ID_QNAME = QName.create(V3_PMA_CERT_NS, V3_CERT_REVISION, "id");

}
