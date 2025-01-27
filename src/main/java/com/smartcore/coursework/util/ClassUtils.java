package com.smartcore.coursework.util;

public class ClassUtils {
    public static String getClassAndMethodName() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        StackTraceElement element = stackTrace[2]; // 0 - getStackTrace, 1 - getClassAndMethodName, 2 - вызывающий метод

        return element.getClassName() + "." + element.getMethodName();
    }
}
