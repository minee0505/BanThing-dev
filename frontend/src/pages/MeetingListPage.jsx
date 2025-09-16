import React, { useState, useEffect, useMemo, useCallback, useRef } from 'react';
import KakaoMap from "../components/Meeting/KakaoMap.jsx";
import MeetingList from "../components/Meeting/MeetingList.jsx";
import { getAllMeetings } from '../services/meetingApi.js';
import styles from './MeetingListPage.module.scss';
import Chatbot from '../components/Chatbot/Chatbot.jsx';

const MeetingListPage = () => {
    const [meetings, setMeetings] = useState([]);
    const [selectedMartId, setSelectedMartId] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [skeletonCount, setSkeletonCount] = useState(3);
    // 최초 렌더링인지 확인하기 위한 ref. 이 값은 리렌더링 되어도 바뀌지 않습니다.
    const isInitialMount = useRef(true);

    // 1. 최초에 모임 데이터를 불러오는 useEffect
    useEffect(() => {
        const fetchMeetings = async () => {
            setIsLoading(true); // 로딩 시작
            try {
                const result = await getAllMeetings();
                if (result.success) {
                    setMeetings(result.data);
                }
            } catch (error) {
                console.error("모임 목록을 불러오는 중 에러 발생:", error);
                setMeetings([]);
            }
            // 여기서 setIsLoading(false)를 하지 않고, 아래의 useEffect가 처리하도록 둡니다.
        };
        fetchMeetings();
    }, []); // 이 useEffect는 맨 처음 한 번만 실행됩니다.

    // ✅ 2. 'selectedMartId'가 바뀔 때마다 로딩과 스켈레톤을 처리하는 전용 useEffect
    useEffect(() => {
        // 최초 렌더링 시에는, 위에서 데이터를 다 불러온 후에 로딩을 해제합니다.
        if (isInitialMount.current) {
            // 데이터 로딩이 끝난 후, 1초간 스켈레톤을 보여주고 로딩을 끕니다.
            setTimeout(() => {
                isInitialMount.current = false; // '최초 렌더링' 상태를 false로 변경
                setIsLoading(false);
            }, 1000);
            return; // 최초 렌더링 시에는 아래 로직을 실행하지 않음
        }

        // 핀 클릭 등으로 selectedMartId가 변경되었을 때 실행되는 로직
        setIsLoading(true);

        let count;
        if (selectedMartId) {
            count = meetings.filter(m => m.martId === selectedMartId).length;
        } else {
            // 선택이 해제되면 전체 목록 기준으로 계산 (최대 3개)
            count = Math.min(meetings.length, 3);
        }
        setSkeletonCount(Math.max(count, 1)); // 0개여도 최소 1개는 보여주도록

        // 짧은 시간 후에 로딩 상태를 해제하여 시각적 피드백을 줍니다.
        const timer = setTimeout(() => setIsLoading(false), 1000);

        // 컴포넌트가 언마운트되거나, selectedMartId가 다시 바뀌면 타이머를 정리합니다.
        return () => clearTimeout(timer);

    }, [selectedMartId, meetings]); // selectedMartId나 meetings 데이터가 바뀔 때마다 이 effect가 실행됩니다.


    // ✅ 3. 클릭 핸들러는 이제 ID 변경만 담당합니다. (훨씬 깔끔!)
    const handleMarkerClick = useCallback((martId) => {
        setSelectedMartId(prevId => (prevId === martId ? null : martId));
    }, []);

    // 필터링된 모임 목록 계산
    const filteredMeetings = useMemo(() => {
        if (isLoading) return []; // 로딩 중일 때는 빈 배열 반환
        if (!selectedMartId) return meetings;
        return meetings.filter(meeting => meeting.martId === selectedMartId);
    }, [meetings, selectedMartId, isLoading]);

    return (
        <div className={styles.container}>
            <p>모임 검색 창</p>
            <button>모임 생성</button>

            <KakaoMap onMarkerClick={handleMarkerClick} />

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