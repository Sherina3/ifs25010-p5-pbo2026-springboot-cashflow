package org.delcom.ifs25010springbootcashflow.usecase;

import org.delcom.ifs25010springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25010springbootcashflow.domain.entity.Role;
import org.delcom.ifs25010springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25010springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25010springbootcashflow.domain.entity.User;
import org.delcom.ifs25010springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25010springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceUseCaseTest {
    private static final int ME = 1;
    private static final int OTHER = 2;

    private TransactionRepository repository;
    private FinanceUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = new TransactionRepository();
        useCase = new FinanceUseCase(repository);
    }

    @Test
    void balanceIsIncomeMinusExpensePerUser() throws ValidationException {
        useCase.addIncome(ME, "Gaji", "5000000", null);
        useCase.addExpense(ME, "Makan", "25000", "bukti.png");
        useCase.addIncome(OTHER, "Bonus", "999", null);

        assertEquals(4_975_000, useCase.getBalance(ME));
        assertEquals(5_000_000, useCase.getTotalIncome(ME));
        assertEquals(25_000, useCase.getTotalExpense(ME));
        assertEquals(2, useCase.countTransactions(ME));
        assertEquals(1, useCase.getTransactions(OTHER).size());
        assertEquals("bukti.png", useCase.getTransactions(ME).get(1).getProofPath());
    }

    @Test
    void rejectsInvalidDescriptionAndAmount() {
        assertThrows(ValidationException.class, () -> useCase.addIncome(ME, null, "1000", null));
        assertThrows(ValidationException.class, () -> useCase.addIncome(ME, "  ", "1000", null));
        assertThrows(ValidationException.class, () -> useCase.addIncome(ME, "Gaji", null, null));
        assertThrows(ValidationException.class, () -> useCase.addIncome(ME, "Gaji", "  ", null));
        assertThrows(ValidationException.class, () -> useCase.addExpense(ME, "Makan", "abc", null));
        assertThrows(ValidationException.class, () -> useCase.addExpense(ME, "Makan", "12.5", null));
        assertThrows(ValidationException.class, () -> useCase.addExpense(ME, "Makan", "0", null));
        assertThrows(ValidationException.class, () -> useCase.addExpense(ME, "Makan", "-5", null));
        assertTrue(useCase.getTransactions(ME).isEmpty());
    }

    @Test
    void removeChecksIdOwnershipAndExistence() throws Exception {
        Transaction mine = useCase.addIncome(ME, "Gaji", "1000", "bukti.png");
        Transaction theirs = useCase.addIncome(OTHER, "Bonus", "500", null);

        assertThrows(ValidationException.class, () -> useCase.removeTransaction(ME, 0));
        assertThrows(EntityNotFoundException.class, () -> useCase.removeTransaction(ME, 99));
        assertThrows(ValidationException.class, () -> useCase.removeTransaction(ME, theirs.getId()));

        Transaction removed = useCase.removeTransaction(ME, mine.getId());
        assertEquals("bukti.png", removed.getProofPath());
        assertEquals(0, useCase.getBalance(ME));
        assertEquals(1, useCase.getTransactions(OTHER).size());
    }

    @Test
    void updateChangesDescriptionAndAmountOfOwnTransaction() throws Exception {
        Transaction mine = useCase.addExpense(ME, "Makan", "1000", null);

        Transaction updated = useCase.updateTransaction(ME, mine.getId(), " Makan malam ", "2500");
        assertEquals("Makan malam", updated.getDescription());
        assertEquals(2500, updated.getAmount());

        assertThrows(ValidationException.class, () -> useCase.updateTransaction(ME, mine.getId(), "", "100"));
        assertThrows(ValidationException.class, () -> useCase.updateTransaction(ME, mine.getId(), "Ok", "x"));
        assertThrows(EntityNotFoundException.class, () -> useCase.updateTransaction(ME, 99, "Ok", "100"));
    }

    @Test
    void searchIsCaseInsensitiveAndScopedToUser() throws ValidationException {
        useCase.addIncome(ME, "Gaji Bulanan", "1000", null);
        useCase.addExpense(ME, "Makan Siang", "10", null);
        useCase.addIncome(OTHER, "Gaji Orang Lain", "1", null);

        assertEquals(1, useCase.searchTransactions(ME, "gAJI").size());
        assertEquals(2, useCase.searchTransactions(ME, null).size());
        assertEquals(2, useCase.searchTransactions(ME, "   ").size());
        assertTrue(useCase.searchTransactions(ME, "tidak ada").isEmpty());
    }

    @Test
    void sortAndFindUseDomainComparators() throws ValidationException {
        useCase.addIncome(ME, "A", "300", null);
        useCase.addExpense(ME, "B", "100", null);
        useCase.addIncome(ME, "C", "200", null);

        assertEquals("B", useCase.sortTransactions(ME, SortOption.AMOUNT_ASC).get(0).getDescription());
        assertEquals("A", useCase.sortTransactions(ME, SortOption.AMOUNT_DESC).get(0).getDescription());
        assertEquals("B", useCase.sortTransactions(ME, SortOption.EXPENSE_FIRST).get(0).getDescription());
        assertThrows(ValidationException.class, () -> useCase.sortTransactions(ME, null));

        List<Transaction> unsorted = useCase.findTransactions(ME, null, null);
        assertEquals("A", unsorted.get(0).getDescription());
        List<Transaction> combined = useCase.findTransactions(ME, "", SortOption.AMOUNT_ASC);
        assertEquals("B", combined.get(0).getDescription());
        assertEquals(1, useCase.findTransactions(ME, "c", SortOption.AMOUNT_ASC).size());
    }

    @Test
    void viewTransactionsOfAllowsAdminOrOwnerOnly() throws ValidationException {
        useCase.addIncome(OTHER, "Bonus", "500", null);
        User admin = new User(9, "admin", "h", "Admin", Role.ADMIN, null);
        User owner = new User(OTHER, "dia", "h", "Dia", Role.USER, null);
        User stranger = new User(ME, "saya", "h", "Saya", Role.USER, null);

        assertEquals(1, useCase.viewTransactionsOf(admin, OTHER).size());
        assertEquals(1, useCase.viewTransactionsOf(owner, OTHER).size());
        assertThrows(ValidationException.class, () -> useCase.viewTransactionsOf(stranger, OTHER));
    }
}
