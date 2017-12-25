package com.bixin.bixin.library.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Live on 2017/5/19.
 */

public class ClassReflection {
	/**
	 * 将from bean的值赋值给 to（相同属性的值）
	 *
	 * @param from
	 * @param to
	 */
	public static Object reflectionAttr(Object from, Object to) throws ClassNotFoundException {
		Class clazzFrom = Class.forName(from.getClass().getName());
		Class clazzTo = Class.forName(to.getClass().getName());
		// 获取属性列表
		Field[] fromFields = clazzFrom.getFields();
		Field[] toFields = clazzTo.getFields();

		for (Field fromField :
				fromFields) {
			String fromName = fromField.getName();
			for (Field toField : toFields) {
				if (fromName.equals(toField.getName())) {
					Object obj = invokeGetMethod(clazzFrom, fromName);
					Object[] objs = {obj};
					invokeSetMethod(clazzTo, toField, objs);
				}
			}
		}
		return to;
	}

	/**
	 * 获取类中的对应属性的get方法
	 */
	public static Object invokeGetMethod(Class clazz, String fieldName) {
		String methodName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		try {
			Method method = Class.forName(clazz.getClass().getName()).getDeclaredMethod("get" + methodName);
			return method.invoke(clazz);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取类中对应属性的set方法
	 */

	public static void invokeSetMethod(Class clazz, Field field, Object[] objs) {
		String fieldName = field.getName();
		String methodName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		try {
			Method method = Class.forName(clazz.getClass().getName()).getDeclaredMethod("set" + methodName, field.getType());
			method.invoke(clazz, objs);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
