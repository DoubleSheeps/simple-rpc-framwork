package ustc.young.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-29 12:51
 **/
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class RpcServiceConfig {
    private String version = "";
    //用分组来区分同一个服务的不同实现类
    private String group = "";
    //目标服务类
    private Object service;
    //传输服务提供者
    private String transport;

    public String getRpcServiceName() {
        return this.getServiceName()+ this.getGroup()+ this.getVersion();
    }

    public String getServiceName() {
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
