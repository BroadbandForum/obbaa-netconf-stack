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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model.support.commands;

import java.util.ArrayList;
import java.util.List;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.CommandExecutionException;

import org.broadband_forum.obbaa.netconf.api.messages.EditConfigErrorOptions;
import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.Command;

import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.LoggerFactory;

public class CompositeEditCommand implements Command {

    private static final String NEW_LINE = System.getProperty("line.separator");

    public CompositeEditCommand() {
    }

    private List<Command> m_commands = new ArrayList<Command>();
    private String m_errorOption;
    private List<String> m_errors = new ArrayList<String>();
    private static final AdvancedLogger ANV_GLOBAL_LOGGER = LoggerFactory.getLogger(CompositeEditCommand.class,
            "netconf-stack", "DEBUG", "GLOBAL");


    @Override
    public void execute() throws CommandExecutionException {
        //execute sequentially
        for (Command command : m_commands) {
            try {
                command.execute();
            } catch (CommandExecutionException e) {
                ANV_GLOBAL_LOGGER.debug("Command execution failed, error option is : {} n {}", m_errorOption, e
                        .toString());
                if (EditConfigErrorOptions.CONTINUE_ON_ERROR.equals(m_errorOption)) {
                    if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                        m_errors.add(e.getMessage());
                    } else if (e.getRpcError() != null) {
                        m_errors.add(e.getRpcError().getErrorMessage());
                    }
                } else {
                    throw e;
                }
            }
        }
    }

    public void appendCommand(Command commmand) {
        //children usually send a null when the command is not applicable to them
        if (commmand != null) {
            this.m_commands.add(commmand);
        }

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CompositeEditCommand{");
        sb.append("m_commands=").append(m_commands);
        sb.append(", m_errorOption='").append(m_errorOption).append('\'');
        sb.append(", m_errors=").append(m_errors);
        sb.append('}');
        return sb.toString();
    }

    public CompositeEditCommand setErrorOption(String errorOption) {
        this.m_errorOption = errorOption;
        return this;
    }

    public boolean isError() {
        return m_errors.size() > 0;
    }

    public String getErrorString() {
        StringBuilder sb = new StringBuilder();
        for (String error : m_errors) {
            sb.append(error + NEW_LINE);
        }
        return sb.toString();
    }

}
