package org.pleasure.easy.sequence;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.BeanUtils;

/**
 * 
 * 功能描述,非线程安全，若要线程安全，则需要限制单线程访问，该非线程安全类是为了把实例放置在threadlocal里面，而避免并发，从而提高序列化的生成效率
 * <p>
 * <a href="RedisLongSequenceGenerator.java"><i>View Source</i></a>
 * 
 * @author xian
 * @version 1.0
 * @since 1.0
 */
public class FiexedConcurrentProxySequenceGenerator<T> implements SequenceGenerator<T> {

    private final static int DEFAULT_CONCURRENT_SIZE=1<<6;
    private final static int MAX_CONCURRENT_SIZE=1<<10;

    private SequenceGenerator<T> template;
    private Method cloneMethod;
    private ThreadSequenceGenerator<T>[] sequenceGeneratorCache;
    private int concurrentSize;
    private Lock objectLock = new ReentrantLock();

    public FiexedConcurrentProxySequenceGenerator(SequenceGenerator<T> template, int concurrentSize) {
        if (template == null) {
            throw new IllegalArgumentException("proxy target[SequenceGenerator] can't not be null");
        }
        if (concurrentSize <= 0) {
            throw new IllegalArgumentException("concurrentSize must greater than zero");
        }

        this.cloneMethod = BeanUtils.findMethod(template.getClass(), "clone", new Class[0]);
        if (this.cloneMethod == null) {
            throw new IllegalArgumentException(String.format("argument[template,class=%s] must implements clone method",
                    template.getClass().getName()));
        }
        this.template = template;
        this.concurrentSize = sizeForCap(concurrentSize);
        initThreadSequenceGeneratorCache(this.concurrentSize);
    }
    public FiexedConcurrentProxySequenceGenerator(SequenceGenerator<T> template) {
       this(template, DEFAULT_CONCURRENT_SIZE);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public T next() {
        int slotIndex = getSlotIndex();
        ThreadSequenceGenerator<T> target = sequenceGeneratorCache[slotIndex];
        if (target.casHolder(null, Thread.currentThread())) {
            try {
                SequenceGenerator<T> sq = target.getSequenceGenerator();
                if (sq == null) {
                    sq = copy();
                    target.setSequenceGenerator(sq);
                }
                return sq.next();
            } finally {
                target.casHolder(Thread.currentThread(), null);
            }
        }
        objectLock.lock();
        try {
            return template.next();
        } finally {
            objectLock.unlock();
        }
    }

    private int getSlotIndex() {
        return Thread.currentThread().getName().hashCode() & (concurrentSize-1);
    }

    @SuppressWarnings("unchecked")
    private SequenceGenerator<T> copy() {
        try {
            return (SequenceGenerator<T>) cloneMethod.invoke(template, new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException(template.getClass().getName() + " not support clone", e);
        }
    }
    @SuppressWarnings("unchecked")
    private void initThreadSequenceGeneratorCache(int size){
        this.sequenceGeneratorCache = new ThreadSequenceGenerator[this.concurrentSize];
        while(size-->0){
            sequenceGeneratorCache[size]=new ThreadSequenceGenerator<T>();
        }
    }
    static int sizeForCap(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAX_CONCURRENT_SIZE) ? MAX_CONCURRENT_SIZE : n + 1;
    }
    static class ThreadSequenceGenerator<T> {
        private AtomicReference<Object> holder = new AtomicReference<Object>(null);
        private volatile SequenceGenerator<T> sequenceGenerator;

        boolean casHolder(Object expect, Object update) {
            return holder.compareAndSet(expect, update);
        }

        SequenceGenerator<T> getSequenceGenerator() {
            return sequenceGenerator;
        }

        void setSequenceGenerator(SequenceGenerator<T> sequenceGenerator) {
            this.sequenceGenerator = sequenceGenerator;
        }
    }
}