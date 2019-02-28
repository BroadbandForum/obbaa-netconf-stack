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

package org.broadband_forum.obbaa.netconf.api.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Created by keshava on 11/20/15.
 */
public class SchemaPathBuilder {
    public static final String DELIMITER = ",";

    public static final String REVISION_DELIMITER = "?revision=";
    public static final Pattern PATTERN_FULL = Pattern.compile("^\\((.+)\\" + REVISION_DELIMITER
            + "(.+)\\)(.+)$");

    private static final String REVISION_SIMPLE_DATE = "yyyy-MM-dd";
    static final SimpleDateFormat SIMPLE_DATE_FORMAT= new SimpleDateFormat(REVISION_SIMPLE_DATE);
    private String m_namespace;
    private String m_revision;
    private List<Object> m_schemaPathChildComponents = new ArrayList<>();
    private SchemaPath m_parentSchemaPath;

    /**
     * Use with appendLocalName and withRevision
     * @param namespace
     * @return
     */
    public SchemaPathBuilder withNamespace(String namespace) {
        m_namespace= namespace;
        return this;
    }

    /**
     * Use with appendLocalName and withNamespace
     * @param revision
     * @return
     */
    public SchemaPathBuilder withRevision(String revision) {
        m_revision = revision;
        return this;
    }
    
    public SchemaPathBuilder withRevision(Revision revision) {
        if (revision == null) {
            m_revision = null;
        }
        else {
            m_revision = revision.toString();
        }
        return this;
    }

    public SchemaPathBuilder appendLocalName(String localName) {
        m_schemaPathChildComponents.add(localName);
        return this;
    }

    public SchemaPath build() {
        List<QName> qnames = new ArrayList<>();
        if(m_parentSchemaPath != null){
            for(QName qname: m_parentSchemaPath.getPathFromRoot()){
                qnames.add(qname);
            }
        }
        String namespace = m_namespace;
        String revision = m_revision;

        if(namespace == null && revision == null && m_parentSchemaPath !=null){
            //inherit from parent in this case
            namespace = m_parentSchemaPath.getLastComponent().getNamespace().toString();
            Optional<Revision> revObj = m_parentSchemaPath.getLastComponent().getRevision();
            revision = (revObj.isPresent() ? revObj.get().toString() : null);
        }
        for(Object part: m_schemaPathChildComponents){
            if(part instanceof String) {
                if(revision!=null && !revision.isEmpty()) {
                    qnames.add(QName.create(namespace, revision, (String) part));
                }else{
                    qnames.add(QName.create(namespace, (String) part));
                }
            }else {
                qnames.add((QName) part);
            }
        }

        return SchemaPath.create(qnames, true);
    }

    public SchemaPathBuilder withParent(SchemaPath parentSchemaPath) {
        m_parentSchemaPath = parentSchemaPath;
        return this;
    }

    public SchemaPathBuilder appendQName(QName name) {
        m_schemaPathChildComponents.add(name);
        return this;
    }

    public static SchemaPath fromString(String schemaPathStr) throws SchemaPathBuilderException {
        return fromString(new String []{schemaPathStr});
    }

    public static SchemaPath fromString(String ... schemaPaths) throws SchemaPathBuilderException {
        SchemaPath path = SchemaPath.ROOT;
        for (String schemaPath : schemaPaths){
            Matcher matcher = PATTERN_FULL.matcher(schemaPath);
            String namespace;
            String revision;
            String localNames;
            if (matcher.matches()) {
                namespace = matcher.group(1).trim();
                revision = matcher.group(2).trim();
                localNames = matcher.group(3).trim();
            }else {
                throw new SchemaPathBuilderException(String.format("Could not determine namespace and revisions info from %s,",
                        schemaPath));
            }

            SchemaPathBuilder builder = new SchemaPathBuilder();
            builder.withParent(path);
            builder.withNamespace(namespace);
            builder.withRevision(revision);

            for (String localName : Arrays.asList(localNames.split(DELIMITER))){
                if(!localName.isEmpty()){
                    builder.appendLocalName(localName.trim());
                }
            }
            path = builder.build();
        }
        return path;

    }

}
