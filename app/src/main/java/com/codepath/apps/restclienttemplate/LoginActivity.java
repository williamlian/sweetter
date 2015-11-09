package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.codepath.apps.restclienttemplate.models.LoginUser;
import com.codepath.oauth.OAuthLoginActionBarActivity;

public class LoginActivity extends OAuthLoginActionBarActivity<RestClient> {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}


	// Inflate the menu; this adds items to the action bar if it is present.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	// OAuth authenticated successfully, launch primary authenticated activity
	// i.e Display application "homepage"
	@Override
	public void onLoginSuccess() {
        LoginUser.load(this, new LoginUser.LoadLoginCallback() {
            @Override
            public void onSuccess() {
                Intent i = new Intent(LoginActivity.this, TimelineActivity.class);
                startActivity(i);
            }
            @Override
            public void onFailure(Throwable t) {
                onLoginFailure((Exception)t);
            }
        });
	}

	// OAuth authentication flow failed, handle the error
	// i.e Display an error dialog or toast
	@Override
	public void onLoginFailure(Exception e) {
        e.printStackTrace();
        new MaterialDialog.Builder(this)
            .title("Sorry, we cannot log you in")
            .content("Some error happened during login process: " + e.getMessage())
            .positiveText("Ok")
            .show();
	}

	// Click handler method for the button used to start OAuth flow
	// Uses the client to initiate OAuth authorization
	// This should be tied to a button used to login
	public void loginToRest(View view) {
		getClient().connect();
	}

}
