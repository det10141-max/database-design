package com.library.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.dto.request.AnnouncementRequest;
import com.library.entity.Announcement;
public interface AnnouncementService {
    void create(Long publisherId, AnnouncementRequest req);
    void update(Long id, AnnouncementRequest req);
    void delete(Long id);
    Page<Announcement> page(int page, int pageSize);
}