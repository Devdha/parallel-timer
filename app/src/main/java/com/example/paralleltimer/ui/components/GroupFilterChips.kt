package com.example.paralleltimer.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Coffee
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.paralleltimer.R
import com.example.paralleltimer.domain.model.DefaultGroups
import com.example.paralleltimer.domain.model.GroupIcon
import com.example.paralleltimer.domain.model.TimerGroup

@Composable
fun GroupFilterChips(
    groups: List<TimerGroup>,
    selectedGroupId: String?,
    onGroupSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // "All" chip
        FilterChip(
            selected = selectedGroupId == null,
            onClick = { onGroupSelected(null) },
            label = { Text(stringResource(R.string.all_timers)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.List,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )

        // Group chips
        DefaultGroups.defaults.forEach { group ->
            FilterChip(
                selected = selectedGroupId == group.id,
                onClick = { onGroupSelected(group.id) },
                label = { Text(getGroupName(group.id)) },
                leadingIcon = {
                    Icon(
                        imageVector = getGroupIcon(group.icon),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun getGroupName(groupId: String): String {
    return when (groupId) {
        "cooking" -> stringResource(R.string.group_cooking)
        "exercise" -> stringResource(R.string.group_exercise)
        "study" -> stringResource(R.string.group_study)
        "work" -> stringResource(R.string.group_work)
        "break" -> stringResource(R.string.group_break)
        else -> groupId
    }
}

private fun getGroupIcon(icon: GroupIcon): ImageVector {
    return when (icon) {
        GroupIcon.COOKING -> Icons.Outlined.Restaurant
        GroupIcon.EXERCISE -> Icons.Outlined.FitnessCenter
        GroupIcon.STUDY -> Icons.Outlined.MenuBook
        GroupIcon.WORK -> Icons.Outlined.Work
        GroupIcon.BREAK -> Icons.Outlined.Coffee
        GroupIcon.CUSTOM -> Icons.Outlined.Label
        GroupIcon.DEFAULT -> Icons.Outlined.Timer
    }
}
