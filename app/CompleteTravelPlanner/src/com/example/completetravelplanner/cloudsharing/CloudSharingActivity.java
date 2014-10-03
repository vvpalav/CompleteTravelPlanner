package com.example.completetravelplanner.cloudsharing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.completetravelplanner.R;
import com.example.completetravelplanner.helper.SharedUtil;

public class CloudSharingActivity extends Activity {

	List<Bitmap> bitmap = new ArrayList<Bitmap>();
	private FileOutputStream fos;
	Button btnGalleryPickMul;
	String action;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_sharing_activity);
		setTitle("Cloud Photo Sharing");
        init();
	}

	private void init() {

		btnGalleryPickMul = (Button) findViewById(R.id.btnGalleryPickMul);
		btnGalleryPickMul.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(Action.ACTION_MULTIPLE_PICK);
				startActivityForResult(i, 200);
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
			String[] all_path = data.getStringArrayExtra("all_path");
			new S3PutObjectTask().execute(all_path);
		}
	}
	public void fetchFromS3(View V){
		new S3GetObjectTask().execute();
	}
	
	private class S3PutObjectTask extends AsyncTask<String[], Void, S3TaskResult> {

		ProgressDialog dialog;

		protected void onPreExecute() {
			dialog = new ProgressDialog(CloudSharingActivity.this);
			dialog.setMessage(CloudSharingActivity.this
					.getString(R.string.uploading));
			dialog.setCancelable(false);
			dialog.show();
		}

		protected S3TaskResult doInBackground(String[]... uris) {
			S3TaskResult result = new S3TaskResult();
			String[] strPaths =  uris[0];
			AmazonS3Client s3Client = new AmazonS3Client( new BasicAWSCredentials( "AKIAIRRUU5VJXWYV32FQ", "EPt4aqKPoUI1ep/A1amwLUeS42oh7lPaMhvQz2pk" ) );
			for(int i = 0; i < strPaths.length; i++){
				PutObjectRequest por = new PutObjectRequest( "ninadbkt", "CTP"+ SharedUtil.getTime() + i, new java.io.File(strPaths[i]) );  
				s3Client.putObject( por );
			}
			return result;
		}

		protected void onPostExecute(S3TaskResult result) {

			dialog.dismiss();

			if (result.getErrorMessage() != null) {

				displayErrorAlert(
						CloudSharingActivity.this
								.getString(R.string.upload_failure_title),
						result.getErrorMessage());
			}
		}
	}
	private class S3GetObjectTask extends AsyncTask<Void, Void, S3TaskResult> {

		ProgressDialog dialog;

		protected void onPreExecute() {
			dialog = new ProgressDialog(CloudSharingActivity.this);
			dialog.setMessage(CloudSharingActivity.this
					.getString(R.string.downloading));
			dialog.setCancelable(false);
			dialog.show();
		}

		protected S3TaskResult doInBackground(Void... voids) {
			S3TaskResult result = new S3TaskResult();
			AmazonS3Client s3Client = new AmazonS3Client( new BasicAWSCredentials( "AKIAIRRUU5VJXWYV32FQ", "EPt4aqKPoUI1ep/A1amwLUeS42oh7lPaMhvQz2pk" ) );
			ObjectListing objectListing = s3Client.listObjects(new ListObjectsRequest().withBucketName("ninadbkt"));
			List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();
			List<String> key = new ArrayList<String>();
			for (S3ObjectSummary summary : objectSummaries) {
				key.add(summary.getKey());
			}
			for(String k:key){
				S3ObjectInputStream content = s3Client.getObject("ninadbkt", k).getObjectContent();
				byte[] bytes = null;
				try {
					bytes = IOUtils.toByteArray(content);
				} catch (IOException e) {
					e.printStackTrace();
				}
				bitmap.add(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
			}
			return result;
		}

		protected void onPostExecute(S3TaskResult result) {
			int i = 0;
			for(Bitmap b: bitmap){
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				b.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
				File file = new File(Environment.getExternalStorageDirectory()
						+ File.separator + "CTP" + File.separator + "CTP"+ SharedUtil.getTime() + i +".jpg");
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				/*--- create a new FileOutputStream and write bytes to file ---*/
				try {
					fos = new FileOutputStream(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				try {
					fos.write(bytes.toByteArray());
					fos.close();
					Toast.makeText(CloudSharingActivity.this, "Image saved", Toast.LENGTH_SHORT).show();
				} catch (IOException e) {
					e.printStackTrace();
				}

				i++;

			}

			dialog.dismiss();

			if (result.getErrorMessage() != null) {

				displayErrorAlert(
						CloudSharingActivity.this
						.getString(R.string.download_failure_title),
						result.getErrorMessage());
			}
		}
	}
	
	protected void displayErrorAlert(String title, String message) {

		AlertDialog.Builder confirm = new AlertDialog.Builder(this);
		confirm.setTitle(title);
		confirm.setMessage(message);

		confirm.setNegativeButton(
				CloudSharingActivity.this.getString(R.string.ok),
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						CloudSharingActivity.this.finish();
					}
				});

		confirm.show().show();
	}

	private class S3TaskResult {
		String errorMessage = null;
		public String getErrorMessage() {
			return errorMessage;
		}
	}
}
