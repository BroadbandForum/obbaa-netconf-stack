package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.service;

import java.util.Collection;
import java.util.Optional;

import org.apache.commons.jxpath.ri.compiler.Expression;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DataStoreValidationUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.MustConstraintAware;
import org.opendaylight.yangtools.yang.model.api.MustDefinition;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.WhenConditionAware;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.ModelNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.DSValidation;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;

public class DSConstraintValidation implements DSValidation {
    private final DSExpressionValidator m_expValidator;
    
    public DSConstraintValidation(DSExpressionValidator expValidator) {
        m_expValidator = expValidator;
    }
    @Override
    public Boolean evaluate(ModelNode parentNode, DataSchemaNode child) {
        boolean returnValue = true;
        Collection<MustDefinition> definitions = null;
        RevisionAwareXPath whenCondition = null;
        if (child instanceof MustConstraintAware) {
            definitions = ((MustConstraintAware) child).getMustConstraints();
        }
        if (child instanceof WhenConditionAware) {
            Optional<RevisionAwareXPath> optWhenCondition = child.getWhenCondition();
            if (optWhenCondition.isPresent()) {
                whenCondition = optWhenCondition.get();
            }
        }
        if (definitions != null && !definitions.isEmpty()) {
            for (MustDefinition must: definitions) {
                m_expValidator.validateMust(must, child, parentNode);
            }
        }
        else if (whenCondition != null) {
            // we cannot have both must and when. If must fails, we throw an error. We dont tolerate.
            Expression when = JXPathUtils.getExpression(whenCondition.toString());
            returnValue = m_expValidator.validateWhen(when, child, parentNode, DataStoreValidationUtil.getValidationContext().getImpactValidation());
        }
        return returnValue;
    }

}
