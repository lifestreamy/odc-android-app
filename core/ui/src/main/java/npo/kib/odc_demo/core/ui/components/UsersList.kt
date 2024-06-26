package npo.kib.odc_demo.core.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import npo.kib.odc_demo.core.design_system.ui.ThemePreviews
import npo.kib.odc_demo.core.design_system.ui.theme.ODCAppTheme
import npo.kib.odc_demo.core.model.CustomBluetoothDevice

//todo add AnimatedVisibility and animateItemPlacement for user items
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UsersList(
    modifier: Modifier = Modifier,
    deviceList: List<CustomBluetoothDevice>,
    showAddresses: Boolean = true,
    onClickDevice: (CustomBluetoothDevice) -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .animateContentSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .border(
                    width = 2.dp,
                    color = LocalContentColor.current,
                    shape = RoundedCornerShape(10.dp)
                )
                .animateContentSize()
        ) {
            Column(
                modifier
                    .height(IntrinsicSize.Max)
                    .fillMaxWidth()
            ) {
                Text(
                    "Found devices:",
                    Modifier
                        .fillMaxHeight()
                        .padding(4.dp)
                        .wrapContentHeight(Alignment.CenterVertically)
                        .align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center
                )
            }
            HorizontalDivider(thickness = Dp.Hairline)
            LazyColumn(
                modifier = Modifier.animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(
                    horizontal = 10.dp, vertical = 4.dp
                )
            ) {
                items(
                    items = deviceList,
//                    key = { device -> device.address + device.name }, can cause app crash due to key collisions
                ) { device ->
                    DeviceItem(modifier = Modifier.animateItemPlacement(),
                        name = device.name,
                        address = if (showAddresses) device.address else null,
                        onItemClick = {
                            onClickDevice(device)
                        })

                }
            }
        }
    }
}

@ThemePreviews
@Composable
private fun UsersListPreview() {
    ODCAppTheme {
        UsersList(deviceList = listOf(
            CustomBluetoothDevice("Device 1", "00:00:00:00:00"),
            CustomBluetoothDevice("Device 2", "11:11:11:11:11")
        ), onClickDevice = {})
    }
}