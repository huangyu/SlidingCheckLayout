package com.huangyu.slidingchecklayout;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.TextView;

import com.huangyu.library.SlidingCheckLayout;

/**
 * Created by huangyu on 2018/8/21.
 */
public class RecyclerViewAdapter extends CommonRecyclerViewAdapter<String> {

    private SlidingCheckLayout scl;
    private SparseBooleanArray selectArray;

    RecyclerViewAdapter(Context context, SlidingCheckLayout scl) {
        super(context);
        this.scl = scl;
        this.selectArray = new SparseBooleanArray();
    }

    @Override
    public void convert(CommonRecyclerViewHolder holder, final String data, final int position) {
        View rlRoot = holder.getView(R.id.rl_root);
        final TextView checkTv = holder.getView(R.id.tv_check);

        checkTv.setSelected(selectArray.get(position));

        final boolean isChecked = checkTv.isSelected();
        rlRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectArray.put(position, !isChecked);
                checkTv.setSelected(!isChecked);
                notifyDataSetChanged();
            }
        });

        scl.setOnSlidingCheckListener(new SlidingCheckLayout.OnSlidingCheckListener() {
            @Override
            public void onSlidingCheckPos(int startPos, int endPos) {
                RecyclerView recyclerView = (RecyclerView) scl.getChildAt(0);
                for (int i = startPos; i <= endPos; i++) {
                    View current = recyclerView.getLayoutManager().findViewByPosition(i);
                    if (current != null) {
                        View currentLayout = current.findViewById(R.id.rl_root);
                        if (currentLayout != null) {
                            currentLayout.performClick();
                        }
                    }
                }
            }
        });
    }

    @Override
    public int getLayoutResource() {
        return R.layout.item_rlv;
    }

}
