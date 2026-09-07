package com.app.rbac.exception;

public class DuplicateResourceException extends RuntimeException{
	
	public DuplicateResourceException(String msg) {
		super(msg);
	}

}
