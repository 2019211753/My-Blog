package com.lrm.util;

import java.util.ArrayList;
import java.util.List;

public class ProcessData {

    public static List<String> removeDupicateElement(List<String> list)
    {
        List<String> listNew = new ArrayList<String>();
        for (String str : list) {
            if (!listNew.contains(str)) {
                listNew.add(str);
            }
        }
        return listNew;
    }
}
