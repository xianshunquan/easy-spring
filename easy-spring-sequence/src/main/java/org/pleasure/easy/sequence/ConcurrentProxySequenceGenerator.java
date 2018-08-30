package org.pleasure.easy.sequence;

import java.lang.reflect.Method;

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
public class ConcurrentProxySequenceGenerator<T> implements SequenceGenerator<T> {

    private SequenceGenerator<T> template;
    private Method cloneMethod;
    private ThreadLocal<SequenceGenerator<T>> threadLocalSequenceGenerator = new ThreadLocal<SequenceGenerator<T>>();

    public ConcurrentProxySequenceGenerator(SequenceGenerator<T> template) {
        if (template == null) {
            throw new IllegalArgumentException("proxy target[SequenceGenerator] can't not be null");
        }
        this.cloneMethod = BeanUtils.findMethod(template.getClass(), "clone", new Class[0]);
        if (this.cloneMethod == null) {
            throw new IllegalArgumentException(String.format("argument[template,class=%s] must implements clone method",
                    template.getClass().getName()));
        }
        this.template = template;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T next() {
        SequenceGenerator<T> target = threadLocalSequenceGenerator.get();
        if (target == null) {
            target = copy();
            threadLocalSequenceGenerator.set(target);
        }
        return target.next();
    }

    @SuppressWarnings("unchecked")
    private SequenceGenerator<T> copy() {
        try {
            return (SequenceGenerator<T>) cloneMethod.invoke(template, new Object[0]);
        } catch (Exception e) {
            throw new RuntimeException(template.getClass().getName() + " not support clone", e);
        }
    }
}