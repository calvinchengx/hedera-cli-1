package com.hedera.cli.hedera.crypto;

import com.hedera.cli.hedera.Hedera;
import com.hedera.cli.shell.ShellHelper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
public class ValidateTransferList {

    @Autowired
    private Hedera hedera;

    @Autowired
    private ShellHelper shellHelper;

    @Autowired
    private ValidateAccounts validateAccounts;

    @Autowired
    private ValidateAmount validateAmount;

    private List<String> amountList;
    private boolean isTiny;

    public void updateAmountList(long sumOfRecipientAmount) {
        this.amountList = finalAmountList(amountList, sumOfRecipientAmount);
        System.out.println("updateAmountList this.amountlist: " + this.amountList);
    }

    public long sumOfAmountList() {
        System.out.println("sumOfRecipientAmountList: " + amountList);
        System.out.println("sumOfRecipientAmountList: " + isTiny);
        long sumOfAmount;
        if (isTiny) {
            sumOfAmount = validateAmount.sumOfTinybarsInLong(amountList);
        } else {
            sumOfAmount = validateAmount.sumOfHbarsInLong(amountList);
        }
        return sumOfAmount;
    }

    public List<String> finalAmountList(List<String> amountList, long sumOfRecipientsAmount) {
        ArrayList<String> finalAmountList = new ArrayList<>(amountList);
        String amount = "-" + sumOfRecipientsAmount;
        finalAmountList.add(0, amount);
        System.out.println("finalAmountList ???");
        System.out.println(finalAmountList);
        return finalAmountList;
    }

    public boolean verifyAmountList(List<String> senderList, List<String> recipientList, List<String> amountList) {
        System.out.println("aaa");
        boolean amountListVerified = false;
        int amountSize = amountList.size();
        System.out.println("bbb " +senderList);
        System.out.println("bbb " +recipientList);
        int transferSize = senderList.size() + recipientList.size();
        this.amountList = amountList;
        System.out.println("ccc");
        this.isTiny = validateAmount.isTiny();
        System.out.println("ddd");
        switch (senderList.size()) {
            case 1:
                if (validateAccounts.senderListHasOperator()) {
                    shellHelper.print("Sender list contains operator");
                    if (amountSize != transferSize) {
                        // add recipients amount and add to amount list
                        long sumOfRecipientAmount = sumOfAmountList();
                        updateAmountList(sumOfRecipientAmount);
                        long sumOfTransferAmount = sumOfAmountList();
                        if (validateAmount.verifyZeroSum(sumOfTransferAmount)) {
                            amountListVerified = true;
                        }
                    } else {
                        // assume amount already contains sender's amount
                        long sumOfTransferAmount = sumOfAmountList();
                        if (validateAmount.verifyZeroSum(sumOfTransferAmount)) {
                            amountListVerified = true;
                        }
                    }
                } else {
                    shellHelper.print("Sender list does not contain operator");
                    if (amountSize != transferSize) {
                        shellHelper.printError("Invalid transfer list. Your transfer list must sum up to 0");
                    } else {
                        // assume amount already contains sender's amount
                        long sumOfTransferAmount = sumOfAmountList();
                        if (validateAmount.verifyZeroSum(sumOfTransferAmount)) {
                            amountListVerified = true;
                        }
                    }
                }
                break;
            case 2:
                if (validateAccounts.senderListHasOperator()) {
                    shellHelper.print("Sender list contains operator");
                } else {
                    shellHelper.print("Sender list does not contain operator");
                }
                break;
            default:
                break;
        }
        return amountListVerified;
    }


}
