package org.delcom.ifs25010springbootcashflow.framework.view;

import org.delcom.ifs25010springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25010springbootcashflow.domain.entity.Role;
import org.delcom.ifs25010springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25010springbootcashflow.domain.entity.User;
import org.delcom.ifs25010springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25010springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25010springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25010springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25010springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25010springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Peninjauan transaksi milik pengguna lain secara read-only (khusus ADMIN).
 * Pengguna dipilih lewat query parameter {@code ?userId=}.
 */
@Route(value = "user-transactions", layout = MainLayout.class)
@PageTitle("Transaksi Pengguna - Cashflow")
public class UserTransactionsView extends VerticalLayout implements BeforeEnterObserver {
    private final FinanceUseCase financeUseCase;
    private final UserUseCase userUseCase;
    private final IUserRepository userRepository;
    private final FinancePresenter presenter;

    private final Grid<Transaction> grid = new Grid<>(Transaction.class, false);
    private final H3 heading = new H3();
    private final Paragraph statusLabel = new Paragraph();
    private final Span totalCount = new Span("0");
    private final Span totalIncome = new Span();
    private final Span totalExpense = new Span();
    private final Span balanceLabel = new Span();

    public UserTransactionsView(FinanceUseCase financeUseCase, UserUseCase userUseCase,
                                IUserRepository userRepository, FinancePresenter presenter) {
        this.financeUseCase = financeUseCase;
        this.userUseCase = userUseCase;
        this.userRepository = userRepository;
        this.presenter = presenter;

        setPadding(true);
        configureGrid();

        HorizontalLayout summary = new HorizontalLayout(
                PageCard.summary("Total Transaksi", totalCount, "summary-blue"),
                PageCard.summary("Total Pemasukan", totalIncome, "summary-green"),
                PageCard.summary("Total Pengeluaran", totalExpense, "summary-red"),
                PageCard.summary("Saldo", balanceLabel, "summary-amber"));
        summary.setWidthFull();
        summary.addClassName("summary-row");

        Button backButton = new Button("Kembali ke Daftar Pengguna", VaadinIcon.ARROW_LEFT.create(),
                event -> UI.getCurrent().navigate(UsersView.class));
        add(backButton, heading, summary, new PageCard("Transaksi (hanya baca)", statusLabel, grid));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<User> actor = AuthSession.currentUser(userRepository);
        if (actor.isEmpty()) {
            event.forwardTo(LoginView.class);
            return;
        }
        if (actor.get().getRole() != Role.ADMIN) {
            event.forwardTo(FinanceView.class);
            return;
        }
        Optional<Integer> targetId = event.getLocation().getQueryParameters().getSingleParameter("userId")
                .flatMap(UserTransactionsView::parseId);
        if (targetId.isEmpty()) {
            event.forwardTo(UsersView.class);
            return;
        }
        load(actor.get(), targetId.get());
    }

    private static Optional<Integer> parseId(String text) {
        try {
            return Optional.of(Integer.parseInt(text));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private void load(User actor, int targetId) {
        try {
            User target = userUseCase.getUser(targetId);
            List<Transaction> transactions = financeUseCase.viewTransactionsOf(actor, targetId);
            heading.setText("Transaksi " + target.getFullName() + " (" + target.getUsername() + ")");
            grid.setItems(transactions);
            totalCount.setText(String.valueOf(transactions.size()));
            totalIncome.setText(presenter.formatMoney(financeUseCase.getTotalIncome(targetId)));
            totalExpense.setText(presenter.formatMoney(financeUseCase.getTotalExpense(targetId)));
            balanceLabel.setText(presenter.formatMoney(financeUseCase.getBalance(targetId)));
            statusLabel.setText(transactions.isEmpty()
                    ? presenter.showEmptyData()
                    : transactions.size() + " transaksi. " + presenter.formatBalance(financeUseCase.getBalance(targetId)));
        } catch (EntityNotFoundException | ValidationException ex) {
            Notifications.error(presenter.showValidationError(ex.getMessage()));
        }
    }

    private void configureGrid() {
        grid.addColumn(Transaction::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0).setSortable(true)
                .setComparator(Comparator.comparingInt(Transaction::getId));
        grid.addColumn(Transaction::getDescription).setHeader("Keterangan").setFlexGrow(1).setSortable(true)
                .setComparator(Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER));
        grid.addColumn(transaction -> presenter.formatMoney(transaction.getAmount())).setHeader("Jumlah")
                .setAutoWidth(true).setSortable(true).setComparator(Comparator.comparingLong(Transaction::getAmount));
        grid.addColumn(transaction -> presenter.formatType(transaction.getType())).setHeader("Tipe")
                .setAutoWidth(true).setSortable(true).setComparator(Comparator.comparing(Transaction::getType));
        grid.addComponentColumn(transaction -> GridStyles.proofCell(
                transaction.getProofPath() == null ? null : presenter.uploadUrl(transaction.getProofPath()),
                "Bukti transaksi")).setHeader("Bukti").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(transaction -> presenter.formatDateTime(transaction.getCreatedAt())).setHeader("Waktu")
                .setAutoWidth(true).setSortable(true).setComparator(Comparator.comparing(Transaction::getCreatedAt));
        grid.setEmptyStateComponent(new Span(presenter.showEmptyData()));
        GridStyles.apply(grid);
    }
}
