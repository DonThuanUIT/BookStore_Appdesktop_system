# Task: Random sidebar images in CartView

## Steps
- [x] Inspect CartView.fxml and CartController.java
- [x] Check BE books endpoint supports paging and returns imageUrl
- [x] Add `ImageView fx:id="imgRandomBook"` to left sidebar in `CartView.fxml`
- [x] Implement random loader + 10s cycling using `BookApiService.fetchBooks(0,50)` in `CartController.java`
- [x] Ensure fallback to DEFAULT_COVER_URL when no imageUrl available
- [ ] Validate by running frontend and opening CartView


