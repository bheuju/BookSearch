package com.gfeo.booksearch;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Utility class for querying the <a
 * href="https://developers.google.com/books/docs/overview">Google Books API</a>.
 *
 * @author gabrielfeo
 */

class QueryUtils {

	private final static String LOG_TAG = QueryUtils.class.getSimpleName();
	/** An integer to hold the number of search results */
	static int numberOfResults;
	/**
	 * A Integer object to hold the maxResults preference value
	 *
	 * @see QueryUtils#getPreferences(Context)
	 * @see QueryUtils#buildQueryUrl(String)
	 */
	static Integer maxResultsValue;
	/**
	 * A String to hold the maxResults preference key
	 *
	 * @see QueryUtils#getPreferences(Context)
	 * @see QueryUtils#buildQueryUrl(String)
	 */
	private static String maxResultsKey;
	/**
	 * A String to hold the orderBy preference key
	 *
	 * @see QueryUtils#getPreferences(Context)
	 * @see QueryUtils#buildQueryUrl(String)
	 */
	private static String orderByKey;
	/**
	 * A String to hold the orderBy preference value
	 *
	 * @see QueryUtils#getPreferences(Context)
	 * @see QueryUtils#buildQueryUrl(String)
	 */
	private static String orderByValue;
	/**
	 * A String to hold the langRestrict preference key
	 *
	 * @see QueryUtils#getPreferences(Context)
	 * @see QueryUtils#buildQueryUrl(String)
	 */
	private static String langRestrictKey;
	/**
	 * A String to hold the langRestrict preference value
	 *
	 * @see QueryUtils#getPreferences(Context)
	 * @see QueryUtils#buildQueryUrl(String)
	 */
	private static String langRestrictValue;

	/**
	 * <p>Builds the query URL using a {@link Uri.Builder} and the
	 * {@link QueryUtils#createUrl(String)} method.</p>
	 * <p>The {@code Uri.Builder} appends to the Uri as parameters the query inputted by the user
	 * (properly encoded) and the key and value of the {@code maxResults}, {@code orderBy} and
	 * {@code langRestrict} according to the user preferences. </p>
	 *
	 * @param searchQuery a String holding the search query inputted by the user in the
	 *                    {@link BooksActivity} Toolbar
	 *                    {@link android.support.v7.widget.SearchView}
	 * @return the built query {@link URL}
	 */
	static URL buildQueryUrl(String searchQuery) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.scheme("https")
		          .authority("www.googleapis.com")
		          .appendPath("books").appendPath("v1").appendPath("volumes")
		          .appendQueryParameter("q", searchQuery)
		          .appendQueryParameter(maxResultsKey, maxResultsValue.toString())
		          .appendQueryParameter(orderByKey, orderByValue);
		if (!langRestrictValue.equals("none")) {
			uriBuilder.appendQueryParameter(langRestrictKey, langRestrictValue);
		}
		return createUrl(uriBuilder.toString());
	}

	/**
	 * Creates a new URL object from the given String URL.
	 *
	 * @param stringUrl a String holding a URL address
	 * @return the created URL object
	 */
	private static URL createUrl(String stringUrl) {
		URL url = null;
		try {
			url = new URL(stringUrl);
		} catch (MalformedURLException e) {
			Log.e(LOG_TAG, "Error creating URL ", e);
		}
		return url;
	}

	/**
	 * Makes an HTTP request to the given URL and return a String as the response.
	 *
	 * @param url the URL for the HTTP request
	 * @return a String of the JSON HTTP response
	 */
	static String makeHttpRequest(URL url) {
		String jsonResponse = "";

		// If the URL is null, then return early.
		if (url == null) {
			return jsonResponse;
		}

		HttpURLConnection urlConnection = null;
		InputStream inputStream = null;
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setReadTimeout(10000);
			urlConnection.setConnectTimeout(15000);
			urlConnection.setRequestMethod("GET");
			urlConnection.connect();

			BooksActivity.httpResponseCode = urlConnection.getResponseCode();
			if (BooksActivity.httpResponseCode == 200) {
				inputStream = urlConnection.getInputStream();
				jsonResponse = readFromStream(inputStream);
			} else {
				Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, "Problem retrieving the JSON results.", e);
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					Log.e(LOG_TAG, "IOException thrown when closing the input stream", e);
				}
			}
		}
		return jsonResponse;
	}

	/**
	 * Converts the {@link InputStream} into a String which contains the
	 * whole JSON response from the server.
	 *
	 * @param inputStream the {@code InputStream} to be read
	 * @return the {@link BufferedReader} output from reading the
	 * {@code InputStream}
	 */
	private static String readFromStream(InputStream inputStream) throws IOException {
		StringBuilder output = new StringBuilder();
		if (inputStream != null) {
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset
					.forName("UTF-8"));
			BufferedReader reader = new BufferedReader(inputStreamReader);
			String line = reader.readLine();
			while (line != null) {
				output.append(line);
				line = reader.readLine();
			}
		}
		return output.toString();
	}

	/**
	 * <p>Parses the JSON response returned by the <a
	 * href="https://developers.google.com/books/docs/overview">Google
	 * Books API</a> to an {@link ArrayList} of {@link Book} objects.</p>
	 * <p>Gets the total number of search results and the array of results from the JSON. Then,
	 * a loop is started and iterated according to the number of results or the maxResults
	 * setting. The loop retrieves Strings from the JSON containing the book title, author(s)
	 * name(s), published year, one address to a thumbnail image and another for an information
	 * page, namely, the fields of a {@code Book} object. Finally, each retrieved value, empty or
	 * not, is used as an argument to the
	 * {@link Book#Book(String, String, String, String, String)} constructor.</p>
	 *
	 * @param jsonResponseString the JSON response to be parsed, in a String object
	 * @param bookArrayList      the {@code ArrayList<Book>} the JSON will
	 *                           be parsed to
	 * @return the fully populated {@code
	 * ArrayList<Book>}
	 */
	static ArrayList<Book> parseJsonToArrayList(String jsonResponseString,
	                                            ArrayList<Book> bookArrayList) {

		String bookTitle = "";
		String bookAuthor = "";
		String bookPublishedYear = "";
		String bookCoverThumbnailUriString = "";
		String bookInfoPageUriString = "";

		try {
			JSONObject jsonObject = new JSONObject(jsonResponseString);
			numberOfResults = jsonObject.getInt("totalItems");
			JSONArray itemsArray = jsonObject.getJSONArray("items");
			int maxIterations = (numberOfResults > maxResultsValue)
			                    ? maxResultsValue
			                    : numberOfResults;
			for (int i = 0; i < maxIterations; i++) {
				JSONObject volumeInfo = itemsArray.getJSONObject(i)
				                                  .getJSONObject("volumeInfo");

				//Get book title
				bookTitle = volumeInfo.getString("title");

				//Try getting the authors' names
				try {
					JSONArray authorsArray = volumeInfo.getJSONArray("authors");
					StringBuilder authorsStringBuilder = new StringBuilder();
					authorsStringBuilder.append(authorsArray.getString(0));
					for (int p = 1; p < authorsArray.length(); p++) {
						authorsStringBuilder.append(", ")
						                    .append(authorsArray.getString(p));
					}
					bookAuthor = authorsStringBuilder.toString();
				} catch (JSONException e) {
					checkForPermittedJsonException(e);
					bookAuthor = "Author name not available";
				}

				//Try getting the book published year
				try {
					bookPublishedYear = volumeInfo.getString("publishedDate")
					                              .substring(0, 4);
				} catch (JSONException e) {
					checkForPermittedJsonException(e);
					bookPublishedYear = "No date available";
				}

				//Try getting the book cover thumbnail URL
				try {
					bookCoverThumbnailUriString = volumeInfo.getJSONObject("imageLinks")
					                                        .getString("smallThumbnail");
				} catch (JSONException e) {
					checkForPermittedJsonException(e);
				}

				//Try getting the URL to the book info page URL
				try {
					bookInfoPageUriString = volumeInfo.getString("infoLink");
				} catch (JSONException e) {
					checkForPermittedJsonException(e);
				}

				//Add current book object
				bookArrayList.add(new Book(bookTitle,
				                           bookAuthor,
				                           bookPublishedYear,
				                           bookCoverThumbnailUriString,
				                           bookInfoPageUriString)
				                 );

			}
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage());
		}

		return bookArrayList;
	}

	/**
	 * Used in the first line in a catch block to check the exception argument message for a
	 * certain keyphrase. If the exception corresponds to the permitted "No value for [{@code
	 * key}]" {@link JSONException}, the exception will be disregarded and the caller method can
	 * resume execution. Otherwise, another exception will be thrown.
	 *
	 * @param e the {@code JSONException} to be checked
	 * @throws JSONException if the {@code JSONException} argument isn't of the allowed type
	 */
	private static void checkForPermittedJsonException(JSONException e) throws JSONException {
		String permittedExceptionKeyphrase = "No value for";
		if (!e.getMessage().contains(permittedExceptionKeyphrase)) {
			throw new JSONException(e.getMessage());
		}
	}

	/**
	 * <p>Sets the preference keys (that correspond to API query parameters) and their values by
	 * querying the {@link SharedPreferences}.</p>
	 * <p>All the preference keys and default values are defined in the {@code strings.xml} so
	 * that they never differ from the ones used in the {@code preferences.xml} file (which uses
	 * the same String resources).</p>
	 *
	 * @param context a {@link Context} for getting the preferences keys and default values in
	 *                the String resources from the {@code strings.xml} file
	 */
	static void getPreferences(Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

		//maxResults setting
		maxResultsKey = context.getString(R.string.preferences_maxresults_key);
		String maxResultsDefault = context.getString(R.string.preferences_maxresults_default);
		String maxResultsValueString = sharedPrefs.getString(maxResultsKey, maxResultsDefault);
		try {
			int mMaxResults = Integer.parseInt(maxResultsValueString);
			maxResultsValue = (mMaxResults >= 1 && mMaxResults <= 40)
			                  ? mMaxResults
			                  : Integer.parseInt(maxResultsDefault);
		} catch (NumberFormatException e) {
			Log.e(LOG_TAG, e.getMessage());
			maxResultsValue = Integer.parseInt(maxResultsDefault);
		}

		//orderBy setting
		orderByKey = context.getString(R.string.preferences_orderby_key);
		String orderByDefault = context.getString(R.string.preferences_orderby_default);
		orderByValue = sharedPrefs.getString(orderByKey, orderByDefault);

		//langRestrict setting
		langRestrictKey = context.getString(R.string.preferences_langrestrict_key);
		String langRestrictDefault = context.getString(R.string.preferences_langrestrict_default)
		                                    .toLowerCase();
		langRestrictValue = sharedPrefs.getString(langRestrictKey, langRestrictDefault);

	}

}
