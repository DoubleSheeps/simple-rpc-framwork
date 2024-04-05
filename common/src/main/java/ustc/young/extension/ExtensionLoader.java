package ustc.young.extension;

import lombok.extern.slf4j.Slf4j;
import ustc.young.utils.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;


/**
 * @author YoungSheep
 * @description 扩展类的加载器，扩展加载方法见 Dubbo 文档 <a href="https://cn.dubbo.apache.org/zh-cn/overview/mannual/java-sdk/reference-manual/spi/description/dubbo-spi/">...</a>
 * @date 2024-03-28 13:10
 **/
@Slf4j
public final class ExtensionLoader<T> {
    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";
    //使用ConcurrentHashMap缓存扩展类的加载器
    private static final Map<Class<?>,ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> EXTENSION_INSTANCES= new ConcurrentHashMap<>();

    private final Class<?> type;

    //扩展类实例缓存
    private final Map<String,Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    //单例模式的扩展类缓存
    private final Holder<Map<String,Class<?>>> cachedClasses = new Holder<>();

    /**
     * 私有构造方法实现单例
     * @param type 类
     */
    private ExtensionLoader(Class<?> type){
        this.type = type;
    }

    /**
     * 获取加载器
     * @param type 扩展类
     * @return 类加载器
     */
    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type){
        if(type==null){
            throw new IllegalArgumentException("扩展类型不能为空。");
        }
        if(!type.isInterface()){
            throw new IllegalArgumentException("扩展类型必须是一个接口。");
        }
        if(type.getAnnotation(SPI.class)==null){
            throw new IllegalArgumentException("扩展类型必须添加@SPI注解。");
        }
        //单例模式，从缓存中获取，没有则创建
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if(extensionLoader==null){
            EXTENSION_LOADERS.putIfAbsent(type,new ExtensionLoader<>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    /**
     * 获取扩展类的实例
     * @param name
     * @return
     */
    public T getExtension(String name){
        if(StringUtil.isBlank(name)){
            throw new IllegalArgumentException("扩展名称不能为null或空字符串。");
        }
        //实现延迟初始化，从缓存中获取实例的holder，没有则新建一个holder
        Holder<Object> holder = cachedInstances.get(name);
        if(holder==null){
            cachedInstances.putIfAbsent(name,new Holder<>());
            holder = cachedInstances.get(name);
        }
        //利用holder进行双重检查，实现单例模式
        Object instance = holder.get();
        if(instance==null){
            synchronized (holder){
                instance = holder.get();
                if(instance==null){
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    /**
     * 单例模式创建扩展类的实例，从缓存中查找，有则返回无则新建
     * @param name 扩展类名称
     * @return 实例对象
     */
    private T createExtension(String name){
        //从所有扩展的class集合中通过名称找到此扩展的class
        Class<?> clazz = getExtensionClasses().get(name);
        if(clazz==null){
            throw new RuntimeException("没有找到此扩展："+name);
        }
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        //双重检查
        if(instance==null){
            try{
                EXTENSION_INSTANCES.putIfAbsent(clazz,clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return (T)instance;
    }

    /**
     * 获取扩展类的class类Map，从缓存中查找，有则返回无则从文件中加载所有扩展类，实现延迟初始化
     * @return
     */
    private Map<String,Class<?>> getExtensionClasses(){
        //从缓存中获取
        Map<String,Class<?>> classes = cachedClasses.get();
        //双重检查
        if (classes == null){
            synchronized (cachedClasses){
                classes = cachedClasses.get();
                if(classes == null){
                    //不需要ConcurrentHashMap，原因是class类只需要在第一次初始化时写入(双重检查已经保证了单线程操作），之后只是需要并发读取
                    classes = new HashMap<>();
                    loadDirectory(classes);
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    private void loadDirectory(Map<String,Class<?>> extensionClasses){
        log.info("loadDirectory");
        String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);

            if(urls!=null){
                log.info("urls hasMoreElements:{}",urls.hasMoreElements());
                while (urls.hasMoreElements()){
                    URL resourceUrl = urls.nextElement();
                    log.info("resourceUrl:{}",resourceUrl.toString());
                    loadResource(extensionClasses,classLoader,resourceUrl);
                }
            }else {
                log.info("urls null!");
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 用于读取和解析配置文件，并通过反射加载类(classLoader.loadClass(clazzName))
     * @param extensionClasses 扩展类的class类Map
     * @param classLoader 类加载器，可以用于动态地根据配置文件中的类名加载对应的类
     * @param resourceUrl 类配置文件的url地址
     */
    private void loadResource(Map<String,Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl){
        log.info("loadResource classLoader:{},url:{}",classLoader.toString(),resourceUrl.toString());
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(),UTF_8))){
            String line;
            //按行读取
            while ((line= reader.readLine())!=null){
                //获取每行注释的下标
                final int ci = line.indexOf('#');
                if(ci>=0){
                    //忽略注释
                    line = line.substring(0,ci);
                }
                line = line.trim();
                if(line.length()>0){
                    try{
                        //读取键值对形式的配置k=v
                        final int ei = line.indexOf('=');
                        String name = line.substring(0,ei).trim();
                        String clazzName = line.substring(ei+1).trim();
                        if(name.length()>0&&clazzName.length()>0){
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            extensionClasses.put(name,clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        log.error(e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
