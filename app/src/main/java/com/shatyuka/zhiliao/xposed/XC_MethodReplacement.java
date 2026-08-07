package com.shatyuka.zhiliao.xposed;

public abstract class XC_MethodReplacement extends XC_MethodHook {
    @Override
    protected final void beforeHookedMethod(MethodHookParam param) throws Throwable {
        param.setResult(replaceHookedMethod(param));
    }

    protected abstract Object replaceHookedMethod(MethodHookParam param) throws Throwable;

    public static XC_MethodReplacement returnConstant(Object constant) {
        return new XC_MethodReplacement() {
            @Override protected Object replaceHookedMethod(MethodHookParam param) { return constant; }
        };
    }
}
