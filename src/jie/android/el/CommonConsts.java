package jie.android.el;

public interface CommonConsts {
	public interface FragmentArgument {
		public enum Action {
			NONE, PACKAGE_CHANGED, SERVICE_NOTIFICATION;
			
			public int getId() {
				return this.ordinal();
			}
		}
		
		public static final String	ACTION	=	"action";
	}
}
