/*
 * Copyright (c) 2020 ForgeRock. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package org.forgerock.android.auth;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.forgerock.android.authenticator.Account;
import org.forgerock.android.authenticator.Mechanism;
import org.forgerock.android.authenticator.Notification;
import org.forgerock.android.authenticator.Oath;
import org.forgerock.android.authenticator.Push;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DefaultStorageClientTest {

    private Context context = ApplicationProvider.getApplicationContext();

    private static final boolean CLEAN_UP_DATA = true;
    private static final String ISSUER = "issuer1";
    private static final String OTHER_ISSUER = "issuer2";
    private static final String ACCOUNT_NAME = "user1";
    private static final String OTHER_ACCOUNT_NAME = "user2";
    private static final String MECHANISM_UID = "b162b325-ebb1-48e0-8ab7-b38cf341da95";
    private static final String OTHER_MECHANISM_UID = "013be51a-8c14-356d-b0fc-b3660cc8a101";
    private static final String SECRET = "JMEZ2W7D462P3JYBDG2HV7PFBM";
    private static final String ALGORITHM = "SHA 256";
    private static final int DIGITS = 6;
    private static final int PERIOD = 30;
    private static final int COUNTER = 0;
    private static final String REGISTRATION_ENDPOINT = "http://openam.forgerock.com:8080/openam/json/push/sns/message?_action=register";
    private static final String OTHER_REGISTRATION_ENDPOINT = "http://develop.openam.forgerock.com:8080/openam/json/push/sns/message?_action=register";
    private static final String AUTHENTICATION_ENDPOINT = "http://openam.forgerock.com:8080/openam/json/push/sns/message?_action=authenticate";
    private static final String OTHER_AUTHENTICATION_ENDPOINT = "http://develop.openam.forgerock.com:8080/openam/json/push/sns/message?_action=authenticate";
    private final String MESSAGE_ID = "AUTHENTICATE:63ca6f18-7cfb-4198-bcd0-ac5041fbbea01583798229441";
    private final String OTHER_MESSAGE_ID = "AUTHENTICATE:07de6f25-8cfb-4198-bcd0-ed4321fbbea0ac33798258645";
    private final String CHALLENGE = "fZl8wu9JBxdRQ7miq3dE0fbF0Bcdd+gRETUbtl6qSuM=";
    private final String AMLB_COOKIE = "ZnJfc3NvX2FtbGJfcHJvZD0wMQ==";
    private final long TTL = 120;

    @After
    public void cleanUp() {
        if(CLEAN_UP_DATA) {
            DefaultStorageClient defaultStorage = new DefaultStorageClient(context);
            defaultStorage.removeAll();
        }
    }

    @Test
    public void testInitialization() {
        DefaultStorageClient defaultStorage = new DefaultStorageClient(context);

        assertNotNull(defaultStorage);
    }

    @Test
    public void testStoreAccount() {
        DefaultStorageClient defaultStorage = new DefaultStorageClient(context);
        Account account = new Account(ISSUER, ACCOUNT_NAME);

        defaultStorage.setAccount(account);

        Account accountFromStorage = defaultStorage.getAccount(account.getId());

        assertNotNull(accountFromStorage);
        assertEquals(account.getIssuer(), accountFromStorage.getIssuer());
        assertEquals(account.getAccountName(), accountFromStorage.getAccountName());
    }

    @Test
    public void testStoreMultipleAccounts() {
        DefaultStorageClient defaultStorage = new DefaultStorageClient(context);
        Account account1 = new Account(ISSUER, ACCOUNT_NAME);
        Account account2 = new Account(OTHER_ISSUER, OTHER_ACCOUNT_NAME);

        defaultStorage.setAccount(account1);
        defaultStorage.setAccount(account2);

        Account account1FromStorage = defaultStorage.getAccount(account1.getId());
        Account account2FromStorage = defaultStorage.getAccount(account2.getId());

        assertNotNull(account1FromStorage);
        assertEquals(account1.getIssuer(), account1FromStorage.getIssuer());
        assertEquals(account1.getAccountName(), account1FromStorage.getAccountName());
        assertNotNull(account2FromStorage);
        assertEquals(account2.getIssuer(), account2FromStorage.getIssuer());
        assertEquals(account2.getAccountName(), account2FromStorage.getAccountName());
    }

    @Test
    public void testNoAccountFound() {
        DefaultStorageClient defaultStorage = new DefaultStorageClient(context);
        Account account1 = new Account(ISSUER, ACCOUNT_NAME);
        Account account2 = new Account(OTHER_ISSUER, OTHER_ACCOUNT_NAME);

        defaultStorage.setAccount(account1);

        Account account1FromStorage = defaultStorage.getAccount(account1.getId());
        Account account2FromStorage = defaultStorage.getAccount(account2.getId());

        assertNotNull(account1FromStorage);
        assertEquals(account1.getIssuer(), account1FromStorage.getIssuer());
        assertEquals(account1.getAccountName(), account1FromStorage.getAccountName());
        assertNull(account2FromStorage);
    }

    @Test
    public void testUpdateExistingAccount() {
        DefaultStorageClient defaultStorage = new DefaultStorageClient(context);

        Account account = new Account(ISSUER, ACCOUNT_NAME);
        defaultStorage.setAccount(account);

        Account accountFromStorage = defaultStorage.getAccount(account.getId());
        assertNotNull(accountFromStorage);
        assertEquals(account.getIssuer(), accountFromStorage.getIssuer());
        assertEquals(account.getAccountName(), accountFromStorage.getAccountName());
        assertNull(accountFromStorage.getImageURL());
        assertNull(accountFromStorage.getBackgroundColor());

        Account updatedAccount = new Account(ISSUER, ACCOUNT_NAME, "http://forgerock.com", "#032b75");
        defaultStorage.setAccount(updatedAccount);

        Account updatedAccountFromStorage = defaultStorage.getAccount(updatedAccount.getId());
        assertNotNull(updatedAccountFromStorage);
        assertEquals(updatedAccount.getIssuer(), updatedAccountFromStorage.getIssuer());
        assertEquals(updatedAccount.getAccountName(), updatedAccountFromStorage.getAccountName());
        assertEquals(updatedAccount.getImageURL(), updatedAccountFromStorage.getImageURL());
        assertEquals(updatedAccount.getBackgroundColor(), updatedAccountFromStorage.getBackgroundColor());
    }

    @Test
    public void testRemoveExistingAccount() {
        DefaultStorageClient defaultStorage = new DefaultStorageClient(context);

        Account account = new Account(ISSUER, ACCOUNT_NAME);
        defaultStorage.setAccount(account);

        Account accountFromStorage = defaultStorage.getAccount(account.getId());

        assertNotNull(accountFromStorage);
        assertEquals(account.getId(), accountFromStorage.getId());
        assertTrue(defaultStorage.removeAccount(accountFromStorage));
        assertNull(defaultStorage.getAccount(accountFromStorage.getId()));
    }

    @Test
    public void testStoreOathMechanism() {
        DefaultStorageClient defaultStorage = new DefaultStorageClient(context);

        Account account = new Account(ISSUER, ACCOUNT_NAME);
        Mechanism mechanism = new Oath(MECHANISM_UID, ISSUER, ACCOUNT_NAME, Mechanism.OATH,
                Oath.TokenType.HOTP, ALGORITHM, SECRET, DIGITS, COUNTER, PERIOD);

        defaultStorage.setAccount(account);
        defaultStorage.setMechanism(mechanism);

        Account accountFromStorage = defaultStorage.getAccount(account.getId());
        List<Mechanism> mechanismsFromStorage = defaultStorage.getMechanismsForAccount(accountFromStorage);

        assertNotNull(accountFromStorage);
        assertEquals(account.getId(), accountFromStorage.getId());
        assertNotNull(mechanismsFromStorage);
        assertEquals(mechanism.getMechanismUID(), mechanismsFromStorage.get(0).getMechanismUID());
    }

    @Test
    public void testStorePushMechanism() {
        DefaultStorageClient defaultStorage = new DefaultStorageClient(context);

        Account account = new Account(ISSUER, ACCOUNT_NAME);
        Mechanism mechanism = new Push(MECHANISM_UID, ISSUER, ACCOUNT_NAME, Mechanism.PUSH,
                REGISTRATION_ENDPOINT, AUTHENTICATION_ENDPOINT);

        defaultStorage.setAccount(account);
        defaultStorage.setMechanism(mechanism);

        Account accountFromStorage = defaultStorage.getAccount(account.getId());
        List<Mechanism> mechanismsFromStorage = defaultStorage.getMechanismsForAccount(accountFromStorage);

        assertNotNull(accountFromStorage);
        assertEquals(account.getId(), accountFromStorage.getId());
        assertNotNull(mechanismsFromStorage);
        assertEquals(mechanism.getMechanismUID(), mechanismsFromStorage.get(0).getMechanismUID());
    }

    @Test
    public void testStoreMultipleMechanismsForSameAccount() {
        DefaultStorageClient defaultStorage = new DefaultStorageClient(context);
        Account account = new Account(ISSUER, ACCOUNT_NAME);
        Mechanism mechanism1 = new Push(MECHANISM_UID, ISSUER, ACCOUNT_NAME, Mechanism.PUSH,
                REGISTRATION_ENDPOINT, AUTHENTICATION_ENDPOINT);
        Mechanism mechanism2 = new Oath(OTHER_MECHANISM_UID, ISSUER, ACCOUNT_NAME, Mechanism.OATH,
                Oath.TokenType.HOTP, ALGORITHM, SECRET, DIGITS, COUNTER, PERIOD);

        defaultStorage.setAccount(account);
        defaultStorage.setMechanism(mechanism1);
        defaultStorage.setMechanism(mechanism2);

        Account accountFromStorage = defaultStorage.getAccount(account.getId());
        List<Mechanism> mechanismsFromStorage = defaultStorage.getMechanismsForAccount(accountFromStorage);

        assertNotNull(accountFromStorage);
        assertEquals(account.getId(), accountFromStorage.getId());
        assertNotNull(mechanismsFromStorage);
        assertEquals(mechanismsFromStorage.size(), 2);
    }

    @Test
    public void testUpdateExistingMechanism() {
        DefaultStorageClient defaultStorage = new DefaultStorageClient(context);

        Account account = new Account(ISSUER, ACCOUNT_NAME);
        Mechanism mechanism = new Push(MECHANISM_UID, ISSUER, ACCOUNT_NAME, Mechanism.PUSH,
                REGISTRATION_ENDPOINT, AUTHENTICATION_ENDPOINT);

        defaultStorage.setAccount(account);
        defaultStorage.setMechanism(mechanism);

        Account accountFromStorage = defaultStorage.getAccount(account.getId());
        Push pushMechanismFromStorage = (Push) defaultStorage.getMechanismsForAccount(accountFromStorage).get(0);

        assertNotNull(accountFromStorage);
        assertEquals(account.getId(), accountFromStorage.getId());
        assertNotNull(pushMechanismFromStorage);
        assertEquals(pushMechanismFromStorage.getMechanismUID(), MECHANISM_UID);
        assertEquals(pushMechanismFromStorage.getRegistrationEndpoint(), REGISTRATION_ENDPOINT);

        Mechanism updatedMechanism = new Push(MECHANISM_UID, ISSUER, ACCOUNT_NAME, Mechanism.PUSH,
                OTHER_REGISTRATION_ENDPOINT, OTHER_AUTHENTICATION_ENDPOINT);
        defaultStorage.setMechanism(updatedMechanism);

        Push updatedPushMechanismFromStorage = (Push) defaultStorage.getMechanismsForAccount(accountFromStorage).get(0);

        assertNotNull(updatedPushMechanismFromStorage);
        assertEquals(updatedPushMechanismFromStorage.getMechanismUID(), MECHANISM_UID);
        assertEquals(updatedPushMechanismFromStorage.getRegistrationEndpoint(), OTHER_REGISTRATION_ENDPOINT);
        assertEquals(updatedPushMechanismFromStorage.getAuthenticationEndpoint(), OTHER_AUTHENTICATION_ENDPOINT);
    }

    @Test
    public void testRemoveExistingMechanism() {
        DefaultStorageClient defaultStorage = new DefaultStorageClient(context);

        Account account = new Account(ISSUER, ACCOUNT_NAME);
        Mechanism mechanism = new Oath(OTHER_MECHANISM_UID, ISSUER, ACCOUNT_NAME, Mechanism.OATH,
                Oath.TokenType.HOTP, ALGORITHM, SECRET, DIGITS, COUNTER, PERIOD);

        defaultStorage.setAccount(account);
        defaultStorage.setMechanism(mechanism);

        Account accountFromStorage = defaultStorage.getAccount(account.getId());
        Oath oathMechanismFromStorage = (Oath) defaultStorage.getMechanismsForAccount(accountFromStorage).get(0);

        assertNotNull(accountFromStorage);
        assertEquals(account.getId(), accountFromStorage.getId());
        assertTrue(defaultStorage.removeMechanism(oathMechanismFromStorage));
        assertEquals(defaultStorage.getMechanismsForAccount(accountFromStorage).size(), 0);
    }

    @Test
    public void testStoreNotification() {
        DefaultStorageClient defaultStorage = new DefaultStorageClient(context);
        Calendar timeAdded = Calendar.getInstance();

        Account account = new Account(ISSUER, ACCOUNT_NAME);
        Mechanism mechanism = new Push(MECHANISM_UID, ISSUER, ACCOUNT_NAME, Mechanism.PUSH,
                REGISTRATION_ENDPOINT, AUTHENTICATION_ENDPOINT);
        Notification notification = new Notification(MECHANISM_UID, MESSAGE_ID, CHALLENGE,
                AMLB_COOKIE, timeAdded, TTL);

        defaultStorage.setAccount(account);
        defaultStorage.setMechanism(mechanism);
        defaultStorage.setNotification(notification);

        Account accountFromStorage = defaultStorage.getAccount(account.getId());
        Push pushMechanismFromStorage = (Push) defaultStorage.getMechanismsForAccount(accountFromStorage).get(0);
        Notification notificationFromStorage = defaultStorage.getAllNotificationsForMechanism(pushMechanismFromStorage).get(0);

        assertNotNull(accountFromStorage);
        assertNotNull(pushMechanismFromStorage);
        assertNotNull(notificationFromStorage);
        assertEquals(notificationFromStorage.getMechanismUID(), MECHANISM_UID);
        assertEquals(notificationFromStorage.getMessageId(), MESSAGE_ID);
    }

    @Test
    public void testStoreMultipleNotificationsForSameAccount() {
        DefaultStorageClient defaultStorage = new DefaultStorageClient(context);

        Calendar timeAdded1 = Calendar.getInstance();
        Calendar timeAdded2 = Calendar.getInstance();
        Calendar timeAdded3 = Calendar.getInstance();
        timeAdded2.setTimeInMillis(timeAdded2.getTimeInMillis()+100);
        timeAdded3.setTimeInMillis(timeAdded3.getTimeInMillis()+200);

        Account account = new Account(ISSUER, ACCOUNT_NAME);
        Mechanism mechanism = new Push(MECHANISM_UID, ISSUER, ACCOUNT_NAME, Mechanism.PUSH,
                REGISTRATION_ENDPOINT, AUTHENTICATION_ENDPOINT);
        Notification notification1 = new Notification(MECHANISM_UID, MESSAGE_ID, CHALLENGE,
                AMLB_COOKIE, timeAdded1, TTL);
        Notification notification2 = new Notification(MECHANISM_UID, OTHER_MESSAGE_ID, CHALLENGE,
                AMLB_COOKIE, timeAdded2, TTL);
        Notification notification3 = new Notification(MECHANISM_UID, OTHER_MESSAGE_ID, CHALLENGE,
                AMLB_COOKIE, timeAdded3, TTL);

        defaultStorage.setAccount(account);
        defaultStorage.setMechanism(mechanism);
        defaultStorage.setNotification(notification1);
        defaultStorage.setNotification(notification2);
        defaultStorage.setNotification(notification3);

        Account accountFromStorage = defaultStorage.getAccount(account.getId());
        Push pushMechanismFromStorage = (Push) defaultStorage.getMechanismsForAccount(accountFromStorage).get(0);
        List<Notification> notificationsFromStorage = defaultStorage.getAllNotificationsForMechanism(pushMechanismFromStorage);

        assertNotNull(accountFromStorage);
        assertNotNull(pushMechanismFromStorage);
        assertNotNull(notificationsFromStorage);
        assertEquals(notificationsFromStorage.size(), 3);
    }


    @Test
    public void testStoreMultipleNotificationsForDifferentAccounts() {
        DefaultStorageClient defaultStorage = new DefaultStorageClient(context);

        Calendar timeAdded1 = Calendar.getInstance();
        Calendar timeAdded2 = Calendar.getInstance();
        timeAdded2.setTimeInMillis(timeAdded2.getTimeInMillis()+100);

        Account account1 = new Account(ISSUER, ACCOUNT_NAME);
        Account account2 = new Account(OTHER_ISSUER, OTHER_ACCOUNT_NAME);
        Mechanism mechanism1 = new Push(MECHANISM_UID, ISSUER, ACCOUNT_NAME, Mechanism.PUSH,
                REGISTRATION_ENDPOINT, AUTHENTICATION_ENDPOINT);
        Mechanism mechanism2 = new Push(OTHER_MECHANISM_UID, OTHER_ISSUER, OTHER_ACCOUNT_NAME, Mechanism.PUSH,
                REGISTRATION_ENDPOINT, AUTHENTICATION_ENDPOINT);
        Notification notification1 = new Notification(MECHANISM_UID, MESSAGE_ID, CHALLENGE,
                AMLB_COOKIE, timeAdded1, TTL);
        Notification notification2 = new Notification(MECHANISM_UID, OTHER_MESSAGE_ID, CHALLENGE,
                AMLB_COOKIE, timeAdded2, TTL);
        Notification notification3 = new Notification(OTHER_MECHANISM_UID, MESSAGE_ID, CHALLENGE,
                AMLB_COOKIE, timeAdded2, TTL);

        defaultStorage.setAccount(account1);
        defaultStorage.setAccount(account2);
        defaultStorage.setMechanism(mechanism1);
        defaultStorage.setMechanism(mechanism2);
        defaultStorage.setNotification(notification1);
        defaultStorage.setNotification(notification2);
        defaultStorage.setNotification(notification3);

        Account accountFromStorage1 = defaultStorage.getAccount(account1.getId());
        Account accountFromStorage2 = defaultStorage.getAccount(account2.getId());
        Push pushMechanismFromStorage1 = (Push) defaultStorage.getMechanismsForAccount(accountFromStorage1).get(0);
        Push pushMechanismFromStorage2 = (Push) defaultStorage.getMechanismsForAccount(accountFromStorage2).get(0);
        List<Notification> notificationsFromStorage1 = defaultStorage.getAllNotificationsForMechanism(pushMechanismFromStorage1);
        List<Notification> notificationsFromStorage2 = defaultStorage.getAllNotificationsForMechanism(pushMechanismFromStorage2);

        assertNotNull(accountFromStorage1);
        assertNotNull(pushMechanismFromStorage1);
        assertNotNull(notificationsFromStorage1);
        assertNotNull(accountFromStorage2);
        assertNotNull(pushMechanismFromStorage2);
        assertNotNull(notificationsFromStorage2);
        assertEquals(notificationsFromStorage1.size(), 2);
        assertEquals(notificationsFromStorage2.size(), 1);
    }

    @Test
    public void testUpdateExistingNotification() {
        DefaultStorageClient defaultStorage = new DefaultStorageClient(context);
        Calendar timeAdded = Calendar.getInstance();
        boolean approved = false;
        boolean pending = true;

        Account account = new Account(ISSUER, ACCOUNT_NAME);
        Mechanism mechanism = new Push(MECHANISM_UID, ISSUER, ACCOUNT_NAME, Mechanism.PUSH,
                REGISTRATION_ENDPOINT, AUTHENTICATION_ENDPOINT);
        Notification notification = new Notification(MECHANISM_UID, MESSAGE_ID, CHALLENGE,
                AMLB_COOKIE, timeAdded, TTL);

        defaultStorage.setAccount(account);
        defaultStorage.setMechanism(mechanism);
        defaultStorage.setNotification(notification);

        Account accountFromStorage = defaultStorage.getAccount(account.getId());
        Push pushMechanismFromStorage = (Push) defaultStorage.getMechanismsForAccount(accountFromStorage).get(0);
        Notification notificationFromStorage = defaultStorage.getAllNotificationsForMechanism(pushMechanismFromStorage).get(0);

        assertNotNull(accountFromStorage);
        assertNotNull(pushMechanismFromStorage);
        assertNotNull(notificationFromStorage);
        assertEquals(notificationFromStorage.getMechanismUID(), MECHANISM_UID);
        assertEquals(notificationFromStorage.getMessageId(), MESSAGE_ID);

        Notification updatedNotification = new Notification(MECHANISM_UID, MESSAGE_ID, CHALLENGE,
                AMLB_COOKIE, timeAdded, TTL, approved, pending);

        defaultStorage.setNotification(updatedNotification);

        Notification updatedNotificationFromStorage = defaultStorage.getAllNotificationsForMechanism(pushMechanismFromStorage).get(0);

        assertNotNull(notificationFromStorage);
        assertEquals(updatedNotificationFromStorage.getMessageId(), MESSAGE_ID);
        assertEquals(notificationFromStorage.isApproved(), approved);
        assertEquals(notificationFromStorage.isPending(), pending);
    }

    @Test
    public void testRemoveExistingNotification() {
        DefaultStorageClient defaultStorage = new DefaultStorageClient(context);
        Calendar timeAdded = Calendar.getInstance();

        Account account = new Account(ISSUER, ACCOUNT_NAME);
        Mechanism mechanism = new Push(MECHANISM_UID, ISSUER, ACCOUNT_NAME, Mechanism.PUSH,
                REGISTRATION_ENDPOINT, AUTHENTICATION_ENDPOINT);
        Notification notification = new Notification(MECHANISM_UID, MESSAGE_ID, CHALLENGE,
                AMLB_COOKIE, timeAdded, TTL);

        defaultStorage.setAccount(account);
        defaultStorage.setMechanism(mechanism);
        defaultStorage.setNotification(notification);

        Account accountFromStorage = defaultStorage.getAccount(account.getId());
        Push pushMechanismFromStorage = (Push) defaultStorage.getMechanismsForAccount(accountFromStorage).get(0);
        Notification notificationFromStorage = defaultStorage.getAllNotificationsForMechanism(pushMechanismFromStorage).get(0);

        assertNotNull(accountFromStorage);
        assertNotNull(pushMechanismFromStorage);
        assertNotNull(notificationFromStorage);
        assertEquals(notificationFromStorage.getMessageId(), MESSAGE_ID);
        assertTrue(defaultStorage.removeNotification(notificationFromStorage));
        assertEquals(defaultStorage.getAllNotificationsForMechanism(pushMechanismFromStorage).size(), 0);
    }

}