package ustc.young.utils;

import lombok.extern.slf4j.Slf4j;
import ustc.young.extension.Holder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author YoungSheep
 * @description 配置文件读取工具
 * @date 2024-03-28 15:55
 **/
@Slf4j
public class PropertiesFileUtil {
    private static final Holder<Map<String,Properties>> cachedProperties = new Holder<>();
    private PropertiesFileUtil(){
    }

    /**
     * 读取指定文件名的属性文件（.properties文件）并返回一个Properties对象。
     * @param fileName 文件名
     * @return Properties对象
     */
    public static Properties getPropertiesFile(String fileName) {
        Map<String,Properties> cached = cachedProperties.get();
        //双重检查Holder
        if(null==cached){
            synchronized (cachedProperties){
                cached = cachedProperties.get();
                if(null==cached){
                    cached = new ConcurrentHashMap<>();
                    cachedProperties.set(cached);
                }
            }
        }
        Properties properties = cached.get(fileName);
        if(properties==null){
            //这段代码保证了只会读取一次，也就是单例的.
            cached.putIfAbsent(fileName,readPropertiesFile(fileName));
            properties = cached.get(fileName);
        }
        return properties;
    }

    private static Properties readPropertiesFile(String fileName) {
        Properties properties = null;
        //通过当前线程的上下文类加载器获取资源的URL。这里使用空字符串作为参数，表示获取当前类加载器的根路径。
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String path = "";
        if (url != null) {
            path = url.getPath() + fileName;
        }
        try (InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)) {
            properties = new Properties();
            properties.load(inputStreamReader);
            log.info("读取配置文件成功:");
            properties.entrySet().forEach(entry -> {
                log.info("[{}={}]", entry.getKey(), entry.getValue());
            });
        } catch (IOException e) {
            log.error("读取配置文件时出错，原因是：{}", e.getMessage());
        }
        return properties;
    }

}
