package org.broadband_forum.obbaa.netconf.mn.fwk.tests.persistence.entities.embeddedchoicecase;


import org.broadband_forum.obbaa.netconf.api.util.SchemaPathBuilder;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class RootContainerTestConstants {

    public static final String ROOT_CONTAINER = "root-container";
    public static final String TCONTS = "tconts";
    public static final String NAME = "name";
    public static final String TM_ROOT = "tm-root";
    public static final String CHILDREN_TYPE ="children-type";
    public static final String QUEUES = "queues";
    public static final String QUEUE = "queue";
    public static final String ID = "id";
    public static final String CFG_TYPE = "cfg-type";
    public static final String INLINE = "inline";
    public static final String PRIORITY = "priority";
    public static final String WEIGHT = "weight";

    public static final String NS = "urn:embedded-choice-case-test";
    public static final String REVISION = "2015-12-14";

    public static final QName PRIORITY_QNAME = QName.create(NS,REVISION,PRIORITY);
    public static final QName WEIGHT_QNAME = QName.create(NS,REVISION,WEIGHT);
    public static final QName ID_QNAME = QName.create(NS,REVISION,ID);

    public static final SchemaPath ROOT_CONTAINER_PATH = SchemaPath.create(true, QName.create(NS, REVISION, ROOT_CONTAINER));
    public static final SchemaPath TCONTS_PATH = new SchemaPathBuilder().withParent(ROOT_CONTAINER_PATH).appendLocalName(TCONTS)
            .build();
    public static final SchemaPath TM_ROOT_PATH = new SchemaPathBuilder().withParent(TCONTS_PATH).appendLocalName(TM_ROOT).build();
    public static final SchemaPath CHILDREN_TYPE_PATH = new SchemaPathBuilder().withParent(TM_ROOT_PATH).appendLocalName(CHILDREN_TYPE)
            .build();
    public static final SchemaPath QUEUES_PATH = new SchemaPathBuilder().withParent(CHILDREN_TYPE_PATH).appendLocalName(QUEUES).build();
    public static final SchemaPath QUEUE_PATH = new SchemaPathBuilder().withParent(QUEUES_PATH).appendLocalName(QUEUE).build();
    public static final SchemaPath CFG_TYPE_PATH = new SchemaPathBuilder().withParent(QUEUE_PATH).appendLocalName(CFG_TYPE).build();
    public static final SchemaPath INLINE_PATH = new SchemaPathBuilder().withParent(CFG_TYPE_PATH).appendLocalName(INLINE).build();





}
