package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BookMapper extends BaseMapper<Book> {

    /** 行级锁查询，用于借书时防并发超借 */
    @Select("SELECT * FROM books WHERE id = #{id} FOR UPDATE")
    Book selectByIdForUpdate(Long id);

    /** 热门借阅 TOP N */
    @Select("SELECT b.id AS bookId, b.title, b.author, COUNT(br.id) AS borrowCount " +
            "FROM books b LEFT JOIN borrow_records br ON b.id = br.book_id " +
            "GROUP BY b.id, b.title, b.author ORDER BY borrowCount DESC LIMIT #{limit}")
    java.util.List<com.library.dto.response.PopularBookVO> selectPopular(@org.apache.ibatis.annotations.Param("limit") int limit);

    /** 调用 fn_can_borrow 函数判断用户借阅资格 */
    @Select("SELECT fn_can_borrow(#{userId})")
    String checkBorrowEligibility(@org.apache.ibatis.annotations.Param("userId") Long userId);
}
