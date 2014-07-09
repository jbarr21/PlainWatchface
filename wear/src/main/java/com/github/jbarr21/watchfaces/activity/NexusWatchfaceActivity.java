package com.github.jbarr21.watchfaces.activity;

import com.github.jbarr21.watchfaces.R;
import com.twotoasters.watchface.gears.activity.GearsWatchfaceActivity;
import com.twotoasters.watchface.gears.widget.IWatchface;

public class NexusWatchfaceActivity extends GearsWatchfaceActivity {
    @Override
    protected int getLayoutResId() {
        return R.layout.nexus;
    }

    @Override
    protected IWatchface getWatchface() {
        return (IWatchface) findViewById(R.id.watchface);
    }
}
