Mô tả

Viết luồng xử lý nghiệp vụ quan trọng nhất: Khách hàng tiến hành thuê xe, cần check tồn tại và cập nhật trạng thái liên dịch vụ (Cross-service).
Tasks

    Nhận request tạo giao dịch (gồm customerId và danh sách carId).
    Dùng CustomerClient check: Customer có tồn tại không?
    Dùng CarClient loop qua các xe để check: Xe có tồn tại và carStatus == 1 (đang rảnh) không? Nếu = 0 ném lỗi.
    Tính totalPrice = (endDate - startDate) * rentingPricePerDay.
    Bọc hàm lại bằng @Transactional. Lưu Transaction và Detail xuống DB.
    Call chéo: Gọi CarClient cập nhật trạng thái tất cả xe vừa thuê thành 0.

Tiêu chí nghiệm thu (AC)

    Nếu 1 xe đã bị thuê, toàn bộ giao dịch bị hủy, trả về lỗi 400 Bad Request.
    Thuê thành công -> DB Renting có data -> DB Car có xe chuyển status = 0.
lam isssue 2