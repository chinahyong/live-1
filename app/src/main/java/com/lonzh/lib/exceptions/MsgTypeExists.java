package com.lonzh.lib.exceptions;

public class MsgTypeExists extends RuntimeException {
	private static final long serialVersionUID = 1409468298346776040L;
	private int miMsgType;

	public MsgTypeExists(int piMsgType) {
		miMsgType = piMsgType;
	}

	public String toString() {
		return "MsgType " + miMsgType + " already Registered";
	}
}
