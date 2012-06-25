package com.dvuckovic.busplus;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

/**
 * Provide List Preference with icons in front of entries
 * 
 * based on
 * http://stackoverflow.com/questions/4549746/custom-row-in-a-listpreference
 * 
 * @author atanarro
 * 
 */
public class IconListPreference extends ListPreference {

	private IconListPreferenceScreenAdapter iconListPreferenceAdapter = null;
	private Context mContext;
	private LayoutInflater mInflater;
	private CharSequence[] entries;
	private CharSequence[] entryValues;
	private int[] mEntryIcons = null;
	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;
	private String mKey;
	private int selectedEntry = -1;

	public IconListPreference(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public IconListPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		mContext = context;

		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.IconPreference, defStyle, 0);

		int entryIconsResId = a.getResourceId(
				R.styleable.IconPreference_entryIcons, -1);
		if (entryIconsResId != -1) {
			setEntryIcons(entryIconsResId);
		}
		mInflater = LayoutInflater.from(context);
		mKey = getKey();
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		editor = prefs.edit();

		a.recycle();

	}

	@Override
	public CharSequence getEntry() {
		if (selectedEntry != -1)
			return entries[selectedEntry];
		return super.getEntry().toString();
	}

	@Override
	public String getValue() {
		if (selectedEntry != -1)
			return entryValues[selectedEntry].toString();
		return super.getValue();
	}

	public void setEntryIcons(int[] entryIcons) {
		mEntryIcons = entryIcons;
	}

	public void setEntryIcons(int entryIconsResId) {
		TypedArray icons_array = mContext.getResources().obtainTypedArray(
				entryIconsResId);
		int[] icon_ids_array = new int[icons_array.length()];
		for (int i = 0; i < icons_array.length(); i++) {
			icon_ids_array[i] = icons_array.getResourceId(i, -1);
		}
		setEntryIcons(icon_ids_array);
	}

	@Override
	protected void onPrepareDialogBuilder(Builder builder) {
		super.onPrepareDialogBuilder(builder);

		entries = getEntries();
		entryValues = getEntryValues();

		if (entries.length != entryValues.length) {
			throw new IllegalStateException(
					"ListPreference requires an entries array and an entryValues array which are both the same length");
		}

		if (mEntryIcons != null && entries.length != mEntryIcons.length) {
			throw new IllegalStateException(
					"IconListPreference requires the icons entries array be the same length than entries or null");
		}

		iconListPreferenceAdapter = new IconListPreferenceScreenAdapter(
				mContext);

		if (mEntryIcons != null) {
			String selectedValue = prefs.getString(mKey, "");
			for (int i = 0; i < entryValues.length; i++) {
				if (selectedValue.compareTo((String) entryValues[i]) == 0) {
					selectedEntry = i;
					break;
				}
			}
			builder.setAdapter(iconListPreferenceAdapter, null);

		}
	}

	private class IconListPreferenceScreenAdapter extends BaseAdapter {
		public IconListPreferenceScreenAdapter(Context context) {

		}

		@Override
		public int getCount() {
			return entries.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			CustomHolder holder = null;
			final int p = position;
			row = mInflater.inflate(R.layout.iconpreference_row, parent, false);
			holder = new CustomHolder(row, position);

			row.setTag(holder);

			// row.setClickable(true);
			// row.setFocusable(true);
			// row.setFocusableInTouchMode(true);
			row.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					v.requestFocus();

					Dialog mDialog = getDialog();
					mDialog.dismiss();

					IconListPreference.this.callChangeListener(entryValues[p]);
					editor.putString(mKey, entryValues[p].toString());
					selectedEntry = p;
					editor.commit();

				}
			});

			return row;
		}

		class CustomHolder {
			private TextView text = null;
			private RadioButton rButton = null;

			CustomHolder(View row, int position) {
				text = (TextView) row
						.findViewById(R.id.image_list_view_row_text_view);
				text.setText(entries[position]);

				rButton = (RadioButton) row
						.findViewById(R.id.image_list_view_row_radio_button);
				rButton.setId(position);
				rButton.setClickable(false);
				rButton.setChecked(selectedEntry == position);

				if (mEntryIcons != null) {
					text.setText(" " + text.getText());
					text.setCompoundDrawablesWithIntrinsicBounds(
							mEntryIcons[position], 0, 0, 0);
				}
			}
		}
	}

}
