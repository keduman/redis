package redis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.List;

@Configuration
@EnableCaching
public class RedisCacheConfig extends CachingConfigurerSupport {

    private final Logger logger = LoggerFactory.getLogger(RedisCacheConfig.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.redis.dev}")
    private boolean devRedis = false;

    @Value("${spring.redis.nodes}")
    private List<String> nodes;

    @Value("${spring.redis.password}")
    private String password;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory(){
        logger.info("Redis Dev: {} ", devRedis);
        JedisConnectionFactory factory;
        if(!devRedis){
            logger.info("Redis Nodes: {} ", nodes);
            RedisClusterConfiguration configuration = new RedisClusterConfiguration(nodes);
            factory = new JedisConnectionFactory(configuration);
        }else{
            factory = new JedisConnectionFactory();
            String value = getValue();
            logger.info("Redis Nodes: {} ", nodes);
            factory.setHostName(value.split(":")[0]);
            factory.setPort(Integer.valueOf(value.split(":")[1]));
            factory.setTimeout(16000);
        }
        factory.setPassword(password);
        return factory;
    }

    private String getValue() {
        String id = "1";
        String sql = "select value from SYS_PARAM where id = :id";
        SqlParameterSource parameterSource = new MapSqlParameterSource("id", id);
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());
        return template.queryForObject(sql, parameterSource, String.class);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(){
        RedisTemplate<String,Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

    @Bean
    @Primary
    @Override
    public CacheManager cacheManager(){
        RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate());
        redisCacheManager.setDefaultExpirition(28800l);
        return redisCacheManager;
    }

    @Bean
    public CacheManager cacheManagerOneWeekExpiration(){
        RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate());
        redisCacheManager.setDefaultExpirition(604800l);
        return redisCacheManager;
    }

    @Bean
    public CacheManager cacheManagerOneHourExpiration(){
        RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate());
        redisCacheManager.setDefaultExpirition(3600l);
        return redisCacheManager;
    }

    @Bean
    public CacheManager cacheManagerNoExpiration(){ return new RedisCacheManager(redisTemplate()); }

    @Bean
    @Override
    public KeyGenerator keyGenerator() { return new CustomKeyGenerator();
    }

}
