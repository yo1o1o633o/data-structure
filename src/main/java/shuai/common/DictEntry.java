package shuai.common;

import lombok.Data;

/**
 * @author shuai.yang
 */
@Data
public class DictEntry {
    private String key;

    private DictEntry next;
}
