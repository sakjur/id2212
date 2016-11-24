package se.kth.id2212.ex2.bankrmi;

public class RejectedException extends Exception {
    private static long serialVersionUID = -314439670131687936L;
    public RejectedException(String e) {
        super(e); 
    }
}
