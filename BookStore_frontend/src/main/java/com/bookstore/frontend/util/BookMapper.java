package com.bookstore.frontend.util;

import com.bookstore.frontend.model.BookModel;
import com.bookstore.frontend.model.dto.Response.BookResponseDto;
import com.bookstore.frontend.model.dto.Request.BookUpsertRequestDto;

import java.util.ArrayList;

public class BookMapper {

    public static BookModel toModel(BookResponseDto dto) {
        if (dto == null) {
            return null;
        }

        BookModel model = new BookModel();
        model.setId(dto.getId());
        model.setTitle(dto.getTitle());
        model.setDescription(dto.getDescription());
        model.setImageUrl(dto.getImageUrl());
        model.setQuantity(dto.getQuantity());
        model.setPrice(dto.getSellPrice());
        model.setPublisherName(dto.getPublisherName());

        model.setAuthorNames(dto.getAuthorNames() != null ? new ArrayList<>(dto.getAuthorNames()) : new ArrayList<>());
        model.setCategoryNames(dto.getCategoryNames() != null ? new ArrayList<>(dto.getCategoryNames()) : new ArrayList<>());

        return model;
    }

    public static BookUpsertRequestDto toUpsertRequest(BookModel model) {
        if (model == null) {
            return null;
        }

        BookUpsertRequestDto dto = new BookUpsertRequestDto();
        dto.setTitle(model.getTitle());
        dto.setDescription(model.getDescription());
        dto.setImageUrl(model.getImageUrl());
        dto.setSellPrice(model.getPrice());
        dto.setPublisherId(model.getPublisherId());
        dto.setAuthorIds(model.getAuthorIds());
        dto.setCategoryIds(model.getCategoryIds());

        return dto;
    }
}