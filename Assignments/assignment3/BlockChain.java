
// The BlockChain class should maintain only limited block nodes to satisfy the functionality.
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.
import java.util.ArrayList;
import java.sql.Timestamp;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    private TransactionPool txPool = new TransactionPool();
    private ArrayList<BlockNode> blockchain = new ArrayList<>();
    private BlockNode maxHeightNode;

    public class BlockNode {
        private Block block;
        private int height = 0;
        private UTXOPool utxoPool = new UTXOPool();
        private TransactionPool txPool = new TransactionPool();
        private Timestamp createdAt;

        public BlockNode(Block block, int height, UTXOPool utxoPool, TransactionPool txPool) {
            this.block = block;
            this.height = height;
            this.utxoPool = utxoPool;
            this.txPool = txPool;
            this.createdAt = new Timestamp(System.currentTimeMillis());
        }

        public UTXOPool getUTXOPool() {
            return this.utxoPool;
        }

        public TransactionPool getTransactionPool() {
            return this.txPool;
        }
    }

    public void updateMaxHeightNode() {
        BlockNode currentMaxNode = this.maxHeightNode;
        for (BlockNode b : blockchain) {
            if (b.height > currentMaxNode.height) {
                currentMaxNode = b;
            } else if (b.height == currentMaxNode.height) {
                if (currentMaxNode.createdAt.after(b.createdAt)) {
                    currentMaxNode = b;
                }
            }
        }
        maxHeightNode = currentMaxNode;
    }

    public BlockNode getParentNode(byte[] blockHash) {
        ByteArrayWrapper b1 = new ByteArrayWrapper(blockHash);
        for (BlockNode b : this.blockchain) {
            ByteArrayWrapper b2 = new ByteArrayWrapper(b.block.getHash());
            if (b1.equals(b2)) {
                return b;
            }
        }
        return null;
    }

    /**
     * create an empty blockchain with just a genesis block. Assume
     * {@code genesisBlock} is a valid
     * block
     */
    public BlockChain(Block genesisBlock) {
        UTXOPool utxoPool = new UTXOPool();
        TransactionPool txPool = new TransactionPool();
        Transaction coinBase = genesisBlock.getCoinbase();
        int numOutputs = coinBase.numInputs();
        for (int i = 0; i < numOutputs; i++) {
            byte[] hash = coinBase.getHash();
            Transaction.Output output = coinBase.getOutput(i);
            UTXO utxo = new UTXO(hash, i);
            utxoPool.addUTXO(utxo, output);
        }
        txPool.addTransaction(coinBase);
        ArrayList<Transaction> transactions = genesisBlock.getTransactions();
        for (Transaction tx : transactions) {
            if (tx != null) {
                for (int i = 0; i < tx.numInputs(); i++) {
                    Transaction.Output output = tx.getOutput(i);
                    UTXO utxo = new UTXO(tx.getHash(), i);
                    utxoPool.addUTXO(utxo, output);
                }
                txPool.addTransaction(tx);
            }
        }
        BlockNode b = new BlockNode(genesisBlock, 1, utxoPool, txPool);
        blockchain.add(b);
    }

    /** Get the maximum height block */
    public Block getMaxHeightBlock() {
        return this.maxHeightNode.block;
    }

    /** Get the UTXOPool for mining a new block on top of max height block */
    public UTXOPool getMaxHeightUTXOPool() {
        return this.maxHeightNode.utxoPool;
    }

    /** Get the transaction pool to mine a new block */
    public TransactionPool getTransactionPool() {
        return this.txPool;
    }

    /**
     * Add {@code block} to the blockchain if it is valid. For validity, all
     * transactions should be
     * valid and block should be at {@code height > (maxHeight - CUT_OFF_AGE)},
     * where maxHeight is
     * the current height of the blockchain.
     * <p>
     * Assume the Genesis block is at height 1.
     * For example, you can try creating a new block over the genesis block (i.e.
     * create a block at
     * height 2) if the current blockchain height is less than or equal to
     * CUT_OFF_AGE + 1. As soon as
     * the current blockchain height exceeds CUT_OFF_AGE + 1, you cannot create a
     * new block at height 2.
     * 
     * @return true if block is successfully added
     */
    public boolean addBlock(Block block) {
        byte[] prevBlockHash = block.getPrevBlockHash();
        if (prevBlockHash == null) {
            return false;
        }
        BlockNode parentNode = getParentNode(prevBlockHash);
        if (parentNode == null) {
            return false;
        }
        int blockHeight = parentNode.height + 1;
        if (blockHeight <= maxHeightNode.height - CUT_OFF_AGE) {
            return false;
        }
        UTXOPool parenUtxoPool = parentNode.getUTXOPool();
        TransactionPool parentTxPool = parentNode.getTransactionPool();
        ArrayList<Transaction> blockTransactions = block.getTransactions();

        UTXOPool utxoPool = new UTXOPool(parenUtxoPool);
        TransactionPool txPool = new TransactionPool(parentTxPool);

        for (Transaction tx : blockTransactions) {
            TxHandler txHandler = new TxHandler(utxoPool);
            if (!txHandler.isValidTx(tx)) {
                return false;
            }
            for (Transaction.Input input : tx.getInputs()) {
                int outputIndex = input.outputIndex;
                byte[] prevTxHash = input.prevTxHash;
                UTXO utxo = new UTXO(prevTxHash, outputIndex);
                utxoPool.removeUTXO(utxo);
            }
            // add new utxo
            byte[] hash = tx.getHash();
            for (int i = 0; i < tx.numOutputs(); i++) {
                UTXO utxo = new UTXO(hash, i);
                utxoPool.addUTXO(utxo, tx.getOutput(i));
            }
        }
        for (int i = 0; i < block.getCoinbase().numOutputs(); i++) {
            utxoPool.addUTXO(new UTXO(block.getCoinbase().getHash(), i), block.getCoinbase().getOutput(i));
        }

        // remove trans pool
        for (Transaction t : blockTransactions) {
            txPool.removeTransaction(t.getHash());
        }

        // add new block
        BlockNode b = new BlockNode(block, blockHeight, utxoPool, txPool);
        boolean addNewBlock = blockchain.add(b);
        if (addNewBlock) {
            updateMaxHeightNode();
        }
        return addNewBlock;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        this.txPool.addTransaction(tx);
    }
}