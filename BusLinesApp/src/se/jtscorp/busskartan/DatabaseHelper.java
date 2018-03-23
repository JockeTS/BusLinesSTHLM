package se.jtscorp.busskartan;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper{
	 
    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/se.jtscorp.busskartan/databases/";
    
    private static String DB_NAME = "Busskartan_Reloaded.db";
 
    public static SQLiteDatabase myDataBase; 
 
    private final Context myContext;
 
    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */

    public DatabaseHelper(Context context) {
    	super(context, DB_NAME, null, 1);
        this.myContext = context;
    }	

  /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException{
 
    	boolean dbExist = checkDataBase();
 
    	if(dbExist){
    		//do nothing - database already exist 
    	}else{
 
    		//By calling this method and empty database will be created into the default system path
               //of your application so we are gonna be able to overwrite that database with our database.
        	this.getReadableDatabase();
 
        	try {

    			copyDataBase();
 
    		} catch (IOException e) {
 
        		throw new Error("Error copying database");
 
        	}
    	}
 
    }
 
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){
 
    	SQLiteDatabase checkDB = null;
 
    	try{
    		String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    	}catch(SQLiteException e){
 
    		//database does't exist yet.
 
    	}
 
    	if(checkDB != null){
 
    		checkDB.close();
 
    	}
 
    	return checkDB != null ? true : false;
    }
 
    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{
 
    	//Open your local db as the input stream
    	InputStream myInput = myContext.getAssets().open(DB_NAME);
 
    	// Path to the just created empty db
    	String outFileName = DB_PATH + DB_NAME;
 
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
 
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
 
    }
 
    public void openDataBase() throws SQLException{
 
    	//Open the database
        String myPath = DB_PATH + DB_NAME;
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    }
 
    @Override
	public synchronized void close() {
 
    	    if(myDataBase != null)
    		    myDataBase.close();
 
    	    super.close();
 
	}
 
	@Override
	public void onCreate(SQLiteDatabase db) {
 
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 
	}
 
       // Add your public helper methods to access and get content from the database.
       // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
       // to you to create adapters for your views.
	
//	public Cursor getBusLine(int line_number) {
//	    Cursor bus_line_table = myDataBase.query("Bus_line_" + line_number, 
//		new String[] { "stop_number", "name", "longitude", "latitude" }, 
//		null, null, null, null, null);
//	    
//	    return bus_line_table;
//	}
	
	public Cursor markerTable(List<String> lines_selected) {
		String bus_lines = "";
		
		for (int i = 0; i < lines_selected.size(); i++) {
			String line_id = lines_selected.get(i);
			
			bus_lines += line_id;
			
			if (i != lines_selected.size()-1) {
				 bus_lines += " OR Bus_Line__Has__Bus_Stop.Bus_Line_ID = ";
			} // end if
		} // end for 

		Cursor marker_table = myDataBase.rawQuery("SELECT DISTINCT Bus_Line_ID, Bus_Stop_ID, Name, Longitude, Latitude, Color, Stop_Number, Route " +
				"FROM Bus_Stop INNER JOIN Bus_Line__has__Bus_Stop ON Bus_Stop._ID = Bus_Line__has__Bus_Stop.Bus_Stop_ID " +
				"JOIN Bus_Line ON Bus_Line__has__Bus_Stop.Bus_Line_ID = Bus_Line._ID " +
				"WHERE Bus_Line__Has__Bus_Stop.Bus_Line_ID = " + bus_lines + " " +
				"ORDER BY Bus_Line__has__Bus_Stop.Bus_Stop_ID*1, Bus_Line__has__Bus_Stop.Bus_Line_ID*1 ASC", null);
		
//		Cursor line_table = myDataBase.rawQuery("SELECT DISTINCT Bus_Line_ID, Bus_Stop_ID, Name, Longitude, Latitude " +
//				"FROM Bus_Stop INNER JOIN Bus_Line__has__Bus_Stop ON Bus_Stop._ID = Bus_Line__has__Bus_Stop.Bus_Stop_ID " +
//				"WHERE Bus_Line__Has__Bus_Stop.Bus_Line_ID = " + bus_lines + " " +
//				"ORDER BY Bus_Line__has__Bus_Stop.Bus_Stop_ID*1, Bus_Line__has__Bus_Stop.Bus_Line_ID*1 ASC", null);
		
		return marker_table;
	} // end method
	
	public Cursor lineTable(List<String> lines_selected) {
		String bus_lines = "";
		
		for (int i = 0; i < lines_selected.size(); i++) {
			String line_id = lines_selected.get(i);
			
			bus_lines += line_id;
			
			if (i != lines_selected.size()-1) {
				 bus_lines += " OR Bus_Line__Has__Bus_Stop.Bus_Line_ID = ";
			} // end if
		} // end for 
		
		Cursor line_table = myDataBase.rawQuery("SELECT Bus_Line_ID, Stop_Number, Route, Longitude, Latitude, Color " +
				"FROM Bus_Stop INNER JOIN Bus_Line__has__Bus_Stop ON Bus_Stop._ID = Bus_Line__has__Bus_Stop.Bus_Stop_ID " +
				"JOIN Bus_Line ON Bus_Line__has__Bus_Stop.Bus_Line_ID = Bus_Line._ID " + 
				"WHERE Bus_Line__Has__Bus_Stop.Bus_Line_ID = " + bus_lines + " " +
				"ORDER BY Bus_Line__has__Bus_Stop.Bus_Line_ID*1, Bus_Line__has__Bus_Stop.Stop_Number*1 ASC", null);
		
//		Cursor line_table = myDataBase.rawQuery("SELECT Bus_Line_ID, Stop_Number, Route, Longitude, Latitude " +
//				"FROM Bus_Stop INNER JOIN Bus_Line__has__Bus_Stop ON Bus_Stop._ID = Bus_Line__has__Bus_Stop.Bus_Stop_ID " +
//				"WHERE Bus_Line__Has__Bus_Stop.Bus_Line_ID = " + bus_lines + " " +
//				"ORDER BY Bus_Line__has__Bus_Stop.Bus_Line_ID*1, Bus_Line__has__Bus_Stop.Stop_Number*1 ASC", null);
		
		return line_table;
	} // end method
	
	public static Cursor getLine(String lineNumber) {
		Cursor cursor = myDataBase.rawQuery("SELECT Bus_Line_ID, Stop_Number, Route, Longitude, Latitude, Color " +
				"FROM Bus_Stop INNER JOIN Bus_Line__has__Bus_Stop ON Bus_Stop._ID = Bus_Line__has__Bus_Stop.Bus_Stop_ID " +
				"JOIN Bus_Line ON Bus_Line__has__Bus_Stop.Bus_Line_ID = Bus_Line._ID " + 
				"WHERE Bus_Line__Has__Bus_Stop.Bus_Line_ID = " + lineNumber + " " +
				"ORDER BY Bus_Line__has__Bus_Stop.Bus_Line_ID*1, Bus_Line__has__Bus_Stop.Stop_Number*1 ASC", null);
		
		return cursor;
	}
	
	public Cursor getListViewTable(String area) {
//		Cursor listview_table = myDataBase.rawQuery("SELECT Bus_Line._ID, Name, 1 _id FROM Bus_Line INNER JOIN Bus_Stop ON " +
//				"Bus_Line.Start_Stop_ID = Bus_Stop._ID OR Bus_Line.End_Stop_ID = Bus_Stop._ID " +
//				"WHERE AREA = '" + area + "' " +
//				"ORDER BY Bus_Line._ID*1 ASC", null);
		
		Cursor listview_table = myDataBase.rawQuery("SELECT DISTINCT Bus_Line_ID, Start_Stop, End_Stop " +
				"FROM Bus_Line__has__Bus_Stop JOIN Bus_Line ON Bus_Line__has__Bus_Stop.Bus_Line_ID = Bus_Line._ID " + 
				"WHERE Area = '" + area + "' " +
				"ORDER BY Bus_Line._ID*1 ASC", null);
		
		return listview_table;
	}
	
	public Cursor getAreaTable() {
		Cursor area_table = myDataBase.rawQuery("SELECT DISTINCT Area FROM Bus_Line", null);
		
		return area_table;
	}
}
