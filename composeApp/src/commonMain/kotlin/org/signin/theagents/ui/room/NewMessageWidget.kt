package org.signin.theagents.ui.room

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.room.message.text
import net.folivo.trixnity.core.model.RoomId
import org.signin.theagents.LocalAppScope


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMessageWidget(
    modifier: Modifier = Modifier,
    client: MatrixClient,
    roomId: RoomId
) {
    var text by remember { mutableStateOf("") }
    var isEnabled by remember { mutableStateOf(true) }
    Surface(modifier) {
        Column {
            Divider(modifier = Modifier.fillMaxWidth().height(DividerDefaults.Thickness))
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                IconButton(
                    onClick = {}
                ) {
                    Icon(
                        modifier = Modifier.size(28.dp),
                        imageVector = Icons.Outlined.Attachment,
                        contentDescription = null
                    )
                }
                BasicTextField(
                    modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
                    maxLines = 5,
                    value = text,
                    onValueChange = { text = it },
                    textStyle = MaterialTheme.typography.labelLarge,
                ) { innerTextField ->
                    TextFieldDefaults.DecorationBox(
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                        ),
                        placeholder = {
                            Text(style = MaterialTheme.typography.labelLarge, text = "Write a message...")
                        },
                        enabled = isEnabled,
                        singleLine = false,
                        value = text,
                        innerTextField = innerTextField,
                        interactionSource = remember { MutableInteractionSource() },
                        visualTransformation = VisualTransformation.None,
                        contentPadding = PaddingValues(4.dp)
                    )
                }
                val appScope = LocalAppScope.current
                IconButton(
                    onClick = {
                        appScope.launch {
                            isEnabled = false
                            try {
                                client.room.sendMessage(roomId) { text(text.trim()) }
                                text = ""
                            } catch (e: Exception) {
                                Napier.e("Send message error", e)
                            } finally {
                                isEnabled = true
                            }
                        }
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(28.dp),
                        imageVector = Icons.Outlined.Send,
                        contentDescription = null
                    )
                }
            }
        }
    }
}