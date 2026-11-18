package org.delcom.ifs25010springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;

/** Kartu konten halaman, dan pembuat kartu ringkasan angka. */
public class PageCard extends Div {

    public PageCard(String title, Component... content) {
        addClassName("page-card");
        if (title != null && !title.isBlank()) {
            H3 heading = new H3(title);
            heading.addClassName("page-card-title");
            add(heading);
        }
        add(content);
    }

    /** Kartu ringkasan (label kecil + nilai besar) berwarna sesuai aksen. */
    public static Div summary(String label, Span value, String cssModifier) {
        Span caption = new Span(label);
        caption.addClassName("summary-label");
        value.addClassName("summary-value");
        Div card = new Div(caption, value);
        card.addClassNames("summary-card", cssModifier);
        return card;
    }
}
