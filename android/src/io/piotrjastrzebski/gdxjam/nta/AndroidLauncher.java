package io.piotrjastrzebski.gdxjam.nta;

import android.os.Bundle;

import android.util.Log;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;

public class AndroidLauncher extends AndroidApplication {
	FirebaseAuth auth;
	FirebaseFunctions functions;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new NukeGame(), config);
		auth = FirebaseAuth.getInstance();
		functions = FirebaseFunctions.getInstance();
	}

	@Override
	protected void onStart () {
		super.onStart();
		FirebaseUser currentUser = auth.getCurrentUser();

		if (currentUser != null) {
			Log.d("AL", "fbu: already signed in");
			return;
		}
		Log.d("AL", "fbu: signing in");
		auth.signInAnonymously().addOnCompleteListener(this, task -> {
			if (task.isSuccessful()) {
				Log.d("AL", "fbu: success");
				userChanged(auth.getCurrentUser());
			} else {
				Log.w("AL", "fbu: failure", task.getException());
				userChanged(null);
			}
		});
	}

	void userChanged (FirebaseUser user) {
		Log.d("AL", "User = " + user);
		if (user != null) {
			// logged in
			Log.d("AL", "uid = " + user.getUid());
		} else {
			// logged out
		}

	}
}
