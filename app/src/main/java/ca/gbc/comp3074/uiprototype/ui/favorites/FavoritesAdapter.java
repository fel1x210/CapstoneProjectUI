package ca.gbc.comp3074.uiprototype.ui.favorites;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ca.gbc.comp3074.uiprototype.R;
import ca.gbc.comp3074.uiprototype.data.PlaceEntity;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {

    private final List<PlaceEntity> data = new ArrayList<>();

    @NonNull
    @Override
    public FavoritesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite_card, parent, false);
        return new FavoritesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoritesViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void submitList(List<PlaceEntity> favorites) {
        data.clear();
        if (favorites != null) {
            data.addAll(favorites);
        }
        notifyDataSetChanged();
    }

    static class FavoritesViewHolder extends RecyclerView.ViewHolder {

        private final TextView emoji;
        private final TextView name;
        private final TextView type;
        private final TextView rating;
        private final TextView reviews;
        private final TextView lastVisited;
        private final TextView remove;
        private final MaterialButton buttonCheckIn;
        private final MaterialButton buttonDirections;
        private final ChipGroup tagGroup;

        FavoritesViewHolder(@NonNull View itemView) {
            super(itemView);
            emoji = itemView.findViewById(R.id.textEmoji);
            name = itemView.findViewById(R.id.textName);
            type = itemView.findViewById(R.id.textType);
            rating = itemView.findViewById(R.id.textRating);
            reviews = itemView.findViewById(R.id.textReviews);
            lastVisited = itemView.findViewById(R.id.textLastVisited);
            remove = itemView.findViewById(R.id.textRemove);
            buttonCheckIn = itemView.findViewById(R.id.buttonCheckIn);
            buttonDirections = itemView.findViewById(R.id.buttonDirections);
            tagGroup = itemView.findViewById(R.id.tagContainer);
        }

        void bind(PlaceEntity place) {
            Context context = itemView.getContext();
            emoji.setText(place.emoji);
            name.setText(place.name);
            type.setText(String.format(Locale.getDefault(), "%s • %s", place.type, place.distance));
            rating.setText(String.format(Locale.getDefault(), "⭐ %.1f", place.rating));
            reviews.setText(context.getString(R.string.favorites_reviews_format, place.reviewCount));
            lastVisited.setText(context.getString(R.string.favorites_last_visited_format, place.lastVisited));

            tagGroup.removeAllViews();
            for (String tag : place.tags) {
                Chip chip = new Chip(context, null,
                        com.google.android.material.R.style.Widget_Material3_Chip_Assist_Elevated);
                chip.setText(tag);
                chip.setChipBackgroundColorResource(R.color.quiet_space_chip_inactive);
                chip.setTextColor(context.getResources()
                        .getColor(R.color.quiet_space_text_secondary, context.getTheme()));
                chip.setRippleColorResource(R.color.quiet_space_primary);
                chip.setEnsureMinTouchTargetSize(false);
                tagGroup.addView(chip);
            }

            remove.setOnClickListener(v -> Toast.makeText(context,
                    context.getString(R.string.favorites_remove_message, place.name),
                    Toast.LENGTH_SHORT).show());

            buttonCheckIn.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(context,
                        ca.gbc.comp3074.uiprototype.ui.checkin.CheckInActivity.class);
                intent.putExtra("PLACE_NAME", place.name);
                context.startActivity(intent);
            });

            buttonDirections.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(context,
                        ca.gbc.comp3074.uiprototype.ui.main.MainActivity.class);
                intent.setFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(ca.gbc.comp3074.uiprototype.ui.main.MainActivity.EXTRA_NAVIGATE_LAT, place.latitude);
                intent.putExtra(ca.gbc.comp3074.uiprototype.ui.main.MainActivity.EXTRA_NAVIGATE_LNG, place.longitude);
                intent.putExtra(ca.gbc.comp3074.uiprototype.ui.main.MainActivity.EXTRA_NAVIGATE_NAME, place.name);
                context.startActivity(intent);
            });
        }
    }
}
