package com.library.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.library.entity.Fine;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FineMapper extends BaseMapper<Fine> {

    @Select("SELECT COALESCE(SUM(amount), 0) FROM fines WHERE status = 'UNPAID'")
    long selectTotalUnpaid();

    /** 行级锁查询罚款记录，用于缴费操作时防并发重复执行 */
    @Select("SELECT * FROM fines WHERE id = #{id} FOR UPDATE")
    Fine selectByIdForUpdate(Long id);
}
