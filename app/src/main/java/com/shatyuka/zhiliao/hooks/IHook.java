package com.shatyuka.zhiliao.hooks;

public interface IHook {
    String getName();
    void init(final ClassLoader classLoader) throws Throwable;
    void hook() throws Throwable;
}
