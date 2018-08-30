package org.pleasure.easy.sequence;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 
 * 功能描述
 * 
 * <p>
 * <a href="RedisDailyLongSequenceGenerator.java"><i>View Source</i></a>
 * 
 * @author xian
 * @version 1.0
 * @since 1.0
 */
public class RedisDailyLongSequenceGenerator extends RedisSafeTemplateSequenceGenerator<Long> {

    private AtomicLong lastRoundTime = new AtomicLong(
            System.currentTimeMillis() / DateUtils.MILLIS_PER_DAY * DateUtils.MILLIS_PER_DAY);
    private long roundTimeMillsec = DateUtils.MILLIS_PER_DAY;
    private long seqMaxNumser;

    public RedisDailyLongSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey, int maxLength, TimeUnit unit) {
        super(redisTemplate,seqKey);
        this.seqMaxNumser = maxValueInLength(maxLength);
        initRoundTime(unit);
    }

    public RedisDailyLongSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey, int maxLength, int increment, TimeUnit unit) {
        super(redisTemplate,seqKey, increment);
        this.seqMaxNumser = maxValueInLength(maxLength);
        initRoundTime(unit);
    }

    public RedisDailyLongSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey, int maxLength) {
        super(redisTemplate,seqKey);
        this.seqMaxNumser = maxValueInLength(maxLength);
    }

    public RedisDailyLongSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey, int maxLength, int increment) {
        super(redisTemplate,seqKey, increment);
        this.seqMaxNumser = maxValueInLength(maxLength);
    }

    private static long maxValueInLength(int maxLength) {
        if (maxLength <= 0 || maxLength > 19) {
            throw new IllegalArgumentException("maxLength must between 1-19");
        }
        long value = 9;
        for (int i = 1; i < maxLength; i++) {
            value = value * 10 + 9;
        }
        return value;
    }

    private void initRoundTime(TimeUnit unit) {
        switch (unit) {
        case HOURS:
            roundTimeMillsec = DateUtils.MILLIS_PER_HOUR;
            break;
        case MINUTES:
            roundTimeMillsec = DateUtils.MILLIS_PER_MINUTE;
            break;
        default:
            roundTimeMillsec = DateUtils.MILLIS_PER_DAY;
        }
        lastRoundTime = new AtomicLong(System.currentTimeMillis() / roundTimeMillsec * roundTimeMillsec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Long long2Sequence(long seq) {
        return seq;
    }

    protected String buildKey() {
        return super.buildKey() + "." + (System.currentTimeMillis() / roundTimeMillsec);
    }

    protected boolean isValidSeq(long seq) {
        if (seq > seqMaxNumser) {
            throw new RuntimeException(
                    String.format(Thread.currentThread().getName() + "seq too large,max numser is %s,current seq is %s",
                            seqMaxNumser, seq));
        }
        long now = System.currentTimeMillis();
        long lastTime = lastRoundTime.get();
        if (now - lastTime >= roundTimeMillsec) {
            lastRoundTime.compareAndSet(lastTime, now / roundTimeMillsec);
            return false;
        }
        return super.isValidSeq(seq);
    }
}
