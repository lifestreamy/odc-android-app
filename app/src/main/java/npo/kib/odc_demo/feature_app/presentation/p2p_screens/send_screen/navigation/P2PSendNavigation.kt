package npo.kib.odc_demo.feature_app.presentation.p2p_screens.send_screen.navigation

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.composable
import npo.kib.odc_demo.feature_app.presentation.p2p_screens.send_screen.SendRoute
import npo.kib.odc_demo.feature_app.presentation.p2p_screens.send_screen.SendViewModel

const val p2pSendRoute = "p2p_send_route"

fun NavController.navigateToSendScreen(navOptions: NavOptions? = null) {
    this.navigate(
        p2pSendRoute,
        navOptions
    )
//    or pass options in builder:
//    this.navigate(p2pSendNavigationRoute){ /*Here is NavOptionsBuilder, like popUpTo(){}, launchSingleTop
//    = true, restoreState = true, etc */ }
}

fun NavGraphBuilder.sendScreen(navigateToP2PRoot: () -> Unit) {
    composable(route = p2pSendRoute) {
        SendRoute(
            navigateToP2PRoot = navigateToP2PRoot,
            navBackStackEntry = it
        )
    }
}