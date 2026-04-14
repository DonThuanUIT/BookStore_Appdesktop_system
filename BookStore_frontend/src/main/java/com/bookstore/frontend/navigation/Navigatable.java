package com.bookstore.frontend.navigation;
/*
    This Interface helps Controller receive data and reset state every time it is navigated to.
*/
public interface Navigatable {
    void onNavigate(Object data);
}
