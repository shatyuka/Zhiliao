package com.shatyuka.zhiliao.xposed;

import java.lang.reflect.Executable;

import io.github.libxposed.api.XposedInterface;

public abstract class XC_MethodHook {
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {}
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {}

    public final class Unhook {
        private final XposedInterface.HookHandle handle;
        Unhook(XposedInterface.HookHandle handle) { this.handle = handle; }
        public void unhook() { handle.unhook(); }
    }

    public static final class MethodHookParam {
        public Executable method;
        public Object thisObject;
        public Object[] args;
        private Object result;
        private Throwable throwable;
        private boolean returnEarly;

        public Object getResult() { return result; }
        public void setResult(Object result) {
            this.result = result;
            this.throwable = null;
            this.returnEarly = true;
        }
        public Throwable getThrowable() { return throwable; }
        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
            this.result = null;
            this.returnEarly = true;
        }
        public boolean hasThrowable() { return throwable != null; }
    }

    final Object intercept(XposedInterface.Chain chain) throws Throwable {
        MethodHookParam param = new MethodHookParam();
        param.method = chain.getExecutable();
        param.thisObject = chain.getThisObject();
        param.args = chain.getArgs().toArray();
        beforeHookedMethod(param);
        if (!param.returnEarly) {
            try {
                param.result = param.thisObject == null
                        ? chain.proceed(param.args)
                        : chain.proceedWith(param.thisObject, param.args);
            } catch (Throwable throwable) {
                param.throwable = throwable;
            }
        }
        param.returnEarly = false;
        afterHookedMethod(param);
        if (param.throwable != null) throw param.throwable;
        return param.result;
    }

    final Unhook newUnhook(XposedInterface.HookHandle handle) { return new Unhook(handle); }
}
