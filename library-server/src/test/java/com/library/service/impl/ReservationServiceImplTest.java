package com.library.service.impl;

import com.library.common.BusinessException;
import com.library.config.AppProperties;
import com.library.entity.*;
import com.library.mapper.*;
import com.library.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 预约服务单元测试
 * 覆盖预约、取消、取书借阅等核心业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("预约服务测试")
class ReservationServiceImplTest {

    @Mock private ReservationMapper reservationMapper;
    @Mock private BookMapper bookMapper;
    @Mock private BorrowRecordMapper borrowRecordMapper;
    @Mock private UserMapper userMapper;
    @Mock private FineMapper fineMapper;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        try {
            var field = ReservationServiceImpl.class.getDeclaredField("appProperties");
            field.setAccessible(true);
            field.set(reservationService, appProperties);
        } catch (Exception e) {
            fail("注入 appProperties 失败: " + e.getMessage());
        }
    }

    /** 构造无库存的图书（可预约） */
    private Book unavailableBook() {
        Book b = new Book();
        b.setId(10L);
        b.setTitle("测试图书");
        b.setAvailableCopies(0);
        b.setTotalCopies(3);
        b.setPrice(new BigDecimal("50.00"));
        return b;
    }

    /** 构造正常的用户 */
    private User normalUser(Long id) {
        User u = new User();
        u.setId(id);
        u.setStatus(1);
        u.setMaxBorrow(5);
        return u;
    }

    @Test
    @DisplayName("预约成功：排队位置正确计算")
    void reserve_success() {
        when(bookMapper.selectById(10L)).thenReturn(unavailableBook());
        // 第一次 selectCount：重复预约校验返回 0；第二次：计算排队位置返回 2
        when(reservationMapper.selectCount(any())).thenReturn(0L).thenReturn(2L);

        reservationService.reserve(1L, 10L);

        // 验证排队位置为 3（当前 2 个等待 + 1）
        verify(reservationMapper).insert(argThat(r ->
            r.getUserId().equals(1L) && r.getBookId().equals(10L) &&
            r.getQueuePosition() == 3 && "WAITING".equals(r.getStatus())));
        // 验证通知发送
        verify(notificationService).create(eq(1L), eq("RESERVE"), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("预约失败：图书有可借副本")
    void reserve_bookAvailable() {
        Book b = unavailableBook();
        b.setAvailableCopies(2);
        when(bookMapper.selectById(10L)).thenReturn(b);

        BusinessException ex = assertThrows(BusinessException.class, () -> reservationService.reserve(1L, 10L));
        assertTrue(ex.getMessage().contains("可借副本"));
        verify(reservationMapper, never()).insert(any());
    }

    @Test
    @DisplayName("预约失败：重复预约")
    void reserve_duplicate() {
        when(bookMapper.selectById(10L)).thenReturn(unavailableBook());
        when(reservationMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> reservationService.reserve(1L, 10L));
        assertTrue(ex.getMessage().contains("已预约"));
    }

    @Test
    @DisplayName("取消预约：后续等待者排队位置前移")
    void cancel_shiftQueue() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setUserId(1L);
        r.setBookId(10L);
        r.setQueuePosition(2);
        r.setStatus("WAITING");
        when(reservationMapper.selectById(1L)).thenReturn(r);

        Reservation r3 = new Reservation();
        r3.setId(3L);
        r3.setQueuePosition(3);
        r3.setStatus("WAITING");
        Reservation r4 = new Reservation();
        r4.setId(4L);
        r4.setQueuePosition(4);
        r4.setStatus("WAITING");
        when(reservationMapper.selectList(any())).thenReturn(List.of(r3, r4));

        reservationService.cancel(1L, 1L);

        // 验证状态改为 CANCELLED
        verify(reservationMapper).updateById(argThat(x -> "CANCELLED".equals(x.getStatus()) && x.getId().equals(1L)));
        // 验证后续排队位置前移
        verify(reservationMapper).updateById(argThat(x -> x.getId().equals(3L) && x.getQueuePosition() == 2));
        verify(reservationMapper).updateById(argThat(x -> x.getId().equals(4L) && x.getQueuePosition() == 3));
    }

    @Test
    @DisplayName("取消失败：非等待状态不可取消")
    void cancel_invalidStatus() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setUserId(1L);
        r.setStatus("FULFILLED");
        when(reservationMapper.selectById(1L)).thenReturn(r);

        BusinessException ex = assertThrows(BusinessException.class, () -> reservationService.cancel(1L, 1L));
        assertTrue(ex.getMessage().contains("等待中"));
    }

    @Test
    @DisplayName("取书借阅成功：库存不重复扣减，创建借阅记录")
    void fulfill_success() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setUserId(1L);
        r.setBookId(10L);
        r.setStatus("FULFILLED");
        r.setPickupDeadline(LocalDateTime.now().plusDays(2));
        when(reservationMapper.selectById(1L)).thenReturn(r);
        when(userMapper.selectById(1L)).thenReturn(normalUser(1L));
        when(borrowRecordMapper.selectCount(any())).thenReturn(0L);
        when(fineMapper.selectCount(any())).thenReturn(0L);
        when(bookMapper.selectById(10L)).thenReturn(unavailableBook());

        reservationService.fulfill(1L);

        // 验证创建借阅记录
        verify(borrowRecordMapper).insert(argThat(br ->
            br.getUserId().equals(1L) && br.getBookId().equals(10L) && "BORROWING".equals(br.getStatus())));
        // 验证预约状态改为 BORROWED
        verify(reservationMapper).updateById(argThat(x -> "BORROWED".equals(x.getStatus())));
        // 关键验证：库存不扣减（bookMapper.updateById 不应被调用用于扣减库存）
        verify(bookMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("取书失败：预约未到馆")
    void fulfill_notReady() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setUserId(1L);
        r.setStatus("WAITING");
        when(reservationMapper.selectById(1L)).thenReturn(r);

        BusinessException ex = assertThrows(BusinessException.class, () -> reservationService.fulfill(1L));
        assertTrue(ex.getMessage().contains("未到馆"));
    }

    @Test
    @DisplayName("取书失败：超过取书保留期限")
    void fulfill_expired() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setUserId(1L);
        r.setStatus("FULFILLED");
        r.setPickupDeadline(LocalDateTime.now().minusDays(1));
        when(reservationMapper.selectById(1L)).thenReturn(r);

        BusinessException ex = assertThrows(BusinessException.class, () -> reservationService.fulfill(1L));
        assertTrue(ex.getMessage().contains("保留期限"));
    }

    @Test
    @DisplayName("取书失败：用户有逾期未还图书")
    void fulfill_hasOverdue() {
        Reservation r = new Reservation();
        r.setId(1L);
        r.setUserId(1L);
        r.setStatus("FULFILLED");
        r.setPickupDeadline(LocalDateTime.now().plusDays(2));
        when(reservationMapper.selectById(1L)).thenReturn(r);
        when(userMapper.selectById(1L)).thenReturn(normalUser(1L));
        when(borrowRecordMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> reservationService.fulfill(1L));
        assertTrue(ex.getMessage().contains("逾期未还"));
    }

    @Test
    @DisplayName("查询预约列表：批量填充书名")
    void listByUser_fillBookTitle() {
        Reservation r1 = new Reservation();
        r1.setId(1L);
        r1.setUserId(1L);
        r1.setBookId(10L);
        Reservation r2 = new Reservation();
        r2.setId(2L);
        r2.setUserId(1L);
        r2.setBookId(20L);
        when(reservationMapper.selectList(any())).thenReturn(List.of(r1, r2));

        Book b1 = new Book(); b1.setId(10L); b1.setTitle("图书A");
        Book b2 = new Book(); b2.setId(20L); b2.setTitle("图书B");
        when(bookMapper.selectBatchIds(any())).thenReturn(List.of(b1, b2));

        List<Reservation> result = reservationService.listByUser(1L);

        assertEquals(2, result.size());
        assertEquals("图书A", result.get(0).getBookTitle());
        assertEquals("图书B", result.get(1).getBookTitle());
    }
}
