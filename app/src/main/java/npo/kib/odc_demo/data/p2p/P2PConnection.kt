package npo.kib.odc_demo.data.p2p

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import npo.kib.odc_demo.R
import npo.kib.odc_demo.data.models.ConnectingStatus
import npo.kib.odc_demo.data.models.SearchingStatus

class P2PConnection(context: Context) {
    private val mConnectionsClient = Nearby.getConnectionsClient(context)
    private val serviceId = "npo.kib.odc_demo"
    private lateinit var connectionEndpoint: String

    private val usernameKey = context.resources.getString(R.string.username_key)
    private val defaultUsername = "User"
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    private val userName = prefs.getString(usernameKey, defaultUsername) ?: defaultUsername

    private val _connectionResult: MutableStateFlow<ConnectingStatus> =
        MutableStateFlow(ConnectingStatus.NoConnection)
    val connectionResult = _connectionResult.asStateFlow()

    private val _searchingStatusFlow: MutableStateFlow<SearchingStatus> =
        MutableStateFlow(SearchingStatus.NONE)
    val searchingStatusFlow = _searchingStatusFlow.asStateFlow()

    private val _receivedBytes = MutableSharedFlow<ByteArray>()
    val receivedBytes = _receivedBytes.asSharedFlow()

    fun startAdvertising() {
        val advertisingOptions =
            AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()
        mConnectionsClient
            .startAdvertising(
                userName, serviceId, connectionLifecycleCallback, advertisingOptions
            )
            .addOnSuccessListener {
                // We're advertising!
                _searchingStatusFlow.update { SearchingStatus.ADVERTISING }
            }
            .addOnFailureListener {
                // We were unable to start advertising.
                _searchingStatusFlow.update { SearchingStatus.FAILURE }
            }
    }

    fun startDiscovery() {
        val discoveryOptions =
            DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build()
        mConnectionsClient
            .startDiscovery(serviceId, endpointDiscoveryCallback, discoveryOptions)
            .addOnSuccessListener {
                _searchingStatusFlow.update { SearchingStatus.DISCOVERING }
            }
            .addOnFailureListener {
                _searchingStatusFlow.update { SearchingStatus.FAILURE }
            }
    }

    fun stopAdvertising() {
        mConnectionsClient.stopAllEndpoints()
        mConnectionsClient.stopAdvertising()
        _searchingStatusFlow.update { SearchingStatus.NONE }
    }

    fun stopDiscovery() {
        mConnectionsClient.stopAllEndpoints()
        mConnectionsClient.stopDiscovery()
        _searchingStatusFlow.update { SearchingStatus.NONE }
    }

    private val endpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
                // An endpoint was found. We request a connection to it.
                Log.d("OpenDigitalCash", "An endpoint was found. We request a connection to it.")
                mConnectionsClient
                    .requestConnection(userName, endpointId, connectionLifecycleCallback)
                    .addOnSuccessListener {
                        // We successfully requested a connection. Now both sides
                        // must accept before the connection is established.
                    }
                    .addOnFailureListener {
                        // Nearby Connections failed to request the connection.})
                    }
            }

            override fun onEndpointLost(endpointId: String) {
                // A previously discovered endpoint has gone away.
            }
        }

    private val connectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(endpointId: String, info: ConnectionInfo) {
                connectionEndpoint = endpointId
                _connectionResult.update { ConnectingStatus.ConnectionInitiated(info) }
            }

            override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
                _connectionResult.update { ConnectingStatus.ConnectionResult(result) }
            }

            override fun onDisconnected(endpointId: String) {
                // We've been disconnected from this endpoint. No more data can be
                // sent or received.
                _connectionResult.update { ConnectingStatus.Disconnected }
            }
        }


    fun send(bytes: ByteArray) {
        val payload = Payload.fromBytes(bytes)
        mConnectionsClient
            .sendPayload(connectionEndpoint, payload)
            .addOnFailureListener { e -> Log.d("sendPayload() failed.", e.toString()) }
    }

    private val mPayloadCallback: PayloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            Log.d("onPayloadReceived", "(endpointId=$endpointId, payload=$payload")
            onReceive(endpointId, payload)
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            Log.d("onPayloadTransferUpdate", "endpointId=$endpointId, update=$update")
        }
    }

    fun onReceive(endpointId: String, payload: Payload) {
        if (payload.type == Payload.Type.BYTES && endpointId == connectionEndpoint) {
            CoroutineScope(Dispatchers.IO).launch {
                payload.asBytes()?.let { _receivedBytes.emit(it) }
            }
        }
    }

    fun acceptConnection() {
        mConnectionsClient.acceptConnection(connectionEndpoint, mPayloadCallback)
    }

    fun rejectConnection() {
        mConnectionsClient.rejectConnection(connectionEndpoint)
    }
}