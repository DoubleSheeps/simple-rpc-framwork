package ustc.young.extension;

/**
 * @author YoungSheep
 * @description 实现延迟初始化
 * @date 2024-03-28 13:09
 **/
public class Holder<T> {
    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
