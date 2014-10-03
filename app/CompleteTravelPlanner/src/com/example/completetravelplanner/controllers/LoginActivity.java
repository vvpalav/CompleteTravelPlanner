package com.example.completetravelplanner.controllers;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.completetravelplanner.R;
import com.example.completetravelplanner.services.ServiceHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.text.TextUtils;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {

	private View focusView = null;

	private EditText userNameView;
	private EditText telephoneView;
	private EditText emailView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		setTitle("Registration");
		userNameView = (EditText) findViewById(R.id.person_name);
		telephoneView = (EditText) findViewById(R.id.person_phone);
		emailView = (EditText) findViewById(R.id.person_email);
		createRegisterListeners();
	}

	private void createRegisterListeners() {
		final Intent intent = new Intent(this,
				InputRegistrationCodeActivity.class);
		final JSONObject json = new JSONObject();
		try {
			final ServiceHandler hand = new ServiceHandler(this);
			json.put("transaction_type", "NEW_USER_REGISTRATION");
			final SharedPreferences shared = PreferenceManager
					.getDefaultSharedPreferences(MainActivity
							.getContextOfApplication());
			Button register = (Button) findViewById(R.id.register);
			register.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (checkUserInputs()) {
						try {
							json.put("name", userNameView.getText().toString()
									.trim());
							String line = telephoneView.getText().toString()
									.trim();
							json.put("telephone",
									line.substring(line.indexOf("+") + 1));
							json.put("email_id", emailView.getText().toString()
									.trim());
							json.put("gcm_id", emailView.getText().toString()
									.trim());
							intent.putExtra("json", json.toString());
							hand.startServices(json.toString());
							while (true) {
								Thread.sleep(500);
								if (shared.contains("is_user_accepted")) {
									if (shared.getBoolean("is_user_accepted", false)) {
										intent.putExtra("TYPE_USER", "NEW_USER");
										startActivity(intent);
										finish();
										break;
									} else {
										shared.edit().remove("is_user_accepted").commit();
										AlertDialog.Builder builder = new AlertDialog.Builder(
												LoginActivity.this);
										builder.setMessage(shared.getString("server_res", 
												json.getString("telephone")
												+ " already registered with server"));
										builder.setTitle("Invalid Telephone");
										builder.setNegativeButton("Use new telephone", 
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int id) {
														telephoneView.setText("");
														focusView = telephoneView;
														telephoneView.setError("Pick unique telephone");
														focusView.requestFocus();
													}
												});
										builder.setPositiveButton("Continue",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int id) {
														try {
															json.put("transaction_type", 
																	"EXISTING_USER_REGISTRATION");
															hand.startServices(json.toString());
															intent.putExtra("TYPE_USER", "EXISTING_USER");
															startActivity(intent);
															finish();
														} catch (JSONException e) {
															e.printStackTrace();
														}
													}
												});
										AlertDialog dialog = builder.create();
										dialog.show();
										break;
									}
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						} catch (InterruptedException ex) {
							ex.printStackTrace();
						}
					} else {
						focusView.requestFocus();
					}
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	protected boolean checkUserInputs() {
		boolean flag = false;

		userNameView.setError(null);
		String userName = userNameView.getText().toString().trim();

		userNameView.setError(null);
		String telephone = telephoneView.getText().toString().trim();

		userNameView.setError(null);
		String email = emailView.getText().toString().trim();

		if (TextUtils.isEmpty(userName)) {
			userNameView.setError(getString(R.string.error_field_required));
			focusView = userNameView;
		} else if (TextUtils.isEmpty(telephone)) {
			telephoneView.setError(getString(R.string.error_field_required));
			focusView = telephoneView;
		} else if (TextUtils.isEmpty(email)) {
			emailView.setError(getString(R.string.error_field_required));
			focusView = emailView;
		} else if (!email.contains("@")) {
			emailView.setError(getString(R.string.error_invalid_email));
			focusView = emailView;
		} else if (TextUtils.isDigitsOnly(telephone)) {
			telephoneView.setError(getString(R.string.error_incorrect_phone));
			focusView = telephoneView;
		} else {
			flag = true;
		}
		return flag;
	}
}