package com.example.buslinesdatabasejtsversion4;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseHandler {
	private SQLiteDatabase myDatabase;
	public String LINE_NUMBER;
	private String AREA;
	
	public DatabaseHandler(SQLiteDatabase myDatabase) {
		this.myDatabase = myDatabase;	
//		LINE_NUMBER = line_number;
//		AREA = area;
		
		createBusLineTable();
		createBusStopTable();
		createBusLineHasBusStopTable();
		
//		boolean line_exists = checkIfBusLineExists();
//		if (line_exists != true) {
//			insertIntoBusLineTable();
//		}	
	}
	public Cursor getBusLines() {
		Cursor bus_lines = myDatabase.rawQuery("SELECT _ID, Start_Stop, End_Stop FROM Bus_Line LEFT JOIN Bus_Line__has__Bus_Stop ON _ID = Bus_Line_ID WHERE Bus_Line_ID IS NULL AND _ID <> '70'", null);
		
//		Cursor bus_lines = myDatabase.rawQuery("SELECT DISTINCT _ID, Start_Stop, End_Stop FROM Bus_Line LEFT JOIN Bus_Line__has__Bus_Stop ON _ID = Bus_Line_ID WHERE _ID = '71'", null);
		
//		Cursor bus_lines = myDatabase.rawQuery("SELECT * FROM Bus_Line", null);
		
		return bus_lines;
	}
	
	private void createBusLineTable() {
		myDatabase.execSQL("CREATE TABLE IF NOT EXISTS Bus_Line (_ID VARCHAR, Area VARCHAR, Start_Stop VARCHAR, End_Stop VARCHAR, Color INTEGER);");
	}
	
	private void createBusStopTable() {
		myDatabase.execSQL("CREATE TABLE IF NOT EXISTS Bus_Stop (_ID VARCHAR, Name VARCHAR, Longitude DOUBLE, Latitude DOUBLE);");
	}
	
	private void createBusLineHasBusStopTable() {
		myDatabase.execSQL("CREATE TABLE IF NOT EXISTS Bus_Line__has__Bus_Stop (Bus_Line_ID VARCHAR, Bus_Stop_ID VARCHAR, Stop_Number VARCHAR, Route CHAR(1), Special VARCHAR);");
	}
	
	public void insertIntoBusLineTable() {
		ContentValues line_data = new ContentValues();
		line_data.put("_ID", LINE_NUMBER);
		line_data.put("Area", AREA);
		
		Random rand = new Random();
		int min = 0, max = 360;

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int color = rand.nextInt(max - min + 1) + min;
		
//		int color = Integer.parseInt(LINE_NUMBER);
//		
//		color *= 100 * (color % 10);
//		
//		while ( color > 360 ) {
//			color -= 360;
//		} // end while
		
		line_data.put("Color", color);
		
		myDatabase.insert("Bus_Line", null, line_data);
	}
	
	public void updateBusLineTable(ContentValues line_info) {
		myDatabase.update("Bus_Line", line_info, "_ID = " + LINE_NUMBER, null);
	}
	
	public boolean checkIfBusLineExists() {
		boolean line_exists = false;
		
		Cursor matchID = myDatabase.rawQuery("SELECT * FROM Bus_Line WHERE _ID = " + LINE_NUMBER, null);
		matchID.moveToFirst();
		
		if (matchID.getCount() != 0) {
			line_exists = true;
		}
		
		return line_exists;
	}

	public void insertIntoBusStopTable(ContentValues stop_data) {
		// System.out.println(stop_object); System.out.println(stop_number);
	myDatabase.insert("Bus_Stop", null, stop_data);
//		try {
//			ContentValues stop_data = new ContentValues();
//			String stop_name = stop_object.getString("Name");
//			stop_data.put("Name", stop_name);
//			
//			// If the search was successful 
//			if (stop_object.length() != 1) {
//				String stop_id = stop_object.getString("locationid");
//				
//				boolean stop_id_exists = searchDuplicateID(stop_id);
//				
//				if (stop_id_exists != true) {
//					DecimalFormat maxDecimalPlaces = new DecimalFormat();
//					maxDecimalPlaces.setMaximumFractionDigits(6);				
//					
//					double longitude = Double.valueOf(stop_object.getString("@x"));
//					double latitude = Double.valueOf(stop_object.getString("@y"));
//
//					longitude = Double.valueOf(maxDecimalPlaces.format(longitude)); // System.out.println(longitude);
//					latitude = Double.valueOf(maxDecimalPlaces.format(latitude));
//					
//					stop_data.put("_ID", stop_id);				
//					stop_data.put("Longitude", longitude);
//					stop_data.put("Latitude", latitude);
//					
//					myDatabase.insert("Bus_Stop", null, stop_data);
//				} else {
//					System.out.println("Bus stop already in database!");
//				}
//				
//				ContentValues line_has_stop_data = new ContentValues();
//				
//				line_has_stop_data.put("Bus_Line_ID", LINE_NUMBER);
//				line_has_stop_data.put("Bus_Stop_ID", stop_id);
//				line_has_stop_data.put("Stop_Number", stop_number);
//				line_has_stop_data.put("Route", route);
//				
//				insertIntoBusLineHasBusStopTable(line_has_stop_data);
//			} else { // If the search didn't return any results
//				boolean stop_name_exists = searchDuplicateName(stop_name);
//				
//				if (stop_name_exists != true) {
//					myDatabase.insert("Bus_Stop", null, stop_data);
//				} else {
//					System.out.println("Bus stop already in database!");
//				}
//			}
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
	}
	
	public void insertIntoBusStop(JSONObject item_object, boolean insert_stop_into_bus_line, int stop_number, boolean is_endstation) {
		try {
			if (item_object.length() != 1) {
				String stop_id = item_object.getString("locationid");
				String stop_name = item_object.getString("stop_name"); // System.out.println(stop_name);
				stop_name = stop_name.replaceAll("\\s[1-2]\\)", ""); // System.out.println(stop_name);
				
				DecimalFormat maxDecimalPlaces = new DecimalFormat();
				maxDecimalPlaces.setMaximumFractionDigits(6);				
				
				double longitude = Double.valueOf(item_object.getString("@x"));
				double latitude = Double.valueOf(item_object.getString("@y"));

				longitude = Double.valueOf(maxDecimalPlaces.format(longitude)); // System.out.println(longitude);
				latitude = Double.valueOf(maxDecimalPlaces.format(latitude));
				
				ContentValues busStopValues = new ContentValues();
				
				busStopValues.put("_ID", stop_id);				
				busStopValues.put("Name", stop_name);
				busStopValues.put("Longitude", longitude);
				busStopValues.put("Latitude", latitude);
				
				boolean stop_id_exists = searchDuplicateID(stop_id);
	
				if (stop_id_exists != true) {
					myDatabase.insert("Bus_Stop", null, busStopValues);
				} else {
					System.out.println("Bus stop already in database!");
				}
				
				ContentValues bus_stop_id = new ContentValues();
				bus_stop_id.put("Bus_Stop_ID", stop_id);
				bus_stop_id.put("Stop_Number", stop_number);
				
				if (insert_stop_into_bus_line == true) {
					// insertIntoBusLine(bus_stop_id);
					insertIntoBusLineHasBusStopTable(bus_stop_id);
				} 
				
				if (is_endstation == true) {
					System.out.println("end_station");
					ContentValues endstation = new ContentValues();
					endstation.put("Is_Endstation", 1); // System.out.println(stop_id);
					myDatabase.update("Bus_Line__has__Bus_Stop", endstation, "Bus_Stop_ID = " + stop_id, null);
				}
			} else { // end if
				String stop_name = item_object.getString("stop_name");
				
				ContentValues busStopValues = new ContentValues();

				busStopValues.put("Name", stop_name.replaceAll("\\s[1-2]\\)", ""));
				
				boolean stop_name_exists = searchDuplicateName(stop_name);
	
				if (stop_name_exists != true) {
					myDatabase.insert("Bus_Stop", null, busStopValues);
				} else {
					System.out.println("Bus stop already in database!");
				}
				
				ContentValues bus_stop_name = new ContentValues();
				bus_stop_name.put("Bus_Stop_ID", stop_name);
				bus_stop_name.put("Stop_Number", stop_number);
				
				if (insert_stop_into_bus_line == true) {
					insertIntoBusLineHasBusStopTable(bus_stop_name);
					// insertIntoBusLine(bus_stop_name);
				}
			} // end else
		} catch (JSONException e) {
			e.printStackTrace();
		}
} // end method insertIntoBusStops
	
	public void insertIntoBusLineHasBusStopTable(ContentValues line_has_stop_data) {
		// bus_stop_id_or_name.put("Bus_Line_ID", LINE_NUMBER);
		myDatabase.insert("Bus_Line__has__Bus_Stop", null, line_has_stop_data);
	}
	
	public boolean searchDuplicateID(String stop_id) {
		boolean stop_id_exists = false;
		
		Cursor myCursor = myDatabase.rawQuery("SELECT * FROM Bus_Stop WHERE _ID = " + stop_id, null);
		myCursor.moveToFirst(); // System.out.println(myCursor.getCount());

		if (myCursor.getCount() != 0) {
			stop_id_exists = true;
		} else {
			stop_id_exists = false;
		}
		
		return stop_id_exists;
	}
	
	public boolean searchDuplicateName(String stop_name) {
		boolean stop_name_exists = false;
		
		Cursor myCursor = myDatabase.rawQuery("SELECT * FROM Bus_Stop WHERE Name LIKE '" + stop_name + "'", null);
		myCursor.moveToFirst(); // System.out.println(myCursor.getCount());

		if (myCursor.getCount() != 0) {
			stop_name_exists = true;
		} else {
			stop_name_exists = false;
		}
		
		return stop_name_exists;
	}
	
	public void testMe() {
		Cursor distinct_stops = myDatabase.rawQuery("SELECT DISTINCT Bus_Stop_ID FROM Bus_Line__has__Bus_Stop WHERE Bus_Line_ID = " + LINE_NUMBER, null);
		distinct_stops.moveToFirst();
		
		for (int i = 0; i < distinct_stops.getCount(); i++) {			
			String stop_id = distinct_stops.getString(0); // System.out.println(stop_id);
			distinct_stops.moveToNext();
			
			Cursor stop_id_count = myDatabase.rawQuery("SELECT COUNT (Bus_Stop_ID) FROM Bus_Line__has__Bus_Stop WHERE Bus_Stop_ID = " + stop_id, null);
			stop_id_count.moveToFirst();
			
			int id_count = stop_id_count.getInt(0);

			if(id_count == 1) {
				ContentValues one_way_only = new ContentValues();
				one_way_only.put("One_Way_Only", 1); System.out.println(stop_id);
				myDatabase.update("Bus_Line__has__Bus_Stop", one_way_only, "Bus_Stop_ID = " + stop_id, null);
			} 
//			else {
//				one_way_only.put("One_Way_Only", 0); System.out.println(stop_id);
//			}
			
			

			// System.out.println(stop_id_count.getInt(0));
		} // end for
	} 
	
	public List<String> getAreas() {
		Cursor areas_c = myDatabase.rawQuery("SELECT DISTINCT Area FROM Bus_Line", null);
		
		List<String> areas_l = new ArrayList<String>();
		
		for ( int i = 0; i < areas_c.getCount(); i++ ) {
			areas_c.moveToPosition(i);
			
			String area = areas_c.getString(0);
			
			areas_l.add(area);
		} // end for

		return areas_l;
	} // end method
	
	public List<String> getIds(String area) {
		Cursor ids_c = myDatabase.rawQuery("SELECT _ID FROM Bus_Line WHERE Area = '" + area + "'", null);
		
		List<String> ids_l = new ArrayList<String>();
		
		for ( int i = 0; i < ids_c.getCount(); i++ ) {
			ids_c.moveToPosition(i);
			
			String id = ids_c.getString(0);
			
			ids_l.add(id);
		} // end for
		
		return ids_l;
	}
	
	public void dbUpdateColors(List<String> ids, List<Integer> values) {
		for (int i = 0; i < ids.size(); i++) {
			System.out.println(ids.get(i));
		}
		
		for (int i = 0; i < values.size(); i++) {
			System.out.println(values.get(i));
		}
		
		for (int i = 0; i < ids.size(); i++) {
			String id = ids.get(i);
			int value = values.get(i);
			
			ContentValues value_cv = new ContentValues();
			
			value_cv.put("Color", value);
			
			myDatabase.update("Bus_Line", value_cv, "_ID = '" + id + "'", null); System.out.println("updating!");
		}
	}

	public void insertIntoDatabase(String lineNumber, List<JSONObject> routeCombined) throws JSONException {
		for ( int i = 0; i < routeCombined.size(); i++ ) {
			JSONObject stop = routeCombined.get(i);
			
			if (jTestSearchDuplicateID(stop) != true) {
				jTestInsertIntoBusStopTable(stop);
			}

			jTestInsertIntoBusLineHasBusStopTable(lineNumber, stop, i + 1);
		}
	}
	
	public boolean jTestSearchDuplicateID(JSONObject stop) throws JSONException {
		boolean stop_id_exists = false;
		
		String stopID = stop.getString("_ID");
		
		Cursor myCursor = myDatabase.rawQuery("SELECT * FROM Bus_Stop WHERE _ID = " + stopID, null);
		myCursor.moveToFirst(); // System.out.println(myCursor.getCount());

		if (myCursor.getCount() != 0) {
			stop_id_exists = true;
		} else {
			stop_id_exists = false;
		}
		
		return stop_id_exists;
	}
	
	public void jTestInsertIntoBusStopTable(JSONObject stop) throws JSONException {
		ContentValues datei = new ContentValues();

		datei.put("_ID", stop.getString("_ID"));
		datei.put("Name", stop.getString("Name"));
		
		String lng = stop.getString("lng");
		String lngFormatted = lng.substring(0, 2) + "." + lng.substring(2, lng.length());
		datei.put("Longitude", Double.valueOf(lngFormatted)); System.out.println(Double.valueOf(lngFormatted));
		
		String lat = stop.getString("lat");
		String latFormatted = lat.substring(0, 2) + "." + lat.substring(2, lat.length());
		datei.put("Latitude", Double.valueOf(latFormatted));

		// System.out.println(stop.getString("lng"));
		// String lngFormatted = stop.getString("lng");
		// String lngFormatted = stop.getString("lng").replaceAll(".", ""); // System.out.println(lngFormatted);
		// lngFormatted = lngFormatted.substring(0, 2) + "." + lngFormatted.substring(2, lngFormatted.length()); System.out.println(lngFormatted);
		
		
		
		// System.out.println(stop.getString("lat"));
		// String latFormatted = stop.getString("lat").replaceAll(".0", ""); System.out.println(latFormatted);
		// latFormatted = latFormatted.substring(0, 2) + "." + latFormatted.substring(2, latFormatted.length()); System.out.println(latFormatted);
		
		myDatabase.insert("Bus_Stop", null, datei);
	}

	public void jTestInsertIntoBusLineHasBusStopTable(String lineNumber, JSONObject stop, int stopNumber) throws JSONException {
		ContentValues datei = new ContentValues();
		
		datei.put("Bus_Line_ID", lineNumber);
		datei.put("Bus_Stop_ID", stop.getString("_ID"));
		datei.put("Stop_Number", stopNumber);
		
		if (stop.has("Route")) {
			datei.put("Route", stop.getString("Route"));
		}
		
		myDatabase.insert("Bus_Line__has__Bus_Stop", null, datei);
	}
	
	public void jTestUpdateColors() {
//		List<String> ids = new ArrayList<String>(); 
//		List<Integer> values = new ArrayList<Integer>();
		
		Cursor areas = myDatabase.rawQuery("SELECT DISTINCT Area FROM Bus_Line", null);
		
		for (int i = 0; i < areas.getCount(); i++) {
			areas.moveToPosition(i);
			
			String area = areas.getString(i);

			List<String> ids = getIds(area);
			
			int difference = 360 / ids.size();
			
			List<Integer> values = new ArrayList<Integer>();
			
			Random rand = new Random();
			int min = 0, max = ids.size();

			for ( int k = 0; k < ids.size(); k++ ) {
				int randomNum = rand.nextInt(max - min + 1) + min;
				
				int value = randomNum * difference;
				
				while (values.contains(value) == true) {
					randomNum = rand.nextInt(max - min + 1) + min;
					
					value = randomNum * difference;
				}
				
				values.add(value); 
			} // end for
			
			dbUpdateColors(ids, values);
			
			/*
			areas.moveToPosition(i);
			
			String area = areas.getString(0);
			
			Cursor linesInArea = myDatabase.rawQuery("SELECT _ID FROM Bus_Line WHERE Area = '" + area + "'", null);
			
			int increment = 360 / linesInArea.getCount();
			
			int color = 0;
			
			for (int k = 0; k < linesInArea.getCount(); k++) {
				linesInArea.moveToPosition(k);
				
				String id = linesInArea.getString(0);
				color += increment;
				
				ids.add(id);
				values.add(color);
				
//				ContentValues value_cv = new ContentValues();
//				
//				value_cv.put("Color", color);
//				
//				myDatabase.update("Bus_Line", value_cv, "_ID = '" + id + "'", null); System.out.println("updating!");
			}
			*/
		}
		
		
	}
	
	
}
