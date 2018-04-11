package com.gfeo.booksearch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * An Adapter of {@link Book} objects. Provides ready Views for ListViews. Uses the
 * <a href="https://bumptech.github.io/glide/">Glide</a> library to load images. Employs a
 * View holder ({@link ViewHolder}) pattern to avoid repetitive calling of the
 * {@link View#findViewById(int)} method.
 *
 * @author gabrielfeo
 */

class BookArrayAdapter extends ArrayAdapter<Book> {

	private Context mContext;

	BookArrayAdapter(Context context, ArrayList<Book> bookArrayList) {
		super(context, 0, bookArrayList);
		mContext = context;
	}

	/**
	 * Gets the {@link android.widget.ListView} item at the specified position and inflates it
	 * if it doesn't exist. Then, sets the text of the TextViews to the Strings provided by the
	 * accessor methods of the {@link Book} object at the current position; queries the
	 * {@link Book#getThumbnailUri()} method for an Uri to a thumbnail image and loads it, or a
	 * placeholder drawable if the method returns an empty Uri, into the layout's {@link ImageView}.
	 */
	@NonNull
	@Override
	public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			LayoutInflater layoutInflater = LayoutInflater.from(mContext);
			View container = layoutInflater.inflate(R.layout.container,
			                                        null,
			                                        false
			                                       );
			convertView = layoutInflater.inflate(R.layout.listitem_books,
			                                     (ViewGroup) container.findViewById(R.id.container)
			                                    );
			viewHolder = new ViewHolder();
			viewHolder.imageView =
					convertView.findViewById(R.id.books_listitem_imageview);
			viewHolder.textViewTitle =
					convertView.findViewById(R.id.books_listitem_textview_title);
			viewHolder.textViewAuthor =
					convertView.findViewById(R.id.books_listitem_textview_author);
			viewHolder.textViewYear =
					convertView.findViewById(R.id.books_listitem_textview_year);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final Book currentBook = getItem(position);

		if (currentBook == null) {
			return convertView;
		}

		if (!currentBook.getThumbnailUri().toString().isEmpty()){
		Glide.with(mContext)
		     .load(currentBook.getThumbnailUri())
		     .into(viewHolder.imageView);
		} else {
			Glide.with(mContext)
		         .load(R.drawable.ic_book_black_36dp)
		         .into(viewHolder.imageView);
			viewHolder.imageView.setImageAlpha(50);
		}
		viewHolder.textViewTitle.setText(currentBook.getTitle());
		viewHolder.textViewAuthor.setText(currentBook.getAuthor());
		String publishedYear = "(" + currentBook.getPublishedYear() + ")";
		viewHolder.textViewYear.setText(publishedYear);

		return convertView;
	}

	/**
	 * Holds View references in order to avoid calling the {@link View#findViewById(int)} method
	 * repetitively.
	 *
	 * @author gabrielfeo
	 */
	private class ViewHolder {
		ImageView imageView;
		TextView textViewTitle;
		TextView textViewAuthor;
		TextView textViewYear;
	}
}
