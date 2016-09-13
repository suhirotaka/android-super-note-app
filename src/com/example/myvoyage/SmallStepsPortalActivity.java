package com.example.myvoyage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class SmallStepsPortalActivity extends BaseActivity {
	
	private long[] goalOrder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Build view
        setContentView(R.layout.activity_small_steps_portal);
        Cursor goalsCursor = getGoalsFromDb();
        int goalsCount = goalsCursor.getCount();
        if (goalsCount > 0) {
    		ListView goalsContainer = (ListView)findViewById(R.id.goals_container);
            ArrayAdapter<String> goalsAdapter = new ArrayAdapter<String>(this, R.layout.partial_goals_list_item);
    		goalOrder = new long[goalsCount];
    		goalsCursor.moveToFirst();
    		do {
        		Long goalId = goalsCursor.getLong(
        		    goalsCursor.getColumnIndexOrThrow(SmallStepsContract.Goals._ID)
        		);
        		String goalTitle = goalsCursor.getString(
        		    goalsCursor.getColumnIndexOrThrow(SmallStepsContract.Goals.COLUMN_NAME_TITLE)
        		);
       		    goalsAdapter.add(goalTitle);
       		    goalOrder[goalsCursor.getPosition()] = goalId;
    		} while (goalsCursor.moveToNext()); 
     		goalsContainer.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                	showTaskContents(goalOrder[position], ((ListView)parent).getItemAtPosition(position).toString(), false);
                }
            });
    		goalsContainer.setAdapter(goalsAdapter);
        }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.small_steps_portal, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void createNewGoal(View view) {
		SmallStepsDbHelper mDbHelper = new SmallStepsDbHelper(this);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();	
		
		ViewGroup inputRow = (ViewGroup)view.getParent();
		String goalTitle = ((EditText)inputRow.findViewById(R.id.input_title)).getText().toString().trim();
		ContentValues values = new ContentValues();
		values.put(SmallStepsContract.Goals.COLUMN_NAME_TITLE, goalTitle);
		values.put(SmallStepsContract.Goals.COLUMN_NAME_CONTAINING_TASK_IDS, "");
		if (!SmallStepsDbHelper.validateGoalsValues(values)) return;
		
		// Insert the new row, returning the primary key value of the new row
		long newGoalId = db.insert(SmallStepsContract.Goals.TABLE_NAME, null, values);

	    // reload the activity
	    finish();
	    startActivity(getIntent());
	    
		showTaskContents(newGoalId, goalTitle, false);
	}
	
	private Cursor getGoalsFromDb() {
		SmallStepsDbHelper mDbHelper = new SmallStepsDbHelper(this);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		String[] projection = {
		    SmallStepsContract.Goals._ID,
		    SmallStepsContract.Goals.COLUMN_NAME_TITLE,
		    SmallStepsContract.Goals.COLUMN_NAME_ID_AS_TASK
		};
		String sortOrder = SmallStepsContract.Goals.COLUMN_NAME_UPDATED_AT + " DESC, " +
		                   SmallStepsContract.Goals._ID + " DESC";
		String selection = SmallStepsContract.Goals.COLUMN_NAME_ID_AS_TASK + " IS NULL";

		Cursor cursor = db.query(
		    SmallStepsContract.Goals.TABLE_NAME,
		    projection,
		    selection,
		    null,
		    null,
		    null,
		    sortOrder
        );
		return cursor;
	}

}