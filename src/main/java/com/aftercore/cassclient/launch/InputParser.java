// Creating class for parsing command line
package com.aftercore.cassclient.launch;

import com.aftercore.cassclient.App;

import com.datastax.driver.core.SimpleStatement;

import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/*
 * First argument: IP address of a Cassandra node.  The address format is checked for format errors, and the connection is checked to see if
 * this is a valid Cassandra node.
 * 
 * Second argument: 
 */


public class InputParser {
	
	private String[] input;
	
	private App cassclient;
	
	String userCommand;
	
	public String[][] commandStatements;	// First dimension array represent command statements; second dimension array represents command statement contents
	
	public InputParser(App app) {
		
		cassclient = app;
		
		input = cassclient.getLaunchInput();
		
	}
		
	
	// Display args
	public void showCommandLine() {
		
		System.out.print("\n" + input.length + " arguments on command line: ");
		for(int i=0; i<input.length; i++)
			System.out.print(input[i] + " ");
		System.out.println("\n");
	}
	
	/* Parses user shell input at launch of cassandra client and calls the appropriate handler mechanism
	 * for each option specified.
	 */
	public void parseLaunchInput() {
		
		// Verify arguments were specified or else terminate
        if(input.length == 0) {
        	System.out.println("No cassandra node IP address specified.\n" +
        			"Make certain you are connecting to a node running Cassandra.\n" +
        			"If you are running Cassandra on the same machine as this app,\n" +
        			"try using loopback address 127.0.0.1 as argument");
        	System.exit(1);
        }
        
        // Check if input string has a valid ip address format
        if(!cassclient.isNetAddressFormat(input[0])) {
        	System.out.println("Bad formatted IPv4 address");
        	System.exit(1);
        }

        int i = 1;
        while(i < input.length) {
        	
        	if(input[i].compareTo("-u") == 0) {
        		        		
        		
        		if( (i+1) >= input.length) {
        			System.out.println("ERROR: Premature end of input ... app terminating!");
        			System.exit(1);
        		}
        		
        		String k_name = input[i+1];
        		        		        	
        		cassclient.setKeyspace(k_name);        		        		
        		
        		i = i + 2;
        		
        		System.out.println("Current keyspace set to " + k_name);
        	}
        	
        	else if(input[i].compareTo("-o") == 0) {
        		
        		if( (i+1) >= input.length) {
        			System.out.println("ERROR: Premature end of input ... app terminating!");
        			System.exit(1);
        		}
        		
        		if ( input[i+1].compareTo("test") == 0) {        		
        			cassclient.setOperationMode(App.OperationMode.TEST);
        			System.out.println("Set operation mode to TEST MODE");
        		}
        		else if (input[i+1].compareTo("batch") == 0) {        		
        			cassclient.setOperationMode(App.OperationMode.BATCH);
        			System.out.println("Set operation mode to BATCH MODE");
        		}
        		
        		i = i + 2;
        		        		
        	}
        	
        	else if ( input[i].compareTo("-q") == 0) {
        		
        		if( (i+1) >= input.length) {
        			System.out.println("ERROR: Premature end of input ... app terminating!");
        			System.exit(1);
        		}
        		
        		/**************************************************************************************** 
        		 * from i+1 to i+n, where (i+n)<input.length, search an index containing a string with a 
        		 * semi-colon as the last character in the String
        		 *
        		 * If semi-colon found but not at the end of the String, error
        		 * Other checks for ill-formed statements can be done via execution?
        		 ***************************************************************************************/        		 
        		int n=1;
        		String statement="";
        		boolean found_semicolon = false;
        		do {
        			statement += input[i+n];
        			if( input[i+n].charAt(input[i+n].length()-1) == ';' ) {
        				found_semicolon = true;
        				break;
        			}
        			else {
        				statement += " ";
        				n++;        				
        			}
        		} while( (i+n) < input.length); 
        		
        		if(!found_semicolon) {
        			System.out.println("ERROR: Ill-formed QUERY STATEMENT! Application terminating");
        			System.exit(1);
        		}
        		
        		cassclient.stmts.add( new SimpleStatement(statement) );
        		
        		i = i + n + 1;
        		
        		System.out.println("Added query statement to lists of query statements");
        		System.out.println("QUERY STATEMENT: " + statement);
        	}
        	else if( input[i].compareTo("-f") == 0) {
        		if( (i+1) >= input.length) {
        			System.out.println("ERROR: Premature end of input ... app terminating!");
        			System.exit(1);
        		}
        		
        		/* Parse the String by looking for the ':' curChariter character to 
        		 * separate file paths in this argument. For each file, add an entry to a file
        		 * list. Save to cass clients member: query_file
        		 * 
        		 */
        		 File file;//s; for each path, add as file name to file list
        		 int index=0;
        		 
        		 // debugging
        		 System.out.print("filepath: ");
        		  do {
        			 int found_index = input[i+1].indexOf(':', index);
        			 
        			 if(found_index == -1) {
        				 
        				 found_index = input[i+1].length();
        				 
        			 }
        			 
        			 String filepath = input[i+1].substring(index, found_index);
        			 
        			 //Debuggng
        			 System.out.print(filepath + ":");
    				 
    				 try {
    					 file = new File(filepath);
    					 cassclient.query_files.add(file);
    				 }
    				 catch (Exception e) {
    					 System.out.println("ERROR: Unable to create File instance\n" + e.getMessage());
    					 System.exit(1);
    				 }
    				
        			 index = found_index + 1;
        			 
        		 }	while( index < input[i+1].length() );
        		 
        		  //Debugging
        		  System.out.println("\n");
        		  
        		  i = i + 2;
        		
        	}	// End of else if( input[i].compareTo("-f") == 0) {
        	
        	else {
        		System.out.println("ERROR: Unsupported or ill-formed option: " + input[i]
        							+ " ... \napp terminating");
        		System.exit(1);
        	}
        	
        }	// End of while loop
		
	}	// End of parseLaunchInput()
	
	/* Display user prompt and wait for user events (keyboard). This is where the Cassandra client
	 * will always return to for the remainder of this process's life time.
	 */
	public void userPrompt() {
		
		// 1) Prompt user for input and await reply
		System.out.print("cass_client > ");
		//System.in.reset();
		
		int[] userPromptInput = new int[1024];
		
		int input_value = 0;		
		
		int numCharRead = 0;
		
		do { // read user input until ENTER KEY PRESSED event occurs
			
			if(numCharRead > userPromptInput.length) {
				System.out.println("ERROR: USER INPUT EXCEEDED MAX SIZE");
				numCharRead = 0;
				System.out.print("cass_client > ");
			}
			
			try {
				input_value = System.in.read();
				userPromptInput[numCharRead] = input_value;
			}
			catch (IOException e) {
				System.out.println("ERROR: Exception thrown while waiting for user input\n" + e.getMessage());
				System.exit(1);
			}
			
			numCharRead++;
			
		} while(input_value != 10);
		
		userCommand = convertIntsToString(userPromptInput);
		
		//System.exit(0);
		// Parse input from user prompt
		
		// Handle the user request!
		
		
		
		//while(2+2==4);
		
		// 2) read in all keyboard events awaiting; end read at ENTER key event
		
		// 3) parse and process user input
		
		// 4) go back to 1
		
	}	// End of userPrompt()
	
	private String convertIntsToString(int[] userPromptInput) {
		
		int i=0;
		
		StringBuilder commandStringBuilder = new StringBuilder(userPromptInput.length);
		
		while( (userPromptInput[i] != 10) && (i < userPromptInput.length) ) {
			
			commandStringBuilder.append( (char)userPromptInput[i] ); 
			
			//System.out.print((char)userPromptInput[i]);
			
			i++;
		}
		
		return commandStringBuilder.toString();
		
	}	// End of convertIntsToString()

	/*
	 * Take single string userCommand, parses it and converts it into an array of command statements
	 * 
	 * static enum ParseCommandStatementStates {SEARCH_COMMAND_STATEMENT_STATE, READ_CMDORARG_STATE, SEARCH_ARG_STATE};
	 * 
	 */
	public void parseCommandStatements() {
		
		ParseCommandStatementStates cur_state = ParseCommandStatementStates.SEARCH_COMMAND_STATEMENT_STATE;
		
		int cur_index = 0, cmd_stmt_index = 0, cmd_arg_index = 0, start_read_index = 0;
		
		commandStatements = null;
		
		do {
			
			switch(cur_state) {
			
			case SEARCH_COMMAND_STATEMENT_STATE:
				if ( (userCommand.charAt(cur_index) == ' ') || (userCommand.charAt(cur_index) == ';') ) {
					
					cur_index++;
					
				}
				else {	//character considered to be the start of a command
					
					cur_state = ParseCommandStatementStates.READ_CMDORARG_STATE;
					start_read_index = cur_index;
					// DON'T INCREMENT cur_index here!!
				}
				
				break;
			
			case READ_CMDORARG_STATE:
				
				if ( (userCommand.charAt(cur_index) == ' ') || (userCommand.charAt(cur_index) == ';') )	{ // just read a word
					
					if(commandStatements == null) {	// First time
						commandStatements = new String[1][];
						commandStatements[0] = new String[1];
					}
					else {
						if(cmd_arg_index == 0) { // New command statement; need to increase size of double array
							
							String[][] tmp_cmd_stmts = commandStatements;
							
							commandStatements = new String[ cmd_stmt_index + 1 ][];
							commandStatements[cmd_stmt_index] = new String[1];
							
							// Reinitialize commandStatements
							for(int i=0; i < tmp_cmd_stmts.length; i++) {
								commandStatements[i] = new String[tmp_cmd_stmts[i].length];
								
								for(int j=0; j < tmp_cmd_stmts[i].length; j++) {
									
									commandStatements[i][j] = tmp_cmd_stmts[i][j];
									
								} // End of for(int j=0; j < tmp_cmd_stmts[i].length; j++)
								
							}	// End of for(int i=0; i < tmp_cmd_stmts.length; i++)
							
						}	// End of if(cmd_arg_index == 0)
						
						else {	// Just increase single array by one
							
							String[] tmp_arg = commandStatements[cmd_stmt_index];
							
							commandStatements[cmd_stmt_index] = new String[ cmd_arg_index + 1 ];
							
							// Reinitialize command statement at that array
							for(int i=0; i < tmp_arg.length; i++) {
								commandStatements[cmd_stmt_index][i]  = tmp_arg[i];
							}
							
						} // End of else ... just increase single array by one
						
						
					}	// End of else ... commandStatements NOT NULL
		
					/*
					System.out.println("commandStatements.length = " + commandStatements.length);
					for(int i=0; i < commandStatements.length; i++)
						System.out.println("commandStatements[" + i + "].length = " + commandStatements[i].length);
					
					System.out.println("cmd_stmt_index = " + cmd_stmt_index);
					System.out.println("cmd_arg_index = " + cmd_arg_index);
					*/
					
					commandStatements[cmd_stmt_index][cmd_arg_index] = userCommand.substring(start_read_index, cur_index);
					
					
					if(userCommand.charAt(cur_index) == ';') {	// End of a command statement; search for new command statement; start argument list over						
						cmd_stmt_index++;
						cmd_arg_index = 0;
						cur_state = ParseCommandStatementStates.SEARCH_COMMAND_STATEMENT_STATE;
						cur_index++;						
					}
					else if(userCommand.charAt(cur_index) == ' ') {	// Command statement has arguments
						cmd_arg_index++;
						cur_state = ParseCommandStatementStates.SEARCH_ARG_STATE;
						cur_index++;
					}
					
				}
				else {
					cur_index++;
				}
				
				break;
				
			case SEARCH_ARG_STATE:
				
				if(userCommand.charAt(cur_index) == ' ') {
					cur_index++;
				}
				else if(userCommand.charAt(cur_index) == ';') {
					cmd_stmt_index++;
					cmd_arg_index = 0;
					cur_state = ParseCommandStatementStates.SEARCH_COMMAND_STATEMENT_STATE;
					cur_index++;
				}
				else {
					cur_state = ParseCommandStatementStates.READ_CMDORARG_STATE;
					start_read_index = cur_index;
					// DON'T INCREMENT cur_index here!!
				}
				
				break;			
			
			}	// End of switch statement
			
		} while ( cur_index < userCommand.length() );
				
		// CHECK THE STATE OF cur_state AND userCommand[cur_index] AND ACT ACCORDINGLY
		
		if(cur_state == ParseCommandStatementStates.READ_CMDORARG_STATE) {	// Reached the end of userCommand string while parsing a word
		
			
			if(commandStatements == null) {	// First time
				commandStatements = new String[1][0];
				commandStatements[0] = new String[1];
			}
			
			else {
			
				if(cmd_arg_index == 0) { // New command statement; need to increase size of double array
					
					String[][] tmp_cmd_stmts = commandStatements;
					
					commandStatements = new String[ cmd_stmt_index + 1 ][];
					commandStatements[cmd_stmt_index] = new String[1];
					
					// Reinitialize commandStatements
					for(int i=0; i < tmp_cmd_stmts.length; i++) {
						commandStatements[i] = new String[tmp_cmd_stmts[i].length];
						
						for(int j=0; j < tmp_cmd_stmts[i].length; j++) {
							
							commandStatements[i][j] = tmp_cmd_stmts[i][j];
							
						} // End of for(int j=0; j < tmp_cmd_stmts[i].length; j++)
						
					}	// End of for(int i=0; i < tmp_cmd_stmts.length; i++)
					
				}	// End of if(cmd_arg_index == 0)
				
				else {	// Just increase single array by one
					
					String[] tmp_arg = commandStatements[cmd_stmt_index];
					
					commandStatements[cmd_stmt_index] = new String[ cmd_arg_index + 1 ];
					
					// Reinitialize command statement at that array
					for(int i=0; i < tmp_arg.length; i++) {
						commandStatements[cmd_stmt_index][i]  = tmp_arg[i];
					}
					
				} // End of else ... just increase single array by one
				
				
			}	// End of else ... commandStatements NOT NULL
			
			
			commandStatements[cmd_stmt_index][cmd_arg_index] = userCommand.substring(start_read_index, cur_index);
		
		}	// End of if(cur_state == ParseCommandStatementStates.READ_CMDORARG_STATE)
				
		printCommandStatements();
		
	}	// End of parseCommandStatements()
	
	
	public void printCommandStatements() {
		
		if(commandStatements == null) {
			System.out.println("Command statements NULL");
			return;
		}
		
		for(int i=0; i < commandStatements.length; i++) {
			for(int j=0; j < commandStatements[i].length; j++)
				System.out.print(commandStatements[i][j] + " ");
			System.out.println("");
		}
		
	}	// End of printCommandStatements()
	
	
	public void executeAllCommandStatements() {
		
		for(int i=0; i < commandStatements.length; i++) {
			
			try {
				executeCommandStatement(i, 0, cassclient.commandList);
			
			}
			catch(MalformedCommandException e) {
				System.out.println("ERROR: MalformedCommandException thrown ... " + e.getMessage() );
			}				
			
		}
		
	}
	
	
	/*
	 * Searches commandStatements member of this class and finds a match in the specified list of supported commands
	 * For arg 0, if a command statement is not found on the list, then consider that command a query and call the
	 * query statement handler.  For all other arguments, if there is not match, then either return and error or throw
	 * an exception, depending on the error. 
	 */
	public void executeCommandStatement(int cmdStmtIndex, int cmdArgIndex, List<CommandEntry> list) throws MalformedCommandException {
		
		boolean match_found = false;
		for(CommandEntry entry: list) {
			
			if( entry.isCqlCommand() ) {
				if(commandStatements[cmdStmtIndex][cmdArgIndex].compareToIgnoreCase( entry.getCommandName() ) == 0) {	// MATCH FOUND!
					if( entry.hasMethod() ) {
						cassclient.curCmdStmt = cmdStmtIndex;
						entry.executeCommand(cassclient);
					}
					
					match_found = true;
				}				
			}
			
			else {
				
				if(commandStatements[cmdStmtIndex][cmdArgIndex].compareToIgnoreCase( entry.getCommandName() ) == 0) {	// MATCH FOUND!
					
					// Check: isCqlCommand. If true, run this if statement block, then return match is found (return true)				
					
					if( (cmdArgIndex+1) >= commandStatements[cmdStmtIndex].length )	{ // THERE ARE NO MORE ARGUMENTS SPECIFIED BY USER
						
						if(entry.hasMethod())	// HAS A METHOD + NO ARGUMENTS SPECIFIED BY USER = EXECUTE METHOD			
							entry.executeCommand(cassclient);
						
						else {	// HAS NO METHOD TO EXECUTED + NO ARGUMENTS SPECIFIED BY USER
							
							if( !entry.isOptionsEmpty() ) {	// HAS NO METHOD TO EXECUTE + NO ARGUMENTS SPECIFIED BY USER + HAS OPTIONS = ERROR: USER SHOULD SPECIFY ARGUMENTS
								System.out.println("ERROR: " + commandStatements[cmdStmtIndex][cmdArgIndex] + " command missing arguments");
							}
							else // has no method or options ... this command is simply not ready ... HAS NO METHOD NOR ARGUMENTS!! THIS SHOULD NOT BE!!
								throw new MalformedCommandException();
								
						}	// End of HAS NO METHOD TO EXECUTED
						
					}	// End of if with no arguments
					
					else {	// MORE ARGUMENTS ARE SPECIFIED BY USER
						
						if( !entry.isOptionsEmpty() ) { // MORE ARGUMENTS ARE SPECIFIED BY USER + MORE OPTIONS = GO FURTHER 
							
							// Recursive call
							executeCommandStatement(cmdStmtIndex, cmdArgIndex+1, entry.getOptionsList());
							
						}
						
						else {	// MORE ARGUMENTS ARE SPECIFIED BY USER + NO MORE OPTIONS = ERROR: NO OPTIONS FOR THIS COMMAND 
							System.out.println("ERROR: No options should be specified after " + commandStatements[cmdStmtIndex][cmdArgIndex]);
						}
						
					} // End of MORE ARGUMENTS ARE SPECIFIED BY USER BLOCK
					
					match_found = true;
										
				}	// End of MATCH FOUND If block
				
			}	// End of else (isCqlCommand == false)
			
			if(match_found) break;
			
		}	// END OF FOR LOOP
				
		
		if(!match_found) {
			
			// if arg is 0, it is a command
			if(cmdArgIndex == 0)
				System.out.println("ERROR: Unknown command: " + commandStatements[cmdStmtIndex][cmdArgIndex]);
			// else display: UNKNOWN ARGUMENT: DISPLAY ARGUMENT
			else
				System.out.println("UNKNOWN OPTION FOR: " + commandStatements[cmdStmtIndex][cmdArgIndex-1]);
			
		}
		
	}	// End of executeCommandStatment()
	
	
	
	/* The idea is similar to parseLaunchInput except for three main differences:
	 * 1) A String is being parsed instead of a String array
	 * 2) Here, we're mainly looking for CQL query commands or cass client commands
	 * 3) Input is coming from System.in.read() instead of the system command prompt
	 * 
	 * Semicolons and spaces are used as word curChariters
	 * 
	 * ? Should all query commands end in semicolon? And if so, should I force cass client commands
	 * to also end in semicolons? ! DEPENDS ON COMMAND!!
	 * 
	 * Deprecated ... uh ... eliminated!!
	 * 
	 */
	public void parseUserPromptInput() {
		
		// The first word defines the command. We're looking for cass client commands; all other commands will be considered queries
		// parse first word; this is determined by ending with w/a space, ';', or the end of the String
		
		if(userCommand.length()==0)
			return;
		
		int index = 0;
		
		do {
			
			int beginIndex = index;
			char curChar=0;
					
			for( ; index < userCommand.length(); index++) {
				
				if( (userCommand.charAt(index) == ' ') || (userCommand.charAt(index) == ';') ) {
					
					curChar = userCommand.charAt(index);
					
					break;
				}
				
			}
			
			String command;			
			if( (index - beginIndex) == 0 )
				command = userCommand.substring(beginIndex, beginIndex+1);
			else
				command = userCommand.substring(beginIndex, index);
			
			
			/*****************************************************************************************************************************************
			 * 																CHECK FOR CASS COMMANDS
			 *****************************************************************************************************************************************/
			// QUIT COMMAND
			if(command.compareToIgnoreCase("quit") == 0) {
				if( (index<userCommand.length()) && (curChar==' ') ) {
					// Keep going through the String one character at a time, stop when character is NOT a space or end of String
					
					index++;	// skips space character
					
					while( ( index < userCommand.length() ) && (userCommand.charAt(index) == ' ') ) {
						index++;					
					}			
					
					if ( ( index < userCommand.length() ) && ( userCommand.charAt(index) != ';' ) ){
						System.out.println("ERROR: QUIT COMMAND TAKES NO ARGUMENTS\n");
						return;
					}

				}
				System.out.println("Exiting cass client");
				System.exit(0);
			}
			// SHOW COMMAND
			else if(command.compareToIgnoreCase("show") == 0) {
				
				if( (index<userCommand.length()) && (curChar==' ') ) {
					// Keep going through the String one character at a time, stop when character is NOT a space or end of String
					
					index++;	// skips space character
					
					while( ( index < userCommand.length() ) && (userCommand.charAt(index) == ' ') ) {
						index++;					
					}			
					
					if ( ( index < userCommand.length() ) && ( userCommand.charAt(index) != ';' ) ){
						System.out.println("ERROR: SHOW COMMAND TAKES NO ARGUMENTS\n");
						return;
					}

				}
				
				cassclient.showSettings();
				
			}
			// MODE COMMAND
			else if(command.compareToIgnoreCase("mode") == 0) {
				
				if(index >= userCommand.length()) {
					System.out.println("ERROR: NO OPERATION MODE SPECIFIED");
					return;
				}
				else if(userCommand.charAt(index) == ';') {
					System.out.println("ERROR: NO OPERATION MODE SPECIFIED");
					return;
				}
				
				index++;	// SKIPPING SPACE CHARACTER BUT TEST TO MAKE SURE WE ARE NOT OUT OF BOUNDS				
				if( index >= userCommand.length() ) {	// IF WE'VE REACHED THE END OF THE STRING
					System.out.println("ERROR: NO OPERATION MODE SPECIFIED\n");
					return;
				}
				
				// READ FROM INPUT STREAM TILL WE REACH EITHER A ';' OR A SPACE CHARACTER, STARTING AT CURRENT INDEX
				int start_index = index;
				for( ; index < userCommand.length(); index++) {				
					if( (userCommand.charAt(index) == ' ') || (userCommand.charAt(index) == ';') )
						break;
				}
				
				int stop_index = index;				
				String mode;
				if ( (stop_index - start_index) == 0 ) {	// STOPPED ON A CHARACTER OR A SEMICOLON AND CONTAINS ONE OF THEM
					mode = userCommand.substring(start_index, start_index+1);
				}
				else {	//HAS AT LEAST ONE CHARACTER; DOES NOT HAVE SEMICOLON OR SPACE CHARACTER IN THE STRING
					mode = userCommand.substring(start_index, stop_index);
				}
				
				// hold
				/*
				if ( (index<userCommand.length()) && ((userCommand.charAt(index) != ';') && (userCommand.charAt(index) != ' ')) ) {
					System.out.println("ERROR: MODE COMMAND TAKES ONLY ONE ARGUMENT\n");
					return;
				}
				*/
				if( (index<userCommand.length()) && (userCommand.charAt(index) == ' ') ) {
					// Keep going through the String one character at a time, stop when character is NOT a space or end of String
					
					index++;	// skips space character
					if(index >= userCommand.length() )
						break;
					
					while( ( index < userCommand.length() ) && (userCommand.charAt(index) == ' ') ) {
						index++;					
					}			
					
					if ( ( index < userCommand.length() ) && ( userCommand.charAt(index) != ';' ) ){
						System.out.println("ERROR: MODE COMMAND TAKES ONLY ARGUMENT\n");
						return;
					}

				}
				
				if(mode.compareToIgnoreCase("test") == 0)
					cassclient.setOperationMode(App.OperationMode.TEST);
				else if(mode.compareToIgnoreCase("batch") == 0)
					cassclient.setOperationMode(App.OperationMode.BATCH);
				else if(mode.compareToIgnoreCase(";") == 0)
					System.out.println("ERROR: NO OPERATION MODE SPECIFIED\n");
				else if(mode.compareToIgnoreCase(" ") == 0)
					System.out.println("ERROR: NO OPERATION MODE SPECIFIED\n");
				else
					System.out.println("ERROR: Unknown operation mode");
				
			}
			// DO NOTHING ON SPACES
			else if(command.compareToIgnoreCase(" ") == 0) {
				// DO NOTHING
				
			}
			// DO NOTHING ON SEMI-COLONS
			else if(command.compareToIgnoreCase(";") == 0) {
				// DO NOTHING
				
			}
			
			else {
				System.out.println("ERROR: UNKNOWN COMMAND!\n");
				break;	// exits do while loop BUT WILL BE LATER USED AS CQL QUERIES
			}
			
			// else, assuming it is a cql query request command
			index++;	// skip space or ';' character, or
			
		} while ( index < userCommand.length() );
		
		
	}	// End of parseUserPromptInput()
	
	
	public static enum ParseCommandStatementStates {SEARCH_COMMAND_STATEMENT_STATE, READ_CMDORARG_STATE, SEARCH_ARG_STATE};
	
	
}	// End of class InputParse	
