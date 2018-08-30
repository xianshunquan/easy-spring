package org.pleasure.easy.sequence;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 
 * 功能描述
 * 
 * <p>
 * <a href="RedisSafeTemplateSequenceGenerator.java"><i>View Source</i></a>
 * 
 * @author xian
 * @version 1.0
 * @since 1.0
 */
public abstract class RedisSafeTemplateSequenceGenerator<T> extends RedisTemplateSequenceGenerator<T> {

    /**
     * @param seqKey
     * @param increment
     */
    RedisSafeTemplateSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey, int increment) {
        super(redisTemplate,seqKey, increment);
        
    }
    /**
     * @param seqKey
     */
    RedisSafeTemplateSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey) {
        super(redisTemplate,seqKey);
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
