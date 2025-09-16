import React, { useState, useEffect, useMemo } from 'react';
import KakaoMap from "../components/Meeting/KakaoMap.jsx";
import MeetingList from "../components/Meeting/MeetingList.jsx";
import { getAllMeetings } from '../services/meetingApi.js';
import styles from './MeetingListPage.module.scss';

const MeetingListPage = () => {
    const [meetings, setMeetings] = useState([]);
    const [selectedMartId, setSelectedMartId] = useState(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        const fetchMeetings = async () => {
            setIsLoading(true); // 데이터 요청 시작 시 로딩 상태로 설정
            try {
                const [_, result] = await Promise.all([
                    new Promise(resolve => setTimeout(resolve, 1500)),
                    getAllMeetings()
                ]);
                if (result.success) {
                    setMeetings(result.data);
                }
            } catch (error) {
                console.error("모임 목록을 불러오는 중 에러 발생:", error);
                // 에러 발생 시 빈 배열로 설정하여 "표시할 모임이 없습니다"가 나타나도록 함
                setMeetings([]);
            } finally {
                setIsLoading(false); // 요청 완료 시(성공/실패 모두) 로딩 상태 해제
            }
        };
        fetchMeetings();
    }, []);

    const handleMarkerClick = (martId) => {
        setSelectedMartId(prevId => prevId === martId ? null : martId);
    };

    const filteredMeetings = useMemo(() => {
        if (!selectedMartId) {
            return meetings;
        }
        return meetings.filter(meeting => meeting.martId === selectedMartId);
    }, [meetings, selectedMartId]);

    return (
        <div className={styles.container}>
            <p>모임 검색 창</p>
            <button>모임 생성</button>

            <KakaoMap onMarkerClick={handleMarkerClick} />

            {/*  isLoading 상태를 MeetingList에 prop으로 전달 */}
            <MeetingList meetings={filteredMeetings} isLoading={isLoading} />
        </div>
    );
};

export default MeetingListPage;