package ustc.young.utils.threadpool;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author YoungSheep
 * @description
 * @date 2024-03-29 13:58
 **/
@Slf4j
public final class ThreadPoolFactoryUtil {

    //可以根据业务名来定义不同的线程池
    private static final Map<String, ExecutorService> THREAD_POOLS = new ConcurrentHashMap<>();

    private ThreadPoolFactoryUtil(){}

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix){
        return createCustomThreadPoolIfAbsent(new CustomTreadPoolConfig(),threadNamePrefix,false);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(CustomTreadPoolConfig customTreadPoolConfig,String threadNamePrefix){
        return createCustomThreadPoolIfAbsent(customTreadPoolConfig,threadNamePrefix,false);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(CustomTreadPoolConfig customTreadPoolConfig,String threadNamePrefix,Boolean daemon){
        ExecutorService threadPool = THREAD_POOLS.computeIfAbsent(threadNamePrefix,k->createThreadPool(customTreadPoolConfig,threadNamePrefix,daemon));
        //判断线程池是否关闭，是则重新创建一个
        if(threadPool.isShutdown()||threadPool.isTerminated()){
            THREAD_POOLS.remove(threadNamePrefix);
            threadPool = createThreadPool(customTreadPoolConfig,threadNamePrefix,daemon);
            THREAD_POOLS.put(threadNamePrefix,threadPool);
        }
        return threadPool;
    }

    private static ExecutorService createThreadPool(CustomTreadPoolConfig customTreadPoolConfig,String threadNamePrefix,Boolean daemon){
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix,daemon);
        return new ThreadPoolExecutor(customTreadPoolConfig.getCorePoolSize(),customTreadPoolConfig.getMaximumPoolSize(),
                customTreadPoolConfig.getKeepAliveTime(),customTreadPoolConfig.getUnit(),customTreadPoolConfig.getWorkQueue(),
                threadFactory);
    }

    public static void shutdownAllThreadPool(){
        log.info("正在关闭所有线程池");
        THREAD_POOLS.entrySet().parallelStream().forEach(entry->{
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            log.info("关闭线程池：[{}] {}",entry.getKey(),executorService.isTerminated()?"成功":"失败");
            try {
                executorService.awaitTermination(10,TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("线程池无法终止");
                executorService.shutdownNow();
            }
        });
    }


    /**
     * 创建 ThreadFactory 。如果threadNamePrefix不为空则使用自建ThreadFactory，否则使用defaultThreadFactory
     * @param threadNamePrefix 业务前缀
     * @param daemon 是否指定为守护线程
     * @return ThreadFactory
     */
    public static ThreadFactory createThreadFactory(String threadNamePrefix,Boolean daemon){
        if(threadNamePrefix!=null){
            if(daemon!=null){
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix+"-%d")
                        .setDaemon(daemon).build();
            }else {
                return new ThreadFactoryBuilder()
                        .setNameFormat(threadNamePrefix+"-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }

    /**
     * 打印线程池的状态
     * @param threadPool 线程池对象
     */
    public static void printThreadPoolStatus(ThreadPoolExecutor threadPool) {
        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(1, createThreadFactory("print-thread-pool-status", false));
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            log.info("============ThreadPool Status=============");
            log.info("ThreadPool Size: [{}]", threadPool.getPoolSize());
            log.info("Active Threads: [{}]", threadPool.getActiveCount());
            log.info("Number of Tasks : [{}]", threadPool.getCompletedTaskCount());
            log.info("Number of Tasks in Queue: {}", threadPool.getQueue().size());
            log.info("===========================================");
        }, 0, 1, TimeUnit.SECONDS);
    }
}
