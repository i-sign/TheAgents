package org.signin.theagents.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.client.room
import net.folivo.trixnity.client.room.RoomService
import net.folivo.trixnity.client.room.getAllState
import net.folivo.trixnity.client.store.Room
import net.folivo.trixnity.client.store.RoomUser
import net.folivo.trixnity.client.store.type
import net.folivo.trixnity.client.user
import net.folivo.trixnity.core.model.RoomId
import net.folivo.trixnity.core.model.UserId
import net.folivo.trixnity.core.model.events.m.room.CreateEventContent
import net.folivo.trixnity.core.model.events.m.space.ChildEventContent
import net.folivo.trixnity.core.model.events.m.space.ParentEventContent


suspend fun RoomService.getSubTree(rootId: RoomId): List<RoomId> {
    val result = mutableListOf<RoomId>()
    getChildren(rootId)
        .map { it to isSpace(it) }
        .sortedBy { (_, isSpace) -> isSpace } // spaces inside space should be after rooms fot better UI
        .forEach { (id, _) ->
            result.add(id)
            result.addAll(getSubTree(id))
        }
    return result
}

suspend fun RoomService.isSpace(roomId: RoomId) =
    getById(roomId).first()?.type == CreateEventContent.RoomType.Space

suspend fun RoomService.isRoot(roomId: RoomId) =
    getAllState<ParentEventContent>(roomId)
        .first()
        .isNullOrEmpty()

suspend fun RoomService.getChildren(roomId: RoomId) =
    getAllState<ChildEventContent>(roomId)
        .first()
        .orEmpty()
        .keys
        .map { RoomId(it) }

fun Room.nameFlow(client: MatrixClient): Flow<String> {
    val name = name ?: return flowOf(roomId.full)

    val (explicitName, roomIsEmpty, otherUsersCount) = name
    val heroes = name.heroes

    fun nameFromHeroes(
        roomUser: RoomUser?,
        heroes: List<UserId>,
        index: Int
    ) = (roomUser?.name ?: heroes[index].full)
    return when {
        !explicitName.isNullOrEmpty() -> flowOf(explicitName)
        heroes.isEmpty() && roomIsEmpty -> flowOf("empty room")
        heroes.isEmpty() -> flowOf(roomId.full)
        else -> combine(heroes.map { client.user.getById(roomId, it) }) {
            val heroConcat = it.mapIndexed { index: Int, roomUser: RoomUser? ->
                when {
                    otherUsersCount == 0L && index < heroes.size - 2 || otherUsersCount > 0L && index < heroes.size - 1 -> {
                        nameFromHeroes(roomUser, heroes, index) + ", "
                    }

                    otherUsersCount == 0L && index == heroes.size - 2 -> {
                        nameFromHeroes(roomUser, heroes, index) + " and "
                    }

                    otherUsersCount > 0L && index == heroes.size - 1 -> {
                        nameFromHeroes(roomUser, heroes, index) + " and $otherUsersCount others"
                    }

                    else -> {
                        nameFromHeroes(roomUser, heroes, index)
                    }
                }
            }.joinToString("")
            if (roomIsEmpty) "empty room (was $heroConcat)"
            else heroConcat
        }
    }
}

fun RoomId.nameFlow(client: MatrixClient): Flow<String> = flow {
    emitAll(client.room.getById(this@nameFlow)
        .flatMapLatest { it?.nameFlow(client) ?: flowOf(this@nameFlow.full) })
}
