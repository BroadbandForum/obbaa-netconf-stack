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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

/**
 * Created by keshava on 12/28/15.
 */
public class SchemaPathUtil {
    public static final String DELIMITER = ",";

    public static Map<String, URI> m_namespaceCache = new ConcurrentHashMap<>();
    public static Map<String, Date> m_revisionCache = new ConcurrentHashMap<>();

    public static SchemaPath fromString(String schemaPathStr) {
        List<String> parts = Arrays.asList(schemaPathStr.split(DELIMITER));
        List<QName> qNames = new ArrayList<>();
        Iterator<String> partsIter = parts.iterator();
        while (partsIter.hasNext()) {
            String namespace = partsIter.next();
            String formattedRev = partsIter.next();
            String localName = partsIter.next();
            QName qName = null;
            if (!formattedRev.isEmpty()) {
                qName = QName.create(getNamespace(namespace), getRevision(formattedRev), localName);
            } else {
                qName = QName.create(getNamespace(namespace), null, localName);
            }
            qNames.add(qName);
        }
        return SchemaPath.create(qNames, true);
    }

    private static URI getNamespace(String namespace) {
        URI uri = m_namespaceCache.get(namespace);
        if (uri == null) {
            try {
                uri = new URI(namespace);
                m_namespaceCache.putIfAbsent(namespace, uri);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(String.format("Namespace '%s' is not a valid URI", namespace), e);
            }
        }
        return uri;
    }

    private static Date getRevision(String revision) {
        Date date = m_revisionCache.get(revision);
        if (date == null) {
            date = QName.parseRevision(revision);
            m_revisionCache.putIfAbsent(revision, date);
        }
        return date;
    }

    public static String toString(SchemaPath schemaPath) {
        Iterator<QName> pathIter = schemaPath.getPathFromRoot().iterator();
        StringBuilder sb = new StringBuilder();
        while (pathIter.hasNext()) {
            QName next = pathIter.next();
            sb.append(next.getNamespace().toString()).append(DELIMITER)
                    .append(next.getFormattedRevision()).append(DELIMITER)
                    .append(next.getLocalName()).append(DELIMITER);
        }
        return sb.toString();
    }

    public static boolean isRootSchemaPath(SchemaPath nodeSchemaPath) {
        return SchemaPath.ROOT.equals(nodeSchemaPath.getParent());
    }
}
