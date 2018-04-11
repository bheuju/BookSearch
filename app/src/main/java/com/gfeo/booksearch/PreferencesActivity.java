package com.gfeo.booksearch;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Displays a list of preferences to the user. The layout consists only of a Toolbar and a
 * Fragment, that is {@link BooksPreferencesFragment}.
 *
 * @author gabrielfeo
 */

public class PreferencesActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preferences);
		setSupportActionBar((Toolbar) findViewById(R.id.preferences_toolbar));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	public static class BooksPreferencesFragment extends PreferenceFragment {
		@Override
		public void onCreate(@Nullable Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);

		}
	}
}
