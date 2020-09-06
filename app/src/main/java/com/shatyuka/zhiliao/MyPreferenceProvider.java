package com.shatyuka.zhiliao;

import com.crossbowffs.remotepreferences.RemotePreferenceProvider;

public class MyPreferenceProvider extends RemotePreferenceProvider {
    public MyPreferenceProvider() {
        super("com.shatyuka.zhiliao.preferences", new String[]{"com.shatyuka.zhiliao_preferences"});
    }
}
