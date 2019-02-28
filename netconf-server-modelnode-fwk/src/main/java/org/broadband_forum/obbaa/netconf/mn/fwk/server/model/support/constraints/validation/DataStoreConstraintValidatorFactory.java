package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation;

import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.ModelNodeHelperRegistry;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.constraints.validation.util.DSExpressionValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.validation.ContainerValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.validation.LeafListValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.validation.LeafValidator;
import org.broadband_forum.obbaa.netconf.mn.fwk.validation.ListValidator;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

public class DataStoreConstraintValidatorFactory {
	private static DataStoreConstraintValidatorFactory c_instance = new DataStoreConstraintValidatorFactory();
	 private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(DataStoreConstraintValidatorFactory.class, LogAppNames.NETCONF_STACK);

	private DataStoreConstraintValidatorFactory() {}
	
	public static DataStoreConstraintValidatorFactory getInstance() {
		return c_instance;
	}
	
	public DataStoreConstraintValidator getValidator(DataSchemaNode schemaNode, SchemaRegistry schemaRegistry,
													 ModelNodeHelperRegistry modelNodeHelperRegistry, DSExpressionValidator expressionValidator) {
		if (schemaNode instanceof LeafSchemaNode) {
			return new LeafValidator(schemaRegistry, modelNodeHelperRegistry, (LeafSchemaNode) schemaNode, expressionValidator);
		} else if (schemaNode instanceof LeafListSchemaNode) {
			return new LeafListValidator(schemaRegistry, modelNodeHelperRegistry, (LeafListSchemaNode) schemaNode, expressionValidator);
		} else if (schemaNode instanceof ContainerSchemaNode) {
			return new ContainerValidator(schemaRegistry, modelNodeHelperRegistry, (ContainerSchemaNode) schemaNode, expressionValidator);
		} else if (schemaNode instanceof ListSchemaNode) {
			return new ListValidator(schemaRegistry, modelNodeHelperRegistry, (ListSchemaNode) schemaNode, expressionValidator);
		} else if (schemaNode instanceof AnyXmlSchemaNode) {
			return new AnyXmlDataStoreConstraintValidator(schemaRegistry, modelNodeHelperRegistry, (AnyXmlSchemaNode) schemaNode);
		} else{
		    if (LOGGER.isDebugEnabled()) {
	            LOGGER.debug("{} of type {} is not supported",schemaNode.getQName(),schemaNode.getClass());
		    }
		}
		
		return null;
	}
}
