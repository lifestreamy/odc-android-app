package npo.kib.odc_demo.feature_app.domain.use_cases

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import npo.kib.odc_demo.common.core.models.AcceptanceBlocks
import npo.kib.odc_demo.common.core.models.Block
import npo.kib.odc_demo.feature_app.data.p2p.connection_util.ObjectSerializer.toByteArray
import npo.kib.odc_demo.feature_app.data.p2p.connection_util.ObjectSerializer.toPayloadContainer
import npo.kib.odc_demo.feature_app.domain.model.serialization.serializable.AmountRequest
import npo.kib.odc_demo.feature_app.domain.model.serialization.serializable.BanknoteWithBlockchain
import npo.kib.odc_demo.feature_app.domain.model.serialization.serializable.PayloadContainer
import npo.kib.odc_demo.feature_app.domain.p2p.P2PConnection
import npo.kib.odc_demo.feature_app.domain.repository.WalletRepository

class P2PSendUseCase(
    override val walletRepository: WalletRepository,
    override val p2pConnection: P2PConnection,
) : P2PBaseUseCase() {

    /**
     * Sending banknotes to another device
     * @param amount Amount to send
     */
    suspend fun sendBanknotes(amount: Int) {
        // Шаг 1

        val blockchainArray = getBanknotesByAmount(amount)

        for (blockchainFromDB in blockchainArray) {
            sendingList.add(blockchainFromDB)
        }

        // Отправка суммы и первой банкноты
        p2pConnection.sendBytes(PayloadContainer(amount = amount).toByteArray())
        sendBanknoteWithBlockchain(sendingList.poll())

        for (i in 0 until blockchainArray.size) {
            // Ждем выполнения шагов 2-4
            val bytes = p2pConnection.receivedBytes.take(1).first()
            val container = bytes.toPayloadContainer()
            if (container.blocks == null) {
                return
            }

            //Шаг 5
            onAcceptanceBlocksReceived(container.blocks)
        }
    }

    override suspend fun onBytesReceive(container: PayloadContainer) = Unit
//    override suspend fun onBytesReceive(container: PayloadContainer) {
//        // Случай, когда другой юзер запросил у нас купюры
//        if (container.amountRequest != null) {
//            onAmountRequest(container.amountRequest)
//            return
//        }
//    }

    private suspend fun getBanknotesByAmount(requiredAmount: Int): ArrayList<BanknoteWithBlockchain> {
        //TODO обработать ситуацию, когда не хватает банкнот для выдачи точной суммы
        var amount = requiredAmount
        val banknoteAmounts = banknotesDao.getBnidsAndAmounts().toCollection(ArrayList())
        banknoteAmounts.sortByDescending { it.amount }

        val blockchainsList = ArrayList<BanknoteWithBlockchain>()
        for (banknoteAmount in banknoteAmounts) {
            if (amount < banknoteAmount.amount) {
                continue
            }

            blockchainsList.add(
                BanknoteWithBlockchain(
                    banknotesDao.getBlockchainByBnid(banknoteAmount.bnid),
                    blockDao.getBlocksByBnid(banknoteAmount.bnid)
                )
            )

            amount -= banknoteAmount.amount
            if (amount <= 0) break
        }

        return blockchainsList
    }

    private suspend fun sendBanknoteWithBlockchain(banknoteWithBlockchain: BanknoteWithBlockchain?) {
        if (banknoteWithBlockchain == null) {
//            _isSendingFlow.update { false }
            return
        }

        //Создание нового ProtectedBlock
        val newProtectedBlock =
            wallet.initProtectedBlock(banknoteWithBlockchain.banknoteWithProtectedBlock.protectedBlock)
        banknoteWithBlockchain.banknoteWithProtectedBlock.protectedBlock = newProtectedBlock

        val payloadContainer = PayloadContainer(banknoteWithBlockchain = banknoteWithBlockchain)
        val blockchainJson = payloadContainer.toByteArray()
        p2pConnection.sendBytes(blockchainJson)

        //Запоминаем отправленный parentBlock для последующей верификации
        sentBlock = banknoteWithBlockchain.blocks.last()
    }


    //Шаг 1
    private suspend fun onAmountRequest(amountRequest: AmountRequest) {
        val requiredAmount = amountRequest.amount
        val currentAmount = walletRepository.getStoredInWalletSum() ?: 0
        if (requiredAmount <= currentAmount) {
            _amountRequestFlow.update { amountRequest }
        } else {
            sendRejection()
        }
    }

    //Шаг 5
    private suspend fun onAcceptanceBlocksReceived(acceptanceBlocks: AcceptanceBlocks) {
        val childBlockFull = wallet.signature(
            sentBlock, acceptanceBlocks.childBlock, acceptanceBlocks.protectedBlock
        )
        banknotesDao.deleteByBnid(acceptanceBlocks.childBlock.bnid)
        sendChildBlockFull(childBlockFull)
        sendBanknoteWithBlockchain(sendingList.poll())
    }

    private suspend fun sendChildBlockFull(childBlock: Block) {
        val payloadContainer = PayloadContainer(childFull = childBlock)
        val blockchainJson = payloadContainer.toByteArray()
        p2pConnection.sendBytes(blockchainJson)
    }
}