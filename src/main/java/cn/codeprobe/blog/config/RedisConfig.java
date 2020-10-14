package cn.codeprobe.blog.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /**
     * 自定义 RedisTemplate <String, Object>
     * <p>
     * 因为在实际开发中所有的对象POJO都需要序列化，对象传递都是通过json的方式
     * 而默认的RedisTemplate<Object, Object>使用起来不方便
     * 同时使用默认的JdkSerializationRedisSerializer() ,来解析POJO序列化对象，是以字节数组的方式存储，带有转义符,即出现乱码
     *
     * @param factory
     * @return
     */
    @Bean
    @SuppressWarnings("all")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        //为了开发方便我们会使用 <String, Object>
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(factory);

        //Json 序列化配置
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        //String 序列化配置
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        //key采用string序列化的方式
        template.setKeySerializer(stringRedisSerializer);
        //hash的key采用string序列化的方式
        template.setHashKeySerializer(stringRedisSerializer);
        //value采用Jackson的序列化方式
        template.setValueSerializer(stringRedisSerializer);
        //hash的value采用Jackson的序列化方式
        template.setHashValueSerializer(stringRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }
}
