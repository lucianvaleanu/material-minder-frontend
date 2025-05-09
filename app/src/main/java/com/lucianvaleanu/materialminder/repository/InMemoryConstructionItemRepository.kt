package com.valeanulucian.materialminder.repository

import android.content.Context
import android.util.Log
import com.valeanulucian.materialminder.MainActivity
import com.valeanulucian.materialminder.R
import com.valeanulucian.materialminder.model.ConstructionItem
import org.json.JSONArray
import java.io.InputStream

class InMemoryConstructionItemRepository() : IConstructionItemRepository {

    private val constructionItems = mutableListOf<ConstructionItem>()

//    init {
//        val c1 = ConstructionItem(1, "Camin apometru PE, cu capac si garnituri trecere DN25, izolat, D 560 mm, H 900 mm", "", 1, 343.00, "https://cdn.dedeman.ro/media/catalog/product/2/0/2033738_poza_principala_1.png?optimize=low&fit=bounds&height=266&width=266&canvas=266:266")
//        val c2 = ConstructionItem(2, "Dozator apa Zass ZWD 22 C, putere incalzire 550 W, putere racire 85 W, compresor pentru racire, 3 setari de temperatura apa, indicatoare LED, alb + negru", "", 1, 1078.00, "https://cdn.dedeman.ro/media/catalog/product/3/0/3045504_1.jpg?optimize=low&fit=bounds&height=266&width=266&canvas=266:266")
//        val c3 = ConstructionItem(3, "Pompa de apa manuala B00012, cu tija extensibila, 3-19 L, albastru + alb", "", 1, 14.99, "https://cdn.dedeman.ro/media/catalog/product/3/0/3048568_1.jpg?optimize=low&fit=bounds&height=266&width=266&canvas=266:266")
//        val c4 = ConstructionItem(4, "Dozator apa Samus WDST-252CWS, putere incalzire 500 W, putere racire 85 W, compresor pentru racire, siguranta copii, alb + argintiu", "", 1, 698.99, "https://cdn.dedeman.ro/media/catalog/product/3/0/3052591.jpg?optimize=low&fit=bounds&height=266&width=266&canvas=266:266")
//        val c5 = ConstructionItem(5, "Dozator apa Samus WDSF-254CS, putere incalzire 500 W, putere racire 85 W, compresor pentru racire, siguranta copii, argintiu", "", 1, 899.00, "https://cdn.dedeman.ro/media/catalog/product/3/0/3052589.jpg?optimize=low&fit=bounds&height=266&width=266&canvas=266:266")
//        val c6 = ConstructionItem(6, "Dozator apa Zass ZWD 09 CS, cu compartiment depozitare de 14 litri, putere incalzire 550 W, putere racire 50 W, rezervor apa inox, termostat automat, argintiu + negru", "", 1, 799.00, "https://cdn.dedeman.ro/media/catalog/product/3/0/3032799_1.jpg?optimize=low&fit=bounds&height=266&width=266&canvas=266:266")
//        val c7 = ConstructionItem(7, "Dozator apa Zass ZWD 07 WF, cu sistem de filtrare, putere incalzire 500 W, putere racire 60 W, rezervor apa inox, alb", "", 1, 1438.00, "https://cdn.dedeman.ro/media/catalog/product/3/0/3035117_1.jpg?optimize=low&fit=bounds&height=266&width=266&canvas=266:266")
//        val c8 = ConstructionItem(8, "Dozator apa Zass ZWD 01 C, putere incalzire 550 W, putere racire 50 W, rezervor apa inox, termostat automat, alb", "", 1, 749.00, "https://cdn.dedeman.ro/media/catalog/product/3/0/3030121_1.jpg?optimize=low&fit=bounds&height=266&width=266&canvas=266:266")
//        val c9 = ConstructionItem(9, "Robinet coltar crom, pentru instalatii apa si incalzire, cu cap ceramic, Valvex Star, 1482670, alama, 1/2\" - 1/2", "", 1, 25.89, "https://cdn.dedeman.ro/media/catalog/product/2/0/2013183_1.jpg?optimize=low&fit=bounds&height=266&width=266&canvas=266:266")
//        val c10 = ConstructionItem(10, "Robinet coltar combinat ERGO 1/2\" x 3/8\" x 3/4", "", 1, 70.00, "https://cdn.dedeman.ro/media/catalog/product/2/0/2022409.jpg?optimize=low&fit=bounds&height=266&width=266&canvas=266:266")
//
//        add(c1)
//        add(c2)
//        add(c3)
//        add(c4)
//        add(c5)
//        add(c6)
//        add(c7)
//        add(c8)
//        add(c9)
//        add(c10)
//
//    }

    init {
        loadFromJson()
    }


    override fun getAll(): List<ConstructionItem> = constructionItems

    override fun getById(id: Int): ConstructionItem? {
        return constructionItems.find { it.id == id }
    }

    override fun add(constructionItem: ConstructionItem): Boolean {
        return constructionItems.add(constructionItem)
    }

    override fun update(constructionItem: ConstructionItem): Boolean {
        val index = constructionItems.indexOfFirst { it.id == constructionItem.id }
        return if (index >= 0) {
            constructionItems[index] = constructionItem
            true
        } else {
            false
        }
    }

    override fun deleteById(id: Int): Boolean {
        return constructionItems.removeIf { it.id == id }
    }

    private fun loadFromJson() {

        val context: Context = MainActivity.getAppContext()!!
        val inputStream: InputStream = context.resources.openRawResource(R.raw.dedeman_products)

        if (inputStream != null) {
            val jsonData = inputStream.bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonData)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val id = i + 1 // Assigning an ID based on index
                val title = jsonObject.optString("title", "No title")
                val description = "This is a construction object" // Placeholder description
                val price = jsonObject.optDouble("price", 1.0)
                val imageUri = jsonObject.optString("image", "")

                val mockObject = ConstructionItem(id, title, description, price, imageUri)
                constructionItems.add(mockObject)
            }
        } else {
            Log.w("JSON READING", "JSON file not found in res/raw, creating mock data.")
            for (i in 1..10) {
                val mockObject =
                    ConstructionItem(i, "Item $i", "This is a construction object",1.00, "")
                constructionItems.add(mockObject)
            }
        }
    }
}
