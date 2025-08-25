document.addEventListener('DOMContentLoaded', () => {
    const deleteButton = document.getElementById('delete-button');

    if (deleteButton) {
        deleteButton.addEventListener('click', async () => {
            // Get user's authentication token from localStorage
            const userString = localStorage.getItem('user'); 

            if (!userString) {
                // alert 대신 모달 사용
                showNotificationModal('error', '세션 만료', '사용자 세션이 만료되었습니다. 다시 로그인해주세요.');
                setTimeout(() => {
                    closeNotificationModal();
                    window.location.href = 'login.html';
                }, 2000);
                return;
            }
            
            // Fix: Parse the JSON string to get the user object
            const user = JSON.parse(userString);
            const userEmail = user.email; // Now userEmail is correctly defined

            // confirm 대신 모달 사용 - 함수를 분리해서 처리
            showDeleteModalWithCallback(async () => {
                await performDelete(userEmail, deleteButton);
            });
        });
    } else {
        console.error("HTML에서 'delete-button' ID를 가진 요소를 찾을 수 없습니다.");
    }
});

// 실제 삭제 수행 함수 (기존 로직 그대로)
async function performDelete(userEmail, deleteButton) {
    const originalButtonText = deleteButton.innerHTML;
    deleteButton.disabled = true;
    deleteButton.innerHTML = `<i class="fas fa-spinner fa-spin"></i> 삭제 중...`;

    try {
        const response = await fetch('/BackEnd/DeleteService', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ email: userEmail })
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || `서버 응답 오류: ${response.status} ${response.statusText}`);
        }
        
        const result = await response.json();

        if (result.success) {
            // alert 대신 모달 사용
            showNotificationModal('success', '삭제 완료', result.message);
            setTimeout(() => {
                localStorage.clear();
                window.location.href = 'login.html';
            }, 2000);
        } else {
            // alert 대신 모달 사용
            showNotificationModal('error', '삭제 실패', result.message);
        }
    } catch (error) {
        console.error('계정 삭제 중 오류 발생:', error);
        // alert 대신 모달 사용
        showNotificationModal('error', '오류 발생', `계정 삭제 중 오류가 발생했습니다: ${error.message}`);
    } finally {
        deleteButton.disabled = false;
        deleteButton.innerHTML = originalButtonText;
    }
}

// 삭제 확인 모달 (콜백 포함)
function showDeleteModalWithCallback(callback) {
    const modal = document.getElementById('deleteModal');
    modal.classList.add('show');
    document.body.style.overflow = 'hidden';
    
    // 기존 이벤트 리스너 제거 후 새로 추가
    const confirmBtn = modal.querySelector('.modal-btn-danger');
    const newConfirmBtn = confirmBtn.cloneNode(true);
    confirmBtn.parentNode.replaceChild(newConfirmBtn, confirmBtn);
    
    newConfirmBtn.onclick = () => {
        closeDeleteModal();
        callback();
    };
}