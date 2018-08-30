package org.pleasure.easy.sequence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 
 * 功能描述
 * 
 * <p>
 * <a href="RedisTemplateSequenceGenerator.java"><i>View Source</i></a>
 * 
 * @author xian
 * @version 1.0
 * @since 1.0
 */
public abstract class RedisTemplateSequenceGenerator<T> extends TemplateSequenceGenerator<T> {
    /**
     * @param seqKey
     * @param increment
     */
    RedisTemplateSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey, int increment) {
        super(seqKey, increment);
        this.redisTemplate=redisTemplate;
    }
    /**
     * @param seqKey
     */
    RedisTemplateSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey) {
        super(seqKey);
        this.redisTemplate=redisTemplate;
    }
    
    private static Logger logger = LoggerFactory.getLogger(RedisTemplateSequenceGenerator.class);

    private StringRedisTemplate redisTemplate;

    public boolean resetSequence(String key) {
        try {
            logger.warn("---------reset redis key[{}] begin------------", key);
            return redisTemplate.execute(new RedisCallback<Boolean>() {
                @Override
                public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
                    String nxKey = key + ".nx";
                    logger.info("ready for require redis key[{}] 'lock ", key);
                    if (connection.setNX(nxKey.getBytes(), "1".getBytes())) {
                        logger.info("require redis key[{}] 'lock success", key);
                        connection.del(key.getBytes());
                        connection.del(nxKey.getBytes());
                        logger.info("release redis key[{}] 'lock success", key);
                        return true;
                    }
                    return false;
                }
            });
        } finally {
            logger.warn("---------reset redis key[{}] end------------", key);
        }
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected long nextStepSequence(String key) {
        return redisTemplate.opsForValue().increment(key, this.getIncrement());
    }
}
