package interfaces;


public interface Payable {
    void pay(double amount) throws Exception;
    double getBalance();
}