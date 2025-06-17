package org.signin.theagents.ui.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.room.flatten
import net.folivo.trixnity.client.store.Room
import net.folivo.trixnity.client.store.sender
import net.folivo.trixnity.client.user
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent
import net.folivo.trixnity.core.model.events.m.room.RoomMessageEventContent
import org.signin.theagents.ui.getSubTree
import org.signin.theagents.ui.isRoot
import org.signin.theagents.ui.isSpace
import org.signin.theagents.ui.nameFlow


class HomeViewScreenModel(
    val client: MatrixClient
) : ScreenModel {
    private val chatsState = MutableStateFlow(listOf<RoomHeader>())
    val chats: StateFlow<List<RoomHeader>> get() = chatsState

    private val catalogState = MutableStateFlow(listOf<RoomHeader>())
    val catalog: StateFlow<List<RoomHeader>> get() = catalogState

    init {
        val roomService = client.room
        val allHeaders = roomService.getAll()
            .flatten()
            .flatMapLatest { rooms ->
                val headers = rooms.map { it.headerFlow() }
                combine(headers) { it.asList() }
            }
            .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), 1)

        val chatsFlow = allHeaders.map { headers ->
            headers
                .filter { !it.isSpace && roomService.isRoot(it.id) }
                .sortedByDescending { it.lastMessageDate }
        }
        val catalogFlow = allHeaders.map { headers ->
            val sortedCatalog = mutableListOf<RoomId>()
            val catalogRoots = headers.filter { it.isSpace && roomService.isRoot(it.id) }
            catalogRoots.forEach { root ->
                sortedCatalog.add(root.id)
                sortedCatalog.addAll(roomService.getSubTree(root.id))
            }
            val index = headers.associateBy { it.id }
            sortedCatalog.mapNotNull { index[it] }
        }
        coroutineScope.launch { chatsFlow.collect { chatsState.value = it } }
        coroutineScope.launch { catalogFlow.collect { catalogState.value = it } }
    }

    private fun Room.headerFlow(): Flow<RoomHeader> {
        val initFlow = flow {
            emit(
                RoomHeader(
                    id = roomId,
                    title = "",
                    lastMessageText = "",
                    lastMessageDate = lastRelevantEventTimestamp ?: Instant.fromEpochMilliseconds(0),
                    unreadCount = unreadMessageCount,
                    avatarUrl = avatarUrl,
                    isSpace = client.room.isSpace(roomId)
                )
            )
        }
        val nameFlow = nameFlow(client)

        class LastMsg(val date: Instant, val userName: String, val text: String)

        val eventFlow = client.room.getLastTimelineEvent(roomId).filterNotNull().flatMapLatest { it }.filterNotNull()
        val user = eventFlow.flatMapLatest { client.user.getById(roomId, it.sender) }
        val date = eventFlow.map { Instant.fromEpochMilliseconds(it.event.originTimestamp) }
        val message = eventFlow.map { timelineEvent ->
            when (val roomEventContent = timelineEvent.content?.getOrNull()) {
                is RoomMessageEventContent.TextMessageEventContent -> roomEventContent.body
                is RoomMessageEventContent.FileMessageEventContent -> "[file]"
                is RoomMessageEventContent.ImageMessageEventContent -> "[image]"
                is MemberEventContent -> "[${roomEventContent.membership.name}]"
                null -> timelineEvent.event::class.simpleName
                else -> roomEventContent::class.simpleName
            }
        }
        val lastMsgFlow = combine(user, message, date) { u, m, d -> LastMsg(d, u?.name.orEmpty(), m.orEmpty()) }

        return combine(initFlow, nameFlow, lastMsgFlow) { header, flows, msg ->
            header.copy(title = flows, lastMessageText = "${msg.userName}: ${msg.text}", lastMessageDate = msg.date)
        }
    }
}