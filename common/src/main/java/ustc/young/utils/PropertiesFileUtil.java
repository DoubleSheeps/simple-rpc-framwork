package ustc.young.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author YoungSheep
 * @description 配置文件读取工具
 * @date 2024-03-28 15:55
 **/
@Slf4j
public class PropertiesFileUtil {
    private PropertiesFileUtil(){
    }

    /**
     * 读取指定文件名的属性文件（.properties文件）并返回一个Properties对象。
     * @param fileName 文件名
     * @return Properties对象
     */
    public static Properties readPropertiesFile(String fileName) {
        //通过当前线程的上下文类加载器获取资源的URL。这里使用空字符串作为参数，表示获取当前类加载器的根路径。
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String path = "";
        if(url!=null){
            path = url.getPath()+fileName;
        }
        Properties properties = null;
        try(InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8)) {
            properties = new Properties();
            properties.load(inputStreamReader);
            log.info("读取配置文件成功:");
            properties.entrySet().forEach(entry->{
                log.info("[{}={}]",entry.getKey(),entry.getValue());
            });

        }catch (IOException e){
            log.error("读取配置文件时出错，原因是：{}",e.getMessage());
        }
        return properties;
    }
}
