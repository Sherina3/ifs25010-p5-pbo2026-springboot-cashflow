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

/** Halaman registrasi (publik). Pengguna yang sudah login dialihkan ke halaman utama. */
@Route("register")
@PageTitle("Daftar - Cashflow")
public class RegisterView extends VerticalLayout implements BeforeEnterObserver {
    private final AuthUseCase authUseCase;
    private final IUserRepository userRepository;
    private final FinancePresenter presenter;

    private final TextField fullName = new TextField("Nama lengkap");
    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final PasswordField confirmPassword = new PasswordField("Ulangi password");
    private final ProgressBar progressBar = new ProgressBar();
    private final Button registerButton = new Button("Daftar");

    public RegisterView(AuthUseCase authUseCase, IUserRepository userRepository, FinancePresenter presenter) {
        this.authUseCase = authUseCase;
        this.userRepository = userRepository;
        this.presenter = presenter;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        fullName.setWidthFull();
        username.setWidthFull();
        username.setHelperText("3-50 karakter: huruf, angka, titik, garis bawah, strip");
        password.setWidthFull();
        password.setHelperText("Minimal 6 karakter");
        confirmPassword.setWidthFull();

        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        registerButton.setWidthFull();
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickShortcut(Key.ENTER);
        registerButton.addClickListener(event -> submit());

        H2 title = new H2("Buat Akun Cashflow");
        Paragraph subtitle = new Paragraph("Daftar untuk mulai mencatat pemasukan dan pengeluaran.");
        RouterLink loginLink = new RouterLink("Sudah punya akun? Masuk", LoginView.class);

        Div card = new Div(title, subtitle, fullName, username, password, confirmPassword,
                progressBar, registerButton, loginLink);
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
        registerButton.setEnabled(false);
        try {
            if (!password.getValue().equals(confirmPassword.getValue())) {
                throw new ValidationException("Konfirmasi password tidak cocok");
            }
            User user = authUseCase.register(username.getValue(), password.getValue(), fullName.getValue());
            AuthSession.login(user);
            Notifications.success(presenter.showRegisterSuccess(user.getFullName()));
            getUI().ifPresent(ui -> ui.navigate(FinanceView.class));
        } catch (ValidationException ex) {
            Notifications.error(presenter.showValidationError(ex.getMessage()));
        } finally {
            progressBar.setVisible(false);
            registerButton.setEnabled(true);
        }
    }
}
