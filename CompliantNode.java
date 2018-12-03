import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {

    private boolean[] node_followees;
    private Set<Transaction> node_pendingTransactions;

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
        // IMPLEMENT THIS
    }

    public void setFollowees(boolean[] followees) {
        // IMPLEMENT THIS
        //Set the followees of the node
        node_followees = followees;

    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        //Set the transactions that the node has heard
        node_pendingTransactions = new HashSet<>(pendingTransactions);
       // for(Transaction tx : pendingTransactions) {
        //    System.out.println("id=" + tx.id);
       //     node_pendingTransactions.add(tx);
        //}
    }


    public Set<Transaction> sendToFollowers() {
        // IMPLEMENT THIS
        return node_pendingTransactions;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
        // IMPLEMENT THIS
        for(Candidate cd : candidates){
            if(!node_pendingTransactions.contains(cd.tx)){
                node_pendingTransactions.add(cd.tx);
            }
        }
    }
}
