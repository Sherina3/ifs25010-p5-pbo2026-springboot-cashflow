package org.delcom.ifs25010springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;

/** Gaya grid yang konsisten dan sel thumbnail bukti transaksi. */
final class GridStyles {

    private GridStyles() {
    }

    static void apply(Grid<?> grid) {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.addClassName("cashflow-grid");
        grid.setMinHeight("320px");
        grid.setWidthFull();
    }

    /** Thumbnail yang dapat diklik untuk membuka modal zoom, atau "-" bila tidak ada gambar. */
    static Component proofCell(String imageUrl, String title) {
        if (imageUrl == null) {
            return new Span("-");
        }
        Image thumbnail = new Image(imageUrl, title);
        thumbnail.addClassName("proof-thumbnail");
        thumbnail.addClickListener(event -> new ImagePreviewDialog(imageUrl, title).open());
        return thumbnail;
    }
}
