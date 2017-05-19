import java.util.*;
import java.security.PublicKey;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    UTXOPool current;
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        current=utxoPool;
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS

        int i;
        double sum=0;
        ArrayList<UTXO> seen=new ArrayList<UTXO>();
        for(i=0;i<tx.numInputs();i++)
        {
            Transaction.Input inp=tx.getInput(i);
            UTXO curr=new UTXO(inp.prevTxHash,inp.outputIndex);
            if(seen.contains(curr))
            {
                return false;
            }
            if(current.contains(curr)==false)
            {
                return false;
            }
            Transaction.Output ot=current.getTxOutput(curr);
            if(Crypto.verifySignature(ot.address,tx.getRawDataToSign(i),inp.signature)==false)
            {
                return false;
            }
            seen.add(curr);
            sum+=ot.value;

        } 
        double sum1=0;
        for(Transaction.Output o:tx.getOutputs())
        {
            if(o.value<0)
            {
                return false;
            }
            sum1+=o.value;
        }
        if(sum1>sum)
        {
            return false;
        }
        
        return true;
       
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        Set<Transaction> good=new HashSet<Transaction>();
        for(Transaction t:possibleTxs)
        {
            if(isValidTx(t))
            {
                good.add(t);
                for (Transaction.Input in:t.getInputs())
                {
                    current.removeUTXO(new UTXO(in.prevTxHash,in.outputIndex));
                }
                int num=0;
                for(num=0;num<t.numOutputs();num++)
                {
                    Transaction.Output out=t.getOutput(num);
                    current.addUTXO(new UTXO(t.getHash(),num),out);
                }
            }
        }
        Transaction[] returning =new Transaction[good.size()];
        return good.toArray(returning);

       

       
    }


}
