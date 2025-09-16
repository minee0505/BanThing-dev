import React, { useState, useEffect, useMemo } from 'react';
import KakaoMap from "../components/Meeting/KakaoMap.jsx";
import MeetingList from "../components/Meeting/MeetingList.jsx";
import { getAllMeetings } from '../services/meetingApi.js';
import styles from './MeetingListPage.module.scss';

const MeetingListPage = () => {
    const [meetings, setMeetings] = useState([]); // 전체 모임 목록
    const [selectedMartId, setSelectedMartId] = useState(null); // 선택된 마트 ID

    // 1. 페이지 로딩 시 전체 모임 목록을 불러옵니다.
    useEffect(() => {
        const fetchMeetings = async () => {
            const result = await getAllMeetings();
            if (result.success) {
                setMeetings(result.data);
            }
        };
        fetchMeetings();
    }, []);

    // 2. 지도에서 마커가 클릭되면 이 함수가 호출됩니다.
    const handleMarkerClick = (martId) => {
        // 동일한 마커를 다시 클릭하면 선택 해제 (전체 목록 보기)
        setSelectedMartId(prevId => prevId === martId ? null : martId);
    };

    // 3. 선택된 마트 ID에 따라 보여줄 모임 목록을 필터링합니다.
    const filteredMeetings = useMemo(() => {
        if (!selectedMartId) {
            return meetings; // 선택된 마트가 없으면 전체 목록 반환
        }
        return meetings.filter(meeting => meeting.martId === selectedMartId);
    }, [meetings, selectedMartId]);

    return (
        <div className={styles.container}>
            <p>모임 검색 창</p>
            <button>모임 생성</button>

            {/* KakaoMap에게 마커 클릭 시 실행할 함수를 props로 전달 */}
            <KakaoMap onMarkerClick={handleMarkerClick} />

            {/* MeetingList에게는 필터링된 모임 목록을 props로 전달 */}
            <MeetingList meetings={filteredMeetings} />
        </div>
    );
};

export default MeetingListPage;