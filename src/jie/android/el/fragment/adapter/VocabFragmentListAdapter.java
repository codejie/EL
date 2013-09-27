package jie.android.el.fragment.adapter;

import android.content.Context;
import android.widget.BaseAdapter;

public abstract class VocabFragmentListAdapter extends BaseAdapter {

	public static final int SORT_BY_ALPHA = 1;
	public static final int SORT_BY_SCORE = 2;

	public static final int GROUP_BY_NONE = 0;
	public static final int GROUP_BY_LESSON = 1;
	public static final int GROUP_BY_SCORE = 2;
	public static final int GROUP_BY_DATE = 3;

	public enum ItemType {
		WORD, LESSON, SCORE, DATE;
	}

	public interface OnAdapterListener {
		public void onLoadEnd(int count, int total, int maxPerPage);

		public void onItemRemoved(ItemType type, String text);
	}

	protected Context context;
	protected OnAdapterListener listener;

	protected boolean isEditable = false;
	protected int sortMode = SORT_BY_SCORE;
	protected int groupMode = GROUP_BY_NONE;

	public VocabFragmentListAdapter(Context context) {
		this.context = context;
	}

	public abstract void load(String filter);

	public abstract void refresh();

	public void setEditable(boolean editable) {
		isEditable = editable;
		this.notifyDataSetChanged();
	}

	public void toggleEditable() {
		setEditable(!isEditable);
	}

	public boolean isEditable() {
		return isEditable;
	}

	public void setOnAdapterListener(OnAdapterListener l) {
		listener = l;
	}

	public void setSortMode(int mode) {
		sortMode = mode;
	}

	public int getSortMode() {
		return sortMode;
	}

	public void setGroupMode(int mode) {
		groupMode = mode;
	}

	public int getGroupMode() {
		return groupMode;
	}

}
