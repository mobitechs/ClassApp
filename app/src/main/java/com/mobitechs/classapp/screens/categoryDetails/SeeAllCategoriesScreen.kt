package com.mobitechs.classapp.screens.categoryDetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mobitechs.classapp.data.model.response.CategoryItem
import com.mobitechs.classapp.screens.common.CourseCardEmptyMessageWithoutBox
import com.mobitechs.classapp.screens.common.Grid
import com.mobitechs.classapp.screens.common.HomeCategoryItem
import com.mobitechs.classapp.screens.home.HomeViewModel
import com.mobitechs.classapp.screens.profile.SearchAppBar
import com.mobitechs.classapp.screens.store.getCategoryIcon
import com.mobitechs.classapp.utils.openCategoryWiseDetailsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeeAllCategoriesScreen(
    navController: NavController,
    categoryJson: String
) {


    val gson = Gson()
    val categoryList: List<CategoryItem> = try {
        // Decode the URL-encoded JSON first
        val decodedJson = java.net.URLDecoder.decode(categoryJson, "UTF-8")
        gson.fromJson(decodedJson, object : TypeToken<List<CategoryItem>>() {}.type)
    } catch (e: Exception) {
        emptyList()
    }


    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Filter courses based on search query
    val filteredCategory = remember(searchQuery, categoryList) {
        if (searchQuery.isEmpty()) {
            categoryList
        } else {
            val query = searchQuery.trim().lowercase()
            categoryList.filter { item ->
                item.name.lowercase().contains(query)
            }
        }
    }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                SearchAppBar(
                    searchFor =  "Search by category name...",
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onCloseSearch = {
                        isSearchActive = false
                        searchQuery = ""
                    }
                )
            } else {
                TopAppBar(
                    title = {
                        Text(
                            text = "Categories"
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (filteredCategory.isEmpty()) {
                CourseCardEmptyMessageWithoutBox(
                    message = if (searchQuery.isNotEmpty())
                        "No categories found matching \"$searchQuery\""
                    else
                        "Categories not available",
                    description = if (searchQuery.isNotEmpty())
                        "Try searching with different keywords"
                    else
                        "Check back later for new categories",
                    Icons.Default.School
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Grid(
                        items = filteredCategory,  // Changed from categoryList to filteredCategory
                        columns = 2,
                        horizontalSpacing = 1.dp,  // Same as HomeScreen
                        verticalSpacing = 1.dp,    // Same as HomeScreen
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    ) { category ->
                        HomeCategoryItem(
                            icon = getCategoryIcon(category.name),
                            name = category.name,
                            isSelected = false,
                            onCategorySelected = {
                                openCategoryWiseDetailsScreen(
                                    navController,
                                    category.id.toString(),
                                    category.name
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}