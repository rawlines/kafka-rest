package com.rest.utils;

import java.io.File;

public class ResourcesLoader {
	private ResourcesLoader() {}
	
	
	public static File load(String res) {
		return new File(new ResourcesLoader().getClass().getClassLoader().getResource(res).getFile());
	}
}
