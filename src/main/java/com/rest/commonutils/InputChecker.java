package com.rest.commonutils;

public abstract class InputChecker {
	//cualquier palabara de A-z sin espacios entre 5 y 10 caracteres con digitos
	public static boolean isValidUsername(String username) {
		if (username.length() < 5 || username.length() > 10)
			return false;
		
		char[] userChar = username.toCharArray();
		for (int i = 0; i < userChar.length; i++) {
			char c = userChar[i];
			if (!Character.isLowerCase(c) && !Character.isUpperCase(c) && !Character.isDigit(c))
				return false;
		}
		
		return true;
	}
	
	//entre 6 y 20 caracteres de A-z y digitos con simbolos permitidos: "_" " " "-"
	public static boolean isValidPassword(String password) {
		final char[] allowedSymbols = new char[] {'_', ' ', '-'};
		int lowers = 0;
		int caps = 0;
		int digits = 0;
		
		if (password.length() < 6 || password.length() > 20)
			return false;
			
		char[] passChar = password.toCharArray();
		for (int i = 0; i < passChar.length; i++) {
			char c = passChar[i];
			if (Character.isDigit(c))
				digits++;
			else if (Character.isLowerCase(c))
				lowers++;
			else if(Character.isUpperCase(c))
				caps++;
			else if (!contains(allowedSymbols, c))
			    return false;
		}
		
		if (lowers == 0 || caps == 0 || digits == 0)
			return false;
		
		return true;
	}
	
	/**
	 * Comprueba si el needle existe en el haystack
	 * 
	 * @param haystack
	 * @param needle
	 * @return - true si needle existe
	 */
	private static boolean contains(char[] haystack, char needle) {
		for (int i = 0; i < haystack.length; i++)
			if (haystack[i] == needle)
				return true;
		
		return false;
	}
}
