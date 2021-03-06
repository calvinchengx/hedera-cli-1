package com.hedera.cli.hedera.crypto;

import java.io.File;
import java.util.Map;

import com.hedera.cli.config.InputReader;
import com.hedera.cli.hedera.Hedera;
import com.hedera.cli.models.AddressBookManager;
import com.hedera.cli.models.DataDirectory;
import com.hedera.cli.services.CurrentAccountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import lombok.Getter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

// @formatter:off
@Getter
@Component
@Command(name = "use", 
        separator = " ", 
        description = "@|fg(225) Switch to use a specific Hedera account as operator.|@",
        helpCommand = true) // @formatter:on
public class AccountUse implements Runnable, Operation {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private Hedera hedera;

    @Autowired
    private DataDirectory dataDirectory;

    @Parameters(index = "0", description = "Hedera account in the format shardNum.realmNum.accountNum"
            + "%n@|bold,underline Usage:|@%n" + "@|fg(yellow) account use 0.0.1003|@")
    private String accountId;

    @Override
    public void run() {
        boolean exists = accountIdExistsInIndex(dataDirectory, accountId);
        if (exists) {
            // since this accountId exists, we set it into our CurrentAccountService
            // singleton and ensure that we know that this account is only for the specified network
            CurrentAccountService currentAccountService = (CurrentAccountService) context.getBean("currentAccount",
                    CurrentAccountService.class);
            AddressBookManager addressBookManager = hedera.getAddressBookManager();
            String network = addressBookManager.getCurrentNetworkAsString();
            currentAccountService.setNetwork(network);
            currentAccountService.setAccountNumber(accountId);
        } else {
            System.out.println("This account does not exist, please add new account using `account recovery`");
        }
    }

    /**
     * 
     * @param dataDirectory
     * @return boolean accountIdExists
     */
    private boolean accountIdExistsInIndex(DataDirectory dataDirectory, String accountId) {
        String networkName = dataDirectory.readFile("network.txt");
        String pathToAccountsFolder = networkName + File.separator + "accounts" + File.separator;
        String pathToIndexTxt = pathToAccountsFolder + "index.txt";
        Map<String, String> readingIndexAccount = dataDirectory.readIndexToHashmap(pathToIndexTxt);
        for (Object key : readingIndexAccount.keySet()) {
            if (accountId.equals(key.toString())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void executeSubCommand(InputReader inputReader, String... args) {
        if (args.length == 0) {
            CommandLine.usage(this, System.out);
        } else {
            try {
                new CommandLine(this).execute(args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
