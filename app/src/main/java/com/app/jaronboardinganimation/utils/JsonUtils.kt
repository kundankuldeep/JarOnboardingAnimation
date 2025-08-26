package com.app.jaronboardinganimation.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * Utility class for JSON operations using Gson
 */
object JsonUtils {
    
    private val gson = Gson()
    
    /**
     * Convert object to JSON string
     */
    fun <T> toJson(obj: T): String {
        return gson.toJson(obj)
    }
    
    /**
     * Convert JSON string to object
     */
    fun <T> fromJson(json: String, clazz: Class<T>): T? {
        return try {
            gson.fromJson(json, clazz)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Convert JSON string to list of objects
     */
    fun <T> fromJsonList(json: String, type: java.lang.reflect.Type): List<T>? {
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Pretty print JSON string
     */
    fun prettify(json: String): String {
        return try {
            val obj = gson.fromJson(json, Any::class.java)
            gson.newBuilder().setPrettyPrinting().create().toJson(obj)
        } catch (e: Exception) {
            json
        }
    }
}
