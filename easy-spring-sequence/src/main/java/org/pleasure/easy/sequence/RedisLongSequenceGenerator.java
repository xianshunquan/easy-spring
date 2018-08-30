package org.pleasure.easy.sequence;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 
 * 功能描述
 * 
 * <p>
 * <a href="RedisLongSequenceGenerator.java"><i>View Source</i></a>
 * 
 * @author xian
 * @version 1.0
 * @since 1.0
 */
public class RedisLongSequenceGenerator extends RedisSafeTemplateSequenceGenerator<Long> {
    
    /**
     * @param seqKey
     * @param increment
     */
    public RedisLongSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey, int increment) {
        super(redisTemplate,seqKey, increment);
    }
    /**
     * @param seqKey
     */
    public RedisLongSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey) {
        super(redisTemplate,seqKey);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected Long long2Sequence(long seq) {
        return seq;
    }
}