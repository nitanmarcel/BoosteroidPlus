package dev.marcelsoftware.boosteroidplus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import compose.icons.TablerIcons
import compose.icons.tablericons.AlertTriangle
import compose.icons.tablericons.AspectRatio
import compose.icons.tablericons.Bolt
import compose.icons.tablericons.DeviceDesktop
import compose.icons.tablericons.Maximize
import compose.icons.tablericons.Rocket
import compose.icons.tablericons.WaveSawTool
import dev.marcelsoftware.boosteroidplus.common.AppDialog
import dev.marcelsoftware.boosteroidplus.common.AppDialogHeader
import dev.marcelsoftware.boosteroidplus.common.BottomSheetHost
import dev.marcelsoftware.boosteroidplus.common.BottomSheetHostState
import dev.marcelsoftware.boosteroidplus.common.ToastHost
import dev.marcelsoftware.boosteroidplus.common.XAppPrefs
import dev.marcelsoftware.boosteroidplus.common.preferences.PickerPreference
import dev.marcelsoftware.boosteroidplus.common.preferences.PrefKeys
import dev.marcelsoftware.boosteroidplus.common.preferences.SwitchPreference
import dev.marcelsoftware.boosteroidplus.common.rememberAppCronDialogState
import dev.marcelsoftware.boosteroidplus.common.rememberBooleanPreference
import dev.marcelsoftware.boosteroidplus.common.rememberBottomSheetHostState
import dev.marcelsoftware.boosteroidplus.common.rememberIntPreference
import dev.marcelsoftware.boosteroidplus.common.rememberPreferences
import dev.marcelsoftware.boosteroidplus.common.rememberStringPreference
import dev.marcelsoftware.boosteroidplus.common.rememberToastHostState
import dev.marcelsoftware.boosteroidplus.ui.theme.AppColors
import dev.marcelsoftware.boosteroidplus.ui.theme.BoosteroidTheme
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BoosteroidTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val preferences = rememberPreferences()
    var enabled by rememberBooleanPreference(PrefKeys.ENABLED, false)
    var unlockFps by rememberBooleanPreference(PrefKeys.UNLOCK_FPS, false)
    var unlockBitrate by rememberBooleanPreference(PrefKeys.UNLOCK_BITRATE, false)
    var aspectRatioIndex by rememberIntPreference(PrefKeys.ASPECT_RATIO, -1)
    var resolutionIndex by rememberIntPreference(PrefKeys.RESOLUTION, 0)
    var extendIntoNotch by rememberBooleanPreference(PrefKeys.EXTEND_INTO_NOTCH, true)
    var targetApplication by rememberStringPreference(PrefKeys.TARGET_APP, "Standard")

    val appDialogState = rememberAppCronDialogState()
    val toastHostState = rememberToastHostState()
    val bottomSheetHostState = rememberBottomSheetHostState()
    val restartRequiredMessage = stringResource(R.string.app_restart_required)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    if (!XAppPrefs.isModuleEnabled()) {
        val xposedNotActiveMessage = stringResource(R.string.xposed_not_active)
        LaunchedEffect(Unit) {
            coroutineScope.launch {
                toastHostState.showToast(xposedNotActiveMessage, TablerIcons.AlertTriangle)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.meteor),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp),
                        )

                        Text(
                            text = stringResource(R.string.app_name),
                            style =
                                MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Medium,
                                ),
                        )
                    }
                },
                actions = {
                    Switch(
                        checked = enabled,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                coroutineScope.launch {
                                    appDialogState.showDialog()
                                }
                            }
                            enabled = isChecked
                        },
                        colors =
                            SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                    )
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                modifier = Modifier.shadow(elevation = 4.dp),
            )
        },
        floatingActionButton = {
            if (!XAppPrefs.isModuleEnabled()) {
                FloatingActionButton(
                    onClick = {
                        val uri =
                            Uri.Builder()
                                .scheme("stream")
                                .authority(if (targetApplication == "Standard") "boosteroid.mobile" else "boosteroid.tv")
                                .path("launch")
                                .apply {
                                    preferences.all.forEach { (key, value) ->
                                        appendQueryParameter(key, value.toString())
                                    }
                                }
                                .build()
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    },
                ) {
                    Icon(
                        imageVector = TablerIcons.Rocket,
                        contentDescription = null,
                    )
                }
            }
        },
    ) { paddingValues ->
        Surface(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            Options(
                modifier = Modifier.fillMaxSize(),
                enabled = enabled,
                unlockFps = unlockFps,
                onUnlockFpsChange = { unlockFps = it },
                unlockBitrate = unlockBitrate,
                onUnlockBitrateChange = { unlockBitrate = it },
                aspectRatioIndex = aspectRatioIndex,
                onAspectRatioChange = { aspectRatioIndex = it },
                resolutionIndex = resolutionIndex,
                onResolutionChange = { resolutionIndex = it },
                extendIntoNotch = extendIntoNotch,
                onExtendIntoNotchChange = { extendIntoNotch = it },
                bottomSheetHostState = bottomSheetHostState,
                targetApplication = targetApplication,
                onTargetApplicationChange = { targetApplication = it },
            )

            AppDialog(
                state = appDialogState,
                header = {
                    AppDialogHeader(
                        title = stringResource(R.string.tweaks_warning_title),
                    )
                },
                onDismissRequest = {
                    coroutineScope.launch {
                        toastHostState.showToast(restartRequiredMessage, TablerIcons.AlertTriangle)
                    }
                },
            ) {
                Text(text = stringResource(R.string.tweaks_warning_message))
            }

            ToastHost(hostState = toastHostState)
            BottomSheetHost(state = bottomSheetHostState)
        }
    }
}

@Composable
fun Options(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    unlockFps: Boolean,
    onUnlockFpsChange: (Boolean) -> Unit,
    unlockBitrate: Boolean,
    onUnlockBitrateChange: (Boolean) -> Unit,
    aspectRatioIndex: Int,
    onAspectRatioChange: (Int) -> Unit,
    resolutionIndex: Int,
    onResolutionChange: (Int) -> Unit,
    extendIntoNotch: Boolean,
    onExtendIntoNotchChange: (Boolean) -> Unit,
    targetApplication: String,
    onTargetApplicationChange: (String) -> Unit,
    bottomSheetHostState: BottomSheetHostState,
) {
    val context = LocalContext.current
    val resolutionManager = remember { ResolutionManager(context) }

    val aspectRatioOptions = listOf(null) + ResolutionManager.AspectRatio.entries

    val selectedAspectRatio =
        remember(aspectRatioIndex) {
            if (aspectRatioIndex >= 0 && aspectRatioIndex < ResolutionManager.AspectRatio.entries.size) {
                ResolutionManager.AspectRatio.entries[aspectRatioIndex]
            } else {
                null
            }
        }

    val availableResolutions =
        remember(selectedAspectRatio) {
            resolutionManager.getResolutionsForAspectRatio(selectedAspectRatio)
        }

    LaunchedEffect(availableResolutions) {
        val currentWidth =
            if (resolutionIndex >= 0 && resolutionIndex < availableResolutions.size) {
                availableResolutions[resolutionIndex].first
            } else {
                resolutionManager.nativeWidth
            }

        val newIndex =
            availableResolutions.indices
                .minByOrNull { abs(availableResolutions[it].first - currentWidth) } ?: 0

        if (newIndex != resolutionIndex) {
            onResolutionChange(newIndex)
        }
    }

    LazyColumn(modifier = modifier) {
        val targets = mutableListOf("Standard", "TV")

        if (!XAppPrefs.isModuleEnabled()) {
            item {
                PickerPreference(
                    title = stringResource(R.string.target_version_title),
                    subtitle = stringResource(R.string.target_version_description),
                    selectedIndex = targets.indexOf(targetApplication),
                    options = targets,
                    optionLabel = { it },
                    onOptionSelected = {
                        onTargetApplicationChange(targets[it])
                    },
                    enabled = true,
                    icon = {
                        Icon(
                            imageVector = TablerIcons.AspectRatio,
                            contentDescription = stringResource(R.string.aspect_ratio_title),
                            tint = AppColors().iconColor,
                        )
                    },
                    bottomSheetHostState = bottomSheetHostState,
                )
            }
        }

        item {
            SwitchPreference(
                title = stringResource(R.string.unlock_fps_title),
                subtitle = stringResource(R.string.unlock_fps_description),
                value = unlockFps,
                onValueChange = onUnlockFpsChange,
                enabled = enabled,
                icon = {
                    Icon(
                        imageVector = TablerIcons.Bolt,
                        contentDescription = stringResource(R.string.unlock_fps_title),
                        tint = AppColors().iconColor,
                    )
                },
            )
        }

        item {
            SwitchPreference(
                title = stringResource(R.string.unlock_bitrate_title),
                subtitle = stringResource(R.string.unlock_bitrate_description),
                value = unlockBitrate,
                onValueChange = onUnlockBitrateChange,
                enabled = enabled,
                icon = {
                    Icon(
                        imageVector = TablerIcons.WaveSawTool,
                        contentDescription = stringResource(R.string.unlock_bitrate_title),
                        tint = AppColors().iconColor,
                    )
                },
            )
        }

        item {
            SwitchPreference(
                title = stringResource(R.string.extend_into_notch_title),
                subtitle = stringResource(R.string.extend_into_notch_description),
                value = extendIntoNotch,
                onValueChange = onExtendIntoNotchChange,
                enabled = enabled,
                icon = {
                    Icon(
                        imageVector = TablerIcons.Maximize,
                        contentDescription = stringResource(R.string.extend_into_notch_title),
                        tint = AppColors().iconColor,
                    )
                },
            )
        }

        item {
            PickerPreference(
                title = stringResource(R.string.resolution_title),
                subtitle = stringResource(R.string.resolution_description),
                selectedIndex = resolutionIndex,
                options = availableResolutions,
                optionLabel = { resolution ->
                    val isNative = availableResolutions.indexOf(resolution) == 0
                    resolutionManager.getDisplayStringForResolution(resolution, isNative)
                },
                onOptionSelected = onResolutionChange,
                enabled = enabled,
                icon = {
                    Icon(
                        imageVector = TablerIcons.DeviceDesktop,
                        contentDescription = stringResource(R.string.resolution_title),
                        tint = AppColors().iconColor,
                    )
                },
                bottomSheetHostState = bottomSheetHostState,
            )
        }

        item {
            PickerPreference(
                title = stringResource(R.string.aspect_ratio_title),
                subtitle = stringResource(R.string.aspect_ratio_description),
                selectedIndex = if (aspectRatioIndex < 0) 0 else aspectRatioIndex + 1,
                options = aspectRatioOptions,
                optionLabel = { it?.label ?: "Native" },
                onOptionSelected = { index ->
                    val newIndex = if (index == 0) -1 else index - 1
                    onAspectRatioChange(newIndex)
                },
                enabled = enabled,
                icon = {
                    Icon(
                        imageVector = TablerIcons.AspectRatio,
                        contentDescription = stringResource(R.string.aspect_ratio_title),
                        tint = AppColors().iconColor,
                    )
                },
                bottomSheetHostState = bottomSheetHostState,
            )
        }
    }
}
