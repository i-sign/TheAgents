package org.signin.theagents.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.folivo.trixnity.client.MatrixClient
import kotlin.math.abs


@Composable
fun Avatar(
    modifier: Modifier = Modifier,
    client: MatrixClient,
    id: String,
    url: String?,
    name: String,
    shape: RoundedCornerShape = CircleShape,
    textSize: TextUnit = 18.sp
) {
    Box(modifier) {
        val initials = name.split(" ").take(2).joinToString("") { it.take(1).uppercase() }
        val txt = if (initials.length == 2) initials else name.take(2).uppercase()
        Text(
            modifier = Modifier
                .fillMaxSize()
                .background(id.asIdColor(), shape)
                .clip(shape)
                .padding(bottom = (18.sp.value - textSize.value).dp / 2) //TODO dirty fix center alignment
                .wrapContentHeight(),
            text = txt,
            fontSize = textSize,
            textAlign = TextAlign.Center,
            color = Color.White
        )
        if (url != null) {
            /*MatrixImage(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, shape)
                    .clip(shape),
                client,
                url
            )*/
        } else if (name.isBlank()) {
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                colorFilter = ColorFilter.tint(Color.White),
                imageVector = Icons.Outlined.Person,
                contentDescription = null
            )
        }
    }
}

//https://www.composables.com/colors
private val avatarColors = arrayOf(
    Color(0xFFEF5350),
    Color(0xFFEC407A),
    Color(0xFF42A5F5),
    Color(0xFF66BB6A),
    Color(0xFFFFA726),
    Color(0xFFFF7043),
)

fun String.asIdColor(): Color {
    val code = hashCode()
    val index = abs(code) % avatarColors.size
    return avatarColors[index]
}