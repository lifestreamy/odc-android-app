package npo.kib.odc_demo.feature_app.presentation.top_level_screens.settings_screen.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import npo.kib.odc_demo.feature_app.presentation.top_level_screens.settings_screen.SettingsRoute

const val settingsRoute = "settings_route"

fun NavController.navigateToSettingsScreen(
    navOptions: NavOptions? = navOptions {
        launchSingleTop = true
        restoreState = true
    }
) {
    this.navigate(settingsRoute, navOptions)
}

fun NavGraphBuilder.settingsScreen(onBackClick: () -> Unit) {
    composable(route = settingsRoute) {
        SettingsRoute(onBackClick = onBackClick)
    }
}