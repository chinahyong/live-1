package com.bixin.bixin.ui.util;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by Live on 2017/4/5.
 */

public class BuildProperties {
	private Properties mProp;

	public BuildProperties() throws IOException {
		mProp = new Properties();
		mProp.load(new FileInputStream(new File(Environment.getRootDirectory(), "build.prop")));
	}

	public boolean containsKey(final Object key) {
		return mProp.containsKey(key);
	}

	public boolean containsValue(final Object value) {
		return mProp.containsValue(value);
	}

	public Set<Map.Entry<Object, Object>> entrySet() {
		return mProp.entrySet();
	}

	public String getProperty(final String name) {
		return mProp.getProperty(name);
	}

	public String getProperty(final String name, final String defaultValue) {
		return mProp.getProperty(name, defaultValue);
	}

	public boolean isEmpty() {
		return mProp.isEmpty();
	}

	public Enumeration<Object> keys() {
		return mProp.keys();
	}

	public Set<Object> keySet() {
		return mProp.keySet();
	}

	public int size() {
		return mProp.size();
	}

	public Collection<Object> values() {
		return mProp.values();
	}

	public static BuildProperties newInstance() throws IOException {
		return new BuildProperties();
	}
}
