package com.bixin.bixin.common;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;
import org.json.JSONObject;

/**
 * Created by Live on 2017/4/25.
 */

public class JacksonUtil {
	private volatile static ObjectMapper objectMapper;

	public static <T> T readValue(JSONObject jsonStr, Class<T> valueType) throws Exception {
		return readValue(jsonStr.toString(), valueType);
	}

	/**
	 * 使用泛型方法，把json字符串转换为相应的JavaBean对象。
	 * (1)转换为普通JavaBean：readValue(json,Student.class)
	 * (2)转换为List,如List<Student>,将第二个参数传递为Student
	 * [].class.然后使用Arrays.asList();方法把得到的数组转换为特定类型的List
	 *
	 * @param jsonStr
	 * @param valueType
	 * @return
	 */
	public static <T> T readValue(String jsonStr, Class<T> valueType) throws Exception {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
		}
		objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return objectMapper.readValue(jsonStr, valueType);
	}

	/**
	 * 获取泛型的Collection Type
	 *
	 * @param jsonStr         json字符串
	 * @param collectionClass 泛型的Collection
	 * @param elementClasses  元素类型
	 */
	public static <T> T readValue(String jsonStr, Class<?> collectionClass, Class<?>... elementClasses) throws Exception {
		ObjectMapper mapper = new ObjectMapper();

		JavaType javaType = mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);

		return mapper.readValue(jsonStr, javaType);

	}

	/**
	 * json数组转List
	 *
	 * @param jsonStr
	 * @param valueTypeRef
	 * @return
	 */
	public static <T> T readValue(String jsonStr, TypeReference<T> valueTypeRef) throws Exception {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
		}
		objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return objectMapper.readValue(jsonStr, valueTypeRef);
	}

	/**
	 * 把JavaBean转换为json字符串
	 *
	 * @param object
	 * @return
	 */
	public static String toJSon(Object object) {
		if (objectMapper == null) {
			objectMapper = new ObjectMapper();
		}
		try {
			return objectMapper.writeValueAsString(object);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
