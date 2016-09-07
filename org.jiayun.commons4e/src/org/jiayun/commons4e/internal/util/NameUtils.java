package org.jiayun.commons4e.internal.util;

public abstract class NameUtils {
	
	
	public static String camelCase(String name,boolean upperFirst) {
		StringBuffer result = new StringBuffer();
		boolean mastUpperCase = false;
		for (int i = 0; i < name.length(); i++) {
			char  c = name.charAt(i);
			if(upperFirst && i==0){
				result.append(Character.toUpperCase(c));
				continue;
			}
			
			if(c=='_'){
				mastUpperCase =  true;
				continue;
			}
			
			if(mastUpperCase){
				result.append(Character.toUpperCase(c));
				mastUpperCase = false;
			}else{
				result.append((c));
			}
		}
		return result.toString();
	}
	
	
}
