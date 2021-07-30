package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.Constants.REGEX_EQUAL_TO_WITH_NO_SLASH_PREFIX;
import static org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.Constants.REGEX_FORWARD_SLASH_WITH_NO_SLASH_PREFIX;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.broadband_forum.obbaa.netconf.api.utils.tree.NaturalOrderComparator;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.SchemaRegistryImpl;
import org.broadband_forum.obbaa.netconf.mn.fwk.schema.constraints.payloadparsing.util.SchemaRegistryUtil;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.CharacterCodec;
import org.broadband_forum.obbaa.netconf.server.RequestScope;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.broadband_forum.obbaa.netconf.stack.logging.LogAppNames;

public class ModelNodeId implements Comparable<ModelNodeId>, Serializable {
	
    private static final long serialVersionUID = 2500631272450246422L;

    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ModelNodeId.class, LogAppNames.NETCONF_STACK);

    public static final String EQUAL_TO = "=";
    private static final String SLASH = "/";
    public static final ModelNodeId EMPTY_NODE_ID = new ModelNodeId();
    List<ModelNodeRdn> m_rdns = new ArrayList<ModelNodeRdn>();
    private volatile String m_pathString;
    private volatile String m_xPathString;
    private Integer m_rdnsHash;
    private static final String COMPARE_CACHE = "CompareCache";

    @SuppressWarnings("unchecked")
    private static Map<String, Map<String,Integer>> getCache() {
        RequestScope scope = RequestScope.getCurrentScope();
        Map<String, Map<String,Integer>> map = (Map<String, Map<String, Integer>>) scope.getFromCache(COMPARE_CACHE);
        if (map == null) {
            map = new HashMap<String, Map<String,Integer>>(2000);
            scope.putInCache(COMPARE_CACHE, map);
        }
        return map;
    }
    private static void addToCache(String params1, String params2, int value) {
        Map<String, Map<String,Integer>> cache = getCache();
        Map<String,Integer> map = cache.get(params1);
        if (map == null) {
            map = new HashMap<String,Integer>(500);
            cache.put(params1, map);
        }
        map.put(params2, value);
    }
    
    private static Integer getValue(String params1, String params2) {
        Map<String, Map<String,Integer>> cache = getCache();
        Map<String, Integer> map = cache.get(params1);
        if (map != null) {
            return map.get(params2);
        }
        return null;
    }

    public ModelNodeId(String idStr, String namespace) {
        String[] pieces = idStr.split(REGEX_FORWARD_SLASH_WITH_NO_SLASH_PREFIX);
        for (String idPiece : pieces) {
            if (!"".equals(idPiece.trim())) {
                String[] rdnPieces = idPiece.split(REGEX_EQUAL_TO_WITH_NO_SLASH_PREFIX);
                ModelNodeRdn rdn = new ModelNodeRdn(rdnPieces[0], namespace, CharacterCodec.decode(rdnPieces[1]));
                m_rdns.add(rdn);
            }
        }
    }

    public ModelNodeId(List<ModelNodeRdn> rdns) {
        this.m_rdns = rdns;
    }

    public ModelNodeId() {
    }

    public ModelNodeId(ModelNodeId modelNodeId) {
        if (modelNodeId != null) {
            for (ModelNodeRdn rdn : modelNodeId.getRdns()) {
                m_rdns.add(rdn);
            }
        }
    }

    public ModelNodeId clone() {
        ModelNodeId copy = new ModelNodeId();
        for (ModelNodeRdn rdn : m_rdns) {
            copy.addRdn(rdn);
        }
        return copy;
    }

    /**
     * Checks whether this ModelNodeId begins with otherId.
     * 
     * @param otherId
     * @return
     */
    public boolean beginsWith(ModelNodeId otherId) {
        if (otherId.getRdns().size() > m_rdns.size()) {
            return false;
        }

        for (int i = 0; i < otherId.getRdns().size(); i++) {
            if (!otherId.getRdns().get(i).equals(m_rdns.get(i))) {
                return false;
            }
        }

        return true;
    }

    public List<ModelNodeRdn> getRdns() {
        return new ArrayList<ModelNodeRdn>(this.m_rdns);
    }

    public List<ModelNodeRdn> getRdnsReadOnly() {
        return this.m_rdns;
    }

    public String getRdnValue(String rdnName) {
        for (ModelNodeRdn rdn : m_rdns) {
            if (rdn.getRdnName().equals(rdnName)) {
                return rdn.getRdnValue();
            }
        }
        return null;
    }

    public ModelNodeRdn getLastRdn(String rdnName) {
        ListIterator<ModelNodeRdn> itr = m_rdns.listIterator(m_rdns.size());
        while(itr.hasPrevious()){
            ModelNodeRdn rdn = itr.previous();
            if (rdn.getRdnName().equals(rdnName)) {
                return rdn;
            }
        }
        return null;
    }

    public ModelNodeRdn getFirstRdn(String rdnName) {
        ListIterator<ModelNodeRdn> itr = m_rdns.listIterator();
        while(itr.hasNext()){
            ModelNodeRdn rdn = itr.next();
            if (rdn.getRdnName().equals(rdnName)) {
                return rdn;
            }
        }
        return null;
    }

    public String getRdnValue(String rdnName, String namespace) {
        for (ModelNodeRdn rdn : m_rdns) {
            if (rdn.getRdnName().equals(rdnName) && rdn.getNamespace().equals(namespace)) {
                return rdn.getRdnValue();
            }
        }
        return null;
    }


    public synchronized ModelNodeId addRdn(ModelNodeRdn rdn) {
        this.m_rdns.add(rdn);
        m_rdnsHash = null;
        m_xPathString = null;
        return this;
    }

    public synchronized ModelNodeId addRdn(String rdnName, String namespace, String rdnValue) {
        this.m_rdns.add(new ModelNodeRdn(rdnName, namespace, rdnValue));
        m_rdnsHash = null;
        m_xPathString = null;
        return this;
    }

    public synchronized void removeFirst(int numbOfRdnsToChop) {
        if (m_rdns.size() >= numbOfRdnsToChop) {
            for (int i = 0; i < numbOfRdnsToChop; i++) {
                m_rdns.remove(0);
            }
            m_rdnsHash = null;
            m_xPathString = null;
        } else {
            LOGGER.error("Cannot remove the specified number of RDNs");
        }
    }

    public String xPathString() {
        if (m_xPathString == null) {
            synchronized (this) {
                if (m_xPathString == null) {
                    m_xPathString = xPathString(null);
                }
            }
        }
        return m_xPathString;
    }

    public String xPathString(NamespaceContext namespaceContext) {
        StringBuilder sb = new StringBuilder();
        if (!m_rdns.isEmpty()) {
            for (ModelNodeRdn rdn : m_rdns) {
                String prefix = "";
                if (namespaceContext != null) {
                    prefix = getPrefix(namespaceContext, rdn.getNamespace());
                    if (prefix == null) {
                        prefix = "";
                    } else {
                        prefix += ":";
                    }
                }

                if (rdn.getRdnName().equals(ModelNodeRdn.CONTAINER)) {
                    sb.append("/").append(prefix).append(rdn.getRdnValue());
                } else {
                    sb.append("[").append(prefix).append(rdn.getRdnName()).append("=").append(rdn.getRdnValueForXPath()).append("]");
                }
            }
        } else {
            sb.append("/");
        }
        return sb.toString();
    }
    
    private String getPrefix(NamespaceContext namespaceContext, String namespace) {
        String returnValue = null;
        if (namespaceContext != null) {
            returnValue = namespaceContext.getPrefix(namespace);
        }
        if (returnValue == null) {
            NamespaceContext mountContext;
            if(namespaceContext instanceof SchemaRegistryImpl
                    && SchemaRegistryUtil.isMountPointEnabled()
                    && ((SchemaRegistryImpl) namespaceContext).getParentRegistry() != null) {
                mountContext = ((SchemaRegistryImpl) namespaceContext).getParentRegistry();
            } else {
                mountContext = SchemaRegistryUtil.getMountRegistry();
            }
            if (mountContext != null) {
                return mountContext.getPrefix(namespace);
            }
        } else {
            return returnValue;
        }
        return returnValue;
    }

    public Map<String, String> xPathStringNsByPrefix(NamespaceContext namespaceContext) {
        Map<String, String> namespaceByPrefix = new HashMap<>();

        for (ModelNodeRdn rdn : m_rdns) {
            String prefix = getPrefix(namespaceContext, rdn.getNamespace());
            if (prefix != null) {
                namespaceByPrefix.put(prefix, rdn.getNamespace());
            }
        }
        return namespaceByPrefix;
    }

    public String appendNameToXPath(String name) {
        StringBuilder sb = new StringBuilder();
        sb.append(xPathString()).append("/").append(name);
        return sb.toString();
    }

    public String pathString() {
        if (m_pathString == null) {
            synchronized (this) {
                if(m_pathString == null) {
                    StringBuilder sb = new StringBuilder();
                    if (!m_rdns.isEmpty()) {
                        for (ModelNodeRdn rdn : m_rdns) {
                            sb.append("/");
                            sb.append(rdn.getRdnValue());
                        }
                    } else {
                        sb.append("/");
                    }
                    m_pathString = sb.toString();
                }
            }
        }
        return m_pathString;
    }

    private int getRdnsHashCode() {
        if (m_rdnsHash == null && m_rdns != null) {
            synchronized (this){
                if(m_rdnsHash == null){
                    m_rdnsHash = this.m_rdns.hashCode();
                }
            }
        }
        return m_rdnsHash;
    }
    @Override
    public int hashCode() {
        final Integer rdnsHash = getRdnsHashCode(); 
        final int prime = 31;
        int result = 1;
        result = prime * result + ((rdnsHash == null) ? 0 : rdnsHash);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ModelNodeId other = (ModelNodeId) obj;
        if (this.m_rdns == null) {
            if (other.m_rdns != null) {
                return false;
            }
        } else if (!this.m_rdns.equals(other.m_rdns)) {
            return false;
        }
        return true;
    }

    public String getModelNodeIdAsString() {
        StringBuilder sb = new StringBuilder();
        for (ModelNodeRdn rdn : m_rdns) {
            sb.append(rdn.encodedString());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ModelNodeId[");
        for (ModelNodeRdn rdn : m_rdns) {
            sb.append(rdn.toString());
        }
        sb.append("]");
        return sb.toString();
    }

    public void appendRdns(List<ModelNodeRdn> rdns) {
        m_rdns.addAll(rdns);
    }

    /**
     * Compares the structure of the this ModelNodeId with the supplied template. If each RDN name in the given node should be same as that
     * in the supplied template, then the method returns true. The RDN values are not considered for the comparison.
     *
     * This ModelNodeId could have more number of RDNs than the template.
     * 
     * @param template
     * @return
     */
    public boolean beginsWithTemplate(ModelNodeId template) {
        for (int i = 0; i < template.getRdns().size(); i++) {
            if (i >= m_rdns.size()) {
                return false;
            }
            ModelNodeRdn templateRdn = template.getRdns().get(i);
            ModelNodeRdn rdn = m_rdns.get(i);
            if (!templateRdn.getRdnName().equals(rdn.getRdnName())) {
                return false;
            }

            if (ModelNodeRdn.CONTAINER.equals(templateRdn.getRdnName()) && !templateRdn.getRdnValue().equals(rdn.getRdnValue())) {
                return false;
            }

            if (templateRdn.getNamespace() != null) {
                if (!templateRdn.getNamespace().equals(rdn.getNamespace())) {
                    return false;
                }
            } else {
                if (rdn.getNamespace() != null) {
                    return false;
                }
            }

        }
        return true;
    }

    public boolean beginsWithTemplateIgnoreKeyOrder(ModelNodeId template) {
        if (template.getRdns().size() > m_rdns.size()) {
            return false;
        }
        List<String> templateKeys = new LinkedList<>();
        List<String> thisKeys = new LinkedList<>();
        boolean insideContainer = false;
        // Assumption: There will be a container RDN at position-zero
        for (int index = 0; index < template.getRdns().size(); index++) {
            ModelNodeRdn thisRdn = m_rdns.get(index);
            ModelNodeRdn templateRdn = template.getRdns().get(index);
            if (ModelNodeRdn.CONTAINER.equals(templateRdn.getRdnName())) {
                if (insideContainer) {
                    if (!thisKeys.isEmpty() && !thisKeys.containsAll(templateKeys)) {
                        return false;
                    }
                    templateKeys.clear();
                    thisKeys.clear();
                }
                if (!thisRdn.getRdnValue().equals(templateRdn.getRdnValue())) {
                    return false;
                }
                if (templateRdn.getNamespace() != null) {
                    if (!templateRdn.getNamespace().equals(thisRdn.getNamespace())) {
                        return false;
                    }
                } else {
                    if (thisRdn.getNamespace() != null) {
                        return false;
                    }
                }
                insideContainer = true;
                templateKeys.clear();
                thisKeys.clear();
            } else {
                if (insideContainer) {
                    // Namespace is not considered for matching keys as they can't be augmented.
                    thisKeys.add(thisRdn.getRdnName());
                    templateKeys.add(templateRdn.getRdnName());
                } else {
                    if (!thisRdn.getRdnName().equals(templateRdn.getRdnName())) {
                        return false;
                    }
                }
            }
        }
        if (insideContainer && !templateKeys.isEmpty() && m_rdns.size() > template.getRdns().size()) {
            // Continue to search in 'this' instance to cover the case where template mentions less keys (in wrong order)
            for (int indexToResumeFrom = template.getRdns().size(); indexToResumeFrom < m_rdns.size(); indexToResumeFrom++) {
                ModelNodeRdn thisRdn = m_rdns.get(indexToResumeFrom);
                // We're concerned only about keys of same list
                if (ModelNodeRdn.CONTAINER.equals(thisRdn.getRdnName())) {
                    break;
                }
                thisKeys.add(thisRdn.getRdnName());
            }
        }
        if (insideContainer && !thisKeys.isEmpty() && !thisKeys.containsAll(templateKeys)) {
            return false;
        }
        return true;
    }

    public synchronized void addRdns(List<ModelNodeRdn> rdns) {
        m_rdns.addAll(rdns);
        m_rdnsHash = null;
        m_xPathString = null;
    }

    /**
     * checks whether the ModelNodeId has the exact same sequence of RDNs as the given template.
     * 
     * @param template
     * @return
     */
    public boolean matchesTemplate(ModelNodeId template) {
        if (this.getRdns().size() != template.getRdns().size()) {
            return false;
        }
        return beginsWithTemplate(template);
    }

    /**
     * Provides Parent ModelNodeId of invoked ModelNodeId instance
     * 
     * @return the parentId of modelNodeId of
     */
    public ModelNodeId getParentId() {
        int parentIRdnSize = findParentRdnSize();
        ModelNodeId parentId = new ModelNodeId();
        for (int containerIndex = 0; containerIndex < parentIRdnSize; containerIndex++) {
            ModelNodeRdn rdn = m_rdns.get(containerIndex);
            parentId.addRdn(rdn);
        }
        return parentId;
    }

    /**
     * find parent ModelNodeId Rdn Size
     * 
     * @return
     */
    private int findParentRdnSize() {
        for (int index = m_rdns.size() - 1; index > 0; index--) {
            ModelNodeRdn rdn = m_rdns.get(index);
            if (ModelNodeRdn.CONTAINER.equals(rdn.getRdnName())) {
                return index;
            }
        }
        return 0;
    }

    public int getDepth() {
        int depth = 0;
        for (ModelNodeRdn rdn : m_rdns) {
            if (ModelNodeRdn.CONTAINER.equals(rdn.getRdnName())) {
                depth++;
            }
        }
        return depth;
    }
    
    
    public synchronized void addRdn(int index, ModelNodeRdn rdn) {
        m_rdns.add(index, rdn);
        m_rdnsHash = null;
        m_xPathString = null;
    }
    
    /**
     * Compares two ModelNodeId objects as follows,
     * 
     * <pre>
     * 
     * case 1. compare same xpath modelNodeIds
     * 
     *     "/anv:platform/users/user[name=admin]"   <  "/anv:platform/users/user[name=guest]"
     * 
     * case 2 comparing different xpath modelNodeIds
     * 
     *      "/anv:device-manager"   <   "/platform:platform"
     *      
     * case 3 compare xpath parent and child modelnodeIds
     * 
     *      "/anv:platform/"  <   "/anv:platform/users"
     *      
     * </pre>
     * 
     */
    public int compareTo(ModelNodeId otherModelNodeId) {
        int diff = 0;
        NaturalOrderComparator naturalOrderComparator = new NaturalOrderComparator();
        List<ModelNodeRdn> otherRdns = otherModelNodeId.getRdns();
        Integer size = m_rdns.size();
        Integer otherSize = otherRdns.size();
        //compare each node rdns
        for(int index =0; index < size && index < otherSize; index++) {
            ModelNodeRdn thisRdn = m_rdns.get(index);
            ModelNodeRdn otherRdn = otherRdns.get(index);
            String params1 = thisRdn.getRdnValue();
            String params2 = otherRdn.getRdnValue();
            Integer value = getValue(params1, params2);
            if (value != null && value != 0) {
                return value;
            } else if (value != null && value == 0) {
               continue;  
            } else {
                diff=naturalOrderComparator.compare(params1, params2);
                addToCache(params1, params2, diff);
                if(diff != 0) {
                    return diff;
                }
            }
        }
        //compare parent and child mode node Ids
        return size.compareTo(otherSize);
    }

    public ModelNodeRdn getLastRdn() {
        if(m_rdns != null && !m_rdns.isEmpty()){
            return m_rdns.get(m_rdns.size() - 1);
        }
        return null;
    }

    public boolean isRootNodeId() {
        return (getRdns().size() == 1);
    }
    /**
     * Provides the next RDN link between the current ModelNodeId and its parentModelNodeId
     * 
     * Note: The caller of this method must ensure the ParentModelNodeId is indeed
     * a (super) parent of this modelNodeId before calling this method. 
     */
    public ModelNodeRdn getNextChildRdn(ModelNodeId parentModelNodeId) {
        return m_rdns.get(parentModelNodeId.getRdns().size());
    }
    
    /**
     * Provides the next ModelNodeId link between the current ModelNodeId and its parentModelNodeId
     * 
     * Note: The caller of this method must ensure the ParentModelNodeId is indeed
     * a (super) parent of this modelNodeId before calling this method. 
     */
    public ModelNodeId getNextChildId(ModelNodeId parentModelNodeId) {
        int i = parentModelNodeId.getRdns().size();
        while (i < m_rdns.size()) {
            i++;
            if (i >= m_rdns.size() || m_rdns.get(i).getRdnName().equals(ModelNodeRdn.CONTAINER)) {
                break;
            }
        }
        
        ModelNodeId newId = new ModelNodeId(parentModelNodeId);
        for (int j=newId.m_rdns.size();j<i;j++) {
            newId.addRdn(m_rdns.get(j));
        }
        return newId;
    }
}
