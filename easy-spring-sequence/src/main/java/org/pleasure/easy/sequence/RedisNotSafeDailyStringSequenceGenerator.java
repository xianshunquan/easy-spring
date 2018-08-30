package org.pleasure.easy.sequence;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.time.DateUtils;
import org.pleasure.easy.sequence.utils.NumberUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 
 * 功能描述
 * 
 * <p>
 * <a href="RedisNotSafeDailyStringSequenceGenerator.java"><i>View Source</i></a>
 * 
 * @author xian
 * @version 1.0
 * @since 1.0
 */
public class RedisNotSafeDailyStringSequenceGenerator  extends RedisTemplateSequenceGenerator<String>{

    private AtomicLong  lastRoundTime= new AtomicLong(System.currentTimeMillis() / DateUtils.MILLIS_PER_DAY * DateUtils.MILLIS_PER_DAY);;
    private long roundTimeMillsec = DateUtils.MILLIS_PER_DAY;
    private int maxLength;
    private long maxSeqInString;
    private long maxSeqNumser;
    private long maxBitMast;

    public RedisNotSafeDailyStringSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey, int maxLength, TimeUnit unit) {
        super(redisTemplate,seqKey);
        this.maxSeqNumser = maxValueInLength(maxLength);
        this.maxSeqInString=max64ValueInLength(maxLength);
        this.maxBitMast=maxMastBitInLength(maxLength);
        initRoundTime(unit);
    }
    public RedisNotSafeDailyStringSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey, int maxLength, int increment, TimeUnit unit) {
        super(redisTemplate,seqKey, increment);
        this.maxLength=maxLength;
        this.maxSeqNumser = maxValueInLength(maxLength);
        this.maxSeqInString=max64ValueInLength(maxLength);
        this.maxBitMast=maxMastBitInLength(maxLength);
        initRoundTime(unit);
    }

    public RedisNotSafeDailyStringSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey, int maxLength) {
        super(redisTemplate,seqKey);
        this.maxLength=maxLength;
        this.maxSeqNumser = maxValueInLength(maxLength);
        this.maxSeqInString=max64ValueInLength(maxLength);
        this.maxBitMast=maxMastBitInLength(maxLength);
    }

    public RedisNotSafeDailyStringSequenceGenerator(StringRedisTemplate redisTemplate,String seqKey, int maxLength, int increment) {
        super(redisTemplate,seqKey, increment);
        this.maxLength=maxLength;
        this.maxSeqNumser = maxValueInLength(maxLength);
        this.maxSeqInString=max64ValueInLength(maxLength);
        this.maxBitMast=maxMastBitInLength(maxLength);
    }
    private static long maxMastBitInLength(int maxLength){
        if(maxLength<=0 || maxLength>11){
            throw new IllegalArgumentException("maxLength must between 1-11");
        }
        if(maxLength==11){
            return 1L<<62;
        }
        return 1L <<(maxLength * 6 - 1);
    }
    private static long maxValueInLength(int maxLength){
        if(maxLength<=0 || maxLength>19){
            throw new IllegalArgumentException("maxLength must between 1-18");
        }
        long value=9;
        for(int i=1;i<maxLength;i++){
            value = value * 10 + 9;
        }
        return value;
    }
    private static long max64ValueInLength(int maxLength){
        if(maxLength<=0){
            throw new IllegalArgumentException("maxLength must greater than zero");
        }
        return maxMastBitInLength(maxLength)-1;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected String long2Sequence(long seq) {
        if(seq>maxSeqNumser){
            if(seq>maxSeqInString){
                throw new RuntimeException(String.format("seq too large ,current seq=%s,max seq=%s",seq,maxSeqInString));
            }
            seq |= maxBitMast;
            String seqStr=NumberUtils.longTo64(seq);
            int seqLength=seqStr.length();
            if(seqLength>this.maxLength){
                return seqStr.substring(seqLength-this.maxLength,seqLength);
            }
            return seqStr;
        }
        return String.valueOf(seq);
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
    protected String buildKey() {
        return super.buildKey() + "." + (System.currentTimeMillis() / roundTimeMillsec);
    }

    protected boolean isValidSeq(long seq) {
        long now = System.currentTimeMillis();
        long lastTime = lastRoundTime.get();
        if (now - lastTime >= roundTimeMillsec) {
            lastRoundTime.compareAndSet(lastTime, now / roundTimeMillsec);
            return false;
        }
        return super.isValidSeq(seq);
    }
}