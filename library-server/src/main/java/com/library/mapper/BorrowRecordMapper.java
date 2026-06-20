package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.BorrowRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BorrowRecordMapper extends BaseMapper<BorrowRecord> {

    /** 行级锁查询借阅记录，用于归还/丢失操作时防并发重复执行 */
    @Select("SELECT * FROM borrow_records WHERE id = #{id} FOR UPDATE")
    BorrowRecord selectByIdForUpdate(Long id);
}
