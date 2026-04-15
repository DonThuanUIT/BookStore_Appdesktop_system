package com.bookstore.frontend.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NavigationService {

    private static volatile NavigationService instance;
    private StackPane contentArea;

    private static class CachedView {
        final Node node;
        final Object controller;

        CachedView(Node node, Object controller) {
            this.node = node;
            this.controller = controller;
        }
    }

    private final Map<PageType, CachedView> screenCache = new HashMap<>();

    private NavigationService() {}

    public static NavigationService getInstance() {
        if (instance == null) {
            synchronized (NavigationService.class) {
                if (instance == null) {
                    instance = new NavigationService();
                }
            }
        }
        return instance;
    }

    public void setContentArea(StackPane contentArea) {
        this.contentArea = contentArea;
    }

    public void navigateTo(PageType pageType) {
        navigateTo(pageType, null);
    }

    public void navigateTo(PageType pageType, Object data) {
        if (contentArea == null) {
            throw new IllegalStateException("Critical: ContentArea for NavigationService not initialized!");
        }

        CachedView view = screenCache.get(pageType);

        if (view == null) {
            try {
                URL fxmlUrl = getClass().getResource(pageType.getFxmlPath());
                if (fxmlUrl == null) {
                    throw new IllegalArgumentException("Don't have file FXML at: " + pageType.getFxmlPath());
                }

                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Node node = loader.load();
                Object controller = loader.getController();

                view = new CachedView(node, controller);
                screenCache.put(pageType, view);
            } catch (IOException e) {
                throw new RuntimeException("Serious error when loading screen: " + pageType.name(), e);
            }
        }

        if (view.controller instanceof Navigatable) {
            ((Navigatable) view.controller).onNavigate(data);
        }

        contentArea.getChildren().setAll(view.node);
    }

    public void clearCache() {
        screenCache.clear();
    }

    public void clearCache(PageType pageType) {
        screenCache.remove(pageType);
    }
}