package org.delcom.ifs25010springbootcashflow.adapter.repository;

import org.delcom.ifs25010springbootcashflow.domain.entity.Role;
import org.delcom.ifs25010springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25010springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25010springbootcashflow.domain.entity.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryRepositoryTest {

    @Test
    void transactionRepositoryAssignsIdsAndFiltersByUser() {
        TransactionRepository repository = new TransactionRepository();
        Transaction first = repository.save(1, "Gaji", 1000, TransactionType.INCOME, null);
        Transaction second = repository.save(2, "Makan", 50, TransactionType.EXPENSE, "b.png");

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertEquals(List.of(first), repository.findByUserId(1));
        assertEquals(2, repository.findAll().size());
        assertEquals("Makan", repository.findById(2).orElseThrow().getDescription());
        assertTrue(repository.findById(99).isEmpty());
        assertEquals(1, repository.findBy(Transaction::isIncome).size());

        repository.update(first);
        repository.findAll().clear();
        assertEquals(2, repository.findAll().size());

        assertTrue(repository.deleteById(1));
        assertFalse(repository.deleteById(1));
        assertEquals(1, repository.findAll().size());
    }

    @Test
    void userRepositoryFindsByUsername() {
        UserRepository repository = new UserRepository();
        User saved = repository.save("sherina", "hash", "Sherina", Role.USER);

        assertEquals(1, saved.getId());
        assertEquals(saved, repository.findByUsername("sherina").orElseThrow());
        assertTrue(repository.findByUsername("lain").isEmpty());
    }
}
