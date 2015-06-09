package net.yura.domination.engine.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class PropertyManager implements Serializable {
	private static final long serialVersionUID = -1233000785199235965L;

	private Map<String, String> properties = new HashMap<String, String>();

	public void put(String key, String value) {
		properties.put(key, value);
	}

	int getIntProperty(String name, int defaultValue) {
		Object value = properties.get(name);
		if (value != null) {
			return Integer.parseInt(String.valueOf(value));
		}
		return defaultValue;
	}

	void setIntProperty(String name, int value, int defaultValue) {
		if (value == defaultValue) {
			properties.remove(name);
		} else {
			properties.put(name, String.valueOf(value));
		}
	}

	public int getCircleSize() {
		return getIntProperty("circle", 20);
	}

	public void setCircleSize(int a) {
		setIntProperty("circle", a, 20);
	}

	public int getVersion() {
		return getIntProperty("ver", 1);
	}

	public void setVersion(int newVersion) {
		setIntProperty("ver", newVersion, 1);
	}

	/**
	 * can return the name or null
	 */
	public String getMapName() {
		return (String) properties.get("name");
	}

	public void setMapName(String name) {
		if (name == null) {
			properties.remove("name");
		} else {
			properties.put("name", name);
		}
	}

	public boolean isEmpty() {
		return properties.isEmpty();
	}

	public void clear() {
		properties.clear();
	}

	public Set<Entry<String, String>> entrySet() {
		return properties.entrySet();
	}
}
