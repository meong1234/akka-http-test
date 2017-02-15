package com.andipangeran.pusatlawak.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class PresentUtil {

    public static boolean isPresent(String str) {
        return str != null && !str.equals("");
    }

    public static boolean isPresent(Integer val) {
        return val != null && val != 0;
    }

    public static boolean isPresent(Long val) {
        return val != null && val != 0;
    }

    public static boolean isPresent(BigDecimal val) {
        return val != null && (val.compareTo(BigDecimal.ZERO) != 0);
    }

    public static <T> boolean isPresent(Collection<T> list) {
        return list != null && !list.isEmpty();
    }

    public static <T, R>boolean isPresent(Map<T, R> map) {
        return map != null && !map.isEmpty();
    }

    public static boolean isPresent(ArrayList<Object> array) {
        return array != null && !array.isEmpty();
    }

    public static boolean isPresent(Object obj) {
        return obj != null;
    }

    public static boolean isPresent(Object[] objs) {
        return objs != null && objs.length > 0;
    }

    public static boolean isValidClass(Object obj, Class clasz) {
        return isPresent(obj) && (clasz.isInstance(obj));
    }
}
