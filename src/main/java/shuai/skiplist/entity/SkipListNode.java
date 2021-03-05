package shuai.skiplist.entity;

import lombok.Data;

import java.util.List;

/**
 * @author shuai.yang
 */
@Data
public class SkipListNode {
    /**
     * 分值
     */
    private double score;
    /**
     * 后退指针
     */
    private SkipListNode backward;
    /**
     * 层
     */
    private List<SkipListLevel> level;
}
