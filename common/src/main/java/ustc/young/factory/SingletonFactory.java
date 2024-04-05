package ustc.young.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author YoungSheep
 * @description 单例对象的工厂类
 * @date 2024-03-29 13:12
 **/
public final class SingletonFactory {
    private static final Map<String, Object> OBJECT_MAP = new ConcurrentHashMap<>();

    private SingletonFactory(){

    }

    public static <T> T getInstance(Class<T> c){
        if(c==null){
            throw new IllegalArgumentException("实例的class不能为空");
        }
        String key = c.toString();
        if(OBJECT_MAP.containsKey(key)){
            return c.cast(OBJECT_MAP.get(key));
        }else {
            return c.cast(OBJECT_MAP.computeIfAbsent(key,k->{
                try {
                    return c.getDeclaredConstructor().newInstance();
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                         NoSuchMethodException e) {
                    throw new RuntimeException("单例工厂实例化失败",e);
                }
            }));
        }
    }

}
