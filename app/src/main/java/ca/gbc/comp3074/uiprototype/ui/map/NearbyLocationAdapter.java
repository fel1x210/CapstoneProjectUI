package ca.gbc.comp3074.uiprototype.ui.map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import ca.gbc.comp3074.uiprototype.R;

/**
 * Adapter for displaying nearby locations in horizontal list
 */
public class NearbyLocationAdapter extends RecyclerView.Adapter<NearbyLocationAdapter.LocationViewHolder> {

    private List<NearbyLocation> locations = new ArrayList<>();
    private OnLocationClickListener listener;

    public interface OnLocationClickListener {
        void onLocationClick(NearbyLocation location);
    }

    public NearbyLocationAdapter(OnLocationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nearby_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        NearbyLocation location = locations.get(position);
        holder.bind(location);
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public void setLocations(List<NearbyLocation> locations) {
        this.locations = locations;
        notifyDataSetChanged();
    }

    class LocationViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardLocation;
        private final TextView tvLocationName;
        private final TextView tvLocationType;
        private final TextView tvDistance;
        private final TextView tvRating;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            cardLocation = itemView.findViewById(R.id.cardLocation);
            tvLocationName = itemView.findViewById(R.id.tvLocationName);
            tvLocationType = itemView.findViewById(R.id.tvLocationType);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvRating = itemView.findViewById(R.id.tvRating);
        }

        public void bind(NearbyLocation location) {
            tvLocationName.setText(location.getName());
            tvLocationType.setText(location.getType());
            tvDistance.setText(String.format("%.1f km", location.getDistance()));
            tvRating.setText(String.format("⭐ %.1f", location.getRating()));

            cardLocation.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLocationClick(location);
                }
            });
        }
    }

    /**
     * Model class for nearby locations
     */
    public static class NearbyLocation {
        private String name;
        private String type;
        private double distance;
        private double rating;
        private LatLng position;

        public NearbyLocation(String name, String type, double distance, double rating, LatLng position) {
            this.name = name;
            this.type = type;
            this.distance = distance;
            this.rating = rating;
            this.position = position;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public double getDistance() {
            return distance;
        }

        public double getRating() {
            return rating;
        }

        public LatLng getPosition() {
            return position;
        }
    }
}
