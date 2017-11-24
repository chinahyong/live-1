package tv.live.bx.library.util;

import java.util.HashMap;
import java.util.Map;

/**
 * K与V一对一关系<br>
 * 新添加的K，V，如果V已经对应一个K1，则清除K1对应的V<br>
 * 确保V只属于一个K<br>
 * Created by duminghui on 14-8-12.
 */
public class SingleKVMap<K, V> {
    private Map<K, V> mainMap = new HashMap<K, V>();
    private Map<V, K> subMap = new HashMap<V, K>();

    public void put(K key, V value) {
        mainMap.remove(subMap.get(value));
        mainMap.put(key, value);
        subMap.put(value, key);
    }

    public V get(K key) {
        return mainMap.get(key);
    }

    public void remove(K key) {
        mainMap.remove(key);
    }

    public void removeValue(V value){
        mainMap.remove(subMap.get(value));
        subMap.remove(value);
    }
}
