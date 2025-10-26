package ca.gbc.comp3074.uiprototype.util;

import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Optimized RecyclerView ItemAnimator for smooth scrolling and better performance
 */
public class OptimizedItemAnimator extends DefaultItemAnimator {
    
    public OptimizedItemAnimator() {
        super();
        // Reduce animation durations for snappier feel
        setAddDuration(200);
        setRemoveDuration(200);
        setMoveDuration(200);
        setChangeDuration(200);
    }
    
    @Override
    public boolean animateChange(RecyclerView.ViewHolder oldHolder,
                                  RecyclerView.ViewHolder newHolder,
                                  int fromX, int fromY, int toX, int toY) {
        // Optimize change animations
        if (oldHolder == newHolder) {
            // Item is the same, just notify
            return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY);
        }
        
        // Different items, cross-fade
        return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY);
    }
}
