package ca.gbc.comp3074.uiprototype.data;

import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Converters {
    private static final String DELIMITER = "|";

    @TypeConverter
    public String fromStringList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            builder.append(values.get(i));
            if (i < values.size() - 1) {
                builder.append(DELIMITER);
            }
        }
        return builder.toString();
    }

    @TypeConverter
    public List<String> toStringList(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(value.split("\\|")));
    }
}
