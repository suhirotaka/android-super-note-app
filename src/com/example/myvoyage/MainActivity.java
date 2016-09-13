package com.example.myvoyage;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
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
	
	public void chooseActivity(View view) {
		Intent intent;
		switch (view.getId()) {
    		case R.id.to_small_steps:
                intent = new Intent(this, SmallStepsPortalActivity.class);
   		        startActivity(intent);
                break;
    		case R.id.to_todo_list:
                intent = new Intent(this, TodoListActivity.class);
   		        startActivity(intent);
                break;
            default:
		}
	}
	
	public void exportAppData(View view) {
		String dstFileName = "MyVoyager_data_" + (new Date()).getTime();
		String[] dataFileNames = { "SmallSteps.db" };
		String dstDirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()
		                  + File.separator + dstFileName;
		boolean failedFlag = false;
		try {
    		for (String dataFileName : dataFileNames) {
        	    File srcFile = new File(getApplicationContext().getDatabasePath(dataFileName).getPath());
        	    File dstFile = new File(dstDirPath, dataFileName);
        	    if (dstFile.getParentFile().mkdirs()) {
            	    dstFile.createNewFile();
            	    if (dstFile.exists()) {
            	        if (!MyUtil.copyFile(srcFile, dstFile)) {
            	        	failedFlag = true;
            	        	break;
            	        }
            	    }else {
            	    	failedFlag = true;
            	    	break;
            	    }
        	    }else {
        	    	failedFlag = true;
        	    	break;
        	    }
    		}
		}catch (IOException e){
			failedFlag = true;
		}finally {
    		if (failedFlag) {
       	       	Toast.makeText(this, R.string.data_export_failed, Toast.LENGTH_SHORT).show();
    		}else {
       	       	Toast.makeText(this, getString(R.string.data_export_succeeded) + "(" + dstDirPath + ")", Toast.LENGTH_LONG).show();
    		}
		}
	}

}