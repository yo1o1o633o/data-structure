package shuai.skiplist;

import shuai.skiplist.entity.SkipList;
import shuai.skiplist.service.SkipService;

/**
 * @author shuai.yang
 */
public class Server {
    public static void main(String[] args) {
        SkipService skipService = new SkipService();

        SkipList skipList = skipService.create();

        skipService.insert(skipList, 1);
    }
}
