package npo.kib.odc_demo.feature_app.domain.transaction_logic

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import npo.kib.odc_demo.feature_app.domain.model.serialization.serializable.data_packet.DataPacketType
import npo.kib.odc_demo.feature_app.domain.model.serialization.serializable.data_packet.variants.*
import npo.kib.odc_demo.feature_app.domain.model.serialization.serializable.data_packet.variants.TransactionResult.ResultType
import npo.kib.odc_demo.feature_app.domain.repository.WalletRepository
import npo.kib.odc_demo.feature_app.domain.transaction_logic.TransactionSteps.TransactionRole
import npo.kib.odc_demo.feature_app.domain.util.cancelChildren

abstract class TransactionController(
    protected val scope: CoroutineScope,
    protected val walletRepository: WalletRepository,
    val role: TransactionRole
) {

    abstract val currentStep: StateFlow<TransactionSteps>

    protected val _transactionDataBuffer: MutableStateFlow<TransactionDataBuffer> =
        MutableStateFlow(
            TransactionDataBuffer()
        )

    val transactionDataBuffer: StateFlow<TransactionDataBuffer> =
        _transactionDataBuffer.asStateFlow()

    protected val _errors = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val errors: SharedFlow<String> = _errors.asSharedFlow()

    //todo change to nullable ?
    protected lateinit var outputDataPacketChannel: Channel<DataPacketVariant>
        private set
    val outputDataPacketFlow: Flow<DataPacketVariant>
        get() = outputDataPacketChannel.consumeAsFlow()

    lateinit var receivedPacketsChannel: Channel<DataPacketVariant>
        private set
    protected val receivedPacketsFlow: Flow<DataPacketVariant>
        get() = receivedPacketsChannel.consumeAsFlow()


    private var started: Boolean = false

    /** Initializes controller
     * @return **false** if already started, else **true** */
    protected open fun initController(): Boolean {
        return if (!started) {
            receivedPacketsChannel = Channel(capacity = UNLIMITED)
            outputDataPacketChannel = Channel(capacity = UNLIMITED)
            //instantly upon the initialization UserInfo is added to the queue as the first packet to be sent
            scope.launch {
                val userInfo = walletRepository.getLocalUserInfo()
                updateLocalUserInfo(userInfo)
                outputDataPacketChannel.send(userInfo)
            }
            started = true
            true
        } else false
    }

    fun resetController(): Boolean {
        return if (started) {
            //reset and clear all channels with .cancel() and to restart assign new channel instances to properties
            // old channels with no references will be GC'd
            receivedPacketsChannel.cancel()
            outputDataPacketChannel.cancel()
            _transactionDataBuffer.update { TransactionDataBuffer() }
            started = false
            scope.cancelChildren()
            true
        } else false
    }


    suspend fun sendUserInfo(userInfo: UserInfo) {
        if (started) outputDataPacketChannel.send(userInfo)
    }

    protected fun updateOtherUserInfo(userInfo: UserInfo) {
        if (started) _transactionDataBuffer.update {
            it.copy(otherUserInfo = userInfo)
        }
    }

    fun updateLocalUserInfo(userInfo: UserInfo) =
        _transactionDataBuffer.update { it.copy(thisUserInfo = userInfo) }

    protected suspend fun sendPositiveResult() {
        if (started) outputDataPacketChannel.send(TransactionResult(ResultType.Success))
    }

    protected suspend fun sendNegativeResult(message: String? = null) {
        if (started) outputDataPacketChannel.send(TransactionResult(ResultType.Failure(message)))
    }

    fun updateAmountRequest(amountRequest: AmountRequest?) {
        _transactionDataBuffer.update { it.copy(amountRequest = amountRequest) }
    }

    fun updateBanknotesList(list: BanknotesList) =
        _transactionDataBuffer.update { it.copy(banknotesList = list) }


    fun updateLastAcceptanceBLocks(blocks: AcceptanceBlocks) =
        _transactionDataBuffer.update { it.copy(lastAcceptanceBlocks = blocks) }

    fun updateLastSignedBLock(block: Block) =
        _transactionDataBuffer.update { it.copy(lastSignedBlock = block) }



    protected fun DataPacketVariant.requireToBeOfTypes(vararg expectedTypes: DataPacketType) {
        if (!expectedTypes.contains(packetType)) throw WrongPacketTypeReceived(
            "Expected packet in ${expectedTypes.contentToString()} but received $packetType"
        )
    }

    class TransactionException(message: String?) : Exception(message)
    class WrongPacketTypeReceived(
        message: String? = null,
        expectedPacketType: DataPacketType? = null,
        receivedPacketType: DataPacketType? = null
    ) : Exception(
        message ?: """Unexpected packet type received. 
        |Expected packet type: $expectedPacketType 
        |Received packet type: $receivedPacketType""".trimMargin()
    )
}