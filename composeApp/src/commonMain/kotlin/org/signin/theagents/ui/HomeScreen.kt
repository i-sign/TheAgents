package org.signin.theagents.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.FolderCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.flow.onEach
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.clientserverapi.client.SyncState
import net.folivo.trixnity.core.model.RoomId
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.signin.theagents.service.SessionManager

class HomeScreen : Screen, KoinComponent {

    @Composable
    override fun Content() {
        val sessionManager by inject<SessionManager>()
        val client = sessionManager.getClient()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) {
            client.startSync()
            client.loginState.onEach { loginState ->
                when (loginState) {
                    MatrixClient.LoginState.LOGGED_OUT_SOFT -> {
                        // TODO only ask for password
                    }

                    MatrixClient.LoginState.LOGGED_OUT -> {
                        navigator.replaceAll(LoginScreen())
                    }

                    else -> {
                        //no-op
                    }
                }
            }
        }

        var selectedRoomId: RoomId? by remember { mutableStateOf(null) }
        var roomNavigator: Navigator? by remember { mutableStateOf(null) }
        val homeView = remember {
            movableContentOf {
                HomeView(
                    sessionManager = sessionManager,
                    selectedRoomId = selectedRoomId,
                    onRoomClick = { roomId ->
                        if (selectedRoomId != roomId) {
                            roomNavigator?.popUntilRoot()
                           // roomNavigator?.push(RoomScreen(roomId))
                        }
                    }
                )
            }
        }
    }


    @Composable
    private fun HomeView(
        sessionManager: SessionManager,
        selectedRoomId: RoomId?,
        onRoomClick: (RoomId) -> Unit
    ) {
        val client = sessionManager.getClient()
       // val screenModel = rememberScreenModel { HomeViewScreenModel(client) }
        val syncState by client.syncState.collectAsState()
        var showChats by remember { mutableStateOf(true) }
        Scaffold(
            topBar = {
                Surface(shadowElevation = 3.dp) {
                   // HomeTopBar(sessionManager, syncState)
                }
            },
            content = { innerPadding ->
                //val chats by screenModel.chats.collectAsState()
                //val catalog by screenModel.catalog.collectAsState()
                if (syncState == SyncState.INITIAL_SYNC) {
                    Box(Modifier.fillMaxSize()) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = innerPadding
                    ) {
                     /*   items(
                            items = if (showChats) chats else catalog,
                            key = { it.id.full }
                        ) { item ->
                            val isSelected = selectedRoomId == item.id
                            item.render(
                                modifier = Modifier
                                    .clickable { onRoomClick(item.id) }
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                client = client
                            )
                        }*/
                    }
                }
            },
            bottomBar = {
               /* SmallNavigationBar {
                    NavigationBarItem(
                        selected = showChats,
                        onClick = { showChats = true },
                        icon = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = Icons.Outlined.Chat,
                                contentDescription = null
                            )
                        },
                        label = null
                    )
                    NavigationBarItem(
                        selected = !showChats,
                        onClick = { showChats = false },
                        icon = {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = Icons.Outlined.FolderCopy,
                                contentDescription = null
                            )
                        },
                        label = null
                    )
                }*/
            }
        )
    }

}