package com.company;

import java.util.*;

//The objective of this class is to see if transactions are valid
public class TxHandler { //Objeto que verifica a validade das classes

    private UTXOPool pool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        pool = utxoPool;
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

        int i = 0;
        double sumOfOutputVals = 0, sumOfInputVals = 0;
        HashSet<UTXO> utxoSet = new HashSet<>();

        for (Transaction.Input input : tx.getInputs()) //enhancedforloop
        {
            //The input of this transaction must be unspent
            UTXO lastUTXO = new UTXO(input.prevTxHash,input.outputIndex);
            Transaction.Output prevTx = pool.getTxOutput(lastUTXO);

            //(1) all outputs claimed by {@code tx} are in the current UTXO pool,
            if (!pool.contains(lastUTXO)){
                //return ThreeState.MAYBE;
                return false;
            }

            //(2) the signatures on each input of {@code tx} are valid,
            if(input.signature == null || !Crypto.verifySignature(prevTx.address ,tx.getRawDataToSign(i),input.signature)){
                return false;
            }

            utxoSet.add(lastUTXO);

            sumOfInputVals += prevTx.value;
            i++;
        }

        //(3) no UTXO is claimed multiple times by {@code tx},
        if(utxoSet.size() < tx.getInputs().size()){
            return false;
        }

        //(4) all of {@code tx}s output values are non-negative
        for (Transaction.Output output : tx.getOutputs()){
            sumOfOutputVals += output.value;
            if(output.value < 0) {
                return false;
            }
        }

        //(5) the sum of {@code tx}s input values is greater than or equal to the sum of its output values; and false otherwise.
        if (sumOfInputVals < sumOfOutputVals){
            return false;
        }

        //System.out.println(i);

        return true;
    }

    //Compute the fee of a transaction
    private double calcTxFees(Transaction tx){
        double sumInputs = 0;
        double sumOutputs = 0;
        for (Transaction.Input in : tx.getInputs()) {
            UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
            if (!pool.contains(utxo) || !isValidTx(tx)) continue;
            Transaction.Output txOutput = pool.getTxOutput(utxo);
            sumInputs += txOutput.value;
        }
        for (Transaction.Output out : tx.getOutputs()) {
            sumOutputs += out.value;
        }
        return sumInputs - sumOutputs;
    }


    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */

    // This is the job of a blockchain miner
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // Compute the fees
        double TotalFee = 0;

        // IMPLEMENT THIS
        Set<Transaction> vTxs = new HashSet<>();

        //Set<Transaction> indepTxs = new HashSet<>();
        Set<Transaction> depTxs = new HashSet<>();

        for (Transaction tx:possibleTxs){
            if(isValidTx(tx)){
                //vTxs.add(tx);
                vTxs.add(tx);

                TotalFee = TotalFee +  calcTxFees(tx);

                //If the transaction is valid remove the correspondent UTXO
                for (Transaction.Input input : tx.getInputs()){
                    UTXO rem_UTXO = new UTXO(input.prevTxHash, input.outputIndex);
                    pool.removeUTXO(rem_UTXO);
                }

                //Create new UTXO
                int i = 0;
                for (Transaction.Output output : tx.getOutputs()){
                    UTXO add_UTXO = new UTXO(tx.getHash(), i);
                    pool.addUTXO(add_UTXO, tx.getOutput(i));
                    i++;
                }
            } else { //There is the possibility it is a dependent transaction
                depTxs.add(tx);
            }
        }

        for (Transaction tx:depTxs){
            if(isValidTx(tx)){
                vTxs.add(tx);

                TotalFee = TotalFee + calcTxFees(tx);

                //If the transaction is valid remove the correspondent UTXO
                for (Transaction.Input input : tx.getInputs()){
                    UTXO rem_UTXO = new UTXO(input.prevTxHash, input.outputIndex);
                    pool.removeUTXO(rem_UTXO);
                }

                //Create new UTXO
                int i = 0;
                for (Transaction.Output output : tx.getOutputs()){
                    UTXO add_UTXO = new UTXO(tx.getHash(), i);
                    pool.addUTXO(add_UTXO, tx.getOutput(i));
                    i++;
                }
            }
        }

        System.out.println(TotalFee);
        Transaction[] vTxArr = new Transaction[vTxs.size()];
        return vTxs.toArray(vTxArr);
    }

}
