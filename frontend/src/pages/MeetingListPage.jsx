import React, { useState, useEffect, useMemo, useCallback, useRef } from 'react';
import KakaoMap from "../components/Meeting/KakaoMap.jsx";
import MeetingList from "../components/Meeting/MeetingList.jsx";
import { getAllMeetings } from '../services/meetingApi.js';
import styles from './MeetingListPage.module.scss';
import Chatbot from '../components/Chatbot/Chatbot.jsx';
import { FaSearch, FaPlus } from 'react-icons/fa';

const MeetingListPage = () => {
    const [meetings, setMeetings] = useState([]);
    const [selectedMartId, setSelectedMartId] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const [skeletonCount, setSkeletonCount] = useState(3);
    const isInitialMount = useRef(true);

    useEffect(() => {
        const fetchMeetings = async () => {
            setIsLoading(true);
            try {
                const result = await getAllMeetings();
                if (result.success) {
                    setMeetings(result.data);
                }
            } catch (error) {
                console.error("모임 목록을 불러오는 중 에러 발생:", error);
                setMeetings([]);
            }
        };
        fetchMeetings();
    }, []);

    useEffect(() => {
        if (isInitialMount.current) {
            setTimeout(() => {
                isInitialMount.current = false;
                setIsLoading(false);
            }, 1000);
            return;
        }

        setIsLoading(true);

        let count;
        if (selectedMartId) {
            count = meetings.filter(m => m.martId === selectedMartId).length;
        } else {
            count = Math.min(meetings.length, 3);
        }
        setSkeletonCount(Math.max(count, 1));

        const timer = setTimeout(() => setIsLoading(false), 1000);

        return () => clearTimeout(timer);

    }, [selectedMartId, meetings]);

    const handleMarkerClick = useCallback((martId) => {
        setSelectedMartId(prevId => (prevId === martId ? null : martId));
    }, []);

    const filteredMeetings = useMemo(() => {
        if (isLoading) return [];
        if (!selectedMartId) return meetings;
        return meetings.filter(meeting => meeting.martId === selectedMartId);
    }, [meetings, selectedMartId, isLoading]);

    return (
        <div className={styles.container}>

            <div className={styles.actionBar}>
                <div className={styles.searchBar}>
                    <FaSearch className={styles.searchIcon} />
                    <input type="text" placeholder="어떤 모임을 찾으시나요?" />
                </div>
                <button className={styles.createButton}>
                    <FaPlus /> 모임 생성
                </button>
            </div>

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