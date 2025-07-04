USE ebookstore;

------------------------------------------------
-- 1. BẢNG USERS
------------------------------------------------
INSERT INTO users (avatar, first_name, last_name, username, email, password, birth_of_date, phone_number) VALUES
('avatar_admin.png', 'Quản trị', 'Hệ thống', 'admin', 'admin@example.com', '$2a$12$ARKga2.yt8CtkQSTi5uDR.cR2tYQBNdym4Gx/p.zecNqhRB.vjWhe', '1980-01-01', '0123456789'),
('avatar_customer.png', 'Khách', 'Hàng', 'customer', 'customer@example.com', '$2a$12$ARKga2.yt8CtkQSTi5uDR.cR2tYQBNdym4Gx/p.zecNqhRB.vjWhe', '1990-05-05', '0987654321');

------------------------------------------------
-- 2. BẢNG ADDRESSES
------------------------------------------------
INSERT INTO addresses (user_id, address_line, ward, district, city, country, postal_code) VALUES
(1, 'Số 1 Đường Lãnh đạo', 'Phường Trung tâm', 'Quận Hoàn Kiếm', 'Hà Nội', 'Việt Nam', '100000'),
(2, 'Số 10 Đường Hòa Bình', 'Phường An Lạc', 'Quận 7', 'TP. Hồ Chí Minh', 'Việt Nam', '700000');

------------------------------------------------
-- 3. BẢNG CATEGORIES
------------------------------------------------
INSERT INTO categories (name, description) VALUES
('Sách dạy lập trình', 'Các sách hướng dẫn lập trình các ngôn ngữ khác nhau.'),
('Sách tiếng Anh', 'Các sách học tiếng Anh từ cơ bản đến nâng cao.');

------------------------------------------------
-- 4. BẢNG SUB_CATEGORIES
------------------------------------------------
-- Các sub-category cho "Sách dạy lập trình" (parent_id = 1)
INSERT INTO sub_categories (parent_id, name, description) VALUES
(1, 'PHP', 'Các sách về PHP.'),
(1, 'Java', 'Các sách về Java.'),
(1, 'HTML, CSS', 'Các sách về HTML và CSS.');
-- Các sub-category cho "Sách tiếng Anh" (parent_id = 2)
INSERT INTO sub_categories (parent_id, name, description) VALUES
(2, 'Ngữ pháp tiếng Anh', 'Các sách học ngữ pháp tiếng Anh.'),
(2, 'Từ vựng tiếng Anh', 'Các sách học từ vựng tiếng Anh.');

------------------------------------------------
-- 5. BẢNG BOOKS (thông tin tổng quát)
------------------------------------------------
-- Dữ liệu cho sub-category "PHP" (sub_category_id = 1)
INSERT INTO books (title, author, cover, sub_category_id, price) VALUES
('Lập trình PHP căn bản', 'Nguyễn Văn A', '', 1, 300000),
('PHP nâng cao', 'Trần Thị B', '', 1, 300000),
-- Dữ liệu cho sub-category "Java" (sub_category_id = 2)
('Java cho người mới bắt đầu', 'Lê Văn C', '', 2, 300000),
('Lập trình Java nâng cao', 'Phạm Thị D', '', 2, 300000),
-- Dữ liệu cho sub-category "HTML, CSS" (sub_category_id = 3)
('Học HTML & CSS', 'Đỗ Văn E', '', 3, 300000),
('Thiết kế web với HTML và CSS', 'Ngô Thị F', '', 3, 300000),
-- Dữ liệu cho sub-category "Ngữ pháp tiếng Anh" (sub_category_id = 4)
('Ngữ pháp tiếng Anh cơ bản', 'Hoàng Văn G', '', 4, 300000),
('Ngữ pháp tiếng Anh nâng cao', 'Trịnh Thị H', '', 4, 300000),
-- Dữ liệu cho sub-category "Từ vựng tiếng Anh" (sub_category_id = 5)
('Từ vựng tiếng Anh hàng ngày', 'Vũ Văn I', '', 5, 300000),
('Từ vựng tiếng Anh giao tiếp', 'Phan Thị J', '', 5, 300000);

------------------------------------------------
-- 6. BẢNG BOOK_DETAIL (thông tin chi tiết)
------------------------------------------------
INSERT INTO book_detail (book_id, description, summary, isbn, publisher, publication_date, pages, file_url) VALUES
(1, 'Hướng dẫn cơ bản về lập trình PHP dành cho người mới.', 'Sách cung cấp kiến thức từ A đến Z về PHP.', 'ISBN001', 'NXB Giáo Dục', '2020-01-15', 250, ''),
(2, 'Các kỹ thuật nâng cao trong PHP.', 'Sách cung cấp kiến thức chuyên sâu về lập trình PHP.', 'ISBN002', 'NXB Công Nghệ', '2021-03-10', 300, ''),
(3, 'Giới thiệu cơ bản về Java cho người mới.', 'Sách dễ hiểu, phù hợp cho người bắt đầu học Java.', 'ISBN003', 'NXB Lập Trình', '2019-07-20', 280, ''),
(4, 'Các chủ đề nâng cao trong lập trình Java.', 'Sách dành cho lập trình viên muốn nâng cao kỹ năng Java.', 'ISBN004', 'NXB Công Nghệ', '2020-11-05', 320, ''),
(5, 'Hướng dẫn cơ bản về HTML và CSS.', 'Sách dành cho người mới làm quen với thiết kế web.', 'ISBN005', 'NXB Thiết Kế', '2018-04-12', 200, ''),
(6, 'Sách chuyên sâu về thiết kế web với HTML và CSS.', 'Hướng dẫn chi tiết cách tạo giao diện web hiện đại.', 'ISBN006', 'NXB Sáng Tạo', '2022-02-18', 220, ''),
(7, 'Giới thiệu các quy tắc ngữ pháp tiếng Anh cơ bản.', 'Sách hướng dẫn người học tiếng Anh những cấu trúc cơ bản.', 'ISBN007', 'NXB Ngoại Ngữ', '2017-09-25', 180, ''),
(8, 'Các cấu trúc ngữ pháp phức tạp trong tiếng Anh.', 'Sách dành cho người học tiếng Anh muốn nâng cao kỹ năng ngữ pháp.', 'ISBN008', 'NXB Giáo Dục', '2021-06-30', 240, ''),
(9, 'Tập hợp từ vựng tiếng Anh thông dụng hàng ngày.', 'Sách hỗ trợ người học mở rộng vốn từ vựng tiếng Anh.', 'ISBN009', 'NXB Ngoại Ngữ', '2016-03-14', 150, ''),
(10, 'Các từ vựng cần thiết cho giao tiếp tiếng Anh.', 'Sách tập trung vào từ vựng trong giao tiếp hàng ngày.', 'ISBN010', 'NXB Giáo Dục', '2018-12-01', 170, '');

------------------------------------------------
-- 7. BẢNG BOOK_IMAGES
------------------------------------------------
-- Để trống vì dữ liệu gốc không cung cấp URL hình ảnh cụ thể
INSERT INTO book_images (book_id, image_url, alt_text) VALUES
(1, '', 'Hình ảnh bìa phụ của "Lập trình PHP căn bản"'),
(2, '', 'Hình ảnh bìa phụ của "PHP nâng cao"');

------------------------------------------------
-- 8. BẢNG REVIEWS
------------------------------------------------
INSERT INTO reviews (user_id, book_id, rating, comment) VALUES
(2, 1, 4, 'Sách rất dễ hiểu, phù hợp cho người mới bắt đầu.'),
(2, 3, 5, 'Sách cực kỳ hữu ích để học Java từ cơ bản.');

------------------------------------------------
-- 9. BẢNG WISHLIST
------------------------------------------------
INSERT INTO wishlist (user_id, book_id) VALUES
(2, 1),
(2, 3);

------------------------------------------------
-- 10. BẢNG CART
------------------------------------------------
INSERT INTO cart (user_id, total) VALUES
(1, 0),
(2, 600000); -- Tổng giá trị giỏ hàng của customer dựa trên cart_item

------------------------------------------------
-- 11. BẢNG CART_ITEM
------------------------------------------------
INSERT INTO cart_item (cart_id, book_id, quantity, price) VALUES
(2, 1, 2, 300000); -- 2 cuốn sách "Lập trình PHP căn bản", tổng 600,000 VNĐ

------------------------------------------------
-- 12. BẢNG ORDER_DETAILS
------------------------------------------------
INSERT INTO order_details (user_id, total, order_address, status, sub_total) VALUES
(2, 600000, 'Số 10 Đường Hòa Bình, Phường An Lạc, Quận 7, TP. Hồ Chí Minh', 'pending', 600000);

------------------------------------------------
-- 13. BẢNG ORDER_ITEM
------------------------------------------------
INSERT INTO order_item (order_id, book_id, quantity, price) VALUES
(1, 1, 2, 300000); -- 2 cuốn sách "Lập trình PHP căn bản", tổng 600,000 VNĐ

------------------------------------------------
-- 14. BẢNG PAYMENT_DETAILS
------------------------------------------------
INSERT INTO payment_details (order_id, amount, provider, status) VALUES
(1, 600000, 'Ví điện tử', 'Đã thanh toán');

------------------------------------------------
-- 15. BẢNG ROLES
------------------------------------------------
INSERT INTO roles (name, description) VALUES
('admin', 'Quyền quản trị hệ thống.'),
('customer', 'Quyền khách hàng.');

------------------------------------------------
-- 16. BẢNG USER_ROLES
------------------------------------------------
INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- Admin có vai trò admin
(2, 2); -- Customer có vai trò customer

------------------------------------------------
-- 17. BẢNG COUPONS
------------------------------------------------
INSERT INTO coupons (id, code, description, discount_type, discount_value, min_purchase, max_discount, start_date, end_date, usage_limit, times_used, is_active) VALUES
(12, 'WELCOME2024', 'Giảm giá 20% cho đơn hàng đầu tiên', 'PERCENTAGE', 20.00, 500000.00, 200000.00, '2024-10-01 00:00:00', '2026-12-31 23:59:59', 1000, 0, TRUE),
(13, 'SALE2024FIX', 'Giảm cố định 50,000 VNĐ cho đơn hàng từ 300,000 VNĐ', 'FIXED_AMOUNT', 50000.00, 300000.00, NULL, '2024-10-01 00:00:00', '2026-12-31 23:59:59', 500, 0, TRUE),
(14, 'BLACKFRIDAY24', 'Giảm 30% cho đơn hàng từ 1,000,000 VNĐ', 'PERCENTAGE', 30.00, 1000000.00, 500000.00, '2024-11-01 00:00:00', '2026-12-31 23:59:59', 200, 0, TRUE),
(15, 'NEWYEAR2025', 'Giảm cố định 100,000 VNĐ cho mọi đơn hàng', 'FIXED_AMOUNT', 100000.00, 0.00, NULL, '2025-01-01 00:00:00', '2026-12-31 23:59:59', 300, 0, TRUE),
(16, 'VIP2024', 'Giảm 25% cho khách hàng VIP, đơn từ 2,000,000 VNĐ', 'PERCENTAGE', 25.00, 2000000.00, 1000000.00, '2024-10-01 00:00:00', '2026-12-31 23:59:59', 50, 0, TRUE);

------------------------------------------------
-- 18. BẢNG USER_COUPONS (Ví dụ sử dụng coupon)
------------------------------------------------
-- Giả sử user_id = 2 sử dụng coupon WELCOME2024 cho order_id = 1
INSERT INTO user_coupons (user_id, coupon_id, order_id, used_at) VALUES
(2, 12, 1, CURRENT_TIMESTAMP);