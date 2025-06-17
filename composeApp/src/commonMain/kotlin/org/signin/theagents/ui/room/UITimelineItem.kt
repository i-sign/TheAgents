package org.signin.theagents.ui.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Instant
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.store.RoomUser
import net.folivo.trixnity.client.store.avatarUrl
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.Event
import net.folivo.trixnity.core.model.events.m.room.AvatarEventContent
import net.folivo.trixnity.core.model.events.m.room.CreateEventContent
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent
import net.folivo.trixnity.core.model.events.m.room.Membership
import net.folivo.trixnity.core.model.events.m.room.NameEventContent
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.signin.theagents.ui.Avatar


interface UITimelineItem {
    val id: String
    val timestamp: Long

    fun applyPrevious(other: UITimelineItem): List<UITimelineItem>

    @Composable
    fun render(client: MatrixClient)
}

data class MessageItem(
    val eventState: StateFlow<Event.MessageEvent<*>>,
    val senderId: UserId,
    val senderState: StateFlow<RoomUser?>,
    val isFist: Boolean = true,
    val isLast: Boolean = true
) : UITimelineItem {
    override val id = eventState.value.id.full
    override val timestamp: Long = eventState.value.originTimestamp

    override fun applyPrevious(other: UITimelineItem): List<UITimelineItem> =
        if (other !is MessageItem) listOf(this, other)
        else if (other.senderId != senderId) listOf(this, other)
        else listOf(this.copy(isFist = false), other.copy(isLast = false))

    @Composable
    override fun render(client: MatrixClient) {
        val event by eventState.collectAsState()
        val sender by senderState.collectAsState()
        val isMy = senderId == client.userId
        val itemText = when (val content = event.content) {
            is RoomMessageEventContent -> content.body
            else -> "[${content.let { it::class.simpleName }}]" //TODO
        }

        val shape = remember(isMy) {
            RoundedCornerShape(
                topStart = if (isFist || isMy) 8.dp else 4.dp,
                topEnd = if (isFist || !isMy) 8.dp else 4.dp,
                bottomStart = if (isMy) 8.dp else 4.dp,
                bottomEnd = if (!isMy) 8.dp else 4.dp,
            )
        }
        val bubbleBg = if (isMy) {
            MaterialTheme.colorScheme.tertiaryContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        }
        val bubbleTextColor = if (isMy) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        }
        val senderName = sender?.name ?: event.sender.full
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = if (isFist) 8.dp else 2.dp,
                    bottom = if (isLast) 8.dp else 2.dp,
                ),
            horizontalArrangement = if (isMy) Arrangement.End else Arrangement.Start
        ) {
            if (!isMy && isLast) {
                Avatar(
                    modifier = Modifier.align(Alignment.Bottom).padding(end = 8.dp).requiredSize(32.dp),
                    client,
                    event.sender.full,
                    url = sender?.avatarUrl,
                    name = senderName,
                    textSize = 12.sp
                )
            } else {
                Spacer(modifier = Modifier.requiredSize(width = 40.dp, height = 32.dp))
            }
            Column(
                modifier = Modifier
                    .weight(1f, false)
                    .widthIn(min = 100.dp)
                    .clip(shape)
                    .background(bubbleBg)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                if (isFist) {
                    Text(
                        text = senderName,
                        style = MaterialTheme.typography.labelSmall,
                       // color = event.sender.full.asIdColor()
                    )
                }
                Text(
                    text = itemText,
                    color = bubbleTextColor,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    modifier = Modifier.align(Alignment.End).alpha(0.5f),
                    //text = Instant.fromEpochMilliseconds(event.originTimestamp).timeText(),
                    text = Instant.fromEpochMilliseconds(event.originTimestamp).toString(),
                    color = bubbleTextColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (isMy && isLast) {
                Avatar(
                    modifier = Modifier.align(Alignment.Bottom).padding(start = 8.dp).requiredSize(32.dp),
                    client,
                    event.sender.full,
                    url = sender?.avatarUrl,
                    name = senderName,
                    textSize = 12.sp
                )
            } else {
                Spacer(modifier = Modifier.requiredSize(width = 40.dp, height = 32.dp))
            }
        }
    }
}

data class MembersItem(
    val events: List<Event.StateEvent<MemberEventContent>>
) : UITimelineItem {
    override val id = events.last().id.full
    override val timestamp: Long = events.last().originTimestamp

    override fun applyPrevious(other: UITimelineItem): List<UITimelineItem> =
        if (other !is MembersItem) listOf(this, other)
        else listOf(MembersItem(events + other.events))

    @Composable
    override fun render(client: MatrixClient) {
        val join = events.count { it.content.membership == Membership.JOIN }
        val leave = events.count { it.content.membership == Membership.LEAVE }
        val ban = events.count { it.content.membership == Membership.BAN }
        val sum = join + leave + ban
        val text: String
        if (sum == 1) {
            val name = events.first().content.displayName ?: events.first().sender.full
            text =
                if (join > 0) "[$name] joined"
                else if (leave > 0) "[$name] left"
                else "[$name] was banned"
        } else if (sum > 1) {
            text = buildString {
                if (join > 0) appendLine("$join users joined")
                if (leave > 0) appendLine("$leave users left")
                if (ban > 0) appendLine("$ban users were banned")
            }
        } else {
            text = "..." //TODO kick and invite
        }
        Text(
            modifier = Modifier.fillMaxWidth().alpha(0.5f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            text = text.trim()
        )
    }
}

data class StateItem(
    val eventState: StateFlow<Event.StateEvent<*>>,
    val senderState: StateFlow<RoomUser?>,
) : UITimelineItem {
    override val id = eventState.value.id.full
    override val timestamp: Long = eventState.value.originTimestamp
    override fun applyPrevious(other: UITimelineItem): List<UITimelineItem> =
        listOf(this, other)

    @Composable
    override fun render(client: MatrixClient) {
        val event by eventState.collectAsState()
        val sender by senderState.collectAsState()
        val senderName = sender?.name ?: event.sender.full
        val text = when (val content = event.content) {
            is NameEventContent -> {
                "$senderName set new name: ${content.name}"
            }
            is AvatarEventContent -> {
                "$senderName set new avatar"
            }
            is CreateEventContent -> {
                "$senderName created the chat"
            }
            else -> "event: ${event.content::class.simpleName}"
        }

        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            text = text
        )
    }
}


data class DateItem(
    override val timestamp: Long
) : UITimelineItem {
    override val id = timestamp.toString()
    override fun applyPrevious(other: UITimelineItem): List<UITimelineItem> =
        listOf(this, other)

    @Composable
    override fun render(client: MatrixClient) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                //text = Instant.fromEpochMilliseconds(timestamp).fullDayText()
                text = Instant.fromEpochMilliseconds(timestamp).toString()
            )
        }
    }
}