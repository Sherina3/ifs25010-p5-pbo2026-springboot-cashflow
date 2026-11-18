package org.delcom.ifs25010springbootcashflow.framework.view;

import org.delcom.ifs25010springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25010springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25010springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25010springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25010springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25010springbootcashflow.domain.entity.User;
import org.delcom.ifs25010springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25010springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25010springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25010springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25010springbootcashflow.usecase.FinanceUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

/**
 * Dashboard transaksi keuangan milik pengguna yang sedang login:
 * kartu ringkasan, pencarian, pengurutan, tambah pemasukan/pengeluaran (bukti opsional), dan hapus.
 */
@Route(value = "", layout = MainLayout.class)
@PageTitle("Cashflow")
public class FinanceView extends VerticalLayout {
    private final FinanceUseCase financeUseCase;
    private final FinancePresenter presenter;
    private final FileStorage fileStorage;
    private final IUserRepository userRepository;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final TextField searchField = new TextField();
    private final ComboBox<SortOption> sortBox = new ComboBox<>();
    private final Paragraph statusLabel = new Paragraph();
    private final Span emptyState = new Span();
    private final Span totalCount = new Span("0");
    private final Span totalIncome = new Span();
    private final Span totalExpense = new Span();
    private final Span balanceLabel = new Span();

    private User currentUser;

    public FinanceView(FinanceUseCase financeUseCase, FinancePresenter presenter, FileStorage fileStorage,
                       IUserRepository userRepository) {
        this.financeUseCase = financeUseCase;
        this.presenter = presenter;
        this.fileStorage = fileStorage;
        this.userRepository = userRepository;

        setSizeFull();
        setPadding(true);

        configureGrid();
        configureToolbarFields();

        HorizontalLayout summary = new HorizontalLayout(
                PageCard.summary("Total Transaksi", totalCount, "summary-blue"),
                PageCard.summary("Total Pemasukan", totalIncome, "summary-green"),
                PageCard.summary("Total Pengeluaran", totalExpense, "summary-red"),
                PageCard.summary("Saldo", balanceLabel, "summary-amber"));
        summary.setWidthFull();
        summary.addClassName("summary-row");

        add(summary, new PageCard("Daftar Transaksi", buildToolbar(), statusLabel, grid));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        AuthSession.currentUser(userRepository).ifPresent(user -> {
            currentUser = user;
            refreshAll();
        });
    }

    // ---------- Grid & toolbar ----------

    private void configureGrid() {
        grid.addColumn(Transaction::getId).setHeader("ID").setKey("id")
                .setAutoWidth(true).setFlexGrow(0).setSortable(true)
                .setComparator(Comparator.comparingInt(Transaction::getId));
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setKey("description")
                .setFlexGrow(1).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));
        grid.addComponentColumn(this::amountCell).setHeader("Jumlah").setKey("amount")
                .setAutoWidth(true).setSortable(true)
                .setComparator(Comparator.comparingLong(Transaction::getAmount));
        grid.addColumn(transaction -> presenter.formatType(transaction.getType())).setHeader("Tipe")
                .setKey("type").setAutoWidth(true).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getType));
        grid.addComponentColumn(transaction -> GridStyles.proofCell(proofUrl(transaction), "Bukti transaksi"))
                .setHeader("Bukti").setKey("proof").setAutoWidth(true).setFlexGrow(0).setSortable(false);
        grid.addColumn(transaction -> presenter.formatDateTime(transaction.getCreatedAt())).setHeader("Waktu")
                .setKey("createdAt").setAutoWidth(true).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getCreatedAt));
        grid.addComponentColumn(this::actionCell).setHeader("Tindakan").setKey("actions")
                .setAutoWidth(true).setFlexGrow(0).setSortable(false);
        grid.setEmptyStateComponent(emptyState);
        GridStyles.apply(grid);
    }

    private Span amountCell(Transaction transaction) {
        Span amount = new Span(presenter.formatMoney(transaction.getAmount()));
        amount.addClassName(transaction.isIncome() ? "amount-income" : "amount-expense");
        return amount;
    }

    private HorizontalLayout actionCell(Transaction transaction) {
        Button deleteButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> openDeleteDialog(transaction));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY_INLINE);
        return new HorizontalLayout(deleteButton);
    }

    private String proofUrl(Transaction transaction) {
        return transaction.getProofPath() == null ? null : presenter.uploadUrl(transaction.getProofPath());
    }

    private void configureToolbarFields() {
        searchField.setPlaceholder("Cari keterangan...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);

        sortBox.setPlaceholder("Urutkan...");
        sortBox.setItems(SortOption.values());
        sortBox.setItemLabelGenerator(presenter::formatSortOption);
        sortBox.setClearButtonVisible(true);
        sortBox.addValueChangeListener(event -> reload());
    }

    private HorizontalLayout buildToolbar() {
        Button searchButton = new Button("Cari", VaadinIcon.SEARCH.create(), event -> reload());
        Button refreshButton = new Button("Muat Ulang", VaadinIcon.REFRESH.create(), event -> refreshAll());
        Button incomeButton = new Button("Tambah Pemasukan", VaadinIcon.PLUS.create(),
                event -> openAddDialog(TransactionType.INCOME));
        incomeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        Button expenseButton = new Button("Tambah Pengeluaran", VaadinIcon.MINUS.create(),
                event -> openAddDialog(TransactionType.EXPENSE));
        expenseButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout toolbar = new HorizontalLayout(
                searchField, searchButton, sortBox, refreshButton, incomeButton, expenseButton);
        toolbar.setWidthFull();
        toolbar.setFlexGrow(1, searchField);
        toolbar.addClassName("toolbar");
        return toolbar;
    }

    // ---------- Dialog tambah ----------

    /** Menampung hasil upload bukti sementara sampai tombol Simpan ditekan. */
    private static final class PendingProof {
        private byte[] bytes;
        private String fileName;
    }

    private void openAddDialog(TransactionType type) {
        boolean income = type == TransactionType.INCOME;
        PendingProof pending = new PendingProof();

        TextField descriptionField = new TextField("Keterangan");
        descriptionField.setWidthFull();
        TextField amountField = new TextField("Jumlah (Rp)");
        amountField.setPlaceholder("contoh: 1500000");
        amountField.setHelperText("Angka bulat dalam rupiah");
        amountField.setWidthFull();

        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024);
        upload.setUploadButton(new Button("Unggah bukti (opsional)", VaadinIcon.UPLOAD.create()));
        upload.addSucceededListener(event -> {
            try {
                pending.bytes = buffer.getInputStream().readAllBytes();
                pending.fileName = event.getFileName();
            } catch (IOException ex) {
                Notifications.error(presenter.showUploadError());
            }
        });
        upload.getElement().addEventListener("file-remove", event -> {
            pending.bytes = null;
            pending.fileName = null;
        });

        ProgressBar progressBar = new ProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        Dialog dialog = new Dialog();
        dialog.addClassName("cashflow-dialog");
        dialog.setHeaderTitle(income ? "Tambah Pemasukan" : "Tambah Pengeluaran");
        dialog.setWidth("520px");
        dialog.setMaxWidth("95vw");
        VerticalLayout form = new VerticalLayout(descriptionField, amountField, upload, progressBar);
        form.setPadding(false);
        dialog.add(form);

        Button saveButton = new Button("Simpan", VaadinIcon.CHECK.create());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClickListener(event -> {
            progressBar.setVisible(true);
            saveButton.setEnabled(false);
            try {
                if (saveTransaction(type, descriptionField.getValue(), amountField.getValue(), pending)) {
                    dialog.close();
                    reload();
                }
            } finally {
                progressBar.setVisible(false);
                saveButton.setEnabled(true);
            }
        });
        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    /** Menyimpan transaksi baru (dan bukti bila ada). Mengembalikan true jika berhasil. */
    private boolean saveTransaction(TransactionType type, String description, String amountText,
                                    PendingProof pending) {
        String proofPath = null;
        try {
            if (pending.bytes != null) {
                proofPath = fileStorage.store(pending.fileName, new ByteArrayInputStream(pending.bytes));
            }
            Transaction created = type == TransactionType.INCOME
                    ? financeUseCase.addIncome(currentUser.getId(), description, amountText, proofPath)
                    : financeUseCase.addExpense(currentUser.getId(), description, amountText, proofPath);
            Notifications.success(presenter.showAddSuccess(created));
            return true;
        } catch (ValidationException ex) {
            discardQuietly(proofPath);
            Notifications.error(presenter.showValidationError(ex.getMessage()));
        } catch (IOException ex) {
            Notifications.error(presenter.showUploadError());
        }
        return false;
    }

    // ---------- Dialog hapus ----------

    private void openDeleteDialog(Transaction transaction) {
        Paragraph message = new Paragraph("Yakin ingin menghapus transaksi \"" + transaction.getDescription()
                + "\" (" + presenter.formatMoney(transaction.getAmount()) + ")?");
        message.addClassName("dialog-message");

        Dialog dialog = new Dialog();
        dialog.addClassName("cashflow-dialog");
        dialog.setHeaderTitle("Hapus Transaksi");
        dialog.setWidth("480px");
        dialog.setMaxWidth("95vw");
        dialog.add(message);

        Button confirmButton = new Button("Hapus", VaadinIcon.TRASH.create(), event -> {
            deleteTransaction(transaction);
            dialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        Button cancelButton = new Button("Batal", VaadinIcon.CLOSE.create(), event -> dialog.close());
        dialog.getFooter().add(cancelButton, confirmButton);
        dialog.open();
    }

    private void deleteTransaction(Transaction transaction) {
        try {
            Transaction removed = financeUseCase.removeTransaction(currentUser.getId(), transaction.getId());
            discardQuietly(removed.getProofPath());
            Notifications.success(presenter.showRemoveSuccess());
        } catch (EntityNotFoundException ex) {
            Notifications.error(presenter.showRemoveFailed(ex.getId()));
        } catch (ValidationException ex) {
            Notifications.error(presenter.showValidationError(ex.getMessage()));
        }
        reload();
    }

    // ---------- Muat data ----------

    private void refreshAll() {
        searchField.clear();
        sortBox.clear();
        reload();
    }

    private void reload() {
        if (currentUser == null) {
            return;
        }
        String keyword = searchField.getValue();
        boolean searching = keyword != null && !keyword.isBlank();
        List<Transaction> transactions =
                financeUseCase.findTransactions(currentUser.getId(), keyword, sortBox.getValue());

        emptyState.setText(searching ? presenter.showSearchEmpty() : presenter.showEmptyData());
        grid.setItems(transactions);

        int userId = currentUser.getId();
        totalCount.setText(String.valueOf(financeUseCase.countTransactions(userId)));
        totalIncome.setText(presenter.formatMoney(financeUseCase.getTotalIncome(userId)));
        totalExpense.setText(presenter.formatMoney(financeUseCase.getTotalExpense(userId)));
        balanceLabel.setText(presenter.formatMoney(financeUseCase.getBalance(userId)));

        String title = searching ? "Hasil pencarian \"" + keyword.trim() + "\"" : "Semua transaksi";
        statusLabel.setText(title + " - " + transactions.size() + " data. "
                + presenter.formatBalance(financeUseCase.getBalance(userId)));
    }

    private void discardQuietly(String path) {
        try {
            fileStorage.delete(path);
        } catch (IOException ex) {
            // File yatim tidak mengganggu fungsi aplikasi; abaikan.
        }
    }
}
