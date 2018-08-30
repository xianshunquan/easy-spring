
package org.pleasure.easy.sequence;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 功能描述
 * 
 * <p>
 * <a href="ConcurrentProxySequenceGeneratorTest.java"><i>View Source</i></a>
 * 
 * @author xian
 * @version 1.0
 * @since 1.0
 */
public class FiexedConcurrentProxySequenceGeneratorTest {

    private final static int threadCount = 100;
    private static ExecutorService es = Executors.newFixedThreadPool(threadCount);

    @Test
    public void test() {
        ApplicationContext ac = new ClassPathXmlApplicationContext("application.xml");
        StringRedisTemplate tt = ac.getBean(org.springframework.data.redis.core.StringRedisTemplate.class);
        RedisNotSafeLongSequenceGenerator r = new RedisNotSafeLongSequenceGenerator(tt, "sequence.test.notsafe", 10000);
        tt.delete("sequence.test.notsafe");
        FiexedConcurrentProxySequenceGenerator<Long> proxy = new FiexedConcurrentProxySequenceGenerator<Long>(r,100);
        List<Future<String>> list = new LinkedList<Future<String>>();
        int count = threadCount;
        while (count-- > 0) {
            list.add(es.submit(new Callable<String>() {
                public String call() {
                    long startTime = System.currentTimeMillis();
                    Long start=proxy.next();
                    Long end=start;
                    for (int i = 1; i < 100000; i++) {
                        end = proxy.next();
                    }
                    return String.format("thread=%s,take=%s,start=%s,end=%s", Thread.currentThread().getName(), (System.currentTimeMillis() - startTime),start,end);
                }
            }));
        }
        for (Future f : list) {
            try {
                System.out.println(f.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
