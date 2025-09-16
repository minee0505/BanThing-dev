import {createBrowserRouter} from "react-router-dom";
import AppLayout from "../layouts/AppLayout.jsx";
import ErrorPage from "../pages/ErrorPage.jsx";
import MeetingListPage from "../pages/MeetingListPage.jsx";
import MeetingDetailPage from "../pages/MeetingDetailPage.jsx";
import CommentPage from "../pages/CommentPage.jsx";
import LoginPage from "../pages/LoginPage.jsx";
import AgreementPage from "../pages/AgreementPage.jsx";

// 라우터 설정
export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppLayout />,
    errorElement: <ErrorPage />,
    children: [
      { index: true, element: <MeetingListPage /> },
      { path: 'meetings/:meetingId', element: <MeetingDetailPage /> },
      { path: 'meetings/:meetingId/comments', element: <CommentPage /> },
      { path: 'login', element: <LoginPage /> },
    ],
  },
  {
    path: '/agreement',
    element: <AgreementPage />
  }
]);

export default router;
