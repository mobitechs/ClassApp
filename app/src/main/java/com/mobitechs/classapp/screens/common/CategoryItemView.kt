package com.mobitechs.classapp.screens.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobitechs.classapp.data.model.response.CategoryItem
import com.mobitechs.classapp.utils.getIconFromFieldName
import com.mobitechs.classapp.utils.toComposeColor


@Composable
fun StoreCategoryItem2(
    category: CategoryItem,
    isSelected: Boolean,
    onCategorySelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight() // Dynamic height
            .padding(5.dp)
            .clickable { onCategorySelected() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = category.backgroundColor.toComposeColor(
                default = MaterialTheme.colorScheme.primaryContainer
            )
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), // Consistent padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = getIconFromFieldName(category.iconName),
                contentDescription = "${category.name}Icon",
                tint = category.iconColor.toComposeColor(
                    default = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = category.name,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f) // Slightly darker
                else
                    MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun StoreCategoryItem(
    icon: ImageVector,
    name: String,
    isSelected: Boolean,
    onCategorySelected: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight() // Dynamic height
            .padding(5.dp)
            .clickable { onCategorySelected() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            else
                Color.Transparent
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp), // Consistent padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.9f) // Slightly darker
                else
                    MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


@Composable
fun CategoryCardWithBgColorNIcon(
    category: CategoryItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().padding(5.dp), //
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = category.backgroundColor.toComposeColor(
                default = MaterialTheme.colorScheme.primaryContainer
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp) // Minimum height but can expand
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getIconFromFieldName(category.iconName),
                contentDescription = null,
                tint = category.iconColor.toComposeColor(
                    default = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Top) // Align icon to top when card expands
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp // Better line spacing
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${category.courseCount} courses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Create a reusable Grid component (like Column/Row)
@Composable
fun <T> Grid(
    items: List<T>,
    columns: Int = 2,
    horizontalSpacing: Dp = 0.dp,
    verticalSpacing: Dp = 0.dp,
    modifier: Modifier = Modifier,
    content: @Composable (T) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(verticalSpacing)
    ) {
        items.chunked(columns).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        content(item)
                    }
                }
                // Fill empty cells
                repeat(columns - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
