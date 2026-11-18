package org.delcom.ifs25010springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Modal preview gambar dengan zoom: klik gambar untuk toggle 1.0x dan 1.8x,
 * atau pakai tombol Perkecil / Perbesar / Tutup di footer (rata kanan).
 */
public class ImagePreviewDialog extends Dialog {
    private static final double MIN_ZOOM = 0.6;
    private static final double MAX_ZOOM = 3.0;
    private static final double STEP = 0.4;
    private static final double CLICK_ZOOM = 1.8;

    private final Image image;
    private double zoom = 1.0;

    public ImagePreviewDialog(String imageUrl, String title) {
        addClassName("cashflow-dialog");
        setHeaderTitle(title);
        setWidth("760px");
        setMaxWidth("95vw");

        image = new Image(imageUrl, title);
        image.addClassName("preview-image");
        image.addClickListener(event -> toggleZoom());

        Div viewport = new Div(image);
        viewport.addClassName("preview-viewport");
        add(viewport);

        Button zoomOut = new Button("Perkecil", VaadinIcon.SEARCH_MINUS.create(), event -> changeZoom(-STEP));
        Button zoomIn = new Button("Perbesar", VaadinIcon.SEARCH_PLUS.create(), event -> changeZoom(STEP));
        Button close = new Button("Tutup", VaadinIcon.CLOSE.create(), event -> close());
        getFooter().add(zoomOut, zoomIn, close);

        applyZoom();
    }

    private void toggleZoom() {
        zoom = zoom > 1.0 ? 1.0 : CLICK_ZOOM;
        applyZoom();
    }

    private void changeZoom(double delta) {
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom + delta));
        applyZoom();
    }

    private void applyZoom() {
        image.getStyle().set("width", Math.round(zoom * 100) + "%");
        image.getStyle().set("cursor", zoom > 1.0 ? "zoom-out" : "zoom-in");
    }
}
