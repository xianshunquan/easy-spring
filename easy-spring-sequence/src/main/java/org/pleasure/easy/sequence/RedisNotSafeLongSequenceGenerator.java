package org.pleasure.easy.sequence;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 
 * 功能描述,非线程安全，若要线程安全，则需要限制单线程访问，该非线程安全类是为了把实例放置在threadlocal里面，而避免并发，从而提高序列化的生成效率
 * 
 * <p>
 * <a href="RedisNotSafeLongSequenceGenerator.java"><i>View Source</i></a>
 * 
 * @author xian
 * @version 1.0
 * @since 1.0
 */
public class RedisNotSafeLongSequenceGenerator extends RedisTemplateSequenceGenerator<Long> {
    /**
     * @param seqKey
     * @param increment
     */
    public RedisNotSafeLongSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey, int increment) {
        super(redisTemplate,seqKey, increment);
    }
    /**
     * @param seqKey
     */
    public RedisNotSafeLongSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey) {
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