package com.skira.app

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skira.app.composeapp.generated.resources.Res
import com.skira.app.composeapp.generated.resources.geist_regular
import com.skira.app.composeapp.generated.resources.plex_light
import com.skira.app.composeapp.generated.resources.plex_medium
import com.skira.app.composeapp.generated.resources.plex_regular
import org.jetbrains.compose.resources.Font

@Composable
fun SKiRATheme(content: @Composable () -> Unit) {
    val IBMPlexSans = FontFamily(
        Font(Res.font.plex_regular, FontWeight.Normal),
        Font(Res.font.plex_medium, FontWeight.Medium),
        Font(Res.font.plex_light, FontWeight.Light)
    )
    val Geist = FontFamily(
        Font(Res.font.geist_regular, FontWeight.Normal)
    )
    MaterialTheme(
        typography = Typography(
            headlineLarge = TextStyle(
                fontFamily = Geist,
                fontWeight = FontWeight.Normal,
                fontSize = 22.sp,
                letterSpacing = (-1).sp
            ),
            headlineMedium = TextStyle(
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp
            ),
            headlineSmall = TextStyle(
                fontFamily = Geist,
                fontWeight = FontWeight.Light,
                fontSize = 15.sp
            ),
            labelLarge = TextStyle(
                fontFamily = Geist,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                letterSpacing = -(0.2).sp
            ),
            labelMedium =  TextStyle(
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                letterSpacing = -(0.2).sp
            ),
            labelSmall = TextStyle(
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp
            ),
            bodyLarge = TextStyle(
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                letterSpacing = -(0.3).sp
            )
        ),
        colorScheme = lightColorScheme(
            primary = Color(0XFFE8EFF5),
            secondary =  Color(0XFFC7CED7),
            outline = Color(0XFF385D8C),
            onBackground = Color(0xFF000000),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEFEFEF),
            surfaceContainer = Color(0xFFD9D9D9)
        ),
        shapes = Shapes(
            small = ShapeDefaults.Small,
            medium = RoundedCornerShape(10.dp),
            large = ShapeDefaults.Large
        )
    ) {
        content()
    }
}