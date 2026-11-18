package org.delcom.ifs25010springbootcashflow.domain.repository;

import org.delcom.ifs25010springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25010springbootcashflow.domain.entity.TransactionType;

import java.util.List;

/** Port spesifik transaksi: penyimpanan dengan ID otomatis dan pencarian per pengguna. */
public interface ITransactionRepository extends IRepository<Transaction, Integer> {
    /** Menyimpan transaksi baru milik {@code userId}. */
    Transaction save(int userId, String description, long amount, TransactionType type, String proofPath);

    /** Semua transaksi milik satu pengguna, terurut berdasarkan ID. */
    List<Transaction> findByUserId(int userId);
}
