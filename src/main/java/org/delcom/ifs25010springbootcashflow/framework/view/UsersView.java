package org.delcom.ifs25010springbootcashflow.framework.view;

import org.delcom.ifs25010springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25010springbootcashflow.domain.entity.Role;
import org.delcom.ifs25010springbootcashflow.domain.entity.User;
import org.delcom.ifs25010springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25010springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25010springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25010springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

/** Daftar seluruh pengguna (khusus ADMIN). Role USER dialihkan ke halaman utama. */
@Route(value = "users", layout = MainLayout.class)
@PageTitle("Pengguna - Cashflow")
public class UsersView extends VerticalLayout implements BeforeEnterObserver {
    private final UserUseCase userUseCase;
    private final IUserRepository userRepository;
    private final FinancePresenter presenter;

    private final Grid<User> grid = new Grid<>(User.class, false);

    public UsersView(UserUseCase userUseCase, IUserRepository userRepository, FinancePresenter presenter) {
        this.userUseCase = userUseCase;
        this.userRepository = userRepository;
        this.presenter = presenter;

        setPadding(true);

        grid.addColumn(User::getId).setHeader("ID").setAutoWidth(true).setFlexGrow(0).setSortable(true)
                .setComparator(Comparator.comparingInt(User::getId));
        grid.addColumn(User::getUsername).setHeader("Username").setSortable(true)
                .setComparator(Comparator.comparing(User::getUsername, String.CASE_INSENSITIVE_ORDER));
        grid.addColumn(User::getFullName).setHeader("Nama lengkap").setFlexGrow(1).setSortable(true)
                .setComparator(Comparator.comparing(User::getFullName, String.CASE_INSENSITIVE_ORDER));
        grid.addColumn(user -> presenter.formatRole(user.getRole())).setHeader("Peran").setAutoWidth(true)
                .setSortable(true).setComparator(Comparator.comparing(User::getRole));
        grid.addComponentColumn(user -> GridStyles.proofCell(
                user.getPhotoPath() == null ? null : presenter.uploadUrl(user.getPhotoPath()), "Foto profil"))
                .setHeader("Foto").setAutoWidth(true).setFlexGrow(0);
        grid.addComponentColumn(this::actionCell).setHeader("Tindakan").setAutoWidth(true).setFlexGrow(0);
        GridStyles.apply(grid);

        add(new PageCard("Daftar Pengguna", grid));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<User> user = AuthSession.currentUser(userRepository);
        if (user.isEmpty()) {
            event.forwardTo(LoginView.class);
        } else if (user.get().getRole() != Role.ADMIN) {
            event.forwardTo(FinanceView.class);
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        AuthSession.currentUser(userRepository).ifPresent(actor -> {
            try {
                grid.setItems(userUseCase.listUsers(actor));
            } catch (ValidationException ex) {
                Notifications.error(presenter.showValidationError(ex.getMessage()));
            }
        });
    }

    private Button actionCell(User user) {
        Button viewButton = new Button("Lihat Transaksi", VaadinIcon.EYE.create(), event ->
                UI.getCurrent().navigate("user-transactions",
                        QueryParameters.simple(Map.of("userId", String.valueOf(user.getId())))));
        viewButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        return viewButton;
    }
}
