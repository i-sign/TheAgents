package org.signin.theagents.ui.room

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.store.RoomUser
import net.folivo.trixnity.client.user
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.Event
import net.folivo.trixnity.core.model.events.m.room.CreateEventContent
import net.folivo.trixnity.core.model.events.m.room.EncryptionEventContent
import net.folivo.trixnity.core.model.events.m.room.GuestAccessEventContent
import net.folivo.trixnity.core.model.events.m.room.HistoryVisibilityEventContent
import net.folivo.trixnity.core.model.events.m.room.JoinRulesEventContent
import net.folivo.trixnity.core.model.events.m.room.MemberEventContent
import net.folivo.trixnity.core.model.events.m.room.PowerLevelsEventContent
import net.folivo.trixnity.core.model.events.m.space.ParentEventContent


val DefaultEventFilter: (Event.RoomEvent<*>) -> Boolean = { event ->
    if (event is Event.StateEvent) {
        event.content is MemberEventContent
                || event.content is PowerLevelsEventContent
                || event.content is JoinRulesEventContent
                || event.content is HistoryVisibilityEventContent
                || event.content is GuestAccessEventContent
                || event.content is ParentEventContent
                || event.content is EncryptionEventContent
    } else false
}

class RoomScreenModel(
    val roomId: RoomId,
    val client: MatrixClient,
    val hideEvents: (Event.RoomEvent<*>) -> Boolean = DefaultEventFilter
) : ScreenModel {
    private val originTimeline = client.room.getTimeline(roomId) { it }
    private val itemsState = MutableStateFlow(listOf<UITimelineItem>())
    val items: StateFlow<List<UITimelineItem>> = itemsState

    val isLoadingBefore: StateFlow<Boolean> = originTimeline.state.map { it.isLoadingBefore }
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(), false)

    init {
        coroutineScope.launch {
            val room = client.room.getById(roomId).filterNotNull()
            val initTimelineFrom = room.mapNotNull { it.lastEventId }.first()
            originTimeline.init(
                initTimelineFrom,
                configBefore = {
                    maxSize = 20
                }
            )
            if (!room.first().membersLoaded) {
                client.user.loadMembers(roomId, false)
            }
            launch {
                room.mapNotNull { it.lastEventId }
                    .distinctUntilChanged()
                    .collect { loadAfter() }
            }
            launch {
                originTimeline.state
                    .transformLatest { state ->
                        coroutineScope {
                            val events = state.elements
                                .map { event -> event.map { it.event }.stateIn(this) }
                                .filterNot { eventState -> hideEvents(eventState.value) }
                            emit(events)
                        }
                    }
                    .distinctUntilChanged()
                    .map { getUiItems(it) }
                    .collect { uiItems ->
                        Napier.d("Room NEW ui items=${uiItems.size}")
                        if (uiItems.isEmpty()) {
                            Napier.i("First page is not visible in UI. Try load more")
                            loadBefore()
                        } else {
                            itemsState.value = uiItems
                        }
                    }
            }
        }
    }

    suspend fun loadBefore() {
        if (originTimeline.state.first().canLoadBefore) {
            val result = originTimeline.loadBefore {
                fetchSize = 20
            }
            val newUiElementsSize = result.newElements
                .map { it.first() }
                .filterNot { i -> hideEvents(i.event) }
                .size
            if (newUiElementsSize == 0) {
                Napier.i("Loaded page is not visible in UI. Try load more")
                loadBefore()
            }
        }
    }

    suspend fun loadAfter() {
        if (originTimeline.state.first().canLoadAfter) {
            originTimeline.loadAfter()
        }
    }

    private val userStates = mutableMapOf<UserId, StateFlow<RoomUser?>>()
    private fun getUserState(userId: UserId): StateFlow<RoomUser?> =
        userStates.getOrPut(userId) {
            client.user.getById(roomId, userId)
                .stateIn(coroutineScope, SharingStarted.Lazily, null)
        }

    private fun getUiItems(events: List<StateFlow<Event.RoomEvent<*>>>): List<UITimelineItem> {
        val tz = TimeZone.currentSystemDefault()
        return events
            .sortedByDescending { it.value.originTimestamp }
            .fold(mutableListOf()) { state, eventState ->
                val last = state.lastOrNull()
                val uiItem = eventState.toUiItem()
                if (last == null) {
                    state.add(uiItem)
                } else {
                    val lastDate = Instant.fromEpochMilliseconds(last.timestamp).toLocalDateTime(tz).date
                    val extDate = Instant.fromEpochMilliseconds(uiItem.timestamp).toLocalDateTime(tz).date
                    if (lastDate != extDate) {
                        state.add(DateItem(last.timestamp))
                        state.add(uiItem)
                    } else {
                        state.remove(last)
                        state.addAll(last.applyPrevious(uiItem))
                    }
                }

                val event = eventState.value
                if (event is Event.StateEvent && event.content is CreateEventContent) {
                    state.add(DateItem(event.originTimestamp))
                }

                state
            }
    }

    private fun StateFlow<Event.RoomEvent<*>>.toUiItem(): UITimelineItem {
        return when (val event = value) {
            is Event.MessageEvent<*> -> {
                this as StateFlow<Event.MessageEvent<*>>
                MessageItem(this, event.sender, getUserState(event.sender))
            }

            is Event.StateEvent<*> -> {
                this as StateFlow<Event.StateEvent<*>>
                when (event.content) {
                    is MemberEventContent -> {
                        MembersItem(listOf(event as Event.StateEvent<MemberEventContent>))
                    }

                    else -> {
                        StateItem(this, getUserState(event.sender))
                    }
                }
            }
        }
    }
}