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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.service;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExtensionFunctionValidator.DERIVED_FROM;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExtensionFunctionValidator.DERIVED_FROM_OR_SELF;

import java.util.List;

import org.apache.commons.jxpath.ri.compiler.Constant;
import org.apache.commons.jxpath.ri.compiler.CoreOperation;
import org.apache.commons.jxpath.ri.compiler.Expression;
import org.apache.commons.jxpath.ri.compiler.ExtensionFunction;
import org.apache.commons.jxpath.ri.compiler.LocationPath;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.ReferringNode;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.jxpath.JXPathUtils;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class AugmentWhenConditionAnalyser {
    public boolean areAugmentConditionsUnderAugmentingSubtree(SchemaPath augmentTarget, ReferringNode referringNode) {
        if (canXpathNotBeOptimised(referringNode.getConstraintXPath())) {
            return false;
        }
        SchemaPath commonParent = getCommonParent(referringNode.getReferredSP(), referringNode.getReferringSP());
        if (commonParent == null) {
            return false;
        }
        return augmentTarget.equals(commonParent);
    }

    private boolean canXpathNotBeOptimised(String constraintXPathString) {
        Expression expression = JXPathUtils.getExpression(constraintXPathString);
        return canXpathNotBeOptimised(expression);
    }

    private boolean canXpathNotBeOptimised(Expression expression) {
        if (expression instanceof LocationPath) {
            if (((LocationPath) expression).getSteps().length > 1) {
                return true;
            }
            return false;
        } else if (expression instanceof CoreOperation) {
            for (Expression exp : ((CoreOperation) expression).getArguments()) {
                if (canXpathNotBeOptimised(exp)) {
                    return true;
                }
            }
            return false;
        } else if (expression instanceof Constant) {
            return false;
        } else if (expression instanceof ExtensionFunction) {
            ExtensionFunction extnFunc = (ExtensionFunction) expression;
            if ((extnFunc.getFunctionName().getName().equals(DERIVED_FROM)
                    || extnFunc.getFunctionName().getName().equals(DERIVED_FROM_OR_SELF))) {
                for (Expression exp : extnFunc.getArguments()) {
                    if (canXpathNotBeOptimised(exp)) {
                        return true;
                    }
                }
                return false;
            }
        }

        return true;
    }

    private SchemaPath getCommonParent(SchemaPath referredSP, SchemaPath referringSP) {
        List<QName> referredSpParts = referredSP.getPath();
        List<QName> referringSpParts = referringSP.getPath();
        int index = 0;
        int maxlength = Math.min(referredSpParts.size(), referringSpParts.size());
        for (; index < maxlength; index++) {
            QName referredSpPart = referredSpParts.get(index);
            QName referringSpPart = referringSpParts.get(index);
            if (!referredSpPart.equals(referringSpPart)) {
                break;
            }
        }
        if (index != 0) {
            return SchemaPath.create(true, referredSpParts.subList(0, index).toArray(new QName[index]));
        }
        return null;
    }
}
