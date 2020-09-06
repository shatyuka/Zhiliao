package com.shatyuka.zhiliao;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class PreferenceDonate extends Preference {
    public PreferenceDonate(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreferenceDonate(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setClickable(false);

        View wechat = holder.findViewById(R.id.imageView_wechat);
        wechat.setClickable(true);
        wechat.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "微信太傻了，只能截图扫码", Toast.LENGTH_SHORT).show();
            }
        });

        View alipay = holder.findViewById(R.id.imageView_alipay);
        alipay.setClickable(true);
        alipay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://qr.alipay.com/tsx01093z70808xrukdy2f6");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                v.getContext().startActivity(intent);
            }
        });
    }
}