-- =============================================
-- Restaurant Reservation and Table Service System
-- SQLServer
-- =============================================

IF DB_ID('Restaurant_Reservation_and_Table_Service_System') IS NULL
BEGIN
    CREATE DATABASE Restaurant_Reservation_and_Table_Service_System;
END
GO

USE Restaurant_Reservation_and_Table_Service_System;
GO

-- =============================================
-- NHÓM 1: NHÂN VIÊN
-- =============================================

CREATE TABLE Role (
  roleID    INT PRIMARY KEY IDENTITY(1,1),
  roleName  VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE Employee (
  employeeID  INT PRIMARY KEY IDENTITY(1,1),
  roleID      INT NOT NULL,
  password    VARCHAR(255) NOT NULL,
  fullName    VARCHAR(150) NOT NULL,
  dob         DATE,
  phoneNumber VARCHAR(20) UNIQUE,   -- for only contact
  email       VARCHAR(150) NOT NULL UNIQUE,
  salary      DECIMAL(12,2),
  isActive    TINYINT NOT NULL DEFAULT 1,
  address     VARCHAR(255),
  image       VARCHAR(255),
  createdAt    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  lastPasswordChangedAt      DATETIME,
  mustChangePassword        TINYINT NOT NULL DEFAULT 1,     -- 1 = must change after login first time

  FOREIGN KEY (roleID) REFERENCES Role(roleID)
);

CREATE TABLE ShiftTemplates (
  templateID INT PRIMARY KEY IDENTITY(1,1),
  shiftName  VARCHAR(100) NOT NULL,
  startTime  TIME NOT NULL,
  endTime    TIME NOT NULL
);

CREATE TABLE EmployeeShifts (
  shiftID       INT PRIMARY KEY IDENTITY(1,1),
  templateID    INT NOT NULL,
  employeeID    INT NOT NULL,
  workDate      DATE NOT NULL,
  checkInTime   DATETIME,
  checkOutTime  DATETIME,
  status        VARCHAR(20) NOT NULL DEFAULT 'scheduled', -- scheduled / completed / absent / late
  
  FOREIGN KEY (templateID)  REFERENCES ShiftTemplates(templateID),
  FOREIGN KEY (employeeID)  REFERENCES Employee(employeeID)
);

CREATE TABLE ShiftSwapRequests (
  swapID            INT PRIMARY KEY IDENTITY(1,1),
  requesterShiftID  INT NOT NULL,
  targetShiftID     INT NOT NULL,
  approvedByID      INT,
  status            VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending / approved / rejected
  reason            VARCHAR(MAX),
  createdAt         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (requesterShiftID) REFERENCES EmployeeShifts(shiftID),
  FOREIGN KEY (targetShiftID)    REFERENCES EmployeeShifts(shiftID),
  FOREIGN KEY (approvedByID)     REFERENCES Employee(employeeID)
);

-- =============================================
-- NHÓM 2: KHÁCH HÀNG
-- =============================================

CREATE TABLE Customer (
  customerID  INT PRIMARY KEY IDENTITY(1,1),
  userName    VARCHAR(100) NOT NULL UNIQUE,
  password    VARCHAR(255),     -- NULL if login with Google
  phoneNumber VARCHAR(20) UNIQUE,
  email       VARCHAR(150) UNIQUE,
  createdAt   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  loginProvider VARCHAR(20) NOT NULL DEFAULT 'local'    -- local / google
);

-- =============================================
-- NHÓM 3: BÀN ĂN & ĐẶT BÀN
-- =============================================

CREATE TABLE [Table] (
  tableID        INT PRIMARY KEY IDENTITY(1,1),
  employeeID     INT,
  reservationID  INT,
  currentStaffID INT,
  tableName      VARCHAR(100) NOT NULL,
  capacity       INT NOT NULL,
  QRCodeToken    VARCHAR(255) NOT NULL UNIQUE,
  areaType       VARCHAR(20) NOT NULL DEFAULT 'public',    -- public / private
  isActive       TINYINT NOT NULL DEFAULT 1,

  FOREIGN KEY (employeeID)    REFERENCES Employee(employeeID),
  FOREIGN KEY (currentStaffID) REFERENCES Employee(employeeID)
);

CREATE TABLE BusinessSchedule (
  scheduleID    INT PRIMARY KEY IDENTITY(1,1),
  dayOfWeek     VARCHAR(20),
  specificDate  DATE,
  openTime      TIME,
  closeTime     TIME,
  isClosed      TINYINT NOT NULL DEFAULT 0,
  reason        VARCHAR(255),
  updatedAt     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Reservation (
  reservationID      INT PRIMARY KEY IDENTITY(1,1),
  customerID         INT NOT NULL,
  reservationDateTime    DATETIME NOT NULL,
  reservationStatus  VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending / confirmed / cancelled / completed
  hasPreOrder        TINYINT NOT NULL DEFAULT 0,
  depositAmount      DECIMAL(12,2),
  depositStatus      VARCHAR(20) NOT NULL DEFAULT 'unpaid',  -- unpaid / paid / refunded
  cancelledAt        DATETIME,
  cancellationReason VARCHAR(255),
  tableStatus    VARCHAR(20) NOT NULL DEFAULT 'available', -- available / occupied / reserved / cleaning
  FOREIGN KEY (customerID) REFERENCES Customer(customerID)
);

-- FK reservationID trên bảng Table trỏ về Reservation (thêm sau khi Reservation đã tạo)
ALTER TABLE [Table]
  ADD FOREIGN KEY (reservationID) REFERENCES Reservation(reservationID);

CREATE TABLE ReservationPolicy (
  policyID    INT PRIMARY KEY IDENTITY(1,1),
  policyKey   VARCHAR(100) NOT NULL UNIQUE, -- deposit_rate / cancel_hours / max_guests
  policyValue VARCHAR(100) NOT NULL,
  description VARCHAR(255),
  updatedAt   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================
-- NHÓM 4: THỰC ĐƠN & ĐẶT MÓN
-- =============================================

CREATE TABLE MenuCategory (
  categoryID    INT PRIMARY KEY IDENTITY(1,1),
  categoryName  VARCHAR(100) NOT NULL
);

CREATE TABLE MenuItem (
  itemID          INT PRIMARY KEY IDENTITY(1,1),
  categoryID      INT NOT NULL,
  itemName        VARCHAR(150) NOT NULL,
  description     VARCHAR(500),
  price           DECIMAL(10,2) NOT NULL,
  discountPercent DECIMAL(5,2) NOT NULL DEFAULT 0.00,
  discountedPrice DECIMAL(10,2),
  image           VARCHAR(255),
  isAvailable     TINYINT NOT NULL DEFAULT 1,
  allergyNotes    VARCHAR(MAX),

  FOREIGN KEY (categoryID) REFERENCES MenuCategory(categoryID)
);

CREATE TABLE [Order] (
  orderID             INT PRIMARY KEY IDENTITY(1,1),
  customerID          INT,
  reservationID       INT,
  tableID             INT,
  invoiceID           INT,
  tableStatus         VARCHAR(20) NOT NULL DEFAULT 'available', -- available / occupied / reserved / cleaning
  orderStatus         VARCHAR(20) NOT NULL DEFAULT 'pending', -- pending / confirmed / preparing / served / checkout
  totalAmount         DECIMAL(12,2),
  checkoutRequestAt   DATETIME,
  isStaffConfirmed    TINYINT NOT NULL DEFAULT 0,
  createdAt           DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (reservationID) REFERENCES Reservation(reservationID),
  FOREIGN KEY (tableID)       REFERENCES [Table](tableID),
  FOREIGN KEY (customerID)    REFERENCES Customer(customerID)
);

CREATE TABLE OrderItem (
  orderItemID INT PRIMARY KEY IDENTITY(1,1),
  orderID     INT NOT NULL,
  itemID      INT NOT NULL,
  quantity    INT NOT NULL DEFAULT 1,
  note        VARCHAR(255),

  FOREIGN KEY (orderID) REFERENCES [Order](orderID),
  FOREIGN KEY (itemID)  REFERENCES MenuItem(itemID)
);

-- =============================================
-- NHÓM 5: HÓA ĐƠN & THANH TOÁN
-- =============================================

CREATE TABLE Invoices (
  invoiceID        INT PRIMARY KEY IDENTITY(1,1),
  invoiceNumber    VARCHAR(50) NOT NULL UNIQUE,
  paymentMethod    VARCHAR(20),                             -- cash / card / qr
  subTotal         DECIMAL(12,2) NOT NULL,
  taxAmount        DECIMAL(12,2) NOT NULL DEFAULT 0,
  depositDeducted  DECIMAL(12,2) NOT NULL DEFAULT 0,
  finalAmount      DECIMAL(12,2) NOT NULL,                 -- subTotal + taxAmount - depositDeducted
  issuedDate       DATE NOT NULL DEFAULT GETDATE(),
  status           VARCHAR(20) NOT NULL DEFAULT 'unpaid'   -- unpaid / paid / partial
);

-- FK invoiceID trên Order (thêm sau khi Invoices đã tạo)
ALTER TABLE [Order]
  ADD FOREIGN KEY (invoiceID) REFERENCES Invoices(invoiceID);

CREATE TABLE Payments (
  paymentID       INT PRIMARY KEY IDENTITY(1,1),
  invoiceID       INT NOT NULL,
  transactionCode VARCHAR(255),
  paymentGateway  VARCHAR(50),   -- vnpay / momo / cash
  amount          DECIMAL(12,2) NOT NULL,
  status          VARCHAR(20) NOT NULL DEFAULT 'pending',  -- pending / success / failed / refunded
  paidAt          DATETIME,

  FOREIGN KEY (invoiceID) REFERENCES Invoices(invoiceID)
);

-- =============================================
-- NHÓM 6: ĐÁNH GIÁ & PHẢN HỒI
-- =============================================

CREATE TABLE Reviews (
  reviewID   INT PRIMARY KEY IDENTITY(1,1),
  customerID INT NOT NULL,
  rating     INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
  comment    VARCHAR(1000),
  createdAt  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (customerID) REFERENCES Customer(customerID)
);

CREATE TABLE Feedback (
  feedbackID    INT PRIMARY KEY IDENTITY(1,1),
  employeeID    INT,
  customerID    INT NOT NULL,
  orderID       INT,
  title         VARCHAR(255),
  createdAt     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  content       VARCHAR(1000),
  replyContent  VARCHAR(1000),
  repliedAt     DATETIME,

  FOREIGN KEY (employeeID) REFERENCES Employee(employeeID),
  FOREIGN KEY (customerID) REFERENCES Customer(customerID),
  FOREIGN KEY (orderID) REFERENCES [Order](orderID)
);

-- =============================================
-- NHÓM 7: THÔNG BÁO
-- =============================================

CREATE TABLE Notifications (
  notificationID INT PRIMARY KEY IDENTITY(1,1),
  recipientID    INT NOT NULL,
  recipientType  VARCHAR(20) NOT NULL,  -- customer / staff / owner
  type           VARCHAR(100) NOT NULL, -- reservation_confirmed / order_ready / shift_reminder...
  message        VARCHAR(MAX) NOT NULL,
  isRead         TINYINT NOT NULL DEFAULT 0,
  createdAt      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);