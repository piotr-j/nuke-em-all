package io.piotrjastrzebski.gdxjam.nta;

import android.os.Bundle;

import android.util.Log;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;
import org.slf4j.LoggerFactory;

public class AndroidLauncher extends AndroidApplication {
	FirebaseAuth auth;
	FirebaseFunctions functions;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initLogs();
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

	void initLogs () {
		// this is equivalent to desktop logger, more or less
		// main difference is that we dont have date, as logcat already sets it
		LoggerContext lc = (LoggerContext)LoggerFactory.getILoggerFactory();
		lc.stop();

		PatternLayoutEncoder tagEncoder = new PatternLayoutEncoder();
		tagEncoder.setContext(lc);
		tagEncoder.setPattern("NTA");
		tagEncoder.start();

		PatternLayoutEncoder encoder = new PatternLayoutEncoder();
		encoder.setContext(lc);
		encoder.setPattern("%5p [%15.15t] .\\(%-40.40(%file:%line\\)) : %msg");
		encoder.start();

		LogcatAppender logcatAppender = new LogcatAppender();
		logcatAppender.setContext(lc);
		logcatAppender.setTagEncoder(tagEncoder);
		logcatAppender.setEncoder(encoder);
		logcatAppender.start();

		Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		root.addAppender(logcatAppender);
	}
}
