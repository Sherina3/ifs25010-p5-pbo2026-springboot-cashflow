package org.delcom.ifs25010springbootcashflow.framework.view;

import org.delcom.ifs25010springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25010springbootcashflow.domain.entity.User;
import org.delcom.ifs25010springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25010springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25010springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25010springbootcashflow.usecase.AuthUseCase;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

/** Halaman login (publik). Pengguna yang sudah login dialihkan ke halaman utama. */
@Route("login")
@PageTitle("Masuk - Cashflow")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {
    private final AuthUseCase authUseCase;
    private final IUserRepository userRepository;
    private final FinancePresenter presenter;

    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final ProgressBar progressBar = new ProgressBar();
    private final Button loginButton = new Button("Masuk");

    public LoginView(AuthUseCase authUseCase, IUserRepository userRepository, FinancePresenter presenter) {
        this.authUseCase = authUseCase;
        this.userRepository = userRepository;
        this.presenter = presenter;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        username.setId("login-username");
        username.setWidthFull();
        username.setAutofocus(true);
        password.setId("login-password");
        password.setWidthFull();

        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        loginButton.setId("login-submit");
        loginButton.setWidthFull();
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.addClickShortcut(Key.ENTER);
        loginButton.addClickListener(event -> submit());

        H2 title = new H2("Cashflow");
        Paragraph subtitle = new Paragraph("Masuk untuk mengelola catatan keuangan Anda.");
        RouterLink registerLink = new RouterLink("Belum punya akun? Daftar", RegisterView.class);

        Div card = new Div(title, subtitle, username, password, progressBar, loginButton, registerLink);
        card.addClassName("auth-card");
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (AuthSession.currentUser(userRepository).isPresent()) {
            event.forwardTo(FinanceView.class);
        }
    }

    private void submit() {
        progressBar.setVisible(true);
        loginButton.setEnabled(false);
        try {
            User user = authUseCase.login(username.getValue(), password.getValue());
            AuthSession.login(user);
            getUI().ifPresent(ui -> ui.navigate(FinanceView.class));
        } catch (ValidationException ex) {
            Notifications.error(presenter.showValidationError(ex.getMessage()));
        } finally {
            progressBar.setVisible(false);
            loginButton.setEnabled(true);
        }
    }
}
