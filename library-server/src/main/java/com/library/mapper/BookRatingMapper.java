package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.dto.response.BookRatingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BookRatingMapper extends BaseMapper<BookRatingVO> {

    @Select("SELECT * FROM v_book_rating WHERE book_id = #{bookId}")
    BookRatingVO selectByBookId(Long bookId);
}
