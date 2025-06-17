package org.signin.theagents.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.core.model.RoomId
import org.signin.theagents.ui.Avatar


data class RoomHeader(
    val id: RoomId,
    val title: String,
    val lastMessageText: String,
    val lastMessageDate: Instant,
    val unreadCount: Long,
    val avatarUrl: String?,
    val isSpace: Boolean
) {
    @Composable
    fun render(modifier: Modifier = Modifier, client: MatrixClient) {
        if (!isSpace) {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(
                    modifier = androidx.compose.ui.Modifier.padding(8.dp).size(40.dp),
                    client,
                    id.full,
                    avatarUrl,
                    title
                )
                Column(
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            style = MaterialTheme.typography.bodySmall,
                            //text = lastMessageDate.toText()
                            text = lastMessageDate.toString()
                        )
                    }
                    Spacer(modifier = androidx.compose.ui.Modifier.size(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = lastMessageText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (unreadCount > 0) Badge {
                            Text(if (unreadCount < 100) unreadCount.toString() else "99+")
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.tertiaryContainer),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp).size(24.dp),
                    client,
                    id.full,
                    avatarUrl,
                    title,
                    RoundedCornerShape(20)
                )
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}