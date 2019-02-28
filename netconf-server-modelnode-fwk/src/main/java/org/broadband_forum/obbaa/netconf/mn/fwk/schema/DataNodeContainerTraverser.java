package org.broadband_forum.obbaa.netconf.mn.fwk.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.opendaylight.yangtools.yang.model.api.ActionDefinition;
import org.opendaylight.yangtools.yang.model.api.ActionNodeContainer;
import org.opendaylight.yangtools.yang.model.api.CaseSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.NotificationDefinition;
import org.opendaylight.yangtools.yang.model.api.NotificationNodeContainer;
import org.opendaylight.yangtools.yang.model.api.RpcDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaNode;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Created by keshava on 11/20/15.
 */
public class DataNodeContainerTraverser {
    public static void traverse(DataNodeContainer dataNodeContainer, SchemaNodeVisitor visitor){
        DataNodeContainerTraverser  dataNodeContainerTraverser = new DataNodeContainerTraverser(dataNodeContainer);
        Iterator<SchemaNode> iterator = dataNodeContainerTraverser.getIterator();
        while(iterator.hasNext()){
            SchemaNode schemaNode = iterator.next();
            visitor.visit(schemaNode);
        }
    }

    private final DataNodeContainer m_container;
    private final List<SchemaNode> m_allChildren;
    
    private final List<ListSchemaNode> m_allLists;
    private final List<ContainerSchemaNode> m_allContainers;
    private final List<ChoiceSchemaNode> m_allChoices;
    private final List<GroupingDefinition> m_allGroupings;
    private final List<TypeDefinition<?>> m_allTypedefs;

    /*
        almost similar to org.opendaylight.yangtools.yang.model.util.DataNodeIterator, but org.opendaylight.yangtools.yang.model.util.DataNodeIterator iterator is buggy.

     */
    private DataNodeContainerTraverser(final DataNodeContainer container) {
        if (container == null) {
            throw new IllegalArgumentException("Data Node Container MUST be specified and cannot be NULL!");
        }

        this.m_allContainers = new ArrayList<>();
        this.m_allLists = new ArrayList<>();
        this.m_allChildren = new ArrayList<>();
        this.m_allChoices = new ArrayList<>();
        this.m_allGroupings = new ArrayList<>();
        this.m_allTypedefs = new ArrayList<>();

        this.m_container = container;
        traverse(this.m_container);
    }

    /**
     * Returns list all containers present in subtree.
     *
     * @return Returns list all containers present in subtree.
     */
    public List<ContainerSchemaNode> allContainers() {
        return m_allContainers;
    }

    /**
     * Returns list all lists present in subtree.
     *
     * @return Returns list all containers present in subtree.
     */
    public List<ListSchemaNode> allLists() {
        return m_allLists;
    }

    /**
     * Returns list all choices present in subtree.
     *
     * @return Returns list all containers present in subtree.
     */
    public List<ChoiceSchemaNode> allChoices() {
        return m_allChoices;
    }

    /**
     * Returns list all groupings present in subtree.
     *
     * @return Returns list all containers present in subtree.
     */
    public List<GroupingDefinition> allGroupings() {
        return m_allGroupings;
    }

    /**
     * Returns list all typedefs present in subtree.
     *
     * @return Returns list all containers present in subtree.
     */
    public List<TypeDefinition<?>> allTypedefs() {
        return m_allTypedefs;
    }

    private void traverse(final DataNodeContainer dataNode) {
        if (dataNode == null) {
            return;
        }

        final Iterable<DataSchemaNode> childNodes = dataNode.getChildNodes();
        if (childNodes != null) {
            for (DataSchemaNode childNode : childNodes) {
                /*
                    Not sure why org.opendaylight.yangtools.yang.model.util.DataNodeIterator ignores walking over augmenting nodes
                if (childNode.isAugmenting()) {
                    continue;
                }*/
                m_allChildren.add(childNode);
                if (childNode instanceof ContainerSchemaNode) {
                    final ContainerSchemaNode containerNode = (ContainerSchemaNode) childNode;
                    m_allContainers.add(containerNode);
                    handleDataNodeContainer(containerNode);
                } else if (childNode instanceof ListSchemaNode) {
                    final ListSchemaNode list = (ListSchemaNode) childNode;
                    m_allLists.add(list);
                    handleDataNodeContainer(list);
                } else if (childNode instanceof ChoiceSchemaNode) {
                    final ChoiceSchemaNode choiceNode = (ChoiceSchemaNode) childNode;
                    m_allChoices.add(choiceNode);
                    final Collection<CaseSchemaNode> cases = choiceNode.getCases().values();
                    if (cases != null) {
                        for (final CaseSchemaNode caseNode : cases) {
                        	m_allChildren.add(caseNode);
                            traverse(caseNode);
                        }
                    }
                }
            }
        }

        this.m_allTypedefs.addAll(dataNode.getTypeDefinitions());
        traverseModule(dataNode);
        traverseGroupings(dataNode);

    }

    private void handleDataNodeContainer(final DataNodeContainer dataNodeContainer) {
        if (dataNodeContainer instanceof ActionNodeContainer) {
            for(ActionDefinition action : ((ActionNodeContainer)dataNodeContainer).getActions()){
                m_allChildren.add(action);
                m_allChildren.add(action.getInput());
                m_allChildren.add(action.getOutput());
                traverse(action.getInput());
                traverse(action.getOutput());
            }
        }
        if (dataNodeContainer instanceof NotificationNodeContainer) {
            for (NotificationDefinition notification: ((NotificationNodeContainer)dataNodeContainer).getNotifications()) {
                m_allChildren.add(notification);
                traverse(notification);
            }
        }
        traverse(dataNodeContainer);
    }
    
    private void traverseModule(final DataNodeContainer dataNode) {
        final Module module;
        if (dataNode instanceof Module) {
            module = (Module) dataNode;
        final Set<NotificationDefinition> notifications = module.getNotifications();
        for (NotificationDefinition notificationDefinition : notifications) {
            traverse(notificationDefinition);
        }
        } else if(dataNode instanceof SchemaContext){

	        final Set<NotificationDefinition> notifications = ((SchemaContext)dataNode).getNotifications();
	        for (NotificationDefinition notificationDefinition : notifications) {
	            m_allChildren.add(notificationDefinition);
	            traverse(notificationDefinition);
	        }

        final Set<RpcDefinition> rpcs = ((SchemaContext)dataNode).getOperations();
        for (RpcDefinition rpcDefinition : rpcs) {
            this.m_allTypedefs.addAll(rpcDefinition.getTypeDefinitions());
            ContainerSchemaNode input = rpcDefinition.getInput();
            if (input != null) {
            	m_allChildren.add(input);
                traverse(input);
            }
            ContainerSchemaNode output = rpcDefinition.getOutput();
            if (output != null) {
            	m_allChildren.add(output);
                traverse(output);
            }
        }
    }
    }

    private void traverseGroupings(final DataNodeContainer dataNode) {
        final Set<GroupingDefinition> groupings = dataNode.getGroupings();
        if (groupings != null) {
            for (GroupingDefinition grouping : groupings) {
                m_allGroupings.add(grouping);
                traverse(grouping);
            }
        }
    }

    public Iterator<SchemaNode> getIterator(){
        return m_allChildren.iterator();
    }
    
}
