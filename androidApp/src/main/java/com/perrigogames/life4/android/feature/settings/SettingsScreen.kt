package com.perrigogames.life4.android.feature.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.perrigogames.life4.android.compose.LIFE4Theme
import com.perrigogames.life4.android.compose.Paddings
import com.perrigogames.life4.feature.settings.SettingsAction
import com.perrigogames.life4.feature.settings.UISettingsItem
import com.perrigogames.life4.feature.settings.UISettingsData
import com.perrigogames.life4.feature.settings.UISettingsMocks
import com.perrigogames.life4.feature.settings.SettingsViewModel
import dev.icerock.moko.mvvm.createViewModelFactory

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    onNavigateToCredits: () -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = createViewModelFactory { SettingsViewModel(onClose, onNavigateToCredits) }
    ),
) {
    val state = viewModel.state.collectAsState()
    state.value?.let { data ->
        SettingsScreen(
            data = data,
            modifier = modifier,
            onAction = { viewModel.handleAction(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    data: UISettingsData,
    modifier: Modifier = Modifier,
    onAction: (SettingsAction) -> Unit = {}
) {
    val context = LocalContext.current

    BackHandler {
        onAction(SettingsAction.NavigateBack)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = { Text(text = data.screenTitle.toString(context)) },
            )
        },
    ) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding)) {
            SettingsScreenContent(
                items = data.settingsItems,
                modifier = Modifier.fillMaxSize(),
                onAction = onAction,
            )
        }
    }
}

@Composable
fun SettingsScreenContent(
    items: List<UISettingsItem>,
    modifier: Modifier = Modifier,
    onAction: (SettingsAction) -> Unit = {}
) {
    LazyColumn(modifier = modifier) {
        items(items) {
            when (it) {
                is UISettingsItem.Header -> SettingsHeaderItem(it)
                is UISettingsItem.Link -> SettingsLinkItem(it, onAction)
                is UISettingsItem.Checkbox -> SettingsCheckboxItem(it, onAction)
                UISettingsItem.Divider -> HorizontalDivider(
                    modifier = Modifier.padding(horizontal = Paddings.LARGE)
                )
            }
        }
    }
}

@Composable
private fun SettingsHeaderItem(
    item: UISettingsItem.Header
) {
    val context = LocalContext.current
    Text(
        text = item.title.toString(context),
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(horizontal = Paddings.LARGE)
            .padding(top = Paddings.LARGE)
    )
}

@Composable
private fun SettingsLinkItem(
    item: UISettingsItem.Link,
    onAction: (SettingsAction) -> Unit = {}
) {
    val context = LocalContext.current
    SettingsMenuLink(
        title = { Text(text = item.title.toString(context)) },
        subtitle = { item.subtitle?.let { Text(text = it.toString(context)) } },
        enabled = item.enabled
    ) { onAction(item.action) }
}

@Composable
private fun SettingsCheckboxItem(
    item: UISettingsItem.Checkbox,
    onAction: (SettingsAction) -> Unit = {}
) {
    val context = LocalContext.current
    SettingsMenuLink(
        title = { Text(text = item.title.toString(context)) },
        subtitle = { item.subtitle?.let { Text(text = it.toString(context)) } },
        enabled = item.enabled
    ) { onAction(item.action) }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    LIFE4Theme {
        SettingsScreen(data = UISettingsMocks.Root.page)
    }
}