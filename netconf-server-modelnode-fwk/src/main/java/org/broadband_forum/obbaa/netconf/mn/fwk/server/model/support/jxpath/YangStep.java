package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath;

import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.Step;

public class YangStep extends Step{

    public YangStep(QName qname, String namespace) {
        super(Compiler.AXIS_CHILD, new NodeNameTest(qname, namespace), new Expression[0]);
    }
    
    public YangStep(QName qname, String namespace, Expression[] predicates) {
        super(Compiler.AXIS_CHILD, new NodeNameTest(qname, namespace), predicates);
    }

    public YangStep(QName qname) {
        super(Compiler.AXIS_CHILD, new NodeNameTest(qname), new Expression[0]);
    }
    
    public YangStep(Step oldStep, Expression[] newPredicates) {
        super(oldStep.getAxis(), oldStep.getNodeTest(), newPredicates);
    }
}
