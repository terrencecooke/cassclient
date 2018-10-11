// CommandEntry.java
package com.aftercore.cassclient.launch;

import com.aftercore.cassclient.*;

import java.util.List;
import java.util.ArrayList;


public class CommandEntry {	
	
	private String commandName;
	
	private ExecuteCommand appCmd;
	
	private final boolean hasMethod;
	
	protected List<CommandEntry> appOptions = new ArrayList();
	
	public CommandEntry(String p_name, ExecuteCommand p_cmd) {
		
		commandName = p_name;
		
		appCmd = p_cmd;
		
		hasMethod = true;
		
	}
	
	public CommandEntry(String p_name) {
		
		commandName = p_name;
		
		hasMethod = false;
		
	}
	
	public boolean hasMethod() {
		return hasMethod;
	}
	
	public void executeCommand(App app) {
		
		if(hasMethod)
			appCmd.execute(app);
		
	}
	
	public boolean addCommandOption(CommandEntry new_option) {
		
		// add to options List
		boolean success_result = appOptions.add(new_option);
		
		return success_result;
		
	}
	
	public CommandEntry getOption(String option_name) {
		
		if(appOptions.isEmpty()) return null;
		
		for(CommandEntry entry:appOptions) {
			String name = entry.getCommandName();
			
			if( name.compareToIgnoreCase(option_name) == 0 )
				return	entry;
		}
		
		return null;
	}
	
	public List<CommandEntry> getOptionsList() {
		
		return appOptions;
		
	}
	
	public boolean isOptionsEmpty() {
		
		return appOptions.isEmpty();		
	}
	
	public String getCommandName() {
		
		return	commandName; 		
	}
	
}	// End of CommandEntry