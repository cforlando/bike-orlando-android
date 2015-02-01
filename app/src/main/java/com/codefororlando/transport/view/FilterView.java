package com.codefororlando.transport.view;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckedTextView;
import android.widget.RelativeLayout;

import com.codefororlando.transport.animation.EmptyAnimatorListener;
import com.codefororlando.transport.bikeorlando.R;
import com.codefororlando.transport.controller.FeatureDescriptor;
import com.codefororlando.transport.controller.IMapController;

public class FilterView extends RelativeLayout implements View.OnClickListener {

    private View buttonFilterFeatures;
    private RecyclerView recyclerView;
    private boolean isExpanded;
    private boolean isOnScreen;
    private IMapController mapController;
    private FeatureDescriptorAdapter adapter;

    public FilterView(Context context) {
        this(context, null);
    }

    public FilterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FilterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Animate animateOpen by sliding in from the right. Animate close by sliding down and off
     * screen.
     *
     * @param open true to slide on screen, false to slide off.
     */
    public void animateOnScreen(boolean open) {

        // Ignore open requests when already on screen
        if (isOnScreen && open) {
            return;
        }

        ViewPropertyAnimator animation = animate();
        if (open) {
            animation.setListener(new EmptyAnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    recyclerView.setVisibility(INVISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    isOnScreen = true;
                }
            }).translationX(0);
        } else {
            animation.setListener(new EmptyAnimatorListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    setTranslationX(getWidth());
                    setTranslationY(0);
                    isOnScreen = false;
                    recyclerView.setVisibility(INVISIBLE);
                }
            }).translationY(getHeight());
        }

        animation.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime))
                .setInterpolator(open ? new DecelerateInterpolator() : new AccelerateInterpolator())
                .start();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.merge_filter_view, this, true);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        buttonFilterFeatures = findViewById(R.id.fab_filter_features);

        adapter = new FeatureDescriptorAdapter(new ViewHolder.OnFeatureToggledListener() {
            @Override
            public void onFeatureToggled(FeatureDescriptor featureDescriptor) {
                mapController.toggleFeature(featureDescriptor);
            }
        });

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new WrapContentCapableLinearLayoutManager(getContext(), WrapContentCapableLinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);


        recyclerView.setAlpha(0);
        buttonFilterFeatures.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_filter_features:
                toggle();
                break;
        }
    }

    private void toggle() {
        animateOpen(!isExpanded);
    }

    public void animateOpen(final boolean flag) {
        if (isExpanded == flag) {
            return;
        }

        isExpanded = flag;
        buttonFilterFeatures.animate()
                .rotationBy(180)
                .start();

        recyclerView.animate()
                .alpha(flag ? 1 : 0)
                .setListener(new EmptyAnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        recyclerView.setVisibility(VISIBLE);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!flag) {
                            recyclerView.setVisibility(INVISIBLE);
                        }
                    }
                }).start();

    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setMapController(IMapController mapController) {
        this.mapController = mapController;
        adapter.setFeatureDescriptors(mapController.getFeatureDescriptors());
    }

    private static final class FeatureDescriptorAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final ViewHolder.OnFeatureToggledListener onFeatureToggledListener;
        private FeatureDescriptor[] featureDescriptors = new FeatureDescriptor[0];

        private FeatureDescriptorAdapter(ViewHolder.OnFeatureToggledListener onFeatureToggledListener) {
            this.onFeatureToggledListener = onFeatureToggledListener;
        }

        public void setFeatureDescriptors(@NonNull FeatureDescriptor[] featureDescriptors) {
            this.featureDescriptors = featureDescriptors;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            final LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
            final View view = inflater.inflate(android.R.layout.simple_list_item_multiple_choice, viewGroup, false);
            return new ViewHolder(onFeatureToggledListener, view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int position) {
            viewHolder.setFeatureDescriptor(getFeatureDescriptor(position));
        }

        @Override
        public int getItemCount() {
            return featureDescriptors.length;
        }

        private FeatureDescriptor getFeatureDescriptor(int position) {
            return featureDescriptors[position];
        }

    }

    private static final class ViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

        private final CheckedTextView checkedTextView;
        private final ViewHolder.OnFeatureToggledListener onFeatureToggledListener;
        private FeatureDescriptor featureDescriptor;

        public ViewHolder(ViewHolder.OnFeatureToggledListener onFeatureToggledListener, View itemView) {
            super(itemView);

            this.onFeatureToggledListener = onFeatureToggledListener;
            checkedTextView = (CheckedTextView) itemView.findViewById(android.R.id.text1);
            checkedTextView.setOnClickListener(this);
        }

        private void setFeatureDescriptor(FeatureDescriptor featureDescriptor) {
            this.featureDescriptor = featureDescriptor;
            checkedTextView.setText(featureDescriptor.getFeatureName());
            checkedTextView.setChecked(featureDescriptor.isEnabled());
        }

        @Override
        public void onClick(View v) {
            onFeatureToggledListener.onFeatureToggled(featureDescriptor);
            checkedTextView.setChecked(featureDescriptor.isEnabled());
        }

        private static interface OnFeatureToggledListener {

            /**
             * Callback for a feature state toggle.
             *
             * @param featureDescriptor descriptor that was toggled
             */
            public void onFeatureToggled(FeatureDescriptor featureDescriptor);

        }

    }

    private static final class WrapContentCapableLinearLayoutManager extends LinearLayoutManager {

        private int[] mMeasuredDimension = new int[2];

        public WrapContentCapableLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        @Override
        public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state,
                              int widthSpec, int heightSpec) {
            final int widthMode = View.MeasureSpec.getMode(widthSpec);
            final int heightMode = View.MeasureSpec.getMode(heightSpec);
            final int widthSize = View.MeasureSpec.getSize(widthSpec);
            final int heightSize = View.MeasureSpec.getSize(heightSpec);
            int width = 0;
            int height = 0;
            for (int i = 0; i < getItemCount(); i++) {
                measureScrapChild(recycler, i,
                        View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                        mMeasuredDimension);

                if (getOrientation() == HORIZONTAL) {
                    width = width + mMeasuredDimension[0];
                    if (i == 0) {
                        height = mMeasuredDimension[1];
                    }
                } else {
                    height = height + mMeasuredDimension[1];
                    if (i == 0) {
                        width = mMeasuredDimension[0];
                    }
                }
            }
            switch (widthMode) {
                case View.MeasureSpec.EXACTLY:
                    width = widthSize;
                case View.MeasureSpec.AT_MOST:
                case View.MeasureSpec.UNSPECIFIED:
            }

            switch (heightMode) {
                case View.MeasureSpec.EXACTLY:
                    height = heightSize;
                case View.MeasureSpec.AT_MOST:
                case View.MeasureSpec.UNSPECIFIED:
            }

            setMeasuredDimension(width, height);
        }

        private void measureScrapChild(RecyclerView.Recycler recycler, int position, int widthSpec,
                                       int heightSpec, int[] measuredDimension) {
            View view = recycler.getViewForPosition(position);
            if (view != null) {
                RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
                int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                        getPaddingLeft() + getPaddingRight(), p.width);
                int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                        getPaddingTop() + getPaddingBottom(), p.height);
                view.measure(childWidthSpec, childHeightSpec);
                measuredDimension[0] = view.getMeasuredWidth() + p.leftMargin + p.rightMargin;
                measuredDimension[1] = view.getMeasuredHeight() + p.bottomMargin + p.topMargin;
                recycler.recycleView(view);
            }
        }
    }

}
