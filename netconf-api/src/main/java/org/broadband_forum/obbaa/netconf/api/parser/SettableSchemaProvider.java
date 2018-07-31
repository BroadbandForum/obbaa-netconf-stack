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

import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceRepresentation;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.spi.PotentialSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceRegistry;

import com.google.common.base.Function;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;

public class SettableSchemaProvider<T extends SchemaSourceRepresentation> implements SchemaSourceProvider<T> {

    private final SettableFuture<T> m_future = SettableFuture.create();
    private final T m_schemaSourceRepresentation;
    private final PotentialSchemaSource<T> m_potentialSchemaSource;

    SettableSchemaProvider(final T schemaSourceRepresentation, final SourceIdentifier sourceIdentifier, final
    Class<T> representation,
                           final int cost) {
        this.m_schemaSourceRepresentation = schemaSourceRepresentation;
        this.m_potentialSchemaSource = PotentialSchemaSource.create(sourceIdentifier, representation, cost);
    }

    public static <T extends SchemaSourceRepresentation> SettableSchemaProvider<T> createImmediate(final T schemaSourceRepresentation,
                                                                                                   final Class<T>
                                                                                                           representation) {
        return new SettableSchemaProvider<>(schemaSourceRepresentation, schemaSourceRepresentation.getIdentifier(),
                representation,
                PotentialSchemaSource.Costs.IMMEDIATE.getValue());
    }

    @Override
    public CheckedFuture<T, SchemaSourceException> getSource(final SourceIdentifier sourceIdentifier) {
        return Futures.makeChecked(m_future, new Function<Exception, SchemaSourceException>() {
            @Override
            public SchemaSourceException apply(final Exception input) {
                return new SchemaSourceException("Failed", input);
            }
        });
    }

    public T getSchemaSourceRepresentation() {
        return m_schemaSourceRepresentation;
    }

    public SourceIdentifier getId() {
        return m_schemaSourceRepresentation.getIdentifier();
    }

    public void setResult() {
        m_future.set(m_schemaSourceRepresentation);
    }

    public void setException(final Throwable ex) {
        m_future.setException(ex);
    }

    public void register(final SchemaSourceRegistry repo) {
        repo.registerSchemaSource(this, m_potentialSchemaSource);
    }

}
