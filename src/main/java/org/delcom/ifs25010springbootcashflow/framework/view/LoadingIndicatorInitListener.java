package org.delcom.ifs25010springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/**
 * Konfigurasi global indikator loading Vaadin (bar bercahaya di atas layar) dengan jeda responsif.
 * Gaya visualnya ada di styles.css (.v-loading-indicator).
 */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            var configuration = uiEvent.getUI().getLoadingIndicatorConfiguration();
            configuration.setFirstDelay(150);
            configuration.setSecondDelay(600);
            configuration.setThirdDelay(1500);
        });
    }
}
