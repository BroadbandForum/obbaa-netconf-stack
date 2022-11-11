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

package org.broadband_forum.obbaa.netconf.api.codec.v2;

import org.w3c.dom.Document;

public class DocumentInfo {
    private Document m_document;
    private int m_length;
    private String m_documentString;

    public DocumentInfo(Document document, String documentString) {
        this.m_document = document;
        this.m_documentString = documentString;
        this.m_length = documentString.length();
    }

    public String getDocumentString() {
        return m_documentString;
    }

    public void setDocumentString(String documentString) {
        this.m_documentString = documentString;
    }

    public Document getDocument() {
        return m_document;
    }

    public void setDocument(Document document) {
        m_document = document;
    }

    public int getLength() {
        return m_length;
    }

    public void setLength(int docLength) {
        m_length = docLength;
    }
}
