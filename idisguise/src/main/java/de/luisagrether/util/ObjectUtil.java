package de.luisagrether.util;

import java.util.Collection;

/**
 * Some utility methods for objects.
 * 
 * @author LuisaGrether
 */
public class ObjectUtil {
	
	/**
	 * Tests whether an object equals any of the others.
	 * 
	 * @param object the object
	 * @param objects the other objects
	 * @return <code>true</code> if the object equals any of the others
	 */
	public static boolean equals(Object object, Object... objects) {
		for(Object obj : objects) {
			if(object.equals(obj)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Tests whether an object equals any of the others.
	 * 
	 * @param object the object
	 * @param objects the other objects
	 * @return <code>true</code> if the object equals any of the others
	 */
	public static boolean equals(Object object, Collection<? extends Object> objects) {
		for(Object obj : objects) {
			if(object.equals(obj)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Tests whether an object is an instance of any of the given classes.
	 * 
	 * @param object the object to test
	 * @param classes the classes to test
	 * @return <code>true</code> if the object is an instance of at least one of the given classes
	 */
	public static boolean instanceOf(Object object, Class<?>... classes) {
		for(Class<?> clazz : classes) {
			if(clazz.isInstance(object)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Tests whether an object is an instance of any of the given classes.
	 * 
	 * @param object the object to test
	 * @param classes the classes to test
	 * @return <code>true</code> if the object is an instance of at least one of the given classes
	 */
	public static boolean instanceOf(Object object, Collection<Class<?>> classes) {
		for(Class<?> clazz : classes) {
			if(clazz.isInstance(object)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Tests whether any of the given objects is an instance of the given class.
	 * 
	 * @param clazz the class to test
	 * @param objects the objects to test
	 * @return <code>true</code> if at least one of the given objects is an instance of the given class
	 */
	public static boolean instanceOf(Class<?> clazz, Object... objects) {
		for(Object obj : objects) {
			if(clazz.isInstance(obj)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Tests whether any of the given objects is an instance of the given class.
	 * 
	 * @param clazz the class to test
	 * @param objects the objects to test
	 * @return <code>true</code> if at least one of the given objects is an instance of the given class
	 */
	public static boolean instanceOf(Class<?> clazz, Collection<? extends Object> objects) {
		for(Object obj : objects) {
			if(clazz.isInstance(obj)) {
				return true;
			}
		}
		return false;
	}
	
}
