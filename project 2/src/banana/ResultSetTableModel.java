package banana;

// A TableModel that supplies ResultSet data to a JTable.
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import javax.swing.table.AbstractTableModel;
import java.util.Properties;
import javax.sql.DataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import java.sql.Statement;


// ResultSet rows and columns are counted from 1 and JTable 
// rows and columns are counted from 0. When processing 
// ResultSet rows or columns for use in a JTable, it is 
// necessary to add 1 to the row or column number to manipulate
// the appropriate ResultSet column (i.e., JTable column 0 is 
// ResultSet column 1 and JTable row 0 is ResultSet row 1).
public class ResultSetTableModel extends AbstractTableModel 
{
   private Connection connection;
   private Statement statement;
   private ResultSet resultSet;
   private ResultSetMetaData metaData;
   private int numberOfRows;

   // keep track of database connection status
   private boolean connectedToDatabase = false;
   
   // constructor initializes resultSet and obtains its meta data object;
   // determines number of rows
   public ResultSetTableModel(Connection connection, String query ) 
      throws SQLException, ClassNotFoundException
   
      {         
	  try { 
	   		
	 
            // create Statement to query database
            statement = connection.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY );

            // update database connection status
            connectedToDatabase = true;

		
		    //set update and execute it
		    //setUpdate (query);
	  } //end try
      catch ( SQLException sqlException ) 
      {
         sqlException.printStackTrace();
         System.exit( 1 );
      } // end catch
      } // end constructor ResultSetTableModel

   // get class that represents column type
   public Class getColumnClass( int column ) throws IllegalStateException
   {
      // ensure database connection is available
      if ( !connectedToDatabase ) 
         throw new IllegalStateException( "Not Connected to Database" );

      // determine Java class of column
      try 
      {
         String className = metaData.getColumnClassName( column + 1 );
         
         // return Class object that represents className
         return Class.forName( className );
      } // end try
      catch ( Exception exception ) 
      {
         exception.printStackTrace();
      } // end catch
      
      return Object.class; // if problems occur above, assume type Object
   } // end method getColumnClass

   // get number of columns in ResultSet
   public int getColumnCount() throws IllegalStateException
   {   
      // ensure database connection is available
      if ( !connectedToDatabase ) 
         throw new IllegalStateException( "Not Connected to Database" );

      // determine number of columns
      try 
      {
         return metaData.getColumnCount(); 
      } // end try
      catch ( SQLException sqlException ) 
      {
         sqlException.printStackTrace();
      } // end catch
      
      return 0; // if problems occur above, return 0 for number of columns
   } // end method getColumnCount

   // get name of a particular column in ResultSet
   public String getColumnName( int column ) throws IllegalStateException
   {    
      // ensure database connection is available
      if ( !connectedToDatabase ) 
         throw new IllegalStateException( "Not Connected to Database" );

      // determine column name
      try 
      {
         return metaData.getColumnName( column + 1 );  
      } // end try
      catch ( SQLException sqlException ) 
      {
         sqlException.printStackTrace();
      } // end catch
      
      return ""; // if problems, return empty string for column name
   } // end method getColumnName

   // return number of rows in ResultSet
   public int getRowCount() throws IllegalStateException
   {      
      // ensure database connection is available
      if ( !connectedToDatabase ) 
         throw new IllegalStateException( "Not Connected to Database" );
 
      return numberOfRows;
   } // end method getRowCount

   // obtain value in particular row and column
   public Object getValueAt( int row, int column ) 
      throws IllegalStateException
   {
      // ensure database connection is available
      if ( !connectedToDatabase ) 
         throw new IllegalStateException( "Not Connected to Database" );

      // obtain a value at specified ResultSet row and column
      try 
      {
		   resultSet.next();  /* fixes a bug in MySQL/Java with date format */
         resultSet.absolute( row + 1 );
         return resultSet.getObject( column + 1 );
      } // end try
      catch ( SQLException sqlException ) 
      {
         sqlException.printStackTrace();
      } // end catch
      
      return ""; // if problems, return empty string object
   } // end method getValueAt
   
   // set new database query string
   public void setQuery( String query ) 
      throws SQLException, IllegalStateException 
   {
      // ensure database connection is available
      if ( !connectedToDatabase ) 
         throw new IllegalStateException( "Not Connected to Database" );

      // specify query and execute it
      resultSet = statement.executeQuery( query );

      // obtain meta data for ResultSet
      metaData = resultSet.getMetaData();

      // determine number of rows in ResultSet
      resultSet.last();                   // move to last row
      numberOfRows = resultSet.getRow();  // get row number    
      
    //load user that can update operations log database
  	try {
  		FileInputStream fileOut;
  		fileOut = new FileInputStream("oplog.properties");
  		 MysqlDataSource dataOut = new MysqlDataSource();
  	      Properties properties = new Properties();     
  	      properties.load(fileOut);
  	      dataOut.setUrl(properties.getProperty("MYSQL_DB_URL"));
  	      dataOut.setUser(properties.getProperty("MYSQL_DB_USERNAME"));
  	      dataOut.setPassword(properties.getProperty("MYSQL_DB_PASSWORD"));
  	      Connection opOut = dataOut.getConnection();
  	      Statement updateSrc = opOut.createStatement();
  	      updateSrc.executeUpdate("Update operationscount Set num_queries = num_queries + 1");
  	      opOut.close();
  	} catch (IOException e) {
  		// TODO Auto-generated catch block
  		e.printStackTrace();
  	}
      
      // notify JTable that model has changed
      fireTableStructureChanged();
   } // end method setQuery


// set new database update-query string
   public void setUpdate( String query ) 
      throws SQLException, IllegalStateException 
   {
	  int res;
      // ensure database connection is available
      if ( !connectedToDatabase ) 
         throw new IllegalStateException( "Not Connected to Database" );

      // specify query and execute it
      res = statement.executeUpdate( query );

      // obtain meta data for ResultSet
      metaData = resultSet.getMetaData();
      // determine number of rows in ResultSet
      resultSet.last();                   // move to last row
      numberOfRows = resultSet.getRow();  // get row number     
      
      
    //load user that can update operations log database
	try {
		FileInputStream fileOut;
		fileOut = new FileInputStream("oplog.properties");
		 MysqlDataSource dataOut = new MysqlDataSource();
	      Properties properties = new Properties();     
	      properties.load(fileOut);
	      dataOut.setUrl(properties.getProperty("MYSQL_DB_URL"));
	      dataOut.setUser(properties.getProperty("MYSQL_DB_USERNAME"));
	      dataOut.setPassword(properties.getProperty("MYSQL_DB_PASSWORD"));
	      Connection opOut = dataOut.getConnection();
	      Statement updateSrc = opOut.createStatement();
	      updateSrc.executeUpdate("Update operationscount Set num_updates = num_updates + 1");
	      opOut.close();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      
      // notify JTable that model has changed
      fireTableStructureChanged();
   } // end method setUpdate

   // close Statement and Connection               
   public void disconnectFromDatabase()            
   {              
      if ( !connectedToDatabase )                  
         return;
      // close Statement and Connection            
      else try                                          
      {                                            
         statement.close();                        
         connection.close();                       
      } // end try                                 
      catch ( SQLException sqlException )          
      {                                            
         sqlException.printStackTrace();           
      } // end catch                               
      finally  // update database connection status
      {                                            
         connectedToDatabase = false;              
      } // end finally                             
   } // end method disconnectFromDatabase          
}  // end class ResultSetTableModel





