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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath;

import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.Step;

public class YangStep extends Step {

    public YangStep(QName qname, String namespace) {
        super(Compiler.AXIS_CHILD, new NodeNameTest(qname, namespace), new Expression[0]);
    }

    public YangStep(QName qname, String namespace, Expression[] predicates) {
        super(Compiler.AXIS_CHILD, new NodeNameTest(qname, namespace), predicates);
    }

    public YangStep(QName qname) {
        super(Compiler.AXIS_CHILD, new NodeNameTest(qname), new Expression[0]);
    }
}
