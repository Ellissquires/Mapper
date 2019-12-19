package com.example.mapper.services.database;

import androidx.room.TypeConverter;

import java.util.Date;

/**
 * Handles conversions for room database
 * @author Ellis Squires
 * @version 1.0
 * @since 1.0
 */
public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
