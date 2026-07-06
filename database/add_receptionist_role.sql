-- [PHAN QUYEN LE TAN] Chay mot lan tren database hien tai.
-- roleID=3 duoc code su dung cho Le tan.
INSERT INTO `Role` (`roleID`, `roleName`)
SELECT 3, 'RECEPTIONIST'
WHERE NOT EXISTS (
    SELECT 1 FROM `Role` WHERE `roleID` = 3
);

-- Gan mot tai khoan Employee hien co thanh Le tan (thay ID truoc khi chay):
-- UPDATE Employee SET roleID = 3 WHERE employeeID = YOUR_EMPLOYEE_ID;
