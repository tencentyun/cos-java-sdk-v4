package com.qcloud.cos.meta;

public enum FileAuthority {
	INVALID("eInvalid"), 
	WPRIVATE("eWRPrivate"), 
	WPRIVATERPUBLIC("eWPrivateRPublic");

	private String authority;

	private FileAuthority(String authority) {
		this.authority = authority;
	}

	@Override
	public String toString() {
		return this.authority;
	}
}