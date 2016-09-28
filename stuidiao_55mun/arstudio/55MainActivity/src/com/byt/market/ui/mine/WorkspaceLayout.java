/*
 * Copyright
 */

package com.byt.market.ui.mine;

import android.content.Context;
import android.util.AttributeSet;

public class WorkspaceLayout extends CellLayout {
    private static final String TAG = "Launcher.RunningProcessesLayout";

    public WorkspaceLayout(Context context) {
        this(context, null);
    }

    public WorkspaceLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WorkspaceLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    private void init() {
    }
}