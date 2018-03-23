package se.jtscorp.busskartan;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

import se.jtscorp.busskartan.ListViewFragment.listBtnClickListener;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MainActivity extends FragmentActivity implements
	listBtnClickListener {
	private DatabaseHelper myDatabaseHelper;

	private MyPagerAdapter myPagerAdapter;
	private ViewPager myViewPager;

	private Fragment listview_fragment;
	private SupportMapFragment map_fragment;

	private MapDrawing myMapDrawing;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_viewpager);

		initializeDatabaseHelper();

		listview_fragment = new ListViewFragment();
		map_fragment = new SupportMapFragment();
		map_fragment.setRetainInstance(true);

		initializePaging();
		createActionBar();
		initializeMapDrawing();
	} // end method

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	} // end method

	private void initializeDatabaseHelper() {
		myDatabaseHelper = new DatabaseHelper(this);
		try {
			myDatabaseHelper.createDataBase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		myDatabaseHelper.openDataBase();
	}

	private void initializeMapDrawing() {
		final Handler myHandler = new Handler();

		myHandler.post(new Runnable() {
			@Override
			public void run() {
				GoogleMap map = ((SupportMapFragment) map_fragment).getMap();

				if (map != null) {
					map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
					map.setMyLocationEnabled(true);
					map.moveCamera(CameraUpdateFactory.newLatLngZoom(
							new LatLng(59.32944, 18.06861), 11f));

					myMapDrawing = new MapDrawing(map, getApplicationContext());
				} else
					myHandler.post(this);
			} // end method
		});
	} // end method

	private void createActionBar() {
		final ActionBar actionBar = getActionBar();

		actionBar.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.bg_block));
		actionBar.setIcon(getResources().getDrawable(R.drawable.ic_launcher));

		// Specify that tabs should be displayed in the action bar.
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create a tab listener that is called when the user changes tabs.
		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
				myViewPager.setCurrentItem(tab.getPosition());

				// getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
				// fragments.get(tab.getPosition())).commit();
				// getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
				// myPagerAdapter.getItem(tab.getPosition())).commit();
			}

			public void onTabUnselected(ActionBar.Tab tab,
					FragmentTransaction ft) {
				// hide the given tab
			}

			public void onTabReselected(ActionBar.Tab tab,
					FragmentTransaction ft) {
				// probably ignore this event
			}
		};

		actionBar.addTab(actionBar.newTab().setText("Karta")
				.setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText("Linjer")
				.setTabListener(tabListener));
	}

	private void initializePaging() {
		List<Fragment> fragments = new Vector<Fragment>();
		fragments.add(map_fragment);
		fragments.add(listview_fragment);
		// fragments.add(Fragment.instantiate(this,
		// Tab3Fragment.class.getName()));
		this.myPagerAdapter = new MyPagerAdapter(
				super.getSupportFragmentManager(), fragments);

		myViewPager = (ViewPager) super.findViewById(R.id.viewpager);
		myViewPager.setAdapter(this.myPagerAdapter);

		myViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						// When swiping between pages, select the
						// corresponding tab.
						getActionBar().setSelectedNavigationItem(position);
					}
				});

		/*
		 * myViewPager.setOnTouchListener(new View.OnTouchListener() {
		 * 
		 * @Override public boolean onTouch(View v, MotionEvent event) { return
		 * true; } });
		 */
	} // end method

	@Override
	public void listBtnClicked(List<String> lineNumbers) {
		myMapDrawing.clearMap();

		if (lineNumbers != null) {
			Cursor markers = myDatabaseHelper.markerTable(lineNumbers);
			Cursor lines = myDatabaseHelper.lineTable(lineNumbers);

			myMapDrawing.drawMarkers(markers);
			myMapDrawing.drawLines(lines);
			
			myViewPager.setCurrentItem(0);
		} // end if
	} // end method
} // end class