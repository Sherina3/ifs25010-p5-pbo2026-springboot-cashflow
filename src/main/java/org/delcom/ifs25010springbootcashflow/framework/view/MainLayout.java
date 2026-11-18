package org.delcom.ifs25010springbootcashflow.framework.view;

import org.delcom.ifs25010springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25010springbootcashflow.domain.entity.User;
import org.delcom.ifs25010springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25010springbootcashflow.framework.auth.AuthSession;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

import java.util.Optional;

/**
 * Layout utama aplikasi (sidebar + menu avatar) sekaligus penjaga rute terproteksi:
 * pengguna yang belum login dialihkan ke /login.
 */
public class MainLayout extends AppLayout implements BeforeEnterObserver {
    private final IUserRepository userRepository;
    private final FinancePresenter presenter;

    private final Div drawerSlot = new Div();
    private final HorizontalLayout userSlot = new HorizontalLayout();

    public MainLayout(IUserRepository userRepository, FinancePresenter presenter) {
        this.userRepository = userRepository;
        this.presenter = presenter;

        H1 title = new H1("Cashflow");
        title.addClassName("app-title");

        userSlot.setAlignItems(FlexComponent.Alignment.CENTER);
        userSlot.setSpacing(true);

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), title, userSlot);
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.expand(title);
        header.addClassName("app-header");
        addToNavbar(header);

        drawerSlot.addClassName("app-drawer");
        addToDrawer(drawerSlot);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<User> user = AuthSession.currentUser(userRepository);
        if (user.isEmpty()) {
            event.forwardTo(LoginView.class);
            return;
        }
        refreshUser(user.get());
    }

    /** Memuat ulang avatar dan menu setelah profil berubah. */
    public void refreshCurrentUser() {
        AuthSession.currentUser(userRepository).ifPresent(this::refreshUser);
    }

    /** Mencari MainLayout dari sebuah komponen anak lalu menyegarkan avatarnya. */
    static void refreshFrom(Component child) {
        Component current = child;
        while (current != null) {
            if (current instanceof MainLayout layout) {
                layout.refreshCurrentUser();
                return;
            }
            current = current.getParent().orElse(null);
        }
        UI.getCurrent().getPage().reload();
    }

    private void refreshUser(User user) {
        SideNav nav = new SideNav();
        nav.addItem(new SideNavItem("Cashflow", FinanceView.class, VaadinIcon.WALLET.create()));
        nav.addItem(new SideNavItem("Profil", ProfileView.class, VaadinIcon.USER.create()));
        if (user.isAdmin()) {
            nav.addItem(new SideNavItem("Pengguna", UsersView.class, VaadinIcon.USERS.create()));
        }
        drawerSlot.removeAll();
        drawerSlot.add(nav);

        Avatar avatar = new Avatar(user.getFullName());
        if (user.getPhotoPath() != null) {
            avatar.setImage(presenter.uploadUrl(user.getPhotoPath()));
        }
        avatar.addClassName("app-avatar");

        Span name = new Span(user.getFullName());
        name.addClassName("app-user-name");
        Span role = new Span(presenter.formatRole(user.getRole()));
        role.addClassName("app-user-role");
        Div identity = new Div(name, role);
        identity.addClassName("app-user-identity");

        ContextMenu menu = new ContextMenu(avatar);
        menu.setOpenOnClick(true);
        menu.addItem("Profil", event -> UI.getCurrent().navigate(ProfileView.class));
        menu.addItem("Keluar", event -> logout());

        userSlot.removeAll();
        userSlot.add(identity, avatar);
    }

    private void logout() {
        AuthSession.logout();
        UI.getCurrent().getPage().setLocation("/login");
    }
}
