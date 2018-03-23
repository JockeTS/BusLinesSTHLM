package se.jtscorp.busskartan;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapDrawing {
	private final GoogleMap map;
	
	private List<Marker> stop_markers;	
	
	private List<LatLng> latlng_multistop;
	private List<Integer> colors_list;
	private List<Integer> dividers_list;
	
	private boolean show_markers = false;
	
	private Builder bounds;
	
	private Context context;
	
	public MapDrawing(GoogleMap map, Context context) {
		this.map = map;
		this.context = context;
		
		setZoomListener();
	} // end ctor
	
	private void setZoomListener() {
		map.setOnCameraChangeListener(new OnCameraChangeListener() {
	        private float previousZoom = -1;
	        private int markers_zoom_level = 14;
	        
	        @Override
	        public void onCameraChange(CameraPosition pos) {
	            if (pos.zoom != previousZoom){
	                if ( pos.zoom >= markers_zoom_level && previousZoom < markers_zoom_level ) {
	                	show_markers = true;
	                	toggleMarkers();
	                } else if ( pos.zoom < markers_zoom_level && previousZoom >= markers_zoom_level ) {
	                	show_markers = false;
	                	toggleMarkers();
	                }
	                	
	                previousZoom = pos.zoom;
	            } // end if
	        } // end method
	    }); // end method
	} // end method
	
	private void toggleMarkers() {
		if (stop_markers != null) {
			for ( int i = 0; i < stop_markers.size(); i++ ) {
	    		Marker stop_marker = stop_markers.get(i);
				stop_marker.setVisible(show_markers);
	    	} // end for
		}
	} // end method
	
	public void drawMarkers(Cursor marker_table) {
		stop_markers = new ArrayList<Marker>();
		
		latlng_multistop = new ArrayList<LatLng>();
		
		colors_list = new ArrayList<Integer>();
		
		dividers_list = new ArrayList<Integer>();
		
		marker_table.moveToFirst();
		
		for ( int i = 0; i < marker_table.getCount(); i++ ) {
			String line_id_current = marker_table.getString(0); // System.out.println(line_id_current);	
			String stop_id_current = marker_table.getString(1); // System.out.println(stop_id_current);	
			
//			String stop_number = marker_table.getString(6);
//			String route = marker_table.getString(7);
			
			String stop_name = marker_table.getString(2); // System.out.println(stop_title_current);
			String snippet = "Linje " + line_id_current; // + " (" + stop_number + ") Route: " + route;
			
			double longitude = marker_table.getDouble(3);
			double latitude = marker_table.getDouble(4);
			LatLng coordinate = new LatLng(latitude, longitude);
			
//			int color = marker_table.getInt(5);
			int dividers = 1;

			int color_one = marker_table.getInt(5);
			
			if (i < marker_table.getCount()-1) {
				marker_table.moveToNext();
				
				String stop_id_next = marker_table.getString(1); // System.out.println(stop_id_next);

				// If next stop_id is the same as current id
				while ( stop_id_next.compareTo(stop_id_current) == 0 ) {
					String line_id_next = marker_table.getString(0); // System.out.println(line_id_next);
					snippet += ", Linje " + line_id_next;
					
//					color += marker_table.getInt(5);
					dividers++;
					
					int color_two = marker_table.getInt(5);
					
					if (color_one > (color_two + 180) ) {
						color_two = 360 - color_two;
					} else if (color_one < (color_two - 180)) {
						color_one = 360 - color_one;
					}
					
					color_one = (color_one + color_two) / 2;
					
					if (marker_table.isLast() != true) {
						marker_table.moveToNext();
						
						stop_id_next = marker_table.getString(1);
					} else {
						stop_id_next = "";
					}

					i++;
				} // end while
			} // end if 	 
			
			// color /= dividers; // System.out.println(color);
	
			Marker stop_marker = map.addMarker(new MarkerOptions()
			.icon(BitmapDescriptorFactory.defaultMarker(color_one))
    		.position(coordinate)
		    .title(stop_name)
		    .snippet(snippet)
		    .visible(show_markers) // boolean variable?
		    );
			
			stop_markers.add(stop_marker); // System.out.println(stop_markers.size());		 

			if (dividers > 1) {
				latlng_multistop.add(coordinate);
				colors_list.add(color_one);
				dividers_list.add(dividers);
			}
		} // end for
	} // end method
	
	public void drawLines(Cursor line_table) {	
		bounds = new LatLngBounds.Builder();
		
		for ( int i = 0; i < line_table.getCount()-1; i++ ) {
			line_table.moveToPosition(i+1);
			String line_id_next = line_table.getString(0);
			
			line_table.moveToPosition(i);
			String line_id_current = line_table.getString(0); 		
			
			double longitude_current = line_table.getDouble(3);
			double latitude_current = line_table.getDouble(4);
			LatLng coordinate_one = new LatLng(latitude_current, longitude_current);
			
			// If current and next id are the same
			if (line_id_current.compareTo(line_id_next) == 0) {
				int route_current = line_table.getInt(2);

				LatLng coordinate_two = null;
				
				int color = line_table.getInt(5);
				
				if (route_current == 0 || route_current == 1) {
					line_table.moveToNext();
					int route_next = line_table.getInt(2); 

					while (route_next == 2) {
						line_table.moveToNext();
						route_next = line_table.getInt(2);
					}
					
					double longitude_next = line_table.getDouble(3);
					double latitude_next = line_table.getDouble(4);
					
					coordinate_two = new LatLng(latitude_next, longitude_next);
					
					drawLine(color, coordinate_one, coordinate_two);

					line_table.moveToPosition(i+1);
					route_next = line_table.getInt(2);
					
					if (route_current == 0 && route_next == 1) {
						while (route_next == 1) {
							line_table.moveToNext();
							route_next = line_table.getInt(2);
						}
						
						if (route_next == 0) {
							longitude_next = line_table.getDouble(3);
							latitude_next = line_table.getDouble(4);
							coordinate_two = new LatLng(latitude_next, longitude_next);
							drawLine(color, coordinate_one, coordinate_two);
						}
					} // end if
				} else if (route_current == 2) {
					line_table.moveToPrevious();
					int route_previous = line_table.getInt(2);
					
					while (route_previous == 1) {		
						line_table.moveToPrevious();
						route_previous = line_table.getInt(2);
					}

					double longitude_previous = line_table.getDouble(3);
					double latitude_previous = line_table.getDouble(4);
					
					coordinate_two = new LatLng(latitude_previous, longitude_previous);
					
					drawLine(color, coordinate_one, coordinate_two);
					
					line_table.moveToPosition(i+1);
					int route_next = line_table.getInt(2);
					
					if (route_next != 2) {
						while (route_next == 1) {		
							line_table.moveToNext();
							route_next = line_table.getInt(2);
						}
						
						if (route_next != 2) {
							double longitude_next = line_table.getDouble(3);
							double latitude_next = line_table.getDouble(4);
							
							coordinate_two = new LatLng(latitude_next, longitude_next);
							
							drawLine(color, coordinate_one, coordinate_two);
						} // end if
					} // end if
				} // end if		
			} else {
				bounds.include(coordinate_one);

				line_table.moveToNext();

				double longitude_next = line_table.getDouble(3);
				double latitude_next = line_table.getDouble(4);
				LatLng coordinate_next = new LatLng(latitude_next, longitude_next);

				bounds.include(coordinate_next);

				line_table.moveToPrevious();
			}

			if (i == 0 || i == line_table.getCount()-2) {
				bounds.include(coordinate_one);
			} // end if
		} // end for

		map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(),
				 context.getResources().getDisplayMetrics().widthPixels,
				 context.getResources().getDisplayMetrics().heightPixels,
				 175));
	} // end method
	
	private void drawLine(int color, LatLng coordinate_one, LatLng coordinate_two) {
		int width = 4;
		int alpha = 255;
		for (int i = 0; i < latlng_multistop.size(); i++) {
			LatLng compare_one = latlng_multistop.get(i); // System.out.println(compare_one);

			if (coordinate_one.equals(compare_one) == true ) { 
				for (int k = 0; k < latlng_multistop.size(); k++) {
					LatLng compare_two = latlng_multistop.get(k);
					
					if (coordinate_two.equals(compare_two) == true) {
						alpha = 128;
									
						int color_one = colors_list.get(i);
						int color_two = colors_list.get(k);
						
						int dividers_one = dividers_list.get(i);
						int dividers_two = dividers_list.get(k);
						
						// If colors are equal
						if (color_one == color_two) {
							color = color_one;
							width = dividers_one * 4;
						} else {
							// If number of dividers are equal
							if (dividers_one == dividers_two) {
								color = (color_one + color_two) / dividers_one;
								alpha = 255;
								width = 4;
							} else if (dividers_one > dividers_two) {
								color = color_two;
								width = dividers_two * 4;
							} else if (dividers_two > dividers_one) {
								color = color_one;
								width = dividers_one * 4;
							}
						}
					} // end if
				} // end for
			} // end if
		} // end for	
			
		float[] hsv_color = {color, 1, 1};
		
		map.addPolyline(new PolylineOptions()
    	.add(coordinate_one, coordinate_two)
    	.width(width)
    	.geodesic(true)
    	.color(Color.HSVToColor(alpha, hsv_color)));
	} // end method
	
	public void clearMap() {
		map.clear();
	} // end method
} // end class