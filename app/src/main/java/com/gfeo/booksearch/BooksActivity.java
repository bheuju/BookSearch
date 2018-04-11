package com.gfeo.booksearch;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;

/**
 * Displays a list of books to the user, loaded from search results returned by the <a
 * href="https://developers.google.com/books/docs/overview">Google
 * Books API</a>, according to the search query entered. The user can adjust query settings, such
 * as results sorting in the {@link PreferencesActivity}.
 *
 * @author gabrielfeo
 */

public class BooksActivity extends AppCompatActivity {

	private final static String LOG_TAG = BooksActivity.class.getSimpleName();
	static int httpResponseCode;
	private static String searchQuery;
	private static ArrayList<Book> bookArrayList;
	private final SearchView.OnQueryTextListener searchViewOnQueryTextListener =
			new SearchView.OnQueryTextListener() {

				/**
				 * Called when the user submits the search query. Returns early if the search
				 * query hasn't changed since the last submission. Clears the static
				 * {@link BooksActivity#bookArrayList} and (re)starts the loader that fetches the
				 * search results using the
				 * <a href="https://developers.google.com/books/docs/overview"> Google Books
				 * API</a>.
				 *
				 * @see BooksActivity#loaderCallbacks
				 * @return true, since the query has already been handled
				 */
				@Override
				public boolean onQueryTextSubmit(String query) {
					searchQuery = query;
					bookArrayList.clear();
					getLoaderManager().restartLoader(0, null, loaderCallbacks);
					Loader loader0 = getLoaderManager().getLoader(0);
					if (loader0 != null) {
						loader0.forceLoad();
					}
					return true;
				}

				@Override
				public boolean onQueryTextChange(String newText) {
					return false;
				}
			};
	private BookArrayAdapter bookArrayAdapter;
	private ListView listView;
	private final LoaderManager.LoaderCallbacks loaderCallbacks =
			new LoaderManager.LoaderCallbacks() {
				@Override
				public Loader onCreateLoader(int id, Bundle args) {
					if (existsActiveNetworkConnection()) {
						showProgressBarView();
						return new BooksLoader(BooksActivity.this,
						                       bookArrayList);
					} else {
						showNoInternetView();
						return null;
					}
				}

				@Override
				public void onLoadFinished(Loader loader, Object data) {
					if (httpResponseCode != 200) {
						Toast.makeText(getApplicationContext(),
						               getString(R.string.books_toast_http_response_code),
						               Toast.LENGTH_LONG)
						     .show();
						return;
					}
					int arrayListSize = ((ArrayList) data).size();
					if (QueryUtils.numberOfResults == 0) {
						showNoBooksFoundView();
						return;
					}
					if (arrayListSize < QueryUtils.maxResultsValue &&
							arrayListSize < QueryUtils.numberOfResults) {
						View container = View.inflate(BooksActivity.this,
						                              R.layout.container,
						                              null
						                             );
						listView.addFooterView(View.inflate(BooksActivity.this,
						                                    R.layout.listitem_books_footer,
						                                    (ViewGroup) container
								                                    .findViewById(R.id.container)
						                                   ));
					}
					updateAdapterWithResults();
				}

				@Override
				public void onLoaderReset(Loader loader) {
				}
			};

	/**
	 * Sets the Toolbar for the Activity; sets the default preference values, if applicable;
	 * initializes the static {@link BooksActivity#bookArrayList} field <u>if it hasn't been
	 * already</u>, so that it isn't reset when the Activity is instantiated a second time; sets
	 * a {@link BookArrayAdapter} as the {@link ListView} adapter and displays an educational
	 * start View ({@link BooksActivity#showInstructionsView()}) if the {@code ListView} isn't
	 * populated (when the adapter has nothing to populate it with); sets an
	 * {@link AdapterView.OnItemClickListener} to the {@code ListView} that either sends an
	 * {@link Intent} for opening a webpage with info of the current {@link Book} or displays an
	 * error {@link Toast} message.
	 *
	 * @see PreferenceManager#setDefaultValues(Context, int, boolean)
	 * @see Book#getInfoPageUri()
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_books);

		PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
		setSupportActionBar((Toolbar) findViewById(R.id.books_toolbar));
		if (bookArrayList == null) {
			bookArrayList = new ArrayList<>();
		}
		bookArrayAdapter = new BookArrayAdapter(this, bookArrayList);
		listView = findViewById(R.id.books_listview);
		listView.setAdapter(bookArrayAdapter);
		if (listView.getCount() == 0) {
			showInstructionsView();
		}
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final Book currentBook = bookArrayAdapter.getItem(position);
				if (currentBook.getInfoPageUri() != null) {
					Intent intent = new Intent(Intent.ACTION_VIEW, currentBook.getInfoPageUri());
					if (intent.resolveActivity(getPackageManager()) != null) {
						startActivity(intent);
					}
				} else {
					Toast.makeText(BooksActivity.this,
					               getString(R.string.books_toast_no_info_page),
					               Toast.LENGTH_SHORT
					              )
					     .show();
				}
			}
		});
	}

	/**
	 * Inflates the layout for the Activity's {@link Menu} and configures the {@link SearchView}
	 * related to the Search button on the {@code Menu}.
	 *
	 * @return true, so that the inflated {@code Menu} layout will be displayed
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.toolbar_menu, menu);
		SearchView searchView = (SearchView) menu.findItem(R.id.books_toolbarmenuitem_search)
		                                         .getActionView();
		searchView.setQueryHint("Search Google Books");
		searchView.setIconifiedByDefault(true);
		searchView.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
		searchView.setOnQueryTextListener(searchViewOnQueryTextListener);

		return true;
	}

	/**
	 * Checks if the selected {@link MenuItem} corresponds to the one referring to
	 * {@link PreferencesActivity}, and sends an Intent for starting the activity, if applicable.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.books_toolbarmenuitem_preferences:
				Intent intent = new Intent(this, PreferencesActivity.class);
				startActivity(intent);
				return true;
		}
		return true;
	}

	/**
	 * Queries the {@link ConnectivityManager} SystemService for the active network's information
	 * . Provides the caller with basic connectivity information (has active connection or
	 * not) useful for flow control on Internet related functions.
	 *
	 * @return a boolean stating whether an active connection (or an impending,
	 * "connecting") is available or not.
	 */
	private boolean existsActiveNetworkConnection() {
		ConnectivityManager cm = (ConnectivityManager)
				getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork;
		try {
			activeNetwork = cm.getActiveNetworkInfo();
		} catch (NullPointerException e) {
			activeNetwork = null;
		}
		return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
	}

	/**
	 * Displays a start View to the user, showing a TextView with basic instructions to use the
	 * app for searching books.
	 */
	private void showInstructionsView() {
		findViewById(R.id.books_textview_instructions).setVisibility(View.VISIBLE);
	}

	/**
	 * Displays a progress bar to the user and hides all the other possibly visible Views.
	 */
	private void showProgressBarView() {
		findViewById(R.id.books_textview_instructions).setVisibility(View.GONE);
		findViewById(R.id.books_textview_no_internet).setVisibility(View.GONE);
		findViewById(R.id.books_textview_no_books_found).setVisibility(View.GONE);
		findViewById(R.id.books_progressbar).setVisibility(View.VISIBLE);
	}

	/**
	 * Hides the progress bar and updates the {@link BookArrayAdapter} with the search results.
	 */
	private void updateAdapterWithResults() {
		findViewById(R.id.books_progressbar).setVisibility(View.GONE);
		bookArrayAdapter.notifyDataSetChanged();
	}

	/**
	 * Displays the "No internet connection" View and hides any other that could be visible.
	 * Before manipulating the Views, the {@link BookArrayAdapter} is updated so
	 * that it will clear all the views. Before this method is called, when the query text is
	 * submitted, the {@link BooksActivity#bookArrayList} is cleared, thus when the
	 * BookArrayAdapter is updated, the ListView will be empty.
	 */
	private void showNoInternetView() {
		bookArrayAdapter.notifyDataSetChanged();
		findViewById(R.id.books_textview_instructions).setVisibility(View.GONE);
		findViewById(R.id.books_progressbar).setVisibility(View.GONE);
		findViewById(R.id.books_textview_no_books_found).setVisibility(View.GONE);
		findViewById(R.id.books_textview_no_internet).setVisibility(View.VISIBLE);
	}

	/**
	 * Displays the "No books found" View and hides any other that could be visible (in
	 * this case it's only possible that the Progress Bar is visible).
	 */
	private void showNoBooksFoundView() {
		findViewById(R.id.books_progressbar).setVisibility(View.GONE);
		findViewById(R.id.books_textview_no_books_found).setVisibility(View.VISIBLE);
	}

	/**
	 * Performs the loading of the search results in a background thread using an
	 * {@link android.os.AsyncTask} through the {@link AsyncTaskLoader} implementation.
	 */
	private static class BooksLoader extends AsyncTaskLoader<ArrayList<Book>> {

		private String mSearchQuery;
		private ArrayList<Book> mBookArrayList;

		BooksLoader(Context context,
		            ArrayList<Book> bookArrayList) {
			super(context);
			mBookArrayList = bookArrayList;
		}

		/**
		 * <p>Loads the {@link BooksLoader#mBookArrayList} from the argument with {@link Book}
		 * objects according to the search results using methods from the {@link QueryUtils}
		 * class.</p>
		 * <p>Gets the user preferences by calling {@link QueryUtils#getPreferences(Context)}
		 * for use in subsequent processes. Builds the query URL by calling
		 * {@link QueryUtils#buildQueryUrl(String)}, then makes an HTTP request using
		 * {@link QueryUtils#makeHttpRequest(URL)}. Finally, the JSON response is parsed to the
		 * {@code mBookArrayList} that was passed as an argument to the
		 * {@link QueryUtils#parseJsonToArrayList(String, ArrayList)} method.</p>
		 *
		 * @return the {@code BooksLoader#mBookArrayList} when the loading completes, regardless
		 * of whether it's been successful.
		 */
		@Override
		public ArrayList<Book> loadInBackground() {
			mSearchQuery = searchQuery;
			QueryUtils.getPreferences(getContext());
			URL queryUrl = QueryUtils.buildQueryUrl(mSearchQuery);
			String jsonResponseString = QueryUtils.makeHttpRequest(queryUrl);
			mBookArrayList = QueryUtils.parseJsonToArrayList(jsonResponseString, mBookArrayList);
			return mBookArrayList;
		}

	}

}
