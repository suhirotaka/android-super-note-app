package com.example.myvoyage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

abstract public class BaseActivity  extends Activity {
	
	private InputMethodManager imm;
	static final protected String MESSAGE_NAME_GOAL_ID = "goal_id";
	static final protected String MESSAGE_NAME_GOAL_TITLE = "goal_title";
	static final protected String MESSAGE_NAME_DELETED_GOAL_ID = "deleted_goal_id";
	static final protected int REQUEST_CODE_SHOW_TASK = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	
	public void showSoftKeyboard(View view) {
		imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
	}
	
	public void hideSoftKeyboard(View view) {
    	imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
    protected void showTaskContents(long goalId, String goalTitle, boolean forResult) {
		// Start SmallStepsActivity
	    Intent intent = new Intent(this, SmallStepsActivity.class);
	    intent.putExtra(MESSAGE_NAME_GOAL_ID, goalId);
	    intent.putExtra(MESSAGE_NAME_GOAL_TITLE, goalTitle);
	    if (forResult) {
	        startActivityForResult(intent, REQUEST_CODE_SHOW_TASK);
	    }else {
	        startActivity(intent);
	    }
    }
    
}