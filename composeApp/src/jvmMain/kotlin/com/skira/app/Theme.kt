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
import com.skira.app.composeapp.generated.resources.plex_condensed_bold
import com.skira.app.composeapp.generated.resources.plex_condensed_light
import com.skira.app.composeapp.generated.resources.plex_condensed_medium
import com.skira.app.composeapp.generated.resources.plex_condensed_regular
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

    val IBMPlexCondensed = FontFamily(
        Font(Res.font.plex_condensed_bold, FontWeight.Bold),
        Font(Res.font.plex_condensed_regular, FontWeight.Normal),
        Font(Res.font.plex_condensed_light, FontWeight.Light),
        Font(Res.font.plex_condensed_medium, FontWeight.Medium),
        Font(Res.font.plex_condensed_bold, FontWeight.Bold)
    )
    val typography = Typography(
        headlineLarge = TextStyle(
            fontFamily = IBMPlexCondensed,
            fontWeight = FontWeight.Medium,
            fontSize = 25.sp,
            letterSpacing = (-0.6).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = IBMPlexCondensed,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            letterSpacing = (-0.4).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = IBMPlexCondensed,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            letterSpacing = (-0.4).sp
        ),
        bodySmall = TextStyle(
            fontFamily = IBMPlexCondensed,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            letterSpacing = (-0.3).sp
        ),
        labelMedium = TextStyle(
            fontFamily = IBMPlexCondensed,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp
        )
    )

    val oldTypography = Typography(
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
    )

    MaterialTheme(
        typography = typography,
        colorScheme = lightColorScheme(
            primary = Color(0XFFE8EFF5),
            secondary =  Color(0XFFC7CED7),
            outline = Color(0XFFE7E7E7),
            onBackground = Color(0xFF000000),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFFFFFFF),
            surfaceVariant = Color(0xFFEFEFEF),
            surfaceContainer = Color(0xFFD9D9D9),
            surfaceContainerLow = Color(0XFFF3F5F6),
            surfaceContainerLowest = Color(0XFFFBFBFB),
        ),
        shapes = Shapes(
            extraSmall = RoundedCornerShape((4.5).dp),
            small = ShapeDefaults.Small,
            medium = RoundedCornerShape(10.dp),
            large = ShapeDefaults.Large
        )
    ) {
        content()
    }
}