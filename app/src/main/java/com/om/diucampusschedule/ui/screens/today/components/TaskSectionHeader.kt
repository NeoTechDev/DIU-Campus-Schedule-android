package com.om.diucampusschedule.ui.screens.today.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.om.diucampusschedule.ui.theme.customFontFamily

@Composable
fun TaskSectionHeader(
    count: Int,
    title: String,
    countColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(26.dp))
                .background(Color.LightGray)
                .then(
                    Modifier.layout { measurable, constraints ->
                        val titlePlaceable = measurable.measure(constraints)
                        val totalWidth = titlePlaceable.width + 10.dp.roundToPx() // Adding start padding
                        val totalHeight = titlePlaceable.height
                        layout(totalWidth, totalHeight) {
                            titlePlaceable.placeRelative(0, 0)
                        }
                    }
                ),
            contentAlignment = Alignment.CenterStart
        ){
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(countColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = customFontFamily()
                )
            }
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(start = 30.dp),
                fontFamily = customFontFamily()
            )
        }
        HorizontalDivider(Modifier, DividerDefaults.Thickness, Color.DarkGray)
    }
}