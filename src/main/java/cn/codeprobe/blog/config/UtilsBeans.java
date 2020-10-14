package cn.codeprobe.blog.config;

import cn.codeprobe.blog.utils.common.RedisUtil;
import cn.codeprobe.blog.utils.id.SnowflakeIdWorker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilsBeans {

    @Bean
    public SnowflakeIdWorker snowflakeIdWorker() {
        return new SnowflakeIdWorker(0, 0);
    }

    @Bean
    public RedisUtil redisUtil() {
        return new RedisUtil();
    }
}
