package com.library.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.dto.request.BookSaveRequest;
import com.library.dto.request.BookQueryRequest;
import com.library.dto.response.BookDetailResponse;
import com.library.entity.Book;
import com.library.dto.response.PopularBookVO;

import java.util.List;

public interface BookService {
    Page<Book> page(BookQueryRequest req);
    BookDetailResponse detail(Long id, Long userId);
    void save(BookSaveRequest req);
    void update(Long id, BookSaveRequest req);
    void delete(Long id);
    List<PopularBookVO> popular(int limit);
}