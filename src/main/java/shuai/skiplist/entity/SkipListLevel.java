package shuai.skiplist.entity;

import lombok.Data;

/**
 * @author shuai.yang
 */
@Data
public class SkipListLevel {

    /**
     * 前进指针
     */
    private SkipListNode forward;

    /**
     * 这个层跨越的节点数量
     */
    private long span;

    public SkipListLevel() {
        this.forward = null;
        this.span = 0;
    }
}
