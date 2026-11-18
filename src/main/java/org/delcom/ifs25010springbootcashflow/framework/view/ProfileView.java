package org.delcom.ifs25010springbootcashflow.framework.view;

import org.delcom.ifs25010springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25010springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25010springbootcashflow.domain.entity.User;
import org.delcom.ifs25010springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25010springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25010springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25010springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25010springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/** Pengaturan profil: nama, password, dan foto. */
@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil - Cashflow")
public class ProfileView extends VerticalLayout {
    private final UserUseCase userUseCase;
    private final IUserRepository userRepository;
    private final FinancePresenter presenter;
    private final FileStorage fileStorage;

    private final Avatar avatar = new Avatar();
    private final Paragraph accountInfo = new Paragraph();
    private final TextField fullName = new TextField("Nama lengkap");
    private final PasswordField newPassword = new PasswordField("Password baru (opsional)");
    private final PasswordField confirmPassword = new PasswordField("Ulangi password baru");
    private final ProgressBar progressBar = new ProgressBar();

    private User currentUser;

    public ProfileView(UserUseCase userUseCase, IUserRepository userRepository, FinancePresenter presenter,
                       FileStorage fileStorage) {
        this.userUseCase = userUseCase;
        this.userRepository = userRepository;
        this.presenter = presenter;
        this.fileStorage = fileStorage;

        setPadding(true);

        avatar.setWidth("96px");
        avatar.setHeight("96px");
        fullName.setWidthFull();
        newPassword.setWidthFull();
        newPassword.setHelperText("Kosongkan bila tidak ingin mengganti password");
        confirmPassword.setWidthFull();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        MemoryBuffer buffer = new MemoryBuffer();
        Upload photoUpload = new Upload(buffer);
        photoUpload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        photoUpload.setMaxFiles(1);
        photoUpload.setMaxFileSize(5 * 1024 * 1024);
        photoUpload.setUploadButton(new Button("Unggah foto profil", VaadinIcon.UPLOAD.create()));
        photoUpload.addSucceededListener(event -> savePhoto(buffer, event.getFileName(), photoUpload));

        Button saveButton = new Button("Simpan Profil", VaadinIcon.CHECK.create(), event -> saveProfile());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        VerticalLayout form = new VerticalLayout(avatar, accountInfo, photoUpload, fullName, newPassword,
                confirmPassword, progressBar, saveButton);
        form.setPadding(false);
        form.setMaxWidth("520px");
        add(new PageCard("Profil Saya", form));
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        AuthSession.currentUser(userRepository).ifPresent(user -> {
            currentUser = user;
            showUser(user);
        });
    }

    private void showUser(User user) {
        fullName.setValue(user.getFullName());
        accountInfo.setText("Username: " + user.getUsername() + " | Peran: " + presenter.formatRole(user.getRole()));
        avatar.setName(user.getFullName());
        avatar.setImage(user.getPhotoPath() == null ? null : presenter.uploadUrl(user.getPhotoPath()));
    }

    private void saveProfile() {
        if (currentUser == null) {
            return;
        }
        progressBar.setVisible(true);
        try {
            if (!newPassword.getValue().equals(confirmPassword.getValue())) {
                throw new ValidationException("Konfirmasi password tidak cocok");
            }
            currentUser = userUseCase.updateProfile(currentUser.getId(), fullName.getValue(), newPassword.getValue());
            newPassword.clear();
            confirmPassword.clear();
            showUser(currentUser);
            MainLayout.refreshFrom(this);
            Notifications.success(presenter.showProfileSaved());
        } catch (ValidationException ex) {
            Notifications.error(presenter.showValidationError(ex.getMessage()));
        } catch (EntityNotFoundException ex) {
            Notifications.error(presenter.showValidationError(ex.getMessage()));
        } finally {
            progressBar.setVisible(false);
        }
    }

    private void savePhoto(MemoryBuffer buffer, String originalName, Upload upload) {
        if (currentUser == null) {
            return;
        }
        progressBar.setVisible(true);
        try {
            String oldPath = currentUser.getPhotoPath();
            String storedName = fileStorage.store(originalName, new ByteArrayInputStream(
                    buffer.getInputStream().readAllBytes()));
            currentUser = userUseCase.updatePhoto(currentUser.getId(), storedName);
            fileStorage.delete(oldPath);
            showUser(currentUser);
            MainLayout.refreshFrom(this);
            Notifications.success(presenter.showPhotoSaved());
        } catch (ValidationException ex) {
            Notifications.error(presenter.showValidationError(ex.getMessage()));
        } catch (EntityNotFoundException | IOException ex) {
            Notifications.error(presenter.showUploadError());
        } finally {
            progressBar.setVisible(false);
            upload.clearFileList();
        }
    }
}
