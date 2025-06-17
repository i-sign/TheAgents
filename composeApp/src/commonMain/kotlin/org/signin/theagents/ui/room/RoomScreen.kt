package org.signin.theagents.ui.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import net.folivo.trixnity.client.MatrixClient
import net.folivo.trixnity.core.model.RoomId
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.signin.theagents.LocalPhoneMode
import org.signin.theagents.ui.nameFlow

object EmptyRoomScreen : Screen {

    @Composable
    override fun Content() {
        val phoneMode by LocalPhoneMode.current
        if (phoneMode) {
            Spacer(modifier = Modifier.fillMaxSize())
        } else {
            Text(
                modifier = Modifier.fillMaxSize().wrapContentHeight(),
                text = "Select chat",
                textAlign = TextAlign.Center
            )
        }
    }
}

class RoomScreen(val id: String) : Screen, KoinComponent {
    constructor(roomId: RoomId) : this(roomId.full)

    override val key = uniqueScreenKey

    @Composable
    override fun Content() {
        val client by inject<MatrixClient>()
        val roomId = RoomId(id)
        Scaffold(
            topBar = {
                Surface(shadowElevation = 3.dp) {
                    RoomTopBar(roomId, client)
                }
            },
            content = {
                Timeline(roomId, client, it)
            },
            bottomBar = {
                NewMessageWidget(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .windowInsetsPadding(WindowInsets.ime),
                    client = client,
                    roomId = roomId
                )
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun RoomTopBar(id: RoomId, client: MatrixClient) {
        val navigator = LocalNavigator.currentOrThrow
        TopAppBar(
            title = {
                val name by id.nameFlow(client).collectAsState("")
                Text(
                    text = name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                val phoneMode by LocalPhoneMode.current
                if (phoneMode) {
                    IconButton(onClick = { navigator.pop() }) {
                        Icon(imageVector = Icons.Outlined.ArrowBack, contentDescription = null)
                    }
                }
            }
        )
    }

    @Composable
    private fun Timeline(
        roomId: RoomId,
        client: MatrixClient,
        paddingValues: PaddingValues
    ) {
        val scope = rememberCoroutineScope()
        val screenModel = rememberScreenModel { RoomScreenModel(roomId, client) }
        val timelineItems by screenModel.items.collectAsState()
        val isLoadingBefore by screenModel.isLoadingBefore.collectAsState()

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            contentPadding = paddingValues,
            reverseLayout = true,
        ) {
            itemsIndexed(
                timelineItems,
                key = { index, element -> if (index == 0) index else element.id }
            ) { index, element ->
                if (index == timelineItems.size - 1) {
                    scope.launch {
                        Napier.d("loadBefore [${roomId.full}]")
                        screenModel.loadBefore()
                    }
                }
                element.render(client)
            }
            item {
                if (isLoadingBefore) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(8.dp).align(Alignment.Center).size(16.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(8.dp))
                }
            }
        }
    }
}