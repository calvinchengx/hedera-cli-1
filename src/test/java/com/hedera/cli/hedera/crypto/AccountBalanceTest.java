package com.hedera.cli.hedera.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hedera.cli.hedera.Hedera;
import com.hedera.cli.models.AccountManager;
import com.hedera.cli.shell.ShellHelper;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.account.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.account.AccountId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AccountBalanceTest {

    @InjectMocks
    private AccountBalance accountBalance;

    @Mock
    private ShellHelper shellHelper;

    @Mock
    private AccountManager accountManager;

    @Mock
    private Hedera hedera;

    // test data
    private Client client;
    private String accountId;

    @BeforeEach
    public void setUp() {
        client = mock(Client.class);
        accountId = "0.0.1121";
    }

    @Test
    public void autoWiredDependenciesNotNull() {
        accountManager = accountBalance.getAccountManager();
        assertNotNull(accountManager);

        shellHelper = accountBalance.getShellHelper();
        assertNotNull(shellHelper);

        accountBalance.setAccountIdInString("0.0.1111");
        String accountIdInString = accountBalance.getAccountIdInString();
        assertEquals("0.0.1111", accountIdInString);
    }

    @Test
    public void run() throws HederaNetworkException, IllegalArgumentException, HederaStatusException {
        accountBalance.setAccountIdInString(accountId);
        when(accountManager.verifyAccountId(eq(accountId))).thenReturn(accountId);

        AccountBalanceQuery accountBalanceQuery = mock(AccountBalanceQuery.class, Answers.RETURNS_DEEP_STUBS);
        when(hedera.createHederaClient()).thenReturn(client);
        when(accountBalanceQuery.setAccountId(AccountId.fromString(accountId)).execute(client)).thenReturn(new Hbar(1));
        accountBalance.setAccountBalanceQuery(accountBalanceQuery);

        accountBalance.run();

        verify(accountManager, times(1)).verifyAccountId(accountId);

        ArgumentCaptor<String> valueCapture = ArgumentCaptor.forClass(String.class);
        verify(shellHelper).printSuccess(valueCapture.capture());
        String actual = valueCapture.getValue();
        String expected = "Balance: " + new Hbar(1).asTinybar();
        assertEquals(expected, actual);
    }

    @Test
    public void runInvalidAccountId() {
        accountBalance = spy(accountBalance);
        accountBalance.setAccountIdInString("");
        when(accountManager.verifyAccountId(eq(""))).thenReturn(null);

        accountBalance.run();

        // getBalance will not be called because the user supplied ""
        // which is not a valid accountId String
        verify(accountBalance, times(0)).getBalance();
    }
}
