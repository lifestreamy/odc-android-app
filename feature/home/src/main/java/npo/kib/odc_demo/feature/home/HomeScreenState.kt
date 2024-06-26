package npo.kib.odc_demo.feature.home

import npo.kib.odc_demo.core.model.user.AppUser

internal data class HomeScreenState(
    val balance: Int = 0,
    val currentUser: AppUser = AppUser(),
    val isUpdatingBalanceAndInfo : Boolean = false
//    val historyState : HistoryState,
//    val transactionHistoryList: List<Int>
    //todo store past transactions and display in history block.
    // Can store failed transactions as well.
)