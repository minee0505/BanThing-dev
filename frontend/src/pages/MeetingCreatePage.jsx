import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { getAllMarts } from '../services/martApi';
import { createMeeting } from '../services/createMeetingApi';
import styles from './MeetingCreatePage.module.scss';

const MeetingCreatePage = () => {
    const navigate = useNavigate();

    // 폼 입력 데이터 (이미지 URL 필드는 이제 필요 없습니다)
    const [formData, setFormData] = useState({
        martId: '',
        title: '',
        description: '',
        meetingDate: '',
    });

    // 업로드할 파일 객체를 저장하는 상태
    const [imageFile, setImageFile] = useState(null);

    const [marts, setMarts] = useState([]);
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchMarts = async () => {
            const result = await getAllMarts();
            if (result.success) {
                setMarts(result.data);
            } else {
                setError('마트 목록을 불러오는 데 실패했습니다.');
            }
        };
        fetchMarts();
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            setImageFile(file);
        }
    };

    // ▼▼▼ [수정] 이 부분이 핵심입니다 ▼▼▼
    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!formData.martId || !formData.title || !formData.description || !formData.meetingDate) {
            setError('모든 필수 항목을 입력해주세요.');
            return;
        }

        setIsLoading(true);
        setError('');

        // API로 보낼 때, 텍스트 데이터와 파일 데이터를 명확히 전달합니다.
        // imageSource 변수를 참조하는 부분을 완전히 삭제했습니다.
        const result = await createMeeting(formData, imageFile);

        setIsLoading(false);

        if (result.success) {
            alert('모임이 성공적으로 생성되었습니다!');
            navigate(`/meetings/${result.data.meetingId}`);
        } else {
            setError(result.message || '모임 생성 중 오류가 발생했습니다.');
        }
    };
    // ▲▲▲ [수정] 여기까지 ▲▲▲

    return (
        <div className={styles.container}>
            <div className={styles.card}>
                <h1 className={styles.title}>새로운 모임 만들기</h1>
                <p className={styles.subtitle}>
                    함께 장보고 나눌 새로운 모임을 만들어보세요!
                </p>

                <form onSubmit={handleSubmit} className={styles.form}>
                    <div className={styles.formGroup}>
                        <label htmlFor="title" className={styles.formLabel}>모임 제목</label>
                        <input type="text" id="title" name="title" className={styles.formInput} placeholder="예: 코스트코 베이글 소분해요" value={formData.title} onChange={handleChange} required />
                    </div>
                    <div className={styles.formRow}>
                        <div className={`${styles.formGroup} ${styles.formGroupHalf}`}>
                            <label htmlFor="martId" className={styles.formLabel}>모임 장소</label>
                            <select id="martId" name="martId" className={styles.formSelect} value={formData.martId} onChange={handleChange} required>
                                <option value="">마트를 선택하세요</option>
                                {marts.map(mart => (<option key={mart.martId} value={mart.martId}>{mart.martName}</option>))}
                            </select>
                        </div>
                        <div className={`${styles.formGroup} ${styles.formGroupHalf}`}>
                            <label htmlFor="meetingDate" className={styles.formLabel}>모임 시간</label>
                            <input type="datetime-local" id="meetingDate" name="meetingDate" className={styles.formInput} value={formData.meetingDate} onChange={handleChange} required />
                        </div>
                    </div>
                    <div className={styles.formGroup}>
                        <label htmlFor="description" className={styles.formLabel}>본문 내용</label>
                        <textarea id="description" name="description" className={styles.formTextarea} placeholder="모임에 대한 자세한 내용을 적어주세요. (예: 구매할 물품, 소분 방식, 만날 장소 등)" rows="8" value={formData.description} onChange={handleChange} required />
                    </div>

                    <div className={styles.formGroup}>
                        <label htmlFor="imageFile" className={styles.formLabel}>
                            썸네일 이미지 (선택)
                        </label>
                        <input
                            type="file"
                            id="imageFile"
                            name="imageFile"
                            className={styles.formInput}
                            accept="image/*"
                            onChange={handleFileChange}
                        />
                    </div>

                    {error && <p className={styles.errorMessage}>{error}</p>}

                    <div className={styles.buttonContainer}>
                        <button type="submit" className={styles.submitButton} disabled={isLoading}>
                            {isLoading ? '생성 중...' : '생성하기'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default MeetingCreatePage;