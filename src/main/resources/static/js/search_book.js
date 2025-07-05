const searchInput = document.getElementById('searchInput');
const searchResults = document.getElementById('searchResults');
let timeoutId;

searchInput.addEventListener('input', (e) => {
    clearTimeout(timeoutId);
    const searchTerm = e.target.value.trim();

    if (searchTerm.length < 2) {
        searchResults.classList.add('d-none');
        return;
    }

    // Debounce search
    timeoutId = setTimeout(() => {
        fetch(`/api/books/search?query=${encodeURIComponent(searchTerm)}`)
            .then(response => response.json())
            .then(books => {
                searchResults.classList.remove('d-none');
                const resultsHtml = books.map(book => `
                            <a href="${book.url}" class="list-group-item list-group-item-action">
                                <div class="d-flex align-items-center">
                                    <img src="/image/book/${book.cover ? book.cover : 'default.jpg'}" 
     alt="Book Cover" 
     class="me-3"
     style="width: 40px; height: 56px; object-fit: cover;">
                                    <div>
                                        <h6 class="mb-0">${book.title}</h6>
                                        <small class="text-muted">${book.price.toLocaleString('vi-VN')} VNĐ</small>
                                    </div>
                                </div>
                            </a>
                        `).join('');

                searchResults.querySelector('.list-group').innerHTML =
                    resultsHtml || '<div class="list-group-item">Không tìm thấy kết quả</div>';
            })
            .catch(error => {
                console.error('Error:', error);
                searchResults.querySelector('.list-group').innerHTML =
                    '<div class="list-group-item text-danger">Đã xảy ra lỗi khi tìm kiếm</div>';
            });
    }, 300);
});

// Thêm biến để kiểm tra trạng thái đăng nhập
const isAuthenticated = document.querySelector('[sec\\:authorize="isAuthenticated()"]') !== null;

// Ẩn dropdown khi click ra ngoài
document.addEventListener('click', (e) => {
    if (!searchInput.contains(e.target) && !searchResults.contains(e.target)) {
        searchResults.classList.add('d-none');
    }
});