package org.delcom.ifs25010springbootcashflow.domain.entity;

import org.delcom.ifs25010springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25010springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25010springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EntityTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 10, 8, 30);

    private static Transaction tx(int id, long amount, TransactionType type) {
        return new Transaction(id, 7, "tx" + id, amount, type, null, NOW);
    }

    @Test
    void transactionExposesFieldsAndSignedAmount() {
        Transaction income = new Transaction(1, 7, "Gaji", 1000, TransactionType.INCOME, "a.png", NOW);
        Transaction expense = tx(2, 400, TransactionType.EXPENSE);

        assertEquals(1, income.getId());
        assertEquals(7, income.getUserId());
        assertEquals("Gaji", income.getDescription());
        assertEquals(1000, income.getAmount());
        assertEquals(TransactionType.INCOME, income.getType());
        assertEquals("a.png", income.getProofPath());
        assertEquals(NOW, income.getCreatedAt());
        assertTrue(income.isIncome());
        assertEquals(1000, income.signedAmount());
        assertFalse(expense.isIncome());
        assertEquals(-400, expense.signedAmount());
    }

    @Test
    void transactionCanBeChangedThroughNamedMethods() {
        Transaction transaction = tx(1, 100, TransactionType.INCOME);
        transaction.changeDescription("Baru");
        transaction.changeAmount(250);
        transaction.changeProofPath("bukti.jpg");
        assertEquals("Baru", transaction.getDescription());
        assertEquals(250, transaction.getAmount());
        assertEquals("bukti.jpg", transaction.getProofPath());
    }

    @Test
    void userExposesFieldsAndCanBeChanged() {
        User admin = new User(1, "admin", "hash", "Administrator", Role.ADMIN, null);
        User user = new User(2, "sherina", "hash2", "Sherina", Role.USER, "foto.png");

        assertEquals(1, admin.getId());
        assertEquals("admin", admin.getUsername());
        assertEquals("hash", admin.getPasswordHash());
        assertEquals("Administrator", admin.getFullName());
        assertEquals(Role.ADMIN, admin.getRole());
        assertTrue(admin.isAdmin());
        assertFalse(user.isAdmin());
        assertEquals("foto.png", user.getPhotoPath());

        user.changeFullName("Sherina Pasaribu");
        user.changePasswordHash("hash3");
        user.changePhotoPath(null);
        assertEquals("Sherina Pasaribu", user.getFullName());
        assertEquals("hash3", user.getPasswordHash());
        assertEquals(null, user.getPhotoPath());
    }

    @Test
    void sortOptionsCarryTheirComparators() {
        Transaction a = tx(1, 300, TransactionType.INCOME);
        Transaction b = tx(2, 100, TransactionType.EXPENSE);
        Transaction c = tx(3, 200, TransactionType.INCOME);

        assertEquals(List.of(b, c, a), sorted(SortOption.AMOUNT_ASC, a, b, c));
        assertEquals(List.of(a, c, b), sorted(SortOption.AMOUNT_DESC, a, b, c));
        assertEquals(List.of(a, c, b), sorted(SortOption.INCOME_FIRST, a, b, c));
        assertEquals(List.of(b, a, c), sorted(SortOption.EXPENSE_FIRST, a, b, c));
        assertEquals(4, SortOption.values().length);
        assertSame(SortOption.AMOUNT_ASC, SortOption.valueOf("AMOUNT_ASC"));
    }

    @Test
    void exceptionsCarryMessageAndCause() {
        RuntimeException cause = new RuntimeException("akar");
        DomainException domain = new DomainException("domain", cause);
        ValidationException validation = new ValidationException("valid", cause);
        EntityNotFoundException notFound = new EntityNotFoundException(42);

        assertSame(cause, domain.getCause());
        assertSame(cause, validation.getCause());
        assertEquals("valid", validation.getMessage());
        assertEquals(42, notFound.getId());
        assertEquals("Data dengan ID 42 tidak ditemukan.", notFound.getMessage());
        assertTrue(notFound instanceof DomainException);
    }

    private static List<Transaction> sorted(SortOption option, Transaction... transactions) {
        List<Transaction> list = new ArrayList<>(List.of(transactions));
        list.sort(option.comparator());
        return list;
    }
}
