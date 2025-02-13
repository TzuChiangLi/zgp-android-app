package com.ftrend.keyboard;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ftrend.library.R;

import java.util.List;

/**
 * @author liziqiang@ftrend.cn
 */
public class KeyboardAdapter extends RecyclerView.Adapter<KeyboardAdapter.ViewHolder> implements View.OnClickListener {
    private static final String TAG = KeyboardAdapter.class.getSimpleName();
    private int parentHeight;
    private List<String> mList;
    private OnItemClickListener mItemClickListener;
    private int style = 0;

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        mItemClickListener = itemClickListener;
    }


    @Override
    public void onClick(View v) {
        if (mItemClickListener != null) {
            mItemClickListener.onItemClick(v, (int) v.getTag());
        }
    }


    public interface OnItemClickListener {
        /**
         * 点击监听
         *
         * @param v
         * @param position
         */
        void onItemClick(View v, int position);
    }


    public KeyboardAdapter(List<String> mList, int style) {
        this.mList = mList;
        this.style = style;
    }


    @NonNull
    @Override
    public KeyboardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.keyboard_rv_item, viewGroup, false);
        ViewHolder viewHolder = new KeyboardAdapter.ViewHolder(view, i);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final KeyboardAdapter.ViewHolder viewHolder, @SuppressLint("RecyclerView") final int i) {
        switch (style) {
            case 1:
                viewHolder.mNumberTv.setText(mList.get(i));
                viewHolder.mNumberTv.setTextColor(Color.BLACK);
                viewHolder.mNumberTv.setTag(i);
                viewHolder.mNumberTv.setBackgroundColor((i == 3 || i == 7 || i == 11 || i == 14 || i == 15) ? Color.parseColor("#FFFFFF") : Color.WHITE);
                viewHolder.mNumberTv.setLayoutParams(
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, parentHeight / 4));
                viewHolder.mNumberTv.setOnClickListener(this);
                viewHolder.mNumberTv.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                            case MotionEvent.ACTION_BUTTON_PRESS:
                                viewHolder.mNumberTv.setBackgroundColor(Color.parseColor("#BFBFBF"));
                                viewHolder.mNumberTv.setTextColor(Color.WHITE);
                                break;
                            case MotionEvent.ACTION_CANCEL:
                            case MotionEvent.ACTION_MOVE:
                                break;
                            default:
                                viewHolder.mNumberTv.setBackgroundColor((i == 3 || i == 7 || i == 11 || i == 14 || i == 15) ? Color.parseColor("#ffffff") : Color.WHITE);
                                viewHolder.mNumberTv.setTextColor(Color.BLACK);
                                break;
                        }
                        return false;
                    }
                });
                break;
            case 0:
            default:
                viewHolder.mNumberTv.setText(mList.get(i));
                viewHolder.mNumberTv.setTextColor(Color.BLACK);
                viewHolder.mNumberTv.setTag(i);
                viewHolder.mNumberTv.setBackgroundColor((i == 9 || i == 11) ? Color.parseColor("#dadada") : Color.WHITE);
                viewHolder.mNumberTv.setLayoutParams(
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, parentHeight / 4));
                viewHolder.mNumberTv.setOnClickListener(this);
                viewHolder.mNumberTv.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                            case MotionEvent.ACTION_BUTTON_PRESS:
                                viewHolder.mNumberTv.setBackgroundColor(Color.parseColor("#BFBFBF"));
                                viewHolder.mNumberTv.setTextColor(Color.WHITE);
                                break;
                            case MotionEvent.ACTION_CANCEL:
                            case MotionEvent.ACTION_MOVE:
                                break;
                            default:
                                viewHolder.mNumberTv.setBackgroundColor((i == 9 || i == 11) ? Color.parseColor("#dadada") : Color.WHITE);
                                viewHolder.mNumberTv.setTextColor(Color.BLACK);
                                break;
                        }
                        return false;
                    }
                });
                break;
        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mNumberTv;

        public ViewHolder(@NonNull View itemView, int position) {
            super(itemView);
            mNumberTv = itemView.findViewById(R.id.keyboard_btn);
        }
    }

    public int getParentHeight() {
        return parentHeight;
    }

    public void setParentHeight(int parentHeight) {
        this.parentHeight = parentHeight;
    }


//    @Override
//    protected void convert(final BaseViewHolder helper, String item) {
//        helper.setText(R.id.keyboard_tv, item);
//        helper.setTextColor(R.id.keyboard_tv, Color.BLACK);
//        helper.setBackgroundColor(R.id.keyboard_tv, (helper.getLayoutPosition() == 9 || helper.getLayoutPosition() == 11) ? Color.parseColor("#dadada") : Color.WHITE);
//        helper.getView(R.id.keyboard_tv).setLayoutParams(
//                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, parentHeight / 4));
//        helper.setOnTouchListener(R.id.keyboard_tv, new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                    case MotionEvent.ACTION_BUTTON_PRESS:
//                        helper.setBackgroundRes(R.id.keyboard_tv, R.drawable.shape_keyboard_btn_press);
//                        break;
//                    case MotionEvent.ACTION_CANCEL:
//                    case MotionEvent.ACTION_MOVE:
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        helper.setBackgroundRes(R.id.keyboard_tv, R.drawable.shape_keyboard_btn_normal);
//                        break;
//                }
//                return false;
//            }
//        });
//    }
}
