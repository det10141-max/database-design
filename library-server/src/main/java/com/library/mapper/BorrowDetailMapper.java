package com.library.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.dto.response.BorrowDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BorrowDetailMapper extends BaseMapper<BorrowDetailVO> {

    @Select("SELECT * FROM v_borrow_detail ${ew.customSqlSegment}")
    IPage<BorrowDetailVO> selectPageView(Page<?> page, @Param(Constants.WRAPPER) Wrapper<BorrowDetailVO> wrapper);
}
