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
		public static final String PLAY_CONTINUALLY = "play_continually";
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

		public static final String ABOUT_DIALOG_SHOWN = "about_dialog_shown";
	}

	public interface UIMsg {
		public static final int SERVICE_NOTIFICATION = 1;
		// public static final int SERVICE_AUDIOPLAYING = 2;
		public static final int SERVICE_PACKAGE_READY = 3;
		public static final int SERVICE_AUDIO_ACTION = 4;
		public static final int SERVICE_UPDATE_AUDIO = 5;

		public static final int UI_CREATED = 100;
		public static final int UI_PACKAGE_CHANGED = 101;
		public static final int UI_LOAD_BUNDLEDPACKAGE = 102;
		public static final int UI_HIDE_TITLE = 103;
	}

	public enum ServiceState {
		READY, UNZIP, ERROR;

		public int getId() {
			return this.ordinal();
		}
	}

	public enum NotificationType {
		PLAY, WARNING, IMPORT;

		public int getId() {
			return this.ordinal();
		}

		public static NotificationType getType(int id) {

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

	public enum UpdateAudioType {
		STATE_CHANGED, AUDIO_IS_SET, AUDIO_CHANGED_OPEN, AUDIO_CHANGED_CLOSE;

		public int getId() {
			return ordinal();
		}
	}

	public enum UpdateUIType {
		LIST_WINDOW_CREATED, AUDIO_WINDOW_SHOW, AUDIO_WINDOW_CLOSE, HIDE_TITLE;

		public int getId() {
			return ordinal();
		}
	}

	public interface AudioNavigateData {
		public static final int DISABLE = 0;
		public static final int SLOWDIALOG = 1;
		public static final int EXPLANATION = 2;
		public static final int FASTDIALOG = 4;
	}

	public enum DownloadCompletedType {
		PACKAGE, CHECK_NEW, LATEST_VERSION;
		public int getId() {
			return this.ordinal();
		}
	}
	
	public interface BroadcastAction {
		public static final String ACTION_DOWNLOAD_COMPLETED = "jie.android.el.action.download_completed"; 
		
		public static final String ACTION_SERVICE_INIT = "jie.android.el.action.service_init";
		public static final String ACTION_SERVICE_BINDED = "jie.android.el.action.service_binded";
		public static final String ACTION_SERVICE_END = "jie.android.el.action.service_end";

		public static final String ACTION_UPDATE_AUDIO = "jie.android.el.action.update_audio";
		public static final String ACTION_UPDATE_AUDIO_PLAYING = "jie.android.el.action.update_audio_playing";
		public static final String ACTION_UPDATE_UI = "jie.android.el.action.update_ui";

		public static final String DATA_TYPE = "jie.android.el.data.type";
		public static final String DATA_TITLE = "jie.android.el.data.title";
		public static final String DATA_TEXT = "jie.android.el.data.text";
		public static final String DATA_ID = "jie.android.el.data.id";
		public static final String DATA_STATE = "jie.android.el.data.state";
		public static final String DATA_NAVIGATE = "jie.android.el.data.navigate";
		public static final String DATA_DURATION = "jie.android.el.data.duration";
		public static final String DATA_POSITION = "jie.android.el.data.position";
	}

	public interface AudioAction extends BroadcastAction {
		public static final String ACTION_AUDIO = "jie.android.el.action.audio";
		public static final String ACTION_AUDIO_SET = "jie.android.el.action.audio_set";
		public static final String ACTION_AUDIO_PREV = "jie.android.el.action.audio_prev";
		public static final String ACTION_AUDIO_NEXT = "jie.android.el.action.audio_next";
		public static final String ACTION_AUDIO_PLAY = "jie.android.el.action.audio_play";
		public static final String ACTION_AUDIO_FORCE_PAUSE = "jie.android.el.action.audio_force_pause";
		public static final String ACTION_AUDIO_STOP = "jie.android.el.action.audio_stop";
		public static final String ACTION_AUDIO_SEEK = "jie.android.el.action.audio_seek";
		public static final String ACTION_AUDIO_NAVIGATE = "jie.android.el.action.audio_navigate";
		public static final String ACTION_AUDIO_NAVIGATE_SLOWDIALOG = "jie.android.el.action.audio_navigate_slowdialog";
		public static final String ACTION_AUDIO_NAVIGATE_EXPLANATION = "jie.android.el.action.audio_navigate_explanation";
		public static final String ACTION_AUDIO_NAVIGATE_FASTDIALOG = "jie.android.el.action.audio_navigate_fastdialog";
		public static final String ACTION_AUDIO_QUERY = "jie.android.el.action.audio_query";
//		public static final String ACTION_AUDIO_CHECK = "jie.android.el.action.audio_check";
	}

	public interface NotificationAction extends BroadcastAction {
		public static final String ACTION_SHOW = "jie.android.el.action.notification_show";
		public static final String ACTION_REMOVE = "jie.android.el.action.notification_remove";
	}

	public interface WidgetAction extends BroadcastAction {
		public static final String ACTION_NAVIGATE = "jie.android.el.action.widget_navigate";
		public static final String ACTION_RANDOMMODE = "jie.android.el.action.widget_randmommode";
		public static final String ACTION_STARTACTIVITY = "jie.android.el.action.widget_startactivity";
	}

	public interface ListItemFlag {
		public static final int LAST_PLAY = 1;
	}

	public enum PlayState {
		INVALID, NONE, PREPARED, PLAY, /* PLAYING, */PAUSED, STOP, COMPLETED, ERROR;

		public int getId() {
			return this.ordinal();
		}
	}

	public interface DownloadRequest {
		public static final String SKYDRIVE_AUTHORIZATION_KEY = "3BAF6CAC74F7919";
		public static final String CHECK_NEW_PACKAGES = "checknewpackages";
		public static final String DOWNLOAD_LATEST_VERSION = "downloadlatestversion";
	}
}
