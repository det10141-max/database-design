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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 借阅服务单元测试
 * 覆盖借阅、归还、续借等核心业务逻辑
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("借阅服务测试")
class BorrowServiceImplTest {

    @Mock private BorrowRecordMapper borrowRecordMapper;
    @Mock private BorrowDetailMapper borrowDetailMapper;
    @Mock private BookMapper bookMapper;
    @Mock private UserMapper userMapper;
    @Mock private ReservationMapper reservationMapper;
    @Mock private FineMapper fineMapper;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private BorrowServiceImpl borrowService;

    private AppProperties appProperties;

    @BeforeEach
    void setUp() {
        // 初始化业务配置
        appProperties = new AppProperties();
        AppProperties.Borrow borrowCfg = new AppProperties.Borrow();
        borrowCfg.setDurationDays(30);
        borrowCfg.setMaxRenew(2);
        borrowCfg.setRenewDays(30);
        appProperties.setBorrow(borrowCfg);
        AppProperties.Fine fineCfg = new AppProperties.Fine();
        fineCfg.setDailyRate(0.5);
        appProperties.setFine(fineCfg);
        AppProperties.Reserve reserveCfg = new AppProperties.Reserve();
        reserveCfg.setPickupDays(3);
        appProperties.setReserve(reserveCfg);

        // 通过反射注入 appProperties（因为 @RequiredArgsConstructor 要求 final）
        try {
            var field = BorrowServiceImpl.class.getDeclaredField("appProperties");
            field.setAccessible(true);
            field.set(borrowService, appProperties);
        } catch (Exception e) {
            fail("注入 appProperties 失败: " + e.getMessage());
        }
    }

    /** 构造一个正常的用户 */
    private User normalUser() {
        User u = new User();
        u.setId(1L);
        u.setUsername("reader1");
        u.setStatus(1);
        u.setMaxBorrow(5);
        return u;
    }

    /** 构造一本可借的书 */
    private Book availableBook() {
        Book b = new Book();
        b.setId(10L);
        b.setTitle("测试图书");
        b.setStatus(1);
        b.setAvailableCopies(3);
        b.setTotalCopies(5);
        b.setPrice(new BigDecimal("50.00"));
        return b;
    }

    @Test
    @DisplayName("正常借阅：库存扣减、记录插入、通知发送")
    void borrow_success() {
        when(userMapper.selectById(1L)).thenReturn(normalUser());
        when(borrowRecordMapper.selectCount(any())).thenReturn(0L);
        when(fineMapper.selectCount(any())).thenReturn(0L);
        when(bookMapper.selectByIdForUpdate(10L)).thenReturn(availableBook());

        borrowService.borrow(1L, 10L);

        // 验证库存扣减
        verify(bookMapper).updateById(argThat(b -> b.getAvailableCopies() == 2));
        // 验证借阅记录插入
        verify(borrowRecordMapper).insert(argThat(r ->
            r.getUserId().equals(1L) && r.getBookId().equals(10L) && "BORROWING".equals(r.getStatus())));
        // 验证通知发送
        verify(notificationService).create(eq(1L), eq("BORROW"), anyString(), anyString(), any());
    }

    @Test
    @DisplayName("借阅失败：用户状态异常（禁用）")
    void borrow_userDisabled() {
        User u = normalUser();
        u.setStatus(0);
        when(userMapper.selectById(1L)).thenReturn(u);

        BusinessException ex = assertThrows(BusinessException.class, () -> borrowService.borrow(1L, 10L));
        assertTrue(ex.getMessage().contains("用户状态异常"));
        verify(bookMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("借阅失败：存在逾期未还图书")
    void borrow_hasOverdue() {
        when(userMapper.selectById(1L)).thenReturn(normalUser());
        when(borrowRecordMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> borrowService.borrow(1L, 10L));
        assertTrue(ex.getMessage().contains("逾期未还"));
    }

    @Test
    @DisplayName("借阅失败：已达借阅上限")
    void borrow_reachMaxBorrow() {
        User u = normalUser();
        u.setMaxBorrow(2);
        when(userMapper.selectById(1L)).thenReturn(u);
        // 第一次 selectCount 返回逾期数 0，第二次返回当前借阅数 2
        when(borrowRecordMapper.selectCount(any())).thenReturn(0L).thenReturn(2L);

        BusinessException ex = assertThrows(BusinessException.class, () -> borrowService.borrow(1L, 10L));
        assertTrue(ex.getMessage().contains("最大借阅数"));
    }

    @Test
    @DisplayName("借阅失败：存在未缴罚款")
    void borrow_hasUnpaidFine() {
        when(userMapper.selectById(1L)).thenReturn(normalUser());
        when(borrowRecordMapper.selectCount(any())).thenReturn(0L);
        when(fineMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> borrowService.borrow(1L, 10L));
        assertTrue(ex.getMessage().contains("未缴罚款"));
    }

    @Test
    @DisplayName("借阅失败：图书无库存")
    void borrow_noAvailableCopy() {
        when(userMapper.selectById(1L)).thenReturn(normalUser());
        when(borrowRecordMapper.selectCount(any())).thenReturn(0L);
        when(fineMapper.selectCount(any())).thenReturn(0L);
        Book b = availableBook();
        b.setAvailableCopies(0);
        when(bookMapper.selectByIdForUpdate(10L)).thenReturn(b);

        BusinessException ex = assertThrows(BusinessException.class, () -> borrowService.borrow(1L, 10L));
        assertTrue(ex.getMessage().contains("无可借副本"));
    }

    @Test
    @DisplayName("归还成功：无预约时库存加回")
    void returnBook_noReservation() {
        BorrowRecord record = new BorrowRecord();
        record.setId(100L);
        record.setUserId(1L);
        record.setBookId(10L);
        record.setStatus("BORROWING");
        record.setBorrowDate(LocalDateTime.now().minusDays(10));
        record.setDueDate(LocalDateTime.now().plusDays(20));
        when(borrowRecordMapper.selectById(100L)).thenReturn(record);
        when(reservationMapper.selectList(any())).thenReturn(java.util.Collections.emptyList());
        Book b = availableBook();
        when(bookMapper.selectById(10L)).thenReturn(b);

        borrowService.returnBook(100L);

        // 验证状态更新为 RETURNED
        verify(borrowRecordMapper).updateById(argThat(r -> "RETURNED".equals(r.getStatus())));
        // 验证库存加回
        verify(bookMapper).updateById(argThat(book -> book.getAvailableCopies() == 4));
    }

    @Test
    @DisplayName("归还成功：有预约时库存锁定给预约者，不加回公共池")
    void returnBook_withReservation() {
        BorrowRecord record = new BorrowRecord();
        record.setId(100L);
        record.setUserId(1L);
        record.setBookId(10L);
        record.setStatus("BORROWING");
        record.setBorrowDate(LocalDateTime.now().minusDays(10));
        record.setDueDate(LocalDateTime.now().plusDays(20));
        when(borrowRecordMapper.selectById(100L)).thenReturn(record);

        // 构造两个预约者
        Reservation r1 = new Reservation();
        r1.setId(1L);
        r1.setUserId(2L);
        r1.setBookId(10L);
        r1.setQueuePosition(1);
        r1.setStatus("WAITING");
        Reservation r2 = new Reservation();
        r2.setId(2L);
        r2.setUserId(3L);
        r2.setBookId(10L);
        r2.setQueuePosition(2);
        r2.setStatus("WAITING");
        when(reservationMapper.selectList(any())).thenReturn(java.util.List.of(r1, r2));

        borrowService.returnBook(100L);

        // 验证第一个预约者状态改为 FULFILLED
        verify(reservationMapper).updateById(argThat(r -> "FULFILLED".equals(r.getStatus()) && r.getId().equals(1L)));
        // 验证第二个预约者排队位置前移
        verify(reservationMapper).updateById(argThat(r -> r.getId().equals(2L) && r.getQueuePosition() == 1));
        // 验证通知发送
        verify(notificationService).create(eq(2L), eq("RESERVE_READY"), anyString(), anyString(), eq(1L));
    }

    @Test
    @DisplayName("归还失败：记录不是借阅中状态")
    void returnBook_invalidStatus() {
        BorrowRecord record = new BorrowRecord();
        record.setId(100L);
        record.setStatus("RETURNED");
        when(borrowRecordMapper.selectById(100L)).thenReturn(record);

        BusinessException ex = assertThrows(BusinessException.class, () -> borrowService.returnBook(100L));
        assertTrue(ex.getMessage().contains("不是借阅中状态"));
    }

    @Test
    @DisplayName("续借失败：已逾期不可续借")
    void renew_overdue() {
        BorrowRecord record = new BorrowRecord();
        record.setId(100L);
        record.setUserId(1L);
        record.setStatus("BORROWING");
        record.setDueDate(LocalDateTime.now().minusDays(5));
        record.setRenewCount(0);
        when(borrowRecordMapper.selectById(100L)).thenReturn(record);

        BusinessException ex = assertThrows(BusinessException.class, () -> borrowService.renew(1L, 100L));
        assertTrue(ex.getMessage().contains("已逾期"));
    }

    @Test
    @DisplayName("续借失败：有他人预约不可续借")
    void renew_hasReservation() {
        BorrowRecord record = new BorrowRecord();
        record.setId(100L);
        record.setUserId(1L);
        record.setStatus("BORROWING");
        record.setDueDate(LocalDateTime.now().plusDays(20));
        record.setRenewCount(0);
        when(borrowRecordMapper.selectById(100L)).thenReturn(record);
        when(reservationMapper.selectCount(any())).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class, () -> borrowService.renew(1L, 100L));
        assertTrue(ex.getMessage().contains("他人预约"));
    }
}
