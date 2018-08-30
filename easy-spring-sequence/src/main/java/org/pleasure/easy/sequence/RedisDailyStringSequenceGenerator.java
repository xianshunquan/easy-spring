package org.pleasure.easy.sequence;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 
 * 功能描述
 * 
 * <p>
 * <a href="RedisDailyStringSequenceGenerator.java"><i>View Source</i></a>
 * 
 * @author xian
 * @version 1.0
 * @since 1.0
 */
public class RedisDailyStringSequenceGenerator extends RedisNotSafeDailyStringSequenceGenerator{

    public RedisDailyStringSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey, int maxLength, TimeUnit unit) {
        super(redisTemplate,seqKey, maxLength, unit);
    }
    public RedisDailyStringSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey, int maxLength, int increment, TimeUnit unit) {
        super(redisTemplate,seqKey, maxLength,increment, unit);
    }

    public RedisDailyStringSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey, int maxLength) {
        super(redisTemplate,seqKey, maxLength);
    }
         
    public RedisDailyStringSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey, int maxLength, int increment) {
        super(redisTemplate,seqKey, maxLength, increment);
    }
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    protected long localNext() {
        lock.readLock().lock();
        try {
            return super.localNext();
        } finally {
            lock.readLock().unlock();
        }
    }

    protected long nextValidSequence(String key) {
        lock.writeLock().lock();
        try {
            return super.nextValidSequence(key);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
