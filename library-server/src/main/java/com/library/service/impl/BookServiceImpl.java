package com.library.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.BusinessException;
import com.library.dto.request.BookQueryRequest;
import com.library.dto.request.BookSaveRequest;
import com.library.dto.response.BookDetailResponse;
import com.library.entity.*;
import com.library.mapper.*;
import com.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookMapper bookMapper;
    private final CategoryMapper categoryMapper;
    private final BookReviewMapper reviewMapper;
    private final ReservationMapper reservationMapper;
    private final BorrowRecordMapper borrowRecordMapper;

    @Override
    public Page<Book> page(BookQueryRequest req) {
        LambdaQueryWrapper<Book> qw = new LambdaQueryWrapper<Book>()
                .eq(Book::getStatus, 1);
        if (req.getKeyword() != null && !req.getKeyword().isEmpty())
            qw.and(w -> w.like(Book::getTitle, req.getKeyword()).or().like(Book::getAuthor, req.getKeyword()));
        if (req.getCategoryId() != null) qw.eq(Book::getCategoryId, req.getCategoryId());
        qw.orderByDesc(Book::getCreatedAt);
        return bookMapper.selectPage(new Page<>(req.getPage(), req.getPageSize()), qw);
    }

    @Override
    public BookDetailResponse detail(Long id, Long userId) {
        Book book = bookMapper.selectById(id);
        if (book == null) throw new BusinessException("图书不存在");
        BookDetailResponse resp = new BookDetailResponse();
        BeanUtils.copyProperties(book, resp);
        if (book.getCategoryId() != null) {
            Category cat = categoryMapper.selectById(book.getCategoryId());
            if (cat != null) resp.setCategoryName(cat.getName());
        }
        // 库存状态：有可借副本为 AVAILABLE，无副本但有预约为 RESERVED，否则 UNAVAILABLE
        if (book.getAvailableCopies() > 0) resp.setStockStatus("AVAILABLE");
        else {
            Long r = reservationMapper.selectCount(new LambdaQueryWrapper<Reservation>().eq(Reservation::getBookId, id).eq(Reservation::getStatus, "WAITING"));
            resp.setStockStatus(r > 0 ? "RESERVED" : "UNAVAILABLE");
        }
        // 查询当前用户是否借阅过该书（存在任意状态的借阅记录即视为借阅过，可评论）
        if (userId != null) {
            Long cnt = borrowRecordMapper.selectCount(new LambdaQueryWrapper<BorrowRecord>()
                    .eq(BorrowRecord::getBookId, id)
                    .eq(BorrowRecord::getUserId, userId));
            resp.setHasBorrowed(cnt > 0);
        } else {
            resp.setHasBorrowed(false);
        }
        // 评论列表
        List<BookReview> reviews = reviewMapper.selectList(new LambdaQueryWrapper<BookReview>().eq(BookReview::getBookId, id).orderByDesc(BookReview::getCreatedAt));
        resp.setReviews(reviews.stream().map(r -> {
            BookDetailResponse.ReviewItem ri = new BookDetailResponse.ReviewItem();
            ri.setId(r.getId()); ri.setRating(r.getRating()); ri.setContent(r.getContent()); ri.setCreatedAt(r.getCreatedAt());
            return ri;
        }).collect(Collectors.toList()));
        resp.setReviewCount(reviews.size());
        if (!reviews.isEmpty()) resp.setAvgRating(reviews.stream().mapToInt(BookReview::getRating).average().orElse(0));
        return resp;
    }

    @Override
    public void save(BookSaveRequest req) {
        Book book = new Book();
        BeanUtils.copyProperties(req, book);
        book.setAvailableCopies(req.getTotalCopies());
        book.setStatus(1);
        bookMapper.insert(book);
    }

    @Override
    public void update(Long id, BookSaveRequest req) {
        Book book = bookMapper.selectById(id);
        if (book == null) throw new BusinessException("图书不存在");
        BeanUtils.copyProperties(req, book, "id","availableCopies","createdAt");
        bookMapper.updateById(book);
    }

    @Override
    public void delete(Long id) {
        Book book = bookMapper.selectById(id);
        if (book == null) throw new BusinessException("图书不存在");
        book.setStatus(0);
        bookMapper.updateById(book);
    }

    @Override
    public List<com.library.dto.response.PopularBookVO> popular(int limit) {
        return bookMapper.selectPopular(limit);
    }
}
