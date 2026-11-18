package org.delcom.ifs25010springbootcashflow.adapter.presenter;

import org.delcom.ifs25010springbootcashflow.domain.entity.Role;
import org.delcom.ifs25010springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25010springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25010springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FinancePresenterTest {
    private final FinancePresenter presenter = new FinancePresenter();
    private final Transaction income =
            new Transaction(3, 1, "Gaji", 1_000_000, TransactionType.INCOME, null, LocalDateTime.of(2026, 11, 10, 9, 5));
    private final Transaction expense =
            new Transaction(4, 1, "Makan", 25_000, TransactionType.EXPENSE, "b.png", LocalDateTime.of(2026, 1, 2, 3, 4));

    @Test
    void formatsMoneyWithThousandsSeparatorAndTwoDecimals() {
        assertEquals("Rp 1,000,000.00", presenter.formatMoney(1_000_000));
        assertEquals("Rp 0.00", presenter.formatMoney(0));
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(4_975_000));
    }

    @Test
    void formatsLinesAndLabels() {
        assertEquals("[3] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.format(income));
        assertEquals("[4] Makan | Rp 25,000.00 | [Pengeluaran]", presenter.format(expense));
        assertEquals("Pemasukan", presenter.formatType(TransactionType.INCOME));
        assertEquals("Admin", presenter.formatRole(Role.ADMIN));
        assertEquals("Pengguna", presenter.formatRole(Role.USER));
        assertEquals("10/11/2026 09:05", presenter.formatDateTime(income.getCreatedAt()));
        assertEquals("/uploads/foto.png", presenter.uploadUrl("foto.png"));
        for (SortOption option : SortOption.values()) {
            assertEquals(false, presenter.formatSortOption(option).isBlank());
        }
        assertEquals("Jumlah terkecil", presenter.formatSortOption(SortOption.AMOUNT_ASC));
    }

    @Test
    void showsTransactionLists() {
        assertEquals("Daftar Transaksi:\n- Data transaksi belum tersedia!", presenter.showTransactions(List.of()));
        assertEquals("Daftar Transaksi:\n[3] Gaji | Rp 1,000,000.00 | [Pemasukan]\n"
                + "[4] Makan | Rp 25,000.00 | [Pengeluaran]", presenter.showTransactions(List.of(income, expense)));
    }

    @Test
    void buildsMessages() {
        assertEquals("- Data transaksi belum tersedia!", presenter.showEmptyData());
        assertEquals("- Transaksi tidak ditemukan!", presenter.showSearchEmpty());
        assertEquals("Berhasil menambah pemasukan: [3] Gaji | Rp 1,000,000.00 | [Pemasukan]",
                presenter.showAddSuccess(income));
        assertEquals("Berhasil menambah pengeluaran: [4] Makan | Rp 25,000.00 | [Pengeluaran]",
                presenter.showAddSuccess(expense));
        assertEquals("Berhasil menghapus transaksi.", presenter.showRemoveSuccess());
        assertEquals("[!] Gagal menghapus transaksi dengan ID: 9.", presenter.showRemoveFailed(9));
        assertEquals("Profil berhasil disimpan.", presenter.showProfileSaved());
        assertEquals("Foto profil berhasil diperbarui.", presenter.showPhotoSaved());
        assertEquals("Registrasi berhasil. Selamat datang, Sherina!", presenter.showRegisterSuccess("Sherina"));
        assertEquals("[!] Jumlah salah", presenter.showValidationError("Jumlah salah"));
        assertEquals("[!] Gagal menyimpan file. Coba lagi.", presenter.showUploadError());
    }
}
