import java.util.ArrayList;

public class TxHandler {

    private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent
     * transaction outputs) is
     * {@code utxoPool}.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     *         (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     *         (2) the signatures on each input of {@code tx} are valid,
     *         (3) no UTXO is claimed multiple times by {@code tx},
     *         (4) all of {@code tx}s output values are non-negative, and
     *         (5) the sum of {@code tx}s input values is greater than or equal to
     *         the sum of its output
     *         values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        double inputSum = 0;
        double outputSum = 0;
        ArrayList<UTXO> utxosUsed = new ArrayList<UTXO>();

        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input input = tx.getInput(i);
            int outputIndex = input.outputIndex;
            byte[] prevTxHash = input.prevTxHash;
            byte[] signature = input.signature;

            UTXO utxo = new UTXO(prevTxHash, outputIndex);
            // Check if utxo is in the current UTXO pool
            if (!this.utxoPool.contains(utxo)) {
                return false;
            }
            // Check if input signature is valid
            Transaction.Output output = this.utxoPool.getTxOutput(utxo);
            byte[] message = tx.getRawDataToSign(i);
            if (!Crypto.verifySignature(output.address, message, signature)) {
                return false;
            }
            // check if utxo is claimed multiple times
            if (utxosUsed.contains(utxo)) {
                return false;
            }
            utxosUsed.add(utxo);
            inputSum += output.value;
        }
        // check if output value is non-negative
        for (int i = 0; i < tx.numOutputs(); i++) {
            Transaction.Output output = tx.getOutput(i);
            if (output.value < 0) {
                return false;
            }
            outputSum += output.value;
        }
        return inputSum >= outputSum;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions,
     * checking each
     * transaction for correctness, returning a mutually valid array of accepted
     * transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        ArrayList<Transaction> validTxs = new ArrayList<Transaction>();
        for (Transaction tx : possibleTxs) {
            if (isValidTx(tx)) {
                validTxs.add(tx);
                for (Transaction.Input input : tx.getInputs()) {
                    UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                    this.utxoPool.removeUTXO(utxo);
                }
                for (int i = 0; i < tx.numOutputs(); i++) {
                    Transaction.Output output = tx.getOutput(i);
                    UTXO utxo = new UTXO(tx.getHash(), i);
                    this.utxoPool.addUTXO(utxo, output);
                }
            }
        }
        return validTxs.toArray(new Transaction[validTxs.size()]);
    }
}
