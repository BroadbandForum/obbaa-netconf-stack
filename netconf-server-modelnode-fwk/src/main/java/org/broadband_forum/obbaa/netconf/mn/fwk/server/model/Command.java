package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;


public interface Command {
	public void execute() throws CommandExecutionException;
}
