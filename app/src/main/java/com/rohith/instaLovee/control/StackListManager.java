package com.rohith.instaLovee.control;

import java.util.Collections;
import java.util.List;

public class StackListManager {

    public static void updateStackIndex(List<String> list, String tabId) {
        while (list.indexOf(tabId) != 0) {
            int i = list.indexOf(tabId);
            Collections.swap(list, i, i - 1);
        }
    }

    public static void updateStackToIndexFirst(List<String> stackList, String tabId) {
        int stackListSize = stackList.size();
        int moveUp = 1;
        while (stackList.indexOf(tabId) != stackListSize - 1) {
            int i = stackList.indexOf(tabId);
            Collections.swap(stackList, moveUp++, i);
        }
    }

    public static void updateTabStackIndex(List<String> tabList, String tabId) {
        if (!tabList.contains(tabId)) {
            tabList.add(tabId);
        }
        while (tabList.indexOf(tabId) != 0) {
            int i = tabList.indexOf(tabId);
            Collections.swap(tabList, i, i - 1);
        }
    }
}
