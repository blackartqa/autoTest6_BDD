package ru.netology.test;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.data.DataHelper;
import ru.netology.page.DashboardPage;
import ru.netology.page.LoginPage;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MoneyTransferTest {

    @BeforeEach
    void setUp() {
        open("http://localhost:9999");
    }

    private DashboardPage validAuthorization() {
        val loginPage = new LoginPage();
        val authInfo = DataHelper.getAuthInfo();
        val verificationPage = loginPage.validLogin(authInfo);
        val verificationCode = DataHelper.getVerificationCodeFor(authInfo);
        return verificationPage.validVerify(verificationCode);
    }

    @Test
    void shouldFailToAuthorizeWithInvalidAuthData() {
        val loginPage = new LoginPage();
        val badAuthInfo = DataHelper.getOtherAuthInfo(DataHelper.getAuthInfo());
        loginPage.invalidLogin(badAuthInfo);
    }

    @Test
    void shouldFailToAuthorizeWithInvalidVerificationCode() {
        val loginPage = new LoginPage();
        val authInfo = DataHelper.getAuthInfo();
        val verificationPage = loginPage.validLogin(authInfo);
        val badVerificationCode = DataHelper.getOtherVerificationCodeFor(authInfo);
        verificationPage.invalidVerify(badVerificationCode);
    }

    @Test
    void shouldFailToTransferMoneyFromFirstCardToSecondIfOutOfLimit() {
        val dashboardPage = validAuthorization();
        val cardPlusFull = DataHelper.findCardPlus();
        val cardMinusFull = DataHelper.findCardMinus(cardPlusFull);
        val initialBalanceCardPlus = dashboardPage.getCardsBalance(DataHelper.getLastDigits(cardPlusFull));
        val initialBalanceCardMinus = dashboardPage.getCardsBalance(DataHelper.getLastDigits(cardMinusFull));
        val uploadAmount = DataHelper.generateTransferAmountOutLimit(initialBalanceCardMinus);
        val uploadPage = dashboardPage.moneyTransferButton(DataHelper.getLastDigits(cardPlusFull));
        uploadPage.shouldErrorTransferAmountIsOutOfLimit(uploadAmount, cardMinusFull);
        setUp();
        val dashboardPage2 = validAuthorization();
        val actualBalanceCardPlus = dashboardPage2.getCardsBalance(DataHelper.getLastDigits(cardPlusFull));
        val actualBalanceCardMinus = dashboardPage2.getCardsBalance(DataHelper.getLastDigits(cardMinusFull));
        assertEquals(initialBalanceCardPlus, actualBalanceCardPlus);
        assertEquals(initialBalanceCardMinus, actualBalanceCardMinus);
    }

    @Test
    void shouldTransferMoneyBetweenRandomCardsWithinLimit() {
        val dashboardPage = validAuthorization();
        val cardPlusFull = DataHelper.findCardPlus();
        val cardMinusFull = DataHelper.findCardMinus(cardPlusFull);

        val initialBalanceCardPlus = dashboardPage.getCardsBalance(DataHelper.getLastDigits(cardPlusFull));
        val initialBalanceCardMinus = dashboardPage.getCardsBalance(DataHelper.getLastDigits(cardMinusFull));
        val uploadAmount = DataHelper.generateTransferAmountWithinLimit(initialBalanceCardMinus);
        val uploadPage = dashboardPage.moneyTransferButton(DataHelper.getLastDigits(cardPlusFull));
        val dashboardPage2 = uploadPage.shouldTransferBetweenCards(uploadAmount, cardMinusFull);
        val actualBalanceCardPlus = dashboardPage2.getCardsBalance(DataHelper.getLastDigits(cardPlusFull));
        val actualBalanceCardMinus = dashboardPage2.getCardsBalance(DataHelper.getLastDigits(cardMinusFull));
        assertEquals(initialBalanceCardPlus + uploadAmount, actualBalanceCardPlus);
        assertEquals(initialBalanceCardMinus - uploadAmount, actualBalanceCardMinus);

        val uploadPage2 = dashboardPage2.moneyTransferButton(DataHelper.getLastDigits(cardMinusFull));
        val dashboardPage3 = uploadPage2.shouldTransferBetweenCards(uploadAmount, cardPlusFull);
        assertEquals(initialBalanceCardPlus, dashboardPage3.getCardsBalance(DataHelper.getLastDigits(cardPlusFull)));
        assertEquals(initialBalanceCardMinus, dashboardPage3.getCardsBalance(DataHelper.getLastDigits(cardMinusFull)));
    }


//    @Test
//    void shouldTransferMoneyBetweenOwnCardsV1() {
//        open("http://localhost:9999");
//        var loginPage = new LoginPage();
//        var authInfo = DataHelper.getAuthInfo();
//        var verificationPage = loginPage.validLogin(authInfo);
//        var verificationInfo = DataHelper.getVerificationCodeFor(authInfo);
//        verificationPage.validVerify(verificationInfo);
//
//    }

}
