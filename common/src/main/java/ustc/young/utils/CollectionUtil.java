package ustc.young.utils;

import java.util.Collection;

/**
 * @author YoungSheep
 * @description 集合工具类
 * @date 2024-03-28 16:26
 **/
public class CollectionUtil {
    public static boolean isEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }
}
