package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

/*
 * flag containing the boolean value whether a new node is created or existing deleted for current netconf request called from  restconf 'put' operation, in  order to send correct status code to the client
 */
public class FlagForRestPutOperations {
     
	public static ThreadLocal<Boolean> m_instanceReplace = new ThreadLocal<Boolean>(){
		@Override
		protected Boolean initialValue() {
			return false;	
		};
	};
    
	public static boolean getInstanceReplaceFlag() {
		return m_instanceReplace.get();
	}
	
	public static void setInstanceReplaceFlag() {
		 m_instanceReplace.set(true);
	}
	
	public static void resetInstanceReplaceFlag() {
		 m_instanceReplace.set(false);
	}
}
