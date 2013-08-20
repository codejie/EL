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
			NONE, PACKAGE_CHANGED, SERVICE_NOTIFICATION;
			
			public int getId() {
				return this.ordinal();
			}
		}
		
		public static final String	ACTION	=	"action";
		public static final String INDEX	=	"index";
	}
	
	public interface Setting {
		public static final String PLAY_STOP_AFTER_CURRENT	= "play_stop_after_current";
		public static final String PLAY_RANDOM_ORDER	= "play_random_order";
		public static final String PLAY_DONT_AUTO_PLAY	= "play_dont_auto_play";
		public static final String CONTENTY_MEDIUM_FONT_SIZE	= "content_medium_font_size";
		public static final String CONTENTY_LARGE_FONT_SIZE	= "content_large_font_size";
		public static final String DICTIONARY_LIST_MAXPERPAGE	= "dictionary_list_maxperpage";
	}
	
	public interface UIMsg {
		public static final int SERVICE_NOTIFICATION	=	1;
		public static final int SERVICE_AUDIOPLAYING	=	2;
		public static final int SERVICE_PACKAGE_READY	=	3;
		
		public static final int UI_CREATED = 100;
		public static final int UI_PACKAGE_CHANGED = 101;
		public static final int UI_LOAD_BUNDLEDPACKAGE = 102;		
	}
	
	public enum ServiceState {
		READY, UNZIP, ERROR;
	
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
		public static final String ACTION_SHOW		= "jie.android.el.action.show";
		public static final String ACTION_REMOVE	= "jie.android.el.action.remove";
		
		public static final String DATA_TYPE		= "jie.android.el.data.type";
		public static final String DATA_TITLE		= "jie.android.el.data.title";
		public static final String DATA_TEXT		= "jie.android.el.data.text";
		public static final String DATA_ID			= "jie.android.el.data.id";
	}
	
	public interface ListItemFlag {
		public static final int LAST_PLAY	= 1;
	}
}
