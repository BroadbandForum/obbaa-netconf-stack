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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.EffectiveModelContextFactory;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaContextFactoryConfiguration;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceException;
import org.opendaylight.yangtools.yang.model.repo.api.SchemaSourceFilter;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.model.repo.api.YinTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.repo.SharedSchemaRepository;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.ASTSchemaSource;
import org.opendaylight.yangtools.yang.parser.rfc7950.repo.TextToASTTransformer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.io.ByteSource;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class YangParserUtil {
    
    public static final String YANG_EXTN = ".yang";

    public static final IOFileFilter YANG_FILE_FILTER = FileFilterUtils.suffixFileFilter(YANG_EXTN);
    
    // pass null for supportedFeatures and supportedDeviations if *all* features and deviations are to be supported
    public static SchemaContext parseFiles(String repoName, List<File> yangFiles, Set<QName> supportedFeatures, Map<QName,Set<QName>> supportedDeviations) {
        List<YangTextSchemaSource> inputFiles = new ArrayList<>();
        for (File yangFile : yangFiles) {
            inputFiles.add(new FileYangSource(yangFile));
        }
        return parseSchemaSources(repoName, inputFiles, supportedFeatures, supportedDeviations);
    }
    
    // pass null for supportedFeatures and supportedDeviations if *all* features and deviations are to be supported
    public static SchemaContext parseSchemaSources(String repoName, List<YangTextSchemaSource> inputFiles, Set<QName> supportedFeatures, Map<QName,Set<QName>> supportedDeviations) {
        try {

            // TODO: once the ODL ticket https://jira.opendaylight.org/browse/YANGTOOLS-859 (tracked internally in FNMS-22439) is fixed we should remove this
            if (supportedFeatures != null) {
                supportedFeatures = new HashSet<>(supportedFeatures);
                QName candidateFeature = QName.create("urn:ietf:params:xml:ns:netconf:base:1.0", "2011-06-01", "candidate");
                supportedFeatures.add(candidateFeature);
            }
            
            List<YangTextSchemaSource> filteredInputFiles = removeDuplicates(inputFiles);
            SharedSchemaRepository repo = new SharedSchemaRepository(repoName);
            List<SourceIdentifier> sourceIds = prepareSchemaRepo(repo, filteredInputFiles);

            SchemaContextFactoryConfiguration factoryConfig = new SchemaContextFactoryConfiguration.Builder()
                    .setFilter(SchemaSourceFilter.ALWAYS_ACCEPT)
                    .setSupportedFeatures(supportedFeatures)
                    .setModulesDeviatedByModules(createDeviationSetMultimap(supportedDeviations))
                    .build();
            EffectiveModelContextFactory factory = repo.createEffectiveModelContextFactory(factoryConfig);
            ListenableFuture<EffectiveModelContext> schemaContextFuture = factory.createEffectiveModelContext(sourceIds);
            return schemaContextFuture.get();
        } catch (Exception e) {
            Throwable deepestException = e;
            while (deepestException.getCause() != null && !deepestException.getCause().equals(deepestException)) {
                deepestException = deepestException.getCause();
            }
            throw new RuntimeException("Error parsing files: " + deepestException.getMessage(), e);
        }
    }

    public static List<SourceIdentifier> prepareSchemaRepo(SharedSchemaRepository repo, List<YangTextSchemaSource> inputFiles) throws SchemaSourceException, IOException, YangSyntaxErrorException, InterruptedException, java.util.concurrent.ExecutionException {
        List<SourceIdentifier> sourceIds = new ArrayList<>();
        for (YangTextSchemaSource yangSource : inputFiles) {
            ListenableFuture<ASTSchemaSource> aSTSchemaSource = Futures.immediateFuture(TextToASTTransformer.transformText(yangSource));
            SettableSchemaProvider<ASTSchemaSource> schemaProvider = SettableSchemaProvider.createImmediate(aSTSchemaSource.get(),
                    ASTSchemaSource.class);
            schemaProvider.setResult();
            schemaProvider.register(repo);
            sourceIds.add(schemaProvider.getId());
        }
        return sourceIds;
    }
    
    public static SetMultimap<QNameModule, QNameModule> createDeviationSetMultimap(Map<QName, Set<QName>> input) {
        SetMultimap<QNameModule, QNameModule> result = HashMultimap.create();
        if (input != null) {
            for (Entry<QName, Set<QName>> entry: input.entrySet()) {
                QNameModule key = createQNameModule(entry.getKey());
                for (QName value: entry.getValue()) {
                    result.put(key, createQNameModule(value));
                }
            }
        }
        return result;
    }
    
    private static QNameModule createQNameModule(QName qname) {
        return QNameModule.create(qname.getNamespace(), qname.getRevision());
    }

	private static List<YangTextSchemaSource> removeDuplicates(List<YangTextSchemaSource> inputFiles) {
        List<YangTextSchemaSource> result = new ArrayList<>();
        Set<SourceIdentifier> identifiers = new HashSet<>();
        for (YangTextSchemaSource inputFile: inputFiles) {
            if (identifiers.add(inputFile.getIdentifier())) {
                result.add(inputFile);
            }
        }
        return result;
    }

    public static Module getParsedModule(SchemaContext schemaContext, String yangModuleFileName) {
    	String fileName = FilenameUtils.getName(yangModuleFileName);
        SourceIdentifier sourceId = YangTextSchemaSource.identifierFromFilename(fileName);
        String moduleName = sourceId.getName();
        for (Module module : schemaContext.getModules()) {
            if (module.getName().equals(moduleName)) {
                return module;
            }
        }
        throw new RuntimeException("No module found for file name " + yangModuleFileName
                + ", is there a mismatch between the file name and the module name?");
    }

    public static YangTextSchemaSource getYangSource(URL url, InputStream in) throws IOException {
        String filename = FilenameUtils.getName(url.getPath());
        SourceIdentifier sourceId = YangTextSchemaSource.identifierFromFilename(filename);
        byte[] bytes = IOUtils.toByteArray(in);
        YangTextSchemaSource result = YangTextSchemaSource.delegateForByteSource(sourceId, ByteSource.wrap(bytes));
        return new FileDelegateYangSource(filename, result);
    }

    public static YangTextSchemaSource getYangSource(URL url) {
        String filename = FilenameUtils.getName(url.getPath());
        SourceIdentifier sourceId = YangTextSchemaSource.identifierFromFilename(filename);
        YangTextSchemaSource result = YangTextSchemaSource.delegateForByteSource(sourceId, Resources.asByteSource(url));
        return new FileDelegateYangSource(filename, result);
    }

    public static YangTextSchemaSource getYangSource(String filename) {
        return new FileYangSource(new File(filename));
    }

	public static YangTextSchemaSource getYangSourceFromContent(String moduleNameAndRevision, String yangContent) {
		byte[] bytes = yangContent.getBytes(Charset.forName("UTF-8"));
		SourceIdentifier sourceId = YangTextSchemaSource.identifierFromFilename(
				moduleNameAndRevision + YangConstants.RFC6020_YANG_FILE_EXTENSION);
		return YangTextSchemaSource.delegateForByteSource(sourceId, ByteSource.wrap(bytes));
	}

    public static YinTextSchemaSource getYinSource(String filename) {
        return new FileYinSource(new File(filename));
    }
    
    public static Set<ModuleImport> getAllModuleImports(Module module){
        Set<ModuleImport> imports = new HashSet<>(module.getImports());
        for ( Module subModule : module.getSubmodules()){
            imports.addAll(subModule.getImports());
        }
        return imports;
        
    }
}
