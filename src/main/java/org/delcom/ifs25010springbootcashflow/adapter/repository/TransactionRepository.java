package org.delcom.ifs25010springbootcashflow.adapter.repository;

import org.delcom.ifs25010springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25010springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25010springbootcashflow.domain.repository.ITransactionRepository;

import java.time.LocalDateTime;
import java.util.List;

/** Repository transaksi in-memory (untuk unit test). */
public class TransactionRepository extends InMemoryRepository<Transaction, Integer>
        implements ITransactionRepository {
    private int idCounter = 0;

    public TransactionRepository() {
        super(Transaction::getId);
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        return save(new Transaction(++idCounter, userId, description, amount, type, proofPath, LocalDateTime.now()));
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return findBy(transaction -> transaction.getUserId() == userId);
    }
}
