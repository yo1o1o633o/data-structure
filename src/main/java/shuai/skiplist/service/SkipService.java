package shuai.skiplist.service;

import shuai.common.RangeSpec;
import shuai.skiplist.entity.SkipList;
import shuai.skiplist.entity.SkipListLevel;
import shuai.skiplist.entity.SkipListNode;

import java.util.Random;

/**
 * @author shuai.yang
 */
public class SkipService {
    private static final Integer SKIP_LIST_MAX_LEVEL = 64;
    private static final Double SKIP_LIST_P = 0.25;

    /**
     * 创建并初始化一个新的跳表
     *
     * @return 返回跳表
     */
    public SkipList create() {
        SkipList skipList = new SkipList();
        skipList.setLevel(1);
        skipList.setLength(0L);
        skipList.setHeader(createNode(SKIP_LIST_MAX_LEVEL, 0));
        for (int i = 0; i < SKIP_LIST_MAX_LEVEL; i++) {
            skipList.getHeader().getLevel()[i] = new SkipListLevel();
        }
        skipList.getHeader().setBackward(null);
        skipList.setTail(null);
        return skipList;
    }

    /**
     * 创建并返回一个新的跳表节点
     *
     * @param level 节点数量
     * @param score 分值
     * @return 跳表节点
     */
    public SkipListNode createNode(int level, double score) {
        SkipListNode skipListNode = new SkipListNode();
        skipListNode.setScore(score);
        skipListNode.setLevel(new SkipListLevel[level]);
        return skipListNode;
    }

    /**
     * 在跳表中插入一个新节点。假定该元素尚不存在（由调用方强制实施）
     *
     * @param skipList 跳表
     * @param score    分值
     * @return 节点
     */
    public SkipListNode insert(SkipList skipList, double score) {
        // 记录寻找元素过程中，每层所跨越的节点数
        int[] rank = new int[SKIP_LIST_MAX_LEVEL];
        // 记录寻找元素过程中，每层能到达的最右节点
        SkipListNode[] update = new SkipListNode[SKIP_LIST_MAX_LEVEL];

        SkipListNode x = skipList.getHeader();
        // 记录沿途访问的节点，并计数 span 等属性
        for (int i = skipList.getLevel() - 1; i >= 0; i--) {
            rank[i] = i == skipList.getLevel() - 1 ? 0 : rank[i + 1];
            // 右节点不为空
            while (x.getLevel()[i].getForward() != null &&
                    // 右节点的 score 比给定 score 小
                    (x.getLevel()[i].getForward().getScore() < score ||
                            // 右节点的 score 相同，但节点的 member 比输入 member 要小
                            (x.getLevel()[i].getForward().getScore() == score))) {
                // 记录跨越了多少个元素
                rank[i] += x.getLevel()[i].getSpan();
                // 继续向右前进
                x = x.getLevel()[i].getForward();
            }
            // 保存访问节点
            update[i] = x;
        }

        // 我们假设该元素尚未在内部，因为我们允许重复的分数，因此永远不会发生重新插入同一元素的情况
        // 因为insert（）的调用者应在哈希表中测试该元素是否已在内部

        // 计算新的随机层数
        int level = randomLevel();
        // 如果 level 比当前 skipList 的最大层数还要大,那么更新 skipList->level 参数.并且初始化 update 和 rank 参数在相应的层的数据
        if (level > skipList.getLevel()) {
            for (int i = skipList.getLevel(); i < level; i++) {
                rank[i] = 0;
                update[i] = skipList.getHeader();
                update[i].getLevel()[i].setSpan(skipList.getLength());
            }
            skipList.setLevel(level);
        }

        // 创建新节点
        x = createNode(level, score);
        // 根据 update 和 rank 两个数组的资料，初始化新节点. 并设置相应的指针
        for (int i = 0; i < level; i++) {
            SkipListLevel skipListLevel = new SkipListLevel();
            skipListLevel.setForward(update[i].getLevel()[i].getForward());
            skipListLevel.setSpan(update[i].getLevel()[i].getSpan() - (rank[0] - rank[i]));
            x.getLevel()[i] = skipListLevel;

            update[i].getLevel()[i].setForward(x);
            update[i].getLevel()[i].setSpan((rank[0] - rank[i]) + 1);
        }

        // 更新沿途访问节点的 span 值
        for (int i = level; i < skipList.getLevel(); i++) {
            update[i].getLevel()[i].setSpan(update[i].getLevel()[i].getSpan() + 1);
        }

        // 设置后退指针
        x.setBackward((update[0] == skipList.getHeader() ? null : update[0]));
        // 设置 x 的前进指针
        if (x.getLevel()[0].getForward() != null) {
            x.getLevel()[0].getForward().setBackward(x);
        } else {
            // 这个是新的表尾节点
            skipList.setTail(x);
        }
        // 更新跳跃表节点数量
        skipList.setLength(skipList.getLength() + 1);
        return x;
    }

    public void delete(SkipList skipList, double score) {

    }

    private boolean valueGteMin(double score, RangeSpec range) {
        return range.getMinex() > 0 ? (score > range.getMin()) : (score >= range.getMin());
    }

    private boolean valueLteMax(double score, RangeSpec range) {
        return range.getMaxex() > 0 ? (score < range.getMax()) : (score <= range.getMax());
    }

    /**
     * 如果集合的一部分在范围内，则返回
     *
     * @param skipList 集合
     * @param range    范围
     */
    public Integer isInRange(SkipList skipList, RangeSpec range) {
        SkipListNode x;
        if (range.getMin() > range.getMax() || (range.getMin().equals(range.getMax()) && (range.getMinex() > 0 || range.getMaxex() > 0))) {
            return 0;
        }
        x = skipList.getTail();
        if (x == null || !valueGteMin(x.getScore(), range)) {
            return 0;
        }
        x = skipList.getHeader().getLevel()[0].getForward();
        if (x == null || !valueLteMax(x.getScore(), range)) {
            return 0;
        }
        return 1;
    }

    /**
     * 查找指定范围内包含的第一个节点。 如果范围内没有元素，则返回NULL
     *
     * @param skipList 跳表
     * @param range    范围
     * @return 节点
     */
    public SkipListNode firstInRange(SkipList skipList, RangeSpec range) {
        SkipListNode x;
        int j;

        // 如果超出范围, 直接返回
        if (isInRange(skipList, range) == 0) {
            return null;
        }

        x = skipList.getHeader();
        for (j = skipList.getLevel() - 1; j >= 0; j--) {
            while (x.getLevel()[j].getForward() != null && valueGteMin(x.getLevel()[j].getForward().getScore(), range)) {
                x = x.getLevel()[j].getForward();
            }
        }
        // 这是一个内部范围，因此下一个节点不能为NULL
        x = x.getLevel()[0].getForward();
        // 检查 score <= max
        if (!valueLteMax(x.getScore(), range)) {
            return null;
        }
        return x;
    }

    /**
     * 查找指定范围内包含的最后一个节点。 如果范围内没有元素，则返回NULL
     *
     * @param skipList 跳表
     * @param range    范围
     * @return 节点
     */
    public SkipListNode lastInRange(SkipList skipList, RangeSpec range) {
        SkipListNode x;
        int j;

        // 如果超出范围, 直接返回
        if (isInRange(skipList, range) == 0) {
            return null;
        }

        x = skipList.getHeader();
        for (j = skipList.getLevel() - 1; j >= 0; j--) {
            while (x.getLevel()[j].getForward() != null && valueLteMax(x.getLevel()[j].getForward().getScore(), range)) {
                x = x.getLevel()[j].getForward();
            }
        }

        if (!valueGteMin(x.getScore(), range)) {
            return null;
        }
        return x;
    }

    private int randomLevel() {
        int level = 1;
        int n = 65535;
        while ((new Random().nextInt(Integer.MAX_VALUE) & n) < (SKIP_LIST_P * n)) {
            level += 1;
        }
        return (level < SKIP_LIST_MAX_LEVEL) ? level : SKIP_LIST_MAX_LEVEL;
    }
}
