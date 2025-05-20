package com.lucianvaleanu.materialminder.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "construction_item")
data class ConstructionItem(
    @PrimaryKey val id: Int,
    val name: String,
    val price: BigDecimal,
    val image: String
)