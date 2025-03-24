package com.educationalapplication.gapsichallenge.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.searchDataStore by preferencesDataStore(name = "search_history")

class RecentSearchRepository(private val context: Context) {

    private val SEARCHES_KEY = stringSetPreferencesKey("recent_searches")

    fun getRecentSearches(): Flow<List<String>> {
        return context.searchDataStore.data.map { preferences ->
            preferences[SEARCHES_KEY]?.toList()?.sortedDescending() ?: emptyList()
        }
    }

    suspend fun saveSearch(query: String) {
        context.searchDataStore.edit { preferences ->
            val current = preferences[SEARCHES_KEY]?.toMutableSet() ?: mutableSetOf()
            current.add(query)
            preferences[SEARCHES_KEY] = current
        }
    }
}

