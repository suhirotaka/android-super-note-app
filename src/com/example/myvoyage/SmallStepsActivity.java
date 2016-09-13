package com.example.myvoyage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class SmallStepsActivity extends BaseActivity {
	
	private long goalId;
	private List<Long> taskOrder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	    // Get the message from the intent
	    Intent intent = getIntent();
	    goalId = intent.getLongExtra(MESSAGE_NAME_GOAL_ID, -1);
	    if (goalId < 0) {
            throw new RuntimeException("Invalid goal id");
	    }
	    String goalTitle = intent.getStringExtra(MESSAGE_NAME_GOAL_TITLE);

	    // Build view
 		View rootView = View.inflate(this, R.layout.activity_small_steps, null);
 	    ViewGroup tasksContainer = (ViewGroup)rootView.findViewById(R.id.tasks_container);
 		taskOrder = getTaskOrdersFromDb();
        Cursor tasksCursor = getTasksFromDb();
        int tasksCount = tasksCursor.getCount();
        // If tasks exist
        if (tasksCount > 0) {
    		tasksCursor.moveToFirst();
    		View[] orderedTaskViews = new View[tasksCount * 2];
    		do {
        		Long taskId = tasksCursor.getLong(
        		    tasksCursor.getColumnIndexOrThrow(SmallStepsContract.Tasks._ID)
        		);
        		String taskTitle = tasksCursor.getString(
        		    tasksCursor.getColumnIndexOrThrow(SmallStepsContract.Tasks.COLUMN_NAME_TITLE)
        		);
        		String taskDeadline = tasksCursor.getString(
        		    tasksCursor.getColumnIndexOrThrow(SmallStepsContract.Tasks.COLUMN_NAME_DEADLINE)
        		);
        		String taskStatus = tasksCursor.getString(
        		    tasksCursor.getColumnIndexOrThrow(SmallStepsContract.Tasks.COLUMN_NAME_STATUS)
        		);
        		Long taskIdAsGoal = tasksCursor.getLong(
        		    tasksCursor.getColumnIndexOrThrow(SmallStepsContract.Tasks.COLUMN_NAME_ID_AS_GOAL)
        		);
        		Map<String, String> taskData = new HashMap<String, String>();
        		taskData.put("id", String.valueOf(taskId));
        		taskData.put("title", taskTitle);
        		taskData.put("deadline", taskDeadline);
        		taskData.put("status", taskStatus);
        		taskData.put("id_as_goal", String.valueOf(taskIdAsGoal));
        		View[] taskView = makeTaskView(taskData);
        		orderedTaskViews[taskOrder.indexOf(taskId) * 2] = taskView[0];
        		orderedTaskViews[taskOrder.indexOf(taskId) * 2 + 1] = taskView[1];
    		} while(tasksCursor.moveToNext());

       		// Insert the task views to the container view
    		for (View v : orderedTaskViews) {
    			tasksContainer.addView(v);
    		}
        }else {
        	// Show operation buttons if task list is empty
        	tasksContainer.findViewById(R.id.task_title_container)
        	              .findViewById(R.id.task_item_buttons_container)
        	              .setTranslationX(0);
        }

		// Toggle operation buttons when task item is long clicked
		tasksContainer.findViewById(R.id.task_title_text_container)
		                         .setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				toggleOperationButtons(view);
				return true;
			}
		});

		// Set the goal title
   		((TextView)rootView.findViewById(R.id.title_goal)).setText(goalTitle);

        setContentView(rootView);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if goal deletion occurred
		if (requestCode == REQUEST_CODE_SHOW_TASK && resultCode == RESULT_OK)  {
		    // reload the activity
		    finish();
		    startActivity(getIntent());
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.small_steps, menu);
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
	
	public void addNewTask(View view) {
		View nextInputRow = getNextInputContainer(getParentTaskContainer(view));
		// Show the input row
		nextInputRow.setVisibility(View.VISIBLE);
		// Set focus and show keyboard
		if (nextInputRow.requestFocus()) {
			showSoftKeyboard(nextInputRow.findViewById(R.id.input_title));
		}
	}

	public void cancelNewTask(View view) {
		View inputRow = getParentTaskContainer(view);
        EditText input_title = (EditText)inputRow.findViewById(R.id.input_title);
        EditText input_deadline = (EditText)inputRow.findViewById(R.id.input_deadline);
        EditText input_status = (EditText)inputRow.findViewById(R.id.input_status);
        input_title.setText("");
        input_deadline.setText("");
        input_status.setText("");
		inputRow.setVisibility(View.GONE);
		// Clear focus and hide keyboard
		inputRow.clearFocus();
		hideSoftKeyboard(view);
	}
	
	public void saveNewTask(View view) {
		SmallStepsDbHelper mDbHelper = new SmallStepsDbHelper(this);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		// Create a new map of values, where column names are the keys
		View inputRow = getParentTaskContainer(view);
        String taskTitle = ((EditText)inputRow.findViewById(R.id.input_title)).getText().toString().trim();
        String taskDeadline = ((EditText)inputRow.findViewById(R.id.input_deadline)).getText().toString().trim();
        String taskStatus = ((EditText)inputRow.findViewById(R.id.input_status)).getText().toString().trim();
		ContentValues values = new ContentValues();
		values.put(SmallStepsContract.Tasks.COLUMN_NAME_TITLE, taskTitle);
		values.put(SmallStepsContract.Tasks.COLUMN_NAME_DEADLINE, taskDeadline);
		values.put(SmallStepsContract.Tasks.COLUMN_NAME_STATUS, taskStatus);
		values.put(SmallStepsContract.Tasks.COLUMN_NAME_BELONGING_GOAL_ID, goalId);
		values.put(SmallStepsContract.Tasks.COLUMN_NAME_UPDATED_AT, MyUtil.getSqlDateTime());
		if (!SmallStepsDbHelper.validateTasksValues(values)) return;

		// Insert the new row, returning the primary key value of the new row
		long newTaskId = db.insert(SmallStepsContract.Tasks.TABLE_NAME, null, values);
		
		// Update the task order
		if (taskOrder.size() < 1) {
    		taskOrder.add(newTaskId);
		}else {
			Object prevTaskIdObj = inputRow.getTag();
			if (prevTaskIdObj == null) { // the current task is the head
        	    taskOrder.add(0, newTaskId);
			}else {
      	        long prevTaskId = Long.parseLong(prevTaskIdObj.toString());
        	    taskOrder.add(taskOrder.indexOf(prevTaskId)+1, newTaskId);
			}
		}
		updateTaskOrderToDb();

		// Insert the new task to view
		Map<String, String> taskData = new HashMap<String, String>();
		taskData.put("id", String.valueOf(newTaskId));
		taskData.put("title", taskTitle);
		taskData.put("deadline", taskDeadline);
		taskData.put("status", taskStatus);
		View[] taskView = makeTaskView(taskData);
		View currentRow = inputRow;
		ViewGroup tasksContainer = (ViewGroup)getWindow().getDecorView().findViewById(R.id.tasks_container);
		tasksContainer.addView(taskView[0], tasksContainer.indexOfChild(currentRow)+1);
		tasksContainer.addView(taskView[1], tasksContainer.indexOfChild(currentRow)+2);

		// Reset the input row
		cancelNewTask(view);
	}
	
	public void confirmToDeleteTask(View view) {
	    View buttonShowDetail = getParentTaskContainer(view).findViewById(R.id.button_show_detail);
		// if the task has a child goal
		if (buttonShowDetail.getVisibility() == View.VISIBLE) {
	        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(R.string.confirm_to_delete_task);
            
            // Define the positive button action
            alertDialogBuilder.setPositiveButton(R.string.button_accept,
            new DialogInterface.OnClickListener() {
            	private View view;
            	private DialogInterface.OnClickListener setLocalVars(View view) {
            		this.view = view;
            		return this;
            	}
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	deleteTask(view);
                }
            }.setLocalVars(view));
            
      		// Define the negative button action
            alertDialogBuilder.setNegativeButton(R.string.button_deny,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                 	return;
                }
            });

            alertDialogBuilder.create().show();
		}else {
			deleteTask(view);
		}
	}
	
	private void deleteTask(View view) {
		SmallStepsDbHelper mDbHelper = new SmallStepsDbHelper(this);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		// Delete DB entry
		View currentTaskRow = getParentTaskContainer(view);
		String taskId = currentTaskRow.getTag().toString();
		String selection = SmallStepsContract.Tasks._ID + " = ?";
		String[] selectionArgs = { taskId };
		db.delete(SmallStepsContract.Tasks.TABLE_NAME, selection, selectionArgs);
		
		// Update the task order
		taskOrder.remove(taskOrder.indexOf(Long.parseLong(taskId)));
		updateTaskOrderToDb();
		
		// Remove the "id_as_task" value
		String[] taskIds = { taskId };
		removeIdAsTaskFromDb(taskIds);
		
		// Remove the view
		View nextInputRow = getNextInputContainer(currentTaskRow);
		currentTaskRow.setVisibility(View.GONE);
		nextInputRow.setVisibility(View.GONE);
		hideSoftKeyboard(view);
	}

	public void confirmToDeleteGoal(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.confirm_to_delete_goal);

        // Define the positive button action
        alertDialogBuilder.setPositiveButton(R.string.button_accept,
        new DialogInterface.OnClickListener() {
        	private View view;
        	private DialogInterface.OnClickListener setLocalVars(View view) {
        		this.view = view;
        		return this;
        	}
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	deleteGoal(view);
            }
        }.setLocalVars(view));

        // Define the negative button action
        alertDialogBuilder.setNegativeButton(R.string.button_deny,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                 	return;
                }
            });

        alertDialogBuilder.create().show();
	}
	
	private void deleteGoal(View view) {
        SmallStepsDbHelper mDbHelper = new SmallStepsDbHelper(SmallStepsActivity.this);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		// delete the goal entry
		String goalsSelection = SmallStepsContract.Goals._ID + " = ?";
		String[] goalsSelectionArgs = { String.valueOf(goalId) };
		db.delete(SmallStepsContract.Goals.TABLE_NAME, goalsSelection, goalsSelectionArgs);
		
		// delete the task entries
		int tasksCount = taskOrder.size();
		if (tasksCount > 0) {
        	String tasksSelection = SmallStepsContract.Goals._ID + " IN (" + SmallStepsDbHelper.makePlaceholders(taskOrder.size()) + ")";
        	String[] tasksSelectionArgs = new String[tasksCount];
        	for (int i = 0; i < tasksCount; i++) {
        		tasksSelectionArgs[i] = String.valueOf(taskOrder.get(i));
        	}
        	db.delete(SmallStepsContract.Tasks.TABLE_NAME, tasksSelection, tasksSelectionArgs);
    
    		// Remove the "parent_goal_id" value
    		removeIdAsTaskFromDb(tasksSelectionArgs);
		}
		
		// Remove the "id_as_goal" value
        ContentValues values = new ContentValues();
        values.put(SmallStepsContract.Tasks.COLUMN_NAME_ID_AS_GOAL, "");
		            values.put(SmallStepsContract.Tasks.COLUMN_NAME_UPDATED_AT, MyUtil.getSqlDateTime());
		String selection = SmallStepsContract.Tasks.COLUMN_NAME_ID_AS_GOAL + " = ?";
		String[] selectionArgs = { String.valueOf(goalId) };
		db.update(
		    SmallStepsContract.Tasks.TABLE_NAME,
		    values,
		    selection,
		    selectionArgs);
		
		// Back to the previous activity
		// If the current activity was called by startActivityForRestult
		if (getCallingActivity() != null) {
   	        Intent resultIntent = new Intent();
    	    resultIntent.putExtra(MESSAGE_NAME_DELETED_GOAL_ID, goalId);
    	    setResult(RESULT_OK, resultIntent);
    	    finish();		
		}else {
		    Intent intent = new Intent(this, SmallStepsPortalActivity.class);
	        startActivity(intent);
		}
	}
	
	public void createChildGoal(View view) {
		SmallStepsDbHelper mDbHelper = new SmallStepsDbHelper(this);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();	
		
		// Insert a new goal entry
        View taskRow = getParentTaskContainer(view);
        String taskId = taskRow.getTag().toString();
        String taskTitle = ((TextView)taskRow.findViewById(R.id.task_title)).getText().toString();
		ContentValues values = new ContentValues();
		values.put(SmallStepsContract.Goals.COLUMN_NAME_TITLE, taskTitle);
		values.put(SmallStepsContract.Goals.COLUMN_NAME_ID_AS_TASK, taskId);
		values.put(SmallStepsContract.Goals.COLUMN_NAME_CONTAINING_TASK_IDS, "");
		
		long newGoalId = db.insert(SmallStepsContract.Goals.TABLE_NAME, null, values);
		
		// Set the new goal id as "id_as_goal" of the task
        values = new ContentValues();
        values.put(SmallStepsContract.Tasks.COLUMN_NAME_ID_AS_GOAL, newGoalId);
		values.put(SmallStepsContract.Tasks.COLUMN_NAME_UPDATED_AT, MyUtil.getSqlDateTime());
		String tasksSelection = SmallStepsContract.Tasks._ID + " = ?";
		String[] tasksSelectionArgs = { taskId };
		db.update(
		    SmallStepsContract.Tasks.TABLE_NAME,
		    values,
		    tasksSelection,
		    tasksSelectionArgs);

		// Update "updated_at" of the goal
        values = new ContentValues();
		values.put(SmallStepsContract.Goals.COLUMN_NAME_UPDATED_AT, MyUtil.getSqlDateTime());
		String goalsSelection = SmallStepsContract.Goals._ID + " = ?";
		String[] goalsSelectionArgs = { String.valueOf(goalId) };
		db.update(
		    SmallStepsContract.Goals.TABLE_NAME,
		    values,
		    goalsSelection,
		    goalsSelectionArgs);

		// Switch the displayed detail button
    	View buttonShowDetail = taskRow.findViewById(R.id.button_show_detail);
        buttonShowDetail.setVisibility(View.VISIBLE);
        buttonShowDetail.setTag(newGoalId);
    	view.setVisibility(View.GONE);

		showTaskContents(newGoalId, taskTitle, true);
	}

	public void showChildGoal(View view) {
		TextView taskTitleCol = (TextView)getParentTaskContainer(view).findViewById(R.id.task_title);
		showTaskContents(Long.parseLong(view.getTag().toString()), taskTitleCol.getText().toString(), true);
	}

	private Cursor getTasksFromDb() {
		SmallStepsDbHelper mDbHelper = new SmallStepsDbHelper(this);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		String[] projection = {
		    SmallStepsContract.Tasks._ID,
		    SmallStepsContract.Tasks.COLUMN_NAME_TITLE,
		    SmallStepsContract.Tasks.COLUMN_NAME_DEADLINE,
		    SmallStepsContract.Tasks.COLUMN_NAME_STATUS,
		    SmallStepsContract.Tasks.COLUMN_NAME_ID_AS_GOAL
		};
		String selection = SmallStepsContract.Tasks.COLUMN_NAME_BELONGING_GOAL_ID + " = ?";
		String[] selectionArgs = { String.valueOf(goalId) };

		Cursor cursor = db.query(
		    SmallStepsContract.Tasks.TABLE_NAME,
		    projection,
		    selection,
		    selectionArgs,
		    null,
		    null,
		    null
        );
		return cursor;
	}
	
	private List<Long> getTaskOrdersFromDb() {
		SmallStepsDbHelper mDbHelper = new SmallStepsDbHelper(this);
		SQLiteDatabase db = mDbHelper.getReadableDatabase();

		String[] projection = {
		    SmallStepsContract.Goals.COLUMN_NAME_CONTAINING_TASK_IDS,
		};
		String selection = SmallStepsContract.Goals._ID + " = ?";
		String[] selectionArgs = { String.valueOf(goalId) };

		Cursor cursor = db.query(
		    SmallStepsContract.Goals.TABLE_NAME,
		    projection,
		    selection,
		    selectionArgs,
		    null,
		    null,
		    null
        );
		cursor.moveToFirst();
    	String taskIdStr = cursor.getString(
            cursor.getColumnIndexOrThrow(SmallStepsContract.Goals.COLUMN_NAME_CONTAINING_TASK_IDS)
        );
      	List<Long> taskIds = new ArrayList<Long>();
    	if (taskIdStr.length() > 2) {
        	taskIdStr = taskIdStr.substring(1, taskIdStr.length()-1);
        	for (String taskId : taskIdStr.split(", ")) {
        		taskIds.add(Long.parseLong(taskId));
        	}
    	}
    	return taskIds;
	}
	
	private void updateTaskOrderToDb() {
		SmallStepsDbHelper mDbHelper = new SmallStepsDbHelper(this);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(SmallStepsContract.Goals.COLUMN_NAME_CONTAINING_TASK_IDS, taskOrder.toString());
		values.put(SmallStepsContract.Goals.COLUMN_NAME_UPDATED_AT, MyUtil.getSqlDateTime());
		String selection = SmallStepsContract.Goals._ID + " = ?";
		String[] selectionArgs = { String.valueOf(goalId) };
		db.update(
		    SmallStepsContract.Goals.TABLE_NAME,
		    values,
		    selection,
		    selectionArgs);
	}
	
	private void removeIdAsTaskFromDb(String[] taskIds) {
		SmallStepsDbHelper mDbHelper = new SmallStepsDbHelper(this);
		SQLiteDatabase db = mDbHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.putNull(SmallStepsContract.Goals.COLUMN_NAME_ID_AS_TASK);
		values.put(SmallStepsContract.Goals.COLUMN_NAME_UPDATED_AT, MyUtil.getSqlDateTime());
		String selection = SmallStepsContract.Goals.COLUMN_NAME_ID_AS_TASK +
				           " IN (" + SmallStepsDbHelper.makePlaceholders(taskIds.length) + ")";
		String[] selectionArgs = taskIds;
		db.update(
		    SmallStepsContract.Goals.TABLE_NAME,
		    values,
		    selection,
		    selectionArgs);
	}
	
	private View[] makeTaskView(Map<String,String> taskData) {
    	// Create a task view from layout xml
		View taskItemRow = View.inflate(this, R.layout.partial_tasks_item_row, null);
		View taskInputRow = View.inflate(this, R.layout.partial_tasks_input_row, null);
		TextView titleView = (TextView)taskItemRow.findViewById(R.id.task_title);
		TextView deadlineView = (TextView)taskItemRow.findViewById(R.id.task_deadline);
		TextView statusView = (TextView)taskItemRow.findViewById(R.id.task_status);

		// Set data to the task view
		if (taskData != null) {
    		taskItemRow.setTag(taskData.get("id"));
    		taskInputRow.setTag(taskData.get("id"));
    		titleView.setText(taskData.get("title"));
    		deadlineView.setText(taskData.get("deadline"));
    		statusView.setText(taskData.get("status"));
		}
		
		// Switch the displayed detail button
		String idAsGoal = taskData.get("id_as_goal");
		if (idAsGoal != null && !idAsGoal.isEmpty() && Long.parseLong(idAsGoal) > 0) {
			View buttonShowDetail = taskItemRow.findViewById(R.id.button_show_detail);
			buttonShowDetail.setVisibility(View.VISIBLE);
			buttonShowDetail.setTag(idAsGoal);
		}else {
			taskItemRow.findViewById(R.id.button_add_detail).setVisibility(View.VISIBLE);
		}
		
		// Toggle operation buttons when task item is long clicked
		taskItemRow.findViewById(R.id.task_item_text_container)
		                         .setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				toggleOperationButtons(view);
				return true;
			}
		});
		
		
		// Return an array containing the item row and the input row
		View[] taskView = new View[2];
		taskView[0] = taskItemRow;
		taskView[1] = taskInputRow;
		return taskView;
	}
	
	private View getParentTaskContainer(View childView) {
		try {
		    View parentView = ((View)childView.getParent());
    		if (parentView != null) {
    			int parentViewId = parentView.getId();
        		if (parentViewId == R.id.task_item_container ||
        		    parentViewId == R.id.task_input_container ||
        		    parentViewId == R.id.task_title_container) {
        			return parentView;
        		}else {
        			return getParentTaskContainer(parentView);
        		}
    		}
		}catch (ClassCastException e) {
			return null;
		}
		return null;
	}
	
	private View getNextInputContainer(View currentTaskContainer) {
		ViewGroup tasksContainer = (ViewGroup)getWindow().getDecorView().findViewById(R.id.tasks_container);
		return tasksContainer.getChildAt(tasksContainer.indexOfChild(currentTaskContainer)+1);
	}
	
	private void toggleOperationButtons(View view) {
	    View taskContainer = getParentTaskContainer(view);
		View buttonsContainer = taskContainer.findViewById(R.id.task_item_buttons_container);
		// If operation buttons are invisible
		if (buttonsContainer.getTranslationX() > 0) {
			buttonsContainer.setTranslationX(buttonsContainer.getWidth());
			buttonsContainer.animate().translationX(0);
		}else {
			buttonsContainer.animate().translationX(buttonsContainer.getWidth());
            View nextInputRow = getNextInputContainer(taskContainer);
            cancelNewTask(nextInputRow.findViewById(R.id.operation_button_cancel));
		}
	}

}