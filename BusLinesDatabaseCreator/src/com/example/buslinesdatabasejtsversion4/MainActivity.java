package com.example.buslinesdatabasejtsversion4;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {

	private SQLiteDatabase myDatabase;	
	private DatabaseHandler myDatabaseHandler;
	
	private final String SL_API_KEY = "b05a1f65fd22199fe2f571244edec177";
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		myDatabase = openOrCreateDatabase("Busskartan_Reloaded.db", MODE_PRIVATE, null);
		
		myDatabaseHandler = new DatabaseHandler(myDatabase);
		
		Cursor bus_lines = myDatabaseHandler.getBusLines();
		new AsyncTest().execute(bus_lines); 
		
//		while (bus_lines.moveToNext()) {
//			System.out.println(bus_lines.getString(0));
//		}
		
//		List<String> ids = myDatabaseHandler.getIds("1-100");
//		myDatabaseHandler.dbUpdateColors(ids, values)
		
//		myDatabaseHandler.jTestUpdateColors();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class AsyncTest extends AsyncTask<Cursor, Void, Void> {
		@Override
	    protected Void doInBackground(Cursor... urls) {
			Cursor bus_lines = urls[0];
			
//			for ( int i = 0; i < bus_lines.getCount(); i++) {
//				bus_lines.moveToPosition(i);
				
			while (bus_lines.moveToNext()) {
				String lineNumber = bus_lines.getString(0); 
				String start_stop_name = bus_lines.getString(1); // System.out.println(start_stop_name);
				String end_stop_name = bus_lines.getString(2); // System.out.println(end_stop_name);
				
				// Route 1
				List<JSONObject> routeOne = getRoute(lineNumber, start_stop_name, end_stop_name); // for ( int k = 0; k < routeOne.size(); k++ ) { System.out.println(routeOne.get(k)); }
				
				// Route 2
				List<JSONObject> routeTwo = getRoute(lineNumber, end_stop_name, start_stop_name); // for ( int k = 0; k < routeTwo.size(); k++ ) { System.out.println(routeTwo.get(k)); }
				
				if ( routeOne != null && routeTwo != null ) {
					try {			
						// Check if route 1 contains any unique stops
						for (int k = 0; k < routeOne.size(); k++) {
							JSONObject stopOne = routeOne.get(k); // System.out.println(stopOne.get("Name"));
							String stopOneID = stopOne.getString("_ID");
							
							boolean isUnique = checkIfUnique(stopOneID, routeTwo); // System.out.println(isUnique);
							
							if (isUnique == true) {
								stopOne = addRouteInfo(stopOne, "1");
								routeOne.set(k, stopOne);
							}
						}	
						
						// Check if route 2 contains any unique stops
						for (int k = 0; k < routeTwo.size(); k++) {
							JSONObject stopTwo = routeTwo.get(k); // System.out.println(stopTwo.get("Name"));
							String stopTwoID = stopTwo.getString("_ID");
							
							boolean isUnique = checkIfUnique(stopTwoID, routeOne); // System.out.println(isUnique);
							
							if (isUnique == true) {
								stopTwo = addRouteInfo(stopTwo, "2");
								routeTwo.set(k, stopTwo);
								
								int index = getIndex(k, routeOne, routeTwo); // System.out.println(index);
								
								routeOne.add(index, stopTwo);
							}
						}	
						
						for ( int k = 0; k < routeOne.size(); k++ ) { System.out.println(routeOne.get(k)); }
						
						myDatabaseHandler.insertIntoDatabase(lineNumber, routeOne);
					} catch (JSONException e) {
						e.printStackTrace();
					}				
				}
			}
			
				
//			}
			
	    	return null;
	    } // end method doInBackground
		
	private int getIndex(int position, List<JSONObject> routeOne, List<JSONObject> routeTwo) throws JSONException {
		int index = 0;
		
		JSONObject stopTwo = routeTwo.get(position);
		
		while (stopTwo.has("Route")) {
			position--;
			
			stopTwo = routeTwo.get(position);
		}
		
		String idTwo = stopTwo.getString("_ID");
		
		for (int i = 0; i < routeOne.size(); i++) {
			JSONObject stopOne = routeOne.get(i);
			String idOne = stopOne.getString("_ID");
			
			if (idOne.equals(idTwo)) {
				index = i;
			}
		}
		
		return index;
	}
		
	private boolean checkIfUnique(String activeID, List<JSONObject> controlRoute) throws JSONException {
			boolean isUnique = true;
			
			for (int i = 0; i < controlRoute.size(); i++) {
				JSONObject controlStop = controlRoute.get(i);
				String controlID = controlStop.getString("_ID");
				
				if (controlID.equals(activeID)) {
					// System.out.println("Not unique!");
					isUnique = false;
				}
			}
			
			return isUnique;
		}
		
		private JSONObject addRouteInfo(JSONObject stop, String route) throws JSONException {
			stop.put("Route", route);
	
			return stop;
		}
		
		private List<JSONObject> getRoute(String lineNumber, String start_stop_name, String end_stop_name) {
			List<JSONObject> stopsParsed = null;
			
			URL url;
			
			try {
				url = new URL("https://api.trafiklab.se/sl/reseplanerare.json?" +
				"S=" + start_stop_name.replaceAll(" ", "%20") + "&Z=" + end_stop_name.replaceAll(" ", "%20") +
				// "&Date=10.09.2013&Time=08%3A00&Timesel=depart&Lang=sv" +
				"&key=" + SL_API_KEY +
				"&REQ0HafasOpFilterMode=2&REQ0HafasOpFilter_value=" + lineNumber); System.out.println(url);
				
				JSONObject response = askSL(url); System.out.println(response);
				
				JSONObject hafasResponse = response.getJSONObject("HafasResponse");
				JSONArray trip = hafasResponse.getJSONArray("Trip");
				JSONObject unnamed = trip.getJSONObject(2);
				JSONObject subTrip = unnamed.getJSONObject("SubTrip"); // System.out.println(subTrip);
				
				String intermediateStopsQuery = subTrip.getString("IntermediateStopsQuery"); // System.out.println(intermediateStopsQuery);
				
				stopsParsed = getIntermediateStops(intermediateStopsQuery); 
				
				JSONObject origin = subTrip.getJSONObject("Origin");
				JSONObject startStop = parseStop(origin); // System.out.println(startStop);
				stopsParsed.add(0, startStop);
				
				JSONObject destination = subTrip.getJSONObject("Destination");
				JSONObject endStop = parseStop(destination); // System.out.println(endStop);				
				stopsParsed.add(stopsParsed.size(), endStop);
				
//				for (int i = 0; i < stopsParsed.size(); i++) {
//					System.out.println(stopsParsed.get(i));
//				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
			return stopsParsed;
		}
		
		private JSONObject parseStop(JSONObject stop) {
			JSONObject stopParsed = new JSONObject();
			
			try {
				String id = stop.getString("@sa");
	
				String name;
				
				if (stop.has("Name")) {
					name = stop.getString("Name");
				} else {
					name = stop.getString("#text");
				}
	
				String lat = stop.getString("@y");
				String lng = stop.getString("@x");
				
				stopParsed.put("_ID", id);
				stopParsed.put("Name", name);
				stopParsed.put("lat", lat);
				stopParsed.put("lng", lng);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return stopParsed;
		}
		
		private List<JSONObject> getIntermediateStops(String intermediateStopsQuery) {
			JSONArray intermediateStops = null;
			
			// JSONArray intermediateStopsParsed = new JSONArray();
			JSONObject jTest = new JSONObject();
			
			List<JSONObject> stopsParsed = new ArrayList<JSONObject>();
			
			try {
				URL url = new URL(intermediateStopsQuery);
				
				JSONObject response = askSL(url);
				
				JSONObject hafasResponse = response.getJSONObject("HafasResponse");
				JSONObject SLSuger = hafasResponse.getJSONObject("IntermediateStops");
				intermediateStops = SLSuger.getJSONArray("IntermediateStop");
				
				for (int i = 0; i < intermediateStops.length(); i++) {
					JSONObject stop = intermediateStops.getJSONObject(i);

					JSONObject stopParsed = parseStop(stop);
					
					stopsParsed.add(stopParsed);
					
					jTest.put(stopParsed.getString("_ID"), stopParsed);
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return stopsParsed;
		}
		
		private JSONObject askSL(URL url) {
			JSONObject response = null;
			
			BufferedReader reader = null;
			StringBuilder builder = new StringBuilder();
			String line = "";
			
			try {	
					HttpURLConnection con = (HttpURLConnection) url
					.openConnection();			
					
					reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
					
					while ((line = reader.readLine()) != null) {
						builder.append(line); // System.out.println(line);
					}
					
					response = new JSONObject(builder.toString());
				} 
			catch (Exception e) {
					e.printStackTrace();
				}
			finally {
			    if (reader != null) {
			      try {
			        reader.close();
			      } catch (IOException e) {
			        e.printStackTrace();
			        }
			    }
			}
		
			return response;
		}
	 } // end class
} // end class
