package org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing;

import java.util.HashMap;
import java.util.Map;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.validation.ContainerValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.validation.LeafListValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.validation.LeafValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.validation.ListValidator;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

/**
 * Created by keshava on 11/23/15.
 */
public class ConstraintValidatorFactoryImpl implements ConstraintValidatorFactory {
    private static ConstraintValidatorFactory c_instance = new ConstraintValidatorFactoryImpl();
    private Map<DataSchemaNode, SchemaNodeConstraintParser> m_constraintValidators = new HashMap<>();
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ConstraintValidatorFactoryImpl.class, LogAppNames.NETCONF_STACK);


    public static ConstraintValidatorFactory getInstance() {
        return c_instance;
    }

    private ConstraintValidatorFactoryImpl(){

    }
    
    @Override
    public void clearCache() {
        m_constraintValidators.clear();
    }
    
    @Override
    public SchemaNodeConstraintParser getConstraintNodeValidator(DataSchemaNode dataSchemaNode, SchemaRegistry schemaRegistry,
            DSExpressionValidator expValidator) {
    	SchemaNodeConstraintParser schemaNodeConstraintParser = schemaRegistry.getSchemaNodeConstraintParser(dataSchemaNode);
    	if (schemaNodeConstraintParser == null) {
    		if(dataSchemaNode instanceof LeafSchemaNode){
    			schemaNodeConstraintParser = new LeafValidator(schemaRegistry, null, (LeafSchemaNode)dataSchemaNode, expValidator);
    		}
    		else if(dataSchemaNode instanceof LeafListSchemaNode){
    			schemaNodeConstraintParser = new LeafListValidator(schemaRegistry, null, (LeafListSchemaNode)dataSchemaNode, expValidator);
    		} else if(dataSchemaNode instanceof ContainerSchemaNode){
    			schemaNodeConstraintParser =  new ContainerValidator(schemaRegistry, null, (ContainerSchemaNode)dataSchemaNode, expValidator);
    		} else if(dataSchemaNode instanceof ListSchemaNode) {
    			schemaNodeConstraintParser = new ListValidator(schemaRegistry, null, (ListSchemaNode) dataSchemaNode, expValidator);
    		} else{
    			LOGGER.warn("{} of type {} is not supported" ,dataSchemaNode.getQName() , dataSchemaNode.getClass().toString());
    		}
		}
    	if (schemaNodeConstraintParser != null){
    		schemaRegistry.putSchemaNodeConstraintParser(dataSchemaNode, schemaNodeConstraintParser);
    	}

        return schemaNodeConstraintParser;
    }
}
