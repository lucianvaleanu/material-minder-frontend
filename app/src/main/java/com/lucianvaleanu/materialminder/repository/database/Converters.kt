package com.lucianvaleanu.materialminder.repository.database

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Converters {
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal): String = value.toPlainString()

    @TypeConverter
    fun toBigDecimal(value: String): BigDecimal = BigDecimal(value)

    @TypeConverter
    fun fromLocalDate(value: LocalDate): String = value.format(DateTimeFormatter.ISO_LOCAL_DATE)

    @TypeConverter
    fun toLocalDate(value: String): LocalDate = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)

    @TypeConverter
    fun fromInstant(value: Instant): String = value.toString()

    @TypeConverter
    fun toInstant(value: String): Instant = Instant.parse(value)
}