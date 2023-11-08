// The BlockChain class should maintain only limited block nodes to satisfy the functionality.
// You should not have all the blocks added to the block chain in memory 
// as it would cause a memory overflow.

// asg3_1702296_AbdelRahmanAdelAbdelFattah
/*
 * The transaction hash on the raw transaction data is computed using SHA-256.
 *  This technique causes problems for coinbase transactions since the transaction
 * has a variable field called the "coinbase input," which miners can change.
 * 
 * If the hash of the transaction contains the coinbase input,
 *  miners can modify the coinbase transaction until they find a valid hash (nonce) 
 * that satisfies the proof-of-work requirement.
 * 
 * Bitcoin avoids this issue by utilizing the "Merkle root." 
 * Rather than hashing the entire raw transaction data directly
 *  Bitcoin builds a Merkle tree, or hash tree, of all the transactions in a block.
 * This tree's root is located in the block header. The Merkle root is then used in the proof-of-work.
 */

/*
* “I acknowledge that I am aware of the academic integrity guidelines of this course,
* and that I worked on this assignment independently without any unauthorized help with coding or testing.” 
* - عبد الرحمن عادل عبد الفتاح عبد الرؤوف 
*/

import java.sql.Timestamp;
import java.util.ArrayList;

public class BlockChain {
    public static final int CUT_OFF_AGE = 10;
    public static final int MAX_SIZE = 1_000;
    private BlockNode maxHeightNode;
    private TransactionPool txPool = new TransactionPool();
    private ArrayList<BlockNode> blockchain = new ArrayList<>();

    public int getBlockchain() {
        return this.blockchain.size();
    }

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

    public BlockNode getParentNode(byte[] blockHash) {
        ByteArrayWrapper b1 = new ByteArrayWrapper(blockHash);
        for (BlockNode b : blockchain) {
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
        Transaction coinbase = genesisBlock.getCoinbase();
        for (Transaction.Output output : coinbase.getOutputs()) {
            UTXO utxo = new UTXO(coinbase.getHash(), coinbase.getOutputs().indexOf(output));
            utxoPool.addUTXO(utxo, output);
        }
        for (Transaction tx : genesisBlock.getTransactions()) {
            if (tx != null) {
                for (Transaction.Output output : tx.getOutputs()) {
                    UTXO utxo = new UTXO(tx.getHash(), tx.getOutputs().indexOf(output));
                    utxoPool.addUTXO(utxo, output);
                }
                txPool.addTransaction(tx);
            }
        }
        BlockNode genesisNode = new BlockNode(genesisBlock, 1, utxoPool, txPool);
        this.maxHeightNode = genesisNode;
        addInBlockchain(genesisNode);
    }

    private boolean addInBlockchain(BlockNode node) {
        if (this.blockchain.size() == MAX_SIZE) {
            this.blockchain.remove(0);
        }
        return this.blockchain.add(node);
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
        if (block.getPrevBlockHash() == null) {
            return false;
        }
        BlockNode parentNode = getParentNode(block.getPrevBlockHash());
        if (parentNode == null) {
            return false;
        }
        int blockHeight = parentNode.height + 1;
        if (blockHeight <= this.maxHeightNode.height - CUT_OFF_AGE) {
            return false;
        }
        UTXOPool utxoPool = new UTXOPool(parentNode.getUTXOPool());
        TransactionPool txPool = new TransactionPool(parentNode.getTransactionPool());
        for (Transaction tx : block.getTransactions()) {
            TxHandler txHandler = new TxHandler(utxoPool);
            if (!txHandler.isValidTx(tx)) {
                return false;
            }
            // remove used utxo
            for (Transaction.Input input : tx.getInputs()) {
                UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                utxoPool.removeUTXO(utxo);
            }
            // add new utxo
            for (Transaction.Output output : tx.getOutputs()) {
                UTXO utxo = new UTXO(tx.getHash(), tx.getOutputs().indexOf(output));
                utxoPool.addUTXO(utxo, output);
            }
        }
        Transaction coinbase = block.getCoinbase();
        for (Transaction.Output output : coinbase.getOutputs()) {
            UTXO utxo = new UTXO(coinbase.getHash(), coinbase.getOutputs().indexOf(output));
            utxoPool.addUTXO(utxo, output);
        }
        BlockNode newNode = new BlockNode(block, blockHeight, utxoPool, txPool);
        boolean addNewBlock = addInBlockchain(newNode);
        if (addNewBlock) {
            updateMaxHeightNode();
        }
        return addNewBlock;

    }

    public void updateMaxHeightNode() {
        BlockNode currentMaxHeightNode = this.maxHeightNode;
        for (BlockNode b : this.blockchain) {
            if (b.height > currentMaxHeightNode.height) {
                currentMaxHeightNode = b;
            } else if (b.height == currentMaxHeightNode.height) {
                if (currentMaxHeightNode.createdAt.after(b.createdAt)) {
                    currentMaxHeightNode = b;
                }
            }
        }
        this.maxHeightNode = currentMaxHeightNode;
    }

    /** Add a transaction to the transaction pool */
    public void addTransaction(Transaction tx) {
        this.txPool.addTransaction(tx);
    }
}
