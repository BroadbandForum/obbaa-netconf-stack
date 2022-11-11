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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.dom;


import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.dom.EncryptDecryptUtil.getChildrenOfType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.common.QName;
import org.w3c.dom.Element;

import com.google.common.base.Stopwatch;

public abstract class DOMVisitor {

    protected static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DOMVisitor.class, LogAppNames.NETCONF_STACK);
    public static final String IS_PASSWORD = "is-password";

    protected final Element m_root;
    protected final QName m_rootQName;

    public DOMVisitor(Element root){
        m_root = root;
        m_rootQName = QName.create(root.getNamespaceURI(), root.getLocalName());
    }

    public abstract void visit(List<Element> elementOfType);

    public Element traverse() {
        Stopwatch stopWatch = Stopwatch.createStarted();
        visitInternal(m_root);
        stopWatch.stop();
        LOGGER.debug("Time taken to traverse dom is " + stopWatch.elapsed(TimeUnit.MILLISECONDS));
        return m_root;
    }

    protected void visitInternal(Element subtreeRoot) {
        visit(Arrays.asList(subtreeRoot));

        Map<QName, List<Element>> childrenOfType = getChildrenOfType(subtreeRoot);
        childrenOfType.forEach((qname, elements) -> visit(elements));

        childrenOfType.values()
                .forEach(elementList ->
                        elementList.forEach(childOfType -> {
                            List<Element> childNodes = DocumentUtils.getChildElements(childOfType);
                            childNodes.forEach(child -> visitInternal(child));
                        }));
    }
}
