package com.app.householdtracing.ui.views

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.app.householdtracing.R
import com.app.householdtracing.ui.theme.HouseHoldTheme

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HouseHoldScaffoldBackground(
    content: @Composable ColumnScope.() -> Unit
) {

    Scaffold(
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 263.29.dp, height = 202.dp)
                    .zIndex(1f),
                alignment = Alignment.Center,
                painter = painterResource(id = R.drawable.shopping_trip_bg),
                contentDescription = null
            )
            Text(
                text = stringResource(R.string.tv_select_shopping_trip),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.W700,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 42.sp,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = HouseHoldTheme.dimens.grid_3_5 * 2)
            )

        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = HouseHoldTheme.dimens.grid_5_5 * 4)
                .clip(
                    RoundedCornerShape(
                        topStart = HouseHoldTheme.dimens.grid_4_5 * 2,
                        topEnd = HouseHoldTheme.dimens.grid_4_5 * 2
                    )
                )
                .background(color = MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content(this)
        }
    }
}

@Preview
@Composable
fun ScaffoldBackgroundPreview() {
    HouseHoldTheme {
        HouseHoldScaffoldBackground(
            content = { /*TODO*/ }
        )
    }
}


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NavScreensScaffoldBackground(
    text: String = "",
    onBackPress: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {

    Scaffold(
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = HouseHoldTheme.dimens.grid_2_5 * 2,
                    start = HouseHoldTheme.dimens.grid_2_5,
                    end = HouseHoldTheme.dimens.grid_2_5
                ),
            verticalAlignment = Alignment.Top
        ) {

            Image(
                modifier = Modifier
                    .zIndex(1f)
                    .clickable { onBackPress() },
                alignment = Alignment.CenterStart,
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface),
                contentScale = ContentScale.Crop
            )

            Text(
                text = text,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.W600,
                    fontSize = 25.54.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 36.48.sp,
                    textAlign = TextAlign.Center
                ),
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = HouseHoldTheme.dimens.grid_4_5 * 4)
                .clip(
                    RoundedCornerShape(
                        topStart = HouseHoldTheme.dimens.grid_4_5 * 2,
                        topEnd = HouseHoldTheme.dimens.grid_4_5 * 2
                    )
                )
                .background(color = MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content(this)
        }
    }
}

@Preview
@Composable
fun NavScaffoldBackgroundPreview() {
    HouseHoldTheme {
        NavScreensScaffoldBackground(
            onBackPress = {},
            content = { /*TODO*/ }
        )
    }
}