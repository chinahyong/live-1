package tv.live.bx.common;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * Created by Live on 2017/5/16.
 * 实体类的转化(主要通过反射)
 */

public class BeanUtil {
	/**
	 * map 转 Object
	 *
	 * @param data
	 * @param obj
	 * @return
	 * @throws IllegalAccessException
	 */
	public static Object mapToBean(Map<String, String> data, Object obj) throws IllegalAccessException {
		if (data == null || data.isEmpty()) {
			return null;
		}
		Field[] fields = obj.getClass().getFields();
		for (String key : data.keySet()) {
			for (Field field : fields) {
				int mod = field.getModifiers();
				if (Modifier.isStatic(mod) || Modifier.isFinal(mod)) {
					continue;
				}
				if (field.getName().equalsIgnoreCase(key)) {
					field.setAccessible(true);
					field.set(obj, data.get(key));
				}
			}
		}
		return obj;
	}
}
