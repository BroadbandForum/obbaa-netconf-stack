package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

/**
 * Created by ttdien on 7/30/18.
 */
public class MountContext {

    public MountContext() {

    }

    public MountContext(String identifier, SchemaRegistry schemaRegistry) {
        this.identifier = identifier;
        this.schemaRegistry = schemaRegistry;
    }

    private String identifier;

    private SchemaRegistry schemaRegistry;

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public SchemaRegistry getSchemaRegistry() {
        return schemaRegistry;
    }

    public void setSchemaRegistry(SchemaRegistry schemaRegistry) {
        this.schemaRegistry = schemaRegistry;
    }

}
