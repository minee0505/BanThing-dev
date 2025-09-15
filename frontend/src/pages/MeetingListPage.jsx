import React from 'react';
import KakaoMap from "../components/Meeting/KakaoMap.jsx";
import MeetingList from "../components/Meeting/MeetingList.jsx";
import AIChatBot from "../components/ChatBot/AIChatBot.jsx";

const MeetingListPage = () => {
    return (
        <>
            <p>메인화면(미팅 리스트 페이지)입니다.</p>
            <p>모임 검색 창</p>
            <button>모임 생성</button>
            <KakaoMap />
            <MeetingList />
            <AIChatBot />
        </>
    );
};

export default MeetingListPage;