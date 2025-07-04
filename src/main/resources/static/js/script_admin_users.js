function editUser(userId) {
    fetch(`/api/users/${userId}`)
        .then(response => response.json())
        .then(user => {
            document.getElementById('userId').value = user.id;
            document.getElementById('firstName').value = user.firstName;
            document.getElementById('lastName').value = user.lastName;
            document.getElementById('email').value = user.email;
            document.getElementById('username').value = user.username;
            document.getElementById('phoneNumber').value = user.phoneNumber;
            
            // Set roles
            const rolesSelect = document.getElementById('roles');
            Array.from(rolesSelect.options).forEach(option => {
                option.selected = user.roles.some(role => role.name === option.value);
            });

            // Show modal
            const userModal = new bootstrap.Modal(document.getElementById('userModal'));
            userModal.show();
        });
}

function saveUser() {
    const userId = document.getElementById('userId').value;
    const userData = {
        firstName: document.getElementById('firstName').value,
        lastName: document.getElementById('lastName').value,
        email: document.getElementById('email').value,
        username: document.getElementById('username').value,
        phoneNumber: document.getElementById('phoneNumber').value,
        roles: Array.from(document.getElementById('roles').selectedOptions).map(option => ({
            name: option.value
        }))
    };

    const method = userId ? 'PUT' : 'POST';
    const url = userId ? `/api/users/${userId}` : '/api/users';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(userData)
    })
    .then(response => {
        if (response.ok) {
            window.location.reload();
        } else {
            alert('Có lỗi xảy ra khi lưu người dùng');
        }
    });
}

function deleteUser(userId) {
    if (confirm('Bạn có chắc chắn muốn xóa người dùng này?')) {
        fetch(`/api/users/${userId}`, {
            method: 'DELETE'
        })
        .then(response => {
            if (response.ok) {
                window.location.reload();
            } else {
                alert('Có lỗi xảy ra khi xóa người dùng');
            }
        });
    }
}

// Sidebar toggle
document.getElementById('sidebarCollapse').addEventListener('click', function () {
    document.getElementById('sidebar').classList.toggle('active');
});
