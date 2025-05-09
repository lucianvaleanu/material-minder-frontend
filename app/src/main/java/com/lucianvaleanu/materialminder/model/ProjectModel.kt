package com.valeanulucian.materialminder.model

import java.time.LocalDate

data class Project(
    var id: Int,
    val title: String,
    val date: LocalDate,
    val objectsList: List<ConstructionObjectWithCount>
)

data class ConstructionObjectWithCount(
    val constructionItem: ConstructionItem,
    val count: Int
)
