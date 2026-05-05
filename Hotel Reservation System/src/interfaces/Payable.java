package interfaces;

import enums.paymentmethod;

public interface Payable {
    void pay(double amount, paymentmethod method) throws Exception;
    double getBalance();
}
