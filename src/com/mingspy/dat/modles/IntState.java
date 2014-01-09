package com.mingspy.dat.modles;

public class IntState{
	private int state;

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
	
	public IntState(int state){
		this.state = state;
	}
	
	public IntState(){
		
	}
	
	public void incOne(){
		state ++;
	}

}
