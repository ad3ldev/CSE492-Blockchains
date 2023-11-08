import java.util.ArrayList;

// “I acknowledge that I am aware of the academic integrity guidelines of this course,
// and that I worked on this assignment independently without any unauthorized help with coding or testing.” 
// - عبد الرحمن عادل عبد الفتاح عبد الرؤوف 
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

        for (Transaction.Input input : tx.getInputs()) {
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            // (1) all outputs claimed by {@code tx} are in the current UTXO pool
            if (!this.utxoPool.contains(utxo)) {
                return false;
            }
            // (2) the signatures on each input of {@code tx} are valid
            Transaction.Output output = this.utxoPool.getTxOutput(utxo);
            byte[] message = tx.getRawDataToSign(tx.getInputs().indexOf(input));
            if (!Crypto.verifySignature(output.address, message, input.signature)) {
                return false;
            }
            // (3) no UTXO is claimed multiple times by {@code tx}
            if (utxosUsed.contains(utxo)) {
                return false;
            }
            utxosUsed.add(utxo);
            inputSum += output.value;
        }
        // (4) all of {@code tx}s output values are non-negative
        for (Transaction.Output output : tx.getOutputs()) {
            if (output.value < 0) {
                return false;
            }
            outputSum += output.value;
        }
        // (5) the sum of {@code tx}s input values is greater than or equal to the sum
        // of its output
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
        ArrayList<Transaction> validTxs = new ArrayList<>();
        for (Transaction tx : possibleTxs) {
            if (isValidTx(tx)) {
                validTxs.add(tx);
                for (Transaction.Input input : tx.getInputs()) {
                    UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                    this.utxoPool.removeUTXO(utxo);
                }
                for (Transaction.Output output : tx.getOutputs()) {
                    UTXO utxo = new UTXO(tx.getHash(), tx.getOutputs().indexOf(output));
                    this.utxoPool.addUTXO(utxo, output);
                }
            }
        }
        return validTxs.toArray(new Transaction[validTxs.size()]);
    }
}
