package com.lonzh.lib.network;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class JSONParser {
	public static List<String> parseList(String psJson) throws JSONException {
		List<String> result = new ArrayList<>();
		JSONArray loJson = new JSONArray(psJson);
		for (int i = 0; i < loJson.length(); i++) {
			result.add(String.valueOf(loJson.get(i)));
		}
		return result;
	}

	public static Map<String, String> parseOne(String psJson) throws JSONException {
		JSONObject loJson = new JSONObject(psJson);
		HashMap<String, String> lmResult = new HashMap<String, String>();
		@SuppressWarnings("unchecked")
		Iterator<String> loIterator = loJson.keys();
		while (loIterator.hasNext()) {
			String lsKey = loIterator.next();
			lmResult.put(lsKey, loJson.getString(lsKey));
		}
		return lmResult;
	}

	public static Map<String, String> parseOne(JSONObject poJson) throws JSONException {
		HashMap<String, String> lmResult = new HashMap<>();
		@SuppressWarnings("unchecked")
		Iterator<String> loIterator = poJson.keys();
		while (loIterator.hasNext()) {
			String lsKey = loIterator.next();
			lmResult.put(lsKey, poJson.getString(lsKey));
		}
		return lmResult;
	}

	public static List<Map<String, String>> parseMulti(String psJson) throws JSONException {
		ArrayList<Map<String, String>> llResult = new ArrayList<Map<String, String>>();
		JSONArray loJsonArray = new JSONArray(psJson);
		for (int i = 0; i < loJsonArray.length(); i++)
			llResult.add(parseOne(loJsonArray.getJSONObject(i)));
		return llResult;
	}

	public static List<Map<String, String>> parseMulti(JSONArray poJsonArray) throws JSONException {
		ArrayList<Map<String, String>> llResult = new ArrayList<Map<String, String>>();
		for (int i = 0; i < poJsonArray.length(); i++)
			llResult.add(parseOne(poJsonArray.getJSONObject(i)));
		return llResult;
	}

	public static Map<String, Object> parseSingleInSingle(String psJson, String[] paArrKeys) throws JSONException {
		return parseSingleInSingle(new JSONObject(psJson), paArrKeys);
	}

	public static Map<String, Object> parseSingleInSingle(JSONObject poJsonObj, String[] paArrKeys)
			throws JSONException {
		Map<String, Object> lmResult = new HashMap<String, Object>();
		@SuppressWarnings("unchecked")
		Iterator<String> loIterator = poJsonObj.keys();
		while (loIterator.hasNext()) {
			String lsKey = loIterator.next();
			if (Arrays.asList(paArrKeys).contains(lsKey))
				lmResult.put(lsKey, parseOne(poJsonObj.getJSONObject(lsKey)));
			else
				lmResult.put(lsKey, poJsonObj.getString(lsKey));
		}
		return lmResult;
	}

	// 解析Map中含有List
	public static Map<String, Object> parseMultiInSingle(String psJson, String[] paArrKeys) throws JSONException {
		JSONObject loJson = new JSONObject(psJson);
		return parseMultiInSingle(loJson, paArrKeys);
	}

	// 解析Map中含有List
	public static Map<String, Object> parseMultiInSingle(JSONObject poJson, String[] paArrKeys) throws JSONException {
		Map<String, Object> lmResult = new HashMap<String, Object>();
		@SuppressWarnings("unchecked")
		Iterator<String> loIterator = poJson.keys();
		while (loIterator.hasNext()) {
			String lsKey = loIterator.next();
			if (Arrays.asList(paArrKeys).contains(lsKey))
				lmResult.put(lsKey, parseMulti(poJson.getJSONArray(lsKey)));
			else
				lmResult.put(lsKey, poJson.getString(lsKey));
		}
		return lmResult;
	}

	// 解析List中含有List
	public static List<Map<String, Object>> parseMultiInMulti(String psJson, String[] paArrKeys) throws JSONException {
		JSONArray loJsonArray = new JSONArray(psJson);
		return parseMultiInMulti(loJsonArray, paArrKeys);
	}

	// 解析List中含有List
	public static List<Map<String, Object>> parseMultiInMulti(JSONArray paJson, String[] paArrKeys)
			throws JSONException {
		List<Map<String, Object>> llResult = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < paJson.length(); i++)
			llResult.add(parseMultiInSingle(paJson.getJSONObject(i), paArrKeys));
		return llResult;
	}

	public static List<Map<String, Object>> parseSingleInMulti(String psJson, String[] paSingleKeys)
			throws JSONException {
		return parseSingleInMulti(new JSONArray(psJson), paSingleKeys);
	}

	public static List<Map<String, Object>> parseSingleInMulti(JSONArray paJsonArr, String[] paSingleKeys)
			throws JSONException {
		List<Map<String, Object>> llResult = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < paJsonArr.length(); i++)
			llResult.add(parseSingleInSingle(paJsonArr.getJSONObject(i), paSingleKeys));
		return llResult;
	}

	/**
	 * 两级JsonArray，解析排行数据
	 */
	public static Map<String, Object> parseMultiInMulti(JSONObject paJson, String[] levelOneKeys, String[] levelTwoKeys)
			throws JSONException {
		Map<String, Object> llResult = new HashMap<String, Object>();

		Iterator<String> loIterator = paJson.keys();
		while (loIterator.hasNext()) {
			String lsKey = loIterator.next();
			if (Arrays.asList(levelOneKeys).contains(lsKey))
				llResult.put(lsKey, parseMultiInSingle(paJson.getJSONObject(lsKey), levelTwoKeys));
			else
				llResult.put(lsKey, paJson.getString(lsKey));
		}

		return llResult;
	}

	/************************** 分享接口获取信息 *******************************/
	/**
	 * 将指定的json数据转成 HashMap<String, Object>对象
	 */
	public HashMap<String, Object> fromJson(String jsonStr) {
		try {
			if (jsonStr.startsWith("[") && jsonStr.endsWith("]")) {
				jsonStr = "{\"fakelist\":" + jsonStr + "}";
			}

			JSONObject json = new JSONObject(jsonStr);
			return fromJson(json);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return new HashMap<String, Object>();
	}

	private HashMap<String, Object> fromJson(JSONObject json) throws JSONException {
		HashMap<String, Object> map = new HashMap<String, Object>();
		@SuppressWarnings("unchecked")
		Iterator<String> iKey = json.keys();
		while (iKey.hasNext()) {
			String key = iKey.next();
			Object value = json.opt(key);
			if (JSONObject.NULL.equals(value)) {
				value = null;
			}
			if (value != null) {
				if (value instanceof JSONObject) {
					value = fromJson((JSONObject) value);
				} else if (value instanceof JSONArray) {
					value = fromJson((JSONArray) value);
				}
				map.put(key, value);
			}
		}
		return map;
	}

	private ArrayList<Object> fromJson(JSONArray array) throws JSONException {
		ArrayList<Object> list = new ArrayList<Object>();
		for (int i = 0, size = array.length(); i < size; i++) {
			Object value = array.opt(i);
			if (value instanceof JSONObject) {
				value = fromJson((JSONObject) value);
			} else if (value instanceof JSONArray) {
				value = fromJson((JSONArray) value);
			}
			list.add(value);
		}
		return list;
	}

	/**
	 * 将指定的HashMap<String, Object>对象转成json数据
	 */
	public static String fromHashMap(HashMap<String, Object> map) {
		try {
			return getJSONObject(map).toString();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return "";
	}


	@SuppressWarnings("unchecked")
	private static JSONObject getJSONObject(HashMap<String, Object> map) throws JSONException {
		JSONObject json = new JSONObject();
		for (Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof HashMap<?, ?>) {
				value = getJSONObject((HashMap<String, Object>) value);
			} else if (value instanceof ArrayList<?>) {
				value = getJSONArray((ArrayList<Object>) value);
			}
			json.put(entry.getKey(), value);
		}
		return json;
	}

	@SuppressWarnings("unchecked")
	private static JSONArray getJSONArray(ArrayList<Object> list) throws JSONException {
		JSONArray array = new JSONArray();
		for (Object value : list) {
			if (value instanceof HashMap<?, ?>) {
				value = getJSONObject((HashMap<String, Object>) value);
			} else if (value instanceof ArrayList<?>) {
				value = getJSONArray((ArrayList<Object>) value);
			}
			array.put(value);
		}
		return array;
	}

	/**
	 * 格式化一个json串
	 */
	public String format(String jsonStr) {
		try {
			return format("", fromJson(jsonStr));
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return "";
	}

	@SuppressWarnings("unchecked")
	private String format(String sepStr, HashMap<String, Object> map) {
		StringBuffer sb = new StringBuffer();
		sb.append("{\n");
		String mySepStr = sepStr + "\t";
		int i = 0;
		for (Entry<String, Object> entry : map.entrySet()) {
			if (i > 0) {
				sb.append(",\n");
			}
			sb.append(mySepStr).append('\"').append(entry.getKey()).append("\":");
			Object value = entry.getValue();
			if (value instanceof HashMap<?, ?>) {
				sb.append(format(mySepStr, (HashMap<String, Object>) value));
			} else if (value instanceof ArrayList<?>) {
				sb.append(format(mySepStr, (ArrayList<Object>) value));
			} else if (value instanceof String) {
				sb.append('\"').append(value).append('\"');
			} else {
				sb.append(value);
			}
			i++;
		}
		sb.append('\n').append(sepStr).append('}');
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	private String format(String sepStr, ArrayList<Object> list) {
		StringBuffer sb = new StringBuffer();
		sb.append("[\n");
		String mySepStr = sepStr + "\t";
		int i = 0;
		for (Object value : list) {
			if (i > 0) {
				sb.append(",\n");
			}
			sb.append(mySepStr);
			if (value instanceof HashMap<?, ?>) {
				sb.append(format(mySepStr, (HashMap<String, Object>) value));
			} else if (value instanceof ArrayList<?>) {
				sb.append(format(mySepStr, (ArrayList<Object>) value));
			} else if (value instanceof String) {
				sb.append('\"').append(value).append('\"');
			} else {
				sb.append(value);
			}
			i++;
		}
		sb.append('\n').append(sepStr).append(']');
		return sb.toString();
	}

}
