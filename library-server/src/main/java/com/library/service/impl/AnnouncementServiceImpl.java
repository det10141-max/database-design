package com.library.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.BusinessException;
import com.library.dto.request.AnnouncementRequest;
import com.library.entity.Announcement;
import com.library.mapper.AnnouncementMapper;
import com.library.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {
    private final AnnouncementMapper announcementMapper;

    @Override
    public void create(Long publisherId, AnnouncementRequest req) {
        Announcement a = new Announcement();
        BeanUtils.copyProperties(req, a);
        a.setPublisherId(publisherId);
        announcementMapper.insert(a);
    }

    @Override
    public void update(Long id, AnnouncementRequest req) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) throw new BusinessException("公告不存在");
        BeanUtils.copyProperties(req, a, "id","publisherId","createdAt");
        announcementMapper.updateById(a);
    }

    @Override
    public void delete(Long id) {
        announcementMapper.deleteById(id);
    }

    @Override
    public Page<Announcement> page(int page, int pageSize) {
        return announcementMapper.selectPage(new Page<>(page,pageSize), new LambdaQueryWrapper<Announcement>().eq(Announcement::getStatus,1).orderByDesc(Announcement::getIsPinned).orderByDesc(Announcement::getCreatedAt));
    }
}
