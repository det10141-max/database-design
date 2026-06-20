package com.library.dto.response;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class DashboardResponse {
    private long totalBooks;
    private long borrowedCount;
    private long overdueCount;
    private long todayBorrow;
    private long totalUsers;
    private long unpaidFines;
    private long activeReservations;
    private long totalPendingFine;
}