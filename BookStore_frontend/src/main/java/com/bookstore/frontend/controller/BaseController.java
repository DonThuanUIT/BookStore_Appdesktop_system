package com.bookstore.frontend.controller;

import com.bookstore.frontend.navigation.Navigatable;
import com.bookstore.frontend.navigation.NavigationService;
import com.bookstore.frontend.navigation.PageType;

public abstract class BaseController implements Navigatable {
    protected void navigate(PageType pageType) {
        NavigationService.getInstance().navigateTo(pageType);
    }

    protected void navigate(PageType pageType, Object data) {
        NavigationService.getInstance().navigateTo(pageType, data);
    }

    @Override
    public abstract void onNavigate(Object data);
}
