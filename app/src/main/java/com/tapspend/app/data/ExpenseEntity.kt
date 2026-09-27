package com.tapspend.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val category: String,
    val categoryIcon: String,
    val title: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val currency: String = "PKR"
)

enum class DefaultCategory(val label: String, val icon: String, val keywords: List<String>) {
    COFFEE("Coffee", "☕", listOf("coffee", "chai", "tea", "cafe", "espresso")),
    FOOD("Food", "🍔", listOf("food", "biryani", "lunch", "dinner", "breakfast", "burger", "pizza", "roti", "shawarma", "snacks")),
    TRAVEL("Travel", "🚕", listOf("travel", "petrol", "fuel", "ride", "uber", "careem", "rickshaw", "indrive", "fare", "bus")),
    SHOPPING("Shopping", "🛒", listOf("shopping", "groceries", "supermarket", "mart", "clothes", "shoes", "bazar", "store")),
    BILLS("Bills", "💡", listOf("bills", "electricity", "gas", "water", "wifi", "internet", "lesco", "sngpl", "kelectric")),
    HOME("Home", "🏠", listOf("home", "rent", "maintenance", "furniture", "repair", "clean")),
    HEALTH("Health", "💊", listOf("health", "medicine", "doctor", "pharmacy", "clinic", "panadol", "hospital")),
    ENTERTAINMENT("Entertainment", "🎮", listOf("entertainment", "game", "movie", "cinema", "netflix", "outing")),
    MOBILE("Mobile", "📱", listOf("mobile", "recharge", "jazz", "telenor", "zong", "easypaisa", "bundle")),
    EDUCATION("Education", "📚", listOf("education", "book", "fees", "course", "tuition", "stationery")),
    OTHER("Other", "➕", listOf("misc", "other", "extra"))
}
