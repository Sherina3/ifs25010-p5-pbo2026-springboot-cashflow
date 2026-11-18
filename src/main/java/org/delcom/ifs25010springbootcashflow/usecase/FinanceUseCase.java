package org.delcom.ifs25010springbootcashflow.usecase;

import org.delcom.ifs25010springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25010springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25010springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25010springbootcashflow.domain.entity.User;
import org.delcom.ifs25010springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25010springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25010springbootcashflow.domain.repository.ITransactionRepository;

import java.util.List;

/**
 * Use case pencatatan keuangan. Semua operasi dibatasi per pengguna (userId).
 * Tidak mengenal komponen UI; kegagalan dilaporkan lewat checked domain exception.
 */
public class FinanceUseCase {
    private final ITransactionRepository transactionRepository;

    public FinanceUseCase(ITransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction addIncome(int userId, String description, String amountText, String proofPath)
            throws ValidationException {
        return add(userId, description, amountText, proofPath, TransactionType.INCOME);
    }

    public Transaction addExpense(int userId, String description, String amountText, String proofPath)
            throws ValidationException {
        return add(userId, description, amountText, proofPath, TransactionType.EXPENSE);
    }

    /** Menghapus transaksi milik userId dan mengembalikan transaksi yang dihapus. */
    public Transaction removeTransaction(int userId, int id) throws EntityNotFoundException, ValidationException {
        Transaction transaction = ownedTransaction(userId, id);
        transactionRepository.deleteById(id);
        return transaction;
    }

    /** Mengubah keterangan dan jumlah transaksi milik userId. */
    public Transaction updateTransaction(int userId, int id, String description, String amountText)
            throws EntityNotFoundException, ValidationException {
        Transaction transaction = ownedTransaction(userId, id);
        String validDescription = validateDescription(description);
        long amount = parseAmount(amountText);
        transaction.changeDescription(validDescription);
        transaction.changeAmount(amount);
        transactionRepository.update(transaction);
        return transaction;
    }

    public List<Transaction> getTransactions(int userId) {
        return transactionRepository.findByUserId(userId);
    }

    /** Mencari berdasarkan keterangan (case-insensitive); kata kunci kosong = semua. */
    public List<Transaction> searchTransactions(int userId, String keyword) {
        List<Transaction> all = transactionRepository.findByUserId(userId);
        if (keyword == null || keyword.isBlank()) {
            return all;
        }
        String lowerKeyword = keyword.trim().toLowerCase();
        return all.stream()
                .filter(transaction -> transaction.getDescription().toLowerCase().contains(lowerKeyword))
                .toList();
    }

    public List<Transaction> sortTransactions(int userId, SortOption option) throws ValidationException {
        if (option == null) {
            throw new ValidationException("Opsi pengurutan tidak valid");
        }
        return transactionRepository.findByUserId(userId).stream().sorted(option.comparator()).toList();
    }

    /** Cari lalu urutkan; {@code option == null} berarti tanpa pengurutan. */
    public List<Transaction> findTransactions(int userId, String keyword, SortOption option) {
        List<Transaction> found = searchTransactions(userId, keyword);
        if (option == null) {
            return found;
        }
        return found.stream().sorted(option.comparator()).toList();
    }

    /** Transaksi milik targetUserId; ADMIN boleh melihat milik siapa pun, USER hanya miliknya sendiri. */
    public List<Transaction> viewTransactionsOf(User actor, int targetUserId) throws ValidationException {
        if (!actor.isAdmin() && actor.getId() != targetUserId) {
            throw new ValidationException("Akses ditolak: bukan data Anda");
        }
        return transactionRepository.findByUserId(targetUserId);
    }

    public int countTransactions(int userId) {
        return transactionRepository.findByUserId(userId).size();
    }

    public long getTotalIncome(int userId) {
        return transactionRepository.findByUserId(userId).stream()
                .filter(Transaction::isIncome)
                .mapToLong(Transaction::getAmount)
                .sum();
    }

    public long getTotalExpense(int userId) {
        return transactionRepository.findByUserId(userId).stream()
                .filter(transaction -> !transaction.isIncome())
                .mapToLong(Transaction::getAmount)
                .sum();
    }

    /** Saldo = total pemasukan - total pengeluaran. */
    public long getBalance(int userId) {
        return transactionRepository.findByUserId(userId).stream()
                .mapToLong(Transaction::signedAmount)
                .sum();
    }

    private Transaction add(int userId, String description, String amountText, String proofPath,
                            TransactionType type) throws ValidationException {
        String validDescription = validateDescription(description);
        long amount = parseAmount(amountText);
        return transactionRepository.save(userId, validDescription, amount, type, proofPath);
    }

    private Transaction ownedTransaction(int userId, int id) throws EntityNotFoundException, ValidationException {
        if (id <= 0) {
            throw new ValidationException("ID tidak valid");
        }
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(id));
        if (transaction.getUserId() != userId) {
            throw new ValidationException("Transaksi ini bukan milik Anda");
        }
        return transaction;
    }

    private String validateDescription(String description) throws ValidationException {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Keterangan transaksi tidak boleh kosong");
        }
        return description.trim();
    }

    private long parseAmount(String amountText) throws ValidationException {
        if (amountText == null || amountText.isBlank()) {
            throw new ValidationException("Jumlah tidak boleh kosong");
        }
        long amount;
        try {
            amount = Long.parseLong(amountText.trim());
        } catch (NumberFormatException ex) {
            throw new ValidationException("Jumlah harus berupa angka bulat (rupiah)", ex);
        }
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih besar dari nol");
        }
        return amount;
    }
}
