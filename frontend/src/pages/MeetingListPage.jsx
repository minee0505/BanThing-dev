import React, { useState, useEffect, useMemo, useCallback } from 'react';
import KakaoMap from "../components/Meeting/KakaoMap.jsx";
import MeetingList from "../components/Meeting/MeetingList.jsx";
import { searchMeetings } from '../services/meetingApi.js';
import styles from './MeetingListPage.module.scss';
import Chatbot from '../components/Chatbot/Chatbot.jsx';
import { FaSearch, FaPlus } from 'react-icons/fa';

const MeetingListPage = () => {
    const [meetings, setMeetings] = useState([]);
    const [selectedMartId, setSelectedMartId] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [skeletonCount, setSkeletonCount] = useState(3);
    const [searchTerm, setSearchTerm] = useState('');

    //  모든 로딩 및 데이터 패치 로직을 하나의 useEffect로 통합하고,
    // 의존성 배열에서 `meetings`를 제거하여 무한 루프를 방지합니다.
    useEffect(() => {
        const fetchMeetings = async () => {
            setIsLoading(true);

            // 디바운스 적용: 300ms 이후에 API 호출 시작
            const timer = setTimeout(async () => {
                try {
                    const result = await searchMeetings(searchTerm);
                    if (result.success) {
                        setMeetings(result.data);
                        // 필터링된 모임 개수에 따라 스켈레톤 카운트 설정
                        let count;
                        if (selectedMartId) {
                            count = result.data.filter(m => m.martId === selectedMartId).length;
                        } else {
                            count = Math.min(result.data.length, 3);
                        }
                        setSkeletonCount(Math.max(count, 1));
                    } else {
                        setMeetings([]);
                        setSkeletonCount(1);
                    }
                } catch (error) {
                    console.error("모임 목록을 불러오는 중 에러 발생:", error);
                    setMeetings([]);
                    setSkeletonCount(1);
                } finally {
                    setIsLoading(false);
                }
            }, 300);

            return () => clearTimeout(timer); // cleanup 함수
        };
        const timer = setTimeout(() => {
        fetchMeetings();
        }, 300);

        return () => clearTimeout(timer);

    }, [searchTerm, selectedMartId]); //  수정: meetings를 제거하고, searchTerm과 selectedMartId만 남깁니다.

    const handleSearchChange = (e) => {
        setSearchTerm(e.target.value);
    };

    const handleMarkerClick = useCallback((martId) => {
        // 마커 클릭 시 selectedMartId를 업데이트합니다.
        // 이 상태 변경이 위 useEffect를 다시 실행시키고, 지도가 다시 그려집니다.
        setSelectedMartId(prevId => (prevId === martId ? null : martId));
    }, []);

    const filteredMeetings = useMemo(() => {
        if (!selectedMartId) return meetings;
        return meetings.filter(meeting => meeting.martId === selectedMartId);
    }, [meetings, selectedMartId]);

    return (
        <div className={styles.container}>
            <div className={styles.actionBar}>
                <div className={styles.searchBar}>
                    <FaSearch className={styles.searchIcon} />
                    <input
                        type="text"
                        placeholder="어떤 모임을 찾으시나요?"
                        value={searchTerm}
                        onChange={handleSearchChange}
                    />
                </div>
                <button className={styles.createButton}>
                    <FaPlus /> 모임 생성
                </button>
            </div>

            <KakaoMap meetings={meetings} onMarkerClick={handleMarkerClick} />

            <MeetingList
                meetings={filteredMeetings}
                isLoading={isLoading}
                skeletonCount={skeletonCount}
            />
            <Chatbot />
        </div>
    );
};

export default MeetingListPage;