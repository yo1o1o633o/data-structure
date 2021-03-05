package shuai.skiplist.entity;

import lombok.Data;

/**
 * @author shuai.yang
 */
@Data
public class SkipList {
    /**
     * 头节点
     */
    private SkipListNode header;
    /**
     * 尾节点
     */
    private SkipListNode tail;
    /**
     * 节点数量
     */
    private int level;
    /**
     * 目前表内节点的最大层数
     */
    private long length;
}
