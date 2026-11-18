package org.delcom.ifs25010springbootcashflow.adapter.repository;

import org.delcom.ifs25010springbootcashflow.domain.entity.Role;
import org.delcom.ifs25010springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25010springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25010springbootcashflow.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcTransactionRepositoryTest {
    private JdbcTransactionRepository repository;
    private User owner;
    private User other;

    @BeforeEach
    void setUp() {
        DataSource dataSource = JdbcSupportForTests.newH2WithSchema();
        JdbcUserRepository users = new JdbcUserRepository(dataSource);
        owner = users.save("sherina", "hash", "Sherina", Role.USER);
        other = users.save("lain", "hash", "Lain", Role.USER);
        repository = new JdbcTransactionRepository(dataSource);
    }

    @Test
    void savesAndReadsBackAllFields() {
        Transaction saved = repository.save(owner.getId(), "Gaji", 5_000_000L, TransactionType.INCOME, null);
        Transaction withProof = repository.save(owner.getId(), "Makan", 25_000L, TransactionType.EXPENSE, "b.png");

        assertTrue(saved.getId() > 0);
        Transaction found = repository.findById(saved.getId()).orElseThrow();
        assertEquals(owner.getId(), found.getUserId());
        assertEquals("Gaji", found.getDescription());
        assertEquals(5_000_000L, found.getAmount());
        assertEquals(TransactionType.INCOME, found.getType());
        assertNull(found.getProofPath());
        assertEquals("b.png", repository.findById(withProof.getId()).orElseThrow().getProofPath());
        assertEquals(saved.getCreatedAt().withNano(0), found.getCreatedAt().withNano(0));
        assertTrue(repository.findById(999).isEmpty());
    }

    @Test
    void queriesByUserAndPredicate() {
        repository.save(owner.getId(), "A", 1, TransactionType.INCOME, null);
        repository.save(owner.getId(), "B", 2, TransactionType.EXPENSE, null);
        repository.save(other.getId(), "C", 3, TransactionType.INCOME, null);

        assertEquals(3, repository.findAll().size());
        assertEquals(2, repository.findByUserId(owner.getId()).size());
        assertEquals(1, repository.findByUserId(other.getId()).size());
        assertEquals(2, repository.findBy(Transaction::isIncome).size());
    }

    @Test
    void updatesAndDeletes() {
        Transaction saved = repository.save(owner.getId(), "Makan", 100, TransactionType.EXPENSE, null);
        saved.changeDescription("Makan malam");
        saved.changeAmount(250);
        saved.changeProofPath("bukti.png");

        repository.update(saved);

        Transaction reloaded = repository.findById(saved.getId()).orElseThrow();
        assertEquals("Makan malam", reloaded.getDescription());
        assertEquals(250, reloaded.getAmount());
        assertEquals("bukti.png", reloaded.getProofPath());

        assertTrue(repository.deleteById(saved.getId()));
        assertFalse(repository.deleteById(saved.getId()));
    }

    @Test
    void rejectsTransactionForUnknownUser() {
        assertThrows(IllegalStateException.class,
                () -> repository.save(999, "Yatim", 1, TransactionType.INCOME, null));
    }
}
