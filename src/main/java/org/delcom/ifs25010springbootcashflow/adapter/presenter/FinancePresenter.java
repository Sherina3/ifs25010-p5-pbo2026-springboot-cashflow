package org.delcom.ifs25010springbootcashflow.adapter.presenter;

import org.delcom.ifs25010springbootcashflow.domain.entity.Role;
import org.delcom.ifs25010springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25010springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25010springbootcashflow.domain.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Presenter: memformat data domain menjadi teks siap tampil
 * (uang, tanggal, label, dan pesan sukses/gagal). Entity tidak berurusan dengan format.
 */
public class FinancePresenter {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String UPLOAD_PREFIX = "/uploads/";

    private static final Map<TransactionType, String> TYPE_LABELS = Map.of(
            TransactionType.INCOME, "Pemasukan",
            TransactionType.EXPENSE, "Pengeluaran");

    private static final Map<Role, String> ROLE_LABELS = Map.of(
            Role.ADMIN, "Admin",
            Role.USER, "Pengguna");

    private static final Map<SortOption, String> SORT_LABELS = Map.of(
            SortOption.AMOUNT_ASC, "Jumlah terkecil",
            SortOption.AMOUNT_DESC, "Jumlah terbesar",
            SortOption.INCOME_FIRST, "Pemasukan dulu",
            SortOption.EXPENSE_FIRST, "Pengeluaran dulu");

    /** Uang dengan pemisah ribuan dan dua desimal, mis. 1000000 menjadi "Rp 1,000,000.00". */
    public String formatMoney(long amount) {
        return String.format(Locale.US, "Rp %,.2f", BigDecimal.valueOf(amount));
    }

    public String formatType(TransactionType type) {
        return TYPE_LABELS.get(type);
    }

    public String formatRole(Role role) {
        return ROLE_LABELS.get(role);
    }

    public String formatSortOption(SortOption option) {
        return SORT_LABELS.get(option);
    }

    public String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMAT);
    }

    /** Satu baris transaksi: [id] keterangan | Rp ... | [Jenis]. */
    public String format(Transaction transaction) {
        return String.format(
                "[%d] %s | %s | [%s]",
                transaction.getId(),
                transaction.getDescription(),
                formatMoney(transaction.getAmount()),
                formatType(transaction.getType()));
    }

    /** Saldo: "Saldo: Rp ...". */
    public String formatBalance(long balance) {
        return "Saldo: " + formatMoney(balance);
    }

    /** Daftar transaksi multi-baris; berisi pesan kosong bila tidak ada data. */
    public String showTransactions(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            return "Daftar Transaksi:\n" + showEmptyData();
        }
        return "Daftar Transaksi:\n" + transactions.stream().map(this::format).collect(Collectors.joining("\n"));
    }

    /** URL publik untuk path relatif file upload. */
    public String uploadUrl(String path) {
        return UPLOAD_PREFIX + path;
    }

    public String showEmptyData() {
        return "- Data transaksi belum tersedia!";
    }

    public String showSearchEmpty() {
        return "- Transaksi tidak ditemukan!";
    }

    public String showAddSuccess(Transaction transaction) {
        String label = transaction.isIncome() ? "pemasukan" : "pengeluaran";
        return "Berhasil menambah " + label + ": " + format(transaction);
    }

    public String showRemoveSuccess() {
        return "Berhasil menghapus transaksi.";
    }

    public String showRemoveFailed(int id) {
        return String.format("[!] Gagal menghapus transaksi dengan ID: %d.", id);
    }

    public String showProfileSaved() {
        return "Profil berhasil disimpan.";
    }

    public String showPhotoSaved() {
        return "Foto profil berhasil diperbarui.";
    }

    public String showRegisterSuccess(String fullName) {
        return "Registrasi berhasil. Selamat datang, " + fullName + "!";
    }

    public String showValidationError(String message) {
        return "[!] " + message;
    }

    public String showUploadError() {
        return "[!] Gagal menyimpan file. Coba lagi.";
    }
}
