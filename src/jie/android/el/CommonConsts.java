package jie.android.el;

public interface CommonConsts {

	public interface AppArgument {
		public static final String NAME = "el";
		public static final String PATH_ROOT = "/jie/";
		public static final String PATH_EL = "/jie/el/";
		public static final String PATH_CACHE = "/jie/cache/";
	}

	public interface FragmentArgument {
		public enum Action {
			NONE, PACKAGE_CHANGED, SERVICE_NOTIFICATION, QUERY, PLAY;

			public int getId() {
				return this.ordinal();
			}
		}

		public static final String ACTION = "action";
		public static final String INDEX = "index";
		public static final String TEXT = "text";
		public static final String STATE = "state";
		public static final String DURATION = "duration";
		public static final String POSITION = "position";
	}

	public interface Setting {
		public static final String PLAY_STOP_AFTER_CURRENT = "play_stop_after_current";
		public static final String PLAY_RANDOM_ORDER = "play_random_order";
		public static final String PLAY_AUTO_PLAY = "play_auto_play";
		public static final String CONTENT_MEDIUM_FONT_SIZE = "content_medium_font_size";
		public static final String CONTENT_LARGE_FONT_SIZE = "content_large_font_size";
		public static final String CONTENT_HIDE_TITLE = "content_hide_title";

		public static final String DICTIONARY_LIST_NOT_EXTENSION = "dictionary_list_not_extension";

		public static final String DICTIONARY_LIST_MAXPERPAGE = "dictionary_list_maxperpage";

		public static final String MEMORY_MODE_RANDOM = "memory_mode_random";
		public static final String MEMORY_NEED_CHECK = "memory_need_check";
		public static final String MEMORY_SHOW_RESULT = "memory_show_result";
		public static final String MEMORY_AUTO_DELETE = "memory_auto_delete";
		public static final String MEMORY_AUTO_SPEAK = "memory_auto_speak";
		public static final String VOCAB_SORT_MODE = "vocab_sort_mode";
		public static final String VOCAB_GROUP_MODE = "vocab_group_mode";
	}

	public interface UIMsg {
		public static final int SERVICE_NOTIFICATION = 1;
		// public static final int SERVICE_AUDIOPLAYING = 2;
		public static final int SERVICE_PACKAGE_READY = 3;

		public static final int UI_CREATED = 100;
		public static final int UI_PACKAGE_CHANGED = 101;
		public static final int UI_LOAD_BUNDLEDPACKAGE = 102;
	}

	public enum ServiceState {
		READY, UNZIP, ERROR, PLAYING;

		public int getId() {
			return this.ordinal();
		}
	}

	public enum UIState {
		START, STOP;

		public int getId() {
			return this.ordinal();
		}
	}

	public enum NotificationType {
		PLAY, WARNING, IMPORT;

		public int getId() {
			return this.ordinal();
		}

		public static NotificationType getLevel(int id) {

			if (id == PLAY.getId()) {
				return PLAY;
			} else if (id == WARNING.getId()) {
				return WARNING;
			} else if (id == IMPORT.getId()) {
				return IMPORT;
			} else {
				return null;
			}
		}
	}

	public interface NotificationAction {
		public static final String ACTION_SHOW = "jie.android.el.action.show";
		public static final String ACTION_REMOVE = "jie.android.el.action.remove";

		public static final String DATA_TYPE = "jie.android.el.data.type";
		public static final String DATA_TITLE = "jie.android.el.data.title";
		public static final String DATA_TEXT = "jie.android.el.data.text";
		public static final String DATA_ID = "jie.android.el.data.id";
	}

	public interface ListItemFlag {
		public static final int LAST_PLAY = 1;
	}

	public enum PlayState {
		INVALID, NONE, PREPARED, PLAYING, PAUSED, COMPLETED, ERROR;

		public int getId() {
			return this.ordinal();
		}

		public static PlayState getState(int id) {
			if (id == PLAYING.getId()) {
				return PLAYING;
			} else if (id == PAUSED.getId()) {
				return PAUSED;
			} else if (id == NONE.getId()) {
				return NONE;
			} else if (id == PREPARED.getId()) {
				return PREPARED;
			} else if (id == ERROR.getId()) {
				return ERROR;
			} else if (id == COMPLETED.getId()) {
				return COMPLETED;
			} else if (id == INVALID.getId()) {
				return INVALID;
			} else {
				return null;
			}
		}
	}
}
