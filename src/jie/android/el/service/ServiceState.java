package jie.android.el.service;

public enum ServiceState {
	READY, UNZIP, ERROR;
	
	public int getId() {
		return this.ordinal();
	}
}
