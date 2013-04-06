package com.drive.utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.drive.utility.database.Article;
import com.drive.utility.database.Database_DataSource_News;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private Adapter_Article adp;
	private ListView listview;
	
	private SharedPreferences prefences;
	private Editor prefences_editor;
	private Database_DataSource_News database;
	
	public static final String ACCOUNT_NAME = "ACCOUNT_NAME";
	static final int REQUEST_ACCOUNT_PICKER = 1;
	static final int REQUEST_DRIVE_DOWNLOAD = 2;
	static final int REQUEST_DRIVE_UPLOAD = 3;

	private static Drive service;
	private GoogleAccountCredential credential;

	@Override
	public void onCreate(Bundle savedInstanceState) {									
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.activity_main);
		
		database = new Database_DataSource_News(this);
		
		adp = new Adapter_Article(this, getArticlesFromDatabase() );
		listview = (ListView) findViewById(R.id.main_listview);
	    listview.setAdapter(adp);
	   
	    credential = GoogleAccountCredential.usingOAuth2(this, DriveScopes.DRIVE_FILE);
	    prefences = getPreferences(MODE_PRIVATE);
	    prefences_editor = prefences.edit();
	    
	   
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
			case REQUEST_ACCOUNT_PICKER:
				if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
					String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
					if (setAccountName (accountName)) 
						getDriveService(accountName);
				}
				break;
			case REQUEST_DRIVE_UPLOAD : case REQUEST_DRIVE_DOWNLOAD :
				if (resultCode == Activity.RESULT_OK) {
					performDriveFunction (requestCode);
				} else {
					startActivityForResult(credential.newChooseAccountIntent(), requestCode);
				}
				break;
		}
	}
	
	private void getDriveService(String accountName){
		credential.setSelectedAccountName(accountName);
		service = getDriveService(credential);
	}
	
	public ArrayList<Article> getArticlesFromDatabase(){
		database.open();
		ArrayList<Article> articles = database.getAllArticle();
		database.close();
		return articles;
	}

	private void saveFileToDrive(final File file) {
	    new Thread(new Runnable() {
	    	
	    	@Override
	    	public void run() {
	    		try {
	    			java.io.File fileContent = Utils_Drive.writeStringToFile(Utils_Drive.articlesToJson(getArticlesFromDatabase()));
	    			FileContent mediaContent = new FileContent(Utils_Drive.DRIVE_MIME_TYPE_TEXT, fileContent );
	    			File body = Utils_Drive.getMetaDataFile();
	    			
	    			if ( file == null) 
	    				service.files().insert(body, mediaContent).execute();
	    			else
	    				service.files().update(file.getId(), body, mediaContent).execute();
	    			showToast("File Upload: " + "Successful");
	    		} catch (UserRecoverableAuthIOException e) {
	    			startActivityForResult(e.getIntent(), REQUEST_DRIVE_UPLOAD);
	    		} catch (IOException e) {
	    			showToast("File Upload: " + "Unsuccessful");
	    			e.printStackTrace();
	    		}
	    	}
    	}).start();
	}
	
	private void getFileFromDrive(final File file) {
		
	    Thread t = new Thread(new Runnable() {
	    	
	    	@Override
	    	public void run() {
	    		
	    		try {
    				if (file != null) {
	    				try {
	    					final Data_Backup backup = Utils_Drive.downloadDriveFile(service,file);
		    				runOnUiThread(new Runnable(){
		    					@Override
		    					public void run() {
		    	    				database.setArticles(backup.articles);
		    						adp.setArticles(backup.articles);
		    						adp.notifyDataSetChanged();
		    						showToast("File Download: " + "Successful");
		    					}});
	    				} catch (com.google.gson.JsonSyntaxException e) {
	    					showToast("Data Corrupted: Deleting file");
	    					service.files().delete(file.getId()).execute();
	    	    		}
	    			} else {
	    				showToast("File not Found");
	    			}
	    			
	    		} catch (UserRecoverableAuthIOException e) {
	    			showToast("Auth Error");
	    			startActivityForResult(e.getIntent(), REQUEST_DRIVE_DOWNLOAD);
	    		} catch (IOException e) {
	    			showToast("I/O Error");
	    			e.printStackTrace();
	    		} 
	    	}
	    	
    	});
	    t.start();
	}
	
	private void performDriveFunction(final int driveFunctionType) {

		new Thread(new Runnable() {
	    	
	    	@Override
	    	public void run() {
	    		try {
	    			File file = null;
	    			List<File> list = Utils_Drive.retrieveRelevantFiles(service);
	    			for (File gFile : list){
	    				if (file == null || gFile.getModifiedDate().getValue() >  file.getModifiedDate().getValue() ){
	    					file = gFile;
	    				} else {
	    					service.files().delete(gFile.getId()).execute();
	    				}
	    			}
	    			switch (driveFunctionType) {
		    			case REQUEST_DRIVE_UPLOAD :
		    				saveFileToDrive(file);
		    				break;
		    			case REQUEST_DRIVE_DOWNLOAD :
		    				getFileFromDrive(file);
		    				break;
	    			}
	    		} catch (UserRecoverableAuthIOException e) {
	    			showToast("Auth Error");
	    			startActivityForResult(e.getIntent(), REQUEST_DRIVE_DOWNLOAD);
	    		} catch (IOException e) {
	    			showToast("I/O Error");
	    			e.printStackTrace();
	    		}
	    	}
		}).start();
	}
	
	private Drive getDriveService(GoogleAccountCredential credential) {
		return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
        	.build();
	}

	public void showToast(final String toast) {
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item){
		int itemId = item.getItemId();
		
		switch(itemId) {
			case R.id.menu_add : case R.id.menu_delete :
				database.open();
				int size = adp.getCount();
				if ( itemId == R.id.menu_add) {
					for ( int i = 0 ; i < 10 ; i ++) {
						adp.addArticle(
								database.createArticle(
										"Title for artcile " + size , 
										"Summary for artcile " + size++ ) );	
					}
					showToast("10 items added");
				} else {
					database.deleteAllArticles();
					adp.deleteAllArticles();
					showToast(size +" items deleted");
				}
				adp.notifyDataSetChanged();
				database.close();
				break;
			case R.id.menu_upload : case R.id.menu_download :
				
				if ( service == null ) {
					String accountName = getAccountName();
					if (accountName == null) {
						showToast("Please add a Google account");
						return true;
					} else {
						getDriveService(accountName); 
					}
				} 
				
				performDriveFunction ( (itemId == R.id.menu_upload) ? REQUEST_DRIVE_UPLOAD  : REQUEST_DRIVE_DOWNLOAD);
				
				break;
			case R.id.menu_account :
				startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
				break;
		}
		return true;
	}
		
	private String getAccountName() {
		return prefences.getString(ACCOUNT_NAME, null);
	}
		
	private boolean setAccountName(String accountName) {
		if ( accountName != null) 
			return  prefences_editor.putString(ACCOUNT_NAME, accountName).commit();
		return false;
	}
	
}




   