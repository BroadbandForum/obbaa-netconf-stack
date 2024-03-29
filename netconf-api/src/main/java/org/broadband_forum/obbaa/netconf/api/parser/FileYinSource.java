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

package org.broadband_forum.obbaa.netconf.api.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.opendaylight.yangtools.yang.model.repo.api.YinTextSchemaSource;

import com.google.common.base.MoreObjects.ToStringHelper;

public class FileYinSource extends YinTextSchemaSource {

    private final File m_yinFile;

    public FileYinSource(File yangFile) {
        super(identifierFromFilename(yangFile.getName()));
        m_yinFile = yangFile;
    }

    @Override
    protected ToStringHelper addToStringAttributes(ToStringHelper toStringHelper) {
        return toStringHelper.add("file", m_yinFile.getAbsolutePath());
    }

    @Override
    public InputStream openStream() throws IOException {
        return new FileInputStream(m_yinFile);
    }
    
    @Override
    public Optional<String> getSymbolicName() {
        return Optional.of(m_yinFile.getAbsolutePath());
    }
}
