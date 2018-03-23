package se.jtscorp.busskartan;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;

public class ListViewFragment extends ListFragment {
	private DatabaseHelper myDatabaseHelper;
	
	private ArrayAdapter<String> adapter;
	
	private List<String> lines_selected;
	
	private listBtnClickListener listener;
	
	private Spinner spinner;
	private ImageView showBtn;
	private ImageView clearBtn;

	private ListView listview;
	
	public ListViewFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.listview_fragment, container, false);

		spinner = (Spinner) v.findViewById(R.id.spinner1);
		showBtn = (ImageView) v.findViewById(R.id.imageView1);

		clearBtn = (ImageView) v.findViewById(R.id.imageView2);

		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		myDatabaseHelper = new DatabaseHelper(getActivity());
		myDatabaseHelper.openDataBase();

		createSpinner();
		
		listview = this.getListView();
				
		setButtonListener();

		clearBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {	
				listview.setAdapter(null);
				listview.clearChoices();
				listview.setAdapter(adapter);
				
				listener.listBtnClicked(null);
			}
		});
	} // end method

	public void createSpinner(/* Cursor area_table */) {
		Cursor area_table = myDatabaseHelper.getAreaTable();

		List<String> area_list = new ArrayList<String>();

		for (int i = 0; i < area_table.getCount(); i++) {
			area_table.moveToPosition(i);

			String area = area_table.getString(0);

			area_list.add(area);
		} // end for

		ArrayAdapter<String> area_adapter = new ArrayAdapter<String>(
				getActivity(), android.R.layout.simple_spinner_item, area_list);

		spinner.setAdapter(area_adapter);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				String area = parent.getItemAtPosition(pos).toString();
				System.out.println(parent.getItemAtPosition(pos).toString());
				populateListView(area);
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	public void populateListView(String area) {
		// Lines
		Cursor listview_table = myDatabaseHelper.getListViewTable(area);

		List<String> line_list = new ArrayList<String>();

		for (int i = 0; i < listview_table.getCount(); i++) {
			listview_table.moveToPosition(i);

			String line_id = listview_table.getString(0);
			String start_stop = listview_table.getString(1);
			String end_stop = listview_table.getString(2);

			line_list.add("Linje " + line_id + " : " + start_stop + " - "
					+ end_stop);
		} // end for

		adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_multiple_choice, line_list);
		setListAdapter(adapter);
	}

	public void setButtonListener() {
		lines_selected = new ArrayList<String>();

		showBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String selected = "";
				int cntChoice = listview.getCount();
				SparseBooleanArray positions_checked = listview
						.getCheckedItemPositions();

				lines_selected.clear();

				for (int i = 0; i < cntChoice; i++) {
					if (positions_checked.get(i) == true) {
						selected += listview.getItemAtPosition(i).toString()
								+ "\n";
						String line_number = listview.getItemAtPosition(i)
								.toString().replaceAll("[^0-9]", ""); // System.out.println(line_number);
						lines_selected.add(line_number);
					} // end if
				} // end for

				if (lines_selected.size() > 0) {
					listener.listBtnClicked(lines_selected);
				}
			}
		});
	}

	public interface listBtnClickListener {
		public void listBtnClicked(List<String> lines);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof listBtnClickListener) {
			listener = (listBtnClickListener) activity;
		} else {
			throw new ClassCastException(activity.toString()
					+ " must implemenet MyListFragment.OnItemSelectedListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		listener = null;
	}
} // end class
