// Đảm bảo DOM đã load hoàn toàn
        $(document).ready(function() {
            console.log('jQuery loaded:', typeof $ !== 'undefined');
            console.log('Bootstrap loaded:', typeof bootstrap !== 'undefined');
            
            // Xử lý modal khi mở
            $('#bookModal').on('show.bs.modal', function () {
                console.log('Modal đang mở...');
                
                // Reset form khi mở modal thêm mới
                if (!$(this).data('edit-mode')) {
                    $('#bookForm')[0].reset();
                    $('#bookForm input[name="id"]').val('');
                    $('#bookModalLabel').text('Thêm sách mới');
                }
            });

            $('#bookModal').on('shown.bs.modal', function () {
                console.log('Modal đã mở thành công!');
            });

            // Reset add form when modal is opened
            $('#addBookModal').on('show.bs.modal', function () {
                $('#addBookForm')[0].reset();
                $('#addBookForm input[name="id"]').val('');
            });
        });

        // Hàm fill form khi sửa
        function fillEditForm(id) {
            console.log('Editing book ID:', id);
            
            // Kiểm tra jQuery có hoạt động không
            if (typeof $ === 'undefined') {
                alert('jQuery chưa được load!');
                return;
            }

            $.ajax({
                url: '/admin/books/edit/' + id,
                type: 'GET',
                success: function(book) {
                    console.log('Dữ liệu sách:', book);
                    
                    // Điền dữ liệu vào form
                    $('#bookForm input[name="id"]').val(book.id);
                    $('#bookForm input[name="title"]').val(book.title);
                    $('#bookForm input[name="author"]').val(book.author);
                    $('#bookForm input[name="price"]').val(book.price);
                    
                    // Điền dữ liệu danh mục con
                    if (book.subCategory) {
                        $('#bookForm select[name="subCategory.id"]').val(book.subCategory.id);
                    }
                    
                    // Điền dữ liệu chi tiết sách
                    if (book.bookDetail) {
                        const detail = book.bookDetail;
                        $('#bookForm textarea[name="bookDetail.description"]').val(detail.description);
                        $('#bookForm textarea[name="bookDetail.summary"]').val(detail.summary);
                        $('#bookForm input[name="bookDetail.isbn"]').val(detail.isbn);
                        $('#bookForm input[name="bookDetail.publisher"]').val(detail.publisher);
                        $('#bookForm input[name="bookDetail.publicationDate"]').val(detail.publicationDate);
                        $('#bookForm input[name="bookDetail.pages"]').val(detail.pages);
                    }

                    // Cập nhật tiêu đề modal và hiển thị
                    $('#bookModalLabel').text('Sửa sách');
                    $('#bookModal').data('edit-mode', true).modal('show');
                },
                error: function(xhr, status, error) {
                    console.error('Lỗi khi lấy dữ liệu sách:', error);
                    alert('Không thể tải thông tin sách. Vui lòng thử lại!');
                }
            });
        }

        // Sửa lại hàm xóa sách
        function deleteBook(id) {
            if (confirm('Bạn có chắc chắn muốn xóa sách này không?')) {
                $.ajax({
                    url: '/admin/books/delete/' + id,
                    type: 'GET',
                    success: function() {
                        // Reload trang sau khi xóa thành công
                        window.location.reload();
                    },
                    error: function(xhr, status, error) {
                        console.error('Lỗi khi xóa sách:', error);
                        alert('Không thể xóa sách. Vui lòng thử lại!');
                    }
                });
            }
        }

        // Test function để kiểm tra modal
        function testModal() {
            $('#bookModal').modal('show');
        }

        function loadBookForEdit(event, id) {
            event.preventDefault(); // Ngăn chặn hành vi mặc định
            
            $.get('/admin/books/edit/' + id, function(book) {
                // Fill form data
                $('#bookForm input[name="id"]').val(book.id);
                $('#bookForm input[name="title"]').val(book.title);
                $('#bookForm input[name="author"]').val(book.author);
                $('#bookForm input[name="price"]').val(book.price);
                
                // Fill subCategory
                if (book.subCategory) {
                    $('#bookForm select[name="subCategory.id"]').val(book.subCategory.id);
                }
                
                // Fill book details
                if (book.bookDetail) {
                    const detail = book.bookDetail;
                    $('#bookForm textarea[name="bookDetail.description"]').val(detail.description);
                    $('#bookForm textarea[name="bookDetail.summary"]').val(detail.summary);
                    $('#bookForm input[name="bookDetail.isbn"]').val(detail.isbn);
                    $('#bookForm input[name="bookDetail.publisher"]').val(detail.publisher);
                    $('#bookForm input[name="bookDetail.publicationDate"]').val(detail.publicationDate);
                    $('#bookForm input[name="bookDetail.pages"]').val(detail.pages);
                }
                
                // Show modal
                $('#bookModalLabel').text('Sửa sách');
                $('#bookModal').modal('show');
            });
        }

        // Hàm sửa sách
        function editBook(id) {
            $.ajax({
                url: '/admin/books/edit/' + id,
                type: 'GET',
                success: function(response) {
                    // Basic info
                    $('#editBookForm input[name="id"]').val(response.id);
                    $('#editBookForm input[name="title"]').val(response.title);
                    $('#editBookForm input[name="author"]').val(response.author);
                    $('#editBookForm input[name="price"]').val(response.price);
                    
                    // Category
                    if (response.subCategoryId) {
                        $('#editBookForm select[name="subCategory.id"]').val(response.subCategoryId);
                    }
                    
                    // Book details
                    $('#editBookForm textarea[name="bookDetail.description"]').val(response.description || '');
                    $('#editBookForm textarea[name="bookDetail.summary"]').val(response.summary || '');
                    $('#editBookForm input[name="bookDetail.isbn"]').val(response.isbn || '');
                    $('#editBookForm input[name="bookDetail.publisher"]').val(response.publisher || '');
                    $('#editBookForm input[name="bookDetail.pages"]').val(response.pages || '');
                    
                    // Handle date specifically
                    if (response.publicationDate) {
                        $('#editBookForm input[name="bookDetail.publicationDate"]').val(response.publicationDate);
                    }

                    // Show current cover image if exists
                    if (response.cover) {
                        const imgPreview = $('<img>')
                            .attr('src', '/image/cover/' + response.cover)
                            .addClass('cover-image mb-2 d-block');
                        $('#editBookForm .current-cover').html('Ảnh bìa hiện tại:').append(imgPreview);
                    }

                    // Show current PDF file name if exists
                    if (response.fileUrl) {
                        $('#editBookForm .current-file').html(
                            `File PDF hiện tại: <a href="/file/book/${response.fileUrl}" target="_blank">${response.fileUrl}</a>`
                        );
                    }

                    // Show modal
                    $('#editBookModal').modal('show');
                },
                error: function(xhr, status, error) {
                    console.error('Error:', error);
                    alert('Không thể tải thông tin sách!');
                }
            });
        }