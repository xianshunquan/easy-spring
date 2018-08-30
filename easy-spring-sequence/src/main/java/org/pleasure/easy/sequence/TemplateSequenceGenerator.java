package org.pleasure.easy.sequence;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * 功能描述
 * 
 * <p>
 * <a href="TemplateSequenceGenerator.java"><i>View Source</i></a>
 * 
 * @author xian
 * @version 1.0
 * @since 1.0
 */
public abstract class TemplateSequenceGenerator<T> implements SequenceGenerator<T>, Cloneable {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    TemplateSequenceGenerator(String seqKey, int increment) {
        if (StringUtils.isEmpty(seqKey)) {
            throw new IllegalArgumentException("seq key can't not be null");
        }
        if (increment <= 0) {
            throw new IllegalArgumentException("increment must be  positive integers");
        }
        this.seqKey = seqKey;
        this.increment = increment;
    }

    TemplateSequenceGenerator(String seqKey) {
        if (StringUtils.isEmpty(seqKey)) {
            throw new IllegalArgumentException("seq key can't not be null");
        }
        this.seqKey = seqKey;
    }

    private int increment = 1000;

    private AtomicLong currentIndex = new AtomicLong(0);

    private volatile long currentMaxIndex = INVALIDATE_SEQ;

    private String seqKey;

    static final long INVALIDATE_SEQ = -1;

    public T next() {
        long seq = localNext();
        if (!isValidSeq(seq)) {
            String key = buildKey();
            seq = nextValidSequence(key);
        }
        return long2Sequence(seq);
    }

    protected long localNext() {
        long index = currentIndex.incrementAndGet();
        if (index <= currentMaxIndex) {
            return index;
        }
        return INVALIDATE_SEQ;
    }

    protected boolean isValidSeq(long index) {
        return index > 0;
    }

    protected long nextValidSequence(String key) {
        long seq = currentIndex.incrementAndGet();
        while (seq > currentMaxIndex || seq < 0) {
            seq = nextStepSequence(key);
            if (seq < 0) {
                resetSequence(key);
            } else {
                currentIndex.set(seq);
                currentMaxIndex = seq + increment - 1;
                logger.info("key={},increment= {} ,current seq= {},current max seq= {}", key, increment, seq,currentMaxIndex);
                break;
            }
        }
        if (!isValidSeq(seq)) {
            throw new RuntimeException(String.format("illegal seq,current seq=%s,current max seq=%s" , seq,currentMaxIndex));
        }
        return seq;
    }

    protected abstract T long2Sequence(long seq);

    protected abstract long nextStepSequence(String key);

    protected abstract boolean resetSequence(String finalKey);

    protected String buildKey() {
        return this.seqKey;
    }

    public int getIncrement() {
        return increment;
    }

    public String getSeqKey() {
        return seqKey;
    }

    @SuppressWarnings("unchecked")
    public Object clone(){
        try {
            TemplateSequenceGenerator<T> t=(TemplateSequenceGenerator<T>) super.clone();
            t.currentIndex = new AtomicLong(0);
            t.currentMaxIndex=-1;
            return t;
        } catch (CloneNotSupportedException e) {
            logger.warn(e.getMessage(), e);
        }
        return null;
    }
}
