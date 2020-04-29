package com.rest.utils;

public class ResourcesLoader {
	private ResourcesLoader() {}
	
	
	public static String load(String res) {
		return new ResourcesLoader().getClass().getClassLoader().getResource(res).getPath();
	}
}
