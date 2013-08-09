package jie.android.el;

public interface CommonConsts {
	
	public interface AppArgument {
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
	}
	
	public interface Setting {
		public static final String PLAY_STOP_AFTER_CURRENT	= "play_stop_after_current";
		public static final String PLAY_RANDOM_ORDER	= "play_random_order";
		public static final String CONTENTY_MEDIUM_FONT_SIZE	= "content_medium_font_size";
		public static final String CONTENTY_LARGE_FONT_SIZE	= "content_large_font_size";
	}
}
