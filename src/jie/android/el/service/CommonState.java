package jie.android.el.service;

public interface CommonState {
	
	public enum Service {
		READY, UNZIP, ERROR;
	
		public int getId() {
			return this.ordinal();
		}
	}
	
	public enum UI {
		START, STOP;
		
		public int getId() {
			return this.ordinal();
		}
	}
}
