package com.aftercore.cassclient;

import java.util.regex.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.io.File;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.Row;
import com.aftercore.cassclient.launch.*;

import com.datastax.driver.core.exceptions.*;
/**
 * Hello world!
 *
 */
public class App 
{
	public static App instance;
	
	public static List<CommandEntry> commandList = new ArrayList();
		
	public String[] launch_input;
	
	public List<SimpleStatement> stmts = new ArrayList();
	
	public List<File> query_files = new ArrayList();
		
	OperationMode mode = OperationMode.TEST;
	
	InputParser input_parser;
	
	Cluster cluster;
	
	Session session;
	
	Metadata metadata;
	
	String current_keyspace;
	
	public int curCmdStmt=0;
	
	static {
		
		instance = new App();
		
		// Initialize list of commands
		CommandEntry cmd_quit = new CommandEntry("quit", (instance)-> instance.exitApp() );
		commandList.add(cmd_quit);
		
		CommandEntry cmd_show = new CommandEntry("show", (instance)-> instance.showSettings() );
		commandList.add(cmd_show);
		
		CommandEntry cmd_mode = new CommandEntry("mode");
		CommandEntry opt_mode_batch = new CommandEntry("batch", (instance)-> instance.setOperationMode("batch") );
		cmd_mode.addCommandOption(opt_mode_batch);
		
		CommandEntry opt_mode_test = new CommandEntry("test", (instance)-> instance.setOperationMode("test") );
		cmd_mode.addCommandOption(opt_mode_test);
		commandList.add(cmd_mode);
		
		CommandEntry cmd_exit = new CommandEntry("exit", (instance)-> instance.exitApp() );
		commandList.add(cmd_exit);
		
		CommandEntry cqlcmd_create = new CommandEntry("create", true, (instance)-> {
			String query = "";
			for(int i=0; i < instance.input_parser.commandStatements[instance.curCmdStmt].length; i++) {
				if(i==0)
					query = instance.input_parser.commandStatements[instance.curCmdStmt][i];
				else
					query += " " + instance.input_parser.commandStatements[instance.curCmdStmt][i];
			}
			
			instance.runQuery(query);
			} );
		commandList.add(cqlcmd_create);
		
		CommandEntry cqlcmd_select = new CommandEntry("select", true, (instance)-> {
			String query = "";
			for(int i=0; i < instance.input_parser.commandStatements[instance.curCmdStmt].length; i++) {
				if(i==0)
					query = instance.input_parser.commandStatements[instance.curCmdStmt][i];
				else
					query += " " + instance.input_parser.commandStatements[instance.curCmdStmt][i];
			}
			
			instance.runQuery(query);
			} );
		commandList.add(cqlcmd_select);
		
		CommandEntry cqlcmd_insert = new CommandEntry("insert", true, (instance)-> {
			String query = "";
			for(int i=0; i < instance.input_parser.commandStatements[instance.curCmdStmt].length; i++) {
				if(i==0)
					query = instance.input_parser.commandStatements[instance.curCmdStmt][i];
				else
					query += " " + instance.input_parser.commandStatements[instance.curCmdStmt][i];
			}
			
			instance.runQuery(query);
			} );
		commandList.add(cqlcmd_insert);
		
		// UPDATE
		CommandEntry cqlcmd_update = new CommandEntry("update", true, (instance)-> {
			String query = "";
			for(int i=0; i < instance.input_parser.commandStatements[instance.curCmdStmt].length; i++) {
				if(i==0)
					query = instance.input_parser.commandStatements[instance.curCmdStmt][i];
				else
					query += " " + instance.input_parser.commandStatements[instance.curCmdStmt][i];
			}
			
			instance.runQuery(query);
			} );
		commandList.add(cqlcmd_update);
		
		// DELETE
		CommandEntry cqlcmd_delete = new CommandEntry("delete", true, (instance)-> {
			String query = "";
			for(int i=0; i < instance.input_parser.commandStatements[instance.curCmdStmt].length; i++) {
				if(i==0)
					query = instance.input_parser.commandStatements[instance.curCmdStmt][i];
				else
					query += " " + instance.input_parser.commandStatements[instance.curCmdStmt][i];
			}
			
			instance.runQuery(query);
			} );
		commandList.add(cqlcmd_delete);
		
		// DROP
		CommandEntry cqlcmd_drop = new CommandEntry("drop", true, (instance)-> {
			String query = "";
			for(int i=0; i < instance.input_parser.commandStatements[instance.curCmdStmt].length; i++) {
				if(i==0)
					query = instance.input_parser.commandStatements[instance.curCmdStmt][i];
				else
					query += " " + instance.input_parser.commandStatements[instance.curCmdStmt][i];
			}
			
			instance.runQuery(query);
			} );
		commandList.add(cqlcmd_drop);
		
		
		
	}
	
    public static void main( String[] args )
    {
    	instance.setLaunchInput(args);
    	
    	// Runs whatever was specified at input or whatever the default test is
    	instance.start();
        
    	// Establish cassclient user prompt and await further instructions - cassclient=> 
    	// prompt for, wait for, and process user input

    	instance.run();        
        
    }
    
    public boolean isNetAddressFormat(String ip_addr) {
    	
    	String _3octets_str = "(((25[0-5]|2[0-4][0-9]|[01][0-9][0-9]|[0-9][0-9]|[0-9]).){3}+)";
        String octet_str = "(25[0-5]|2[0-4][0-9]|[01][0-9][0-9]|[0-9][0-9]|[0-9])";
        //Pattern p = Pattern.compile("(25[0-5]|2[0-4][0-9]|[01][0-9][0-9].){4}");
        Pattern p = Pattern.compile(_3octets_str + octet_str);
        Matcher m = p.matcher(ip_addr);
        //boolean b = m.matches();
        return m.matches();
        
    }
    
    public void start() {    	    
    /*******************************************************************************
     * 						Parse the input - LAUNCH_INPUT MODULE
     *******************************************************************************/
    	processLaunchInput();
    	
    /********************************************************************************************************
     * 		Setup the connection / session with a Cassandra cluster node specified in launch_input
     * 						SETUP MODULE - CONFIGURES SESSION WITH CASSANDRA NODE
     ********************************************************************************************************/
    	setupConnection();
            
    /************************************************************************************************************ 
     * Set operation mode to either test mode or batch mode, depending on launch_input or default value
     ************************************************************************************************************/    
    	runQuery();
    	
    }	// End of start()
    
    public void setLaunchInput(String[] str) {
    	
    	launch_input = str;
    	
    }
    
    /***********************************************
     * Terminates if there's any error
     ***********************************************/
    void processLaunchInput() {
    	
    	input_parser = new InputParser(this);
    	
    	input_parser.showCommandLine();
    	
    	input_parser.parseLaunchInput();
    	
    	// 
    	
        
    }	// End of processLaunchInput()
    
    // Sets up cluster and session objects for connecting with the specified Cassandra node (IP_Address)
    void setupConnection() {
    	
        cluster = Cluster.builder().addContactPoint(launch_input[0]).build();
        
        System.out.println("Datastax Java driver version: " + cluster.getDriverVersion() + "\n");
        
        try {
        	System.out.println("Attempting to connect to Cassandra node at address: "
        						+ launch_input[0] + " ... ");        
        	session = cluster.connect("my_keyspace");
        }
        catch(Exception e) {
        	System.out.println("ERROR: Unable to connect to my_keyspace\n" + e.getMessage());
        	System.exit(1);
        }
        
        try {
        	metadata = cluster.getMetadata();
        }
        catch(Exception e) {
        	System.out.println("ERROR: Unable to acquire Metadata on cluster.\n" + e.getMessage());
        	System.exit(1);
        }               
        
        showSettings();
        
    }	// End of setupConnection()
    
    void runQuery(String query) {
    	
    	String query_stmt = query + ";";
    	SimpleStatement statement = new SimpleStatement(query_stmt);
    	
    	ResultSetFuture query_status = session.executeAsync(statement);
        ResultSet userSelectResult = null;
        boolean exception_occurred = false;
        try {
        	userSelectResult = query_status.getUninterruptibly(5000L, TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException q_exception) {
        	System.out.println("Timeout ERROR: " + q_exception.getMessage());
        	exception_occurred = true;
        }
        catch (QueryExecutionException q_exception) {
        	System.out.println("ERROR: Query execution exception\n" + q_exception.getMessage());
        	exception_occurred = true;
        }
        catch (NoHostAvailableException	q_exception) {
        	System.out.println("ERROR: Specified Host Unavailable!\n" + q_exception.getMessage());
        	exception_occurred = true;
        }
        catch (QueryValidationException q_exception) {
        	System.out.println("ERROR: Query syntax error\n" + q_exception.getMessage());
        	exception_occurred = true;
        }
        
        if (!exception_occurred)	{
        	System.out.println("Execution complete!");
        	
        	List<Row> rows = userSelectResult.all();
        	
        	if(rows.isEmpty()) return;
            
            System.out.println("Retrieved rows\n");
            
            /*
            System.out.println("first_name\tlast_name\n");
            
            for(Row row : rows) {
            	System.out.print( row.getString("first_name") + "\t\t");
            	System.out.println( row.getString("last_name") );
            }
            */
            
            boolean display_column_heading = false;
            for(Row row : rows) {
            	
            	ColumnDefinitions col_defs = row.getColumnDefinitions();
            	
            	int col_size = col_defs.size();
            	
            	if(!display_column_heading) {
            		// display column names as a heading
            		
            		System.out.print("\n");
            		for(int col_index=0; col_index < col_size; col_index++) {
            			System.out.print(col_defs.getName(col_index) + "\t\t");
            		}
            		System.out.println("");
            		
            		for(int col_index = 0; col_index < col_size; col_index++)
            			System.out.print("=====================");
            		System.out.println("");
            		
            		display_column_heading = true;
            	}
            	
            	for(int col_index = 0; col_index < col_size; col_index++) {
            		
            		String row_value = row.getString(col_defs.getName(col_index));
            		if(row_value==null) row_value = "null";
            		
            		String tabs = "\t";
            		
            		int num_of_8s = (int)(row_value.length() / 8);	            		
            		int extra_tabs = 2-num_of_8s;
            		if(extra_tabs > 0) {
            			for(int tab_num = 0; tab_num < extra_tabs; tab_num++)
            				tabs = tabs + "\t";
            		}
            		
            		System.out.print(row_value + tabs);
            		
            	}
            	System.out.println("");
            	
            	
            }	// End of for(Row row : rows) 
            
            
            System.out.println("\n(" + rows.size() + " rows)\n");
        } // End of if (!exception_occurred)
        
    	
    }	// Endof runQuery(String query)
    
    void runQuery()	{
    	//System.out.println("Session class: " + session.getClass().getName());
        
        SimpleStatement userSelect = new SimpleStatement("SELECT first_name, last_name FROM user;");
        
        stmts.add(userSelect);
        
    	//if(stmts.isEmpty()) return;

    	for(int i=0; i<stmts.size(); i++) {
    	
	        System.out.println("Executing statement ... ");
	        //ResultSet userSelectResult = session.execute(userSelect);
	         
	        ResultSetFuture query_status = session.executeAsync(stmts.get(i));
	        ResultSet userSelectResult = null;
	        boolean exception_occurred = false;
	        try {
	        	userSelectResult = query_status.getUninterruptibly(5000L, TimeUnit.MILLISECONDS);
	        }
	        catch (TimeoutException q_exception) {
	        	System.out.println("Timeout ERROR: " + q_exception.getMessage());
	        	exception_occurred = true;
	        }
	        catch (QueryExecutionException q_exception) {
	        	System.out.println("ERROR: Query execution exception\n" + q_exception.getMessage());
	        	exception_occurred = true;
	        }
	        catch (NoHostAvailableException	q_exception) {
	        	System.out.println("ERROR: Specified Host Unavailable!\n" + q_exception.getMessage());
	        	exception_occurred = true;
	        }
	        catch (QueryValidationException q_exception) {
	        	System.out.println("ERROR: Query syntax error\n" + q_exception.getMessage());
	        	exception_occurred = true;
	        }
	                
	        if (!exception_occurred)	{
	        	System.out.println("Execution complete!");
	        	
	        	List<Row> rows = userSelectResult.all();
	        	
	        	if(rows.isEmpty()) return;
	            
	            System.out.println("Retrieved rows\n");
	            
	            /*
	            System.out.println("first_name\tlast_name\n");
	            
	            for(Row row : rows) {
	            	System.out.print( row.getString("first_name") + "\t\t");
	            	System.out.println( row.getString("last_name") );
	            }
	            */
	            
	            boolean display_column_heading = false;
	            for(Row row : rows) {
	            	
	            	ColumnDefinitions col_defs = row.getColumnDefinitions();
	            	
	            	int col_size = col_defs.size();
	            	
	            	if(!display_column_heading) {
	            		// display column names as a heading
	            		
	            		System.out.print("\n");
	            		for(int col_index=0; col_index < col_size; col_index++) {
	            			System.out.print(col_defs.getName(col_index) + "\t\t");
	            		}
	            		System.out.println("");
	            		
	            		for(int col_index = 0; col_index < col_size; col_index++)
	            			System.out.print("=====================");
	            		System.out.println("");
	            		
	            		display_column_heading = true;
	            	}
	            	
	            	for(int col_index = 0; col_index < col_size; col_index++) {
	            		
	            		String row_value = row.getString(col_defs.getName(col_index));
	            		if(row_value==null) row_value = "null";
	            		
	            		String tabs = "\t";
	            		
	            		int num_of_8s = (int)(row_value.length() / 8);	            		
	            		int extra_tabs = 2-num_of_8s;
	            		if(extra_tabs > 0) {
	            			for(int tab_num = 0; tab_num < extra_tabs; tab_num++)
	            				tabs = tabs + "\t";
	            		}
	            		
	            		System.out.print(row_value + tabs);
	            		
	            	}
	            	System.out.println("");
	            	
	            	
	            }	// End of for(Row row : rows) 
	            
	            
	            System.out.println("\n(" + rows.size() + " rows)\n");
	        } // End of if (!exception_occurred)
    	}	// end of for loop
    	
    	stmts.clear();
        
    }	// End runQuery()
    
    /*
     * Prompt for, wait for, and assign pertinent functions to user input
     */
    void handleUserPrompt() {
    	
    	// wait for user input
    	input_parser.userPrompt();
    	
    	input_parser.parseCommandStatements();
    	
    	input_parser.executeAllCommandStatements();
    	
    	//input_parser.parseUserPromptInput();
    	
    }
    
    void run() {
    	
    	while(true) {
    		handleUserPrompt();
    	
    		// EXECUTE TEST OR BATCH IF IT IS NOT EMPTY
    	}
    }
    
    public void showSettings() {
    
    	System.out.println("\nConnected to Cassandra cluster: " + metadata.getClusterName()
				+ "\nPartitioner used: " + metadata.getPartitioner() + "\n"
				+ "Current operation mode: " + mode + "\n"
				+ "Datastax Java driver version: " + cluster.getDriverVersion() + "\n");
    }
    
    public void setKeyspace(String keyspace_name) {
    	
    	current_keyspace = keyspace_name;
    	
    }
    
    public String getKeyspace() {
    	
    	return current_keyspace;
    }
    
    public String[]	getLaunchInput() {
    	
    	return launch_input;
    	
    }
    
    public void setOperationMode(OperationMode new_mode) {
    	
    	mode = new_mode;
    	
    }
    
    public void setOperationMode(String new_mode) {
    	
    	if(new_mode.compareToIgnoreCase("test") == 0)
    		setOperationMode(OperationMode.TEST);
    	else if(new_mode.compareToIgnoreCase("batch") == 0)
    		setOperationMode(OperationMode.BATCH);
    	//else DO NOTHING!
    }
    
    public OperationMode getOperationMode()	{
    	
    	return mode;
    	
    }
    
    public void exitApp() {
    	
    	System.exit(0);
    	
    }
    
    public static enum OperationMode {TEST, BATCH};
    
}	// End of class App
