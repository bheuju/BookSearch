package com.gfeo.booksearch;

import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Represents a book. Holds simple information such as the book title, author name and the year
 * it was published, as well as a link to a page with more information about the book.
 *
 * @author gabrielfeo
 */

class Book {

	/** A String holding the book title. */
	private final String mTitle;
	/** A String holding the book author name. */
	private final String mAuthor;
	/** A String holding the year the book was published. */
	private final String mPublishedYear;
	/** A Uri pointing to a thumbnail image resource. */
	private final Uri mThumbnailUri;
	/**
	 * A Uri pointing to an information page about the book, generally on Google Books or Google
	 * Play Books.
	 */
	private Uri mInfoPageUri;

	/**
	 * Sets the basic information about the book.
	 *
	 * @param title         A String holding the book title.
	 * @param author        A String holding the book author name.
	 * @param publishedYear A String holding the year the book was published.
	 * @param thumbnailUri  A String containing a link to a thumbnail image resource on the
	 *                      internet. The String will be parsed to a Uri using
	 *                      {@link Uri#parse(String)}.
	 * @param infoPageUri   A String of a link to an information page about the book,
	 *                         generally on Google Books or Google Play Books. The String will be
	 *                         parsed to a Uri using {@link Uri#parse(String)}.
	 */
	Book(String title,
	     String author,
	     String publishedYear,
	     String thumbnailUri,
	     String infoPageUri) {
		mTitle = title;
		mAuthor = author;
		mPublishedYear = publishedYear;

		mThumbnailUri = Uri.parse(thumbnailUri);

		/*If no info URL is available, the getInfoPageUri method should return null so that the
		 OnItemClicklistener will display a toast with an error message instead of sending an intent
		  to view the empty
		 URL*/
		if (!infoPageUri.isEmpty()) {
			mInfoPageUri = Uri.parse(infoPageUri);
		}
	}

	String getTitle() {
		return mTitle;
	}

	String getAuthor() {
		return mAuthor;
	}

	String getPublishedYear() {
		return mPublishedYear;
	}

	Uri getThumbnailUri() {
		return mThumbnailUri;
	}

	Uri getInfoPageUri() {
		return mInfoPageUri;
	}

}
