# Bản đồ code phần Table, QR Order, Payment và Invoice

Tài liệu này dùng để tra nhanh khi bảo vệ. Trong code Java, có thể dùng
`Ctrl + F` với các từ khóa `LUỒNG`, tên `action` hoặc tên hàm bên dưới.

## 1. Quy ước kiến trúc

Luồng chung của dự án:

`JSP -> Controller -> DAO -> Database -> Controller -> JSP/Redirect`

- **JSP**: hiển thị và gửi tham số.
- **Controller**: kiểm tra session, role, CSRF, validate và chọn nghiệp vụ.
- **DAO**: chứa SQL và transaction.
- **Model**: đối tượng trung gian mang dữ liệu giữa DAO, Controller và JSP.

## 2. Table Management

Thứ tự vật lý trong `TableManageController` và `TableDAO` đã được đồng bộ:

1. `LIST TABLE`
2. `ADD TABLE`
3. `EDIT TABLE`
4. `TABLE DETAIL + QR`
5. `SAVE ADD / EDIT`
6. `VALIDATION / SHARED HELPERS`

### Xem danh sách bàn

1. `table_list.jsp` gửi GET tới `/owner/manage-table`.
2. `TableManageController.doGet()` đọc filter và page.
3. `TableDAO.countSearchTables()` đếm kết quả.
4. `TableDAO.searchTablesPaging()` lấy đúng trang dữ liệu.
5. Controller forward lại `table_list.jsp`.

### Thêm hoặc sửa bàn

1. Owner mở form bằng action `add` hoặc `edit`.
2. `TableManageController.doGet()` tải `table_form.jsp`.
3. Form POST về `TableManageController.doPost()`.
4. Controller kiểm tra CSRF, role Owner và validate dữ liệu.
5. Gọi `TableDAO.addTable()` hoặc `TableDAO.updateTable()`.
6. Khi thêm mới, `addTable()` tự sinh `QRCodeToken` bằng UUID.
7. Khi sửa, câu SQL không cập nhật `QRCodeToken`, nên QR cũ vẫn dùng được.

### Xem chi tiết và QR

1. `TableManageController.doGet()` nhận action `detail` và id.
2. `TableDAO.getTableByTableID()` lấy dữ liệu.
3. `table_form.jsp` ở mode `detail` hiển thị read-only và tạo ảnh QR.

## 3. Quét QR và vào bàn

1. QR gọi `/scan?token=...`.
2. `ScanQRController.doGet()` gọi `TableDAO.getTableByToken()`.
3. `OrderDAO.getActiveOrderByTableId()` tìm phiên order của bàn.
4. Nếu đúng cookie HOST, khôi phục session HOST.
5. Người quét sau được chuyển tới `join_table.jsp`.
6. `TableJoinController` và `TableJoinRequestDAO` xử lý xin vào bàn.
7. Nếu bàn chưa có order, `ScanQRController` tạo order mới và hostToken.
8. Khi lễ tân mở/check-in bàn, `StaffTableDAO` đặt `isStaffConfirmed = 1`.
9. Khách được chuyển tới `/menu`.

Từ khóa tra nhanh:

- `HOST_OF_TABLE_`: cookie nhận diện chủ bàn.
- `roleInTable`: vai trò HOST hoặc GUEST trong session.
- `pendingOrderID`: order mà thiết bị đang chờ được duyệt.
- `isStaffConfirmed`: nhân viên đã cho phép bàn gọi món hay chưa.

## 4. Menu và thêm món vào giỏ

1. `MenuItemController.doGet()` kiểm tra trạng thái bàn.
2. Controller đọc search, category, cooking method, price và page.
3. Controller forward `user/menu.jsp`.
4. Form món ăn POST `/order` với action `add`.
5. `OrderController.doPost()` lấy lại món và giá từ DB.
6. Món được thêm vào `sessionCart`, chưa trừ kho và chưa ghi OrderItem.
7. `returnUrl` đưa khách về đúng trang/filter menu đang xem.

Từ khóa:

- `action=add`
- `sessionCart`
- `getMenuItemById`
- `returnUrl`
- `canModifyOrder`

## 5. Cart và gửi bếp

Trong `OrderController` và `OrderDAO`, tìm các section theo thứ tự:

1. `VIEW CART`
2. `SESSION CART: ADD/UPDATE/REMOVE`
3. `SEND TO KITCHEN`
4. `CHECK / REQUEST PAYMENT`
5. `SHARED HELPERS`

### Xem giỏ

`OrderController.doGet()` ghép hai nhóm:

- `dbOrderItems`: món đã gửi bếp, nằm trong database.
- `sessionCart`: món mới chọn, còn được sửa hoặc xóa.

### Sửa và xóa

- action `update`: đổi số lượng trong `sessionCart`.
- action `remove`: xóa món khỏi `sessionCart`.

### Gửi bếp

1. `cart.jsp` gửi action `sendToKitchen` và các `selectedItems`.
2. Controller gọi `canModifyOrder()` để kiểm tra bàn đã mở.
3. Với từng món được chọn, gọi `OrderDAO.sendItemToKitchen()`.
4. DAO mở transaction và khóa dòng DailyInventory.
5. Nếu đủ kho: trừ kho, ghi OrderItem và commit.
6. Nếu thiếu kho: rollback và giữ món trong `sessionCart`.

Đây là hàm quan trọng nhất của luồng gọi món:

`OrderDAO.sendItemToKitchen()`

## 6. Yêu cầu tính tiền

1. Chỉ HOST thấy nút yêu cầu tính tiền trong `cart.jsp`.
2. action `checkoutTotal` gọi `OrderDAO.requestCheckout()`.
3. Order chuyển sang trạng thái chờ thanh toán.
4. Controller tạo notification cho nhân viên phụ trách.
5. Session `checkoutWaiting` bật popup chờ.
6. action `checkPaymentStatus` kiểm tra staff đã thanh toán chưa.

## 7. Nhân viên chốt hóa đơn

1. Nhân viên mở chi tiết order tại `staff/order-detail.jsp`.
2. `StaffTableController` kiểm tra order thuộc nhân viên.
3. Nhân viên chuyển tới `/checkout`.
4. `CheckoutController.processCheckoutDisplay()` tải order và món.
5. `createOrGetMealInvoice()` tạo hoặc dùng lại invoice unpaid.
6. `processPaymentConfirm()` nhận phương thức cash hoặc VNPay.

## 8. Thanh toán

### Tiền mặt

`CheckoutController.processPaymentConfirm()` gọi
`InvoicesDAO.updatePaymentSuccessAndCleaningTable()`.

Transaction này thực hiện đồng thời:

1. Ghi bảng Payments.
2. Đổi Invoice thành `paid`.
3. Đổi Order thành `completed`.
4. Đổi tableStatus thành `cleaning`.

### VNPay

1. `PaymentController.doGet()` tạo URL và chữ ký VNPay.
2. VNPay trả về `/vnpay_return`.
3. `PaymentReturnController.doGet()` kiểm tra chữ ký và số tiền.
4. Callback hợp lệ gọi
   `InvoicesDAO.updatePaymentSuccessAndCleaningTable()`.
5. Chuyển tới `/payment-info`.

## 9. Invoice Management của Owner

Thứ tự vật lý trong `InvoicesDAO`:

1. `OWNER - INVOICE LIST`
2. `OWNER - INVOICE DETAIL`
3. `CHECKOUT - CREATE / LINK INVOICE`
4. `PAYMENT - UPDATE STATUS`
5. báo cáo doanh thu

### Danh sách

1. `/owner/invoices` gọi `AdminInvoiceController.doGet()`.
2. Controller kiểm tra role Owner.
3. Validate ngày và status.
4. `InvoicesDAO.getTotalFilteredInvoices()` đếm bản ghi.
5. `InvoicesDAO.getFilteredInvoices()` lấy một trang.
6. Forward `owner/invoices.jsp`.

### Chi tiết và in

1. `/owner/invoice-detail?id=...` gọi
   `AdminInvoiceDetailController.doGet()`.
2. `InvoicesDAO.getInvoiceById()` lấy invoice.
3. `OrderDAO.getOrderByInvoiceId()` lấy order liên kết.
4. DAO lấy OrderItem/MenuItem.
5. Forward `owner/invoice-detail.jsp`.
6. CSS `@media print` chỉ hiển thị `#printableInvoice`.

## 10. Các điểm bảo mật thường được hỏi

- **CSRF**: `CsrfUtil` tạo token theo session; POST thay đổi dữ liệu phải gửi
  đúng token.
- **Không tin dữ liệu client**: giá món được đọc lại từ DB.
- **Authorization**: Owner, Receptionist, Staff và HOST được kiểm tra riêng.
- **Transaction**: gửi bếp và hoàn tất thanh toán phải thành công toàn bộ hoặc
  rollback toàn bộ.
- **Idempotency thanh toán**: invoice đã paid không được xử lý lần hai.
- **HOST cookie**: đặt HttpOnly để JavaScript không đọc được token.
